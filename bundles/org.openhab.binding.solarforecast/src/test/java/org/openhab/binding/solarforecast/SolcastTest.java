/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.mockito.Mockito.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.solcast.SolcastConstants;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastBridgeHandler;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastPlaneMock;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

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

    public static final String TOO_LATE_INDICATOR = "too late";
    public static final String DAY_MISSING_INDICATOR = "not available in forecast";

    public static SolcastObject scfo = new SolcastObject("sc-test", null, Instant.MIN, TIMEZONEPROVIDER,
            mock(Storage.class));
    public static ZonedDateTime now = ZonedDateTime.now(TEST_ZONE);

    static JSONArray getForecast() {
        String actuals = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        JSONArray actualsJson = (new JSONObject(actuals)).getJSONArray(KEY_ACTUALS);
        String forecasString = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        JSONArray forecastJson = (new JSONObject(forecasString)).getJSONArray(KEY_FORECAST);
        return SolcastPlaneMock.merge(actualsJson, forecastJson);
    }

    @BeforeAll
    static void setFixedTimeJul17() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-17T21:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, TEST_ZONE);
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(TIMEZONEPROVIDER);

        JSONArray forecastJson = getForecast();
        now = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(TEST_ZONE);
        scfo = new SolcastObject("sc-test", forecastJson, now.toInstant(), TIMEZONEPROVIDER, mock(Storage.class));
    }

    static void setFixedTimeJul18() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-18T14:23:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, TEST_ZONE);
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(TIMEZONEPROVIDER);

        JSONArray forecastJson = getForecast();
        now = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(TEST_ZONE);
        scfo = new SolcastObject("sc-test", forecastJson, now.toInstant(), TIMEZONEPROVIDER, mock(Storage.class));
    }

    static void httpActualResponse(HttpClient httpMock, int status, String content, String resourceId) {
        String currentEstimateUrl = String.format(CURRENT_ESTIMATE_URL, resourceId);
        Request actualsRequest = mock(Request.class);
        when(httpMock.newRequest(currentEstimateUrl)).thenReturn(actualsRequest);
        ContentResponse actualsResponse = mock(ContentResponse.class);
        try {
            when(actualsRequest.send()).thenReturn(actualsResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail();
        }
        when(actualsResponse.getContentAsString()).thenReturn(FileReader.readFileInString(content));
        when(actualsResponse.getStatus()).thenReturn(status);
    }

    static void httpForecastResponse(HttpClient httpMock, int status, String content, String resourceId) {
        String forecastUrl = String.format(FORECAST_URL, resourceId);
        Request forecastRequest = mock(Request.class);
        when(httpMock.newRequest(forecastUrl)).thenReturn(forecastRequest);
        ContentResponse forecastResponse = mock(ContentResponse.class);
        try {
            when(forecastRequest.send()).thenReturn(forecastResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail();
        }
        when(forecastResponse.getContentAsString()).thenReturn(FileReader.readFileInString(content));
        when(forecastResponse.getStatus()).thenReturn(status);
    }

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
     * "2022-07-18T14:30+02:00[Europe/Berlin]": 2.t09,
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
        // test one day, step ahead in time and cross check channel values
        double dayTotal = scfo.getDayTotal(now.toLocalDate(), QueryMode.Average);
        double actual = scfo.getActualEnergyValue(now, QueryMode.Average);
        double remain = scfo.getRemainingProduction(now, QueryMode.Average);
        assertEquals(0.0, actual, TOLERANCE, "Begin of day actual");
        assertEquals(23.107, remain, TOLERANCE, "Begin of day remaining");
        assertEquals(23.107, dayTotal, TOLERANCE, "Day total");
        assertEquals(0.0, scfo.getActualPowerValue(now, QueryMode.Average), TOLERANCE, "Begin of day power");
        double previousPower = 0;
        for (int i = 0; i < 47; i++) {
            now = now.plusMinutes(30);
            double power = scfo.getActualPowerValue(now, QueryMode.Average) / 2.0;
            double powerAddOn = ((power + previousPower) / 2.0);
            actual += powerAddOn;
            assertEquals(actual, scfo.getActualEnergyValue(now, QueryMode.Average), TOLERANCE, "Actual at " + now);
            remain -= powerAddOn;
            assertEquals(remain, scfo.getRemainingProduction(now, QueryMode.Average), TOLERANCE, "Remain at " + now);
            assertEquals(dayTotal, actual + remain, TOLERANCE, "Total sum at " + now);
            previousPower = power;
        }
    }

    @Test
    void testPower() {
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
        ZonedDateTime now = LocalDateTime.of(2022, 7, 23, 16, 00).atZone(TEST_ZONE);
        assertEquals(1.9176, scfo.getActualPowerValue(now, QueryMode.Average), TOLERANCE, "Estimate power " + now);
        assertEquals(1.9176, scfo.getPower(now.toInstant(), "average").doubleValue(), TOLERANCE,
                "Estimate power " + now);
        assertEquals(1.754, scfo.getActualPowerValue(now.plusMinutes(30), QueryMode.Average), TOLERANCE,
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
        assertEquals(1.932, scfo.getActualPowerValue(past, QueryMode.Average), TOLERANCE, "Estimate power " + past);
        assertEquals(1.724, scfo.getActualPowerValue(past.plusMinutes(30), QueryMode.Average), TOLERANCE,
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
        setFixedTimeJul17();
        now = LocalDateTime.of(2022, 7, 17, 7, 0).atZone(TEST_ZONE);
        assertEquals(0.42, scfo.getActualEnergyValue(now, QueryMode.Average), TOLERANCE, "Actual estimation");
        assertEquals(25.413, scfo.getDayTotal(now.toLocalDate(), QueryMode.Average), TOLERANCE, "Day total");
    }

    @Test
    void testActions() {
        assertEquals("2022-07-10T23:30+02:00[Europe/Berlin]", scfo.getForecastBegin().atZone(TEST_ZONE).toString(),
                "Forecast begin");
        assertEquals("2022-07-24T23:00+02:00[Europe/Berlin]", scfo.getForecastEnd().atZone(TEST_ZONE).toString(),
                "Forecast end");
        // test daily forecasts + cumulated getEnergy
        double totalEnergy = 0;
        ZonedDateTime start = LocalDateTime.of(2022, 7, 18, 0, 0).atZone(TEST_ZONE);
        for (int i = 0; i < 6; i++) {
            QuantityType<Energy> qt = scfo.getDay(start.toLocalDate().plusDays(i));
            QuantityType<Energy> eqt = scfo.getEnergy(start.plusDays(i).toInstant(), start.plusDays(i + 1).toInstant());

            // check if energy calculation fits to daily query
            assertEquals(qt.doubleValue(), eqt.doubleValue(), TOLERANCE, "Total " + i + " days forecast");
            totalEnergy += qt.doubleValue();

            // check if sum is fitting to total energy query
            qt = scfo.getEnergy(start.toInstant(), start.plusDays(i + 1).toInstant());
            assertEquals(totalEnergy, qt.doubleValue(), TOLERANCE * 2, "Total " + i + " days forecast");
        }
    }

    @Test
    void testOptimisticPessimistic() {
        assertEquals(19.389, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Average), TOLERANCE,
                "Estimation");
        assertEquals(7.358, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Pessimistic), TOLERANCE,
                "Estimation");
        assertEquals(22.283, scfo.getDayTotal(now.toLocalDate().plusDays(2), QueryMode.Optimistic), TOLERANCE,
                "Estimation");
        assertEquals(23.316, scfo.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.Average), TOLERANCE,
                "Estimation");
        assertEquals(9.8, scfo.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.Pessimistic), TOLERANCE,
                "Estimation");
        assertEquals(23.949, scfo.getDayTotal(now.toLocalDate().plusDays(6), QueryMode.Optimistic), TOLERANCE,
                "Estimation");

        // access in past shall be rejected
        Instant past = Utils.now().minus(5, ChronoUnit.MINUTES);
        try {
            scfo.getPower(past, SolarForecast.OPTIMISTIC);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Solcast argument optimistic only available for future values", e.getMessage(),
                    "Optimistic Power");
        }
        try {
            scfo.getPower(past, SolarForecast.PESSIMISTIC);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Solcast argument pessimistic only available for future values", e.getMessage(),
                    "Pessimistic Power");
        }
        try {
            scfo.getPower(past, "total", "rubbish");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Solcast doesn't support 2 arguments", e.getMessage(), "Too many qrguments");
        }
        try {
            scfo.getPower(past.plus(2, ChronoUnit.HOURS), "rubbish");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Solcast doesn't support argument rubbish", e.getMessage(), "Rubbish argument");
        }
        try {
            scfo.getPower(Utils.now().plus(8, ChronoUnit.DAYS));
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
        ZonedDateTime now = ZonedDateTime.now(TEST_ZONE);
        try {
            double d = scfo.getActualEnergyValue(now, QueryMode.Average);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = scfo.getActualEnergyValue(now, QueryMode.Average);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = scfo.getDayTotal(now.toLocalDate(), QueryMode.Average);
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
        double startValue = scfo.getActualPowerValue(now, QueryMode.Average);
        double endValue = scfo.getActualPowerValue(now.plusMinutes(30), QueryMode.Average);
        for (int i = 0; i < 31; i++) {
            double interpolation = i / 30.0;
            double expected = ((1 - interpolation) * startValue) + (interpolation * endValue);
            assertEquals(expected, scfo.getActualPowerValue(now.plusMinutes(i), QueryMode.Average), TOLERANCE,
                    "Step " + i);
        }
    }

    @Test
    void testEnergyInterpolation() {
        double maxDiff = 0;
        double productionExpected = 0;
        for (int i = 0; i < 1000; i++) {
            double forecast = scfo.getActualEnergyValue(now.plusMinutes(i), QueryMode.Average);
            double addOnExpected = scfo.getActualPowerValue(now.plusMinutes(i), QueryMode.Average) / 60.0;
            productionExpected += addOnExpected;
            double diff = forecast - productionExpected;
            maxDiff = Math.max(diff, maxDiff);
            assertEquals(productionExpected, scfo.getActualEnergyValue(now.plusMinutes(i), QueryMode.Average),
                    100 * TOLERANCE, "Step " + i);
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
        TimeSeries powerSeries = scfo.getPowerTimeSeries(QueryMode.Average);
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

        TimeSeries powerSeries10 = scfo.getPowerTimeSeries(QueryMode.Pessimistic);
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

        TimeSeries powerSeries90 = scfo.getPowerTimeSeries(QueryMode.Optimistic);
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

        TimeSeries energySeries = scfo.getEnergyTimeSeries(QueryMode.Average);
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

        TimeSeries energySeries10 = scfo.getEnergyTimeSeries(QueryMode.Pessimistic);
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

        TimeSeries energySeries90 = scfo.getEnergyTimeSeries(QueryMode.Optimistic);
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

    @Test
    void testCombinedPowerTimeSeries() {
        setFixedTimeJul17();
        BridgeImpl bi = new BridgeImpl(SolarForecastBindingConstants.SOLCAST_SITE, "bridge");
        SolcastBridgeHandler scbh = new SolcastBridgeHandler(bi, new TimeZP());
        bi.setHandler(scbh);
        CallbackMock cm = new CallbackMock();
        scbh.setCallback(cm);

        Thing thing = new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, "sc-plane-1-test");
        thing.setBridgeUID(bi.getUID());
        HttpClient httpMock = mock(HttpClient.class);
        Storage<String> store = new VolatileStorage<>();

        String resourceId1 = "plane-1";
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId1);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId1);
        SolcastPlaneMock scph1 = new SolcastPlaneMock(thing, httpMock, store);
        CallbackMock cm1 = new CallbackMock();
        cm1.setBridge(bi);
        scph1.setCallback(cm1);
        Map<String, Object> planeConfigMap1 = new HashMap<>();
        planeConfigMap1.put("resourceId", resourceId1);
        Configuration planeConfig1 = new Configuration(planeConfigMap1);
        scph1.updateConfig(planeConfig1);
        scph1.initialize();

        Thing thing2 = new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, "sc-plane-2-test");
        thing2.setBridgeUID(bi.getUID());

        String resourceId2 = "plane-2";
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId2);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId2);
        SolcastPlaneMock scph2 = new SolcastPlaneMock(thing2, httpMock, store);
        CallbackMock cm2 = new CallbackMock();
        cm2.setBridge(bi);
        scph2.setCallback(cm2);
        Map<String, Object> planeConfigMap2 = new HashMap<>();
        planeConfigMap2.put("resourceId", resourceId2);
        Configuration planeConfig2 = new Configuration(planeConfigMap2);
        scph2.updateConfig(planeConfig2);
        scph2.initialize();

        // simulate trigger of refresh job
        scbh.getData();

        TimeSeries ts1 = cm.getTimeSeries("solarforecast:sc-site:bridge:average#power-estimate");
        TimeSeries ts2 = cm2.getTimeSeries("solarforecast:sc-plane:sc-plane-2-test:average#power-estimate");
        assertEquals(338, ts1.size(), "TimeSeries size");
        assertEquals(338, ts2.size(), "TimeSeries size");
        Iterator<TimeSeries.Entry> iter1 = ts1.getStates().iterator();
        Iterator<TimeSeries.Entry> iter2 = ts2.getStates().iterator();
        while (iter1.hasNext()) {
            TimeSeries.Entry e1 = iter1.next();
            TimeSeries.Entry e2 = iter2.next();
            assertEquals("kW", ((QuantityType<?>) e1.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) e2.state()).getUnit().toString(), "Power Unit");
            assertEquals(((QuantityType<?>) e1.state()).doubleValue(), ((QuantityType<?>) e2.state()).doubleValue() * 2,
                    0.01, "Power Value");
        }
        scbh.dispose();
        scph1.dispose();
        scph2.dispose();
    }

    @Test
    void testRefreshManual() {
        setFixedTimeJul17();
        BridgeImpl bi = new BridgeImpl(SolarForecastBindingConstants.SOLCAST_SITE, "bridge");
        SolcastBridgeHandler scbh = new SolcastBridgeHandler(bi, new TimeZP());
        bi.setHandler(scbh);
        CallbackMock cm = new CallbackMock();
        scbh.setCallback(cm);

        Thing thing = new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, "sc-plane-1-test");
        thing.setBridgeUID(bi.getUID());
        HttpClient httpMock = mock(HttpClient.class);
        Storage<String> store = new VolatileStorage<>();

        String resourceId1 = "plane-1";
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId1);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId1);
        SolcastPlaneMock scph1 = new SolcastPlaneMock(thing, httpMock, store);
        CallbackMock cm1 = new CallbackMock();
        cm1.setBridge(bi);
        scph1.setCallback(cm1);
        Map<String, Object> planeConfigMap1 = new HashMap<>();
        planeConfigMap1.put("resourceId", resourceId1);
        planeConfigMap1.put("refreshInterval", 0);
        scph1.handleConfigurationUpdate(planeConfigMap1);
        scph1.initialize();
        scbh.getData();
        // no update shall happen
        assertEquals(Instant.MAX, scbh.getSolarForecasts().get(0).getForecastBegin(), "Bridge forecast begin");
        assertEquals(Instant.MIN, scbh.getSolarForecasts().get(0).getForecastEnd(), "Bridge forecast begin");
        assertEquals(Instant.MAX, scph1.getSolarForecasts().get(0).getForecastBegin(), "Plane 1 forecast begin");
        assertEquals(Instant.MIN, scph1.getSolarForecasts().get(0).getForecastEnd(), "Plane 1 forecast begin");

        String resourceId2 = "plane-2";
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId2);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId2);
        SolcastPlaneMock scph2 = new SolcastPlaneMock(thing, httpMock, store);
        CallbackMock cm2 = new CallbackMock();
        scph2.setCallback(cm2);
        cm2.setBridge(bi);
        Map<String, Object> planeConfigMap2 = new HashMap<>();
        planeConfigMap2.put("resourceId", resourceId2);
        planeConfigMap2.put("refreshInterval", 0);
        scph2.handleConfigurationUpdate(planeConfigMap2);
        scph2.initialize();
        scbh.getData();

        assertEquals(Instant.MAX, scbh.getSolarForecasts().get(0).getForecastBegin(), "Bridge forecast begin");
        assertEquals(Instant.MIN, scbh.getSolarForecasts().get(0).getForecastEnd(), "Bridge forecast begin");
        assertEquals(Instant.MAX, scbh.getSolarForecasts().get(1).getForecastBegin(), "Bridge forecast begin");
        assertEquals(Instant.MIN, scbh.getSolarForecasts().get(1).getForecastEnd(), "Bridge forecast begin");
        assertEquals(Instant.MAX, scph1.getSolarForecasts().get(0).getForecastBegin(), "Plane 1 forecast begin");
        assertEquals(Instant.MIN, scph1.getSolarForecasts().get(0).getForecastEnd(), "Plane 1 forecast begin");
        assertEquals(Instant.MAX, scph2.getSolarForecasts().get(0).getForecastBegin(), "Plane 2 forecast begin");
        assertEquals(Instant.MIN, scph2.getSolarForecasts().get(0).getForecastEnd(), "Plane 2 forecast begin");

        planeConfigMap1.put("refreshInterval", 5);
        planeConfigMap2.put("refreshInterval", 5);
        scph1.handleConfigurationUpdate(planeConfigMap1);
        scph1.initialize();
        scph2.handleConfigurationUpdate(planeConfigMap2);
        scph2.initialize();
        scbh.getData();
        assertEquals(Instant.parse("2022-07-17T20:30:00Z"),
                scbh.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.Average).getBegin(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                scbh.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.Average).getEnd(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-17T20:30:00Z"),
                scbh.getSolarForecasts().get(1).getPowerTimeSeries(QueryMode.Average).getBegin(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                scbh.getSolarForecasts().get(1).getPowerTimeSeries(QueryMode.Average).getEnd(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-17T20:30:00Z"),
                scph1.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.Average).getBegin(),
                "Plane 1 forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                scph1.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.Average).getEnd(),
                "Plane 1 forecast begin");
        assertEquals(Instant.parse("2022-07-17T20:30:00Z"),
                scph2.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.Average).getBegin(),
                "Plane 2 forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                scph2.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.Average).getEnd(),
                "Plane 2 forecast begin");
        scbh.dispose();
        scph1.dispose();
        scph2.dispose();
    }

    @Test
    void testCombinedEnergyTimeSeries() {
        setFixedTimeJul18();
        BridgeImpl bi = new BridgeImpl(SolarForecastBindingConstants.SOLCAST_SITE, "bridge");
        SolcastBridgeHandler scbh = new SolcastBridgeHandler(bi, new TimeZP());
        bi.setHandler(scbh);
        CallbackMock cm = new CallbackMock();
        scbh.setCallback(cm);

        Thing thing = new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, "sc-plane-1-test");
        thing.setBridgeUID(bi.getUID());
        HttpClient httpMock = mock(HttpClient.class);
        Storage<String> store = new VolatileStorage<>();

        String resourceId1 = "plane-1";
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId1);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId1);
        SolcastPlaneMock scph1 = new SolcastPlaneMock(thing, httpMock, store);
        CallbackMock cm1 = new CallbackMock();
        cm1.setBridge(bi);
        scph1.setCallback(cm1);
        Map<String, Object> planeConfigMap1 = new HashMap<>();
        planeConfigMap1.put("resourceId", resourceId1);
        Configuration planeConfig1 = new Configuration(planeConfigMap1);
        scph1.updateConfig(planeConfig1);
        scph1.initialize();

        Thing thing2 = new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, "sc-plane-2-test");
        thing2.setBridgeUID(bi.getUID());

        String resourceId2 = "plane-2";
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId2);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId2);
        SolcastPlaneMock scph2 = new SolcastPlaneMock(thing2, httpMock, store);
        CallbackMock cm2 = new CallbackMock();
        cm2.setBridge(bi);
        scph2.setCallback(cm2);
        Map<String, Object> planeConfigMap2 = new HashMap<>();
        planeConfigMap2.put("resourceId", resourceId2);
        Configuration planeConfig2 = new Configuration(planeConfigMap2);
        scph2.updateConfig(planeConfig2);
        scph2.initialize();

        // simulate trigger of refresh job
        scbh.getData();

        TimeSeries ts1 = cm.getTimeSeries("solarforecast:sc-site:bridge:average#energy-estimate");
        TimeSeries ts2 = cm2.getTimeSeries("solarforecast:sc-plane:sc-plane-2-test:average#energy-estimate");
        assertEquals(303, ts1.size(), "TimeSeries size");
        assertEquals(303, ts2.size(), "TimeSeries size");

        Iterator<TimeSeries.Entry> iter1 = ts1.getStates().iterator();
        Iterator<TimeSeries.Entry> iter2 = ts2.getStates().iterator();
        while (iter1.hasNext()) {
            TimeSeries.Entry e1 = iter1.next();
            TimeSeries.Entry e2 = iter2.next();
            assertEquals("kWh", ((QuantityType<?>) e1.state()).getUnit().toString(), "Power Unit");
            assertEquals("kWh", ((QuantityType<?>) e2.state()).getUnit().toString(), "Power Unit");
            assertEquals(((QuantityType<?>) e1.state()).doubleValue(), ((QuantityType<?>) e2.state()).doubleValue() * 2,
                    0.1, "Power Value");
        }
        scbh.dispose();
        scph1.dispose();
        scph2.dispose();
    }

    @Test
    void testSingleEnergyTimeSeries() {
        setFixedTimeJul18();
        BridgeImpl bi = new BridgeImpl(SolarForecastBindingConstants.SOLCAST_SITE, "bridge");
        SolcastBridgeHandler scbh = new SolcastBridgeHandler(bi, new TimeZP());
        bi.setHandler(scbh);
        CallbackMock cm = new CallbackMock();
        scbh.setCallback(cm);

        Thing thing = new ThingImpl(SolarForecastBindingConstants.SOLCAST_PLANE, "sc-plane-1-test");
        thing.setBridgeUID(bi.getUID());
        Storage<String> store = new VolatileStorage<>();

        thing.setBridgeUID(bi.getUID());

        String resourceId = "plane-1";
        HttpClient httpMock = mock(HttpClient.class);
        httpActualResponse(httpMock, 200, "src/test/resources/solcast/estimated-actuals.json", resourceId);
        httpForecastResponse(httpMock, 200, "src/test/resources/solcast/forecasts.json", resourceId);

        SolcastPlaneMock scph1 = new SolcastPlaneMock(thing, httpMock, store);
        CallbackMock cm1 = new CallbackMock();
        scph1.setCallback(cm1);
        cm1.setBridge(bi);
        Map<String, Object> planeConfigMap = new HashMap<>();
        planeConfigMap.put("resourceId", resourceId);
        Configuration planeConfig = new Configuration(planeConfigMap);
        scph1.updateConfig(planeConfig);
        scph1.initialize();

        // simulate trigger of refresh job
        scbh.getData();

        TimeSeries ts1 = cm.getTimeSeries("solarforecast:sc-site:bridge:average#energy-estimate");
        assertEquals(303, ts1.size(), "TimeSeries size");
        Iterator<TimeSeries.Entry> iter1 = ts1.getStates().iterator();
        while (iter1.hasNext()) {
            TimeSeries.Entry e1 = iter1.next();
            assertEquals("kWh", ((QuantityType<?>) e1.state()).getUnit().toString(), "Power Unit");
        }
    }

    @Test
    void testStorageRead() {
        JSONArray forecastJson = getForecast();

        Storage<String> store = new VolatileStorage<>();
        scfo = new SolcastObject("sc-test", null, Instant.MIN, TIMEZONEPROVIDER, store);
        // assert values without stored object
        assertEquals(Instant.MAX, scfo.getForecastBegin(), "Forecast invalid begin");
        assertEquals(Instant.MIN, scfo.getForecastEnd(), "Forecast invalid end");
        assertTrue(scfo.isExpired());

        store.put("sc-test-forecast", forecastJson.toString());
        store.put("sc-test-expiration", Instant.now().plus(1, ChronoUnit.HOURS).toString());
        scfo = new SolcastObject("sc-test", null, Instant.MIN, TIMEZONEPROVIDER, store);
        // assert values after storeage is filled
        assertEquals("2022-07-10T21:30:00Z", scfo.getForecastBegin().toString(), "Forecast invalid begin");
        assertEquals("2022-07-24T21:00:00Z", scfo.getForecastEnd().toString(), "Forecast invalid end");
        assertFalse(scfo.isExpired());
    }

    @Test
    void testStorageWrite() {
        JSONArray forecastJson = getForecast();

        Storage<String> store = new VolatileStorage<>();
        scfo = new SolcastObject("sc-test", forecastJson, Instant.now().plus(1, ChronoUnit.HOURS), TIMEZONEPROVIDER,
                store);

        // store shall be filled now after instatiation
        // create new new SolcastObject to read from store
        scfo = new SolcastObject("sc-test", null, Instant.MIN, TIMEZONEPROVIDER, store);
        assertEquals("2022-07-10T21:30:00Z", scfo.getForecastBegin().toString(), "Forecast invalid begin");
        assertEquals("2022-07-24T21:00:00Z", scfo.getForecastEnd().toString(), "Forecast invalid end");
        assertFalse(scfo.isExpired());
    }

    @Test
    void testMergeArrays() {
        String actuals = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        JSONArray actualsJson = (new JSONObject(actuals)).getJSONArray(KEY_ACTUALS);
        String forecasString = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        JSONArray forecastJson = (new JSONObject(forecasString)).getJSONArray(KEY_FORECAST);
        assertEquals(336, actualsJson.length());
        assertEquals(336, forecastJson.length());
        JSONArray wholeForecast = SolcastPlaneMock.merge(actualsJson, forecastJson);
        assertEquals(336 + 336, wholeForecast.length());
    }

    @Test
    void testMergeWithOverride() {
        // Simulation of plane update
        setFixedTimeJul18();

        // Get todays values of present forecast
        String before = FileReader.readFileInString("src/test/resources/solcast/forecast-before.json");
        JSONArray beforeJson = new JSONArray(before);
        JSONArray todayJson = SolcastPlaneMock.getTodaysJson(beforeJson);
        assertEquals(48, todayJson.length());
        todayJson.forEach(entry -> {
            int estimateValue = ((JSONObject) entry).getInt(KEY_ESTIMATE);
            assertTrue(estimateValue == 0 || estimateValue == 1);
        });

        // New forecast with partly data of today - all estimates = 1
        String after = FileReader.readFileInString("src/test/resources/solcast/forecast-after.json");
        JSONArray afterJson = new JSONArray(after);
        JSONArray todayJsonCheck = SolcastPlaneMock.getTodaysJson(afterJson);
        assertEquals(20, todayJsonCheck.length());
        // 20 values today, 10 tomorrow
        assertEquals(20 + 10, afterJson.length());

        // get forecast earliest daretime
        Instant forecastBegin = Instant.MAX;
        for (int i = 0; i < todayJsonCheck.length(); i++) {
            String dateTime = todayJsonCheck.getJSONObject(i).getString(KEY_PERIOD_END);
            Instant entryDatetime = Instant.parse(dateTime);
            if (entryDatetime.isBefore(forecastBegin)) {
                forecastBegin = entryDatetime;
            }
        }

        // merge them and check parts of todays values are overridden with new forecast - estimates = 2
        JSONArray wholeForecast = SolcastPlaneMock.merge(todayJson, afterJson);
        // 48 values today plus 10 from forecast tomorrow
        assertEquals(48 + 10, wholeForecast.length());

        // Check for 28 values from "before" and 20 "after"
        int countBefore = 0;
        int countAfter = 0;
        for (int i = 0; i < wholeForecast.length(); i++) {
            JSONObject entry = wholeForecast.getJSONObject(i);
            int estimateValue = entry.getInt(KEY_ESTIMATE);
            Instant entryDatetime = Instant.parse(entry.getString(KEY_PERIOD_END));
            if (entryDatetime.isBefore(forecastBegin)) {
                assertTrue(estimateValue == 0 || estimateValue == 1, "Value fail for " + entry);
                countBefore++;
            } else {
                assertTrue(estimateValue == 0 || estimateValue == 2, "Value fail for " + entry);
                countAfter++;
            }
        }
        assertEquals(28, countBefore, "Persisted value count");
        assertEquals(30, countAfter, "Forecast value count");
    }

    @Test
    void testTodayValues() {
        JSONArray forecastJson = getForecast();
        JSONArray todayArray = SolcastPlaneMock.getTodaysJson(forecastJson);
        assertEquals(48, todayArray.length());
        // 30 minutes interval gives 48 values for each day

        LocalDate today = ZonedDateTime.now(Utils.getClock()).toLocalDate();
        todayArray.forEach(entry -> {
            JSONObject todayEntry = (JSONObject) entry;
            String periodEnd = todayEntry.getString(KEY_PERIOD_END);
            ZonedDateTime periodEndZdt = Utils.getZdtFromUTC(periodEnd);
            assertEquals(today.toString(), periodEndZdt.toLocalDate().toString(), "Same day");
        });
    }
}
