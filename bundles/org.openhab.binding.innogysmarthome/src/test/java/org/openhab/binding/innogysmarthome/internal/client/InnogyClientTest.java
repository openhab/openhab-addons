/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;

/**
 * @author Sven Strohschein - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class InnogyClientTest {

    private static final String DEVICES_URL = "https://api.services-smarthome.de/API/1.1/device";

    private InnogyClient client;
    @Mock
    private OAuthClientService oAuthClient;
    @Mock
    private HttpClient httpClient;

    @BeforeEach
    public void before() throws Exception {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("accessToken");
        when(oAuthClient.getAccessTokenResponse()).thenReturn(accessTokenResponse);

        client = new InnogyClient(oAuthClient, httpClient);
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

    private void mockRequest(String url, String responseContent) throws Exception {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString()).thenReturn(responseContent);

        Request requestMock = mock(Request.class);
        when(httpClient.newRequest(url)).thenReturn(requestMock);
        when(requestMock.method(any(HttpMethod.class))).thenReturn(requestMock);
        when(requestMock.header(any(HttpHeader.class), any())).thenReturn(requestMock);
        when(requestMock.idleTimeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.timeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(response);
    }
}
