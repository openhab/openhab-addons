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
package org.openhab.binding.viessmann.internal.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PkceUtil} class provides utility methods for generating PKCE code verifier and code challenge for OAuth2.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class PkceUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Generates a random PKCE code verifier (Base64URL, 43–128 chars). */
    public static String generateCodeVerifier() {
        byte[] code = new byte[32];
        RANDOM.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    /** Generates a PKCE code challenge (Base64URL SHA-256) from the given verifier. */
    public static String generateCodeChallenge(String verifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(verifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
