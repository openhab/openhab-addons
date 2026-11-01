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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;

/**
 * Tests for {@link Chacha20Cipher}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class Chacha20CipherTest {

    private static final HexFormat HEX = HexFormat.of();

    private static final byte[] KEY_A = HEX
            .parseHex("808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f");
    private static final byte[] KEY_B = HEX
            .parseHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");

    /** RFC 8439 section 2.8.2. */
    @Test
    void rfc8439AeadVector() {
        byte[] nonce = HEX.parseHex("070000004041424344454647");
        byte[] aad = HEX.parseHex("50515253c0c1c2c3c4c5c6c7");
        byte[] plaintext = ("Ladies and Gentlemen of the class of '99: If I could offer you only one tip for "
                + "the future, sunscreen would be it.").getBytes(StandardCharsets.US_ASCII);
        byte[] expected = HEX.parseHex("d31a8d34648e60db7b86afbc53ef7ec2a4aded51296e08fea9e2b5a736ee62d6"
                + "3dbea45e8ca9671282fafb69da92728b1a71de0a9e060b2905d6a5b67ecd3b3692ddbd7f2d778b8c9803aee3"
                + "28091b58fab324e4fad675945585808b4831d7bc3ff4def08e4b7a9de576d26586cec64b6116"
                + "1ae10b594f09e26a7e902ecbd0600691");

        Chacha20Cipher cipher = new Chacha20Cipher(KEY_A, KEY_A);
        assertArrayEquals(expected, cipher.encrypt(plaintext, nonce, aad));
        assertArrayEquals(plaintext, cipher.decrypt(expected, nonce, aad));
    }

    @Test
    void eightByteCounterNonceLayout() {
        Chacha20Cipher cipher = new Chacha20Cipher(KEY_A, KEY_B);
        assertArrayEquals(new byte[12], cipher.outNonce());
        cipher.encrypt(new byte[1]);
        assertArrayEquals(HEX.parseHex("000000000100000000000000"), cipher.outNonce());
        for (int i = 1; i < 0x102; i++) {
            cipher.encrypt(new byte[1]);
        }
        assertArrayEquals(HEX.parseHex("000000000201000000000000"), cipher.outNonce());
    }

    @Test
    void twelveByteCounterNonceLayout() {
        Chacha20Cipher cipher = new Chacha20Cipher(KEY_A, KEY_B, 12);
        cipher.encrypt(new byte[1]);
        assertArrayEquals(HEX.parseHex("010000000000000000000000"), cipher.outNonce());
    }

    @Test
    void counterNonceMatchesExplicitNonce() {
        Chacha20Cipher counter = new Chacha20Cipher(KEY_A, KEY_B);
        Chacha20Cipher explicit = new Chacha20Cipher(KEY_A, KEY_B);
        byte[] data = "hello".getBytes(StandardCharsets.US_ASCII);
        counter.encrypt(data);
        assertArrayEquals(explicit.encrypt(data, HEX.parseHex("000000000100000000000000"), null),
                counter.encrypt(data));
    }

    @Test
    void shortExplicitNonceIsLeftPaddedAndLeavesCounter() {
        Chacha20Cipher cipher = new Chacha20Cipher(KEY_A, KEY_A);
        byte[] data = "pairing".getBytes(StandardCharsets.US_ASCII);
        byte[] shortNonce = "PS-Msg05".getBytes(StandardCharsets.US_ASCII);
        byte[] padded = new byte[12];
        System.arraycopy(shortNonce, 0, padded, 4, shortNonce.length);

        assertArrayEquals(cipher.encrypt(data, padded, null), cipher.encrypt(data, shortNonce, null));
        assertArrayEquals(new byte[12], cipher.outNonce());
    }

    @Test
    void roundTripBetweenPeers() {
        Chacha20Cipher local = new Chacha20Cipher(KEY_A, KEY_B, 12);
        Chacha20Cipher remote = new Chacha20Cipher(KEY_B, KEY_A, 12);
        for (int i = 0; i < 5; i++) {
            byte[] message = ("message " + i).getBytes(StandardCharsets.US_ASCII);
            byte[] aad = { (byte) i };
            assertArrayEquals(message, remote.decrypt(local.encrypt(message, null, aad), null, aad));
            assertArrayEquals(message, local.decrypt(remote.encrypt(message)));
        }
    }

    @Test
    void failedDecryptDoesNotAdvanceCounter() {
        Chacha20Cipher sender = new Chacha20Cipher(KEY_A, KEY_B);
        Chacha20Cipher receiver = new Chacha20Cipher(KEY_B, KEY_A);
        byte[] first = sender.encrypt("first".getBytes(StandardCharsets.US_ASCII));
        byte[] second = sender.encrypt("second".getBytes(StandardCharsets.US_ASCII));

        byte[] tampered = first.clone();
        tampered[0] ^= 1;
        assertThrows(AuthenticationError.class, () -> receiver.decrypt(tampered));
        assertArrayEquals(new byte[12], receiver.inNonce());

        assertArrayEquals("first".getBytes(StandardCharsets.US_ASCII), receiver.decrypt(first));
        assertArrayEquals("second".getBytes(StandardCharsets.US_ASCII), receiver.decrypt(second));
    }

    @Test
    void truncatedCiphertextFailsAuthentication() {
        Chacha20Cipher cipher = new Chacha20Cipher(KEY_A, KEY_B);
        assertThrows(AuthenticationError.class, () -> cipher.decrypt(new byte[5]));
    }

    /**
     * The openHAB Docker image runs Java with the limited crypto policy, which caps JDK cipher keys at 128
     * bits. The policy is read once per JVM, so this runs in a fresh one.
     */
    @Test
    void worksUnderLimitedCryptoPolicy(@TempDir Path dir) throws Exception {
        String java = Path.of(Objects.requireNonNull(System.getProperty("java.home")), "bin", "java").toString();
        // Output goes to a file so a long stack trace cannot fill the pipe and stall the child.
        Path log = dir.resolve("child.log");
        Process process = new ProcessBuilder(java, "-cp", Objects.requireNonNull(System.getProperty("java.class.path")),
                LimitedPolicyCheck.class.getName()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(60, TimeUnit.SECONDS), "child JVM timed out");
            assertEquals(0, process.exitValue(), Files.readString(log));
        } finally {
            process.destroyForcibly();
        }
    }

    static final class LimitedPolicyCheck {
        public static void main(String[] args) throws Exception {
            Security.setProperty("crypto.policy", "limited");
            if (Cipher.getMaxAllowedKeyLength("ChaCha20") >= 256) {
                throw new IllegalStateException("limited crypto policy did not take effect");
            }
            new Chacha20CipherTest().rfc8439AeadVector();
        }
    }
}
