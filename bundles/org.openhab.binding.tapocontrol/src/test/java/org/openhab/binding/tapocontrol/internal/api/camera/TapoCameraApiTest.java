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
package org.openhab.binding.tapocontrol.internal.api.camera;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests the camera login handshake and secured passthrough using a scripted transport.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class TapoCameraApiTest {
    private static final String CNONCE = "9B5F6CE8";
    private static final String NONCE = "18D1CD86D1AE9934";
    private static final String PW_HASH_SHA = "5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8";
    private static final String DEVICE_CONFIRM = TapoCameraCrypto.computeDeviceConfirm(PW_HASH_SHA, CNONCE, NONCE);

    private Deque<String> responses;
    private TestApi api;

    static class TestApi extends TapoCameraApi {
        private final Deque<String> responses;
        String capturedUrl;
        Map<String, String> capturedHeaders;
        String capturedBody;

        TestApi(Deque<String> responses) {
            super(null, "192.168.1.50", 443, "admin", "password", new Gson(), CNONCE);
            this.responses = responses;
        }

        @Override
        protected String post(String url, Map<String, String> headers, String body) throws Exception {
            this.capturedUrl = url;
            this.capturedHeaders = headers;
            this.capturedBody = body;
            String next = responses.poll();
            if (next == null) {
                throw new java.io.IOException("no scripted response left");
            }
            return next;
        }
    }

    @BeforeEach
    void setUp() {
        responses = new ConcurrentLinkedDeque<>();
        api = new TestApi(responses);
    }

    @Test
    void legacyLoginStoresToken() throws Exception {
        // camera rejects the encrypted probe, so login falls back to the legacy hashed login
        responses.add("{\"error_code\":-40413}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"LEGACYTOKEN\"}}");
        api.login();
        assertTrue(api.isLoggedIn());
        assertEquals("LEGACYTOKEN", api.getSession().stok());
        assertFalse(api.getSession().secure());
        assertTrue(api.capturedUrl.endsWith("/"));
        assertTrue(api.capturedBody.contains("\"hashed\":true"));
        assertTrue(api.capturedBody.contains("5F4DCC3B5AA765D61D8327DEB882CF99")); // md5 password hash
    }

    @Test
    void secureLoginPerformsHandshakeAndEncryptedCommand() throws Exception {
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"nonce\":\"" + NONCE + "\",\"device_confirm\":\""
                + DEVICE_CONFIRM + "\"}}}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"SECRETTOKEN\",\"start_seq\":100}}");
        api.login();

        assertTrue(api.isLoggedIn());
        TapoCameraSession session = api.getSession();
        assertTrue(session.secure());
        assertEquals("SECRETTOKEN", session.stok());
        assertEquals(PW_HASH_SHA, session.passwordHash());
        assertTrue(api.capturedBody.contains("\"digest_passwd\":"));

        // send a command: wrapped envelope, Seq/Tapo_tag headers, encrypted inner payload, decrypted answer
        responses.add("{\"error_code\":0,\"result\":{\"response\":\""
                + encryptedEnvelope("{\"error_code\":0,\"result\":{\"led\":{\"config\":{\"enabled\":\"on\"}}}}")
                + "\"}}");
        JsonObject answer = api.sendCommand(TapoCameraCommands.getLedConfig());

        assertEquals(0, answer.get("error_code").getAsInt());
        assertTrue(api.capturedUrl.contains("/stok=SECRETTOKEN/ds"));
        assertEquals("100", api.capturedHeaders.get("Seq"));
        assertNotNull(api.capturedHeaders.get("Tapo_tag"));
        String wrapped = api.capturedBody;
        assertTrue(wrapped.contains("\"method\":\"securePassthrough\""));

        // next sequence number: login set 100, first secured command consumed 101
        assertEquals(101, api.getCurrentSeq());
    }

    @Test
    void secureCommandAcceptsDirectResponse() throws Exception {
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"nonce\":\"" + NONCE + "\",\"device_confirm\":\""
                + DEVICE_CONFIRM + "\"}}}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"SECRETTOKEN\",\"start_seq\":100}}");
        api.login();
        responses.add("{\"error_code\":0,\"led\":{\"config\":{\"enabled\":\"off\"}}}");

        JsonObject answer = api.sendCommand(TapoCameraCommands.getLedConfig());

        assertEquals("off", answer.getAsJsonObject("led").getAsJsonObject("config").get("enabled").getAsString());
    }

    @Test
    void secureLoginSupportsMd5PasswordMethod() throws Exception {
        // some firmware stores the Tapo API password MD5-hashed, revealed by device_confirm
        String pwHashMd5 = TapoCameraCrypto.md5HashUpper("password");
        String md5Confirm = TapoCameraCrypto.computeDeviceConfirm(pwHashMd5, CNONCE, NONCE);
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"nonce\":\"" + NONCE + "\",\"device_confirm\":\""
                + md5Confirm + "\"}}}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"MD5TOKEN\",\"start_seq\":7}}");

        api.login();

        TapoCameraSession session = api.getSession();
        assertTrue(session.secure());
        assertEquals("MD5TOKEN", session.stok());
        assertEquals(pwHashMd5, session.passwordHash());
        assertTrue(api.capturedBody.contains(
                "\"digest_passwd\":\"" + TapoCameraCrypto.computeDigestPasswd(pwHashMd5, CNONCE, NONCE) + "\""));

        // secured commands must be encrypted with keys derived from the matched hash
        responses.add("{\"error_code\":0,\"result\":{\"response\":\"" + encryptedEnvelope(
                "{\"error_code\":0,\"result\":{\"led\":{\"config\":{\"enabled\":\"on\"}}}}", pwHashMd5) + "\"}}");
        JsonObject answer = api.sendCommand(TapoCameraCommands.getLedConfig());
        assertEquals(0, answer.get("error_code").getAsInt());
    }

    @Test
    void authFailureThrowsWithErrorCode() {
        responses.add("{\"error_code\":-40401}");
        assertThrows(TapoCameraApiException.class, () -> api.login());
        assertFalse(api.isLoggedIn());
    }

    @Test
    void deviceConfirmMismatchFailsAuthWithoutLegacyFallback() {
        // the camera offered the secure handshake, so a mismatch means wrong credentials -
        // it must be reported as an auth failure instead of attempting the legacy login
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"code\":-40401,\"nonce\":\"" + NONCE
                + "\",\"device_confirm\":\"INVALID\"}}}");
        var e = assertThrows(TapoCameraApiException.class, () -> api.login());

        assertEquals(TapoCameraApi.ERROR_AUTH_FAILURE, e.getErrorCode());
        assertFalse(api.isLoggedIn());
        assertTrue(responses.isEmpty()); // no legacy fallback request was made
    }

    @Test
    void cameraLockoutIsReportedAsAuthFailure() {
        // after too many failed attempts the camera signals a temporary lockout
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"sec_left\":30}}}");
        var e = assertThrows(TapoCameraApiException.class, () -> api.login());

        assertEquals(TapoCameraApi.ERROR_AUTH_FAILURE, e.getErrorCode());
        assertTrue(e.getMessage().contains("30"));
        assertTrue(responses.isEmpty());
    }

    @Test
    void sendCommandWithoutLoginThrows() {
        assertThrows(TapoCameraApiException.class, () -> api.sendCommand(TapoCameraCommands.getLedConfig()));
    }

    @Test
    void sendCommandClearsSessionOnAuthError() throws Exception {
        responses.add("{\"error_code\":-40413}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"T\",\"start_seq\":1}}");
        api.login();
        responses.add("{\"error_code\":-40401}");
        assertThrows(TapoCameraApiException.class, () -> api.sendCommand(TapoCameraCommands.getLedConfig()));
        assertFalse(api.isLoggedIn());
    }

    @Test
    void sendSecuredClearsSessionOnOuterAuthError() throws Exception {
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"nonce\":\"" + NONCE + "\",\"device_confirm\":\""
                + DEVICE_CONFIRM + "\"}}}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"TOKEN\",\"start_seq\":10}}");
        api.login();
        assertTrue(api.isLoggedIn());
        TapoCameraSession session = api.getSession();
        assertNotNull(session);
        assertTrue(session.secure());

        responses.add("{\"error_code\":-40401}");
        var e = assertThrows(TapoCameraApiException.class, () -> api.sendCommand(TapoCameraCommands.getLedConfig()));
        assertEquals(-40401, e.getErrorCode());
        assertFalse(api.isLoggedIn());
    }

    @Test
    void legacySendCommandPostsPlainPayload() throws Exception {
        responses.add("{\"error_code\":-40413}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"T\"}}");
        api.login();
        responses.add("{\"error_code\":0,\"result\":{\"led\":{\"config\":{\"enabled\":\"off\"}}}}");
        JsonObject answer = api.sendCommand(TapoCameraCommands.getLedConfig());
        assertEquals(0, answer.get("error_code").getAsInt());
        assertFalse(api.capturedBody.contains("securePassthrough"));
        assertNull(api.capturedHeaders == null ? null : api.capturedHeaders.get("Seq"));
    }

    @Test
    void sendCommandThrowsOnDeviceError() throws Exception {
        responses.add("{\"error_code\":-40413}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"T\"}}");
        api.login();
        responses.add("{\"error_code\":-40100}");
        var e = assertThrows(TapoCameraApiException.class, () -> api.sendCommand(TapoCameraCommands.getLedConfig()));
        assertEquals(-40100, e.getErrorCode());
    }

    @Test
    void sendCommandThrowsOnSecuredInnerError() throws Exception {
        responses.add("{\"error_code\":-40413,\"result\":{\"data\":{\"nonce\":\"" + NONCE + "\",\"device_confirm\":\""
                + DEVICE_CONFIRM + "\"}}}");
        responses.add("{\"error_code\":0,\"result\":{\"stok\":\"SECRETTOKEN\",\"start_seq\":100}}");
        api.login();
        responses.add("{\"error_code\":0,\"result\":{\"response\":\"" + encryptedEnvelope("{\"error_code\":-40100}")
                + "\"}}");
        var e = assertThrows(TapoCameraApiException.class, () -> api.sendCommand(TapoCameraCommands.getLedConfig()));
        assertEquals(-40100, e.getErrorCode());
    }

    private static String encryptedEnvelope(String innerJson) throws Exception {
        return encryptedEnvelope(innerJson, PW_HASH_SHA);
    }

    private static String encryptedEnvelope(String innerJson, String passwordHash) throws Exception {
        String lsk = TapoCameraCrypto.deriveLsk(passwordHash, CNONCE, NONCE);
        String ivb = TapoCameraCrypto.deriveIvb(passwordHash, CNONCE, NONCE);
        return TapoCameraCrypto.encryptBase64(innerJson, lsk, ivb);
    }
}
