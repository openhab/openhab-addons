/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.kaldi.internal;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.RecognitionStartEvent;
import org.eclipse.smarthome.core.voice.STTListener;
import org.eclipse.smarthome.core.voice.SpeechRecognitionErrorEvent;

import ee.ioc.phon.netspeechapi.duplex.RecognitionEvent;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEventListener;
import ee.ioc.phon.netspeechapi.duplex.WsDuplexRecognitionSession;

/**
 * A Runnable that sends AudioStream data in a WsDuplexRecognitionSession
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class STTServiceKaldiRunnable implements Runnable, RecognitionEventListener {

    /**
     * Boolean indicating if the server closed the connection
     */
    private volatile boolean isClosed;

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
    private final WsDuplexRecognitionSession recognitionSession;

    /**
     * Constructs an instance targeting the passed WsDuplexRecognitionSession
     *
     * @param recognitionSession The WsDuplexRecognitionSession sesion
     * @param sttListener The STTListener targeted for STTEvents
     * @param audioStream The AudioSource data
     */
    public STTServiceKaldiRunnable(WsDuplexRecognitionSession recognitionSession, STTListener sttListener,
            AudioStream audioStream) {
        this.isClosed = false;
        this.isAborting = false;
        this.audioStream = audioStream;
        this.sttListener = sttListener;
        this.recognitionSession = recognitionSession;

        this.recognitionSession.addRecognitionEventListener(this);
    }

    /**
     * This method sends AudioSource data in the WsDuplexRecognitionSession
     */
    @Override
    public void run() {
        try {
            this.recognitionSession.connect();
            AudioFormat audioFormat = this.audioStream.getFormat();
            int bitRate = audioFormat.getBitRate().intValue();
            int byteRate = (bitRate / 8);
            int chunkRate = 4; // 4 <= chunkRate [See: http://bit.ly/1V4Ktw2]
            byte buffer[] = new byte[byteRate / chunkRate];

            sttListener.sttEventReceived(new RecognitionStartEvent());

            boolean sentLastChunk = false;
            while (!this.isAborting && !this.isClosed) {
                long millisWithinChunkSecond = System.currentTimeMillis() % (1000 / chunkRate);
                int size = audioStream.read(buffer);
                if (size < 0) {
                    sentLastChunk = true;
                    byte buffer2[] = new byte[0];
                    this.recognitionSession.sendChunk(buffer2, true);
                    break;
                }
                if (size == (byteRate / chunkRate)) {
                    this.recognitionSession.sendChunk(buffer, false);
                } else {
                    sentLastChunk = true;
                    byte buffer2[] = Arrays.copyOf(buffer, size);
                    this.recognitionSession.sendChunk(buffer2, true);
                    break;
                }
                Thread.sleep(1000 / chunkRate - millisWithinChunkSecond);
            }

            if (this.isAborting && !this.isClosed && !sentLastChunk) {
                byte buffer2[] = new byte[0];
                this.recognitionSession.sendChunk(buffer2, true);
            }
        } catch (IOException e) {
            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Unable to send audio data to the server"));
        } catch (InterruptedException e) {
            sttListener.sttEventReceived(
                    new SpeechRecognitionErrorEvent("Unable to send data to the server at the proper rate"));
        } catch (RuntimeException e) {
            // Note: This is a workaround for a bug in net-speech-api and Java-WebSocket.
            //
            // The problem is RecognitionEventListener's onClose() are only called
            // after the connection is closed. Thus, the loop above does not know
            // when to stop sending data and may try to send data on a session that
            // is closed.
            //
            // A possible solution would be to have WsDuplexRecognitionSession call
            // RecognitionEventListener's onClose() methods in an override of the
            // method onCloseInitiated() of WebSocketClient. However, this also
            // doesn't work as this method is never called before or after a session
            // is closed.
            //
            // This temporary, but working, solution is to catch a RuntimeException
            // here and assume that it results from sendChunk() being called on a
            // closed session then proceede as if onClose() was called.
        }
    }

    /**
     * This method initiates the process of aborting this thread
     */
    public void abort() {
        this.isAborting = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRecognitionEvent(RecognitionEvent recognitionEvent) {
        // RecognitionEvent are ignored
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
        this.isClosed = true;
    }
}
