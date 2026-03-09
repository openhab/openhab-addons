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
package org.openhab.binding.energyforecast;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.energyforecast.internal.EnergyForecastBindingConstants.THING_TYPE_PRICE_FORECAST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.energyforecast.internal.handler.EnergyForecastHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.TimeSeries;

/**
 * {@link HandlerTest} tests handler initialization, data update cycle and channel updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class HandlerTest {
    private TimeZoneProvider tzp = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.of("Europe/Berlin");
        }
    };

    @BeforeAll
    static void setup() {
        CurrencyUnits.addUnit(CurrencyUnits.createCurrency("EUR", "Euro"));
    }

    String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            fail(e.getMessage());
            return "";
        }
    }

    HttpClient prepareResponse(String fileName) {
        HttpClient client = mock(HttpClient.class);
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.timeout(10, TimeUnit.SECONDS)).thenReturn(request);
        try {
            when(request.send()).thenReturn(response);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail(e.getMessage());
        }
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString()).thenReturn(readFile(fileName));
        return client;
    }

    List<?> createHandler(@Nullable Configuration config) {
        ThingImpl thing = new ThingImpl(THING_TYPE_PRICE_FORECAST, "forecast_test");
        if (config != null) {
            thing.setConfiguration(config);
        }
        HttpClient client = prepareResponse("src/test/resources/2026-02-26-response.json");
        VolatileStorageService storageService = new VolatileStorageService();
        EnergyForecastHandler tester = new EnergyForecastHandler(thing, client, storageService, tzp);
        CallbackMock callback = new CallbackMock();
        tester.setCallback(callback);
        return List.of(tester, callback, thing, storageService);
    }

    @Test
    void testConfigError() {
        List<?> testObjects = createHandler(null);
        EnergyForecastHandler tester = (EnergyForecastHandler) testObjects.get(0);
        assertNotNull(tester);
        CallbackMock callback = (CallbackMock) testObjects.get(1);
        assertNotNull(callback);
        ThingImpl thing = (ThingImpl) testObjects.get(2);
        assertNotNull(thing);
        tester.initialize();

        ThingStatusInfo statusInfo = callback.getStatus();
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, statusInfo.getStatusDetail());
        assertEquals("@text/thing-status.energyforecast.token-empty", statusInfo.getDescription());

        Configuration config = new Configuration();
        config.put("token", "abc");
        thing.setConfiguration(config);
        tester.initialize();

        statusInfo = callback.getStatus();
        assertEquals(ThingStatus.OFFLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, statusInfo.getStatusDetail());
        assertEquals("@text/thing-status.energyforecast.zone-empty", statusInfo.getDescription());
    }

    @Test
    void testFullCycle() {
        Configuration config = new Configuration();
        config.put("token", "abc");
        config.put("zone", "DE-LU");
        List<?> testObjects = createHandler(config);
        EnergyForecastHandler tester = (EnergyForecastHandler) testObjects.get(0);
        assertNotNull(tester);
        CallbackMock callback = (CallbackMock) testObjects.get(1);
        assertNotNull(callback);
        ThingImpl thing = (ThingImpl) testObjects.get(2);
        assertNotNull(thing);
        VolatileStorageService storageService = (VolatileStorageService) testObjects.get(3);
        assertNotNull(storageService);
        Storage<String> store = storageService.getStorage(thing.getUID().getAsString());
        tester.initialize();
        callback.waitForOnline();
        TimeSeries priceSeries = callback.getTimeSeries("energyforecast:price-forecast:forecast_test:price#series");
        assertNotNull(priceSeries);
        assertEquals(23 + 48, priceSeries.size());

        String storeData = store.get("forecast");
        assertNotNull(storeData);
        JSONObject storedJson = new JSONObject(storeData);
        assertEquals(23, storedJson.length());
    }
}
