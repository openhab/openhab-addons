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

import java.util.Collection;
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
    private @Nullable ScheduledFuture<?> motionPollJob;
    private volatile long lastCommandTimestamp = 0;
    private volatile boolean motionPollRunning;

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
        stopMotionPoll();
        apiClient = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (AutoBlindBindingConstants.CHANNEL_FORCE_REFRESH.equals(channelUID.getId())
                && OnOffType.ON.equals(command)) {
            logger.debug("Force refresh requested — clearing motion/suppression and polling");
            clearAllMotion();
            clearAllSuppression();
            stopMotionPoll();
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

    private void clearAllSuppression() {
        for (AutoBlindShadeHandler handler : shadeHandlers.values()) {
            handler.clearSuppression();
        }
    }

    /**
     * Starts polling the hub every {@link AutoBlindBindingConstants#MOTION_POLL_INTERVAL_MS} while any shade is
     * in motion, so each shade's own settlement check (see {@link AutoBlindShadeHandler#checkSettlement}) gets
     * fresh data promptly instead of waiting for the next scheduled long-interval poll.
     */
    public synchronized void startMotionPoll() {
        lastCommandTimestamp = System.currentTimeMillis();
        if (motionPollRunning) {
            logger.debug("Motion poll already running, skipping restart");
            return;
        }
        stopMotionPoll();
        long delayMs = AutoBlindBindingConstants.COMMAND_SPACING_MS + 200;
        motionPollJob = scheduler.schedule(this::motionPollLoop, delayMs, TimeUnit.MILLISECONDS);
        logger.debug("Motion poll scheduled in {}ms", delayMs);
    }

    private synchronized void stopMotionPoll() {
        motionPollRunning = false;
        ScheduledFuture<?> job = motionPollJob;
        if (job != null) {
            job.cancel(true);
            motionPollJob = null;
        }
    }

    private void motionPollLoop() {
        motionPollRunning = true;
        logger.debug("Motion poll started");

        try {
            while (motionPollRunning && hasPendingMotion()) {
                long elapsed = System.currentTimeMillis() - lastCommandTimestamp;
                if (elapsed > AutoBlindBindingConstants.MOTION_FAILSAFE_MS) {
                    // Give up on active polling, but keep showing the last commanded position — the hub's
                    // own status reporting hasn't been reliable enough to trust automatically. Only a real
                    // settlement match or an explicit force-refresh should overwrite it from here.
                    logger.debug("Motion poll failsafe — {}ms since last command, stopping poll (suppression kept)",
                            elapsed);
                    clearAllMotion();
                    break;
                }

                poll();
                if (!hasPendingMotion()) {
                    logger.debug("All shades settled");
                    break;
                }

                Thread.sleep(AutoBlindBindingConstants.MOTION_POLL_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.debug("Motion poll error: {}", e.getMessage());
        }

        motionPollRunning = false;
        logger.debug("Motion poll ended");
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
