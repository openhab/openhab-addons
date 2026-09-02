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
package org.openhab.binding.transitapp.internal.net.dto;

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

        @SerializedName("itineraries")
        private @Nullable List<Itinerary> itineraries;

        public @Nullable String getRouteShortName() {
            return routeShortName;
        }

        public @Nullable String getRouteLongName() {
            return routeLongName;
        }

        public List<Itinerary> getItineraries() {
            List<Itinerary> list = itineraries;
            return list != null ? list : Collections.emptyList();
        }
    }

    public static class Itinerary {
        @SerializedName("headsign")
        private @Nullable String headsign;

        @SerializedName("schedule_items")
        private @Nullable List<ScheduleItem> scheduleItems;

        public @Nullable String getHeadsign() {
            return headsign;
        }

        public List<ScheduleItem> getScheduleItems() {
            List<ScheduleItem> list = scheduleItems;
            return list != null ? list : Collections.emptyList();
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

        @SerializedName("rt_trip_id")
        private @Nullable String rtTripId;

        @SerializedName("trip_search_key")
        private @Nullable String tripSearchKey;

        @SerializedName("wheelchair_accessible")
        private @Nullable Integer wheelchairAccessible;

        @SerializedName("track")
        private @Nullable String track;

        @SerializedName("occupancy_status")
        private @Nullable String occupancyStatus;

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

        // Return the real-time status or null if unknown
        public @Nullable Boolean getIsCancelled() {
            return isCancelled;
        }

        // Rückgabetyp @Nullable Boolean
        public @Nullable Boolean getIsRealTime() {
            return isRealTime;
        }

        public @Nullable String getRtTripId() {
            return rtTripId;
        }

        public @Nullable String getTripSearchKey() {
            return tripSearchKey;
        }

        public @Nullable Integer getWheelchairAccessible() {
            return wheelchairAccessible;
        }

        public @Nullable String getTrack() {
            return track;
        }

        public @Nullable String getOccupancyStatus() {
            return occupancyStatus;
        }
    }
}
