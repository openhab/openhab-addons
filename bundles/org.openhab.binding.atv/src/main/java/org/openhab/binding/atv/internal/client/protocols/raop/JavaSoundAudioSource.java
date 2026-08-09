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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Audio source decoding a file or stream via Java Sound ({@code javax.sound.sampled}).
 *
 * <p>
 * The input is decoded with {@link AudioSystem} and converted to the requested
 * signed-PCM format (typically 44100 Hz / 16 bit / 2 channels) when the installed
 * providers support it.
 *
 * <p>
 * Only PCM/WAV (and other formats bundled with the JDK, e.g. AIFF/AU) are guaranteed
 * to work. Additional SPI decoders (e.g. mp3spi/vorbisspi) are picked up automatically
 * by {@link AudioSystem} if present on the classpath, but none are shipped as
 * dependencies of this project.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class JavaSoundAudioSource implements AudioSource {

    private final AudioInputStream pcmStream;
    private final int sampleRate;
    private final int channels;
    private final int sampleSize;
    private final long totalFrames;
    private boolean exhausted;

    private JavaSoundAudioSource(AudioInputStream pcmStream) {
        AudioFormat format = pcmStream.getFormat();
        this.pcmStream = pcmStream;
        this.sampleRate = Math.round(format.getSampleRate());
        this.channels = format.getChannels();
        this.sampleSize = format.getSampleSizeInBits() / 8;
        this.totalFrames = pcmStream.getFrameLength() < 0 ? -1 : pcmStream.getFrameLength();
    }

    /**
     * Opens a file and converts it to the requested format.
     *
     * @param sampleRate target sample rate in Hz (e.g. 44100)
     * @param channels target channel count (e.g. 2)
     * @param sampleSize target bytes per sample (e.g. 2 for 16-bit)
     * @throws NotSupportedError if the file format or the conversion is unsupported
     */
    public static JavaSoundAudioSource open(File file, int sampleRate, int channels, int sampleSize)
            throws IOException {
        @Nullable
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(JavaSoundAudioSource.class.getClassLoader());
        try {
            return open(AudioSystem.getAudioInputStream(file), sampleRate, channels, sampleSize);
        } catch (UnsupportedAudioFileException e) {
            throw new NotSupportedError("unsupported audio file: " + file, e);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    /**
     * Opens a stream and converts it to the requested format. The stream must support
     * mark/reset for format detection; wrap in a {@link BufferedInputStream} otherwise.
     *
     * @throws NotSupportedError if the stream format or the conversion is unsupported
     */
    public static JavaSoundAudioSource open(InputStream stream, int sampleRate, int channels, int sampleSize)
            throws IOException {
        InputStream marked = stream.markSupported() ? stream : new BufferedInputStream(stream);
        @Nullable
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(JavaSoundAudioSource.class.getClassLoader());
        try {
            return open(AudioSystem.getAudioInputStream(marked), sampleRate, channels, sampleSize);
        } catch (UnsupportedAudioFileException e) {
            throw new NotSupportedError("unsupported audio stream", e);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    /** Converts an already-open {@link AudioInputStream} to the requested format. */
    public static JavaSoundAudioSource open(AudioInputStream input, int sampleRate, int channels, int sampleSize) {
        // Big-endian target so no byte swapping is needed in readFrames
        AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSize * 8, channels,
                sampleSize * channels, sampleRate, true);
        if (input.getFormat().matches(target)) {
            return new JavaSoundAudioSource(input);
        }
        if (!AudioSystem.isConversionSupported(target, input.getFormat())) {
            throw new NotSupportedError("conversion to " + target + " not supported from " + input.getFormat());
        }
        return new JavaSoundAudioSource(AudioSystem.getAudioInputStream(target, input));
    }

    @Override
    public byte[] readFrames(int nframes) throws IOException {
        if (exhausted) {
            return NO_FRAMES;
        }
        byte[] data = pcmStream.readNBytes(nframes * frameSize());
        if (data.length == 0) {
            exhausted = true;
            return NO_FRAMES;
        }
        if (data.length < nframes * frameSize()) {
            exhausted = true;
        }
        // Already big-endian (see target format in open); no swap needed
        return data;
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
        pcmStream.close();
    }
}
