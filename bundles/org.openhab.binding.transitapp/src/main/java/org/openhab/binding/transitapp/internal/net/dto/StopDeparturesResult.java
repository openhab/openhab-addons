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

import java.util.List;
import com.google.gson.annotations.SerializedName;
import org.eclipse.jdt.annotation.Nullable;

public class StopDeparturesResult {
    @SerializedName("route_departures") public @Nullable List<RouteDeparture> routeDepartures;

    public static class RouteDeparture {
        @SerializedName("route_short_name") public @Nullable String routeShortName;
        @SerializedName("route_long_name") public @Nullable String routeLongName;
        public @Nullable List<Itinerary> itineraries;
    }
    public static class Itinerary {
        @SerializedName("schedule_items") public @Nullable List<ScheduleItem> scheduleItems;
    }
    public static class ScheduleItem {
        @SerializedName("departure_time") public @Nullable Long departureTime;
        public @Nullable Long delay;
        public @Nullable String track;
        @SerializedName("wheelchair_accessible") public @Nullable Boolean wheelchairAccessible;
        @SerializedName("occupancy_status") public @Nullable String occupancyStatus;
        @SerializedName("is_cancelled") public @Nullable Boolean isCancelled;
    }
}
