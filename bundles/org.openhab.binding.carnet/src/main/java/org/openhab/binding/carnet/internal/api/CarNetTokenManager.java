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
package org.openhab.binding.carnet.internal.api;

import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;
import static org.openhab.binding.carnet.internal.api.CarNetHttpClient.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.CarNetSecurityException;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNApiToken;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetSecurityPinAuthInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetSecurityPinAuthentication;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link CarNetTokenManager} implements token creation and refreshing.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = CarNetTokenManager.class)
public class CarNetTokenManager implements CarNetBrandAuthenticator {
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private final Logger logger = LoggerFactory.getLogger(CarNetTokenManager.class);
    private final Gson gson = new Gson();
    private @Nullable CarNetBrandAuthenticator authenticator;
    private Map<String, TokenSet> accountTokens = new HashMap<>();
    private CarNetHttpClient http = new CarNetHttpClient();
    private CopyOnWriteArrayList<CarNetToken> securityTokens = new CopyOnWriteArrayList<CarNetToken>();

    private class TokenSet {
        private CarNetToken idToken = new CarNetToken();
        private CarNetToken vwToken = new CarNetToken();
        private String csrf = "";
    }

    public void setup(CarNetHttpClient httpClient, CarNetBrandAuthenticator authenticator) {
        this.http = httpClient;
        this.authenticator = authenticator;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws CarNetException {
        if (authenticator != null) {
            return authenticator.updateAuthorizationUrl(url);
        }
        throw new CarNetException("No authenticator active");
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
    public String createVwToken(CarNetCombinedConfig config) throws CarNetException {
        TokenSet tokens = getTokenSet(config.tokenSetId);
        if (!tokens.vwToken.isExpired()) {
            // Token is still valid
            return tokens.vwToken.accessToken;
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
        String url = "", html = "", csrf = "";
        String idToken = "", accessToken = "", expiresIn = "";
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> data = new LinkedHashMap<>();
        try {
            logger.debug("{}: Logging in, account={}", config.vehicle.vin, config.account.user);
            headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0");
            headers.put(HttpHeader.ACCEPT.toString(),
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            headers.put(HttpHeader.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded");
            headers.put("x-requested-with", config.api.xrequest);
            headers.put("upgrade-insecure-requests", "1");

            logger.debug("{}: OAuth: Get signin form", config.vehicle.vin);
            String state = UUID.randomUUID().toString();
            String nonce = generateNonce();
            url = CNAPI_OAUTH_AUTHORIZE_URL + "?response_type=" + urlEncode(config.api.responseType) + "&client_id="
                    + urlEncode(config.api.clientId) + "&redirect_uri=" + urlEncode(config.api.redirect_uri) + "&scope="
                    + urlEncode(config.api.authScope) + "&state=" + state + "&nonce=" + nonce;
            url = updateAuthorizationUrl(url);
            http.get(url, headers, false);
            url = http.getRedirect(); // Signin URL
            if (url.isEmpty()) {
                throw new CarNetException("Unable to get signin URL");
            }
            if (url.contains("error=consent_require")) {
                String message = URLDecoder.decode(url, UTF_8);
                message = substringBefore(substringAfter(message, "&error_description="), "&");
                throw new CarNetSecurityException(
                        "Login failed, Consent missing. Login to the Web App and give consent: " + message);
            }
            html = http.get(url, headers, false);
            url = http.getRedirect();
            csrf = substringBetween(html, "name=\"_csrf\" value=\"", "\"/>");
            String relayState = substringBetween(html, "name=\"relayState\" value=\"", "\"/>");
            String hmac = substringBetween(html, "name=\"hmac\" value=\"", "\"/>");

            // Authenticate: Username
            logger.trace("{}: OAuth input: User", config.vehicle.vin);
            url = CNAPI_OAUTH_BASE_URL + "/signin-service/v1/" + config.api.clientId + "/login/identifier";
            data.put("_csrf", csrf);
            data.put("relayState", relayState);
            data.put("hmac", hmac);
            data.put("email", URLEncoder.encode(config.account.user, UTF_8));
            http.post(url, headers, data, false);

            // Authenticate: Password
            logger.trace("{}: OAuth input: Password", config.vehicle.vin);
            url = CNAPI_OAUTH_BASE_URL + http.getRedirect(); // Signin URL
            html = http.get(url, headers, false);
            csrf = substringBetween(html, "name=\"_csrf\" value=\"", "\"/>");
            relayState = substringBetween(html, "name=\"relayState\" value=\"", "\"/>");
            hmac = substringBetween(html, "name=\"hmac\" value=\"", "\"/>");

            logger.trace("{}: OAuth input: Authenticate", config.vehicle.vin);
            url = CNAPI_OAUTH_BASE_URL + "/signin-service/v1/" + config.api.clientId + "/login/authenticate";
            data.clear();
            data.put("_csrf", csrf);
            data.put("relayState", relayState);
            data.put("hmac", hmac);
            data.put("email", URLEncoder.encode(config.account.user, UTF_8));
            data.put("password", URLEncoder.encode(config.account.password, UTF_8));
            html = http.post(url, headers, data, false, false);
            url = http.getRedirect(); // Continue URL
            if (url.contains("error=login.error.throttled")) {
                throw new CarNetSecurityException(
                        "Login failed due to invalid password, locked account or API throtteling!");
            }

            // Now we need to follow multiple redirects, required data is fetched from the redirect URLs
            // String userId = "";
            int count = 10;
            while (count-- > 0) {
                html = http.get(url, headers, false);
                url = http.getRedirect(); // Continue URL
                // if (url.contains("&user_id=")) {
                // userId = getUrlParm(url, "user_id");
                // }
                /*
                 * if (url.contains("&code=")) {
                 * authCode = getUrlParm(url, "code");
                 * break;
                 * }
                 */
                if (url.contains("&id_token=")) {
                    idToken = getUrlParm(url, "id_token");
                }
                if (url.contains("&expires_in=")) {
                    expiresIn = getUrlParm(url, "expires_in");
                }
                if (url.contains("&access_token=")) {
                    accessToken = getUrlParm(url, "access_token");
                    break; // that's what we are looking for
                }

                if (url.contains(config.api.redirect_uri)) {
                    break;
                }
            }
        } catch (CarNetException | UnsupportedEncodingException e) {
            throw new CarNetSecurityException("Login failed", e);
        }

        try {
            CNApiToken token;
            String json = "";
            if (!idToken.isEmpty()) {
                // In this case the id and access token were returned by the login process
                logger.trace("{}: OAuth successful, idToken and accessToken retrieved", config.vehicle.vin);
                tokens.idToken = new CarNetToken(idToken, accessToken, "bearer", Integer.parseInt(expiresIn, 10));
            }
            logger.debug("{}: OAuth successful", config.vehicle.vin);
            tokens.csrf = csrf;

            // Last step: Request the access token from the VW token management
            // We save the generated tokens as tokenSet. Account handler and vehicle handler(s) are sharing the same
            // tokens. The tokenSetId provides access to that set.
            logger.debug("{}: Get VW Token", config.vehicle.vin);
            headers.clear();
            headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0");
            headers.put(CNAPI_HEADER_APP, config.api.xappName);
            headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
            headers.put(CNAPI_HEADER_CLIENTID, config.api.xClientId);
            headers.put(CNAPI_HEADER_HOST, "mbboauth-1d.prd.ece.vwg-connect.com");
            headers.put(HttpHeader.ACCEPT.toString(), "*/*");
            data.clear();
            data.put("grant_type", "id_token");
            data.put("token", tokens.idToken.idToken);
            data.put("scope", "sc2:fal");
            json = http.post(CNAPI_URL_GET_SEC_TOKEN, headers, data, false);
            token = fromJson(gson, json, CNApiToken.class);
            if ((token.accessToken == null) || token.accessToken.isEmpty()) {
                throw new CarNetSecurityException("Authentication failed: Unable to get access token!");
            }
            tokens.vwToken = new CarNetToken(token);
            updateTokenSet(config.tokenSetId, tokens);
            return tokens.vwToken.accessToken;
        } catch (CarNetException e) {
            throw new CarNetSecurityException("Unable to create API access token", e);
        }
    }

    /**
     * Create security token required for priviledged functions like lock/unlock.
     *
     * @param config The combined config (account+vehicle)
     * @param service Service requesting this access level
     * @param action Action to be performed
     * @return Security Token
     * @throws CarNetException
     */
    public String createSecurityToken(CarNetCombinedConfig config, String service, String action)
            throws CarNetException {
        if (config.vehicle.pin.isEmpty()) {
            throw new CarNetException("No SPIN is confirgured, can't perform authentication");
        }

        // First check for a valid token
        Iterator<CarNetToken> it = securityTokens.iterator();
        while (it.hasNext()) {
            CarNetToken stoken = it.next();
            if (stoken.service.equals(service) && stoken.isValid()) {
                return stoken.securityToken;
            }
        }

        /*
         * 1. Security token is based on access token. We use the cashed token if still valid or request a new one.
         * 2. Send a challenge to the token manager
         * 3. Send authentication and request token, save token to the cache
         */
        String accessToken = createVwToken(config);

        // "User-Agent": "okhttp/3.7.0",
        // "X-App-Version": "3.14.0",
        // "X-App-Name": "myAudi",
        // "Accept": "application/json",
        // "Authorization": "Bearer " + self.vwToken.get("access_token"),
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0");
        headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
        headers.put(CNAPI_HEADER_APP, config.api.xappName);
        headers.put(HttpHeader.ACCEPT.toString(), CNAPI_ACCEPTT_JSON);
        // Build Hash: SHA512(SPIN+Challenge)
        String url = "https://mal-1a.prd.ece.vwg-connect.com/api/rolesrights/authorization/v2/vehicles/"
                + config.vehicle.vin.toUpperCase() + "/services/" + service + "/operations/" + action
                + "/security-pin-auth-requested";
        String json = http.get(url, headers, accessToken);
        CarNetSecurityPinAuthInfo authInfo = fromJson(gson, json, CarNetSecurityPinAuthInfo.class);
        String pinHash = sha512(config.vehicle.pin, authInfo.securityPinAuthInfo.securityPinTransmission.challenge)
                .toUpperCase();
        logger.debug("Authenticating SPIN, retires={}",
                authInfo.securityPinAuthInfo.securityPinTransmission.remainingTries);

        // Request authentication
        CarNetSecurityPinAuthentication pinAuth = new CarNetSecurityPinAuthentication();
        pinAuth.securityPinAuthentication.securityToken = authInfo.securityPinAuthInfo.securityToken;
        pinAuth.securityPinAuthentication.securityPin.challenge = authInfo.securityPinAuthInfo.securityPinTransmission.challenge;
        pinAuth.securityPinAuthentication.securityPin.securityPinHash = pinHash;
        String data = gson.toJson(pinAuth);
        json = http.post(
                "https://mal-1a.prd.ece.vwg-connect.com/api/rolesrights/authorization/v2/security-pin-auth-completed",
                headers, data);
        CNApiToken t = fromJson(gson, json, CNApiToken.class);
        CarNetToken securityToken = new CarNetToken(t);
        if (securityToken.securityToken.isEmpty()) {
            throw new CarNetSecurityException("Authentication failed: Unable to get access token!");
        }
        logger.debug("securityToken granted successful!");
        synchronized (securityTokens) {
            securityToken.setService(service);
            if (securityTokens.contains(securityToken)) {
                securityTokens.remove(securityToken);
            }
            securityTokens.add(securityToken);
        }
        return securityToken.securityToken;
    }

    public String getCsrfToken(String tokenSetId) {
        TokenSet tokens = getTokenSet(tokenSetId);
        return tokens.csrf;
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
     * @throws CarNetException
     */
    public boolean refreshTokens(CarNetCombinedConfig config) throws CarNetException {
        try {
            TokenSet tokens = getTokenSet(config.tokenSetId);
            refreshToken(config, tokens.vwToken);

            Iterator<CarNetToken> it = securityTokens.iterator();
            while (it.hasNext()) {
                CarNetToken stoken = it.next();
                if (!refreshToken(config, stoken)) {
                    // Token invalid / refresh failed -> remove
                    securityTokens.remove(stoken);
                }
            }
        } catch (CarNetException e) {
            // Ignore problems with the idToken or securityToken if the accessToken was requested successful
            logger.debug("Unable to create secondary token: {}", e.toString()); // "normal, no stack trace"
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid token!");
        }
        return false;
    }

    /**
     * Refresh the access token
     *
     * @param config Combined account/vehicle config
     * @param token Token to refresh
     * @return new token
     * @throws CarNetException
     */
    public boolean refreshToken(CarNetCombinedConfig config, CarNetToken token) throws CarNetException {
        if (!token.isValid()) {
            // token is still valid
            return false;
        }

        TokenSet tokens = getTokenSet(config.tokenSetId);
        if (token.isExpired()) {
            logger.debug("{}: Refreshing Token {}", config.vehicle.vin, token);
            try {
                String url = "";
                Map<String, String> data = new TreeMap<>();
                url = CNAPI_VW_TOKEN_URL;
                data.put("grant_type", "refresh_token");
                data.put("refresh_token", tokens.vwToken.refreshToken);
                data.put("scope", "sc2%3Afal");
                String json = http.post(url, http.fillRefreshHeaders(), data, false);
                CNApiToken newToken = gson.fromJson(json, CNApiToken.class);
                if (newToken == null) {
                    throw new CarNetSecurityException("Unable to parse token information from JSON");
                }
                tokens.vwToken = new CarNetToken(newToken);
                updateTokenSet(config.tokenSetId, tokens);
                logger.debug("{}: Token refresh successful", config.vehicle.vin);
                return true;
            } catch (CarNetException e) {
                logger.debug("{}: Unable to refresh token: {}", config.vehicle.vin, e.toString());
                // Invalidate token
                if (token.isExpired()) {
                    tokens.vwToken.invalidate();
                    updateTokenSet(config.tokenSetId, tokens);
                }
            }
        }

        return false;
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
