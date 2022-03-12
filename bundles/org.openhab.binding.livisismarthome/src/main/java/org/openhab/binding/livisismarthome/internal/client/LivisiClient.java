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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.livisismarthome.internal.LivisiBindingConstants;
import org.openhab.binding.livisismarthome.internal.client.api.entity.StatusResponseDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ActionDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionType;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.StateActionSetterDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.auth.LivisiAccessTokenRequestDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.auth.LivisiAccessTokenResponseDTO;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The main client that handles the communication with the LIVISI SmartHome API service.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@NonNullByDefault
public class LivisiClient {

    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";
    private static final String CONTENT_TYPE = "application/json";
    private static final int HTTP_REQUEST_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(LivisiClient.class);

    /**
     * date format as used in json in API. Example: 2016-07-11T10:55:52.3863424Z
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    private final LivisiBridgeConfiguration bridgeConfiguration;
    private final OAuthClientService oAuthService;
    private final HttpClient httpClient;
    private String configVersion = "";

    public LivisiClient(final LivisiBridgeConfiguration bridgeConfiguration, final OAuthClientService oAuthService,
            final HttpClient httpClient) {
        this.bridgeConfiguration = bridgeConfiguration;
        this.oAuthService = oAuthService;
        this.httpClient = httpClient;
    }

    /**
     * Gets the status
     *
     * As the API returns the details of the SmartHome controller (SHC), the {@link #configVersion} is set.
     *
     * @throws SessionExistsException thrown, if a session already exists
     */
    public void refreshStatus() throws IOException, ApiException, AuthenticationException {
        logger.debug("Get LIVISI SmartHome status...");
        final StatusResponseDTO status = executeGet(URLCreator.createStatusURL(bridgeConfiguration.host),
                StatusResponseDTO.class);

        configVersion = status.getGatewayConfigVersion();

        logger.debug("LIVISI SmartHome status loaded. Configuration version is {}.", configVersion);
    }

    /**
     * Executes a HTTP GET request with default headers and returns data as object of type T.
     *
     * @param url request URL
     * @param clazz type of data to return
     * @return response content
     */
    private <T> T executeGet(final String url, final Class<T> clazz)
            throws IOException, AuthenticationException, ApiException {
        final ContentResponse response = request(httpClient.newRequest(url).method(HttpMethod.GET));

        return gson.fromJson(response.getContentAsString(), clazz);
    }

    /**
     * Executes a HTTP GET request with default headers and returns data as List of type T.
     *
     * @param url request URL
     * @param clazz array type of data to return as list
     * @return response content (as a List)
     */
    private <T> List<T> executeGetList(final String url, final Class<T[]> clazz)
            throws IOException, AuthenticationException, ApiException {
        return Arrays.asList(executeGet(url, clazz));
    }

    /**
     * Executes a HTTP POST request with the given {@link ActionDTO} as content.
     *
     * @param url request URL
     * @param action action to execute
     */
    private void executePost(final String url, final ActionDTO action)
            throws IOException, AuthenticationException, ApiException {
        final String json = gson.toJson(action);
        logger.debug("Action {} JSON: {}", action.getType(), json);

        request(httpClient.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(json), CONTENT_TYPE).accept(CONTENT_TYPE));
    }

    private ContentResponse request(final Request request) throws IOException, AuthenticationException, ApiException {
        final ContentResponse response;
        try {
            final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

            response = request.header(HttpHeader.ACCEPT, CONTENT_TYPE)
                    .header(HttpHeader.AUTHORIZATION, BEARER + accessTokenResponse.getAccessToken())
                    .timeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IOException(e);
        }
        handleResponseErrors(response, request.getURI());
        return response;
    }

    public AccessTokenResponse login() throws AuthenticationException, IOException, ApiException {
        LivisiAccessTokenRequestDTO requestBody = new LivisiAccessTokenRequestDTO();
        requestBody.setUserName(LivisiBindingConstants.USERNAME);
        requestBody.setPassword(bridgeConfiguration.password);
        requestBody.setGrantType("password");

        final String json = gson.toJson(requestBody);
        Request request = httpClient.newRequest(URLCreator.createTokenURL(bridgeConfiguration.host))
                .method(HttpMethod.POST).content(new StringContentProvider(json), CONTENT_TYPE).accept(CONTENT_TYPE);

        ContentResponse response;
        try {
            response = request.header(HttpHeader.ACCEPT, CONTENT_TYPE)
                    .header(HttpHeader.AUTHORIZATION, BASIC + LivisiBindingConstants.AUTH_HEADER_VALUE)
                    .timeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
        handleResponseErrors(response, request.getURI());

        LivisiAccessTokenResponseDTO tokenResponse = gson.fromJson(response.getContentAsString(),
                LivisiAccessTokenResponseDTO.class);
        return tokenResponse.createAccessTokenResponse();
    }

    public AccessTokenResponse getAccessTokenResponse() throws AuthenticationException, IOException {
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
     * Handles errors from the {@link ContentResponse} and throws the following errors:
     *
     * @param response response
     * @param uri uri of api call made
     * @throws ControllerOfflineException thrown, if the LIVISI SmartHome controller (SHC) is offline.
     */
    private void handleResponseErrors(final ContentResponse response, final URI uri) throws IOException, ApiException {

        final int status = response.getStatus();
        if (HttpStatus.OK_200 == status) {
            logger.debug("Statuscode is OK: [{}]", uri);
        } else if (HttpStatus.SERVICE_UNAVAILABLE_503 == status) {
            logger.debug("LIVISI SmartHome service is unavailable (503).");
            throw new ServiceUnavailableException("LIVISI SmartHome service is unavailable (503).");
        } else {
            logger.debug("Statuscode {} is NOT OK: [{}]", status, uri);
            String content = response.getContentAsString();
            try {
                logger.trace("Response error content: {}", content);
                final ErrorResponseDTO error = gson.fromJson(content, ErrorResponseDTO.class);
                switch (error.getCode()) {
                    case ErrorResponseDTO.ERR_SESSION_EXISTS:
                        logger.debug("Session exists: {}", error);
                        throw new SessionExistsException(error.getDescription());
                    case ErrorResponseDTO.ERR_SESSION_NOT_FOUND:
                        logger.debug("Session not found: {}", error);
                        throw new SessionNotFoundException(error.getDescription());
                    case ErrorResponseDTO.ERR_CONTROLLER_OFFLINE:
                        logger.debug("Controller offline: {}", error);
                        throw new ControllerOfflineException(error.getDescription());
                    case ErrorResponseDTO.ERR_REMOTE_ACCESS_NOT_ALLOWED:
                        logger.debug("Remote access not allowed. Access is allowed only from the SHC device network.");
                        throw new RemoteAccessNotAllowedException(
                                "Remote access not allowed. Access is allowed only from the SHC device network.");
                    case ErrorResponseDTO.ERR_INVALID_ACTION_TRIGGERED:
                        logger.debug("Invalid action triggered. Message: {}", error.getMessages());
                        throw new InvalidActionTriggeredException(error.getDescription());
                    default:
                        logger.debug("Unknown error: {}", error);
                        throw new ApiException("Unknown error: " + error);
                }
            } catch (final JsonSyntaxException e) {
                throw new ApiException("Invalid JSON syntax in error response: " + content);
            }
        }
    }

    /**
     * Sets a new state of a SwitchActuator.
     */
    public void setSwitchActuatorState(final String capabilityId, final boolean state)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_SWITCHACTUATOR, state));
    }

    /**
     * Sets the dimmer level of a DimmerActuator.
     */
    public void setDimmerActuatorState(final String capabilityId, final int dimLevel)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_DIMMERACTUATOR, dimLevel));
    }

    /**
     * Sets the roller shutter level of a RollerShutterActuator.
     */
    public void setRollerShutterActuatorState(final String capabilityId, final int rollerShutterLevel)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR, rollerShutterLevel));
    }

    /**
     * Starts or stops moving a RollerShutterActuator
     */
    public void setRollerShutterAction(final String capabilityId, final ShutterActionType rollerShutterAction)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(), new ShutterActionDTO(capabilityId, rollerShutterAction));
    }

    /**
     * Sets a new state of a VariableActuator.
     */
    public void setVariableActuatorState(final String capabilityId, final boolean state)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_VARIABLEACTUATOR, state));
    }

    /**
     * Sets the point temperature.
     */
    public void setPointTemperatureState(final String capabilityId, final double pointTemperature)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_THERMOSTATACTUATOR, pointTemperature));
    }

    /**
     * Sets the operation mode to "Auto" or "Manu".
     */
    public void setOperationMode(final String capabilityId, final boolean isAutoMode)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetterDTO(capabilityId, CapabilityDTO.TYPE_THERMOSTATACTUATOR, isAutoMode));
    }

    /**
     * Sets the alarm state.
     */
    public void setAlarmActuatorState(final String capabilityId, final boolean alarmState)
            throws IOException, ApiException, AuthenticationException {
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
    public List<DeviceDTO> getDevices(Collection<String> deviceIds)
            throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading LIVISI SmartHome devices...");
        List<DeviceDTO> devices = executeGetList(URLCreator.createDevicesURL(bridgeConfiguration.host),
                DeviceDTO[].class);
        return devices.stream().filter(d -> isDeviceUsable(d, deviceIds)).collect(Collectors.toList());
    }

    /**
     * Loads the {@link DeviceDTO} with the given deviceId.
     */
    public DeviceDTO getDeviceById(final String deviceId) throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading device with id {}...", deviceId);
        return executeGet(URLCreator.createDeviceURL(bridgeConfiguration.host, deviceId), DeviceDTO.class);
    }

    /**
     * Loads the states for all {@link DeviceDTO}s.
     */
    public List<DeviceStateDTO> getDeviceStates() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading device states...");
        return executeGetList(URLCreator.createDeviceStatesURL(bridgeConfiguration.host), DeviceStateDTO[].class);
    }

    /**
     * Loads the device state for the given deviceId.
     */
    public StateDTO getDeviceStateByDeviceId(final String deviceId)
            throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading device states for device id {}...", deviceId);
        return executeGet(URLCreator.createDeviceStateURL(bridgeConfiguration.host, deviceId), StateDTO.class);
    }

    /**
     * Loads the locations and returns a {@link List} of {@link LocationDTO}s.
     *
     * @return a List of Devices
     */
    public List<LocationDTO> getLocations() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading locations...");
        return executeGetList(URLCreator.createLocationURL(bridgeConfiguration.host), LocationDTO[].class);
    }

    /**
     * Loads and returns a {@link List} of {@link CapabilityDTO}s for the given deviceId.
     *
     * @param deviceId the id of the {@link DeviceDTO}
     * @return capabilities of the device
     */
    public List<CapabilityDTO> getCapabilitiesForDevice(final String deviceId)
            throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading capabilities for device {}...", deviceId);
        return executeGetList(URLCreator.createDeviceCapabilitiesURL(bridgeConfiguration.host, deviceId),
                CapabilityDTO[].class);
    }

    /**
     * Loads and returns a {@link List} of all {@link CapabilityDTO}s.
     */
    public List<CapabilityDTO> getCapabilities() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading capabilities...");
        return executeGetList(URLCreator.createCapabilityURL(bridgeConfiguration.host), CapabilityDTO[].class);
    }

    /**
     * Loads and returns a {@link List} of all {@link CapabilityDTO}States.
     */
    public List<CapabilityStateDTO> getCapabilityStates() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading capability states...");
        return executeGetList(URLCreator.createCapabilityStatesURL(bridgeConfiguration.host),
                CapabilityStateDTO[].class);
    }

    /**
     * Returns a {@link List} of all {@link MessageDTO}s.
     */
    public List<MessageDTO> getMessages() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading messages...");
        return executeGetList(URLCreator.createMessageURL(bridgeConfiguration.host), MessageDTO[].class);
    }

    /**
     * @return the configVersion
     */
    public String getConfigVersion() {
        return configVersion;
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
}
