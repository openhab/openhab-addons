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

package org.openhab.binding.bluetooth.govee.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class EncryptionHelperTest {

    public EncryptionHelperTest() {
    }

    @Test
    public void partitialAesTest() {
        // Check AES part of encryption (test values from NIST:
        // https://csrc.nist.gov/CSRC/media/Projects/Cryptographic-Algorithm-Validation-Program/documents/aes/KAT_AES.zip)
        assertAesEncryption("10a58869d74be5a374cf867cfb473859", "6d251e6944b051e04eaa6fb4dbf78465");
        assertAesEncryption("47d6742eefcc0465dc96355e851b64d9", "0306194f666d183624aa230a8b264ae7");
        assertAesEncryption("da84367f325d42d601b4326964802e8e", "bba071bcb470f8f6586e5d3add18bc66");
        assertAesEncryption("71b5c08a1993e1362e4d0ce9b22b78d5", "c2dabd117f8a3ecabfbb11d12194d9d0");
        assertAesEncryption("febd9a24d8b65c1c787d50a4ed3619a9", "f4a70d8af877f9b02b4c40df57d45b17");

        assertAesDecryption("10a58869d74be5a374cf867cfb473859", "6d251e6944b051e04eaa6fb4dbf78465");
        assertAesDecryption("47d6742eefcc0465dc96355e851b64d9", "0306194f666d183624aa230a8b264ae7");
        assertAesDecryption("da84367f325d42d601b4326964802e8e", "bba071bcb470f8f6586e5d3add18bc66");
        assertAesDecryption("71b5c08a1993e1362e4d0ce9b22b78d5", "c2dabd117f8a3ecabfbb11d12194d9d0");
        assertAesDecryption("febd9a24d8b65c1c787d50a4ed3619a9", "f4a70d8af877f9b02b4c40df57d45b17");
    }

    private void assertAesEncryption(String hexKey, String hexExpectedCipherText) {
        // the first 16 bytes are AES only, the rest is RC4 only
        byte[] inputPacket = new byte[20];
        byte[] key = HexFormat.of().parseHex(hexKey);
        EncryptionHelper encryption = new EncryptionHelper(key);
        byte[] resultPacket = encryption.encrypt(inputPacket);
        assertArrayEquals(HexFormat.of().parseHex(hexExpectedCipherText), Arrays.copyOf(resultPacket, 16));
    }

    private void assertAesDecryption(String hexKey, String hexEncryptedData) {
        // the first 16 bytes are AES only, the rest is RC4 only
        byte[] inputPacket = new byte[20];
        System.arraycopy(HexFormat.of().parseHex(hexEncryptedData), 0, inputPacket, 0, 16);
        byte[] key = HexFormat.of().parseHex(hexKey);
        EncryptionHelper encryption = new EncryptionHelper(key);
        byte[] resultPacket = encryption.decrypt(inputPacket);
        assertArrayEquals(new byte[16], Arrays.copyOf(resultPacket, 16));
    }

    @Test
    public void testRc4Reversible() {
        // RC4 applied twice must yield the original data
        byte[] input = new byte[] { 1, 2, 3, 4, 5 };
        byte[] key = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        EncryptionHelper encryption = new EncryptionHelper(key);
        byte[] encrypted = encryption.rc4(input);
        assertFalse(Arrays.equals(encrypted, input));
        byte[] decrypted = encryption.rc4(encrypted);
        assertTrue(Arrays.equals(decrypted, input));
    }

    @Test
    public void testRc4() {
        // Testvector from RFC6229 (https://datatracker.ietf.org/doc/html/rfc6229)
        assertTestVector("0102030405060708090a0b0c0d0e0f10", """
                DEC    0 HEX    0:  9a c7 cc 9a  60 9d 1e f7   b2 93 28 99  cd e4 1b 97
                DEC   16 HEX   10:  52 48 c4 95  90 14 12 6a   6e 8a 84 f1  1d 1a 9e 1c
                DEC  240 HEX   f0:  06 59 02 e4  b6 20 f6 cc   36 c8 58 9f  66 43 2f 2b
                DEC  256 HEX  100:  d3 9d 56 6b  c6 bc e3 01   07 68 15 15  49 f3 87 3f
                DEC  496 HEX  1f0:  b6 d1 e6 c4  a5 e4 77 1c   ad 79 53 8d  f2 95 fb 11
                DEC  512 HEX  200:  c6 8c 1d 5c  55 9a 97 41   23 df 1d bc  52 a4 3b 89
                DEC  752 HEX  2f0:  c5 ec f8 8d  e8 97 fd 57   fe d3 01 70  1b 82 a2 59
                DEC  768 HEX  300:  ec cb e1 3d  e1 fc c9 1c   11 a0 b2 6c  0b c8 fa 4d
                DEC 1008 HEX  3f0:  e7 a7 25 74  f8 78 2a e2   6a ab cf 9e  bc d6 60 65
                DEC 1024 HEX  400:  bd f0 32 4e  60 83 dc c6   d3 ce dd 3c  a8 c5 3c 16
                DEC 1520 HEX  5f0:  b4 01 10 c4  19 0b 56 22   a9 61 16 b0  01 7e d2 97
                DEC 1536 HEX  600:  ff a0 b5 14  64 7e c0 4f   63 06 b8 92  ae 66 11 81
                DEC 2032 HEX  7f0:  d0 3d 1b c0  3c d3 3d 70   df f9 fa 5d  71 96 3e bd
                DEC 2048 HEX  800:  8a 44 12 64  11 ea a7 8b   d5 1e 8d 87  a8 87 9b f5
                DEC 3056 HEX  bf0:  fa be b7 60  28 ad e2 d0   e4 87 22 e4  6c 46 15 a3
                DEC 3072 HEX  c00:  c0 5d 88 ab  d5 03 57 f9   35 a6 3c 59  ee 53 76 23
                DEC 4080 HEX  ff0:  ff 38 26 5c  16 42 c1 ab   e8 d3 c2 fe  5e 57 2b f8
                DEC 4096 HEX 1000:  a3 6a 4c 30  1a e8 ac 13   61 0c cb c1  22 56 ca cc
                """);
    }

    public void assertTestVector(String hexKeyString, String result) {
        byte[] key = HexFormat.of().parseHex(hexKeyString);

        List<TestRange> testVektor = parseTestVector(result);

        int lastDataPoint = testVektor.stream().map(tr -> tr.offset() + tr.expectedResult().length)
                .max(Integer::compare).orElseThrow();

        EncryptionHelper encryption = new EncryptionHelper(key);

        byte[] testdata = new byte[lastDataPoint];
        byte[] encrypted = encryption.rc4(testdata);

        for (TestRange testRange : testVektor) {
            assertArrayEquals(testRange.expectedResult(), Arrays.copyOfRange(encrypted, testRange.offset(),
                    testRange.offset() + testRange.expectedResult().length));
        }
    }

    private List<TestRange> parseTestVector(String input) {
        Pattern testLinePattern = Pattern.compile("DEC\\s+(\\d+)\\s+HEX\\s+[0-9a-f]+:\\s+(.*)");
        List<TestRange> ranges = new ArrayList<>();
        for (String line : input.split("[\r\n]+")) {
            Matcher m = testLinePattern.matcher(line);
            byte[] buffer = new byte[256];
            if (m.matches()) {
                int offset = Integer.parseInt(m.group(1));
                int byteCount = 0;
                for (String hexByte : m.group(2).split("\\s+")) {
                    byteCount++;
                    buffer[byteCount - 1] = (byte) Integer.parseInt(hexByte, 16);
                }
                ranges.add(new TestRange(offset, Arrays.copyOf(buffer, byteCount)));
            }
        }
        return ranges;
    }

    private record TestRange(int offset, byte[] expectedResult) {

    }
}
