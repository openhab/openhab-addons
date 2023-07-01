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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VoiceActivityDetector} a basic voice activity detector.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class VoiceActivityDetector {

    private final float acThreshold;

    private final Logger logger = LoggerFactory.getLogger(VoiceActivityDetector.class);

    public VoiceActivityDetector(float acThreshold) {
        this.acThreshold = acThreshold;
    }

    public boolean runDetection(float[] samples, int offset, int length) {
        return getAutocorrelation(samples, offset, length) > acThreshold;
    }

    private float getAutocorrelation(float[] samples, int offset, int length) {
        float ac = 0.0f;
        for (int lag = 0; lag < length - offset; lag++) {
            float sum = 0.0f;
            for (int i = offset; i < (length - lag); i++) {
                sum += samples[i] * samples[i + lag];
            }
            ac += sum;
        }
        float result = (ac / (length - offset)) * 1000; // x1000 to avoid setting too small threshold
        logger.debug("VAD: autocorrelation {}", result);
        return result;
    }
}
