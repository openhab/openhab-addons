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
package org.openhab.binding.bmwcardata.internal.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.DeviceAuthorization;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.PollResult;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.PollStatus;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.gson.Gson;

/**
 * Tests the {@link BMWCarDataAuthClient}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BMWCarDataAuthClientTest {

    private static final WireMockServer SERVER = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();
    private static final String DEVICE_CODE_PATH = "/gcdm/oauth/device/code";
    private static final String TOKEN_PATH = "/gcdm/oauth/token";
    private static final String TOKEN_RESPONSE = """
            {"access_token":"access","refresh_token":"refresh2","id_token":"id","gcid":"gcid-1",
             "expires_in":3600,"token_type":"Bearer"}""";

    private @NonNullByDefault({}) BMWCarDataAuthClient client;

    @BeforeAll
    static void startServer() throws Exception {
        SERVER.start();
        HTTP_CLIENT.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        SERVER.stop();
        HTTP_CLIENT.stop();
    }

    @BeforeEach
    void setUp() {
        SERVER.resetAll();
        client = new BMWCarDataAuthClient(HTTP_CLIENT, new Gson(), "client-1", "http://localhost:" + SERVER.port());
    }

    @Test
    void requestDeviceCodeSendsPkceChallenge() throws Exception {
        SERVER.stubFor(post(DEVICE_CODE_PATH).willReturn(okJson("""
                {"device_code":"dc","user_code":"UC-1","verification_uri":"https://example.com/link",
                 "expires_in":300,"interval":7}""")));

        DeviceAuthorization authorization = client.requestDeviceCode();

        assertEquals("dc", authorization.deviceCode());
        assertEquals("UC-1", authorization.userCode());
        assertEquals("https://example.com/link", authorization.verificationUri());
        assertEquals(7, authorization.interval());
        assertTrue(authorization.expiresAt().isAfter(Instant.now()));

        Map<String, String> form = form(SERVER.findAll(postRequestedFor(urlEqualTo(DEVICE_CODE_PATH))).get(0));
        assertEquals("client-1", form.get("client_id"));
        assertEquals("device_code", form.get("response_type"));
        assertEquals("S256", form.get("code_challenge_method"));
        assertTrue(form.getOrDefault("scope", "").contains("cardata:api:read"));
        assertTrue(form.getOrDefault("scope", "").contains("cardata:streaming:read"));
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(authorization.codeVerifier().getBytes(StandardCharsets.US_ASCII));
        assertEquals(Base64.getUrlEncoder().withoutPadding().encodeToString(digest), form.get("code_challenge"));
    }

    @Test
    void requestDeviceCodeReportsErrorDescription() {
        SERVER.stubFor(post(DEVICE_CODE_PATH).willReturn(
                badRequest().withBody("{\"error\":\"invalid_request\",\"error_description\":\"Unknown client_id\"}")));

        BMWCarDataException e = assertThrows(BMWCarDataException.class, () -> client.requestDeviceCode());

        assertEquals("Device authorization request failed: invalid_request (Unknown client_id)", e.getMessage());
        assertEquals(400, e.getHttpStatus());
    }

    @Test
    void requestDeviceCodeRejectsIncompleteResponse() {
        SERVER.stubFor(post(DEVICE_CODE_PATH).willReturn(okJson("{\"device_code\":\"dc\"}")));

        assertThrows(BMWCarDataException.class, () -> client.requestDeviceCode());
    }

    @Test
    void pollDeviceTokenMapsOAuthErrors() throws Exception {
        DeviceAuthorization authorization = new DeviceAuthorization("dc", "UC", "https://example.com",
                Instant.now().plusSeconds(300), 5, "verifier");

        assertEquals(PollStatus.PENDING, pollWithError(authorization, 403, "authorization_pending").status());
        assertEquals(PollStatus.SLOW_DOWN, pollWithError(authorization, 400, "slow_down").status());
        assertEquals(PollStatus.EXPIRED, pollWithError(authorization, 400, "expired_token").status());
        assertEquals(PollStatus.DENIED, pollWithError(authorization, 403, "access_denied").status());

        // without WWW-Authenticate header, the HTTP client keeps only the status of a 401
        SERVER.stubFor(post(TOKEN_PATH).willReturn(unauthorized().withBody("{\"error\":\"invalid_client\"}")));
        BMWCarDataException e = assertThrows(BMWCarDataException.class, () -> client.pollDeviceToken(authorization));
        assertEquals(401, e.getHttpStatus());
        assertFalse(e instanceof BMWCarDataAuthException);
    }

    @Test
    void pollDeviceTokenReturnsTokens() throws Exception {
        DeviceAuthorization authorization = new DeviceAuthorization("dc", "UC", "https://example.com",
                Instant.now().plusSeconds(300), 5, "verifier");
        SERVER.stubFor(post(TOKEN_PATH).willReturn(okJson(TOKEN_RESPONSE)));

        PollResult result = client.pollDeviceToken(authorization);

        assertEquals(PollStatus.AUTHORIZED, result.status());
        Tokens tokens = result.tokens();
        assertNotNull(tokens);
        assertEquals("access", tokens.accessToken());
        assertEquals("refresh2", tokens.refreshToken());
        assertEquals("id", tokens.idToken());
        assertEquals("gcid-1", tokens.gcid());
        assertFalse(tokens.toString().contains("access"));

        Map<String, String> form = form(SERVER.findAll(postRequestedFor(urlEqualTo(TOKEN_PATH))).get(0));
        assertEquals("urn:ietf:params:oauth:grant-type:device_code", form.get("grant_type"));
        assertEquals("dc", form.get("device_code"));
        assertEquals("verifier", form.get("code_verifier"));
    }

    private PollResult pollWithError(DeviceAuthorization authorization, int status, String error)
            throws BMWCarDataException {
        SERVER.stubFor(
                post(TOKEN_PATH).willReturn(aResponse().withStatus(status).withBody("{\"error\":\"" + error + "\"}")));
        return client.pollDeviceToken(authorization);
    }

    private static Map<String, String> form(LoggedRequest request) {
        Map<String, String> form = new HashMap<>();
        for (String pair : request.getBodyAsString().split("&")) {
            String[] parts = pair.split("=", 2);
            form.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
        }
        return form;
    }
}
