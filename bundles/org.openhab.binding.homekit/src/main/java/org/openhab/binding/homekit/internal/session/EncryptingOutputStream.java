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
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link OutputStream} that encrypts data to an underlying Socket OutputStream.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class EncryptingOutputStream extends OutputStream {

    private static final int MAX_FRAME_LEN = 1024;

    private final OutputStream out;
    private final AtomicInteger writeCounter;
    private final AsymmetricSessionKeys keys;

    public EncryptingOutputStream(OutputStream outputStream, AtomicInteger writeCounter,
            AsymmetricSessionKeys sessionKeys) {
        this.out = outputStream;
        this.writeCounter = writeCounter;
        this.keys = sessionKeys;
    }

    @Override
    public void write(int b) throws IOException {
        byte[] one = { (byte) b };
        write(one, 0, 1);
    }

    @Override
    public void write(byte @Nullable [] b, int off, int len) throws IOException {
        if (b == null) {
            throw new IllegalArgumentException("b is null");
        }
        if (len == 0) {
            return;
        }
        send(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

    /**
     * Splits the plain text into frames of 1024 or fewer bytes, encrypts them, and sends each one separately.
     *
     * @param plainText the complete plain text message to be sent.
     * @throws IOException
     */
    private void send(byte[] plainText, int off, int len) throws IOException {
        int remaining = len;
        int offset = off;
        while (remaining > 0) {
            int frameSize = Math.min(remaining, MAX_FRAME_LEN);
            sendFrame(plainText, offset, frameSize);
            offset += frameSize;
            remaining -= frameSize;
        }
    }

    /**
     * Sends a single data frame over the output stream. This method encrypts a portion of the input plain text, and
     * sends it as a frame with a 2-byte length prefix, and a 16 byte tag. The length prefix is included in the cipher
     * AAD to ensure integrity. The write counter is incremented to ensure nonce uniqueness.
     * 
     * @param plainText the complete plain text message to be sent.
     * @param offset the offset into the plain text to start sending from.
     * @param size the size of the frame to send.
     * @throws IOException
     */
    private void sendFrame(byte[] plainText, int offset, int size) throws IOException {
        byte[] frameAad = new byte[2];
        frameAad[0] = (byte) (size & 0xFF);
        frameAad[1] = (byte) ((size >>> 8) & 0xFF);

        byte[] plainFrame = new byte[size];
        System.arraycopy(plainText, offset, plainFrame, 0, size);

        byte[] nonce64 = generateNonce64(writeCounter.getAndIncrement());

        try {
            byte[] cipherText = encrypt(keys.getWriteKey(), nonce64, plainFrame, frameAad);
            out.write(frameAad);
            out.write(cipherText);
        } catch (InvalidCipherTextException e) {
            throw new IOException("Encryption failed", e);
        }
    }
}
