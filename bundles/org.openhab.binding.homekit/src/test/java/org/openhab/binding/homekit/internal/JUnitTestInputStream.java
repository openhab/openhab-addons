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
package org.openhab.binding.homekit.internal;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link InputStream} for unit test purposes.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
// NonNullByDefault is disabled since it otherwise prevents overriding methods of {@link InputStream}
public @NonNullByDefault({}) class JUnitTestInputStream extends InputStream {

    private final Deque<byte[]> chunks = new ArrayDeque<>();
    private byte[] current = null;
    private int pos = 0;

    public JUnitTestInputStream(List<byte[]> chunks) {
        this.chunks.addAll(chunks);
    }

    @Override
    public int read() {
        if (!ensureChunk()) {
            return -1;
        }
        return current[pos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (!ensureChunk()) {
            return -1;
        }

        int remaining = current.length - pos;
        int n = Math.min(len, remaining);
        System.arraycopy(current, pos, b, off, n);
        pos += n;
        return n;
    }

    private boolean ensureChunk() {
        if (current != null && pos < current.length) {
            return true;
        }
        current = chunks.pollFirst();
        pos = 0;
        return current != null;
    }
}
