/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
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
     * @throws IOException when the response does not contain the expected authentication header
     */
    private static Map<String, String> getAuthParams(HttpClient httpClient, URI loginUri, int timeout)
            throws IOException {
        LOGGER.debug("Sending login request to get authentication challenge");
        CountDownLatch latch = new CountDownLatch(1);
        Request initialRequest = httpClient.newRequest(loginUri).timeout(timeout, TimeUnit.MILLISECONDS);
        XWwwAuthenticateHeaderListener XWwwAuthenticateHeaderListener = new XWwwAuthenticateHeaderListener(latch);
        initialRequest.onResponseHeaders(XWwwAuthenticateHeaderListener);
        initialRequest.send(result -> latch.countDown());
        // Wait for the request to complete
        try {
            latch.await();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }

        String authHeader = XWwwAuthenticateHeaderListener.getAuthHeader();
        if (authHeader == null) {
            throw new IOException("No authentication header found in login response");
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
     * @throws FroniusCommunicationException if an authentication parameter is missing
     */
    private static String createDigestHeader(@Nullable String nonce, @Nullable String realm, @Nullable String qop,
            String uri, HttpMethod method, String username, String password, int nc, String cnonce)
            throws FroniusCommunicationException {
        if (nonce == null || realm == null || qop == null) {
            throw new FroniusCommunicationException("Missing authentication parameter");
        }
        LOGGER.debug("Creating digest authentication header");
        String ha1 = md5Hex(username + ":" + realm + ":" + password);
        String ha2 = md5Hex(method.asString() + ":" + uri);
        String response = md5Hex(
                ha1 + ":" + nonce + ":" + String.format("%08x", nc) + ":" + cnonce + ":" + qop + ":" + ha2);

        return String.format(DIGEST_AUTH_HEADER_FORMAT, username, realm, nonce, uri, response, qop, nc, cnonce);
    }

    /**
     * Computes the MD5 has of the given data and returns it as a hex string.
     *
     * @param data the data to hash
     * @return the hashed data as a hex string
     */
    private static String md5Hex(String data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // should never occur
            throw new RuntimeException(e);
        }
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
     * @throws InterruptedException when the request is interrupted
     * @throws FroniusCommunicationException when the login request failed
     */
    private static void performLoginRequest(HttpClient httpClient, URI loginUri, String authHeader, int timeout)
            throws InterruptedException, FroniusCommunicationException {
        Request loginRequest = httpClient.newRequest(loginUri).header(HttpHeader.AUTHORIZATION, authHeader)
                .timeout(timeout, TimeUnit.MILLISECONDS);
        ContentResponse loginResponse;
        try {
            loginResponse = loginRequest.send();
            if (loginResponse.getStatus() != 200) {
                throw new FroniusCommunicationException(
                        "Failed to send login request, status code: " + loginResponse.getStatus());
            }
        } catch (TimeoutException | ExecutionException e) {
            throw new FroniusCommunicationException("Failed to send login request", e);
        }
    }

    /**
     * Logs in to the Fronius inverter settings, retries on failure and returns the authentication header for the next
     * request.
     *
     * @param httpClient the {@link HttpClient} to use for the request
     * @param baseUri the base URI of the Fronius inverter
     * @param username the username to use for the login
     * @param password the password to use for the login
     * @param method the {@link HttpMethod} to be used by the next request
     * @param relativeUrl the relative URL to be accessed with the next request
     * @param timeout the timeout in milliseconds for the login requests
     * @return the authentication header for the next request
     * @throws FroniusCommunicationException when the login failed or interrupted
     */
    public static synchronized String login(HttpClient httpClient, URI baseUri, String username, String password,
            HttpMethod method, String relativeUrl, int timeout) throws FroniusCommunicationException {
        // Perform request to get authentication parameters
        LOGGER.debug("Getting authentication parameters");
        URI loginUri = baseUri.resolve(URI.create(LOGIN_ENDPOINT + "?user=" + username));
        String relativeLoginUrl = LOGIN_ENDPOINT + "?user=" + username;
        Map<String, String> authDetails;

        int attemptCount = 1;
        try {
            while (true) {
                Throwable lastException;
                try {
                    authDetails = getAuthParams(httpClient, loginUri, timeout);
                    break;
                } catch (IOException e) {
                    LOGGER.debug("HTTP error on attempt #{} {}", attemptCount, loginUri);
                    Thread.sleep(500 * attemptCount);
                    attemptCount++;
                    lastException = e;
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

        // Create auth header for login request
        int nc = 1;
        String cnonce = md5Hex(String.valueOf(System.currentTimeMillis()));
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
                } catch (InterruptedException ie) {
                    throw new FroniusCommunicationException("Failed to send login request", ie);
                } catch (FroniusCommunicationException e) {
                    LOGGER.debug("HTTP error on attempt #{} {}", attemptCount, loginUri);
                    Thread.sleep(500 * attemptCount);
                    attemptCount++;
                    lastException = e;
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
}
