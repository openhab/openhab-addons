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

import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiToken.OAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiWithOAuth} extends ApiBase and adds the default OAuth implementation
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ApiWithOAuth extends ApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(ApiWithOAuth.class);
    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    public ApiWithOAuth(ApiHttpClient httpClient, TokenManager tokenManager, @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public String getLoginUrl(TokenOAuthFlow oauth) throws ApiException {
        String url;
        String nonce = generateNonce();
        String state = UUID.randomUUID().toString();

        String authUrl = config.api.issuerRegionMappingUrl + "/oidc/v1/authorize";
        oauth.header(HttpHeader.USER_AGENT, CNAPI_HEADER_USER_AGENT).header(HttpHeader.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header("x-requested-with", config.api.xrequest).header("upgrade-insecure-requests", "1");
        url = authUrl + "?client_id=" + urlEncode(config.api.clientId) + "&scope="
                + urlEncode(config.api.authScope).replace("%20", "+") + "&response_type="
                + urlEncode(config.api.responseType).replace("%20", "+") + "&redirect_uri="
                + urlEncode(config.api.redirect_uri) + "&nonce=" + nonce + "&state=" + state;
        if (config.authenticator != null) {
            url = config.authenticator.updateAuthorizationUrl(url);
        }
        return oauth.addCodeChallenge(url);
    }

    @Override
    public ApiToken login(String loginUrl, TokenOAuthFlow oauth) throws ApiException {
        try {
            ApiResult res = oauth.get(loginUrl);
            String url = oauth.location;
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
            oauth.post(url, false);
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
            res = oauth.post(url, false);
            url = oauth.location; // Continue URL
            if (url.contains("error=login.error.password_invalid")) {
                throw new ApiSecurityException("Login failed due to invalid password or locked account!");
            }
            if (url.contains("error=login.errors.throttled")) {
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

            if (oauth.idToken.isEmpty()) {
                if (res.response.contains("Allow access")) // additional consent required
                {
                    logger.debug("Consent missing, URL={}\n   HTML: {}", res.url, res.response);
                    throw new ApiSecurityException(
                            "Consent missing. Login to the Web App and give consent: " + config.api.authScope);
                }
                throw new ApiSecurityException("Login/OAuth failed, didn't got accessToken/idToken");
            }
            if (oauth.userId.isEmpty() && oauth.idToken.isEmpty()) {
                throw new ApiException("OAuth failed, check credentials!");
            }

            // In this case the id and access token were returned by the login process
            logger.trace("{}: OAuth successful, idToken was retrieved, valid for {}sec", config.vehicle.vin,
                    oauth.expiresIn);
            return new ApiToken(oauth.idToken, oauth.accessToken, Integer.parseInt(oauth.expiresIn, 10));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Technical problem with algorithms", e);
            throw new ApiException("Technical problem with algorithms", e);
        }
    }

    @Override
    public ApiToken grantAccess(TokenOAuthFlow oauth) throws ApiException {
        // Last step: Request the access token from the VW token management
        // We save the generated tokens as tokenSet. Account handler and vehicle handler(s) are sharing the same
        // tokens. The tokenSetId provides access to that set.
        logger.debug("{}: Get Access Token", config.vehicle.vin);
        String json = oauth.clearHeader().header(HttpHeader.USER_AGENT, "okhttp/3.7.0")
                .header(CNAPI_HEADER_CLIENTID, config.api.xClientId)
                .header(HttpHeader.HOST.toString(), "mbboauth-1d.prd.ece.vwg-connect.com")
                .header(CNAPI_HEADER_APP, config.api.xappName).header(CNAPI_HEADER_VERS, config.api.xappVersion)
                .header(HttpHeader.ACCEPT, "*/*") //
                .clearData().data("grant_type", "id_token").data("token", oauth.idToken).data("scope", "sc2:fal") //
                .post(CNAPI_URL_GET_SEC_TOKEN, false).response;
        return new ApiToken(fromJson(gson, json, OAuthToken.class));
    }

    @Override
    public OAuthToken refreshToken(ApiToken token) throws ApiException {

        ApiHttpMap data = new ApiHttpMap().data("grant_type", "refresh_token").data("refresh_token", token.refreshToken)
                .data("scope", "sc2%3Afal");
        String json = http.post(config.api.tokenRefreshUrl, http.fillRefreshHeaders(), data.getData(), false).response;
        return fromJson(gson, json, OAuthToken.class);
    }
}
