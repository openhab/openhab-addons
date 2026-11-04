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

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jvoiceproject.libfvadjni.VoiceActivityDetector;

/**
 * The {@link VAD} class is a voice activity detector implementation over libfvad-jni or {@link SileroVAD}.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Dan Cunningham - Add Silero detection
 */
@NonNullByDefault
public class VAD implements AutoCloseable {
    public static final String SILERO_MODE = "SILERO";

    private final Logger logger = LoggerFactory.getLogger(VAD.class);
    private final @Nullable VoiceActivityDetector libfvad;
    private final @Nullable SileroVAD silero;
    private final short[] stepSamples;
    private final float sensitivity;
    private final float sileroThreshold;
    private int stepSamplesFilled;

    /**
     *
     * @param mode desired vad mode, a libfvad mode or {@link #SILERO_MODE}.
     * @param sampleRate audio sample rate.
     * @param stepMs libfvad partial step ms, Silero always uses 32 ms.
     * @param sensitivity detector sensitivity percent in range 0 - 1.
     * @param sileroThreshold Silero speech probability in range 0 - 1 to consider a frame as voice.
     * @throws IOException
     */
    public VAD(String mode, int sampleRate, int stepMs, float sensitivity, float sileroThreshold) throws IOException {
        if (SILERO_MODE.equals(mode)) {
            if (sampleRate != 16000) {
                throw new IOException("Silero VAD requires 16 kHz audio");
            }
            this.libfvad = null;
            this.silero = new SileroVAD();
            this.stepSamples = new short[SileroVAD.FRAME_SIZE];
        } else {
            VoiceActivityDetector libfvad = VoiceActivityDetector.newInstance();
            libfvad.setMode(VoiceActivityDetector.Mode.valueOf(mode));
            libfvad.setSampleRate(VoiceActivityDetector.SampleRate.fromValue(sampleRate));
            this.libfvad = libfvad;
            this.silero = null;
            this.stepSamples = new short[sampleRate / 1000 * stepMs];
        }
        this.sensitivity = sensitivity;
        this.sileroThreshold = sileroThreshold;
    }

    public VADResult analyze(short[] samples) throws IOException {
        int voiceInHead = 0;
        int voiceInTail = 0;
        boolean silenceFound = false;
        int partialVADCounter = 0;
        int totalPartialDetections = 0;
        int offset = 0;
        // partial steps can span calls, as the Silero frame size does not divide the audio step
        while (offset < samples.length) {
            int length = Math.min(stepSamples.length - stepSamplesFilled, samples.length - offset);
            System.arraycopy(samples, offset, stepSamples, stepSamplesFilled, length);
            offset += length;
            stepSamplesFilled += length;
            if (stepSamplesFilled < stepSamples.length) {
                break;
            }
            stepSamplesFilled = 0;
            totalPartialDetections++;
            if (isVoice()) {
                partialVADCounter++;
                if (!silenceFound) {
                    voiceInHead++;
                }
                voiceInTail++;
            } else {
                silenceFound = true;
                voiceInTail = 0;
            }
        }
        // require at least one voiced partial, so short steps are not voice by default
        int detectionThreshold = Math.max(1, (int) ((((float) totalPartialDetections) / 100f) * (sensitivity * 100)));
        logger.debug("VAD: {}/{} - required: {}", partialVADCounter, totalPartialDetections, detectionThreshold);
        return new VADResult( //
                partialVADCounter >= detectionThreshold, //
                voiceInHead * stepSamples.length, //
                voiceInTail * stepSamples.length //
        );
    }

    private boolean isVoice() throws IOException {
        SileroVAD silero = this.silero;
        if (silero != null) {
            return silero.process(stepSamples) >= sileroThreshold;
        }
        return Objects.requireNonNull(libfvad).process(stepSamples, stepSamples.length);
    }

    @Override
    public void close() {
        VoiceActivityDetector libfvad = this.libfvad;
        if (libfvad != null) {
            libfvad.close();
        }
    }

    /**
     * Voice activity detection result.
     *
     * @param isVoice Does the block contain enough voice
     * @param voiceSamplesInHead Number of samples consecutively reported as voice from the beginning of the chunk
     * @param voiceSamplesInTail Number of samples consecutively reported as voice from the end of the chunk
     */
    public record VADResult(boolean isVoice, int voiceSamplesInHead, int voiceSamplesInTail) {
    }
}
