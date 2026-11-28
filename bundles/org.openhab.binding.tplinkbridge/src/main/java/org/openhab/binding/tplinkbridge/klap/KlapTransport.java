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
import java.net.URI;
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
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpCookieStore;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tplinkbridge.TPLinkCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side of TP-Link's KLAP protocol, which carries an opaque JSON payload over HTTP to a device. It is payload
 * neutral: Kasa devices exchange IOT JSON over it and Tapo devices SMART JSON.
 * <p>
 * The session is established on first use and renewed shortly before it expires or when the device rejects it. All
 * candidate credentials are checked against a single handshake1 response in both KLAP versions, so alternatives cost
 * no extra round trip. Version 1 (MD5 auth hash) is used by older Kasa firmware, version 2 (SHA auth hash) by Tapo
 * and newer Kasa firmware. The credentials are fetched again at every exchange and login, so changes on the account
 * bridge apply on the next login.
 *
 * @author Christopher Smith - Initial contribution, derived from the tapocontrol KLAP session by Christian Wild
 */
@NonNullByDefault
public class KlapTransport {

    private static final int SEED_LENGTH = 16;
    private static final long TIMEOUT_MS = 5_000;
    /** Renew a session this long, or half its lifetime if that is shorter, before the device would expire it. */
    private static final long RENEW_MARGIN_MS = 40_000;
    private static final long DEFAULT_SESSION_TIMEOUT_S = 86_400;
    private static final String SESSION_COOKIE = "TP_SESSIONID";
    private static final String SESSION_TIMEOUT = "TIMEOUT";

    private final Logger logger = LoggerFactory.getLogger(KlapTransport.class);
    private final SecureRandom random = new SecureRandom();
    private final HttpClient httpClient;
    private final Supplier<List<TPLinkCredentials>> credentials;

    private @Nullable String host;
    // Volatile so that close() can drop the session without waiting for an exchange in progress
    private volatile @Nullable KlapCipher cipher;
    private volatile String cookie = "";
    private volatile long renewAt;

    /**
     * @param httpClient started HTTP client
     * @param host device address, optionally with a port
     * @param credentials candidate credentials in order of preference, fetched outside the transport's lock at every
     *            exchange and login. They are tried as given, including blank ones, and an empty list fails the login
     *            without contacting the device.
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
    public String exchange(String payload) throws IOException {
        List<TPLinkCredentials> candidates = credentials.get();
        synchronized (this) {
            return exchange(payload, candidates);
        }
    }

    private String exchange(String payload, List<TPLinkCredentials> candidates) throws IOException {
        for (int attempt = 1;; attempt++) {
            KlapCipher activeCipher = cipher;
            if (activeCipher == null || !isLoggedIn()) {
                activeCipher = handshake(candidates);
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
    public void login() throws IOException {
        List<TPLinkCredentials> candidates = credentials.get();
        synchronized (this) {
            handshake(candidates);
        }
    }

    /**
     * @return whether there is a session that isn't about to expire
     */
    public boolean isLoggedIn() {
        return cipher != null && System.currentTimeMillis() < renewAt;
    }

    /**
     * Drops the current session. The next {@link #exchange(String)} logs in again. It doesn't wait for an exchange in
     * progress, so it can be called while a Thing is disposed.
     */
    public void close() {
        cipher = null;
        cookie = "";
        renewAt = 0;
    }

    private KlapCipher handshake(List<TPLinkCredentials> candidates) throws IOException {
        close();
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

            Match match = findMatch(candidates, localSeed, remoteSeed, serverHash);
            if (match == null) {
                throw new KlapAuthenticationException("The device rejected the TP-Link account credentials");
            }
            Version version = match.version();
            byte[] authHash = match.authHash();

            cookie = sessionCookie;
            ContentResponse handshake2 = post(url("handshake2"), version.clientHash(localSeed, remoteSeed, authHash));
            if (handshake2.getStatus() != 200) {
                // handshake1 already proved the credentials, so this is a device or communication problem
                throw new IOException("Unexpected KLAP handshake2 response, HTTP status " + handshake2.getStatus());
            }
            KlapCipher newCipher = new KlapCipher(localSeed, remoteSeed, authHash);
            cipher = newCipher;
            long timeoutMs = timeoutSeconds * 1000;
            renewAt = System.currentTimeMillis() + timeoutMs - Math.min(RENEW_MARGIN_MS, timeoutMs / 2);
            logger.trace("KLAP {} session established with {}", version, host);
            return newCipher;
        } catch (GeneralSecurityException e) {
            close();
            throw new IOException("KLAP handshake failed", e);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private static @Nullable Match findMatch(List<TPLinkCredentials> candidates, byte[] localSeed, byte[] remoteSeed,
            byte[] serverHash) throws GeneralSecurityException {
        for (TPLinkCredentials candidate : candidates) {
            for (Version version : Version.values()) {
                byte[] authHash = version.authHash(candidate);
                if (MessageDigest.isEqual(version.serverHash(localSeed, remoteSeed, authHash), serverHash)) {
                    return new Match(version, authHash);
                }
            }
        }
        return null;
    }

    private record Match(Version version, byte[] authHash) {
    }

    /**
     * The two KLAP versions differ only in the auth hash and the handshake hashes. The session cipher is the same.
     */
    private enum Version {
        V1 {
            @Override
            byte[] authHash(TPLinkCredentials credentials) throws GeneralSecurityException {
                return KlapCipher.md5(KlapCipher.md5(bytes(credentials.username())),
                        KlapCipher.md5(bytes(credentials.password())));
            }

            @Override
            byte[] serverHash(byte[] localSeed, byte[] remoteSeed, byte[] authHash) throws GeneralSecurityException {
                return KlapCipher.sha256(localSeed, authHash);
            }

            @Override
            byte[] clientHash(byte[] localSeed, byte[] remoteSeed, byte[] authHash) throws GeneralSecurityException {
                return KlapCipher.sha256(remoteSeed, authHash);
            }
        },
        V2 {
            @Override
            byte[] authHash(TPLinkCredentials credentials) throws GeneralSecurityException {
                return KlapCipher.sha256(KlapCipher.sha1(bytes(credentials.username())),
                        KlapCipher.sha1(bytes(credentials.password())));
            }

            @Override
            byte[] serverHash(byte[] localSeed, byte[] remoteSeed, byte[] authHash) throws GeneralSecurityException {
                return KlapCipher.sha256(localSeed, remoteSeed, authHash);
            }

            @Override
            byte[] clientHash(byte[] localSeed, byte[] remoteSeed, byte[] authHash) throws GeneralSecurityException {
                return KlapCipher.sha256(remoteSeed, localSeed, authHash);
            }
        };

        abstract byte[] authHash(TPLinkCredentials credentials) throws GeneralSecurityException;

        /** The hash the device sends in its handshake1 response. */
        abstract byte[] serverHash(byte[] localSeed, byte[] remoteSeed, byte[] authHash)
                throws GeneralSecurityException;

        /** The hash the client sends in handshake2. */
        abstract byte[] clientHash(byte[] localSeed, byte[] remoteSeed, byte[] authHash)
                throws GeneralSecurityException;

        private static byte[] bytes(String value) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
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
            ContentResponse response = request.method(HttpMethod.POST).body(new BytesRequestContent(body))
                    .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            forgetStoredSessionCookies(request.getURI());
            return response;
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

    /**
     * The HTTP client also stores the session cookie from handshake1 and would send it along with the one this
     * transport sets, and even with the next handshake1 after the session was dropped.
     */
    private void forgetStoredSessionCookies(URI uri) {
        HttpCookieStore store = httpClient.getHttpCookieStore();
        for (HttpCookie stored : store.match(uri)) {
            if (SESSION_COOKIE.equals(stored.getName()) || SESSION_TIMEOUT.equalsIgnoreCase(stored.getName())) {
                store.remove(uri, stored);
            }
        }
    }
}
