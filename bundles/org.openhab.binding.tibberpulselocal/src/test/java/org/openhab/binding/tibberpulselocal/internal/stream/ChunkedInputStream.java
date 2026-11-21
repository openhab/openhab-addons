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
package org.openhab.binding.tibberpulselocal.internal.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;
import java.util.function.IntConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Delivers a byte array in chunks of chosen sizes, to test arbitrary TCP segmentation. At the end of the data it
 * reports end of stream.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class ChunkedInputStream extends InputStream {

    private final byte[] data;
    private final int[] chunks;
    private final @Nullable IntConsumer beforeRead;
    private int position;
    private int chunkIndex;
    private int readLimit = Integer.MAX_VALUE;

    ChunkedInputStream(byte[] data, int... chunks) {
        this(data, null, chunks);
    }

    /** @param beforeRead called with the number of bytes delivered so far, before every read */
    ChunkedInputStream(byte[] data, @Nullable IntConsumer beforeRead, int... chunks) {
        this.data = data;
        this.chunks = chunks.length == 0 ? new int[] { Integer.MAX_VALUE } : chunks;
        this.beforeRead = beforeRead;
    }

    /** Chunks of seeded pseudo-random size 1 to {@code max}. */
    static ChunkedInputStream random(byte[] data, long seed, int max) {
        Random random = new Random(seed);
        int[] sizes = new int[Math.max(data.length, 1)];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = 1 + random.nextInt(max);
        }
        return new ChunkedInputStream(data, sizes);
    }

    /** A read that would go past this many bytes in total fails the test. */
    ChunkedInputStream failBeyond(int limit) {
        this.readLimit = limit;
        return this;
    }

    @Override
    public int read() throws IOException {
        byte[] one = new byte[1];
        int n = read(one, 0, 1);
        return n < 0 ? -1 : one[0] & 0xFF;
    }

    @Override
    public int read(byte @Nullable [] buffer, int offset, int length) throws IOException {
        Objects.requireNonNull(buffer);
        IntConsumer hook = beforeRead;
        if (hook != null) {
            hook.accept(position);
        }
        if (length == 0) {
            return 0;
        }
        if (position >= readLimit) {
            throw new AssertionError("read beyond the allowed " + readLimit + " bytes");
        }
        if (position >= data.length) {
            return -1;
        }
        int chunk = chunks[Math.min(chunkIndex++, chunks.length - 1)];
        int n = Math.min(Math.min(length, chunk), data.length - position);
        n = Math.min(n, readLimit - position);
        System.arraycopy(data, position, buffer, offset, n);
        position += n;
        return n;
    }
}
