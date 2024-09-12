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
package org.openhab.voice.pollytts.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;

/**
 * Implementation of the {@link AudioStream} interface for the {@link PollyTTSService}.
 * An AudioStream with an {@link InputStream} inside
 *
 * @author Robert Hillman - Initial contribution
 * @author Gwendal Roulleau - Refactor to simple audiostream
 */
@NonNullByDefault
public class PollyTTSAudioStream extends AudioStream {

    public InputStream innerInputStream;
    public AudioFormat audioFormat;

    public PollyTTSAudioStream(InputStream innerInputStream, AudioFormat audioFormat) {
        super();
        this.innerInputStream = innerInputStream;
        this.audioFormat = audioFormat;
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return innerInputStream.read();
    }

    @Override
    public int read(byte @Nullable [] b) throws IOException {
        return innerInputStream.read(b);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        return innerInputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return innerInputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return innerInputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte @Nullable [] b, int off, int len) throws IOException {
        return innerInputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return innerInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return innerInputStream.available();
    }

    @Override
    public void close() throws IOException {
        innerInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        innerInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        innerInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return innerInputStream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return innerInputStream.transferTo(out);
    }
}
