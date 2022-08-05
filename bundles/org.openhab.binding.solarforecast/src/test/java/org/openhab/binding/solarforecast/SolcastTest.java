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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.solcast.SolcastConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastPlaneHandler;
import org.openhab.core.library.unit.Units;

/**
 * The {@link SolcastTest} tests responses from forecast solar website
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastTest {

    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 23, 16, 23).atZone(ZoneId.systemDefault());
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(16.809, scfo.getActualValue(now), 0.001, "Actual estimation");
    }

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
    @Test
    void testActualPower() {
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 23, 16, 23).atZone(ZoneId.systemDefault());
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(1.855, scfo.getActualPowerValue(now), 0.001, "Actual estimation");

        ZonedDateTime zdt = LocalDateTime.of(2022, 7, 23, 0, 5).atZone(ZoneId.systemDefault());
        for (int i = 0; i < 96; i++) {
            zdt = zdt.plusMinutes(15);
            System.out.println(zdt + " " + scfo.getActualPowerValue(zdt));
        }
    }

    /**
     * Data from TreeMap for manual validation
     * 2022-07-17T04:30+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T05:00+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T05:30+02:00[Europe/Berlin]=0.0,
     * 2022-07-17T06:00+02:00[Europe/Berlin]=0.0262,
     * 2022-07-17T06:30+02:00[Europe/Berlin]=0.4252,
     * 2022-07-17T07:00+02:00[Europe/Berlin]=0.7772,
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
     * 2022-07-17T16:00+02:00[Europe/Berlin]=2.1015, <=
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
     * <= 41,0262
     */
    @Test
    void testForecastTreeMap() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(ZoneId.systemDefault());
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(25.413, scfo.getDayTotal(now, 0), 0.001, "Day total");
        assertEquals(21.254, scfo.getActualValue(now), 0.001, "Actual estimation");
    }

    @Test
    void testJoin() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(ZoneId.systemDefault());
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now), 0.01, "Invalid");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(19.408, scfo.getActualValue(now), 0.01, "Actual data");
        assertEquals(23.107, scfo.getDayTotal(now, 0), 0.01, "Today data");
        JSONObject rawJson = new JSONObject(scfo.getRaw());
        assertTrue(rawJson.has("forecasts"));
        assertTrue(rawJson.has("estimated_actuals"));
    }

    @Test
    void testOptimisticPessimistic() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(ZoneId.systemDefault());
        SolcastObject scfo = new SolcastObject(content, now);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(19.389, scfo.getDayTotal(now, 2), 0.001, "Estimation");
        assertEquals(7.358, scfo.getPessimisticDayTotal(now, 2), 0.001, "Estimation");
        assertEquals(22.283, scfo.getOptimisticDayTotal(now, 2), 0.001, "Estimation");
    }

    @Test
    void testInavlid() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now), 0.01, "Data available - day not in");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(-1.0, scfo.getActualValue(now), 0.01, "Data available after merge - day not in");
        assertEquals(-1.0, scfo.getDayTotal(now, 0), 0.01, "Data available after merge - day not in");
    }

    @Test
    void testRawChannel() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        ZonedDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23).atZone(ZoneId.systemDefault());
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
    void testTimeframes() {
        ZonedDateTime zdt = ZonedDateTime.of(2022, 7, 22, 17, 3, 10, 345, ZoneId.systemDefault());
        assertEquals("17:15", SolcastPlaneHandler.getNextTimeframe(zdt).toLocalTime().toString(), "Q1");
        zdt = zdt.plusMinutes(20);
        assertEquals("17:30", SolcastPlaneHandler.getNextTimeframe(zdt).toLocalTime().toString(), "Q2");
        zdt = zdt.plusMinutes(3);
        assertEquals("17:30", SolcastPlaneHandler.getNextTimeframe(zdt).toLocalTime().toString(), "Q2");
        zdt = zdt.plusMinutes(5);
        assertEquals("17:45", SolcastPlaneHandler.getNextTimeframe(zdt).toLocalTime().toString(), "Q3");
        zdt = zdt.plusMinutes(25);
        assertEquals("18:00", SolcastPlaneHandler.getNextTimeframe(zdt).toLocalTime().toString(), "Q4");
        zdt = zdt.plusMinutes(6);
        assertEquals("18:15", SolcastPlaneHandler.getNextTimeframe(zdt).toLocalTime().toString(), "Q4");
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
