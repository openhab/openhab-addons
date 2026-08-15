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
package org.openhab.binding.unifi.internal.protect.handler;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.internal.api.UniFiApiKeyManager;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiSession;
import org.openhab.binding.unifi.internal.handler.UniFiControllerBridgeHandler;
import org.openhab.binding.unifi.internal.protect.UnifiProtectBindingConstants;
import org.openhab.binding.unifi.internal.protect.UnifiProtectDiscoveryService;
import org.openhab.binding.unifi.internal.protect.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifi.internal.protect.api.priv.client.UniFiProtectPrivateWebSocket.WebSocketUpdate;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Camera;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Chime;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Light;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Sensor;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Bootstrap;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Event;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Nvr;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.types.ModelType;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.types.SmartDetectObjectType;
import org.openhab.binding.unifi.internal.protect.api.priv.exception.AuthenticationException;
import org.openhab.binding.unifi.internal.protect.api.priv.exception.ThrottledException;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.DeviceState;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.ObjectType;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraMotionEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectLineEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectLoiterEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.CameraSmartDetectZoneEvent;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.events.EventType;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.gson.DeviceTypeAdapterFactory;
import org.openhab.binding.unifi.internal.protect.api.pub.dto.gson.EventTypeAdapterFactory;
import org.openhab.binding.unifi.internal.protect.config.UnifiProtectNVRConfiguration;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectAbstractDeviceHandler.WSEventType;
import org.openhab.binding.unifi.internal.protect.util.StaticChannelHelper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Bridge handler for the UniFi Protect NVR.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectNVRHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(UnifiProtectNVRHandler.class);
    private final ThingTypeRegistry thingTypeRegistry;
    private volatile @Nullable UniFiProtectHybridClient apiClient;
    private @Nullable ScheduledFuture<?> pollTask;
    private @Nullable ScheduledFuture<?> reconnectTask;
    private @Nullable UnifiProtectDiscoveryService discoveryService;
    private final Gson gson;
    private volatile boolean shuttingDown = false;
    private boolean autoManageToken = true;
    private boolean tokenAutoManaged = false;

    private static final long WS_UPDATE_DEBOUNCE_MS = 500;
    private static final long WS_UPDATE_MAX_WAIT_MS = 2000;
    private static final long CHILD_REFRESH_RETRY_DELAY_SECONDS = 10;
    private static final int WS_CONNECT_MAX_RETRIES = 3;
    private static final long WS_CONNECT_RETRY_DELAY_MS = 2000;
    private static final int MAX_RECONNECT_DELAY_SECONDS = 300;
    private static final int THROTTLED_INITIAL_DELAY_SECONDS = 60;
    private static final int THROTTLED_MAX_DELAY_SECONDS = 1800;

    private int reconnectAttempt = 0;
    private int throttledReconnectAttempt = 0;
    final Map<String, PendingUpdate> pendingEventUpdates = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> childRefreshRetryTasks = new ConcurrentHashMap<>();
    // Events can arrive on both WebSockets. Only ADDs are de-duplicated; UPDATEs pass through.
    private final Map<String, Long> dispatchedEventKeys = new ConcurrentHashMap<>();
    private static final long EVENT_DEDUP_TTL_MS = 120_000;
    // Last full payload per private event id, so incremental UPDATE deltas can be merged into it.
    private final Map<String, TimestampedPayload> privateEventPayloads = new ConcurrentHashMap<>();
    private static final long EVENT_PAYLOAD_TTL_MS = 600_000;

    private record TimestampedPayload(JsonObject payload, long timestamp) {
    }

    // Already-fetched ids, so a merged payload carrying them does not re-fetch the same image.
    private final Map<String, TimestampedMedia> fetchedEventMedia = new ConcurrentHashMap<>();

    private record TimestampedMedia(String ids, long timestamp) {
    }

    // Stamped on the WebSocket thread, so a superseded snapshot can be skipped on dispatch.
    private final AtomicLong eventSequence = new AtomicLong();
    // Single thread so an event's ADD is always processed before the UPDATEs that follow it.
    private volatile @Nullable ExecutorService privateEventExecutor;
    // Survives PendingUpdate, which is discarded on delivery, so a straggler cannot look new.
    private final Map<String, TimestampedSequence> deliveredEventSequences = new ConcurrentHashMap<>();
    private static final long EVENT_SEQUENCE_TTL_MS = 600_000;

    private record TimestampedSequence(long sequence, long timestamp) {
    }

    static final class PendingUpdate {
        long lastSequence = Long.MIN_VALUE;
        @Nullable
        BaseEvent lastEvent;
        @Nullable
        ScheduledFuture<?> debounceFuture;
        @Nullable
        ScheduledFuture<?> maxFuture;
    }

    public UnifiProtectNVRHandler(Thing thing, ThingTypeRegistry thingTypeRegistry) {
        super((Bridge) thing);
        this.thingTypeRegistry = thingTypeRegistry;
        gson = new GsonBuilder().registerTypeAdapterFactory(new DeviceTypeAdapterFactory())
                .registerTypeAdapterFactory(new EventTypeAdapterFactory()).create();
    }

    /**
     * Returns the parent {@code unifi:controller} bridge handler, or {@code null} if this NVR thing has no
     * parent configured yet (e.g. an orphaned legacy {@code unifiprotect:nvr} thing that has not been
     * reparented after upgrade).
     */
    private @Nullable UniFiControllerBridgeHandler getParentHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof UniFiControllerBridgeHandler ucbh) {
            return ucbh;
        }
        return null;
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
        privateEventExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "OH-unifi-protect-events"));
        ensureStaticChannels();

        UniFiControllerBridgeHandler parentHandler = getParentHandler();
        if (parentHandler == null) {
            Bridge parentBridge = getBridge();
            if (parentBridge != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                Object storedHost = getThing().getConfiguration().get("hostname");
                String hint = storedHost != null ? " (previously configured host: " + storedHost + ")" : "";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.requires-unifi-controller-bridge" + hint);
            }
            return;
        }

        final UnifiProtectNVRConfiguration config = getConfigAs(UnifiProtectNVRConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "@text/offline.initializing");

        final HttpClient httpClient = parentHandler.getHttpClient();
        final String host = parentHandler.getHost();
        final int port = parentHandler.getPort();

        parentHandler.getSessionAsync().whenComplete((session, sessionError) -> {
            if (sessionError != null) {
                logger.debug("Parent bridge session not available", sessionError);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, sessionError.getMessage());
                return;
            }
            scheduler.execute(() -> initializeWithSession(config, httpClient, host, port, session));
        });
    }

    /**
     * Adds any statically-defined channels missing from a stored NVR thing (e.g. a thing migrated
     * during the unify merge that was persisted before its thing-type was available).
     */
    private void ensureStaticChannels() {
        List<Channel> missing = StaticChannelHelper.addMissingChannels(getCallback(), thingTypeRegistry, getThing(),
                logger);
        if (!missing.isEmpty()) {
            List<Channel> channels = new ArrayList<>(getThing().getChannels());
            channels.addAll(missing);
            updateThing(editThing().withChannels(channels).build());
        }
    }

    private void initializeWithSession(UnifiProtectNVRConfiguration config, HttpClient httpClient, String host,
            int port, UniFiSession session) {
        try {
            String apiToken = config.token;
            autoManageToken = config.autoManageToken;
            tokenAutoManaged = config.tokenAutoManaged;

            if (apiToken == null || apiToken.isBlank()) {
                if (!autoManageToken) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "API token required when auto-manage is disabled");
                    return;
                }
                logger.debug("No API token provided, auto-creating via parent API key manager...");
                try {
                    UniFiControllerBridgeHandler parentHandler = getParentHandler();
                    UniFiApiKeyManager keyManager = parentHandler != null ? parentHandler.getApiKeyManager() : null;
                    if (keyManager == null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                "Parent bridge API key manager not available");
                        return;
                    }

                    String keyName = "openHAB-" + getThing().getUID().getId();
                    apiToken = keyManager.provisionApiToken(keyName);
                    logger.debug("Successfully created API key '{}': {}***", keyName,
                            apiToken.substring(0, Math.min(8, apiToken.length())));

                    Configuration thingConfig = editConfiguration();
                    thingConfig.put("token", apiToken);
                    thingConfig.put("tokenAutoManaged", true);
                    updateConfiguration(thingConfig);
                    tokenAutoManaged = true;
                } catch (Exception e) {
                    logger.debug("Failed to auto-create API key", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-api-key-creation");
                    return;
                }
            }

            logger.debug("Initializing with hybrid API client (Public + Private)");
            UniFiProtectHybridClient apiClient = new UniFiProtectHybridClient(httpClient, gson, apiToken, scheduler,
                    host, port, session);

            this.apiClient = apiClient;

            connectEventWebSocket(apiClient);
            connectDeviceWebSocket(apiClient);
            logger.debug("Enabling Private API WebSocket for real-time updates");
            apiClient.getPrivateClient().enableWebSocket(update -> {
                // Merged here, on the WebSocket thread, where frames are still in NVR order.
                long sequence = eventSequence.incrementAndGet();
                if (update.modelType == ModelType.EVENT) {
                    update.data = trackPrivateEventPayload(update.action, update.id, update.data);
                }
                ExecutorService eventExecutor = privateEventExecutor;
                Executor dispatcher = update.modelType == ModelType.EVENT && eventExecutor != null
                        && !eventExecutor.isShutdown() ? eventExecutor : scheduler;
                dispatcher.execute(() -> {
                    logger.trace("Private API WebSocket update: action={}, model={}", update.action, update.modelType);
                    routePrivateApiUpdate(update, sequence);
                });
            }).whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.debug("Failed to enable Private API WebSocket", ex);
                    handleInitFailure(ex);
                }
            });
            reconnectAttempt = 0;
            throttledReconnectAttempt = 0;
            updateNVRStatus();
        } catch (Exception e) {
            logger.debug("Initialization failed", e);
            handleInitFailure(e);
        }
    }

    private void handleInitFailure(Throwable e) {
        Throwable cause = e;
        Throwable inner = cause.getCause();
        while ((cause instanceof ExecutionException || cause instanceof CompletionException) && inner != null) {
            cause = inner;
            inner = cause.getCause();
        }
        if (cause instanceof AuthenticationException) {
            // Already survived an in-client re-auth; treat as a transient expired session and reconnect with a
            // status detail so the failure is visible rather than a bare OFFLINE.
            setOfflineAndReconnect(false, "@text/offline.comm-error-retrying");
        } else if (cause instanceof ThrottledException) {
            setOfflineAndReconnect(true, null);
        } else {
            // Public API 401 (bad token) arrives as IOException("HTTP 401: ...").
            if (cause instanceof IOException ioe) {
                String msg = ioe.getMessage();
                if (msg != null && (msg.startsWith("HTTP 401") || msg.startsWith("HTTP 403"))) {
                    attemptTokenRecovery();
                }
            }
            setOfflineAndReconnect(false, null);
        }
    }

    private void attemptTokenRecovery() {
        if (!autoManageToken || !tokenAutoManaged) {
            // Don't bulldoze a user-supplied token.
            return;
        }
        UniFiControllerBridgeHandler parentHandler = getParentHandler();
        UniFiApiKeyManager keyManager = parentHandler != null ? parentHandler.getApiKeyManager() : null;
        if (keyManager == null) {
            return;
        }
        try {
            String keyName = "openHAB-" + getThing().getUID().getId();
            String newToken = keyManager.provisionApiToken(keyName);
            Configuration thingConfig = editConfiguration();
            thingConfig.put("token", newToken);
            thingConfig.put("tokenAutoManaged", true);
            updateConfiguration(thingConfig);
            logger.debug("Re-provisioned API token after auth failure");
        } catch (UniFiException ex) {
            logger.debug("Failed to re-provision API token", ex);
        }
    }

    @Override
    public void dispose() {
        shuttingDown = true;
        stopTasks();
        stopApiClient();
        ExecutorService eventExecutor = privateEventExecutor;
        if (eventExecutor != null) {
            eventExecutor.shutdownNow();
            privateEventExecutor = null;
        }
        super.dispose();
    }

    @Nullable
    public UniFiProtectHybridClient getApiClient() {
        return apiClient;
    }

    @Nullable
    public String getHostname() {
        UniFiControllerBridgeHandler parentHandler = getParentHandler();
        return parentHandler != null ? parentHandler.getHost() : null;
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

    private synchronized void setOfflineAndReconnect(boolean throttled, @Nullable String message) {
        ScheduledFuture<?> existing = this.reconnectTask;
        if (shuttingDown) {
            return;
        }
        if (existing != null && !existing.isDone()) {
            // Throttled supersedes any pending fast reconnect.
            if (throttled) {
                existing.cancel(false);
            } else {
                return;
            }
        }
        stopApiClient();
        stopTasks();

        int delay;
        if (throttled) {
            delay = Math.min((int) Math.pow(2, throttledReconnectAttempt) * THROTTLED_INITIAL_DELAY_SECONDS,
                    THROTTLED_MAX_DELAY_SECONDS);
            throttledReconnectAttempt++;
            logger.debug("Scheduling reconnect in {} seconds (throttled, attempt {})", delay,
                    throttledReconnectAttempt);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.login-throttled");
        } else {
            delay = Math.min((int) Math.pow(2, reconnectAttempt) * 5, MAX_RECONNECT_DELAY_SECONDS);
            reconnectAttempt++;
            logger.debug("Scheduling reconnect in {} seconds (attempt {})", delay, reconnectAttempt);
            if (message != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }

        this.reconnectTask = scheduler.schedule(this::initialize, delay, TimeUnit.SECONDS);
    }

    private void connectEventWebSocket(UniFiProtectHybridClient apiClient)
            throws InterruptedException, ExecutionException {
        for (int attempt = 1; attempt <= WS_CONNECT_MAX_RETRIES; attempt++) {
            try {
                apiClient.getPublicClient().subscribeEvents(add -> {
                    logger.debug("Public events WS event add, id={}, type={}", add.item.id, add.item.type);
                    routePublicApiEvent(add.item, WSEventType.ADD);
                }, update -> {
                    handleUpdateEvent(update.item);
                }, () -> {
                    updateStatus(ThingStatus.ONLINE);
                    scheduler.execute(() -> syncDevices());
                }, (code, reason) -> {
                    logger.debug("Event WS closed: {} {}", code, reason);
                    setOfflineAndReconnect(false, null);
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
                    setOfflineAndReconnect(false, null);
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
        // ADD can arrive on both the public integration WS and fallback private updates WS,
        // so de-dupe it. UPDATEs repeat over an event's lifetime and are debounced by
        // handleUpdateEvent; de-duping them would drop public-WS sensor/ring updates for the dedup TTL.
        String dedupId = event.id;
        if (eventType == WSEventType.ADD && dedupId != null && !markEventDispatched(dedupId)) {
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

    private boolean markEventDispatched(String key) {
        long now = System.currentTimeMillis();
        dispatchedEventKeys.values().removeIf(ts -> now - ts > EVENT_DEDUP_TTL_MS);
        return dispatchedEventKeys.putIfAbsent(key, now) == null;
    }

    /**
     * Whether this frame carries a thumbnail or heatmap id not already fetched for this event.
     *
     * The NVR generates those asynchronously and announces each once, but the merged payload keeps
     * them from then on, so the id has to be compared against what was last seen rather than merely
     * being present. Returns false when neither id is set.
     */
    private boolean isNewEventMedia(@Nullable String eventId, @Nullable String thumbnailId,
            @Nullable String heatmapId) {
        if (eventId == null || (thumbnailId == null && heatmapId == null)) {
            return false;
        }
        String seen = thumbnailId + "|" + heatmapId;
        long now = System.currentTimeMillis();
        fetchedEventMedia.values().removeIf(p -> now - p.timestamp() > EVENT_PAYLOAD_TTL_MS);
        TimestampedMedia previous = fetchedEventMedia.put(eventId, new TimestampedMedia(seen, now));
        return previous == null || !seen.equals(previous.ids());
    }

    /**
     * Keep the last full payload per event id and merge incremental UPDATE frames into it.
     *
     * Protect's private updates WebSocket sends an "add" with the complete event and subsequent
     * "update" frames containing only the changed fields. Converting such a delta on its own fails,
     * because the mapping needs the type and camera that only the "add" carried, which would drop
     * the *_UPDATE dispatch and stop the contact latch from being refreshed.
     *
     * @return the full payload to work with. An update for an event whose add was never seen -- one
     *         that started before openHAB did, say -- yields the delta unchanged; it cannot be
     *         converted into a camera event without the type and camera the add carried, so it is
     *         effectively dropped. Only a {@code null} input gives {@code null} back.
     */
    @Nullable
    JsonObject trackPrivateEventPayload(@Nullable String action, @Nullable String id, @Nullable JsonObject data) {
        if (id == null || data == null) {
            return data;
        }
        long now = System.currentTimeMillis();
        privateEventPayloads.values().removeIf(p -> now - p.timestamp > EVENT_PAYLOAD_TTL_MS);

        if ("remove".equals(action)) {
            privateEventPayloads.remove(id);
            return data;
        }
        if ("add".equals(action)) {
            privateEventPayloads.put(id, new TimestampedPayload(data.deepCopy(), now));
            return data;
        }
        if (!"update".equals(action)) {
            return data;
        }
        // One atomic step, so overlapping calls cannot merge from the same snapshot.
        TimestampedPayload result = privateEventPayloads.computeIfPresent(id, (key, cached) -> {
            JsonObject merged = cached.payload().deepCopy();
            data.entrySet().forEach(e -> merged.add(e.getKey(), e.getValue()));
            return new TimestampedPayload(merged, now);
        });
        if (result == null) {
            return data;
        }
        return result.payload().deepCopy();
    }

    /**
     * Convert a private-API {@link Event} into the matching public camera event so the
     * standard {@link #routePublicApiEvent} dispatch (channels + contacts) can be reused.
     * Returns {@code null} for events that are not camera motion / smart-detect zone, line or
     * loiter zone (audio and ring stay on the public integration path only).
     */
    static @Nullable BaseEvent toPublicCameraEvent(Event event) {
        var type = event.type;
        String cameraId = event.cameraId;
        if (type == null || cameraId == null) {
            return null;
        }
        BaseEvent pub;
        switch (type) {
            case MOTION:
                pub = new CameraMotionEvent();
                pub.type = EventType.CAMERA_MOTION;
                break;
            case SMART_DETECT:
                CameraSmartDetectZoneEvent zone = new CameraSmartDetectZoneEvent();
                zone.smartDetectTypes = toObjectTypes(event.smartDetectTypes);
                pub = zone;
                pub.type = EventType.SMART_DETECT_ZONE;
                break;
            case SMART_DETECT_LINE:
                CameraSmartDetectLineEvent line = new CameraSmartDetectLineEvent();
                line.smartDetectTypes = toObjectTypes(event.smartDetectTypes);
                pub = line;
                pub.type = EventType.SMART_DETECT_LINE;
                break;
            case SMART_DETECT_LOITER_ZONE:
                CameraSmartDetectLoiterEvent loiter = new CameraSmartDetectLoiterEvent();
                loiter.smartDetectTypes = toObjectTypes(event.smartDetectTypes);
                pub = loiter;
                pub.type = EventType.SMART_DETECT_LOITER_ZONE;
                break;
            default:
                return null;
        }
        pub.device = cameraId;
        pub.start = event.start != null ? event.start.toEpochMilli() : null;
        pub.end = event.end != null ? event.end.toEpochMilli() : null;
        return pub;
    }

    private static List<ObjectType> toObjectTypes(@Nullable List<SmartDetectObjectType> types) {
        List<ObjectType> out = new ArrayList<>();
        if (types == null) {
            return out;
        }
        for (SmartDetectObjectType type : types) {
            if (type == null) {
                // Gson yields null for a value this enum does not know, so a smart-detect type
                // added by a later Protect release would otherwise take the whole event down here.
                continue;
            }
            try {
                out.add(ObjectType.valueOf(type.name()));
            } catch (IllegalArgumentException ignored) {
                // an audio object type in a smart-detect list — not a camera object type
            }
        }
        return out;
    }

    private void routePrivateApiUpdate(WebSocketUpdate update, long sequence) {
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
                case EVENT:
                    // update.data has already been merged with the last full payload for this event
                    // id, on the WebSocket thread -- see where the update handler is registered.
                    Event event = update.data == null ? null : gson.fromJson(update.data, Event.class);
                    if (event != null) {
                        logger.debug("Private updates WS event {}, id={}, type={}", update.action, update.id,
                                event.type);
                        BaseEvent pubEvent = toPublicCameraEvent(event);
                        if (pubEvent != null) {
                            pubEvent.id = update.id;
                            // "remove" must not fire a detection.
                            if ("add".equals(update.action)) {
                                routePublicApiEvent(pubEvent, WSEventType.ADD);
                            } else if ("update".equals(update.action)) {
                                handleUpdateEvent(pubEvent, sequence);
                            }
                        }
                        // Merging makes the ids sticky, so only act on an id not already fetched.
                        if ("update".equals(update.action) && event.cameraId != null
                                && isNewEventMedia(update.id, event.thumbnailId, event.heatmapId)) {
                            UnifiProtectCameraHandler camHandler = findChildHandler(event.cameraId,
                                    UnifiProtectCameraHandler.class);
                            if (camHandler != null) {
                                logger.trace(
                                        "Private API event update with thumbnail/heatmap for camera {} (event: {})",
                                        event.cameraId, update.id);
                                camHandler.handleEventUpdate(event);
                            }
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

    private void handleUpdateEvent(@Nullable BaseEvent event) {
        handleUpdateEvent(event, eventSequence.incrementAndGet());
    }

    /**
     * @param sequence arrival order of this snapshot, taken on the WebSocket thread. Dispatch tasks
     *            are submitted independently to the shared multi-threaded scheduler, so without it
     *            a task carrying an older snapshot could run last and overwrite a newer one.
     */
    synchronized void handleUpdateEvent(@Nullable BaseEvent event, long sequence) {
        if (event == null || event.id == null) {
            return;
        }
        final String eventId = event.id;
        // PendingUpdate is gone after delivery, so check the delivered mark too.
        TimestampedSequence delivered = deliveredEventSequences.get(eventId);
        if (delivered != null && sequence <= delivered.sequence()) {
            logger.trace("Skipping event {} snapshot {}, {} was already delivered", eventId, sequence,
                    delivered.sequence());
            return;
        }
        PendingUpdate state = Objects
                .requireNonNull(pendingEventUpdates.computeIfAbsent(eventId, k -> new PendingUpdate()));
        if (sequence < state.lastSequence) {
            logger.trace("Skipping event {} snapshot {}, a newer one ({}) already arrived", eventId, sequence,
                    state.lastSequence);
            return;
        }
        state.lastSequence = sequence;
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

    synchronized void deliverDebouncedUpdate(String eventId, PendingUpdate state) {
        // Guard against races if another task already delivered and cleared state
        PendingUpdate current = pendingEventUpdates.get(eventId);
        if (!state.equals(current)) {
            return;
        }
        // Remember how far this event got before the pending state is dropped.
        long now = System.currentTimeMillis();
        deliveredEventSequences.values().removeIf(s -> now - s.timestamp() > EVENT_SEQUENCE_TTL_MS);
        if (state.lastSequence != Long.MIN_VALUE) {
            deliveredEventSequences.put(eventId, new TimestampedSequence(state.lastSequence, now));
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
