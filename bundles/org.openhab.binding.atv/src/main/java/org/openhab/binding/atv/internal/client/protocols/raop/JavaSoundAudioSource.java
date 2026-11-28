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
 * signed-PCM format (typically 44100 Hz / 16 bit / 2 channels), resampling and
 * mixing channels as needed.
 *
 * <p>
 * WAV, AIFF and AU are handled by the JDK. MP3 is decoded by the embedded mp3spi
 * provider. The {@code open} methods set the thread context class loader to this
 * bundle's loader so {@link AudioSystem} can find that provider under OSGi.
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
        AudioInputStream pcm = decodeToPcm(input);
        if (pcm.getFormat().matches(target)) {
            return new JavaSoundAudioSource(pcm);
        }
        return new JavaSoundAudioSource(convert(target, pcm));
    }

    /**
     * Decodes a compressed stream (e.g. MP3) to PCM at its own sample rate and channel count.
     *
     * <p>
     * This has to be a separate step. mp3spi also claims it can decode straight to another sample rate, but it
     * only relabels the stream without resampling, so 24 kHz audio would play at 44.1 kHz speed. Once the audio is
     * PCM, the JDK's own converter does the resampling and channel mixing properly.
     */
    private static AudioInputStream decodeToPcm(AudioInputStream input) {
        AudioFormat source = input.getFormat();
        AudioFormat.Encoding encoding = source.getEncoding();
        if (AudioFormat.Encoding.PCM_SIGNED.equals(encoding) || AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) {
            return input;
        }
        AudioFormat decoded = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getSampleRate(), 16,
                source.getChannels(), source.getChannels() * 2, source.getSampleRate(), true);
        return convert(decoded, input);
    }

    private static AudioInputStream convert(AudioFormat target, AudioInputStream input) {
        if (!AudioSystem.isConversionSupported(target, input.getFormat())) {
            throw new NotSupportedError("conversion to " + target + " not supported from " + input.getFormat());
        }
        return AudioSystem.getAudioInputStream(target, input);
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
