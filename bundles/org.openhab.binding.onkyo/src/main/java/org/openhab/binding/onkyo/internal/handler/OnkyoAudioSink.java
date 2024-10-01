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
package org.openhab.binding.onkyo.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * * The {@link OnkyoAudioSink} implements the AudioSink interface.
 *
 * @author Paul Frank - Initial contribution
 * @author Laurent Garnier - Extracted from UpnpAudioSinkHandler to extend AudioSinkAsync
 */
@NonNullByDefault
public class OnkyoAudioSink extends AudioSinkAsync {

    private final Logger logger = LoggerFactory.getLogger(OnkyoAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.WAV, AudioFormat.MP3);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);

    private OnkyoHandler handler;
    private AudioHTTPServer audioHTTPServer;
    private @Nullable String callbackUrl;

    public OnkyoAudioSink(OnkyoHandler handler, AudioHTTPServer audioHTTPServer, @Nullable String callbackUrl) {
        this.handler = handler;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
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
        if (audioStream == null) {
            handler.stop();
            return;
        }

        String url;
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            url = urlAudioStream.getURL();
            tryClose(audioStream);
        } else if (callbackUrl != null) {
            // we serve it on our own HTTP server
            StreamServed streamServed;
            try {
                streamServed = audioHTTPServer.serve(audioStream, 10, true);
            } catch (IOException e) {
                tryClose(audioStream);
                throw new UnsupportedAudioStreamException(
                        "Onkyo was not able to handle the audio stream (cache on disk failed).", audioStream.getClass(),
                        e);
            }
            url = callbackUrl + streamServed.url();
            streamServed.playEnd().thenRun(() -> this.playbackFinished(audioStream));
        } else {
            logger.warn("We do not have any callback url, so Onkyo cannot play the audio stream!");
            tryClose(audioStream);
            return;
        }
        handler.playMedia(url);
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
    public PercentType getVolume() throws IOException {
        return handler.getVolume();
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        handler.setVolume(volume);
    }
}
