/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO;
import org.openhab.binding.livisismarthome.internal.handler.LivisiBridgeConfiguration;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class LivisiClientTest {

    private static final String DEVICES_URL = "http://127.0.0.1:8080/device";
    private static final String CAPABILITY_STATES_URL = "http://127.0.0.1:8080/capability/states";

    private LivisiClient client;
    private HttpClient httpClientMock;

    @BeforeEach
    public void before() throws Exception {
        httpClientMock = mock(HttpClient.class);

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("accessToken");

        OAuthClientService oAuthClientMock = mock(OAuthClientService.class);
        when(oAuthClientMock.getAccessTokenResponse()).thenReturn(accessTokenResponse);

        LivisiBridgeConfiguration bridgeConfiguration = new LivisiBridgeConfiguration();
        bridgeConfiguration.host = "127.0.0.1";
        client = new LivisiClient(bridgeConfiguration, oAuthClientMock, httpClientMock);
    }

    @Test
    public void testGetDevices() throws Exception {
        mockRequest(DEVICES_URL, "[ { id: 123 }, { id: 789, type: 'VariableActuator' } ]");
        assertEquals(2, client.getDevices(Arrays.asList("123", "456")).size());
    }

    @Test
    public void testGetDevicesNoDeviceIds() throws Exception {
        mockRequest(DEVICES_URL, "[ { id: 123 } ]");
        assertEquals(0, client.getDevices(Collections.emptyList()).size());
    }

    @Test
    public void testGetDevicesFalseDeviceIds() throws Exception {
        mockRequest(DEVICES_URL, "[ { id: 789 }]");
        assertEquals(0, client.getDevices(Arrays.asList("123", "456")).size());
    }

    @Test
    public void testGetDevicesNoDevicesNoDeviceIds() throws Exception {
        mockRequest(DEVICES_URL, "[]");
        assertEquals(0, client.getDevices(Collections.emptyList()).size());
    }

    @Test
    public void testGetDevicesNoDevicesDeviceIds() throws Exception {
        mockRequest(DEVICES_URL, "[]");
        assertEquals(0, client.getDevices(Arrays.asList("123", "456")).size());
    }

    @Test
    public void testGetCapabilityStates() throws Exception {
        mockRequest(CAPABILITY_STATES_URL,
                "[{\"id\":\"123\",\"state\":{\"isOpen\":{\"value\":false,\"lastChanged\":\"2022-03-12T20:54:50.6930000Z\"}}},{\"id\":\"456\",\"state\":{\"isOpen\":{\"value\":false,\"lastChanged\":\"2022-03-13T13:48:36.6830000Z\"}}},{\"id\":\"789\",\"state\":{\"isOpen\":{\"value\":true,\"lastChanged\":\"2022-03-13T13:48:36.6830000Z\"}}}]");
        assertEquals(3, client.getCapabilityStates().size());
    }

    @Test
    public void testGetCapabilityStatesStateNULL() throws Exception {
        mockRequest(CAPABILITY_STATES_URL,
                "[{\"id\":\"123\",\"state\":{\"isOpen\":{\"value\":false,\"lastChanged\":\"2022-03-12T20:54:50.6930000Z\"}}},{\"id\":\"456\",\"state\":[]},{\"id\":\"789\",\"state\":[]}]");
        List<CapabilityStateDTO> capabilityStates = client.getCapabilityStates();
        assertEquals(3, capabilityStates.size());
    }

    private void mockRequest(String url, String responseContent) throws Exception {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString()).thenReturn(responseContent);

        Request requestMock = mock(Request.class);
        when(httpClientMock.newRequest(url)).thenReturn(requestMock);
        when(requestMock.method(any(HttpMethod.class))).thenReturn(requestMock);
        when(requestMock.header(any(HttpHeader.class), any())).thenReturn(requestMock);
        when(requestMock.idleTimeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.timeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(response);
    }
}
