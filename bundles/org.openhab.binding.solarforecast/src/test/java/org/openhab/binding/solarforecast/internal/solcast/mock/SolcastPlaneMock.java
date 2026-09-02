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
package org.openhab.binding.solarforecast.internal.solcast.mock;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.FileReader;
import org.openhab.binding.solarforecast.internal.solcast.SolcastCache;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastPlaneHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link SolcastPlaneMock} mocks Plane Handler for solar.forecast
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneMock extends SolcastPlaneHandler {

    public SolcastPlaneMock(Thing thing, HttpClient hc, Storage<String> storage) {
        super(thing, hc, storage);
    }

    @Override
    public void updateForecast(SolcastObject fso) {
        super.updateForecast(fso);
    }

    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }

    @Override
    public void updateConfiguration(Configuration config) {
        super.updateConfiguration(config);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> config) {
        super.handleConfigurationUpdate(config);
    }

    @Override
    public boolean isInitialized() {
        return super.isInitialized();
    }

    /**
     * Prepare HttpClient mock for request answers
     *
     * @param httpMock
     * @param url
     * @param urlId
     * @param status
     * @param content
     * @param resourceId
     */
    public static void httpActualResponse(HttpClient httpMock, String url, String resourceId, int status,
            String response) {
        String currentEstimateUrl = String.format(url, resourceId);
        Request actualsRequest = mock(Request.class);
        when(httpMock.newRequest(currentEstimateUrl)).thenReturn(actualsRequest);
        ContentResponse actualsResponse = mock(ContentResponse.class);
        try {
            when(actualsRequest.send()).thenReturn(actualsResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail();
        }
        when(actualsResponse.getContentAsString()).thenReturn(FileReader.readFileInString(response));
        when(actualsResponse.getStatus()).thenReturn(status);
    }

    public static JSONArray getPreparedForecast() {
        String actuals = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        JSONArray actualsJson = (new JSONObject(actuals)).getJSONArray(KEY_ACTUALS);
        String forecasString = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        JSONArray forecastJson = (new JSONObject(forecasString)).getJSONArray(KEY_FORECAST);
        return SolcastCache.merge(actualsJson, forecastJson);
    }
}
