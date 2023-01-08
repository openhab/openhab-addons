/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.driver.hub;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * ReadByteBuffer buffer class
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class ReadByteBuffer {
    private byte buf[]; // the actual buffer
    private int count; // number of valid bytes
    private int index = 0; // current read index
    private boolean done = false;

    /**
     * Constructor for ByteArrayIO with dynamic size
     *
     * @param size initial size, but will grow dynamically
     */
    public ReadByteBuffer(int size) {
        this.buf = new byte[size];
    }

    /**
     * Done reading bytes
     */
    public synchronized void done() {
        done = true;
        notifyAll();
    }

    /**
     * Number of unread bytes
     *
     * @return number of bytes not yet read
     */
    public synchronized int remaining() {
        return count - index;
    }

    /**
     * Blocking read of a single byte
     *
     * @return byte read
     * @throws IOException
     */
    public synchronized byte get() throws IOException {
        while (!done && remaining() < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new IOException("interrupted");
            }
        }

        if (done) {
            throw new IOException("done");
        }

        return buf[index++];
    }

    /**
     * Blocking read of multiple bytes
     *
     * @param bytes destination array for bytes read
     * @param off offset into dest array
     * @param len max number of bytes to read into dest array
     * @return number of bytes actually read
     * @throws IOException
     */
    public synchronized int get(byte @Nullable [] bytes, int off, int len) throws IOException {
        while (!done && remaining() < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new IOException("interrupted");
            }
        }

        if (done) {
            throw new IOException("done");
        }

        int b = Math.min(len, remaining());
        if (bytes != null) {
            System.arraycopy(buf, index, bytes, off, b);
        }
        index += b;
        return b;
    }

    /**
     * Adds bytes to the byte buffer
     *
     * @param b byte array with new bytes
     * @param off starting offset into buffer
     * @param len number of bytes to add
     */
    private synchronized void add(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int nCount = count + len;
        if (nCount > buf.length) {
            // dynamically grow the array
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, nCount));
        }
        // append new data to end of buffer
        System.arraycopy(b, off, buf, count, len);
        count = nCount;
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
     * Shrink the buffer to smallest size possible
     */
    public synchronized void makeCompact() {
        if (index == 0) {
            return;
        }
        byte[] newBuf = new byte[remaining()];
        System.arraycopy(buf, index, newBuf, 0, newBuf.length);
        index = 0;
        count = newBuf.length;
        buf = newBuf;
    }
}
