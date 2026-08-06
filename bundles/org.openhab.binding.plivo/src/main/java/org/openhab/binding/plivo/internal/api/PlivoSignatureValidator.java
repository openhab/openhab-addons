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
 * Validates Plivo webhook request signatures using the X-Plivo-Signature-V3 scheme.
 * <p>
 * Plivo signs each webhook with the {@code X-Plivo-Signature-V3} and
 * {@code X-Plivo-Signature-V3-Nonce} headers. For a POST callback the signed
 * string is the callback URL up to (and including) a literal {@code ?}, followed
 * by the POST body parameters as a key+value concatenation in ascending key
 * order with no separators, followed by {@code .} and the nonce. The signature
 * is {@code Base64(HMAC-SHA256(authToken, signedString))}. The header may carry
 * several comma-separated signatures; the request is valid if any of them match.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Validates a Plivo request signature.
     *
     * @param url the full URL that Plivo requested
     * @param params the POST parameters from the request
     * @param signatureHeader the X-Plivo-Signature-V3 header value (may be comma-separated)
     * @param nonce the X-Plivo-Signature-V3-Nonce header value
     * @param authToken the Plivo Auth Token
     * @return true if the signature is valid
     */
    public static boolean validate(String url, Map<String, String> params, @Nullable String signatureHeader,
            @Nullable String nonce, String authToken) {
        if (signatureHeader == null || signatureHeader.isBlank() || nonce == null || nonce.isBlank()) {
            return false;
        }

        String expected = computeSignature(url, params, nonce, authToken);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        for (String candidate : signatureHeader.split(",")) {
            if (MessageDigest.isEqual(candidate.trim().getBytes(StandardCharsets.UTF_8), expectedBytes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the expected X-Plivo-Signature-V3 for a POST callback.
     *
     * @param url the full URL Plivo requested
     * @param params the POST parameters
     * @param nonce the signature nonce
     * @param authToken the Auth Token
     * @return the Base64-encoded HMAC-SHA256 signature
     */
    public static String computeSignature(String url, Map<String, String> params, String nonce, String authToken) {
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

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hmac = mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 signature", e);
        }
    }

    private static String sortedQueryString(String query) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            if (eq >= 0) {
                sorted.put(pair.substring(0, eq), pair.substring(eq + 1));
            } else {
                sorted.put(pair, "");
            }
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
