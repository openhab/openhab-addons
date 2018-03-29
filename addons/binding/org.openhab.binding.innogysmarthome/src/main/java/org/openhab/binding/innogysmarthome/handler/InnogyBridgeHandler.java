/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.handler;

import static org.openhab.binding.innogysmarthome.InnogyBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.innogysmarthome.InnogyBindingConstants;
import org.openhab.binding.innogysmarthome.internal.InnogyWebSocket;
import org.openhab.binding.innogysmarthome.internal.client.InnogyClient;
import org.openhab.binding.innogysmarthome.internal.client.InnogyConfig;
import org.openhab.binding.innogysmarthome.internal.client.entity.Message;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.Event;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.exception.ApiException;
import org.openhab.binding.innogysmarthome.internal.client.exception.ConfigurationException;
import org.openhab.binding.innogysmarthome.internal.client.exception.ControllerOfflineException;
import org.openhab.binding.innogysmarthome.internal.client.exception.InvalidActionTriggeredException;
import org.openhab.binding.innogysmarthome.internal.client.exception.InvalidAuthCodeException;
import org.openhab.binding.innogysmarthome.internal.client.exception.RemoteAccessNotAllowedException;
import org.openhab.binding.innogysmarthome.internal.client.exception.SessionExistsException;
import org.openhab.binding.innogysmarthome.internal.listener.DeviceStatusListener;
import org.openhab.binding.innogysmarthome.internal.listener.EventListener;
import org.openhab.binding.innogysmarthome.internal.manager.DeviceStructureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.gson.Gson;

/**
 * The {@link InnogyBridgeHandler} is responsible for handling the innogy SmartHome controller including the connection
 * to the innogy backend for all communications with the innogy {@link Device}s.
 * <p/>
 * It implements the {@link CredentialRefreshListener} to handle updates of the oauth2 tokens and the
 * {@link EventListener} to handle {@link Event}s, that are received by the {@link InnogyWebSocket}.
 * <p/>
 * The {@link Device}s are organized by the {@link DeviceStructureManager}, which is also responsible for the connection
 * to the innogy SmartHome webservice via the {@link InnogyClient}.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class InnogyBridgeHandler extends BaseBridgeHandler implements CredentialRefreshListener, EventListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private final Logger logger = LoggerFactory.getLogger(InnogyBridgeHandler.class);
    private InnogyConfig innogyConfig;
    private InnogyClient client;
    private InnogyWebSocket webSocket;
    private DeviceStructureManager deviceStructMan;
    private Gson gson = new Gson();

    private Set<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArraySet<>();

    private ScheduledFuture<?> reinitJob;

    /**
     * The {@link Initializer} class implements the initialization process of the bridge including starting the
     * {@link DeviceStructureManager} (who loads all the {@link Device}s and states) and the {@link InnogyWebSocket}.
     *
     * @author Oliver Kuhl - Initial contribution
     *
     */
    private class Initializer implements Runnable {

        @Override
        public void run() {
            client = new InnogyClient(innogyConfig);
            client.setCredentialRefreshListener(InnogyBridgeHandler.this);
            try {
                logger.debug("Initializing innogy SmartHome client...");
                client.initialize();
            } catch (Exception e) {
                if (!handleClientException(e)) {
                    logger.error("Error initializing innogy SmartHome client.", e);
                    return;
                }
            }

            if (StringUtils.isNotBlank(client.getConfig().getRefreshToken())) {
                getThing().getConfiguration().put(CONFIG_REFRESH_TOKEN, client.getConfig().getRefreshToken());
                if (StringUtils.isNotBlank(client.getConfig().getAccessToken())) {
                    getThing().getConfiguration().put(CONFIG_ACCESS_TOKEN, client.getConfig().getAccessToken());
                }
                Configuration configuration = editConfiguration();
                configuration.put(CONFIG_AUTH_CODE, "");
                updateConfiguration(configuration);
                innogyConfig.setAuthCode("");
            }

            deviceStructMan = new DeviceStructureManager(client);
            try {
                deviceStructMan.start();
            } catch (Exception e) {
                if (!handleClientException(e)) {
                    logger.error("Error starting device structure manager.", e);
                    return;
                }
            }

            updateStatus(ThingStatus.ONLINE);
            setBridgeProperties(deviceStructMan.getBridgeDevice());

            onEventRunnerStopped();
        }

        private void setBridgeProperties(Device bridgeDevice) {
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_VENDOR, bridgeDevice.getManufacturer());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, bridgeDevice.getSerialnumber());
            properties.put(PROPERTY_ID, bridgeDevice.getId());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, bridgeDevice.getFirmwareVersion());
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, bridgeDevice.getHardwareVersion());
            properties.put(PROPERTY_SOFTWARE_VERSION, bridgeDevice.getSoftwareVersion());
            properties.put(PROPERTY_IP_ADDRESS, bridgeDevice.getIpAddress());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, bridgeDevice.getMacAddress());
            properties.put(PROPERTY_REGISTRATION_TIME, bridgeDevice.getRegistrationTimeFormattedString());
            updateProperties(properties);
        }
    };

    /**
     * Runnable to run the websocket for receiving permanent update {@link Event}s from the innogy API.
     *
     * @author Oliver Kuhl - Initial contribution
     */
    private class WebSocketRunner implements Runnable {

        private InnogyBridgeHandler bridgeHandler;

        /**
         * Constructs the {@link WebSocketRunner} with the given {@link InnogyBridgeHandler}.
         *
         * @param bridgeHandler
         */
        public WebSocketRunner(InnogyBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
        }

        @Override
        public void run() {
            String webSocketUrl = WEBSOCKET_API_URL_EVENTS.replace("{token}",
                    (String) getConfig().get(CONFIG_ACCESS_TOKEN));
            logger.debug("WebSocket URL: {}...{}", webSocketUrl.substring(0, 70),
                    webSocketUrl.substring(webSocketUrl.length() - 10));

            try {
                if (webSocket != null && webSocket.isRunning()) {
                    webSocket.stop();
                    webSocket = null;
                }

                BigDecimal idleTimeout = (BigDecimal) getConfig().get(CONFIG_WEBSOCKET_IDLE_TIMEOUT);
                webSocket = new InnogyWebSocket(bridgeHandler, URI.create(webSocketUrl), idleTimeout.intValue() * 1000);
                logger.debug("Starting innogy websocket.");
                webSocket.start();
            } catch (Exception e) {
                if (!handleClientException(e)) {
                    logger.error("Error starting websocket.", e);
                    return;
                }
            }
        }
    }

    /**
     * Constructs a new {@link InnogyBridgeHandler}.
     *
     * @param bridge
     */
    public InnogyBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    @Override
    public void initialize() {
        logger.debug("Initializing innogy SmartHome BridgeHandler...");

        // Start an extra thread to readout the configuration and check the connection, because it takes sometimes more
        // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
        InnogyConfig config = loadAndCheckConfig();

        if (config != null) {
            logger.debug("innogy config: {}", config.toString());
            scheduler.execute(new Initializer());
        }
    }

    /**
     * Schedules a re-initialization in the given future.
     *
     * @param seconds
     */
    private void scheduleReinitialize(long seconds) {
        if (reinitJob != null && !reinitJob.isDone()) {
            logger.debug("Scheduling reinitialize in {} seconds - ignored: already triggered in {} seconds.", seconds,
                    reinitJob.getDelay(TimeUnit.SECONDS));
            return;
        }
        logger.debug("Scheduling reinitialize in {} seconds.", seconds);
        reinitJob = scheduler.schedule(() -> initialize(), seconds, TimeUnit.SECONDS);
    }

    /**
     * Schedules a re-initialization using the default {@link InnogyBindingConstants#REINITIALIZE_DELAY_SECONDS}.
     */
    private void scheduleReinitialize() {
        scheduleReinitialize(REINITIALIZE_DELAY_SECONDS);
    }

    /**
     * Loads the {@link Configuration} of the bridge thing, creates an new
     * {@link InnogyConfig}, checks and returns it.
     *
     * @return the {@link InnogyConfig} for the {@link InnogyClient}.
     */
    private InnogyConfig loadAndCheckConfig() {
        Configuration thingConfig = super.getConfig();

        if (innogyConfig == null) {
            innogyConfig = new InnogyConfig();
        }

        // load and check connection and authorization data
        String brand;
        if (StringUtils.isNotBlank((String) thingConfig.get(CONFIG_BRAND))) {
            brand = thingConfig.get(CONFIG_BRAND).toString();
        } else {
            brand = DEFAULT_BRAND;
        }
        switch (brand) {
            case BRAND_INNOGY_SMARTHOME:
                innogyConfig.setClientId(CLIENT_ID_INNOGY_SMARTHOME);
                innogyConfig.setClientSecret(CLIENT_SECRET_INNOGY_SMARTHOME);
                innogyConfig.setRedirectUrl(REDIRECT_URL_INNOGY_SMARTHOME);
                break;
            case BRAND_SMARTHOME_AUSTRIA:
                innogyConfig.setClientId(CLIENT_ID_SMARTHOME_AUSTRIA);
                innogyConfig.setClientSecret(CLIENT_SECRET_SMARTHOME_AUSTRIA);
                innogyConfig.setRedirectUrl(REDIRECT_URL_SMARTHOME_AUSTRIA);
                break;
            case BRAND_START_SMARTHOME:
                innogyConfig.setClientId(CLIENT_ID_START_SMARTHOME);
                innogyConfig.setClientSecret(CLIENT_SECRET_START_SMARTHOME);
                innogyConfig.setRedirectUrl(REDIRECT_URL_START_SMARTHOME);
                break;
            default:
                logger.debug("Invalid brand '{}'. Make sure to select a brand in the SHC thing configuration!", brand);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid brand '" + brand + "'. Make sure to select a brand in the SHC thing configuration!");
                dispose();
                break;
        }

        if (StringUtils.isNotBlank((String) thingConfig.get(CONFIG_ACCESS_TOKEN))) {
            innogyConfig.setAccessToken(thingConfig.get(CONFIG_ACCESS_TOKEN).toString());
        }
        if (StringUtils.isNotBlank((String) thingConfig.get(CONFIG_REFRESH_TOKEN))) {
            innogyConfig.setRefreshToken(thingConfig.get(CONFIG_REFRESH_TOKEN).toString());
        }

        if (innogyConfig.checkRefreshToken()) {
            return innogyConfig;
        } else {
            if (StringUtils.isNotBlank((String) thingConfig.get(CONFIG_AUTH_CODE))) {
                innogyConfig.setAuthCode(thingConfig.get(CONFIG_AUTH_CODE).toString());
                return innogyConfig;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Cannot connect to innogy SmartHome service. Please set auth-code!");
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Disposing innogy SmartHome bridge handler '{}'", getThing().getUID().getId());
        if (reinitJob != null) {
            reinitJob.cancel(true);
            reinitJob = null;
        }

        if (webSocket != null) {
            webSocket.stop();
            webSocket = null;
        }

        if (client != null) {
            try {
                client.dispose();
            } catch (Exception e) {
                logger.trace("Error disposing client: {}", e.getMessage());
            }
            client = null;
        }

        deviceStructMan = null;

        super.dispose();
        logger.debug("innogy SmartHome bridge handler shut down.");
    }

    /**
     * Registers a {@link DeviceStatusListener}.
     *
     * @param deviceStatusListener
     * @return true, if successful
     */
    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.add(deviceStatusListener);
    }

    /**
     * Unregisters a {@link DeviceStatusListener}.
     *
     * @param deviceStatusListener
     * @return true, if successful
     */
    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    /**
     * Loads a Collection of {@link Device}s from the bridge and returns them.
     *
     * @return a Collection of {@link Device}s
     */
    public Collection<Device> loadDevices() {
        Collection<Device> devices = null;
        if (deviceStructMan != null) {
            try {
                devices = deviceStructMan.getDeviceList();
            } catch (Exception e) {
                logger.error("Error loading devices from device structure manager.", e);
            }
        }

        return devices;
    }

    /**
     * Returns the {@link Device} with the given deviceId.
     *
     * @param deviceId
     * @return {@link Device} or null, if it does not exist or no {@link DeviceStructureManager} is available
     */
    public Device getDeviceById(String deviceId) {
        if (deviceStructMan != null) {
            return deviceStructMan.getDeviceById(deviceId);
        }
        return null;
    }

    /**
     * Refreshes the {@link Device} with the given id, by reloading the full device from the innogy webservice.
     *
     * @param deviceId
     * @return the {@link Device} or null, if it does not exist or no {@link DeviceStructureManager} is available
     */
    public Device refreshDevice(String deviceId) {
        if (deviceStructMan == null) {
            return null;
        }

        Device device = null;
        try {
            deviceStructMan.refreshDevice(deviceId);
            device = deviceStructMan.getDeviceById(deviceId);
        } catch (Exception e) {
            handleClientException(e);
        }
        return device;
    }

    // CredentialRefreshListener implementation

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.api.client.auth.oauth2.CredentialRefreshListener#onTokenResponse(com.google.api.client.auth.oauth2.
     * Credential, com.google.api.client.auth.oauth2.TokenResponse)
     */
    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        String accessToken = credential.getAccessToken();
        innogyConfig.setAccessToken(accessToken);
        getThing().getConfiguration().put(CONFIG_ACCESS_TOKEN, accessToken);
        logger.debug("Access token for innogy expired. New access token saved.");
        logger.debug("innogy access token saved (onTokenResponse): {}...{}", accessToken.substring(0, 10),
                accessToken.substring(accessToken.length() - 10));

        // restart WebSocket
        onEventRunnerStopped();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.api.client.auth.oauth2.CredentialRefreshListener#onTokenErrorResponse(com.google.api.client.auth.
     * oauth2.Credential, com.google.api.client.auth.oauth2.TokenErrorResponse)
     */
    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
        String accessToken = "";
        innogyConfig.setAccessToken(accessToken);
        getThing().getConfiguration().put(CONFIG_ACCESS_TOKEN, accessToken);
        logger.debug("innogy access token removed (onTokenErrorResponse): {}...{}", accessToken.substring(0, 10),
                accessToken.substring(accessToken.length() - 10));

        // restart binding
        scheduleReinitialize();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.innogysmarthome.handler.EventListener#onEvent(java.lang.String)
     */
    @Override
    public void onEvent(String msg) {
        logger.trace("onEvent called. Msg: {}", msg);

        try {
            Event[] eventArray = gson.fromJson(msg, Event[].class);
            for (Event event : eventArray) {
                logger.debug("Event found: Type:{} Capability:{}", event.getType(),
                        event.getLink() != null ? event.getLink().getValue() : "(no link)");
                switch (event.getType()) {
                    case Event.TYPE_STATE_CHANGED:
                        handleStateChangedEvent(event);
                        break;

                    case Event.TYPE_DISCONNECT:
                        logger.info("Websocket disconnected. Reason: {}", event.getPropertyList().get(0).getValue());
                        scheduleReinitialize(0);
                        break;

                    case Event.TYPE_CONFIG_CHANGED:
                        logger.info("Configuration changed to version {}. Restarting innogy binding...",
                                event.getConfigurationVersion());
                        dispose();
                        scheduleReinitialize(0);
                        break;

                    case Event.TYPE_CONTROLLER_CONNECTIVITY_CHANGED:
                        handleControllerConnectivityChangedEvent(event);
                        break;

                    case Event.TYPE_NEW_MESSAGE_RECEIVED:
                        handleNewMessageReceivedEvent(event);
                        break;

                    case Event.TYPE_MESSAGE_DELETED:
                        handleMessageDeletedEvent(event);
                        break;

                    default:
                        logger.debug("Unknown eventtype {}.", event.getType());
                        break;
                }
            }
        } catch (Exception e) {
            logger.debug("Error with Event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the event that occurs, when the state of a device (like reachability) or a capability (like a temperature
     * value) has changed.
     *
     * @param event
     * @throws ApiException
     * @throws IOException
     */
    public void handleStateChangedEvent(Event event) throws ApiException, IOException {
        if (deviceStructMan == null) {
            scheduleReinitialize();
            return;
        }

        // CAPABILITY
        if (event.isLinkedtoCapability()) {
            Device device = deviceStructMan.getDeviceByCapabilityLink(event.getLink().getValue());
            if (device != null) {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    deviceStatusListener.onDeviceStateChanged(device, event);
                }
            } else {
                logger.debug("Unknown/unsupported device for capability {}.", event.getLink().getValue());
            }

            // DEVICE
        } else if (event.isLinkedtoDevice()) {
            deviceStructMan.refreshDevice(event.getLinkId());
            Device device = deviceStructMan.getDeviceById(event.getLinkId());
            if (device != null) {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    deviceStatusListener.onDeviceStateChanged(device, event);
                }
            } else {
                logger.debug("Unknown/unsupported device {}.", event.getLinkId());
            }

        } else {
            logger.debug("link type {} not supported (yet?)", event.getLinkType());
        }

    }

    /**
     * Handles the event that occurs, when the connectivity of the bridge has changed.
     *
     * @param event
     * @throws ApiException
     * @throws IOException
     */
    public void handleControllerConnectivityChangedEvent(Event event) throws ApiException, IOException {
        Boolean connected = event.getIsConnected();
        if (connected != null) {
            logger.debug("SmartHome Controller connectivity changed to {}.", connected ? "online" : "offline");
            if (connected) {
                deviceStructMan = new DeviceStructureManager(client);
                deviceStructMan.start();
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
                deviceStructMan = null;
            }
        } else {
            logger.warn("isConnected property missing in event! (returned null)");
        }
    }

    /**
     * Handles the event that occurs, when a new message was received. Currently only handles low battery messages.
     *
     * @param event
     * @throws ApiException
     * @throws IOException
     */
    public void handleNewMessageReceivedEvent(Event event) throws ApiException, IOException {
        if (deviceStructMan == null) {
            scheduleReinitialize();
        }

        List<Message> messageList = event.getDataListAsMessage();
        for (Message m : messageList) {
            if (Message.TYPE_DEVICE_LOW_BATTERY.equals(m.getType())) {
                for (Link dl : m.getDeviceLinkList()) {
                    deviceStructMan.refreshDevice(dl.getId());
                    Device device = deviceStructMan.getDeviceById(dl.getId());
                    if (device != null) {
                        for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                            deviceStatusListener.onDeviceStateChanged(device);
                        }
                    } else {
                        logger.debug("Unknown/unsupported device {}.", event.getLinkId());
                    }
                }
            } else {
                logger.debug("Message received event not yet implemented for Messagetype {}.", m.getType());
            }
        }

    }

    /**
     * Handle the event that occurs, when a message was deleted. In case of a low battery message this means, that the
     * device is back to normal. Currently, only messages linked to devices are handled by refreshing the device data
     * and informing the {@link InnogyDeviceHandler} about the changed device.
     *
     * @param event
     * @throws ApiException
     * @throws IOException
     */
    public void handleMessageDeletedEvent(Event event) throws ApiException, IOException {
        if (deviceStructMan != null) {
            if (Link.LINK_TYPE_MESSAGE.equals(event.getLinkType())) {
                Device device = deviceStructMan.getDeviceWithMessageId(event.getLinkId());
                if (device != null) {
                    deviceStructMan.refreshDevice(device.getId());
                    device = deviceStructMan.getDeviceById(device.getId());
                    for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                        deviceStatusListener.onDeviceStateChanged(device);
                    }
                } else {
                    logger.debug("Unknown/unsupported device {}.", event.getLinkId());
                }
            }
        } else {
            scheduleReinitialize();
        }
    }

    /**
     * This method is called, when the eventRunner stops and must be restarted after the given delay in seconds.
     *
     * @param delay long in seconds
     */
    public void onEventRunnerStopped(long delay) {
        logger.trace("onEventRunnerStopped called");
        scheduler.schedule(new WebSocketRunner(this), delay, TimeUnit.SECONDS);
    }

    /**
     * This method is called, whenever the eventRunner stops and must be restarted immediately.
     */
    public void onEventRunnerStopped() {
        onEventRunnerStopped(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.innogysmarthome.internal.listener.EventListener#onEventRunnerStoppedAbnormally()
     */
    @Override
    public void connectionClosed() {
        scheduleReinitialize(REINITIALIZE_DELAY_SECONDS);
    }

    /**
     * Sends the command to switch the {@link Device} with the given id to the new state. Is called by the
     * {@link InnogyDeviceHandler} for switch devices like the VariableActuator, PSS, PSSO or ISS2.
     *
     * @param deviceId
     * @param state
     */
    public void commandSwitchDevice(String deviceId, boolean state) {
        try {
            // TODO: ADD DEVICES
            // VariableActuator
            String deviceType = deviceStructMan.getDeviceById(deviceId).getType();
            if (deviceType.equals(DEVICE_VARIABLE_ACTUATOR)) {
                String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_VARIABLEACTUATOR);
                client.setVariableActuatorState(capabilityId, state);

                // PSS / PSSO / ISS2
            } else if (deviceType.equals(DEVICE_PSS) || deviceType.equals(DEVICE_PSSO)
                    || deviceType.equals(DEVICE_ISS2)) {
                String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_SWITCHACTUATOR);
                client.setSwitchActuatorState(capabilityId, state);
            }
        } catch (Exception e) {
            handleClientException(e);
        }
    }

    /**
     * Sends the command to update the point temperature of the {@link Device} with the given deviceId. Is called by the
     * {@link InnogyDeviceHandler} for thermostat {@link Device}s like RST or WRT.
     *
     * @param deviceId
     * @param pointTemperature
     */
    public void commandUpdatePointTemperature(String deviceId, double pointTemperature) {
        try {
            String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_THERMOSTATACTUATOR);
            client.setPointTemperatureState(capabilityId, pointTemperature);
        } catch (Exception e) {
            handleClientException(e);
        }
    }

    /**
     * Sends the command to turn the alarm of the {@link Device} with the given id on or off. Is called by the
     * {@link InnogyDeviceHandler} for smoke detector {@link Device}s like WSD or WSD2.
     *
     * @param deviceId
     * @param alarmState
     */
    public void commandSwitchAlarm(String deviceId, boolean alarmState) {
        try {
            String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_ALARMACTUATOR);
            client.setAlarmActuatorState(capabilityId, alarmState);
        } catch (Exception e) {
            handleClientException(e);
        }
    }

    /**
     * Sends the command to set the operation mode of the {@link Device} with the given deviceId to auto (or manual, if
     * false). Is called by the {@link InnogyDeviceHandler} for thermostat {@link Device}s like RST.
     *
     * @param deviceId
     * @param autoMode true activates the automatic mode, false the manual mode.
     */
    public void commandSetOperationMode(String deviceId, boolean autoMode) {
        try {
            String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_THERMOSTATACTUATOR);
            client.setOperationMode(capabilityId, autoMode);
        } catch (Exception e) {
            handleClientException(e);
        }
    }

    /**
     * Sends the command to set the dimm level of the {@link Device} with the given id. Is called by the
     * {@link InnogyDeviceHandler} for {@link Device}s like ISD2 or PSD.
     *
     * @param deviceId
     * @param dimLevel
     */
    public void commandSetDimmLevel(String deviceId, int dimLevel) {
        try {
            String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_DIMMERACTUATOR);
            client.setDimmerActuatorState(capabilityId, dimLevel);
        } catch (Exception e) {
            handleClientException(e);
        }
    }

    /**
     * Sends the command to set the rollershutter level of the {@link Device} with the given id. Is called by the
     * {@link InnogyDeviceHandler} for {@link Device}s like ISR2.
     *
     * @param deviceId
     * @param rollerSchutterLevel
     */
    public void commandSetRollerShutterLevel(String deviceId, int rollerSchutterLevel) {
        try {
            String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_ROLLERSHUTTERACTUATOR);
            client.setRollerShutterActuatorState(capabilityId, rollerSchutterLevel);
        } catch (Exception e) {
            handleClientException(e);
        }
    }

    /**
     * Handles all Exceptions of the client communication. For minor "errors" like an already existing session, it
     * returns true to inform the binding to continue running. In other cases it may e.g. schedule a reinitialization of
     * the binding.
     *
     * @param e the Exception
     * @return boolean true, if binding should continue.
     */
    private boolean handleClientException(Exception e) {

        // Session exists
        if (e instanceof SessionExistsException) {
            logger.debug("Session already exists. Continuing...");
            return true;
        }

        // Remote access not allowed (usually by IP address change)
        if (e instanceof RemoteAccessNotAllowedException) {
            logger.debug("Remote access not allowed. Dropping access token and reinitializing binding...");
            innogyConfig.setAccessToken("");
            getThing().getConfiguration().put(CONFIG_ACCESS_TOKEN, "");
            scheduleReinitialize(0);
        }

        // Controller offline
        if (e instanceof ControllerOfflineException) {
            logger.debug("innogy SmartHome Controller is offline.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            dispose();
            scheduleReinitialize();
            return false;
        }

        // Configuration error
        if (e instanceof ConfigurationException) {
            logger.debug("Configuration error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            dispose();
            return false;
        }

        // invalid auth code
        if (e instanceof InvalidAuthCodeException) {
            logger.debug("Error fetching access tokens. Invalid authcode! Please generate a new one. Detail: {}",
                    e.getMessage());
            org.eclipse.smarthome.config.core.Configuration configuration = editConfiguration();
            configuration.put(CONFIG_AUTH_CODE, "");
            updateConfiguration(configuration);
            innogyConfig.setAuthCode("");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid authcode. Please generate a new one!");
            dispose();
            return false;
        }

        if (e instanceof InvalidActionTriggeredException) {
            logger.debug("Error triggering action: {}", e.getMessage());
            return true;
        }

        // io error
        if (e instanceof IOException) {
            logger.debug("IO error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            dispose();
            scheduleReinitialize(REINITIALIZE_DELAY_LONG_SECONDS);
            return false;
        }

        // unexpected API error
        if (e instanceof ApiException) {
            logger.debug("Unexcepted API error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            dispose();
            scheduleReinitialize(REINITIALIZE_DELAY_LONG_SECONDS);
            return false;
        }

        // java.net.SocketTimeoutException
        if (e instanceof SocketTimeoutException) {
            logger.debug("Socket timeout: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            dispose();
            scheduleReinitialize();
            return false;
        }

        // ExecutionException
        if (e instanceof ExecutionException) {
            logger.debug("ExecutionException: {}", ExceptionUtils.getRootCauseMessage(e));
            dispose();
            scheduleReinitialize();
            return false;
        }

        // unknown
        logger.debug("Unknown exception", e);
        dispose();
        scheduleReinitialize();
        return false;
    }
}
