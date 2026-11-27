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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StopsResult} is the response of the {@code nearby_stops} endpoint ({@code stops}) and of the
 * {@code search_stops} endpoint ({@code results}).
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class StopsResult {
    private @Nullable List<Stop> stops;
    private @Nullable List<Stop> results;

    public List<Stop> getStops() {
        List<Stop> list = stops;
        if (list == null) {
            list = results;
        }
        return list != null ? list : Collections.emptyList();
    }

    public static class Stop {
        @SerializedName("global_stop_id")
        private @Nullable String globalStopId;

        @SerializedName("stop_name")
        private @Nullable String stopName;

        @SerializedName("city_name")
        private @Nullable String cityName;

        @SerializedName("stop_lat")
        private @Nullable Double stopLat;

        @SerializedName("stop_lon")
        private @Nullable Double stopLon;

        // distance in meters from the requested location
        private @Nullable Double distance;

        @SerializedName("location_type")
        private @Nullable Integer locationType;

        public @Nullable String getGlobalStopId() {
            return globalStopId;
        }

        public @Nullable String getStopName() {
            return stopName;
        }

        public @Nullable String getCityName() {
            return cityName;
        }

        public @Nullable Double getStopLat() {
            return stopLat;
        }

        public @Nullable Double getStopLon() {
            return stopLon;
        }

        public @Nullable Double getDistance() {
            return distance;
        }

        public @Nullable Integer getLocationType() {
            return locationType;
        }
    }
}
