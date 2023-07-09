/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import io.github.givimad.libfvadjni.VoiceActivityDetector;

/**
 * The {@link VAD} a basic voice activity detector.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class VAD implements AutoCloseable {
    private final VoiceActivityDetector libfvad;

    private final short[] frameSamples;
    private final int detectionsPerRun;
    private final int requiredDetections;

    private final Logger logger = LoggerFactory.getLogger(VAD.class);

    public VAD(VoiceActivityDetector.Mode mode, int sampleRate, int stepMs, float sensitivity) throws IOException {
        this.libfvad = VoiceActivityDetector.newInstance();
        this.libfvad.setMode(mode);
        this.libfvad.setSampleRate(VoiceActivityDetector.SampleRate.fromValue(sampleRate));
        this.frameSamples = new short[sampleRate / 1000 * stepMs];
        detectionsPerRun = (sampleRate / frameSamples.length);
        requiredDetections = (int) ((((float) detectionsPerRun) / 100f) * (sensitivity * 100));
    }

    public boolean isVoice(short[] samples) throws IOException {
        int partialVADCounter = 0;
        for (int i = 0; i < detectionsPerRun; i++) {
            System.arraycopy(samples, i * frameSamples.length, frameSamples, 0, frameSamples.length);
            if (libfvad.process(frameSamples, frameSamples.length)) {
                partialVADCounter++;
            }
        }
        logger.debug("VAD: {}/{} - required: {}", partialVADCounter, detectionsPerRun, requiredDetections);
        return partialVADCounter >= requiredDetections;
    }

    @Override
    public void close() {
        libfvad.close();
    }
}
