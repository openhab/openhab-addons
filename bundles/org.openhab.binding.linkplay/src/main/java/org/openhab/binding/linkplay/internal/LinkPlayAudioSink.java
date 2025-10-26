/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSinkSync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.StreamServed;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audio sink for LinkPlay players that uses the HTTP audio servlet to serve the audio stream.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayAudioSink extends AudioSinkSync {

    private final Logger logger = LoggerFactory.getLogger(LinkPlayAudioSink.class);

    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);
    protected LinkPlayHandler handler;
    protected AudioHTTPServer audioHTTPServer;
    protected @Nullable String callbackUrl;

    public LinkPlayAudioSink(LinkPlayHandler handler, AudioHTTPServer audioHTTPServer, @Nullable String callbackUrl) {
        this.handler = handler;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String getId() {
        return handler.getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return handler.getThing().getLabel();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(AudioFormat.WAV, AudioFormat.MP3, AudioFormat.OGG, AudioFormat.AAC);
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        if (handler.getState(LinkPlayBindingConstants.GROUP_PLAYBACK,
                LinkPlayBindingConstants.CHANNEL_VOLUME) instanceof PercentType volume) {
            return volume;
        }
        return new PercentType(0);
    }

    @Override
    public void setVolume(@Nullable PercentType volume) throws IOException {
        if (volume != null) {
            try {
                handler.setVolume(volume);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new IOException("Error while setting volume: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        logger.debug("{}: process: {}", handler.getThing().getUID(), audioStream);
        if (audioStream instanceof URLAudioStream) {
            processAsynchronously(audioStream);
        } else {
            processSynchronously(audioStream);
        }
    }

    @Override
    public CompletableFuture<@Nullable Void> processAndComplete(@Nullable AudioStream audioStream) {
        logger.debug("{}: processAndComplete: {}", handler.getThing().getUID(), audioStream);
        if (audioStream instanceof URLAudioStream) {
            // Asynchronous handling for URLAudioStream
            CompletableFuture<@Nullable Void> completableFuture = new CompletableFuture<@Nullable Void>();
            try {
                processAsynchronously(audioStream);
            } catch (UnsupportedAudioFormatException | UnsupportedAudioStreamException e) {
                completableFuture.completeExceptionally(e);
            }
            return completableFuture;
        } else {
            return super.processAndComplete(audioStream);
        }
    }

    @Override
    protected void processSynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        logger.debug("{}: processSynchronously: {}", handler.getThing().getUID(), audioStream);
        if (audioStream instanceof URLAudioStream) {
            return;
        }

        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            try {
                handler.stopPlaying();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.debug("Error while stopping media: {}", e.getMessage(), e);
            }
            return;
        }

        String callbackUrl = this.callbackUrl;
        if (callbackUrl != null) {
            StreamServed streamServed;
            try {
                streamServed = audioHTTPServer.serve(audioStream, 10, true);
                logger.debug("Audio stream accessible through HTTP served at {} for playback", streamServed.url());
            } catch (IOException e) {
                try {
                    audioStream.close();
                } catch (IOException ex) {
                }
                throw new UnsupportedAudioStreamException(
                        "Was not able to handle the audio stream (cache on disk failed).", audioStream.getClass(), e);
            }
            String url = callbackUrl + streamServed.url();
            try {
                playMedia(url).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.debug("Error while playing notification: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("We do not have any callback url, so cannot play the audio stream!");
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        }
    }

    private void processAsynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        logger.debug("{}: processAsynchronously: {}", handler.getThing().getUID(), audioStream);
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            playMedia(urlAudioStream.getURL());
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        }
    }

    protected CompletableFuture<@Nullable Void> playMedia(String url) {
        String newUrl = url;
        if (!url.startsWith("x-") && !url.startsWith("http")) {
            newUrl = "x-file-cifs:" + url;
        }
        return handler.playNotification(newUrl);
    }
}
