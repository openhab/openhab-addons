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
package org.openhab.automation.jsscripting.internal.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Simple wrapper around a byte array to provide a SeekableByteChannel for consumption
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ReadOnlySeekableByteArrayChannel implements SeekableByteChannel {
    private byte[] data;
    private int position;
    private boolean closed;

    public ReadOnlySeekableByteArrayChannel(byte[] data) {
        this.data = data;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        ensureOpen();
        position = (int) Math.max(0, Math.min(newPosition, size()));
        return this;
    }

    @Override
    public long size() {
        return data.length;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        ensureOpen();
        int remaining = (int) size() - position;
        if (remaining <= 0) {
            return -1;
        }
        int readBytes = buf.remaining();
        if (readBytes > remaining) {
            readBytes = remaining;
        }
        buf.put(data, position, readBytes);
        position += readBytes;
        return readBytes;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public int write(ByteBuffer b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel truncate(long newSize) {
        throw new UnsupportedOperationException();
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }
}
