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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.session.DecryptingInputStream;

/**
 * Tests for the {@link DecryptingInputStream} class.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestDecryptingInputStream {

    private static final String MESSAGE_1 = "Hello";
    private static final String MESSAGE_2 = "World!";
    private static final String MESSAGE_0 = MESSAGE_1 + MESSAGE_2;

    @Test
    void testSingleFrame() throws Exception {
        byte[] plainTextIn = MESSAGE_0.getBytes();
        short len = (short) plainTextIn.length;

        byte[] frame = buildFrame(len);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(frame);

        AtomicInteger messageCounter = new AtomicInteger(0);
        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);

        byte[] readKey = new byte[32];
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        try (MockedStatic<CryptoUtils> crypto = mockStatic(CryptoUtils.class)) {
            crypto.when(() -> CryptoUtils.generateNonce64(0)).thenReturn(new byte[8]);
            crypto.when(() -> CryptoUtils.decrypt(eq(readKey), any(byte[].class), any(byte[].class), any(byte[].class)))
                    .thenReturn(plainTextIn);

            try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, messageCounter,
                    sessionKeys)) {
                byte[] plainTextOut = testStream.readAllBytes();

                assertTrue(Arrays.equals(plainTextIn, plainTextOut));
                assertEquals(1, messageCounter.get());
            }
        }
    }

    @Test
    void testTwoFrames() throws Exception {
        byte[] plainText1 = MESSAGE_1.getBytes();
        byte[] plainText2 = MESSAGE_2.getBytes();
        byte[] frame1 = buildFrame((short) plainText1.length);
        byte[] frame2 = buildFrame((short) plainText2.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(CryptoUtils.concat(frame1, frame2));

        AtomicInteger messageCounter = new AtomicInteger(0);

        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);
        byte[] readKey = new byte[32];
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        try (MockedStatic<CryptoUtils> crypto = mockStatic(CryptoUtils.class)) {
            crypto.when(() -> CryptoUtils.generateNonce64(0)).thenReturn(new byte[8]);
            crypto.when(() -> CryptoUtils.generateNonce64(1)).thenReturn(new byte[8]);
            crypto.when(() -> CryptoUtils.decrypt(eq(readKey), any(byte[].class), any(byte[].class), any(byte[].class)))
                    .thenReturn(plainText1, plainText2);

            try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, messageCounter,
                    sessionKeys)) {
                byte[] plainTextOut = testStream.readAllBytes();

                assertEquals(MESSAGE_0, new String(plainTextOut));
                assertEquals(2, messageCounter.get());
            }
        }
    }

    @Test
    void testUnexpectedEof() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
        AtomicInteger messageCounter = new AtomicInteger(0);
        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);

        try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, messageCounter, sessionKeys)) {
            assertEquals(-1, testStream.read());
        }
    }

    @Test
    void testUnexpectedEofMidFrame() throws Exception {
        // only 1 byte of header instead of 2
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 0x05 });
        AtomicInteger messageCounter = new AtomicInteger(0);
        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);

        try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, messageCounter, sessionKeys)) {
            assertEquals(-1, testStream.read());
        }
    }

    @Test
    void testSingleFrameWithRealCrypto() throws Exception {
        byte[] plainTextIn = MESSAGE_0.getBytes();
        short len = (short) plainTextIn.length;

        byte[] readKey = new byte[32];
        new SecureRandom().nextBytes(readKey);
        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        AtomicInteger messageCounter = new AtomicInteger(0);

        byte[] aad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(len).array();
        byte[] nonce = CryptoUtils.generateNonce64(0);
        byte[] cipherText = CryptoUtils.encrypt(readKey, nonce, plainTextIn, aad);
        byte[] frame = new byte[aad.length + cipherText.length];

        System.arraycopy(aad, 0, frame, 0, aad.length);
        System.arraycopy(cipherText, 0, frame, aad.length, cipherText.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(frame);

        try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, messageCounter, sessionKeys)) {
            byte[] plainTextOut = testStream.readAllBytes();

            assertFalse(Arrays.equals(plainTextIn, cipherText));
            assertTrue(Arrays.equals(plainTextIn, plainTextOut));
            assertEquals(1, messageCounter.get());
        }
    }

    @Test
    void testTwoFramesWithRealCrypto() throws Exception {
        byte[] plain1 = MESSAGE_1.getBytes();
        byte[] plain2 = MESSAGE_2.getBytes();

        short len1 = (short) plain1.length;
        short len2 = (short) plain2.length;

        byte[] readKey = new byte[32];
        new SecureRandom().nextBytes(readKey);

        AsymmetricSessionKeys keys = mock(AsymmetricSessionKeys.class);
        when(keys.getReadKey()).thenReturn(readKey);

        AtomicInteger counter = new AtomicInteger(0);

        // frame 1
        byte[] aad1 = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(len1).array();
        byte[] nonce1 = CryptoUtils.generateNonce64(0);
        byte[] cipher1 = CryptoUtils.encrypt(readKey, nonce1, plain1, aad1);
        byte[] frame1 = new byte[aad1.length + cipher1.length];
        System.arraycopy(aad1, 0, frame1, 0, aad1.length);
        System.arraycopy(cipher1, 0, frame1, aad1.length, cipher1.length);

        // frame 2
        byte[] aad2 = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(len2).array();
        byte[] nonce2 = CryptoUtils.generateNonce64(1);
        byte[] cipher2 = CryptoUtils.encrypt(readKey, nonce2, plain2, aad2);
        byte[] frame2 = new byte[aad2.length + cipher2.length];
        System.arraycopy(aad2, 0, frame2, 0, aad2.length);
        System.arraycopy(cipher2, 0, frame2, aad2.length, cipher2.length);

        // concatenate both frames
        byte[] allFrames = new byte[frame1.length + frame2.length];
        System.arraycopy(frame1, 0, allFrames, 0, frame1.length);
        System.arraycopy(frame2, 0, allFrames, frame1.length, frame2.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(allFrames);

        try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, counter, keys)) {
            byte[] plainTextOut = testStream.readAllBytes();

            assertFalse(Arrays.equals(plain1, cipher1));
            assertFalse(Arrays.equals(plain2, cipher2));
            assertEquals(MESSAGE_0, new String(plainTextOut));
            assertEquals(2, counter.get());
        }
    }

    @Test
    void testDecryptThrowsInvalidCipherTextException() throws Exception {
        byte[] plain = MESSAGE_1.getBytes();
        short len = (short) plain.length;

        byte[] frame = new byte[2 + len + 16];
        ByteBuffer.wrap(frame).order(ByteOrder.LITTLE_ENDIAN).putShort(len);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(frame);

        AtomicInteger counter = new AtomicInteger(0);
        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);
        byte[] readKey = new byte[32];
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        try (MockedStatic<CryptoUtils> crypto = mockStatic(CryptoUtils.class)) {
            crypto.when(() -> CryptoUtils.generateNonce64(0)).thenReturn(new byte[8]);
            crypto.when(() -> CryptoUtils.decrypt(eq(readKey), any(byte[].class), any(byte[].class), any(byte[].class)))
                    .thenThrow(new InvalidCipherTextException("bad tag"));

            try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, counter, sessionKeys)) {
                IOException ex = assertThrows(IOException.class, testStream::readAllBytes);

                assertTrue(ex.getMessage() instanceof String m && m.contains("Invalid cipher text"));
            }
        }
    }

    @Test
    void testPartialReads() throws Exception {
        byte[] plainText = MESSAGE_0.getBytes(); // "HelloWorld!"
        short len = (short) plainText.length;

        byte[] aad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(len).array();
        byte[] nonce = CryptoUtils.generateNonce64(0);
        byte[] readKey = new byte[32];
        new SecureRandom().nextBytes(readKey);

        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        byte[] cipherText = CryptoUtils.encrypt(readKey, nonce, plainText, aad);
        byte[] frame = new byte[aad.length + cipherText.length];
        System.arraycopy(aad, 0, frame, 0, aad.length);
        System.arraycopy(cipherText, 0, frame, aad.length, cipherText.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(frame);
        AtomicInteger messageCounter = new AtomicInteger(0);

        try (DecryptingInputStream testStream = new DecryptingInputStream(inputStream, messageCounter, sessionKeys)) {
            byte[] buf = new byte[4];

            // first partial read: 4 bytes
            int n1 = testStream.read(buf, 0, 4);
            assertEquals(4, n1);
            assertEquals("Hell", new String(buf, 0, 4));

            // second partial read: next 4 bytes
            int n2 = testStream.read(buf, 0, 4);
            assertEquals(4, n2);
            assertEquals("oWor", new String(buf, 0, 4));

            // final read: remaining bytes
            int n3 = testStream.read(buf, 0, 4);
            assertEquals(plainText.length - 8, n3); // remaining bytes
            assertEquals("ld!", new String(buf, 0, n3));

            // EOF
            assertEquals(-1, testStream.read());

            // only one frame was read
            assertEquals(1, messageCounter.get());
        }
    }

    @Test
    void testFrameDeliveredByteByByte() throws Exception {
        byte[] plainTextIn = MESSAGE_0.getBytes();
        short len = (short) plainTextIn.length;

        byte[] aad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(len).array();
        byte[] nonce = CryptoUtils.generateNonce64(0);

        byte[] readKey = new byte[32];
        new SecureRandom().nextBytes(readKey);

        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        byte[] cipherText = CryptoUtils.encrypt(readKey, nonce, plainTextIn, aad);

        byte[] frame = new byte[aad.length + cipherText.length];
        System.arraycopy(aad, 0, frame, 0, aad.length);
        System.arraycopy(cipherText, 0, frame, aad.length, cipherText.length);

        // wrap in a stream that returns 1 byte per read
        InputStream slowStream = new InputStream() {
            int pos = 0;

            @Override
            public int read() {
                return pos < frame.length ? (frame[pos++] & 0xFF) : -1;
            }
        };

        AtomicInteger messageCounter = new AtomicInteger(0);

        try (DecryptingInputStream testStream = new DecryptingInputStream(slowStream, messageCounter, sessionKeys)) {
            byte[] plainTextOut = testStream.readAllBytes();

            assertTrue(Arrays.equals(plainTextIn, plainTextOut));
            assertEquals(1, messageCounter.get());
        }
    }

    @Test
    void testFrameDeliveredInRandomChunks() throws Exception {
        byte[] plainTextIn = MESSAGE_0.getBytes();
        short len = (short) plainTextIn.length;

        byte[] aad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(len).array();
        byte[] nonce = CryptoUtils.generateNonce64(0);

        byte[] readKey = new byte[32];
        new SecureRandom().nextBytes(readKey);

        AsymmetricSessionKeys sessionKeys = mock(AsymmetricSessionKeys.class);
        when(sessionKeys.getReadKey()).thenReturn(readKey);

        byte[] cipherText = CryptoUtils.encrypt(readKey, nonce, plainTextIn, aad);

        byte[] frame = new byte[aad.length + cipherText.length];
        System.arraycopy(aad, 0, frame, 0, aad.length);
        System.arraycopy(cipherText, 0, frame, aad.length, cipherText.length);

        // wrap in a stream that returns random-sized chunks
        InputStream flakyStream = new FragmentingInputStream(frame);

        AtomicInteger messageCounter = new AtomicInteger(0);

        try (DecryptingInputStream testStream = new DecryptingInputStream(flakyStream, messageCounter, sessionKeys)) {
            byte[] plainTextOut = testStream.readAllBytes();

            assertTrue(Arrays.equals(plainTextIn, plainTextOut));
            assertEquals(1, messageCounter.get());
        }
    }

    // helpers

    private static byte[] buildFrame(short len) {
        byte[] frame = new byte[2 + len + 16];
        ByteBuffer.wrap(frame).order(ByteOrder.LITTLE_ENDIAN).putShort(len);
        return frame;
    }

    @NonNullByDefault({})
    class FragmentingInputStream extends InputStream {

        private final byte[] data;
        private int pos = 0;

        FragmentingInputStream(byte[] data) {
            this.data = data;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (pos >= data.length) {
                return -1;
            }
            int chunk = Math.min(len, 1 + new java.security.SecureRandom().nextInt(5));
            int remaining = data.length - pos;
            int n = Math.min(chunk, remaining);
            System.arraycopy(data, pos, b, off, n);
            pos += n;
            return n;
        }

        @Override
        public int read() {
            return pos < data.length ? (data[pos++] & 0xFF) : -1;
        }
    }
}
