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
package org.openhab.binding.solarforecast;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ForecastSolarTest} tests responses from forecast solar object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ForecastSolarTest {
    private static final double TOLERANCE = 0.001;
    public static final ZoneId TEST_ZONE = ZoneId.of("Europe/Berlin");

    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 17, 00).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime.toInstant());
        // "2022-07-17 21:32:00": 63583,
        assertEquals(63.583, fo.getDayTotal(queryDateTime.toLocalDate()), TOLERANCE, "Total production");
        // "2022-07-17 17:00:00": 52896,
        assertEquals(52.896, fo.getActualEnergyValue(queryDateTime), TOLERANCE, "Current Production");
        // 63583 - 52896 = 10687
        assertEquals(10.687, fo.getRemainingProduction(queryDateTime), TOLERANCE, "Current Production");
        // sum cross check
        assertEquals(fo.getDayTotal(queryDateTime.toLocalDate()),
                fo.getActualEnergyValue(queryDateTime) + fo.getRemainingProduction(queryDateTime), TOLERANCE,
                "actual + remain = total");

        queryDateTime = LocalDateTime.of(2022, 7, 18, 19, 00).atZone(TEST_ZONE);
        // "2022-07-18 19:00:00": 63067,
        assertEquals(63.067, fo.getActualEnergyValue(queryDateTime), TOLERANCE, "Actual production");
        // "2022-07-18 21:31:00": 65554
        assertEquals(65.554, fo.getDayTotal(queryDateTime.toLocalDate()), TOLERANCE, "Total production");
    }

    @Test
    void testActualPower() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 10, 00).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime.toInstant());
        // "2022-07-17 10:00:00": 4874,
        assertEquals(4.874, fo.getActualPowerValue(queryDateTime), TOLERANCE, "Actual estimation");

        queryDateTime = LocalDateTime.of(2022, 7, 18, 14, 00).atZone(TEST_ZONE);
        // "2022-07-18 14:00:00": 7054,
        assertEquals(7.054, fo.getActualPowerValue(queryDateTime), TOLERANCE, "Actual estimation");
    }

    @Test
    void testInterpolation() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 0).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime.toInstant());

        // test steady value increase
        double previousValue = 0;
        for (int i = 0; i < 60; i++) {
            queryDateTime = queryDateTime.plusMinutes(1);
            assertTrue(previousValue < fo.getActualEnergyValue(queryDateTime));
            previousValue = fo.getActualEnergyValue(queryDateTime);
        }

        queryDateTime = LocalDateTime.of(2022, 7, 18, 6, 23).atZone(TEST_ZONE);
        // "2022-07-18 06:00:00": 132,
        // "2022-07-18 07:00:00": 1188,
        // 1188 - 132 = 1056 | 1056 * 23 / 60 = 404 | 404 + 131 = 535
        assertEquals(0.535, fo.getActualEnergyValue(queryDateTime), 0.002, "Actual estimation");
    }

    @Test
    void testForecastSum() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime.toInstant());
        QuantityType<Energy> actual = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        State st = Utils.getEnergyState(fo.getActualEnergyValue(queryDateTime));
        assertTrue(st instanceof QuantityType);
        actual = actual.add((QuantityType<Energy>) st);
        assertEquals(49.431, actual.floatValue(), TOLERANCE, "Current Production");
        actual = actual.add((QuantityType<Energy>) st);
        assertEquals(98.862, actual.floatValue(), TOLERANCE, "Doubled Current Production");
    }

    @Test
    void testCornerCases() {
        // invalid object
        ForecastSolarObject fo = new ForecastSolarObject();
        assertFalse(fo.isValid());
        ZonedDateTime query = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        assertEquals(-1.0, fo.getActualEnergyValue(query), TOLERANCE, "Actual Production");
        assertEquals(-1.0, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Today Production");
        assertEquals(-1.0, fo.getRemainingProduction(query), TOLERANCE, "Remaining Production");
        assertEquals(-1.0, fo.getDayTotal(query.plusDays(1).toLocalDate()), TOLERANCE, "Tomorrow Production");

        // valid object - query date one day too early
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        query = LocalDateTime.of(2022, 7, 16, 23, 59).atZone(TEST_ZONE);
        fo = new ForecastSolarObject(content, query.toInstant());
        assertEquals(-1.0, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(-1.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // one minute later we reach a valid date
        query = query.plusMinutes(1);
        assertEquals(63.583, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(63.583, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // valid object - query date one day too late
        query = LocalDateTime.of(2022, 7, 19, 0, 0).atZone(TEST_ZONE);
        assertEquals(-1.0, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(-1.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // one minute earlier we reach a valid date
        query = query.minusMinutes(1);
        assertEquals(65.554, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(65.554, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // test times between 2 dates
        query = LocalDateTime.of(2022, 7, 17, 23, 59).atZone(TEST_ZONE);
        assertEquals(63.583, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(63.583, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");
        query = query.plusMinutes(1);
        assertEquals(65.554, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(65.554, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");
    }

    @Test
    void testActions() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime.toInstant());
        assertEquals("2022-07-17T05:31:00",
                fo.getForecastBegin().atZone(TEST_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Forecast begin");
        assertEquals("2022-07-18T21:31:00",
                fo.getForecastEnd().atZone(TEST_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "Forecast end");
        assertEquals(QuantityType.valueOf(63.583, Units.KILOWATT_HOUR).toString(),
                fo.getDay(queryDateTime.toLocalDate()).toFullString(), "Actual out of scope");

        queryDateTime = LocalDateTime.of(2022, 7, 17, 0, 0).atZone(TEST_ZONE);
        // "watt_hours_day": {
        // "2022-07-17": 63583,
        // "2022-07-18": 65554
        // }
        assertEquals(QuantityType.valueOf(129.137, Units.KILOWATT_HOUR).toString(),
                fo.getEnergy(queryDateTime.toInstant(), queryDateTime.plusDays(2).toInstant()).toFullString(),
                "Actual out of scope");

        assertEquals(UnDefType.UNDEF, fo.getDay(queryDateTime.toLocalDate(), "optimistic"));
        assertEquals(UnDefType.UNDEF, fo.getDay(queryDateTime.toLocalDate(), "pessimistic"));
        assertEquals(UnDefType.UNDEF, fo.getDay(queryDateTime.toLocalDate(), "total", "rubbish"));
    }

    @Test
    void testTimeSeries() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime.toInstant());

        TimeSeries powerSeries = fo.getPowerTimeSeries();
        assertEquals(36, powerSeries.size()); // 18 values each day for 2 days
        powerSeries.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<Power>) s).getUnit().toString());
        });

        TimeSeries energySeries = fo.getEnergyTimeSeries();
        assertEquals(36, energySeries.size());
        energySeries.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<Energy>) s).getUnit().toString());
        });
    }
}
