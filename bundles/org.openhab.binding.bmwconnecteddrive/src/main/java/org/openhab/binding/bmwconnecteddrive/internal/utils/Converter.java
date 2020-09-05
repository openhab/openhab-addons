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
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

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

    private static final Gson GSON = new Gson();

    public static double round(double value) {
        double scale = Math.pow(10, 1);
        return Math.round(value * scale) / scale;
    }

    public static String getLocalDateTime(@Nullable String input) {
        if (input == null) {
            return Converter.toTitleCase(Constants.UNKNOWN);
        }

        LocalDateTime ldt = LocalDateTime.parse(input, Converter.DATE_INPUT_PATTERN);
        ZonedDateTime zdtUTC = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtLZ = zdtUTC.withZoneSameInstant(ZoneId.systemDefault());
        return zdtLZ.format(Converter.DATE_OUTPUT_PATTERN);
    }

    public static String getZonedDateTime(@Nullable String input) {
        if (input == null) {
            return Converter.toTitleCase(Constants.UNKNOWN);
        }

        ZonedDateTime zdt = ZonedDateTime.parse(input, Converter.DATE_TIMEZONE_INPUT_PATTERN);
        ZonedDateTime zdtLZ = zdt.withZoneSameInstant(ZoneId.systemDefault());
        return zdtLZ.format(Converter.DATE_OUTPUT_PATTERN);
    }

    public static String toTitleCase(@Nullable String input) {
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

    /**
     * Measure distance between 2 coordinates
     *
     * @param sourceLatitude
     * @param sourceLongitude
     * @param destinationLatitude
     * @param destinationLongitude
     * @return distance
     */
    public static double measureDistance(float sourceLatitude, float sourceLongitude, float destinationLatitude,
            float destinationLongitude) {
        double earthRadius = 6378.137; // Radius of earth in KM
        double dLat = destinationLatitude * Math.PI / 180 - sourceLatitude * Math.PI / 180;
        double dLon = destinationLongitude * Math.PI / 180 - sourceLongitude * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(sourceLatitude * Math.PI / 180)
                * Math.cos(destinationLatitude * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * Easy function but there's some measures behind:
     * Guessing the range of the Vehicle on Map. If you can drive x kilometers with your car it's not feasible to
     * project this x km Radius on Map. The roads to be taken are causing some overhead because they are not a straight
     * line from Location A to B.
     * I've taken some measurements to calculate the overhead factor based on Google Maps
     * Berlin - Dresden: Road Distance: 193 air-line Distance 167 = Factor 87%
     * Kassel - Frankfurt: Road Distance: 199 air-line Distance 143 = Factor 72%
     * After measuring more distances you'll find out that the outcome is between 70% and 90%. So
     *
     * This depends also on the roads of a concrete route but this is only a guess without any Route Navigation behind
     *
     * In future it's foreseen to replace this with BMW RangeMap Service which isn't running at the moment.
     *
     * @param range
     * @return mapping from air-line distance to "real road" distance
     */
    public static double guessRangeRadius(float range) {
        return range * 0.8;
    }

    public static Gson getGson() {
        return GSON;
    }
}
