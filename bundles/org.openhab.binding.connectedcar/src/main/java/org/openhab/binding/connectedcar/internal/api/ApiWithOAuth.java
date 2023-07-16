/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.OAuthToken;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiWithOAuth} extends ApiBase and adds the default OAuth implementation
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiWithOAuth extends ApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(ApiWithOAuth.class);
    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    public ApiWithOAuth(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public String getLoginUrl(IdentityOAuthFlow oauth) throws ApiException {
        String authUrl = config.api.issuerRegionMappingUrl + "/oidc/v1/authorize";
        oauth.headers(config.api.loginHeaders).header(HttpHeader.USER_AGENT, CNAPI_HEADER_USER_AGENT).header(
                HttpHeader.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        if (!config.api.xrequest.isEmpty()) {
            oauth.header("x-requested-with", config.api.xrequest).header("upgrade-insecure-requests", "1");
        }
        logger.trace("{}: getLoginUrl tokenSetId: {} clientId: {}", config.getLogId(), config.tokenSetId,
                config.api.clientId);
        String url = authUrl + "?client_id=" + urlEncode(config.api.clientId) //
                + "&scope=" + urlEncode(config.api.authScope).replace("%20", "+") //
                + "&response_type=" + urlEncode(config.api.responseType).replace("%20", "+") //
                + "&redirect_uri=" + urlEncode(config.api.redirect_uri) //
                + "&nonce=" + generateNonce() + "&state=" + UUID.randomUUID().toString();
        if (config.authenticator != null) {
            url = config.authenticator.updateAuthorizationUrl(url);
        }
        return oauth.addCodeChallenge(url);
    }

    @Override
    public ApiIdentity login(String loginUrl, IdentityOAuthFlow oauth) throws ApiException {
        String logId = config.getLogId();
        try {
            ApiResult res = oauth.get(loginUrl);
            String url = oauth.location;
            if (url.contains("error=consent_required")) {
                logger.debug("{}: Missing consent: {}", logId, url);
                String message = URLDecoder.decode(url, UTF_8);
                message = substringBefore(substringAfter(message, "&error_description="), "&");
                throw new ApiSecurityException(
                        "Login failed, Consent missing. Login to the Web App and give consent: " + message);
            }
            res = oauth.follow();
            if (oauth.csrf.isEmpty() || oauth.relayState.isEmpty() || oauth.hmac.isEmpty()) {
                logger.debug("{}: OAuth failed, can't get parameters\nHTML {}: {}", logId, res.httpCode, res.response);
                throw new ApiSecurityException("Unable to login - can't get OAuth parameters!");
            }

            // Authenticate: Username
            logger.trace("{}: OAuth input: User", logId);
            // "/signin-service/v1/" + config.api.clientId + "/login/identifier";
            url = config.api.issuerRegionMappingUrl + oauth.action;
            oauth.clearData().data("_csrf", oauth.csrf).data("relayState", oauth.relayState).data("hmac", oauth.hmac)
                    .data(config.api.authUserAttr, URLEncoder.encode(config.account.user, UTF_8));
            if (config.authenticator != null) {
                config.authenticator.updateSigninParameters(oauth);
            }
            oauth.post(url, false);
            if (oauth.location.isEmpty()) {
                logger.debug("{}: OAuth failed, can't input password; HTML {}: {}", logId, res.httpCode, res.response);
                throw new ApiSecurityException("Unable to login - can't get OAuth parameters!");
            }

            // Authenticate: Password
            logger.trace("{}: OAuth: Input password", logId);
            url = config.api.issuerRegionMappingUrl + oauth.location; // Signin URL
            String authUrl = url.split("\\?")[0];
            res = oauth.get(url);

            logger.trace("{}: OAuth: Authenticate", logId);
            url = authUrl;
            res = oauth.clearData().data("_csrf", oauth.csrf).data("relayState", oauth.relayState)
                    .data("hmac", oauth.hmac).data("email", URLEncoder.encode(config.account.user, UTF_8))
                    .data("password", URLEncoder.encode(config.account.password, UTF_8)) //
                    .post(url, false);
            url = oauth.location; // Continue URL
            if (url.contains("terms-and-conditions")) {
                throw new ApiException(
                        "Consent to terms&conditions required, login to the Web portal and give consent");
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

            // Check for required consent, do not respond automatically, this has to be done by the user with regards to
            // data protection
            if (oauth.idToken.isEmpty() && oauth.code.isEmpty()) {
                if (res.response.contains("Allow access")) // additional consent required
                {
                    logger.debug("{}: Consent missing, URL={}\n   HTML: {}", logId, res.url, res.response);
                    throw new ApiSecurityException(
                            "Consent missing. Login to the Web App and give consent: " + config.api.authScope);
                }
                throw new ApiSecurityException("Login/OAuth failed, didn't got accessToken/idToken");
            }

            // In this case the id and access token were returned by the login process
            return new ApiIdentity(oauth.idToken, oauth.accessToken, Integer.parseInt(oauth.expiresIn, 10));
        } catch (UnsupportedEncodingException e) {
            logger.warn("{}: Technical problem with algorithms", logId, e);
            throw new ApiException("Technical problem with algorithms", e);
        }
    }

    @Override
    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException {
        // Last step: Request the access token from the VW token management
        // We save the generated tokens as tokenSet. Account handler and vehicle handler(s) are sharing the same
        // tokens. The tokenSetId provides access to that set.
        String json = oauth.clearHeader().header(HttpHeader.USER_AGENT, CNAPI_HEADER_USER_AGENT)
                .header(HttpHeader.ACCEPT, "*/*").header(HttpHeader.HOST, "mbboauth-1d.prd.ece.vwg-connect.com")
                .header(CNAPI_HEADER_APP, config.api.xappName).header(CNAPI_HEADER_VERS, config.api.xappVersion)
                .header(CNAPI_HEADER_CLIENTID, config.api.xClientId)//
                .clearData().data("grant_type", "id_token").data("token", oauth.idToken).data("scope", "sc2:fal") //
                .post(config.api.tokenUrl, false).response;
        return new ApiIdentity(fromJson(gson, json, OAuthToken.class));
    }

    @Override
    public OAuthToken refreshToken(ApiIdentity token) throws ApiException {
        logger.trace("{}: ApiWithOAuth.refreshToken for {}/{}", config.getLogId(), config.tokenSetId,
                config.api.clientId);
        ApiHttpMap map = new ApiHttpMap().header(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT)
                .header(HttpHeader.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded")
                .header(HttpHeader.HOST, "mbboauth-1d.prd.ece.vwg-connect.com")
                .header(CNAPI_HEADER_APP, config.api.xappName).header(CNAPI_HEADER_VERS, config.api.xappVersion)
                .header(CNAPI_HEADER_CLIENTID, config.api.xClientId) //
                .data("grant_type", "refresh_token").data("refresh_token", token.refreshToken)
                .data("scope", "sc2%3Afal");
        String json = http.post(config.api.tokenRefreshUrl, map.getHeaders(), map.getData(), false).response;
        return fromJson(gson, json, OAuthToken.class);
    }
}
