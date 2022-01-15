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
package org.openhab.binding.buienradar;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.openhab.binding.buienradar.internal.buienradarapi.BuienradarParseException;
import org.openhab.binding.buienradar.internal.buienradarapi.BuienradarPredictionAPI;
import org.openhab.binding.buienradar.internal.buienradarapi.Prediction;

/**
 * Tests {@link BuienradarPredictionAPI}.
 *
 * @author Edwin de Jong - Initial contribution
 */
public class BuienradarPredictionAPITest {
    private static final ZonedDateTime NOW = ZonedDateTime.of(2019, 3, 10, 20, 37, 0, 0, ZoneId.of("Europe/Amsterdam"));

    private static final ZonedDateTime ACTUAL = ZonedDateTime.of(2019, 3, 10, 20, 35, 0, 0,
            ZoneId.of("Europe/Amsterdam"));

    private static final ZonedDateTime NOW_LONDON = ZonedDateTime.of(2019, 3, 10, 20, 37, 0, 0,
            ZoneId.of("Europe/London"));

    @Test
    public void testParseIntensity000() throws BuienradarParseException {
        assertEquals(BigDecimal.valueOf(0, 2), BuienradarPredictionAPI.parseIntensity("000"));
    }

    @Test
    public void testParseIntensity050() throws BuienradarParseException {
        assertEquals(BigDecimal.valueOf(1, 2), BuienradarPredictionAPI.parseIntensity("050"));
    }

    @Test
    public void testParseIntensity500() throws BuienradarParseException {
        assertEquals(BigDecimal.valueOf(165481709994318L, 2), BuienradarPredictionAPI.parseIntensity("500"));
    }

    @Test
    public void testParseIntensity101() throws BuienradarParseException {
        assertEquals(BigDecimal.valueOf(56, 2), BuienradarPredictionAPI.parseIntensity("101"));
    }

    @Test
    public void testParseDateTime() throws BuienradarParseException {
        final ZonedDateTime parsed = BuienradarPredictionAPI.parseDateTime("20:45", NOW);
        assertEquals(ZonedDateTime.of(2019, 3, 10, 20, 45, 0, 0, ZoneId.of("Europe/Amsterdam")), parsed);
    }

    @Test
    public void testParseDateTimeLondon() throws BuienradarParseException {
        // 20:37 in London is *before* 20:45 in Amsterdam, therefore, it should be parsed as a timestamp happening
        // tomorrow.
        final ZonedDateTime parsed = BuienradarPredictionAPI.parseDateTime("20:45", NOW_LONDON);
        assertEquals(ZonedDateTime.of(2019, 3, 11, 20, 45, 0, 0, ZoneId.of("Europe/Amsterdam")), parsed);
    }

    @Test
    public void testParseDateTimeTomorrow() throws BuienradarParseException {
        final ZonedDateTime parsed = BuienradarPredictionAPI.parseDateTime("19:40", NOW);
        assertEquals(ZonedDateTime.of(2019, 3, 11, 19, 40, 0, 0, ZoneId.of("Europe/Amsterdam")), parsed);
    }

    @Test
    public void testParseLine() throws BuienradarParseException {
        final Prediction parsed = BuienradarPredictionAPI.parseLine("000|19:35", NOW, Optional.of(ACTUAL));
        assertEquals(ZonedDateTime.of(2019, 3, 11, 19, 35, 0, 0, ZoneId.of("Europe/Amsterdam")),
                parsed.getDateTimeOfPrediction());
        assertEquals(BigDecimal.valueOf(0, 2), parsed.getIntensity());
    }
}
