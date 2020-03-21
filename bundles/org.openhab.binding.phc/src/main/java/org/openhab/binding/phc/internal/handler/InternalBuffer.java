/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Buffer for received messages
 *
 * @author Jonas Hohaus - Initial contribution
 */
class InternalBuffer {
    private static final int MAX_SIZE = 512;

    private final Queue<byte[]> byteQueue = new ConcurrentLinkedQueue<byte[]>();
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
        return (getBuffer() != null);
    }

    public byte get() {
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

    private byte[] getBuffer() {
        if (buffer == null || bufferIndex == buffer.length) {
            buffer = byteQueue.poll();
            bufferIndex = 0;
        }

        return buffer;
    }
}
