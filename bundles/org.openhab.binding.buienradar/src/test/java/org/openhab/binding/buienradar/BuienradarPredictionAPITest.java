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
package org.openhab.binding.buienradar;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.openhab.binding.buienradar.internal.buienradarapi.BuienradarParseException;
import org.openhab.binding.buienradar.internal.buienradarapi.BuienradarPredictionAPI;
import org.openhab.binding.buienradar.internal.buienradarapi.Prediction;

public class BuienradarPredictionAPITest {
    private static final ZonedDateTime NOW = ZonedDateTime.of(2019, 3, 10, 20, 37, 0, 0, ZoneId.of("Europe/Amsterdam"));

    @Test
    public void testParseIntensity000() throws BuienradarParseException {
        assertEquals(3.9241e-4, BuienradarPredictionAPI.parseIntensity("000"), 1e-5);
    }

    @Test
    public void testParseIntensity050() throws BuienradarParseException {
        assertEquals(0.01433, BuienradarPredictionAPI.parseIntensity("050"), 1e-5);
    }

    @Test
    public void testParseIntensity500() throws BuienradarParseException {
        assertEquals(1.654817e12, BuienradarPredictionAPI.parseIntensity("500"), 1e7);
    }

    @Test
    public void testParseDateTime() throws BuienradarParseException {
        final ZonedDateTime parsed = BuienradarPredictionAPI.parseDateTime("20:45", NOW);
        assertEquals(ZonedDateTime.of(2019, 3, 10, 20, 45, 0, 0, ZoneId.of("Europe/Amsterdam")), parsed);
    }

    @Test
    public void testParseDateTimeTomorrow() throws BuienradarParseException {
        final ZonedDateTime parsed = BuienradarPredictionAPI.parseDateTime("19:40", NOW);
        assertEquals(ZonedDateTime.of(2019, 3, 11, 19, 40, 0, 0, ZoneId.of("Europe/Amsterdam")), parsed);
    }

    @Test
    public void testParseLine() throws BuienradarParseException {
        final Prediction parsed = BuienradarPredictionAPI.parseLine("000|19:35", NOW);
        assertEquals(ZonedDateTime.of(2019, 3, 11, 19, 35, 0, 0, ZoneId.of("Europe/Amsterdam")), parsed.getDateTime());
        assertEquals(3.9241e-4, parsed.getIntensity(), 1e-5);
    }

}
