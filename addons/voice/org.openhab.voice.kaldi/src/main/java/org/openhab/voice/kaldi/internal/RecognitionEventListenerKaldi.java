/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.kaldi.internal;

import java.util.List;

import org.eclipse.smarthome.core.voice.RecognitionStopEvent;
import org.eclipse.smarthome.core.voice.STTEvent;
import org.eclipse.smarthome.core.voice.STTListener;
import org.eclipse.smarthome.core.voice.SpeechRecognitionErrorEvent;
import org.eclipse.smarthome.core.voice.SpeechRecognitionEvent;

import ee.ioc.phon.netspeechapi.duplex.RecognitionEvent;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEventListener;

/**
 * A RecognitionEventListener forwarding RecognitionEvent's to STTEvent's
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class RecognitionEventListenerKaldi implements RecognitionEventListener {

    /**
     * Target for forwarded events
     */
    private final STTListener sttListener;

    /**
     * A RecognitionEventListener that forwards RecognitionEvent's to STTEvent's
     *
     * The target of the STTEvent's is the passed STTListener
     *
     * @param sttListener The targeted STTListener
     */
    public RecognitionEventListenerKaldi(STTListener sttListener) {
        this.sttListener = sttListener;
    }

    /**
     * Target of RecognitionEvent events that are forwarded to the contained STTListener
     *
     * @param recognitionEvent The fired RecognitionEvent
     */
    @Override
    public void onRecognitionEvent(RecognitionEvent recognitionEvent) {
        int status = recognitionEvent.getStatus();
        switch (status) {
            case RecognitionEvent.STATUS_SUCCESS:
                RecognitionEvent.Result result = recognitionEvent.getResult();
                if (result.isFinal()) {
                    sttListener.sttEventReceived(getSTTEvent(recognitionEvent));
                }
                break;
            case RecognitionEvent.STATUS_NO_SPEECH:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("No speech"));
                break;
            case RecognitionEvent.STATUS_ABORTED:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Aborted"));
                break;
            case RecognitionEvent.STATUS_AUDIO_CAPTURE:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Error with audio capture"));
                break;
            case RecognitionEvent.STATUS_NETWORK:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Network error"));
                break;
            case RecognitionEvent.STATUS_NOT_ALLOWED:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Not allowed"));
                break;
            case RecognitionEvent.STATUS_SERVICE_NOT_ALLOWED:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Service not allowed"));
                break;
            case RecognitionEvent.STATUS_BAD_GRAMMAR:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Grammar invalid"));
                break;
            case RecognitionEvent.STATUS_LANGUAGE_NOT_SUPPORTED:
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Language not supported"));
                break;
        }
    }

    /**
     * Utility method to create a STTEvent from a successful, final RecognitionEvent
     *
     * @param recognitionEvent The successful, final RecognitionEvent
     * @return A STTEvent created from the passed RecognitionEvent
     */
    private STTEvent getSTTEvent(RecognitionEvent recognitionEvent) {
        RecognitionEvent.Result result = recognitionEvent.getResult();
        List<RecognitionEvent.Hypothesis> hypotheses = result.getHypotheses();

        float confidence = -1.0f;
        String transcript = new String();
        for (RecognitionEvent.Hypothesis hypothesis : hypotheses) {
            if (confidence < hypothesis.getConfidence()) {
                confidence = hypothesis.getConfidence();
                transcript = hypothesis.getTranscript();
            }
        }
        return new SpeechRecognitionEvent(transcript, confidence);
    }

    /**
     * Called when the WebSocket is closed
     */
    @Override
    public void onClose() {
        sttListener.sttEventReceived(new RecognitionStopEvent());
    }
}
