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
package org.openhab.binding.transitapp.internal.handler;

import static org.openhab.binding.transitapp.internal.TransitAppBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.config.TransitAppTripConfiguration;
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
public class TransitAppTripDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppTripDetailsHandler.class);

    private TransitAppTripConfiguration config = new TransitAppTripConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppTripDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TransitAppTripConfiguration.class);
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
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            logger.debug("Bridge is ONLINE, starting polling for {}", getThing().getUID());
            updateStatus(ThingStatus.UNKNOWN);
            startPolling();
        } else {
            logger.debug("Bridge is {}, stopping polling for {}", bridgeStatusInfo.getStatus(), getThing().getUID());
            stopPolling();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void startPolling() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            long refreshInterval = Math.max(30L, config.refreshInterval);
            refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void stopPolling() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::pollTransitApi);
        }
    }

    private synchronized void pollTransitApi() {
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
        } catch (InterruptedException e) {
            // Preserve interrupt status for proper task cancellation
            Thread.currentThread().interrupt();
            logger.debug("Trip details polling task interrupted");
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            logger.debug("Communication error while polling trip {}: {}", tripSearchKey, errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    private void updateGroupState(String group, String channel, State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state);
    }

    public @Nullable TransitAppBridgeHandler getTransitBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof TransitAppBridgeHandler bridgeHandler) {
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
