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
package org.openhab.binding.plivo.internal.api;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validates Plivo webhook request signatures.
 * <p>
 * Plivo signs each webhook with one or more headers. The V3 family ({@code X-Plivo-Signature-V3} /
 * {@code X-Plivo-Signature-Ma-V3}, nonce {@code X-Plivo-Signature-V3-Nonce}) is used for voice and is
 * also seen on messaging. The V2 family ({@code X-Plivo-Signature-V2} / {@code X-Plivo-Signature-Ma-V2},
 * nonce {@code X-Plivo-Signature-V2-Nonce}) is documented for messaging. A header may itself carry
 * several comma-separated signatures, and {@link #validateV3} / {@link #validateV2} accept the request
 * when any signature in that header matches. {@link #validateCallback} selects the accepted family in a
 * callback-aware way and never downgrades a failed V3 signature to V2. The per-family signed-string
 * construction is documented on {@link #computeV3Signature} and {@link #computeV2Signature}.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Validates a V3-family signature ({@code X-Plivo-Signature-V3} and/or {@code X-Plivo-Signature-Ma-V3}).
     *
     * @param url the full URL that Plivo requested
     * @param params the POST body parameters from the request
     * @param signatureHeader the V3-family header value, which the caller may join with commas
     * @param nonce the {@code X-Plivo-Signature-V3-Nonce} header value
     * @param authToken the Plivo Auth Token
     * @return true if the signature is valid
     */
    public static boolean validateV3(String url, Map<String, String> params, @Nullable String signatureHeader,
            @Nullable String nonce, String authToken) {
        if (signatureHeader == null || signatureHeader.isBlank() || nonce == null || nonce.isBlank()) {
            return false;
        }

        String expected = computeV3Signature(url, params, nonce, authToken);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        for (String candidate : signatureHeader.split(",")) {
            if (MessageDigest.isEqual(candidate.trim().getBytes(StandardCharsets.UTF_8), expectedBytes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the expected V3 signature for a POST callback.
     * <p>
     * The signed string is the request URL with its query string removed, followed by a literal
     * {@code ?}. When the URL had a query string, its parameters are appended sorted by key as
     * {@code key=value} pairs joined by {@code &}, followed by a {@code .} separator. The POST body
     * parameters are then appended sorted by key as a {@code key}+{@code value} concatenation with
     * no separators, and finally a {@code .} and the nonce are appended.
     *
     * @param url the full URL Plivo requested
     * @param params the POST parameters
     * @param nonce the signature nonce
     * @param authToken the Auth Token
     * @return the Base64-encoded HMAC-SHA256 signature
     */
    public static String computeV3Signature(String url, Map<String, String> params, String nonce, String authToken) {
        int queryIndex = url.indexOf('?');
        String base = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        String query = queryIndex >= 0 ? url.substring(queryIndex + 1) : "";

        StringBuilder data = new StringBuilder(base).append('?');
        if (!query.isEmpty()) {
            data.append(sortedQueryString(query)).append('.');
        }
        TreeMap<String, String> sorted = new TreeMap<>(params);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            data.append(entry.getKey()).append(entry.getValue());
        }
        data.append('.').append(nonce);

        return hmacBase64(data.toString(), authToken);
    }

    /**
     * Validates a Plivo V2-family signature ({@code X-Plivo-Signature-V2} and/or
     * {@code X-Plivo-Signature-Ma-V2}). Plivo documents this family for messaging callbacks.
     *
     * @param url the full URL that Plivo requested
     * @param signatureHeader the V2 signature header value, which the caller may join with commas
     * @param nonce the {@code X-Plivo-Signature-V2-Nonce} header value
     * @param authToken the Plivo Auth Token
     * @return true if the signature is valid
     */
    public static boolean validateV2(String url, @Nullable String signatureHeader, @Nullable String nonce,
            String authToken) {
        if (signatureHeader == null || signatureHeader.isBlank() || nonce == null || nonce.isBlank()) {
            return false;
        }
        String expected = computeV2Signature(url, nonce, authToken);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        for (String candidate : signatureHeader.split(",")) {
            if (MessageDigest.isEqual(candidate.trim().getBytes(StandardCharsets.UTF_8), expectedBytes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the expected V2 signature. The signed string is the request URL with its query string
     * removed, followed immediately by the nonce.
     *
     * @param url the full URL Plivo requested
     * @param nonce the signature nonce
     * @param authToken the Auth Token
     * @return the Base64-encoded HMAC-SHA256 signature
     */
    public static String computeV2Signature(String url, String nonce, String authToken) {
        int queryIndex = url.indexOf('?');
        String base = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        return hmacBase64(base + nonce, authToken);
    }

    /**
     * Validates a callback signature in a callback-aware way. A V3-family signature
     * ({@code X-Plivo-Signature-V3} or {@code X-Plivo-Signature-Ma-V3}) is authoritative whenever it is
     * present, so a failed V3 signature is never downgraded to the V2 family. When no V3 signature is
     * sent, messaging callbacks may use the documented V2 family, while other callbacks (voice, gather,
     * answer) require V3.
     *
     * @param messaging whether this is a messaging callback (SMS, WhatsApp, or a message status)
     * @param url the full URL that Plivo requested
     * @param params the POST body parameters
     * @param authToken the Plivo Auth Token
     * @param v3 the {@code X-Plivo-Signature-V3} header value, or null
     * @param maV3 the {@code X-Plivo-Signature-Ma-V3} header value, or null
     * @param v3Nonce the {@code X-Plivo-Signature-V3-Nonce} header value, or null
     * @param v2 the {@code X-Plivo-Signature-V2} header value, or null
     * @param maV2 the {@code X-Plivo-Signature-Ma-V2} header value, or null
     * @param v2Nonce the {@code X-Plivo-Signature-V2-Nonce} header value, or null
     * @return true if the request carries a valid signature for its callback type
     */
    public static boolean validateCallback(boolean messaging, String url, Map<String, String> params, String authToken,
            @Nullable String v3, @Nullable String maV3, @Nullable String v3Nonce, @Nullable String v2,
            @Nullable String maV2, @Nullable String v2Nonce) {
        boolean v3Present = !isBlank(v3) || !isBlank(maV3);
        if (v3Present) {
            String v3Combined = messaging ? join(maV3, v3) : join(v3, maV3);
            return validateV3(url, params, v3Combined, v3Nonce, authToken);
        }
        // No V3 signature was sent. Only messaging callbacks may use the documented V2 family.
        if (!messaging) {
            return false;
        }
        return validateV2(url, join(maV2, v2), v2Nonce, authToken);
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.isBlank();
    }

    private static @Nullable String join(@Nullable String primary, @Nullable String secondary) {
        boolean hasPrimary = !isBlank(primary);
        boolean hasSecondary = !isBlank(secondary);
        if (hasPrimary && hasSecondary) {
            return primary + "," + secondary;
        }
        if (hasPrimary) {
            return primary;
        }
        return hasSecondary ? secondary : null;
    }

    private static String hmacBase64(String data, String authToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 signature", e);
        }
    }

    private static String safeDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // A malformed percent-encoding must not crash validation; sign the raw value instead.
            return s;
        }
    }

    private static String sortedQueryString(String query) {
        // Plivo signs the URL-decoded query params, so decode each key and value and sort by the key.
        TreeMap<String, String> sorted = new TreeMap<>();
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            sorted.put(safeDecode(key), safeDecode(value));
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }
}
