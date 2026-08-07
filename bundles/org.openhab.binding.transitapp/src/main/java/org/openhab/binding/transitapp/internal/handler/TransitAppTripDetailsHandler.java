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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppTripDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppTripDetailsHandler.class);

    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppTripDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        var job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        var config = getConfigAs(TransitAppTripConfiguration.class);
        long refreshInterval = Math.max(30L, config.refreshInterval);
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.submit(this::pollTransitApi);
        }
    }

    private synchronized void pollTransitApi() {
        var config = getConfigAs(TransitAppTripConfiguration.class);
        String tripSearchKey = config.tripSearchKey;
        if (tripSearchKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Trip Search Key is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        try {
            var bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge handler not initialized");
                return;
            }
            var result = bridgeHandler.getTripDetails(tripSearchKey);
            logger.debug("Successfully polled trip details for trip search key {}", tripSearchKey);
            updateStatus(ThingStatus.ONLINE);

            var route = result.route;
            if (route != null) {
                var shortName = route.routeShortName;
                if (shortName != null) {
                    updateState("trip#route-short-name", new StringType(shortName));
                } else {
                    updateState("trip#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                }
            } else {
                updateState("trip#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
            }

            var targetStopId = config.targetStopId;
            long now = System.currentTimeMillis() / 1000;

            updateState("trip#time-to-target", org.openhab.core.types.UnDefType.UNDEF);

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
                            updateState("trip#time-to-target", new QuantityType<>(diff, Units.MINUTE));
                        }
                    }

                    if (stopIdx <= 10 && depTime != null && depTime > now) {
                        String prefix = "stop" + stopIdx + "#";
                        if (sName != null) {
                            updateState(prefix + "stop-name", new StringType(sName));
                        } else {
                            updateState(prefix + "stop-name", org.openhab.core.types.UnDefType.UNDEF);
                        }
                        long diff = (depTime - now) / 60;
                        updateState(prefix + "minutes-until-departure", new QuantityType<>(diff, Units.MINUTE));
                        stopIdx++;
                    }
                }
            }

            for (int i = stopIdx; i <= 10; i++) {
                String prefix = "stop" + i + "#";
                updateState(prefix + "stop-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "minutes-until-departure", org.openhab.core.types.UnDefType.UNDEF);
            }
        } catch (InterruptedException e) {
            // Preserve interrupt status for proper task cancellation
            Thread.currentThread().interrupt();
            logger.debug("Trip details polling task interrupted");
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            logger.warn("Communication error while polling trip {}: {}", tripSearchKey, errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
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
        var job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        super.dispose();
    }
}
