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
package org.openhab.binding.atv.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Limits how many bytes each bulk read asks the wrapped stream for.
 *
 * <p>
 * Some openHAB audio streams, such as {@code URLAudioStream}, only implement {@link InputStream#read()}. A bulk
 * read then waits until every requested byte has arrived. The MP3 decoder reads through a
 * {@link java.io.BufferedInputStream} whose requests grow to megabytes, which takes minutes on a live radio
 * stream. Small requests return as soon as a little audio has arrived.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class SmallReadInputStream extends FilterInputStream {

    /** About 64 ms of a 128 kbit/s MP3 stream. */
    static final int MAX_READ = 1024;

    SmallReadInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        return super.read(b, off, Math.min(len, MAX_READ));
    }
}
