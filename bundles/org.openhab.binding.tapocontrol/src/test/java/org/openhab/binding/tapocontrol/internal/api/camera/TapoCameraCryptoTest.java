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
package org.openhab.binding.tapocontrol.internal.api.camera;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests with deterministic vectors for {@link TapoCameraCrypto}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class TapoCameraCryptoTest {
    private static final String CNONCE = "9B5F6CE8";
    private static final String NONCE = "18D1CD86D1AE9934";

    @Test
    void md5HashIsUppercaseAndZeroPadded() {
        assertEquals("5F4DCC3B5AA765D61D8327DEB882CF99", TapoCameraCrypto.md5HashUpper("password"));
        // hash starting with a zero nibble must still be 32 chars long
        assertEquals(32, TapoCameraCrypto.md5HashUpper("a").length());
        assertEquals('0', TapoCameraCrypto.md5HashUpper("a").charAt(0));
    }

    @Test
    void sha256HashIsUppercase() {
        assertEquals("5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8",
                TapoCameraCrypto.sha256HexUpper("password"));
    }

    @Test
    void legacyAndSecurePasswordHashesMatchReference() {
        assertEquals(TapoCameraCrypto.md5HashUpper("password"), TapoCameraCrypto.legacyPasswordHash("password"));
        assertEquals(TapoCameraCrypto.sha256HexUpper("password"), TapoCameraCrypto.securePasswordHash("password"));
    }

    @Test
    void deviceConfirmMatchesVector() {
        String pwHash = TapoCameraCrypto.sha256HexUpper("password");
        assertEquals("7283894EE91D1A459F4D21D2B080267E5ADA7C30F77DD184B7CEBBA5B42998CA18D1CD86D1AE99349B5F6CE8",
                TapoCameraCrypto.computeDeviceConfirm(pwHash, CNONCE, NONCE));
        assertTrue(TapoCameraCrypto.validateDeviceConfirm(pwHash, CNONCE, NONCE,
                "7283894EE91D1A459F4D21D2B080267E5ADA7C30F77DD184B7CEBBA5B42998CA18D1CD86D1AE99349B5F6CE8"));
        assertFalse(TapoCameraCrypto.validateDeviceConfirm(pwHash, CNONCE, NONCE, "invalid"));
    }

    @Test
    void digestPasswdMatchesVector() {
        String pwHash = TapoCameraCrypto.sha256HexUpper("password");
        assertEquals("CFF6EE71638403578E0AA64C55E072D65578BA89C326E8ADA37588FCA9845E7A9B5F6CE818D1CD86D1AE9934",
                TapoCameraCrypto.computeDigestPasswd(pwHash, CNONCE, NONCE));
    }

    @Test
    void derivedKeysMatchVectors() {
        String pwHash = TapoCameraCrypto.sha256HexUpper("password");
        assertEquals("C6233C9BA898EAD521DC9B35BC9FE3D6", TapoCameraCrypto.deriveLsk(pwHash, CNONCE, NONCE));
        assertEquals("B5DBC667994F5931513184F052EDCFD7", TapoCameraCrypto.deriveIvb(pwHash, CNONCE, NONCE));
        assertEquals("C6233C9BA898EAD521DC9B35BC9FE3D6", TapoCameraCrypto.deriveKeyToken("lsk", pwHash, CNONCE, NONCE));
    }

    @Test
    void tapoTagMatchesVector() {
        String pwHash = TapoCameraCrypto.sha256HexUpper("password");
        String data = "{\"method\":\"get\",\"led\":{\"name\":[\"config\"]}}";
        assertEquals("F93931BF3EF923EB806A47DFC00A2678E3A9D32E20F4F99EC92D46A04C0B81E4",
                TapoCameraCrypto.computeTapoTag(pwHash, CNONCE, data, 100));
    }

    @Test
    void aesRoundTripMatchesVector() throws Exception {
        String lsk = "C6233C9BA898EAD521DC9B35BC9FE3D6";
        String ivb = "B5DBC667994F5931513184F052EDCFD7";
        String data = "{\"method\":\"get\",\"led\":{\"name\":[\"config\"]}}";
        String encrypted = TapoCameraCrypto.encryptBase64(data, lsk, ivb);
        assertEquals("saH/JC45HiPfongpp7qECSkCjtnx0RZLxyGEAVoA3g3FIPLpkR16RAGex9hUYcFU", encrypted);
        assertEquals(data, TapoCameraCrypto.decryptBase64(encrypted, lsk, ivb));
    }
}
