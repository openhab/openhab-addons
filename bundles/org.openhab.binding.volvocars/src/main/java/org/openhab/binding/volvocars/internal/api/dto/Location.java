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
package org.openhab.binding.volvocars.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * GeoJSON feature as returned by <code>GET /location/v1/vehicles/{vin}/location</code>.
 *
 * @author Michael Weger - Initial contribution
 */
public class Location {
    public static class Properties {
        public @Nullable String timestamp;
        public @Nullable String heading;
    }

    public static class Geometry {
        /** GeoJSON order: longitude, latitude and optionally altitude. */
        public @Nullable List<Double> coordinates;
    }

    public @Nullable Properties properties;
    public @Nullable Geometry geometry;
}
