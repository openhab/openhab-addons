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
package org.openhab.binding.solarforecastinternal.solcast.mock;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.solarforecast.CallbackMock;
import org.openhab.binding.solarforecast.FileReader;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * The {@link SolcastMockFactory} creates mock handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastMockFactory {
    public static Storage<String> storage = new VolatileStorage<>();

    public static SolcastBridgeMock createBridgeHandler() {
        BridgeImpl solcastBridge = new BridgeImpl(SOLCAST_SITE, "solcast-site-test");

        SolcastBridgeMock solcastBridgeHandler = new SolcastBridgeMock(solcastBridge);
        solcastBridge.setHandler(solcastBridgeHandler);
        CallbackMock solcastSiteCallback = new CallbackMock("Bridge");
        solcastSiteCallback.setBridge(solcastBridge);
        solcastBridgeHandler.setCallback(solcastSiteCallback);
        solcastBridgeHandler.updateConfiguration(getDefaultBridgeConfig());
        solcastBridgeHandler.initialize();
        return solcastBridgeHandler;
    }

    public static SolcastPlaneMock createPlaneHandler(String name, SolcastBridgeHandler bridgeHandler,
            HttpClient httpClient, Storage<String> store, ThingStatus status, Configuration config) {
        ThingUID planeThingUID = new ThingUID(SOLCAST_PLANE, bridgeHandler.getThing().getUID(), name);
        ThingImpl solcastPlaneThing = new ThingImpl(SOLCAST_PLANE, planeThingUID);
        solcastPlaneThing.setBridgeUID(bridgeHandler.getThing().getUID());

        CallbackMock solcastPlaneCallback = new CallbackMock(name);
        solcastPlaneCallback.setBridge(bridgeHandler.getThing());
        SolcastPlaneMock solcastPlaneHandler = new SolcastPlaneMock(solcastPlaneThing, httpClient, store);
        solcastPlaneThing.setHandler(solcastPlaneHandler);
        solcastPlaneHandler.setCallback(solcastPlaneCallback);
        solcastPlaneHandler.updateConfiguration(config);
        solcastPlaneHandler.initialize();
        solcastPlaneCallback.waitForStatus(status);
        return solcastPlaneHandler;
    }

    public static SolcastPlaneMock createPlaneHandler(String name, SolcastBridgeHandler bridgeHandler,
            HttpClient httpClient, Storage<String> store) {
        return createPlaneHandler(name, bridgeHandler, httpClient, store, ThingStatus.ONLINE,
                getDefaultPlaneConfig(name));
    }

    private static Configuration getDefaultBridgeConfig() {
        Configuration config = new Configuration();
        config.put("apiKey", "unitTestKey");
        return config;
    }

    private static Configuration getDefaultPlaneConfig(String name) {
        Configuration config = new Configuration();
        config.put("resourceId", name);
        return config;
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
}
