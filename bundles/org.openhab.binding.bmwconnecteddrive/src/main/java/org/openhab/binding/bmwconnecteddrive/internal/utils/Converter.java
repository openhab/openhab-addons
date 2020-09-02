/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Converter} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Converter {
    public static final DateTimeFormatter SERVICE_DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SERVICE_DATE_OUTPUT_PATTERN = DateTimeFormatter.ofPattern("MMM yyyy");

    public static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter DATE_TIMEZONE_INPUT_PATTERN = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final DateTimeFormatter DATE_OUTPUT_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static double round(double value) {
        double scale = Math.pow(10, 1);
        return Math.round(value * scale) / scale;
    }

    public static String getLocalDateTime(String input) {
        if (input == null) {
            return Converter.toTitleCase(Constants.UNKNOWN);
        }

        LocalDateTime ldt = LocalDateTime.parse(input, Converter.DATE_INPUT_PATTERN);
        ZonedDateTime zdtUTC = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtLZ = zdtUTC.withZoneSameInstant(ZoneId.systemDefault());
        return zdtLZ.format(Converter.DATE_OUTPUT_PATTERN);
    }

    public static String getZonedDateTime(String input) {
        if (input == null) {
            return Converter.toTitleCase(Constants.UNKNOWN);
        }

        ZonedDateTime zdt = ZonedDateTime.parse(input, Converter.DATE_TIMEZONE_INPUT_PATTERN);
        ZonedDateTime zdtLZ = zdt.withZoneSameInstant(ZoneId.systemDefault());
        return zdtLZ.format(Converter.DATE_OUTPUT_PATTERN);
    }

    public static String toTitleCase(String input) {
        if (input == null) {
            return Converter.toTitleCase(Constants.UNKNOWN);
        } else {
            String lower = input.replaceAll(Constants.UNDERLINE, Constants.SPACE).toLowerCase();
            String[] arr = lower.split(Constants.SPACE);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < arr.length; i++) {
                sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
            }
            return sb.toString().trim();
        }
    }

    public static String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static double measure(float lat1, float lon1, float lat2, float lon2) {
        double earthRadius = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180)
                * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = earthRadius * c;
        return d * 1000; // meters
    }

    public static double guessRange(float totalRange) {
        // Simple guess right now
        if (totalRange < 20) {
            return totalRange * 0.8;
        } else {
            return totalRange * 0.7;
        }
    }
}
