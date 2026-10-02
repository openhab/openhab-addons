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
public class RouteDetailsResult {
    public @Nullable Route route;

    @SerializedName("route_long_name")
    public @Nullable String routeLongNameFlat;
    @SerializedName("route_short_name")
    public @Nullable String routeShortNameFlat;
    @SerializedName("route_color")
    public @Nullable String routeColorFlat;

    public static class Route {
        public @Nullable List<Object> alerts;
        @SerializedName("route_long_name")
        public @Nullable String routeLongName;
        @SerializedName("route_short_name")
        public @Nullable String routeShortName;
        @SerializedName("route_color")
        public @Nullable String routeColor;
    }

    public @Nullable Route getEffectiveRoute() {
        if (route != null) {
            return route;
        }
        if (routeLongNameFlat == null && routeShortNameFlat == null && routeColorFlat == null) {
            return null;
        }
        Route r = new Route();
        r.routeLongName = routeLongNameFlat;
        r.routeShortName = routeShortNameFlat;
        r.routeColor = routeColorFlat;
        return r;
    }
}
