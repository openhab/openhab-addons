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

/**
 * An {@link InputStream} for unit test purposes.
 * 
 * NOTE: this class is not annotated as NonNullByDefault since it overrides methods from InputStream
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class JUnitTestInputStream extends InputStream {

    private final Deque<byte[]> chunks = new ArrayDeque<>();
    private byte[] current = null;
    private int pos = 0;

    public JUnitTestInputStream(List<byte[]> chunks) {
        this.chunks.addAll(chunks);
    }

    @Override
    public int read() {
        if (!ensureChunk()) {
            return 0; // not -1 EOF
        }
        return current[pos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (!ensureChunk()) {
            return 0; // not -1 EOF;
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
