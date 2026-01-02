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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.SolarForecastException;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarBridgeMock;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarMockFactory;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneMock;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
    public static final String NO_FORECAST_INDICATOR = "No forecast data";
    public static final String DAY_MISSING_INDICATOR = "not available in forecast";

    @BeforeAll
    static void setFixedTime() {
        // Instant matching the date of test resources
        String fixedInstant = "2022-07-17T15:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), TEST_ZONE);
        Utils.setClock(fixedClock);
    }

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
            assertTrue(message.contains(NO_FORECAST_INDICATOR),
                    "Expected: " + NO_FORECAST_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getDayTotal(query.toLocalDate());
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(NO_FORECAST_INDICATOR),
                    "Expected: " + NO_FORECAST_INDICATOR + " Received: " + sfe.getMessage());
        }
        try {
            double d = fo.getDayTotal(query.plusDays(1).toLocalDate());
            fail("Exception expected instead of " + d);
        } catch (SolarForecastException sfe) {
            String message = sfe.getMessage();
            assertNotNull(message);
            assertTrue(message.contains(NO_FORECAST_INDICATOR),
                    "Expected: " + NO_FORECAST_INDICATOR + " Received: " + sfe.getMessage());
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
        Instant now = Utils.now().minus(1, ChronoUnit.HOURS);
        // 24 hours of data plus current hour = 25
        assertEquals(25, powerSeries.size());
        powerSeries.getStates().forEachOrdered(entry -> {
            assertTrue(Utils.isAfterOrEqual(entry.timestamp(), now));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kW", ((QuantityType<?>) s).getUnit().toString());
        });

        TimeSeries energySeries = fo.getEnergyTimeSeries(QueryMode.Average);
        assertEquals(25, energySeries.size());
        energySeries.getStates().forEachOrdered(entry -> {
            assertTrue(Utils.isAfterOrEqual(entry.timestamp(), now));
            State s = entry.state();
            assertTrue(s instanceof QuantityType<?>);
            assertEquals("kWh", ((QuantityType<?>) s).getUnit().toString());
        });
    }

    @Test
    void testPowerTimeSeries() {
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();
        ForecastSolarPlaneMock fsPlaneHandler1 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane1",
                "src/test/resources/forecastsolar/result.json");
        ForecastSolarPlaneMock fsPlaneHandler2 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane2",
                "src/test/resources/forecastsolar/result.json");

        fsBridgeHandler.forecastUpdate();

        CallbackMock cmSite = (CallbackMock) fsBridgeHandler.getCallback();
        CallbackMock cmPlane1 = (CallbackMock) fsPlaneHandler1.getCallback();
        CallbackMock cmPlane2 = (CallbackMock) fsPlaneHandler2.getCallback();
        assertNotNull(cmSite);
        assertNotNull(cmPlane1);
        assertNotNull(cmPlane2);

        TimeSeries tsSite = cmSite.getTimeSeries("solarforecast:fs-site:bridge:power-estimate");
        TimeSeries tsPlaneOne = cmPlane1.getTimeSeries("test::plane1:power-estimate");
        TimeSeries tsPlaneTwo = cmPlane2.getTimeSeries("test::plane2:power-estimate");

        Iterator<TimeSeries.Entry> siteIter = tsSite.getStates().iterator();
        Iterator<TimeSeries.Entry> plane1Iter = tsPlaneOne.getStates().iterator();
        Iterator<TimeSeries.Entry> plane2Iter = tsPlaneTwo.getStates().iterator();
        while (siteIter.hasNext()) {
            TimeSeries.Entry siteEntry = siteIter.next();
            TimeSeries.Entry plane1Entry = plane1Iter.next();
            TimeSeries.Entry plane2Entry = plane2Iter.next();
            assertEquals("kW", ((QuantityType<?>) siteEntry.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) plane1Entry.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) plane2Entry.state()).getUnit().toString(), "Power Unit");
            assertEquals(((QuantityType<?>) siteEntry.state()).doubleValue(),
                    ((QuantityType<?>) plane1Entry.state()).doubleValue()
                            + ((QuantityType<?>) plane2Entry.state()).doubleValue(),
                    0.1, "Power Value");
        }
    }

    @Test
    void testCommonForecastStartEnd() {
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();
        ForecastSolarPlaneMock fsPlaneHandler1 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane1",
                "src/test/resources/forecastsolar/result.json");
        ForecastSolarPlaneMock fsPlaneHandler2 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane2",
                "src/test/resources/forecastsolar/resultNextDay.json");

        fsBridgeHandler.forecastUpdate();

        CallbackMock cmSite = (CallbackMock) fsBridgeHandler.getCallback();
        CallbackMock cmPlane1 = (CallbackMock) fsPlaneHandler1.getCallback();
        CallbackMock cmPlane2 = (CallbackMock) fsPlaneHandler2.getCallback();
        assertNotNull(cmSite);
        assertNotNull(cmPlane1);
        assertNotNull(cmPlane2);

        TimeSeries tsSite = cmSite.getTimeSeries("solarforecast:fs-site:bridge:power-estimate");
        TimeSeries tsPlaneOne = cmPlane1.getTimeSeries("test::plane1:power-estimate");
        TimeSeries tsPlaneTwo = cmPlane2.getTimeSeries("test::plane2:power-estimate");

        Iterator<TimeSeries.Entry> siteIter = tsSite.getStates().iterator();
        while (siteIter.hasNext()) {
            TimeSeries.Entry siteEntry = siteIter.next();
            TimeSeries.Entry plane1Entry = null;
            Iterator<TimeSeries.Entry> planeIter1 = tsPlaneOne.getStates().iterator();
            while (planeIter1.hasNext()) {
                TimeSeries.Entry e = planeIter1.next();
                if (e.timestamp().equals(siteEntry.timestamp())) {
                    plane1Entry = e;
                    break;
                }
            }
            TimeSeries.Entry plane2Entry = null;
            Iterator<TimeSeries.Entry> planeIter2 = tsPlaneTwo.getStates().iterator();
            while (planeIter2.hasNext()) {
                TimeSeries.Entry e = planeIter2.next();
                if (e.timestamp().equals(siteEntry.timestamp())) {
                    plane2Entry = e;
                    break;
                }
            }
            assertNotNull(plane1Entry);
            assertNotNull(plane2Entry);
            assertEquals("kW", ((QuantityType<?>) plane1Entry.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) plane2Entry.state()).getUnit().toString(), "Power Unit");
            assertEquals("kW", ((QuantityType<?>) siteEntry.state()).getUnit().toString(), "Power Unit");
            assertEquals(
                    ((QuantityType<?>) plane1Entry.state()).doubleValue()
                            + ((QuantityType<?>) plane2Entry.state()).doubleValue(),
                    ((QuantityType<?>) siteEntry.state()).doubleValue(), 0.1, "Power Value");
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
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();
        ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane1",
                "src/test/resources/forecastsolar/result.json");
        ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane2",
                "src/test/resources/forecastsolar/resultNextDay.json");
        fsBridgeHandler.forecastUpdate();

        SolarForecastActions sfa = new SolarForecastActions();
        sfa.setThingHandler(fsBridgeHandler);
        // only one day shall be reported which is available in both planes
        LocalDate ld = LocalDate.of(2022, 7, 18);
        assertEquals(ld.atStartOfDay(ZoneId.of("UTC")).toInstant(), sfa.getForecastBegin().truncatedTo(ChronoUnit.DAYS),
                "TimeSeries start");
        assertEquals(ld.atStartOfDay(ZoneId.of("UTC")).toInstant(), sfa.getForecastEnd().truncatedTo(ChronoUnit.DAYS),
                "TimeSeries end");
    }

    @Test
    void testEnergyTimeSeries() {
        ForecastSolarBridgeMock fsBridgeHandler = ForecastSolarMockFactory.createBridgeHandler();
        ForecastSolarPlaneMock fsPlaneHandler1 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane1",
                "src/test/resources/forecastsolar/result.json");
        ForecastSolarPlaneMock fsPlaneHandler2 = ForecastSolarMockFactory.createPlaneHandler(fsBridgeHandler, "plane2",
                "src/test/resources/forecastsolar/result.json");

        fsBridgeHandler.forecastUpdate();

        CallbackMock cmSite = (CallbackMock) fsBridgeHandler.getCallback();
        CallbackMock cmPlane1 = (CallbackMock) fsPlaneHandler1.getCallback();
        CallbackMock cmPlane2 = (CallbackMock) fsPlaneHandler2.getCallback();
        assertNotNull(cmSite);
        assertNotNull(cmPlane1);
        assertNotNull(cmPlane2);

        TimeSeries tsSite = cmSite.getTimeSeries("solarforecast:fs-site:bridge:energy-estimate");
        TimeSeries tsPlaneOne = cmPlane1.getTimeSeries("test::plane1:energy-estimate");
        TimeSeries tsPlaneTwo = cmPlane2.getTimeSeries("test::plane2:energy-estimate");

        Iterator<TimeSeries.Entry> siteIter = tsSite.getStates().iterator();
        Iterator<TimeSeries.Entry> plane1Iter = tsPlaneOne.getStates().iterator();
        Iterator<TimeSeries.Entry> plane2Iter = tsPlaneTwo.getStates().iterator();
        while (siteIter.hasNext()) {
            TimeSeries.Entry siteEntry = siteIter.next();
            TimeSeries.Entry plane1Entry = plane1Iter.next();
            TimeSeries.Entry plane2Entry = plane2Iter.next();
            assertEquals("kWh", ((QuantityType<?>) siteEntry.state()).getUnit().toString(), "Energy Unit");
            assertEquals("kWh", ((QuantityType<?>) plane1Entry.state()).getUnit().toString(), "Energy Unit");
            assertEquals("kWh", ((QuantityType<?>) plane2Entry.state()).getUnit().toString(), "Energy Unit");
            assertEquals(((QuantityType<?>) siteEntry.state()).doubleValue(),
                    ((QuantityType<?>) plane1Entry.state()).doubleValue()
                            + ((QuantityType<?>) plane2Entry.state()).doubleValue(),
                    0.1, "Energy Value");
        }
    }

    @Test
    void testCalmDown() {
        ForecastSolarBridgeMock fsbh = ForecastSolarMockFactory.createBridgeHandler();
        ForecastSolarMockFactory.createPlaneHandler(fsbh, "plane1", "src/test/resources/forecastsolar/result.json");
        CallbackMock cm = (CallbackMock) fsbh.getCallback();
        assertNotNull(cm);

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
        String fixedInstant = "2022-07-17T16:15:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(fixedInstant), ZoneId.of("UTC"));
        Utils.setClock(fixedClock);
        fsbh.handleCommand(
                new ChannelUID("solarforecast:fs-site:bridge:" + SolarForecastBindingConstants.CHANNEL_ENERGY_ACTUAL),
                RefreshType.REFRESH);
        assertEquals(3, cm.getStateList("solarforecast:fs-site:bridge:power-actual").size(), "Second update");
        assertEquals(ThingStatus.ONLINE, cm.getStatus().getStatus(), "Online");
    }
}
