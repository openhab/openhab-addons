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

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link Utils} Utilities for binding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {

    public static double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        var earthRadius = 6371; // Radius of the earth in km
        var dLat = deg2rad(lat2 - lat1); // deg2rad below
        var dLon = deg2rad(lon2 - lon1);
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = earthRadius * c; // Distance in km
        return d;
    }

    public static double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

    public static String toTitleCase(@Nullable String input) {
        if (input == null) {
            return toTitleCase(UNKNOWN);
        } else {
            String lower = input.replaceAll(UNDERLINE, SPACE).toLowerCase();
            String converted = toTitleCase(lower, SPACE);
            converted = toTitleCase(converted, HYPHEN);
            return converted;
        }
    }

    private static String toTitleCase(String input, String splitter) {
        String[] arr = input.split(splitter);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(splitter.replaceAll("\\\\", EMPTY));
            }
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1));
        }
        return sb.toString().trim();
    }
}
