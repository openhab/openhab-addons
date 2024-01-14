/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The message returned to the client by the text.synthesize method.
 *
 * @author Wouter Born - Initial contribution
 */
public class SynthesizeSpeechResponse {

    /**
     * The audio data bytes encoded as specified in the request, including the header (For LINEAR16 audio, we include
     * the WAV header). Note: as with all bytes fields, protobuffers use a pure binary representation, whereas JSON
     * representations use base64.
     * 
     * A base64-encoded string.
     */
    private String audioContent;

    public String getAudioContent() {
        return audioContent;
    }

    public void setAudioContent(String audioContent) {
        this.audioContent = audioContent;
    }
}
