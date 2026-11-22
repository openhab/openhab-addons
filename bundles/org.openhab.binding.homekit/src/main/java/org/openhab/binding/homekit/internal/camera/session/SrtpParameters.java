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
package org.openhab.binding.homekit.internal.camera.session;

import static org.openhab.binding.homekit.internal.camera.tlv.TlvHelpers.*;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;

/**
 * RTP <=> SRTP encryption / decryption parameters for camera streams.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public record SrtpParameters(SrtpCryptoSuite cryptoSuite, byte[] masterKey, byte[] masterSalt) {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int TYPE_CRYPTO_SUITE = 0x01;
    private static final int TYPE_MASTER_KEY = 0x02;
    private static final int TYPE_MASTER_SALT = 0x03;

    public SrtpParameters {
        if (cryptoSuite == SrtpCryptoSuite.NONE) {
            if (masterKey.length != 0 || masterSalt.length != 0) {
                throw new IllegalArgumentException("Key and salt must be empty when crypto suite is NONE");
            }
        } else {
            requireLength(masterKey, cryptoSuite.keyLength);
            requireLength(masterSalt, cryptoSuite.saltLength);
        }
    }

    /**
     * Encodes the parameters into a TLV8 byte array to send in a camera setup request.
     */
    public byte[] encode() {
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TYPE_CRYPTO_SUITE, u8(cryptoSuite.value));
        tlv.put(TYPE_MASTER_KEY, masterKey);
        tlv.put(TYPE_MASTER_SALT, masterSalt);
        return Tlv8Codec.encode(tlv);
    }

    /**
     * Decodes the camera setup response TLV8 data into the respective parameters.
     *
     * @throws IllegalArgumentException if the TLV is invalid or contains an unknown crypto suite
     */
    public static SrtpParameters decode(byte[] data) {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(data);
        SrtpCryptoSuite suite = SrtpCryptoSuite.fromValue(readU8(required(tlv, TYPE_CRYPTO_SUITE)));
        return new SrtpParameters(suite, tlv.getOrDefault(TYPE_MASTER_KEY, new byte[0]),
                tlv.getOrDefault(TYPE_MASTER_SALT, new byte[0]));
    }

    /**
     * Generates random SRTP parameters.
     *
     * @return a new SrtpParameters object with random key and salt
     */
    public static SrtpParameters random() {
        byte[] key = new byte[16];
        byte[] salt = new byte[14];
        RANDOM.nextBytes(key);
        RANDOM.nextBytes(salt);
        return new SrtpParameters(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, key, salt);
    }
}
