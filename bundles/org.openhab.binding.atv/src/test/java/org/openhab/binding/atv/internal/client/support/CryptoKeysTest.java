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
 * Tests for {@link CryptoKeys} against the RFC 7748 and RFC 8032 test vectors.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class CryptoKeysTest {

    private static final HexFormat HEX = HexFormat.of();

    // RFC 7748 section 6.1
    private static final byte[] ALICE_PRIVATE = HEX
            .parseHex("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a");
    private static final byte[] ALICE_PUBLIC = HEX
            .parseHex("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a");
    private static final byte[] BOB_PRIVATE = HEX
            .parseHex("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb");
    private static final byte[] BOB_PUBLIC = HEX
            .parseHex("de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f");
    private static final byte[] SHARED = HEX
            .parseHex("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742");

    @Test
    void x25519PublicKeys() {
        assertArrayEquals(ALICE_PUBLIC, CryptoKeys.x25519PublicKey(ALICE_PRIVATE));
        assertArrayEquals(BOB_PUBLIC, CryptoKeys.x25519PublicKey(BOB_PRIVATE));
    }

    @Test
    void x25519SharedSecret() {
        assertArrayEquals(SHARED, CryptoKeys.x25519SharedSecret(ALICE_PRIVATE, BOB_PUBLIC));
        assertArrayEquals(SHARED, CryptoKeys.x25519SharedSecret(BOB_PRIVATE, ALICE_PUBLIC));
    }

    @Test
    void x25519GeneratedPairsAgree() {
        CryptoKeys.X25519KeyPair a = CryptoKeys.x25519Generate();
        CryptoKeys.X25519KeyPair b = CryptoKeys.x25519Generate();
        assertArrayEquals(CryptoKeys.x25519SharedSecret(a.privateKey(), b.publicKey()),
                CryptoKeys.x25519SharedSecret(b.privateKey(), a.publicKey()));
    }

    @Test
    void x25519RejectsLowOrderPoint() {
        assertThrows(IllegalStateException.class, () -> CryptoKeys.x25519SharedSecret(ALICE_PRIVATE, new byte[32]));
    }

    /** RFC 8032 section 7.1, tests 1 and 2. */
    @Test
    void ed25519Rfc8032Vectors() {
        assertEd25519("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60",
                "d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a", "",
                "e5564300c360ac729086e2cc806e828a84877f1eb8e5d974d873e065224901555fb8821590a33bacc61e39701cf9b46bd25bf5f0595bbe24655141438e7a100b");
        assertEd25519("4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb",
                "3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c", "72",
                "92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00");
    }

    @Test
    void ed25519RejectsTamperedOrMalformedInput() {
        CryptoKeys.Ed25519KeyPair pair = CryptoKeys.ed25519Generate();
        byte[] message = { 1, 2, 3 };
        byte[] signature = CryptoKeys.ed25519Sign(pair.seed(), message);
        assertTrue(CryptoKeys.ed25519Verify(pair.publicKey(), message, signature));

        byte[] tampered = signature.clone();
        tampered[10] ^= 1;
        assertFalse(CryptoKeys.ed25519Verify(pair.publicKey(), message, tampered));
        assertFalse(CryptoKeys.ed25519Verify(pair.publicKey(), new byte[] { 1, 2, 4 }, signature));
        assertFalse(CryptoKeys.ed25519Verify(new byte[31], message, signature));
        assertFalse(CryptoKeys.ed25519Verify(pair.publicKey(), message, new byte[63]));
    }

    @Test
    void ed25519FromSeedMatchesGenerate() {
        CryptoKeys.Ed25519KeyPair pair = CryptoKeys.ed25519Generate();
        assertArrayEquals(pair.publicKey(), CryptoKeys.ed25519FromSeed(pair.seed()).publicKey());
    }

    private static void assertEd25519(String seedHex, String publicHex, String messageHex, String signatureHex) {
        byte[] seed = HEX.parseHex(seedHex);
        byte[] publicKey = HEX.parseHex(publicHex);
        byte[] message = HEX.parseHex(messageHex);
        byte[] signature = HEX.parseHex(signatureHex);
        assertArrayEquals(publicKey, CryptoKeys.ed25519PublicKey(seed));
        assertArrayEquals(signature, CryptoKeys.ed25519Sign(seed, message));
        assertTrue(CryptoKeys.ed25519Verify(publicKey, message, signature));
    }
}
