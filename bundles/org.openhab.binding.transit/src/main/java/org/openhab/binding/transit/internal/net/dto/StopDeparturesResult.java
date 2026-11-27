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

@NonNullByDefault
public class StopDeparturesResult {

    @SerializedName("route_departures")
    private @Nullable List<RouteDeparture> routeDepartures;

    public List<RouteDeparture> getRouteDepartures() {
        List<RouteDeparture> list = routeDepartures;
        return list != null ? list : Collections.emptyList();
    }

    public static class RouteDeparture {
        @SerializedName("route_short_name")
        private @Nullable String routeShortName;

        @SerializedName("route_long_name")
        private @Nullable String routeLongName;

        @SerializedName("merged_itineraries")
        private @Nullable List<MergedItinerary> mergedItineraries;

        @SerializedName("global_stop_id")
        private @Nullable String globalStopId;

        @SerializedName("global_route_id")
        private @Nullable String globalRouteId;

        @SerializedName("route_color")
        private @Nullable String routeColor;

        @SerializedName("mode_key")
        private @Nullable String modeKey;

        @SerializedName("route_network_id")
        private @Nullable String routeNetworkId;

        private @Nullable List<ServiceAlert> alerts;

        public @Nullable String getRouteShortName() {
            return routeShortName;
        }

        public @Nullable String getRouteLongName() {
            return routeLongName;
        }

        public List<MergedItinerary> getMergedItineraries() {
            List<MergedItinerary> list = mergedItineraries;
            return list != null ? list : Collections.emptyList();
        }

        public @Nullable String getGlobalStopId() {
            return globalStopId;
        }

        public @Nullable String getGlobalRouteId() {
            return globalRouteId;
        }

        public @Nullable String getRouteColor() {
            return routeColor;
        }

        public @Nullable String getModeKey() {
            return modeKey;
        }

        public @Nullable String getRouteNetworkId() {
            return routeNetworkId;
        }

        public List<ServiceAlert> getAlerts() {
            List<ServiceAlert> list = alerts;
            return list != null ? list : Collections.emptyList();
        }
    }

    public static class MergedItinerary {
        @SerializedName("itineraries")
        private @Nullable List<Itinerary> itineraries;

        @SerializedName("schedule_items")
        private @Nullable List<ScheduleItem> scheduleItems;

        public List<Itinerary> getItineraries() {
            List<Itinerary> list = itineraries;
            return list != null ? list : Collections.emptyList();
        }

        public List<ScheduleItem> getScheduleItems() {
            List<ScheduleItem> list = scheduleItems;
            return list != null ? list : Collections.emptyList();
        }

        /**
         * Returns the headsign of the itinerary the given schedule item belongs to, or {@code null} if unknown.
         */
        public @Nullable String getHeadsign(ScheduleItem scheduleItem) {
            String itineraryId = scheduleItem.getInternalItineraryId();
            for (Itinerary itinerary : getItineraries()) {
                if (itineraryId == null || itineraryId.equals(itinerary.getInternalItineraryId())) {
                    return itinerary.getHeadsign();
                }
            }
            return null;
        }
    }

    public static class Itinerary {
        @SerializedName("internal_itinerary_id")
        private @Nullable String internalItineraryId;

        @SerializedName("headsign")
        private @Nullable String headsign;

        public @Nullable String getInternalItineraryId() {
            return internalItineraryId;
        }

        public @Nullable String getHeadsign() {
            return headsign;
        }
    }

    public static class ScheduleItem {
        @SerializedName("scheduled_departure_time")
        private @Nullable Long scheduledDepartureTime;

        @SerializedName("departure_time")
        private @Nullable Long departureTime;

        @SerializedName("scheduled_arrival_time")
        private @Nullable Long scheduledArrivalTime;

        @SerializedName("arrival_time")
        private @Nullable Long arrivalTime;

        @SerializedName("is_cancelled")
        private @Nullable Boolean isCancelled;

        @SerializedName("is_real_time")
        private @Nullable Boolean isRealTime;

        @SerializedName("is_last")
        private @Nullable Boolean isLast;

        @SerializedName("rt_trip_id")
        private @Nullable String rtTripId;

        @SerializedName("trip_search_key")
        private @Nullable String tripSearchKey;

        @SerializedName("internal_itinerary_id")
        private @Nullable String internalItineraryId;

        @SerializedName("wheelchair_accessible")
        private @Nullable Integer wheelchairAccessible;

        public @Nullable Instant getScheduledDepartureTime() {
            Long time = this.scheduledDepartureTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable Instant getDepartureTime() {
            Long time = this.departureTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable Instant getScheduledArrivalTime() {
            Long time = this.scheduledArrivalTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        public @Nullable Instant getArrivalTime() {
            Long time = this.arrivalTime;
            return time != null ? Instant.ofEpochSecond(time) : null;
        }

        // Return the cancellation status or null if unknown
        public @Nullable Boolean getIsCancelled() {
            return isCancelled;
        }

        // Return the real-time status or null if unknown
        public @Nullable Boolean getIsRealTime() {
            return isRealTime;
        }

        // Return whether this is the last departure of the day for this itinerary or null if unknown
        public @Nullable Boolean getIsLast() {
            return isLast;
        }

        public @Nullable String getRtTripId() {
            return rtTripId;
        }

        public @Nullable String getTripSearchKey() {
            return tripSearchKey;
        }

        public @Nullable String getInternalItineraryId() {
            return internalItineraryId;
        }

        public @Nullable Integer getWheelchairAccessible() {
            return wheelchairAccessible;
        }
    }
}
