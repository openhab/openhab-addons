/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client;

import static org.openhab.binding.innogysmarthome.internal.client.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.binding.innogysmarthome.internal.client.entity.StatusResponse;
import org.openhab.binding.innogysmarthome.internal.client.entity.action.Action;
import org.openhab.binding.innogysmarthome.internal.client.entity.action.SetStateAction;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.CapabilityState;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.DeviceState;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Gateway;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.State;
import org.openhab.binding.innogysmarthome.internal.client.entity.error.ErrorResponse;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.location.Location;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;
import org.openhab.binding.innogysmarthome.internal.client.exception.ApiException;
import org.openhab.binding.innogysmarthome.internal.client.exception.ConfigurationException;
import org.openhab.binding.innogysmarthome.internal.client.exception.ControllerOfflineException;
import org.openhab.binding.innogysmarthome.internal.client.exception.InvalidActionTriggeredException;
import org.openhab.binding.innogysmarthome.internal.client.exception.InvalidAuthCodeException;
import org.openhab.binding.innogysmarthome.internal.client.exception.RemoteAccessNotAllowedException;
import org.openhab.binding.innogysmarthome.internal.client.exception.ServiceUnavailableException;
import org.openhab.binding.innogysmarthome.internal.client.exception.SessionExistsException;
import org.openhab.binding.innogysmarthome.internal.client.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The main client that handles the communication with the innogy SmartHome API service.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 *
 */
public class InnogyClient {
    private static final String BEARER = "Bearer ";
    private static final String CONTENT_TYPE = "application/json";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(InnogyClient.class);
    private InnogyConfig config;

    /**
     * date format as used in json in API. Example: 2016-07-11T10:55:52.3863424Z
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private Gateway bridgeDetails;
    private String configVersion;
    private long currentConfigurationVersion;
    private AccessTokenRefreshListener accessTokenRefreshListener;
    private long apiCallCounter = 0;
    private OAuthClientService oAuthService;

    public InnogyClient(InnogyConfig config, OAuthFactory oAuthFactory, HttpClient httpClient) {
        this.config = config;
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    /**
     * @return the bridgeInfo
     */
    public Gateway getBridgeDetails() {
        return bridgeDetails;
    }

    /**
     * Initializes the client and connects to the innogy SmartHome service via Client API. Based on the provided
     * {@Link Configuration} while constructing {@Link InnogyClient}, the given oauth2 access and refresh tokens are
     * used or - if not yet available - new tokens are fetched from the service using the provided auth code.
     *
     * Throws {@link ApiException}s or {@link IOException}s as described in {@link #getOAuth2Tokens()} and
     * {@link #getStatus()}.
     *
     * @param handle
     *
     * @throws IOException
     * @throws ApiException
     * @throws ConfigurationException
     */
    public void initialize(@NonNull String handle) throws IOException, ApiException, ConfigurationException {
        initializeHttpClient(handle);

        if (!config.checkClientData()) {
            throw new ConfigurationException("Invalid configuration: clientId and clientSecret must not be empty!");
        }

        if (!config.checkRefreshToken()) {
            // tokens missing, so try to get them via oauth2 from innogy backend
            getOAuth2Tokens();
        }
        if (!config.checkAccessToken()) {
            getAccessToken();
        }

        getStatus();
    }

    /**
     * Initializes the HTTP client
     *
     * @param handle
     */
    private void initializeHttpClient(@NonNull String handle) {
        if (accessTokenRefreshListener == null) {
            accessTokenRefreshListener = new InnogyCredentialRefreshListener(config);
        }

        oAuthService = oAuthFactory.createOAuthClientService(handle, API_URL_TOKEN, API_URL_TOKEN, config.getClientId(),
                config.getClientSecret(), null, true);
        oAuthService.addAccessTokenRefreshListener(accessTokenRefreshListener);
    }

    /**
     * Gets the status
     *
     * As the API returns the details of the SmartHome controller (SHC), the data is saved in {@link #bridgeDetails} and
     * the {@link #configVersion} is set.
     *
     * @throws SessionExistsException thrown, if a session already exists
     * @throws IOException
     * @throws ApiException
     */
    private void getStatus() throws IOException, ApiException {
        logger.debug("Get innogy SmartHome status...");
        ContentResponse response = executeGet(API_URL_STATUS);

        handleResponseErrors(response);

        StatusResponse status = gson.fromJson(response.getContentAsString(), StatusResponse.class);
        bridgeDetails = status.gateway;
        configVersion = bridgeDetails.getConfigVersion();

        logger.debug("innogy SmartHome Status loaded. Configuration version is {}.", configVersion);
    }

    /**
     * Fetches the oauth2 tokens from innogy SmartHome service and saves them in the {@Link Configuration}. The needed
     * authcode must be set in the {@Link Configuration}.
     *
     * Throws an {@link RemoteAccessNotAllowedException}, if the authcode is missing.
     * Throws an {@link ApiException} on an unexpected error with the API.
     * Throws an {@link IOException} on any I/O error.
     *
     * @throws IOException
     * @throws ApiException
     * @throws ConfigurationException thrown if the authcode is not set in the {@link InnogyConfig}
     */
    private void getOAuth2Tokens() throws IOException, ApiException, ConfigurationException {
        if (!config.checkAuthCode()) {
            throw new ConfigurationException("Invalid configuration: authcode must not be empty!");
        }

        try {
            logger.debug("Trying to get access and refresh tokens");
            final AccessTokenResponse response = oAuthService
                    .getAccessTokenResponseByAuthorizationCode(config.getAuthCode(), config.getRedirectUrl());

            logger.debug("Saving access and refresh tokens.");
            logger.trace("Access token: {}", response.getAccessToken());
            logger.trace("Refresh token: {}", response.getRefreshToken());
            config.setAccessToken(response.getAccessToken());
            config.setRefreshToken(response.getRefreshToken());
        } catch (OAuthException | OAuthResponseException e) {
            throw new InvalidAuthCodeException("Error fetching access token: " + e.getMessage());
        }
    }

    /**
     * Fetches the access token from innogy SmartHome service and saves it in the {@Link Configuration}. The needed
     * refreshToken must be set in the {@Link Configuration}.
     *
     * Throws an {@link RemoteAccessNotAllowedException}, if the authcode is missing.
     * Throws an {@link ApiException} on an unexpected error with the API.
     * Throws an {@link IOException} on any I/O error.
     *
     * @throws IOException
     * @throws ApiException
     * @throws ConfigurationException thrown if the refreshToken is not set in the {@link InnogyConfig}
     */
    private void getAccessToken() throws IOException, ConfigurationException {
        if (!config.checkRefreshToken()) {
            throw new ConfigurationException("Invalid configuration: refreshToken must not be empty!");
        }

        logger.debug("Trying to get access token");
        AccessTokenResponse response;
        try {
            response = oAuthService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException e) {
            throw new ConfigurationException("No innogy accesstoken. Is this thing authorized: " + e.getMessage());
        }
        logger.debug("Saving access token.");
        config.setAccessToken(response.getAccessToken());
    }

    /**
     * Executes a HTTP GET request with default headers.
     *
     * @param url
     * @return
     * @throws IOException
     */
    private ContentResponse executeGet(String url) throws IOException {
        apiCallCounter++;
        return request(httpClient.newRequest(url).method(HttpMethod.GET));
    }

    /**
     * Executes a HTTP POST request with the given {@link Action} as content.
     *
     * @param url
     * @param action
     * @return
     * @throws IOException
     */
    private ContentResponse executePost(String url, Action action) throws IOException {
        apiCallCounter++;
        return executePost(url, gson.toJson(action));
    }

    /**
     * Executes a HTTP POST request with JSON formatted content.
     *
     * @param url
     * @param content
     * @return
     * @throws IOException
     */
    private ContentResponse executePost(String url, String content) throws IOException {
        apiCallCounter++;
        return request(httpClient.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(content), CONTENT_TYPE).accept("application/json"));
    }

    private ContentResponse request(Request request) throws IOException {
        AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException e) {
            throw new IOException("Error fetching access token: " + e.getMessage());
        }
        final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            // throw new ConfigurationException("No innogy accesstoken. Is this thing authorized?");
            throw new IOException("No innogy accesstoken. Is this thing authorized?");
        }

        try {
            return request.header("Accept", CONTENT_TYPE).header(AUTHORIZATION_HEADER, BEARER + accessToken)
                    .timeout(HTTP_CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    /**
     * Handles errors from the {@link ContentResponse} and throws the following errors:
     *
     * @param response
     * @throws SessionExistsException
     * @throws SessionNotFoundException
     * @throws ControllerOfflineException thrown, if the innogy SmartHome controller (SHC) is offline.
     * @throws IOException
     * @throws ApiException
     */
    private void handleResponseErrors(ContentResponse response) throws IOException, ApiException {
        String content = "";

        switch (response.getStatus()) {
            case HttpStatus.OK_200:
                logger.debug("[{}] Statuscode is OK.", apiCallCounter);
                return;
            case HttpStatus.SERVICE_UNAVAILABLE_503:
                logger.debug("innogy service is unavailabe (503).");
                throw new ServiceUnavailableException("innogy service is unavailabe (503).");
            default:
                logger.debug("[{}] Statuscode is NOT OK: {}", apiCallCounter, response.getStatus());
                try {
                    content = response.getContentAsString();
                    logger.trace("Response error content: {}", content);
                    ErrorResponse error = gson.fromJson(content, ErrorResponse.class);

                    if (error == null) {
                        logger.debug("Error without JSON message, code: {} / message: {}", response.getStatus(),
                                response.getReason());
                        throw new ApiException("Error code: " + response.getStatus());
                    }

                    switch (error.getCode()) {
                        case ErrorResponse.ERR_SESSION_EXISTS:
                            logger.debug("Session exists: {}", error.toString());
                            throw new SessionExistsException(error.getDescription());
                        case ErrorResponse.ERR_SESSION_NOT_FOUND:
                            logger.debug("Session not found: {}", error.toString());
                            throw new SessionNotFoundException(error.getDescription());
                        case ErrorResponse.ERR_CONTROLLER_OFFLINE:
                            logger.debug("Controller offline: {}", error.toString());
                            throw new ControllerOfflineException(error.getDescription());
                        case ErrorResponse.ERR_REMOTE_ACCESS_NOT_ALLOWED:
                            logger.debug(
                                    "Remote access not allowed. Access is allowed only from the SHC device network.");
                            throw new RemoteAccessNotAllowedException(
                                    "Remote access not allowed. Access is allowed only from the SHC device network.");
                        case ErrorResponse.ERR_INVALID_ACTION_TRIGGERED:
                            logger.error("Invalid action triggered. Message: {}", error.getMessages());
                            throw new InvalidActionTriggeredException(error.getDescription());
                        default:
                            logger.debug("Unknown error: {}", error.toString());
                            throw new ApiException("Unknown error: " + error.toString());
                    }
                } catch (JsonSyntaxException e) {
                    throw new ApiException("Invalid JSON syntax in error response: " + content);
                }
        }
    }

    /**
     * Sets a new state of a SwitchActuator.
     *
     * @param capabilityId
     * @param state
     * @throws IOException
     * @throws ApiException
     */
    public void setSwitchActuatorState(String capabilityId, boolean state) throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_SWITCHACTUATOR, state);

        String json = gson.toJson(action);
        logger.debug("Action toggle JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Sets the dimmer level of a DimmerActuator.
     *
     * @param capabilityId
     * @param dimLevel
     * @throws IOException
     * @throws ApiException
     */
    public void setDimmerActuatorState(String capabilityId, int dimLevel) throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_DIMMERACTUATOR, dimLevel);

        String json = gson.toJson(action);
        logger.debug("Action dimm JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Sets the roller shutter level of a RollerShutterActuator.
     *
     * @param capabilityId
     * @param rollerShutterLevel
     * @throws IOException
     * @throws ApiException
     */
    public void setRollerShutterActuatorState(String capabilityId, int rollerShutterLevel)
            throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_ROLLERSHUTTERACTUATOR, rollerShutterLevel);

        String json = gson.toJson(action);
        logger.debug("Action rollershutter JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Sets a new state of a VariableActuator.
     *
     * @param capabilityId
     * @param state
     * @throws IOException
     * @throws ApiException
     */
    public void setVariableActuatorState(String capabilityId, boolean state) throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_VARIABLEACTUATOR, state);

        String json = gson.toJson(action);
        logger.debug("Action toggle JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Sets the point temperature.
     *
     * @param capabilityId
     * @param pointTemperature
     * @throws IOException
     * @throws ApiException
     */
    public void setPointTemperatureState(String capabilityId, double pointTemperature)
            throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_THERMOSTATACTUATOR, pointTemperature);

        String json = gson.toJson(action);
        logger.debug("Action toggle JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Sets the operation mode to "Auto" or "Manu".
     *
     * @param capabilityId
     * @param autoMode
     * @throws IOException
     * @throws ApiException
     */
    public void setOperationMode(String capabilityId, boolean autoMode) throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_THERMOSTATACTUATOR,
                autoMode ? "Auto" : "Manu");

        String json = gson.toJson(action);
        logger.debug("Action toggle JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Sets the alarm state.
     *
     * @param capabilityId
     * @param alarmState
     * @throws IOException
     * @throws ApiException
     */
    public void setAlarmActuatorState(String capabilityId, boolean alarmState) throws IOException, ApiException {
        Action action = new SetStateAction(capabilityId, Capability.TYPE_ALARMACTUATOR, alarmState);

        String json = gson.toJson(action);
        logger.debug("Action toggle JSON: {}", json);

        ContentResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Load the device and returns a {@link List} of {@link Device}s..
     *
     * @return List of Devices
     * @throws IOException
     * @throws ApiException
     */
    public List<Device> getDevices() throws IOException, ApiException {
        logger.debug("Loading innogy devices...");
        ContentResponse response = executeGet(API_URL_DEVICE);

        handleResponseErrors(response);

        Device[] deviceList = gson.fromJson(response.getContentAsString(), Device[].class);
        return Arrays.asList(deviceList);
    }

    /**
     * Loads the {@link Device} with the given deviceId.
     *
     * @param deviceId
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public Device getDeviceById(String deviceId) throws IOException, ApiException {
        logger.debug("Loading device with id {}...", deviceId);
        ContentResponse response = executeGet(API_URL_DEVICE_ID.replace("{id}", deviceId));

        handleResponseErrors(response);

        return gson.fromJson(response.getContentAsString(), Device.class);
    }

    /**
     * Returns a {@link List} of all {@link Device}s with the full configuration details, {@link Capability}s and
     * states. Calling this may take a while...
     *
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<Device> getFullDevices() throws IOException, ApiException {
        // LOCATIONS
        List<Location> locationList = getLocations();
        Map<String, Location> locationMap = new HashMap<>();
        for (Location l : locationList) {
            locationMap.put(l.getId(), l);
        }

        // CAPABILITIES
        List<Capability> capabilityList = getCapabilities();
        Map<String, Capability> capabilityMap = new HashMap<>();
        for (Capability c : capabilityList) {
            capabilityMap.put(c.getId(), c);
        }

        // CAPABILITY STATES
        List<CapabilityState> capabilityStateList = getCapabilityStates();
        Map<String, CapabilityState> capabilityStateMap = new HashMap<>();
        for (CapabilityState cs : capabilityStateList) {
            capabilityStateMap.put(cs.getId(), cs);
        }

        // DEVICE STATES
        List<DeviceState> deviceStateList = getDeviceStates();
        Map<String, DeviceState> deviceStateMap = new HashMap<>();
        for (DeviceState es : deviceStateList) {
            deviceStateMap.put(es.getId(), es);
        }

        // MESSAGES
        List<Message> messageList = getMessages();
        Map<String, List<Message>> deviceMessageMap = new HashMap<>();
        for (Message m : messageList) {
            if (m.getDeviceLinkList() != null && !m.getDeviceLinkList().isEmpty()) {
                String deviceId = m.getDeviceLinkList().get(0).replace("/device/", "");
                List<Message> ml;
                if (deviceMessageMap.containsKey(deviceId)) {
                    ml = deviceMessageMap.get(deviceId);
                } else {
                    ml = new ArrayList<Message>();
                }
                ml.add(m);
                deviceMessageMap.put(deviceId, ml);
            }
        }

        // DEVICES
        List<Device> deviceList = getDevices();
        for (Device d : deviceList) {
            if (BATTERY_POWERED_DEVICES.contains(d.getType())) {
                d.setIsBatteryPowered(true);
            }

            // location
            d.setLocation(locationMap.get(d.getLocationId()));
            HashMap<String, Capability> deviceCapabilityMap = new HashMap<>();

            // capabilities and their states
            for (String cl : d.getCapabilityLinkList()) {
                Capability c = capabilityMap.get(Link.getId(cl));
                String capabilityId = c.getId();
                CapabilityState capabilityState = capabilityStateMap.get(capabilityId);
                c.setCapabilityState(capabilityState);
                deviceCapabilityMap.put(capabilityId, c);
            }
            d.setCapabilityMap(deviceCapabilityMap);

            // device states
            d.setDeviceState(deviceStateMap.get(d.getId()));

            // messages
            if (deviceMessageMap.containsKey(d.getId())) {
                d.setMessageList(deviceMessageMap.get(d.getId()));
                for (Message m : d.getMessageList()) {
                    switch (m.getType()) {
                        case Message.TYPE_DEVICE_LOW_BATTERY:
                            d.setLowBattery(true);
                            d.setLowBatteryMessageId(m.getId());
                            break;
                    }
                }
            }
        }

        return deviceList;
    }

    /**
     * Returns the {@link Device} with the given deviceId with full configuration details, {@link Capability}s and
     * states. Calling this may take a little bit longer...
     *
     * @param deviceId
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public Device getFullDeviceById(String deviceId) throws IOException, ApiException {
        // LOCATIONS
        List<Location> locationList = getLocations();
        Map<String, Location> locationMap = new HashMap<>();
        for (Location l : locationList) {
            locationMap.put(l.getId(), l);
        }

        // CAPABILITIES FOR DEVICE
        List<Capability> capabilityList = getCapabilitiesForDevice(deviceId);
        Map<String, Capability> capabilityMap = new HashMap<>();
        for (Capability c : capabilityList) {
            capabilityMap.put(c.getId(), c);
        }

        // CAPABILITY STATES
        List<CapabilityState> capabilityStateList = getCapabilityStates();
        Map<String, CapabilityState> capabilityStateMap = new HashMap<>();
        for (CapabilityState cs : capabilityStateList) {
            capabilityStateMap.put(cs.getId(), cs);
        }

        // DEVICE STATE
        State state = getDeviceStateByDeviceId(deviceId);
        DeviceState deviceState = new DeviceState();
        deviceState.setId(deviceId);
        deviceState.setState(state);

        // deviceState.setStateList(deviceStateList);

        // MESSAGES
        List<Message> messageList = getMessages();

        List<Message> ml = new ArrayList<>();

        for (Message m : messageList) {
            logger.trace("Message Type {} with ID {}", m.getType(), m.getId());
            if (m.getDeviceLinkList() != null && !m.getDeviceLinkList().isEmpty()) {
                for (String li : m.getDeviceLinkList()) {
                    if (li.equals("/device/" + deviceId)) {
                        ml.add(m);
                    }
                }
            }
        }

        // DEVICE
        Device d = getDeviceById(deviceId);
        if (BATTERY_POWERED_DEVICES.contains(d.getType())) {
            d.setIsBatteryPowered(true);
            d.setLowBattery(false);
        }

        // location
        d.setLocation(locationMap.get(d.getLocationId()));

        // capabilities and their states
        HashMap<String, Capability> deviceCapabilityMap = new HashMap<>();
        for (String cl : d.getCapabilityLinkList()) {

            Capability c = capabilityMap.get(Link.getId(cl));
            c.setCapabilityState(capabilityStateMap.get(c.getId()));
            deviceCapabilityMap.put(c.getId(), c);

        }
        d.setCapabilityMap(deviceCapabilityMap);

        // device states
        d.setDeviceState(deviceState);

        // messages
        if (ml.size() > 0) {
            d.setMessageList(ml);
            for (Message m : d.getMessageList()) {
                switch (m.getType()) {
                    case Message.TYPE_DEVICE_LOW_BATTERY:
                        d.setLowBattery(true);
                        d.setLowBatteryMessageId(m.getId());
                        break;
                }
            }
        }

        return d;
    }

    /**
     * Loads the states for all {@link Device}s.
     *
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<DeviceState> getDeviceStates() throws IOException, ApiException {
        logger.debug("Loading device states...");

        ContentResponse response = executeGet(API_URL_DEVICE_STATES);

        handleResponseErrors(response);

        DeviceState[] deviceStateArray = gson.fromJson(response.getContentAsString(), DeviceState[].class);
        return Arrays.asList(deviceStateArray);
    }

    /**
     * Loads the device state for the given deviceId.
     *
     * @param deviceId
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public State getDeviceStateByDeviceId(String deviceId) throws IOException, ApiException {
        logger.debug("Loading device states for device id {}...", deviceId);

        ContentResponse response = executeGet(API_URL_DEVICE_ID_STATE.replace("{id}", deviceId));

        handleResponseErrors(response);

        State state = gson.fromJson(response.getContentAsString(), State.class);

        return state;
    }

    /**
     * Loads the locations and returns a {@link List} of {@link Location}s.
     *
     * @return a List of Devices
     * @throws IOException
     * @throws ApiException
     */
    public List<Location> getLocations() throws IOException, ApiException {
        logger.debug("Loading locations...");

        ContentResponse response = executeGet(API_URL_LOCATION);

        handleResponseErrors(response);

        Location[] locationArray = gson.fromJson(response.getContentAsString(), Location[].class);
        List<Location> locationList = Arrays.asList(locationArray);

        return locationList;
    }

    /**
     * Loads and returns a {@link List} of {@link Capability}s for the given deviceId.
     *
     * @param deviceId the id of the {@link Device}
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<Capability> getCapabilitiesForDevice(String deviceId) throws IOException, ApiException {
        logger.debug("Loading capabilities for device {}...", deviceId);

        ContentResponse response = executeGet(API_URL_DEVICE_CAPABILITIES.replace("{id}", deviceId));

        handleResponseErrors(response);

        Capability[] capabilityArray = gson.fromJson(response.getContentAsString(), Capability[].class);
        List<Capability> capabilityList = Arrays.asList(capabilityArray);
        return capabilityList;
    }

    /**
     * Loads and returns a {@link List} of all {@link Capability}s.
     *
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<Capability> getCapabilities() throws IOException, ApiException {
        logger.debug("Loading capabilities...");

        ContentResponse response = executeGet(API_URL_CAPABILITY);

        handleResponseErrors(response);

        Capability[] capabilityArray = gson.fromJson(response.getContentAsString(), Capability[].class);
        List<Capability> capabilityList = Arrays.asList(capabilityArray);
        return capabilityList;
    }

    /**
     * Loads and returns a {@link List} of all {@link Capability}States.
     *
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<CapabilityState> getCapabilityStates() throws IOException, ApiException {
        logger.debug("Loading capability states...");

        ContentResponse response = executeGet(API_URL_CAPABILITY_STATES);

        handleResponseErrors(response);

        CapabilityState[] capabilityStatesArray = gson.fromJson(response.getContentAsString(), CapabilityState[].class);
        List<CapabilityState> capabilityStatesList = Arrays.asList(capabilityStatesArray);
        return capabilityStatesList;
    }

    /**
     * Returns a {@link List} of all {@link Message}s.
     *
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<Message> getMessages() throws IOException, ApiException {
        logger.debug("Loading messages...");

        ContentResponse response = executeGet(API_URL_MESSAGE);

        handleResponseErrors(response);

        Message[] messageArray = gson.fromJson(response.getContentAsString(), Message[].class);
        List<Message> messageList = Arrays.asList(messageArray);
        return messageList;
    }

    /**
     * Returns the {@link InnogyConfig}. Be aware that
     *
     * @return the Configuration
     */
    public InnogyConfig getConfig() {
        return config;
    }

    /**
     * @param accessTokenRefreshListener the accessTokenRefreshListener to set
     */
    public void setAccessTokenRefreshListener(AccessTokenRefreshListener accessTokenRefreshListener) {
        this.accessTokenRefreshListener = accessTokenRefreshListener;
    }

    /**
     * @return the configVersion
     */
    public String getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the configVersion to set
     */
    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }
}
