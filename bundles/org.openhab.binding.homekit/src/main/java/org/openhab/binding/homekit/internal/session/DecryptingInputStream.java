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
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link InputStream} that decrypts data from an underlying Socket InputStream.
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
            throw new IllegalArgumentException("b is null");
        }
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("b is null");
        }
        if (len == 0) {
            return 0;
        }

        // serve leftover plain text first
        if (plainTextPos < plainText.length) {
            int byteCount = Math.min(len, plainText.length - plainTextPos);
            System.arraycopy(plainText, plainTextPos, b, off, byteCount);
            plainTextPos += byteCount;
            return byteCount;
        }

        // no leftover plain text; read next frame
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

    /**
     * Receives and decrypts a single data frame from the input stream.
     *
     * @return the decrypted plain text, or null if EOF is reached.
     * @throws IOException
     */
    private byte @Nullable [] receiveFrame() throws IOException {
        byte[] frameAad = new byte[2];
        if (!readFully(frameAad)) {
            return null; // if we cannot read the 2-byte header, this is EOF
        }

        short frameLen = (short) ((frameAad[0] & 0xFF) | ((frameAad[1] & 0xFF) << 8));
        if (frameLen < 0 || frameLen > 1024) {
            throw new IOException(
                    "Invalid frame length {0x%02x, 0x%02x}".formatted(frameAad[0] & 0xFF, frameAad[1] & 0xFF));
        }

        byte[] cipherText = new byte[frameLen + 16];
        if (!readFully(cipherText)) {
            throw new IOException("Truncated encrypted frame");
        }

        byte[] nonce64 = generateNonce64(readCounter.getAndIncrement());

        try {
            return decrypt(keys.getReadKey(), nonce64, cipherText, frameAad);
        } catch (InvalidCipherTextException e) {
            throw new IOException("Invalid cipher text", e);
        }
    }

    /**
     * Reads buffer.length bytes unless EOF occurs. If a SocketTimeoutException occurs before any bytes have
     * been read, it is propagated. If some bytes have already been read, the read operation is forced to continue
     * until the buffer is fully filled or EOF occurs.
     * 
     * @param buffer the buffer to fill
     * @throws IOException
     * @return true if buffer was fully filled, false if EOF occurred.
     */
    private boolean readFully(byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            try {
                int read = inputStream.read(buffer, offset, buffer.length - offset);
                if (read == -1) {
                    return false; // EOF
                }
                offset += read;
            } catch (SocketTimeoutException e) {
                // propagate the exception if nothing has been read yet
                if (offset == 0) {
                    throw e;
                }
                // if a partial frame has already been read, we must continue reading until done
                continue;
            }
        }
        return true;
    }
}
