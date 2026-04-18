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
package org.openhab.binding.unifi.access.internal.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.access.internal.UnifiAccessBindingConstants;
import org.openhab.binding.unifi.access.internal.UnifiAccessDiscoveryService;
import org.openhab.binding.unifi.access.internal.api.UnifiAccessApiClient;
import org.openhab.binding.unifi.access.internal.config.UnifiAccessBridgeConfiguration;
import org.openhab.binding.unifi.access.internal.dto.Device;
import org.openhab.binding.unifi.access.internal.dto.Door;
import org.openhab.binding.unifi.access.internal.dto.DoorEmergencySettings;
import org.openhab.binding.unifi.access.internal.dto.UnifiAccessApiException;
import org.openhab.binding.unifi.api.UniFiException.AuthState;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifi.handler.UniFiControllerBridgeHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Bridge handler that manages the UniFi Access API client, WebSocket
 * notifications, and periodic device polling.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(UnifiAccessBridgeHandler.class);
    private static final int MAX_RECONNECT_DELAY_SECONDS = 300;
    private static final int THROTTLED_INITIAL_DELAY_SECONDS = 60;
    private static final int THROTTLED_MAX_DELAY_SECONDS = 1800;

    final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    final Map<String, String> remoteViewRequestToDeviceId = new ConcurrentHashMap<>();

    private @Nullable UnifiAccessApiClient apiClient;
    private UnifiAccessBridgeConfiguration config = new UnifiAccessBridgeConfiguration();
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private @Nullable ScheduledFuture<?> pollingFuture;
    private @Nullable UnifiAccessDiscoveryService discoveryService;
    private @Nullable UnifiAccessNotificationRouter notificationRouter;
    private int reconnectAttempt = 0;
    private int throttledReconnectAttempt = 0;

    public UnifiAccessBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Returns the parent {@code unifi:controller} bridge handler, or {@code null} if this Access bridge has no
     * parent configured yet (e.g. an orphaned legacy {@code unifiaccess:bridge} thing that has not been
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
        return List.of(UnifiAccessDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing bridge handler");

        UniFiControllerBridgeHandler parentHandler = getParentHandler();
        if (parentHandler == null) {
            Object storedHost = getThing().getConfiguration().get("host");
            String hint = storedHost != null ? " (previously configured host: " + storedHost + ")" : "";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.requires-unifi-controller-bridge" + hint);
            return;
        }

        config = getConfigAs(UnifiAccessBridgeConfiguration.class);
        notificationRouter = new UnifiAccessNotificationRouter(gson, this, remoteViewRequestToDeviceId);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::connect);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge handler");
        cancelReconnect();
        cancelPolling();
        try {
            UnifiAccessApiClient client = this.apiClient;
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            logger.debug("Failed to close notifications WebSocket: {}", e.getMessage());
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::syncDevices);
            return;
        }
        String channelId = channelUID.getId();
        if (UnifiAccessBindingConstants.CHANNEL_BRIDGE_EMERGENCY_STATUS.equals(channelId)) {
            UnifiAccessApiClient api = this.apiClient;
            if (api == null) {
                return;
            }
            String value = command.toString().toLowerCase(java.util.Locale.ROOT);
            DoorEmergencySettings des = new DoorEmergencySettings();
            String status = "normal";
            if ("lockdown".equals(value)) {
                des.lockdown = Boolean.TRUE;
                des.evacuation = Boolean.FALSE;
                status = "lockdown";
            } else if ("evacuation".equals(value)) {
                des.lockdown = Boolean.FALSE;
                des.evacuation = Boolean.TRUE;
                status = "evacuation";
            } else {
                des.lockdown = Boolean.FALSE;
                des.evacuation = Boolean.FALSE;
            }
            try {
                api.setEmergencySettings(des);
                updateState(UnifiAccessBindingConstants.CHANNEL_BRIDGE_EMERGENCY_STATUS, new StringType(status));
            } catch (UnifiAccessApiException e) {
                logger.debug("Failed to set emergency settings: {}", e.getMessage());
            }
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        logger.debug("Child handler initialized: {}", childHandler);
        if (childHandler instanceof UnifiAccessBaseHandler && getThing().getStatus() == ThingStatus.ONLINE) {
            syncDevices();
        }
    }

    private synchronized void connect() {
        UnifiAccessApiClient existing = this.apiClient;
        if (existing != null) {
            existing.close();
        }

        UniFiControllerBridgeHandler parentHandler = getParentHandler();
        if (parentHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.requires-unifi-controller-bridge");
            return;
        }

        UniFiSession session;
        try {
            session = parentHandler.getSessionAsync().get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.debug("Parent bridge session not available: {}", e.getMessage());
            setOfflineAndReconnect(e.getMessage(), false);
            return;
        }

        HttpClient httpClient = parentHandler.getHttpClient();
        String host = parentHandler.getHost();

        UnifiAccessApiClient client = new UnifiAccessApiClient(httpClient, host, gson, session, scheduler);
        this.apiClient = client;
        UnifiAccessNotificationRouter router = this.notificationRouter;
        try {
            client.openNotifications(() -> {
                logger.debug("Notifications WebSocket opened");
                reconnectAttempt = 0;
                throttledReconnectAttempt = 0;
                updateStatus(ThingStatus.ONLINE);
                startPolling();
                scheduler.execute(UnifiAccessBridgeHandler.this::syncDevices);
            }, notification -> {
                if (router != null) {
                    router.routeNotification(notification);
                }
            }, error -> {
                logger.debug("Notifications error: {}", error.getMessage());
                setOfflineAndReconnect(error.getMessage(), false);
            }, (statusCode, reason) -> {
                logger.debug("Notifications closed: {} - {}", statusCode, reason);
                setOfflineAndReconnect(reason, false);
            });
        } catch (UnifiAccessApiException e) {
            logger.debug("Failed to open notifications WebSocket", e);
            switch (e.getAuthState()) {
                case REJECTED:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.auth-failed");
                    break;
                case THROTTLED:
                    setOfflineAndReconnect("@text/offline.login-throttled", true);
                    break;
                default:
                    setOfflineAndReconnect("@text/offline.notifications-failed", false);
                    break;
            }
        }
    }

    public void setDiscoveryService(UnifiAccessDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    private synchronized void setOfflineAndReconnect(@Nullable String message, boolean throttled) {
        ScheduledFuture<?> existing = this.reconnectFuture;
        if (existing != null && !existing.isDone()) {
            // Throttled supersedes any pending fast reconnect, otherwise we'd just get throttled again.
            if (throttled) {
                existing.cancel(false);
            } else {
                return;
            }
        }
        cancelPolling();
        String msg = message != null ? message : "Unknown error";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, msg);

        int delay;
        if (throttled) {
            delay = Math.min((int) Math.pow(2, throttledReconnectAttempt) * THROTTLED_INITIAL_DELAY_SECONDS,
                    THROTTLED_MAX_DELAY_SECONDS);
            throttledReconnectAttempt++;
            logger.debug("Scheduling reconnect in {} seconds (throttled, attempt {})", delay,
                    throttledReconnectAttempt);
        } else {
            delay = Math.min((int) Math.pow(2, reconnectAttempt) * 5, MAX_RECONNECT_DELAY_SECONDS);
            reconnectAttempt++;
            logger.debug("Scheduling reconnect in {} seconds (attempt {})", delay, reconnectAttempt);
        }

        this.reconnectFuture = scheduler.schedule(() -> {
            try {
                scheduler.execute(this::connect);
            } catch (Exception ex) {
                logger.debug("Reconnect attempt failed to schedule connect: {}", ex.getMessage());
            }
        }, delay, TimeUnit.SECONDS);
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

    private void startPolling() {
        cancelPolling();
        int interval = config.refreshInterval;
        if (interval > 0) {
            pollingFuture = scheduler.scheduleWithFixedDelay(this::syncDevices, interval, interval, TimeUnit.SECONDS);
        }
    }

    private void cancelPolling() {
        ScheduledFuture<?> f = pollingFuture;
        if (f != null) {
            f.cancel(true);
            pollingFuture = null;
        }
    }

    synchronized void syncDevices() {
        UnifiAccessApiClient client = this.apiClient;
        if (client == null) {
            return;
        }
        try {
            List<Door> doors = client.getDoors();
            List<Device> devices = client.getDevices();

            // For discovery: exclude devices whose locationId matches a door (avoid duplicate things).
            // For state sync: update ALL devices that have handlers, regardless of locationId.
            List<Device> discoveryDevices = devices.stream()
                    .filter(device -> device.locationId == null
                            || !doors.stream().anyMatch(door -> door.id.equals(device.locationId)))
                    .collect(Collectors.toList());

            UnifiAccessDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                discoveryService.discoverDoors(doors);
                discoveryService.discoverDevices(discoveryDevices);
            }
            logger.trace("Polled UniFi Access: {} doors, {} devices", doors.size(), devices.size());
            for (Door door : doors) {
                logger.trace("Checking door: {}", door.id);
                UnifiAccessDoorHandler dh = getDoorHandler(door.id);
                if (dh != null) {
                    logger.trace("Updating door: {}", dh.deviceId);
                    dh.updateFromDoor(door);
                }
            }
            for (Device device : devices) {
                String devId = device.id;
                if (devId == null || device.isHub()) {
                    continue;
                }
                UnifiAccessDeviceHandler dh = getDeviceHandler(devId);
                if (dh != null) {
                    logger.debug("Syncing device {} (type={}, online={}, locationId={})", device.id, device.type,
                            device.isOnline, device.locationId);
                    // Set online/offline based on device status, independent of settings
                    boolean online = !Boolean.FALSE.equals(device.isOnline);
                    dh.updateState(UnifiAccessBindingConstants.CHANNEL_DEVICE_ONLINE,
                            online ? OnOffType.ON : OnOffType.OFF);
                    if (!online) {
                        dh.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "@text/offline.device-offline");
                    } else if (dh.getThing().getStatus() != ThingStatus.ONLINE) {
                        dh.setOnline();
                    }
                    // Update thing properties from device metadata
                    dh.updateDeviceProperties(device);
                    // Cross-populate door state for devices linked to a door
                    if (device.locationId != null) {
                        doors.stream().filter(d -> d.id.equals(device.locationId)).findFirst()
                                .ifPresent(dh::updateFromDoor);
                    }
                    // Store configs and build settings from bootstrap
                    dh.updateConfigMap(device.configMap);
                    if (!device.configMap.isEmpty()) {
                        var settings = UnifiAccessApiClient.buildSettingsFromConfigs(device.configMap);
                        dh.updateFromSettings(settings);
                    }
                }
            }
        } catch (UnifiAccessApiException e) {
            logger.debug("Polling error: {}", e.getMessage());
            if (e.getAuthState() == AuthState.REJECTED) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.auth-failed");
                cancelPolling();
            } else if (e.getAuthState() == AuthState.THROTTLED) {
                setOfflineAndReconnect("@text/offline.login-throttled", true);
            }
        }
    }

    public @Nullable UnifiAccessApiClient getApiClient() {
        return apiClient;
    }

    public @Nullable UnifiAccessBridgeConfiguration getUaConfig() {
        return config;
    }

    // -- Package-private handler lookup methods used by NotificationRouter --

    @Nullable
    UnifiAccessDoorHandler getDoorHandler(String doorId) {
        if (getBaseHandler(doorId) instanceof UnifiAccessDoorHandler dh) {
            return dh;
        }
        return null;
    }

    @Nullable
    UnifiAccessDeviceHandler getDeviceHandler(String deviceId) {
        if (getBaseHandler(deviceId) instanceof UnifiAccessDeviceHandler dh) {
            return dh;
        }
        return null;
    }

    @Nullable
    UnifiAccessBaseHandler getBaseHandler(String deviceId) {
        for (Thing t : getThing().getThings()) {
            if (t.getHandler() instanceof UnifiAccessBaseHandler bh && bh.deviceId.equals(deviceId)) {
                return bh;
            }
        }
        return null;
    }

    void fireTriggerChannel(String channelId, String payload) {
        triggerChannel(channelId, payload);
    }

    void updateBridgeState(String channelId, State state) {
        updateState(channelId, state);
    }
}
