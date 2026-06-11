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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.DecryptingInputStream;
import org.openhab.binding.homekit.internal.session.EncryptingOutputStream;

/**
 * Test for {@link EncryptingOutputStream} and {@link DecryptingInputStream} round trip.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestCryptoRoundTrip {

    public static AsymmetricSessionKeys createSessionKeys() {
        byte[] key = fixedKey((byte) 0x11);
        return new AsymmetricSessionKeys(key, key); // same key for read and write
    }

    private static byte[] fixedKey(byte value) {
        byte[] key = new byte[32];
        Arrays.fill(key, value);
        return key;
    }

    @Test
    void testEncryptDecryptRoundTrip() throws Exception {
        AsymmetricSessionKeys sessionKeys = createSessionKeys();
        AtomicInteger writeCounter = new AtomicInteger(0);
        AtomicInteger readCounter = new AtomicInteger(0);

        // original "plain text" (spans multiple frames)
        byte[] originalBytes = new byte[5000];
        for (int i = 0; i < originalBytes.length; i++) {
            originalBytes[i] = (byte) (i % 251);
        }

        // encrypt
        byte[] encryptedBytes;
        try (ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
                EncryptingOutputStream encryptingOutputStream = new EncryptingOutputStream(encryptedOut, writeCounter,
                        sessionKeys)) {
            encryptingOutputStream.write(originalBytes);
            encryptingOutputStream.flush();
            encryptedBytes = encryptedOut.toByteArray();
        }

        // decrypt
        byte[] decryptedBytes;
        try (ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedBytes);
                DecryptingInputStream decryptingInputStream = new DecryptingInputStream(encryptedIn, readCounter,
                        sessionKeys);
                ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = decryptingInputStream.read(buf)) != -1) {
                decryptedOut.write(buf, 0, n);
            }
            decryptedBytes = decryptedOut.toByteArray();
        }

        assertEquals(5000, originalBytes.length);
        assertEquals(5000 + (5 * (16 + 2)), encryptedBytes.length); // 5 frames, each with 16 byte tag and 2 byte length
        assertEquals(5000, decryptedBytes.length);
        assertEquals(5, writeCounter.get());
        assertEquals(5, readCounter.get());
        assertTrue(Arrays.equals(originalBytes, decryptedBytes));
        assertTrue(!Arrays.equals(originalBytes, encryptedBytes));
    }
}
