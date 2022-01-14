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
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The audio sink for openhab, implemented by a connection to a pulseaudio sink
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public class PulseAudioAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(PulseAudioAudioSink.class);

    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    private PulseaudioHandler pulseaudioHandler;
    private ScheduledExecutorService scheduler;

    private @Nullable Socket clientSocket;

    private boolean isIdle = true;

    private @Nullable ScheduledFuture<?> scheduledDisconnection;

    static {
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_FORMATS.add(AudioFormat.MP3);
        SUPPORTED_STREAMS.add(FixedLengthAudioStream.class);
    }

    public PulseAudioAudioSink(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        this.pulseaudioHandler = pulseaudioHandler;
        this.scheduler = scheduler;
    }

    @Override
    public String getId() {
        return pulseaudioHandler.getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return pulseaudioHandler.getThing().getLabel();
    }

    /**
     * Connect to pulseaudio with the simple protocol
     *
     * @throws IOException
     * @throws InterruptedException when interrupted during the loading module wait
     */
    public void connectIfNeeded() throws IOException, InterruptedException {
        Socket clientSocketLocal = clientSocket;
        if (clientSocketLocal == null || !clientSocketLocal.isConnected() || clientSocketLocal.isClosed()) {
            String host = pulseaudioHandler.getHost();
            int port = pulseaudioHandler.getSimpleTcpPort();
            clientSocket = new Socket(host, port);
            clientSocket.setSoTimeout(500);
        }
    }

    /**
     * Disconnect the socket to pulseaudio simple protocol
     */
    public void disconnect() {
        final Socket clientSocketLocal = clientSocket;
        if (clientSocketLocal != null && isIdle) {
            logger.debug("Disconnecting");
            try {
                clientSocketLocal.close();
            } catch (IOException e) {
            }
        } else {
            logger.debug("Stream still running or socket not open");
        }
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {

        if (audioStream == null) {
            return;
        }

        try (ConvertedInputStream normalizedPCMStream = new ConvertedInputStream(audioStream)) {
            for (int countAttempt = 1; countAttempt <= 2; countAttempt++) { // two attempts allowed
                try {
                    connectIfNeeded();
                    final Socket clientSocketLocal = clientSocket;
                    if (clientSocketLocal != null) {
                        // send raw audio to the socket and to pulse audio
                        isIdle = false;
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
                        if (logger.isWarnEnabled()) {
                            String port = clientSocket != null ? Integer.toString(clientSocket.getPort()) : "unknown";
                            logger.warn(
                                    "Error while trying to send audio to pulseaudio audio sink. Cannot connect to {}:{}, error: {}",
                                    pulseaudioHandler.getHost(), port, e.getMessage());
                        }
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
            scheduleDisconnect();
        }
        isIdle = true;
    }

    public void scheduleDisconnect() {
        if (scheduledDisconnection != null) {
            scheduledDisconnection.cancel(true);
        }
        int idleTimeout = pulseaudioHandler.getIdleTimeout();
        if (idleTimeout > -1) {
            logger.debug("Scheduling disconnect");
            scheduledDisconnection = scheduler.schedule(this::disconnect, idleTimeout, TimeUnit.MILLISECONDS);
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

    @Override
    public PercentType getVolume() {
        return new PercentType(pulseaudioHandler.getLastVolume());
    }

    @Override
    public void setVolume(PercentType volume) {
        pulseaudioHandler.setVolume(volume.intValue());
    }
}
