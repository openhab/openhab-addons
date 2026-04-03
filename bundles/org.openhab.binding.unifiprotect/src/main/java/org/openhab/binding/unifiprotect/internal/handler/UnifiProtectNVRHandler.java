/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.handler;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.UnifiProtectDiscoveryService;
import org.openhab.binding.unifiprotect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifiprotect.internal.api.priv.client.UniFiProtectPrivateClient;
import org.openhab.binding.unifiprotect.internal.api.priv.client.UniFiProtectPrivateWebSocket.WebSocketUpdate;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Chime;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Light;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Sensor;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.ApiKey;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Bootstrap;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Nvr;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.ModelType;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.DeviceState;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.EventType;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.gson.DeviceTypeAdapterFactory;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.gson.EventTypeAdapterFactory;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectNVRConfiguration;
import org.openhab.binding.unifiprotect.internal.handler.UnifiProtectAbstractDeviceHandler.WSEventType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Bridge handler for the UniFi Protect NVR.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectNVRHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(UnifiProtectNVRHandler.class);
    private @Nullable UniFiProtectHybridClient apiClient;
    private @Nullable ScheduledFuture<?> pollTask;
    private @Nullable ScheduledFuture<?> reconnectTask;
    private @Nullable UnifiProtectDiscoveryService discoveryService;
    private final HttpClient httpClient;
    private final Gson gson;
    private volatile boolean shuttingDown = false;

    private static final long WS_UPDATE_DEBOUNCE_MS = 500; // inactivity window
    private static final long WS_UPDATE_MAX_WAIT_MS = 2000; // max wait per burst
    private static final long CHILD_REFRESH_RETRY_DELAY_SECONDS = 10; // retry delay for failed child refresh
    private static final int WS_CONNECT_MAX_RETRIES = 3;
    private static final long WS_CONNECT_RETRY_DELAY_MS = 2000;
    private final Map<String, PendingUpdate> pendingEventUpdates = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> childRefreshRetryTasks = new ConcurrentHashMap<>();

    private static final class PendingUpdate {
        @Nullable
        BaseEvent lastEvent;
        @Nullable
        ScheduledFuture<?> debounceFuture;
        @Nullable
        ScheduledFuture<?> maxFuture;
    }

    public UnifiProtectNVRHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super((Bridge) thing);
        gson = new GsonBuilder().registerTypeAdapterFactory(new DeviceTypeAdapterFactory())
                .registerTypeAdapterFactory(new EventTypeAdapterFactory()).create();
        httpClient = httpClientFactory.createHttpClient(UnifiProtectBindingConstants.BINDING_ID,
                new SslContextFactory.Client(true));
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start HTTP client", e);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(UnifiProtectDiscoveryService.class);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child handler initialized: {}", childHandler);
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            if (childHandler instanceof UnifiProtectAbstractDeviceHandler<?> handler) {
                scheduler.execute(() -> {
                    Object devIdObj = childThing.getConfiguration().get(UnifiProtectBindingConstants.DEVICE_ID);
                    String deviceId = devIdObj != null ? String.valueOf(devIdObj) : null;
                    if (deviceId == null) {
                        return;
                    }
                    refreshChildFromApi(deviceId, handler);
                });
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("Initializing NVR");
        shuttingDown = false;
        final UnifiProtectNVRConfiguration config = getConfigAs(UnifiProtectNVRConfiguration.class);

        // Validate required fields (private API credentials are now required)
        if (config.hostname.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-credentials");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "@text/offline.initializing");

        scheduler.execute(() -> {
            try {
                String apiToken = config.token;

                // Auto-create API key if not provided
                if (apiToken == null || apiToken.isBlank()) {
                    logger.debug("No API token provided, auto-creating via Private API...");

                    try {
                        // Create temporary private client for key management
                        UniFiProtectPrivateClient tempClient = new UniFiProtectPrivateClient(httpClient, scheduler,
                                config.hostname, config.port, config.username, config.password);

                        tempClient.initialize().get(30, TimeUnit.SECONDS);

                        String userId = tempClient.getCurrentUserId().get(10, TimeUnit.SECONDS);
                        if (userId == null || userId.isBlank()) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/offline.conf-error-no-user-id");
                            return;
                        }

                        String keyName = "openHAB-" + getThing().getUID().getId();
                        ApiKey key = tempClient.getOrCreateApiKey(userId, keyName).get(10, TimeUnit.SECONDS);
                        if (key == null || key.fullApiKey == null || key.fullApiKey.isBlank()) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/offline.conf-error-api-key-empty");
                            return;
                        }

                        apiToken = key.fullApiKey;
                        logger.debug("Successfully created API key '{}': {}***", keyName,
                                apiToken.substring(0, Math.min(8, apiToken.length())));

                        Configuration thingConfig = editConfiguration();
                        thingConfig.put("token", apiToken);
                        updateConfiguration(thingConfig);
                        logger.debug("Saved auto-created API token to configuration");

                        tempClient.close();
                    } catch (Exception e) {
                        logger.debug("Failed to auto-create API key", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/offline.conf-error-api-key-creation");
                        return;
                    }
                }

                logger.debug("Initializing with hybrid API client (Public + Private)");
                UniFiProtectHybridClient apiClient = new UniFiProtectHybridClient(httpClient, gson, apiToken, scheduler,
                        config.hostname, config.port, config.username, config.password);

                this.apiClient = apiClient;

                connectEventWebSocket(apiClient);
                connectDeviceWebSocket(apiClient);
                logger.debug("Enabling Private API WebSocket for real-time updates");
                apiClient.getPrivateClient().enableWebSocket(update -> {
                    scheduler.execute(() -> {
                        logger.trace("Private API WebSocket update: action={}, model={}", update.action,
                                update.modelType);
                        routePrivateApiUpdate(update);
                    });
                }).whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.debug("Failed to enable Private API WebSocket", ex);
                        setOfflineAndReconnect();
                    }
                });
                updateNVRStatus();
            } catch (Exception e) {
                logger.debug("Initialization failed", e);
                setOfflineAndReconnect();
            }
        });
    }

    @Override
    public void dispose() {
        shuttingDown = true;
        stopTasks();
        stopApiClient();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Error stopping HTTP client", e);
        }
        super.dispose();
    }

    @Nullable
    public UniFiProtectHybridClient getApiClient() {
        return apiClient;
    }

    @Nullable
    public String getHostname() {
        final UnifiProtectNVRConfiguration config = getConfigAs(UnifiProtectNVRConfiguration.class);
        return config.hostname;
    }

    public void setDiscoveryService(UnifiProtectDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    private @Nullable <T extends ThingHandler> T findChildHandler(String deviceId, Class<T> handlerType) {
        for (Thing t : getThing().getThings()) {
            String devId = getDeviceId(t);
            if (devId != null && devId.equals(deviceId)) {
                ThingHandler handler = t.getHandler();
                if (handlerType.isInstance(handler)) {
                    return handlerType.cast(handler);
                }
            }
        }
        return null;
    }

    private @Nullable String getDeviceId(Thing thing) {
        Object devIdObj = thing.getConfiguration().get(UnifiProtectBindingConstants.DEVICE_ID);
        return devIdObj != null ? String.valueOf(devIdObj) : null;
    }

    private void setChildStatus(String deviceId, DeviceState state) {
        ThingStatus status = switch (state) {
            case CONNECTED -> ThingStatus.ONLINE;
            case CONNECTING -> ThingStatus.UNKNOWN;
            default -> ThingStatus.OFFLINE;
        };
        UnifiProtectAbstractDeviceHandler<?> handler = findChildHandler(deviceId,
                UnifiProtectAbstractDeviceHandler.class);
        if (handler != null && handler.getThing().getStatus() != status) {
            handler.updateStatus(status);
        }
    }

    private void refreshChildFromApi(String deviceId) {
        UnifiProtectAbstractDeviceHandler<?> handler = findChildHandler(deviceId,
                UnifiProtectAbstractDeviceHandler.class);
        if (handler != null) {
            refreshChildFromApi(deviceId, handler);
        }
    }

    private void refreshChildFromApi(String deviceId, UnifiProtectAbstractDeviceHandler<?> handler) {
        UniFiProtectHybridClient apiClient = this.apiClient;
        if (apiClient == null) {
            return;
        }
        try {
            Bootstrap bootstrap = apiClient.getPrivateClient().getBootstrap().get();
            if (handler instanceof UnifiProtectCameraHandler cameraHandler
                    && bootstrap.cameras.get(deviceId) instanceof Camera privCamera) {
                cameraHandler.refreshFromDevice(privCamera);
            } else if (handler instanceof UnifiProtectLightHandler lightHandler
                    && bootstrap.lights.get(deviceId) instanceof Light privLight) {
                lightHandler.refreshFromDevice(privLight);
            } else if (handler instanceof UnifiProtectSensorHandler sensorHandler
                    && bootstrap.sensors.get(deviceId) instanceof Sensor privSensor) {
                sensorHandler.refreshFromDevice(privSensor);
            } else if (handler instanceof UnifiProtectDoorlockHandler doorlockHandler
                    && bootstrap.doorlocks.get(deviceId) instanceof Doorlock privDoorlock) {
                doorlockHandler.refreshFromDevice(privDoorlock);
            } else if (handler instanceof UnifiProtectChimeHandler chimeHandler
                    && bootstrap.chimes.get(deviceId) instanceof Chime privChime) {
                chimeHandler.refreshFromDevice(privChime);
            }
            cancelChildRefreshRetry(deviceId);
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Failed to refresh child {} from API", deviceId, e);
            handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-refresh-failed");
            scheduleChildRefreshRetry(deviceId);
        }
    }

    private void scheduleChildRefreshRetry(String deviceId) {
        ScheduledFuture<?> existing = childRefreshRetryTasks.get(deviceId);
        if (existing != null) {
            existing.cancel(true);
        }
        ScheduledFuture<?> future = scheduler.schedule(() -> refreshChildFromApi(deviceId),
                CHILD_REFRESH_RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
        childRefreshRetryTasks.put(deviceId, future);
    }

    private void cancelChildRefreshRetry(String deviceId) {
        ScheduledFuture<?> existing = childRefreshRetryTasks.remove(deviceId);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    private void stopChildRefreshRetryTasks() {
        for (ScheduledFuture<?> f : childRefreshRetryTasks.values()) {
            f.cancel(true);
        }
        childRefreshRetryTasks.clear();
    }

    private void markChildGone(String deviceId) {
        UnifiProtectAbstractDeviceHandler<?> handler = findChildHandler(deviceId,
                UnifiProtectAbstractDeviceHandler.class);
        if (handler != null) {
            handler.markGone();
        }
    }

    private void syncDevices() {
        UniFiProtectHybridClient apiClient = this.apiClient;
        if (apiClient == null) {
            return;
        }
        try {
            UnifiProtectDiscoveryService discoveryService = Objects.requireNonNull(this.discoveryService,
                    "Discovery service not set");
            Bootstrap bootstrap = apiClient.getPrivateClient().getBootstrap().get();

            // NVR version (previously from public API getMetaInfo)
            if (bootstrap.nvr != null && bootstrap.nvr.version != null) {
                updateProperty(UnifiProtectBindingConstants.PROPERTY_APPLICATION_VERSION, bootstrap.nvr.version);
            }

            // Sync cameras from Bootstrap
            bootstrap.cameras.forEach((id, privCamera) -> {
                UnifiProtectCameraHandler ch = findChildHandler(id, UnifiProtectCameraHandler.class);
                if (ch != null) {
                    ch.refreshFromDevice(privCamera);
                } else {
                    discoveryService.discoverDevice(id, privCamera.name, privCamera.type,
                            UnifiProtectBindingConstants.THING_TYPE_CAMERA, "Camera");
                }
            });
            // Sync lights from Bootstrap
            bootstrap.lights.forEach((id, privLight) -> {
                UnifiProtectLightHandler lh = findChildHandler(id, UnifiProtectLightHandler.class);
                if (lh != null) {
                    lh.refreshFromDevice(privLight);
                } else {
                    discoveryService.discoverDevice(id, privLight.name, privLight.type,
                            UnifiProtectBindingConstants.THING_TYPE_LIGHT, "Light");
                }
            });
            // Sync sensors from Bootstrap
            bootstrap.sensors.forEach((id, privSensor) -> {
                UnifiProtectSensorHandler sh = findChildHandler(id, UnifiProtectSensorHandler.class);
                if (sh != null) {
                    sh.refreshFromDevice(privSensor);
                } else {
                    discoveryService.discoverDevice(id, privSensor.name, privSensor.type,
                            UnifiProtectBindingConstants.THING_TYPE_SENSOR, "Sensor");
                }
            });
            // Sync doorlocks from Bootstrap
            bootstrap.doorlocks.forEach((id, privDoorlock) -> {
                UnifiProtectDoorlockHandler dlh = findChildHandler(id, UnifiProtectDoorlockHandler.class);
                if (dlh != null) {
                    dlh.refreshFromDevice(privDoorlock);
                }
            });
            // Sync chimes from Bootstrap
            bootstrap.chimes.forEach((id, privChime) -> {
                UnifiProtectChimeHandler ch = findChildHandler(id, UnifiProtectChimeHandler.class);
                if (ch != null) {
                    ch.refreshFromDevice(privChime);
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Initial sync failed", e);
        }
    }

    private synchronized void setOfflineAndReconnect() {
        ScheduledFuture<?> reconnectTask = this.reconnectTask;
        if (shuttingDown || (reconnectTask != null && !reconnectTask.isDone())) {
            return;
        }
        updateStatus(ThingStatus.OFFLINE);
        stopApiClient();
        stopTasks();
        this.reconnectTask = scheduler.schedule(this::initialize, 5, TimeUnit.SECONDS);
    }

    private void connectEventWebSocket(UniFiProtectHybridClient apiClient)
            throws InterruptedException, ExecutionException {
        for (int attempt = 1; attempt <= WS_CONNECT_MAX_RETRIES; attempt++) {
            try {
                apiClient.getPublicClient().subscribeEvents(add -> {
                    routePublicApiEvent(add.item, WSEventType.ADD);
                }, update -> {
                    handleUpdateEvent(update.item);
                }, () -> {
                    updateStatus(ThingStatus.ONLINE);
                    scheduler.execute(() -> syncDevices());
                }, (code, reason) -> {
                    logger.debug("Event WS closed: {} {}", code, reason);
                    setOfflineAndReconnect();
                }, err -> logger.debug("Event WS error", err)).get();
                return;
            } catch (ExecutionException e) {
                if (attempt < WS_CONNECT_MAX_RETRIES) {
                    logger.debug("Event WebSocket connect attempt {} failed, retrying in {}ms", attempt,
                            WS_CONNECT_RETRY_DELAY_MS, e);
                    Thread.sleep(WS_CONNECT_RETRY_DELAY_MS);
                } else {
                    throw e;
                }
            }
        }
    }

    private void connectDeviceWebSocket(UniFiProtectHybridClient apiClient)
            throws InterruptedException, ExecutionException {
        for (int attempt = 1; attempt <= WS_CONNECT_MAX_RETRIES; attempt++) {
            try {
                apiClient.getPublicClient().subscribeDevices(add -> {
                    UnifiProtectDiscoveryService discoveryService = this.discoveryService;
                    if (discoveryService == null) {
                        logger.debug("Discovery service not set");
                        return;
                    }
                    switch (add.item.modelKey) {
                        case CAMERA:
                            discoveryService.discoverDevice(add.item.id, add.item.name, null,
                                    UnifiProtectBindingConstants.THING_TYPE_CAMERA, "Camera");
                            break;
                        case LIGHT:
                            discoveryService.discoverDevice(add.item.id, add.item.name, null,
                                    UnifiProtectBindingConstants.THING_TYPE_LIGHT, "Light");
                            break;
                        case SENSOR:
                            discoveryService.discoverDevice(add.item.id, add.item.name, null,
                                    UnifiProtectBindingConstants.THING_TYPE_SENSOR, "Sensor");
                            break;
                        default:
                            // ignore
                    }
                }, update -> {
                    scheduler.execute(() -> {
                        if (update.item == null || update.item.id == null) {
                            return;
                        }
                        DeviceState state = update.item.state;
                        if (state != null) {
                            setChildStatus(update.item.id, state);
                        }
                    });
                }, remove -> {
                    scheduler.execute(() -> {
                        if (remove.item == null || remove.item.id == null) {
                            return;
                        }
                        markChildGone(remove.item.id);
                    });
                }, () -> {
                    // ignore on-open
                }, (code, reason) -> {
                    logger.debug("Device WS closed: {} {}", code, reason);
                    setOfflineAndReconnect();
                }, err -> logger.debug("Device WS error", err)).get();
                return;
            } catch (ExecutionException e) {
                if (attempt < WS_CONNECT_MAX_RETRIES) {
                    logger.debug("Device WebSocket connect attempt {} failed, retrying in {}ms", attempt,
                            WS_CONNECT_RETRY_DELAY_MS, e);
                    Thread.sleep(WS_CONNECT_RETRY_DELAY_MS);
                } else {
                    throw e;
                }
            }
        }
    }

    private void routePublicApiEvent(BaseEvent event, WSEventType eventType) {
        if (event.device == null) {
            return;
        }
        String deviceId = event.device;
        EventType et = event.type;
        switch (et) {
            case CAMERA_MOTION:
            case SMART_AUDIO_DETECT:
            case SMART_DETECT_ZONE:
            case SMART_DETECT_LINE:
            case SMART_DETECT_LOITER_ZONE:
            case RING: {
                UnifiProtectCameraHandler ch = findChildHandler(deviceId, UnifiProtectCameraHandler.class);
                if (ch != null) {
                    ch.handleEvent(event, eventType);
                }
                break;
            }
            case LIGHT_MOTION: {
                UnifiProtectLightHandler lh = findChildHandler(deviceId, UnifiProtectLightHandler.class);
                if (lh != null) {
                    lh.handleEvent(event, eventType);
                }
                break;
            }
            case SENSOR_MOTION:
            case SENSOR_OPENED:
            case SENSOR_CLOSED:
            case SENSOR_ALARM:
            case SENSOR_BATTERY_LOW:
            case SENSOR_TAMPER:
            case SENSOR_WATER_LEAK:
            case SENSOR_EXTREME_VALUES: {
                UnifiProtectSensorHandler sh = findChildHandler(deviceId, UnifiProtectSensorHandler.class);
                if (sh != null) {
                    sh.handleEvent(event, eventType);
                }
                break;
            }
            default:
                break;
        }
    }

    private void routePrivateApiUpdate(WebSocketUpdate update) {
        if (update.data == null) {
            return;
        }

        try {
            // Parse the data JsonObject into the appropriate device type and update the handler
            Gson gson = JsonUtil.getGson();

            // Handle NVR updates (NVR doesn't have a device ID like other devices)
            if (update.modelType == ModelType.NVR) {
                Nvr nvr = gson.fromJson(update.data, Nvr.class);

                if (nvr != null) {
                    logger.trace("Private API NVR real-time update (action: {})", update.action);
                    // Update NVR channels with the data from WebSocket
                    updateNVRChannels(nvr);
                }
                return; // NVR updates are handled, no need to continue
            }

            // For device updates, we need an ID
            if (update.id == null) {
                return;
            }

            // Route to appropriate handler based on model type
            String deviceId = update.id;
            switch (update.modelType) {
                case CAMERA:
                    UnifiProtectCameraHandler ch = findChildHandler(deviceId, UnifiProtectCameraHandler.class);
                    if (ch != null) {
                        Camera camera = gson.fromJson(update.data, Camera.class);
                        if (camera != null) {
                            logger.trace("Private API camera real-time update for device {} (action: {})", deviceId,
                                    update.action);
                            ch.updateFromPrivateDevice(camera);
                        }
                    }
                    break;
                case DOORLOCK:
                    UnifiProtectDoorlockHandler dlh = findChildHandler(deviceId, UnifiProtectDoorlockHandler.class);
                    if (dlh != null) {
                        Doorlock doorlock = gson.fromJson(update.data, Doorlock.class);
                        if (doorlock != null) {
                            logger.trace("Private API doorlock real-time update for device {} (action: {})", deviceId,
                                    update.action);
                            dlh.updateDoorlockChannels(doorlock);
                        }
                    }
                    break;
                case CHIME:
                    UnifiProtectChimeHandler chimeHandler = findChildHandler(deviceId, UnifiProtectChimeHandler.class);
                    if (chimeHandler != null) {
                        Chime chime = gson.fromJson(update.data, Chime.class);
                        if (chime != null) {
                            logger.trace("Private API chime real-time update for device {} (action: {})", deviceId,
                                    update.action);
                            chimeHandler.updateChimeChannels(chime);
                        }
                    }
                    break;
                case LIGHT:
                    UnifiProtectLightHandler lightHandler = findChildHandler(deviceId, UnifiProtectLightHandler.class);
                    if (lightHandler != null) {
                        Light light = gson.fromJson(update.data, Light.class);
                        if (light != null) {
                            logger.trace("Private API light real-time update for device {} (action: {})", deviceId,
                                    update.action);
                            lightHandler.updateLightChannels(light);
                        }
                    }
                    break;
                case SENSOR:
                    UnifiProtectSensorHandler sensorHandler = findChildHandler(deviceId,
                            UnifiProtectSensorHandler.class);
                    if (sensorHandler != null) {
                        Sensor sensor = gson.fromJson(update.data, Sensor.class);
                        if (sensor != null) {
                            logger.trace("Private API sensor real-time update for device {} (action: {})", deviceId,
                                    update.action);
                            sensorHandler.refreshFromDevice(sensor);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.debug("Error processing Private API WebSocket update for device {}", update.id, e);
        }
    }

    private void stopApiClient() {
        UniFiProtectHybridClient apiClient = this.apiClient;
        if (apiClient != null) {
            try {
                apiClient.close();
            } catch (IOException e) {
                logger.debug("Error closing API client", e);
            }
            this.apiClient = null;
        }
    }

    private void stopTasks() {
        stopPollTask();
        stopReconnectTask();
        stopPendingUpdateTasks();
        stopChildRefreshRetryTasks();
    }

    private void stopPollTask() {
        ScheduledFuture<?> pollTask = this.pollTask;
        if (pollTask != null) {
            pollTask.cancel(true);
            this.pollTask = null;
        }
    }

    private void stopReconnectTask() {
        ScheduledFuture<?> reconnectTask = this.reconnectTask;
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
            this.reconnectTask = null;
        }
    }

    private synchronized void stopPendingUpdateTasks() {
        for (Map.Entry<String, PendingUpdate> e : pendingEventUpdates.entrySet()) {
            PendingUpdate pu = e.getValue();
            ScheduledFuture<?> f1 = pu.debounceFuture;
            if (f1 != null) {
                f1.cancel(true);
            }
            ScheduledFuture<?> f2 = pu.maxFuture;
            if (f2 != null) {
                f2.cancel(true);
            }
        }
        pendingEventUpdates.clear();
    }

    private synchronized void handleUpdateEvent(@Nullable BaseEvent event) {
        if (event == null || event.id == null) {
            return;
        }
        final String eventId = event.id;
        PendingUpdate state = Objects
                .requireNonNull(pendingEventUpdates.computeIfAbsent(eventId, k -> new PendingUpdate()));
        // Schedule max wait once per burst (only if not already scheduled)
        if (state.maxFuture == null) {
            final PendingUpdate stateFinalForMax = state;
            state.maxFuture = scheduler.schedule(() -> deliverDebouncedUpdate(eventId, stateFinalForMax),
                    WS_UPDATE_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        }

        // Update the latest event
        state.lastEvent = event;

        // Reschedule the inactivity debounce timer
        ScheduledFuture<?> existing = state.debounceFuture;
        if (existing != null) {
            existing.cancel(false);
        }
        final PendingUpdate stateFinal = state;
        state.debounceFuture = scheduler.schedule(() -> deliverDebouncedUpdate(eventId, stateFinal),
                WS_UPDATE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
    }

    private synchronized void deliverDebouncedUpdate(String eventId, PendingUpdate state) {
        // Guard against races if another task already delivered and cleared state
        PendingUpdate current = pendingEventUpdates.get(eventId);
        if (!state.equals(current)) {
            return;
        }
        // Cancel all update timers
        ScheduledFuture<?> f1 = state.debounceFuture;
        if (f1 != null) {
            f1.cancel(false);
            state.debounceFuture = null;
        }
        ScheduledFuture<?> f2 = state.maxFuture;
        if (f2 != null) {
            f2.cancel(false);
            state.maxFuture = null;
        }
        BaseEvent last = state.lastEvent;
        pendingEventUpdates.remove(eventId);
        if (last != null) {
            routePublicApiEvent(last, WSEventType.UPDATE);
        }
    }

    /**
     * Fetch and update NVR status channels from Private API
     */
    private void updateNVRStatus() {
        UniFiProtectHybridClient client = apiClient;
        if (client == null) {
            return;
        }

        try {
            // Fetch NVR data from Private API Bootstrap
            client.getPrivateClient().getBootstrap().thenAccept(bootstrap -> {
                if (bootstrap.nvr != null) {
                    scheduler.execute(() -> {
                        updateNVRChannels(bootstrap.nvr);
                    });
                }
            }).exceptionally(ex -> {
                logger.debug("Failed to fetch NVR status from Private API", ex);
                return null;
            });
        } catch (Exception e) {
            logger.debug("Error updating NVR status", e);
        }
    }

    /**
     * Update NVR channels from Private API NVR data
     */
    private void updateNVRChannels(Nvr nvr) {
        // Storage Monitoring
        if (nvr.storageStats != null) {
            if (nvr.storageStats.recordingSpace != null) {
                if (nvr.storageStats.recordingSpace.total != null) {
                    updateState(UnifiProtectBindingConstants.CHANNEL_STORAGE_TOTAL,
                            new DecimalType(nvr.storageStats.recordingSpace.total));
                }
                if (nvr.storageStats.recordingSpace.used != null) {
                    updateState(UnifiProtectBindingConstants.CHANNEL_STORAGE_USED,
                            new DecimalType(nvr.storageStats.recordingSpace.used));
                }
                if (nvr.storageStats.recordingSpace.available != null) {
                    updateState(UnifiProtectBindingConstants.CHANNEL_STORAGE_AVAILABLE,
                            new DecimalType(nvr.storageStats.recordingSpace.available));
                }
            }
            if (nvr.storageStats.utilization != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_STORAGE_UTILIZATION,
                        new QuantityType<>(nvr.storageStats.utilization, Units.PERCENT));
            }
        }

        // Storage Device Health (from systemInfo.storage.devices)
        if (nvr.systemInfo != null && nvr.systemInfo.storage != null && nvr.systemInfo.storage.devices != null
                && !nvr.systemInfo.storage.devices.isEmpty()) {
            boolean allHealthy = nvr.systemInfo.storage.devices.stream()
                    .allMatch(d -> d.healthy != null && "health".equalsIgnoreCase(d.healthy));
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_STORAGE_DEVICE_HEALTHY, OnOffType.from(allHealthy));
        }

        // Camera Capacity
        if (nvr.cameraUtilization != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_CAMERA_UTILIZATION,
                    new DecimalType(nvr.cameraUtilization));
        }
        if (nvr.maxCameraCapacity != null && !nvr.maxCameraCapacity.isEmpty()) {
            // Format capacity as a string (e.g., "4K: 10, 2K: 20, HD: 40")
            StringBuilder capacity = new StringBuilder();
            nvr.maxCameraCapacity.forEach((key, value) -> {
                if (capacity.length() > 0) {
                    capacity.append(", ");
                }
                capacity.append(key).append(": ").append(value);
            });
            updateProperty(UnifiProtectBindingConstants.PROPERTY_CAMERA_CAPACITY_MAX, capacity.toString());
        }

        // Software Versions (Properties)
        if (nvr.version != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_PROTECT_VERSION, nvr.version);
        }
        if (nvr.ucoreVersion != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_UCORE_VERSION, nvr.ucoreVersion);
        }
        if (nvr.uiVersion != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_UI_VERSION, nvr.uiVersion);
        }

        // Network Information (Properties)
        if (nvr.publicIp != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_PUBLIC_IP, nvr.publicIp);
        }
        if (nvr.wanIp != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_WAN_IP, nvr.wanIp);
        }

        // Recording Settings
        if (nvr.globalCameraSettings != null && nvr.globalCameraSettings.recordingMode != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_RECORDING_MODE,
                    new StringType(nvr.globalCameraSettings.recordingMode));
        }
        if (nvr.isRecordingDisabled != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_RECORDING_DISABLED,
                    OnOffType.from(nvr.isRecordingDisabled));
        }
        if (nvr.isRecordingMotionOnly != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_RECORDING_MOTION_ONLY,
                    OnOffType.from(nvr.isRecordingMotionOnly));
        }
        if (nvr.recordingRetentionDurationMs != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_RECORDING_RETENTION,
                    new QuantityType<>(nvr.recordingRetentionDurationMs, MetricPrefix.MILLI(Units.SECOND)));
        }

        // Away Mode
        if (nvr.isAway != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_IS_AWAY, OnOffType.from(nvr.isAway));
        }
        if (nvr.locationSettings != null && nvr.locationSettings.isGeofencingEnabled != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_GEOFENCING_ENABLED,
                    OnOffType.from(nvr.locationSettings.isGeofencingEnabled));
        }

        // Feature Flags
        if (nvr.smartDetection != null && nvr.smartDetection.enable != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_SMART_DETECTION_AVAILABLE,
                    OnOffType.from(nvr.smartDetection.enable));
        }
        if (nvr.isInsightsEnabled != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_INSIGHTS_ENABLED,
                    OnOffType.from(nvr.isInsightsEnabled));
        }

        if (nvr.hostShortname != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_HARDWARE_PLATFORM, nvr.hostShortname);
        }
        if (nvr.marketName != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MARKET_NAME, nvr.marketName);
        }
        if (nvr.hardwareRevision != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_IS_HARDWARE,
                    String.valueOf(!nvr.hardwareRevision.isEmpty()));
        }
        if (nvr.name != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_NAME, nvr.name);
        }
        if (nvr.hosts != null && !nvr.hosts.isEmpty()) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_HOST, nvr.hosts.get(0));
        }

        if (nvr.canAutoUpdate != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_CAN_AUTO_UPDATE, OnOffType.from(nvr.canAutoUpdate));
        }
        if (nvr.lastUpdateAt != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_LAST_UPDATE_AT,
                    new DateTimeType(ZonedDateTime.ofInstant(nvr.lastUpdateAt, ZoneId.systemDefault())));
        }
        if (nvr.isProtectUpdatable != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_NVR_PROTECT_UPDATABLE,
                    OnOffType.from(nvr.isProtectUpdatable));
        }
    }
}
