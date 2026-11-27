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
package org.openhab.binding.transit.internal.net.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PlanResult} is the response of the {@code plan} endpoint. Only the fields used by the binding are
 * mapped.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class PlanResult {
    public static final String LEG_MODE_TRANSIT = "transit";

    private @Nullable List<Itinerary> results;

    public List<Itinerary> getResults() {
        List<Itinerary> list = results;
        return list != null ? list : Collections.emptyList();
    }

    /**
     * Returns the itinerary that arrives first among all itineraries that do not start in the past, or {@code null}
     * if there is none.
     */
    public @Nullable Itinerary getEarliestArrival(long nowSeconds) {
        Itinerary best = null;
        for (Itinerary itinerary : getResults()) {
            Long start = itinerary.startTime;
            Long end = itinerary.endTime;
            if (start == null || end == null || start < nowSeconds) {
                continue;
            }
            Long bestEnd = best != null ? best.endTime : null;
            if (bestEnd == null || end < bestEnd) {
                best = itinerary;
            }
        }
        return best;
    }

    public static class Itinerary {
        // duration in seconds
        private @Nullable Long duration;

        @SerializedName("start_time")
        private @Nullable Long startTime;

        @SerializedName("end_time")
        private @Nullable Long endTime;

        private @Nullable List<Leg> legs;

        public @Nullable Long getDuration() {
            return duration;
        }

        public @Nullable Instant getStartTime() {
            Long time = startTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable Instant getEndTime() {
            Long time = endTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public List<Leg> getLegs() {
            List<Leg> list = legs;
            return list != null ? list : Collections.emptyList();
        }

        public List<Leg> getTransitLegs() {
            return getLegs().stream().filter(Leg::isTransit).toList();
        }
    }

    public static class Leg {
        @SerializedName("leg_mode")
        private @Nullable String legMode;

        private @Nullable List<Route> routes;

        private @Nullable List<Departure> departures;

        public @Nullable String getLegMode() {
            return legMode;
        }

        public boolean isTransit() {
            return LEG_MODE_TRANSIT.equals(legMode);
        }

        public List<Route> getRoutes() {
            List<Route> list = routes;
            return list != null ? list : Collections.emptyList();
        }

        public List<Departure> getDepartures() {
            List<Departure> list = departures;
            return list != null ? list : Collections.emptyList();
        }

        /**
         * The leg starts with the first departure of the list.
         */
        public @Nullable Departure getFirstDeparture() {
            List<Departure> list = getDepartures();
            return list.isEmpty() ? null : list.get(0);
        }

        /**
         * A transit leg can be served by several routes (e.g. U6, U7 and U12 on the same track). Returns the route of
         * the first departure, or the first route if the departure does not name its route.
         */
        public @Nullable Route getFirstDepartureRoute() {
            List<Route> list = getRoutes();
            if (list.isEmpty()) {
                return null;
            }
            Departure departure = getFirstDeparture();
            String routeId = departure != null ? departure.getGlobalRouteId() : null;
            if (routeId != null) {
                for (Route route : list) {
                    if (routeId.equals(route.globalRouteId)) {
                        return route;
                    }
                }
            }
            return list.get(0);
        }

        public @Nullable String getRouteShortName() {
            Route route = getFirstDepartureRoute();
            return route != null ? route.routeShortName : null;
        }
    }

    public static class Route {
        @SerializedName("route_short_name")
        private @Nullable String routeShortName;

        @SerializedName("global_route_id")
        private @Nullable String globalRouteId;

        public @Nullable String getRouteShortName() {
            return routeShortName;
        }

        public @Nullable String getGlobalRouteId() {
            return globalRouteId;
        }
    }

    public static class Departure {
        @SerializedName("departure_time")
        private @Nullable Long departureTime;

        @SerializedName("scheduled_departure_time")
        private @Nullable Long scheduledDepartureTime;

        @SerializedName("is_real_time")
        private @Nullable Boolean isRealTime;

        @SerializedName("is_cancelled")
        private @Nullable Boolean isCancelled;

        @SerializedName("trip_search_key")
        private @Nullable String tripSearchKey;

        @SerializedName("plan_details")
        private @Nullable PlanDetails planDetails;

        public @Nullable Instant getDepartureTime() {
            Long time = departureTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable Instant getScheduledDepartureTime() {
            Long time = scheduledDepartureTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable Boolean getIsRealTime() {
            return isRealTime;
        }

        public @Nullable Boolean getIsCancelled() {
            return isCancelled;
        }

        public @Nullable String getTripSearchKey() {
            return tripSearchKey;
        }

        public @Nullable String getGlobalRouteId() {
            PlanDetails details = planDetails;
            return details != null ? details.globalRouteId : null;
        }
    }

    public static class PlanDetails {
        @SerializedName("global_route_id")
        private @Nullable String globalRouteId;
    }
}
