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
package org.openhab.voice.whisperstt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WhisperSTTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez Díez - Initial contribution
 */
@NonNullByDefault
public class WhisperSTTConfiguration {

    /**
     * Model name without '.bin' extension.
     */
    public String modelName = "";
    /**
     * Keep model loaded.
     */
    public boolean preloadModel;
    /**
     * Audio process keep ms.
     */
    public int keepMs = 0;
    /**
     * Audio process step seconds.
     */
    public int stepSeconds = 5;
    /**
     * Audio process length seconds.
     */
    public int lengthSeconds = 10;
    /**
     * Max seconds to wait to force stop the transcription.
     */
    public int maxSeconds = 20;
    /**
     * Use voice activity detection.
     */
    public boolean useVAD = true;
    /**
     * Voice activity detection auto-correlation threshold.
     */
    public float vadThreshold = 0.01f;
    /**
     * Max silence seconds for triggering transcription.
     */
    public int vadMaxSilenceSeconds = 2;
    /**
     * Number of threads used by whisper.
     */
    public int threads;
    /**
     * Overwrite the audio context size (0 = use default).
     */
    public int audioContextSize = 512;
    /**
     * Speed up audio by x2 (reduced accuracy).
     */
    public boolean speedUp;
    /**
     * Sampling strategy.
     */
    public String samplingStrategy = "GREEDY";
    /**
     * Beam Size configuration for sampling strategy Bean Search.
     */
    public int beamSize = 2;
    /**
     * Best Of configuration for sampling strategy Greedy.
     */
    public int greedyBestOf = -1;
    /**
     * Temperature threshold.
     */
    public float temperature;
    /**
     * Single phrase mode.
     */
    public boolean singleUtteranceMode = true;
    /**
     * Remove some characters from the transcription: ",", ".", "¿", "?", "¡", "!".
     */
    public boolean removeSpecials = true;
    /**
     * Message to be told when no results.
     */
    public String noResultsMessage = "Sorry, I didn't understand you";
    /**
     * Message to be told when an error has happened.
     */
    public String errorMessage = "Sorry, something went wrong";
    /**
     * Create wav audio file for each transcription call.
     */
    public boolean createWAVFile;
}
