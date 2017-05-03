/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.cmusphinx.internal;

import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.KSListener;
import org.eclipse.smarthome.core.voice.KSpottedEvent;
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
public class CMUSphinxRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(CMUSphinxRunnable.class);

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
    private volatile STTListener sttListener;

    /**
     * The KSListener notified of KSEvents
     */
    private volatile KSListener ksListener;

    /**
     * The keyword to spot for notifying the KSListener
     */
    private volatile String keyword;

    /**
     * The StreamSpeechRecognizer from CMU Sphinx
     */
    private final StreamSpeechRecognizer speechRecognizer;

    /**
     * Constructs an instance targeting the passed WsDuplexRecognitionSession
     *
     * @param speechRecognizer The StreamSpeechRecognizer from CMU Sphinx
     * @param audioStream The AudioSource data
     */
    public CMUSphinxRunnable(StreamSpeechRecognizer speechRecognizer, AudioStream audioStream) {
        this.isAborting = false;
        this.audioStream = audioStream;
        this.speechRecognizer = speechRecognizer;
    }

    /**
     * This method sends AudioSource data in the WsDuplexRecognitionSession
     */
    @Override
    public void run() {
        try {
            this.speechRecognizer.startRecognition(this.audioStream);

            SpeechResult result;
            while ((result = this.speechRecognizer.getResult()) != null) {
                String hypothesis = result.getHypothesis();

                if ("<unk>".equals(result.getHypothesis()) || result.getHypothesis().isEmpty()) {
                    logger.debug("Unknown or empty hypothesis");

                    // sttListener.sttEventReceived(
                    // new SpeechRecognitionErrorEvent("empty or unknown recognition hypothesis"));
                } else {
                    logger.debug("Hypothesis: {}", hypothesis);

                    // confidence doesn't seem to work yet
                    // float score = result.getResult().getBestToken().getAcousticScore();
                    // double confidence = result.getResult().getLogMath().logToLinear(score);
                    float confidence = 1;

                    if (hypothesis.equals(keyword) && this.ksListener != null) {
                        logger.info("Keyword recognized: {}, speak command now", hypothesis);

                        ksListener.ksEventReceived(new KSpottedEvent(new AudioSource() {
                            // KSpottedEvent API inconsistent
                            @Override
                            public Set<AudioFormat> getSupportedFormats() {
                                return null;
                            }

                            @Override
                            public String getLabel(Locale locale) {
                                return null;
                            }

                            @Override
                            public AudioStream getInputStream(AudioFormat format) throws AudioException {
                                return null;
                            }

                            @Override
                            public String getId() {
                                return null;
                            }
                        }));

                    } else {
                        if (sttListener == null) {
                            logger.warn("Ignoring CMU Sphinx STT result '{}' because no STTListener attached.",
                                    hypothesis);
                        } else {
                            logger.info("Command recognized: {}", hypothesis);
                            sttListener.sttEventReceived(new SpeechRecognitionEvent(hypothesis, confidence));
                        }
                    }

                }
                if (this.isAborting) {
                    logger.info("Aborting CMU Sphinx keyword spotting and/or speech-to-text");
                    break;
                }
            }
            this.speechRecognizer.stopRecognition();
            if (this.sttListener != null) {
                sttListener.sttEventReceived(new RecognitionStopEvent());
            }

        } catch (Exception e) {
            logger.error("Recognition error", e);
            e.printStackTrace();
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(e.toString()));
        }
    }

    public void setSTTListener(STTListener sttListener) {
        if (this.sttListener != null) {
            this.sttListener.sttEventReceived(new RecognitionStopEvent());
        }
        this.sttListener = sttListener;
        this.sttListener.sttEventReceived(new RecognitionStartEvent());
    }

    public void setKSListener(KSListener ksListener) {
        this.ksListener = ksListener;
        // this is to work around a logic flaw in the DialogProcessor
        if (this.ksListener instanceof STTListener) {
            ((STTListener) this.ksListener).sttEventReceived(new RecognitionStopEvent());
        }
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * This method initiates the process of aborting this thread
     */
    public void abort() {
        this.isAborting = true;
    }
}
