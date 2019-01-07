/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal.protocol;

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
