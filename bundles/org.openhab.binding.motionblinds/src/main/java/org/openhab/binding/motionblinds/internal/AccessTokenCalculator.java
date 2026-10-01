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
 * uppercase hex.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public final class AccessTokenCalculator {

    private AccessTokenCalculator() {
    }

    public static String calculate(String key, String token) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("Key must be 16 characters long");
        }
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
