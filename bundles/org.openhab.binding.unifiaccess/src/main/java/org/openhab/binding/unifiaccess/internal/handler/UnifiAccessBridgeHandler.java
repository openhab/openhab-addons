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
package org.openhab.binding.unifiaccess.internal.handler;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifiaccess.internal.UnifiAccessBindingConstants;
import org.openhab.binding.unifiaccess.internal.UnifiAccessDiscoveryService;
import org.openhab.binding.unifiaccess.internal.api.UniFiAccessApiClient;
import org.openhab.binding.unifiaccess.internal.config.UnifiAccessBridgeConfiguration;
import org.openhab.binding.unifiaccess.internal.dto.Device;
import org.openhab.binding.unifiaccess.internal.dto.Door;
import org.openhab.binding.unifiaccess.internal.dto.Notification;
import org.openhab.binding.unifiaccess.internal.dto.Notification.DeviceUpdateV2Data;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationUpdateV2Data;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteUnlockData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewChangeData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewData;
import org.openhab.binding.unifiaccess.internal.dto.UniFiAccessApiException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Bridge handler that manages the UniFi Access API client
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(UnifiAccessBridgeHandler.class);
    private static final int DEFAULT_PORT = 12445;
    private static final String DEFAULT_PATH = "/api/v1/developer";
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private final HttpClient httpClient;
    private @Nullable UniFiAccessApiClient apiClient;
    private UnifiAccessBridgeConfiguration config = new UnifiAccessBridgeConfiguration();
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private @Nullable UnifiAccessDiscoveryService discoveryService;
    private final Map<String, String> remoteViewRequestToDeviceId = new ConcurrentHashMap<>();

    public UnifiAccessBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        httpClient = httpClientFactory.createHttpClient(UnifiAccessBindingConstants.BINDING_ID,
                new SslContextFactory.Client(true));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(UnifiAccessDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing bridge handler");
        config = getConfigAs(UnifiAccessBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge handler");
        try {
            UniFiAccessApiClient client = this.apiClient;
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            logger.debug("Failed to close notifications WebSocket: {}", e.getMessage());
        }
        cancelReconnect();
        try {
            httpClient.stop();
        } catch (Exception ignored) {
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::syncDevices);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        logger.debug("Child handler initialized: {}", childHandler);
        if (childHandler instanceof UnifiAccessDoorHandler) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                syncDevices();
            }
        } else if (childHandler instanceof UnifiAccessDeviceHandler) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                syncDevices();
            }
        }
    }

    private synchronized void connect() {
        UniFiAccessApiClient client = this.apiClient;
        if (client != null) {
            client.close();
        }
        if (!httpClient.isStarted()) {
            try {
                httpClient.start();
            } catch (Exception e) {
                logger.debug("Failed to start HTTP client: {}", e.getMessage());
                setOfflineAndReconnect(e.getMessage());
            }
        }
        URI configuredBase = URI.create("https://" + config.host + ":" + DEFAULT_PORT + DEFAULT_PATH);
        client = new UniFiAccessApiClient(httpClient, configuredBase, gson, config.authToken, scheduler);
        this.apiClient = client;
        try {
            client.openNotifications(() -> {
                logger.info("Notifications WebSocket opened");
                updateStatus(ThingStatus.ONLINE);
                scheduler.execute(UnifiAccessBridgeHandler.this::syncDevices);
            }, notification -> {

                logger.debug("Notification event: {} data: {}", notification.event, notification.data);
                try {
                    switch (notification.event) {
                        // When a doorbell rings
                        case "access.remote_view":
                            RemoteViewData rv = notification.dataAsRemoteView(gson);
                            if (rv == null) {
                                break;
                            }
                            try {
                                if (rv.requestId != null && rv.deviceId != null) {
                                    remoteViewRequestToDeviceId.put(rv.requestId, rv.deviceId);
                                }
                                if (rv.clearRequestId != null && rv.deviceId != null) {
                                    remoteViewRequestToDeviceId.put(rv.clearRequestId, rv.deviceId);
                                }
                                handleRemoteView(rv);
                            } catch (Exception ex) {
                                logger.debug("Failed to handle remote_view: {}", ex.getMessage());
                            }
                            break;
                        // Doorbell status change
                        case "access.remote_view.change":
                            RemoteViewChangeData rvc = notification.dataAsRemoteViewChange(gson);
                            if (rvc == null) {
                                break;
                            }
                            try {
                                handleRemoteViewChange(rvc);
                                // route doorbell status to both device and door handlers if possible
                                if (rvc.remoteCallRequestId != null) {
                                    String deviceId = remoteViewRequestToDeviceId.get(rvc.remoteCallRequestId);
                                    if (deviceId != null) {
                                        UnifiAccessDeviceHandler dh = getDeviceHandlerByDeviceId(deviceId);
                                        if (dh != null) {
                                            dh.handleRemoteViewChange(rvc);
                                        }
                                        UnifiAccessDoorHandler d = getDoorHandler(deviceId);
                                        if (d != null) {
                                            d.handleDoorbellStatus(rvc);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                logger.debug("Failed to handle remote_view.change: {}", ex.getMessage());
                            }
                            break;
                        // Remote door unlock by admin
                        case "access.data.device.remote_unlock":
                            RemoteUnlockData ru = notification.dataAsRemoteUnlock(gson);
                            logger.debug("Device remote unlock: {}", ru.name);
                            handleRemoteUnlock(ru);
                            break;
                        case "access.data.device.update":
                            // TODO: handle device update which carries Online status
                            notification.dataAsDeviceUpdate(gson);
                            break;
                        case "access.data.v2.device.update":
                            DeviceUpdateV2Data du2 = notification.dataAsDeviceUpdateV2(gson);
                            if (du2 == null) {
                                break;
                            }
                            try {
                                handleDeviceUpdateV2(du2);
                            } catch (Exception ex) {
                                logger.debug("Failed to handle device update: {}", ex.getMessage());
                            }

                            break;
                        case "access.data.v2.location.update":
                            LocationUpdateV2Data lu2 = notification.dataAsLocationUpdateV2(gson);
                            if (lu2 == null) {
                                break;
                            }
                            try {
                                handleLocationUpdateV2(lu2);
                            } catch (Exception ex) {
                                logger.debug("Failed to handle location update: {}", ex.getMessage());
                            }
                            break;
                        case "access.logs.insights.add": {
                            var data = notification.dataAsInsightLogsAdd(gson);
                            if (data == null) {
                                break;
                            }
                            String cameraId = data.metadata != null && data.metadata.cameraCapture != null
                                    ? data.metadata.cameraCapture.alternateId
                                    : null;
                            // fire bridge trigger with compact JSON payload
                            Map<String, Object> insight = new LinkedHashMap<>();
                            if (data.logKey != null) {
                                insight.put("logKey", data.logKey);
                            }
                            if (data.eventType != null) {
                                insight.put("eventType", data.eventType);
                            }
                            if (data.message != null) {
                                insight.put("message", data.message);
                            }
                            if (data.published != null) {
                                insight.put("published", data.published);
                            }
                            if (data.result != null) {
                                insight.put("result", data.result);
                            }
                            String actorName = (data.metadata != null && data.metadata.actor != null)
                                    ? data.metadata.actor.displayName
                                    : null;
                            if (actorName != null) {
                                insight.put("actorName", actorName);
                            }
                            String insightDoorId = (data.metadata != null && data.metadata.door != null)
                                    ? data.metadata.door.id
                                    : null;
                            if (insightDoorId != null) {
                                insight.put("doorId", insightDoorId);
                            }
                            String insightDoorName = (data.metadata != null && data.metadata.door != null)
                                    ? data.metadata.door.displayName
                                    : null;
                            if (insightDoorName != null) {
                                insight.put("doorName", insightDoorName);
                            }
                            String insightDeviceId = (data.metadata != null && data.metadata.device != null)
                                    ? data.metadata.device.id
                                    : null;
                            if (insightDeviceId != null) {
                                insight.put("deviceId", insightDeviceId);
                            }
                            if (cameraId != null) {
                                insight.put("cameraId", cameraId);
                            }
                            String payload = gson.toJson(insight);
                            // bridge-level trigger
                            triggerChannel(UnifiAccessBindingConstants.CHANNEL_BRIDGE_LOG_INSIGHT, payload);

                            // route to specific door/device if referenced
                            String doorId = data.metadata != null && data.metadata.door != null ? data.metadata.door.id
                                    : null;
                            if (doorId != null) {
                                UnifiAccessDoorHandler dh = getDoorHandler(doorId);
                                if (dh != null) {
                                    dh.triggerLogInsight(payload);
                                }
                            }
                            String deviceId = data.metadata != null && data.metadata.device != null
                                    ? data.metadata.device.id
                                    : null;
                            if (deviceId != null) {
                                UnifiAccessDeviceHandler d = getDeviceHandlerByDeviceId(deviceId);
                                if (d != null) {
                                    d.triggerLogInsight(payload);
                                }
                            }
                        }
                            break;
                        case "access.logs.add": {
                            var data = notification.dataAsLogsAdd(gson);
                            if (data == null || data.source == null) {
                                break;
                            }
                            Map<String, Object> logMap = new LinkedHashMap<>();
                            if (data.source.event != null) {
                                if (data.source.event.type != null) {
                                    logMap.put("type", data.source.event.type);
                                }
                                if (data.source.event.displayMessage != null) {
                                    logMap.put("displayMessage", data.source.event.displayMessage);
                                }
                                if (data.source.event.result != null) {
                                    logMap.put("result", data.source.event.result);
                                }
                                if (data.source.event.published != null) {
                                    logMap.put("published", data.source.event.published);
                                }
                                if (data.source.event.logKey != null) {
                                    logMap.put("logKey", data.source.event.logKey);
                                }
                                if (data.source.event.logCategory != null) {
                                    logMap.put("logCategory", data.source.event.logCategory);
                                }
                            }
                            if (data.source.actor != null && data.source.actor.displayName != null) {
                                logMap.put("actorName", data.source.actor.displayName);
                            }
                            String payload = gson.toJson(logMap);
                            triggerChannel(UnifiAccessBindingConstants.CHANNEL_BRIDGE_LOG, payload);

                            // door-level success/failure triggers
                            String doorId = (data.source.target == null) ? null
                                    : data.source.target.stream().filter(t -> "door".equalsIgnoreCase(t.type))
                                            .map(t -> t.id).findFirst().orElse(null);

                            if (doorId != null) {
                                boolean isSuccess = data.source.event != null
                                        && "ACCESS".equalsIgnoreCase(data.source.event.result);
                                Map<String, Object> accessMap = new LinkedHashMap<>();
                                if (data.source.actor != null && data.source.actor.displayName != null) {
                                    accessMap.put("actorName", data.source.actor.displayName);
                                }
                                if (data.source.authentication != null
                                        && data.source.authentication.credentialProvider != null) {
                                    accessMap.put("credentialProvider", data.source.authentication.credentialProvider);
                                }
                                if (data.source.event != null && data.source.event.displayMessage != null) {
                                    accessMap.put("message", data.source.event.displayMessage);
                                }
                                String accessPayload = gson.toJson(accessMap);
                                UnifiAccessDoorHandler dh = getDoorHandler(doorId);
                                if (dh != null) {
                                    if (isSuccess) {
                                        dh.triggerAccessAttemptSuccess(accessPayload);
                                    } else {
                                        dh.triggerAccessAttemptFailure(accessPayload);
                                    }
                                }
                            }
                        }
                            break;

                        case "access.base.info":
                            notification.dataAsBaseInfo(gson);
                            break;
                        case "access.hw.door_bell":
                            // this is notified of a hardware doorbell event start, but we don't get a stop
                            // event
                            notification.dataAsDoorBell(gson);
                            break;
                        default:
                            // leave as raw
                            break;
                    }
                } catch (Exception ex) {
                    logger.debug("Failed to parse typed notification for {}: {}", notification.event, ex.getMessage());
                }
            }, error -> {
                logger.debug("Notifications error: {}", error.getMessage());
                setOfflineAndReconnect(error.getMessage());
            }, (statusCode, reason) -> {
                logger.debug("Notifications closed: {} - {}", statusCode, reason);
                setOfflineAndReconnect(reason);
            });
        } catch (UniFiAccessApiException e) {
            logger.debug("Failed to open notifications WebSocket", e);
            setOfflineAndReconnect("Failed to open notifications WebSocket");
        }
    }

    public void setDiscoveryService(UnifiAccessDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    private void setOfflineAndReconnect(@Nullable String message) {
        ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
        if (reconnectFuture != null && !reconnectFuture.isDone()) {
            return;
        }
        String msg = message != null ? message : "Unknown error";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);
        this.reconnectFuture = scheduler.schedule(() -> {
            try {
                // schedule this so our reconnectFuture completes after calling right away so if
                // we need to reconnect again, this job is already finished.
                scheduler.execute(this::connect);
            } catch (Exception ex) {
                logger.debug("Reconnect attempt failed to schedule connect: {}", ex.getMessage());
            }
        }, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void cancelReconnect() {
        try {
            ScheduledFuture<?> f = reconnectFuture;
            if (f != null) {
                f.cancel(true);
            }
        } catch (Exception ignored) {
        } finally {
            reconnectFuture = null;
        }
    }

    private synchronized void syncDevices() {
        UniFiAccessApiClient client = this.apiClient;
        if (client == null) {
            return;
        }
        try {
            List<Door> doors = client.getDoors();
            List<Device> devices = client.getDevices();

            // TODO we need to use the isOnline status of the "device" to update both a
            // device, and a doors thing status

            // exclude any whose locationId matches a door id
            List<Device> filteredDevices = devices.stream()
                    .filter(device -> device.locationId == null
                            || !doors.stream().anyMatch(door -> door.id.equals(device.locationId)))
                    .collect(Collectors.toList());

            UnifiAccessDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                discoveryService.discoverDoors(doors);
                discoveryService.discoverDevices(filteredDevices);
            }
            logger.trace("Polled UniFi Access: {} doors", doors.size());
            if (!doors.isEmpty()) {
                for (Thing t : getThing().getThings()) {
                    logger.trace("Checking thing: {} against {}", t.getThingTypeUID(),
                            UnifiAccessBindingConstants.DOOR_THING_TYPE);
                    if (UnifiAccessBindingConstants.DOOR_THING_TYPE.equals(t.getThingTypeUID())) {
                        if (t.getHandler() instanceof UnifiAccessDoorHandler dh) {
                            logger.trace("Updating door: {}", dh.deviceId);
                            String did = String
                                    .valueOf(t.getConfiguration().get(UnifiAccessBindingConstants.CONFIG_DEVICE_ID));
                            logger.trace("Device ID: {}", did);
                            Door match = doors.stream().filter(x -> did.equals(x.id)).findFirst().orElse(null);
                            if (match != null) {
                                dh.updateFromDoor(match);
                            }
                        }
                    }
                }
            }
            if (!filteredDevices.isEmpty()) {
                for (Thing t : getThing().getThings()) {
                    if (UnifiAccessBindingConstants.DEVICE_THING_TYPE.equals(t.getThingTypeUID())) {
                        if (t.getHandler() instanceof UnifiAccessDeviceHandler dh) {
                            try {
                                var settings = client.getDeviceAccessMethodSettings(dh.deviceId);
                                dh.updateFromSettings(settings);
                            } catch (UniFiAccessApiException ex) {
                                logger.debug("Failed to update device {}: {}", dh.deviceId, ex.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (UniFiAccessApiException e) {
            logger.debug("Polling error: {}", e.getMessage());
        }
    }

    public @Nullable UniFiAccessApiClient getApiClient() {
        return apiClient;
    }

    public @Nullable UnifiAccessBridgeConfiguration getUaConfig() {
        return config;
    }

    private void handleRemoteUnlock(Notification.RemoteUnlockData data) {
        for (Thing t : getThing().getThings()) {
            if (UnifiAccessBindingConstants.DOOR_THING_TYPE.equals(t.getThingTypeUID())) {
                String did = String.valueOf(t.getConfiguration().get(UnifiAccessBindingConstants.CONFIG_DEVICE_ID));
                if (data.uniqueId.equals(did) && t.getHandler() instanceof UnifiAccessDoorHandler dh) {
                    dh.handleRemoteUnlock(data);
                }
            }
        }
    }

    private void handleRemoteView(RemoteViewData rv) {
        UnifiAccessDeviceHandler dh = getDeviceHandlerByDeviceId(rv.deviceId);
        if (dh != null) {
            dh.handleRemoteView(rv);
        }
    }

    private void handleRemoteViewChange(RemoteViewChangeData rvc) {
        // First try to route via remote call request id mapping (if available)
        String deviceId = null;
        if (rvc.remoteCallRequestId != null) {
            deviceId = remoteViewRequestToDeviceId.get(rvc.remoteCallRequestId);
        }
        if (deviceId != null) {
            UnifiAccessDeviceHandler dh = getDeviceHandlerByDeviceId(deviceId);
            if (dh != null) {
                dh.handleRemoteViewChange(rvc);
                return;
            }
        }
    }

    private void handleDeviceUpdateV2(Notification.DeviceUpdateV2Data updateData) {
        if (updateData.locationStates != null) {
            updateData.locationStates.forEach(ls -> {
                UnifiAccessDoorHandler dh = getDoorHandler(ls.locationId);
                if (dh != null) {
                    dh.handleLocationState(ls);
                }
            });
        } else {
            // update for a device ?
            // TODO this update carries the isOnline status of the device, so we need to
            // update the device thing status
        }
    }

    private void handleLocationUpdateV2(LocationUpdateV2Data lu2) {
        // Forward to matching device handlers by device ids under this location
        if (lu2.state != null && lu2.deviceIds != null) {
            for (String deviceId : lu2.deviceIds) {
                UnifiAccessDoorHandler dh = getDoorHandler(deviceId);
                if (dh != null) {
                    dh.handleLocationUpdateV2(lu2);
                }
                UnifiAccessDeviceHandler uh = getDeviceHandlerByDeviceId(deviceId);
                if (uh != null) {
                    uh.handleLocationState(lu2.state);
                }
            }
        }
    }

    private @Nullable UnifiAccessDoorHandler getDoorHandler(String doorId) {
        for (Thing t : getThing().getThings()) {
            if (UnifiAccessBindingConstants.DOOR_THING_TYPE.equals(t.getThingTypeUID())) {
                String did = String.valueOf(t.getConfiguration().get(UnifiAccessBindingConstants.CONFIG_DEVICE_ID));
                if (doorId.equals(did) && t.getHandler() instanceof UnifiAccessDoorHandler dh) {
                    return dh;
                }
            }
        }
        return null;
    }

    private @Nullable UnifiAccessDeviceHandler getDeviceHandlerByDeviceId(String deviceId) {
        for (Thing t : getThing().getThings()) {
            if (UnifiAccessBindingConstants.DEVICE_THING_TYPE.equals(t.getThingTypeUID())) {
                String did = String.valueOf(t.getConfiguration().get(UnifiAccessBindingConstants.CONFIG_DEVICE_ID));
                if (deviceId.equals(did) && t.getHandler() instanceof UnifiAccessDeviceHandler dh) {
                    return dh;
                }
            }
        }
        return null;
    }
}
