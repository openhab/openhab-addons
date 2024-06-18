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
package org.openhab.binding.pegelonline.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pegelonline.internal.config.PegelOnlineConfiguration;
import org.openhab.binding.pegelonline.internal.dto.Measure;
import org.openhab.binding.pegelonline.internal.dto.Station;
import org.openhab.binding.pegelonline.internal.util.FileReader;
import org.openhab.binding.pegelonline.internal.utils.Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.State;

/**
 * The {@link PegelTest} Test helper utils
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class PegelTest {
    public static final String TEST_STATION_UUID = "1ebd0f94-cc06-445c-8e73-43fe2b8c72dc";

    @Test
    void testConfigurationValidations() {
        PegelOnlineConfiguration config = new PegelOnlineConfiguration();
        assertFalse(config.uuidCheck(), config.uuid);
        config.uuid = "abc@";
        assertFalse(config.uuidCheck(), config.uuid);
        config.uuid = "abc d";
        assertFalse(config.uuidCheck(), config.uuid);
        config.uuid = "1234567a-abc1-efd9-cdf3-0123456789ab";
        assertTrue(config.uuidCheck(), config.uuid);
        assertTrue(config.warningCheck(), "Warnings");

        String content = FileReader.readFileInString("src/test/resources/stations.json");
        Station[] stationArray = GSON.fromJson(content, Station[].class);
        assertNotNull(stationArray);
        for (Station station : stationArray) {
            config.uuid = station.uuid;
            assertTrue(config.uuidCheck(), config.uuid);
        }
    }

    @Test
    void testNameConversion() {
        String stationName = "EIDER-SPERRWERK BP";
        String conversion = Utils.toTitleCase(stationName);
        assertEquals("Eider-Sperrwerk Bp", conversion, "Station Name");

        String content = FileReader.readFileInString("src/test/resources/stations.json");
        Station[] stationArray = GSON.fromJson(content, Station[].class);
        assertNotNull(stationArray);
        for (Station station : stationArray) {
            assertTrue(Character.isUpperCase(Utils.toTitleCase(station.shortname).charAt(0)),
                    "First Character Upper Case");
            assertTrue(Character.isUpperCase(Utils.toTitleCase(station.water.shortname).charAt(0)),
                    "First Character Upper Case");
        }
    }

    @Test
    void testDistance() {
        // Frankfurt Main: 50.117461111005, 8.639069127891485
        String content = FileReader.readFileInString("src/test/resources/stations.json");
        Station[] stationArray = GSON.fromJson(content, Station[].class);
        assertNotNull(stationArray);
        int hitCounter = 0;
        for (Station station : stationArray) {
            double distance = Utils.calculateDistance(50.117461111005, 8.639069127891485, station.latitude,
                    station.longitude);
            if (distance < 50) {
                hitCounter++;
                assertTrue(station.water.shortname.equals("RHEIN") || station.water.shortname.equals("MAIN"),
                        "RHEIN or MAIN");
            }
        }
        assertEquals(11, hitCounter, "Meassurement Stations around FRA");
    }

    @Test
    void testMeasureObject() {
        String content = FileReader.readFileInString("src/test/resources/measure.json");
        Measure measure = GSON.fromJson(content, Measure.class);
        if (measure != null) {
            assertEquals("2021-08-01T16:00:00+02:00", measure.timestamp, "Timestamp");
            assertEquals(238, measure.value, "Level");
            assertEquals(-1, measure.trend, "Trend");
        } else {
            fail();
        }
    }

    @Test
    void test404Status() {
        String stationContent = FileReader.readFileInString("src/test/resources/stations.json");
        ContentResponse stationResponse = mock(ContentResponse.class);
        when(stationResponse.getStatus()).thenReturn(200);
        when(stationResponse.getContentAsString()).thenReturn(stationContent);

        String content = "{}";
        ContentResponse measureResponse = mock(ContentResponse.class);
        when(measureResponse.getStatus()).thenReturn(404);
        when(measureResponse.getContentAsString()).thenReturn(content);

        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(STATIONS_URI + "/" + TEST_STATION_UUID + "/W/currentmeasurement.json"))
                    .thenReturn(measureResponse);
            when(httpClientMock.GET(STATIONS_URI)).thenReturn(stationResponse);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock callback = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        Configuration config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        handler.setCallback(callback);
        handler.updateConfiguration(config);
        handler.initialize();
        handler.performMeasurement();
        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertEquals("@text/pegelonline.handler.status.uuid-not-found [\"" + TEST_STATION_UUID + "\"]", description,
                "Description");
    }

    @Test
    void testWrongContent() {
        String stationContent = FileReader.readFileInString("src/test/resources/stations.json");
        ContentResponse stationResponse = mock(ContentResponse.class);
        when(stationResponse.getStatus()).thenReturn(200);
        when(stationResponse.getContentAsString()).thenReturn(stationContent);

        String content = "{}";
        ContentResponse measureResponse = mock(ContentResponse.class);
        when(measureResponse.getStatus()).thenReturn(200);
        when(measureResponse.getContentAsString()).thenReturn(content);

        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(STATIONS_URI + "/" + TEST_STATION_UUID + "/W/currentmeasurement.json"))
                    .thenReturn(measureResponse);
            when(httpClientMock.GET(STATIONS_URI)).thenReturn(stationResponse);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock callback = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        Configuration config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        handler.setCallback(callback);
        handler.updateConfiguration(config);
        handler.initialize();
        handler.performMeasurement();
        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertEquals("@text/pegelonline.handler.status.json-error [\"{}\"]", description, "Description");
    }

    @Test
    public void testWrongConfiguration() {
        CallbackMock callback = new CallbackMock();
        PegelOnlineHandler handler = getConfiguredHandler(callback, 99);

        Configuration config = new Configuration();
        config.put("uuid", " ");
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();

        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertEquals("@text/pegelonline.handler.status.uuid [\" \"]", description, "Description");
    }

    @Test
    public void testInconsistentLevels() {
        CallbackMock callback = new CallbackMock();
        PegelOnlineHandler handler = getConfiguredHandler(callback, 99);

        Configuration config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        config.put("warningLevel1", 100);
        config.put("warningLevel2", 200);
        config.put("warningLevel3", 150);
        handler.updateConfiguration(config);
        handler.initialize();

        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertEquals("@text/pegelonline.handler.status.warning", description, "Description");

        handler.dispose();
        config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        config.put("warningLevel1", 100);
        config.put("warningLevel2", 200);
        config.put("warningLevel3", 300);
        config.put("hqExtreme", 600);
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();

        tsi = callback.getThingStatus();
        assertNotNull(tsi);
        // In function initialize scheduler is started.
        // If schedule took place status is ONLINE else UNKNOWN
        switch (tsi.getStatus()) {
            case UNKNOWN:
                assertEquals(ThingStatusDetail.NONE, tsi.getStatusDetail(), "Detail");
                description = tsi.getDescription();
                assertNotNull(description);
                assertEquals("@text/pegelonline.handler.status.wait-feedback", description, "Description");
                break;
            case ONLINE:
                break;
            default:
                fail();
        }

        handler.dispose();
        config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        config.put("warningLevel1", 100);
        config.put("warningLevel2", 200);
        config.put("warningLevel3", 300);
        config.put("hq10", 100);
        config.put("hq100", 200);
        config.put("hqExtreme", 150);
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();

        tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail(), "Detail");
        description = tsi.getDescription();
        assertNotNull(description);
        assertEquals("@text/pegelonline.handler.status.warning", description, "Description");
    }

    @Test
    public void testWrongResponse() {
        String measureContent = "{}";
        ContentResponse measureResponse = mock(ContentResponse.class);
        when(measureResponse.getStatus()).thenReturn(500);
        when(measureResponse.getContentAsString()).thenReturn(measureContent);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(STATIONS_URI + "/" + TEST_STATION_UUID + "/W/currentmeasurement.json"))
                    .thenReturn(measureResponse);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock callback = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        Configuration config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        handler.setCallback(callback);
        handler.updateConfiguration(config);
        handler.initialize();
        handler.performMeasurement();
        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertEquals("@text/pegelonline.handler.status.http-status [\"500\"]", description, "Description");
    }

    @Test
    public void testWarnings() {
        CallbackMock callback = new CallbackMock();
        PegelOnlineHandler handler = getConfiguredHandler(callback, 99);
        handler.initialize();
        handler.performMeasurement();
        State state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(NO_WARNING, ((DecimalType) state).intValue(), "No warning");

        handler = getConfiguredHandler(callback, 100);
        handler.initialize();
        handler.performMeasurement();
        state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(WARN_LEVEL_1, ((DecimalType) state).intValue(), "Warn Level 1");

        handler = getConfiguredHandler(callback, 299);
        handler.initialize();
        handler.performMeasurement();
        state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(WARN_LEVEL_2, ((DecimalType) state).intValue(), "Warn Level 2");

        handler = getConfiguredHandler(callback, 1000);
        handler.initialize();
        handler.performMeasurement();
        state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(HQ_EXTREME, ((DecimalType) state).intValue(), "HQ extreme");
    }

    private PegelOnlineHandler getConfiguredHandler(CallbackMock callback, int levelSimulation) {
        String stationContent = FileReader.readFileInString("src/test/resources/stations.json");
        ContentResponse stationResponse = mock(ContentResponse.class);
        when(stationResponse.getStatus()).thenReturn(200);
        when(stationResponse.getContentAsString()).thenReturn(stationContent);

        String measureContent = "{  \"timestamp\": \"2021-08-01T16:00:00+02:00\",  \"value\": " + levelSimulation
                + ",  \"trend\": -1}";
        ContentResponse measureResponse = mock(ContentResponse.class);
        when(measureResponse.getStatus()).thenReturn(200);
        when(measureResponse.getContentAsString()).thenReturn(measureContent);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(STATIONS_URI + "/" + TEST_STATION_UUID + "/W/currentmeasurement.json"))
                    .thenReturn(measureResponse);
            when(httpClientMock.GET(STATIONS_URI)).thenReturn(stationResponse);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        Configuration config = new Configuration();
        config.put("uuid", TEST_STATION_UUID);
        config.put("warningLevel1", 100);
        config.put("warningLevel2", 200);
        config.put("warningLevel3", 300);
        config.put("hq10", 400);
        config.put("hq100", 500);
        config.put("hqExtreme", 600);
        handler.setCallback(callback);
        handler.updateConfiguration(config);
        return handler;
    }
}
