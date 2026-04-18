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
package org.openhab.binding.dahuadoor.internal.sip;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper class for SIP Digest MD5 authentication.
 *
 * Based on RFC 2617 - HTTP Authentication: Basic and Digest Access Authentication
 * and RFC 3261 - SIP: Session Initiation Protocol (Section 22.4)
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DigestAuthHelper {

    /**
     * Calculate Digest MD5 response for SIP authentication.
     *
     * @param username SIP username (e.g., "9901#2")
     * @param realm Realm from WWW-Authenticate header (e.g., "VDP")
     * @param password Password
     * @param method SIP method (e.g., "REGISTER", "INVITE")
     * @param uri Request URI (e.g., "sip:172.18.1.111")
     * @param nonce Nonce from WWW-Authenticate header
     * @return MD5 hash response string
     */
    public static String calculateResponse(String username, String realm, String password, String method, String uri,
            String nonce) {
        try {
            // HA1 = MD5(username:realm:password)
            String ha1Input = username + ":" + realm + ":" + password;
            String ha1 = md5(ha1Input);

            // HA2 = MD5(method:uri)
            String ha2Input = method + ":" + uri;
            String ha2 = md5(ha2Input);

            // response = MD5(HA1:nonce:HA2)
            String responseInput = ha1 + ":" + nonce + ":" + ha2;
            return md5(responseInput);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    /**
     * Calculate MD5 hash and return as lowercase hex string.
     */
    private static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Extract value from WWW-Authenticate or Authorization header.
     * Examples:
     * extractValue("Digest realm=\"VDP\", nonce=\"abc\"", "realm") -> "VDP"
     * extractValue("Digest realm=\"VDP\", nonce=\"abc\"", "nonce") -> "abc"
     */
    public static @Nullable String extractValue(@Nullable String headerValue, String key) {
        if (headerValue == null) {
            return null;
        }
        String searchKey = key + "=\"";
        int startIdx = headerValue.indexOf(searchKey);
        if (startIdx == -1) {
            return null;
        }
        startIdx += searchKey.length();
        int endIdx = headerValue.indexOf("\"", startIdx);
        if (endIdx == -1) {
            return null;
        }
        return headerValue.substring(startIdx, endIdx);
    }
}
