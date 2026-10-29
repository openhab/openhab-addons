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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RouteDetailsResult} is the response of the {@code route_details} endpoint.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class RouteDetailsResult {
    public @Nullable Route route;

    public static class Route {
        public @Nullable List<ServiceAlert> alerts;
        @SerializedName("route_long_name")
        public @Nullable String routeLongName;
        @SerializedName("route_short_name")
        public @Nullable String routeShortName;
        @SerializedName("route_color")
        public @Nullable String routeColor;
        @SerializedName("mode_key")
        public @Nullable String modeKey;
        @SerializedName("route_network_id")
        public @Nullable String routeNetworkId;
    }
}
