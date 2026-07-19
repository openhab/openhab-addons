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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Audio source reading raw PCM samples from an {@link InputStream}.
 *
 * <p>
 * The input is assumed to already be raw PCM in <em>little-endian</em> byte order with
 * the given sample rate, channel count and sample size; {@link #readFrames(int)} swaps
 * samples to big-endian as required by the {@link AudioSource} contract.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class PcmStreamAudioSource implements AudioSource {

    private final InputStream stream;
    private final int sampleRate;
    private final int channels;
    private final int sampleSize;
    private final long totalFrames;
    private boolean exhausted;

    /**
     * Creates a source with unknown length.
     */
    public PcmStreamAudioSource(InputStream stream, int sampleRate, int channels, int sampleSize) {
        this(stream, sampleRate, channels, sampleSize, -1);
    }

    /**
     * Creates a source with a known total number of frames ({@code -1} when unknown).
     */
    public PcmStreamAudioSource(InputStream stream, int sampleRate, int channels, int sampleSize, long totalFrames) {
        this.stream = stream;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.sampleSize = sampleSize;
        this.totalFrames = totalFrames;
    }

    @Override
    public byte[] readFrames(int nframes) throws IOException {
        if (exhausted) {
            return NO_FRAMES;
        }
        int totalBytes = nframes * frameSize();
        byte[] data = stream.readNBytes(totalBytes);
        if (data.length == 0) {
            exhausted = true;
            return NO_FRAMES;
        }
        if (data.length < totalBytes) {
            exhausted = true;
        }
        // Truncate to whole frames in case the stream ends mid-frame
        int wholeFrames = data.length - data.length % frameSize();
        if (wholeFrames != data.length) {
            byte[] truncated = new byte[wholeFrames];
            System.arraycopy(data, 0, truncated, 0, wholeFrames);
            data = truncated;
        }
        return AudioSource.toAudioSamples(data, sampleSize);
    }

    @Override
    public int sampleRate() {
        return sampleRate;
    }

    @Override
    public int channels() {
        return channels;
    }

    @Override
    public int sampleSize() {
        return sampleSize;
    }

    @Override
    public long totalFrames() {
        return totalFrames;
    }

    @Override
    public int duration() {
        if (totalFrames < 0 || sampleRate <= 0) {
            return 0;
        }
        return Math.round((float) totalFrames / sampleRate);
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
