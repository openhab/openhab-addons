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

import io.github.givimad.libfvadjni.VoiceActivityDetector;

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
     * Max seconds to wait to force stop the transcription.
     */
    public int maxSeconds = 20;
    /**
     * Voice activity detection mode.
     */
    public String vadMode = VoiceActivityDetector.Mode.AGGRESSIVE.toString();
    /**
     * Voice activity detection sensitivity.
     */
    public float vadSensitivity = 0.25f;
    /**
     * Voice activity detection step in ms (vad dependency only allows 10, 20 or 30 ms steps).
     */
    public int vadStep = 20;
    /**
     * Initial silence seconds for discard transcription.
     */
    public int initSilenceSeconds = 3;
    /**
     * Max silence seconds for triggering transcription.
     */
    public int maxSilenceSeconds = 2;
    /**
     * Remove silence frames.
     */
    public boolean removeSilence = true;
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
