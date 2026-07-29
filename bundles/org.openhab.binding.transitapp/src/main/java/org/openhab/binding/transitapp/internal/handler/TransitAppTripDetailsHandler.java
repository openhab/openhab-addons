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

import java.net.URI;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
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
import org.openhab.binding.transitapp.internal.net.dto.TripDetailsResult;
import org.openhab.binding.transitapp.internal.config.TransitAppTripConfiguration;

@NonNullByDefault
public class TransitAppTripDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppTripDetailsHandler.class);
    
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppTripDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        TransitAppTripConfiguration config = getConfigAs(TransitAppTripConfiguration.class);
        long refreshInterval = Math.max(30L, config.refreshInterval);
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            pollTransitApi();
        }
    }

    private synchronized void pollTransitApi() {
        TransitAppTripConfiguration config = getConfigAs(TransitAppTripConfiguration.class);
        String tripId = config.tripId;
        if (tripId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Trip ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge handler not initialized");
                return;
            }
            TripDetailsResult result = bridgeHandler.getTripDetails(tripId);
            logger.debug("Successfully polled trip details for trip ID {}", tripId);
            updateStatus(ThingStatus.ONLINE);

            TripDetailsResult.Trip trip = result.getEffectiveTrip();
            if (trip != null) {
                String headsign = trip.tripHeadsign;
                if (headsign != null) {
                    updateState("trip#trip-headsign", new StringType(headsign));
                } else {
                    updateState("trip#trip-headsign", org.openhab.core.types.UnDefType.UNDEF);
                }
                String shortName = trip.routeShortName;
                if (shortName != null) {
                    updateState("trip#route-short-name", new StringType(shortName));
                } else {
                    updateState("trip#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                }
            } else {
                updateState("trip#trip-headsign", org.openhab.core.types.UnDefType.UNDEF);
                updateState("trip#route-short-name", org.openhab.core.types.UnDefType.UNDEF);
            }

            Double lat = null;
            Double lon = null;
            TripDetailsResult.Vehicle v = result.vehicle;
            if (v != null) {
                TripDetailsResult.Location loc = v.location;
                if (loc != null) {
                    lat = loc.lat;
                    lon = loc.lon;
                }
            }

            if (lat != null && lon != null) {
                updateState("trip#location", new PointType(new DecimalType(lat), new DecimalType(lon)));
            } else {
                updateState("trip#location", org.openhab.core.types.UnDefType.UNDEF);
            }

            String targetStopId = config.targetStopId;
            long now = System.currentTimeMillis() / 1000;

            updateState("trip#time-to-target", org.openhab.core.types.UnDefType.UNDEF);

            int stopIdx = 1;
            if (result.stops != null) {
                for (TripDetailsResult.Stop stop : result.stops) {
                    String gStopId = stop.globalStopId;
                    Long depTime = stop.departureTime;
                    String sName = stop.stopName;

                    if (targetStopId != null && !targetStopId.isBlank() && targetStopId.equals(gStopId) && depTime != null) {
                        long diff = (depTime - now) / 60;
                        if (diff >= 0) {
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
        } catch (Exception e) {
            logger.warn("Communication error while polling trip {}: {}", tripId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        super.dispose();
    }
}
