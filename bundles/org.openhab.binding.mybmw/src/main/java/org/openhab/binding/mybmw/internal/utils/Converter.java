/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.utils;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.dto.charge.Time;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Converter} Conversion Helpers
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - extract some methods to other classes
 */
@NonNullByDefault
public interface Converter {
    static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);

    public static final String DATE_INPUT_PATTERN_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern(DATE_INPUT_PATTERN_STRING);
    public static final DateTimeFormatter LOCALE_ENGLISH_TIMEFORMATTER = DateTimeFormatter.ofPattern("hh:mm a",
            Locale.ENGLISH);
    public static final SimpleDateFormat ISO_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    static final String SPLIT_HYPHEN = "-";
    static final String SPLIT_BRACKET = "\\(";

    public static State zonedToLocalDateTime(@Nullable String input) {
        if (input != null && !input.isEmpty()) {
            try {
                String dateString = ZonedDateTime.parse(input).withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime().format(Converter.DATE_INPUT_PATTERN);
                return DateTimeType.valueOf(dateString);
            } catch (Exception e) {
                LOGGER.debug("Unable to parse date {} - {}", input, e.getMessage());
                return UnDefType.UNDEF;
            }
        } else {
            return UnDefType.UNDEF;
        }
    }

    /**
     * converts a string into a unified format
     * - string is Capitalized
     * - null is empty string
     * - single character remains
     * 
     * @param input
     * @return
     */
    public static String toTitleCase(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return toTitleCase(Constants.UNDEF);
        } else if (input.length() == 1) {
            return input;
        } else {
            // first, replace all underscores with spaces and make it lower case
            String lower = input.replaceAll(Constants.UNDERLINE, Constants.SPACE).toLowerCase();

            //
            String converted = toTitleCase(lower, Constants.SPACE);
            converted = toTitleCase(converted, SPLIT_HYPHEN);
            converted = toTitleCase(converted, SPLIT_BRACKET);
            return converted;
        }
    }

    private static String toTitleCase(String input, String splitter) {
        // first, split all parts by the splitting string
        String[] arr = input.split(splitter);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(splitter.replaceAll("\\\\", Constants.EMPTY));
            }
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1));
        }
        return sb.toString().trim();
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
    public static double measureDistance(double sourceLatitude, double sourceLongitude, double destinationLatitude,
            double destinationLongitude) {
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
     * Guessing the range of the Vehicle on Map. If you can drive x kilometers with
     * your Vehicle it's not feasible to
     * project this x km Radius on Map. The roads to be taken are causing some
     * overhead because they are not a straight
     * line from Location A to B.
     * I've taken some measurements to calculate the overhead factor based on Google
     * Maps
     * Berlin - Dresden: Road Distance: 193 air-line Distance 167 = Factor 87%
     * Kassel - Frankfurt: Road Distance: 199 air-line Distance 143 = Factor 72%
     * After measuring more distances you'll find out that the outcome is between
     * 70% and 90%. So
     *
     * This depends also on the roads of a concrete route but this is only a guess
     * without any Route Navigation behind
     *
     * In future it's foreseen to replace this with BMW RangeMap Service which isn't
     * running at the moment.
     *
     * @param range
     * @return mapping from air-line distance to "real road" distance
     */
    public static int guessRangeRadius(double range) {
        return (int) (range * 0.8);
    }

    /**
     * checks if a string is a valid integer
     * 
     * @param fullString
     * @return
     */
    public static int parseIntegerString(String fullString) {
        int index = -1;
        try {
            index = Integer.parseInt(fullString);
        } catch (NumberFormatException nfe) {
        }
        return index;
    }

    public static String getRandomString(int size) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1).limit(size)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

        return generatedString;
    }

    public static State getConnectionState(boolean connected) {
        if (connected) {
            return StringType.valueOf(Constants.CONNECTED);
        } else {
            return StringType.valueOf(Constants.UNCONNECTED);
        }
    }

    public static String getCurrentISOTime() {
        Date date = new Date(System.currentTimeMillis());
        synchronized (ISO_FORMATTER) {
            ISO_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
            return ISO_FORMATTER.format(date);
        }
    }

    public static String getTime(Time t) {
        StringBuffer time = new StringBuffer();
        if (t.getHour() < 10) {
            time.append("0");
        }
        time.append(Integer.toString(t.getHour())).append(":");
        if (t.getMinute() < 10) {
            time.append("0");
        }
        time.append(Integer.toString(t.getMinute()));
        return time.toString();
    }

    public static int getOffsetMinutes() {
        ZoneOffset zo = ZonedDateTime.now().getOffset();
        return zo.getTotalSeconds() / 60;
    }

    public static int stringToInt(String intStr) {
        int integer = Constants.INT_UNDEF;
        try {
            integer = Integer.parseInt(intStr);

        } catch (Exception e) {
            LOGGER.debug("Unable to convert range {} into int value", intStr);
        }
        return integer;
    }

    public static String getLocalTime(String chrageInfoLabel) {
        String[] timeSplit = chrageInfoLabel.split(Constants.TILDE);
        if (timeSplit.length == 2) {
            try {
                LocalTime timeL = LocalTime.parse(timeSplit[1].trim(), LOCALE_ENGLISH_TIMEFORMATTER);
                return timeSplit[0] + Constants.TILDE + timeL.toString();
            } catch (Exception e) {
                LOGGER.debug("Unable to parse date {} - {}", timeSplit[1], e.getMessage());
            }
        }
        return chrageInfoLabel;
    }
}
