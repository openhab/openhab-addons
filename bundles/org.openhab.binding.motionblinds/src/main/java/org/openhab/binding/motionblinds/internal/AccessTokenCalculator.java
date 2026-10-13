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
package org.openhab.binding.motionblinds.internal;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AccessTokenCalculator} calculates the {@code AccessToken} that authorizes read and write requests:
 * the 16 character token received from the motor, AES-128-ECB encrypted with the 16 character app key, as
 * uppercase hex. The key is used as the 16 byte AES key, so it must consist of 16 ASCII characters.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public final class AccessTokenCalculator {

    private static final int KEY_LENGTH = 16;

    private AccessTokenCalculator() {
    }

    /**
     * Check the key as shown by the app: 16 printable ASCII characters (e.g. {@code 12ab345c-d67e-8f}), which gives
     * the 16 bytes of the AES key.
     */
    public static boolean isValidKey(String key) {
        return key.length() == KEY_LENGTH && key.chars().allMatch(c -> c > 0x20 && c < 0x7f);
    }

    public static String calculate(String key, String token) {
        if (!isValidKey(key)) {
            throw new IllegalArgumentException("Key must consist of 16 ASCII characters");
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        if (tokenBytes.length != 16) {
            throw new IllegalArgumentException("Token must be 16 characters long");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
            return HexFormat.of().withUpperCase().formatHex(cipher.doFinal(tokenBytes));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES is not available", e);
        }
    }
}
