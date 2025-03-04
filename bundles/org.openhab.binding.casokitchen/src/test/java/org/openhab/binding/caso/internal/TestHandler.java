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
package org.openhab.binding.caso.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;
import org.openhab.binding.casokitchen.internal.CasoKitchenBindingConstants;
import org.openhab.binding.casokitchen.internal.handler.TwoZonesWinecoolerHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.State;

/**
 * The {@link TestHandler} is testing handler functions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class TestHandler {
    private static final int FULL_UPDATE_COUNT = 11;

    TimeZoneProvider tzp = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };

    private HttpClientFactory prepareHttpResponse() {
        // Prepare http response
        HttpClientFactory httpFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpFactory.getCommonHttpClient()).thenReturn(httpClient);
        Request httpStatusRequest = mock(Request.class);
        when(httpClient.POST(CasoKitchenBindingConstants.STATUS_URL)).thenReturn(httpStatusRequest);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(contentResponse.getStatus()).thenReturn(200);
        String content = CasoKitchenBindingConstants.EMPTY;
        try {
            content = Files.readString(Path.of("src/test/resources/", "StatusResponse.json"), Charset.defaultCharset());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        when(contentResponse.getContentAsString()).thenReturn(content);
        when(httpStatusRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(httpStatusRequest);
        try {
            when(httpStatusRequest.send()).thenReturn(contentResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail(e.getMessage());
        }
        return httpFactory;
    }

    @Test
    void testConfigErrors() {
        ThingImpl thing = new ThingImpl(CasoKitchenBindingConstants.THING_TYPE_WINECOOLER, "test");

        FactoryMock factory = new FactoryMock(prepareHttpResponse(), tzp);
        ThingHandler handler = factory.createHandler(thing);
        assertNotNull(handler);
        assertTrue(handler instanceof TwoZonesWinecoolerHandler);
        TwoZonesWinecoolerHandler winecoolerHandler = (TwoZonesWinecoolerHandler) handler;
        CallbackMock callback = new CallbackMock();
        winecoolerHandler.setCallback(callback);
        winecoolerHandler.initialize();

        ThingStatusInfo tsi = thing.getStatusInfo();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail());
        assertEquals("@text/casokitchen.winecooler-2z.status.api-key-missing", tsi.getDescription());

        Configuration config = new Configuration();
        config.put("apiKey", "abc");
        thing.setConfiguration(config);
        winecoolerHandler.initialize();
        tsi = thing.getStatusInfo();
        assertEquals(ThingStatus.OFFLINE, tsi.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, tsi.getStatusDetail());
        assertEquals("@text/casokitchen.winecooler-2z.status.device-id-missing", tsi.getDescription());

        config.put("deviceId", "xyz");
        thing.setConfiguration(config);
        winecoolerHandler.initialize();
        callback.waitForOnline();
        tsi = thing.getStatusInfo();
        assertEquals(ThingStatus.ONLINE, tsi.getStatus());
    }

    @Test
    void testHandler() {
        // Prepare Thing
        ThingImpl thing = new ThingImpl(CasoKitchenBindingConstants.THING_TYPE_WINECOOLER, "test");
        Configuration config = new Configuration();
        config.put("apiKey", "abc");
        config.put("deviceId", "xyz");
        thing.setConfiguration(config);

        // Prepare handler
        FactoryMock factory = new FactoryMock(prepareHttpResponse(), tzp);
        ThingHandler handler = factory.createHandler(thing);
        assertNotNull(handler);
        assertTrue(handler instanceof TwoZonesWinecoolerHandler);
        TwoZonesWinecoolerHandler winecoolerHandler = (TwoZonesWinecoolerHandler) handler;
        CallbackMock callback = new CallbackMock();
        winecoolerHandler.setCallback(callback);
        winecoolerHandler.initialize();
        callback.waitForOnline();
        callback.waitForFullUpdate(FULL_UPDATE_COUNT);

        // generic
        assertEquals(OnOffType.OFF, callback.states.get("casokitchen:winecooler-2z:test:generic#light-switch"));
        State dateTime = callback.states.get("casokitchen:winecooler-2z:test:generic#last-update");
        assertTrue(dateTime instanceof DateTimeType);
        Instant lastTimestamp = ((DateTimeType) dateTime).getInstant();
        assertEquals("2024-08-13T23:25:32.238209200Z", lastTimestamp.toString());

        // top
        State currentTopTemp = callback.states.get("casokitchen:winecooler-2z:test:top#temperature");
        assertTrue(currentTopTemp instanceof QuantityType);
        assertEquals("9 °C", currentTopTemp.toFullString());
        State currentTopSetTemp = callback.states.get("casokitchen:winecooler-2z:test:top#set-temperature");
        assertTrue(currentTopSetTemp instanceof QuantityType);
        assertEquals("9 °C", currentTopSetTemp.toFullString());
        assertEquals(OnOffType.ON, callback.states.get("casokitchen:winecooler-2z:test:top#power"));
        assertEquals(OnOffType.OFF, callback.states.get("casokitchen:winecooler-2z:test:top#light-switch"));

        // bottom
        State currentBottomTemp = callback.states.get("casokitchen:winecooler-2z:test:bottom#temperature");
        assertTrue(currentBottomTemp instanceof QuantityType);
        assertEquals("10 °C", currentBottomTemp.toFullString());
        State currentBottomSetTemp = callback.states.get("casokitchen:winecooler-2z:test:bottom#set-temperature");
        assertTrue(currentBottomSetTemp instanceof QuantityType);
        assertEquals("10 °C", currentBottomSetTemp.toFullString());
        assertEquals(OnOffType.ON, callback.states.get("casokitchen:winecooler-2z:test:bottom#power"));
        assertEquals(OnOffType.OFF, callback.states.get("casokitchen:winecooler-2z:test:bottom#light-switch"));
    }
}
