/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.api;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ecobee.internal.dto.oauth.AuthorizeResponseDTO;
import org.openhab.binding.ecobee.internal.dto.oauth.TokenResponseDTO;
import org.openhab.binding.ecobee.internal.handler.EcobeeAccountBridgeHandler;
import org.openhab.binding.ecobee.internal.util.ExceptionUtils;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link EcobeeAuth} performs the initial OAuth authorization
 * with the Ecobee authorization servers. Once this process is complete, the
 * AccessTokenResponse will be imported into the OHC OAuth Client Service. At
 * that point, the OHC OAuth service will be responsible for refreshing tokens.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeAuth {

    private final Logger logger = LoggerFactory.getLogger(EcobeeAuth.class);

    private final EcobeeAccountBridgeHandler bridgeHandler;
    private final String apiKey;
    private final int apiTimeout;
    private final OAuthClientService oAuthClientService;
    private final HttpClient httpClient;

    private EcobeeAuthState state;

    private @Nullable AuthorizeResponseDTO authResponse;

    private long pinExpirationTime;

    /**
     * The authorization code needed to make the first-time request
     * of the refresh and access tokens. Obtained from the call to {@code authorize()}.
     */
    private @Nullable String code;

    public EcobeeAuth(EcobeeAccountBridgeHandler bridgeHandler, String apiKey, int apiTimeout,
            OAuthClientService oAuthClientService, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.apiTimeout = apiTimeout;
        this.oAuthClientService = oAuthClientService;
        this.httpClient = httpClient;
        this.bridgeHandler = bridgeHandler;
        pinExpirationTime = 0;
        state = EcobeeAuthState.NEED_PIN;
        authResponse = null;
    }

    public void setState(EcobeeAuthState newState) {
        if (newState != state) {
            logger.debug("EcobeeAuth: Change state from {} to {}", state, newState);
            state = newState;
        }
    }

    public boolean isComplete() {
        return state == EcobeeAuthState.COMPLETE;
    }

    public EcobeeAuthState doAuthorization() throws EcobeeAuthException {
        switch (state) {
            case NEED_PIN:
                authorize();
                break;
            case NEED_TOKEN:
                getTokens();
                break;
            case COMPLETE:
                bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
                break;
        }
        return state;
    }

    /**
     * Call the Ecobee authorize endpoint to get the authorization code and PIN
     * that will be used a) validate the application in the the Ecobee user web portal,
     * and b) make the first time request for the access and refresh tokens.
     * Warnings are suppressed to avoid the Gson.fromJson warnings.
     */
    @SuppressWarnings({ "null", "unused" })
    private void authorize() throws EcobeeAuthException {
        logger.debug("EcobeeAuth: State is {}: Executing step: 'authorize'", state);
        StringBuilder url = new StringBuilder(ECOBEE_AUTHORIZE_URL);
        url.append("?response_type=ecobeePin");
        url.append("&client_id=").append(apiKey);
        url.append("&scope=").append(ECOBEE_SCOPE);

        logger.trace("EcobeeAuth: Getting authorize URL={}", url);
        String response = executeUrl("GET", url.toString());
        logger.trace("EcobeeAuth: Auth response: {}", response);

        try {
            authResponse = EcobeeApi.getGson().fromJson(response, AuthorizeResponseDTO.class);
            if (authResponse == null) {
                logger.debug("EcobeeAuth: Got null authorize response from Ecobee API");
                setState(EcobeeAuthState.NEED_PIN);
            } else {
                String error = authResponse.error;
                if (error != null && !error.isEmpty()) {
                    throw new EcobeeAuthException(error + ": " + authResponse.errorDescription);
                }
                code = authResponse.code;
                writeLogMessage(authResponse.pin, authResponse.expiresIn);
                setPinExpirationTime(authResponse.expiresIn.longValue());
                updateBridgeStatus();
                setState(EcobeeAuthState.NEED_TOKEN);
            }
        } catch (JsonSyntaxException e) {
            logger.info("EcobeeAuth: Exception while parsing authorize response: {}", e.getMessage());
            setState(EcobeeAuthState.NEED_PIN);
        }
    }

    /**
     * Call the Ecobee token endpoint to get the access and refresh tokens. Once successfully retrieved,
     * the access and refresh tokens will be injected into the OHC OAuth service.
     * Warnings are suppressed to avoid the Gson.fromJson warnings.
     */
    @SuppressWarnings({ "null", "unused" })
    private void getTokens() throws EcobeeAuthException {
        logger.debug("EcobeeAuth: State is {}: Executing step: 'getToken'", state);
        StringBuilder url = new StringBuilder(ECOBEE_TOKEN_URL);
        url.append("?grant_type=ecobeePin");
        url.append("&code=").append(code);
        url.append("&client_id=").append(apiKey);

        logger.trace("EcobeeAuth: Posting token URL={}", url);
        String response = executeUrl("POST", url.toString());
        logger.trace("EcobeeAuth: Got a valid token response: {}", response);

        TokenResponseDTO tokenResponse = EcobeeApi.getGson().fromJson(response, TokenResponseDTO.class);
        if (tokenResponse == null) {
            logger.debug("EcobeeAuth: Got null token response from Ecobee API");
            updateBridgeStatus();
            setState(isPinExpired() ? EcobeeAuthState.NEED_PIN : EcobeeAuthState.NEED_TOKEN);
            return;
        }
        String error = tokenResponse.error;
        if (error != null && !error.isEmpty()) {
            throw new EcobeeAuthException(error + ": " + tokenResponse.errorDescription);
        }
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setRefreshToken(tokenResponse.refreshToken);
        accessTokenResponse.setAccessToken(tokenResponse.accessToken);
        accessTokenResponse.setScope(tokenResponse.scope);
        accessTokenResponse.setTokenType(tokenResponse.tokenType);
        accessTokenResponse.setExpiresIn(tokenResponse.expiresIn);
        try {
            logger.debug("EcobeeAuth: Importing AccessTokenResponse into oAuthClientService!!!");
            oAuthClientService.importAccessTokenResponse(accessTokenResponse);
            bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
            setState(EcobeeAuthState.COMPLETE);
            return;
        } catch (OAuthException e) {
            logger.info("EcobeeAuth: Got OAuthException", e);
            // No other processing needed here
        }
        updateBridgeStatus();
        setState(isPinExpired() ? EcobeeAuthState.NEED_PIN : EcobeeAuthState.NEED_TOKEN);
    }

    private void writeLogMessage(String pin, Integer expiresIn) {
        logger.info("#################################################################");
        logger.info("# Ecobee: U S E R   I N T E R A C T I O N   R E Q U I R E D !!");
        logger.info("# Go to the Ecobee web portal, then:");
        logger.info("# Enter PIN '{}' in My Apps within {} minutes.", pin, expiresIn);
        logger.info("# NOTE: All API attempts will fail in the meantime.");
        logger.info("#################################################################");
    }

    private void updateBridgeStatus() {
        AuthorizeResponseDTO response = authResponse;
        if (response != null) {
            bridgeHandler.updateBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    String.format("Enter PIN '%s' in MyApps. PIN expires in %d minutes", response.pin,
                            getMinutesUntilPinExpiration()));
        }
    }

    private void setPinExpirationTime(long expiresIn) {
        pinExpirationTime = expiresIn + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
    }

    private long getMinutesUntilPinExpiration() {
        return pinExpirationTime - TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
    }

    private boolean isPinExpired() {
        return getMinutesUntilPinExpiration() <= 0;
    }

    private @Nullable String executeUrl(String method, String url) {
        Request request = httpClient.newRequest(url);
        request.timeout(apiTimeout, TimeUnit.MILLISECONDS);
        request.method(method);
        EcobeeApi.HTTP_HEADERS.forEach((k, v) -> request.header((String) k, (String) v));

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
                    logger.debug("HTTP {} failed: {}, {}", method, contentResponse.getStatus(),
                            contentResponse.getReason());
                    break;
            }
        } catch (TimeoutException e) {
            logger.debug("TimeoutException: Call to Ecobee API timed out");
        } catch (ExecutionException e) {
            if (ExceptionUtils.getRootThrowable(e) instanceof HttpResponseException) {
                logger.info("Awaiting entry of PIN in Ecobee portal. Expires in {} minutes",
                        getMinutesUntilPinExpiration());
            } else {
                logger.debug("ExecutionException on call to Ecobee authorization API", e);
            }
        } catch (InterruptedException e) {
            logger.debug("InterruptedException on call to Ecobee authorization API: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
