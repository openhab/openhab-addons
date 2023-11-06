/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Astro Thing configuration.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class AstroThingConfig {
    public static final String GEOLOCATION = "geolocation";
    public static final String USE_METEOROLOGICAL_SEASON = "useMeteorologicalSeason";
    public @Nullable String geolocation;
    public @Nullable Double altitude;
    public @Nullable Double latitude;
    public @Nullable Double longitude;
    public boolean useMeteorologicalSeason;
    public int interval = 300;

    /**
     * Splits the geolocation into latitude and longitude.
     */
    public void parseGeoLocation() {
        if (geolocation != null) {
            String[] geoParts = geolocation.split(",");
            if (geoParts.length >= 2) {
                latitude = toDouble(geoParts[0]);
                longitude = toDouble(geoParts[1]);
            }
            if (geoParts.length == 3) {
                altitude = toDouble(geoParts[2]);
            }
        }
    }

    private @Nullable Double toDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
        }
        return null;
    }
}
