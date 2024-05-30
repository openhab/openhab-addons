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
import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.GSON;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants;
import org.openhab.binding.pegelonline.internal.dto.Measure;
import org.openhab.binding.pegelonline.internal.util.FileReader;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.ThingImpl;

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
    public void testWarningLevels() {
        String content = FileReader.readFileInString("src/test/resources/measure.json");
        ContentResponse crMock = mock(ContentResponse.class);
        when(crMock.getStatus()).thenReturn(200);
        when(crMock.getContentAsString()).thenReturn(content);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(anyString())).thenReturn(crMock);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock thc = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        handler.setCallback(thc);
        handler.initialize();
        Map<String, Object> config = new HashMap<>();
        handler.updateConfiguration(new Configuration(config));
        Measure m = new Measure();
        m.value = 500;
        assertEquals(PegelOnlineBindingConstants.NO_WARNING, handler.getWarnLevel(m), "No Warn Level");
        config.put("warningLevel1", 1000);
        config.put("warningLevel2", 2000);
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();
        assertEquals(PegelOnlineBindingConstants.NO_WARNING, handler.getWarnLevel(m), "No Warn Level");
        m.value = 1200;
        assertEquals(PegelOnlineBindingConstants.WARN_LEVEL_1, handler.getWarnLevel(m), "Warn Level 1");
        m.value = 2100;
    }
}
