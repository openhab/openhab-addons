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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.solcast.SolcastConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SolcastTest} tests responses from forecast solar website
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastTest {
    public static final ZoneId TEST_ZONE = ZoneId.of("Europe/Berlin");
    private static final TimeZP TIMEZONEPROVIDER = new TimeZP();
    // double comparison tolerance = 1 Watt
    private static final double TOLERANCE = 0.001;

    /**
     * "2022-07-18T00:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T00:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T01:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T01:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T02:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T02:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T03:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T03:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T04:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T04:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T05:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T05:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T06:00+02:00[Europe/Berlin]": 0.0205,
     * "2022-07-18T06:30+02:00[Europe/Berlin]": 0.1416,
     * "2022-07-18T07:00+02:00[Europe/Berlin]": 0.4478,
     * "2022-07-18T07:30+02:00[Europe/Berlin]": 0.763,
     * "2022-07-18T08:00+02:00[Europe/Berlin]": 1.1367,
     * "2022-07-18T08:30+02:00[Europe/Berlin]": 1.4044,
     * "2022-07-18T09:00+02:00[Europe/Berlin]": 1.6632,
     * "2022-07-18T09:30+02:00[Europe/Berlin]": 1.8667,
     * "2022-07-18T10:00+02:00[Europe/Berlin]": 2.0729,
     * "2022-07-18T10:30+02:00[Europe/Berlin]": 2.2377,
     * "2022-07-18T11:00+02:00[Europe/Berlin]": 2.3516,
     * "2022-07-18T11:30+02:00[Europe/Berlin]": 2.4295,
     * "2022-07-18T12:00+02:00[Europe/Berlin]": 2.5136,
     * "2022-07-18T12:30+02:00[Europe/Berlin]": 2.5295,
     * "2022-07-18T13:00+02:00[Europe/Berlin]": 2.526,
     * "2022-07-18T13:30+02:00[Europe/Berlin]": 2.4879,
     * "2022-07-18T14:00+02:00[Europe/Berlin]": 2.4092,
     * "2022-07-18T14:30+02:00[Europe/Berlin]": 2.3309,
     * "2022-07-18T15:00+02:00[Europe/Berlin]": 2.1984,
     * "2022-07-18T15:30+02:00[Europe/Berlin]": 2.0416,
     * "2022-07-18T16:00+02:00[Europe/Berlin]": 1.9076,
     * "2022-07-18T16:30+02:00[Europe/Berlin]": 1.7416,
     * "2022-07-18T17:00+02:00[Europe/Berlin]": 1.5414,
     * "2022-07-18T17:30+02:00[Europe/Berlin]": 1.3683,
     * "2022-07-18T18:00+02:00[Europe/Berlin]": 1.1603,
     * "2022-07-18T18:30+02:00[Europe/Berlin]": 0.9527,
     * "2022-07-18T19:00+02:00[Europe/Berlin]": 0.7705,
     * "2022-07-18T19:30+02:00[Europe/Berlin]": 0.5673,
     * "2022-07-18T20:00+02:00[Europe/Berlin]": 0.3588,
     * "2022-07-18T20:30+02:00[Europe/Berlin]": 0.1948,
     * "2022-07-18T21:00+02:00[Europe/Berlin]": 0.0654,
     * "2022-07-18T21:30+02:00[Europe/Berlin]": 0.0118,
     * "2022-07-18T22:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T22:30+02:00[Europe/Berlin]": 0,
     * "2022-07-18T23:00+02:00[Europe/Berlin]": 0,
     * "2022-07-18T23:30+02:00[Europe/Berlin]": 0
     **/
    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        scfo.join(content);
        // test one day, step ahead in time and cross check channel values
        double dayTotal = scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation);
        double actual = scfo.getActualEnergyValue(now, QueryMode.Estimation);
        double remain = scfo.getRemainingProduction(now, QueryMode.Estimation);
        assertEquals(0.0, actual, TOLERANCE, "Begin of day actual");
        assertEquals(23.107, remain, TOLERANCE, "Begin of day remaining");
        assertEquals(23.107, dayTotal, TOLERANCE, "Day total");
        assertEquals(0.0, scfo.getActualPowerValue(now, QueryMode.Estimation), TOLERANCE, "Begin of day power");
        double previousPower = 0;
        for (int i = 0; i < 47; i++) {
            now = now.plusMinutes(30);
            double power = scfo.getActualPowerValue(now, QueryMode.Estimation) / 2.0;
            double powerAddOn = ((power + previousPower) / 2.0);
            actual += powerAddOn;
            assertEquals(actual, scfo.getActualEnergyValue(now, QueryMode.Estimation), TOLERANCE, "Actual at " + now);
            remain -= powerAddOn;
            assertEquals(remain, scfo.getRemainingProduction(now, QueryMode.Estimation), TOLERANCE, "Remain at " + now);
            assertEquals(dayTotal, actual + remain, TOLERANCE, "Total sum at " + now);
            previousPower = power;
        }
    }

    @Test
    void testPower() {
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 23, 16, 00).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        scfo.join(content);

        /**
         * {
         * "pv_estimate": 1.9176,
         * "pv_estimate10": 0.8644,
         * "pv_estimate90": 2.0456,
         * "period_end": "2022-07-23T14:00:00.0000000Z",
         * "period": "PT30M"
         * },
         * {
         * "pv_estimate": 1.7544,
         * "pv_estimate10": 0.7708,
         * "pv_estimate90": 1.864,
         * "period_end": "2022-07-23T14:30:00.0000000Z",
         * "period": "PT30M"
         */
        assertEquals(1.9176, scfo.getActualPowerValue(now, QueryMode.Estimation), TOLERANCE, "Estimate power " + now);
        assertEquals(1.754, scfo.getActualPowerValue(now.plusMinutes(30), QueryMode.Estimation), TOLERANCE,
                "Estimate power " + now.plusMinutes(30));

        assertEquals(2.046, scfo.getActualPowerValue(now, QueryMode.Optimistic), TOLERANCE, "Optimistic power " + now);
        assertEquals(1.864, scfo.getActualPowerValue(now.plusMinutes(30), QueryMode.Optimistic), TOLERANCE,
                "Optimistic power " + now.plusMinutes(30));

        assertEquals(0.864, scfo.getActualPowerValue(now, QueryMode.Pessimistic), TOLERANCE,
                "Pessimistic power " + now);
        assertEquals(0.771, scfo.getActualPowerValue(now.plusMinutes(30), QueryMode.Pessimistic), TOLERANCE,
                "Pessimistic power " + now.plusMinutes(30));

        /**
         * {
         * "pv_estimate": 1.9318,
         * "period_end": "2022-07-17T14:30:00.0000000Z",
         * "period": "PT30M"
         * },
         * {
         * "pv_estimate": 1.724,
         * "period_end": "2022-07-17T15:00:00.0000000Z",
         * "period": "PT30M"
         * },
         **/
        // get same values for optimistic / pessimistic and estimate in the past
        ZonedDateTime past = LocalDateTime.of(2022, 7, 17, 16, 30).atZone(TEST_ZONE);
        assertEquals(1.932, scfo.getActualPowerValue(past, QueryMode.Estimation), TOLERANCE, "Estimate power " + past);
        assertEquals(1.724, scfo.getActualPowerValue(past.plusMinutes(30), QueryMode.Estimation), TOLERANCE,
                "Estimate power " + now.plusMinutes(30));

        assertEquals(1.932, scfo.getActualPowerValue(past, QueryMode.Optimistic), TOLERANCE,
                "Optimistic power " + past);
        assertEquals(1.724, scfo.getActualPowerValue(past.plusMinutes(30), QueryMode.Optimistic), TOLERANCE,
                "Optimistic power " + past.plusMinutes(30));

        assertEquals(1.932, scfo.getActualPowerValue(past, QueryMode.Pessimistic), TOLERANCE,
                "Pessimistic power " + past);
        assertEquals(1.724, scfo.getActualPowerValue(past.plusMinutes(30), QueryMode.Pessimistic), TOLERANCE,
                "Pessimistic power " + past.plusMinutes(30));
    }

    /**
     * Data from TreeMap for manual validation
     * 2022-07-17T04:30+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T05:00+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T05:30+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T06:00+02:00[Europe/Berlin]=0.0262,
     * 2022-07-17T06:30+02:00[Europe/Berlin]=0.4252,
     * 2022-07-17T07:00+02:00[Europe/Berlin]=0.7772, <<<
     * 2022-07-17T07:30+02:00[Europe/Berlin]=1.0663,
     * 2022-07-17T08:00+02:00[Europe/Berlin]=1.3848,
     * 2022-07-17T08:30+02:00[Europe/Berlin]=1.6401,
     * 2022-07-17T09:00+02:00[Europe/Berlin]=1.8614,
     * 2022-07-17T09:30+02:00[Europe/Berlin]=2.0613,
     * 2022-07-17T10:00+02:00[Europe/Berlin]=2.2365,
     * 2022-07-17T10:30+02:00[Europe/Berlin]=2.3766,
     * 2022-07-17T11:00+02:00[Europe/Berlin]=2.4719,
     * 2022-07-17T11:30+02:00[Europe/Berlin]=2.5438,
     * 2022-07-17T12:00+02:00[Europe/Berlin]=2.602,
     * 2022-07-17T12:30+02:00[Europe/Berlin]=2.6213,
     * 2022-07-17T13:00+02:00[Europe/Berlin]=2.6061,
     * 2022-07-17T13:30+02:00[Europe/Berlin]=2.6181,
     * 2022-07-17T14:00+02:00[Europe/Berlin]=2.5378,
     * 2022-07-17T14:30+02:00[Europe/Berlin]=2.4651,
     * 2022-07-17T15:00+02:00[Europe/Berlin]=2.3656,
     * 2022-07-17T15:30+02:00[Europe/Berlin]=2.2374,
     * 2022-07-17T16:00+02:00[Europe/Berlin]=2.1015,
     * 2022-07-17T16:30+02:00[Europe/Berlin]=1.9318,
     * 2022-07-17T17:00+02:00[Europe/Berlin]=1.724,
     * 2022-07-17T17:30+02:00[Europe/Berlin]=1.5031,
     * 2022-07-17T18:00+02:00[Europe/Berlin]=1.2834,
     * 2022-07-17T18:30+02:00[Europe/Berlin]=1.0839,
     * 2022-07-17T19:00+02:00[Europe/Berlin]=0.8581,
     * 2022-07-17T19:30+02:00[Europe/Berlin]=0.6164,
     * 2022-07-17T20:00+02:00[Europe/Berlin]=0.4465,
     * 2022-07-17T20:30+02:00[Europe/Berlin]=0.2543,
     * 2022-07-17T21:00+02:00[Europe/Berlin]=0.0848,
     * 2022-07-17T21:30+02:00[Europe/Berlin]=0.0132,
     * 2022-07-17T22:00+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T22:30+02:00[Europe/Berlin]=0.0
     *
     * <<< = 0.0262 + 0.4252 + 0.7772 = 1.2286 / 2 = 0.6143
     */
    @Test
    void testForecastTreeMap() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 17, 7, 0).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        assertEquals(0.42, scfo.getActualEnergyValue(now, QueryMode.Estimation), TOLERANCE, "Actual estimation");
        assertEquals(25.413, scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation), TOLERANCE, "Day total");
    }

    @Test
    void testJoin() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        assertEquals(-1.0, scfo.getActualEnergyValue(now, QueryMode.Estimation), 0.01, "Invalid");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(18.946, scfo.getActualEnergyValue(now, QueryMode.Estimation), 0.01, "Actual data");
        assertEquals(23.107, scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation), 0.01, "Today data");
        JSONObject rawJson = new JSONObject(scfo.getRaw());
        assertTrue(rawJson.has("forecasts"));
        assertTrue(rawJson.has("estimated_actuals"));
    }

    @Test
    void testActions() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        assertEquals(-1.0, scfo.getActualEnergyValue(now, QueryMode.Estimation), 0.01, "Invalid");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);

        assertEquals("2022-07-10T23:30+02:00[Europe/Berlin]", scfo.getForecastBegin().atZone(TEST_ZONE).toString(),
                "Forecast begin");
        assertEquals("2022-07-24T23:00+02:00[Europe/Berlin]", scfo.getForecastEnd().atZone(TEST_ZONE).toString(),
                "Forecast end");
        // test daily forecasts + cumulated getEnergy
        double totalEnergy = 0;
        ZonedDateTime start = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(TEST_ZONE);
        for (int i = 0; i < 7; i++) {
            QuantityType qt = (QuantityType<?>) scfo.getDay(start.toLocalDate().plusDays(i));
            QuantityType eqt = (QuantityType<?>) scfo.getEnergy(start.plusDays(i).toInstant(),
                    start.plusDays(i + 1).toInstant());

            // check if energy calculation fits to daily query
            assertEquals(qt.doubleValue(), eqt.doubleValue(), TOLERANCE, "Total " + i + " days forecast");
            // System.out.println("Day: " + day + " ADay: " + qt.doubleValue() + " EDay: " + eqt.doubleValue());
            totalEnergy += qt.doubleValue();

            // check if sum is fitting to total energy query
            qt = (QuantityType<?>) scfo.getEnergy(start.toInstant(), start.plusDays(i + 1).toInstant());
            // System.out.println("Total: " + qt.doubleValue());
            assertEquals(totalEnergy, qt.doubleValue(), TOLERANCE * 2, "Total " + i + " days forecast");
        }
    }

    @Test
    void testOptimisticPessimistic() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(19.389, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Estimation), TOLERANCE,
                "Estimation");
        assertEquals(7.358, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Pessimistic), TOLERANCE,
                "Estimation");
        assertEquals(22.283, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Optimistic), TOLERANCE,
                "Estimation");
        assertEquals(23.316, scfo.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.Estimation), TOLERANCE,
                "Estimation");
        assertEquals(9.8, scfo.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.Pessimistic), TOLERANCE,
                "Estimation");
        assertEquals(23.949, scfo.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.Optimistic), TOLERANCE,
                "Estimation");

        // access in past shall be rejected
        Instant past = Instant.now().minus(5, ChronoUnit.MINUTES);
        assertEquals(UnDefType.UNDEF, scfo.getPower(past, SolarForecast.OPTIMISTIC), "Optimistic Power");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past, SolarForecast.PESSIMISTIC), "Pessimistic Power");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past, "total", "rubbish"), "Rubbish arguments");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past.plus(2, ChronoUnit.HOURS), "total", "rubbish"),
                "Rubbish arguments");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past), "Normal Power");
    }

    @Test
    void testInavlid() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = ZonedDateTime.now(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        assertEquals(-1.0, scfo.getActualEnergyValue(now, QueryMode.Estimation), 0.01, "Data available - day not in");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(-1.0, scfo.getActualEnergyValue(now, QueryMode.Estimation), 0.01,
                "Data available after merge - day not in");
        assertEquals(-1.0, scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation), 0.01,
                "Data available after merge - day not in");
    }

    @Test
    void testPowerInterpolation() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 15, 0).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);

        double startValue = sco.getActualPowerValue(now, QueryMode.Estimation);
        double endValue = sco.getActualPowerValue(now.plusMinutes(30), QueryMode.Estimation);
        for (int i = 0; i < 31; i++) {
            // System.out.println(i + " : " + sco.getActualPowerValue(now.plusMinutes(i), QueryMode.Estimation));
            double interpolation = i / 30.0;
            double expected = ((1 - interpolation) * startValue) + (interpolation * endValue);
            assertEquals(expected, sco.getActualPowerValue(now.plusMinutes(i), QueryMode.Estimation), TOLERANCE,
                    "Step " + i);
        }
    }

    @Test
    void testEnergyInterpolation() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 5, 30).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);

        double maxDiff = 0;
        double productionExpected = 0;
        for (int i = 0; i < 1000; i++) {
            double forecast = sco.getActualEnergyValue(now.plusMinutes(i), QueryMode.Estimation);
            // int hour = now.plusMinutes(i).getHour();
            // int minute = now.plusMinutes(i).getMinute();
            double addOnExpected = sco.getActualPowerValue(now.plusMinutes(i), QueryMode.Estimation) / 60.0;
            productionExpected += addOnExpected;
            double diff = forecast - productionExpected;
            maxDiff = Math.max(diff, maxDiff);
            // System.out.println(hour + ":" + minute + " : Forecast " + Math.round(forecast * 1000) / 1000.0
            // + " - Expected " + Math.round(productionExpected * 1000) / 1000.0 + " - Diff "
            // + Math.round(diff * 1000) / 1000.0);
            assertEquals(productionExpected, sco.getActualEnergyValue(now.plusMinutes(i), QueryMode.Estimation),
                    100 * TOLERANCE, "Step " + i);
        }
    }

    @Test
    void testRawChannel() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);
        JSONObject joined = new JSONObject(sco.getRaw());
        assertTrue(joined.has("forecasts"), "Forecasts available");
        assertTrue(joined.has("estimated_actuals"), "Actual data available");
    }

    @Test
    void testUpdates() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);
        JSONObject joined = new JSONObject(sco.getRaw());
        assertTrue(joined.has("forecasts"), "Forecasts available");
        assertTrue(joined.has("estimated_actuals"), "Actual data available");
        // System.out.println(sco.getDay(LocalDate.now()));
    }

    @Test
    void testUnitDetection() {
        assertEquals("kW", SolcastConstants.KILOWATT_UNIT.toString(), "Kilowatt");
        assertEquals("W", Units.WATT.toString(), "Watt");
    }

    @Test
    void testTimes() {
        String utcTimeString = "2022-07-17T19:30:00.0000000Z";
        SolcastObject so = new SolcastObject(TIMEZONEPROVIDER);
        ZonedDateTime zdt = so.getZdtFromUTC(utcTimeString);
        assertEquals("2022-07-17T21:30+02:00[Europe/Berlin]", zdt.toString(), "ZonedDateTime");
        LocalDateTime ldt = zdt.toLocalDateTime();
        assertEquals("2022-07-17T21:30", ldt.toString(), "LocalDateTime");
        LocalTime lt = zdt.toLocalTime();
        assertEquals("21:30", lt.toString(), "LocalTime");
    }

    @Test
    void testPowerTimeSeries() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);

        TimeSeries powerSeries = sco.getPowerTimeSeries(QueryMode.Estimation);
        List<QuantityType<Power>> estimateL = new ArrayList<QuantityType<Power>>();
        assertEquals(672, powerSeries.size());
        powerSeries.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<Power>) s).getUnit().toString());
            estimateL.add((QuantityType<Power>) entry.state());
        });

        TimeSeries powerSeries10 = sco.getPowerTimeSeries(QueryMode.Pessimistic);
        List<QuantityType<Power>> estimate10 = new ArrayList<QuantityType<Power>>();
        assertEquals(672, powerSeries10.size());
        powerSeries10.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<Power>) s).getUnit().toString());
            estimate10.add((QuantityType<Power>) entry.state());
        });

        TimeSeries powerSeries90 = sco.getPowerTimeSeries(QueryMode.Optimistic);
        List<QuantityType<Power>> estimate90 = new ArrayList<QuantityType<Power>>();
        assertEquals(672, powerSeries90.size());
        powerSeries90.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<Power>) s).getUnit().toString());
            estimate90.add((QuantityType<Power>) entry.state());
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
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now.toInstant(), TIMEZONEPROVIDER);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);

        TimeSeries energySeries = sco.getEnergyTimeSeries(QueryMode.Estimation);
        List<QuantityType<Power>> estimateL = new ArrayList<QuantityType<Power>>();
        assertEquals(672, energySeries.size()); // 18 values each day for 2 days
        energySeries.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<Power>) s).getUnit().toString());
            estimateL.add((QuantityType<Power>) entry.state());
        });

        TimeSeries energySeries10 = sco.getEnergyTimeSeries(QueryMode.Pessimistic);
        List<QuantityType<Power>> estimate10 = new ArrayList<QuantityType<Power>>();
        assertEquals(672, energySeries10.size()); // 18 values each day for 2 days
        energySeries10.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<Power>) s).getUnit().toString());
            estimate10.add((QuantityType<Power>) entry.state());
        });

        TimeSeries energySeries90 = sco.getEnergyTimeSeries(QueryMode.Optimistic);
        List<QuantityType<Power>> estimate90 = new ArrayList<QuantityType<Power>>();
        assertEquals(672, energySeries90.size()); // 18 values each day for 2 days
        energySeries90.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<Power>) s).getUnit().toString());
            estimate90.add((QuantityType<Power>) entry.state());
        });

        for (int i = 0; i < estimateL.size(); i++) {
            double lowValue = estimate10.get(i).doubleValue();
            double estValue = estimateL.get(i).doubleValue();
            double highValue = estimate90.get(i).doubleValue();
            assertTrue(lowValue <= estValue && estValue <= highValue);
        }
    }
}
