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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Optional;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarBridgeHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneMock;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The {@link ForecastSolarTest} tests responses from forecast solar object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ForecastSolarTest {
    private static final double TOLERANCE = 0.001;
    public static final ZoneId TEST_ZONE = ZoneId.of("Europe/Berlin");
    public static final QuantityType<Power> POWER_UNDEF = Utils.getPowerState(-1);
    public static final QuantityType<Energy> ENERGY_UNDEF = Utils.getEnergyState(-1);

    public static final String TOO_EARLY_INDICATOR = "too early";
    public static final String TOO_LATE_INDICATOR = "too late";
    public static final String INVALID_RANGE_INDICATOR = "invalid time range";
    public static final String NO_GORECAST_INDICATOR = "No forecast data";
    public static final String DAY_MISSING_INDICATOR = "not available in forecast";

    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 17, 00).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject("fs-test", content,
                queryDateTime.toInstant().plus(1, ChronoUnit.DAYS));
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
        ForecastSolarObject fo = new ForecastSolarObject("fs-test", content,
                queryDateTime.toInstant().plus(1, ChronoUnit.DAYS));
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
        ForecastSolarObject fo = new ForecastSolarObject("fs-test", content,
                queryDateTime.toInstant().plus(1, ChronoUnit.DAYS));

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
        ForecastSolarObject fo = new ForecastSolarObject("fs-test", content,
                queryDateTime.toInstant().plus(1, ChronoUnit.DAYS));
        QuantityType<Energy> actual = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        QuantityType<Energy> st = Utils.getEnergyState(fo.getActualEnergyValue(queryDateTime));
        assertTrue(st instanceof QuantityType);
        actual = actual.add(st);
        assertEquals(49.431, actual.floatValue(), TOLERANCE, "Current Production");
        actual = actual.add(st);
        assertEquals(98.862, actual.floatValue(), TOLERANCE, "Doubled Current Production");
    }

    @Test
    void testCornerCases() {
        // invalid object
        ForecastSolarObject fo = new ForecastSolarObject("fs-test");
        ZonedDateTime query = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        try {
            double d = fo.getActualEnergyValue(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(INVALID_RANGE_INDICATOR),
                    "Expected: " + INVALID_RANGE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getRemainingProduction(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(NO_GORECAST_INDICATOR),
                    "Expected: " + NO_GORECAST_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getDayTotal(query.toLocalDate());
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(NO_GORECAST_INDICATOR),
                    "Expected: " + NO_GORECAST_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getDayTotal(query.plusDays(1).toLocalDate());
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(NO_GORECAST_INDICATOR),
                    "Expected: " + NO_GORECAST_INDICATOR + " Received: " + sfe.getMessage());
        }

        // valid object - query date one day too early
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        query = LocalDateTime.of(2022, 7, 16, 23, 59).atZone(TEST_ZONE);
        fo = new ForecastSolarObject("fs-test", content, query.toInstant());
        try {
            double d = fo.getActualEnergyValue(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_EARLY_INDICATOR),
                    "Expected: " + TOO_EARLY_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getRemainingProduction(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(DAY_MISSING_INDICATOR),
                    "Expected: " + DAY_MISSING_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getActualPowerValue(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_EARLY_INDICATOR),
                    "Expected: " + TOO_EARLY_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getDayTotal(query.toLocalDate());
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(DAY_MISSING_INDICATOR),
                    "Expected: " + DAY_MISSING_INDICATOR + " Received: " + sfe.getMessage());
        }

        // one minute later we reach a valid date
        query = query.plusMinutes(1);
        assertEquals(63.583, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getActualEnergyValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(63.583, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // valid object - query date one day too late
        query = LocalDateTime.of(2022, 7, 19, 0, 0).atZone(TEST_ZONE);
        try {
            double d = fo.getActualEnergyValue(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getRemainingProduction(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(DAY_MISSING_INDICATOR),
                    "Expected: " + DAY_MISSING_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getActualPowerValue(query);
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(TOO_LATE_INDICATOR),
                    "Expected: " + TOO_LATE_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getDayTotal(query.toLocalDate());
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(DAY_MISSING_INDICATOR),
                    "Expected: " + DAY_MISSING_INDICATOR + " Received: " + sfe.getMessage());
        }

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
    void testExceptions() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject("fs-test", content, queryDateTime.toInstant());
        assertEquals("2022-07-17T05:31:00",
                fo.getForecastBegin().atZone(TEST_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Forecast begin");
        assertEquals("2022-07-18T21:31:00",
                fo.getForecastEnd().atZone(TEST_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "Forecast end");
        assertEquals(QuantityType.valueOf(63.583, Units.KILOWATT_HOUR).toString(),
                fo.getDay(queryDateTime.toLocalDate()).toFullString(), "Actual out of scope");

        queryDateTime = LocalDateTime.of(2022, 7, 10, 0, 0).atZone(TEST_ZONE);
        // "watt_hours_day": {
        // "2022-07-17": 63583,
        // "2022-07-18": 65554
        // }
        try {
            fo.getEnergy(queryDateTime.toInstant(), queryDateTime.plusDays(2).toInstant());
            fail("Too early exception missing");
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("not available"), "not available expected: " + sfe.getMessage());
        }
        try {
            fo.getDay(queryDateTime.toLocalDate(), "optimistic");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("ForecastSolar doesn't accept arguments", e.getMessage(), "optimistic");
        }
        try {
            fo.getDay(queryDateTime.toLocalDate(), "pessimistic");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("ForecastSolar doesn't accept arguments", e.getMessage(), "pessimistic");
        }
        try {
            fo.getDay(queryDateTime.toLocalDate(), "total", "rubbish");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("ForecastSolar doesn't accept arguments", e.getMessage(), "rubbish");
        }
    }

    @Test
    void testTimeSeries() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ZonedDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 23).atZone(TEST_ZONE);
        ForecastSolarObject fo = new ForecastSolarObject("fs-test", content, queryDateTime.toInstant());

        TimeSeries powerSeries = fo.getPowerTimeSeries(QueryMode.Average);
        assertEquals(36, powerSeries.size()); // 18 values each day for 2 days
        powerSeries.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<?>) s).getUnit().toString());
        });

        TimeSeries energySeries = fo.getEnergyTimeSeries(QueryMode.Average);
        assertEquals(36, energySeries.size());
        energySeries.getStates().forEachOrdered(entry -> {
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<?>) s).getUnit().toString());
        });
    }

    @Test
    void testPowerTimeSeries() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
        ForecastSolarBridgeHandler fsbh = new ForecastSolarBridgeHandler(
                new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge"),
                Optional.of(PointType.valueOf("1,2")));
        CallbackMock cm = new CallbackMock();
        fsbh.setCallback(cm);

        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ForecastSolarObject fso1 = new ForecastSolarObject("fs-test", content, Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph1 = new ForecastSolarPlaneMock(fso1);
        fsbh.addPlane(fsph1);
        fsbh.forecastUpdate();
        TimeSeries ts1 = cm.getTimeSeries("solarforecast:fs-site:bridge:power-estimate");

        ForecastSolarPlaneHandler fsph2 = new ForecastSolarPlaneMock(fso1);
        fsbh.addPlane(fsph2);
        fsbh.forecastUpdate();
        TimeSeries ts2 = cm.getTimeSeries("solarforecast:fs-site:bridge:power-estimate");
        Iterator<TimeSeries.Entry> iter1 = ts1.getStates().iterator();
        Iterator<TimeSeries.Entry> iter2 = ts2.getStates().iterator();
        while (iter1.hasNext()) {
            TimeSeries.Entry e1 = iter1.next();
            TimeSeries.Entry e2 = iter2.next();
            assertEquals("kW", ((QuantityType<?>) e1.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) e2.state()).getUnit().toString(), "Power Unit");
            assertEquals(((QuantityType<?>) e1.state()).doubleValue(), ((QuantityType<?>) e2.state()).doubleValue() / 2,
                    0.1, "Power Value");
        }
    }

    @Test
    void testCommonForecastStartEnd() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
        ForecastSolarBridgeHandler fsbh = new ForecastSolarBridgeHandler(
                new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge"),
                Optional.of(PointType.valueOf("1,2")));
        CallbackMock cmSite = new CallbackMock();
        fsbh.setCallback(cmSite);

        String contentOne = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ForecastSolarObject fso1One = new ForecastSolarObject("fs-test", contentOne,
                Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph1 = new ForecastSolarPlaneMock(fso1One);
        fsbh.addPlane(fsph1);
        fsbh.forecastUpdate();

        String contentTwo = FileReader.readFileInString("src/test/resources/forecastsolar/resultNextDay.json");
        ForecastSolarObject fso1Two = new ForecastSolarObject("fs-plane", contentTwo,
                Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph2 = new ForecastSolarPlaneMock(fso1Two);
        CallbackMock cmPlane = new CallbackMock();
        fsph2.setCallback(cmPlane);
        ((ForecastSolarPlaneMock) fsph2).updateForecast(fso1Two);
        fsbh.addPlane(fsph2);
        fsbh.forecastUpdate();

        TimeSeries tsPlaneOne = cmPlane.getTimeSeries("test::plane:power-estimate");
        TimeSeries tsSite = cmSite.getTimeSeries("solarforecast:fs-site:bridge:power-estimate");
        Iterator<TimeSeries.Entry> planeIter = tsPlaneOne.getStates().iterator();
        Iterator<TimeSeries.Entry> siteIter = tsSite.getStates().iterator();
        while (siteIter.hasNext()) {
            TimeSeries.Entry planeEntry = planeIter.next();
            TimeSeries.Entry siteEntry = siteIter.next();
            assertEquals("kW", ((QuantityType<?>) planeEntry.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) siteEntry.state()).getUnit().toString(), "Power Unit");
            assertEquals(((QuantityType<?>) planeEntry.state()).doubleValue(),
                    ((QuantityType<?>) siteEntry.state()).doubleValue() / 2, 0.1, "Power Value");
        }
        // only one day shall be reported which is available in both planes
        LocalDate ld = LocalDate.of(2022, 7, 18);
        assertEquals(ld.atStartOfDay(ZoneId.of("UTC")).toInstant(), tsSite.getBegin().truncatedTo(ChronoUnit.DAYS),
                "TimeSeries start");
        assertEquals(ld.atStartOfDay(ZoneId.of("UTC")).toInstant(), tsSite.getEnd().truncatedTo(ChronoUnit.DAYS),
                "TimeSeries end");
    }

    @Test
    void testActions() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
        ForecastSolarBridgeHandler fsbh = new ForecastSolarBridgeHandler(
                new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge"),
                Optional.of(PointType.valueOf("1,2")));
        CallbackMock cmSite = new CallbackMock();
        fsbh.setCallback(cmSite);

        String contentOne = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ForecastSolarObject fso1One = new ForecastSolarObject("fs-test", contentOne,
                Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph1 = new ForecastSolarPlaneMock(fso1One);
        fsbh.addPlane(fsph1);
        fsbh.forecastUpdate();

        String contentTwo = FileReader.readFileInString("src/test/resources/forecastsolar/resultNextDay.json");
        ForecastSolarObject fso1Two = new ForecastSolarObject("fs-plane", contentTwo,
                Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph2 = new ForecastSolarPlaneMock(fso1Two);
        CallbackMock cmPlane = new CallbackMock();
        fsph2.setCallback(cmPlane);
        ((ForecastSolarPlaneMock) fsph2).updateForecast(fso1Two);
        fsbh.addPlane(fsph2);
        fsbh.forecastUpdate();

        SolarForecastActions sfa = new SolarForecastActions();
        sfa.setThingHandler(fsbh);
        // only one day shall be reported which is available in both planes
        LocalDate ld = LocalDate.of(2022, 7, 18);
        assertEquals(ld.atStartOfDay(ZoneId.of("UTC")).toInstant(), sfa.getForecastBegin().truncatedTo(ChronoUnit.DAYS),
                "TimeSeries start");
        assertEquals(ld.atStartOfDay(ZoneId.of("UTC")).toInstant(), sfa.getForecastEnd().truncatedTo(ChronoUnit.DAYS),
                "TimeSeries end");
    }

    @Test
    void testEnergyTimeSeries() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
        ForecastSolarBridgeHandler fsbh = new ForecastSolarBridgeHandler(
                new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge"),
                Optional.of(PointType.valueOf("1,2")));
        CallbackMock cm = new CallbackMock();
        fsbh.setCallback(cm);

        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ForecastSolarObject fso1 = new ForecastSolarObject("fs-test", content, Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph1 = new ForecastSolarPlaneMock(fso1);
        fsbh.addPlane(fsph1);
        fsbh.forecastUpdate();
        TimeSeries ts1 = cm.getTimeSeries("solarforecast:fs-site:bridge:energy-estimate");

        ForecastSolarPlaneHandler fsph2 = new ForecastSolarPlaneMock(fso1);
        fsbh.addPlane(fsph2);
        fsbh.forecastUpdate();
        TimeSeries ts2 = cm.getTimeSeries("solarforecast:fs-site:bridge:energy-estimate");
        Iterator<TimeSeries.Entry> iter1 = ts1.getStates().iterator();
        Iterator<TimeSeries.Entry> iter2 = ts2.getStates().iterator();
        while (iter1.hasNext()) {
            TimeSeries.Entry e1 = iter1.next();
            TimeSeries.Entry e2 = iter2.next();
            assertEquals("kWh", ((QuantityType<?>) e1.state()).getUnit().toString(), "Power Unit");
            assertEquals("kWh", ((QuantityType<?>) e2.state()).getUnit().toString(), "Power Unit");
            assertEquals(((QuantityType<?>) e1.state()).doubleValue(), ((QuantityType<?>) e2.state()).doubleValue() / 2,
                    0.1, "Power Value");
        }
    }

    @Test
    void testCalmDown() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
        ForecastSolarBridgeHandler fsbh = new ForecastSolarBridgeHandler(
                new BridgeImpl(SolarForecastBindingConstants.FORECAST_SOLAR_SITE, "bridge"),
                Optional.of(PointType.valueOf("1,2")));
        CallbackMock cm = new CallbackMock();
        fsbh.setCallback(cm);

        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        ForecastSolarObject fso1 = new ForecastSolarObject("fs-test", content, Instant.now().plus(1, ChronoUnit.DAYS));
        ForecastSolarPlaneHandler fsph1 = new ForecastSolarPlaneMock(fso1);
        fsbh.addPlane(fsph1);
        // first update after add plane - 1 state shall be received
        assertEquals(1, cm.getStateList("solarforecast:fs-site:bridge:power-actual").size(), "First update");
        assertEquals(ThingStatus.ONLINE, cm.getStatus().getStatus(), "Online");
        fsbh.handleCommand(
                new ChannelUID("solarforecast:fs-site:bridge:" + SolarForecastBindingConstants.CHANNEL_ENERGY_ACTUAL),
                RefreshType.REFRESH);
        // second update after refresh request - 2 states shall be received
        assertEquals(2, cm.getStateList("solarforecast:fs-site:bridge:power-actual").size(), "Second update");
        assertEquals(ThingStatus.ONLINE, cm.getStatus().getStatus(), "Online");

        fsbh.calmDown();
        fsbh.handleCommand(
                new ChannelUID("solarforecast:fs-site:bridge:" + SolarForecastBindingConstants.CHANNEL_ENERGY_ACTUAL),
                RefreshType.REFRESH);
        // after calm down refresh shall have no effect . still 2 states
        assertEquals(2, cm.getStateList("solarforecast:fs-site:bridge:power-actual").size(), "Calm update");
        assertEquals(ThingStatus.OFFLINE, cm.getStatus().getStatus(), "Offline");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, cm.getStatus().getStatusDetail(), "Offline");

        // forward Clock to get ONLINE again
        fixedInstant = "2022-07-17T16:15:00Z";
        fixedClock = Clock.fixed(Instant.parse(fixedInstant), ZoneId.of("UTC"));
        Utils.setClock(fixedClock);
        fsbh.handleCommand(
                new ChannelUID("solarforecast:fs-site:bridge:" + SolarForecastBindingConstants.CHANNEL_ENERGY_ACTUAL),
                RefreshType.REFRESH);
        assertEquals(3, cm.getStateList("solarforecast:fs-site:bridge:power-actual").size(), "Second update");
        assertEquals(ThingStatus.ONLINE, cm.getStatus().getStatus(), "Online");
    }
}
