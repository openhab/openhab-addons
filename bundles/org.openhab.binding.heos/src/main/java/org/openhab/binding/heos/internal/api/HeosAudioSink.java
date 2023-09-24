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
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.handler.HeosThingBaseHandler;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioSinkAsync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.StreamServed;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.audio.utils.AudioStreamUtils;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes HEOS to serve as an {@link AudioSink}.
 *
 * @author Johannes Einig - Initial contribution
 * @author Laurent Garnier - Extend AudioSinkAsync
 */
@NonNullByDefault
public class HeosAudioSink extends AudioSinkAsync {
    private final Logger logger = LoggerFactory.getLogger(HeosAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Set.of(AudioFormat.WAV, AudioFormat.MP3,
            AudioFormat.AAC);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Set.of(AudioStream.class);

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
    protected void processAsynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (!ThingHandlerHelper.isHandlerInitialized(handler)) {
            logger.debug("HEOS speaker '{}' is not initialized - status is {}", handler.getThing().getUID(),
                    handler.getThing().getStatus());
            tryClose(audioStream);
            return;
        }

        if (audioStream == null) {
            return;
        }

        AudioFormat audioFormat = audioStream.getFormat();
        if (!AudioFormat.MP3.isCompatible(audioFormat) && !AudioFormat.WAV.isCompatible(audioFormat)
                && !AudioFormat.AAC.isCompatible(audioFormat)) {
            tryClose(audioStream);
            throw new UnsupportedAudioFormatException("HEOS speaker only supports MP3, WAV and AAC formats.",
                    audioFormat);
        }

        String url;
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            url = urlAudioStream.getURL();
            tryClose(audioStream);
        } else if (callbackUrl != null) {
            StreamServed streamServed;
            try {
                streamServed = audioHTTPServer.serve(audioStream, 10, true);
            } catch (IOException e) {
                tryClose(audioStream);
                throw new UnsupportedAudioStreamException(
                        "HEOS was not able to handle the audio stream (cache on disk failed).", audioStream.getClass(),
                        e);
            }
            url = callbackUrl + streamServed.url() + AudioStreamUtils.EXTENSION_SEPARATOR;
            if (AudioFormat.MP3.isCompatible(audioFormat)) {
                url += FileAudioStream.MP3_EXTENSION;
            } else if (AudioFormat.WAV.isCompatible(audioFormat)) {
                url += FileAudioStream.WAV_EXTENSION;
            } else if (AudioFormat.AAC.isCompatible(audioFormat)) {
                url += FileAudioStream.AAC_EXTENSION;
            }
            streamServed.playEnd().thenRun(() -> this.playbackFinished(audioStream));
        } else {
            logger.warn("We do not have any callback url, so HEOS cannot play the audio stream!");
            tryClose(audioStream);
            return;
        }
        try {
            handler.playURL(url);
        } catch (IOException | ReadException e) {
            logger.warn("Failed to play audio stream: {}", e.getMessage());
        }
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
