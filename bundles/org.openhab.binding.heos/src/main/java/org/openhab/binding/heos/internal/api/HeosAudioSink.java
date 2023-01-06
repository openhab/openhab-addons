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
package org.openhab.binding.heos.internal.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.handler.HeosThingBaseHandler;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.utils.AudioStreamUtils;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes HEOS to serve as an {@link AudioSink}.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosAudioSink implements AudioSink {
    private final Logger logger = LoggerFactory.getLogger(HeosAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = new HashSet<>();
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = new HashSet<>();

    static {
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.MP3);
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.AAC);

        SUPPORTED_AUDIO_STREAMS.add(URLAudioStream.class);
        SUPPORTED_AUDIO_STREAMS.add(FixedLengthAudioStream.class);
    }

    private final HeosThingBaseHandler handler;
    private final AudioHTTPServer audioHTTPServer;
    private @Nullable final String callbackUrl;

    public HeosAudioSink(HeosThingBaseHandler handler, AudioHTTPServer audioHTTPServer, @Nullable String callbackUrl) {
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
    public void process(@Nullable AudioStream audioStream) throws UnsupportedAudioFormatException {
        try {
            if (audioStream instanceof URLAudioStream) {
                // it is an external URL, the speaker can access it itself and play it.
                URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                handler.playURL(urlAudioStream.getURL());
            } else if (audioStream instanceof FixedLengthAudioStream) {
                if (callbackUrl != null) {
                    // we serve it on our own HTTP server for 30 seconds as HEOS requests the stream several times
                    String relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 30);
                    String url = callbackUrl + relativeUrl + AudioStreamUtils.EXTENSION_SEPARATOR;
                    AudioFormat audioFormat = audioStream.getFormat();
                    if (!ThingHandlerHelper.isHandlerInitialized(handler)) {
                        logger.debug("HEOS speaker '{}' is not initialized - status is {}", handler.getThing().getUID(),
                                handler.getThing().getStatus());
                    } else if (AudioFormat.MP3.isCompatible(audioFormat)) {
                        handler.playURL(url + FileAudioStream.MP3_EXTENSION);
                    } else if (AudioFormat.WAV.isCompatible(audioFormat)) {
                        handler.playURL(url + FileAudioStream.WAV_EXTENSION);
                    } else if (AudioFormat.AAC.isCompatible(audioFormat)) {
                        handler.playURL(url + FileAudioStream.AAC_EXTENSION);
                    } else {
                        throw new UnsupportedAudioFormatException("HEOS only supports MP3, WAV and AAC.", audioFormat);
                    }
                } else {
                    logger.warn("We do not have any callback url, so HEOS cannot play the audio stream!");
                }
            } else {
                throw new UnsupportedAudioFormatException(
                        "HEOS can only handle FixedLengthAudioStreams & URLAudioStream.", null);
            }
        } catch (IOException | ReadException e) {
            logger.warn("Failed to play audio stream: {}", e.getMessage());
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
        return handler.getNotificationSoundVolume();
    }

    @Override
    public void setVolume(PercentType volume) {
        handler.setNotificationSoundVolume(volume);
    }
}
