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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.audio.utils.AudioSinkUtils;
import org.openhab.core.common.Disposable;
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

    private AudioSinkUtils audioSinkUtils;

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.WAV, AudioFormat.MP3);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);
    private static final AudioFormat TARGET_FORMAT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, 4 * 44100, 44100L, 2);

    public PulseAudioAudioSink(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler,
            AudioSinkUtils audioSinkUtils) {
        super(pulseaudioHandler, scheduler);
        this.audioSinkUtils = audioSinkUtils;
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        processAndComplete(audioStream);
    }

    @Override
    public CompletableFuture<@Nullable Void> processAndComplete(@Nullable AudioStream audioStream) {
        if (audioStream == null) {
            return CompletableFuture.completedFuture(null);
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
                        if (normalizedPCMStream.getDuration() != -1) {
                            // ensure, if the sound has a duration
                            // that we let at least this time for the system to play
                            normalizedPCMStream.transferTo(clientSocketLocal.getOutputStream());
                            Instant end = Instant.now();
                            long millisSecondTimedToSendAudioData = Duration.between(start, end).toMillis();
                            if (millisSecondTimedToSendAudioData < normalizedPCMStream.getDuration()) {
                                CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
                                long timeToWait = normalizedPCMStream.getDuration() - millisSecondTimedToSendAudioData;
                                logger.debug("Some time to let the system play sound : {}", timeToWait);
                                scheduler.schedule(() -> soundPlayed.complete(null), timeToWait, TimeUnit.MILLISECONDS);
                                return soundPlayed;
                            } else {
                                return CompletableFuture.completedFuture(null);
                            }
                        } else {
                            // We have a second method available to guess the duration, and it is during transfer
                            Long timeStampEnd = audioSinkUtils.transferAndAnalyzeLength(normalizedPCMStream,
                                    clientSocketLocal.getOutputStream(), TARGET_FORMAT);
                            CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
                            if (timeStampEnd != null) {
                                long now = System.nanoTime();
                                long timeToWait = timeStampEnd - now;
                                if (timeToWait > 0) {
                                    scheduler.schedule(() -> soundPlayed.complete(null), timeToWait,
                                            TimeUnit.NANOSECONDS);
                                }
                                return soundPlayed;
                            } else {
                                return CompletableFuture.completedFuture(null);
                            }
                        }
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
                        return CompletableFuture.completedFuture(null);
                    }
                } catch (InterruptedException ie) {
                    logger.info("Interrupted during sink audio connection: {}", ie.getMessage());
                    return CompletableFuture.completedFuture(null);
                }
            }
        } catch (UnsupportedAudioFileException | UnsupportedAudioFormatException | IOException e) {
            return CompletableFuture.failedFuture(new UnsupportedAudioFormatException(
                    "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
        } finally {
            minusClientCount();
            // if the stream is not needed anymore, then we should call back the AudioStream to let it a chance
            // to auto dispose.
            if (audioStream instanceof Disposable disposableAudioStream) {
                try {
                    disposableAudioStream.dispose();
                } catch (IOException e) {
                    String fileName = audioStream instanceof FileAudioStream file ? file.toString() : "unknown";
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot dispose of stream {}", fileName, e);
                    } else {
                        logger.warn("Cannot dispose of stream {}, reason {}", fileName, e.getMessage());
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(null);
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
