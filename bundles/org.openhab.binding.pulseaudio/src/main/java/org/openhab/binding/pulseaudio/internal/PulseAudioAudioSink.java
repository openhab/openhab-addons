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
package org.openhab.binding.pulseaudio.internal;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The audio sink for openhab, implemented by a connection to a pulseaudio sink
 *
 * @author Gwendal Roulleau - Initial contribution
 * @author Miguel Álvarez - move some code to the PulseaudioSimpleProtocolStream class so sink and source can extend
 *         from it.
 *
 */
@NonNullByDefault
public class PulseAudioAudioSink extends PulseaudioSimpleProtocolStream implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(PulseAudioAudioSink.class);

    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    static {
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_FORMATS.add(AudioFormat.MP3);
        SUPPORTED_STREAMS.add(FixedLengthAudioStream.class);
    }

    public PulseAudioAudioSink(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        super(pulseaudioHandler, scheduler);
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            return;
        }
        addClientCount();
        try (ConvertedInputStream normalizedPCMStream = new ConvertedInputStream(audioStream)) {
            for (int countAttempt = 1; countAttempt <= 2; countAttempt++) { // two attempts allowed
                try {
                    connectIfNeeded();
                    final Socket clientSocketLocal = clientSocket;
                    if (clientSocketLocal != null) {
                        // send raw audio to the socket and to pulse audio
                        Instant start = Instant.now();
                        normalizedPCMStream.transferTo(clientSocketLocal.getOutputStream());
                        if (normalizedPCMStream.getDuration() != -1) { // ensure, if the sound has a duration
                            // that we let at least this time for the system to play
                            Instant end = Instant.now();
                            long millisSecondTimedToSendAudioData = Duration.between(start, end).toMillis();
                            if (millisSecondTimedToSendAudioData < normalizedPCMStream.getDuration()) {
                                long timeToSleep = normalizedPCMStream.getDuration() - millisSecondTimedToSendAudioData;
                                logger.debug("Sleep time to let the system play sound : {}", timeToSleep);
                                Thread.sleep(timeToSleep);
                            }
                        }
                        break;
                    }
                } catch (IOException e) {
                    disconnect(); // disconnect force to clear connection in case of socket not cleanly shutdown
                    if (countAttempt == 2) { // we won't retry : log and quit
                        final Socket clientSocketLocal = clientSocket;
                        String port = clientSocketLocal != null ? Integer.toString(clientSocketLocal.getPort())
                                : "unknown";
                        logger.warn(
                                "Error while trying to send audio to pulseaudio audio sink. Cannot connect to {}:{}, error: {}",
                                pulseaudioHandler.getHost(), port, e.getMessage());
                        break;
                    }
                } catch (InterruptedException ie) {
                    logger.info("Interrupted during sink audio connection: {}", ie.getMessage());
                    break;
                }
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new UnsupportedAudioFormatException("Cannot send sound to the pulseaudio sink",
                    audioStream.getFormat(), e);
        } finally {
            minusClientCount();
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }
}
