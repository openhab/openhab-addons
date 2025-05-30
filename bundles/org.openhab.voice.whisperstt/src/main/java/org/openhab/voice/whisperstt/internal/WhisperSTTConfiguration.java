/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.List;

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
     * Defines the audio step.
     */
    public float stepSeconds = 1f;
    /**
     * Min audio seconds to call whisper with.
     */
    public float minSeconds = 2f;
    /**
     * Max seconds to wait to force stop the transcription.
     */
    public int maxSeconds = 10;
    /**
     * Voice activity detection mode.
     */
    public String vadMode = VoiceActivityDetector.Mode.VERY_AGGRESSIVE.toString();
    /**
     * Voice activity detection sensitivity.
     */
    public float vadSensitivity = 0.3f;
    /**
     * Voice activity detection step in ms (vad dependency only allows 10, 20 or 30 ms steps).
     */
    public int vadStep = 20;
    /**
     * Initial silence seconds for discard transcription.
     */
    public float initSilenceSeconds = 3;
    /**
     * Max silence seconds for triggering transcription.
     */
    public float maxSilenceSeconds = 0.5f;
    /**
     * Remove silence frames.
     */
    public boolean removeSilence = true;
    /**
     * Number of threads used by whisper. (0 to use host max threads)
     */
    public int threads;
    /**
     * Overwrite the audio context size. (0 to use whisper default context size).
     */
    public int audioContext;
    /**
     * Speed up audio by x2 (reduced accuracy).
     */
    public boolean speedUp;
    /**
     * Sampling strategy.
     */
    public String samplingStrategy = "BEAN_SEARCH";
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
     * Initial whisper prompt
     */
    public String initialPrompt = "";
    /**
     * Grammar in GBNF format.
     */
    public List<String> grammarLines = List.of();
    /**
     * Enables grammar usage.
     */
    public boolean useGrammar = false;
    /**
     * Grammar penalty.
     */
    public float grammarPenalty = 100f;
    /**
     * Enables GPU usage. (built-in binaries do not support GPU usage)
     */
    public boolean useGPU = true;
    /**
     * OpenVINO device name
     */
    public String openvinoDevice = "CPU";
    /**
     * Single phrase mode.
     */
    public boolean singleUtteranceMode = true;
    /**
     * Message to be told when no results.
     */
    public String noResultsMessage = "Sorry, I didn't understand you";
    /**
     * Message to be told when an error has happened.
     */
    public String errorMessage = "Sorry, something went wrong";
    /**
     * Create wav audio record for each whisper invocation.
     */
    public boolean createWAVRecord;
    /**
     * Record sample format. Values: i16, f32.
     */
    public String recordSampleFormat = "i16";
    /**
     * Print whisper.cpp library logs as binding debug logs.
     */
    public boolean enableWhisperLog;
    /**
     * local to use embedded whisper or openaiapi to use an external API
     */
    public Mode mode = Mode.LOCAL;
    /**
     * If mode set to openaiapi, then use this URL
     */
    public String apiUrl = "https://api.openai.com/v1/audio/transcriptions";
    /**
     * if mode set to openaiapi, use this api key to access apiUrl
     */
    public String apiKey = "";
    /**
     * If specified, speed up recognition by avoiding auto-detection
     */
    public String language = "";
    /**
     * Model name (API only)
     */
    public String apiModelName = "whisper-1";

    public static enum Mode {
        LOCAL,
        API;
    }
}
