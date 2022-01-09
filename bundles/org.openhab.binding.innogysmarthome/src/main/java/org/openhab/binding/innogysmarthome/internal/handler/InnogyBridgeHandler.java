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
package org.openhab.binding.innogysmarthome.internal.handler;

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.*;
import static org.openhab.binding.innogysmarthome.internal.client.Constants.API_URL_TOKEN;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.innogysmarthome.internal.InnogyWebSocket;
import org.openhab.binding.innogysmarthome.internal.client.InnogyClient;
import org.openhab.binding.innogysmarthome.internal.client.entity.action.ShutterAction;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.DeviceConfig;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.BaseEvent;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.Event;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.MessageEvent;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;
import org.openhab.binding.innogysmarthome.internal.client.exception.ApiException;
import org.openhab.binding.innogysmarthome.internal.client.exception.AuthenticationException;
import org.openhab.binding.innogysmarthome.internal.client.exception.ControllerOfflineException;
import org.openhab.binding.innogysmarthome.internal.client.exception.InvalidActionTriggeredException;
import org.openhab.binding.innogysmarthome.internal.client.exception.RemoteAccessNotAllowedException;
import org.openhab.binding.innogysmarthome.internal.client.exception.SessionExistsException;
import org.openhab.binding.innogysmarthome.internal.discovery.InnogyDeviceDiscoveryService;
import org.openhab.binding.innogysmarthome.internal.listener.DeviceStatusListener;
import org.openhab.binding.innogysmarthome.internal.listener.EventListener;
import org.openhab.binding.innogysmarthome.internal.manager.DeviceStructureManager;
import org.openhab.binding.innogysmarthome.internal.manager.FullDeviceManager;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link InnogyBridgeHandler} is responsible for handling the innogy SmartHome controller including the connection
 * to the innogy backend for all communications with the innogy {@link Device}s.
 * <p/>
 * It implements the {@link AccessTokenRefreshListener} to handle updates of the oauth2 tokens and the
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

    private @Nullable InnogyClient client;
    private @Nullable InnogyWebSocket webSocket;
    private @Nullable DeviceStructureManager deviceStructMan;
    private @Nullable String bridgeId;
    private @Nullable ScheduledFuture<?> reinitJob;
    private @NonNullByDefault({}) InnogyBridgeConfiguration bridgeConfiguration;
    private @Nullable OAuthClientService oAuthService;

    /**
     * Constructs a new {@link InnogyBridgeHandler}.
     *
     * @param bridge Bridge thing to be used by this handler
     * @param oAuthFactory Factory class to get OAuth2 service
     * @param httpClient httpclient instance
     */
    public InnogyBridgeHandler(final Bridge bridge, final OAuthFactory oAuthFactory, final HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // not needed
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(InnogyDeviceDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing innogy SmartHome BridgeHandler...");
        final InnogyBridgeConfiguration bridgeConfiguration = getConfigAs(InnogyBridgeConfiguration.class);
        if (checkConfig(bridgeConfiguration)) {
            this.bridgeConfiguration = bridgeConfiguration;
            getScheduler().execute(this::initializeClient);
        }
    }

    /**
     * Checks bridge configuration. If configuration is valid returns true.
     *
     * @return true if the configuration if valid
     */
    private boolean checkConfig(final InnogyBridgeConfiguration bridgeConfiguration) {
        if (BRAND_INNOGY_SMARTHOME.equals(bridgeConfiguration.brand)) {
            return true;
        } else {
            logger.debug("Invalid brand '{}'. Make sure to select a brand in the SHC thing configuration!",
                    bridgeConfiguration.brand);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid brand '"
                    + bridgeConfiguration.brand + "'. Make sure to select a brand in the SHC thing configuration!");
            return false;
        }
    }

    /**
     * Initializes the services and InnogyClient.
     */
    private void initializeClient() {
        final OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                API_URL_TOKEN, API_URL_TOKEN, bridgeConfiguration.clientId, bridgeConfiguration.clientSecret, null,
                true);
        this.oAuthService = oAuthService;

        if (checkOnAuthCode()) {
            final InnogyClient localClient = createInnogyClient(oAuthService, httpClient);
            client = localClient;
            deviceStructMan = new DeviceStructureManager(createFullDeviceManager(localClient));
            oAuthService.addAccessTokenRefreshListener(this);
            registerDeviceStatusListener(InnogyBridgeHandler.this);
            scheduleRestartClient(false);
        }
    }

    /**
     * Fetches the OAuth2 tokens from innogy SmartHome service if the auth code is set in the configuration and if
     * successful removes the auth code. Returns true if the auth code was not set or if the authcode was successfully
     * used to get a new refresh and access token.
     *
     * @return true if success
     */
    private boolean checkOnAuthCode() {
        if (!bridgeConfiguration.authcode.isBlank()) {
            logger.debug("Trying to get access and refresh tokens");
            try {
                oAuthService.getAccessTokenResponseByAuthorizationCode(bridgeConfiguration.authcode,
                        bridgeConfiguration.redirectUrl);
                final Configuration configuration = editConfiguration();
                configuration.put(CONFIG_AUTH_CODE, "");
                updateConfiguration(configuration);
            } catch (IOException | OAuthException | OAuthResponseException e) {
                logger.debug("Error fetching access tokens. Invalid authcode! Please generate a new one. Detail: {}",
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Cannot connect to innogy SmartHome service. Please set auth-code!");
                return false;
            }
        }
        return true;
    }

    /**
     * Initializes the client and connects to the innogy SmartHome service via Client API. Based on the provided
     * {@Link Configuration} while constructing {@Link InnogyClient}, the given oauth2 access and refresh tokens are
     * used or - if not yet available - new tokens are fetched from the service using the provided auth code.
     */
    private void startClient() {
        try {
            logger.debug("Initializing innogy SmartHome client...");
            final InnogyClient localClient = this.client;
            if (localClient != null) {
                localClient.refreshStatus();
            }
        } catch (AuthenticationException | ApiException | IOException e) {
            if (handleClientException(e)) {
                // If exception could not be handled properly it's no use to continue so we won't continue start
                logger.debug("Error initializing innogy SmartHome client.", e);
                return;
            }
        }
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            deviceStructMan.refreshDevices();
        } catch (IOException | ApiException | AuthenticationException e) {
            if (handleClientException(e)) {
                // If exception could not be handled properly it's no use to continue so we won't continue start
                logger.debug("Error starting device structure manager.", e);
                return;
            }
        }

        Device bridgeDevice = deviceStructMan.getBridgeDevice();
        if (bridgeDevice == null) {
            logger.debug("Failed to get bridge device, re-scheduling startClient.");
            scheduleRestartClient(true);
            return;
        }
        setBridgeProperties(bridgeDevice);
        bridgeId = bridgeDevice.getId();
        startWebsocket();
    }

    /**
     * Start the websocket connection for receiving permanent update {@link Event}s from the innogy API.
     */
    private void startWebsocket() {
        try {
            InnogyWebSocket localWebSocket = createWebSocket();

            if (this.webSocket != null && this.webSocket.isRunning()) {
                this.webSocket.stop();
                this.webSocket = null;
            }

            logger.debug("Starting innogy websocket.");
            this.webSocket = localWebSocket;
            localWebSocket.start();
            updateStatus(ThingStatus.ONLINE);
        } catch (final Exception e) { // Catch Exception because websocket start throws Exception
            logger.warn("Error starting websocket.", e);
            handleClientException(e);
        }
    }

    InnogyWebSocket createWebSocket() throws IOException, AuthenticationException {
        final AccessTokenResponse accessTokenResponse = client.getAccessTokenResponse();
        final String webSocketUrl = WEBSOCKET_API_URL_EVENTS.replace("{token}", accessTokenResponse.getAccessToken());

        logger.debug("WebSocket URL: {}...{}", webSocketUrl.substring(0, 70),
                webSocketUrl.substring(webSocketUrl.length() - 10));

        return new InnogyWebSocket(this, URI.create(webSocketUrl), bridgeConfiguration.websocketidletimeout * 1000);
    }

    @Override
    public void onAccessTokenResponse(final AccessTokenResponse credential) {
        scheduleRestartClient(true);
    }

    /**
     * Schedules a re-initialization in the given future.
     *
     * @param delayed when it is scheduled delayed, it starts with a delay of
     *            {@link org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants#REINITIALIZE_DELAY_SECONDS}
     *            seconds,
     *            otherwise it starts directly
     */
    private synchronized void scheduleRestartClient(final boolean delayed) {
        @Nullable
        final ScheduledFuture<?> localReinitJob = reinitJob;

        if (localReinitJob != null && isAlreadyScheduled(localReinitJob)) {
            logger.debug("Scheduling reinitialize - ignored: already triggered in {} seconds.",
                    localReinitJob.getDelay(TimeUnit.SECONDS));
            return;
        }

        final long seconds = delayed ? REINITIALIZE_DELAY_SECONDS : 0;
        logger.debug("Scheduling reinitialize in {} seconds.", seconds);
        reinitJob = getScheduler().schedule(this::startClient, seconds, TimeUnit.SECONDS);
    }

    private void setBridgeProperties(final Device bridgeDevice) {
        final DeviceConfig config = bridgeDevice.getConfig();

        logger.debug("Setting Bridge Device Properties for Bridge of type '{}' with ID '{}'", config.getName(),
                bridgeDevice.getId());
        final Map<String, String> properties = editProperties();

        setPropertyIfPresent(Thing.PROPERTY_VENDOR, bridgeDevice.getManufacturer(), properties);
        setPropertyIfPresent(Thing.PROPERTY_SERIAL_NUMBER, bridgeDevice.getSerialnumber(), properties);
        setPropertyIfPresent(PROPERTY_ID, bridgeDevice.getId(), properties);
        setPropertyIfPresent(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion(), properties);
        setPropertyIfPresent(Thing.PROPERTY_HARDWARE_VERSION, config.getHardwareVersion(), properties);
        setPropertyIfPresent(PROPERTY_SOFTWARE_VERSION, config.getSoftwareVersion(), properties);
        setPropertyIfPresent(PROPERTY_IP_ADDRESS, config.getIPAddress(), properties);
        setPropertyIfPresent(Thing.PROPERTY_MAC_ADDRESS, config.getMACAddress(), properties);
        if (config.getRegistrationTime() != null) {
            properties.put(PROPERTY_REGISTRATION_TIME,
                    config.getRegistrationTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        }
        setPropertyIfPresent(PROPERTY_CONFIGURATION_STATE, config.getConfigurationState(), properties);
        setPropertyIfPresent(PROPERTY_SHC_TYPE, bridgeDevice.getType(), properties);
        setPropertyIfPresent(PROPERTY_TIME_ZONE, config.getTimeZone(), properties);
        setPropertyIfPresent(PROPERTY_PROTOCOL_ID, config.getProtocolId(), properties);
        setPropertyIfPresent(PROPERTY_GEOLOCATION, config.getGeoLocation(), properties);
        setPropertyIfPresent(PROPERTY_CURRENT_UTC_OFFSET, config.getCurrentUTCOffset(), properties);
        setPropertyIfPresent(PROPERTY_BACKEND_CONNECTION_MONITORED, config.getBackendConnectionMonitored(), properties);
        setPropertyIfPresent(PROPERTY_RFCOM_FAILURE_NOTIFICATION, config.getRFCommFailureNotification(), properties);
        updateProperties(properties);
    }

    private void setPropertyIfPresent(final String key, final @Nullable Object data,
            final Map<String, String> properties) {
        if (data != null) {
            properties.put(key, data instanceof String ? (String) data : data.toString());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing innogy SmartHome bridge handler '{}'", getThing().getUID().getId());
        unregisterDeviceStatusListener(this);
        cancelReinitJob();
        if (webSocket != null) {
            webSocket.stop();
            webSocket = null;
        }
        client = null;
        deviceStructMan = null;

        super.dispose();
        logger.debug("innogy SmartHome bridge handler shut down.");
    }

    private synchronized void cancelReinitJob() {
        ScheduledFuture<?> reinitJob = this.reinitJob;

        if (reinitJob != null) {
            reinitJob.cancel(true);
            this.reinitJob = null;
        }
    }

    /**
     * Registers a {@link DeviceStatusListener}.
     *
     * @param deviceStatusListener
     * @return true, if successful
     */
    public boolean registerDeviceStatusListener(final DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.add(deviceStatusListener);
    }

    /**
     * Unregisters a {@link DeviceStatusListener}.
     *
     * @param deviceStatusListener
     * @return true, if successful
     */
    public boolean unregisterDeviceStatusListener(final DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    /**
     * Loads a Collection of {@link Device}s from the bridge and returns them.
     *
     * @return a Collection of {@link Device}s
     */
    public Collection<Device> loadDevices() {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
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
    public @Nullable Device getDeviceById(final String deviceId) {
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
    public @Nullable Device refreshDevice(final String deviceId) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return null;
        }

        Device device = null;
        try {
            deviceStructMan.refreshDevice(deviceId);
            device = deviceStructMan.getDeviceById(deviceId);
        } catch (IOException | ApiException | AuthenticationException e) {
            handleClientException(e);
        }
        return device;
    }

    @Override
    public void onDeviceStateChanged(final Device device) {
        synchronized (this.lock) {
            if (!bridgeId.equals(device.getId())) {
                logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                        bridgeId);
                return;
            }

            logger.debug("onDeviceStateChanged called with device {}/{}", device.getConfig().getName(), device.getId());

            // DEVICE STATES
            if (device.hasDeviceState()) {
                final Double cpuUsage = device.getDeviceState().getState().getCpuUsage().getValue();
                if (cpuUsage != null) {
                    logger.debug("-> CPU usage state: {}", cpuUsage);
                    updateState(CHANNEL_CPU, new DecimalType(cpuUsage));
                }
                final Double diskUsage = device.getDeviceState().getState().getDiskUsage().getValue();
                if (diskUsage != null) {
                    logger.debug("-> Disk usage state: {}", diskUsage);
                    updateState(CHANNEL_DISK, new DecimalType(diskUsage));
                }
                final Double memoryUsage = device.getDeviceState().getState().getMemoryUsage().getValue();
                if (memoryUsage != null) {
                    logger.debug("-> Memory usage state: {}", memoryUsage);
                    updateState(CHANNEL_MEMORY, new DecimalType(memoryUsage));
                }

            }

        }
    }

    @Override
    public void onDeviceStateChanged(final Device device, final Event event) {
        synchronized (this.lock) {
            if (!bridgeId.equals(device.getId())) {
                logger.trace("DeviceId {} not relevant for this handler (responsible for id {})", device.getId(),
                        bridgeId);
                return;
            }

            logger.trace("DeviceId {} relevant for this handler.", device.getId());

            if (event.isLinkedtoDevice() && DEVICE_SHCA.equals(device.getType())) {
                device.getDeviceState().getState().getCpuUsage().setValue(event.getProperties().getCpuUsage());
                device.getDeviceState().getState().getDiskUsage().setValue(event.getProperties().getDiskUsage());
                device.getDeviceState().getState().getMemoryUsage().setValue(event.getProperties().getMemoryUsage());
                onDeviceStateChanged(device);
            }
        }
    }

    @Override
    public void onEvent(final String msg) {
        logger.trace("onEvent called. Msg: {}", msg);

        try {
            final BaseEvent be = gson.fromJson(msg, BaseEvent.class);
            logger.debug("Event no {} found. Type: {}", be.getSequenceNumber(), be.getType());
            if (!BaseEvent.SUPPORTED_EVENT_TYPES.contains(be.getType())) {
                logger.debug("Event type {} not supported. Skipping...", be.getType());
            } else {
                final Event event = gson.fromJson(msg, Event.class);

                switch (event.getType()) {
                    case BaseEvent.TYPE_STATE_CHANGED:
                    case BaseEvent.TYPE_BUTTON_PRESSED:
                        handleStateChangedEvent(event);
                        break;

                    case BaseEvent.TYPE_DISCONNECT:
                        logger.debug("Websocket disconnected.");
                        scheduleRestartClient(true);
                        break;

                    case BaseEvent.TYPE_CONFIGURATION_CHANGED:
                        if (client.getConfigVersion().equals(event.getConfigurationVersion().toString())) {
                            logger.debug(
                                    "Ignored configuration changed event with version '{}' as current version is '{}' the same.",
                                    event.getConfigurationVersion(), client.getConfigVersion());
                        } else {
                            logger.info("Configuration changed from version {} to {}. Restarting innogy binding...",
                                    client.getConfigVersion(), event.getConfigurationVersion());
                            scheduleRestartClient(false);
                        }
                        break;

                    case BaseEvent.TYPE_CONTROLLER_CONNECTIVITY_CHANGED:
                        handleControllerConnectivityChangedEvent(event);
                        break;

                    case BaseEvent.TYPE_NEW_MESSAGE_RECEIVED:
                    case BaseEvent.TYPE_MESSAGE_CREATED:
                        final MessageEvent messageEvent = gson.fromJson(msg, MessageEvent.class);
                        handleNewMessageReceivedEvent(Objects.requireNonNull(messageEvent));
                        break;

                    case BaseEvent.TYPE_MESSAGE_DELETED:
                        handleMessageDeletedEvent(event);
                        break;

                    default:
                        logger.debug("Unsupported eventtype {}.", event.getType());
                        break;
                }
            }
        } catch (IOException | ApiException | AuthenticationException | RuntimeException e) {
            logger.debug("Error with Event: {}", e.getMessage(), e);
            handleClientException(e);
        }
    }

    @Override
    public void onError(final Throwable cause) {
        if (cause instanceof Exception) {
            handleClientException((Exception) cause);
        }
    }

    /**
     * Handles the event that occurs, when the state of a device (like reachability) or a capability (like a temperature
     * value) has changed.
     *
     * @param event
     * @throws ApiException
     * @throws IOException
     * @throws AuthenticationException
     */
    public void handleStateChangedEvent(final Event event) throws ApiException, IOException, AuthenticationException {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }

        // CAPABILITY
        if (event.isLinkedtoCapability()) {
            logger.trace("Event is linked to capability");
            final Device device = deviceStructMan.getDeviceByCapabilityId(event.getSourceId());
            if (device != null) {
                for (final DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
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
            final Device device = deviceStructMan.getDeviceById(event.getSourceId());
            if (device != null) {
                for (final DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
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
     * @throws AuthenticationException
     */
    public void handleControllerConnectivityChangedEvent(final Event event)
            throws ApiException, IOException, AuthenticationException {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        final Boolean connected = event.getIsConnected();
        if (connected != null) {
            logger.debug("SmartHome Controller connectivity changed to {}.", connected ? "online" : "offline");
            if (connected) {
                deviceStructMan.refreshDevices();
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
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
     * @throws AuthenticationException
     */
    public void handleNewMessageReceivedEvent(final MessageEvent event)
            throws ApiException, IOException, AuthenticationException {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        final Message message = event.getMessage();
        if (logger.isTraceEnabled()) {
            logger.trace("Message: {}", gson.toJson(message));
            logger.trace("Messagetype: {}", message.getType());
        }
        if (Message.TYPE_DEVICE_LOW_BATTERY.equals(message.getType()) && message.getDevices() != null) {
            for (final String link : message.getDevices()) {
                deviceStructMan.refreshDevice(Link.getId(link));
                final Device device = deviceStructMan.getDeviceById(Link.getId(link));
                if (device != null) {
                    for (final DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
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
     * @throws AuthenticationException
     */
    public void handleMessageDeletedEvent(final Event event) throws ApiException, IOException, AuthenticationException {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        final String messageId = event.getData().getId();

        logger.debug("handleMessageDeletedEvent with messageId '{}'", messageId);
        Device device = deviceStructMan.getDeviceWithMessageId(messageId);

        if (device != null) {
            String id = device.getId();
            deviceStructMan.refreshDevice(id);
            device = deviceStructMan.getDeviceById(id);
            if (device != null) {
                for (final DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    deviceStatusListener.onDeviceStateChanged(device);
                }
            } else {
                logger.debug("No device with id {} found after refresh.", id);
            }
        } else {
            logger.debug("No device found with message id {}.", messageId);
        }
    }

    @Override
    public void connectionClosed() {
        scheduleRestartClient(true);
    }

    /**
     * Sends the command to switch the {@link Device} with the given id to the new state. Is called by the
     * {@link InnogyDeviceHandler} for switch devices like the VariableActuator, PSS, PSSO or ISS2.
     *
     * @param deviceId
     * @param state
     */
    public void commandSwitchDevice(final String deviceId, final boolean state) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            // VariableActuator
            final String deviceType = deviceStructMan.getDeviceById(deviceId).getType();
            if (DEVICE_VARIABLE_ACTUATOR.equals(deviceType)) {
                final String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_VARIABLEACTUATOR);
                if (capabilityId == null) {
                    return;
                }
                client.setVariableActuatorState(capabilityId, state);

                // PSS / PSSO / ISS2
            } else if (DEVICE_PSS.equals(deviceType) || DEVICE_PSSO.equals(deviceType)
                    || DEVICE_ISS2.equals(deviceType)) {
                final String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_SWITCHACTUATOR);
                if (capabilityId == null) {
                    return;
                }
                client.setSwitchActuatorState(capabilityId, state);
            }
        } catch (IOException | ApiException | AuthenticationException e) {
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
    public void commandUpdatePointTemperature(final String deviceId, final double pointTemperature) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            final String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_THERMOSTATACTUATOR);
            if (capabilityId == null) {
                return;
            }
            client.setPointTemperatureState(capabilityId, pointTemperature);
        } catch (IOException | ApiException | AuthenticationException e) {
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
    public void commandSwitchAlarm(final String deviceId, final boolean alarmState) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            final String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_ALARMACTUATOR);
            if (capabilityId == null) {
                return;
            }
            client.setAlarmActuatorState(capabilityId, alarmState);
        } catch (IOException | ApiException | AuthenticationException e) {
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
    public void commandSetOperationMode(final String deviceId, final boolean autoMode) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            final String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_THERMOSTATACTUATOR);
            if (capabilityId == null) {
                return;
            }
            client.setOperationMode(capabilityId, autoMode);
        } catch (IOException | ApiException | AuthenticationException e) {
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
    public void commandSetDimmLevel(final String deviceId, final int dimLevel) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            final String capabilityId = deviceStructMan.getCapabilityId(deviceId, Capability.TYPE_DIMMERACTUATOR);
            if (capabilityId == null) {
                return;
            }
            client.setDimmerActuatorState(capabilityId, dimLevel);
        } catch (IOException | ApiException | AuthenticationException e) {
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
    public void commandSetRollerShutterLevel(final String deviceId, final int rollerSchutterLevel) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            final String capabilityId = deviceStructMan.getCapabilityId(deviceId,
                    Capability.TYPE_ROLLERSHUTTERACTUATOR);
            if (capabilityId == null) {
                return;
            }
            client.setRollerShutterActuatorState(capabilityId, rollerSchutterLevel);
        } catch (IOException | ApiException | AuthenticationException e) {
            handleClientException(e);
        }
    }

    /**
     * Sends the command to start or stop moving the rollershutter (ISR2) in a specified direction
     *
     * @param deviceId
     * @param action
     */
    public void commandSetRollerShutterStop(final String deviceId, ShutterAction.ShutterActions action) {
        final DeviceStructureManager deviceStructMan = this.deviceStructMan;
        if (deviceStructMan == null) {
            return;
        }
        try {
            final String capabilityId = deviceStructMan.getCapabilityId(deviceId,
                    Capability.TYPE_ROLLERSHUTTERACTUATOR);
            if (capabilityId == null) {
                return;
            }
            client.setRollerShutterAction(capabilityId, action);
        } catch (IOException | ApiException | AuthenticationException e) {
            handleClientException(e);
        }
    }

    ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    FullDeviceManager createFullDeviceManager(InnogyClient client) {
        return new FullDeviceManager(client);
    }

    InnogyClient createInnogyClient(final OAuthClientService oAuthService, final HttpClient httpClient) {
        return new InnogyClient(oAuthService, httpClient);
    }

    /**
     * Handles all Exceptions of the client communication. For minor "errors" like an already existing session, it
     * returns true to inform the binding to continue running. In other cases it may e.g. schedule a reinitialization of
     * the binding.
     *
     * @param e the Exception
     * @return boolean true, if binding should continue.
     */
    private boolean handleClientException(final Exception e) {
        boolean isReinitialize = true;
        if (e instanceof SessionExistsException) {
            logger.debug("Session already exists. Continuing...");
            isReinitialize = false;
        } else if (e instanceof InvalidActionTriggeredException) {
            logger.debug("Error triggering action: {}", e.getMessage());
            isReinitialize = false;
        } else if (e instanceof RemoteAccessNotAllowedException) {
            // Remote access not allowed (usually by IP address change)
            logger.debug("Remote access not allowed. Dropping access token and reinitializing binding...");
            refreshAccessToken();
        } else if (e instanceof ControllerOfflineException) {
            logger.debug("innogy SmartHome Controller is offline.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        } else if (e instanceof AuthenticationException) {
            logger.debug("OAuthenticaton error, refreshing tokens: {}", e.getMessage());
            refreshAccessToken();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } else if (e instanceof IOException) {
            logger.debug("IO error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof ApiException) {
            logger.warn("Unexpected API error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof TimeoutException) {
            logger.debug("WebSocket timeout: {}", e.getMessage());
        } else if (e instanceof SocketTimeoutException) {
            logger.debug("Socket timeout: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof InterruptedException) {
            isReinitialize = false;
            Thread.currentThread().interrupt();
        } else if (e instanceof ExecutionException) {
            logger.debug("ExecutionException: {}", ExceptionUtils.getRootCauseMessage(e));
            updateStatus(ThingStatus.OFFLINE);
        } else {
            logger.debug("Unknown exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
        if (isReinitialize) {
            scheduleRestartClient(true);
            return true;
        }
        return false;
    }

    private void refreshAccessToken() {
        try {
            final OAuthClientService localOAuthService = this.oAuthService;

            if (localOAuthService != null) {
                oAuthService.refreshToken();
            }
        } catch (IOException | OAuthResponseException | OAuthException e) {
            logger.debug("Could not refresh tokens", e);
        }
    }

    /**
     * Checks if the job is already (re-)scheduled.
     *
     * @param job job to check
     * @return true, when the job is already (re-)scheduled, otherwise false
     */
    private static boolean isAlreadyScheduled(ScheduledFuture<?> job) {
        return job.getDelay(TimeUnit.SECONDS) > 0;
    }
}
