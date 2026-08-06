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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.TransitAppBindingConstants;
import org.openhab.binding.transitapp.internal.config.TransitAppStopConfiguration;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.Itinerary;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.RouteDeparture;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult.ScheduleItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppStopHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppStopHandler.class);

    private final Map<String, String> latestLineDepartures = new ConcurrentHashMap<>();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppStopHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        TransitAppStopConfiguration config = getConfigAs(TransitAppStopConfiguration.class);
        long refreshInterval = Math.max(30L, config.refreshInterval);
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Initialized Transit Stop with refresh interval: {} seconds", refreshInterval);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.submit(this::pollTransitApi);
        }
    }

    private synchronized void pollTransitApi() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        TransitAppStopConfiguration config = getConfigAs(TransitAppStopConfiguration.class);
        String globalStopId = config.globalStopId;
        if (globalStopId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Stop ID is missing");
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge handler not initialized");
                return;
            }
            logger.debug("Polling transit API for stop ID: {}", globalStopId);
            StopDeparturesResult result = bridgeHandler.getStopDepartures(globalStopId);
            updateStatus(ThingStatus.ONLINE);

            long now = System.currentTimeMillis() / 1000;
            latestLineDepartures.clear();

            int groupIdx = processDepartures(result.routeDepartures, now);
            clearRemainingDepartures(groupIdx);
        } catch (Exception e) {
            latestLineDepartures.clear();
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            logger.error("Communication issue while polling stop: {}", errorMessage, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    private int processDepartures(@Nullable List<RouteDeparture> routeDepartures, long currentTimeSeconds) {
        int groupIdx = 1;
        TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
        int maxDepartures = bridgeHandler != null ? bridgeHandler.getMaxDepartures()
                : TransitAppBindingConstants.DEFAULT_MAX_DEPARTURES;

        // Cap maxDepartures to 10, matching the number of channel groups in the thing-type
        if (maxDepartures > 10) {
            maxDepartures = 10;
        }

        if (routeDepartures == null) {
            return groupIdx;
        }

        for (RouteDeparture routeDep : routeDepartures) {
            if (groupIdx > maxDepartures) {
                break;
            }

            String shortName = routeDep.routeShortName != null ? routeDep.routeShortName : "";
            String longName = routeDep.routeLongName != null ? routeDep.routeLongName : "";
            List<Itinerary> itineraries = routeDep.itineraries;

            if (itineraries == null) {
                continue;
            }

            for (Itinerary itinerary : itineraries) {
                if (groupIdx > maxDepartures) {
                    break;
                }

                List<ScheduleItem> schedules = itinerary.scheduleItems;
                if (schedules == null) {
                    continue;
                }

                for (ScheduleItem schedule : schedules) {
                    if (groupIdx > maxDepartures) {
                        break;
                    }

                    Long depTime = schedule.departureTime;
                    if (depTime == null || depTime < currentTimeSeconds) {
                        continue;
                    }

                    long minutesUntilDeparture = (depTime - currentTimeSeconds) / 60;
                    if (minutesUntilDeparture < 0) {
                        continue;
                    }

                    // Inline null-safe ternary to ensure non-null strings for JDT analysis
                    updateDepartureState(groupIdx, shortName != null ? shortName : "", longName != null ? longName : "",
                            depTime, minutesUntilDeparture, schedule);
                    groupIdx++;
                }
            }
        }

        return groupIdx;
    }

    private void updateDepartureState(int groupIdx, String shortName, String longName, long depTime,
            long minutesUntilDeparture, ScheduleItem schedule) {
        String prefix = "depart" + groupIdx + "#";

        updateState(prefix + "route-short-name", shortName.isEmpty() ? UnDefType.UNDEF : new StringType(shortName));
        updateState(prefix + "route-long-name", longName.isEmpty() ? UnDefType.UNDEF : new StringType(longName));
        updateState(prefix + "minutes-until-departure", new QuantityType<>(minutesUntilDeparture, Units.MINUTE));
        updateState(prefix + "departure-time",
                new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(depTime), ZoneId.systemDefault())));

        if (!shortName.isEmpty() && !latestLineDepartures.containsKey(shortName)) {
            latestLineDepartures.put(shortName,
                    LocalTime.ofInstant(Instant.ofEpochSecond(depTime), ZoneId.systemDefault()).toString());
        }

        Long delay = schedule.delay;
        updateState(prefix + "delay-minutes",
                delay != null ? new QuantityType<>(delay / 60, Units.MINUTE) : UnDefType.UNDEF);

        String track = schedule.track;
        updateState(prefix + "platform", track != null ? new StringType(track) : UnDefType.UNDEF);

        Boolean wheelchair = schedule.wheelchairAccessible;
        updateState(prefix + "wheelchair-accessible",
                wheelchair != null ? (wheelchair ? OnOffType.ON : OnOffType.OFF) : UnDefType.UNDEF);

        String occupancy = schedule.occupancyStatus;
        updateState(prefix + "occupancy", occupancy != null ? new StringType(occupancy) : UnDefType.UNDEF);

        Boolean isCancelled = schedule.isCancelled;
        updateState(prefix + "is-cancelled",
                isCancelled != null ? (isCancelled ? OnOffType.ON : OnOffType.OFF) : UnDefType.UNDEF);
    }

    private void clearRemainingDepartures(int startIdx) {
        // Always clear up to depart10 to ensure stale values are removed
        // even if maxDepartures was reduced or fewer departures are returned
        final int TOTAL_DEPARTURE_GROUPS = 10;

        for (int i = startIdx; i <= TOTAL_DEPARTURE_GROUPS; i++) {
            String prefix = "depart" + i + "#";
            updateState(prefix + "route-short-name", UnDefType.UNDEF);
            updateState(prefix + "route-long-name", UnDefType.UNDEF);
            updateState(prefix + "departure-time", UnDefType.UNDEF);
            updateState(prefix + "minutes-until-departure", UnDefType.UNDEF);
            updateState(prefix + "delay-minutes", UnDefType.UNDEF);
            updateState(prefix + "platform", UnDefType.UNDEF);
            updateState(prefix + "wheelchair-accessible", UnDefType.UNDEF);
            updateState(prefix + "occupancy", UnDefType.UNDEF);
            updateState(prefix + "is-cancelled", UnDefType.UNDEF);
        }
    }

    public String findNextDepartureByLine(String lineName) {
        return latestLineDepartures.getOrDefault(lineName, "N/A");
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
