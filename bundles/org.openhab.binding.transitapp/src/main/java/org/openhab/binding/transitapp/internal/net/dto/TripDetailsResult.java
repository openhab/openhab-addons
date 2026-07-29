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

public class TripDetailsResult {
    public Trip trip;
    public Vehicle vehicle;
    public List<Stop> stops;

    @SerializedName("trip_headsign")
    public String tripHeadsignFlat;
    @SerializedName("route_short_name")
    public String routeShortNameFlat;

    public static class Trip {
        @SerializedName("trip_headsign")
        public String tripHeadsign;
        @SerializedName("route_short_name")
        public String routeShortName;
    }

    public static class Vehicle {
        public Location location;
    }

    public static class Location {
        public Double lat;
        public Double lon;
    }

    public static class Stop {
        @SerializedName("global_stop_id")
        public String globalStopId;
        @SerializedName("departure_time")
        public Long departureTime;
        @SerializedName("stop_name")
        public String stopName;
    }

    public Trip getEffectiveTrip() {
        if (trip != null)
            return trip;
        Trip t = new Trip();
        t.tripHeadsign = tripHeadsignFlat;
        t.routeShortName = routeShortNameFlat;
        return t;
    }
}
