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
package org.openhab.binding.lgthinq.lgservices.api;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.i18n.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RestUtils} rest utilities
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class RestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    // ---- TLS fallback configuration ----
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    /**
     * Generic TLS compatibility fallback:
     * - Force TLS 1.2
     * - Allow RSA key-exchange ciphers (no ECDHE-only mode)
     * NOTE: This is a workaround for broken server/edge TLS configs that reject modern ECDHE suites.
     * It keeps certificate validation enabled.
     */
    private static final String[] TLS_FALLBACK_PROTOCOLS = new String[] { "TLSv1.2" };

    private static final String[] TLS_FALLBACK_CIPHERS = new String[] { "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_128_GCM_SHA256" };

    private static final String[] TLS_FALLBACK_EXCLUDE_CIPHERS = new String[] { ".*_DHE_.*", ".*_DSS_.*" };

    /**
     * The fallback client is shared by all bridges and is owned by the binding's handler factory: it may only be
     * created between {@link #enableTlsFallback()} and {@link #disableTlsFallback()}. Both fields are guarded by
     * {@link #TLS_FALLBACK_LOCK}.
     */
    private static final Object TLS_FALLBACK_LOCK = new Object();
    private static @Nullable HttpClient tlsFallbackClient;
    private static boolean tlsFallbackEnabled;

    // ---------------- Existing crypto helpers ----------------

    public static String getPreLoginEncPwd(String pwdToEnc) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("The required algorithm is not available.", e);
            throw new IllegalStateException("Unexpected error. SHA-512 algorithm must exists in JDK distribution", e);
        }
        digest.reset();
        digest.update(pwdToEnc.getBytes(StandardCharsets.UTF_8));

        return String.format("%0128x", new BigInteger(1, digest.digest()));
    }

    public static byte[] getOauth2Sig(String messageSign, String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec signingKey = new SecretKeySpec(secretBytes, HMAC_SHA1_ALGORITHM);

        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return Base64.getEncoder().encode(mac.doFinal(messageSign.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.debug("Unexpected error. SHA1 algorithm must exists in JDK distribution.", e);
            throw new IllegalStateException("Unexpected error. SHA1 algorithm must exists in JDK distribution", e);
        } catch (InvalidKeyException e) {
            LOGGER.debug("Unexpected error.", e);
            throw new IllegalStateException("Unexpected error.", e);
        }
    }

    /**
     * Builds the string-to-sign used by LG OAuth signatures: {@code "<path>?<query>\n<timestamp>"}.
     */
    static String getTokenSignatureMessage(String authUrl, Map<String, String> empData, String timestamp) {
        UriBuilder builder = UriBuilder.fromUri(authUrl);
        empData.forEach(builder::queryParam);

        URI reqUri = builder.build();
        String signUrl = !empData.isEmpty() ? reqUri.getPath() + "?" + reqUri.getRawQuery() : reqUri.getPath();
        return String.format("%s\n%s", signUrl, timestamp);
    }

    public static byte[] getTokenSignature(String authUrl, String secretKey, Map<String, String> empData,
            String timestamp) {
        String messageToSign = getTokenSignatureMessage(authUrl, empData, timestamp);
        return getOauth2Sig(messageToSign, secretKey);
    }

    // ---------------- Public HTTP helpers ----------------

    public static RestResult getCall(HttpClient httpClient, String encodedUrl, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> params) {

        try {
            return doGet(httpClient, encodedUrl, headers, params);
        } catch (ExecutionException e) {
            if (shouldRetryWithTlsFallback(encodedUrl, e)) {
                LOGGER.debug(
                        "TLS handshake failed for GET {}. Retrying once with TLS compatibility profile (TLS1.2 + RSA ciphers).",
                        safeUrlForLog(encodedUrl));
                try {
                    return doGet(getTlsFallbackHttpClient(), encodedUrl, headers, params);
                } catch (Exception e2) {
                    LOGGER.debug("TLS fallback retry also failed for GET {}: {}", safeUrlForLog(encodedUrl),
                            e2.getMessage(), e2);
                    throw new CommunicationException(e2);
                }
            }
            LOGGER.debug("Exception occurred during GET execution: {}", e.getMessage(), e);
            throw new CommunicationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommunicationException(e);
        } catch (TimeoutException e) {
            LOGGER.debug("Timeout during GET execution: {}", e.getMessage(), e);
            throw new CommunicationException(e);
        }
    }

    @Nullable
    public static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            String jsonData) {
        return postCall(httpClient, encodedUrl, headers, new StringContentProvider(jsonData));
    }

    @Nullable
    public static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            Map<String, String> formParams) {
        Fields fields = new Fields();
        formParams.forEach(fields::put);
        return postCall(httpClient, encodedUrl, headers, new FormContentProvider(fields));
    }

    @Nullable
    private static RestResult postCall(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            ContentProvider contentProvider) {

        try {
            return doPost(httpClient, encodedUrl, headers, contentProvider);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout reading post call result from LG API", e);
            // callers retry when no result is returned
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommunicationException(e);
        } catch (ExecutionException e) {
            if (shouldRetryWithTlsFallback(encodedUrl, e)) {
                LOGGER.debug(
                        "TLS handshake failed for POST {}. Retrying once with TLS compatibility profile (TLS1.2 + RSA ciphers).",
                        safeUrlForLog(encodedUrl));
                try {
                    return doPost(getTlsFallbackHttpClient(), encodedUrl, headers, contentProvider);
                } catch (TimeoutException te) {
                    LOGGER.warn("Timeout on TLS fallback retry for POST {}", safeUrlForLog(encodedUrl), te);
                    return null;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new CommunicationException(ie);
                } catch (Exception ex) {
                    throw new CommunicationException(ex);
                }
            }
            throw new CommunicationException(e);
        }
    }

    // ---------------- Internal request execution ----------------

    private static RestResult doGet(HttpClient httpClient, String encodedUrl, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> params) throws InterruptedException, TimeoutException, ExecutionException {
        Request request = httpClient.newRequest(encodedUrl).method("GET").timeout(REQUEST_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);

        if (params != null) {
            params.forEach(request::param);
        }
        if (headers != null) {
            headers.forEach(request::header);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("GET request: {}", request.getURI());
        }

        ContentResponse response = request.send();

        LOGGER.trace("GET response: {}", response.getContentAsString());

        return new RestResult(response.getContentAsString(), response.getStatus());
    }

    private static RestResult doPost(HttpClient httpClient, String encodedUrl, Map<String, String> headers,
            ContentProvider contentProvider) throws InterruptedException, TimeoutException, ExecutionException {
        Request request = httpClient.newRequest(encodedUrl).method("POST").content(contentProvider)
                .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        headers.forEach(request::header);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("POST request to URI: {}", request.getURI());
        }

        ContentResponse response = request.send();

        LOGGER.trace("POST response: {}", response.getContentAsString());

        return new RestResult(response.getContentAsString(), response.getStatus());
    }

    // ---------------- TLS fallback helpers ----------------

    private static boolean shouldRetryWithTlsFallback(String encodedUrl, ExecutionException e) {
        return isHandshakeFailure(e) && isHttpsUrl(encodedUrl);
    }

    private static boolean isHttpsUrl(String url) {
        try {
            return URI.create(url).getScheme() != null && URI.create(url).getScheme().equalsIgnoreCase("https");
        } catch (Exception ex) {
            // If URL is odd, don't attempt special handling
            return false;
        }
    }

    /**
     * Detects an SSLHandshakeException in the causal chain.
     */
    private static boolean isHandshakeFailure(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof javax.net.ssl.SSLHandshakeException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * Avoid logging sensitive paths (e.g., account email inside URL).
     */
    private static String safeUrlForLog(String encodedUrl) {
        try {
            URI u = URI.create(encodedUrl);
            // Only log scheme://host[:port]/path (no query)
            String path = u.getPath();
            // the path can contain the account e-mail
            if (path != null) {
                path = path.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+", "<redacted>");
            }
            String hostPort = u.getPort() > 0 ? (u.getHost() + ":" + u.getPort()) : u.getHost();
            return u.getScheme() + "://" + hostPort + (path == null ? "" : path);
        } catch (Exception e) {
            return "<unparseable-url>";
        }
    }

    private static HttpClient getTlsFallbackHttpClient() throws Exception {
        synchronized (TLS_FALLBACK_LOCK) {
            if (!tlsFallbackEnabled) {
                throw new IllegalStateException("TLS fallback HttpClient is not available while the binding is down");
            }
            @Nullable
            HttpClient existing = tlsFallbackClient;
            if (existing != null) {
                return existing;
            }

            SslContextFactory.Client ssl = new SslContextFactory.Client();

            // Keep certificate validation enabled
            ssl.setEndpointIdentificationAlgorithm("HTTPS");

            // TLS 1.2 only
            ssl.setIncludeProtocols(TLS_FALLBACK_PROTOCOLS);
            ssl.setExcludeProtocols("TLSv1.3");

            // RSA key-exchange suites
            ssl.setIncludeCipherSuites(TLS_FALLBACK_CIPHERS);

            ssl.setExcludeCipherSuites(TLS_FALLBACK_EXCLUDE_CIPHERS);

            final HttpClient created = new HttpClient(ssl);
            created.setConnectTimeout(10_000);
            created.start();

            tlsFallbackClient = created;
            LOGGER.info("Initialized TLS fallback HttpClient (TLSv1.2 + RSA cipher suites).");

            return created;
        }
    }

    /**
     * Allows the TLS fallback client to be created on demand. Called when the handler factory is activated.
     */
    public static void enableTlsFallback() {
        synchronized (TLS_FALLBACK_LOCK) {
            tlsFallbackEnabled = true;
        }
    }

    /**
     * Stops the TLS fallback client and prevents it from being created again until {@link #enableTlsFallback()} is
     * called. Called when the handler factory is deactivated, after all bridges have been disposed.
     */
    public static void disableTlsFallback() {
        HttpClient c;
        synchronized (TLS_FALLBACK_LOCK) {
            tlsFallbackEnabled = false;
            c = tlsFallbackClient;
            tlsFallbackClient = null;
        }
        if (c != null) {
            try {
                c.stop();
            } catch (Exception e) {
                LOGGER.debug("Error stopping TLS fallback HttpClient: {}", e.getMessage(), e);
            }
        }
    }
}
