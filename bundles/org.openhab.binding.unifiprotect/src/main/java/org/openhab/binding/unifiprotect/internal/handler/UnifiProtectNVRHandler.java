/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.UnifiProtectDiscoveryService;
import org.openhab.binding.unifiprotect.internal.api.UniFiProtectApiClient;
import org.openhab.binding.unifiprotect.internal.api.dto.Camera;
import org.openhab.binding.unifiprotect.internal.api.dto.DeviceState;
import org.openhab.binding.unifiprotect.internal.api.dto.Light;
import org.openhab.binding.unifiprotect.internal.api.dto.Nvr;
import org.openhab.binding.unifiprotect.internal.api.dto.ProtectVersionInfo;
import org.openhab.binding.unifiprotect.internal.api.dto.Sensor;
import org.openhab.binding.unifiprotect.internal.api.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.api.dto.events.EventType;
import org.openhab.binding.unifiprotect.internal.api.dto.gson.DeviceTypeAdapterFactory;
import org.openhab.binding.unifiprotect.internal.api.dto.gson.EventTypeAdapterFactory;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectNVRConfiguration;
import org.openhab.binding.unifiprotect.internal.handler.UnifiProtectAbstractDeviceHandler.WSEventType;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
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

    private @Nullable UnifiProtectNVRConfiguration config;

    private @Nullable UniFiProtectApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollTask;
    private @Nullable ScheduledFuture<?> reconnectTask;
    private @Nullable UnifiProtectDiscoveryService discoveryService;
    private final HttpClient httpClient;
    private Gson gson;
    private boolean shuttingDown = false;

    private static final long WS_UPDATE_DEBOUNCE_MS = 500; // inactivity window
    private static final long WS_UPDATE_MAX_WAIT_MS = 2000; // max wait per burst
    private static final long CHILD_REFRESH_RETRY_DELAY_SECONDS = 10; // retry delay for failed child refresh
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
        if (config.hostname.isBlank() || config.token.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname or token is blank");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                URI base = URI.create("https://" + config.hostname + "/proxy/protect/integration/");
                UniFiProtectApiClient apiClient = new UniFiProtectApiClient(httpClient, base, gson, config.token,
                        scheduler);
                this.apiClient = apiClient;

                apiClient.subscribeEvents(add -> {
                    routeEvent(add.item, WSEventType.ADD);
                }, update -> {
                    handleUpdateEvent(update.item);
                }, () -> {
                    updateStatus(ThingStatus.ONLINE);
                    scheduler.execute(() -> syncDevices());
                }, (code, reason) -> {
                    logger.debug("Event WS closed: {} {}", code, reason);
                    setOfflineAndReconnect();
                }, err -> logger.debug("Event WS error", err)).get();

                apiClient.subscribeDevices(add -> {
                    UnifiProtectDiscoveryService discoveryService = this.discoveryService;
                    if (discoveryService == null) {
                        logger.debug("Discovery service not set");
                        return;
                    }
                    switch (add.item.modelKey) {
                        case CAMERA:
                            discoveryService.discoverCameras(apiClient);
                            break;
                        case LIGHT:
                            discoveryService.discoverLights(apiClient);
                            break;
                        case SENSOR:
                            discoveryService.discoverSensors(apiClient);
                            break;
                        default:
                            // ignore
                    }
                }, update -> {
                    scheduler.execute(() -> {
                        if (update.item == null || update.item.id == null) {
                            return;
                        }
                        String id = update.item.id;
                        DeviceState state = update.item.state;
                        if (state != null) {
                            switch (update.item.modelKey) {
                                case CAMERA:
                                    setChildStatus(UnifiProtectBindingConstants.THING_TYPE_CAMERA, id, state);
                                    break;
                                case LIGHT:
                                    setChildStatus(UnifiProtectBindingConstants.THING_TYPE_LIGHT, id, state);
                                    break;
                                case SENSOR:
                                    setChildStatus(UnifiProtectBindingConstants.THING_TYPE_SENSOR, id, state);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }, remove -> {
                    scheduler.execute(() -> {
                        if (remove.item == null || remove.item.id == null) {
                            return;
                        }
                        String id = remove.item.id;
                        switch (remove.item.modelKey) {
                            case CAMERA:
                                markChildGone(UnifiProtectBindingConstants.THING_TYPE_CAMERA, id);
                                break;
                            case LIGHT:
                                markChildGone(UnifiProtectBindingConstants.THING_TYPE_LIGHT, id);
                                break;
                            case SENSOR:
                                markChildGone(UnifiProtectBindingConstants.THING_TYPE_SENSOR, id);
                                break;
                            default:
                                break;
                        }
                    });
                }, () -> {
                    // ignore on-open
                }, (code, reason) -> {
                    logger.debug("Device WS closed: {} {}", code, reason);
                    setOfflineAndReconnect();
                }, err -> logger.debug("Device WS error", err)).get();
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
        } catch (Exception ignored) {
        }
        super.dispose();
    }

    @Nullable
    public UniFiProtectApiClient getApiClient() {
        return apiClient;
    }

    public void setDiscoveryService(UnifiProtectDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    private @Nullable <T extends ThingHandler> T findChildHandler(ThingTypeUID thingType, String deviceId,
            Class<T> handlerType) {
        for (Thing t : getThing().getThings()) {
            if (thingType.equals(t.getThingTypeUID())) {
                String devId = getDeviceId(t);
                if (devId != null && devId.equals(deviceId)) {
                    ThingHandler handler = t.getHandler();
                    if (handlerType.isInstance(handler)) {
                        return handlerType.cast(handler);
                    }
                }
            }
        }
        return null;
    }

    private @Nullable String getDeviceId(Thing thing) {
        Object devIdObj = thing.getConfiguration().get(UnifiProtectBindingConstants.DEVICE_ID);
        return devIdObj != null ? String.valueOf(devIdObj) : null;
    }

    private void refreshChildFromApi(ThingTypeUID type, String deviceId) {
        if (UnifiProtectBindingConstants.THING_TYPE_CAMERA.equals(type)) {
            UnifiProtectCameraHandler handler = findChildHandler(type, deviceId, UnifiProtectCameraHandler.class);
            if (handler != null) {
                refreshChildFromApi(deviceId, handler);
            }
        } else if (UnifiProtectBindingConstants.THING_TYPE_LIGHT.equals(type)) {
            UnifiProtectLightHandler handler = findChildHandler(type, deviceId, UnifiProtectLightHandler.class);
            if (handler != null) {
                refreshChildFromApi(deviceId, handler);
            }
        } else if (UnifiProtectBindingConstants.THING_TYPE_SENSOR.equals(type)) {
            UnifiProtectSensorHandler handler = findChildHandler(type, deviceId, UnifiProtectSensorHandler.class);
            if (handler != null) {
                refreshChildFromApi(deviceId, handler);
            }
        }
    }

    private void setChildStatus(ThingTypeUID type, String deviceId, DeviceState state) {
        ThingStatus status = ThingStatus.OFFLINE;
        switch (state) {
            case CONNECTED:
                status = ThingStatus.ONLINE;
                break;
            case DISCONNECTED:
                status = ThingStatus.OFFLINE;
                break;
            case CONNECTING:
                status = ThingStatus.UNKNOWN;
                break;
            default:
                status = ThingStatus.OFFLINE;
                break;
        }
        if (UnifiProtectBindingConstants.THING_TYPE_CAMERA.equals(type)) {
            UnifiProtectCameraHandler handler = findChildHandler(type, deviceId, UnifiProtectCameraHandler.class);
            if (handler != null && handler.getThing().getStatus() != status) {
                handler.updateStatus(status);
            }
        } else if (UnifiProtectBindingConstants.THING_TYPE_LIGHT.equals(type)) {
            UnifiProtectLightHandler handler = findChildHandler(type, deviceId, UnifiProtectLightHandler.class);
            if (handler != null && handler.getThing().getStatus() != status) {
                handler.updateStatus(status);
            }
        } else if (UnifiProtectBindingConstants.THING_TYPE_SENSOR.equals(type)) {
            UnifiProtectSensorHandler handler = findChildHandler(type, deviceId, UnifiProtectSensorHandler.class);
            if (handler != null && handler.getThing().getStatus() != status) {
                handler.updateStatus(status);
            }
        }
    }

    private void refreshChildFromApi(String deviceId, UnifiProtectAbstractDeviceHandler<?> handler) {
        UniFiProtectApiClient apiClient = this.apiClient;
        if (apiClient == null) {
            return;
        }
        try {
            if (handler instanceof UnifiProtectCameraHandler cameraHandler) {
                Camera cam = apiClient.getCamera(deviceId);
                cameraHandler.updateFromDevice(cam);
            } else if (handler instanceof UnifiProtectLightHandler lightHandler) {
                Light light = apiClient.getLight(deviceId);
                lightHandler.updateFromDevice(light);
            } else if (handler instanceof UnifiProtectSensorHandler sensorHandler) {
                Sensor sensor = apiClient.getSensor(deviceId);
                sensorHandler.updateFromDevice(sensor);
            }
            cancelChildRefreshRetry(deviceId);
        } catch (IOException e) {
            logger.debug("Failed to refresh child {} from API", deviceId, e);
            handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    Objects.requireNonNull(e.getMessage(), "Failed to refresh child from API"));
            scheduleChildRefreshRetry(handler.getThing().getThingTypeUID(), deviceId);
        }
    }

    private void scheduleChildRefreshRetry(ThingTypeUID type, String deviceId) {
        ScheduledFuture<?> existing = childRefreshRetryTasks.get(deviceId);
        if (existing != null) {
            existing.cancel(true);
        }
        ScheduledFuture<?> future = scheduler.schedule(() -> refreshChildFromApi(type, deviceId),
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

    private void markChildGone(ThingTypeUID type, String deviceId) {
        if (UnifiProtectBindingConstants.THING_TYPE_CAMERA.equals(type)) {
            UnifiProtectCameraHandler ch = findChildHandler(type, deviceId, UnifiProtectCameraHandler.class);
            if (ch != null) {
                ch.markGone();
            }
        } else if (UnifiProtectBindingConstants.THING_TYPE_LIGHT.equals(type)) {
            UnifiProtectLightHandler lh = findChildHandler(type, deviceId, UnifiProtectLightHandler.class);
            if (lh != null) {
                lh.markGone();
            }
        } else if (UnifiProtectBindingConstants.THING_TYPE_SENSOR.equals(type)) {
            UnifiProtectSensorHandler sh = findChildHandler(type, deviceId, UnifiProtectSensorHandler.class);
            if (sh != null) {
                sh.markGone();
            }
        }
    }

    private void syncDevices() {
        UniFiProtectApiClient apiClient = this.apiClient;
        if (apiClient == null) {
            return;
        }
        try {
            try {
                ProtectVersionInfo meta = apiClient.getMetaInfo();
                if (meta.applicationVersion != null) {
                    updateProperty("applicationVersion", meta.applicationVersion);
                }
            } catch (IOException e) {
                logger.debug("Failed to read meta info", e);
                setOfflineAndReconnect();
                return;
            }

            // Basic NVR fetch (validate connectivity and log)
            Nvr nvr = apiClient.getNvr();
            logger.debug("NVR name: {}", nvr.name);

            UnifiProtectDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                apiClient.listCameras().forEach(camera -> {
                    UnifiProtectCameraHandler ch = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_CAMERA,
                            camera.id, UnifiProtectCameraHandler.class);
                    if (ch != null) {
                        ch.updateFromDevice(camera);
                    } else {
                        discoveryService.discoverCamera(camera);
                    }
                });
                apiClient.listLights().forEach(light -> {
                    UnifiProtectLightHandler lh = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_LIGHT,
                            light.id, UnifiProtectLightHandler.class);
                    if (lh != null) {
                        lh.updateFromDevice(light);
                    } else {
                        discoveryService.discoverLight(light);
                    }
                });
                apiClient.listSensors().forEach(sensor -> {
                    UnifiProtectSensorHandler sh = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_SENSOR,
                            sensor.id, UnifiProtectSensorHandler.class);
                    if (sh != null) {
                        sh.updateFromDevice(sensor);
                    } else {
                        discoveryService.discoverSensor(sensor);
                    }
                });
            } else {
                logger.debug("Discovery service not set");
            }
        } catch (IOException e) {
            logger.debug("Initial sync failed", e);
        }
    }

    private synchronized void setOfflineAndReconnect() {
        ScheduledFuture<?> reconnectTask = this.reconnectTask;
        if (shuttingDown || reconnectTask != null && !reconnectTask.isDone()) {
            return;
        }
        shuttingDown = true;
        updateStatus(ThingStatus.OFFLINE);
        stopApiClient();
        stopTasks();
        stopReconnectTask();
        this.reconnectTask = scheduler.schedule(this::initialize, 5, TimeUnit.SECONDS);
    }

    private void routeEvent(BaseEvent event, WSEventType eventType) {
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
            case SMART_DETECT_LOITER_ZONE: {
                UnifiProtectCameraHandler ch = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_CAMERA,
                        deviceId, UnifiProtectCameraHandler.class);
                if (ch != null) {
                    ch.handleEvent(event, eventType);
                }
                break;
            }
            case LIGHT_MOTION: {
                UnifiProtectLightHandler lh = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_LIGHT, deviceId,
                        UnifiProtectLightHandler.class);
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
                UnifiProtectSensorHandler sh = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_SENSOR,
                        deviceId, UnifiProtectSensorHandler.class);
                if (sh != null) {
                    sh.handleEvent(event, eventType);
                }
                break;
            }
            case RING: {
                UnifiProtectCameraHandler ch = findChildHandler(UnifiProtectBindingConstants.THING_TYPE_CAMERA,
                        deviceId, UnifiProtectCameraHandler.class);
                if (ch != null) {
                    ch.handleEvent(event, eventType);
                }
                break;
            }
            default:
                break;
        }
    }

    private void stopApiClient() {
        UniFiProtectApiClient apiClient = this.apiClient;
        if (apiClient != null) {
            try {
                apiClient.close();
            } catch (IOException ignored) {
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
            routeEvent(last, WSEventType.UPDATE);
        }
    }
}
