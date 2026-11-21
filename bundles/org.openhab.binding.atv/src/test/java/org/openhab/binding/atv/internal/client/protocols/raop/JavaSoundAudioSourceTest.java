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

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link JavaSoundAudioSource} delivers audio at the RAOP format (44.1 kHz, 16-bit stereo) with the
 * right duration, whatever the source rate and channel count. A source that is relabeled instead of resampled
 * comes out with the wrong number of bytes and plays at the wrong speed.
 *
 * <p>
 * The MP3 fixtures are one second of a 440 Hz sine, encoded with ffmpeg.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class JavaSoundAudioSourceTest {

    private static final int RATE = 44100;
    private static final int CHANNELS = 2;
    private static final int SAMPLE_SIZE = 2;
    private static final int BYTES_PER_SECOND = RATE * CHANNELS * SAMPLE_SIZE;

    @Test
    void mp3At24kHzMonoIsResampled() throws IOException {
        byte[] pcm = decodeResource("sine-24k-mono.mp3");
        assertAboutOneSecond(pcm);
        assertPitchAbout440Hz(pcm);
    }

    @Test
    void mp3At44kHzStereoPassesThrough() throws IOException {
        byte[] pcm = decodeResource("sine-44k-stereo.mp3");
        assertAboutOneSecond(pcm);
        assertPitchAbout440Hz(pcm);
    }

    @Test
    void wavAt22kHzMonoIsResampled() throws IOException {
        AudioFormat source = new AudioFormat(22050, 16, 1, true, false);
        byte[] pcm = new byte[22050 * 2];
        ByteArrayOutputStream wav = new ByteArrayOutputStream();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(pcm), source, 22050), AudioFileFormat.Type.WAVE,
                wav);
        assertAboutOneSecond(decode(new ByteArrayInputStream(wav.toByteArray())));
    }

    private byte[] decodeResource(String name) throws IOException {
        try (InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(name), name)) {
            return decode(new BufferedInputStream(in));
        }
    }

    private static byte[] decode(InputStream in) throws IOException {
        try (JavaSoundAudioSource source = JavaSoundAudioSource.open(in, RATE, CHANNELS, SAMPLE_SIZE)) {
            assertEquals(RATE, source.sampleRate());
            assertEquals(CHANNELS, source.channels());
            assertEquals(SAMPLE_SIZE, source.sampleSize());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] frames;
            while ((frames = source.readFrames(352)).length > 0) {
                out.write(frames);
            }
            return out.toByteArray();
        }
    }

    /** Allows for MP3 encoder delay and padding, which add a few tens of milliseconds. */
    private static void assertAboutOneSecond(byte[] pcm) {
        assertTrue(pcm.length >= BYTES_PER_SECOND * 0.95 && pcm.length <= BYTES_PER_SECOND * 1.15,
                "expected about " + BYTES_PER_SECOND + " bytes (1 s), got " + pcm.length);
    }

    /** Estimates the tone from zero crossings on the left channel (16-bit big-endian samples). */
    private static void assertPitchAbout440Hz(byte[] pcm) {
        int frameSize = CHANNELS * SAMPLE_SIZE;
        int frames = pcm.length / frameSize;
        int crossings = 0;
        int previous = 0;
        for (int i = 0; i < frames; i++) {
            int sample = (short) (((pcm[i * frameSize] & 0xFF) << 8) | (pcm[i * frameSize + 1] & 0xFF));
            if (i > 0 && (previous < 0) != (sample < 0)) {
                crossings++;
            }
            previous = sample;
        }
        double hz = crossings / 2.0 / ((double) frames / RATE);
        assertEquals(440, hz, 25, "tone should stay at 440 Hz");
    }
}
