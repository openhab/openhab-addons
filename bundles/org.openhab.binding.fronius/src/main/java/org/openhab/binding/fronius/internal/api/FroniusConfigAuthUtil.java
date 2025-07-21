/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.api;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusConfigAuthUtil} handles the authentication process to access Fronius inverter settings, which are
 * available on the <code>/config</code> HTTP endpoints.
 * <br>
 * Due to Fronius not using the standard HTTP authorization header, it is not possible to use
 * {@link org.eclipse.jetty.client.api.AuthenticationStore} together with
 * {@link org.eclipse.jetty.client.util.DigestAuthentication} to authenticate against the Fronius inverter settings.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class FroniusConfigAuthUtil {
    private static final String AUTHENTICATE_HEADER = "X-Www-Authenticate";
    private static final String DIGEST_AUTH_HEADER_FORMAT = "Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", response=\"%s\", qop=%s, nc=%08x, cnonce=\"%s\"";
    private static final String LOGIN_ENDPOINT = "/commands/Login";

    private static final Logger LOGGER = LoggerFactory.getLogger(FroniusConfigAuthUtil.class);

    /**
     * Sends a HTTP GET request to the given login URI and extracts the authentication parameters from the
     * authentication header.
     * This method uses a {@link Response.Listener.Adapter} to intercept the response headers and extract the
     * authentication header, as normal digest authentication using
     * {@link org.eclipse.jetty.client.util.DigestAuthentication} does not work because Fronius uses a custom
     * authentication header.
     *
     * @param httpClient the {@link HttpClient} to use for the request
     * @param loginUri the {@link URI} of the login endpoint
     * @return a {@link Map} containing the authentication parameters of the authentication challenge
     * @throws FroniusCommunicationException when the authentication challenge request failed or the response does not
     *             contain the expected authentication header
     */
    private static Map<String, String> getAuthParams(HttpClient httpClient, URI loginUri, int timeout)
            throws FroniusCommunicationException {
        LOGGER.debug("Sending login request to get authentication challenge");
        CountDownLatch latch = new CountDownLatch(1);
        Request request = httpClient.newRequest(loginUri).timeout(timeout, TimeUnit.MILLISECONDS);
        XWwwAuthenticateHeaderListener xWwwAuthenticateHeaderListener = new XWwwAuthenticateHeaderListener(latch);
        request.onResponseHeaders(xWwwAuthenticateHeaderListener);
        request.send(result -> latch.countDown());
        // Wait for the request to complete
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new FroniusCommunicationException("Failed to sent authentication challenge request", e);
        }

        String authHeader = xWwwAuthenticateHeaderListener.getAuthHeader();
        if (authHeader == null) {
            throw new FroniusCommunicationException("No authentication header found in login response");
        }
        LOGGER.debug("Parsing authentication challenge");

        // Extract parameters from the header
        Map<String, String> params = new HashMap<>();
        String[] parts = authHeader.split(" ", 2)[1].split(",");
        for (String part : parts) {
            part = part.trim();
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].replace("\"", "").trim();
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * Creates a Digest Authentication header for the given parameters.
     *
     * @param nonce
     * @param realm
     * @param qop
     * @param uri
     * @param method
     * @param username
     * @param password
     * @param nc
     * @param cnonce
     * @return the digest authentication header
     * @throws FroniusCommunicationException if an authentication parameter is missing or the digest header could not be
     *             created
     */
    private static String createDigestHeader(@Nullable String nonce, @Nullable String realm, @Nullable String qop,
            String uri, HttpMethod method, String username, String password, int nc, String cnonce)
            throws FroniusCommunicationException {
        if (nonce == null || realm == null || qop == null) {
            throw new FroniusCommunicationException("Failed to create digest header",
                    new IllegalArgumentException("Missing authentication parameters"));
        }
        LOGGER.debug("Creating digest authentication header");
        String ha1;
        String ha2;
        String response;
        try {
            ha1 = md5Hex(username + ":" + realm + ":" + password);
            ha2 = md5Hex(method.asString() + ":" + uri);
            response = md5Hex(
                    ha1 + ":" + nonce + ":" + String.format("%08x", nc) + ":" + cnonce + ":" + qop + ":" + ha2);
        } catch (NoSuchAlgorithmException e) {
            throw new FroniusCommunicationException("Failed to create digest header", e);
        }

        return String.format(DIGEST_AUTH_HEADER_FORMAT, username, realm, nonce, uri, response, qop, nc, cnonce);
    }

    /**
     * Computes the MD5 has of the given data and returns it as a hex string.
     *
     * @param data the data to hash
     * @return the hashed data as a hex string
     * @throws NoSuchAlgorithmException if the MD5 algorithm is not available
     */
    private static String md5Hex(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Performs the login request to the Fronius inverter's settings.
     *
     * @param httpClient the {@link HttpClient} to use for the request
     * @param loginUri the {@link URI} of the login endpoint
     * @param authHeader the authentication header to use for the login request
     * @throws FroniusCommunicationException when the login request failed
     * @throws FroniusUnauthorizedException when the login failed due to invalid credentials
     */
    private static void performLoginRequest(HttpClient httpClient, URI loginUri, String authHeader, int timeout)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        CountDownLatch latch = new CountDownLatch(1);
        Request request = httpClient.newRequest(loginUri).header(HttpHeader.AUTHORIZATION, authHeader).timeout(timeout,
                TimeUnit.MILLISECONDS);
        StatusListener statusListener = new StatusListener(latch);
        request.onResponseBegin(statusListener);
        Integer status;
        try {
            request.send(result -> latch.countDown());
            // Wait for the request to complete
            latch.await();

            status = statusListener.getStatus();
            if (status == null) {
                throw new FroniusCommunicationException("Failed to send login request: No status code received.");
            }
        } catch (IOException | InterruptedException e) {
            throw new FroniusCommunicationException("Failed to send login request", e);
        }

        if (status == 401) {
            throw new FroniusUnauthorizedException(
                    "Failed to send login request, status code: 401 Unauthorized. Please check your credentials.");
        }
        if (status != 200) {
            throw new FroniusCommunicationException("Failed to send login request, status code: " + status);
        }
    }

    /**
     * Logs in to the Fronius inverter settings, retries on failure and returns the authentication header for the next
     * request.
     *
     * @param httpClient the {@link HttpClient} to use for the request
     * @param baseUri the base URI of the Fronius inverter, MUST NOT end with a slash
     * @param username the username to use for the login
     * @param password the password to use for the login
     * @param method the {@link HttpMethod} to be used by the next request
     * @param relativeUrl the relative URL to be accessed with the next request
     * @param timeout the timeout in milliseconds for the login requests
     * @return the authentication header for the next request
     * @throws FroniusCommunicationException when the login failed or interrupted
     * @throws FroniusUnauthorizedException when the login failed due to invalid credentials
     */
    public static synchronized String login(HttpClient httpClient, URI baseUri, String username, String password,
            HttpMethod method, String relativeUrl, int timeout)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        // Perform request to get authentication parameters
        LOGGER.debug("Getting authentication parameters");
        URI loginUri = URI.create(baseUri + LOGIN_ENDPOINT + "?user=" + username);
        String relativeLoginUrl = loginUri.getPath();
        Map<String, String> authDetails;

        int attemptCount = 1;
        while (true) {
            Throwable lastException;
            try {
                authDetails = getAuthParams(httpClient, loginUri, timeout);
                break;
            } catch (IOException e) {
                LOGGER.debug("HTTP error on attempt #{} {}", attemptCount, loginUri);
                try {
                    Thread.sleep(500 * attemptCount);
                } catch (InterruptedException ie) {
                    throw new FroniusCommunicationException("Failed to request authentication challenge", ie);
                }
                attemptCount++;
                lastException = e;
            }

            if (attemptCount >= 3) {
                LOGGER.debug("Failed connecting to {} after {} attempts.", loginUri, attemptCount, lastException);
                throw new FroniusCommunicationException("Unable to connect", lastException);
            }
        }

        // Create auth header for login request
        int nc = 1;
        String cnonce;
        try {
            cnonce = md5Hex(String.valueOf(System.currentTimeMillis()));
        } catch (NoSuchAlgorithmException e) {
            throw new FroniusCommunicationException("Failed to create cnonce", e);
        }
        String authHeader = createDigestHeader(authDetails.get("nonce"), authDetails.get("realm"),
                authDetails.get("qop"), relativeLoginUrl, HttpMethod.GET, username, password, nc, cnonce);

        // Perform login request with Digest Authentication
        LOGGER.debug("Sending login request");
        attemptCount = 1;
        try {
            while (true) {
                Throwable lastException;
                try {
                    performLoginRequest(httpClient, loginUri, authHeader, timeout);
                    break;
                } catch (FroniusCommunicationException e) {
                    LOGGER.debug("HTTP error on attempt #{} {}", attemptCount, loginUri);
                    Thread.sleep(500L * attemptCount);
                    attemptCount++;
                    lastException = e;
                } catch (FroniusUnauthorizedException e) {
                    throw e;
                }

                if (attemptCount >= 3) {
                    LOGGER.debug("Failed connecting to {} after {} attempts.", loginUri, attemptCount, lastException);
                    throw new FroniusCommunicationException("Unable to connect", lastException);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FroniusCommunicationException("Interrupted", e);
        }

        // Create new auth header for next request
        LOGGER.debug("Login successful, creating auth header for next request");
        nc++;
        authHeader = createDigestHeader(authDetails.get("nonce"), authDetails.get("realm"), authDetails.get("qop"),
                relativeUrl, method, username, password, nc, cnonce);

        return authHeader;
    }

    /**
     * Listener to extract the X-Www-Authenticate header from the response of a {@link Request}.
     * Required to mitigate {@link org.eclipse.jetty.client.HttpResponseException}: HTTP protocol violation:
     * Authentication challenge without WWW-Authenticate header being thrown due to Fronius non-standard authentication
     * header.
     */
    private static class XWwwAuthenticateHeaderListener extends Response.Listener.Adapter {
        private final CountDownLatch latch;
        private @Nullable String authHeader;

        public XWwwAuthenticateHeaderListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onHeaders(Response response) {
            authHeader = response.getHeaders().get(AUTHENTICATE_HEADER);
            latch.countDown();
        }

        public @Nullable String getAuthHeader() {
            return authHeader;
        }
    }

    /**
     * Listener to extract the HTTP status code from the response of a {@link Request} on response begin.
     * Required to mitigate {@link org.eclipse.jetty.client.HttpResponseException}: HTTP protocol violation:
     * Authentication challenge without WWW-Authenticate header being thrown due to Fronius non-standard authentication
     * header.
     */
    private static class StatusListener extends Response.Listener.Adapter {
        private final CountDownLatch latch;
        private @Nullable Integer status;

        public StatusListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onBegin(Response response) {
            this.status = response.getStatus();
            latch.countDown();
            super.onBegin(response);
        }

        public @Nullable Integer getStatus() {
            return status;
        }
    }
}
