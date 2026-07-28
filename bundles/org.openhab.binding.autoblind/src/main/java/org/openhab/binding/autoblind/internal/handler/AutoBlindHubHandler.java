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
package org.openhab.binding.autoblind.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.autoblind.internal.AutoBlindBindingConstants;
import org.openhab.binding.autoblind.internal.api.AutoBlindApiClient;
import org.openhab.binding.autoblind.internal.api.dto.AllPeripheralResponse;
import org.openhab.binding.autoblind.internal.api.dto.NotificationEvent;
import org.openhab.binding.autoblind.internal.api.dto.PeripheralStatus;
import org.openhab.binding.autoblind.internal.api.dto.RegistrationResponse;
import org.openhab.binding.autoblind.internal.api.dto.StatusResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Bridge handler for the AutoBlind Hub. Manages API communication and polling.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class AutoBlindHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AutoBlindHubHandler.class);
    private final HttpClient httpClient;
    private final Map<Integer, AutoBlindShadeHandler> shadeHandlers = new ConcurrentHashMap<>();

    private @Nullable AutoBlindApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> refreshPollJob;
    private @Nullable ScheduledFuture<?> trackingPollJob;
    private @Nullable ScheduledFuture<?> trackingTimeoutJob;
    private @Nullable ScheduledFuture<?> verificationPollJob;
    private @Nullable ScheduledFuture<?> notificationWatchJob;
    private volatile Map<Integer, Integer> previousPositions = Map.of();
    private volatile long lastCommandTimestamp = 0;
    private volatile long notificationWatermark = 0;
    private volatile boolean notificationWatchRunning;

    public AutoBlindHubHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        Object hostObj = getConfig().get(AutoBlindBindingConstants.CONFIG_HOST);
        if (hostObj == null || hostObj.toString().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host not configured");
            return;
        }
        String host = hostObj.toString();

        Object intervalObj = getConfig().get(AutoBlindBindingConstants.CONFIG_POLLING_INTERVAL);
        int pollingInterval = intervalObj instanceof Number n ? n.intValue() : 1800;

        apiClient = new AutoBlindApiClient(httpClient, host);

        scheduler.execute(() -> {
            try {
                AutoBlindApiClient client = apiClient;
                if (client == null) {
                    return;
                }
                RegistrationResponse reg = client.register();
                if (reg.error != 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Hub returned error " + reg.error);
                    return;
                }

                Map<String, String> properties = editProperties();
                properties.put("firmwareVersion", reg.firmwareVersion);
                properties.put("model", reg.model);
                properties.put("thingName", reg.thingName);
                properties.put("wifiSsid", reg.wifiSsid);
                updateProperties(properties);

                updateStatus(ThingStatus.ONLINE);
                logger.debug("Hub online: {} (firmware {})", reg.model, reg.firmwareVersion);

                pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 5, pollingInterval, TimeUnit.SECONDS);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        ScheduledFuture<?> refresh = refreshPollJob;
        if (refresh != null) {
            refresh.cancel(false);
            refreshPollJob = null;
        }
        stopTrackingPoll();
        stopNotificationWatch();
        ScheduledFuture<?> verification = verificationPollJob;
        if (verification != null) {
            verification.cancel(false);
            verificationPollJob = null;
        }
        apiClient = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (AutoBlindBindingConstants.CHANNEL_FORCE_REFRESH.equals(channelUID.getId())
                && OnOffType.ON.equals(command)) {
            logger.debug("Force refresh requested — clearing motion/suppression and polling");
            clearAllMotion();
            clearAllSuppression();
            stopNotificationWatch();
            scheduler.execute(this::poll);
            updateState(AutoBlindBindingConstants.CHANNEL_FORCE_REFRESH, OnOffType.OFF);
        }
    }

    private void poll() {
        try {
            AutoBlindApiClient client = apiClient;
            if (client == null) {
                return;
            }
            StatusResponse status = client.getStatus();
            if (status.error != 0) {
                logger.debug("Hub status poll returned error {}", status.error);
                return;
            }

            for (PeripheralStatus peripheral : status.peripherals) {
                AutoBlindShadeHandler handler = shadeHandlers.get(peripheral.peripheralUid);
                if (handler != null) {
                    handler.updateFromStatus(peripheral);
                }
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.debug("Poll failed: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public synchronized void scheduleRefreshPoll(int delaySec) {
        ScheduledFuture<?> existing = refreshPollJob;
        if (existing != null) {
            existing.cancel(false);
        }
        refreshPollJob = scheduler.schedule(this::poll, delaySec, TimeUnit.SECONDS);
    }

    public synchronized void startTrackingPoll() {
        stopTrackingPoll();
        lastCommandTimestamp = System.currentTimeMillis();
        previousPositions = Map.of();
        trackingPollJob = scheduler.scheduleWithFixedDelay(this::trackingPoll, 5, 5, TimeUnit.SECONDS);
        trackingTimeoutJob = scheduler.schedule(this::stopTrackingAndVerify, 60, TimeUnit.SECONDS);
        logger.debug("Started tracking poll for position changes");
    }

    private void trackingPoll() {
        try {
            AutoBlindApiClient client = apiClient;
            if (client == null) {
                return;
            }
            StatusResponse status = client.getStatus();
            if (status.error != 0) {
                logger.debug("Tracking poll returned error {}", status.error);
                return;
            }

            Map<Integer, Integer> currentPositions = new HashMap<>();
            for (PeripheralStatus peripheral : status.peripherals) {
                currentPositions.put(peripheral.peripheralUid, peripheral.bottomRailPosition);
                AutoBlindShadeHandler handler = shadeHandlers.get(peripheral.peripheralUid);
                if (handler != null) {
                    handler.updateFromStatus(peripheral);
                }
            }

            Map<Integer, Integer> previous = previousPositions;
            if (!previous.isEmpty() && currentPositions.equals(previous)) {
                logger.debug("Shade positions stabilized, stopping tracking poll");
                stopTrackingAndVerify();
            }
            previousPositions = Map.copyOf(currentPositions);
        } catch (Exception e) {
            logger.debug("Tracking poll failed: {}", e.getMessage());
        }
    }

    public synchronized void stopTrackingPoll() {
        ScheduledFuture<?> job = trackingPollJob;
        if (job != null) {
            job.cancel(false);
            trackingPollJob = null;
        }
        ScheduledFuture<?> timeout = trackingTimeoutJob;
        if (timeout != null) {
            timeout.cancel(false);
            trackingTimeoutJob = null;
        }
    }

    private synchronized void stopTrackingAndVerify() {
        stopTrackingPoll();
        scheduleVerificationPoll();
    }

    private synchronized void scheduleVerificationPoll() {
        ScheduledFuture<?> existing = verificationPollJob;
        if (existing != null) {
            existing.cancel(false);
        }
        long elapsed = System.currentTimeMillis() - lastCommandTimestamp;
        long remaining = AutoBlindBindingConstants.COMMAND_SUPPRESSION_MS - elapsed;
        long delaySec = Math.max(5, (remaining / 1000) + 2);
        verificationPollJob = scheduler.schedule(this::verificationPoll, delaySec, TimeUnit.SECONDS);
        logger.debug("Scheduled verification poll in {}s (suppression expires in {}ms)", delaySec, remaining);
    }

    private void verificationPoll() {
        if (notificationWatchRunning) {
            logger.debug("Verification poll skipped — notification watch still active");
            return;
        }
        logger.debug("Verification poll — clearing suppression and reading hub positions");
        clearAllSuppression();
        poll();
    }

    private void clearAllSuppression() {
        for (AutoBlindShadeHandler handler : shadeHandlers.values()) {
            handler.clearSuppression();
        }
    }

    private synchronized void startNotificationWatch() {
        if (notificationWatchRunning) {
            logger.debug("Notification watch already running, skipping restart");
            return;
        }
        stopNotificationWatch();
        long delayMs = AutoBlindBindingConstants.COMMAND_SPACING_MS + 200;
        notificationWatchJob = scheduler.schedule(this::notificationWatchLoop, delayMs, TimeUnit.MILLISECONDS);
        logger.debug("Notification watch scheduled in {}ms", delayMs);
    }

    private synchronized void stopNotificationWatch() {
        notificationWatchRunning = false;
        ScheduledFuture<?> job = notificationWatchJob;
        if (job != null) {
            job.cancel(true);
            notificationWatchJob = null;
        }
    }

    private void notificationWatchLoop() {
        notificationWatchRunning = true;
        long watermark = System.currentTimeMillis() / 1000;
        if (notificationWatermark > 0 && notificationWatermark >= watermark - 1) {
            watermark = notificationWatermark;
        }
        logger.debug("Notification watch started (watermark={})", watermark);

        try {
            while (notificationWatchRunning && hasPendingMotion()) {
                long elapsed = System.currentTimeMillis() - lastCommandTimestamp;
                if (elapsed > AutoBlindBindingConstants.NOTIFICATION_FAILSAFE_MS) {
                    logger.debug("Notification watch failsafe — {}ms since last command, clearing motion", elapsed);
                    clearAllMotion();
                    clearAllSuppression();
                    poll();
                    break;
                }

                AutoBlindApiClient client = apiClient;
                if (client == null) {
                    break;
                }

                String raw = client.notification(watermark, AutoBlindBindingConstants.NOTIFICATION_TIMEOUT_SEC);
                if (raw == null) {
                    continue;
                }

                List<NotificationEvent> events = parseNotificationChunks(raw);
                for (NotificationEvent event : events) {
                    if (event.timestamp > 0) {
                        long ts = event.timestamp;
                        if (ts > 10_000_000_000L) {
                            ts = ts / 1000;
                        }
                        watermark = ts;
                        notificationWatermark = ts;
                    }
                    if (!event.peripheralList.isEmpty()) {
                        logger.debug("Notification received for UIDs: {}", event.peripheralList);
                        poll();
                        if (!hasPendingMotion()) {
                            logger.debug("All shades settled via notification");
                            stopTrackingPoll();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Notification watch error: {}", e.getMessage());
        }

        notificationWatchRunning = false;
        logger.debug("Notification watch ended");
    }

    private boolean hasPendingMotion() {
        for (AutoBlindShadeHandler handler : shadeHandlers.values()) {
            if (handler.isInMotion()) {
                return true;
            }
        }
        return false;
    }

    private void clearAllMotion() {
        for (AutoBlindShadeHandler handler : shadeHandlers.values()) {
            handler.clearMotion();
        }
    }

    private List<NotificationEvent> parseNotificationChunks(String raw) {
        List<NotificationEvent> events = new ArrayList<>();
        String[] chunks = raw.trim().split("\\}\\s*\\{");
        for (int i = 0; i < chunks.length; i++) {
            String chunk = chunks[i];
            if (!chunk.startsWith("{")) {
                chunk = "{" + chunk;
            }
            if (!chunk.endsWith("}")) {
                chunk = chunk + "}";
            }
            try {
                JsonObject obj = JsonParser.parseString(chunk).getAsJsonObject();
                NotificationEvent event = new NotificationEvent();

                if (obj.has("Status")) {
                    event.timestamp = obj.get("Status").getAsLong();
                } else if (obj.has("Timestamp")) {
                    event.timestamp = obj.get("Timestamp").getAsLong();
                }

                if (obj.has("PeripheralList")) {
                    JsonArray arr = obj.getAsJsonArray("PeripheralList");
                    List<Integer> uids = new ArrayList<>();
                    for (JsonElement el : arr) {
                        uids.add(el.getAsInt());
                    }
                    event.peripheralList = uids;
                }

                events.add(event);
            } catch (Exception e) {
                logger.debug("Failed to parse notification chunk: {}", e.getMessage());
            }
        }
        return events;
    }

    public void registerShadeHandler(int peripheralUid, AutoBlindShadeHandler handler) {
        shadeHandlers.put(peripheralUid, handler);
    }

    public void unregisterShadeHandler(int peripheralUid) {
        shadeHandlers.remove(peripheralUid);
    }

    public @Nullable AutoBlindApiClient getApiClient() {
        return apiClient;
    }

    public @Nullable AllPeripheralResponse getAllPeripherals() {
        try {
            AutoBlindApiClient client = apiClient;
            if (client != null) {
                return client.getAllPeripherals();
            }
        } catch (Exception e) {
            logger.debug("Failed to get peripherals: {}", e.getMessage());
        }
        return null;
    }

    public Collection<AutoBlindShadeHandler> getShadeHandlers() {
        return shadeHandlers.values();
    }
}
