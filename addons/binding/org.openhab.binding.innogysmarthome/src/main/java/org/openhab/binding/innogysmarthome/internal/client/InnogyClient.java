/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client;

import static org.openhab.binding.innogysmarthome.internal.client.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.innogysmarthome.internal.client.entity.Location;
import org.openhab.binding.innogysmarthome.internal.client.entity.Message;
import org.openhab.binding.innogysmarthome.internal.client.entity.Property;
import org.openhab.binding.innogysmarthome.internal.client.entity.SHCInfo;
import org.openhab.binding.innogysmarthome.internal.client.entity.action.Action;
import org.openhab.binding.innogysmarthome.internal.client.entity.action.SetStateAction;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.error.ErrorResponse;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.CapabilityLink;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.CapabilityState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DeviceState;
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

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.Credential.Builder;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The main client that handles the communication with the innogy SmartHome API service.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class InnogyClient {
    private final Logger logger = LoggerFactory.getLogger(InnogyClient.class);
    private InnogyConfig config;

    /**
     * date format as used in json in API. Example: 2016-07-11T10:55:52.3863424Z
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private Builder credentialBuilder;
    private HttpRequestFactory requestFactory;
    private Device bridgeDetails;
    private long currentConfigurationVersion;
    private CredentialRefreshListener credentialRefreshListener;
    private long apiCallCounter = 0;

    public InnogyClient(InnogyConfig config) {
        this.config = config;
    }

    /**
     * @return the bridgeInfo
     */
    public Device getBridgeDetails() {
        return bridgeDetails;
    }

    /**
     * Initializes the client and connects to the innogy SmartHome service via Client API. Based on the provided
     * {@Link Configuration} while constructing {@Link InnogyClient}, the given oauth2 access and refresh tokens are
     * used or - if not yet available - new tokens are fetched from the service using the provided auth code.
     *
     * Throws {@link ApiException}s or {@link IOException}s as described in {@link #getOAuth2Tokens()} and
     * {@link #initializeSession()}.
     *
     * @throws IOException
     * @throws ApiException
     * @throws ConfigurationException
     */
    public void initialize() throws IOException, ApiException, ConfigurationException {
        initializeHttpClient();

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

        initializeSession();
    }

    /**
     * Initializes the HTTP client
     */
    private void initializeHttpClient() {
        httpTransport = new NetHttpTransport();
        jsonFactory = new JacksonFactory();

        if (credentialRefreshListener == null) {
            credentialRefreshListener = new InnogyCredentialRefreshListener(config);
        }

        // prepare credentials & transport for oauth2 access
        credentialBuilder = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport).setJsonFactory(jsonFactory).addRefreshListener(credentialRefreshListener)
                .setTokenServerUrl(new GenericUrl(API_URL_TOKEN))
                .setClientAuthentication(new BasicAuthentication(config.getClientId(), config.getClientSecret()));
        Credential credential = new Credential(credentialBuilder) {
            @Override
            public void initialize(HttpRequest request) {
                request.setInterceptor(this);
                request.setUnsuccessfulResponseHandler(this);
                request.setThrowExceptionOnExecuteError(false);
                request.setParser(new JsonObjectParser(jsonFactory)); // TODO: maybe better remove this one and use GSON
                                                                      // manually
                setAccessToken(config.getAccessToken());
                setRefreshToken(config.getRefreshToken());
            }
        };

        requestFactory = httpTransport.createRequestFactory(credential);
    }

    /**
     * Logs into the innogy SmartHome service and initializes the session.
     *
     * As the API returns the details of the SmartHome controller (SHC), the data is saved in {@link #bridgeDetails} and
     * the {@link #currentConfigurationVersion} is set.
     *
     * @throws SessionExistsException thrown, if a session already exists
     * @throws IOException
     * @throws ApiException
     */
    private void initializeSession() throws IOException, ApiException {
        logger.debug("Initializing innogy SmartHome Session...");
        HttpResponse response = executeGet(API_URL_INITIALIZE);

        handleResponseErrors(response);

        SHCInfo info = response.parseAs(SHCInfo.class);
        bridgeDetails = info.deviceList.get(0);
        currentConfigurationVersion = info.currentConfigurationVersion;

        logger.debug("innogy SmartHome Session initialized. Configuration version is {}",
                info.currentConfigurationVersion);
    }

    /**
     * Uninitializes the session.
     *
     * @throws IOException
     * @throws ApiException
     */
    public void uninitializeSession() throws IOException, ApiException {
        logger.debug("Uninitializing innogy SmartHome Session...");
        bridgeDetails = null;

        HttpResponse response = executeGet(API_URL_UNINITIALIZE);

        try {
            handleResponseErrors(response);
        } catch (SessionNotFoundException e) {
            // Session not found - ignoring
        }
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
            TokenResponse response = new AuthorizationCodeTokenRequest(httpTransport, jsonFactory,
                    new GenericUrl(API_URL_TOKEN), config.getAuthCode()).setRedirectUri(config.getRedirectUrl())
                            .setClientAuthentication(
                                    new BasicAuthentication(config.getClientId(), config.getClientSecret()))
                            .execute();
            logger.debug("Saving access and refresh tokens.");
            logger.trace("Access token: {}", response.getAccessToken());
            logger.trace("Refresh token: {}", response.getRefreshToken());
            config.setAccessToken(response.getAccessToken());
            config.setRefreshToken(response.getRefreshToken());
        } catch (TokenResponseException e) {
            throw new InvalidAuthCodeException("Error fetching access token: " + e.getDetails());
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
        TokenResponse response = new RefreshTokenRequest(httpTransport, jsonFactory, new GenericUrl(API_URL_TOKEN),
                config.getRefreshToken())
                        .setClientAuthentication(
                                new BasicAuthentication(config.getClientId(), config.getClientSecret()))
                        .execute();
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
    private HttpResponse executeGet(String url) throws IOException {
        apiCallCounter++;
        return requestFactory.buildGetRequest(new GenericUrl(url)).execute();
    }

    /**
     * Executes a HTTP GET request with custom headers.
     *
     * @param url
     * @param headers
     * @return
     * @throws IOException
     */
    private HttpResponse executeGet(String url, HttpHeaders headers) throws IOException {
        apiCallCounter++;
        return requestFactory.buildGetRequest(new GenericUrl(url)).setHeaders(headers).execute();
    }

    /**
     * Executes a HTTP POST request with the given {@link Action} as content.
     *
     * @param url
     * @param action
     * @return
     * @throws IOException
     */
    private HttpResponse executePost(String url, Action action) throws IOException {
        apiCallCounter++;
        return executePost(url, new JsonHttpContent(jsonFactory, action));
    }

    /**
     * Executes a HTTP POST request with JSON formatted content.
     *
     * @param url
     * @param content
     * @return
     * @throws IOException
     */
    private HttpResponse executePost(String url, JsonHttpContent content) throws IOException {
        apiCallCounter++;
        return requestFactory.buildPostRequest(new GenericUrl(url), content)
                .setHeaders(new HttpHeaders().setAccept("application/json")).execute();
    }

    /**
     * Handles errors from the {@link HttpResponse} and throws the following errors:
     *
     * @param response
     * @throws SessionExistsException
     * @throws SessionNotFoundException
     * @throws ControllerOfflineException thrown, if the innogy SmartHome controller (SHC) is offline.
     * @throws IOException
     * @throws ApiException
     */
    private void handleResponseErrors(HttpResponse response) throws IOException, ApiException {
        String content = "";

        switch (response.getStatusCode()) {
            case HttpStatusCodes.STATUS_CODE_OK:
                logger.debug("[{}] Statuscode is OK.", apiCallCounter);
                return;
            case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE:
                logger.debug("innogy service is unavailabe (503).");
                throw new ServiceUnavailableException("innogy service is unavailabe (503).");
            default:
                logger.debug("[{}] Statuscode is NOT OK: {}", apiCallCounter, response.getStatusCode());
                try {
                    content = IOUtils.toString(response.getContent());
                    logger.trace("Response error content: {}", content);
                    ErrorResponse error = gson.fromJson(content, ErrorResponse.class);

                    if (error == null) {
                        logger.debug("Error without JSON message, code: {} / message: {}", response.getStatusCode(),
                                response.getStatusMessage());
                        throw new ApiException("Error code: " + response.getStatusCode());
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

        HttpResponse response = executePost(API_URL_ACTION, action);

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

        HttpResponse response = executePost(API_URL_ACTION, action);

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

        HttpResponse response = executePost(API_URL_ACTION, action);

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

        HttpResponse response = executePost(API_URL_ACTION, action);

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

        HttpResponse response = executePost(API_URL_ACTION, action);

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

        HttpResponse response = executePost(API_URL_ACTION, action);

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

        HttpResponse response = executePost(API_URL_ACTION, action);

        handleResponseErrors(response);
    }

    /**
     * Disposes the client including disconnecting a maybe remaining session.
     */
    public void dispose() {
        try {
            uninitializeSession();
            if (httpTransport != null) {
                httpTransport.shutdown();
                httpTransport = null;
            }
            jsonFactory = null;
            requestFactory = null;
            credentialBuilder = null;
        } catch (IOException | ApiException e) {
            logger.debug("Error disposing resources: {}", e.getMessage());
            logger.trace("Trace:", e);
        }

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
        HttpResponse response = executeGet(API_URL_DEVICE);

        handleResponseErrors(response);

        Device[] deviceList = response.parseAs(Device[].class);
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
        HttpResponse response = executeGet(API_URL_DEVICE_ID.replace("{id}", deviceId));

        handleResponseErrors(response);

        return response.parseAs(Device.class);
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
                String deviceId = m.getDeviceLinkList().get(0).getValue().replace("/device/", "");
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
            for (CapabilityLink cl : d.getCapabilityLinkList()) {
                Capability c = capabilityMap.get(cl.getId());
                c.setCapabilityState(capabilityStateMap.get(c.getId()));
                deviceCapabilityMap.put(c.getId(), c);
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
        List<Property> deviceStateList = getDeviceStatesByDeviceId(deviceId);
        DeviceState deviceState = new DeviceState();
        deviceState.setId(deviceId);
        deviceState.setStateList(deviceStateList);

        // MESSAGES
        List<Message> messageList = getMessages();

        List<Message> ml = new ArrayList<>();

        for (Message m : messageList) {
            if (m.getDeviceLinkList() != null && !m.getDeviceLinkList().isEmpty()) {
                for (Link li : m.getDeviceLinkList()) {
                    if (li.getValue().equals("/device/" + deviceId)) {
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
        for (CapabilityLink cl : d.getCapabilityLinkList()) {

            Capability c = capabilityMap.get(cl.getId());
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

        HttpResponse response = executeGet(API_URL_DEVICE_STATES);

        handleResponseErrors(response);

        DeviceState[] deviceStateArray = response.parseAs(DeviceState[].class);
        return Arrays.asList(deviceStateArray);
    }

    /**
     * Loads the device states for the given deviceId.
     *
     * @param deviceId
     * @return
     * @throws IOException
     * @throws ApiException
     */
    public List<Property> getDeviceStatesByDeviceId(String deviceId) throws IOException, ApiException {
        logger.debug("Loading device states for device id {}...", deviceId);

        HttpResponse response = executeGet(API_URL_DEVICE_ID_STATE.replace("{id}", deviceId));

        handleResponseErrors(response);

        Property[] propertyArray = response.parseAs(Property[].class);
        List<Property> propertyList = Arrays.asList(propertyArray);

        return propertyList;
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

        HttpResponse response = executeGet(API_URL_LOCATION);

        handleResponseErrors(response);

        Location[] locationArray = response.parseAs(Location[].class);
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

        HttpResponse response = executeGet(API_URL_DEVICE_CAPABILITIES.replace("{id}", deviceId));

        handleResponseErrors(response);

        Capability[] capabilityArray = response.parseAs(Capability[].class);
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

        HttpResponse response = executeGet(API_URL_CAPABILITY);

        handleResponseErrors(response);

        Capability[] capabilityArray = response.parseAs(Capability[].class);
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

        HttpResponse response = executeGet(API_URL_CAPABILITY_STATES);

        handleResponseErrors(response);

        CapabilityState[] capabilityStatesArray = response.parseAs(CapabilityState[].class);
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

        HttpResponse response = executeGet(API_URL_MESSAGE);

        handleResponseErrors(response);

        Message[] messageArray = response.parseAs(Message[].class);
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
     * @param credentialRefreshListener the credentialRefreshListener to set
     */
    public void setCredentialRefreshListener(CredentialRefreshListener credentialRefreshListener) {
        this.credentialRefreshListener = credentialRefreshListener;
    }
}
