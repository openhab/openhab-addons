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
package org.openhab.binding.twilio.internal.api;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validates Twilio webhook request signatures using HMAC-SHA1.
 * <p>
 * Twilio signs each webhook request with the X-Twilio-Signature header.
 * The signature is computed as: Base64(HMAC-SHA1(AuthToken, URL + sorted POST params))
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioSignatureValidator {

    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * Validates a Twilio request signature.
     *
     * @param url the full URL that Twilio requested
     * @param params the POST parameters from the request
     * @param signature the X-Twilio-Signature header value
     * @param authToken the Twilio Auth Token
     * @return true if the signature is valid
     */
    public static boolean validate(String url, Map<String, String> params, @Nullable String signature,
            String authToken) {
        if (signature == null || signature.isBlank()) {
            return false;
        }

        String expectedSignature = computeSignature(url, params, authToken);
        return signature.equals(expectedSignature);
    }

    /**
     * Computes the expected signature for a Twilio webhook request.
     *
     * @param url the full URL
     * @param params the POST parameters
     * @param authToken the Auth Token
     * @return the Base64-encoded HMAC-SHA1 signature
     */
    public static String computeSignature(String url, Map<String, String> params, String authToken) {
        StringBuilder data = new StringBuilder(url);

        // Sort parameters alphabetically by key and concatenate key+value
        TreeMap<String, String> sorted = new TreeMap<>(params);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            data.append(entry.getKey());
            data.append(entry.getValue());
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
            byte[] hmac = mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA1 signature", e);
        }
    }
}
