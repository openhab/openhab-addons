/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.cmusphinx.internal;

import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.RecognitionStartEvent;
import org.eclipse.smarthome.core.voice.RecognitionStopEvent;
import org.eclipse.smarthome.core.voice.STTListener;
import org.eclipse.smarthome.core.voice.SpeechRecognitionErrorEvent;
import org.eclipse.smarthome.core.voice.SpeechRecognitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

/**
 * A Runnable that sends AudioStream data in a StreamSpeechRecognizer
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */
public class STTServiceCMUSphinxRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(STTServiceCMUSphinxRunnable.class);

    /**
     * Boolean indicating if the thread is aborting
     */
    private volatile boolean isAborting;

    /**
     * The source of audio data
     */
    private final AudioStream audioStream;

    /**
     * The STTListener notified of STTEvents
     */
    private final STTListener sttListener;

    /**
     * The WsDuplexRecognitionSession communication is over
     */
    private final StreamSpeechRecognizer speechRecognizer;

    /**
     * Constructs an instance targeting the passed WsDuplexRecognitionSession
     *
     * @param recognitionSession The WsDuplexRecognitionSession sesion
     * @param sttListener The STTListener targeted for STTEvents
     * @param audioStream The AudioSource data
     */
    public STTServiceCMUSphinxRunnable(StreamSpeechRecognizer speechRecognizer, STTListener sttListener,
            AudioStream audioStream) {
        this.isAborting = false;
        this.audioStream = audioStream;
        this.sttListener = sttListener;
        this.speechRecognizer = speechRecognizer;
    }

    /**
     * This method sends AudioSource data in the WsDuplexRecognitionSession
     */
    @Override
    public void run() {
        try {
            sttListener.sttEventReceived(new RecognitionStartEvent());
            this.speechRecognizer.startRecognition(this.audioStream);

            SpeechResult result;
            while ((result = this.speechRecognizer.getResult()) != null) {
                if ("<unk>".equals(result.getHypothesis())) {
                    sttListener.sttEventReceived(new SpeechRecognitionEvent(result.getHypothesis(), 0));
                } else {
                    // confidence doesn't seem to work yet
                    // float score = result.getResult().getBestToken().getAcousticScore();
                    // double confidence = result.getResult().getLogMath().logToLinear(score);
                    float confidence = 1;
                    sttListener.sttEventReceived(new SpeechRecognitionEvent(result.getHypothesis(), confidence));
                }
                // how to retrieve the confidence?
                if (this.isAborting) {
                    break;
                }
            }
            this.speechRecognizer.stopRecognition();
            sttListener.sttEventReceived(new RecognitionStopEvent());

        } catch (Exception e) {
            logger.error("Recognition error", e);
            e.printStackTrace();
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(e.toString()));
        }
    }

    /**
     * This method initiates the process of aborting this thread
     */
    public void abort() {
        this.isAborting = true;
    }
}
