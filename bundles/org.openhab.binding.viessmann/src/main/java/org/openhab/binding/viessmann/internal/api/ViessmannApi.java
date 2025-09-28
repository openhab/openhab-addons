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
package org.openhab.binding.viessmann.internal.api;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.viessmann.internal.dto.device.DeviceDTO;
import org.openhab.binding.viessmann.internal.dto.error.ViErrorDTO;
import org.openhab.binding.viessmann.internal.dto.events.EventsDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeaturesDTO;
import org.openhab.binding.viessmann.internal.dto.installation.Data;
import org.openhab.binding.viessmann.internal.dto.installation.Gateway;
import org.openhab.binding.viessmann.internal.dto.installation.InstallationDTO;
import org.openhab.binding.viessmann.internal.dto.oauth.TokenResponseDTO;
import org.openhab.binding.viessmann.internal.handler.ViessmannBridgeHandler;
import org.openhab.binding.viessmann.internal.interfaces.ApiInterface;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ViessmannApi} is responsible for managing all communication with
 * the Viessmann API service.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class ViessmannApi {

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String PARAM_VI_ERROR_ID = "viErrorId";

    private final Logger logger = LoggerFactory.getLogger(ViessmannApi.class);

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private static final int TOKEN_MIN_DIFF_MS = 120000;

    public final Properties httpHeaders;

    private final HttpClient httpClient;
    private final @Nullable String callbackUrl;

    private final String apiKey;
    private final String user;
    private final String password;

    private String installationId;
    private String gatewaySerial;

    private long tokenExpiryDate;
    private long refreshTokenExpiryDate;

    private @NonNullByDefault({}) ViessmannAuth viessmannAuth;

    public ViessmannApi(final String apiKey, HttpClient httpClient, String user, String password, String installationId,
            String gatewaySerial, @Nullable String callbackUrl) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.user = user;
        this.password = password;
        this.installationId = installationId;
        this.gatewaySerial = gatewaySerial;
        this.callbackUrl = callbackUrl;
        tokenResponse = null;
        httpHeaders = new Properties();
        httpHeaders.put("User-Agent", "openhab-viessmann-api/2.0");
    }

    public Gson getGson() {
        return GSON;
    }

    private @Nullable TokenResponseDTO tokenResponse;

    public void setTokenResponseDTO(TokenResponseDTO newTokenResponse) {
        tokenResponse = newTokenResponse;
    }

    public @Nullable TokenResponseDTO getTokenResponseDTO() throws ViessmannAuthException {
        return tokenResponse;
    }

    public void setTokenExpiryDate(long expiresIn) {
        tokenExpiryDate = System.currentTimeMillis() + expiresIn;
    }

    public long getTokenExpiryDate() {
        return tokenExpiryDate;
    }

    public void setRefreshTokenExpiryDate(long expiresIn) {
        refreshTokenExpiryDate = System.currentTimeMillis() + expiresIn;
    }

    public long getRefreshTokenExpiryDate() {
        return refreshTokenExpiryDate;
    }

    public void createOAuthClientService(ApiInterface handler) {
        String bridgeUID = handler.getThingUIDasString();
        logger.debug("API: Creating OAuth Client Service for {}", bridgeUID);
        viessmannAuth = new ViessmannAuth(this, handler, apiKey, httpClient, user, password, callbackUrl);
    }

    public boolean doAuthorize() {
        return authorize();
    }

    /**
     * Check to see if the Viessmann authorization process is complete. This will be determined
     * by requesting an AccessTokenResponse from the API. If we get a valid
     * response, then assume that the Viessmann authorization process is complete. Otherwise,
     * start the Viessmann authorization process.
     */
    private boolean authorize() {
        try {
            TokenResponseDTO localAccessTokenResponseDTO = getTokenResponseDTO();
            if (localAccessTokenResponseDTO != null) {
                if (localAccessTokenResponseDTO.accessToken != null) {
                    logger.trace("API: Got AccessTokenResponse from OAuth service: {}", localAccessTokenResponseDTO);
                    logger.debug("Checking if new access token is needed...");
                    long difference = getTokenExpiryDate() - System.currentTimeMillis();
                    if (difference <= TOKEN_MIN_DIFF_MS) {
                        viessmannAuth.setState(ViessmannAuthState.NEED_REFRESH_TOKEN);
                        viessmannAuth.setRefreshToken(localAccessTokenResponseDTO.refreshToken);
                    } else {
                        viessmannAuth.setState(ViessmannAuthState.COMPLETE);
                    }
                } else {
                    logger.debug("API: Didn't get an AccessTokenResponse from OAuth service");
                    if (viessmannAuth.isComplete()) {
                        viessmannAuth.setState(ViessmannAuthState.NEED_AUTH);
                    }
                }
            }
            if (ViessmannAuthState.COMPLETE.equals(viessmannAuth.doAuthorization())) {
                return true;
            }
        } catch (ViessmannAuthException e) {
            if (logger.isDebugEnabled()) {
                logger.info("API: The Viessmann authorization process threw an exception", e);
            } else {
                logger.info("API: The Viessmann authorization process threw an exception: {}", e.getMessage());
            }
            viessmannAuth.setState(ViessmannAuthState.NEED_AUTH);
        }
        return false;
    }

    public void checkExpiringToken() {
        logger.debug("Checking if new access token is expired...");
        TokenResponseDTO localAccessTokenResponseDTO;
        try {
            localAccessTokenResponseDTO = getTokenResponseDTO();
            if (localAccessTokenResponseDTO != null) {
                long diffRefreshTokenExpire = getRefreshTokenExpiryDate() - System.currentTimeMillis();
                if (diffRefreshTokenExpire <= TOKEN_MIN_DIFF_MS) {
                    viessmannAuth.setState(ViessmannAuthState.NEED_AUTH);
                } else {
                    long difference = getTokenExpiryDate() - System.currentTimeMillis();
                    if (difference <= TOKEN_MIN_DIFF_MS) {
                        viessmannAuth.setState(ViessmannAuthState.NEED_REFRESH_TOKEN);
                        viessmannAuth.setRefreshToken(localAccessTokenResponseDTO.refreshToken);
                    } else {
                        viessmannAuth.setState(ViessmannAuthState.COMPLETE);
                    }
                }
            }
            viessmannAuth.doAuthorization();
        } catch (ViessmannAuthException e) {
            if (logger.isDebugEnabled()) {
                logger.info("API: The Viessmann authorization process threw an exception", e);
            } else {
                logger.info("API: The Viessmann authorization process threw an exception: {}", e.getMessage());
            }
            viessmannAuth.setState(ViessmannAuthState.NEED_AUTH);
        }
    }

    public @Nullable DeviceDTO getAllDevices(ApiInterface interfaceHandler) throws ViessmannCommunicationException {
        String response = executeGet(interfaceHandler, VIESSMANN_BASE_URL + "iot/v2/equipment/installations/"
                + installationId + "/gateways/" + gatewaySerial + "/devices");
        return GSON.fromJson(response, DeviceDTO.class);
    }

    public @Nullable DeviceDTO getAllDevices(ApiInterface interfaceHandler, String installationId, String gatewaySerial)
            throws ViessmannCommunicationException {
        String response = executeGet(interfaceHandler, VIESSMANN_BASE_URL + "iot/v2/equipment/installations/"
                + installationId + "/gateways/" + gatewaySerial + "/devices");
        return GSON.fromJson(response, DeviceDTO.class);
    }

    public @Nullable FeaturesDTO getAllFeatures(ApiInterface interfaceHandler, String deviceId)
            throws ViessmannCommunicationException {
        String response = executeGet(interfaceHandler, VIESSMANN_BASE_URL + "iot/v2/features/installations/"
                + installationId + "/gateways/" + gatewaySerial + "/devices/" + deviceId + "/features/");
        if (response != null) {
            response = response.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll(" ", "");
            response = response.replace("enum", "enumValue");
            int i = response.indexOf("\"entries\":{\"type\":\"array\",\"value\"");
            while (i > 0) {
                response = response.substring(0, i) + "\"errorEntries\"" + response.substring(i + 9, response.length());
                i = response.indexOf("\"entries\":{\"type\":\"array\",\"value\"");
            }
            return GSON.fromJson(response, FeaturesDTO.class);
        }
        return null;
    }

    public @Nullable FeaturesDTO getAllFeatures(ApiInterface interfaceHandler, String deviceId, String installationId,
            String gatewaySerial) throws ViessmannCommunicationException {
        String response = executeGet(interfaceHandler, VIESSMANN_BASE_URL + "iot/v2/features/installations/"
                + installationId + "/gateways/" + gatewaySerial + "/devices/" + deviceId + "/features/");
        if (response != null) {
            response = response.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll(" ", "");
            response = response.replace("enum", "enumValue");
            int i = response.indexOf("\"entries\":{\"type\":\"array\",\"value\"");
            while (i > 0) {
                response = response.substring(0, i) + "\"errorEntries\"" + response.substring(i + 9, response.length());
                i = response.indexOf("\"entries\":{\"type\":\"array\",\"value\"");
            }
            return GSON.fromJson(response, FeaturesDTO.class);
        }
        return null;
    }

    public @Nullable EventsDTO getSelectedEvents(ApiInterface interfaceHandler, String eventType)
            throws ViessmannCommunicationException {
        String response = executeGet(interfaceHandler, VIESSMANN_BASE_URL + "iot/v2/events-history/installations/"
                + installationId + "/events?eventType=" + eventType);
        return GSON.fromJson(response, EventsDTO.class);
    }

    public @Nullable EventsDTO getSelectedEvents(ApiInterface interfaceHandler, String eventType, String installationId,
            String gatewaySerial) throws ViessmannCommunicationException {
        String response = executeGet(interfaceHandler, VIESSMANN_BASE_URL + "iot/v2/events-history/installations/"
                + installationId + "/events?eventType=" + eventType);
        return GSON.fromJson(response, EventsDTO.class);
    }

    public void setInstallationAndGatewayId(ApiInterface interfaceHandler) {
        try {
            String response = executeGet(interfaceHandler,
                    VIESSMANN_BASE_URL + "iot/v2/equipment/installations?includeGateways=true");
            InstallationDTO installation = GSON.fromJson(response, InstallationDTO.class);
            if (installation != null) {
                List<Data> listData = installation.data;
                Data data = listData.get(0);
                List<Gateway> listGateway = data.gateways;
                String gatewaySerial = listGateway.get(0).serial;
                for (Gateway gateway : listGateway) {
                    if (!"Lancard".equals(gateway.gatewayType)) {
                        gatewaySerial = gateway.serial;
                        break;
                    }
                }

                this.installationId = data.id.toString();
                this.gatewaySerial = gatewaySerial;
                if (interfaceHandler instanceof ViessmannBridgeHandler) {
                    interfaceHandler.setInstallationGatewayId(data.id.toString(), gatewaySerial);
                }
            }
        } catch (ViessmannCommunicationException | JsonSyntaxException | IllegalStateException e) {
            // should not happen
        }
    }

    public @Nullable InstallationDTO getInstallationsAndGateways(ApiInterface interfaceHandler) {
        logger.debug("[Requesting Installations and Gateway]");
        InstallationDTO installation = null;
        try {
            String response = executeGet(interfaceHandler,
                    VIESSMANN_BASE_URL + "iot/v2/equipment/installations?includeGateways=true");
            installation = GSON.fromJson(response, InstallationDTO.class);

            if (installation != null) {
                List<Data> listData = installation.data;
                Data data = listData.get(0);
                List<Gateway> listGateway = data.gateways;
                String gatewaySerial = listGateway.get(0).serial;
                for (Gateway gateway : listGateway) {
                    if (!"Lancard".equals(gateway.gatewayType)) {
                        gatewaySerial = gateway.serial;
                        break;
                    }
                }
                this.installationId = data.id.toString();
                this.gatewaySerial = gatewaySerial;
            }
        } catch (ViessmannCommunicationException | JsonSyntaxException | IllegalStateException e) {
            // should not happen
        }
        return installation;
    }

    public boolean setData(ApiInterface interfaceHandler, String url, String json)
            throws ViessmannCommunicationException {
        return executePost(interfaceHandler, url, json);
    }

    private @Nullable String executeGet(ApiInterface interfaceHandler, String url)
            throws ViessmannCommunicationException {
        String response = null;
        try {
            logger.trace("API: GET Request URL is '{}'", url);
            long startTime = System.currentTimeMillis();
            response = HttpUtil.executeUrl(HTTP_METHOD_GET, url, setHeaders(), null, null, API_TIMEOUT_MS);
            logger.trace("API: Response took {} msec: {}", System.currentTimeMillis() - startTime, response);
            if (response.contains(PARAM_VI_ERROR_ID)) {
                handleViError(interfaceHandler, response);
                return null;
            }
        } catch (IOException e) {
            logger.info("API IOException: Unable to execute GET: {}", e.getMessage());
        } catch (ViessmannAuthException e) {
            logger.info("API AuthException: Unable to execute GET: {}", e.getMessage());
            authorize();
        }
        return response;
    }

    private boolean executePost(ApiInterface interfaceHandler, String url, String json)
            throws ViessmannCommunicationException {
        try {
            logger.trace("API: POST Request URL is '{}', JSON is '{}'", url, json);
            long startTime = System.currentTimeMillis();
            String response = HttpUtil.executeUrl(HTTP_METHOD_POST, url, setHeaders(),
                    new ByteArrayInputStream(json.getBytes()), CONTENT_TYPE_APPLICATION_JSON, API_TIMEOUT_MS);
            logger.trace("API: Response took {} msec: {}", System.currentTimeMillis() - startTime, response);
            if (response.contains(PARAM_VI_ERROR_ID)) {
                handleViError(interfaceHandler, response);
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.info("API IOException: Unable to execute POST: {}", e.getMessage());
        } catch (ViessmannAuthException e) {
            logger.info("API AuthException: Unable to execute POST: {}", e.getMessage());
            authorize();
        }
        return false;
    }

    private Properties setHeaders() throws ViessmannAuthException {
        TokenResponseDTO atr = getTokenResponseDTO();

        if (atr == null) {
            throw new ViessmannAuthException("Can not set auth header because access token is null");
        }
        if (atr.accessToken == null) {
            throw new ViessmannAuthException("Can not set auth header because access token is null");
        }
        Properties headers = new Properties();
        headers.putAll(httpHeaders);
        headers.put("Authorization", "Bearer " + atr.accessToken);
        return headers;
    }

    private void handleViError(ApiInterface interfaceHandler, String response) throws ViessmannCommunicationException {
        ViErrorDTO viError = GSON.fromJson(response, ViErrorDTO.class);
        if (viError != null) {
            if ("INTERNAL_SERVER_ERROR".equals(viError.getErrorType())) {
                logger.debug("ViError: {} | Device not reachable INTERNAL_SERVER_ERROR", viError.getMessage());
                throw new ViessmannCommunicationException("INTERNAL_SERVER_ERROR");
            } else if ("PACKAGE_NOT_PAID_FOR".equals(viError.getErrorType())) {
                logger.warn("ViError: User does not have access to given feature.");
            } else {
                switch (viError.getStatusCode()) {
                    case HttpStatus.TOO_MANY_REQUESTS_429:
                        logger.warn("ViError: {} | Resetting Limit at {}", viError.getMessage(),
                                viError.getExtendedPayload().getLimitResetDateTime());
                        interfaceHandler.updateBridgeStatusExtended(ThingStatus.OFFLINE,
                                ThingStatusDetail.COMMUNICATION_ERROR,
                                String.format("API Call limit reached. Reset at %s",
                                        viError.getExtendedPayload().getLimitResetDateTime()));
                        interfaceHandler.waitForApiCallLimitReset(viError.getExtendedPayload().getLimitReset());

                        break;
                    case HttpStatus.BAD_GATEWAY_502:
                        logger.debug("ViError: {} | Device not reachable", viError.getMessage());
                        throw new ViessmannCommunicationException(viError.getMessage());
                    case HttpStatus.BAD_REQUEST_400:
                        logger.debug("ViError: {} | Gateway offline", viError.getExtendedPayload().getReason());
                        throw new ViessmannCommunicationException(viError.getExtendedPayload().getReason());
                    default:
                        logger.error("ViError: {} | StatusCode: {} | Reason: {}", viError.getMessage(),
                                viError.getStatusCode(), viError.getExtendedPayload().getReason());
                        break;
                }
            }
        }
    }
}
