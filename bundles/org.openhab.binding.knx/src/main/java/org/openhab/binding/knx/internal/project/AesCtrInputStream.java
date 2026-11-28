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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Decrypts a range of an in-memory array with AES in counter mode as WinZip AES uses it: a 16-byte counter that
 * counts little-endian. The JDK's counter mode counts big-endian, so it cannot be used.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class AesCtrInputStream extends InputStream {

    private static final int BLOCK = 16;

    private final Cipher ecb;
    private final byte[] data;
    private final int end;
    private final byte[] counter;
    private final byte[] keyStream = new byte[BLOCK];
    private int position;
    private int used = BLOCK;

    /**
     * @param ecb AES on single blocks, see {@link WinZipAes#blockCipher}
     * @param initialCounter the counter of the first block; it is taken over and changed
     */
    AesCtrInputStream(Cipher ecb, byte[] data, int offset, int length, byte[] initialCounter) {
        this.ecb = ecb;
        this.data = data;
        this.position = offset;
        this.end = offset + length;
        this.counter = initialCounter;
    }

    /** The counter of a WinZip AES entry, which starts at 1. */
    static AesCtrInputStream winZip(Cipher ecb, byte[] data, int offset, int length) {
        byte[] counter = new byte[BLOCK];
        counter[0] = 1;
        return new AesCtrInputStream(ecb, data, offset, length, counter);
    }

    @Override
    public int read() throws IOException {
        if (position >= end) {
            return -1;
        }
        if (used == BLOCK) {
            nextBlock();
        }
        return (data[position++] ^ keyStream[used++]) & 0xFF;
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        byte[] buffer = Objects.requireNonNull(b);
        Objects.checkFromIndexSize(off, len, buffer.length);
        if (len == 0) {
            return 0;
        }
        if (position >= end) {
            return -1;
        }
        int n = Math.min(len, end - position);
        for (int i = 0; i < n; i++) {
            if (used == BLOCK) {
                nextBlock();
            }
            buffer[off + i] = (byte) (data[position++] ^ keyStream[used++]);
        }
        return n;
    }

    @Override
    public int available() {
        return end - position;
    }

    @Override
    public void close() {
        Arrays.fill(keyStream, (byte) 0);
        Arrays.fill(counter, (byte) 0);
        position = end;
    }

    private void nextBlock() throws IOException {
        try {
            ecb.update(counter, 0, BLOCK, keyStream, 0);
        } catch (ShortBufferException e) {
            throw new IOException("AES block failed", e);
        }
        for (int i = 0; i < BLOCK; i++) {
            if (++counter[i] != 0) {
                break;
            }
        }
        used = 0;
    }
}
