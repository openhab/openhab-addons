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
package org.openhab.binding.atv.internal.client.support;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Hkdf}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class HkdfTest {

    private static final HexFormat HEX = HexFormat.of();
    private static final byte[] RFC_IKM = HEX.parseHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");

    /** RFC 5869 test case 1 (SHA-256), checking the shared extract-then-expand code. */
    @Test
    void rfc5869Case1() {
        byte[] okm = Hkdf.derive("HmacSHA256", 32, HEX.parseHex("000102030405060708090a0b0c"),
                HEX.parseHex("f0f1f2f3f4f5f6f7f8f9"), RFC_IKM, 42);
        assertArrayEquals(
                HEX.parseHex("3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865"),
                okm);
    }

    /** RFC 5869 test case 3 (SHA-256): empty salt and info. */
    @Test
    void rfc5869Case3EmptySalt() {
        byte[] okm = Hkdf.derive("HmacSHA256", 32, new byte[0], new byte[0], RFC_IKM, 42);
        assertArrayEquals(
                HEX.parseHex("8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d9d201395faa4b61a96c8"),
                okm);
    }

    /** HKDF-SHA512 as used for HAP keys; expected value from an independent implementation. */
    @Test
    void sha512HapDerivation() {
        byte[] ikm = new byte[32];
        for (int i = 0; i < ikm.length; i++) {
            ikm[i] = (byte) i;
        }
        assertArrayEquals(HEX.parseHex("52890146745a52e57b82b859a7a3679c7f3d40bb295b055a0c8fa8af92a3746d"),
                Hkdf.expand("Pair-Setup-Encrypt-Salt", "Pair-Setup-Encrypt-Info", ikm));
    }

    @Test
    void rejectsOversizedOutput() {
        assertThrows(IllegalArgumentException.class,
                () -> Hkdf.expand(new byte[0], new byte[0], new byte[32], 255 * 64 + 1));
    }
}
