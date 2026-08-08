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
 * Validates Plivo webhook request signatures using the V3 signing scheme.
 * <p>
 * Plivo signs each webhook with an {@code X-Plivo-Signature-V3-Nonce} header plus one or more
 * signature headers: {@code X-Plivo-Signature-V3} is generated with the (sub)account Auth Token
 * that owns the request, while {@code X-Plivo-Signature-Ma-V3} is always generated with the main
 * account Auth Token. Voice callbacks carry the {@code V3} signature and messaging callbacks
 * (SMS/MMS/WhatsApp/status) carry the {@code Ma-V3} signature, but both are computed identically,
 * so this validator can check a request against any of the signatures supplied.
 * <p>
 * For a POST callback the signed string is built as follows. Take the request URL, drop its query
 * string, and append a literal {@code ?}. When the URL originally had a query string, append its
 * parameters sorted by key in ascending order and rendered as {@code key=value} pairs joined by
 * {@code &}, then a {@code .} separator. Next append the POST body parameters sorted by key in
 * ascending order as a {@code key}+{@code value} concatenation with no separators. Finally append
 * a {@code .} and the nonce. The signature is {@code Base64(HMAC-SHA256(authToken, signedString))}.
 * A signature header may carry several comma-separated signatures; the request is valid if any of
 * them matches.
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
     * @param signatureHeader a V3-family signature header value ({@code X-Plivo-Signature-V3} and/or
     *            {@code X-Plivo-Signature-Ma-V3}); a single value may itself be comma-separated and
     *            several headers may be joined with commas by the caller
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
