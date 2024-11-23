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
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * Wrapper for a {@link SeekableByteChannel} allowing prefixing the stream with a fixed array of bytes
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class PrefixedSeekableByteChannel implements SeekableByteChannel {

    private byte[] prefix;
    private SeekableByteChannel source;
    private long position;

    public PrefixedSeekableByteChannel(byte[] prefix, SeekableByteChannel source) {
        this.prefix = prefix;
        this.source = source;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int read = 0;

        if (position < prefix.length) {
            dst.put(Arrays.copyOfRange(prefix, (int) position, prefix.length));
            read += prefix.length - position;
        }

        read += source.read(dst);

        position += read;

        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new IOException("Read only!");
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        this.position = newPosition;

        if (newPosition > prefix.length) {
            source.position(newPosition - prefix.length);
        }

        return this;
    }

    @Override
    public long size() throws IOException {
        return source.size() + prefix.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new IOException("Read only!");
    }

    @Override
    public boolean isOpen() {
        return source.isOpen();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
