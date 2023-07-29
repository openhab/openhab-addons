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
package org.openhab.binding.squeezebox.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.squeezebox.internal.handler.SqueezeBoxPlayerHandler;
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
public class SqueezeBoxAudioSink extends AudioSinkSync {
    private final Logger logger = LoggerFactory.getLogger(SqueezeBoxAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.WAV, AudioFormat.MP3);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);

    // Needed because Squeezebox does multiple requests for the stream
    private static final int STREAM_TIMEOUT = 10;

    private String callbackUrl;

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
    public void processSynchronously(AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            return;
        }
        AudioFormat format = audioStream.getFormat();
        if (!AudioFormat.WAV.isCompatible(format) && !AudioFormat.MP3.isCompatible(format)) {
            tryClose(audioStream);
            throw new UnsupportedAudioFormatException("Currently only MP3 and WAV formats are supported: ", format);
        }

        String url;
        if (audioStream instanceof URLAudioStream) {
            url = ((URLAudioStream) audioStream).getURL();
            tryClose(audioStream);
        } else {
            try {
                // Since Squeezebox will make multiple requests for the stream, set multiple to true
                StreamServed streamServed = audioHTTPServer.serve(audioStream, STREAM_TIMEOUT, true);
                url = streamServed.url();

                if (AudioFormat.WAV.isCompatible(format)) {
                    url += AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.WAV_EXTENSION;
                } else if (AudioFormat.MP3.isCompatible(format)) {
                    url += AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.MP3_EXTENSION;
                }

                // Form the URL for streaming the notification from the OH web server
                // Use the callback URL if it is set in the binding configuration
                String host = callbackUrl == null || callbackUrl.isEmpty() ? playerHandler.getHostAndPort()
                        : callbackUrl;
                if (host == null) {
                    logger.warn("Unable to get host/port from which to stream notification");
                    tryClose(audioStream);
                    return;
                }
                url = host + url;
            } catch (IOException e) {
                tryClose(audioStream);
                throw new UnsupportedAudioStreamException(
                        "Squeezebox binding was not able to handle the audio stream (cache on disk failed)",
                        audioStream.getClass(), e);
            }
        }

        logger.debug("Processing audioStream {} of format {}", url, format);
        playerHandler.playNotificationSoundURI(new StringType(url));
    }

    private void tryClose(@Nullable InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ignored) {
            }
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
        return playerHandler.getNotificationSoundVolume();
    }

    @Override
    public void setVolume(PercentType volume) {
        playerHandler.setNotificationSoundVolume(volume);
    }
}
