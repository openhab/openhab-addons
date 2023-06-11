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
package org.openhab.voice.googletts.internal.dto;

/**
 * Synthesizes speech synchronously: receive results after all text input has been processed.
 *
 * @author Wouter Born - Initial contribution
 */
public class SynthesizeSpeechRequest {

    /**
     * Required. The configuration of the synthesized audio.
     */
    private AudioConfig audioConfig = new AudioConfig();

    /**
     * Required. The Synthesizer requires either plain text or SSML as input.
     */
    private SynthesisInput input = new SynthesisInput();

    /**
     * Required. The desired voice of the synthesized audio.
     */
    private VoiceSelectionParams voice = new VoiceSelectionParams();

    public SynthesizeSpeechRequest() {
    }

    public SynthesizeSpeechRequest(AudioConfig audioConfig, SynthesisInput input, VoiceSelectionParams voice) {
        this.audioConfig = audioConfig;
        this.input = input;
        this.voice = voice;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public SynthesisInput getInput() {
        return input;
    }

    public VoiceSelectionParams getVoice() {
        return voice;
    }

    public void setAudioConfig(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public void setInput(SynthesisInput input) {
        this.input = input;
    }

    public void setVoice(VoiceSelectionParams voice) {
        this.voice = voice;
    }
}
