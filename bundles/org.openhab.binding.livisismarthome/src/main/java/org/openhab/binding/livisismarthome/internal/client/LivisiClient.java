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
import org.openhab.binding.livisismarthome.internal.client.entity.StatusResponse;
import org.openhab.binding.livisismarthome.internal.client.entity.action.Action;
import org.openhab.binding.livisismarthome.internal.client.entity.action.ShutterAction;
import org.openhab.binding.livisismarthome.internal.client.entity.action.StateActionSetter;
import org.openhab.binding.livisismarthome.internal.client.entity.auth.LivisiAccessTokenRequest;
import org.openhab.binding.livisismarthome.internal.client.entity.auth.LivisiAccessTokenResponse;
import org.openhab.binding.livisismarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.livisismarthome.internal.client.entity.capability.CapabilityState;
import org.openhab.binding.livisismarthome.internal.client.entity.device.Device;
import org.openhab.binding.livisismarthome.internal.client.entity.device.DeviceState;
import org.openhab.binding.livisismarthome.internal.client.entity.device.Gateway;
import org.openhab.binding.livisismarthome.internal.client.entity.device.State;
import org.openhab.binding.livisismarthome.internal.client.entity.error.ErrorResponse;
import org.openhab.binding.livisismarthome.internal.client.entity.location.Location;
import org.openhab.binding.livisismarthome.internal.client.entity.message.Message;
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
 * The main client that handles the communication with the Livisi SmartHome API service.
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
    private static final int HTTP_REQUEST_IDLE_TIMEOUT_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(LivisiClient.class);

    /**
     * date format as used in json in API. Example: 2016-07-11T10:55:52.3863424Z
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    private final LivisiBridgeConfiguration bridgeConfiguration;
    private final OAuthClientService oAuthService;
    private final HttpClient httpClient;
    private @Nullable Gateway bridgeDetails;
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
     * As the API returns the details of the SmartHome controller (SHC), the data is saved in {@link #bridgeDetails} and
     * the {@link #configVersion} is set.
     *
     * @throws SessionExistsException thrown, if a session already exists
     */
    public void refreshStatus() throws IOException, ApiException, AuthenticationException {
        logger.debug("Get Livisi SmartHome status...");
        final StatusResponse status = executeGet(URLCreator.createStatusURL(bridgeConfiguration.host),
                StatusResponse.class);

        bridgeDetails = status.gateway;
        configVersion = bridgeDetails.getConfigVersion();

        logger.debug("Livisi SmartHome Status loaded. Configuration version is {}.", configVersion);
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
     * Executes a HTTP POST request with the given {@link Action} as content.
     *
     * @param url request URL
     * @param action action to execute
     */
    private void executePost(final String url, final Action action)
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
                    .idleTimeout(HTTP_REQUEST_IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .timeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IOException(e);
        }
        handleResponseErrors(response, request.getURI());
        return response;
    }

    public AccessTokenResponse login() throws AuthenticationException, IOException, ApiException {
        LivisiAccessTokenRequest requestBody = new LivisiAccessTokenRequest();
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
                    .idleTimeout(HTTP_REQUEST_IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .timeout(HTTP_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
        handleResponseErrors(response, request.getURI());

        LivisiAccessTokenResponse tokenResponse = gson.fromJson(response.getContentAsString(),
                LivisiAccessTokenResponse.class);
        if (tokenResponse == null) {
            throw new AuthenticationException("The access token response couldn't get parsed!");
        }
        return tokenResponse.createAccessTokenResponse();
    }

    public AccessTokenResponse getAccessTokenResponse() throws AuthenticationException, IOException {
        @Nullable
        final AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException e) {
            throw new AuthenticationException("Error fetching access token: " + e.getMessage());
        }
        if (accessTokenResponse == null || accessTokenResponse.getAccessToken() == null
                || accessTokenResponse.getAccessToken().isBlank()) {
            throw new AuthenticationException("No Livisi access token. Is this thing authorized?");
        }
        return accessTokenResponse;
    }

    /**
     * Handles errors from the {@link ContentResponse} and throws the following errors:
     *
     * @param response response
     * @param uri uri of api call made
     * @throws ControllerOfflineException thrown, if the Livisi SmartHome controller (SHC) is offline.
     */
    private void handleResponseErrors(final ContentResponse response, final URI uri) throws IOException, ApiException {
        String content = "";

        switch (response.getStatus()) {
            case HttpStatus.OK_200:
                logger.debug("Statuscode is OK: [{}]", uri);
                return;
            case HttpStatus.SERVICE_UNAVAILABLE_503:
                logger.debug("Livisi service is unavailabe (503).");
                throw new ServiceUnavailableException("Livisi service is unavailabe (503).");
            default:
                logger.debug("Statuscode {} is NOT OK: [{}]", response.getStatus(), uri);
                try {
                    content = response.getContentAsString();
                    logger.trace("Response error content: {}", content);
                    final ErrorResponse error = gson.fromJson(content, ErrorResponse.class);

                    if (error == null) {
                        logger.debug("Error without JSON message, code: {} / message: {}", response.getStatus(),
                                response.getReason());
                        throw new ApiException("Error code: " + response.getStatus());
                    }

                    switch (error.getCode()) {
                        case ErrorResponse.ERR_SESSION_EXISTS:
                            logger.debug("Session exists: {}", error);
                            throw new SessionExistsException(error.getDescription());
                        case ErrorResponse.ERR_SESSION_NOT_FOUND:
                            logger.debug("Session not found: {}", error);
                            throw new SessionNotFoundException(error.getDescription());
                        case ErrorResponse.ERR_CONTROLLER_OFFLINE:
                            logger.debug("Controller offline: {}", error);
                            throw new ControllerOfflineException(error.getDescription());
                        case ErrorResponse.ERR_REMOTE_ACCESS_NOT_ALLOWED:
                            logger.debug(
                                    "Remote access not allowed. Access is allowed only from the SHC device network.");
                            throw new RemoteAccessNotAllowedException(
                                    "Remote access not allowed. Access is allowed only from the SHC device network.");
                        case ErrorResponse.ERR_INVALID_ACTION_TRIGGERED:
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
        executePost(createActionURL(), new StateActionSetter(capabilityId, Capability.TYPE_SWITCHACTUATOR, state));
    }

    /**
     * Sets the dimmer level of a DimmerActuator.
     */
    public void setDimmerActuatorState(final String capabilityId, final int dimLevel)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(), new StateActionSetter(capabilityId, Capability.TYPE_DIMMERACTUATOR, dimLevel));
    }

    /**
     * Sets the roller shutter level of a RollerShutterActuator.
     */
    public void setRollerShutterActuatorState(final String capabilityId, final int rollerShutterLevel)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetter(capabilityId, Capability.TYPE_ROLLERSHUTTERACTUATOR, rollerShutterLevel));
    }

    /**
     * Starts or stops moving a RollerShutterActuator
     */
    public void setRollerShutterAction(final String capabilityId,
            final ShutterAction.ShutterActions rollerShutterAction)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(), new ShutterAction(capabilityId, rollerShutterAction));
    }

    /**
     * Sets a new state of a VariableActuator.
     */
    public void setVariableActuatorState(final String capabilityId, final boolean state)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(), new StateActionSetter(capabilityId, Capability.TYPE_VARIABLEACTUATOR, state));
    }

    /**
     * Sets the point temperature.
     */
    public void setPointTemperatureState(final String capabilityId, final double pointTemperature)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetter(capabilityId, Capability.TYPE_THERMOSTATACTUATOR, pointTemperature));
    }

    /**
     * Sets the operation mode to "Auto" or "Manu".
     */
    public void setOperationMode(final String capabilityId, final boolean autoMode)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(),
                new StateActionSetter(capabilityId, Capability.TYPE_THERMOSTATACTUATOR,
                        autoMode ? CapabilityState.STATE_VALUE_OPERATION_MODE_AUTO
                                : CapabilityState.STATE_VALUE_OPERATION_MODE_MANUAL));
    }

    /**
     * Sets the alarm state.
     */
    public void setAlarmActuatorState(final String capabilityId, final boolean alarmState)
            throws IOException, ApiException, AuthenticationException {
        executePost(createActionURL(), new StateActionSetter(capabilityId, Capability.TYPE_ALARMACTUATOR, alarmState));
    }

    /**
     * Load the device and returns a {@link List} of {@link Device}s.
     * VariableActuators are returned additionally (independent from the device ids),
     * because VariableActuators are everytime available and never have a device state.
     *
     * @param deviceIds Ids of the devices to return
     * @return List of Devices
     */
    public List<Device> getDevices(Collection<String> deviceIds)
            throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading Livisi devices...");
        List<Device> devices = executeGetList(URLCreator.createDevicesURL(bridgeConfiguration.host), Device[].class);
        return devices.stream().filter(d -> isDeviceUsable(d, deviceIds)).collect(Collectors.toList());
    }

    /**
     * Loads the {@link Device} with the given deviceId.
     */
    public Device getDeviceById(final String deviceId) throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading device with id {}...", deviceId);
        return executeGet(URLCreator.createDeviceURL(bridgeConfiguration.host, deviceId), Device.class);
    }

    /**
     * Loads the states for all {@link Device}s.
     */
    public List<DeviceState> getDeviceStates() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading device states...");
        return executeGetList(URLCreator.createDeviceStatesURL(bridgeConfiguration.host), DeviceState[].class);
    }

    /**
     * Loads the device state for the given deviceId.
     */
    public State getDeviceStateByDeviceId(final String deviceId)
            throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading device states for device id {}...", deviceId);
        return executeGet(URLCreator.createDeviceStateURL(bridgeConfiguration.host, deviceId), State.class);
    }

    /**
     * Loads the locations and returns a {@link List} of {@link Location}s.
     *
     * @return a List of Devices
     */
    public List<Location> getLocations() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading locations...");
        return executeGetList(URLCreator.createLocationURL(bridgeConfiguration.host), Location[].class);
    }

    /**
     * Loads and returns a {@link List} of {@link Capability}s for the given deviceId.
     *
     * @param deviceId the id of the {@link Device}
     * @return capabilities of the device
     */
    public List<Capability> getCapabilitiesForDevice(final String deviceId)
            throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading capabilities for device {}...", deviceId);
        return executeGetList(URLCreator.createDeviceCapabilitiesURL(bridgeConfiguration.host, deviceId),
                Capability[].class);
    }

    /**
     * Loads and returns a {@link List} of all {@link Capability}s.
     */
    public List<Capability> getCapabilities() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading capabilities...");
        return executeGetList(URLCreator.createCapabilityURL(bridgeConfiguration.host), Capability[].class);
    }

    /**
     * Loads and returns a {@link List} of all {@link Capability}States.
     */
    public List<CapabilityState> getCapabilityStates() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading capability states...");
        return executeGetList(URLCreator.createCapabilityStatesURL(bridgeConfiguration.host), CapabilityState[].class);
    }

    /**
     * Returns a {@link List} of all {@link Message}s.
     */
    public List<Message> getMessages() throws IOException, ApiException, AuthenticationException {
        logger.debug("Loading messages...");
        return executeGetList(URLCreator.createMessageURL(bridgeConfiguration.host), Message[].class);
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
    private static boolean isDeviceUsable(Device device, Collection<String> activeDeviceIds) {
        return activeDeviceIds.contains(device.getId())
                || LivisiBindingConstants.DEVICE_VARIABLE_ACTUATOR.equals(device.getType());
    }
}
