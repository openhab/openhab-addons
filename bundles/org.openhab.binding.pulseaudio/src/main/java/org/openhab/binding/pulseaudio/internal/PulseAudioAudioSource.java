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
package org.openhab.binding.pulseaudio.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSource;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.PipedAudioStream;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The audio source for openhab, implemented by a connection to a pulseaudio source using Simple TCP protocol
 *
 * @author Miguel Álvarez - Initial contribution
 *
 */
@NonNullByDefault
public class PulseAudioAudioSource extends PulseaudioSimpleProtocolStream implements AudioSource {

    private final Logger logger = LoggerFactory.getLogger(PulseAudioAudioSource.class);
    private final PipedAudioStream.Group streamGroup;
    private final ScheduledExecutorService executor;
    private final AudioFormat streamFormat;

    private @Nullable Future<?> pipeWriteTask;

    public PulseAudioAudioSource(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        super(pulseaudioHandler, scheduler);
        streamFormat = pulseaudioHandler.getSourceAudioFormat();
        executor = ThreadPoolManager
                .getScheduledPool("OH-binding-" + pulseaudioHandler.getThing().getUID() + "-source");
        streamGroup = PipedAudioStream.newGroup(streamFormat);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(streamFormat);
    }

    @Override
    public AudioStream getInputStream(AudioFormat audioFormat) throws AudioException {
        try {
            for (int countAttempt = 1; countAttempt <= 2; countAttempt++) { // two attempts allowed
                try {
                    connectIfNeeded();
                    final Socket clientSocketLocal = clientSocket;
                    if (clientSocketLocal == null) {
                        break;
                    }
                    if (!audioFormat.isCompatible(streamFormat)) {
                        throw new AudioException("Incompatible audio format requested");
                    }
                    var audioStream = streamGroup.getAudioStreamInGroup();
                    audioStream.onClose(() -> {
                        minusClientCount();
                        stopPipeWriteTask();
                    });
                    addClientCount();
                    startPipeWrite();
                    // get raw audio from the pulse audio socket
                    return audioStream;
                } catch (IOException e) {
                    disconnect(); // disconnect to force clear connection in case of socket not cleanly shutdown
                    if (countAttempt == 2) { // we won't retry : log and quit
                        final Socket clientSocketLocal = clientSocket;
                        String port = clientSocketLocal != null ? Integer.toString(clientSocketLocal.getPort())
                                : "unknown";
                        logger.warn(
                                "Error while trying to get audio from pulseaudio audio source. Cannot connect to {}:{}, error: {}",
                                pulseaudioHandler.getHost(), port, e.getMessage());
                        throw e;
                    }
                } catch (InterruptedException ie) {
                    logger.info("Interrupted during source audio connection: {}", ie.getMessage());
                    throw new AudioException(ie);
                }
                countAttempt++;
            }
        } catch (IOException e) {
            throw new AudioException(e);
        }
        throw new AudioException("Unable to create input stream");
    }

    /**
     * As startPipeWrite is called for every chunk read,
     * this wrapper method make the test before effectively
     * locking the object (which is a costly operation)
     */
    private void startPipeWrite() {
        if (this.pipeWriteTask == null) {
            startPipeWriteSynchronized();
        }
    }

    private synchronized void startPipeWriteSynchronized() {
        if (this.pipeWriteTask == null) {
            this.pipeWriteTask = executor.submit(() -> {
                int lengthRead;
                byte[] buffer = new byte[1200];
                int readRetries = 3;
                while (!streamGroup.isEmpty()) {
                    var stream = getSourceInputStream();
                    if (stream != null) {
                        try {
                            lengthRead = stream.read(buffer);
                            readRetries = 3;
                            streamGroup.write(buffer, 0, lengthRead);
                            streamGroup.flush();
                        } catch (IOException e) {
                            logger.warn("IOException while reading from pulse source: {}", getExceptionMessage(e));
                            if (readRetries == 0) {
                                // force reconnection on persistent IOException
                                super.disconnect();
                            } else {
                                readRetries--;
                            }
                        } catch (RuntimeException e) {
                            logger.warn("RuntimeException while reading from pulse source: {}", getExceptionMessage(e));
                        }
                    } else {
                        logger.warn("Unable to get source input stream");
                    }
                }
                this.pipeWriteTask = null;
            });
        }
    }

    private synchronized void stopPipeWriteTask() {
        var pipeWriteTask = this.pipeWriteTask;
        if (streamGroup.isEmpty() && pipeWriteTask != null) {
            pipeWriteTask.cancel(true);
            this.pipeWriteTask = null;
        }
    }

    private @Nullable String getExceptionMessage(Exception e) {
        String message = e.getMessage();
        var cause = e.getCause();
        if (message == null && cause != null) {
            message = cause.getMessage();
        }
        return message;
    }

    private @Nullable InputStream getSourceInputStream() {
        try {
            connectIfNeeded();
        } catch (IOException | InterruptedException ignored) {
        }
        try {
            var clientSocketFinal = clientSocket;
            return (clientSocketFinal != null) ? clientSocketFinal.getInputStream() : null;
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public void disconnect() {
        stopPipeWriteTask();
        super.disconnect();
    }
}
