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
package org.openhab.binding.sonos.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonos.internal.handler.ZonePlayerHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
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
 *
 */
@NonNullByDefault
public class SonosAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(SonosAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Collections
            .unmodifiableSet(Stream.of(AudioFormat.MP3, AudioFormat.WAV).collect(Collectors.toSet()));
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Collections
            .unmodifiableSet(Stream.of(FixedLengthAudioStream.class, URLAudioStream.class).collect(Collectors.toSet()));

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
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            handler.stopPlaying(OnOffType.ON);
        } else if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            handler.playURI(new StringType(urlAudioStream.getURL()));
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        } else if (audioStream instanceof FixedLengthAudioStream) {
            // we serve it on our own HTTP server and treat it as a notification
            // Note that we have to pass a FixedLengthAudioStream, since Sonos does multiple concurrent requests to
            // the AudioServlet, so a one time serving won't work.
            if (callbackUrl != null) {
                String relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 10).toString();
                String url = callbackUrl + relativeUrl;

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
            }
        } else {
            try {
                audioStream.close();
            } catch (IOException e) {
            }
            throw new UnsupportedAudioStreamException(
                    "Sonos can only handle FixedLengthAudioStreams and URLAudioStreams.", audioStream.getClass());
            // Instead of throwing an exception, we could ourselves try to wrap it into a
            // FixedLengthAudioStream, but this might be dangerous as we have no clue, how much data to expect from
            // the stream.
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
