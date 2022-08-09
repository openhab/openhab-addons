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
package org.openhab.binding.solarforecast;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.Utils;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.solcast.SolcastConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SolcastTest} tests responses from forecast solar website
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastTest {
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Berlin");
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
        SolcastObject scfo = new SolcastObject(content, now);
        content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        scfo.join(content);

        // test one day, step ahead in time and cross check channel values
        double dayTotal = scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation);
        double actual = scfo.getActualValue(now, QueryMode.Estimation);
        double remain = scfo.getRemainingProduction(now, QueryMode.Estimation);
        assertEquals(0.0, actual, TOLERANCE, "Begin of day actual");
        assertEquals(23.107, remain, TOLERANCE, "Begin of day remaining");
        assertEquals(23.107, dayTotal, TOLERANCE, "Day total");
        assertEquals(0.0, scfo.getActualPowerValue(now, QueryMode.Estimation), TOLERANCE, "Begin of day power");
        for (int i = 0; i < 47; i++) {
            now = now.plusMinutes(30);
            double power = scfo.getActualPowerValue(now, QueryMode.Estimation) / 2.0;
            actual += power;
            assertEquals(actual, scfo.getActualValue(now, QueryMode.Estimation), TOLERANCE, "Actual at " + now);
            remain -= power;
            assertEquals(remain, scfo.getRemainingProduction(now, QueryMode.Estimation), TOLERANCE, "Remain at " + now);
            assertEquals(dayTotal, actual + remain, TOLERANCE, "Total sum at " + now);
        }
    }

    @Test
    void testPower() {
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 23, 16, 00).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now);
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
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(0.614, scfo.getActualValue(now, QueryMode.Estimation), TOLERANCE, "Actual estimation");
        assertEquals(25.413, scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation), TOLERANCE, "Day total");
    }

    @Test
    void testJoin() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now, QueryMode.Estimation), 0.01, "Invalid");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(19.408, scfo.getActualValue(now, QueryMode.Estimation), 0.01, "Actual data");
        assertEquals(23.107, scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation), 0.01, "Today data");
        JSONObject rawJson = new JSONObject(scfo.getRaw());
        assertTrue(rawJson.has("forecasts"));
        assertTrue(rawJson.has("estimated_actuals"));
    }

    @Test
    void testActions() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now, QueryMode.Estimation), 0.01, "Invalid");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);

        assertEquals("2022-07-10T23:30", scfo.getForecastBegin().toString(), "Forecast begin");
        assertEquals("2022-07-24T23:00", scfo.getForecastEnd().toString(), "Forecast end");
        // test daily forecasts + cumulated getEnergy
        double totalEnergy = 0;
        LocalDateTime ldtStartTime = LocalDateTime.of(2022, 7, 18, 0, 0);
        ZonedDateTime zdtStartTime = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(TEST_ZONE);
        for (int i = 0; i < 7; i++) {
            double day = scfo.getDayTotal(zdtStartTime.toLocalDate().plusDays(i), QueryMode.Estimation);
            QuantityType qt = (QuantityType<?>) scfo.getDay(ldtStartTime.toLocalDate().plusDays(i));
            QuantityType eqt = (QuantityType<?>) scfo.getEnergy(ldtStartTime.plusDays(i), ldtStartTime.plusDays(i + 1));
            // System.out.println("Day: " + day + " ADay: " + qt.doubleValue() + " EDay: " + eqt.doubleValue());
            totalEnergy += qt.doubleValue();

            qt = (QuantityType<?>) scfo.getEnergy(ldtStartTime, ldtStartTime.plusDays(i + 1));
            // System.out.println("Total: " + qt.doubleValue());
            assertEquals(totalEnergy, qt.doubleValue(), i * TOLERANCE, "Total " + i + " days forecast");
            // System.out.println(i + " done");
        }
    }

    @Test
    void testOptimisticPessimistic() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(19.389, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Estimation), TOLERANCE,
                "Estimation");
        assertEquals(7.358, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Pessimistic), TOLERANCE,
                "Estimation");
        assertEquals(22.283, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Optimistic), TOLERANCE,
                "Estimation");

        // access in past shall be rejected
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        assertEquals(UnDefType.UNDEF, scfo.getPower(past, SolarForecast.OPTIMISTIC), "Optimistic Power");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past, SolarForecast.PESSIMISTIC), "Pessimistic Power");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past, "total", "rubbish"), "Rubbish arguments");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past.plusHours(2), "total", "rubbish"), "Rubbish arguments");
        assertEquals(UnDefType.UNDEF, scfo.getPower(past), "Normal Power");

        LocalDate ld = LocalDate.of(2022, 7, 20);
        System.out.println(scfo.getDayTotal(ld, QueryMode.Estimation));
        System.out.println(scfo.getDayTotal(ld, QueryMode.Optimistic));
        System.out.println(scfo.getDayTotal(ld, QueryMode.Pessimistic));
    }

    @Test
    void testInavlid() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = ZonedDateTime.now(TEST_ZONE);
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now, QueryMode.Estimation), 0.01, "Data available - day not in");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(-1.0, scfo.getActualValue(now, QueryMode.Estimation), 0.01,
                "Data available after merge - day not in");
        assertEquals(-1.0, scfo.getDayTotal(now.toLocalDate(), QueryMode.Estimation), 0.01,
                "Data available after merge - day not in");
    }

    @Test
    void testTimeframes() {
        ZonedDateTime zdt = ZonedDateTime.of(2022, 7, 22, 17, 3, 10, 345, TEST_ZONE);
        assertEquals("17:15", Utils.getNextTimeframe(zdt).toLocalTime().toString(), "Q1");
        zdt = zdt.plusMinutes(20);
        assertEquals("17:30", Utils.getNextTimeframe(zdt).toLocalTime().toString(), "Q2");
        zdt = zdt.plusMinutes(3);
        assertEquals("17:30", Utils.getNextTimeframe(zdt).toLocalTime().toString(), "Q2");
        zdt = zdt.plusMinutes(5);
        assertEquals("17:45", Utils.getNextTimeframe(zdt).toLocalTime().toString(), "Q3");
        zdt = zdt.plusMinutes(25);
        assertEquals("18:00", Utils.getNextTimeframe(zdt).toLocalTime().toString(), "Q4");
        zdt = zdt.plusMinutes(6);
        assertEquals("18:15", Utils.getNextTimeframe(zdt).toLocalTime().toString(), "Q4");
    }

    @Test
    void testRawChannel() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(TEST_ZONE);
        SolcastObject sco = new SolcastObject(content, now);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);
        JSONObject joined = new JSONObject(sco.getRaw());
        assertTrue(joined.has("forecasts"), "Forecasts available");
        assertTrue(joined.has("estimated_actuals"), "Actual data available");
    }

    @Test
    void testUnitDetection() {
        assertEquals("kW", SolcastConstants.KILOWATT_UNIT.toString(), "Kilowatt");
        assertEquals("W", Units.WATT.toString(), "Watt");
    }

    @Test
    void testTimes() {
        String utcTimeString = "2022-07-17T19:30:00.0000000Z";
        ZonedDateTime zdt = SolcastObject.getZdtFromUTC(utcTimeString);
        assertEquals("2022-07-17T21:30+02:00[Europe/Berlin]", zdt.toString(), "ZonedDateTime");
        LocalDateTime ldt = zdt.toLocalDateTime();
        assertEquals("2022-07-17T21:30", ldt.toString(), "LocalDateTime");
        LocalTime lt = zdt.toLocalTime();
        assertEquals("21:30", lt.toString(), "LocalTime");
    }
}
