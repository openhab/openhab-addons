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
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.action.TransitActions;
import org.openhab.binding.transit.internal.config.TransitStopConfiguration;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult.MergedItinerary;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult.RouteDeparture;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult.ScheduleItem;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TransitStopHandler} shows the upcoming departures of one or more stops.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitStopHandler extends TransitPollingHandler<StopDeparturesResult> {

    private static final List<String> DEPARTURE_CHANNELS = List.of(CHANNEL_ROUTE_SHORT_NAME, CHANNEL_ROUTE_LONG_NAME,
            CHANNEL_DEPARTURE_TIME, CHANNEL_MINUTES_UNTIL_DEPARTURE, CHANNEL_DELAY_MINUTES,
            CHANNEL_WHEELCHAIR_ACCESSIBLE, CHANNEL_IS_CANCELLED, CHANNEL_HEADSIGN, CHANNEL_IS_REAL_TIME,
            CHANNEL_IS_LAST, CHANNEL_ROUTE_COLOR, CHANNEL_TRANSPORT_MODE, CHANNEL_GLOBAL_STOP_ID,
            CHANNEL_GLOBAL_ROUTE_ID, CHANNEL_TRIP_SEARCH_KEY);

    private final Map<String, String> latestLineDepartures = new ConcurrentHashMap<>();
    private TransitStopConfiguration config = new TransitStopConfiguration();
    private String globalStopIds = "";
    private int maxDepartures = MAX_CHANNEL_GROUPS;

    public TransitStopHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String loadConfiguration() {
        config = getConfigAs(TransitStopConfiguration.class);
        globalStopIds = normalizeList(config.globalStopId);
        if (globalStopIds.isEmpty()) {
            return "@text/offline.conf-error-no-stop-id";
        }
        return null;
    }

    @Override
    protected long getRefreshInterval() {
        return config.refreshInterval;
    }

    @Override
    protected StopDeparturesResult fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException {
        maxDepartures = bridgeHandler.getMaxDepartures();
        return bridgeHandler.getStopDepartures(globalStopIds);
    }

    @Override
    protected void publish(StopDeparturesResult result, long nowSeconds) {
        latestLineDepartures.clear();

        List<Departure> departures = flattenDepartures(result.getRouteDepartures(), nowSeconds);
        int groupIdx = 1;
        for (Departure departure : departures) {
            if (groupIdx > maxDepartures) {
                break;
            }
            updateDepartureState(groupIdx, departure, nowSeconds);
            groupIdx++;
        }
        clearRemainingDepartures(groupIdx);
    }

    @Override
    protected void onCommunicationError() {
        latestLineDepartures.clear();
        clearRemainingDepartures(1);
    }

    /**
     * A single departure of a route at one of the stops.
     */
    record Departure(Instant departureTime, RouteDeparture route, MergedItinerary itinerary, ScheduleItem schedule) {
    }

    /**
     * Collects all future departures of all routes and itineraries, sorted by departure time.
     */
    static List<Departure> flattenDepartures(List<RouteDeparture> routeDepartures, long nowSeconds) {
        List<Departure> allDepartures = new ArrayList<>();
        for (RouteDeparture routeDep : routeDepartures) {
            for (MergedItinerary mergedItinerary : routeDep.getMergedItineraries()) {
                for (ScheduleItem schedule : mergedItinerary.getScheduleItems()) {
                    Instant depTime = schedule.getDepartureTime();
                    if (depTime == null || depTime.getEpochSecond() <= nowSeconds) {
                        continue;
                    }
                    allDepartures.add(new Departure(depTime, routeDep, mergedItinerary, schedule));
                }
            }
        }
        allDepartures.sort(Comparator.comparing(Departure::departureTime));
        return allDepartures;
    }

    private void updateDepartureState(int groupIdx, Departure departure, long nowSeconds) {
        String group = CHANNEL_GROUP_DEPARTURE_PREFIX + groupIdx;
        RouteDeparture route = departure.route();
        ScheduleItem schedule = departure.schedule();
        Instant depTime = departure.departureTime();

        String shortName = route.getRouteShortName();
        updateGroupState(group, CHANNEL_ROUTE_SHORT_NAME, toStringState(shortName));
        updateGroupState(group, CHANNEL_ROUTE_LONG_NAME, toStringState(route.getRouteLongName()));

        long minutesUntilDeparture = (depTime.getEpochSecond() - nowSeconds) / 60;
        updateGroupState(group, CHANNEL_MINUTES_UNTIL_DEPARTURE,
                new QuantityType<>(minutesUntilDeparture, Units.MINUTE));
        updateGroupState(group, CHANNEL_DEPARTURE_TIME, toDateTimeState(depTime));

        if (shortName != null && !shortName.isEmpty() && !latestLineDepartures.containsKey(shortName)) {
            latestLineDepartures.put(shortName, LocalTime.ofInstant(depTime, ZoneId.systemDefault()).toString());
        }

        Instant scheduledDepTime = schedule.getScheduledDepartureTime();
        long delayMinutes = 0;
        if (scheduledDepTime != null) {
            long delaySeconds = depTime.getEpochSecond() - scheduledDepTime.getEpochSecond();
            delayMinutes = delaySeconds / 60;
        }
        updateGroupState(group, CHANNEL_DELAY_MINUTES, new QuantityType<>(delayMinutes, Units.MINUTE));

        updateGroupState(group, CHANNEL_WHEELCHAIR_ACCESSIBLE, toWheelchairState(schedule.getWheelchairAccessible()));
        updateGroupState(group, CHANNEL_IS_CANCELLED, toOnOffState(schedule.getIsCancelled()));
        updateGroupState(group, CHANNEL_HEADSIGN, toStringState(departure.itinerary().getHeadsign(schedule)));
        updateGroupState(group, CHANNEL_IS_REAL_TIME, toOnOffState(schedule.getIsRealTime()));
        updateGroupState(group, CHANNEL_IS_LAST, toOnOffState(schedule.getIsLast()));
        updateGroupState(group, CHANNEL_ROUTE_COLOR, toStringState(route.getRouteColor()));
        updateGroupState(group, CHANNEL_TRANSPORT_MODE, toStringState(route.getModeKey()));
        updateGroupState(group, CHANNEL_GLOBAL_STOP_ID, toStringState(route.getGlobalStopId()));
        updateGroupState(group, CHANNEL_GLOBAL_ROUTE_ID, toStringState(route.getGlobalRouteId()));
        updateGroupState(group, CHANNEL_TRIP_SEARCH_KEY, toStringState(schedule.getTripSearchKey()));
    }

    private void clearRemainingDepartures(int startIdx) {
        for (int i = startIdx; i <= MAX_CHANNEL_GROUPS; i++) {
            String group = CHANNEL_GROUP_DEPARTURE_PREFIX + i;
            for (String channel : DEPARTURE_CHANNELS) {
                updateGroupState(group, channel, UnDefType.UNDEF);
            }
        }
    }

    public String findNextDepartureByLine(String lineName) {
        return latestLineDepartures.getOrDefault(lineName, "N/A");
    }

    /**
     * @return the normalized, comma-separated list of the configured stop IDs
     */
    public String getGlobalStopIds() {
        return globalStopIds;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TransitActions.class);
    }
}
