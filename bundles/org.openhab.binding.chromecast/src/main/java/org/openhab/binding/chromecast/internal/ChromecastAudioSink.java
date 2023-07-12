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
package org.openhab.binding.chromecast.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chromecast.internal.handler.ChromecastHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSinkAsync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.StreamServed;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the AudioSink portion of the Chromecast add-on.
 *
 * @author Jason Holmes - Initial contribution
 */
@NonNullByDefault
public class ChromecastAudioSink extends AudioSinkAsync {
    private final Logger logger = LoggerFactory.getLogger(ChromecastAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);

    private static final String MIME_TYPE_AUDIO_WAV = "audio/wav";
    private static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";

    private final ChromecastHandler handler;
    private final AudioHTTPServer audioHTTPServer;
    private final @Nullable String callbackUrl;

    public ChromecastAudioSink(ChromecastHandler handler, AudioHTTPServer audioHTTPServer,
            @Nullable String callbackUrl) {
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
    public void processAsynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            handler.stop();
        } else {
            final String url;
            if (audioStream instanceof URLAudioStream) {
                // it is an external URL, the speaker can access it itself and play it.
                URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                url = urlAudioStream.getURL();
                tryClose(audioStream);
            } else {
                if (callbackUrl != null) {
                    // we serve it on our own HTTP server
                    String relativeUrl;
                    try {
                        StreamServed streamServed = audioHTTPServer.serve(audioStream, 10, true);
                        relativeUrl = streamServed.url();
                        // we have to run the delayed task when the server has completely played the stream
                        streamServed.playEnd().thenRun(() -> this.playbackFinished(audioStream));
                    } catch (IOException e) {
                        tryClose(audioStream);
                        throw new UnsupportedAudioStreamException(
                                "Chromecast binding was not able to handle the audio stream (cache on disk failed)",
                                audioStream.getClass(), e);
                    }
                    url = callbackUrl + relativeUrl;
                } else {
                    logger.warn("We do not have any callback url, so Chromecast cannot play the audio stream!");
                    tryClose(audioStream);
                    return;
                }
            }
            handler.playURL("Notification", url,
                    AudioFormat.MP3.isCompatible(audioStream.getFormat()) ? MIME_TYPE_AUDIO_MPEG : MIME_TYPE_AUDIO_WAV);
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
        return SUPPORTED_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        return handler.getVolume();
    }

    @Override
    public void setVolume(PercentType percentType) throws IOException {
        handler.setVolume(percentType);
    }
}
