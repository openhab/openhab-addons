/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.cloudrain.internal.api.model.AuthParams;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.Token;
import org.openhab.binding.cloudrain.internal.api.model.Zone;

/**
 * A test for the {@link CloudrainAPI} implementation.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
class CloudrainAPIv1ImplTest {

    // Auth
    private static final String ACCESS_TOKEN = "2YotnFZxyzCsicMWpAA";
    private static final String REFRESH_TOKEN = "tGzv3JxzyQx2TlKWIA";
    private static final Integer EXPIRES_IN = 2678400;
    private static final String API_AUTHENTICATE_SUCCESS = "{\n" + "  \"access_token\": \"" + ACCESS_TOKEN + "\",\n"
            + "  \"expires_in\": " + EXPIRES_IN + ",\n" + "  \"refresh_token\": \"" + REFRESH_TOKEN + "\"\n" + "}";
    // Controller
    private static final String CONTROLLER_ID = "XY191140030006";
    private static final String CONTROLLER_NAME = "My Controller";
    // Zones
    private static final String ZONE1_ID = "1234";
    private static final String ZONE1_NAME = "Garden";
    private static final String ZONE2_ID = "1235";
    private static final String ZONE2_NAME = "Lawn";
    private static final String ZONE3_ID = "1236";
    private static final String ZONE3_NAME = "Backyard";
    private static final String ZONE_PATTERN = "{\"zoneId\": \"%s\", \"zoneName\": \"%s\", \"controllerId\": \"%s\", \"controllerName\": \"%s\"}";
    private static final String ZONE1 = String.format(ZONE_PATTERN, ZONE1_ID, ZONE1_NAME, CONTROLLER_ID,
            CONTROLLER_NAME);
    private static final String ZONE2 = String.format(ZONE_PATTERN, ZONE2_ID, ZONE2_NAME, CONTROLLER_ID,
            CONTROLLER_NAME);
    private static final String ZONE3 = String.format(ZONE_PATTERN, ZONE3_ID, ZONE3_NAME, CONTROLLER_ID,
            CONTROLLER_NAME);
    private static final String GET_ZONES_PATTERN = "{\"userZones\": [ %s, %s, %s]}\n";
    private static final String API_GET_ZONES_EMPTY = "{\"userZones\": []}\n";
    private static final String API_GET_ZONES_SUCCESS = String.format(GET_ZONES_PATTERN, ZONE1, ZONE2, ZONE3);
    // Irrigation
    private static final String IRR1_START = "16:18";
    private static final String IRR1_END = "16:20";
    private static final String IRR1_REM_SEC = "117";
    private static final String IRR1_DURATION = "120";
    private static final String IRR2_START = "17:18";
    private static final String IRR2_END = "17:21";
    private static final String IRR2_REM_SEC = "155";
    private static final String IRR2_DURATION = "180";
    private static final String IRRIGATION_PATTERN = "{\"plannedEndTime\": \"%s\",\"remainingSeconds\": %s,\"zoneId\": %s,\"startTime\": \"%s\",\"controllerName\": \"%s\",\"duration\": %s,\"controllerId\": \"%s\",\"zoneName\": \"%s\"}";
    private static final String IRRIGATION1 = String.format(IRRIGATION_PATTERN, IRR1_END, IRR1_REM_SEC, ZONE1_ID,
            IRR1_START, CONTROLLER_NAME, IRR1_DURATION, CONTROLLER_ID, ZONE1_NAME);
    private static final String IRRIGATION2 = String.format(IRRIGATION_PATTERN, IRR2_END, IRR2_REM_SEC, ZONE2_ID,
            IRR2_START, CONTROLLER_NAME, IRR2_DURATION, CONTROLLER_ID, ZONE2_NAME);
    // All Irrigations
    private static final String GET_IRRIGATIONS_PATTERN = "{\"currentlyRunningZones\": [%s,%s]}";
    private static final String API_GET_IRRIGATIONS_EMPTY = "{\"currentlyRunningZones\": []}";
    private static final String API_GET_IRRIGATIONS_SUCCESS = String.format(GET_IRRIGATIONS_PATTERN, IRRIGATION1,
            IRRIGATION2);
    // Irrigation in Zone
    private static final String GET_IRRIGATION_PATTERN = "{\"currentlyRunningIrrigationsInZone\": [%s]}";
    private static final String API_GET_IRRIGATION_EMPTY = "{\"currentlyRunningIrrigationsInZone\": []}";
    private static final String API_GET_IRRIGATION_SUCCESS = String.format(GET_IRRIGATION_PATTERN, IRRIGATION1);

    @Test
    void testAuthenticate() {
        AuthParams params = new AuthParams("user", "pw", "client_id", "client_secret");
        String url = "https://api.cloudrain.com/v1/token";
        Token token = null;
        try {
            CloudrainAPIv1Impl api = new CloudrainAPIv1Impl(mockResponse(url, API_AUTHENTICATE_SUCCESS, 200));
            api.authenticate(params);
            token = api.getToken();
        } catch (Exception e) {
            fail("Exception during testAuthenticate: " + e.toString());
        }
        assertNotNull(token);
        assertEquals(ACCESS_TOKEN, token.getAccessToken());
        assertEquals(REFRESH_TOKEN, token.getRefreshToken());
        assertEquals(EXPIRES_IN, token.getExpiresIn());
    }

    @Test
    void testReAuthenticate() {
        AuthParams params = new AuthParams("user", "pw", "client_id", "client_secret");
        String url = "https://api.cloudrain.com/v1/token";
        Token firstToken = null;
        Token secondToken = null;
        try {
            // first authenticate
            HttpClient client = mockHttpClient();
            CloudrainAPIv1Impl api = new CloudrainAPIv1Impl(mockResponse(client, url, API_AUTHENTICATE_SUCCESS, 200));
            api.authenticate(params);
            firstToken = api.getToken();
            // then perform request with expired token
            api.setToken(mockExpiredToken());
            String url2 = "https://api.cloudrain.com/v1/irrigations";
            mockResponse(client, url2, API_GET_IRRIGATIONS_EMPTY, 200);
            api.getIrrigations();
            secondToken = api.getToken();
        } catch (Exception e) {
            fail("Exception during testAuthenticate: " + e.toString());
        }
        assertNotNull(firstToken);
        assertNotNull(secondToken);
        assertNotEquals(firstToken, secondToken);
        assertEquals(ACCESS_TOKEN, secondToken.getAccessToken());
    }

    @Test
    void testGetIrrigations() throws Exception {
        List<Irrigation> result = testGetIrrigationsInternal(API_GET_IRRIGATIONS_SUCCESS);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
        assertNotNull(result.get(0).getStartTime());
        assertNotNull(result.get(0).getPlannedEndTime());
        assertEquals(CONTROLLER_ID, result.get(0).getControllerId());
        assertEquals(CONTROLLER_NAME, result.get(0).getControllerName());
    }

    @Test
    void testGetIrrigationsEmptyList() throws Exception {
        List<Irrigation> result = testGetIrrigationsInternal(API_GET_IRRIGATIONS_EMPTY);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private List<Irrigation> testGetIrrigationsInternal(String content) {
        List<Irrigation> result = new ArrayList<Irrigation>();
        String url = "https://api.cloudrain.com/v1/irrigations";
        try {
            CloudrainAPIv1Impl api = new CloudrainAPIv1Impl(mockResponse(url, content, 200));
            api.setToken(mockValidToken());
            result = api.getIrrigations();
        } catch (Exception e) {
            fail("Exception during testGetIrrigationsInternal: " + e.toString());
        }
        return result;
    }

    @Test
    void testGetZones() {
        List<Zone> result = testGetZonesInternal(API_GET_ZONES_SUCCESS);
        assertNotNull(result);
        assertEquals(result.size(), 3);
        assertNotNull(result.get(0));
        assertEquals("1234", result.get(0).getId());
        assertEquals("Garden", result.get(0).getName());
        assertEquals("XY191140030006", result.get(0).getControllerId());
        assertEquals("My Controller", result.get(0).getControllerName());
    }

    @Test
    void testGetZonesEmptyList() throws Exception {
        List<Zone> result = testGetZonesInternal(API_GET_ZONES_EMPTY);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    private List<Zone> testGetZonesInternal(String content) {
        List<Zone> result = new ArrayList<Zone>();
        String url = "https://api.cloudrain.com/v1/zones";
        try {
            CloudrainAPIv1Impl api = new CloudrainAPIv1Impl(mockResponse(url, content, 200));
            api.setToken(mockValidToken());
            result = api.getZones();
        } catch (Exception e) {
            fail("Exception during testGetZonesInternal: " + e.toString());
        }
        return result;
    }

    @Test
    void testGetZone() throws Exception {
        Zone result = null;
        String url = "https://api.cloudrain.com/v1/zones/1234";

        try {
            CloudrainAPIv1Impl api = new CloudrainAPIv1Impl(mockResponse(url, ZONE1, 200));
            api.setToken(mockValidToken());
            result = api.getZone(ZONE1_ID);
        } catch (Exception e) {
            fail("Exception during testGetZone: " + e.toString());
        }
        assertNotNull(result);
        assertEquals(ZONE1_ID, result.getId());
        assertEquals(ZONE1_NAME, result.getName());
        assertEquals(CONTROLLER_ID, result.getControllerId());
        assertEquals(CONTROLLER_NAME, result.getControllerName());
    }

    @Test
    void testGetIrrigation() {
        Irrigation result = testGetIrrigationInternal(API_GET_IRRIGATION_SUCCESS);
        assertNotNull(result);
        assertEquals(ZONE1_ID, result.getZoneId());
        assertEquals(Integer.parseInt(IRR1_REM_SEC), result.getRemainingSeconds());
        assertEquals(Integer.parseInt(IRR1_DURATION), result.getDuration());
        assertEquals(IRR1_START, String.format("%tR", result.getStartTime()));
        assertEquals(IRR1_END, String.format("%tR", result.getPlannedEndTime()));
        assertEquals(CONTROLLER_ID, result.getControllerId());
        assertEquals(CONTROLLER_NAME, result.getControllerName());
    }

    @Test
    void testGetIrrigationEmpty() {
        Irrigation result = testGetIrrigationInternal(API_GET_IRRIGATION_EMPTY);
        assertNull(result);
    }

    private @Nullable Irrigation testGetIrrigationInternal(String content) {
        Irrigation result = null;
        String url = "https://api.cloudrain.com/v1/zones/1234/irrigation";
        try {
            CloudrainAPIv1Impl api = new CloudrainAPIv1Impl(mockResponse(url, content, 200));
            api.setToken(mockValidToken());
            result = api.getIrrigation(ZONE1_ID);
        } catch (Exception e) {
            fail("Exception during testGetIrrigationInternal: " + e.toString());
        }
        return result;
    }

    private HttpClient mockHttpClient() {
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        when(mockHttpClient.isStarted()).thenReturn(true);
        return mockHttpClient;
    }

    private HttpClient mockResponse(String url, String content, int status) throws Exception {
        return mockResponse(mockHttpClient(), url, content, status);
    }

    private HttpClient mockResponse(HttpClient mockClient, String url, String content, int status) throws Exception {
        Request request = Mockito.mock(Request.class);
        ContentResponse response = Mockito.mock(ContentResponse.class);
        when(mockClient.newRequest(url)).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.header(any(HttpHeader.class), any(String.class))).thenReturn(request);
        when(request.content(any(ContentProvider.class))).thenReturn(request);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(content);
        return mockClient;
    }

    private Token mockValidToken() throws Exception {
        Token mockToken = Mockito.mock(Token.class);
        when(mockToken.isAccessTokenValid()).thenReturn(true);
        when(mockToken.getTokenType()).thenReturn("Bearer");
        when(mockToken.getAccessToken()).thenReturn(ACCESS_TOKEN);
        return mockToken;
    }

    private Token mockExpiredToken() throws Exception {
        Token mockToken = Mockito.mock(Token.class);
        when(mockToken.isAccessTokenValid()).thenReturn(false);
        when(mockToken.isRefreshTokenValid()).thenReturn(false);
        return mockToken;
    }
}
