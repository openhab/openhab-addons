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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Audio source playing a local WAV file in its native format.
 *
 * <p>
 * The file is read via {@link AudioSystem} without sample-rate/channel conversion, and
 * samples are swapped to big-endian on read per the {@link AudioSource} contract. Use
 * {@link JavaSoundAudioSource} when conversion to a specific output format is needed.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class WavFileAudioSource implements AudioSource {

    private final AudioInputStream stream;
    private final int sampleRate;
    private final int channels;
    private final int sampleSize;
    private final long totalFrames;
    private final boolean bigEndian;
    private boolean exhausted;

    /**
     * Opens a WAV file.
     *
     * @throws NotSupportedError if the file is not a readable WAV/PCM file
     */
    public WavFileAudioSource(File file) throws IOException {
        try {
            this.stream = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException e) {
            throw new NotSupportedError("unsupported audio file: " + file, e);
        }
        this.sampleRate = Math.round(stream.getFormat().getSampleRate());
        this.channels = stream.getFormat().getChannels();
        this.sampleSize = stream.getFormat().getSampleSizeInBits() / 8;
        this.totalFrames = stream.getFrameLength() < 0 ? -1 : stream.getFrameLength();
        this.bigEndian = stream.getFormat().isBigEndian();
    }

    @Override
    public byte[] readFrames(int nframes) throws IOException {
        if (exhausted) {
            return NO_FRAMES;
        }
        byte[] data = stream.readNBytes(nframes * frameSize());
        if (data.length == 0) {
            exhausted = true;
            return NO_FRAMES;
        }
        if (data.length < nframes * frameSize()) {
            exhausted = true;
        }
        return bigEndian ? data : AudioSource.toAudioSamples(data, sampleSize);
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
