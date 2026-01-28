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
package org.openhab.binding.solarforecast.internal.solcast;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.binding.solarforecastinternal.solcast.mock.SolcastPlaneMock;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The {@link SolcastForecastTest} tests forecast calculations provided to actions and channels
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastForecastTest {
    // double comparison tolerance = 1 Watt
    private static final double TOLERANCE = 0.001;
    private static final String TOO_LATE_INDICATOR = "too late";
    private static final String DAY_MISSING_INDICATOR = "not available in forecast";

    private static SolcastObject solcastForecast = new SolcastObject("sc-test");
    private static TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };
    private static ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());

    @BeforeEach
    void setFixedTimeJul17() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, timeZoneProvider.getTimeZone());
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(timeZoneProvider);
        JSONArray forecastJson = SolcastPlaneMock.getPreparedForecast();
        now = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(timeZoneProvider.getTimeZone());
        solcastForecast = new SolcastObject("sc-test", forecastJson, now.toInstant());
    }

    static void setFixedTimeJul18() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-18T14:23:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, timeZoneProvider.getTimeZone());
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(timeZoneProvider);

        JSONArray forecastJson = SolcastPlaneMock.getPreparedForecast();
        now = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(timeZoneProvider.getTimeZone());
        solcastForecast = new SolcastObject("sc-test", forecastJson, now.toInstant());
    }

    @Test
    void testForecastObject() {
        // test one day, step ahead in time and cross check channel values
        double dayTotal = solcastForecast.getDayTotal(now.toLocalDate(), QueryMode.AVERAGE);
        double actual = solcastForecast.getActualEnergyValue(now, QueryMode.AVERAGE);
        double remain = solcastForecast.getRemainingProduction(now, QueryMode.AVERAGE);
        assertEquals(0.0, actual, TOLERANCE, "Begin of day actual");
        assertEquals(23.107, remain, TOLERANCE, "Begin of day remaining");
        assertEquals(23.107, dayTotal, TOLERANCE, "Day total");
        assertEquals(0.0, solcastForecast.getActualPowerValue(now, QueryMode.AVERAGE), TOLERANCE, "Begin of day power");
        double previousPower = 0;
        for (int i = 0; i < 47; i++) {
            now = now.plusMinutes(30);
            double power = solcastForecast.getActualPowerValue(now, QueryMode.AVERAGE) / 2.0;
            double powerAddOn = ((power + previousPower) / 2.0);
            actual += powerAddOn;
            assertEquals(actual, solcastForecast.getActualEnergyValue(now, QueryMode.AVERAGE), TOLERANCE,
                    "Actual at " + now);
            remain -= powerAddOn;
            assertEquals(remain, solcastForecast.getRemainingProduction(now, QueryMode.AVERAGE), TOLERANCE,
                    "Remain at " + now);
            assertEquals(dayTotal, actual + remain, TOLERANCE, "Total sum at " + now);
            previousPower = power;
        }
    }

    @Test
    void testPower() {
        ZonedDateTime now = LocalDateTime.of(2022, 7, 23, 16, 00).atZone(timeZoneProvider.getTimeZone());
        assertEquals(1.9176, solcastForecast.getActualPowerValue(now, QueryMode.AVERAGE), TOLERANCE,
                "Estimate power " + now);
        assertEquals(1.9176, solcastForecast.getPower(now.toInstant(), "average").doubleValue(), TOLERANCE,
                "Estimate power " + now);
        assertEquals(1.754, solcastForecast.getActualPowerValue(now.plusMinutes(30), QueryMode.AVERAGE), TOLERANCE,
                "Estimate power " + now.plusMinutes(30));

        assertEquals(2.046, solcastForecast.getActualPowerValue(now, QueryMode.OPTIMISTIC), TOLERANCE,
                "Optimistic power " + now);
        assertEquals(1.864, solcastForecast.getActualPowerValue(now.plusMinutes(30), QueryMode.OPTIMISTIC), TOLERANCE,
                "Optimistic power " + now.plusMinutes(30));

        assertEquals(0.864, solcastForecast.getActualPowerValue(now, QueryMode.PESSIMISTIC), TOLERANCE,
                "Pessimistic power " + now);
        assertEquals(0.771, solcastForecast.getActualPowerValue(now.plusMinutes(30), QueryMode.PESSIMISTIC), TOLERANCE,
                "Pessimistic power " + now.plusMinutes(30));

        // get same values for optimistic / pessimistic and estimate in the past
        ZonedDateTime past = LocalDateTime.of(2022, 7, 17, 16, 30).atZone(timeZoneProvider.getTimeZone());
        assertEquals(1.932, solcastForecast.getActualPowerValue(past, QueryMode.AVERAGE), TOLERANCE,
                "Estimate power " + past);
        assertEquals(1.724, solcastForecast.getActualPowerValue(past.plusMinutes(30), QueryMode.AVERAGE), TOLERANCE,
                "Estimate power " + now.plusMinutes(30));

        assertEquals(1.932, solcastForecast.getActualPowerValue(past, QueryMode.OPTIMISTIC), TOLERANCE,
                "Optimistic power " + past);
        assertEquals(1.724, solcastForecast.getActualPowerValue(past.plusMinutes(30), QueryMode.OPTIMISTIC), TOLERANCE,
                "Optimistic power " + past.plusMinutes(30));

        assertEquals(1.932, solcastForecast.getActualPowerValue(past, QueryMode.PESSIMISTIC), TOLERANCE,
                "Pessimistic power " + past);
        assertEquals(1.724, solcastForecast.getActualPowerValue(past.plusMinutes(30), QueryMode.PESSIMISTIC), TOLERANCE,
                "Pessimistic power " + past.plusMinutes(30));
    }

    @Test
    void testForecastTreeMap() {
        setFixedTimeJul17();
        now = LocalDateTime.of(2022, 7, 17, 7, 0).atZone(timeZoneProvider.getTimeZone());
        assertEquals(0.42, solcastForecast.getActualEnergyValue(now, QueryMode.AVERAGE), TOLERANCE,
                "Actual estimation");
        assertEquals(25.413, solcastForecast.getDayTotal(now.toLocalDate(), QueryMode.AVERAGE), TOLERANCE, "Day total");
    }

    @Test
    void testActions() {
        assertEquals("2022-07-10T23:30+02:00[Europe/Berlin]",
                solcastForecast.getForecastBegin().atZone(timeZoneProvider.getTimeZone()).toString(), "Forecast begin");
        assertEquals("2022-07-24T23:00+02:00[Europe/Berlin]",
                solcastForecast.getForecastEnd().atZone(timeZoneProvider.getTimeZone()).toString(), "Forecast end");
        // test daily forecasts + cumulated getEnergy
        double totalEnergy = 0;
        ZonedDateTime start = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(timeZoneProvider.getTimeZone());
        for (int i = 0; i < 6; i++) {
            QuantityType<Energy> qt = solcastForecast.getDay(start.toLocalDate().plusDays(i));
            QuantityType<Energy> eqt = solcastForecast.getEnergy(start.plusDays(i).toInstant(),
                    start.plusDays(i + 1).toInstant());

            // check if energy calculation fits to daily query
            assertEquals(qt.doubleValue(), eqt.doubleValue(), TOLERANCE, "Total " + i + " days forecast");
            totalEnergy += qt.doubleValue();

            // check if sum is fitting to total energy query
            qt = solcastForecast.getEnergy(start.toInstant(), start.plusDays(i + 1).toInstant());
            assertEquals(totalEnergy, qt.doubleValue(), TOLERANCE * 2, "Total " + i + " days forecast");
        }
    }

    @Test
    void testOptimisticPessimistic() {
        assertEquals(19.389, solcastForecast.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.AVERAGE), TOLERANCE,
                "Estimation");
        assertEquals(7.358, solcastForecast.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.PESSIMISTIC),
                TOLERANCE, "Estimation");
        assertEquals(22.283, solcastForecast.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.OPTIMISTIC),
                TOLERANCE, "Estimation");
        assertEquals(23.316, solcastForecast.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.AVERAGE), TOLERANCE,
                "Estimation");
        assertEquals(9.8, solcastForecast.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.PESSIMISTIC), TOLERANCE,
                "Estimation");
        assertEquals(23.949, solcastForecast.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.OPTIMISTIC),
                TOLERANCE, "Estimation");

        Instant past = Utils.now().minus(5, ChronoUnit.MINUTES);
        try {
            solcastForecast.getPower(past, "total", "rubbish");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Too many arguments [total, rubbish]", e.getMessage(), "Too many qrguments");
        }
        try {
            solcastForecast.getPower(past.plus(2, ChronoUnit.HOURS), "rubbish");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "No enum constant org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode.RUBBISH",
                    e.getMessage(), "Rubbish argument");
        }
        try {
            solcastForecast.getPower(Utils.now().plus(8, ChronoUnit.DAYS));
            fail("Exception expected");
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
    }

    @Test
    void testInavlid() {
        ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        try {
            double d = solcastForecast.getActualEnergyValue(now, QueryMode.AVERAGE);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = solcastForecast.getActualEnergyValue(now, QueryMode.AVERAGE);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = solcastForecast.getDayTotal(now.toLocalDate(), QueryMode.AVERAGE);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(DAY_MISSING_INDICATOR),
                    "Expected: " + DAY_MISSING_INDICATOR + " Received: " + sfe.getMessage());
        }
    }

    @Test
    void testPowerInterpolation() {
        double startValue = solcastForecast.getActualPowerValue(now, QueryMode.AVERAGE);
        double endValue = solcastForecast.getActualPowerValue(now.plusMinutes(30), QueryMode.AVERAGE);
        for (int i = 0; i < 31; i++) {
            double interpolation = i / 30.0;
            double expected = ((1 - interpolation) * startValue) + (interpolation * endValue);
            assertEquals(expected, solcastForecast.getActualPowerValue(now.plusMinutes(i), QueryMode.AVERAGE),
                    TOLERANCE, "Step " + i);
        }
    }

    @Test
    void testEnergyInterpolation() {
        double maxDiff = 0;
        double productionExpected = 0;
        for (int i = 0; i < 1000; i++) {
            double forecast = solcastForecast.getActualEnergyValue(now.plusMinutes(i), QueryMode.AVERAGE);
            double addOnExpected = solcastForecast.getActualPowerValue(now.plusMinutes(i), QueryMode.AVERAGE) / 60.0;
            productionExpected += addOnExpected;
            double diff = forecast - productionExpected;
            maxDiff = Math.max(diff, maxDiff);
            assertEquals(productionExpected,
                    solcastForecast.getActualEnergyValue(now.plusMinutes(i), QueryMode.AVERAGE), 100 * TOLERANCE,
                    "Step " + i);
        }
    }

    @Test
    void testUnitDetection() {
        assertEquals("kW", SolcastConstants.KILOWATT_UNIT.toString(), "Kilowatt");
        assertEquals("W", Units.WATT.toString(), "Watt");
    }

    @Test
    void testPowerTimeSeries() {
        setFixedTimeJul18();
        TimeSeries powerSeries = solcastForecast.getPowerTimeSeries(QueryMode.AVERAGE);
        List<QuantityType<?>> estimateL = new ArrayList<>();
        assertEquals(303, powerSeries.size());
        powerSeries.getStates().forEachOrdered(entry -> {
            assertTrue(entry.timestamp().isAfter(Utils.now().minus(30, ChronoUnit.MINUTES)));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<?>) s).getUnit().toString());
            if (s instanceof QuantityType<?> qt) {
                estimateL.add(qt);
            } else {
                fail();
            }
        });

        TimeSeries powerSeries10 = solcastForecast.getPowerTimeSeries(QueryMode.PESSIMISTIC);
        List<QuantityType<?>> estimate10 = new ArrayList<>();
        assertEquals(303, powerSeries10.size());
        powerSeries10.getStates().forEachOrdered(entry -> {
            assertTrue(entry.timestamp().isAfter(Utils.now().minus(30, ChronoUnit.MINUTES)));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<?>) s).getUnit().toString());
            if (s instanceof QuantityType<?> qt) {
                estimate10.add(qt);
            } else {
                fail();
            }
        });

        TimeSeries powerSeries90 = solcastForecast.getPowerTimeSeries(QueryMode.OPTIMISTIC);
        List<QuantityType<?>> estimate90 = new ArrayList<>();
        assertEquals(303, powerSeries90.size());
        powerSeries90.getStates().forEachOrdered(entry -> {
            assertTrue(entry.timestamp().isAfter(Utils.now().minus(30, ChronoUnit.MINUTES)));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<?>) s).getUnit().toString());
            if (s instanceof QuantityType<?> qt) {
                estimate90.add(qt);
            } else {
                fail();
            }
        });

        for (int i = 0; i < estimateL.size(); i++) {
            double lowValue = estimate10.get(i).doubleValue();
            double estValue = estimateL.get(i).doubleValue();
            double highValue = estimate90.get(i).doubleValue();
            assertTrue(lowValue <= estValue && estValue <= highValue);
        }
    }

    @Test
    void testEnergyTimeSeries() {
        setFixedTimeJul18();

        TimeSeries energySeries = solcastForecast.getEnergyTimeSeries(QueryMode.AVERAGE);
        List<QuantityType<?>> estimateL = new ArrayList<>();
        assertEquals(303, energySeries.size()); // 48 values each day for next 7 days
        energySeries.getStates().forEachOrdered(entry -> {
            assertTrue(Utils.isAfterOrEqual(entry.timestamp(), now.toInstant()));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<?>) s).getUnit().toString());
            if (s instanceof QuantityType<?> qt) {
                estimateL.add(qt);
            } else {
                fail();
            }
        });

        TimeSeries energySeries10 = solcastForecast.getEnergyTimeSeries(QueryMode.PESSIMISTIC);
        List<QuantityType<?>> estimate10 = new ArrayList<>();
        assertEquals(303, energySeries10.size()); // 48 values each day for next 7 days
        energySeries10.getStates().forEachOrdered(entry -> {
            assertTrue(Utils.isAfterOrEqual(entry.timestamp(), now.toInstant()));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<?>) s).getUnit().toString());
            if (s instanceof QuantityType<?> qt) {
                estimate10.add(qt);
            } else {
                fail();
            }
        });

        TimeSeries energySeries90 = solcastForecast.getEnergyTimeSeries(QueryMode.OPTIMISTIC);
        List<QuantityType<?>> estimate90 = new ArrayList<>();
        assertEquals(303, energySeries90.size()); // 48 values each day for next 7 days
        energySeries90.getStates().forEachOrdered(entry -> {
            assertTrue(Utils.isAfterOrEqual(entry.timestamp(), now.toInstant()));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<?>) s).getUnit().toString());
            if (s instanceof QuantityType<?> qt) {
                estimate90.add(qt);
            } else {
                fail();
            }
        });

        for (int i = 0; i < estimateL.size(); i++) {
            double lowValue = estimate10.get(i).doubleValue();
            double estValue = estimateL.get(i).doubleValue();
            double highValue = estimate90.get(i).doubleValue();
            assertTrue(lowValue <= estValue && estValue <= highValue);
        }
    }
}
