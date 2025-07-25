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
package org.openhab.binding.homeconnect.internal.client;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;
import static org.openhab.binding.homeconnect.internal.client.HttpHelper.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.ApiRequest;
import org.openhab.binding.homeconnect.internal.client.model.AvailableProgram;
import org.openhab.binding.homeconnect.internal.client.model.AvailableProgramOption;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.client.model.HomeAppliance;
import org.openhab.binding.homeconnect.internal.client.model.HomeConnectRequest;
import org.openhab.binding.homeconnect.internal.client.model.HomeConnectResponse;
import org.openhab.binding.homeconnect.internal.client.model.Option;
import org.openhab.binding.homeconnect.internal.client.model.PowerStateAccess;
import org.openhab.binding.homeconnect.internal.client.model.Program;
import org.openhab.binding.homeconnect.internal.configuration.ApiBridgeConfiguration;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Client for Home Connect API.
 *
 * @author Jonas Brüstel - Initial contribution
 * @author Laurent Garnier - Replace okhttp by the Jetty HTTP client provided by the openHAB core framework
 *
 */
@NonNullByDefault
public class HomeConnectApiClient {
    private static final String BSH_JSON_V1 = "application/vnd.bsh.sdk.v1+json";
    private static final String BASE = "/api/homeappliances";
    private static final String BASE_PATH = BASE + "/";
    private static final int REQUEST_TIMEOUT_SEC = 30;
    private static final int VALUE_TYPE_STRING = 0;
    private static final int VALUE_TYPE_INT = 1;
    private static final int VALUE_TYPE_BOOLEAN = 2;
    private static final int COMMUNICATION_QUEUE_SIZE = 50;

    private final Logger logger = LoggerFactory.getLogger(HomeConnectApiClient.class);
    private final HttpClient client;
    private final String apiUrl;
    private final OAuthClientService oAuthClientService;
    private final CircularQueue<ApiRequest> communicationQueue;
    private final ApiBridgeConfiguration apiBridgeConfiguration;

    public HomeConnectApiClient(HttpClient httpClient, OAuthClientService oAuthClientService, boolean simulated,
            @Nullable List<ApiRequest> apiRequestHistory, ApiBridgeConfiguration apiBridgeConfiguration) {
        this.client = httpClient;
        this.oAuthClientService = oAuthClientService;
        this.apiBridgeConfiguration = apiBridgeConfiguration;

        apiUrl = simulated ? API_SIMULATOR_BASE_URL : API_BASE_URL;
        communicationQueue = new CircularQueue<>(COMMUNICATION_QUEUE_SIZE);
        if (apiRequestHistory != null) {
            communicationQueue.addAll(apiRequestHistory);
        }
    }

    /**
     * Get all home appliances
     *
     * @return list of {@link HomeAppliance}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     */
    public List<HomeAppliance> getHomeAppliances() throws CommunicationException, AuthorizationException {
        Request request = createRequest(HttpMethod.GET, BASE);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.OK_200, request, response, null, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(null, request, null, response, responseBody);

            return mapToHomeAppliances(responseBody);
        } catch (InterruptedException | TimeoutException | ExecutionException | ApplianceOfflineException e) {
            logger.warn("Failed to fetch home appliances! error={}", e.getMessage());
            trackAndLogApiRequest(null, request, null, null, null);
            throw new CommunicationException(e);
        }
    }

    /**
     * Get home appliance by id
     *
     * @param haId home appliance id
     * @return {@link HomeAppliance}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     */
    public HomeAppliance getHomeAppliance(String haId) throws CommunicationException, AuthorizationException {
        Request request = createRequest(HttpMethod.GET, BASE_PATH + haId);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.OK_200, request, response, haId, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, null, response, responseBody);

            return mapToHomeAppliance(responseBody);
        } catch (InterruptedException | TimeoutException | ExecutionException | ApplianceOfflineException e) {
            logger.warn("Failed to get home appliance! haId={}, error={}", haId, e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
    }

    /**
     * Get ambient light state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getAmbientLightState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_AMBIENT_LIGHT_ENABLED);
    }

    /**
     * Set ambient light state of device.
     *
     * @param haId home appliance id
     * @param enable enable or disable ambient light
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setAmbientLightState(String haId, boolean enable)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_AMBIENT_LIGHT_ENABLED, String.valueOf(enable), null), VALUE_TYPE_BOOLEAN);
    }

    /**
     * Get functional light state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getFunctionalLightState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_LIGHTING);
    }

    /**
     * Set functional light state of device.
     *
     * @param haId home appliance id
     * @param enable enable or disable functional light
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setFunctionalLightState(String haId, boolean enable)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_LIGHTING, String.valueOf(enable), null), VALUE_TYPE_BOOLEAN);
    }

    /**
     * Get functional light brightness state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getFunctionalLightBrightnessState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_LIGHTING_BRIGHTNESS);
    }

    /**
     * Set functional light brightness of device.
     *
     * @param haId home appliance id
     * @param value brightness value 10-100
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setFunctionalLightBrightnessState(String haId, int value)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_LIGHTING_BRIGHTNESS, String.valueOf(value), "%"), VALUE_TYPE_INT);
    }

    /**
     * Get ambient light brightness state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getAmbientLightBrightnessState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_AMBIENT_LIGHT_BRIGHTNESS);
    }

    /**
     * Set ambient light brightness of device.
     *
     * @param haId home appliance id
     * @param value brightness value 10-100
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setAmbientLightBrightnessState(String haId, int value)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_AMBIENT_LIGHT_BRIGHTNESS, String.valueOf(value), "%"), VALUE_TYPE_INT);
    }

    /**
     * Get ambient light color state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getAmbientLightColorState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_AMBIENT_LIGHT_COLOR);
    }

    /**
     * Set ambient light color of device.
     *
     * @param haId home appliance id
     * @param value color code
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setAmbientLightColorState(String haId, String value)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_AMBIENT_LIGHT_COLOR, value, null));
    }

    /**
     * Get ambient light custom color state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getAmbientLightCustomColorState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_AMBIENT_LIGHT_CUSTOM_COLOR);
    }

    /**
     * Set ambient light color of device.
     *
     * @param haId home appliance id
     * @param value color code
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setAmbientLightCustomColorState(String haId, String value)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_AMBIENT_LIGHT_CUSTOM_COLOR, value, null));
    }

    /**
     * Get power state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getPowerState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_POWER_STATE);
    }

    /**
     * Provides information on whether the power state of device can be set or only read.
     *
     * @param haId home appliance id
     * @return {@link PowerStateAccess}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public PowerStateAccess getPowerStateAccess(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        String powerStateSettings = getRaw(haId, BASE_PATH + haId + "/settings/" + SETTING_POWER_STATE);

        /***
         * Example response:
         * {
         * "data": {
         * "key": "BSH.Common.Setting.PowerState",
         * "value": "BSH.Common.EnumType.PowerState.Off",
         * "type": "BSH.Common.EnumType.PowerState",
         * "constraints": {
         * "allowedvalues": [
         * "BSH.Common.EnumType.PowerState.Off",
         * "BSH.Common.EnumType.PowerState.On"
         * ],
         * "default": "BSH.Common.EnumType.PowerState.On",
         * "access": "readWrite"
         * }
         * }
         * }
         */

        if (powerStateSettings != null) {
            JsonObject responseObject = parseString(powerStateSettings).getAsJsonObject();
            JsonObject data = responseObject.getAsJsonObject("data");
            JsonElement jsonConstraints = data.get("constraints");
            if (jsonConstraints.isJsonObject()) {
                JsonElement jsonAccess = jsonConstraints.getAsJsonObject().get("access");
                if (jsonAccess.isJsonPrimitive()) {
                    return PowerStateAccess.fromString(jsonAccess.getAsString());
                }
            }
        }

        return PowerStateAccess.READ_ONLY;
    }

    /**
     * Set power state of device.
     *
     * @param haId home appliance id
     * @param state target state
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setPowerState(String haId, String state)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_POWER_STATE, state, null));
    }

    /**
     * Get setpoint temperature of freezer
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getFreezerSetpointTemperature(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_FREEZER_SETPOINT_TEMPERATURE);
    }

    /**
     * Set setpoint temperature of freezer
     *
     * @param haId home appliance id
     * @param state new temperature
     * @param unit temperature unit
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setFreezerSetpointTemperature(String haId, String state, String unit)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_FREEZER_SETPOINT_TEMPERATURE, state, unit), VALUE_TYPE_INT);
    }

    /**
     * Get setpoint temperature of fridge
     *
     * @param haId home appliance id
     * @return {@link Data} or null in case of communication error
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getFridgeSetpointTemperature(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_REFRIGERATOR_SETPOINT_TEMPERATURE);
    }

    /**
     * Set setpoint temperature of fridge
     *
     * @param haId home appliance id
     * @param state new temperature
     * @param unit temperature unit
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setFridgeSetpointTemperature(String haId, String state, String unit)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_REFRIGERATOR_SETPOINT_TEMPERATURE, state, unit), VALUE_TYPE_INT);
    }

    /**
     * Get fridge super mode
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getFridgeSuperMode(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_REFRIGERATOR_SUPER_MODE);
    }

    /**
     * Set fridge super mode
     *
     * @param haId home appliance id
     * @param enable enable or disable fridge super mode
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setFridgeSuperMode(String haId, boolean enable)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_REFRIGERATOR_SUPER_MODE, String.valueOf(enable), null), VALUE_TYPE_BOOLEAN);
    }

    /**
     * Get freezer super mode
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getFreezerSuperMode(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getSetting(haId, SETTING_FREEZER_SUPER_MODE);
    }

    /**
     * Set freezer super mode
     *
     * @param haId home appliance id
     * @param enable enable or disable freezer super mode
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public void setFreezerSuperMode(String haId, boolean enable)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, new Data(SETTING_FREEZER_SUPER_MODE, String.valueOf(enable), null), VALUE_TYPE_BOOLEAN);
    }

    /**
     * Get door state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getDoorState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getStatus(haId, STATUS_DOOR_STATE);
    }

    /**
     * Get operation state of device.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getOperationState(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getStatus(haId, STATUS_OPERATION_STATE);
    }

    /**
     * Get current cavity temperature of oven.
     *
     * @param haId home appliance id
     * @return {@link Data}
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public Data getCurrentCavityTemperature(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getStatus(haId, STATUS_OVEN_CURRENT_CAVITY_TEMPERATURE);
    }

    /**
     * Is remote start allowed?
     *
     * @param haId haId home appliance id
     * @return true or false
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public boolean isRemoteControlStartAllowed(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Data data = getStatus(haId, STATUS_REMOTE_CONTROL_START_ALLOWED);
        return Boolean.parseBoolean(data.getValue());
    }

    /**
     * Is remote control allowed?
     *
     * @param haId haId home appliance id
     * @return true or false
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public boolean isRemoteControlActive(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Data data = getStatus(haId, STATUS_REMOTE_CONTROL_ACTIVE);
        return Boolean.parseBoolean(data.getValue());
    }

    /**
     * Is local control allowed?
     *
     * @param haId haId home appliance id
     * @return true or false
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public boolean isLocalControlActive(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Data data = getStatus(haId, STATUS_LOCAL_CONTROL_ACTIVE);
        return Boolean.parseBoolean(data.getValue());
    }

    /**
     * Get active program of device.
     *
     * @param haId home appliance id
     * @return {@link Program} or null if there is no active program
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public @Nullable Program getActiveProgram(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getProgram(haId, BASE_PATH + haId + "/programs/active");
    }

    /**
     * Get selected program of device.
     *
     * @param haId home appliance id
     * @return {@link Program} or null if there is no selected program
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public @Nullable Program getSelectedProgram(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getProgram(haId, BASE_PATH + haId + "/programs/selected");
    }

    public void setSelectedProgram(String haId, String program)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putData(haId, BASE_PATH + haId + "/programs/selected", new Data(program, null, null), VALUE_TYPE_STRING);
    }

    public void startProgram(String haId, String program)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putData(haId, BASE_PATH + haId + "/programs/active", new Data(program, null, null), VALUE_TYPE_STRING);
    }

    public void startSelectedProgram(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        String selectedProgram = getRaw(haId, BASE_PATH + haId + "/programs/selected");
        if (selectedProgram != null) {
            putRaw(haId, BASE_PATH + haId + "/programs/active", selectedProgram);
        }
    }

    public void startCustomProgram(String haId, String json)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putRaw(haId, BASE_PATH + haId + "/programs/active", json);
    }

    public void setProgramOptions(String haId, String key, String value, @Nullable String unit, boolean valueAsInt,
            boolean isProgramActive) throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        String programState = isProgramActive ? "active" : "selected";

        putOption(haId, BASE_PATH + haId + "/programs/" + programState + "/options", new Option(key, value, unit),
                valueAsInt);
    }

    public void stopProgram(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        sendDelete(haId, BASE_PATH + haId + "/programs/active");
    }

    public List<AvailableProgram> getPrograms(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getAvailablePrograms(haId, BASE_PATH + haId + "/programs");
    }

    public List<AvailableProgram> getAvailablePrograms(String haId)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getAvailablePrograms(haId, BASE_PATH + haId + "/programs/available");
    }

    /**
     * Get the available options of a program.
     *
     * @param haId home appliance id
     * @param programKey program id
     * @return list of {@link AvailableProgramOption} or null if the program is unsupported by the API
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public @Nullable List<AvailableProgramOption> getProgramOptions(String haId, String programKey)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.GET, BASE_PATH + haId + "/programs/available/" + programKey);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(List.of(HttpStatus.OK_200, HttpStatus.NOT_FOUND_404), request, response, haId, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, null, response, responseBody);

            // Code 404 accepted only if the returned error is "SDK.Error.UnsupportedProgram"
            if (response.getStatus() == HttpStatus.NOT_FOUND_404
                    && (responseBody == null || !responseBody.contains("SDK.Error.UnsupportedProgram"))) {
                throw new CommunicationException(HttpStatus.NOT_FOUND_404, response.getReason(),
                        responseBody == null ? "" : responseBody);
            }

            return response.getStatus() == HttpStatus.OK_200 ? mapToAvailableProgramOption(responseBody, haId) : null;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get program options! haId={}, programKey={}, error={}", haId, programKey,
                    e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
    }

    /**
     * Get latest API requests.
     *
     * @return communication queue
     */
    public Collection<ApiRequest> getLatestApiRequests() {
        return communicationQueue.getAll();
    }

    private Data getSetting(String haId, String setting)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getData(haId, BASE_PATH + haId + "/settings/" + setting);
    }

    private void putSettings(String haId, Data data)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putSettings(haId, data, VALUE_TYPE_STRING);
    }

    private void putSettings(String haId, Data data, int valueType)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        putData(haId, BASE_PATH + haId + "/settings/" + data.getName(), data, valueType);
    }

    private Data getStatus(String haId, String status)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getData(haId, BASE_PATH + haId + "/status/" + status);
    }

    public @Nullable String getRaw(String haId, String path)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        return getRaw(haId, path, false);
    }

    public @Nullable String getRaw(String haId, String path, boolean ignoreResponseCode)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.GET, path);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.OK_200, request, response, haId, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, null, response, responseBody);

            if (ignoreResponseCode || response.getStatus() == HttpStatus.OK_200) {
                return responseBody;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get raw! haId={}, path={}, error={}", haId, path, e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
        return null;
    }

    public String putRaw(String haId, String path, String requestBodyPayload)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.PUT, path).content(new StringContentProvider(requestBodyPayload),
                BSH_JSON_V1);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.NO_CONTENT_204, request, response, haId, requestBodyPayload);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, requestBodyPayload, response, responseBody);
            return responseBody;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to put raw! haId={}, path={}, payload={}, error={}", haId, path, requestBodyPayload,
                    e.getMessage());
            trackAndLogApiRequest(haId, request, requestBodyPayload, null, null);
            throw new CommunicationException(e);
        }
    }

    private @Nullable Program getProgram(String haId, String path)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.GET, path);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(List.of(HttpStatus.OK_200, HttpStatus.NOT_FOUND_404), request, response, haId, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, null, response, responseBody);

            if (response.getStatus() == HttpStatus.OK_200) {
                return mapToProgram(responseBody);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get program! haId={}, path={}, error={}", haId, path, e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
        return null;
    }

    private List<AvailableProgram> getAvailablePrograms(String haId, String path)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.GET, path);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.OK_200, request, response, haId, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, null, response, responseBody);

            return mapToAvailablePrograms(responseBody, haId);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get available programs! haId={}, path={}, error={}", haId, path, e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
    }

    private void sendDelete(String haId, String path)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.DELETE, path);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.NO_CONTENT_204, request, response, haId, null);

            trackAndLogApiRequest(haId, request, null, response, response.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to send delete! haId={}, path={}, error={}", haId, path, e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
    }

    private Data getData(String haId, String path)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        Request request = createRequest(HttpMethod.GET, path);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.OK_200, request, response, haId, null);

            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, null, response, responseBody);

            return mapToState(responseBody);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get data! haId={}, path={}, error={}", haId, path, e.getMessage());
            trackAndLogApiRequest(haId, request, null, null, null);
            throw new CommunicationException(e);
        }
    }

    private void putData(String haId, String path, Data data, int valueType)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("key", data.getName());

        if (data.getValue() != null) {
            if (valueType == VALUE_TYPE_INT) {
                innerObject.addProperty("value", data.getValueAsInt());
            } else if (valueType == VALUE_TYPE_BOOLEAN) {
                innerObject.addProperty("value", data.getValueAsBoolean());
            } else {
                innerObject.addProperty("value", data.getValue());
            }
        }

        if (data.getUnit() != null) {
            innerObject.addProperty("unit", data.getUnit());
        }

        JsonObject dataObject = new JsonObject();
        dataObject.add("data", innerObject);
        String requestBodyPayload = dataObject.toString();

        Request request = createRequest(HttpMethod.PUT, path).content(new StringContentProvider(requestBodyPayload),
                BSH_JSON_V1);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.NO_CONTENT_204, request, response, haId, requestBodyPayload);

            trackAndLogApiRequest(haId, request, requestBodyPayload, response, response.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to put data! haId={}, path={}, data={}, valueType={}, error={}", haId, path, data,
                    valueType, e.getMessage());
            trackAndLogApiRequest(haId, request, requestBodyPayload, null, null);
            throw new CommunicationException(e);
        }
    }

    private void putOption(String haId, String path, Option option, boolean asInt)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("key", option.getKey());

        if (option.getValue() != null) {
            if (asInt) {
                innerObject.addProperty("value", option.getValueAsInt());
            } else {
                innerObject.addProperty("value", option.getValue());
            }
        }

        if (option.getUnit() != null) {
            innerObject.addProperty("unit", option.getUnit());
        }

        JsonArray optionsArray = new JsonArray();
        optionsArray.add(innerObject);

        JsonObject optionsObject = new JsonObject();
        optionsObject.add("options", optionsArray);

        JsonObject dataObject = new JsonObject();
        dataObject.add("data", optionsObject);

        String requestBodyPayload = dataObject.toString();

        Request request = createRequest(HttpMethod.PUT, path).content(new StringContentProvider(requestBodyPayload),
                BSH_JSON_V1);
        try {
            ContentResponse response = sendRequest(request, apiBridgeConfiguration.getClientId());
            checkResponseCode(HttpStatus.NO_CONTENT_204, request, response, haId, requestBodyPayload);

            trackAndLogApiRequest(haId, request, requestBodyPayload, response, response.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to put option! haId={}, path={}, option={}, asInt={}, error={}", haId, path, option,
                    asInt, e.getMessage());
            trackAndLogApiRequest(haId, request, requestBodyPayload, null, null);
            throw new CommunicationException(e);
        }
    }

    private void checkResponseCode(int desiredCode, Request request, ContentResponse response, @Nullable String haId,
            @Nullable String requestPayload)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        checkResponseCode(List.of(desiredCode), request, response, haId, requestPayload);
    }

    private void checkResponseCode(List<Integer> desiredCodes, Request request, ContentResponse response,
            @Nullable String haId, @Nullable String requestPayload)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        if (!desiredCodes.contains(HttpStatus.UNAUTHORIZED_401)
                && response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            logger.debug("Current access token is invalid.");
            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, requestPayload, response, responseBody);
            throw new AuthorizationException("Token invalid!");
        }

        if (!desiredCodes.contains(response.getStatus())) {
            int code = response.getStatus();
            String message = response.getReason();

            logger.debug("Invalid HTTP response code {} (allowed: {})", code, desiredCodes);
            String responseBody = response.getContentAsString();
            trackAndLogApiRequest(haId, request, requestPayload, response, responseBody);

            responseBody = responseBody == null ? "" : responseBody;
            if (code == HttpStatus.CONFLICT_409 && responseBody.toLowerCase().contains("error")
                    && responseBody.toLowerCase().contains("offline")) {
                throw new ApplianceOfflineException(code, message, responseBody);
            } else {
                throw new CommunicationException(code, message, responseBody);
            }
        }
    }

    private Program mapToProgram(String json) {
        ArrayList<Option> optionList = new ArrayList<>();
        JsonObject responseObject = parseString(json).getAsJsonObject();
        JsonObject data = responseObject.getAsJsonObject("data");
        Program result = new Program(data.get("key").getAsString(), optionList);
        JsonArray options = data.getAsJsonArray("options");

        options.forEach(option -> {
            JsonObject obj = (JsonObject) option;

            @Nullable
            String key = obj.get("key") != null ? obj.get("key").getAsString() : null;
            @Nullable
            String value = obj.get("value") != null && !obj.get("value").isJsonNull() ? obj.get("value").getAsString()
                    : null;
            @Nullable
            String unit = obj.get("unit") != null ? obj.get("unit").getAsString() : null;

            optionList.add(new Option(key, value, unit));
        });

        return result;
    }

    private List<AvailableProgram> mapToAvailablePrograms(String json, String haId) {
        ArrayList<AvailableProgram> result = new ArrayList<>();

        try {
            JsonObject responseObject = parseString(json).getAsJsonObject();

            JsonArray programs = responseObject.getAsJsonObject("data").getAsJsonArray("programs");
            programs.forEach(program -> {
                JsonObject obj = (JsonObject) program;
                @Nullable
                String key = obj.get("key") != null ? obj.get("key").getAsString() : null;
                JsonObject constraints = obj.getAsJsonObject("constraints");
                boolean available = constraints.get("available") != null && constraints.get("available").getAsBoolean();
                @Nullable
                String execution = constraints.get("execution") != null ? constraints.get("execution").getAsString()
                        : null;

                if (key != null && execution != null) {
                    result.add(new AvailableProgram(key, available, execution));
                }
            });
        } catch (Exception e) {
            logger.warn("Could not parse available programs response! haId={}, error={}", haId, e.getMessage());
        }

        return result;
    }

    private List<AvailableProgramOption> mapToAvailableProgramOption(String json, String haId) {
        ArrayList<AvailableProgramOption> result = new ArrayList<>();

        try {
            JsonObject responseObject = parseString(json).getAsJsonObject();

            JsonArray options = responseObject.getAsJsonObject("data").getAsJsonArray("options");
            options.forEach(option -> {
                JsonObject obj = (JsonObject) option;
                @Nullable
                String key = obj.get("key") != null ? obj.get("key").getAsString() : null;
                ArrayList<String> allowedValues = new ArrayList<>();
                obj.getAsJsonObject("constraints").getAsJsonArray("allowedvalues")
                        .forEach(value -> allowedValues.add(value.getAsString()));

                if (key != null) {
                    result.add(new AvailableProgramOption(key, allowedValues));
                }
            });
        } catch (Exception e) {
            logger.warn("Could not parse available program options response! haId={}, error={}", haId, e.getMessage());
        }

        return result;
    }

    private HomeAppliance mapToHomeAppliance(String json) {
        JsonObject responseObject = parseString(json).getAsJsonObject();

        JsonObject data = responseObject.getAsJsonObject("data");

        return new HomeAppliance(data.get("haId").getAsString(), data.get("name").getAsString(),
                data.get("brand").getAsString(), data.get("vib").getAsString(), data.get("connected").getAsBoolean(),
                data.get("type").getAsString(), data.get("enumber").getAsString());
    }

    private ArrayList<HomeAppliance> mapToHomeAppliances(String json) {
        final ArrayList<HomeAppliance> result = new ArrayList<>();
        JsonObject responseObject = parseString(json).getAsJsonObject();

        JsonObject data = responseObject.getAsJsonObject("data");
        JsonArray homeappliances = data.getAsJsonArray("homeappliances");

        homeappliances.forEach(appliance -> {
            JsonObject obj = (JsonObject) appliance;

            result.add(new HomeAppliance(obj.get("haId").getAsString(), obj.get("name").getAsString(),
                    obj.get("brand").getAsString(), obj.get("vib").getAsString(), obj.get("connected").getAsBoolean(),
                    obj.get("type").getAsString(), obj.get("enumber").getAsString()));
        });

        return result;
    }

    private Data mapToState(String json) {
        JsonObject responseObject = parseString(json).getAsJsonObject();

        JsonObject data = responseObject.getAsJsonObject("data");

        @Nullable
        String unit = data.get("unit") != null ? data.get("unit").getAsString() : null;

        return new Data(data.get("key").getAsString(), data.get("value").getAsString(), unit);
    }

    private Request createRequest(HttpMethod method, String path)
            throws AuthorizationException, CommunicationException {
        return client.newRequest(apiUrl + path)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(oAuthClientService))
                .header(HttpHeaders.ACCEPT, BSH_JSON_V1).method(method).timeout(REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private void trackAndLogApiRequest(@Nullable String haId, Request request, @Nullable String requestBody,
            @Nullable ContentResponse response, @Nullable String responseBody) {
        HomeConnectRequest homeConnectRequest = map(request, requestBody);
        @Nullable
        HomeConnectResponse homeConnectResponse = response != null ? map(response, responseBody) : null;

        logApiRequest(haId, homeConnectRequest, homeConnectResponse);
        trackApiRequest(homeConnectRequest, homeConnectResponse);
    }

    private void logApiRequest(@Nullable String haId, HomeConnectRequest homeConnectRequest,
            @Nullable HomeConnectResponse homeConnectResponse) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();

            if (haId != null) {
                sb.append("[").append(haId).append("] ");
            }

            sb.append(homeConnectRequest.getMethod()).append(" ");
            if (homeConnectResponse != null) {
                sb.append(homeConnectResponse.getCode()).append(" ");
            }
            sb.append(homeConnectRequest.getUrl()).append("\n");
            homeConnectRequest.getHeader()
                    .forEach((key, value) -> sb.append("> ").append(key).append(": ").append(value).append("\n"));

            if (homeConnectRequest.getBody() != null) {
                sb.append(homeConnectRequest.getBody()).append("\n");
            }

            if (homeConnectResponse != null) {
                sb.append("\n");
                homeConnectResponse.getHeader()
                        .forEach((key, value) -> sb.append("< ").append(key).append(": ").append(value).append("\n"));
            }
            if (homeConnectResponse != null && homeConnectResponse.getBody() != null) {
                sb.append(homeConnectResponse.getBody()).append("\n");
            }

            logger.debug("{}", sb.toString());
        }
    }

    private void trackApiRequest(HomeConnectRequest homeConnectRequest,
            @Nullable HomeConnectResponse homeConnectResponse) {
        communicationQueue.add(new ApiRequest(ZonedDateTime.now(), homeConnectRequest, homeConnectResponse));
    }

    private HomeConnectRequest map(Request request, @Nullable String requestBody) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaders().forEach(field -> headers.put(field.getName(), field.getValue()));

        return new HomeConnectRequest(request.getURI().toString(), request.getMethod(), headers,
                requestBody != null ? formatJsonBody(requestBody) : null);
    }

    private HomeConnectResponse map(ContentResponse response, @Nullable String responseBody) {
        Map<String, String> headers = new HashMap<>();
        response.getHeaders().forEach(field -> headers.put(field.getName(), field.getValue()));

        return new HomeConnectResponse(response.getStatus(), headers,
                responseBody != null ? formatJsonBody(responseBody) : null);
    }
}
