/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler.auth;

import static org.openhab.binding.mybmw.internal.utils.BimmerConstants.*;
import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.dto.auth.OAuthSettingsQueryResponse;
import org.openhab.binding.mybmw.internal.handler.MyBMWBridgeHandler;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * requests the tokens for MyBMW API authorization
 *
 * thanks to bimmer_connected
 * https://github.com/bimmerconnected/bimmer_connected
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - extracted from myBmwProxy
 * @author Mark Herwege - refactor to use OAuthFactory
 * @author Mark Herwege - remove China
 */
@NonNullByDefault
public class MyBMWTokenController {

    private final Logger logger = LoggerFactory.getLogger(MyBMWTokenController.class);

    private MyBMWBridgeHandler bridgeHandler;
    private MyBMWBridgeConfiguration bridgeConfiguration;
    private final HttpClient httpClient;
    private final OAuthFactory oAuthFactory;

    private String oAuthClientServiceId;
    private @Nullable OAuthClientService oAuthClientService = null;
    private AccessTokenResponse tokenResponse = new AccessTokenResponse();
    private boolean waitingForInitialToken = false;

    private static final String SESSION_ID = UUID.randomUUID().toString();

    public MyBMWTokenController(MyBMWBridgeHandler bridgeHandler, MyBMWBridgeConfiguration configuration,
            HttpClient httpClient, OAuthFactory oAuthFactory) {
        this.bridgeHandler = bridgeHandler;
        this.bridgeConfiguration = configuration;
        this.httpClient = httpClient;
        this.oAuthFactory = oAuthFactory;
        this.oAuthClientServiceId = bridgeHandler.getThing().getUID().getAsString();

        this.oAuthClientService = oAuthFactory.getOAuthClientService(oAuthClientServiceId);
    }

    public synchronized void setBridgeConfiguration(MyBMWBridgeConfiguration bridgeConfiguration) {
        this.bridgeConfiguration = bridgeConfiguration;
    }

    /**
     * Gets new token if old one is expired or invalid. In case of error the token remains. So if token refresh fails
     * the corresponding requests will also fail and update the Thing status accordingly.
     *
     * @return token
     */
    public synchronized AccessTokenResponse getToken() {
        logger.trace("getToken");

        if (waitingForInitialToken && !bridgeConfiguration.getHCaptchaToken().isBlank()) {
            // if the hCaptchaToken is available, then a new login is triggered
            logger.trace("initial login, using captchatoken {}", bridgeConfiguration.getHCaptchaToken());

            boolean tokenCreationSuccess = loginROW();
            if (tokenCreationSuccess) {
                waitingForInitialToken = false;
                logger.trace("get inital token success");
            } else {
                logger.warn(
                        "initial Authentication failed, maybe request a new captcha token, see https://bimmer-connected.readthedocs.io/en/stable/captcha.html!");
                bridgeHandler.tokenInitError();
                logger.trace("get inital token failed");
            }

            // reset the token as it times out
            bridgeHandler.setHCaptchaToken(Constants.EMPTY);
        } else if (!waitingForInitialToken && tokenResponse.isExpired(Instant.now(), 5)) {
            // try to refresh the token
            boolean tokenUpdateSuccess = refreshTokenROW();
            logger.trace("update token {}", tokenUpdateSuccess ? "success" : "failed");

            if (!tokenUpdateSuccess) {
                logger.warn("Updating token failed!");
                waitingForInitialToken = true;

                if (bridgeConfiguration.getHCaptchaToken().isBlank()) {
                    logger.warn(
                            "initial Authentication failed, request a new captcha token, see https://bimmer-connected.readthedocs.io/en/stable/captcha.html!");
                    bridgeHandler.tokenInitError();
                } else {
                    getToken();
                }
            }
        }

        return tokenResponse;
    }

    /**
     * @return true if the token was successfully retrieved
     */
    private boolean loginROW() {
        logger.trace("get initial token");

        try {
            /**
             * Step 1) Get OAuth2 settings for further queries
             */
            OAuthSettingsQueryResponse aqr = getOAuthSettings();

            /**
             * Step 2) Calculate values for oauth base parameters
             */
            String codeVerifier = generateToken(86);
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state = generateToken(22);
            String nonce = generateToken(22);

            MultiMap<@Nullable String> baseParams = new MultiMap<>();
            baseParams.put(CLIENT_ID, aqr.clientId);
            baseParams.put(RESPONSE_TYPE, CODE);
            baseParams.put(REDIRECT_URI, aqr.returnUrl);
            baseParams.put(STATE, state);
            baseParams.put(NONCE, nonce);
            baseParams.put(SCOPE, aqr.scopes());
            baseParams.put(CODE_CHALLENGE, codeChallenge);
            baseParams.put(CODE_CHALLENGE_METHOD, "S256");

            /**
             * Step 3) Authentication with username and password, get authentication code
             */
            String loginUrl = aqr.gcdmBaseUrl + OAUTH_ENDPOINT;
            Request loginRequest = httpClient.POST(loginUrl);

            loginRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
            loginRequest.header(HCAPTCHA_TOKEN, bridgeConfiguration.getHCaptchaToken());

            MultiMap<@Nullable String> loginParams = new MultiMap<>(baseParams);
            loginParams.put(GRANT_TYPE, AUTHORIZATION_CODE);
            loginParams.put(USERNAME, bridgeConfiguration.getUserName());
            loginParams.put(PASSWORD, bridgeConfiguration.getPassword());
            loginRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(loginParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse loginResponse = loginRequest.send();
            if (loginResponse.getStatus() != 200) {
                throw new HttpResponseException("URL: " + loginRequest.getURI() + ", Error: "
                        + loginResponse.getStatus() + ", Message: " + loginResponse.getContentAsString(),
                        loginResponse);
            }

            String authCode = getAuthCode(loginResponse.getContentAsString());
            logger.trace("authentication code: {}", authCode);

            /**
             * Step 4) With authentication code get code
             */
            Request authRequest = httpClient.POST(loginUrl).followRedirects(false);
            MultiMap<@Nullable String> authParams = new MultiMap<>(baseParams);
            authParams.put(AUTHORIZATION, authCode);
            authRequest.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
            authRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(authParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse authResponse = authRequest.send();
            if (authResponse.getStatus() != 302) {
                throw new HttpResponseException("URL: " + authRequest.getURI() + ", Error: " + authResponse.getStatus()
                        + ", Message: " + authResponse.getContentAsString(), authResponse);
            }
            String code = codeFromUrl(authResponse.getHeaders().get(HttpHeader.LOCATION));
            logger.trace("code: {}", code);

            /**
             * Step 5) With code get token
             */
            if (oAuthFactory.getOAuthClientService(oAuthClientServiceId) != null) {
                oAuthFactory.ungetOAuthService(oAuthClientServiceId);
            }
            OAuthClientService oAuthClientService = oAuthFactory.createOAuthClientService(oAuthClientServiceId,
                    aqr.tokenEndpoint, loginUrl, aqr.clientId, aqr.clientSecret, aqr.scopes(), false);
            this.oAuthClientService = oAuthClientService;

            oAuthClientService.addExtraAuthField(CODE_VERIFIER, codeVerifier);
            tokenResponse = oAuthClientService.getAccessTokenResponseByAuthorizationCode(code, aqr.returnUrl);

            return true;
        } catch (ExecutionException | OAuthException | IOException | OAuthResponseException | InterruptedException
                | HttpResponseException | TimeoutException | NoSuchAlgorithmException e) {
            logger.warn("Exception at login: {}", e.getMessage());
        }
        return false;
    }

    /**
     * refresh the existing token
     *
     * @return true if token has successfully been refreshed
     */
    private boolean refreshTokenROW() {
        logger.trace("refreshToken");
        OAuthClientService oAuthClientService = this.oAuthClientService;
        if (oAuthClientService == null) {
            return false;
        }
        try {
            AccessTokenResponse tokenResponse;
            tokenResponse = oAuthClientService.getAccessTokenResponse();
            if (tokenResponse != null) {
                this.tokenResponse = tokenResponse;
                return true;
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.warn("Exception refreshing token: ", e);
        }
        return false;
    }

    private OAuthSettingsQueryResponse getOAuthSettings()
            throws InterruptedException, TimeoutException, ExecutionException {
        String uuidString = UUID.randomUUID().toString();

        String oAuthSettingsUrl = "https://" + EADRAX_SERVER_MAP.get(bridgeConfiguration.getRegion())
                + API_OAUTH_CONFIG;
        Request oAuthSettingsRequest = httpClient.newRequest(oAuthSettingsUrl);
        oAuthSettingsRequest.header(HEADER_ACP_SUBSCRIPTION_KEY, OCP_APIM_KEYS.get(bridgeConfiguration.getRegion()));
        oAuthSettingsRequest.header(HEADER_X_USER_AGENT, String.format(X_USER_AGENT, BRAND_BMW,
                APP_VERSIONS.get(bridgeConfiguration.getRegion()), bridgeConfiguration.getRegion()));
        oAuthSettingsRequest.header(HEADER_X_IDENTITY_PROVIDER, AUTH_PROVIDER);
        oAuthSettingsRequest.header(HEADER_X_CORRELATION_ID, uuidString);
        oAuthSettingsRequest.header(HEADER_BMW_CORRELATION_ID, uuidString);
        oAuthSettingsRequest.header(HEADER_BMW_SESSION_ID, SESSION_ID);

        ContentResponse oAuthSettingsRepsonse = oAuthSettingsRequest.send();
        if (oAuthSettingsRepsonse.getStatus() != 200) {
            throw new HttpResponseException("URL: " + oAuthSettingsRequest.getURI() + ", Error: "
                    + oAuthSettingsRepsonse.getStatus() + ", Message: " + oAuthSettingsRepsonse.getContentAsString(),
                    oAuthSettingsRepsonse);
        }
        OAuthSettingsQueryResponse aqr = JsonStringDeserializer
                .deserializeString(oAuthSettingsRepsonse.getContentAsString(), OAuthSettingsQueryResponse.class);
        return aqr;
    }

    private String generateToken(int length) {
        String bytes = StringUtils.getRandomAlphabetic(length).toLowerCase();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.getBytes());
    }

    private String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String getAuthCode(String response) {
        String[] keys = response.split("&");
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].startsWith(AUTHORIZATION)) {
                String authCode = keys[i].split("=")[1];
                authCode = authCode.split("\"")[0];
                return authCode;
            }
        }
        return Constants.EMPTY;
    }

    private String codeFromUrl(String encodedUrl) {
        final MultiMap<@Nullable String> tokenMap = new MultiMap<>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        final StringBuilder codeFound = new StringBuilder();
        tokenMap.forEach((key, value) -> {
            if (!value.isEmpty()) {
                String val = value.get(0);
                if (key.endsWith(CODE) && (val != null)) {
                    codeFound.append(val.toString());
                }
            }
        });
        return codeFound.toString();
    }
}
