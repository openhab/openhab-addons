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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pegelonline.internal.dto.Measure;
import org.openhab.binding.pegelonline.internal.util.FileReader;
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
        String content = FileReader.readFileInString("src/test/resources/measure.json");
        ContentResponse crMock = mock(ContentResponse.class);
        when(crMock.getStatus()).thenReturn(404);
        when(crMock.getContentAsString()).thenReturn(content);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(anyString())).thenReturn(crMock);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock callback = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        handler.setCallback(callback);
        handler.initialize();
        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("404"), "Detail");
    }

    @Test
    void testWrongContent() {
        String content = "{}";
        ContentResponse crMock = mock(ContentResponse.class);
        when(crMock.getStatus()).thenReturn(200);
        when(crMock.getContentAsString()).thenReturn(content);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(anyString())).thenReturn(crMock);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock callback = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        handler.setCallback(callback);
        handler.initialize();
        ThingStatusInfo tsi = callback.getThingStatus();
        assertNotNull(tsi);
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus(), "Status");
        assertEquals(ThingStatusDetail.NONE, tsi.getStatusDetail(), "Detail");
        String description = tsi.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("json-error"), "Detail");
    }

    @Test
    public void testWarnings() {
        CallbackMock callback = new CallbackMock();
        PegelOnlineHandler handler = getConfiguredHandler(callback, 99);
        handler.performMeasurement();
        State state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(NO_WARNING, ((DecimalType) state).intValue(), "No warning");

        handler = getConfiguredHandler(callback, 100);
        handler.performMeasurement();
        state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(WARN_LEVEL_1, ((DecimalType) state).intValue(), "Warn Level 1");

        handler = getConfiguredHandler(callback, 299);
        handler.performMeasurement();
        state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(WARN_LEVEL_2, ((DecimalType) state).intValue(), "Warn Level 2");

        handler = getConfiguredHandler(callback, 1000);
        handler.performMeasurement();
        state = callback.getState("pegelonline:station:test:warning");
        assertTrue(state instanceof DecimalType);
        assertEquals(HQ_EXTREME, ((DecimalType) state).intValue(), "HQ extreme");
    }

    private PegelOnlineHandler getConfiguredHandler(CallbackMock callback, int levelSimulation) {
        String content = "{  \"timestamp\": \"2021-08-01T16:00:00+02:00\",  \"value\": " + levelSimulation
                + ",  \"trend\": -1}";
        ContentResponse crMock = mock(ContentResponse.class);
        when(crMock.getStatus()).thenReturn(200);
        when(crMock.getContentAsString()).thenReturn(content);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(anyString())).thenReturn(crMock);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        Map<String, Object> config = new HashMap<>();
        config.put("warningLevel1", 100);
        config.put("warningLevel2", 200);
        config.put("warningLevel3", 300);
        config.put("hq10", 400);
        config.put("hq100", 500);
        config.put("hqExtreme", 600);
        handler.setCallback(callback);
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();
        return handler;
    }
}
