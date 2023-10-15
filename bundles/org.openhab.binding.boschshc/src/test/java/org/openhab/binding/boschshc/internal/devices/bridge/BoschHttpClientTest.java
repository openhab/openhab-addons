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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.openhab.binding.boschshc.internal.services.binaryswitch.dto.BinarySwitchServiceState;
import org.slf4j.Logger;

/**
 * Tests cases for {@link BoschHttpClient}.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
class BoschHttpClientTest {

    private @NonNullByDefault({}) BoschHttpClient httpClient;

    @BeforeAll
    static void beforeAll() {
        BoschSslUtilTest.prepareTempFolderForKeyStore();
    }

    @BeforeEach
    void beforeEach() throws PairingFailedException {
        SslContextFactory sslFactory = new BoschSslUtil("127.0.0.1").getSslContextFactory();
        httpClient = new BoschHttpClient("127.0.0.1", "dummy", sslFactory);
        assertNotNull(httpClient);
    }

    @Test
    void getPublicInformationUrl() {
        assertEquals("https://127.0.0.1:8446/smarthome/public/information", httpClient.getPublicInformationUrl());
    }

    @Test
    void getPairingUrl() {
        assertEquals("https://127.0.0.1:8443/smarthome/clients", httpClient.getPairingUrl());
    }

    @Test
    void getBoschShcUrl() {
        assertEquals("https://127.0.0.1:8444/testEndpoint", httpClient.getBoschShcUrl("testEndpoint"));
    }

    @Test
    void getBoschSmartHomeUrl() {
        assertEquals("https://127.0.0.1:8444/smarthome/endpointForTest",
                httpClient.getBoschSmartHomeUrl("endpointForTest"));
    }

    @Test
    void getServiceUrl() {
        assertEquals("https://127.0.0.1:8444/smarthome/devices/testDevice/services/testService",
                httpClient.getServiceUrl("testService", "testDevice"));
    }

    @Test
    void getServiceStateUrl() {
        assertEquals("https://127.0.0.1:8444/smarthome/devices/testDevice/services/testService/state",
                httpClient.getServiceStateUrl("testService", "testDevice"));
    }

    @Test
    void isAccessPossible() throws InterruptedException {
        assertFalse(httpClient.isAccessPossible());
    }

    @Test
    void isOnline() throws InterruptedException {
        assertFalse(httpClient.isOnline());
    }

    @Test
    void isOnlineErrorResponse() throws InterruptedException, IllegalArgumentException, IllegalAccessException,
            TimeoutException, ExecutionException {
        BoschHttpClient mockedHttpClient = mock(BoschHttpClient.class);
        when(mockedHttpClient.isOnline()).thenCallRealMethod();
        when(mockedHttpClient.getPublicInformationUrl()).thenCallRealMethod();

        // mock a logger using reflection to avoid NPEs during logger calls
        Logger mockedLogger = mock(Logger.class);
        List<Field> fields = ReflectionSupport.findFields(BoschHttpClient.class,
                f -> "logger".equalsIgnoreCase(f.getName()), HierarchyTraversalMode.TOP_DOWN);
        Field field = fields.iterator().next();
        field.setAccessible(true);
        field.set(mockedHttpClient, mockedLogger);

        Request request = mock(Request.class);
        when(mockedHttpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(500);
        assertFalse(mockedHttpClient.isOnline());
    }

    @Test
    void isOnlineMockedResponse() throws InterruptedException, TimeoutException, ExecutionException,
            IllegalArgumentException, IllegalAccessException {
        BoschHttpClient mockedHttpClient = mock(BoschHttpClient.class);
        when(mockedHttpClient.isOnline()).thenCallRealMethod();
        when(mockedHttpClient.getPublicInformationUrl()).thenCallRealMethod();

        // mock a logger using reflection to avoid NPEs during logger calls
        Logger mockedLogger = mock(Logger.class);
        List<Field> fields = ReflectionSupport.findFields(BoschHttpClient.class,
                f -> "logger".equalsIgnoreCase(f.getName()), HierarchyTraversalMode.TOP_DOWN);
        Field field = fields.iterator().next();
        field.setAccessible(true);
        field.set(mockedHttpClient, mockedLogger);

        Request request = mock(Request.class);
        when(mockedHttpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("response");
        assertTrue(mockedHttpClient.isOnline());
    }

    @Test
    void doPairing() throws InterruptedException {
        assertFalse(httpClient.doPairing());
    }

    @Test
    void createRequest() {
        Request request = httpClient.createRequest("https://127.0.0.1", HttpMethod.GET);
        assertNotNull(request);
        assertEquals("3.2", request.getHeaders().get("api-version"));
    }

    @Test
    void createRequestWithObject() {
        BinarySwitchServiceState binarySwitchState = new BinarySwitchServiceState();
        binarySwitchState.on = true;
        Request request = httpClient.createRequest("https://127.0.0.1", HttpMethod.GET, binarySwitchState);
        assertNotNull(request);
        assertEquals("{\"on\":true,\"stateType\":\"binarySwitchState\",\"@type\":\"binarySwitchState\"}",
                StandardCharsets.UTF_8.decode(request.getContent().iterator().next()).toString());
    }

    @Test
    void sendRequest() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"jsonrpc\": \"2.0\", \"result\": \"test result\"}");

        SubscribeResult subscribeResult = httpClient.sendRequest(request, SubscribeResult.class,
                SubscribeResult::isValid, null);
        assertEquals("2.0", subscribeResult.getJsonrpc());
        assertEquals("test result", subscribeResult.getResult());
    }

    @Test
    void sendRequestResponseError()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(500);
        ExecutionException e = assertThrows(ExecutionException.class,
                () -> httpClient.sendRequest(request, SubscribeResult.class, SubscribeResult::isValid, null));
        assertEquals("Send request failed with status code 500", e.getMessage());
    }

    @Test
    void sendRequestResponseErrorWithErrorHandler()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(500);
        when(response.getContentAsString()).thenReturn(
                "{\"@type\": \"JsonRestExceptionResponseEntity\", \"errorCode\": \"500\", \"statusCode\": \"500\"}");

        BoschSHCException e = assertThrows(BoschSHCException.class, () -> httpClient.sendRequest(request, Device.class,
                Device::isValid, (Integer statusCode, String content) -> new BoschSHCException("test exception")));
        assertEquals("test exception", e.getMessage());
    }

    @Test
    void sendRequestEmptyResponse()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        ExecutionException e = assertThrows(ExecutionException.class,
                () -> httpClient.sendRequest(request, SubscribeResult.class, SubscribeResult::isValid, null));
        assertEquals(
                "Received no content in response, expected type org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult",
                e.getMessage());
    }

    @Test
    void sendRequestInvalidResponse()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(
                "{\"@type\": \"JsonRestExceptionResponseEntity\", \"errorCode\": \"500\", \"statusCode\": \"500\"}");
        ExecutionException e = assertThrows(ExecutionException.class,
                () -> httpClient.sendRequest(request, SubscribeResult.class, sr -> false, null));
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(
                "Received invalid content for type org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult:"));
    }

    @Test
    void sendRequestInvalidSyntaxInResponse()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"@type\": \"JsonRestExceptionResponseEntity}");
        ExecutionException e = assertThrows(ExecutionException.class,
                () -> httpClient.sendRequest(request, SubscribeResult.class, sr -> false, null));
        assertEquals(
                "Received invalid content in response, expected type org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult: com.google.gson.stream.MalformedJsonException: Unterminated string at line 1 column 44 path $.@type",
                e.getMessage());
    }
}
