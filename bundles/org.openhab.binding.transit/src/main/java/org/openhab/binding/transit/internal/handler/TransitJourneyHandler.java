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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitJourneyConfiguration;
import org.openhab.binding.transit.internal.net.TransitPlace;
import org.openhab.binding.transit.internal.net.dto.PlanResult;
import org.openhab.binding.transit.internal.net.dto.PlanResult.Departure;
import org.openhab.binding.transit.internal.net.dto.PlanResult.Itinerary;
import org.openhab.binding.transit.internal.net.dto.PlanResult.Leg;
import org.openhab.binding.transit.internal.net.dto.PlanResult.Route;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TransitJourneyHandler} plans a trip between two places and shows the itinerary that arrives first.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitJourneyHandler extends TransitPollingHandler<PlanResult> {

    private static final List<String> JOURNEY_CHANNELS = List.of(CHANNEL_DEPARTURE_TIME, CHANNEL_ARRIVAL_TIME,
            CHANNEL_DURATION, CHANNEL_MINUTES_UNTIL_DEPARTURE, CHANNEL_TRANSFERS, CHANNEL_LINES,
            CHANNEL_ROUTE_SHORT_NAME, CHANNEL_GLOBAL_ROUTE_ID, CHANNEL_DELAY_MINUTES, CHANNEL_IS_REAL_TIME,
            CHANNEL_IS_CANCELLED, CHANNEL_TRIP_SEARCH_KEY);

    private TransitJourneyConfiguration config = new TransitJourneyConfiguration();
    private TransitPlace from = TransitPlace.ofLocation(0, 0);
    private TransitPlace to = TransitPlace.ofLocation(0, 0);

    public TransitJourneyHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String loadConfiguration() {
        config = getConfigAs(TransitJourneyConfiguration.class);
        TransitPlace fromPlace = toPlace(config.fromStopId, config.fromLocation);
        TransitPlace toPlace = toPlace(config.toStopId, config.toLocation);
        if (fromPlace == null) {
            return "@text/offline.conf-error-no-from";
        }
        if (toPlace == null) {
            return "@text/offline.conf-error-no-to";
        }
        from = fromPlace;
        to = toPlace;
        return null;
    }

    /**
     * The stop ID has priority over the location. Returns {@code null} if neither is configured or the location is
     * invalid.
     */
    static @Nullable TransitPlace toPlace(String stopId, String location) {
        if (!stopId.isBlank()) {
            return TransitPlace.ofStop(stopId.trim());
        }
        if (location.isBlank()) {
            return null;
        }
        try {
            PointType point = new PointType(location.trim());
            return TransitPlace.ofLocation(point.getLatitude().doubleValue(), point.getLongitude().doubleValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected long getRefreshInterval() {
        return config.refreshInterval;
    }

    @Override
    protected PlanResult fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException {
        return bridgeHandler.getPlan(from, to);
    }

    @Override
    protected void publish(PlanResult result, long nowSeconds) {
        Itinerary itinerary = result.getEarliestArrival(nowSeconds);
        if (itinerary == null) {
            logger.debug("No upcoming itinerary found for {}", getThing().getUID());
            JOURNEY_CHANNELS.forEach(channel -> updateGroupState(CHANNEL_GROUP_JOURNEY, channel, UnDefType.UNDEF));
            return;
        }

        Instant start = itinerary.getStartTime();
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_DEPARTURE_TIME, toDateTimeState(start));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_ARRIVAL_TIME, toDateTimeState(itinerary.getEndTime()));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_MINUTES_UNTIL_DEPARTURE,
                start != null ? new QuantityType<>((start.getEpochSecond() - nowSeconds) / 60, Units.MINUTE)
                        : UnDefType.UNDEF);
        Long duration = itinerary.getDuration();
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_DURATION,
                duration != null ? new QuantityType<>(Math.round(duration / 60.0), Units.MINUTE) : UnDefType.UNDEF);

        List<Leg> transitLegs = itinerary.getTransitLegs();
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_TRANSFERS,
                new DecimalType(Math.max(0, transitLegs.size() - 1)));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_LINES, toStringState(transitLegs.stream()
                .map(Leg::getRouteShortName).filter(Objects::nonNull).collect(Collectors.joining(", "))));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_IS_CANCELLED, getCancelledState(transitLegs));

        Leg firstLeg = transitLegs.isEmpty() ? null : transitLegs.get(0);
        Departure firstDeparture = firstLeg != null ? firstLeg.getFirstDeparture() : null;
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_ROUTE_SHORT_NAME,
                toStringState(firstLeg != null ? firstLeg.getRouteShortName() : null));
        Route firstRoute = firstLeg != null ? firstLeg.getFirstDepartureRoute() : null;
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_GLOBAL_ROUTE_ID,
                toStringState(firstRoute != null ? firstRoute.getGlobalRouteId() : null));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_IS_REAL_TIME,
                toOnOffState(firstDeparture != null ? firstDeparture.getIsRealTime() : null));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_TRIP_SEARCH_KEY,
                toStringState(firstDeparture != null ? firstDeparture.getTripSearchKey() : null));
        updateGroupState(CHANNEL_GROUP_JOURNEY, CHANNEL_DELAY_MINUTES, getDelayState(firstDeparture));
    }

    @Override
    protected void onCommunicationError() {
        JOURNEY_CHANNELS.forEach(channel -> updateGroupState(CHANNEL_GROUP_JOURNEY, channel, UnDefType.UNDEF));
    }

    private static State getDelayState(@Nullable Departure departure) {
        if (departure == null) {
            return UnDefType.UNDEF;
        }
        Instant actual = departure.getDepartureTime();
        Instant scheduled = departure.getScheduledDepartureTime();
        if (actual == null || scheduled == null) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<>((actual.getEpochSecond() - scheduled.getEpochSecond()) / 60, Units.MINUTE);
    }

    /**
     * {@code ON} if one of the transit legs is cancelled, {@code OFF} if none is, {@code UNDEF} if unknown.
     */
    static State getCancelledState(List<Leg> transitLegs) {
        boolean unknown = false;
        for (Leg leg : transitLegs) {
            Departure departure = leg.getFirstDeparture();
            Boolean cancelled = departure != null ? departure.getIsCancelled() : null;
            if (cancelled == null) {
                unknown = true;
            } else if (cancelled) {
                return OnOffType.ON;
            }
        }
        return unknown ? UnDefType.UNDEF : OnOffType.OFF;
    }
}
