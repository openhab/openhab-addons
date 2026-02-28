/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.api;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.viessmann.internal.dto.oauth.AuthorizeResponseDTO;
import org.openhab.binding.viessmann.internal.dto.oauth.TokenResponseDTO;
import org.openhab.binding.viessmann.internal.interfaces.ApiInterface;
import org.openhab.binding.viessmann.internal.util.PkceUtil;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ViessmannAuth} performs the initial OAuth authorization
 * with the Viessmann authorization servers.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class ViessmannAuth {

    private final Logger logger = LoggerFactory.getLogger(ViessmannAuth.class);

    private final ApiInterface bridgeHandler;
    private final ViessmannApi api;
    private final String apiKey;
    private final String user;
    private final String password;
    private final @Nullable String callbackUrl;

    private final HttpClient httpClient;

    private ViessmannAuthState state;

    private @Nullable AuthorizeResponseDTO authResponse;
    public @Nullable String accessToken;

    private String codeVerifier = "";

    /**
     * The authorization code needed to make the first-time request
     * of the refresh and access tokens. Obtained from the call to {@code authorize()}.
     */
    private @Nullable String code;

    private @Nullable String refreshToken;

    public ViessmannAuth(ViessmannApi api, ApiInterface bridgeHandler, String apiKey, HttpClient httpClient,
            String user, String password, @Nullable String callbackUrl) {
        this.api = api;
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.bridgeHandler = bridgeHandler;
        this.user = user;
        this.password = password;
        this.callbackUrl = callbackUrl;
        state = ViessmannAuthState.NEED_AUTH;
        authResponse = null;
    }

    public void setState(ViessmannAuthState newState) {
        if (newState != state) {
            logger.debug("ViessmannAuth: Change state from {} to {}", state, newState);
            state = newState;
        }
    }

    public void setRefreshToken(String newRefreshToken) {
        if (!newRefreshToken.equals(refreshToken)) {
            logger.debug("ViessmannAuth: Change refreshToken from {} to {}", refreshToken, newRefreshToken);
            refreshToken = newRefreshToken;
        }
    }

    public boolean isComplete() {
        return state == ViessmannAuthState.COMPLETE;
    }

    public ViessmannAuthState doAuthorization() throws ViessmannAuthException {
        switch (state) {
            case NEED_AUTH:
                authorize();
                if (state == ViessmannAuthState.NEED_TOKEN) {
                    getTokens();
                }
                break;
            case NEED_LOGIN:
                break;
            case NEED_TOKEN:
                getTokens();
                break;
            case NEED_REFRESH_TOKEN:
                getRefreshTokens();
                break;
            case COMPLETE:
                bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
                break;
        }
        return state;
    }

    public @Nullable String getAccessToken() throws ViessmannAuthException {
        return this.accessToken;
    }

    public void setAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    /**
     * Call the Viessmann authorize endpoint to get the authorization code.
     */
    private void authorize() throws ViessmannAuthException {
        logger.debug("ViessmannAuth: State is {}: Executing step: 'authorize'", state);
        if (callbackUrl != null) {
            codeVerifier = PkceUtil.generateCodeVerifier();

            String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

            StringBuilder url = new StringBuilder(VIESSMANN_AUTHORIZE_URL);
            url.append("?response_type=code");
            url.append("&client_id=").append(apiKey);
            url.append("&code_challenge=").append(codeChallenge);
            url.append("&redirect_uri=").append(callbackUrl).append("/viessmann/authcode/");
            url.append("&scope=").append(VIESSMANN_SCOPE);
            url.append("&code_challenge_method=S256");

            logger.trace("ViessmannAuth: Getting authorize URL={}", url);
            String response = executeUrlAuthorize(url.toString());
            logger.trace("ViessmannAuth: Auth response: {}", response);
            if (response != null) {
                if (response.contains("<!DOCTYPE html>")) {
                    logger.warn("ViessmannAuth: Login failed. Please check user and password.");
                    updateBridgeStatusLogin();
                    return;
                }
                if (response.contains("error")) {
                    JsonObject error = JsonParser.parseString(response).getAsJsonObject();
                    String description = error.get("error_description").getAsString();
                    logger.warn("ViessmannAuth: Login failed. {}", description);
                    return;
                }
            }
            try {
                authResponse = api.getGson().fromJson(response, AuthorizeResponseDTO.class);
                if (authResponse == null) {
                    logger.debug("ViessmannAuth: Got null authorize response from Viessmann API");
                    setState(ViessmannAuthState.NEED_AUTH);
                } else {
                    AuthorizeResponseDTO resp = this.authResponse;
                    if (resp == null) {
                        logger.warn("AuthorizeResponseDTO is null. This should not happen.");
                        return;
                    }
                    if (resp.errorMsg != null) {
                        logger.debug("ViessmannAuth: Got null authorize response from Viessmann API");
                        setState(ViessmannAuthState.NEED_AUTH);
                        return;
                    }
                    code = resp.code;
                    setState(ViessmannAuthState.NEED_TOKEN);
                }
            } catch (JsonSyntaxException e) {
                logger.info("ViessmannAuth: Exception while parsing authorize response: {}", e.getMessage());
                setState(ViessmannAuthState.NEED_AUTH);
            }
        } else {
            logger.warn("We do not have any callback url, so we cannot authorize!");
        }
    }

    /**
     * Call the Viessmann token endpoint to get the access and refresh tokens. Once successfully retrieved,
     * the access and refresh tokens will be injected into the OHC OAuth service.
     * Warnings are suppressed to avoid the Gson.fromJson warnings.
     */
    private void getTokens() throws ViessmannAuthException {
        logger.debug("ViessmannAuth: State is {}: Executing step: 'getToken'", state);
        if (callbackUrl != null) {
            StringBuilder url = new StringBuilder(VIESSMANN_TOKEN_URL);
            url.append("?grant_type=authorization_code");
            url.append("&client_id=").append(apiKey);
            url.append("&redirect_uri=").append(callbackUrl).append("/viessmann/authcode/");
            url.append("&code_verifier=").append(codeVerifier);
            url.append("&code=").append(code);

            logger.trace("ViessmannAuth: Posting token URL={}", url);
            String response = executeUrlToken(url.toString());

            TokenResponseDTO tokenResponse = api.getGson().fromJson(response, TokenResponseDTO.class);
            if (tokenResponse == null) {
                logger.debug("ViessmannAuth: Got null token response from Viessmann API");
                bridgeHandler.updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "ViessmannAuth: Got null token response from Viessmann API");
                setState(ViessmannAuthState.NEED_AUTH);
                return;
            }
            logger.trace("ViessmannAuth: Got a valid token response: {}", response);
            api.setTokenResponseDTO(tokenResponse);
            refreshToken = tokenResponse.refreshToken;
            api.setTokenExpiryDate(TimeUnit.SECONDS.toMillis(tokenResponse.expiresIn));
            api.setRefreshTokenExpiryDate(TimeUnit.SECONDS.toMillis(REFRESH_TOKEN_EXPIRE));
            setState(ViessmannAuthState.COMPLETE);
        } else {
            logger.warn("We do not have any callback url, so we cannot get token!");
        }
    }

    /**
     * Call the Viessmann token endpoint to get the access and refresh tokens. Once successfully retrieved,
     * the access and refresh tokens will be injected into the OHC OAuth service.
     * Warnings are suppressed to avoid the Gson.fromJson warnings.
     */
    private void getRefreshTokens() throws ViessmannAuthException {
        logger.debug("ViessmannAuth: State is {}: Executing step: 'getRefreshToken'", state);
        StringBuilder url = new StringBuilder(VIESSMANN_TOKEN_URL);
        url.append("?grant_type=refresh_token");
        url.append("&client_id=").append(apiKey);
        url.append("&refresh_token=").append(refreshToken);

        logger.trace("ViessmannAuth: Posting token URL={}", url);
        String response = executeUrlToken(url.toString());

        TokenResponseDTO tokenResponse = api.getGson().fromJson(response, TokenResponseDTO.class);
        if (tokenResponse == null) {
            logger.debug("ViessmannAuth: Got null token response from Viessmann API");
            bridgeHandler.updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "ViessmannAuth: Got null token response from Viessmann API");
            setState(ViessmannAuthState.NEED_AUTH);
            return;
        }
        logger.trace("ViessmannAuth: Got a valid token response: {}", response);
        bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
        api.setTokenResponseDTO(tokenResponse);
        api.setTokenExpiryDate(TimeUnit.SECONDS.toMillis(tokenResponse.expiresIn));

        setState(ViessmannAuthState.COMPLETE);
    }

    private void updateBridgeStatusLogin() {
        bridgeHandler.updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Login fails. Please check user and password.");
    }

    private void updateBridgeStatusApiKey() {
        bridgeHandler.updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "The login failed. Please check API Key.");
    }

    private void updateBridgeStatusRedirectionUri() {
        bridgeHandler.updateBridgeStatusExtended(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "The login failed. Please check the Redirection URI for your client in the Viessmann Developer Portal. <br> It must be <b>"
                        + callbackUrl + "/viessmann/authcode/</b>");
    }

    private @Nullable String executeUrlAuthorize(String url) {
        String authorization = new String(Base64.getEncoder().encode((user + ":" + password).getBytes()),
                StandardCharsets.UTF_8);
        httpClient.getAuthenticationStore().clearAuthentications();
        httpClient.getCookieStore().removeAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Request request = httpClient.newRequest(url).timeout(API_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .method(HttpMethod.GET).header("Authorization", "Basic " + authorization).header("Host", IAM_HOST)
                .header("Accept", "application/json");
        try {
            ContentResponse contentResponse = request.onResponseContent((resp, buffer) -> {
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                baos.write(bytes, 0, bytes.length);
            }).send();

            String body = baos.toString(StandardCharsets.UTF_8);
            logger.debug("Response body: {}", body);

            switch (contentResponse.getStatus()) {
                case HttpStatus.OK_200:
                    return contentResponse.getContentAsString();
                case HttpStatus.BAD_REQUEST_400:
                    logger.debug("BAD REQUEST(400) response received: {}", contentResponse.getContentAsString());
                    updateBridgeStatusRedirectionUri();
                    return contentResponse.getContentAsString();
                case HttpStatus.UNAUTHORIZED_401:
                    logger.debug("UNAUTHORIZED(401) response received: {}", contentResponse.getContentAsString());
                    return contentResponse.getContentAsString();
                case HttpStatus.NO_CONTENT_204:
                    logger.debug("HTTP response 204: No content. Check configuration");
                    break;
                default:
                    logger.debug("HTTP GET failed: {}, {}", contentResponse.getStatus(), contentResponse.getReason());
                    break;
            }
        } catch (TimeoutException e) {
            logger.debug("TimeoutException: Call to Viessmann API timed out");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpResponseException httpEx) {
                int status = httpEx.getResponse().getStatus();
                String body = httpEx.getResponse().getReason();
                logger.warn("HTTP error {} - {}", status, body);
                String content = baos.toString(StandardCharsets.UTF_8);
                logger.debug("Response content: {}", content);

                updateBridgeStatusApiKey();
            } else {
                logger.error("ExecutionException cause: {}", cause == null ? "null" : cause.toString(), cause);
            }
        } catch (InterruptedException e) {
            logger.debug("InterruptedException on call to Viessmann authorization API: {}", e.getMessage());
        }
        return null;
    }

    private @Nullable String executeUrlToken(String url) {
        Request request = httpClient.newRequest(url).timeout(API_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .method(HttpMethod.POST).header("Content-Type", "application/x-www-form-urlencoded")
                .header("Host", IAM_HOST);
        try {
            ContentResponse contentResponse = request.send();
            switch (contentResponse.getStatus()) {
                case HttpStatus.OK_200:
                    return contentResponse.getContentAsString();
                case HttpStatus.BAD_REQUEST_400:
                    logger.debug("BAD REQUEST(400) response received: {}", contentResponse.getContentAsString());
                    return contentResponse.getContentAsString();
                case HttpStatus.UNAUTHORIZED_401:
                    logger.debug("UNAUTHORIZED(401) response received: {}", contentResponse.getContentAsString());
                    return contentResponse.getContentAsString();
                case HttpStatus.NO_CONTENT_204:
                    logger.debug("HTTP response 204: No content. Check configuration");
                    break;
                default:
                    logger.debug("HTTP POST failed: {}, {}", contentResponse.getStatus(), contentResponse.getReason());
                    break;
            }
        } catch (TimeoutException e) {
            logger.debug("TimeoutException: Call to Viessmann API timed out");
        } catch (ExecutionException e) {
            logger.debug("ExecutionException on call to Viessmann authorization API", e);
        } catch (InterruptedException e) {
            logger.debug("InterruptedException on call to Viessmann authorization API: {}", e.getMessage());
        }
        return null;
    }
}
