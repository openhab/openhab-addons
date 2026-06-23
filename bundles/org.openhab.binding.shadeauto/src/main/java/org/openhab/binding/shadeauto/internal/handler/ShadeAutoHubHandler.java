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
package org.openhab.binding.shadeauto.internal.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shadeauto.internal.ShadeAutoBindingConstants;
import org.openhab.binding.shadeauto.internal.api.ShadeAutoApiClient;
import org.openhab.binding.shadeauto.internal.api.dto.AllPeripheralResponse;
import org.openhab.binding.shadeauto.internal.api.dto.PeripheralStatus;
import org.openhab.binding.shadeauto.internal.api.dto.RegistrationResponse;
import org.openhab.binding.shadeauto.internal.api.dto.StatusResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler for the ShadeAuto Hub. Manages API communication and polling.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class ShadeAutoHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ShadeAutoHubHandler.class);
    private final HttpClient httpClient;
    private final Map<Integer, ShadeAutoShadeHandler> shadeHandlers = new ConcurrentHashMap<>();

    private @Nullable ShadeAutoApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> refreshPollJob;
    private @Nullable ScheduledFuture<?> trackingPollJob;
    private @Nullable ScheduledFuture<?> trackingTimeoutJob;
    private @Nullable ScheduledFuture<?> verificationPollJob;
    private volatile Map<Integer, Integer> previousPositions = Map.of();
    private volatile long lastCommandTimestamp = 0;

    public ShadeAutoHubHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        Object hostObj = getConfig().get(ShadeAutoBindingConstants.CONFIG_HOST);
        if (hostObj == null || hostObj.toString().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host not configured");
            return;
        }
        String host = hostObj.toString();

        Object intervalObj = getConfig().get(ShadeAutoBindingConstants.CONFIG_POLLING_INTERVAL);
        int pollingInterval = intervalObj instanceof Number n ? n.intValue() : 1800;

        apiClient = new ShadeAutoApiClient(httpClient, host);

        scheduler.execute(() -> {
            try {
                ShadeAutoApiClient client = apiClient;
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
        ScheduledFuture<?> verification = verificationPollJob;
        if (verification != null) {
            verification.cancel(false);
            verificationPollJob = null;
        }
        apiClient = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ShadeAutoBindingConstants.CHANNEL_FORCE_REFRESH.equals(channelUID.getId())
                && OnOffType.ON.equals(command)) {
            logger.debug("Force refresh requested — clearing suppression and polling");
            clearAllSuppression();
            scheduler.execute(this::poll);
            updateState(ShadeAutoBindingConstants.CHANNEL_FORCE_REFRESH, OnOffType.OFF);
        }
    }

    private void poll() {
        try {
            ShadeAutoApiClient client = apiClient;
            if (client == null) {
                return;
            }
            StatusResponse status = client.getStatus();
            if (status.error != 0) {
                logger.debug("Hub status poll returned error {}", status.error);
                return;
            }

            for (PeripheralStatus peripheral : status.peripherals) {
                ShadeAutoShadeHandler handler = shadeHandlers.get(peripheral.peripheralUid);
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
            ShadeAutoApiClient client = apiClient;
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
                ShadeAutoShadeHandler handler = shadeHandlers.get(peripheral.peripheralUid);
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
        long remaining = ShadeAutoBindingConstants.COMMAND_SUPPRESSION_MS - elapsed;
        long delaySec = Math.max(5, (remaining / 1000) + 2);
        verificationPollJob = scheduler.schedule(this::verificationPoll, delaySec, TimeUnit.SECONDS);
        logger.debug("Scheduled verification poll in {}s (suppression expires in {}ms)", delaySec, remaining);
    }

    private void verificationPoll() {
        logger.debug("Verification poll — clearing suppression and reading hub positions");
        clearAllSuppression();
        poll();
    }

    private void clearAllSuppression() {
        for (ShadeAutoShadeHandler handler : shadeHandlers.values()) {
            handler.clearSuppression();
        }
    }

    public void registerShadeHandler(int peripheralUid, ShadeAutoShadeHandler handler) {
        shadeHandlers.put(peripheralUid, handler);
    }

    public void unregisterShadeHandler(int peripheralUid) {
        shadeHandlers.remove(peripheralUid);
    }

    public @Nullable ShadeAutoApiClient getApiClient() {
        return apiClient;
    }

    public @Nullable AllPeripheralResponse getAllPeripherals() {
        try {
            ShadeAutoApiClient client = apiClient;
            if (client != null) {
                return client.getAllPeripherals();
            }
        } catch (Exception e) {
            logger.debug("Failed to get peripherals: {}", e.getMessage());
        }
        return null;
    }

    public Collection<ShadeAutoShadeHandler> getShadeHandlers() {
        return shadeHandlers.values();
    }
}
