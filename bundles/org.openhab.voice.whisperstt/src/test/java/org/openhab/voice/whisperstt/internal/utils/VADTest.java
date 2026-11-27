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
import java.util.Arrays;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import io.github.jvoiceproject.silerovad.SileroVAD;

/**
 * Tests for {@link VAD}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class VADTest {
    private static final int STEP = 16000;
    private static final int SHORT_STEP = 1600;

    @Test
    public void detectsSpeechButNotNoise() throws IOException {
        short[] speech = speechSamples();
        VAD vad = new VAD(0.3f, 0.5f);
        VAD.VADResult speechResult = vad.analyze(step(speech, 0));
        // let the clip end before the noise, as the model holds voice for a few frames after speech is cut off
        vad.analyze(step(speech, STEP));
        VAD.VADResult noiseResult = vad.analyze(noise());

        assertTrue(speechResult.isVoice());
        assertTrue(speechResult.voiceSamplesInTail() > 0);
        assertFalse(noiseResult.isVoice());
        assertEquals(0, noiseResult.voiceSamplesInTail());
    }

    @Test
    public void shortStepsDetectSpeechButNotSilence() throws IOException {
        short[] speech = speechSamples();
        VAD vad = new VAD(0.3f, 0.5f);
        boolean voiceFound = false;
        for (int offset = 0; offset + SHORT_STEP <= speech.length; offset += SHORT_STEP) {
            voiceFound |= vad.analyze(Arrays.copyOfRange(speech, offset, offset + SHORT_STEP)).isVoice();
        }
        assertTrue(voiceFound);
        for (int i = 0; i < 20; i++) {
            assertFalse(vad.analyze(new short[SHORT_STEP]).isVoice(), "step " + i);
        }
    }

    @Test
    public void voiceBoundariesIncludeCarriedAndPendingSamples() throws IOException {
        short[] speech = speechSamples();
        int frameSize = SileroVAD.FRAME_SIZE;
        // reference speech flags of each frame over the whole clip
        SileroVAD silero = new SileroVAD();
        boolean[] isVoice = new boolean[speech.length / frameSize];
        for (int i = 0; i < isVoice.length; i++) {
            isVoice[i] = silero.process(Arrays.copyOfRange(speech, i * frameSize, (i + 1) * frameSize)) >= 0.5f;
        }
        VAD vad = new VAD(0.3f, 0.5f);
        for (int start = 0; start + SHORT_STEP <= speech.length; start += SHORT_STEP) {
            int end = start + SHORT_STEP;
            VAD.VADResult result = vad.analyze(Arrays.copyOfRange(speech, start, end));
            // frames completed by this step, the first one may start in the previous step
            int first = start / frameSize;
            int last = end / frameSize - 1;
            int headEnd = first;
            while (headEnd <= last && isVoice[headEnd]) {
                headEnd++;
            }
            int expectedHead = headEnd > last ? SHORT_STEP : Math.max(0, headEnd * frameSize - start);
            int tailStart = last;
            while (tailStart >= first && isVoice[tailStart]) {
                tailStart--;
            }
            int expectedTail = tailStart == last ? 0 : Math.min(SHORT_STEP, end - (tailStart + 1) * frameSize);
            assertEquals(expectedHead, result.voiceSamplesInHead(), "head at " + start);
            assertEquals(expectedTail, result.voiceSamplesInTail(), "tail at " + start);
        }
    }

    private static short[] step(short[] samples, int offset) {
        short[] step = new short[STEP];
        System.arraycopy(samples, offset, step, 0, Math.min(samples.length - offset, STEP));
        return step;
    }

    private static short[] noise() {
        Random random = new Random(1);
        short[] step = new short[STEP];
        for (int i = 0; i < step.length; i++) {
            step[i] = (short) (random.nextGaussian() * 1500);
        }
        return step;
    }

    /**
     * "Turn on the kitchen lights." synthesized by Piper TTS with the en_US-ljspeech-medium voice (MIT, trained on
     * the public domain LJ Speech dataset), resampled to 16 kHz.
     */
    public static short[] speechSamples() throws IOException {
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

    private static byte[] bytes(String name) throws IOException {
        try (InputStream in = VADTest.class.getResourceAsStream(name)) {
            assertNotNull(in, name);
            return in.readAllBytes();
        }
    }
}
