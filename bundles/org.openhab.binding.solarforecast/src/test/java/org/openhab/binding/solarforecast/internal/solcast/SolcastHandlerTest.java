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
import static org.mockito.Mockito.mock;
import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.GROUP_AVERAGE;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.FileReader;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.mock.SolcastBridgeMock;
import org.openhab.binding.solarforecast.internal.solcast.mock.SolcastMockFactory;
import org.openhab.binding.solarforecast.internal.solcast.mock.SolcastPlaneMock;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The {@link SolcastHandlerTest} tests solcast handlers and helper objects related to configuration, initialization and
 * runtime behavior
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastHandlerTest {
    private static TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.of("Europe/Berlin");
        }
    };
    private static Storage<String> store = new VolatileStorage<>();

    @BeforeEach
    void setFixedTimeJul17() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-17T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, timeZoneProvider.getTimeZone());
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(timeZoneProvider);
    }

    /**
     * Set different time on request
     */
    static void setFixedTimeJul18() {
        // Instant matching the date of test resources
        Instant fixedInstant = Instant.parse("2022-07-18T14:23:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, timeZoneProvider.getTimeZone());
        Utils.setClock(fixedClock);
        Utils.setTimeZoneProvider(timeZoneProvider);
    }

    @Test
    void testQueryMode() {
        try {
            QueryMode.valueOf("nonsense");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        }

        QueryMode averageMode = QueryMode.valueOf("average".toUpperCase(Locale.ENGLISH));
        assertEquals(GROUP_AVERAGE, averageMode.toString(), "Average mode group");
    }

    @Test
    void testAssuredValues() {
        String actuals = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        JSONArray actualsJson = (new JSONObject(actuals)).getJSONArray(KEY_ACTUALS);
        actualsJson.forEach(entry -> {
            JSONObject forecastEntry = (JSONObject) entry;
            assertFalse(forecastEntry.has(KEY_ESTIMATE10));
            assertFalse(forecastEntry.has(KEY_ESTIMATE90));
        });
        SolcastCache cache = new SolcastCache("", store);
        cache.assureValues(actualsJson);
        actualsJson.forEach(entry -> {
            JSONObject forecastEntry = (JSONObject) entry;
            assertTrue(forecastEntry.getDouble(KEY_ESTIMATE10) >= 0.0, "Pessimistic estimate present");
            assertTrue(forecastEntry.getDouble(KEY_ESTIMATE90) >= 0.0, "Optimistic estimate present");
        });
    }

    @Test
    void testCacheRead() {
        String identifier = "solcast-test";
        store.put(identifier + FORECAST_APPENDIX, SolcastPlaneMock.getPreparedForecast().toString());
        // create new cache with storage, no update
        SolcastCache cache = new SolcastCache(identifier, store);
        // store shall be filled now, but only starting from today
        assertEquals(383, cache.getForecast().length(), "Cache forecast length");
        assertTrue(cache.isFilled());

        // move back and see if cache is still filled
        Instant fixedInstant = Instant.parse("2022-07-15T14:23:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, timeZoneProvider.getTimeZone());
        Utils.setClock(fixedClock);
        assertFalse(cache.isFilled());
    }

    @Test
    void testCacheWrite() {
        String identifier = "solcast-test";
        JSONArray forecastJson = SolcastPlaneMock.getPreparedForecast();
        // 672 entries expected = 7 days back + 7 days forward
        assertEquals(672, forecastJson.length(), "Complete forecast length");
        // create new cache with storage
        SolcastCache cache = new SolcastCache(identifier, store);
        // update cache with forecast
        cache.update(new JSONObject(Map.of(KEY_FORECAST, forecastJson)));
        // store shall be filled now, but only starting from today
        JSONArray storedForecast = new JSONArray(store.get(identifier + FORECAST_APPENDIX));
        assertEquals(383, storedForecast.length(), "Stored forecast length");
        assertTrue(cache.isFilled());
    }

    @Test
    void testFirstStartupNoStorage() {
        setFixedTimeJul18();
        SolcastBridgeMock bridgeMock = SolcastMockFactory.createBridgeHandler();
        HttpClient httpMock = mock(HttpClient.class);
        String planeId = "sc-plane-1-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");

        Storage<String> store = new VolatileStorage<>();
        SolcastPlaneMock planeMock = SolcastMockFactory.createPlaneHandler(planeId, bridgeMock, httpMock, store);
        CallbackMock callback = (CallbackMock) planeMock.getCallback();
        assertNotNull(callback);

        State counterState = callback
                .getLastState("solarforecast:sc-plane:solcast-site-test:sc-plane-1-test:update#api-count");
        assertTrue(counterState instanceof StringType);
        assertEquals("{\"200\":2,\"other\":0,\"429\":0}", ((StringType) counterState).toFullString(), "API call count");
    }

    @Test
    void testSingleEnergyTimeSeries() {
        setFixedTimeJul18();
        SolcastBridgeMock bridgeMock = SolcastMockFactory.createBridgeHandler();
        HttpClient httpMock = mock(HttpClient.class);
        String planeId = "sc-plane-1-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");

        Storage<String> store = new VolatileStorage<>();
        SolcastPlaneMock planeMock = SolcastMockFactory.createPlaneHandler(planeId, bridgeMock, httpMock, store);
        CallbackMock callback = (CallbackMock) planeMock.getCallback();
        assertNotNull(callback);

        TimeSeries ts1 = callback
                .getTimeSeries("solarforecast:sc-plane:solcast-site-test:sc-plane-1-test:average#energy-estimate");
        assertEquals(303, ts1.size(), "TimeSeries size");
        Iterator<TimeSeries.Entry> iter1 = ts1.getStates().iterator();
        while (iter1.hasNext()) {
            TimeSeries.Entry e1 = iter1.next();
            assertEquals("kWh", ((QuantityType<?>) e1.state()).getUnit().toString(), "Power Unit");
        }
        planeMock.dispose();
        bridgeMock.dispose();
    }

    @Test
    void testMergeArrays() {
        String actuals = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        JSONArray actualsJson = (new JSONObject(actuals)).getJSONArray(KEY_ACTUALS);
        String forecasString = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        JSONArray forecastJson = (new JSONObject(forecasString)).getJSONArray(KEY_FORECAST);
        assertEquals(336, actualsJson.length());
        assertEquals(336, forecastJson.length());
        JSONArray wholeForecast = SolcastCache.merge(actualsJson, forecastJson);
        assertEquals(336 + 336, wholeForecast.length());
    }

    @Test
    void testCombinedPowerTimeSeries() {
        SolcastBridgeMock bridgeMock = SolcastMockFactory.createBridgeHandler();
        CallbackMock callbackBridge = (CallbackMock) bridgeMock.getCallback();
        assertNotNull(callbackBridge);

        HttpClient httpMock = mock(HttpClient.class);
        String planeId1 = "sc-plane-1-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId1, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId1, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");
        Storage<String> store = new VolatileStorage<>();
        SolcastPlaneMock planeMock1 = SolcastMockFactory.createPlaneHandler(planeId1, bridgeMock, httpMock, store);
        CallbackMock callbackPlane1 = (CallbackMock) planeMock1.getCallback();
        assertNotNull(callbackPlane1);

        // clear callback from bridge to wait for update from both planes
        callbackBridge.clear();

        String planeId2 = "sc-plane-2-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId2, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId2, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");
        SolcastPlaneMock planeMock2 = SolcastMockFactory.createPlaneHandler(planeId2, bridgeMock, httpMock, store);
        CallbackMock callbackPlane2 = (CallbackMock) planeMock2.getCallback();
        assertNotNull(callbackPlane2);

        // clear callback, call manual update and wait for bridge update
        callbackBridge.clear();
        ScheduledFuture<?> result = bridgeMock.getScheduler().schedule(bridgeMock::updateData, 0, TimeUnit.SECONDS);
        try {
            result.get();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        TimeSeries bridgeTimeseries = callbackBridge
                .getTimeSeries("solarforecast:sc-site:solcast-site-test:average#power-estimate");
        TimeSeries planeTimeseries = callbackPlane2
                .getTimeSeries("solarforecast:sc-plane:solcast-site-test:sc-plane-2-test:average#power-estimate");
        assertEquals(356, bridgeTimeseries.size(), "Bridge TimeSeries size");
        assertEquals(356, planeTimeseries.size(), "Plane TimeSeries size");
        Iterator<TimeSeries.Entry> bridgeIterator = bridgeTimeseries.getStates().iterator();
        Iterator<TimeSeries.Entry> planeIterator = planeTimeseries.getStates().iterator();
        while (bridgeIterator.hasNext()) {
            TimeSeries.Entry bridgeEntry = bridgeIterator.next();
            TimeSeries.Entry planeEntry = planeIterator.next();
            assertEquals("kW", ((QuantityType<?>) bridgeEntry.state()).getUnit().toString(), "Bridge Power Unit");
            assertEquals("kW", ((QuantityType<?>) planeEntry.state()).getUnit().toString(), "Plane Power Unit");
            assertEquals(((QuantityType<?>) bridgeEntry.state()).doubleValue(),
                    ((QuantityType<?>) planeEntry.state()).doubleValue() * 2, 0.01, "Power Value");
        }
        planeMock1.dispose();
        planeMock2.dispose();
        bridgeMock.dispose();
    }

    @Test
    void testRefreshManual() {
        SolcastBridgeMock bridgeMock = SolcastMockFactory.createBridgeHandler();
        CallbackMock callbackBridge = (CallbackMock) bridgeMock.getCallback();
        assertNotNull(callbackBridge);

        Storage<String> store = new VolatileStorage<>();
        HttpClient httpMock = mock(HttpClient.class);
        String planeId1 = "sc-plane-1-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId1, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId1, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");
        Configuration config1 = new Configuration();
        config1.put("resourceId", planeId1);
        config1.put("refreshInterval", 0);

        SolcastPlaneMock planeMock1 = SolcastMockFactory.createPlaneHandler(planeId1, bridgeMock, httpMock, store,
                ThingStatus.OFFLINE, config1);
        CallbackMock callbackPlane1 = (CallbackMock) planeMock1.getCallback();
        assertNotNull(callbackPlane1);

        // no update shall happen
        assertEquals(Instant.MAX, bridgeMock.getSolarForecasts().get(0).getForecastBegin(), "Bridge forecast begin");
        assertEquals(Instant.MIN, bridgeMock.getSolarForecasts().get(0).getForecastEnd(), "Bridge forecast begin");
        assertEquals(Instant.MAX, planeMock1.getSolarForecasts().get(0).getForecastBegin(), "Plane 1 forecast begin");
        assertEquals(Instant.MIN, planeMock1.getSolarForecasts().get(0).getForecastEnd(), "Plane 1 forecast begin");

        String planeId2 = "sc-plane-2-test";
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId2, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId2, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");
        Configuration config2 = new Configuration();
        config2.put("resourceId", planeId2);
        config2.put("refreshInterval", 0);
        SolcastPlaneMock planeMock2 = SolcastMockFactory.createPlaneHandler(planeId2, bridgeMock, httpMock, store,
                ThingStatus.OFFLINE, config2);
        CallbackMock callbackPlane2 = (CallbackMock) planeMock2.getCallback();
        assertNotNull(callbackPlane2);

        assertEquals(Instant.MAX, bridgeMock.getSolarForecasts().get(0).getForecastBegin(), "Bridge forecast begin");
        assertEquals(Instant.MIN, bridgeMock.getSolarForecasts().get(0).getForecastEnd(), "Bridge forecast begin");
        assertEquals(Instant.MAX, bridgeMock.getSolarForecasts().get(1).getForecastBegin(), "Bridge forecast begin");
        assertEquals(Instant.MIN, bridgeMock.getSolarForecasts().get(1).getForecastEnd(), "Bridge forecast begin");
        assertEquals(Instant.MAX, planeMock1.getSolarForecasts().get(0).getForecastBegin(), "Plane 1 forecast begin");
        assertEquals(Instant.MIN, planeMock1.getSolarForecasts().get(0).getForecastEnd(), "Plane 1 forecast begin");
        assertEquals(Instant.MAX, planeMock2.getSolarForecasts().get(0).getForecastBegin(), "Plane 2 forecast begin");
        assertEquals(Instant.MIN, planeMock2.getSolarForecasts().get(0).getForecastEnd(), "Plane 2 forecast begin");

        // update refreshInterval and http responses
        config1.put("refreshInterval", 5);
        planeMock1.handleConfigurationUpdate(config1.getProperties());
        planeMock1.initialize();
        config2.put("refreshInterval", 5);
        planeMock2.handleConfigurationUpdate(config2.getProperties());
        planeMock2.initialize();
        callbackPlane1.waitForStatus(ThingStatus.ONLINE);
        callbackPlane2.waitForStatus(ThingStatus.ONLINE);

        assertEquals(Instant.parse("2022-07-17T11:30:00Z"),
                bridgeMock.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.AVERAGE).getBegin(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                bridgeMock.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.AVERAGE).getEnd(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-17T11:30:00Z"),
                bridgeMock.getSolarForecasts().get(1).getPowerTimeSeries(QueryMode.AVERAGE).getBegin(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                bridgeMock.getSolarForecasts().get(1).getPowerTimeSeries(QueryMode.AVERAGE).getEnd(),
                "Bridge forecast begin");
        assertEquals(Instant.parse("2022-07-17T11:30:00Z"),
                planeMock1.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.AVERAGE).getBegin(),
                "Plane 1 forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                planeMock1.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.AVERAGE).getEnd(),
                "Plane 1 forecast begin");
        assertEquals(Instant.parse("2022-07-17T11:30:00Z"),
                planeMock2.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.AVERAGE).getBegin(),
                "Plane 2 forecast begin");
        assertEquals(Instant.parse("2022-07-24T21:00:00Z"),
                planeMock2.getSolarForecasts().get(0).getPowerTimeSeries(QueryMode.AVERAGE).getEnd(),
                "Plane 2 forecast begin");
        bridgeMock.dispose();
        planeMock1.dispose();
        planeMock2.dispose();
    }

    @Test
    void testCombinedEnergyTimeSeries() {
        setFixedTimeJul18();
        SolcastBridgeMock bridgeMock = SolcastMockFactory.createBridgeHandler();
        CallbackMock callbackBridge = (CallbackMock) bridgeMock.getCallback();
        assertNotNull(callbackBridge);

        HttpClient httpMock = mock(HttpClient.class);
        String planeId1 = "sc-plane-1-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId1, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId1, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");
        Storage<String> store = new VolatileStorage<>();
        SolcastPlaneMock planeMock1 = SolcastMockFactory.createPlaneHandler(planeId1, bridgeMock, httpMock, store);
        CallbackMock callbackPlane1 = (CallbackMock) planeMock1.getCallback();
        assertNotNull(callbackPlane1);

        String planeId2 = "sc-plane-2-test";
        SolcastPlaneMock.httpActualResponse(httpMock, CURRENT_ESTIMATE_URL, planeId2, HttpStatus.OK_200,
                "src/test/resources/solcast/estimated-actuals.json");
        SolcastPlaneMock.httpActualResponse(httpMock, FORECAST_URL, planeId2, HttpStatus.OK_200,
                "src/test/resources/solcast/forecasts.json");
        SolcastPlaneMock planeMock2 = SolcastMockFactory.createPlaneHandler(planeId2, bridgeMock, httpMock, store);
        CallbackMock callbackPlane2 = (CallbackMock) planeMock2.getCallback();
        assertNotNull(callbackPlane2);

        // clear callback, call manual update and wait for bridge update
        callbackBridge.clear();
        ScheduledFuture<?> result = bridgeMock.getScheduler().schedule(bridgeMock::updateData, 0, TimeUnit.SECONDS);
        try {
            result.get();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        TimeSeries bridgeTimeseries = callbackBridge
                .getTimeSeries("solarforecast:sc-site:solcast-site-test:average#energy-estimate");
        TimeSeries plane2Timeseries = callbackPlane2
                .getTimeSeries("solarforecast:sc-plane:solcast-site-test:sc-plane-2-test:average#energy-estimate");
        assertEquals(303, bridgeTimeseries.size(), "TimeSeries size");
        assertEquals(303, plane2Timeseries.size(), "TimeSeries size");

        Iterator<TimeSeries.Entry> bridgeIterator = bridgeTimeseries.getStates().iterator();
        Iterator<TimeSeries.Entry> plane2Iterator = plane2Timeseries.getStates().iterator();
        while (bridgeIterator.hasNext()) {
            TimeSeries.Entry bridgeEntry = bridgeIterator.next();
            TimeSeries.Entry plane2Entry = plane2Iterator.next();
            assertEquals("kWh", ((QuantityType<?>) bridgeEntry.state()).getUnit().toString(), "Bridge Power Unit");
            assertEquals("kWh", ((QuantityType<?>) plane2Entry.state()).getUnit().toString(), "Plane 2 Power Unit");
            assertEquals(((QuantityType<?>) bridgeEntry.state()).doubleValue(),
                    ((QuantityType<?>) plane2Entry.state()).doubleValue() * 2, 0.1, "Power Value");
        }
        planeMock1.dispose();
        planeMock2.dispose();
        bridgeMock.dispose();
    }
}
