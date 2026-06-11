/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastAdjuster;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.QuantityType;

/**
 * The {@link SmartForecastSolarTest} tests responses from forecast solar object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SmartForecastSolarTest {
    private static final double TOLERANCE = 0.001;
    public static final ZoneId TEST_ZONE = ZoneId.of("Europe/Berlin");
    public static final QuantityType<Power> POWER_UNDEF = Utils.getPowerState(-1);
    public static final QuantityType<Energy> ENERGY_UNDEF = Utils.getEnergyState(-1);

    public static final String TOO_EARLY_INDICATOR = "too early";
    public static final String TOO_LATE_INDICATOR = "too late";
    public static final String INVALID_RANGE_INDICATOR = "invalid time range";
    public static final String NO_GORECAST_INDICATOR = "No forecast data";
    public static final String DAY_MISSING_INDICATOR = "not available in forecast";

    @BeforeAll
    static void setFixedTime() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
    }

    @Test
    void testFirstTimestamp() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 17, 00).atZone(TEST_ZONE);
        ForecastSolarObject forecastObject = new ForecastSolarObject("fs-test", content,
                queryDateTime.toInstant().plus(1, ChronoUnit.DAYS));
        assertEquals(Instant.parse("2022-07-17T03:31:00Z"), forecastObject.getForecastBegin(), "First entry");
        assertEquals(Instant.parse("2022-07-17T04:00:00Z"), forecastObject.getFirstPowerTimestamp().get(),
                "First entry with positive power value");
    }

    @Test
    void testSmartAdjsutment() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 17, 00).atZone(TEST_ZONE);
        ForecastSolarObject forecastObject = new ForecastSolarObject("fs-test", content,
                queryDateTime.toInstant().plus(1, ChronoUnit.DAYS));
        // set half of energy production for adjustment
        ForecastSolarObject adjusted = new ForecastSolarObject(forecastObject,
                forecastObject.getActualEnergyValue(queryDateTime) / 2, true);
        Optional<SolarForecastAdjuster> adjuster = adjusted.getAdjuster();
        assertTrue(adjuster.isPresent(), "Adjuster present");
        assertEquals(0.5, adjuster.get().getCorrectionFactor(), TOLERANCE, "Factor");
        // "2022-07-17 21:32:00": 63583,
        assertEquals(31.792, adjusted.getDayTotal(queryDateTime.toLocalDate()), TOLERANCE, "Total production");
        // "2022-07-17 17:00:00": 52896,
        assertEquals(26.448, adjusted.getActualEnergyValue(queryDateTime), TOLERANCE, "Current Production");
        // 63583 - 52896 = 10687
        assertEquals(5.344, adjusted.getRemainingProduction(queryDateTime), TOLERANCE, "Current Production");
        // sum cross check
        assertEquals(adjusted.getDayTotal(queryDateTime.toLocalDate()),
                adjusted.getActualEnergyValue(queryDateTime) + adjusted.getRemainingProduction(queryDateTime),
                TOLERANCE, "actual + remain = total");

        queryDateTime = LocalDateTime.of(2022, 7, 18, 19, 00).atZone(TEST_ZONE);
        // "2022-07-18 19:00:00": 63067,
        assertEquals(63.067, adjusted.getActualEnergyValue(queryDateTime), TOLERANCE, "Actual production");
        // "2022-07-18 21:31:00": 65554
        assertEquals(65.554, adjusted.getDayTotal(queryDateTime.toLocalDate()), TOLERANCE, "Total production");
    }
}
