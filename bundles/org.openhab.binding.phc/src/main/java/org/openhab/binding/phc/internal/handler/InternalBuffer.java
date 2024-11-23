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
package org.openhab.binding.phc.internal.handler;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Buffer for received messages
 *
 * @author Jonas Hohaus - Initial contribution
 */
class InternalBuffer {
    private static final int MAX_SIZE = 512;

    private final BlockingQueue<byte[]> byteQueue = new LinkedBlockingQueue<>();
    private byte[] buffer;
    private int bufferIndex = 0;
    private int size;

    public void offer(byte[] buffer) {
        // If the buffer becomes too large, already processed commands accumulate and
        // the reaction becomes slow.
        if (size < MAX_SIZE) {
            byte[] localBuffer = Arrays.copyOf(buffer, Math.min(MAX_SIZE - size, buffer.length));
            byteQueue.offer(localBuffer);
            synchronized (this) {
                size += localBuffer.length;
            }
        }
    }

    public boolean hasNext() {
        return (size > 0);
    }

    public byte get() throws InterruptedException {
        byte[] buf = getBuffer();
        if (buf != null) {
            byte result = buf[bufferIndex++];
            synchronized (this) {
                size--;
            }

            return result;
        }

        throw new IllegalStateException("get without hasNext");
    }

    public int size() {
        return size;
    }

    private byte[] getBuffer() throws InterruptedException {
        if (buffer == null || bufferIndex == buffer.length) {
            buffer = byteQueue.take();
            bufferIndex = 0;
        }

        return buffer;
    }
}
