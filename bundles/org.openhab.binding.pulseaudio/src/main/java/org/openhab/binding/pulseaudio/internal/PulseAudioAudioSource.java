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
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSource;
import org.openhab.core.audio.AudioStream;
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

    private HashSet<AudioFormat> supportedFormats = new HashSet<>();

    public PulseAudioAudioSource(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        super(pulseaudioHandler, scheduler);
        var audioFormat = pulseaudioHandler.getSourceAudioFormat();
        if (audioFormat != null) {
            supportedFormats.add(audioFormat);
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
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
                    // get raw audio from the pulse audio socket
                    return new PulseAudioStream(sourceFormat, this::getSourceInputStream, (idle) -> {
                        setIdle(idle);
                        if (idle) {
                            scheduleDisconnect();
                        }
                    });
                } catch (IOException e) {
                    disconnect(); // disconnect force to clear connection in case of socket not cleanly shutdown
                    if (countAttempt == 2) { // we won't retry : log and quit
                        if (logger.isWarnEnabled()) {
                            String port = clientSocket != null ? Integer.toString(clientSocket.getPort()) : "unknown";
                            logger.warn(
                                    "Error while trying to get audio from pulseaudio audio source. Cannot connect to {}:{}, error: {}",
                                    pulseaudioHandler.getHost(), port, e.getMessage());
                        }
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

    private @Nullable InputStream getSourceInputStream() {
        try {
            connectIfNeeded();
        } catch (IOException | InterruptedException ignored) {
        }
        try {
            return (clientSocket != null) ? clientSocket.getInputStream() : null;
        } catch (IOException ignored) {
            return null;
        }
    }

    static class PulseAudioStream extends AudioStream {
        private final Logger logger = LoggerFactory.getLogger(PulseAudioAudioSource.class);
        private final AudioFormat format;
        private final Supplier<@Nullable InputStream> getInput;
        private final Consumer<Boolean> setIdle;

        public PulseAudioStream(AudioFormat format, Supplier<@Nullable InputStream> getInput,
                Consumer<Boolean> setIdle) {
            this.getInput = getInput;
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

        @SuppressWarnings("null")
        @Override
        public int read(byte @Nullable [] b) throws IOException {
            logger.trace("reading from pulseaudio stream");
            setIdle.accept(false);
            return getInputStream().read(b, 0, b.length);
        }

        @Override
        public int read(byte @Nullable [] b, int off, int len) throws IOException {
            logger.trace("reading from pulseaudio stream");
            setIdle.accept(false);
            return getInputStream().read(b, off, len);
        }

        private InputStream getInputStream() throws IOException {
            var input = getInput.get();
            if (input == null) {
                throw new IOException("Unable to access to the source input stream");
            }
            return input;
        }

        @Override
        public void close() throws IOException {
            logger.debug("set idle");
            setIdle.accept(true);
            // input can not be closed as it's a shared instance
        }
    };
}
