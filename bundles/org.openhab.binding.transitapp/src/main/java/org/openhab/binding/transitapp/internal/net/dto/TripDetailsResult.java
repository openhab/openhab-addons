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

public class TripDetailsResult {
    public @Nullable Trip trip;
    public @Nullable Vehicle vehicle;
    public @Nullable List<Stop> stops;
    
    @SerializedName("trip_headsign") public @Nullable String tripHeadsignFlat;
    @SerializedName("route_short_name") public @Nullable String routeShortNameFlat;

    public static class Trip {
        @SerializedName("trip_headsign") public @Nullable String tripHeadsign;
        @SerializedName("route_short_name") public @Nullable String routeShortName;
    }
    public static class Vehicle {
        public @Nullable Location location;
    }
    public static class Location {
        public @Nullable Double lat;
        public @Nullable Double lon;
    }
    public static class Stop {
        @SerializedName("global_stop_id") public @Nullable String globalStopId;
        @SerializedName("departure_time") public @Nullable Long departureTime;
        @SerializedName("stop_name") public @Nullable String stopName;
    }
    
    public Trip getEffectiveTrip() {
        if (trip != null) {
            return trip;
        }
        Trip t = new Trip();
        t.tripHeadsign = tripHeadsignFlat;
        t.routeShortName = routeShortNameFlat;
        return t;
    }
}
