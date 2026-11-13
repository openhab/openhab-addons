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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Growable byte buffer for reassembling frames from a stream. Appending only copies the
 * new bytes (plus an occasional compaction), so reading a large frame in small chunks
 * stays linear.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class ReadBuffer {

    private byte[] data = new byte[8192];
    private int start;
    private int end;

    /**
     * Appends bytes to the end of the buffer.
     *
     * @param src source array
     * @param offset offset in {@code src}
     * @param length number of bytes to append
     */
    public void append(byte[] src, int offset, int length) {
        ensureSpace(length);
        System.arraycopy(src, offset, data, end, length);
        end += length;
    }

    /** Number of unread bytes. */
    public int size() {
        return end - start;
    }

    /**
     * Returns an unsigned byte without consuming it.
     *
     * @param index index relative to the first unread byte
     * @return the byte value (0-255)
     */
    public int peek(int index) {
        return data[start + index] & 0xFF;
    }

    /**
     * Returns a read-only view of the unread bytes. The view is only valid until the next
     * {@link #append} or {@link #take}.
     */
    public ByteBuffer view() {
        return ByteBuffer.wrap(data, start, size()).slice().asReadOnlyBuffer();
    }

    /**
     * Discards bytes from the front of the buffer.
     *
     * @param count number of bytes to discard
     */
    public void skip(int count) {
        start += count;
        if (start == end) {
            start = 0;
            end = 0;
        }
    }

    /**
     * Removes and returns bytes from the front of the buffer.
     *
     * @param count number of bytes to take
     * @return the bytes
     */
    public byte[] take(int count) {
        byte[] result = Arrays.copyOfRange(data, start, start + count);
        skip(count);
        return result;
    }

    private void ensureSpace(int length) {
        if (end + length <= data.length) {
            return;
        }
        int size = size();
        byte[] target = size + length <= data.length ? data : new byte[Math.max(data.length * 2, size + length)];
        System.arraycopy(data, start, target, 0, size);
        data = target;
        start = 0;
        end = size;
    }
}
