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
package org.openhab.binding.dirigera.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.light.ColorLightHandler;
import org.openhab.binding.dirigera.internal.handler.light.LightCommand;
import org.openhab.binding.dirigera.internal.interfaces.DirigeraAPI;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.network.DirigeraAPIImpl;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.HSBType;

/**
 * {@link TestGeneric} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestGeneric {
    static String output = "fine!";

    @Test
    void testStringFormatWithNull() {
        try {
            String error = String.format(
                    "{\"http-error-flag\":true,\"http-error-status\":%s,\"http-error-message\":\"%s\"}", "5", null);
            JSONObject errorJSON = new JSONObject(error);
            assertFalse(errorJSON.isNull("http-error-message"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void hsbCloseTo() {
        HSBType first = new HSBType("180, 100, 100");
        HSBType second = new HSBType("177, 97, 50");
        boolean isClose = first.closeTo(second, 0.02);
        assertTrue(isClose);
    }

    @Test
    void lightCommandQueueTest() {
        ArrayList<LightCommand> lightRequestQueue = new ArrayList<>();
        JSONObject dummy1 = new JSONObject();
        dummy1.put("dunny1", false);
        LightCommand brightness1 = new LightCommand(dummy1, LightCommand.Action.BRIGHTNESS);
        lightRequestQueue.add(brightness1);
        JSONObject dummy2 = new JSONObject();
        dummy2.put("dunny2", true);
        LightCommand brightness2 = new LightCommand(dummy2, LightCommand.Action.BRIGHTNESS);
        assertTrue(lightRequestQueue.contains(brightness1));
        assertTrue(lightRequestQueue.contains(brightness2));
        assertTrue(brightness1.equals(brightness2));
        JSONObject dummy3 = null;
        assertFalse(brightness1.equals(dummy3));
        LightCommand color = new LightCommand(dummy2, LightCommand.Action.COLOR);
        assertFalse(lightRequestQueue.contains(color));
    }

    @Test
    void testKelvinToHSB() {
        HSBType hsb = ColorLightHandler.getHSBTemperature(1000);
        assertEquals(16, hsb.getHue().intValue(), "HSB Color Hue");
        assertEquals(100, hsb.getSaturation().intValue(), "HSB Saturation");

        hsb = ColorLightHandler.getHSBTemperature(3000);
        assertEquals(27, hsb.getHue().intValue(), "HSB Color Hue");
        assertEquals(56, hsb.getSaturation().intValue(), "HSB Saturation");

        hsb = ColorLightHandler.getHSBTemperature(5000);
        assertEquals(26, hsb.getHue().intValue(), "HSB Color Hue");
        assertEquals(19, hsb.getSaturation().intValue(), "HSB Saturation");
    }

    @Test
    void testApiJsonException() {
        HttpClient httpMock = mock(HttpClient.class);
        Request requestMock = mock(Request.class);
        when(httpMock.isRunning()).thenReturn(true);
        when(httpMock.getSslContextFactory()).thenReturn(new SslContextFactory.Client(true));
        when(httpMock.newRequest(anyString())).thenReturn(requestMock);
        when(requestMock.timeout(10, TimeUnit.SECONDS)).thenReturn(requestMock);
        when(requestMock.header(HttpHeader.AUTHORIZATION, "Bearer 1234")).thenReturn(requestMock);

        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        // response will force a JSON format exception in API implementation
        when(response.getContentAsString()).thenReturn("{\"rubbish\":true,butNoJson:\"false\",]}");

        try {
            when(requestMock.send()).thenReturn(response);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail();
        }

        Gateway gateway = mock(Gateway.class);
        when(gateway.getToken()).thenReturn("1234");
        DirigeraAPIImpl api = new DirigeraAPIImpl(httpMock, gateway);
        JSONObject apiResponse = api.readDevice("abc");
        assertNotNull(apiResponse);
        // test completeness and status of error message
        assertTrue(apiResponse.has(DirigeraAPI.HTTP_ERROR_FLAG));
        assertTrue(apiResponse.has(DirigeraAPI.HTTP_ERROR_STATUS));
        assertEquals(500, apiResponse.getInt(DirigeraAPI.HTTP_ERROR_STATUS));
        assertTrue(apiResponse.has(DirigeraAPI.HTTP_ERROR_MESSAGE));
    }

    @Test
    void testThreadpoolExcpetion() {
        ScheduledExecutorService ses = ThreadPoolManager.getScheduledPool("test");
        ScheduledFuture<?> sf = ses.scheduleWithFixedDelay(this::printOutput, 0, 50, TimeUnit.MILLISECONDS);
        sleep();
        assertFalse(sf.isDone());
        output = "throw";
        sleep();
        assertTrue(sf.isDone());
    }

    void printOutput() {
        if ("throw".equals(output)) {
            throw new RuntimeException("crash");
        }
    }

    void sleep() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            fail();
        }
    }
}
