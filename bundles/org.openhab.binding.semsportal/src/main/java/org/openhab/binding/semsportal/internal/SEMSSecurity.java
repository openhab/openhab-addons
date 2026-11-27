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
package org.openhab.binding.semsportal.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper class for the security that the SEMS portal requires: the hashed password at login, and the signature of
 * every request. Both are part of the protocol of the portal.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
@NonNullByDefault
public class SEMSSecurity {

    private SEMSSecurity() {
        // hide constructor, no initialisation possible
    }

    /**
     * @param password the password of the account
     * @return the password as the portal wants it: the Base64 of the hexadecimal MD5 hash
     */
    public static String hashPassword(String password) {
        String hash = HexFormat.of().formatHex(digest("MD5", password));
        return Base64.getEncoder().encodeToString(hash.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param uid the uid of the session token
     * @param token the token of the session token
     * @param timestamp the time of the request, in milliseconds since the epoch
     * @return the value of the signature header of a request: the Base64 of the hexadecimal SHA-256 hash of the
     *         timestamp, uid and token, followed by the timestamp
     */
    public static String signature(String uid, String token, long timestamp) {
        String hash = HexFormat.of().formatHex(digest("SHA-256", timestamp + "@" + uid + "@" + token));
        return Base64.getEncoder().encodeToString((hash + "@" + timestamp).getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] digest(String algorithm, String value) {
        try {
            return MessageDigest.getInstance(algorithm).digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // MD5 and SHA-256 are available in every Java runtime
            throw new IllegalStateException(e);
        }
    }
}
