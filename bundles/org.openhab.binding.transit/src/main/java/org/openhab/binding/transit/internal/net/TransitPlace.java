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
package org.openhab.binding.transit.internal.net;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TransitPlace} is the start or destination of a trip plan, either a global stop ID or a coordinate.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public record TransitPlace(@Nullable String globalStopId, double latitude, double longitude) {

    public static TransitPlace ofStop(String globalStopId) {
        return new TransitPlace(globalStopId, 0, 0);
    }

    public static TransitPlace ofLocation(double latitude, double longitude) {
        return new TransitPlace(null, latitude, longitude);
    }

    /**
     * Builds the query parameters for this place, e.g. {@code from_lat=..&from_lon=..} or
     * {@code from_global_stop_id=..}.
     */
    public String toQuery(String prefix) {
        String stopId = globalStopId;
        if (stopId != null) {
            return prefix + "_global_stop_id=" + URLEncoder.encode(stopId, StandardCharsets.UTF_8);
        }
        return prefix + "_lat=" + latitude + "&" + prefix + "_lon=" + longitude;
    }
}
