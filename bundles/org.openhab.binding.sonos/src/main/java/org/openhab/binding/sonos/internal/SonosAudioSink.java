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
package org.openhab.binding.sonos.internal;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonos.internal.handler.ZonePlayerHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioSinkSync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.StreamServed;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.audio.utils.AudioStreamUtils;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes a Sonos speaker to serve as an {@link AudioSink}-
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Christoph Weitkamp - Added getSupportedStreams() and UnsupportedAudioStreamException
 * @author Laurent Garnier - Support for more audio streams through the HTTP audio servlet
 *
 */
@NonNullByDefault
public class SonosAudioSink extends AudioSinkSync {

    private final Logger logger = LoggerFactory.getLogger(SonosAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Set.of(AudioStream.class);

    private AudioHTTPServer audioHTTPServer;
    private ZonePlayerHandler handler;
    private @Nullable String callbackUrl;

    public SonosAudioSink(ZonePlayerHandler handler, AudioHTTPServer audioHTTPServer, @Nullable String callbackUrl) {
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
    public CompletableFuture<@Nullable Void> processAndComplete(@Nullable AudioStream audioStream) {
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
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream instanceof URLAudioStream) {
            processAsynchronously(audioStream);
        } else {
            processSynchronously(audioStream);
        }
    }

    private void processAsynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            handler.playURI(new StringType(urlAudioStream.getURL()));
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void processSynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream instanceof URLAudioStream) {
            return;
        }

        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            handler.stopPlaying(OnOffType.ON);
            return;
        }

        // we serve it on our own HTTP server and treat it as a notification
        // Note that Sonos does multiple concurrent requests to the AudioServlet,
        // so a one time serving won't work.
        if (callbackUrl != null) {
            StreamServed streamServed;
            try {
                streamServed = audioHTTPServer.serve(audioStream, 10, true);
            } catch (IOException e) {
                try {
                    audioStream.close();
                } catch (IOException ex) {
                }
                throw new UnsupportedAudioStreamException(
                        "Sonos was not able to handle the audio stream (cache on disk failed).", audioStream.getClass(),
                        e);
            }
            String url = callbackUrl + streamServed.url();

            AudioFormat format = audioStream.getFormat();
            if (!ThingHandlerHelper.isHandlerInitialized(handler)) {
                logger.warn("Sonos speaker '{}' is not initialized - status is {}", handler.getThing().getUID(),
                        handler.getThing().getStatus());
            } else if (AudioFormat.WAV.isCompatible(format)) {
                handler.playNotificationSoundURI(
                        new StringType(url + AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.WAV_EXTENSION));
            } else if (AudioFormat.MP3.isCompatible(format)) {
                handler.playNotificationSoundURI(
                        new StringType(url + AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.MP3_EXTENSION));
            } else {
                throw new UnsupportedAudioFormatException("Sonos only supports MP3 or WAV.", format);
            }
        } else {
            logger.warn("We do not have any callback url, so Sonos cannot play the audio stream!");
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_AUDIO_STREAMS;
    }

    @Override
    public PercentType getVolume() {
        String volume = handler.getVolume();
        return volume != null ? new PercentType(volume) : PercentType.ZERO;
    }

    @Override
    public void setVolume(PercentType volume) {
        handler.setVolume(volume);
    }
}
