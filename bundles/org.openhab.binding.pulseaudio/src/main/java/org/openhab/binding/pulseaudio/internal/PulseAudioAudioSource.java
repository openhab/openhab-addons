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
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.binding.pulseaudio.internal.items.SimpleProtocolTCPModule;
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
        streamGroup = PipedAudioStream.newGroup(streamFormat, 1024 * 10);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(streamFormat);
    }

    @Override
    public AudioStream getInputStream(AudioFormat audioFormat) throws AudioException {
        try {
            for (int countAttempt = 1; countAttempt <= 2; countAttempt++) { // two attempts allowed
                @Nullable
                PipedAudioStream audioStream = null;
                try {
                    if (!audioFormat.isCompatible(streamFormat)) {
                        throw new AudioException("Incompatible audio format requested");
                    }
                    audioStream = streamGroup.getAudioStreamInGroup();
                    audioStream.onClose(() -> {
                        stopPipeWriteTask();
                    });
                    startPipeWrite();
                    // get raw audio from the pulse audio socket
                    return audioStream;
                } catch (IOException e) {
                    if (countAttempt == 2) { // we won't retry : log and quit
                        logger.warn("Error while trying to get audio from pulseaudio audio source: {}", e.getMessage());
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
    private void startPipeWrite() throws IOException, InterruptedException {
        if (this.pipeWriteTask == null && !streamGroup.isEmpty()) {
            startPipeWriteSynchronized();
        }
    }

    private synchronized void startPipeWriteSynchronized() throws IOException, InterruptedException {
        if (this.pipeWriteTask == null) {
            AcquireModuleResult acquireModuleResult = acquireSimpleProtocolModule(streamFormat);
            if (acquireModuleResult.module().isEmpty()) {
                throw new IOException("Unable to create simple protocol module instance on pulseaudio server.");
            }
            SimpleProtocolTCPModule spModule = acquireModuleResult.module().get();
            Runnable releaseModuleOp = acquireModuleResult.releaseModule();
            this.pipeWriteTask = executor.submit(() -> {
                int lengthRead;
                byte[] buffer = new byte[1024];
                int readRetries = 4;
                while (!streamGroup.isEmpty()) {
                    Socket spSocket = null;
                    try {
                        spSocket = connectIfNeeded(spModule);
                        var stream = spSocket.getInputStream();
                        lengthRead = stream.read(buffer);
                        if (lengthRead == -1) {
                            logger.warn("Unable to read audio data.");
                            throw new IOException("Stream closed");
                        }
                        readRetries = 4;
                        streamGroup.write(buffer, 0, lengthRead);
                        streamGroup.flush();
                    } catch (IOException e) {
                        logger.warn("IOException while reading from pulse source: {}", getExceptionMessage(e));
                        readRetries--;
                        if (readRetries == 1) {
                            // disconnect the socket in case it recovers
                            if (spSocket != null) {
                                disconnect(spSocket);
                            }
                        } else if (readRetries == 0) {
                            // unload the source so dialogs connected to it get stopped,
                            // the source will be reloaded on next state update
                            pulseaudioHandler.audioSourceUnsetup();
                            this.pipeWriteTask = null;
                            return;
                        }
                    } catch (RuntimeException e) {
                        logger.warn("RuntimeException while reading from pulse source: {}", getExceptionMessage(e));
                    }
                }
                releaseModuleOp.run();
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

    @Override
    public void close() {
        streamGroup.close();
        stopPipeWriteTask();
        super.close();
    }
}
