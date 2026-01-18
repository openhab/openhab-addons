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

/**
 * An {@link InputStream} that de-crypts data from the underlying Socket InputStream.
 *
 * NOTE: this class is not annotated as NonNullByDefault since it overrides methods from InputStream
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
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
        int byteCount = read(b, 0, 1);
        return byteCount == -1 ? -1 : (b[0] & 0xFF);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
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
        // no leftover plain text; read and de-crypt next frame
        byte[] frame = receiveFrame();
        if (frame == null) {
            return -1;
        }
        plainText = frame;
        plainTextPos = 0;
        int byteCount = Math.min(len, plainText.length);
        System.arraycopy(plainText, 0, b, off, byteCount);
        plainTextPos = byteCount;
        return byteCount;
    }

    private byte[] receiveFrame() throws IOException {
        byte[] frameAad = new byte[2]; // AAD data length prefix
        readFully(frameAad);
        short frameLen = ByteBuffer.wrap(frameAad).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (frameLen < 0 || frameLen > 1024) {
            throw new IOException("Invalid frame length");
        }
        byte[] cipherText = new byte[frameLen + 16]; // read 16 extra bytes for the auth tag
        readFully(cipherText);
        byte[] nonce64 = generateNonce64(readCounter.getAndIncrement());
        try {
            return decrypt(keys.getReadKey(), nonce64, cipherText, frameAad);
        } catch (InvalidCipherTextException e) {
            throw new IOException("Invalid cipher text", e);
        }
    }

    private void readFully(byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = inputStream.read(buffer, offset, buffer.length - offset);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            offset += read;
        }
    }
}
