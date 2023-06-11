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
package org.openhab.binding.squeezebox.internal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayerHandler;
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
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes a SqueezeBox Player serve as an {@link AudioSink}-
 *
 * @author Mark Hilbush - Initial contribution
 * @author Mark Hilbush - Add callbackUrl
 */
public class SqueezeBoxAudioSink implements AudioSink {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxAudioSink.class);

    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    // Needed because Squeezebox does multiple requests for the stream
    private static final int STREAM_TIMEOUT = 15;

    private String callbackUrl;

    static {
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_FORMATS.add(AudioFormat.MP3);

        SUPPORTED_STREAMS.add(FixedLengthAudioStream.class);
        SUPPORTED_STREAMS.add(URLAudioStream.class);
    }

    private AudioHTTPServer audioHTTPServer;
    private SqueezeBoxPlayerHandler playerHandler;

    public SqueezeBoxAudioSink(SqueezeBoxPlayerHandler playerHandler, AudioHTTPServer audioHTTPServer,
            String callbackUrl) {
        this.playerHandler = playerHandler;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            logger.debug("SqueezeBox AudioSink created with callback URL: {}", callbackUrl);
        }
    }

    @Override
    public String getId() {
        return playerHandler.getThing().getUID().toString();
    }

    @Override
    public String getLabel(Locale locale) {
        return playerHandler.getThing().getLabel();
    }

    @Override
    public void process(AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        AudioFormat format = audioStream.getFormat();
        if (!AudioFormat.WAV.isCompatible(format) && !AudioFormat.MP3.isCompatible(format)) {
            throw new UnsupportedAudioFormatException("Currently only MP3 and WAV formats are supported: ", format);
        }

        String url;
        if (audioStream instanceof URLAudioStream) {
            url = ((URLAudioStream) audioStream).getURL();
        } else if (audioStream instanceof FixedLengthAudioStream) {
            // Since Squeezebox will make multiple requests for the stream, set a timeout on the stream
            url = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, STREAM_TIMEOUT).toString();

            if (AudioFormat.WAV.isCompatible(format)) {
                url += AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.WAV_EXTENSION;
            } else if (AudioFormat.MP3.isCompatible(format)) {
                url += AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.MP3_EXTENSION;
            }

            // Form the URL for streaming the notification from the OH2 web server
            // Use the callback URL if it is set in the binding configuration
            String host = callbackUrl == null || callbackUrl.isEmpty() ? playerHandler.getHostAndPort() : callbackUrl;
            if (host == null) {
                logger.warn("Unable to get host/port from which to stream notification");
                return;
            }
            url = host + url;
        } else {
            throw new UnsupportedAudioStreamException(
                    "SqueezeBox can only handle URLAudioStream or FixedLengthAudioStreams.", null);
        }

        logger.debug("Processing audioStream {} of format {}", url, format);
        playerHandler.playNotificationSoundURI(new StringType(url));
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
        return playerHandler.getNotificationSoundVolume();
    }

    @Override
    public void setVolume(PercentType volume) {
        playerHandler.setNotificationSoundVolume(volume);
    }
}
