/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * ReadByteBuffer buffer class
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class ReadByteBuffer {
    private byte[] buffer; // the actual buffer
    private int count; // number of valid bytes
    private int index = 0; // current read index
    private boolean eof = false;

    /**
     * Constructor for ByteArrayIO with dynamic size
     *
     * @param size initial size, but will grow dynamically
     */
    public ReadByteBuffer(int size) {
        this.buffer = new byte[size];
    }

    /**
     * Closes buffer
     */
    public synchronized void close() {
        eof = true;
        notifyAll();
    }

    /**
     * Returns number of unread bytes
     *
     * @return number of bytes not yet read
     */
    public synchronized int remaining() {
        return count - index;
    }

    /**
     * Blocking read of a single byte
     *
     * @return -1 if eof, otherwise next byte read as an integer
     * @throws IOException
     */
    public synchronized int get() throws IOException {
        while (!eof && remaining() < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new IOException("interrupted");
            }
        }

        if (eof) {
            return -1;
        }

        return (int) buffer[index++];
    }

    /**
     * Blocking read of multiple bytes
     *
     * @param b destination array for bytes read
     * @param off offset into dest array
     * @param len max number of bytes to read into dest array
     * @return -1 if eof, otherwise number of bytes read
     * @throws IOException
     */
    public synchronized int get(byte[] b, int off, int len) throws IOException {
        while (!eof && remaining() < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new IOException("interrupted");
            }
        }

        if (eof) {
            return -1;
        }

        int numBytes = Math.min(len, remaining());
        System.arraycopy(buffer, index, b, off, numBytes);
        index += numBytes;
        return numBytes;
    }

    /**
     * Adds bytes to the byte buffer
     *
     * @param b byte array with new bytes
     * @param off starting offset into buffer
     * @param len number of bytes to add
     */
    private synchronized void add(byte[] b, int off, int len) {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newCount = count + len;
        if (newCount > buffer.length) {
            // dynamically grow the array
            buffer = Arrays.copyOf(buffer, Math.max(buffer.length << 1, newCount));
        }
        // append new data to end of buffer
        System.arraycopy(b, off, buffer, count, len);
        count = newCount;
        notifyAll();
    }

    /**
     * Adds bytes to the byte buffer
     *
     * @param b the new bytes to be added
     */
    public void add(byte[] b) {
        add(b, 0, b.length);
    }

    /**
     * Shrinks the buffer to smallest size possible
     */
    public synchronized void makeCompact() {
        if (index == 0) {
            return;
        }
        byte[] newBuffer = new byte[remaining()];
        System.arraycopy(buffer, index, newBuffer, 0, newBuffer.length);
        index = 0;
        count = newBuffer.length;
        buffer = newBuffer;
    }
}
