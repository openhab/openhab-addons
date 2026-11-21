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
package org.openhab.binding.asuswrt.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtErrorHandler;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtConfiguration;
import org.openhab.binding.asuswrt.internal.things.AsuswrtRouter;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;

/**
 * Tests the AsusWRT authentication flows.
 *
 * @author Daniel Malmgren - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class AsuswrtConnectorTest {
    private static final String NONCE = "test-nonce";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "hunter2";

    private final Queue<ContentResponse> responses = new ArrayDeque<>();
    private final List<RecordedRequest> requests = new ArrayList<>();
    private HttpClient httpClient = mock(HttpClient.class);
    private AsuswrtRouter router = mock(AsuswrtRouter.class);
    private @NonNullByDefault({}) AsuswrtConnector connector;

    @BeforeEach
    void setUp() throws Exception {
        AsuswrtConfiguration configuration = new AsuswrtConfiguration();
        configuration.hostname = "router.example.test";
        configuration.username = USERNAME;
        configuration.password = PASSWORD;

        httpClient = mock(HttpClient.class);
        router = mock(AsuswrtRouter.class);
        router.errorHandler = new AsuswrtErrorHandler();
        when(router.getConfiguration()).thenReturn(configuration);
        when(router.getHttpClient()).thenReturn(httpClient);
        when(router.getUID()).thenReturn(new ThingUID("asuswrt:router:test"));
        doNothing().when(router).setState(any(ThingStatus.class));

        when(httpClient.newRequest(anyString())).thenAnswer(invocation -> newRequest(invocation.getArgument(0)));
        connector = new AsuswrtConnector(router);
    }

    @Test
    void loginUsesNonceChallengeAndCookieResponse() throws Exception {
        responses.add(response(HttpStatus.OK_200, "{\"nonce\":\"" + NONCE + "\"}"));
        responses.add(response(HttpStatus.OK_200, "<html></html>", "asus_token=test-token; HttpOnly"));

        assertTrue(connector.login());

        assertEquals(2, requests.size());
        RecordedRequest nonceRequest = requests.get(0);
        assertEquals("http://router.example.test/get_Nonce.cgi", nonceRequest.url);
        assertEquals("application/json", nonceRequest.contentType);
        assertEquals("http://router.example.test/", nonceRequest.headers.get("referer"));
        String requestId = nonceRequest.body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        assertTrue(requestId.matches("[A-Za-z0-9]{10}"));

        RecordedRequest loginRequest = requests.get(1);
        assertEquals("http://router.example.test/login_v2.cgi", loginRequest.url);
        Map<String, String> form = parseForm(loginRequest.body);
        assertEquals(requestId, form.get("id"));
        assertTrue(form.get("cnonce").matches("[A-Za-z0-9]{32}"));
        assertEquals(sha256(USERNAME + ":" + NONCE + ":" + PASSWORD + ":" + form.get("cnonce")),
                form.get("login_authorization"));
        assertEquals("asus_token=test-token", connector.cookieStore.getCookie());
        verify(router).setState(ThingStatus.ONLINE);
    }

    @Test
    void unsupportedNonceEndpointFallsBackToLegacyLogin() {
        responses.add(response(HttpStatus.NOT_FOUND_404, ""));
        responses.add(response(HttpStatus.OK_200, "{\"asus_token\":\"legacy-token\"}"));

        assertTrue(connector.login());

        assertEquals(2, requests.size());
        assertEquals("http://router.example.test/get_Nonce.cgi", requests.get(0).url);
        assertEquals("http://router.example.test/login.cgi", requests.get(1).url);
        assertEquals("YWRtaW46aHVudGVyMg==", parseForm(requests.get(1).body).get("login_authorization"));
    }

    @Test
    void failedVersion2LoginDoesNotTryLegacyCredentials() {
        responses.add(response(HttpStatus.OK_200, "{\"nonce\":\"" + NONCE + "\"}"));
        responses.add(response(HttpStatus.OK_200, "<html>Authentication failed</html>"));

        assertFalse(connector.login());

        assertEquals(2, requests.size());
        assertEquals("http://router.example.test/login_v2.cgi", requests.get(1).url);
        assertFalse(connector.cookieStore.isValid());
    }

    @Test
    void malformedNonceResponseDoesNotTryAnyLogin() {
        responses.add(response(HttpStatus.OK_200, "<html>Unexpected response</html>"));

        assertFalse(connector.login());

        assertEquals(1, requests.size());
        assertEquals("Nonce response was not valid JSON", router.errorHandler.getInfoMessage());
    }

    @Test
    void jsonWithoutNonceDoesNotTryAnyLogin() {
        responses.add(response(HttpStatus.OK_200, "{}"));

        assertFalse(connector.login());

        assertEquals(1, requests.size());
        assertEquals("Nonce response did not contain a nonce", router.errorHandler.getInfoMessage());
    }

    private Request newRequest(String url) throws Exception {
        Request request = mock(Request.class);
        RecordedRequest recordedRequest = new RecordedRequest(url);
        requests.add(recordedRequest);

        when(request.method(anyString())).thenReturn(request);
        when(request.header(anyString(), anyString())).thenAnswer(invocation -> {
            recordedRequest.headers.put(invocation.getArgument(0), invocation.getArgument(1));
            return request;
        });
        when(request.timeout(anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(request);
        when(request.content(any(ContentProvider.class), anyString())).thenAnswer(invocation -> {
            recordedRequest.body = contentAsString(invocation.getArgument(0));
            recordedRequest.contentType = invocation.getArgument(1);
            return request;
        });
        when(request.send()).thenAnswer(invocation -> responses.remove());
        return request;
    }

    private ContentResponse response(int status, String body, String... setCookieHeaders) {
        ContentResponse response = mock(ContentResponse.class);
        HttpFields headers = new HttpFields();
        for (String setCookieHeader : setCookieHeaders) {
            headers.add(HttpHeader.SET_COOKIE, setCookieHeader);
        }
        when(response.getStatus()).thenReturn(status);
        when(response.getContentAsString()).thenReturn(body);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getReason()).thenReturn(HttpStatus.getMessage(status));
        return response;
    }

    private String contentAsString(ContentProvider contentProvider) {
        StringBuilder body = new StringBuilder();
        for (ByteBuffer buffer : contentProvider) {
            body.append(StandardCharsets.UTF_8.decode(buffer.slice()));
        }
        return body.toString();
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String parameter : body.split("&")) {
            String[] pair = parameter.split("=", 2);
            result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.length == 2 ? pair[1] : "", StandardCharsets.UTF_8));
        }
        return result;
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static class RecordedRequest {
        private final String url;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String body = "";
        private String contentType = "";

        private RecordedRequest(String url) {
            this.url = url;
        }
    }
}
