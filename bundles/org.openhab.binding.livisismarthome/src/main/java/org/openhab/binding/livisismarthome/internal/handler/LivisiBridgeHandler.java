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
package org.openhab.binding.livisismarthome.internal.handler;

import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.livisismarthome.internal.LivisiBindingConstants;
import org.openhab.binding.livisismarthome.internal.LivisiWebSocket;
import org.openhab.binding.livisismarthome.internal.client.GsonOptional;
import org.openhab.binding.livisismarthome.internal.client.LivisiClient;
import org.openhab.binding.livisismarthome.internal.client.URLConnectionFactory;
import org.openhab.binding.livisismarthome.internal.client.URLCreator;
import org.openhab.binding.livisismarthome.internal.client.api.entity.action.ShutterActionType;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceConfigDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.BaseEventDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.MessageEventDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;
import org.openhab.binding.livisismarthome.internal.client.exception.ApiException;
import org.openhab.binding.livisismarthome.internal.client.exception.AuthenticationException;
import org.openhab.binding.livisismarthome.internal.client.exception.ControllerOfflineException;
import org.openhab.binding.livisismarthome.internal.client.exception.InvalidActionTriggeredException;
import org.openhab.binding.livisismarthome.internal.client.exception.RemoteAccessNotAllowedException;
import org.openhab.binding.livisismarthome.internal.client.exception.SessionExistsException;
import org.openhab.binding.livisismarthome.internal.discovery.LivisiDeviceDiscoveryService;
import org.openhab.binding.livisismarthome.internal.listener.DeviceStatusListener;
import org.openhab.binding.livisismarthome.internal.listener.EventListener;
import org.openhab.binding.livisismarthome.internal.manager.DeviceStructureManager;
import org.openhab.binding.livisismarthome.internal.manager.FullDeviceManager;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LivisiBridgeHandler} is responsible for handling the LIVISI SmartHome controller including the connection
 * to the LIVISI SmartHome backend for all communications with the LIVISI SmartHome {@link DeviceDTO}s.
 * <p/>
 * It implements the {@link AccessTokenRefreshListener} to handle updates of the oauth2 tokens and the
 * {@link EventListener} to handle {@link EventDTO}s, that are received by the {@link LivisiWebSocket}.
 * <p/>
 * The {@link DeviceDTO}s are organized by the {@link DeviceStructureManager}, which is also responsible for the
 * connection
 * to the LIVISI SmartHome webservice via the {@link LivisiClient}.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored to use openHAB http and oauth2 libraries
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@NonNullByDefault
public class LivisiBridgeHandler extends BaseBridgeHandler
        implements AccessTokenRefreshListener, EventListener, DeviceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(LivisiBridgeHandler.class);
    private final GsonOptional gson = new GsonOptional();
    private final Object lock = new Object();
    private final Map<String, DeviceStatusListener> deviceStatusListeners;
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    private @NonNullByDefault({}) LivisiClient client;
    private @Nullable LivisiWebSocket webSocket;
    private @NonNullByDefault({}) DeviceStructureManager deviceStructMan;
    private @Nullable String bridgeId;
    private @Nullable ScheduledFuture<?> reInitJob;
    private @Nullable ScheduledFuture<?> bridgeRefreshJob;
    private @NonNullByDefault({}) LivisiBridgeConfiguration bridgeConfiguration;
    private @NonNullByDefault({}) OAuthClientService oAuthService;
    private String configVersion = "";

    /**
     * Constructs a new {@link LivisiBridgeHandler}.
     *
     * @param bridge Bridge thing to be used by this handler
     * @param oAuthFactory Factory class to get OAuth2 service
     * @param httpClient httpclient instance
     */
    public LivisiBridgeHandler(final Bridge bridge, final OAuthFactory oAuthFactory, final HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        deviceStatusListeners = new ConcurrentHashMap<>();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // not needed
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LivisiDeviceDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing LIVISI SmartHome BridgeHandler...");
        bridgeConfiguration = getConfigAs(LivisiBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        initializeClient();
    }

    /**
     * Initializes the services and LivisiClient.
     */
    private void initializeClient() {
        String tokenURL = URLCreator.createTokenURL(bridgeConfiguration.host);
        oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), tokenURL, tokenURL,
                "clientId", "clientPass", null, true);
        client = createClient(oAuthService);
        deviceStructMan = new DeviceStructureManager(createFullDeviceManager(client));
        oAuthService.addAccessTokenRefreshListener(this);

        getScheduler().schedule(() -> {
            try {
                requestAccessToken();

                scheduleRestartClient(false);
            } catch (IOException | OAuthException | OAuthResponseException e) {
                logger.debug("Error fetching access tokens. Please check your credentials. Detail: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.connect");
            }
        }, 0, TimeUnit.SECONDS);
    }

    /**
     * Initializes the client and connects to the LIVISI SmartHome service via Client API. Based on the provided
     * configuration while constructing {@link LivisiClient}, the given oauth2 access and refresh tokens are
     * used or - if not yet available - new tokens are fetched from the service using the provided auth code.
     */
    private void startClient() {
        logger.debug("Initializing LIVISI SmartHome client...");
        boolean isSuccessfullyRefreshed = refreshDevices();
        if (isSuccessfullyRefreshed) {
            Optional<DeviceDTO> bridgeDeviceOptional = getBridgeDevice();
            if (bridgeDeviceOptional.isPresent()) {
                DeviceDTO bridgeDevice = bridgeDeviceOptional.get();
                bridgeId = bridgeDevice.getId();
                setBridgeProperties(bridgeDevice);

                registerDeviceStatusListener(bridgeDevice.getId(), this);
                onDeviceStateChanged(bridgeDevice); // initialize channels
                scheduleBridgeRefreshJob(bridgeDevice);

                startWebSocket(bridgeDevice);
            } else {
                logger.debug("Failed to get bridge device, re-scheduling startClient.");
                scheduleRestartClient(true);
            }
        }
    }

    private boolean refreshDevices() {
        try {
            configVersion = client.refreshStatus();
            deviceStructMan.refreshDevices();
            return true;
        } catch (IOException e) {
            if (handleClientException(e)) {
                // If exception could not be handled properly it's no use to continue so we won't continue start
                logger.debug("Error initializing LIVISI SmartHome client.", e);
            }
        }
        return false;
    }

    /**
     * Start the websocket connection for receiving permanent update {@link EventDTO}s from the LIVISI API.
     */
    private void startWebSocket(DeviceDTO bridgeDevice) {
        try {
            stopWebSocket();

            logger.debug("Starting LIVISI SmartHome websocket.");
            webSocket = createAndStartWebSocket(bridgeDevice);
            updateStatus(ThingStatus.ONLINE);
        } catch (final IOException e) {
            logger.warn("Error starting websocket.", e);
            handleClientException(e);
        }
    }

    private void stopWebSocket() {
        LivisiWebSocket webSocket = this.webSocket;
        if (webSocket != null && webSocket.isRunning()) {
            logger.debug("Stopping LIVISI SmartHome websocket.");
            webSocket.stop();
            this.webSocket = null;
        }
    }

    @Nullable
    LivisiWebSocket createAndStartWebSocket(DeviceDTO bridgeDevice) throws IOException {
        final Optional<String> accessToken = getAccessToken(client);
        if (accessToken.isEmpty()) {
            return null;
        }

        final String webSocketUrl = URLCreator.createEventsURL(bridgeConfiguration.host, accessToken.get(),
                bridgeDevice.isClassicController());

        logger.debug("WebSocket URL: {}...{}", webSocketUrl.substring(0, 70),
                webSocketUrl.substring(webSocketUrl.length() - 10));

        LivisiWebSocket webSocket = new LivisiWebSocket(httpClient, this, URI.create(webSocketUrl),
                bridgeConfiguration.webSocketIdleTimeout * 1000);
        webSocket.start();
        return webSocket;
    }

    private static Optional<String> getAccessToken(LivisiClient client) throws IOException {
        return Optional.of(client.getAccessTokenResponse().getAccessToken());
    }

    @Override
    public void onAccessTokenResponse(final AccessTokenResponse credential) {
        scheduleRestartClient(true);
    }

    /**
     * Schedules a re-initialization in the given future.
     *
     * @param delayed when it is scheduled delayed, it starts with a delay of
     *            {@link org.openhab.binding.livisismarthome.internal.LivisiBindingConstants#REINITIALIZE_DELAY_SECONDS}
     *            seconds,
     *            otherwise it starts directly
     */
    private synchronized void scheduleRestartClient(final boolean delayed) {
        final ScheduledFuture<?> reInitJobLocal = this.reInitJob;
        if (reInitJobLocal == null || !isAlreadyScheduled(reInitJobLocal)) {
            long delaySeconds = 0;
            if (delayed) {
                delaySeconds = REINITIALIZE_DELAY_SECONDS;
            }
            logger.debug("Scheduling reinitialize in {} delaySeconds.", delaySeconds);
            this.reInitJob = getScheduler().schedule(this::startClient, delaySeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Starts a refresh job for the bridge channels, because the SHC 1 (classic) doesn't send events
     * for cpu, memory, disc or operation state changes.
     * The refresh job is only executed for SHC 1 (classic) bridges, newer bridges like SHC 2 do send events.
     */
    private void scheduleBridgeRefreshJob(DeviceDTO bridgeDevice) {
        if (bridgeDevice.isClassicController()) {
            final ScheduledFuture<?> bridgeRefreshJobLocal = this.bridgeRefreshJob;
            if (bridgeRefreshJobLocal == null || !isAlreadyScheduled(bridgeRefreshJobLocal)) {
                logger.debug("Scheduling bridge refresh job with an interval of {} seconds.", BRIDGE_REFRESH_SECONDS);

                this.bridgeRefreshJob = getScheduler().scheduleWithFixedDelay(() -> {
                    logger.debug("Refreshing bridge");

                    refreshBridgeState();
                    onDeviceStateChanged(bridgeDevice);
                }, BRIDGE_REFRESH_SECONDS, BRIDGE_REFRESH_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void setBridgeProperties(final DeviceDTO bridgeDevice) {
        final DeviceConfigDTO config = bridgeDevice.getConfig();

        logger.debug("Setting Bridge Device Properties for Bridge of type '{}' with ID '{}'", config.getName(),
                bridgeDevice.getId());
        final Map<String, String> properties = editProperties();

        setPropertyIfPresent(Thing.PROPERTY_VENDOR, bridgeDevice.getManufacturer(), properties);
        setPropertyIfPresent(Thing.PROPERTY_SERIAL_NUMBER, bridgeDevice.getSerialNumber(), properties);
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
            properties.put(key, data.toString());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing LIVISI SmartHome bridge handler '{}'", getThing().getUID().getId());
        unregisterDeviceStatusListener(bridgeId);
        cancelJobs();
        stopWebSocket();
        client = null;
        deviceStructMan = null;

        super.dispose();
        logger.debug("LIVISI SmartHome bridge handler shut down.");
    }

    private synchronized void cancelJobs() {
        if (cancelJob(reInitJob)) {
            reInitJob = null;
        }
        if (cancelJob(bridgeRefreshJob)) {
            bridgeRefreshJob = null;
        }
    }

    private static boolean cancelJob(@Nullable ScheduledFuture<?> job) {
        if (job != null) {
            job.cancel(true);
            return true;
        }
        return false;
    }

    /**
     * Registers a {@link DeviceStatusListener}.
     *
     * @param deviceStatusListener listener
     */
    public void registerDeviceStatusListener(final String deviceId, final DeviceStatusListener deviceStatusListener) {
        deviceStatusListeners.putIfAbsent(deviceId, deviceStatusListener);
    }

    /**
     * Unregisters a {@link DeviceStatusListener}.
     *
     * @param deviceId id of the device to which the listener is registered
     */
    public void unregisterDeviceStatusListener(@Nullable final String deviceId) {
        if (deviceId != null) {
            deviceStatusListeners.remove(deviceId);
        }
    }

    /**
     * Loads a Collection of {@link DeviceDTO}s from the bridge and returns them.
     *
     * @return a Collection of {@link DeviceDTO}s
     */
    public Collection<DeviceDTO> loadDevices() {
        return deviceStructMan.getDeviceList();
    }

    public boolean isSHCClassic() {
        return getBridgeDevice().filter(DeviceDTO::isClassicController).isPresent();
    }

    /**
     * Returns the bridge {@link DeviceDTO}.
     *
     * @return bridge {@link DeviceDTO}
     */
    private Optional<DeviceDTO> getBridgeDevice() {
        return deviceStructMan.getBridgeDevice();
    }

    /**
     * Returns the {@link DeviceDTO} with the given deviceId.
     *
     * @param deviceId device id
     * @return {@link DeviceDTO} or null, if it does not exist or no {@link DeviceStructureManager} is available
     */
    public Optional<DeviceDTO> getDeviceById(final String deviceId) {
        return deviceStructMan.getDeviceById(deviceId);
    }

    private void refreshBridgeState() {
        Optional<DeviceDTO> bridgeOptional = getBridgeDevice();
        if (bridgeOptional.isPresent()) {
            try {
                DeviceDTO bridgeDevice = bridgeOptional.get();

                DeviceStateDTO deviceState = new DeviceStateDTO();
                deviceState.setId(bridgeDevice.getId());
                deviceState.setState(client.getDeviceStateByDeviceId(bridgeDevice.getId(), isSHCClassic()));
                bridgeDevice.setDeviceState(deviceState);
            } catch (IOException e) {
                logger.debug("Exception occurred on reloading bridge", e);
            }
        }
    }

    /**
     * Refreshes the {@link DeviceDTO} with the given id, by reloading the full device from the LIVISI webservice.
     *
     * @param deviceId device id
     * @return the {@link DeviceDTO} or null, if it does not exist or no {@link DeviceStructureManager} is available
     */
    public Optional<DeviceDTO> refreshDevice(final String deviceId) {
        try {
            return deviceStructMan.refreshDevice(deviceId, isSHCClassic());
        } catch (IOException e) {
            handleClientException(e);
        }
        return Optional.empty();
    }

    @Override
    public void onDeviceStateChanged(final DeviceDTO bridgeDevice) {
        synchronized (this.lock) {
            // DEVICE STATES
            if (bridgeDevice.hasDeviceState()) {
                final boolean isSHCClassic = bridgeDevice.isClassicController();
                final Double cpuUsage = bridgeDevice.getDeviceState().getState().getCpuUsage(isSHCClassic).getValue();
                if (cpuUsage != null) {
                    logger.debug("-> CPU usage state: {}", cpuUsage);
                    updateState(CHANNEL_CPU, QuantityType.valueOf(cpuUsage, Units.PERCENT));
                }
                final Double diskUsage = bridgeDevice.getDeviceState().getState().getDiskUsage().getValue();
                if (diskUsage != null) {
                    logger.debug("-> Disk usage state: {}", diskUsage);
                    updateState(CHANNEL_DISK, QuantityType.valueOf(diskUsage, Units.PERCENT));
                }
                final Double memoryUsage = bridgeDevice.getDeviceState().getState().getMemoryUsage(isSHCClassic)
                        .getValue();
                if (memoryUsage != null) {
                    logger.debug("-> Memory usage state: {}", memoryUsage);
                    updateState(CHANNEL_MEMORY, QuantityType.valueOf(memoryUsage, Units.PERCENT));
                }
                String operationStatus = bridgeDevice.getDeviceState().getState().getOperationStatus(isSHCClassic)
                        .getValue();
                if (operationStatus != null) {
                    logger.debug("-> Operation status: {}", operationStatus);
                    updateState(CHANNEL_OPERATION_STATUS, new StringType(operationStatus.toUpperCase()));
                }
            }
        }
    }

    @Override
    public void onDeviceStateChanged(final DeviceDTO bridgeDevice, final EventDTO event) {
        synchronized (this.lock) {
            if (event.isLinkedtoDevice()) {
                final boolean isSHCClassic = bridgeDevice.isClassicController();
                bridgeDevice.getDeviceState().getState().getOperationStatus(isSHCClassic)
                        .setValue(event.getProperties().getOperationStatus(isSHCClassic));
                bridgeDevice.getDeviceState().getState().getCpuUsage(isSHCClassic)
                        .setValue(event.getProperties().getCpuUsage(isSHCClassic));
                bridgeDevice.getDeviceState().getState().getDiskUsage().setValue(event.getProperties().getDiskUsage());
                bridgeDevice.getDeviceState().getState().getMemoryUsage(isSHCClassic)
                        .setValue(event.getProperties().getMemoryUsage(isSHCClassic));
                onDeviceStateChanged(bridgeDevice);
            }
        }
    }

    @Override
    public void onEvent(final String msg) {
        logger.trace("onEvent called. Msg: {}", msg);

        try {
            final Optional<EventDTO> eventOptional = parseEvent(msg);
            if (eventOptional.isPresent()) {
                EventDTO event = eventOptional.get();
                switch (event.getType()) {
                    case BaseEventDTO.TYPE_STATE_CHANGED:
                    case BaseEventDTO.TYPE_BUTTON_PRESSED:
                        handleStateChangedEvent(event);
                        break;
                    case BaseEventDTO.TYPE_DISCONNECT:
                        logger.debug("Websocket disconnected.");
                        scheduleRestartClient(true);
                        break;
                    case BaseEventDTO.TYPE_CONFIGURATION_CHANGED:
                        handleConfigurationChangedEvent(event);
                        break;
                    case BaseEventDTO.TYPE_CONTROLLER_CONNECTIVITY_CHANGED:
                        handleControllerConnectivityChangedEvent(event);
                        break;
                    case BaseEventDTO.TYPE_NEW_MESSAGE_RECEIVED:
                    case BaseEventDTO.TYPE_MESSAGE_CREATED:
                        final Optional<MessageEventDTO> messageEvent = gson.fromJson(msg, MessageEventDTO.class);
                        if (messageEvent.isPresent()) {
                            handleNewMessageReceivedEvent(Objects.requireNonNull(messageEvent.get()));
                        }
                        break;
                    case BaseEventDTO.TYPE_MESSAGE_DELETED:
                        handleMessageDeletedEvent(event);
                        break;
                    default:
                        logger.debug("Unsupported event type {}.", event.getType());
                        break;
                }
            }
        } catch (IOException | RuntimeException e) {
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
     * @param event event
     */
    private void handleStateChangedEvent(final EventDTO event) throws IOException {

        // CAPABILITY
        if (event.isLinkedtoCapability()) {
            logger.trace("Event is linked to capability");
            final Optional<DeviceDTO> device = deviceStructMan.getDeviceByCapabilityId(event.getSourceId());
            notifyDeviceStatusListeners(device, event);

            // DEVICE
        } else if (event.isLinkedtoDevice()) {
            logger.trace("Event is linked to device");
            final String sourceId = event.getSourceId();

            final Optional<DeviceDTO> bridgeDevice = deviceStructMan.getBridgeDevice();
            final Optional<DeviceDTO> device;
            if (bridgeDevice.isPresent() && !sourceId.equals(bridgeDevice.get().getId())) {
                device = deviceStructMan.refreshDevice(sourceId, isSHCClassic());
            } else {
                device = deviceStructMan.getDeviceById(sourceId);
            }
            notifyDeviceStatusListeners(device, event);

        } else {
            logger.debug("link type {} not supported (yet?)", event.getSourceLinkType());
        }
    }

    /**
     * Handles the event that occurs, when the connectivity of the bridge has changed.
     *
     * @param event event
     */
    private void handleControllerConnectivityChangedEvent(final EventDTO event) throws IOException {

        final Boolean connected = event.getIsConnected();
        if (connected != null) {
            final ThingStatus thingStatus;
            if (connected) {
                deviceStructMan.refreshDevices();
                thingStatus = ThingStatus.ONLINE;
                updateStatus(thingStatus);
            } else {
                thingStatus = ThingStatus.OFFLINE;
            }
            logger.debug("SmartHome Controller connectivity changed to {} by {} event.", thingStatus,
                    BaseEventDTO.TYPE_CONTROLLER_CONNECTIVITY_CHANGED);
        } else {
            logger.debug("isConnected property missing in {} event (returned null)!",
                    BaseEventDTO.TYPE_CONTROLLER_CONNECTIVITY_CHANGED);
        }
    }

    /**
     * Handles the event that occurs, when a new message was received. Currently only handles low battery messages.
     *
     * @param event event
     */
    private void handleNewMessageReceivedEvent(final MessageEventDTO event) throws IOException {

        final MessageDTO message = event.getMessage();
        if (logger.isTraceEnabled()) {
            logger.trace("Message: {}", gson.toJson(message));
            logger.trace("Messagetype: {}", message.getType());
        }
        if (MessageDTO.TYPE_DEVICE_LOW_BATTERY.equals(message.getType()) && message.getDevices() != null) {
            for (final String link : message.getDevices()) {
                final Optional<DeviceDTO> device = deviceStructMan.refreshDevice(LinkDTO.getId(link), isSHCClassic());
                notifyDeviceStatusListener(event.getSourceId(), device);
            }
        } else {
            logger.debug("Message received event not yet implemented for Messagetype {}.", message.getType());
        }
    }

    /**
     * Handle the event that occurs, when a message was deleted. In case of a low battery message this means, that the
     * device is back to normal. Currently, only messages linked to devices are handled by refreshing the device data
     * and informing the {@link LivisiDeviceHandler} about the changed device.
     *
     * @param event event
     */
    private void handleMessageDeletedEvent(final EventDTO event) throws IOException {

        final String messageId = event.getData().getId();
        logger.debug("handleMessageDeletedEvent with messageId '{}'", messageId);

        Optional<DeviceDTO> device = deviceStructMan.getDeviceWithMessageId(messageId);
        if (device.isPresent()) {
            String id = device.get().getId();
            Optional<DeviceDTO> deviceRefreshed = deviceStructMan.refreshDevice(id, isSHCClassic());
            notifyDeviceStatusListener(event.getSourceId(), deviceRefreshed);
        } else {
            logger.debug("No device found with message id {}.", messageId);
        }
    }

    private void handleConfigurationChangedEvent(EventDTO event) {
        if (configVersion.equals(event.getConfigurationVersion().toString())) {
            logger.debug("Ignored configuration changed event with version '{}' as current version is '{}' the same.",
                    event.getConfigurationVersion(), configVersion);
        } else {
            logger.info("Configuration changed from version {} to {}. Restarting LIVISI SmartHome binding...",
                    configVersion, event.getConfigurationVersion());
            scheduleRestartClient(false);
        }
    }

    private void notifyDeviceStatusListener(String deviceId, Optional<DeviceDTO> device) {
        if (device.isPresent()) {
            DeviceStatusListener deviceStatusListener = deviceStatusListeners.get(device.get().getId());
            if (deviceStatusListener != null) {
                deviceStatusListener.onDeviceStateChanged(device.get());
            } else {
                logger.debug("No device status listener registered for device {}.", deviceId);
            }
        } else {
            logger.debug("Unknown/unsupported device {}.", deviceId);
        }
    }

    private void notifyDeviceStatusListeners(Optional<DeviceDTO> device, EventDTO event) {
        String sourceId = event.getSourceId();
        if (device.isPresent()) {
            DeviceStatusListener deviceStatusListener = deviceStatusListeners.get(device.get().getId());
            if (deviceStatusListener != null) {
                deviceStatusListener.onDeviceStateChanged(device.get(), event);
            } else {
                logger.debug("No device status listener registered for device / capability {}.", sourceId);
            }
        } else {
            logger.debug("Unknown/unsupported device / capability {}.", sourceId);
        }
    }

    @Override
    public void connectionClosed() {
        scheduleRestartClient(true);
    }

    /**
     * Sends the command to switch the {@link DeviceDTO} with the given id to the new state. Is called by the
     * {@link LivisiDeviceHandler} for switch devices like the VariableActuator, PSS, PSSO or ISS2.
     *
     * @param deviceId device id
     * @param state state (boolean)
     */
    public void commandSwitchDevice(final String deviceId, final boolean state) {
        // VariableActuator
        Optional<DeviceDTO> device = deviceStructMan.getDeviceById(deviceId);
        if (device.isPresent()) {
            final String deviceType = device.get().getType();
            if (DEVICE_VARIABLE_ACTUATOR.equals(deviceType)) {
                executeCommand(deviceId, CapabilityDTO.TYPE_VARIABLEACTUATOR,
                        (capabilityId) -> client.setVariableActuatorState(capabilityId, state));
                // PSS / PSSO / ISS2 / BT-PSS
            } else if (DEVICE_PSS.equals(deviceType) || DEVICE_PSSO.equals(deviceType) || DEVICE_ISS2.equals(deviceType)
                    || DEVICE_BT_PSS.equals((deviceType))) {
                executeCommand(deviceId, CapabilityDTO.TYPE_SWITCHACTUATOR,
                        (capabilityId) -> client.setSwitchActuatorState(capabilityId, state));
            }
        } else {
            logger.debug("No device with id {} could get found!", deviceId);
        }
    }

    /**
     * Sends the command to update the point temperature of the {@link DeviceDTO} with the given deviceId. Is called by
     * the
     * {@link LivisiDeviceHandler} for thermostat {@link DeviceDTO}s like RST or WRT.
     *
     * @param deviceId device id
     * @param pointTemperature point temperature
     */
    public void commandUpdatePointTemperature(final String deviceId, final double pointTemperature) {
        executeCommand(deviceId, CapabilityDTO.TYPE_THERMOSTATACTUATOR,
                (capabilityId) -> client.setPointTemperatureState(capabilityId, pointTemperature));
    }

    /**
     * Sends the command to turn the alarm of the {@link DeviceDTO} with the given id on or off. Is called by the
     * {@link LivisiDeviceHandler} for smoke detector {@link DeviceDTO}s like WSD or WSD2.
     *
     * @param deviceId device id
     * @param alarmState alarm state (boolean)
     */
    public void commandSwitchAlarm(final String deviceId, final boolean alarmState) {
        executeCommand(deviceId, CapabilityDTO.TYPE_ALARMACTUATOR,
                (capabilityId) -> client.setAlarmActuatorState(capabilityId, alarmState));
    }

    /**
     * Sends the command to set the operation mode of the {@link DeviceDTO} with the given deviceId to auto (or manual,
     * if
     * false). Is called by the {@link LivisiDeviceHandler} for thermostat {@link DeviceDTO}s like RST.
     *
     * @param deviceId device id
     * @param isAutoMode true activates the automatic mode, false the manual mode.
     */
    public void commandSetOperationMode(final String deviceId, final boolean isAutoMode) {
        executeCommand(deviceId, CapabilityDTO.TYPE_THERMOSTATACTUATOR,
                (capabilityId) -> client.setOperationMode(capabilityId, isAutoMode));
    }

    /**
     * Sends the command to set the dimm level of the {@link DeviceDTO} with the given id. Is called by the
     * {@link LivisiDeviceHandler} for {@link DeviceDTO}s like ISD2 or PSD.
     *
     * @param deviceId device id
     * @param dimLevel dim level
     */
    public void commandSetDimLevel(final String deviceId, final int dimLevel) {
        executeCommand(deviceId, CapabilityDTO.TYPE_DIMMERACTUATOR,
                (capabilityId) -> client.setDimmerActuatorState(capabilityId, dimLevel));
    }

    /**
     * Sends the command to set the rollershutter level of the {@link DeviceDTO} with the given id. Is called by the
     * {@link LivisiDeviceHandler} for {@link DeviceDTO}s like ISR2.
     *
     * @param deviceId device id
     * @param rollerShutterLevel roller shutter level
     */
    public void commandSetRollerShutterLevel(final String deviceId, final int rollerShutterLevel) {
        executeCommand(deviceId, CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR,
                (capabilityId) -> client.setRollerShutterActuatorState(capabilityId, rollerShutterLevel));
    }

    /**
     * Sends the command to start or stop moving the rollershutter (ISR2) in a specified direction
     *
     * @param deviceId device id
     * @param action action
     */
    public void commandSetRollerShutterStop(final String deviceId, final ShutterActionType action) {
        executeCommand(deviceId, CapabilityDTO.TYPE_ROLLERSHUTTERACTUATOR,
                (capabilityId) -> client.setRollerShutterAction(capabilityId, action));
    }

    private void executeCommand(final String deviceId, final String capabilityType,
            final CommandExecutor commandExecutor) {
        try {
            final Optional<String> capabilityId = deviceStructMan.getCapabilityId(deviceId, capabilityType);
            if (capabilityId.isPresent()) {
                commandExecutor.executeCommand(capabilityId.get());
            }
        } catch (IOException e) {
            handleClientException(e);
        }
    }

    ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    FullDeviceManager createFullDeviceManager(LivisiClient client) {
        return new FullDeviceManager(client);
    }

    LivisiClient createClient(final OAuthClientService oAuthService) {
        return new LivisiClient(bridgeConfiguration, oAuthService, new URLConnectionFactory());
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
            logger.debug("LIVISI SmartHome Controller is offline.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof AuthenticationException) {
            logger.debug("OAuthenticaton error, refreshing tokens: {}", e.getMessage());
            refreshAccessToken();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } else if (e instanceof ApiException) {
            logger.warn("Unexpected API error: {}", e.getMessage());
            logger.debug("Unexpected API error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof TimeoutException) {
            logger.debug("WebSocket timeout: {}", e.getMessage());
        } else if (e instanceof SocketTimeoutException) {
            logger.debug("Socket timeout: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof IOException) {
            logger.debug("IOException occurred", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof InterruptedException) {
            isReinitialize = false;
            Thread.currentThread().interrupt();
        } else if (e instanceof ExecutionException) {
            logger.debug("ExecutionException occurred", e);
            updateStatus(ThingStatus.OFFLINE);
        } else {
            logger.debug("Unknown exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
        if (isReinitialize) {
            scheduleRestartClient(true);
        }
        return isReinitialize;
    }

    private void refreshAccessToken() {
        try {
            requestAccessToken();
        } catch (IOException | OAuthException | OAuthResponseException e) {
            logger.debug("Could not refresh tokens", e);
        }
    }

    private void requestAccessToken() throws OAuthException, IOException, OAuthResponseException {
        oAuthService.getAccessTokenByResourceOwnerPasswordCredentials(LivisiBindingConstants.USERNAME,
                bridgeConfiguration.password, null);
    }

    private Optional<EventDTO> parseEvent(final String msg) {
        final Optional<BaseEventDTO> baseEventOptional = gson.fromJson(msg, BaseEventDTO.class);
        if (baseEventOptional.isPresent()) {
            BaseEventDTO baseEvent = baseEventOptional.get();
            logger.debug("Event no {} found. Type: {}", baseEvent.getSequenceNumber(), baseEvent.getType());
            if (BaseEventDTO.SUPPORTED_EVENT_TYPES.contains(baseEvent.getType())) {
                return gson.fromJson(msg, EventDTO.class);
            }
            logger.debug("Event type {} not supported. Skipping...", baseEvent.getType());
        }
        return Optional.empty();
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

    @FunctionalInterface
    private interface CommandExecutor {

        void executeCommand(String capabilityId) throws IOException;
    }
}
