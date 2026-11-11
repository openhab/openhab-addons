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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jvoiceproject.silerovad.SileroVAD;

/**
 * The {@link VAD} class is a voice activity detector implementation over {@link SileroVAD}.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Dan Cunningham - Replace libfvad with Silero
 */
@NonNullByDefault
public class VAD {
    private final Logger logger = LoggerFactory.getLogger(VAD.class);
    private final SileroVAD silero = new SileroVAD();
    private final short[] stepSamples = new short[SileroVAD.FRAME_SIZE];
    private final float sensitivity;
    private final float threshold;
    private int stepSamplesFilled;

    /**
     * @param sensitivity percent in range 0 - 1 of voice frames in each analyzed step to consider it as voice.
     * @param threshold Silero speech probability in range 0 - 1 to consider a frame as voice.
     * @throws IOException if the Silero model can't be loaded
     */
    public VAD(float sensitivity, float threshold) throws IOException {
        this.sensitivity = sensitivity;
        this.threshold = threshold;
    }

    public VADResult analyze(short[] samples) {
        int voiceInHead = 0;
        int voiceInTail = 0;
        boolean silenceFound = false;
        int partialVADCounter = 0;
        int totalPartialDetections = 0;
        int offset = 0;
        // samples of the first frame that came with the previous call
        int carriedSamples = stepSamplesFilled;
        // frames can span calls, as the Silero frame size does not divide the audio step
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
        // require at least one voice frame, so short steps are not voice by default
        int detectionThreshold = Math.max(1, (int) ((((float) totalPartialDetections) / 100f) * (sensitivity * 100)));
        logger.debug("VAD: {}/{} - required: {}", partialVADCounter, totalPartialDetections, detectionThreshold);
        // samples left for the next call are added to a head or tail of voice
        int pendingSamples = stepSamplesFilled;
        int voiceSamplesInHead = 0;
        if (voiceInHead > 0) {
            voiceSamplesInHead = voiceInHead * stepSamples.length - carriedSamples
                    + (silenceFound ? 0 : pendingSamples);
        }
        int voiceSamplesInTail = voiceInTail > 0 ? voiceInTail * stepSamples.length + pendingSamples : 0;
        return new VADResult( //
                partialVADCounter >= detectionThreshold, //
                Math.max(0, Math.min(samples.length, voiceSamplesInHead)), //
                Math.min(samples.length, voiceSamplesInTail) //
        );
    }

    private boolean isVoice() {
        return silero.process(stepSamples) >= threshold;
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
