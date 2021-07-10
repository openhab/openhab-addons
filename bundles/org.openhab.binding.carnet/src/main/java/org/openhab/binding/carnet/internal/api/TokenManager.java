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

import static org.openhab.binding.carnet.internal.BindingConstants.*;
import static org.openhab.binding.carnet.internal.CarUtils.*;
import static org.openhab.binding.carnet.internal.api.ApiHttpClient.urlEncode;
import static org.openhab.binding.carnet.internal.api.carnet.CarNetApiConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CNApiToken;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CarNetSecurityPinAuthInfo;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CarNetSecurityPinAuthentication;
import org.openhab.binding.carnet.internal.config.CombinedConfig;
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
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    private final Gson gson = new Gson();
    private Map<String, TokenSet> accountTokens = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<ApiToken> securityTokens = new CopyOnWriteArrayList<ApiToken>();

    private class TokenSet {
        private ApiToken apiToken = new ApiToken();
        private ApiToken idToken = new ApiToken();
        private String csrf = "";
        private ApiHttpClient http = new ApiHttpClient();
    }

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
        if (!tokens.apiToken.refreshToken.isEmpty() && refreshToken(config, tokens.apiToken)) {
            // successful, return new token, otherwise re-login
            return tokens.apiToken.accessToken;
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
        String url = "";
        String state = UUID.randomUUID().toString();
        String nonce = generateNonce();
        ApiResult res = new ApiResult();

        TokenOAuthFlow oauth = new TokenOAuthFlow(tokens.http);
        try {
            logger.debug("{}: Logging in, account={}", config.vehicle.vin, config.account.user);
            String authUrl = "";
            if (CNAPI_BRAND_VWID.equals(config.api.brand)) {
                res = oauth.get(config.api.loginUrl);
                authUrl = res.response;
                url = res.getLocation();
            } else {
                authUrl = config.api.issuerRegionMappingUrl + "/oidc/v1/authorize";
                oauth.header(HttpHeader.USER_AGENT, CNAPI_HEADER_USER_AGENT).header(HttpHeader.ACCEPT,
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                        .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .header("x-requested-with", config.api.xrequest).header("upgrade-insecure-requests", "1");
                url = authUrl + "?client_id=" + urlEncode(config.api.clientId) + "&scope="
                        + urlEncode(config.api.authScope).replace("%20", "+") + "&response_type="
                        + urlEncode(config.api.responseType).replace("%20", "+") + "&redirect_uri="
                        + urlEncode(config.api.redirect_uri) + "&state=" + state + "&nonce=" + nonce;
                if (config.authenticator != null) {
                    url = config.authenticator.updateAuthorizationUrl(url);
                }
                url = oauth.addCodeChallenge(url);
            }

            logger.debug("{}: OAuth: Get signin form", config.vehicle.vin);
            res = oauth.get(url);
            url = oauth.location;
            if (url.contains("error=consent_required")) {
                logger.debug("Missing consent: {}", url);
                String message = URLDecoder.decode(url, UTF_8);
                message = substringBefore(substringAfter(message, "&error_description="), "&");
                throw new ApiSecurityException(
                        "Login failed, Consent missing. Login to the Web App and give consent: " + message);
            }
            res = oauth.follow();
            if (oauth.csrf.isEmpty() || oauth.relayState.isEmpty() || oauth.hmac.isEmpty()) {
                logger.debug("{}: OAuth failed, can't get parameters\nHTML {}: {}", config.vehicle.vin, res.httpCode,
                        res.response);
                throw new ApiSecurityException("Unable to login - can't get OAuth parameters!");
            }

            // Authenticate: Username
            logger.trace("{}: OAuth input: User", config.vehicle.vin);
            // "/signin-service/v1/" + config.api.clientId + "/login/identifier";
            url = config.api.issuerRegionMappingUrl + oauth.action;
            oauth.clearData().data("_csrf", oauth.csrf).data("relayState", oauth.relayState).data("hmac", oauth.hmac)
                    .data("email", URLEncoder.encode(config.account.user, UTF_8));
            oauth.post(url);
            if (oauth.location.isEmpty()) {
                logger.debug("{}: OAuth failed, can't input password - HTML {}: {}", config.vehicle.vin, res.httpCode,
                        res.response);
                throw new ApiSecurityException("Unable to login - can't get OAuth parameters!");
            }

            // Authenticate: Password
            logger.trace("{}: OAuth input: Password", config.vehicle.vin);
            url = config.api.issuerRegionMappingUrl + oauth.location; // Signin URL
            res = oauth.get(url);

            logger.trace("{}: OAuth input: Authenticate", config.vehicle.vin);
            // "/signin-service/v1/" + config.api.clientId + "/login/authenticate";
            url = config.api.issuerRegionMappingUrl + oauth.action;
            oauth.clearData().data("_csrf", oauth.csrf).data("relayState", oauth.relayState).data("hmac", oauth.hmac)
                    .data("email", URLEncoder.encode(config.account.user, UTF_8))
                    .data("password", URLEncoder.encode(config.account.password, UTF_8));
            res = oauth.post(url);
            url = oauth.location; // Continue URL
            if (url.contains("error=login.error.password_invalid")) {
                throw new ApiSecurityException("Login failed due to invalid password or locked account!");
            }
            if (url.contains("error=login.error.throttled")) {
                throw new ApiSecurityException(
                        "Login failed due to invalid password, locked account or API throtteling!");
            }
            if (url.contains("&updated=dataprivacy")) {
                throw new ApiSecurityException(
                        "Login failed: New Terms&Conditions/Data Privacy Policy has to be accepted, login to Web portal");
            }

            // Now we need to follow multiple redirects, required data is fetched from the redirect URLs
            // String userId = "";
            int count = 10;
            while (count-- > 0) {
                res = oauth.follow(); // also fetches oauth fields from redirect urls
                url = oauth.location; // Continue URL
                if (url.isEmpty()) {
                    break; // end of redirects -> failure
                }
                if (url.contains(config.api.redirect_uri)) {
                    break;// end of redirects -> expected/successful
                }
            }

            if (oauth.idToken.isEmpty() || oauth.accessToken.isEmpty()) {
                if (res.response.contains("Allow access")) // additional consent required
                {
                    logger.debug("Consent missing, URL={}\n   HTML: {}", res.url, res.response);
                    throw new ApiSecurityException(
                            "Consent missing. Login to the Web App and give consent: " + config.api.authScope);
                }
                throw new ApiSecurityException("Login/OAuth failed, didn't got accessToken/idToken");
            }
            // In this case the id and access token were returned by the login process
            tokens.idToken = new ApiToken(oauth.idToken, oauth.accessToken, "bearer",
                    Integer.parseInt(oauth.expiresIn, 10));
            logger.trace("{}: OAuth successful, idToken was retrieved, valid for {}sec", config.vehicle.vin,
                    tokens.idToken.validity);
            tokens.csrf = oauth.csrf;
        } catch (UnsupportedEncodingException e) {
            logger.warn("Technical problem with algorithms", e);
            throw new ApiException("Technical problem with algorithms", e);
        }
        if (oauth.userId.isEmpty() && oauth.idToken.isEmpty()) {
            throw new ApiException("OAuth failed, check credentials!");
        }

        try {
            CNApiToken token;
            String json = "";

            if (CNAPI_BRAND_VWID.equals(config.api.brand)) { // config.api.xClientId.isEmpty()) {
                // We Connect
                logger.debug("{}: Login to We Connect", config.vehicle.vin);
                /*
                 * state: jwtstate,
                 * id_token: jwtid_token,
                 * redirect_uri: redirerctUri,
                 * region: "emea",
                 * access_token: jwtaccess_token,
                 * authorizationCode: jwtauth_code,
                 * });
                 */
                json = oauth.clearHeader().header(HttpHeader.HOST, "login.apps.emea.vwapps.io")//
                        .clearData().data("state", oauth.state).data("id_token", oauth.idToken)
                        .data("redirect_uri", config.api.redirect_uri).data("region", "emea")
                        .data("access_token", oauth.accessToken).data("authorizationCode", oauth.code) //
                        .post("https://login.apps.emea.vwapps.io/login/v1", true).response;
                token = fromJson(gson, json, CNApiToken.class);
            } else {
                // Last step: Request the access token from the VW token management
                // We save the generated tokens as tokenSet. Account handler and vehicle handler(s) are sharing the same
                // tokens. The tokenSetId provides access to that set.
                logger.debug("{}: Get Access Token", config.vehicle.vin);
                json = oauth.clearHeader().header(HttpHeader.USER_AGENT, "okhttp/3.7.0")
                        .header(CNAPI_HEADER_CLIENTID, config.api.xClientId)
                        .header(HttpHeader.HOST.toString(), "mbboauth-1d.prd.ece.vwg-connect.com")
                        .header(CNAPI_HEADER_APP, config.api.xappName).header(CNAPI_HEADER_VERS, config.api.xappVersion)
                        .header(HttpHeader.ACCEPT, "*/*") //
                        .clearData().data("grant_type", "id_token").data("token", tokens.idToken.idToken)
                        .data("scope", "sc2:fal") //
                        .post(CNAPI_URL_GET_SEC_TOKEN).response;
                token = fromJson(gson, json, CNApiToken.class);
            }
            token.normalize();
            if ((token.accessToken == null) || token.accessToken.isEmpty()) {
                throw new ApiSecurityException("Authentication failed: Unable to get access token!");
            }
            tokens.apiToken = new ApiToken(token);
            logger.debug("{}: accessToken was created, valid for {}sec", config.api.brand, tokens.apiToken.validity);
            updateTokenSet(config.tokenSetId, tokens);
            return tokens.apiToken.accessToken;
        } catch (ApiException e) {
            throw new ApiSecurityException("Unable to create API access token", e);
        }
    }

    public String createIdToken(CombinedConfig config) throws ApiException {
        TokenSet tokens = getTokenSet(config.tokenSetId);
        if (!tokens.idToken.isValid() || tokens.idToken.isExpired()) {
            // Token got invalid, force recreation
            logger.debug("{}: idToken experied, re-login", config.vehicle.vin);
            tokens.apiToken.invalidate();
            createAccessToken(config);
        }
        return tokens.idToken.idToken;
    }

    public String createProfileToken(CombinedConfig config) throws ApiException {
        TokenSet tokens = getTokenSet(config.tokenSetId);
        createIdToken(config);
        return tokens.idToken.accessToken;
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
        logger.debug("Authenticating SPIN, retires={}", authInfo.securityPinAuthInfo.remainingTries);

        // Request authentication
        CarNetSecurityPinAuthentication pinAuth = new CarNetSecurityPinAuthentication();
        pinAuth.securityPinAuthentication.securityToken = authInfo.securityPinAuthInfo.securityToken;
        pinAuth.securityPinAuthentication.securityPin.challenge = authInfo.securityPinAuthInfo.securityPinTransmission.challenge;
        pinAuth.securityPinAuthentication.securityPin.securityPinHash = pinHash;
        // "https://mal-3a.prd.ece.vwg-connect.com/api/rolesrights/authorization/v2/security-pin-auth-completed",
        String data = gson.toJson(pinAuth);
        json = http.post(config.vstatus.rolesRightsUrl + "/rolesrights/authorization/v2/security-pin-auth-completed",
                headers, data).response;
        CNApiToken t = fromJson(gson, json, CNApiToken.class);
        ApiToken securityToken = new ApiToken(t);
        if (securityToken.securityToken.isEmpty()) {
            throw new ApiSecurityException("Authentication failed: Unable to get access token!");
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
     * @throws ApiException
     */
    public boolean refreshTokens(CombinedConfig config) throws ApiException {
        try {
            TokenSet tokens = getTokenSet(config.tokenSetId);
            refreshToken(config, tokens.apiToken);

            Iterator<ApiToken> it = securityTokens.iterator();
            while (it.hasNext()) {
                ApiToken stoken = it.next();
                if (!refreshToken(config, stoken)) {
                    // Token invalid / refresh failed -> remove
                    logger.debug("{}: Security token for service {} expired, remove", config.vehicle.vin,
                            stoken.service);
                    securityTokens.remove(stoken);
                }
            }
        } catch (ApiException e) {
            // Ignore problems with the idToken or securityToken if the accessToken was requested successful
            logger.debug("Unable to refresh token: {}", e.toString()); // "normal, no stack trace"
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid token!");
        }
        return false;
    }

    public boolean isAccessTokenValid(CombinedConfig config) {
        try {
            TokenSet tokens = getTokenSet(config.tokenSetId);
            return tokens.apiToken.isValid();
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid token!");
            return false;
        }
    }

    /**
     * Refresh the access token
     *
     * @param config Combined account/vehicle config
     * @param token Token to refresh
     * @return new token
     * @throws ApiException
     */
    public boolean refreshToken(CombinedConfig config, ApiToken token) throws ApiException {
        if (!token.isValid()) {
            return false;
        }

        TokenSet tokens = getTokenSet(config.tokenSetId);
        ApiHttpClient http = tokens.http;
        if (tokens.apiToken.refreshToken.isEmpty()) {
            logger.debug("{}: No refreshToken available, token is now invalid", config.vehicle.vin);
            token.invalidate();
            return false;
        }

        if (token.isExpired()) {
            logger.debug("{}: Refreshing Token {}", config.vehicle.vin, token.accessToken);
            try {
                String json = "";
                if (CNAPI_BRAND_VWID.equals(config.api.brand)) {
                    ApiHttpMap headers = new ApiHttpMap().header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + token.refreshToken);
                    json = http.get(config.api.tokenRefreshUrl, headers.getHeaders()).response;
                } else {
                    ;
                    ApiHttpMap data = new ApiHttpMap().data("grant_type", "refresh_token")
                            .data("refresh_token", tokens.apiToken.refreshToken).data("scope", "sc2%3Afal");
                    json = http.post(config.api.tokenRefreshUrl, http.fillRefreshHeaders(), data.getData(),
                            false).response;
                }
                CNApiToken newToken = gson.fromJson(json, CNApiToken.class);
                if (newToken == null) {
                    throw new ApiSecurityException("Unable to parse token information from JSON");
                }
                newToken.normalize();
                tokens.apiToken.accessToken = newToken.accessToken;
                tokens.apiToken.setValidity(getInteger(newToken.validity));
                updateTokenSet(config.tokenSetId, tokens);
                logger.debug("{}: Token refresh successful, valid for {} sec, new token={}", config.vehicle.vin,
                        tokens.apiToken.validity, tokens.apiToken.accessToken);
            } catch (ApiException e) {
                logger.debug("{}: Unable to refresh token: {}", config.vehicle.vin, e.toString());
                // Invalidate token (triggers a new login when accessToken is required)
                if (token.isExpired()) {
                    tokens.apiToken.invalidate();
                    updateTokenSet(config.tokenSetId, tokens);
                    logger.debug("Token refresh failed and current accessToken is expired");
                } else {
                    logger.debug("Token refresh failed, but accessToken is still valid");
                }
                return false;
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
