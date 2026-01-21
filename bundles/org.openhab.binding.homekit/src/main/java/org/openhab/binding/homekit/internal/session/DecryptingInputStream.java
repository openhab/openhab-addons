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
package org.openhab.binding.homekit.internal.session;

import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link InputStream} that decrypts data from the underlying Socket InputStream.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DecryptingInputStream extends InputStream {

    private final InputStream inputStream;
    private final AtomicInteger readCounter;
    private final AsymmetricSessionKeys keys;

    private byte[] plainText = new byte[0];
    private int plainTextPos = 0;

    public DecryptingInputStream(InputStream inputStream, AtomicInteger readCounter, AsymmetricSessionKeys keys) {
        this.inputStream = inputStream;
        this.readCounter = readCounter;
        this.keys = keys;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int c = read(b, 0, 1);
        return c == -1 ? -1 : (b[0] & 0xFF);
    }

    @Override
    public int read(byte @Nullable [] b) throws IOException {
        if (b == null) {
            throw new IOException(new NullPointerException("b is null"));
        }
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        if (b == null) {
            throw new IOException(new NullPointerException("b is null"));
        }
        if (len == 0) {
            return 0;
        }

        // serve leftover plaintext first
        if (plainTextPos < plainText.length) {
            int byteCount = Math.min(len, plainText.length - plainTextPos);
            System.arraycopy(plainText, plainTextPos, b, off, byteCount);
            plainTextPos += byteCount;
            return byteCount;
        }

        // no leftover plaintext; read next frame
        byte[] frame = receiveFrame();
        if (frame == null) {
            return -1; // EOF
        }

        plainText = frame;
        plainTextPos = 0;

        int byteCount = Math.min(len, plainText.length);
        System.arraycopy(plainText, 0, b, off, byteCount);
        plainTextPos = byteCount;
        return byteCount;
    }

    private byte @Nullable [] receiveFrame() throws IOException {
        byte[] frameAad = new byte[2];

        // If we cannot read the 2-byte header, this is EOF
        if (!readFully(frameAad)) {
            return null;
        }

        short frameLen = ByteBuffer.wrap(frameAad).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (frameLen < 0 || frameLen > 1024) {
            throw new IOException("Invalid frame length");
        }

        byte[] cipherText = new byte[frameLen + 16];

        // If ciphertext cannot be fully read, this is EOF
        if (!readFully(cipherText)) {
            return null;
        }

        byte[] nonce64 = generateNonce64(readCounter.getAndIncrement());
        try {
            return decrypt(keys.getReadKey(), nonce64, cipherText, frameAad);
        } catch (InvalidCipherTextException e) {
            throw new IOException("Invalid cipher text", e);
        }
    }

    /**
     * Reads buffer.length bytes unless EOF occurs.
     *
     * @return true if buffer was fully filled, false if EOF occurred before any bytes were read.
     */
    private boolean readFully(byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = inputStream.read(buffer, offset, buffer.length - offset);
            if (read == -1) {
                return false; // EOF
            }
            offset += read;
        }
        return true;
    }
}
