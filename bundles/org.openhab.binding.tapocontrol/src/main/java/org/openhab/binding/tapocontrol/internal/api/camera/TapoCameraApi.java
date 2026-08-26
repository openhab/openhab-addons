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

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Transport and session management for the Tapo camera local API. Supports the secure
 * challenge-response handshake (encrypt_type 3) with AES secured-passthrough commands and
 * the legacy MD5-hash login. Not thread-safe per instance; callers serialize access.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TapoCameraApi {
    public static final int ERROR_AUTH_FAILURE = -40401;
    private static final int ERROR_ENCRYPT_UNSUPPORTED = -40413;
    private static final String USER_AGENT = "Tapo CameraClient Android";
    private static final Logger LOGGER = LoggerFactory.getLogger(TapoCameraApi.class);

    private final @Nullable HttpClient httpClient; // may be null only in unit-test subclasses
    private final String baseUrl;
    private final String username;
    private final String password;
    private final Gson gson;
    private final @Nullable String fixedCnonce;

    private @Nullable TapoCameraSession session;
    private long currentSeq;

    public TapoCameraApi(HttpClient httpClient, String host, int port, String username, String password, Gson gson) {
        this(httpClient, host, port, username, password, gson, null);
    }

    TapoCameraApi(@Nullable HttpClient httpClient, String host, int port, String username, String password, Gson gson,
            @Nullable String fixedCnonce) {
        this.httpClient = httpClient;
        this.baseUrl = "https://" + host + ":" + port;
        this.username = username;
        this.password = password;
        this.gson = gson;
        this.fixedCnonce = fixedCnonce;
    }

    public synchronized boolean isLoggedIn() {
        return session != null;
    }

    public synchronized @Nullable TapoCameraSession getSession() {
        return session;
    }

    public synchronized long getCurrentSeq() {
        return currentSeq;
    }

    public synchronized void clearSession() {
        session = null;
    }

    public synchronized void login() throws TapoCameraApiException {
        clearSession();
        try {
            loginSecure();
        } catch (TapoCameraApiException e) {
            if (e.getErrorCode() != ERROR_ENCRYPT_UNSUPPORTED) {
                throw e;
            }
            LOGGER.debug("{}: secure handshake failed ({}), falling back to legacy login", baseUrl, e.getMessage());
            loginLegacy();
        }
    }

    private void loginSecure() throws TapoCameraApiException {
        String cnonce = newCnonce();
        JsonObject response = execute(baseUrl + "/", Map.of(), buildLoginParams(true, cnonce, null));
        int errorCode = response.get("error_code").getAsInt();
        if (errorCode == ERROR_AUTH_FAILURE) {
            throw new TapoCameraApiException("authentication failed", errorCode);
        }
        if (errorCode != ERROR_ENCRYPT_UNSUPPORTED || !response.has("result")
                || !response.getAsJsonObject("result").has("data")) {
            throw new TapoCameraApiException("unexpected probe response", errorCode);
        }
        JsonObject data = response.getAsJsonObject("result").getAsJsonObject("data");
        if (data.has("code") && data.get("code").getAsInt() != 0) {
            LOGGER.debug("{}: handshake challenge reports code {}", baseUrl, data.get("code"));
        }
        if (data.has("sec_left") && data.get("sec_left").getAsInt() > 0) {
            throw new TapoCameraApiException(
                    "camera temporarily locked, retry in " + data.get("sec_left").getAsInt() + " seconds",
                    ERROR_AUTH_FAILURE);
        }
        if (!data.has("nonce") || !data.has("device_confirm")) {
            throw new TapoCameraApiException("incomplete handshake data", errorCode);
        }
        String nonce = data.get("nonce").getAsString();
        String deviceConfirm = data.get("device_confirm").getAsString();
        // cameras store the account password either SHA-256- or MD5-hashed; device_confirm reveals which
        String sha256Hash = TapoCameraCrypto.securePasswordHash(password);
        String md5Hash = TapoCameraCrypto.md5HashUpper(password);
        String passwordHash;
        if (TapoCameraCrypto.validateDeviceConfirm(sha256Hash, cnonce, nonce, deviceConfirm)) {
            passwordHash = sha256Hash;
        } else if (TapoCameraCrypto.validateDeviceConfirm(md5Hash, cnonce, nonce, deviceConfirm)) {
            passwordHash = md5Hash;
        } else {
            // the camera supports the secure handshake, so this is a credential problem - report it as
            // an auth failure instead of falling back to the legacy login it cannot accept anyway
            throw new TapoCameraApiException("device confirmation mismatch - check camera account password",
                    ERROR_AUTH_FAILURE);
        }
        JsonObject loginResponse = execute(baseUrl + "/", Map.of(),
                buildLoginParams(true, cnonce, TapoCameraCrypto.computeDigestPasswd(passwordHash, cnonce, nonce)));
        int loginCode = loginResponse.get("error_code").getAsInt();
        if (loginCode != 0 || !loginResponse.has("result") || !loginResponse.getAsJsonObject("result").has("stok")) {
            throw new TapoCameraApiException("secure login rejected", loginCode);
        }
        JsonObject result = loginResponse.getAsJsonObject("result");
        long startSeq = result.get("start_seq").getAsLong();
        currentSeq = startSeq;
        session = new TapoCameraSession(result.get("stok").getAsString(), startSeq, cnonce, nonce, passwordHash,
                TapoCameraCrypto.deriveLsk(passwordHash, cnonce, nonce),
                TapoCameraCrypto.deriveIvb(passwordHash, cnonce, nonce), true);
        LOGGER.debug("{}: secure login ok, start_seq {}", baseUrl, startSeq);
    }

    private JsonObject buildLoginParams(boolean secure, String cnonce, @Nullable String digestPasswd) {
        JsonObject params = new JsonObject();
        params.addProperty("username", username);
        if (secure) {
            params.addProperty("encrypt_type", "3");
            params.addProperty("cnonce", cnonce);
            if (digestPasswd != null) {
                params.addProperty("digest_passwd", digestPasswd);
            }
        } else {
            params.addProperty("hashed", true);
            params.addProperty("password", TapoCameraCrypto.legacyPasswordHash(password));
        }
        JsonObject login = new JsonObject();
        login.addProperty("method", "login");
        login.add("params", params);
        return login;
    }

    private void loginLegacy() throws TapoCameraApiException {
        JsonObject response = execute(baseUrl + "/", Map.of(), buildLoginParams(false, "", null));
        int errorCode = response.get("error_code").getAsInt();
        if (errorCode != 0 || !response.has("result") || !response.getAsJsonObject("result").has("stok")) {
            throw new TapoCameraApiException(errorCode == 0 ? "legacy login missing stok" : "legacy login failed",
                    errorCode);
        }
        session = new TapoCameraSession(response.getAsJsonObject("result").get("stok").getAsString(), 0, "", "", "", "",
                "", false);
        LOGGER.debug("{}: legacy login ok", baseUrl);
    }

    public synchronized JsonObject sendCommand(JsonObject command) throws TapoCameraApiException {
        TapoCameraSession currentSession = session;
        if (currentSession == null) {
            throw new TapoCameraApiException("not logged in", 0);
        }
        JsonObject response = currentSession.secure() ? sendSecured(currentSession, command)
                : execute(baseUrl + "/stok=" + currentSession.stok() + "/ds", Map.of(), command);
        int errorCode = response.get("error_code").getAsInt();
        if (errorCode == ERROR_AUTH_FAILURE) {
            LOGGER.debug("{}: session invalidated by camera", baseUrl);
            clearSession();
            throw new TapoCameraApiException("session expired", errorCode);
        }
        if (errorCode != 0) {
            // surface device-side failures to callers instead of silently returning them
            throw new TapoCameraApiException("command rejected by camera", errorCode);
        }
        return response;
    }

    private JsonObject sendSecured(TapoCameraSession currentSession, JsonObject command) throws TapoCameraApiException {
        try {
            String encrypted = TapoCameraCrypto.encryptBase64(command.toString(), currentSession.lsk(),
                    currentSession.ivb());
            JsonObject requestParams = new JsonObject();
            requestParams.addProperty("request", encrypted);
            JsonObject envelope = new JsonObject();
            envelope.addProperty("method", "securePassthrough");
            envelope.add("params", requestParams);

            long seq = ++currentSeq;
            Map<String, String> headers = new HashMap<>();
            headers.put("Seq", Long.toString(seq));
            headers.put("Tapo_tag", TapoCameraCrypto.computeTapoTag(currentSession.passwordHash(),
                    currentSession.cnonce(), envelope.toString(), seq));
            JsonObject outerResponse = execute(baseUrl + "/stok=" + currentSession.stok() + "/ds", headers, envelope);
            int errorCode = outerResponse.get("error_code").getAsInt();
            if (errorCode != 0) {
                throw new TapoCameraApiException("secured command failed", errorCode);
            }
            if (outerResponse.has("result") && outerResponse.get("result").isJsonObject()
                    && outerResponse.getAsJsonObject("result").has("response")) {
                String cipher = outerResponse.getAsJsonObject("result").get("response").getAsString();
                return JsonParser
                        .parseString(TapoCameraCrypto.decryptBase64(cipher, currentSession.lsk(), currentSession.ivb()))
                        .getAsJsonObject();
            }
            // Some firmware returns the decrypted module response directly instead of wrapping it
            // in result.response.
            return outerResponse;
        } catch (TapoCameraApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TapoCameraApiException("interrupted during secured command", 0);
        } catch (Exception e) {
            LOGGER.debug("{}: secured command failed: {}", baseUrl, e.getMessage());
            throw new TapoCameraApiException("secured command transport error", 0);
        }
    }

    /** Merges extra headers and delegates to the overridable post seam. */
    private JsonObject execute(String url, Map<String, String> extraHeaders, JsonObject payload)
            throws TapoCameraApiException {
        try {
            Map<String, String> headers = new HashMap<>(extraHeaders);
            String raw = post(url, headers, payload.toString());
            return JsonParser.parseString(raw).getAsJsonObject();
        } catch (TapoCameraApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TapoCameraApiException("interrupted", 0);
        } catch (Exception e) {
            LOGGER.debug("{}: request to {} failed: {}", baseUrl, url, e.getMessage());
            throw new TapoCameraApiException("transport error: " + e.getMessage(), 0);
        }
    }

    /**
     * HTTP seam — production implementation uses the shared Jetty client; unit tests override it
     * with scripted responses.
     */
    protected String post(String url, Map<String, String> headers, String body) throws Exception {
        HttpClient client = httpClient;
        if (client == null) {
            throw new IllegalStateException("no httpClient configured");
        }
        Request request = client.newRequest(url).method(HttpMethod.POST).timeout(15, TimeUnit.SECONDS);
        request.header(HttpHeader.CONTENT_TYPE, "application/json; charset=UTF-8");
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.CONNECTION, "close");
        request.header("requestByApp", "true");
        request.agent(USER_AGENT);
        headers.forEach(request::header);
        request.content(new StringContentProvider(body, StandardCharsets.UTF_8));
        ContentResponse response = request.send();
        String content = response.getContentAsString();
        if (content == null) {
            throw new IllegalStateException("empty response body");
        }
        return content;
    }

    private String newCnonce() {
        String fixed = fixedCnonce;
        if (fixed != null) {
            return fixed;
        }
        byte[] bytes = new byte[4];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder(8);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
