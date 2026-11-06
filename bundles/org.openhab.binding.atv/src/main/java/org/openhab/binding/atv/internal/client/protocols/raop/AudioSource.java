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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Audio source that returns raw PCM frames that can be streamed via RAOP.
 *
 * <p>
 * Built on Java Sound. Frames are returned with samples in <em>network byte order</em>
 * (big-endian), as AirPlay receivers expect for PCM payloads.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AudioSource extends AutoCloseable {

    /** Number of frames per RTP audio packet. */
    int FRAMES_PER_PACKET = 352;

    /** Returned by {@link #readFrames(int)} when no more audio is available. */
    byte[] NO_FRAMES = new byte[0];

    /**
     * Reads up to {@code nframes} frames and advances in the stream.
     *
     * <p>
     * Returns fewer frames at end of stream and {@link #NO_FRAMES} (an empty array)
     * once exhausted. Samples are big-endian signed PCM.
     */
    byte[] readFrames(int nframes) throws IOException;

    /** Returns the sample rate in Hz. */
    int sampleRate();

    /** Returns the number of audio channels. */
    int channels();

    /** Returns the number of bytes per sample (per channel). */
    int sampleSize();

    /**
     * Returns the total number of frames, or {@code -1} when unknown (e.g. for
     * unbounded streams).
     */
    long totalFrames();

    /** Returns the duration in whole seconds, or {@code 0} when unknown. */
    int duration();

    /** Closes underlying resources. */
    @Override
    void close() throws IOException;

    /** Returns the size in bytes of a single frame (all channels). */
    default int frameSize() {
        return channels() * sampleSize();
    }

    /**
     * Converts little-endian samples to big-endian ("audio samples" as sent on the wire).
     * Samples are swapped per {@code sampleSize} bytes; single-byte samples pass through
     * unchanged.
     */
    static byte[] toAudioSamples(byte[] data, int sampleSize) {
        if (sampleSize <= 1 || data.length == 0) {
            return data;
        }
        byte[] output = new byte[data.length];
        int whole = data.length - data.length % sampleSize;
        for (int i = 0; i < whole; i += sampleSize) {
            for (int j = 0; j < sampleSize; j++) {
                output[i + j] = data[i + sampleSize - 1 - j];
            }
        }
        // Copy any trailing partial sample unchanged (should not happen in practice)
        System.arraycopy(data, whole, output, whole, data.length - whole);
        return output;
    }
}
