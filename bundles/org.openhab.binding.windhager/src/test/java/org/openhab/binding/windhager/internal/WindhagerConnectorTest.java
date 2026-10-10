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
package org.openhab.binding.windhager.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class WindhagerConnectorTest {

    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final String REALM = "BioWin test";
    private static final String NONCE = "fixed-test-nonce";
    private static final String OPAQUE = "fixed-test-opaque";
    private static final String OID = "1/60/0/23/103/0";
    private static final Pattern AUTH_PARAMETER = Pattern.compile("(\\w+)=\"?([^,\"]+)\"?");

    private final AtomicReference<Integer> responseStatus = new AtomicReference<>(200);
    private final AtomicReference<String> responseBody = new AtomicReference<>("{\"value\":\"23.97\"}");
    private final AtomicReference<String> authorizationHeader = new AtomicReference<>();

    private HttpServer server;
    private HttpClient httpClient;
    private WindhagerConnector connector;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handleRequest);
        server.start();

        httpClient = new HttpClient();
        httpClient.start();
        connector = new WindhagerConnector(httpClient, "127.0.0.1", server.getAddress().getPort(), USERNAME, PASSWORD);
    }

    @AfterEach
    void tearDown() throws Exception {
        connector.dispose();
        httpClient.stop();
        server.stop(0);
    }

    @Test
    void readValueAuthenticatesUsingDigestChallenge() throws Exception {
        WindhagerConnector.ValueReadResult result = connector.readValue(OID);

        assertEquals(23.97, Objects.requireNonNull(result.value()).doubleValue());
        assertFalse(result.communicationFailure());

        String authorization = authorizationHeader.get();
        assertNotNull(authorization);
        Map<String, String> parameters = parseAuthorization(authorization);
        assertEquals(USERNAME, parameters.get("username"));
        assertEquals(REALM, parameters.get("realm"));
        assertEquals(OPAQUE, parameters.get("opaque"));
        assertEquals("auth", parameters.get("qop"));

        URI requestUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/1.0/datapoint/" + OID);
        String ha1 = md5(USERNAME + ":" + REALM + ":" + PASSWORD);
        String ha2 = md5("GET:" + requestUri.getRawPath());
        String expectedResponse = md5(
                ha1 + ":" + NONCE + ":" + parameters.get("nc") + ":" + parameters.get("cnonce") + ":auth:" + ha2);
        assertEquals(expectedResponse, parameters.get("response"));
    }

    @Test
    void malformedNumericValueIsInvalidDataButNotACommunicationFailure() {
        responseBody.set("{\"value\":\"not-a-number\"}");

        WindhagerConnector.ValueReadResult result = connector.readValue(OID);

        assertNull(result.value());
        assertFalse(result.communicationFailure());
    }

    @Test
    void malformedJsonIsInvalidDataButNotACommunicationFailure() {
        responseBody.set("not-json");

        WindhagerConnector.ValueReadResult result = connector.readValue(OID);

        assertNull(result.value());
        assertFalse(result.communicationFailure());
    }

    @Test
    void serverFailureIsReportedAsCommunicationFailure() {
        responseStatus.set(503);

        WindhagerConnector.ValueReadResult result = connector.readValue(OID);

        assertNull(result.value());
        assertTrue(result.communicationFailure());
    }

    @Test
    void clientErrorDoesNotImplyTheServerIsUnreachable() {
        responseStatus.set(404);

        WindhagerConnector.ValueReadResult result = connector.readValue(OID);

        assertNull(result.value());
        assertFalse(result.communicationFailure());
    }

    @Test
    void invalidHostnameIsRejectedDuringConnectorCreation() {
        org.junit.jupiter.api.Assertions.assertThrows(URISyntaxException.class, () -> new WindhagerConnector(httpClient,
                "invalid hostname", server.getAddress().getPort(), USERNAME, PASSWORD));
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization == null) {
            exchange.getResponseHeaders().add("WWW-Authenticate", "Digest realm=\"" + REALM + "\", nonce=\"" + NONCE
                    + "\", opaque=\"" + OPAQUE + "\", algorithm=MD5, qop=\"auth,auth-int\"");
            exchange.sendResponseHeaders(401, -1);
        } else {
            authorizationHeader.set(authorization);
            byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus.get(), body.length);
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }

    private static Map<String, String> parseAuthorization(String authorization) {
        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = AUTH_PARAMETER.matcher(authorization.substring("Digest ".length()));
        while (matcher.find()) {
            parameters.put(matcher.group(1), matcher.group(2));
        }
        return parameters;
    }

    private static String md5(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte item : digest) {
            result.append(String.format("%02x", item & 0xff));
        }
        return result.toString();
    }
}
