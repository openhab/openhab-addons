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
package org.openhab.binding.mybmw.internal.handler.backend;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * anonymizes all occurrencies of locations and vins
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring & extension for any occurrence
 * @author Mark Herwege - extended log anonymization
 */
@NonNullByDefault
public interface ResponseContentAnonymizer {

    static final String ANONYMOUS_VIN = "anonymousVin";
    static final String VIN_PATTERN = "\"vin\":";
    static final String VEHICLE_CHARGING_LOCATION_PATTERN = "\"subtitle\":";
    static final String VEHICLE_LOCATION_PATTERN = "\"location\":";
    static final String VEHICLE_LOCATION_LATITUDE_PATTERN = "latitude";
    static final String VEHICLE_LOCATION_LONGITUDE_PATTERN = "longitude";
    static final String VEHICLE_LOCATION_FORMATTED_PATTERN = "formatted";
    static final String VEHICLE_LOCATION_HEADING_PATTERN = "heading";
    static final String VEHICLE_LOCATION_LATITUDE = "1.1";
    static final String VEHICLE_LOCATION_LONGITUDE = "2.2";
    static final String ANONYMOUS_ADDRESS = "anonymousAddress";
    static final String VEHICLE_LOCATION_HEADING = "-1";
    static final String RAW_VEHICLE_LOCATION_PATTERN_START = "\\\"location\\\"";
    static final String RAW_VEHICLE_LOCATION_PATTERN_END = "\\\"heading\\\"";
    static final String RAW_VEHICLE_LOCATION_PATTERN_REPLACER = "\"location\":{\"coordinates\":{\"latitude\":"
            + VEHICLE_LOCATION_LATITUDE + ",\"longitude\":" + VEHICLE_LOCATION_LONGITUDE
            + "},\"address\":{\"formatted\":\"" + ANONYMOUS_ADDRESS + "\"},";

    static final String CLOSING_BRACKET = "}";
    static final String QUOTE = "\"";
    static final String CLOSE_VALUE = "\":";
    static final String COMMA = ",";

    /**
     * anonymizes the responseContent
     * <p>
     * - vin
     * </p>
     * <p>
     * - location
     * </p>
     *
     * @param responseContent
     * @return
     */
    public static String anonymizeResponseContent(@Nullable String responseContent) {
        if (responseContent == null) {
            return "";
        }

        String anonymizedVinString = replaceVins(responseContent);

        String anonymizedLocationString = replaceLocations(anonymizedVinString);

        String anonymizedRawLocationString = replaceRawLocations(anonymizedLocationString);

        String anonymizedChargingLocationString = replaceChargingLocations(anonymizedRawLocationString);

        return anonymizedChargingLocationString;
    }

    static String replaceChargingLocations(String stringToBeReplaced) {
        String[] locationStrings = stringToBeReplaced.split(VEHICLE_CHARGING_LOCATION_PATTERN);

        StringBuffer replacedString = new StringBuffer();
        replacedString.append(locationStrings[0]);
        for (int i = 1; locationStrings.length > 0 && i < locationStrings.length && locationStrings[i] != null; i++) {
            replacedString.append(VEHICLE_CHARGING_LOCATION_PATTERN);
            replacedString.append(replaceChargingLocation(locationStrings[i]));
        }

        return replacedString.toString();
    }

    static String replaceChargingLocation(String responseContent) {
        String[] subtitleStrings = responseContent.split(" • ", 2);

        StringBuffer replacedString = new StringBuffer();

        replacedString.append("\"");
        replacedString.append(ANONYMOUS_ADDRESS);
        if (subtitleStrings.length > 1) {
            replacedString.append(" • ");
            replacedString.append(subtitleStrings[1]);
        }

        return replacedString.toString();
    }

    static String replaceRawLocations(String stringToBeReplaced) {
        String[] locationStrings = stringToBeReplaced.split(Pattern.quote(RAW_VEHICLE_LOCATION_PATTERN_START));

        StringBuffer replacedString = new StringBuffer();
        replacedString.append(locationStrings[0]);
        for (int i = 1; locationStrings.length > 0 && i < locationStrings.length && locationStrings[i] != null; i++) {
            replacedString.append(replaceRawLocation(locationStrings[i]));
        }

        return replacedString.toString();
    }

    /**
     * this just replaces a string
     *
     * @param string
     * @return
     */
    static String replaceRawLocation(String stringToBeReplaced) {
        String[] stringParts = stringToBeReplaced.split(Pattern.quote(RAW_VEHICLE_LOCATION_PATTERN_END));

        StringBuffer replacedString = new StringBuffer();
        replacedString.append(RAW_VEHICLE_LOCATION_PATTERN_REPLACER);
        replacedString.append(RAW_VEHICLE_LOCATION_PATTERN_END);
        replacedString.append(stringParts[1]);
        return replacedString.toString();
    }

    static String replaceLocations(String stringToBeReplaced) {
        String[] locationStrings = stringToBeReplaced.split(VEHICLE_LOCATION_PATTERN);

        StringBuffer replacedString = new StringBuffer();
        replacedString.append(locationStrings[0]);
        for (int i = 1; locationStrings.length > 0 && i < locationStrings.length && locationStrings[i] != null; i++) {
            replacedString.append(VEHICLE_LOCATION_PATTERN);
            replacedString.append(replaceLocation(locationStrings[i]));
        }

        return replacedString.toString();
    }

    static String replaceLocation(String responseContent) {
        String stringToBeReplaced = responseContent;

        StringBuffer replacedString = new StringBuffer();
        // latitude
        stringToBeReplaced = replaceNumberValue(stringToBeReplaced, replacedString, VEHICLE_LOCATION_LATITUDE_PATTERN,
                VEHICLE_LOCATION_LATITUDE);

        // longitude
        stringToBeReplaced = replaceNumberValue(stringToBeReplaced, replacedString, VEHICLE_LOCATION_LONGITUDE_PATTERN,
                VEHICLE_LOCATION_LONGITUDE);

        // formatted address
        stringToBeReplaced = replaceStringValue(stringToBeReplaced, replacedString, VEHICLE_LOCATION_FORMATTED_PATTERN,
                ANONYMOUS_ADDRESS);

        // heading
        stringToBeReplaced = replaceNumberValue(stringToBeReplaced, replacedString, VEHICLE_LOCATION_HEADING_PATTERN,
                VEHICLE_LOCATION_HEADING);

        replacedString.append(stringToBeReplaced);

        return replacedString.toString();
    }

    static String replaceNumberValue(String stringToBeReplaced, StringBuffer replacedString, String replacerPattern,
            String replacerValue) {
        int startIndex = stringToBeReplaced.indexOf(replacerPattern, 1)
                + (replacerPattern.length() + CLOSE_VALUE.length());
        int endIndex = -1;

        // in an object, the comma comes after the value or a closing bracket
        if (stringToBeReplaced.indexOf(COMMA, startIndex) < stringToBeReplaced.indexOf(CLOSING_BRACKET, startIndex)) {
            endIndex = stringToBeReplaced.indexOf(COMMA, startIndex);
        } else {
            endIndex = stringToBeReplaced.indexOf(CLOSING_BRACKET, startIndex);
        }

        replacedString.append(stringToBeReplaced.substring(0, startIndex));
        replacedString.append(replacerValue);

        return stringToBeReplaced.substring(endIndex);
    }

    static String replaceStringValue(String stringToBeReplaced, StringBuffer replacedString, String replacerPattern,
            String replacerValue) {
        // the startIndex is the String after the first quote of the value after the key
        // detect end of key
        int startIndex = stringToBeReplaced.indexOf(replacerPattern, 1)
                + (replacerPattern.length() + CLOSE_VALUE.length());
        // detect start of value
        startIndex = stringToBeReplaced.indexOf(QUOTE, startIndex) + 1;

        // detect end of value
        int endIndex = stringToBeReplaced.indexOf(QUOTE, startIndex);

        replacedString.append(stringToBeReplaced.substring(0, startIndex));
        replacedString.append(replacerValue);

        return stringToBeReplaced.substring(endIndex);
    }

    static String replaceVins(String stringToBeReplaced) {
        String[] vinStrings = stringToBeReplaced.split(VIN_PATTERN);

        StringBuffer replacedString = new StringBuffer();
        replacedString.append(vinStrings[0]);
        for (int i = 1; vinStrings.length > 0 && i < vinStrings.length; i++) {
            replacedString.append(VIN_PATTERN);
            replacedString.append(replaceVin(vinStrings[i]));
        }

        return replacedString.toString();
    }

    static String replaceVin(String stringToBeReplaced) {
        // the vin is between two quotes
        int startIndex = stringToBeReplaced.indexOf(QUOTE) + 1;
        int endIndex = stringToBeReplaced.indexOf(QUOTE, startIndex);

        StringBuffer replacedString = new StringBuffer();
        replacedString.append(stringToBeReplaced.substring(0, startIndex));
        replacedString.append(ANONYMOUS_VIN);
        replacedString.append(stringToBeReplaced.substring(endIndex));

        return replacedString.toString();
    }

    static @Nullable String replaceVin(@Nullable String stringToBeReplaced, @Nullable String vin) {
        if (stringToBeReplaced == null) {
            return null;
        }
        return vin != null ? stringToBeReplaced.replace(vin, ANONYMOUS_VIN) : stringToBeReplaced;
    }
}
