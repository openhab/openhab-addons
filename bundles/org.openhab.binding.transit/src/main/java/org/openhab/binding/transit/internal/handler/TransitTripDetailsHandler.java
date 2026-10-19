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
package org.openhab.binding.transit.internal.handler;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitTripConfiguration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitTripDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitTripDetailsHandler.class);

    private TransitTripConfiguration config = new TransitTripConfiguration();
    private final AtomicBoolean pollInProgress = new AtomicBoolean();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitTripDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TransitTripConfiguration.class);
        if (config.tripSearchKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-trip-search-key");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            startPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        synchronized (this) {
            if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Bridge is ONLINE, starting polling for {}", getThing().getUID());
                updateStatus(ThingStatus.UNKNOWN);
                startPolling();
            } else {
                logger.debug("Bridge is {}, stopping polling for {}", bridgeStatusInfo.getStatus(),
                        getThing().getUID());
                stopPolling();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    private void startPolling() {
        synchronized (this) {
            ScheduledFuture<?> job = refreshJob;
            if (job == null || job.isCancelled()) {
                long refreshInterval = Math.max(30L, config.refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void stopPolling() {
        synchronized (this) {
            ScheduledFuture<?> job = refreshJob;
            if (job != null && !job.isCancelled()) {
                job.cancel(true);
                refreshJob = null;
            }
        }
    }

    /**
     * Must be called while holding the handler lock. Returns {@code false} if polling was stopped or the bridge went
     * offline while a request was in flight, so that outdated results are not published.
     */
    private boolean isPollingActive() {
        Bridge bridge = getBridge();
        return refreshJob != null && bridge != null && bridge.getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::pollTransitApi);
        }
    }

    private void pollTransitApi() {
        // skip if the previous poll (scheduled or triggered by REFRESH) is still running
        if (!pollInProgress.compareAndSet(false, true)) {
            logger.debug("Previous poll still running, skipping this one for {}", getThing().getUID());
            return;
        }
        try {
            poll();
        } finally {
            pollInProgress.set(false);
        }
    }

    /**
     * Requests the data without holding the handler lock and publishes it while holding the handler lock, the same
     * lock that {@link #bridgeStatusChanged} and {@link #updateStatus} use, so no lock-order inversion is possible.
     */
    private void poll() {
        String tripSearchKey = config.tripSearchKey;

        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not ONLINE. Skipping poll for {}", getThing().getUID());
            return;
        }

        try {
            var bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                return;
            }
            var result = bridgeHandler.getTripDetails(tripSearchKey);
            synchronized (this) {
                if (!isPollingActive()) {
                    logger.debug("Polling stopped while request was in flight, discarding result for {}",
                            getThing().getUID());
                    return;
                }
                logger.debug("Successfully polled trip details for trip search key {}", tripSearchKey);
                updateStatus(ThingStatus.ONLINE);

                var route = result.route;
                var shortName = route != null ? route.routeShortName : null;
                updateGroupState(CHANNEL_GROUP_TRIP, CHANNEL_ROUTE_SHORT_NAME,
                        shortName != null ? new StringType(shortName) : UnDefType.UNDEF);

                var targetStopId = config.targetStopId;
                long now = System.currentTimeMillis() / 1000;

                updateGroupState(CHANNEL_GROUP_TRIP, CHANNEL_TIME_TO_TARGET, UnDefType.UNDEF);

                int stopIdx = 1;
                var scheduleItems = result.scheduleItems;
                if (scheduleItems != null) {
                    for (var schedule : scheduleItems) {
                        var stop = schedule.stop;
                        if (stop == null) {
                            continue;
                        }

                        var gStopId = stop.globalStopId;
                        var depTime = schedule.departureTime;
                        var sName = stop.stopName;

                        if (targetStopId != null && !targetStopId.isBlank() && targetStopId.equals(gStopId)
                                && depTime != null) {
                            // Only show countdown for future departures
                            if (depTime >= now) {
                                long diff = (depTime - now) / 60;
                                updateGroupState(CHANNEL_GROUP_TRIP, CHANNEL_TIME_TO_TARGET,
                                        new QuantityType<>(diff, Units.MINUTE));
                            }
                        }

                        if (stopIdx <= MAX_CHANNEL_GROUPS && depTime != null && depTime > now) {
                            String group = CHANNEL_GROUP_STOP_PREFIX + stopIdx;
                            updateGroupState(group, CHANNEL_STOP_NAME,
                                    sName != null ? new StringType(sName) : UnDefType.UNDEF);
                            long diff = (depTime - now) / 60;
                            updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE,
                                    new QuantityType<>(diff, Units.MINUTE));
                            stopIdx++;
                        }
                    }
                }

                for (int i = stopIdx; i <= MAX_CHANNEL_GROUPS; i++) {
                    String group = CHANNEL_GROUP_STOP_PREFIX + i;
                    updateGroupState(group, CHANNEL_STOP_NAME, UnDefType.UNDEF);
                    updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE, UnDefType.UNDEF);
                }
            }
        } catch (InterruptedException e) {
            // Preserve interrupt status for proper task cancellation
            Thread.currentThread().interrupt();
            logger.debug("Trip details polling task interrupted");
        } catch (IOException e) {
            synchronized (this) {
                if (!isPollingActive()) {
                    return;
                }
                String errorMessage = e.getMessage();
                logger.debug("Communication error while polling trip {}: {}", tripSearchKey, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    private void updateGroupState(String group, String channel, State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state);
    }

    public @Nullable TransitBridgeHandler getTransitBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof TransitBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        return null;
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }
}
