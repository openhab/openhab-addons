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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class TripDetailsResult {
    public @Nullable Route route;

    @SerializedName("rt_trip_id")
    public @Nullable String rtTripId;

    @SerializedName("schedule_items")
    public @Nullable List<ScheduleItem> scheduleItems;

    public static class Route {
        @SerializedName("route_short_name")
        public @Nullable String routeShortName;

        @SerializedName("route_long_name")
        public @Nullable String routeLongName;

        @SerializedName("route_color")
        public @Nullable String routeColor;
    }

    public static class ScheduleItem {
        @SerializedName("departure_time")
        public @Nullable Long departureTime;

        public @Nullable Stop stop;
    }

    public static class Stop {
        @SerializedName("global_stop_id")
        public @Nullable String globalStopId;

        @SerializedName("stop_name")
        public @Nullable String stopName;
    }
}
