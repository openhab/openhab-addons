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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Known-answer tests for the building blocks of the protected-project reader, from published test vectors.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class WinZipAesTest {

    private static final HexFormat HEX = HexFormat.of();

    private static byte[] hex(String text) {
        return HEX.parseHex(text.replace(" ", ""));
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] sha1(String password, String salt, int iterations, int length) {
        return WinZipAes.pbkdf2("HmacSHA1", ascii(password), ascii(salt), iterations, length);
    }

    private static byte[] sha256(String password, String salt, int iterations, int length) {
        return WinZipAes.pbkdf2("HmacSHA256", ascii(password), ascii(salt), iterations, length);
    }

    @Test
    void pbkdf2HmacSha1MatchesRfc6070() {
        // RFC 6070, section 2, test vectors 1, 2, 3, 5 and 6
        assertArrayEquals(hex("0c60c80f961f0e71f3a9b524af6012062fe037a6"), sha1("password", "salt", 1, 20));
        assertArrayEquals(hex("ea6c014dc72d6f8ccd1ed92ace1d41f0d8de8957"), sha1("password", "salt", 2, 20));
        assertArrayEquals(hex("4b007901b765489abead49d926f721d065a429c1"), sha1("password", "salt", 4096, 20));
        assertArrayEquals(hex("3d2eec4fe41c849b80c8d83662c0e44a8b291a964cf2f07038"),
                sha1("passwordPASSWORDpassword", "saltSALTsaltSALTsaltSALTsaltSALTsalt", 4096, 25));
        assertArrayEquals(hex("56fa6aa75548099dcc37d7f03425e0c3"), sha1("pass\0word", "sa\0lt", 4096, 16));
    }

    @Test
    void pbkdf2HmacSha256MatchesRfc7914() {
        // RFC 7914, section 11, the two PBKDF2-HMAC-SHA256 test vectors
        assertArrayEquals(
                hex("55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc"
                        + "49ca9cccf179b645991664b39d77ef317c71b845b1e30bd509112041d3a19783"),
                sha256("passwd", "salt", 1, 64));
        assertArrayEquals(
                hex("4ddcd8f60b98be21830cee5ef22701f9641a4418d04c0414aeff08876b34ab56"
                        + "a1d425a1225833549adb841b51c9b3176a272bdebba1d078478f62b397f33c8d"),
                sha256("Password", "NaCl", 80000, 64));
    }

    @Test
    void etsZipPasswordOfAnAsciiPasswordMatchesTheJdk() throws Exception {
        // For ASCII text the UTF-16LE bytes are again ASCII, so the JDK's PBKDF2 (which encodes chars as UTF-8)
        // gets the same bytes when each byte is passed as one char.
        char[] password = "made-up Pass 42".toCharArray();
        char[] asBytes = new String(new String(password).getBytes(StandardCharsets.UTF_16LE),
                StandardCharsets.ISO_8859_1).toCharArray();
        byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(new PBEKeySpec(asBytes, ascii("21.project.ets.knx.org"), 65536, 256)).getEncoded();

        byte[] zipPassword = WinZipAes.etsZipPassword(password);
        assertEquals(java.util.Base64.getEncoder().encodeToString(key),
                new String(zipPassword, StandardCharsets.US_ASCII));
        assertEquals(44, zipPassword.length);
        assertEquals(ProtectedPartWriter.zipPassword("made-up Pass 42"),
                new String(zipPassword, StandardCharsets.US_ASCII));
    }

    @Test
    void etsZipPasswordUsesUtf16LittleEndianBytes() throws Exception {
        String password = "W\u00e4chter-\u20ac-\ud834\udd1e";
        assertEquals(ProtectedPartWriter.zipPassword(password),
                new String(WinZipAes.etsZipPassword(password.toCharArray()), StandardCharsets.US_ASCII));
    }

    @Test
    void authenticationCodeIsTheFirstTenBytesOfHmacSha1() {
        // RFC 2202, section 3, test case 5 (HMAC-SHA-1 with truncation)
        byte[] key = new byte[24];
        java.util.Arrays.fill(key, 2, 22, (byte) 0x0c);
        byte[] data = ascii("xxTest With Truncationxx");

        assertArrayEquals(hex("4c1a03424b55e07fe7f2"), WinZipAes.authenticationCode(key, 2, 20, data, 2, 20));
    }

    @Test
    void counterModeBlockMatchesSp80038a() throws Exception {
        // NIST SP 800-38A, appendix F.5.1, F.5.3 and F.5.5 (CTR-AESxxx.Encrypt), block 1. The later blocks of
        // those vectors count big-endian, so only the first block applies to a counter that counts the other way.
        byte[] counter = hex("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff");
        byte[] plain = hex("6bc1bee22e409f96e93d7e117393172a");
        assertArrayEquals(hex("874d6191b620e3261bef6864990db6ce"),
                ctr(hex("2b7e151628aed2a6abf7158809cf4f3c"), counter, plain));
        assertArrayEquals(hex("1abc932417521ca24f2b0459fe7e6e0b"),
                ctr(hex("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b"), counter, plain));
        assertArrayEquals(hex("601ec313775789a5b7a7f504bbf3d228"),
                ctr(hex("603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4"), counter, plain));
    }

    @Test
    void counterStartsAtOneAndCountsLittleEndian() throws Exception {
        byte[] key = hex("000102030405060708090a0b0c0d0e0f");
        byte[] zeros = new byte[16 * 258 + 5];
        byte[] keyStream;
        try (InputStream in = AesCtrInputStream.winZip(WinZipAes.blockCipher(key, 0, 16), zeros, 0, zeros.length)) {
            keyStream = readInPieces(in);
        }
        assertEquals(zeros.length, keyStream.length);

        Cipher aes = Cipher.getInstance("AES/ECB/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        assertBlock(aes, "01000000000000000000000000000000", keyStream, 0);
        assertBlock(aes, "02000000000000000000000000000000", keyStream, 1);
        assertBlock(aes, "ff000000000000000000000000000000", keyStream, 254);
        assertBlock(aes, "00010000000000000000000000000000", keyStream, 255);
        assertBlock(aes, "01010000000000000000000000000000", keyStream, 256);
        assertBlock(aes, "02010000000000000000000000000000", keyStream, 257);
        byte[] last = aes.doFinal(hex("03010000000000000000000000000000"));
        assertArrayEquals(java.util.Arrays.copyOf(last, 5),
                java.util.Arrays.copyOfRange(keyStream, 16 * 258, keyStream.length));
    }

    @Test
    void counterCarriesThroughAllBytes() throws Exception {
        byte[] key = hex("000102030405060708090a0b0c0d0e0f");
        byte[] zeros = new byte[32];
        byte[] keyStream;
        try (InputStream in = new AesCtrInputStream(WinZipAes.blockCipher(key, 0, 16), zeros, 0, 32,
                hex("ffffffffffffffffffffffffffffff7f"))) {
            keyStream = in.readAllBytes();
        }
        Cipher aes = Cipher.getInstance("AES/ECB/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        assertBlock(aes, "ffffffffffffffffffffffffffffff7f", keyStream, 0);
        assertBlock(aes, "00000000000000000000000000000080", keyStream, 1);
    }

    private static void assertBlock(Cipher aes, String counter, byte[] keyStream, int block) throws Exception {
        assertArrayEquals(aes.doFinal(hex(counter)),
                java.util.Arrays.copyOfRange(keyStream, block * 16, block * 16 + 16), "block " + block);
    }

    private static byte[] ctr(byte[] key, byte[] counter, byte[] data) throws Exception {
        try (InputStream in = new AesCtrInputStream(WinZipAes.blockCipher(key, 0, key.length), data, 0, data.length,
                counter.clone())) {
            return in.readAllBytes();
        }
    }

    /** Reads with odd piece sizes, so a block is split over several reads. */
    private static byte[] readInPieces(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[7];
        int b = in.read();
        if (b >= 0) {
            out.write(b);
        }
        int n;
        while ((n = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
