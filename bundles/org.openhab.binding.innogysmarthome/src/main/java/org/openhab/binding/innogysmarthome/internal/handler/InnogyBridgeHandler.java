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
package org.openhab.binding.innogysmarthome.internal.handler;

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants;
import org.openhab.binding.innogysmarthome.internal.InnogyWebSocket;
import org.openhab.binding.innogysmarthome.internal.client.InnogyClient;
import org.openhab.binding.innogysmarthome.internal.client.InnogyConfig;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.BaseEvent;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.Event;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.MessageEvent;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;
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
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 */
@NonNullByDefault
public class InnogyBridgeHandler extends BaseBridgeHandler
        implements AccessTokenRefreshListener, EventListener, DeviceStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(InnogyBridgeHandler.class);
    private final Gson gson = new Gson();
    private final Object lock = new Object();
    private final Set<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArraySet<>();
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    private @Nullable InnogyConfig innogyConfig;
    private @Nullable InnogyClient client;
    private @Nullable InnogyWebSocket webSocket;
    private @Nullable DeviceStructureManager deviceStructMan;
    private @Nullable String bridgeId;
    private @Nullable ScheduledFuture<?> reinitJob;

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
            client = new InnogyClient(innogyConfig, oAuthFactory, httpClient);
            client.setAccessTokenRefreshListener(InnogyBridgeHandler.this);
            try {
                logger.debug("Initializing innogy SmartHome client...");
                client.initialize(thing.getUID().getAsString());
            } catch (ApiException | IOException | ConfigurationException e) {
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
            } catch (IOException | ApiException e) {
                if (!handleClientException(e)) {
                    logger.error("Error starting device structure manager.", e);
                    return;
                }
            }

            updateStatus(ThingStatus.ONLINE);
            setBridgeProperties(deviceStructMan.getBridgeDevice());
            bridgeId = deviceStructMan.getBridgeDevice().getId();

            onEventRunnerStopped();

            registerDeviceStatusListener(InnogyBridgeHandler.this);
        }

        private void setBridgeProperties(Device bridgeDevice) {
            logger.debug("Setting Bridge Device Properties for Bridge of type '{}' with ID '{}'",
                    bridgeDevice.getConfig().getName(), bridgeDevice.getId());

            Map<String, String> properties = editProperties();
            Optional.ofNullable(bridgeDevice.getManufacturer())
                    .ifPresent(manufacturer -> properties.put(Thing.PROPERTY_VENDOR, manufacturer));
            Optional.ofNullable(bridgeDevice.getSerialnumber())
                    .ifPresent(serialNumber -> properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber));
            Optional.ofNullable(bridgeDevice.getId()).ifPresent(id -> properties.put(PROPERTY_ID, id));
            Optional.ofNullable(bridgeDevice.getConfig().getFirmwareVersion())
                    .ifPresent(firmwareVersion -> properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion));
            Optional.ofNullable(bridgeDevice.getConfig().getHardwareVersion())
                    .ifPresent(hardwareVersion -> properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareVersion));
            Optional.ofNullable(bridgeDevice.getConfig().getSoftwareVersion())
                    .ifPresent(softwareVersion -> properties.put(PROPERTY_SOFTWARE_VERSION, softwareVersion));
            Optional.ofNullable(bridgeDevice.getConfig().getIPAddress())
                    .ifPresent(ipAddress -> properties.put(PROPERTY_IP_ADDRESS, ipAddress));
            Optional.ofNullable(bridgeDevice.getConfig().getMACAddress())
                    .ifPresent(macAddress -> properties.put(Thing.PROPERTY_MAC_ADDRESS, macAddress));
            Optional.ofNullable(bridgeDevice.getConfig().getRegistrationTime())
                    .ifPresent(registrationTime -> properties.put(PROPERTY_REGISTRATION_TIME,
                            registrationTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))));
            Optional.ofNullable(bridgeDevice.getConfig().getConfigurationState())
                    .ifPresent(configurationState -> properties.put(PROPERTY_CONFIGURATION_STATE, configurationState));
            Optional.ofNullable(bridgeDevice.getType())
                    .ifPresent(shcType -> properties.put(PROPERTY_SHC_TYPE, shcType));
            Optional.ofNullable(bridgeDevice.getConfig().getTimeZone())
                    .ifPresent(timeZone -> properties.put(PROPERTY_TIME_ZONE, timeZone));
            Optional.ofNullable(bridgeDevice.getConfig().getProtocolId())
                    .ifPresent(protocolId -> properties.put(PROPERTY_PROTOCOL_ID, protocolId));
            Optional.ofNullable(bridgeDevice.getConfig().getGeoLocation())
                    .ifPresent(geoLocation -> properties.put(PROPERTY_GEOLOCATION, geoLocation));
            Optional.ofNullable(bridgeDevice.getConfig().getCurrentUTCOffset()).ifPresent(
                    currentUTCOffset -> properties.put(PROPERTY_CURRENT_UTC_OFFSET, currentUTCOffset.toString()));
            Optional.ofNullable(bridgeDevice.getConfig().getBackendConnectionMonitored())
                    .ifPresent(backendConnectionMonitored -> properties.put(PROPERTY_BACKEND_CONNECTION_MONITORED,
                            backendConnectionMonitored.toString()));
            Optional.ofNullable(bridgeDevice.getConfig().getRFCommFailureNotification())
                    .ifPresent(rfCommFailureNotification -> properties.put(PROPERTY_RFCOM_FAILURE_NOTIFICATION,
                            rfCommFailureNotification.toString()));

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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) { // Catch Exception because websocket start throws Exception
                if (!handleClientException(e)) {
                    logger.warn("Error starting websocket.", e);
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
    public InnogyBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
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
    private @Nullable InnogyConfig loadAndCheckConfig() {
        final Configuration thingConfig = super.getConfig();
        InnogyConfig innogyConfig = this.innogyConfig;

        if (innogyConfig == null) {
            innogyConfig = new InnogyConfig();
            this.innogyConfig = innogyConfig;
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

    @Override
    public void dispose() {
        logger.debug("Disposing innogy SmartHome bridge handler '{}'", getThing().getUID().getId());

        unregisterDeviceStatusListener(this);

        if (reinitJob != null) {
            reinitJob.cancel(true);
            reinitJob = null;
        }

        if (webSocket != null) {
            webSocket.stop();
            webSocket = null;
        }

        client = null;
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
        final Collection<Device> devices;
        if (deviceStructMan == null) {
            devices = Collections.emptyList();
        } else {
            devices = deviceStructMan.getDeviceList();
        }

        return devices;
    }

    /**
     * Returns the {@link Device} with the given deviceId.
     *
     * @param deviceId
     * @return {@link Device} or null, if it does not exist or no {@link DeviceStructureManager} is available
     */
    public @Nullable Device getDeviceById(String deviceId) {
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
    public @Nullable Device refreshDevice(String deviceId) {
        if (deviceStructMan == null) {
            return null;
        }

        Device device = null;
        try {
            deviceStructMan.refreshDevice(deviceId);
            device = deviceStructMan.getDeviceById(deviceId);
        } catch (IOException | ApiException e) {
            handleClientException(e);
        }
        return device;
    }

    // CredentialRefreshListener implementation

    @Override
    public void onAccessTokenResponse(AccessTokenResponse credential) {
        String accessToken = credential.getAccessToken();
        innogyConfig.setAccessToken(accessToken);
        getThing().getConfiguration().put(CONFIG_ACCESS_TOKEN, accessToken);
        logger.debug("Access token for innogy expired. New access token saved.");
        logger.debug("innogy access token saved (onTokenResponse): {}...{}", accessToken.substring(0, 10),
                accessToken.substring(accessToken.length() - 10));

        // restart WebSocket
        onEventRunnerStopped();
    }

    @Override
    public void onDeviceStateChanged(Device device) {
        synchronized (this.lock) {
            if (!bridgeId.equals(device.getId())) {
                logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                        bridgeId);
                return;
            }

            logger.debug("onDeviceStateChanged called with device {}/{}", device.getConfig().getName(), device.getId());

            // DEVICE STATES
            if (device.hasDeviceState()) {
                Double cpuUsage = device.getDeviceState().getState().getCpuUsage().getValue();
                if (cpuUsage != null) {
                    logger.debug("-> CPU usage state: {}", cpuUsage);
                    updateState(CHANNEL_CPU, new DecimalType(cpuUsage));
                }
                Double diskUsage = device.getDeviceState().getState().getDiskUsage().getValue();
                if (diskUsage != null) {
                    logger.debug("-> Disk usage state: {}", diskUsage);
                    updateState(CHANNEL_DISK, new DecimalType(diskUsage));
                }
                Double memoryUsage = device.getDeviceState().getState().getMemoryUsage().getValue();
                if (memoryUsage != null) {
                    logger.debug("-> Memory usage state: {}", memoryUsage);
                    updateState(CHANNEL_MEMORY, new DecimalType(memoryUsage));
                }

            }

        }

    }

    @Override
    public void onDeviceStateChanged(Device device, Event event) {
        synchronized (this.lock) {
            if (!bridgeId.equals(device.getId())) {
                logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                        bridgeId);
                return;
            }

            logger.trace("DeviceId {} relevant for this handler.", device.getId());

            if (event.isLinkedtoDevice() && Device.DEVICE_TYPE_SHCA.equals(device.getType())) {
                device.getDeviceState().getState().getCpuUsage().setValue(event.getProperties().getCpuUsage());
                device.getDeviceState().getState().getDiskUsage().setValue(event.getProperties().getDiskUsage());
                device.getDeviceState().getState().getMemoryUsage().setValue(event.getProperties().getMemoryUsage());
                onDeviceStateChanged(device);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.innogysmarthome.handler.EventListener#onEvent(java.lang.String)
     */
    @Override
    public void onEvent(String msg) {
        logger.trace("=====================================================");
        logger.trace("onEvent called. Msg: {}", msg);

        try {
            BaseEvent be = gson.fromJson(msg, BaseEvent.class);
            logger.debug("Event no {} found. Type: {}", be.getSequenceNumber(), be.getType());
            if (!BaseEvent.SUPPORTED_EVENT_TYPES.contains(be.getType())) {
                logger.debug("Event type {} not supported. Skipping...", be.getType());
            } else {

                Event event = gson.fromJson(msg, Event.class);

                switch (event.getType()) {
                    case BaseEvent.TYPE_STATE_CHANGED:
                    case BaseEvent.TYPE_BUTTON_PRESSED:
                        handleStateChangedEvent(event);
                        break;

                    case BaseEvent.TYPE_DISCONNECT:
                        logger.info("Websocket disconnected. Reason: {}", event.getProperties());// TODO log Reason!
                        scheduleReinitialize(0);
                        break;

                    case BaseEvent.TYPE_CONFIGURATION_CHANGED:
                        if (client.getConfigVersion().equals(event.getConfigurationVersion().toString())) {
                            logger.debug(
                                    "Ignored configuration changed event with version '{}' as current version is '{}' the same.",
                                    event.getConfigurationVersion(), client.getConfigVersion());
                        } else {
                            logger.info("Configuration changed from version {} to {}. Restarting innogy binding...",
                                    client.getConfigVersion(), event.getConfigurationVersion());
                            dispose();
                            scheduleReinitialize(0);
                        }
                        break;

                    case BaseEvent.TYPE_CONTROLLER_CONNECTIVITY_CHANGED:
                        handleControllerConnectivityChangedEvent(event);
                        break;

                    case BaseEvent.TYPE_NEW_MESSAGE_RECEIVED:
                    case BaseEvent.TYPE_MESSAGE_CREATED:
                        MessageEvent messageEvent = gson.fromJson(msg, MessageEvent.class);
                        handleNewMessageReceivedEvent(messageEvent);
                        break;

                    case BaseEvent.TYPE_MESSAGE_DELETED:
                        handleMessageDeletedEvent(event);
                        break;

                    default:
                        logger.debug("Unsupported eventtype {}.", event.getType());
                        break;
                }
            }
        } catch (IOException | ApiException e) {
            logger.debug("Error with Event: {}", e.getMessage(), e);
        }
        logger.trace("=====================================================");
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
            logger.trace("Event is linked to capability");
            Device device = deviceStructMan.getDeviceByCapabilityId(event.getSourceId());
            if (device != null) {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    deviceStatusListener.onDeviceStateChanged(device, event);
                }
            } else {
                logger.debug("Unknown/unsupported device for capability {}.", event.getSource());
            }

            // DEVICE
        } else if (event.isLinkedtoDevice()) {
            logger.trace("Event is linked to device");

            if (!event.getSourceId().equals(deviceStructMan.getBridgeDevice().getId())) {
                deviceStructMan.refreshDevice(event.getSourceId());
            }
            Device device = deviceStructMan.getDeviceById(event.getSourceId());
            if (device != null) {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    deviceStatusListener.onDeviceStateChanged(device, event);
                }
            } else {
                logger.debug("Unknown/unsupported device {}.", event.getSourceId());
            }

        } else {
            logger.debug("link type {} not supported (yet?)", event.getSourceLinkType());
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
    public void handleNewMessageReceivedEvent(MessageEvent event) throws ApiException, IOException {
        if (deviceStructMan == null) {
            scheduleReinitialize();
        }

        Message message = event.getMessage();
        logger.trace("Message: {}", gson.toJson(message));
        logger.trace("Messagetype: {}", message.getType());
        if (Message.TYPE_DEVICE_LOW_BATTERY.equals(message.getType())) {
            for (String link : message.getDeviceLinkList()) {
                deviceStructMan.refreshDevice(Link.getId(link));
                Device device = deviceStructMan.getDeviceById(Link.getId(link));
                if (device != null) {
                    for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                        deviceStatusListener.onDeviceStateChanged(device);
                    }
                } else {
                    logger.debug("Unknown/unsupported device {}.", event.getSourceId());
                }
            }
        } else {
            logger.debug("Message received event not yet implemented for Messagetype {}.", message.getType());
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

        String messageId = event.getData().getId();

        if (deviceStructMan != null) {
            logger.debug("handleMessageDeletedEvent with messageId '{}'", messageId);
            Device device = deviceStructMan.getDeviceWithMessageId(messageId);
            if (device != null) {
                deviceStructMan.refreshDevice(device.getId());
                device = deviceStructMan.getDeviceById(device.getId());
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    deviceStatusListener.onDeviceStateChanged(device);
                }
            } else {
                logger.debug("No device found with message id {}.", messageId);
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
        } catch (IOException | ApiException e) {
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
        } catch (IOException | ApiException e) {
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
        } catch (IOException | ApiException e) {
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
        } catch (IOException | ApiException e) {
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
        } catch (IOException | ApiException e) {
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
        } catch (IOException | ApiException e) {
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
