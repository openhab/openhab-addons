/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.cmusphinx.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * Timeout in seconds for a (non-keyword) hypothesis to be sent as a
     * {@code SpeechRecognitionEvent} before a the keyword has to be uttered again.
     */
    private static final int TIMEOUT = 10;

    /**
     * Boolean indicating if the thread is aborting
     */
    private volatile boolean isAborting;

    /**
     * Boolean indicating if a keyword has been spotted and
     * a SST result is now expected within the timeout period.
     */
    private AtomicBoolean isKeywordSpotted = new AtomicBoolean();

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
     * The scheduler for timeout events
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Constructs an instance targeting the passed StreamSpeechRecognizer and AudioStream
     *
     * @param speechRecognizer The StreamSpeechRecognizer from CMU Sphinx
     * @param audioStream The audio stream data
     */
    public CMUSphinxRunnable(StreamSpeechRecognizer speechRecognizer, AudioStream audioStream) {
        this.isAborting = false;
        this.audioStream = audioStream;
        this.speechRecognizer = speechRecognizer;
    }

    /**
     * This method processes CMU Sphinx results and sends keyword spotting
     * or STT events to the provided listeners.
     */
    @Override
    public void run() {
        try {
            this.speechRecognizer.startRecognition(this.audioStream);
            logger.info("CMU Sphinx: StreamSpeechRecognizer recognition started");

            SpeechResult result;
            ScheduledFuture<?> timeoutFuture = null;

            while ((result = this.speechRecognizer.getResult()) != null) {
                String hypothesis = result.getHypothesis();

                if ("<unk>".equals(hypothesis) || hypothesis.isEmpty()) {
                    logger.debug("Unknown or empty hypothesis");
                } else {
                    logger.debug("Hypothesis: {}", hypothesis);

                    // confidence doesn't seem to work yet
                    // float score = result.getResult().getBestToken().getAcousticScore();
                    // double confidence = result.getResult().getLogMath().logToLinear(score);
                    float confidence = 1;

                    if (hypothesis.equals(keyword) && this.ksListener != null) {
                        if (this.isKeywordSpotted.get() == true) {
                            logger.debug("Spotted keyword, but ignoring because already we're awaiting a command");
                            continue;
                        } else {
                            logger.info("Keyword recognized: {}, speak command now", hypothesis);

                            ksListener.ksEventReceived(new KSpottedEvent());

                            // Indicate we're now listening for a spoken command
                            if (this.sttListener != null) {
                                this.sttListener.sttEventReceived(new RecognitionStartEvent());
                            }
                            this.isKeywordSpotted.set(true);
                            AtomicBoolean isKeywordSpotted = this.isKeywordSpotted;
                            STTListener sttListener = this.sttListener;
                            Logger logger = this.logger;
                            timeoutFuture = this.scheduler.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    logger.info("Timeout reached after keyword spotted: dialog interrupted");
                                    isKeywordSpotted.set(false);
                                    sttListener.sttEventReceived(new RecognitionStopEvent());
                                }
                            }, TIMEOUT, TimeUnit.SECONDS);

                        }

                    } else if (timeoutFuture != null && !timeoutFuture.isDone()) {
                        // Non-keyword recognized during the timeout period

                        timeoutFuture.cancel(false);
                        this.isKeywordSpotted.set(false);

                        if (sttListener == null) {
                            logger.warn("Ignoring CMU Sphinx STT result '{}' because no STTListener attached.",
                                    hypothesis);
                        } else {
                            logger.info("Command recognized: {}", hypothesis);
                            sttListener.sttEventReceived(new SpeechRecognitionEvent(hypothesis, confidence));
                            sttListener.sttEventReceived(new RecognitionStopEvent());
                        }
                    }

                }
                if (this.isAborting) {
                    logger.info("Aborting CMU Sphinx keyword spotting and/or speech-to-text");
                    break;
                }
            }
            this.speechRecognizer.stopRecognition();
            logger.info("CMU Sphinx: StreamSpeechRecognizer recognition stopped");
            if (this.sttListener != null) {
                sttListener.sttEventReceived(new RecognitionStopEvent());
            }

        } catch (Exception e) {
            logger.error("Recognition error", e);
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(e.toString()));
        }
    }

    /**
     * Sets the {@code STTListener} to send speech-to-text events to
     *
     * @param sttListener the STT listener
     */
    public void setSTTListener(STTListener sttListener) {
        logger.debug("CMU Sphinx: STTListener set");
        if (this.sttListener != null) {
            this.sttListener.sttEventReceived(new RecognitionStopEvent());
        }
        this.sttListener = sttListener;
        this.sttListener.sttEventReceived(new RecognitionStartEvent());
    }

    /**
     * Sets the {@code KSListener} to send keyword spotting events to
     *
     * @param sttListener the keyword spotting listener
     */
    public void setKSListener(KSListener ksListener) {
        this.ksListener = ksListener;
        logger.debug("CMU Sphinx: KSListener set");
    }

    /**
     * Sets the keyword to spot
     *
     * @param keyword to spot
     */
    public void setKeyword(String keyword) {
        logger.debug("CMU Sphinx: Keyword set: {}", keyword);
        this.keyword = keyword;
    }

    /**
     * This method initiates the process of aborting this thread
     */
    public void abort() {
        this.isAborting = true;
    }
}
