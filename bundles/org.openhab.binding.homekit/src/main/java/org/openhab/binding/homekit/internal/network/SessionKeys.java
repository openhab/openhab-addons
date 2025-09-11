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
package org.openhab.binding.homekit.internal.network;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Derives session keys for encrypting and decrypting messages between a HomeKit controller and accessory.
 * Uses HKDF with HMAC-SHA512 as the underlying hash function.
 * The derived keys are used for ChaCha20 encryption in the secure session.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class SessionKeys {

    private static final String HMAC_ALGO = "HmacSHA512";

    public final byte[] writeKey; // Controller → Accessory
    public final byte[] readKey; // Accessory → Controller

    public SessionKeys(byte[] sharedSecret) {
        byte[] salt = "Control-Salt".getBytes(StandardCharsets.UTF_8);
        this.writeKey = hkdf(sharedSecret, salt, "Control-Write-Encryption-Key".getBytes(StandardCharsets.UTF_8), 32);
        this.readKey = hkdf(sharedSecret, salt, "Control-Read-Encryption-Key".getBytes(StandardCharsets.UTF_8), 32);
    }

    private byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(salt, HMAC_ALGO));
            byte[] prk = mac.doFinal(ikm);

            mac.init(new SecretKeySpec(prk, HMAC_ALGO));
            mac.update(info);
            mac.update((byte) 0x01);
            byte[] okm = mac.doFinal();

            byte[] result = new byte[length];
            System.arraycopy(okm, 0, result, 0, length);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("HKDF derivation failed", e);
        }
    }
}
