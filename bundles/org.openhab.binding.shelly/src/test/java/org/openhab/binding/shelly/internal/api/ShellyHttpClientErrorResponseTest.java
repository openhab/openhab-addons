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
package org.openhab.binding.shelly.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.core.net.NetworkAddressService;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class ShellyHttpClientErrorResponseTest {

    private static final String DIGEST_CHALLENGE = "Digest qop=\"auth\", realm=\"shelly\", nonce=\"1\", algorithm=SHA-256";

    @Test
    void malformedGen2ErrorBodyIsReportedWithHttpStatus() throws Exception {
        ShellyHttpClient client = buildClient(500, "{\"id\":1,\"error\":{\"code\":-103,\"message\":");

        ShellyApiException e = assertThrows(ShellyApiException.class, () -> client.httpPost("/rpc", "{}"));
        assertEquals(500, e.getApiResult().httpCode);
    }

    @Test
    void wellFormedGen2ErrorBodyKeepsDeviceErrorCode() throws Exception {
        ShellyHttpClient client = buildClient(500,
                "{\"id\":1,\"error\":{\"code\":-103,\"message\":\"Invalid argument\"}}");

        ShellyApiException e = assertThrows(ShellyApiException.class, () -> client.httpPost("/rpc", "{}"));
        assertEquals(-103, e.getApiResult().httpCode);
        assertEquals("Invalid argument", e.getApiResult().response);
    }

    @Test
    void malformedGen2ErrorBodyKeepsAuthChallengeHeader() throws Exception {
        HttpFields headers = new HttpFields();
        headers.put(HttpHeader.WWW_AUTHENTICATE, DIGEST_CHALLENGE);
        ShellyHttpClient client = buildClient(401, "{\"id\":1,\"error\":{\"code\":401,", headers);

        ShellyApiException e = assertThrows(ShellyApiException.class, () -> client.httpPost("/rpc", "{}"));
        assertEquals(401, e.getApiResult().httpCode);
        assertEquals(DIGEST_CHALLENGE, e.getApiResult().authChallenge);
    }

    private static ShellyHttpClient buildClient(int status, String body) throws Exception {
        return buildClient(status, body, new HttpFields());
    }

    private static ShellyHttpClient buildClient(int status, String body, HttpFields headers) throws Exception {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(status);
        when(response.getContentAsString()).thenReturn(body);
        when(response.getHeaders()).thenReturn(headers);

        Request request = mock(Request.class);
        when(request.method(anyString())).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.header(any(HttpHeader.class), anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(request.getURI()).thenReturn(URI.create("http://192.168.1.2/rpc"));
        when(request.getMethod()).thenReturn("POST");
        when(response.getRequest()).thenReturn(request);

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.newRequest(anyString())).thenReturn(request);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test", "");
        return new ShellyHttpClient("test", config, httpClient);
    }
}
