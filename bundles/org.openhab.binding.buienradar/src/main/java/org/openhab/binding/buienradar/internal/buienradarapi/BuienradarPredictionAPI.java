/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.buienradar.internal.buienradarapi;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BuienradarPredictionAPI} class implements the methods for retrieving results from the buienradar.nl
 * service.
 *
 * @author Edwin de Jong - Initial contribution
 */
@NonNullByDefault
public class BuienradarPredictionAPI implements PredictionAPI {
    private static final String BASE_ADDRESS = "https://gpsgadget.buienradar.nl/data/raintext";

    private static final int TIMEOUT = 15000;
    private final Logger logger = LoggerFactory.getLogger(BuienradarPredictionAPI.class);

    /**
     * Parses a raw intensity string, such as <code>000</code>, into the right intensity in mm / hour.
     *
     * @param intensityStr The raw intensity string, being 3 characters long.
     * @return The real intensity in mm / hour
     * @throws BuienradarParseException when the intensity string could not be parsed.
     */
    public static double parseIntensity(String intensityStr) throws BuienradarParseException {
        assert (intensityStr.length() == 3);
        final int lastZero = intensityStr.lastIndexOf('0');
        final String trimmedStr = lastZero == -1 || lastZero == 2 ? intensityStr : intensityStr.substring(lastZero + 1);
        try {
            // Intensity in mm / hour = 10^((value-109)/32)
            return Math.pow(10.0, (Integer.parseInt(trimmedStr) - 109) / 32.0);
        } catch (NumberFormatException e) {
            throw new BuienradarParseException("Could not parse intensityStr", e);
        }
    }

    /**
     * Parses a time string, such as <code>10:30</code> into a ZonedDateTime, using the reference ZonedDateTime to find
     * the closest
     * match.
     *
     * @param timeStr The time string to parse.
     * @param now     The reference time to use.
     * @return A ZonedDateTime of the indicated time.
     * @throws BuienradarParseException When the time string cannot be correctly parsed.
     */
    public static ZonedDateTime parseDateTime(String timeStr, ZonedDateTime now) throws BuienradarParseException {
        final String[] timeElements = timeStr.split(":");
        if (timeElements.length != 2) {
            throw new BuienradarParseException("Expecting exactly two time elements");
        }

        final int hour = Integer.parseInt(timeElements[0]);
        final int minute = Integer.parseInt(timeElements[1]);
        final LocalTime time = LocalTime.of(hour, minute);

        final ZonedDateTime tryDateTime = time.atDate(now.toLocalDate()).atZone(ZoneId.of("Europe/Amsterdam"));
        if (tryDateTime.plusMinutes(20).isBefore(now)) {
            // Check me: could this go wrong at DTS days?
            return time.atDate(now.toLocalDate().plusDays(1)).atZone(ZoneId.of("Europe/Amsterdam"));
        } else {
            return tryDateTime;
        }
    }

    /**
     * Parses a line returned from the buienradar API service. An example line could be <code>100|23:00</code>.
     *
     * @param line The line to parse, such as <code>100|23:00</code>
     * @param now  The reference time to determine which instant to match to.
     * @return A Prediction interface, which contains the tuple with the intensity and the time.
     * @throws BuienradarParseException Thrown when the line could not be correctly parsed.
     */
    public static Prediction parseLine(String line, ZonedDateTime now) throws BuienradarParseException {
        final String[] lineElements = line.trim().split("\\|");
        if (lineElements.length != 2) {
            throw new BuienradarParseException(
                    String.format("Expected two line elements, but found %s", lineElements.length));
        }
        final double intensityOut = parseIntensity(lineElements[0]);
        final ZonedDateTime dateTime = parseDateTime(lineElements[1], now);
        return new Prediction() {

            @Override
            public final double getIntensity() {
                return intensityOut;
            }

            @Override
            public final ZonedDateTime getDateTime() {
                return dateTime;
            }
        };
    }

    @Override
    public List<Prediction> getPredictions(double lat, double lon) throws IOException {
        final String address = String.format(BASE_ADDRESS + "?lat=%.6f&lon=%.6f", lat, lon);
        final String result = HttpUtil.executeUrl("GET", address, TIMEOUT);
        final List<Prediction> predictions = new ArrayList<Prediction>(24);
        final List<String> errors = new LinkedList<String>();
        logger.debug("Returned result from buienradar: {}", result);
        for (String line : result.split("\n")) {
            try {
                predictions.add(parseLine(line, ZonedDateTime.now()));
            } catch (BuienradarParseException e) {
                errors.add(e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            logger.error("Could not parse all results: " + errors.stream().collect(Collectors.joining(", ")));
        }
        return predictions;
    }

}
