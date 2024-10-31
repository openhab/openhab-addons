/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.electroluxappliances.internal.api;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesBridgeConfiguration;
import org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesException;
import org.openhab.binding.electroluxappliances.internal.dto.AirPurifierStateDTO;
import org.openhab.binding.electroluxappliances.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliances.internal.dto.ApplianceInfoDTO;
import org.openhab.binding.electroluxappliances.internal.dto.ApplianceStateDTO;
import org.openhab.binding.electroluxappliances.internal.dto.WashingMachineStateDTO;
import org.openhab.binding.electroluxappliances.internal.listener.TokenUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ElectroluxGroupAPI} class defines the Elextrolux Group API
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxGroupAPI {

    private static final String BASE_URL = "https://api.developer.electrolux.one";
    private static final String TOKEN_URL = BASE_URL + "/api/v1/token/refresh";
    private static final String APPLIANCES_URL = BASE_URL + "/api/v1/appliances";

    private static final int MAX_RETRIES = 3;
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(ElectroluxGroupAPI.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final ElectroluxAppliancesBridgeConfiguration configuration;
    private Instant tokenExpiry = Instant.MAX;
    private final TokenUpdateListener tokenUpdateListener;

    public ElectroluxGroupAPI(ElectroluxAppliancesBridgeConfiguration configuration, Gson gson, HttpClient httpClient,
            TokenUpdateListener listener) {
        this.gson = gson;
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.tokenUpdateListener = listener;
    }

    public boolean refresh(Map<String, ApplianceDTO> electroluxAppliancesThings, boolean isCommunicationError) {
        try {
            if (Instant.now().isAfter(this.tokenExpiry) || isCommunicationError) {
                logger.debug("Is communication error: {}", isCommunicationError);
                // Refresh since token is expired
                refreshToken();
            } else {
                logger.debug("Now: {} Token expiry: {}", Instant.now(), this.tokenExpiry);

            }
            // Get all appliances
            String json = getAppliances();
            ApplianceDTO[] dtos = gson.fromJson(json, ApplianceDTO[].class);
            if (dtos != null) {
                for (ApplianceDTO dto : dtos) {
                    String applianceId = dto.getApplianceId();
                    // Get appliance info
                    String jsonApplianceInfo = getApplianceInfo(applianceId);
                    ApplianceInfoDTO applianceInfo = gson.fromJson(jsonApplianceInfo, ApplianceInfoDTO.class);
                    if (applianceInfo != null) {
                        dto.setApplianceInfo(applianceInfo);
                        if ("AIR_PURIFIER".equals(applianceInfo.getApplianceInfo().getDeviceType())) {
                            // Get appliance state
                            String jsonApplianceState = getApplianceState(applianceId);
                            ApplianceStateDTO applianceState = gson.fromJson(jsonApplianceState,
                                    AirPurifierStateDTO.class);
                            if (applianceState != null) {
                                dto.setApplianceState(applianceState);
                            }
                            electroluxAppliancesThings.put(applianceInfo.getApplianceInfo().getSerialNumber(), dto);
                        } else if ("WASHING_MACHINE".equals(applianceInfo.getApplianceInfo().getDeviceType())) {
                            // Get appliance state
                            String jsonApplianceState = getApplianceState(applianceId);
                            ApplianceStateDTO applianceState = gson.fromJson(jsonApplianceState,
                                    WashingMachineStateDTO.class);
                            if (applianceState != null) {
                                dto.setApplianceState(applianceState);
                            }
                            electroluxAppliancesThings.put(applianceInfo.getApplianceInfo().getSerialNumber(), dto);
                        }
                    }
                }
                return true;
            }
        } catch (JsonSyntaxException | ElectroluxAppliancesException e) {
            logger.warn("Failed to refresh! {}", e.getMessage());
        }
        return false;
    }

    public boolean workModePowerOff(String applianceId) {
        String commandJSON = "{ \"Workmode\": \"PowerOff\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAppliancesException e) {
            logger.warn("Work mode powerOff failed {}", e.getMessage());
        }
        return false;
    }

    public boolean workModeAuto(String applianceId) {
        String commandJSON = "{ \"Workmode\": \"Auto\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAppliancesException e) {
            logger.warn("Work mode auto failed {}", e.getMessage());
        }
        return false;
    }

    public boolean workModeManual(String applianceId) {
        String commandJSON = "{ \"Workmode\": \"Manual\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAppliancesException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setFanSpeedLevel(String applianceId, int fanSpeedLevel) {
        if (fanSpeedLevel < 1 && fanSpeedLevel > 10) {
            return false;
        } else {
            String commandJSON = "{ \"Fanspeed\": " + fanSpeedLevel + "}";
            try {
                return sendCommand(commandJSON, applianceId);
            } catch (ElectroluxAppliancesException e) {
                logger.warn("Work mode manual failed {}", e.getMessage());
            }
        }
        return false;
    }

    public boolean setIonizer(String applianceId, String ionizerStatus) {
        String commandJSON = "{ \"Ionizer\": " + ionizerStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAppliancesException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setUILight(String applianceId, String uiLightStatus) {
        String commandJSON = "{ \"UILight\": " + uiLightStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAppliancesException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setSafetyLock(String applianceId, String safetyLockStatus) {
        String commandJSON = "{ \"SafetyLock\": " + safetyLockStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAppliancesException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    private Request createRequest(String uri, HttpMethod httpMethod) {
        Request request = httpClient.newRequest(uri).method(httpMethod);
        request.timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        request.header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);
        request.header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        logger.trace("HTTP Request {}.", request.toString());

        return request;
    }

    private void refreshToken() throws ElectroluxAppliancesException {
        try {
            String json = "{\"refreshToken\": \"" + this.configuration.refreshToken + "\"}";
            Request request = createRequest(TOKEN_URL, HttpMethod.POST);
            request.content(new StringContentProvider(json), MediaType.APPLICATION_JSON);
            logger.debug("HTTP POST Request {}.", request.toString());
            ContentResponse httpResponse;
            httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxAppliancesException("Failed to refresh tokens" + httpResponse.getContentAsString());
            }
            json = httpResponse.getContentAsString();
            logger.trace("Tokens: {}", json);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            this.configuration.accessToken = jsonObject.get("accessToken").getAsString();
            this.configuration.refreshToken = jsonObject.get("refreshToken").getAsString();
            // Notify the listener about the updated tokens
            tokenUpdateListener.onTokenUpdated(this.configuration.accessToken, this.configuration.refreshToken);
            long expiresIn = jsonObject.get("expiresIn").getAsLong();
            logger.debug("Token expires in: {}s", expiresIn);
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
            logger.debug("Token expires: {}", this.tokenExpiry);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ElectroluxAppliancesException(e);
        }
    }

    private String getFromApi(String uri) throws ElectroluxAppliancesException, InterruptedException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = createRequest(uri, HttpMethod.GET);
                    request.header("x-api-key", this.configuration.apiKey);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.configuration.accessToken);
                    logger.trace("Request header {}", request);

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.debug("getFromApi failed, HTTP status: {}", response.getStatus());
                        refreshToken();
                    } else {
                        return content;
                    }
                } catch (TimeoutException e) {
                    logger.debug("TimeoutException error in get: {}", e.getMessage());
                }
            }
            throw new ElectroluxAppliancesException("Failed to fetch from API!");
        } catch (JsonSyntaxException | ElectroluxAppliancesException | ExecutionException e) {
            throw new ElectroluxAppliancesException(e);
        }
    }

    private String getAppliances() throws ElectroluxAppliancesException {
        try {
            return getFromApi(APPLIANCES_URL);
        } catch (ElectroluxAppliancesException | InterruptedException e) {
            throw new ElectroluxAppliancesException(e);
        }
    }

    private String getApplianceInfo(String applianceId) throws ElectroluxAppliancesException {
        try {
            return getFromApi(APPLIANCES_URL + "/" + applianceId + "/info");
        } catch (ElectroluxAppliancesException | InterruptedException e) {
            throw new ElectroluxAppliancesException(e);
        }
    }

    private String getApplianceState(String applianceId) throws ElectroluxAppliancesException {
        try {
            return getFromApi(APPLIANCES_URL + "/" + applianceId + "/state");
        } catch (ElectroluxAppliancesException | InterruptedException e) {
            throw new ElectroluxAppliancesException(e);
        }
    }

    private boolean sendCommand(String commandJSON, String applianceId) throws ElectroluxAppliancesException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = createRequest(APPLIANCES_URL + "/" + applianceId + "/command", HttpMethod.PUT);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.configuration.accessToken);
                    request.header("x-api-key", this.configuration.apiKey);
                    request.content(new StringContentProvider(commandJSON), MediaType.APPLICATION_JSON);
                    logger.trace("Command JSON: {}", commandJSON);

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.debug("sendCommand failed, HTTP status: {}", response.getStatus());
                        refreshToken();
                    } else {
                        return true;
                    }
                } catch (TimeoutException | InterruptedException e) {
                    logger.warn("TimeoutException error in get");
                }
            }
        } catch (JsonSyntaxException | ElectroluxAppliancesException | ExecutionException e) {
            throw new ElectroluxAppliancesException(e);
        }
        return false;
    }
}
