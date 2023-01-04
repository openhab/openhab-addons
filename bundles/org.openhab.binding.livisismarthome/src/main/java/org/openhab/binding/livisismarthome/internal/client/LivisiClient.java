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
package org.openhab.binding.livisismarthome.internal.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.livisismarthome.internal.LivisiBindingConstants;
import org.openhab.binding.livisismarthome.internal.client.api.entity.StatusResponseDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ActionDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionType;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.StateActionSetterDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.StateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.error.ErrorResponseDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.location.LocationDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;
import org.openhab.binding.livisismarthome.internal.client.exception.ApiException;
import org.openhab.binding.livisismarthome.internal.client.exception.AuthenticationException;
import org.openhab.binding.livisismarthome.internal.client.exception.ControllerOfflineException;
import org.openhab.binding.livisismarthome.internal.client.exception.InvalidActionTriggeredException;
import org.openhab.binding.livisismarthome.internal.client.exception.RemoteAccessNotAllowedException;
import org.openhab.binding.livisismarthome.internal.client.exception.ServiceUnavailableException;
import org.openhab.binding.livisismarthome.internal.client.exception.SessionExistsException;
import org.openhab.binding.livisismarthome.internal.client.exception.SessionNotFoundException;
import org.openhab.binding.livisismarthome.internal.handler.LivisiBridgeConfiguration;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The main client that handles the communication with the LIVISI SmartHome API service.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 * @author Sven Strohschein - Renamed from Innogy to Livisi and refactored
 */
@NonNullByDefault
public class LivisiClient {

    private final Logger logger = LoggerFactory.getLogger(LivisiClient.class);

    private final GsonOptional gson = new GsonOptional();
    private final LivisiBridgeConfiguration bridgeConfiguration;
    private final OAuthClientService oAuthService;
    private final URLConnectionFactory connectionFactory;

    public LivisiClient(final LivisiBridgeConfiguration bridgeConfiguration, final OAuthClientService oAuthService,
            final URLConnectionFactory connectionFactory) {
        this.bridgeConfiguration = bridgeConfiguration;
        this.oAuthService = oAuthService;
        this.connectionFactory = connectionFactory;
    }

    /**
     * Gets the status
     * As the API returns the details of the SmartHome controller (SHC), the config version is returned.
     *
     * @return config version
     */
    public String refreshStatus() throws IOException {
        logger.debug("Get LIVISI SmartHome status...");
        final Optional<StatusResponseDTO> status = executeGet(URLCreator.createStatusURL(bridgeConfiguration.host),
                StatusResponseDTO.class);

        if (status.isPresent()) {
            String configVersion = status.get().getConfigVersion();
            logger.debug("LIVISI SmartHome status loaded. Configuration version is {}.", configVersion);
            return configVersion;
        }
        return "";
    }

    /**
     * Executes a HTTP GET request with default headers and returns data as object of type T.
     *
     * @param url request URL
     * @param clazz type of data to return
     * @return response content
     */
    private <T> Optional<T> executeGet(final String url, final Class<T> clazz) throws IOException {
        HttpURLConnection connection = createBaseRequest(url, HttpMethod.GET);
        String responseContent = executeRequest(connection);
        return gson.fromJson(responseContent, clazz);
    }

    /**
     * Executes a HTTP GET request with default headers and returns data as List of type T.
     *
     * @param url request URL
     * @param clazz array type of data to return as list
     * @return response content (as a List)
     */
    private <T> List<T> executeGetList(final String url, final Class<T[]> clazz) throws IOException {
        Optional<T[]> objects = executeGet(url, clazz);
        if (objects.isPresent()) {
            return Arrays.asList(objects.get());
        }
        return Collections.emptyList();
    }

    /**
     * Executes a HTTP POST request with the given {@link ActionDTO} as content.
     *
     * @param url request URL
     * @param action action to execute
     */
    private void executePost(final String url, final ActionDTO action) throws IOException {
        final String json = gson.toJson(action);
        final byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        logger.debug("Action {} JSON: {}", action.getType(), json);

        HttpURLConnection connection = createBaseRequest(url, HttpMethod.POST);
        connection.setDoOutput(true);
        connection.setRequestProperty(HttpHeader.CONTENT_LENGTH.asString(), String.valueOf(jsonBytes.length));
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(jsonBytes);
        }

        executeRequest(connection);
    }

    private String executeRequest(HttpURLConnection connection) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        String responseContent = stringBuilder.toString();
        logger.trace("RAW-RESPONSE: {}", responseContent);
        handleResponseErrors(connection, responseContent);
        return normalizeResponseContent(responseContent);
    }

    private HttpURLConnection createBaseRequest(String url, HttpMethod httpMethod) throws IOException {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();
        return connectionFactory.createBaseRequest(url, httpMethod, accessTokenResponse);
    }

    public AccessTokenResponse getAccessTokenResponse() throws IOException {
        try {
            @Nullable
            final AccessTokenResponse accessTokenResponse = oAuthService.getAccessTokenResponse();
            if (accessTokenResponse == null || accessTokenResponse.getAccessToken() == null
                    || accessTokenResponse.getAccessToken().isBlank()) {
                throw new AuthenticationException("No LIVISI SmartHome access token. Is this thing authorized?");
            }
            return accessTokenResponse;
        } catch (OAuthException | OAuthResponseException e) {
            throw new AuthenticationException("Error fetching access token: " + e.getMessage());
        }
    }

    /**
     * Handles errors from the {@link org.eclipse.jetty.client.api.ContentResponse} and throws the following errors:
     *
     * @param connection connection
     * @param responseContent response content
     * @throws ControllerOfflineException thrown, if the LIVISI SmartHome controller (SHC) is offline.
     */
    private void handleResponseErrors(final HttpURLConnection connection, final String responseContent)
            throws IOException {
        final int status = connection.getResponseCode();
        if (HttpStatus.OK_200 == status) {
            logger.debug("Statuscode is OK: [{}]", connection.getURL());
        } else if (HttpStatus.SERVICE_UNAVAILABLE_503 == status) {
            throw new ServiceUnavailableException("LIVISI SmartHome service is unavailable (503).");
        } else {
            logger.debug("Statuscode {} is NOT OK: [{}]", status, connection.getURL());
            String content = normalizeResponseContent(responseContent);
            try {
                logger.trace("Response error content: {}", content);
                final Optional<ErrorResponseDTO> errorOptional = gson.fromJson(content, ErrorResponseDTO.class);
                if (errorOptional.isPresent()) {
                    ErrorResponseDTO error = errorOptional.get();
                    switch (error.getCode()) {
                        case ErrorResponseDTO.ERR_SESSION_EXISTS:
                            throw new SessionExistsException("Session exists: " + error.getDescription());
                        case ErrorResponseDTO.ERR_SESSION_NOT_FOUND:
                            throw new SessionNotFoundException("Session not found: " + error.getDescription());
                        case ErrorResponseDTO.ERR_CONTROLLER_OFFLINE:
                            throw new ControllerOfflineException("Controller offline: " + error.getDescription());
                        case ErrorResponseDTO.ERR_REMOTE_ACCESS_NOT_ALLOWED:
                            throw new RemoteAccessNotAllowedException(
                                    "Remote access not allowed. Access is allowed only from the SHC device network.");
                        case ErrorResponseDTO.ERR_INVALID_ACTION_TRIGGERED:
                            throw new InvalidActionTriggeredException(
                                    "Invalid action triggered. Message: " + error.getDescription());
                    }
                    throw new ApiException("Unknown error: " + error);
                }
            } catch (final JsonSyntaxException e) {
                throw new ApiException("Invalid JSON syntax in error response: " + content, e);
            }
        }
    }

    /**
     * Sets a new state of a SwitchActuator.
     */
    public void setSwitchActuatorState(final String capabilityId, final boolean state) throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_SWITCHACTUATOR, state));
    }

    /**
     * Sets the dimmer level of a DimmerActuator.
     */
    public void setDimmerActuatorState(final String capabilityId, final int dimLevel) throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_DIMMERACTUATOR, dimLevel));
    }

    /**
     * Sets the roller shutter level of a RollerShutterActuator.
     */
    public void setRollerShutterActuatorState(final String capabilityId, final int rollerShutterLevel)
            throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, rollerShutterLevel));
    }

    /**
     * Starts or stops moving a RollerShutterActuator
     */
    public void setRollerShutterAction(final String capabilityId, final ShutterActionType rollerShutterAction)
            throws IOException {
        executePost(createActionURL(), new ShutterActionDTO(capabilityId, rollerShutterAction));
    }

    /**
     * Sets a new state of a VariableActuator.
     */
    public void setVariableActuatorState(final String capabilityId, final boolean state) throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_VARIABLEACTUATOR, state));
    }

    /**
     * Sets the point temperature.
     */
    public void setPointTemperatureState(final String capabilityId, final double pointTemperature) throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_THERMOSTATACTUATOR, pointTemperature));
    }

    /**
     * Sets the operation mode to "Auto" or "Manu".
     */
    public void setOperationMode(final String capabilityId, final boolean isAutoMode) throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_THERMOSTATACTUATOR, isAutoMode));
    }

    /**
     * Sets the alarm state.
     */
    public void setAlarmActuatorState(final String capabilityId, final boolean alarmState) throws IOException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_ALARMACTUATOR, alarmState));
    }

    /**
     * Load the device and returns a {@link List} of {@link DeviceDTO}s.
     * VariableActuators are returned additionally (independent from the device ids),
     * because VariableActuators are everytime available and never have a device state.
     *
     * @param deviceIds Ids of the devices to return
     * @return List of Devices
     */
    public List<DeviceDTO> getDevices(Collection<String> deviceIds) throws IOException {
        logger.debug("Loading LIVISI SmartHome devices...");
        List<DeviceDTO> devices = executeGetList(URLCreator.createDevicesURL(bridgeConfiguration.host),
                DeviceDTO[].class);
        return devices.stream().filter(d -> isDeviceUsable(d, deviceIds)).collect(Collectors.toList());
    }

    /**
     * Loads the {@link DeviceDTO} with the given deviceId.
     */
    public Optional<DeviceDTO> getDeviceById(final String deviceId) throws IOException {
        logger.debug("Loading device with id {}...", deviceId);
        return executeGet(URLCreator.createDeviceURL(bridgeConfiguration.host, deviceId), DeviceDTO.class);
    }

    /**
     * Loads the states for all {@link DeviceDTO}s.
     */
    public List<DeviceStateDTO> getDeviceStates() throws IOException {
        logger.debug("Loading device states...");
        return executeGetList(URLCreator.createDeviceStatesURL(bridgeConfiguration.host), DeviceStateDTO[].class);
    }

    /**
     * Loads the device state for the given deviceId.
     */
    public @Nullable StateDTO getDeviceStateByDeviceId(final String deviceId, final boolean isSHCClassic)
            throws IOException {
        logger.debug("Loading device states for device id {}...", deviceId);
        if (isSHCClassic) {
            Optional<DeviceStateDTO> deviceState = executeGet(
                    URLCreator.createDeviceStateURL(bridgeConfiguration.host, deviceId), DeviceStateDTO.class);
            return deviceState.map(DeviceStateDTO::getState).orElse(null);
        }
        return executeGet(URLCreator.createDeviceStateURL(bridgeConfiguration.host, deviceId), StateDTO.class)
                .orElse(null);
    }

    /**
     * Loads the locations and returns a {@link List} of {@link LocationDTO}s.
     *
     * @return a List of Devices
     */
    public List<LocationDTO> getLocations() throws IOException {
        logger.debug("Loading locations...");
        return executeGetList(URLCreator.createLocationURL(bridgeConfiguration.host), LocationDTO[].class);
    }

    /**
     * Loads and returns a {@link List} of {@link CapabilityDTO}s for the given deviceId.
     *
     * @param deviceId the id of the {@link DeviceDTO}
     * @return capabilities of the device
     */
    public List<CapabilityDTO> getCapabilitiesForDevice(final String deviceId) throws IOException {
        logger.debug("Loading capabilities for device {}...", deviceId);
        return executeGetList(URLCreator.createDeviceCapabilitiesURL(bridgeConfiguration.host, deviceId),
                CapabilityDTO[].class);
    }

    /**
     * Loads and returns a {@link List} of all {@link CapabilityDTO}s.
     */
    public List<CapabilityDTO> getCapabilities() throws IOException {
        logger.debug("Loading capabilities...");
        return executeGetList(URLCreator.createCapabilityURL(bridgeConfiguration.host), CapabilityDTO[].class);
    }

    /**
     * Loads and returns a {@link List} of all {@link CapabilityDTO}States.
     */
    public List<CapabilityStateDTO> getCapabilityStates() throws IOException {
        logger.debug("Loading capability states...");
        return executeGetList(URLCreator.createCapabilityStatesURL(bridgeConfiguration.host),
                CapabilityStateDTO[].class);
    }

    /**
     * Returns a {@link List} of all {@link MessageDTO}s.
     */
    public List<MessageDTO> getMessages() throws IOException {
        logger.debug("Loading messages...");
        return executeGetList(URLCreator.createMessageURL(bridgeConfiguration.host), MessageDTO[].class);
    }

    private String createActionURL() {
        return URLCreator.createActionURL(bridgeConfiguration.host);
    }

    /**
     * Decides if a (discovered) device is usable (available and supported).
     *
     * @param device device to check
     * @param activeDeviceIds active device id (devices with an according available device state)
     * @return true when usable, otherwise false
     */
    private static boolean isDeviceUsable(DeviceDTO device, Collection<String> activeDeviceIds) {
        return activeDeviceIds.contains(device.getId())
                || LivisiBindingConstants.DEVICE_VARIABLE_ACTUATOR.equals(device.getType());
    }

    /**
     * Normalizes the JSON response content.
     * The LIVISI SmartHome local API returns "[]" for missing objects instead of "null". This method fixes
     * this issue.
     * 
     * @param responseContent response
     * @return normalized response content
     */
    private static String normalizeResponseContent(String responseContent) {
        return responseContent.replace("[]", "null");
    }
}
