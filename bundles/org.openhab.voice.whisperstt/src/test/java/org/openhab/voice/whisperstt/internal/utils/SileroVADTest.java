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
package org.openhab.voice.whisperstt.internal.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SileroVAD}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SileroVADTest {

    @Test
    public void matchesOnnxModel() throws IOException {
        short[] audio = withTrailingSilence(speechSamples(), 8000);
        String[] expected = resource("/speech-silero-probs.txt").trim().split("\n");
        SileroVAD vad = new SileroVAD();

        assertEquals(expected.length, audio.length / SileroVAD.FRAME_SIZE);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(Float.parseFloat(expected[i]), vad.process(frame(audio, i)), 1e-4, "frame " + i);
        }
    }

    @Test
    public void noiseIsNotSpeech() throws IOException {
        Random random = new Random(1);
        short[] audio = new short[16000];
        for (int i = 0; i < audio.length; i++) {
            audio[i] = (short) (random.nextGaussian() * 1500);
        }
        SileroVAD vad = new SileroVAD();
        for (int i = 0; i < audio.length / SileroVAD.FRAME_SIZE; i++) {
            assertTrue(vad.process(frame(audio, i)) < 0.5f, "frame " + i);
        }
    }

    private static short[] frame(short[] audio, int index) {
        short[] frame = new short[SileroVAD.FRAME_SIZE];
        System.arraycopy(audio, index * SileroVAD.FRAME_SIZE, frame, 0, frame.length);
        return frame;
    }

    private static short[] withTrailingSilence(short[] samples, int silence) {
        short[] audio = new short[samples.length + silence];
        System.arraycopy(samples, 0, audio, 0, samples.length);
        return audio;
    }

    /**
     * "Turn on the kitchen lights." synthesized by Piper TTS with the en_US-ljspeech-medium voice (MIT, trained on
     * the public domain LJ Speech dataset), resampled to 16 kHz.
     * speech-silero-probs.txt holds the official ONNX model output for this clip followed by 8000 silent samples.
     */
    static short[] speechSamples() throws IOException {
        byte[] wav = bytes("/speech.wav");
        ByteBuffer buffer = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);
        int offset = 12;
        while (!"data".equals(new String(wav, offset, 4, StandardCharsets.US_ASCII))) {
            offset += 8 + buffer.getInt(offset + 4);
        }
        short[] samples = new short[buffer.getInt(offset + 4) / 2];
        buffer.position(offset + 8);
        buffer.asShortBuffer().get(samples);
        return samples;
    }

    private static String resource(String name) throws IOException {
        return new String(bytes(name), StandardCharsets.UTF_8);
    }

    private static byte[] bytes(String name) throws IOException {
        try (InputStream in = SileroVADTest.class.getResourceAsStream(name)) {
            assertNotNull(in, name);
            return in.readAllBytes();
        }
    }
}
