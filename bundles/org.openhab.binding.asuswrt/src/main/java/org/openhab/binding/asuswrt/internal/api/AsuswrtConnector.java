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

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtErrorConstants.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.getValueOrDefault;

import java.net.NoRouteToHostException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtConfiguration;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtCredentials;
import org.openhab.binding.asuswrt.internal.things.AsuswrtRouter;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link AsuswrtConnector} is a {@link AsuswrtHttpClient} that also keeps track of router configuration and
 * credentials.
 *
 * @author Christian Wild - Initial contribution
 * @author Daniel Malmgren - Nonce-based authentication support
 */
@NonNullByDefault
public class AsuswrtConnector extends AsuswrtHttpClient {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String RANDOM_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(AsuswrtConnector.class);
    private AsuswrtCredentials credentials;
    private AsuswrtConfiguration routerConfig;
    protected Long lastQuery = 0L;
    // serializes device requests so the next one is only sent once the previous one has finished
    private CompletableFuture<@Nullable Void> commandQueue = CompletableFuture.completedFuture(null);

    public AsuswrtConnector(AsuswrtRouter router) {
        super(router);
        routerConfig = router.getConfiguration();
        this.credentials = new AsuswrtCredentials(routerConfig);
    }

    /*
     * Connector commands
     */

    /**
     * Login to the router.
     */
    public Boolean login() {
        logout(); // logout (unset cookie) first
        router.errorHandler.reset();

        LoginChallenge challenge = getLoginChallenge();
        switch (challenge.protocol()) {
            case VERSION_2 -> loginVersion2(challenge);
            case LEGACY -> loginLegacy();
            case UNAVAILABLE -> {
                return false;
            }
        }

        if (cookieStore.isValid()) {
            router.setState(ThingStatus.ONLINE);
            return true;
        }
        router.errorHandler.raiseError(ERR_RESPONSE, "Authentication failed");
        return false;
    }

    private LoginChallenge getLoginChallenge() {
        String requestId = randomString(10);
        String payload = "{\"id\":\"" + requestId + "\"}";
        ContentResponse response = getSyncRequest(getURL("get_Nonce.cgi"), payload, JSON_CONTENT_TYPE);
        if (response == null) {
            return LoginChallenge.unavailable();
        }
        if (response.getStatus() == HttpStatus.NOT_FOUND_404
                || response.getStatus() == HttpStatus.METHOD_NOT_ALLOWED_405) {
            return LoginChallenge.legacy();
        }
        if (response.getStatus() != HttpStatus.OK_200) {
            router.errorHandler.raiseError(ERR_RESPONSE, "Nonce request returned HTTP " + response.getStatus());
            return LoginChallenge.unavailable();
        }

        String responseBody = response.getContentAsString();
        try {
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            if (responseJson.has("nonce")) {
                String nonce = responseJson.get("nonce").getAsString();
                if (!nonce.isBlank()) {
                    return LoginChallenge.version2(requestId, nonce);
                }
            }
            router.errorHandler.raiseError(ERR_RESPONSE, "Nonce response did not contain a nonce");
            return LoginChallenge.unavailable();
        } catch (RuntimeException e) {
            router.errorHandler.raiseError(ERR_RESPONSE, "Nonce response was not valid JSON");
            return LoginChallenge.unavailable();
        }
    }

    private void loginVersion2(LoginChallenge challenge) {
        String cnonce = randomString(32);
        String digestInput = credentials.getUsername() + ":" + challenge.nonce() + ":" + credentials.getPassword() + ":"
                + cnonce;
        String authorization = sha256(digestInput);
        String payload = formEncode(Map.of("group_id", "", "action_mode", "", "action_script", "", "action_wait", "5",
                "current_page", "Main_Login.asp", "next_page", "", "login_authorization", authorization, "id",
                challenge.requestId(), "cnonce", cnonce, "login_captcha", ""));

        ContentResponse response = getSyncRequest(getURL("login_v2.cgi"), payload);
        if (response != null) {
            setCookieFromResponse(response);
        }
    }

    private void loginLegacy() {
        String payload = "login_authorization="
                + URLEncoder.encode(credentials.getEncodedCredentials(), StandardCharsets.UTF_8);
        ContentResponse response = getSyncRequest(getURL("login.cgi"), payload);
        if (response != null) {
            setCookieFromResponse(response);
        }
    }

    private String randomString(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(RANDOM_CHARACTERS.charAt(RANDOM.nextInt(RANDOM_CHARACTERS.length())));
        }
        return result.toString();
    }

    private String sha256(String value) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private String formEncode(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    private enum LoginProtocol {
        VERSION_2,
        LEGACY,
        UNAVAILABLE
    }

    private record LoginChallenge(LoginProtocol protocol, String requestId, String nonce) {
        private static LoginChallenge version2(String requestId, String nonce) {
            return new LoginChallenge(LoginProtocol.VERSION_2, requestId, nonce);
        }

        private static LoginChallenge legacy() {
            return new LoginChallenge(LoginProtocol.LEGACY, "", "");
        }

        private static LoginChallenge unavailable() {
            return new LoginChallenge(LoginProtocol.UNAVAILABLE, "", "");
        }
    }

    /**
     * Logout and unsets the cookie.
     */
    public void logout() {
        this.cookieStore.resetCookie();
    }

    /**
     * Gets system information from the device.
     */
    public void querySysInfo(boolean asyncRequest) {
        queryDeviceData(CMD_GET_SYSINFO, asyncRequest);
    }

    /**
     * Queries data from the device.
     *
     * Requests are serialized: the next call will only be sent to the device once the previous one has finished.
     *
     * @param command command constant to sent
     * @param asyncRequest <code>true</code> if request should be sent asynchronous, <code>false</code> if synchronous
     */
    public void queryDeviceData(String command, boolean asyncRequest) {
        synchronized (this) {
            commandQueue = commandQueue.thenCompose(v -> {
                CompletableFuture<@Nullable Void> requestDone = new CompletableFuture<>();
                queryDeviceDataInternal(command, asyncRequest, () -> requestDone.complete(null));
                return requestDone;
            });
        }
    }

    /**
     * Queries data from the device.
     *
     * @param command command constant to sent
     * @param asyncRequest <code>true</code> if request should be sent asynchronous, <code>false</code> if synchronous
     * @param onComplete callback run once the request has finished, or {@code null} if not needed
     */
    private void queryDeviceDataInternal(String command, boolean asyncRequest, @Nullable Runnable onComplete) {
        logger.trace("({}) queryDeviceData", uid);
        Long now = System.currentTimeMillis();

        router.errorHandler.reset();
        if (cookieStore.cookieIsExpired()) {
            login();
        }

        if (now > this.lastQuery + HTTP_QUERY_MIN_GAP_MS) {
            String url = getURL("appGet.cgi");
            String payload = "hook=" + command;
            this.lastQuery = now;

            // Send payload as url parameter
            url = url + "?" + payload;
            url = url.replace(";", "%3B");

            // Send asynchronous or synchronous HTTP request
            if (asyncRequest) {
                sendAsyncRequest(url, payload, command, onComplete);
            } else {
                sendSyncRequest(url, payload, command);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        } else {
            logger.trace("({}) query skipped cause of min_gap: {} <- {}", uid, now, lastQuery);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Applies an update to nvram variable on the device. Will execute rcService if provided.
     *
     * Requests are serialized: the next call will only be sent to the device once the previous one has finished.
     *
     * @param variable nvram variable to update
     * @param value new value for the nvram variable
     * @param rcService optional rc service to apply the change
     * @param asyncRequest <code>true</code> if request should be sent asynchronous, <code>false</code> if synchronous
     */
    public void applyNVRAMCommand(String variable, String value, @Nullable String rcService, boolean asyncRequest) {
        synchronized (this) {
            commandQueue = commandQueue.thenCompose(v -> {
                CompletableFuture<@Nullable Void> requestDone = new CompletableFuture<>();
                applyNVRAMCommandInternal(variable, value, rcService, asyncRequest, () -> requestDone.complete(null));
                return requestDone;
            });
        }
    }

    /**
     * Applies an update to nvram variable on the device. Will execute rcService if provided.
     *
     * @param variable nvram variable to update
     * @param value new value for the nvram variable
     * @param rcService optional rc service to apply the change
     * @param asyncRequest <code>true</code> if request should be sent asynchronous, <code>false</code> if synchronous
     * @param onComplete callback run once the request has finished, or {@code null} if not needed
     */
    private void applyNVRAMCommandInternal(String variable, String value, @Nullable String rcService,
            boolean asyncRequest, @Nullable Runnable onComplete) {

        router.errorHandler.reset();
        if (cookieStore.cookieIsExpired()) {
            login();
        }

        String url = getURL("apply.cgi");
        String payload = "action_mode=apply&" + URLEncoder.encode(variable, StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);

        if (rcService != null) {
            payload += "&action_script=" + URLEncoder.encode(rcService, StandardCharsets.UTF_8);
        }

        logger.trace("Setting NVRAM variable: name={}, service={}", variable, rcService);

        // Send asynchronous or synchronous HTTP request
        if (asyncRequest) {
            sendAsyncRequest(url, payload, "applyNVRAMCommand", onComplete);
        } else {
            sendSyncRequest(url, payload, "applyNVRAMCommand");
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /*
     * Response handling
     */

    /**
     * Handle successful HTTP response by delegating to the connector class.
     *
     * @param responseBody response body as string
     * @param command command constant which was sent
     */
    @Override
    protected void handleHttpSuccessResponse(String responseBody, String command) {
        JsonObject jsonObject = getJsonFromString(responseBody);
        router.dataReceived(jsonObject, command);
    }

    /**
     * Handles HTTP result failures.
     *
     * @param e Throwable exception
     * @param payload full payload for debugging
     */
    @Override
    protected void handleHttpResultError(Throwable e, String payload) {
        super.handleHttpResultError(e, payload);
        String errorMessage = getValueOrDefault(e.getMessage(), "");

        if (e instanceof TimeoutException || e instanceof NoRouteToHostException) {
            router.errorHandler.raiseError(ERR_CONN_TIMEOUT, errorMessage);
            router.setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else if (e instanceof SSLException || e instanceof SSLKeyException || e instanceof SSLHandshakeException) {
            router.errorHandler.raiseError(ERR_SSL_EXCEPTION, payload);
            router.setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else if (e instanceof InterruptedException) {
            router.errorHandler.raiseError(new Exception(e), payload);
            router.setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        } else {
            router.errorHandler.raiseError(new Exception(e), errorMessage);
            router.setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    /*
     * Other
     */

    /**
     * Gets the target URL.
     */
    protected String getURL(String site) {
        String url = routerConfig.hostname;
        if (routerConfig.useSSL) {
            url = HTTPS_PROTOCOL + url;
            if (routerConfig.httpsPort != 443) {
                url = url + ":" + routerConfig.httpsPort;
            }
        } else {
            url = HTTP_PROTOCOL + url;
            if (routerConfig.httpPort != 80) {
                url = url + ":" + routerConfig.httpPort;
            }
        }
        return url + "/" + site;
    }
}
