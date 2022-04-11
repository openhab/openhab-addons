/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSource;
import org.openhab.core.audio.AudioStream;
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
    private final ConcurrentLinkedQueue<PipedOutputStream> pipeOutputs = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor;

    private @Nullable Future<?> pipeWriteTask;

    public PulseAudioAudioSource(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        super(pulseaudioHandler, scheduler);
        executor = ThreadPoolManager
                .getScheduledPool("OH-binding-" + pulseaudioHandler.getThing().getUID() + "-source");
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        var supportedFormats = new HashSet<AudioFormat>();
        var audioFormat = pulseaudioHandler.getSourceAudioFormat();
        if (audioFormat != null) {
            supportedFormats.add(audioFormat);
        }
        return supportedFormats;
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
                    var sourceFormat = pulseaudioHandler.getSourceAudioFormat();
                    if (sourceFormat == null) {
                        throw new AudioException("Unable to get source audio format");
                    }
                    if (!audioFormat.isCompatible(sourceFormat)) {
                        throw new AudioException("Incompatible audio format requested");
                    }
                    setIdle(true);
                    var pipeOutput = new PipedOutputStream();
                    var pipeInput = new PipedInputStream(pipeOutput, 1024 * 10) {
                        @Override
                        public void close() throws IOException {
                            unregisterPipe(pipeOutput);
                            super.close();
                        }
                    };
                    registerPipe(pipeOutput);
                    // get raw audio from the pulse audio socket
                    return new PulseAudioStream(sourceFormat, pipeInput, (idle) -> {
                        setIdle(idle);
                        if (idle) {
                            scheduleDisconnect();
                        } else {
                            // ensure pipe is writing
                            startPipeWrite();
                        }
                    });
                } catch (IOException e) {
                    disconnect(); // disconnect to force clear connection in case of socket not cleanly shutdown
                    if (countAttempt == 2) { // we won't retry : log and quit
                        final Socket clientSocketLocal = clientSocket;
                        String port = clientSocketLocal != null ? Integer.toString(clientSocketLocal.getPort())
                                : "unknown";
                        logger.warn(
                                "Error while trying to get audio from pulseaudio audio source. Cannot connect to {}:{}, error: {}",
                                pulseaudioHandler.getHost(), port, e.getMessage());
                        setIdle(true);
                        throw e;
                    }
                } catch (InterruptedException ie) {
                    logger.info("Interrupted during source audio connection: {}", ie.getMessage());
                    setIdle(true);
                    throw new AudioException(ie);
                }
                countAttempt++;
            }
        } catch (IOException e) {
            throw new AudioException(e);
        } finally {
            scheduleDisconnect();
        }
        setIdle(true);
        throw new AudioException("Unable to create input stream");
    }

    private synchronized void registerPipe(PipedOutputStream pipeOutput) {
        this.pipeOutputs.add(pipeOutput);
        startPipeWrite();
    }

    private synchronized void startPipeWrite() {
        if (this.pipeWriteTask == null) {
            this.pipeWriteTask = executor.submit(() -> {
                int lengthRead;
                byte[] buffer = new byte[1024];
                int readRetries = 3;
                while (!pipeOutputs.isEmpty()) {
                    var stream = getSourceInputStream();
                    if (stream != null) {
                        try {
                            lengthRead = stream.read(buffer);
                            readRetries = 3;
                            for (var output : pipeOutputs) {
                                try {
                                    output.write(buffer, 0, lengthRead);
                                    if (pipeOutputs.contains(output)) {
                                        output.flush();
                                    }
                                } catch (InterruptedIOException e) {
                                    if (pipeOutputs.isEmpty()) {
                                        // task has been ended while writing
                                        return;
                                    }
                                    logger.warn("InterruptedIOException while writing to from pulse source pipe: {}",
                                            getExceptionMessage(e));
                                } catch (IOException e) {
                                    logger.warn("IOException while writing to from pulse source pipe: {}",
                                            getExceptionMessage(e));
                                } catch (RuntimeException e) {
                                    logger.warn("RuntimeException while writing to pulse source pipe: {}",
                                            getExceptionMessage(e));
                                }
                            }
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

    private synchronized void unregisterPipe(PipedOutputStream pipeOutput) {
        this.pipeOutputs.remove(pipeOutput);
        try {
            Thread.sleep(0);
        } catch (InterruptedException ignored) {
        }
        stopPipeWriteTask();
        try {
            pipeOutput.close();
        } catch (IOException ignored) {
        }
    }

    private synchronized void stopPipeWriteTask() {
        var pipeWriteTask = this.pipeWriteTask;
        if (pipeOutputs.isEmpty() && pipeWriteTask != null) {
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

    static class PulseAudioStream extends AudioStream {
        private final Logger logger = LoggerFactory.getLogger(PulseAudioAudioSource.class);
        private final AudioFormat format;
        private final InputStream input;
        private final Consumer<Boolean> setIdle;
        private boolean closed = false;

        public PulseAudioStream(AudioFormat format, InputStream input, Consumer<Boolean> setIdle) {
            this.input = input;
            this.format = format;
            this.setIdle = setIdle;
        }

        @Override
        public AudioFormat getFormat() {
            return format;
        }

        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int bytesRead = read(b);
            if (-1 == bytesRead) {
                return bytesRead;
            }
            Byte bb = Byte.valueOf(b[0]);
            return bb.intValue();
        }

        @Override
        public int read(byte @Nullable [] b) throws IOException {
            return read(b, 0, b == null ? 0 : b.length);
        }

        @Override
        public int read(byte @Nullable [] b, int off, int len) throws IOException {
            if (b == null) {
                throw new IOException("Buffer is null");
            }
            logger.trace("reading from pulseaudio stream");
            if (closed) {
                throw new IOException("Stream is closed");
            }
            setIdle.accept(false);
            return input.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            setIdle.accept(true);
            input.close();
        }
    };
}
