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
package org.openhab.binding.tplinkbridge.klap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.BytesRequestContent;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side of TP-Link's KLAP protocol (version 2 auth hash), which carries an opaque JSON payload over HTTP to a
 * device. It is payload neutral: Kasa devices exchange IOT JSON over it and Tapo devices SMART JSON.
 * <p>
 * The session is established on first use and renewed shortly before it expires or when the device rejects it. All
 * candidate credentials are checked against a single handshake1 response, so alternatives cost no extra round trip.
 * The credentials are fetched again at every login, so changes on the account bridge apply on the next login.
 *
 * @author Christopher Smith - Initial contribution, derived from the tapocontrol KLAP session by Christian Wild
 */
@NonNullByDefault
public class KlapTransport {

    private static final int SEED_LENGTH = 16;
    private static final long TIMEOUT_MS = 5_000;
    /** Renew a session this long before the device would expire it. */
    private static final long RENEW_MARGIN_MS = 40_000;
    private static final long DEFAULT_SESSION_TIMEOUT_S = 86_400;
    private static final String SESSION_COOKIE = "TP_SESSIONID";
    private static final String SESSION_TIMEOUT = "TIMEOUT";

    private final Logger logger = LoggerFactory.getLogger(KlapTransport.class);
    private final SecureRandom random = new SecureRandom();
    private final HttpClient httpClient;
    private final Supplier<List<TPLinkCredentials>> credentials;

    private @Nullable String host;
    private @Nullable KlapCipher cipher;
    private String cookie = "";
    private long expiresAt;

    /**
     * @param httpClient started HTTP client
     * @param host device address, optionally with a port
     * @param credentials candidate credentials in order of preference, fetched at every login
     */
    public KlapTransport(HttpClient httpClient, @Nullable String host, Supplier<List<TPLinkCredentials>> credentials) {
        this.httpClient = httpClient;
        this.host = host;
        this.credentials = credentials;
    }

    /**
     * Changes the device address and drops the current session.
     */
    public synchronized void setHost(String host) {
        if (!host.equals(this.host)) {
            this.host = host;
            close();
        }
    }

    /**
     * Sends a payload and returns the device's decrypted response. Logs in first if there is no valid session, and
     * logs in again once if the device rejects the session.
     *
     * @throws KlapAuthenticationException if no candidate credential is accepted
     * @throws IOException on any other communication failure
     */
    public synchronized String exchange(String payload) throws IOException {
        for (int attempt = 1;; attempt++) {
            KlapCipher activeCipher = cipher;
            if (activeCipher == null || !isLoggedIn()) {
                activeCipher = handshake();
            }
            int seq = activeCipher.nextSequence();
            byte[] body;
            try {
                body = activeCipher.encrypt(payload, seq);
            } catch (GeneralSecurityException e) {
                throw new IOException("Failed to encrypt KLAP request", e);
            }
            ContentResponse response = post(url("request").param("seq", Integer.toString(seq)), body);
            int status = response.getStatus();
            if (status == 200) {
                try {
                    return activeCipher.decrypt(response.getContent(), seq);
                } catch (GeneralSecurityException e) {
                    close();
                    throw new IOException("Failed to decrypt KLAP response", e);
                }
            }
            close();
            if (status != 403 || attempt >= 2) {
                throw new IOException("KLAP request failed with HTTP status " + status);
            }
            logger.trace("KLAP session rejected by {}, logging in again", host);
        }
    }

    /**
     * Establishes a new session now instead of on the next {@link #exchange(String)}.
     *
     * @throws KlapAuthenticationException if no candidate credential is accepted
     * @throws IOException on any other communication failure
     */
    public synchronized void login() throws IOException {
        handshake();
    }

    /**
     * @return whether there is a session that isn't about to expire
     */
    public synchronized boolean isLoggedIn() {
        return cipher != null && System.currentTimeMillis() < expiresAt - RENEW_MARGIN_MS;
    }

    /**
     * Drops the current session. The next {@link #exchange(String)} logs in again.
     */
    public synchronized void close() {
        cipher = null;
        cookie = "";
        expiresAt = 0;
    }

    private KlapCipher handshake() throws IOException {
        close();
        List<TPLinkCredentials> candidates = credentials.get().stream().filter(TPLinkCredentials::areSet).toList();
        if (candidates.isEmpty()) {
            throw new KlapAuthenticationException("No TP-Link account credentials available");
        }
        try {
            byte[] localSeed = new byte[SEED_LENGTH];
            random.nextBytes(localSeed);

            ContentResponse handshake1 = post(url("handshake1"), localSeed);
            byte[] content = handshake1.getContent();
            if (handshake1.getStatus() != 200 || content.length != SEED_LENGTH + 32) {
                throw new IOException("Unexpected KLAP handshake1 response, HTTP status " + handshake1.getStatus());
            }
            String sessionCookie = "";
            long timeoutSeconds = DEFAULT_SESSION_TIMEOUT_S;
            String setCookie = handshake1.getHeaders().get(HttpHeader.SET_COOKIE);
            if (setCookie != null) {
                for (String part : setCookie.split(";")) {
                    String[] pair = part.trim().split("=", 2);
                    if (pair.length == 2 && SESSION_COOKIE.equals(pair[0])) {
                        sessionCookie = SESSION_COOKIE + "=" + pair[1];
                    } else if (pair.length == 2 && SESSION_TIMEOUT.equalsIgnoreCase(pair[0])) {
                        try {
                            timeoutSeconds = Long.parseLong(pair[1]);
                        } catch (NumberFormatException e) {
                            logger.debug("Ignoring invalid KLAP session timeout '{}'", pair[1]);
                        }
                    }
                }
            }
            byte[] remoteSeed = Arrays.copyOfRange(content, 0, SEED_LENGTH);
            byte[] serverHash = Arrays.copyOfRange(content, SEED_LENGTH, content.length);

            byte @Nullable [] authHash = null;
            for (TPLinkCredentials candidate : candidates) {
                byte[] hash = authHash(candidate);
                if (MessageDigest.isEqual(KlapCipher.sha256(localSeed, remoteSeed, hash), serverHash)) {
                    authHash = hash;
                    break;
                }
            }
            if (authHash == null) {
                throw new KlapAuthenticationException("The device rejected the TP-Link account credentials");
            }

            cookie = sessionCookie;
            ContentResponse handshake2 = post(url("handshake2"), KlapCipher.sha256(remoteSeed, localSeed, authHash));
            if (handshake2.getStatus() != 200) {
                cookie = "";
                throw new KlapAuthenticationException(
                        "The device rejected KLAP handshake2, HTTP status " + handshake2.getStatus());
            }
            KlapCipher newCipher = new KlapCipher(localSeed, remoteSeed, authHash);
            cipher = newCipher;
            expiresAt = System.currentTimeMillis() + timeoutSeconds * 1000;
            logger.trace("KLAP session established with {}", host);
            return newCipher;
        } catch (GeneralSecurityException e) {
            close();
            throw new IOException("KLAP handshake failed", e);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private static byte[] authHash(TPLinkCredentials credentials) throws GeneralSecurityException {
        return KlapCipher.sha256(KlapCipher.sha1(credentials.username().getBytes(StandardCharsets.UTF_8)),
                KlapCipher.sha1(credentials.password().getBytes(StandardCharsets.UTF_8)));
    }

    private Request url(String command) throws IOException {
        String deviceHost = host;
        if (deviceHost == null || deviceHost.isBlank()) {
            throw new IOException("No device address configured");
        }
        return httpClient.newRequest("http://" + deviceHost + "/app/" + command);
    }

    private ContentResponse post(Request request, byte[] body) throws IOException {
        String sessionCookie = cookie;
        if (!sessionCookie.isEmpty()) {
            request.headers(headers -> headers.put(HttpHeader.COOKIE, sessionCookie));
        }
        try {
            return request.method(HttpMethod.POST).body(new BytesRequestContent(body))
                    .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while waiting for the device");
        } catch (TimeoutException e) {
            throw new IOException("Timeout waiting for the device", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new IOException(cause != null ? cause.getMessage() : e.getMessage(), e);
        }
    }
}
