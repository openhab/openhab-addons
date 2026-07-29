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
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
public class TransitAppStopHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppStopHandler.class);

    private final java.util.Map<String, String> latestLineDepartures = new java.util.concurrent.ConcurrentHashMap<>();
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
        Object refreshIntervalObj = getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalObj instanceof Number ? ((Number) refreshIntervalObj).longValue() : 60L;
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            pollTransitApi();
        }
    }

    private void pollTransitApi() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        String globalStopId = (String) getThing().getConfiguration().get("globalStopId");
        if (globalStopId == null || globalStopId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Stop ID is missing");
            return;
        }

        String apiKey = (String) bridge.getConfiguration().get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key missing on bridge");
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = getTransitBridgeHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge handler not initialized");
                return;
            }
            StopDeparturesResult result = bridgeHandler.getApiClient().getStopDepartures(apiKey, globalStopId);
            logger.debug("Polling transit API for stop ID: {}", globalStopId);
            updateStatus(ThingStatus.ONLINE);

            int groupIdx = 1;
            long now = System.currentTimeMillis() / 1000;
            latestLineDepartures.clear();

            if (result.routeDepartures != null) {
                for (StopDeparturesResult.RouteDeparture routeDep : result.routeDepartures) {
                    if (groupIdx > 10)
                        break;

                    String shortName = routeDep.routeShortName;
                    String longName = routeDep.routeLongName;

                    if (routeDep.itineraries != null) {
                        for (StopDeparturesResult.Itinerary itinerary : routeDep.itineraries) {
                            if (groupIdx > 10)
                                break;
                            if (itinerary.scheduleItems != null) {
                                for (StopDeparturesResult.ScheduleItem schedule : itinerary.scheduleItems) {
                                    if (groupIdx > 10)
                                        break;

                                    String prefix = "depart" + groupIdx + "#";

                                    if (shortName != null)
                                        updateState(prefix + "route-short-name", new StringType(shortName));
                                    if (longName != null)
                                        updateState(prefix + "route-long-name", new StringType(longName));

                                    if (schedule.departureTime != null) {
                                        long depTime = schedule.departureTime;
                                        long diff = (depTime - now) / 60;
                                        if (diff < 0)
                                            continue;

                                        updateState(prefix + "minutes-until-departure",
                                                new QuantityType<>(diff, org.openhab.core.library.unit.Units.MINUTE));
                                        updateState(prefix + "departure-time",
                                                new org.openhab.core.library.types.DateTimeType(java.time.ZonedDateTime
                                                        .ofInstant(java.time.Instant.ofEpochSecond(depTime),
                                                                java.time.ZoneId.systemDefault())));
                                        if (shortName != null && !latestLineDepartures.containsKey(shortName)) {
                                            latestLineDepartures
                                                    .put(shortName,
                                                            java.time.LocalTime
                                                                    .ofInstant(java.time.Instant.ofEpochSecond(depTime),
                                                                            java.time.ZoneId.systemDefault())
                                                                    .toString());
                                        }
                                    } else {
                                        updateState(prefix + "departure-time", org.openhab.core.types.UnDefType.UNDEF);
                                    }

                                    if (schedule.delay != null)
                                        updateState(prefix + "delay-minutes", new QuantityType<>(schedule.delay / 60,
                                                org.openhab.core.library.unit.Units.MINUTE));
                                    else
                                        updateState(prefix + "delay-minutes", org.openhab.core.types.UnDefType.UNDEF);

                                    if (schedule.track != null)
                                        updateState(prefix + "platform", new StringType(schedule.track));
                                    else
                                        updateState(prefix + "platform", org.openhab.core.types.UnDefType.UNDEF);

                                    if (schedule.wheelchairAccessible != null)
                                        updateState(prefix + "wheelchair-accessible",
                                                schedule.wheelchairAccessible
                                                        ? org.openhab.core.library.types.OnOffType.ON
                                                        : org.openhab.core.library.types.OnOffType.OFF);
                                    else
                                        updateState(prefix + "wheelchair-accessible",
                                                org.openhab.core.types.UnDefType.UNDEF);

                                    if (schedule.occupancyStatus != null)
                                        updateState(prefix + "occupancy", new StringType(schedule.occupancyStatus));
                                    else
                                        updateState(prefix + "occupancy", org.openhab.core.types.UnDefType.UNDEF);

                                    if (schedule.isCancelled != null)
                                        updateState(prefix + "is-cancelled",
                                                schedule.isCancelled ? org.openhab.core.library.types.OnOffType.ON
                                                        : org.openhab.core.library.types.OnOffType.OFF);
                                    else
                                        updateState(prefix + "is-cancelled", org.openhab.core.types.UnDefType.UNDEF);

                                    groupIdx++;
                                }
                            }
                        }
                    }
                }
            }

            for (int i = groupIdx; i <= 10; i++) {
                String prefix = "depart" + i + "#";
                updateState(prefix + "route-short-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "route-long-name", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "departure-time", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "minutes-until-departure", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "delay-minutes", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "platform", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "wheelchair-accessible", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "occupancy", org.openhab.core.types.UnDefType.UNDEF);
                updateState(prefix + "is-cancelled", org.openhab.core.types.UnDefType.UNDEF);
            }
        } catch (Exception e) {
            logger.warn("Warning/Communication issue while polling stop {}: {}", globalStopId, e.getMessage());
            logger.error("Detailed API error for stop {}: {}", globalStopId, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
