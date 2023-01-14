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
package org.openhab.voice.rustpotterks.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RustpotterKSConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class RustpotterKSConfiguration {
    /**
     * Configures the detector threshold, is the min score (in range 0. to 1.) that some wake word template should
     * obtain to trigger a detection. Defaults to 0.5.
     */
    public float threshold = 0.5f;
    /**
     * Configures the detector averaged threshold, is the min score (in range 0. to 1.) that the audio should obtain
     * against a
     * combination of the wake word templates, the detection will be aborted if this is not the case. This way it can
     * prevent to
     * run the comparison of the current frame against each of the wake word templates which saves cpu.
     * If set to 0 this functionality is disabled.
     */
    public float averagedThreshold = 0.2f;
    /**
     * Terminate the detection as son as one result is above the score,
     * instead of wait to see if the next frame has a higher score.
     */
    public boolean eagerMode = true;
    /**
     * Use build-in noise detection to reduce computation on absence of noise.
     * Configures the difficulty to consider a frame as noise (the required noise level).
     */
    public String noiseDetectionMode = "disabled";
    /**
     * Noise/silence ratio in the last second to consider noise is detected. Defaults to 0.5.
     */
    public float noiseSensitivity = 0.5f;
    /**
     * Seconds to disable the vad detector after voice is detected. Defaults to 3.
     */
    public int vadDelay = 3;
    /**
     * Voice/silence ratio in the last second to consider voice is detected.
     */
    public float vadSensitivity = 0.5f;
    /**
     * Use a voice activity detector to reduce computation in the absence of vocal sound.
     */
    public String vadMode = "disabled";
    /**
     * Configures the reference for the comparator used to match the samples.
     */
    public float comparatorRef = 0.22f;
    /**
     * Configures the band-size for the comparator used to match the samples.
     */
    public int comparatorBandSize = 6;
}
