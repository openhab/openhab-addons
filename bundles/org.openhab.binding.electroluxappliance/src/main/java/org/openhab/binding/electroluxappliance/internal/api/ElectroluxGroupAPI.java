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
package org.openhab.binding.electroluxappliance.internal.api;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBridgeConfiguration;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceException;
import org.openhab.binding.electroluxappliance.internal.dto.AirPurifierStateDTO;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceInfoDTO;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceStateDTO;
import org.openhab.binding.electroluxappliance.internal.dto.PortableAirConditionerStateDTO;
import org.openhab.binding.electroluxappliance.internal.dto.WashingMachineStateDTO;
import org.openhab.binding.electroluxappliance.internal.listener.TokenUpdateListener;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
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
    private final ElectroluxApplianceBridgeConfiguration configuration;
    private String accessToken = "";
    private Instant tokenExpiry = Instant.MAX;
    private final TokenUpdateListener tokenUpdateListener;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    public ElectroluxGroupAPI(ElectroluxApplianceBridgeConfiguration configuration, Gson gson, HttpClient httpClient,
            TokenUpdateListener listener, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        this.gson = gson;
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.tokenUpdateListener = listener;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
    }

    private String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    public boolean refresh(Map<String, ApplianceDTO> electroluxApplianceThings, boolean isCommunicationError) {
        try {
            if (Instant.now().isAfter(this.tokenExpiry) || isCommunicationError) {
                logger.debug("Is communication error: {}", isCommunicationError);
                // Refresh since token has expired
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
                    Instant retrievalTs = Instant.now();
                    ApplianceInfoDTO applianceInfo = gson.fromJson(jsonApplianceInfo, ApplianceInfoDTO.class);
                    if (applianceInfo != null) {
                        dto.setApplianceInfo(applianceInfo);
                        if ("AIR_PURIFIER".equals(applianceInfo.getApplianceInfo().getDeviceType())) {
                            // Get appliance state
                            String jsonApplianceState = getApplianceState(applianceId);
                            ApplianceStateDTO applianceState = gson.fromJson(jsonApplianceState,
                                    AirPurifierStateDTO.class);
                            if (applianceState != null) {
                                dto.setApplianceState(applianceState, retrievalTs);
                            }
                            electroluxApplianceThings.put(applianceInfo.getApplianceInfo().getSerialNumber(), dto);
                        } else if ("WASHING_MACHINE".equals(applianceInfo.getApplianceInfo().getDeviceType())) {
                            // Get appliance state
                            String jsonApplianceState = getApplianceState(applianceId);
                            ApplianceStateDTO applianceState = gson.fromJson(jsonApplianceState,
                                    WashingMachineStateDTO.class);
                            if (applianceState != null) {
                                dto.setApplianceState(applianceState, retrievalTs);
                            }
                            electroluxApplianceThings.put(applianceInfo.getApplianceInfo().getSerialNumber(), dto);
                        } else if ("PORTABLE_AIR_CONDITIONER"
                                .equals(applianceInfo.getApplianceInfo().getDeviceType())) {
                            String jsonApplianceState = getApplianceState(applianceId);
                            ApplianceStateDTO applianceState = gson.fromJson(jsonApplianceState,
                                    PortableAirConditionerStateDTO.class);
                            if (applianceState != null) {
                                dto.setApplianceState(applianceState, retrievalTs);
                            }
                            electroluxApplianceThings.put(applianceInfo.getApplianceInfo().getSerialNumber(), dto);
                        }
                    }
                }
                return true;
            }
        } catch (JsonSyntaxException | ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("error.electroluxappliance.api.failed-to-refresh", e.getMessage()));
        }
        return false;
    }

    public boolean workModePowerOff(String applianceId) {
        String commandJSON = "{ \"Workmode\": \"PowerOff\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}",
                    getLocalizedText("warning.electroluxappliance.work-mode-poweroff-failed", e.getMessage()));
        }
        return false;
    }

    public boolean workModeAuto(String applianceId) {
        String commandJSON = "{ \"Workmode\": \"Auto\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.work-mode-auto-failed", e.getMessage()));
        }
        return false;
    }

    public boolean workModeManual(String applianceId) {
        String commandJSON = "{ \"Workmode\": \"Manual\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.work-mode-manual-failed", e.getMessage()));
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
            } catch (ElectroluxApplianceException e) {
                logger.warn("{}", getLocalizedText("warning.electroluxappliance.fanspeed-failed", e.getMessage()));
            }
        }
        return false;
    }

    public boolean setIonizer(String applianceId, String ionizerStatus) {
        String commandJSON = "{ \"Ionizer\": " + ionizerStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.ionizer-failed", e.getMessage()));
        }
        return false;
    }

    public boolean setUILight(String applianceId, String uiLightStatus) {
        String commandJSON = "{ \"UILight\": " + uiLightStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.uilight-failed", e.getMessage()));
        }
        return false;
    }

    public boolean setSafetyLock(String applianceId, String safetyLockStatus) {
        String commandJSON = "{ \"SafetyLock\": " + safetyLockStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.safetylock-failed", e.getMessage()));
        }
        return false;
    }

    public boolean sendCapabilityRequest(final String applianceId, final String capbilityNmae,
            final @Nullable ApplianceDTO dto, final String value) {
        if (dto == null) {
            logger.warn("{}", getLocalizedText("error.electroluxappliance.api.capability.unknown-dto", capbilityNmae));
            return false;
        }

        final ApplianceInfoDTO.@Nullable Capability capability = dto.getApplianceInfo().getCapability(capbilityNmae);
        if (capability == null) {
            logger.warn("{}",
                    getLocalizedText("error.electroluxappliance.api.capability.capability-unknown", capbilityNmae));
            return false;
        }

        // If the capability on the device does not support readwrite then we cant set it
        if (!capability.getAccess().equals("readwrite")) {
            logger.warn("{}",
                    getLocalizedText("error.electroluxappliance.api.capability.no-read-write", capbilityNmae));
            return false;
        }

        String payload = "";

        // Determine the capability type for further processing
        switch (capability.getType()) {
            case "string":
                // If it's a string typically there is a range of values allowed.
                String command = value;
                if (!capability.getValuesContains(command)) {
                    // try forcing uppercase as all commands are for the PAC units
                    command = command.toUpperCase();
                    if (!capability.getValuesContains(command)) {
                        logger.warn("{}", getLocalizedText("error.electroluxappliance.api.capability.not-in-values",
                                capbilityNmae, value));
                        return false;
                    }
                }
                payload = "{ \"" + capbilityNmae + "\": \"" + command + "\"}";
                break;
            case "temperature":
            case "number":
                int valNum = Integer.MIN_VALUE;
                try {
                    valNum = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    logger.warn("{}", getLocalizedText("error.electroluxappliance.api.capability.not-expected-numeric",
                            capbilityNmae, value));
                    return false;
                }
                if (capability.getIsReadMin() && capability.getMin() > valNum) {
                    logger.warn("{}", getLocalizedText("error.electroluxappliance.api.capability.numeric-below-min",
                            capbilityNmae, value, capability.getMin()));
                    return false;
                }
                if (capability.getIsReadMax() && valNum > capability.getMax()) {
                    logger.warn("{}", getLocalizedText("error.electroluxappliance.api.capability.numeric-above-max",
                            capbilityNmae, value, capability.getMax()));
                    return false;
                }

                // If step is defined ensure the transmitted value is rounded to the nearest step
                if (capability.getIsReadStep()) {
                    int remainder = valNum % capability.getStep();
                    if (remainder != 0) {
                        valNum = Math.round((float) valNum / capability.getStep()) * capability.getStep();
                    }
                }

                if (!capability.getValuesContains(String.valueOf(valNum))) {
                    logger.warn("{}",
                            getLocalizedText(
                                    "error.electroluxappliance.api.capability.numeric-after-step-not-in-values",
                                    capbilityNmae, value, valNum));
                    return false;
                }

                payload = "{ \"" + capbilityNmae + "\": " + valNum + " }";

                break;
            case "boolean":
                final String boolCommand = value.toLowerCase();
                if (!"true".equals(boolCommand) && !"false".equals(boolCommand)) {
                    logger.warn("{}", getLocalizedText("error.electroluxappliance.api.capability.invalid-boolean-value",
                            capbilityNmae, value));
                }
                payload = "{ \"" + capbilityNmae + "\": " + boolCommand + " }";
                break;
        }

        try {
            return sendCommand(payload, applianceId);
        } catch (ElectroluxApplianceException e) {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.failed-capability-send", capbilityNmae,
                    e.getMessage()));
            return false;
        }
    }

    private Request createRequest(String uri, HttpMethod httpMethod) {
        Request request = httpClient.newRequest(uri).method(httpMethod);
        request.timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        request.header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON);
        request.header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        logger.trace("HTTP Request {}.", request.toString());

        return request;
    }

    private void refreshToken() throws ElectroluxApplianceException {
        try {
            String json = "{\"refreshToken\": \"" + this.configuration.refreshToken + "\"}";
            Request request = createRequest(TOKEN_URL, HttpMethod.POST);
            request.content(new StringContentProvider(json), MediaType.APPLICATION_JSON);
            logger.debug("HTTP POST Request {}.", request.toString());
            ContentResponse httpResponse;
            httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxApplianceException("Failed to refresh tokens" + httpResponse.getContentAsString());
            }
            json = httpResponse.getContentAsString();
            logger.trace("Tokens: {}", json);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            this.accessToken = jsonObject.get("accessToken").getAsString();
            this.configuration.refreshToken = jsonObject.get("refreshToken").getAsString();
            // Notify the listener about the updated tokens
            tokenUpdateListener.onTokenUpdated(this.configuration.refreshToken);
            long expiresIn = jsonObject.get("expiresIn").getAsLong();
            logger.debug("Token expires in: {}s", expiresIn);
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
            logger.debug("Token expires: {}", this.tokenExpiry);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ElectroluxApplianceException(e);
        }
    }

    private String getFromApi(String uri) throws ElectroluxApplianceException, InterruptedException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = createRequest(uri, HttpMethod.GET);
                    request.header("x-api-key", this.configuration.apiKey);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.accessToken);
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
            throw new ElectroluxApplianceException("Failed to fetch from API!");
        } catch (JsonSyntaxException | ElectroluxApplianceException | ExecutionException e) {
            throw new ElectroluxApplianceException(e);
        }
    }

    private String getAppliances() throws ElectroluxApplianceException {
        try {
            return getFromApi(APPLIANCES_URL);
        } catch (ElectroluxApplianceException | InterruptedException e) {
            throw new ElectroluxApplianceException(e);
        }
    }

    private String getApplianceInfo(String applianceId) throws ElectroluxApplianceException {
        try {
            return getFromApi(APPLIANCES_URL + "/" + applianceId + "/info");
        } catch (ElectroluxApplianceException | InterruptedException e) {
            throw new ElectroluxApplianceException(e);
        }
    }

    private String getApplianceState(String applianceId) throws ElectroluxApplianceException {
        try {
            return getFromApi(APPLIANCES_URL + "/" + applianceId + "/state");
        } catch (ElectroluxApplianceException | InterruptedException e) {
            throw new ElectroluxApplianceException(e);
        }
    }

    private boolean sendCommand(String commandJSON, String applianceId) throws ElectroluxApplianceException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = createRequest(APPLIANCES_URL + "/" + applianceId + "/command", HttpMethod.PUT);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.accessToken);
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
                    logger.warn("{}", getLocalizedText("error.electroluxappliance.api.get-timeout"));
                }
            }
        } catch (JsonSyntaxException | ElectroluxApplianceException | ExecutionException e) {
            throw new ElectroluxApplianceException(e);
        }
        return false;
    }
}
