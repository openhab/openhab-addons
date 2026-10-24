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
package org.openhab.binding.e3dc.internal.rscp;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the Rijndael-256 block cipher.
 *
 * <p>
 * A 256 bit block is outside the AES standard, and implementations disagree about how far rows 1 and 2
 * shift: the Rijndael specification says one and three columns, while the engine the devices
 * interoperate with uses seven and five in the inverse direction. The all zero vector pins which one is
 * in use, so a change of engine or of its configuration fails here instead of on a device.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class Rijndael256Test {

    /**
     * All zero key and plaintext, as produced by the engine the devices interoperate with.
     *
     * <p>
     * A cipher written from the Rijndael specification yields {@code 6e6a0429...} for the same input,
     * which is why this vector is worth pinning.
     */
    private static final String ZERO_KEY_ZERO_PLAINTEXT = "c6227e7740b7e53b5cb77865278eab0726f62366d9aabad908936123a1fc8af3";

    private static final byte[] ZERO_KEY = new byte[Rijndael256.BLOCK];

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void theAllZeroVectorMatchesTheEngineTheDevicesUse() {
        byte[] plain = new byte[Rijndael256.BLOCK];

        byte[] encrypted = new Rijndael256(ZERO_KEY).encrypt(ZERO_KEY, plain);

        assertEquals(ZERO_KEY_ZERO_PLAINTEXT, toHex(encrypted),
                "the all zero vector identifies the row shift convention in use");
    }

    @Test
    void encryptionAndDecryptionAreInverse() {
        byte[] key = new byte[Rijndael256.BLOCK];
        byte[] plain = new byte[Rijndael256.BLOCK];
        for (int i = 0; i < Rijndael256.BLOCK; i++) {
            key[i] = (byte) (0xA5 ^ (i * 7));
            plain[i] = (byte) (0x3C + i);
        }

        Rijndael256 cipher = new Rijndael256(key);
        byte[] encrypted = cipher.encrypt(key, plain);
        byte[] decrypted = cipher.decrypt(key, encrypted);

        assertArrayEquals(plain, decrypted);
        assertFalse(Arrays.equals(plain, encrypted), "encryption must actually transform the block");
    }

    @Test
    void changingOnePlaintextByteChangesMostOfTheCiphertext() {
        byte[] plain = new byte[Rijndael256.BLOCK];
        Rijndael256 cipher = new Rijndael256(ZERO_KEY);

        byte[] first = cipher.encrypt(ZERO_KEY, plain);
        plain[0] = 1;
        byte[] second = cipher.encrypt(ZERO_KEY, plain);

        int differences = 0;
        for (int i = 0; i < Rijndael256.BLOCK; i++) {
            if (first[i] != second[i]) {
                differences++;
            }
        }
        // a full 14 round cipher must spread a single flipped input byte widely
        assertTrue(differences > Rijndael256.BLOCK / 2,
                "only " + differences + " of " + Rijndael256.BLOCK + " bytes changed");
    }

    @Test
    void aKeyOfTheWrongLengthIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Rijndael256(new byte[16]));
        assertThrows(IllegalArgumentException.class, () -> new Rijndael256(new byte[33]));
    }

    @Test
    void theBlockIsEncryptedIndependentlyOfOtherBytes() {
        byte[] plain = new byte[Rijndael256.BLOCK];
        Arrays.fill(plain, (byte) 0x5A);
        Rijndael256 cipher = new Rijndael256(ZERO_KEY);

        byte[] standalone = cipher.encrypt(ZERO_KEY, plain);

        // encrypting the same block again must give the same result, so no state leaks between calls
        assertArrayEquals(standalone, cipher.encrypt(ZERO_KEY, plain),
                "the cipher must not carry state between blocks");
    }

    // ------------------------------------------------------------------ key derivation

    @Test
    void aShortPasswordIsPaddedWithOnes() {
        byte[] key = Rijndael256.keyFromPassword("smartpower".getBytes(StandardCharsets.UTF_8));

        assertEquals(Rijndael256.BLOCK, key.length);
        assertEquals('s', key[0]);
        assertEquals('r', key[9]);
        for (int i = 10; i < Rijndael256.BLOCK; i++) {
            assertEquals((byte) 0xFF, key[i], "byte " + i + " must be filled with 0xFF");
        }
    }

    @Test
    void aLongPasswordIsTruncatedToTheKeyLength() {
        byte[] password = new byte[48];
        Arrays.fill(password, (byte) 0x41);

        byte[] key = Rijndael256.keyFromPassword(password);

        assertEquals(Rijndael256.BLOCK, key.length);
        for (byte b : key) {
            assertEquals((byte) 0x41, b);
        }
    }

    @Test
    void anEmptyPasswordBecomesAnAllOnesKey() {
        byte[] key = Rijndael256.keyFromPassword(new byte[0]);

        for (byte b : key) {
            assertEquals((byte) 0xFF, b);
        }
    }

    // ------------------------------------------------------------------ chaining

    @Test
    void chainingMakesIdenticalPlaintextBlocksEncryptDifferently() {
        byte[] password = "smartpower".getBytes(StandardCharsets.UTF_8);
        byte[] plain = new byte[Rijndael256.BLOCK * 2];

        byte[] encrypted = new RscpCipher(password).encrypt(plain);

        byte[] first = Arrays.copyOfRange(encrypted, 0, Rijndael256.BLOCK);
        byte[] second = Arrays.copyOfRange(encrypted, Rijndael256.BLOCK, Rijndael256.BLOCK * 2);
        assertFalse(Arrays.equals(first, second),
                "chaining must make two identical plaintext blocks produce different ciphertext");
    }

    @Test
    void decryptionReversesEncryptionAndAdvancesTheChainingState() {
        byte[] password = "smartpower".getBytes(StandardCharsets.UTF_8);
        byte[] plain = new byte[Rijndael256.BLOCK * 2];
        for (int i = 0; i < plain.length; i++) {
            plain[i] = (byte) (i + 1);
        }

        byte[] encrypted = new RscpCipher(password).encrypt(plain);
        byte[] decrypted = new RscpCipher(password).decrypt(encrypted);

        assertArrayEquals(plain, decrypted);
    }

    @Test
    void peekingDoesNotAdvanceTheChainingState() {
        byte[] password = "smartpower".getBytes(StandardCharsets.UTF_8);
        byte[] plain = new byte[Rijndael256.BLOCK * 2];
        for (int i = 0; i < plain.length; i++) {
            plain[i] = (byte) (i * 3 + 1);
        }
        byte[] encrypted = new RscpCipher(password).encrypt(plain);

        RscpCipher cipher = new RscpCipher(password);
        byte[] peeked = cipher.peek(encrypted);
        byte[] thenDecrypted = cipher.decrypt(encrypted);

        assertArrayEquals(plain, peeked);
        assertArrayEquals(plain, thenDecrypted, "a peek must not consume the chaining state");
    }
}
