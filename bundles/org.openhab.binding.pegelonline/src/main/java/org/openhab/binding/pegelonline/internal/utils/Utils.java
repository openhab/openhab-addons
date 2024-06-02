/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pegelonline.internal.utils;

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.UNKNOWN;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.StringUtils;

/**
 * {@link Utils} Utilities for binding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    public static final int EARTH_RADIUS = 6371;

    /**
     * Calculate the Distance Using Equirectangular Distance Approximation
     *
     * @param lat1 - Latitude of coordinate 1
     * @param lon1 - Longitude of coordinate 1
     * @param lat2 - Latitude of coordinate 2
     * @param lon2 - Longitude of coordinate 2
     * @return distance in km
     *
     * @see https://www.baeldung.com/java-find-distance-between-points#equirectangular-distance-approximation
     *
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);

        double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
        double y = (lat2Rad - lat1Rad);
        double distance = Math.sqrt(x * x + y * y) * EARTH_RADIUS;

        return distance;
    }

    /**
     * Converts String from "all upper case" into "title case" after space and hyphen
     *
     * @param input - string to convert
     * @return title case string
     */
    public static String toTitleCase(@Nullable String input) {
        if (input == null) {
            return toTitleCase(UNKNOWN);
        } else {
            StringBuffer titleCaseString = new StringBuffer();
            for (String string : StringUtils.splitByCharacterType(input)) {
                String converted = StringUtils.capitalize(string.toLowerCase());
                titleCaseString.append(converted);
            }
            return titleCaseString.toString();
        }
    }
}
