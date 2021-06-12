/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import javax.sound.sampled.AudioInputStream;
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
     * Convert MP3 to PCM, as this is the only possible format
     *
     * @param input
     * @return
     */
    private @Nullable InputStream getPCMStreamFromMp3Stream(InputStream input) {
        try {
            MpegAudioFileReader mpegAudioFileReader = new MpegAudioFileReader();
            AudioInputStream sourceAIS = mpegAudioFileReader.getAudioInputStream(input);
            javax.sound.sampled.AudioFormat sourceFormat = sourceAIS.getFormat();

            MpegFormatConversionProvider mpegconverter = new MpegFormatConversionProvider();
            javax.sound.sampled.AudioFormat convertFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16,
                    sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);

            return mpegconverter.getAudioInputStream(convertFormat, sourceAIS);

        } catch (IOException | UnsupportedAudioFileException e) {
            logger.warn("Cannot convert this mp3 stream to pcm stream: {}", e.getMessage());
        }
        return null;
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
        if (clientSocket != null && isIdle) {
            logger.debug("Disconnecting");
            try {
                clientSocket.close();
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

        InputStream audioInputStream = null;
        try {

            if (AudioFormat.MP3.isCompatible(audioStream.getFormat())) {
                audioInputStream = getPCMStreamFromMp3Stream(audioStream);
            } else if (AudioFormat.WAV.isCompatible(audioStream.getFormat())) {
                audioInputStream = audioStream;
            } else {
                throw new UnsupportedAudioFormatException("pulseaudio audio sink can only play pcm or mp3 stream",
                        audioStream.getFormat());
            }

            for (int countAttempt = 1; countAttempt <= 2; countAttempt++) { // two attempts allowed
                try {
                    connectIfNeeded();
                    if (audioInputStream != null && clientSocket != null) {
                        // send raw audio to the socket and to pulse audio
                        isIdle = false;
                        audioInputStream.transferTo(clientSocket.getOutputStream());
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
        } finally {
            try {
                if (audioInputStream != null) {
                    audioInputStream.close();
                }
                audioStream.close();
                scheduleDisconnect();
            } catch (IOException e) {
            }
        }
        isIdle = true;
    }

    public void scheduleDisconnect() {
        logger.debug("Scheduling disconnect");
        scheduler.schedule(this::disconnect, pulseaudioHandler.getIdleTimeout(), TimeUnit.MILLISECONDS);
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
