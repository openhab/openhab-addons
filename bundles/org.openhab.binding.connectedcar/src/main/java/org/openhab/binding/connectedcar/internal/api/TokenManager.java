/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiToken.OAuthToken;
import org.openhab.binding.connectedcar.internal.api.ApiToken.TokenSet;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetSecurityPinAuthInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetSecurityPinAuthentication;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link TokenManager} implements token creation and refreshing.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = TokenManager.class)
public class TokenManager {
    private final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    private final Gson gson = new Gson();
    private Map<String, TokenSet> accountTokens = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<ApiToken> securityTokens = new CopyOnWriteArrayList<ApiToken>();

    public void setup(String tokenSetId, ApiHttpClient httpClient) {
        TokenSet tokens = getTokenSet(tokenSetId);
        tokens.http = httpClient;
    }

    /**
     * Generate a unique Id for all tokens related to the Account thing, but also for all depending Vehicle things. This
     * allows sharing the tokens across all things associated with the account.
     *
     * @return unique group Id
     */
    public String generateTokenSetId() {
        String id = UUID.randomUUID().toString();
        createTokenSet(id);
        return id;
    }

    /**
     * Create the API access token
     */
    public String createAccessToken(CombinedConfig config) throws ApiException {
        TokenSet tokens = getTokenSet(config.tokenSetId);
        if (tokens.apiToken.isValid() && !tokens.apiToken.isExpired()) {
            // Token is still valid
            return tokens.apiToken.accessToken;
        }

        // First try to refresh token
        if (!tokens.apiToken.refreshToken.isEmpty() && refreshToken(config, tokens, tokens.apiToken)) {
            // successful, return new token, otherwise re-login
            return tokens.apiToken.accessToken;
        }

        BrandAuthenticator authenticator = config.authenticator;
        if (authenticator == null) {
            throw new ApiSecurityException("No authenticator available");
        }

        /*
         * Authentication is performed as follows
         * 1. OAuth based login is performed given the account credentials. This results in the so-called Id Token
         * 2. The identity token is used to create an access token. In beetween Audi accepts tokens created by the VW
         * token management.
         * 3. The process also returns a refresh token, which could be used to refresh the access token before it
         * expires. This avoids sending the credentials again and again.
         * The token service also provides the option to revoke a token, this is not used. Token stays until unless the
         * refresh fails.
         */
        logger.debug("{}: Logging in, account={}", config.getLogId(), config.account.user);
        TokenOAuthFlow oauth = new TokenOAuthFlow(tokens.http);
        String url = authenticator.getLoginUrl(oauth);
        tokens.idToken = authenticator.login(url, oauth);

        logger.debug("{}: Login successful, grating API access", config.getLogId());
        tokens.apiToken = authenticator.grantAccess(oauth);
        logger.debug("{}: accessToken was created, valid for {}sec", config.getLogId(), tokens.apiToken.validity);
        if (tokens.apiToken.accessToken.isEmpty() && tokens.idToken.idToken.isEmpty()) {
            throw new ApiSecurityException("Authentication failed: Unable to get access token!");
        }

        tokens.apiToken.xcsrf = oauth.csrf;
        updateTokenSet(config.tokenSetId, tokens);
        return tokens.apiToken.accessToken;
    }

    public String createXCSRF(CombinedConfig config) throws ApiException {
        createAccessToken(config);
        TokenSet tokens = getTokenSet(config.tokenSetId);
        return tokens.apiToken.xcsrf;
    }

    public String createIdToken(CombinedConfig config) throws ApiException {
        TokenSet tokens = getTokenSet(config.tokenSetId);
        if (!tokens.idToken.isValid() || tokens.idToken.isExpired()) {
            // Token got invalid, force recreation
            logger.debug("{}: idToken experied, re-login", config.getLogId());
            tokens.apiToken.invalidate();
            createAccessToken(config);
        }
        return tokens.idToken.idToken;
    }

    public String createProfileToken(CombinedConfig config) throws ApiException {
        TokenSet tokens = getTokenSet(config.tokenSetId);
        createIdToken(config);
        return !tokens.idToken.accessToken.isEmpty() ? tokens.idToken.accessToken : tokens.idToken.idToken;
    }

    /**
     * Create security token required for priviledged functions like lock/unlock.
     *
     * @param config The combined config (account+vehicle)
     * @param service Service requesting this access level
     * @param action Action to be performed
     * @return Security Token
     * @throws ApiException
     */
    public String createSecurityToken(CombinedConfig config, String service, String action) throws ApiException {
        if (config.vehicle.pin.isEmpty()) {
            throw new ApiSecurityException("No SPIN is confirgured, can't perform authentication");
        }

        // First check for a valid token
        Iterator<ApiToken> it = securityTokens.iterator();
        while (it.hasNext()) {
            ApiToken stoken = it.next();
            if (stoken.service.equals(service) && stoken.isValid()) {
                // return stoken.securityToken;
            }
        }

        /*
         * 1. Security token is based on access token. We use the cashed token if still valid or request a new one.
         * 2. Send a challenge to the token manager
         * 3. Send authentication and request token, save token to the cache
         */
        TokenSet tokens = getTokenSet(config.tokenSetId);
        ApiHttpClient http = tokens.http;

        String accessToken = createAccessToken(config);

        // "User-Agent": "okhttp/3.7.0",
        // "X-App-Version": "3.14.0",
        // "X-App-Name": "myAudi",
        // "Accept": "application/json",
        // "Authorization": "Bearer " + self.vwToken.get("access_token"),
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
        headers.put(CNAPI_HEADER_APP, config.api.xappName);
        headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON);
        // Build Hash: SHA512(SPIN+Challenge)
        // https://mal-3a.prd.eu.dp.vwg-connect.com/api/rolesrights/operationlist/v3/vehicles/WAUZZZGE0MB027113

        // "https://mal-3a.prd.eu.dp.vwg-connect.com/api/rolesrights/authorization/v2/vehicles/"
        String url = config.vstatus.rolesRightsUrl + "/rolesrights/authorization/v2/vehicles/"
                + config.vehicle.vin.toUpperCase() + "/services/" + service + "/operations/" + action
                + "/security-pin-auth-requested";
        String json = http.get(url, headers, accessToken).response;
        CarNetSecurityPinAuthInfo authInfo = fromJson(gson, json, CarNetSecurityPinAuthInfo.class);
        String pinHash = sha512(config.vehicle.pin, authInfo.securityPinAuthInfo.securityPinTransmission.challenge)
                .toUpperCase();
        logger.debug("{}: Authenticating SPIN, retires={}", config.getLogId(),
                authInfo.securityPinAuthInfo.remainingTries);

        // Request authentication
        CarNetSecurityPinAuthentication pinAuth = new CarNetSecurityPinAuthentication();
        pinAuth.securityPinAuthentication.securityToken = authInfo.securityPinAuthInfo.securityToken;
        pinAuth.securityPinAuthentication.securityPin.challenge = authInfo.securityPinAuthInfo.securityPinTransmission.challenge;
        pinAuth.securityPinAuthentication.securityPin.securityPinHash = pinHash;
        // "https://mal-3a.prd.ece.vwg-connect.com/api/rolesrights/authorization/v2/security-pin-auth-completed",
        String data = gson.toJson(pinAuth);
        json = http.post(config.vstatus.rolesRightsUrl + "/rolesrights/authorization/v2/security-pin-auth-completed",
                headers, data).response;
        OAuthToken t = fromJson(gson, json, OAuthToken.class);
        ApiToken securityToken = new ApiToken(t);
        if (securityToken.securityToken.isEmpty()) {
            throw new ApiSecurityException("Authentication failed: Unable to get access token!");
        }
        logger.debug("{}: securityToken granted successful!", config.getLogId());
        synchronized (securityTokens) {
            securityToken.setService(service);
            if (securityTokens.contains(securityToken)) {
                securityTokens.remove(securityToken);
            }
            securityTokens.add(securityToken);
        }
        return securityToken.securityToken;
    }

    public boolean isAccessTokenValid(CombinedConfig config) {
        try {
            TokenSet tokens = getTokenSet(config.tokenSetId);
            return tokens.apiToken.isValid();
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Invalid token!", config.getLogId());
            return false;
        }
    }

    /**
     *
     * Request/refreh the different tokens
     * accessToken, which is required to access the API
     * idToken, which is required to request the securityToken and
     * securityToken, which is required to perform control functions
     *
     * The validity is checked and if token is not expired it will be reused.
     *
     * @throws ApiException
     */
    public boolean refreshTokens(CombinedConfig config) throws ApiException {
        try {
            TokenSet tokens = getTokenSet(config.tokenSetId);
            refreshToken(config, tokens, tokens.apiToken);
            updateTokenSet(config.tokenSetId, tokens);

            Iterator<ApiToken> it = securityTokens.iterator();
            while (it.hasNext()) {
                ApiToken stoken = it.next();
                if (!refreshToken(config, tokens, stoken)) {
                    // Token invalid / refresh failed -> remove
                    logger.debug("{}: Security token for service {} expired, remove", config.getLogId(),
                            stoken.service);
                    securityTokens.remove(stoken);
                }
            }
            return true;
        } catch (ApiException e) {
            // Ignore problems with the idToken or securityToken if the accessToken was requested successful
            logger.debug("{}: Unable to refresh token: {}", config.getLogId(), e.toString()); // "normal, no stack
                                                                                              // trace"
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid tokenSet!");
        }
        return false;
    }

    /**
     * Refresh the access token
     *
     * @param config Combined account/vehicle config
     * @param token Token to refresh
     * @return new token
     * @throws ApiException
     */
    public boolean refreshToken(CombinedConfig config, TokenSet tokenSet, ApiToken token) throws ApiException {
        if (!token.isValid()) {
            return false;
        }

        if (tokenSet.apiToken.refreshToken.isEmpty()) {
            logger.debug("{}: No refreshToken available, token is now invalid", config.getLogId());
            token.invalidate();
            return false;
        }

        if (token.isExpired()) {
            try {
                logger.debug("{}: Refreshing Token {}", config.getLogId(), token.accessToken);
                BrandAuthenticator authenticator = config.authenticator;
                if (authenticator == null) {
                    throw new ApiSecurityException("No authenticator available");
                }

                // OAuthToken newToken = authenticator.refreshToken(token).normalize();
                OAuthToken newToken = authenticator.refreshToken(token);
                // tokens.apiToken.accessToken = newToken.accessToken;
                // tokens.apiToken.setValidity(getInteger(newToken.validity));
                logger.debug("{}: Token refresh successful, valid for {} sec", config.getLogId(), newToken.validity);
                logger.trace("{}: new token={}", config.getLogId(), newToken.accessToken);
                token.updateToken(newToken.normalize());
            } catch (ApiException e) {
                logger.debug("{}: Unable to refresh token: {}", config.getLogId(), e.toString());
                // Invalidate token (triggers a new login when accessToken is required)
                if (token.isExpired()) {
                    token.invalidate();
                    logger.debug("{}: Token refresh failed and current accessToken is expired", config.getLogId());
                    return false;
                } else {
                    logger.debug("{}: Token refresh failed, but accessToken is still valid", config.getLogId());
                }
            }
        }
        return true;
    }

    /**
     * Create new tokenSet identified by tokenSetId
     *
     * @param tokenSetId UUID to manage the token set
     * @return successful yes/no
     */
    public boolean createTokenSet(String tokenSetId) {
        if (!accountTokens.containsKey(tokenSetId)) {
            accountTokens.put(tokenSetId, new TokenSet());
            return true;
        }
        return false;
    }

    TokenSet getTokenSet(String tokenSetId) {
        if (tokenSetId.isEmpty()) {
            int i = 1;
            return new TokenSet();
        }
        TokenSet ts = accountTokens.get(tokenSetId);
        if (ts != null) {
            return ts;
        }
        throw new IllegalArgumentException("tokenSetId is invalid");
    }

    synchronized void updateTokenSet(String tokenSetId, TokenSet tokens) {
        if (accountTokens.containsKey(tokenSetId)) {
            accountTokens.remove(tokenSetId);
        }
        accountTokens.put(tokenSetId, tokens);
    }
}
