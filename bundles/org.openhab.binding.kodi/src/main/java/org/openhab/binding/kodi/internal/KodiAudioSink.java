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
package org.openhab.binding.kodi.internal;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.kodi.internal.handler.KodiHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes Kodi to serve as an {@link AudioSink}.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Paul Frank - Adapted for Kodi
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public class KodiAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(KodiAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Collections
            .unmodifiableSet(Stream.of(AudioFormat.MP3, AudioFormat.WAV).collect(Collectors.toSet()));
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Collections
            .unmodifiableSet(Stream.of(FixedLengthAudioStream.class, URLAudioStream.class).collect(Collectors.toSet()));
    // Needed because Kodi does multiple requests for the stream
    private static final int STREAM_TIMEOUT = 30;

    private final KodiHandler handler;
    private final AudioHTTPServer audioHTTPServer;
    private final String callbackUrl;

    public KodiAudioSink(KodiHandler handler, AudioHTTPServer audioHTTPServer, String callbackUrl) {
        this.handler = handler;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String getId() {
        return handler.getThing().getUID().toString();
    }

    @Override
    public String getLabel(Locale locale) {
        return handler.getThing().getLabel();
    }

    @Override
    public void process(AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            handler.stop();
        } else {
            AudioFormat format = audioStream.getFormat();
            if (!AudioFormat.MP3.isCompatible(format) && !AudioFormat.WAV.isCompatible(format)) {
                throw new UnsupportedAudioFormatException("Currently only MP3 and WAV formats are supported.", format);
            }

            if (audioStream instanceof URLAudioStream) {
                // it is an external URL, the speaker can access it itself and play it
                String url = ((URLAudioStream) audioStream).getURL();
                logger.trace("Processing audioStream URL {} of format {}.", url, format);
                handler.playURI(new StringType(url));
            } else if (audioStream instanceof FixedLengthAudioStream) {
                if (callbackUrl != null) {
                    // we serve it on our own HTTP server for 30 seconds as Kodi requests the stream several times
                    // Form the URL for streaming the notification from the OH2 web server
                    String url = callbackUrl
                            + audioHTTPServer.serve((FixedLengthAudioStream) audioStream, STREAM_TIMEOUT);
                    logger.trace("Processing audioStream URL {} of format {}.", url, format);
                    handler.playNotificationSoundURI(new StringType(url));
                } else {
                    logger.warn("We do not have any callback url, so Kodi cannot play the audio stream!");
                }
            } else {
                throw new UnsupportedAudioStreamException(
                        "Kodi can only handle URLAudioStream or FixedLengthAudioStreams.", audioStream.getClass());
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
        return handler.getVolume();
    }

    @Override
    public void setVolume(PercentType volume) {
        handler.setVolume(volume);
    }
}
