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

public class RouteDetailsResult {
    public Route route;
    public List<Object> alerts;

    @SerializedName("route_long_name")
    public String routeLongNameFlat;
    @SerializedName("route_short_name")
    public String routeShortNameFlat;
    @SerializedName("route_color")
    public String routeColorFlat;

    public static class Route {
        @SerializedName("route_long_name")
        public String routeLongName;
        @SerializedName("route_short_name")
        public String routeShortName;
        @SerializedName("route_color")
        public String routeColor;
    }

    public Route getEffectiveRoute() {
        if (route != null)
            return route;
        Route r = new Route();
        r.routeLongName = routeLongNameFlat;
        r.routeShortName = routeShortNameFlat;
        r.routeColor = routeColorFlat;
        return r;
    }
}
