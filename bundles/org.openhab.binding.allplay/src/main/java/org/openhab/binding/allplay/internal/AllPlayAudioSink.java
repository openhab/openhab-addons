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
package org.openhab.binding.allplay.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.allplay.internal.handler.AllPlayHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioSinkAsync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.StreamServed;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kaizencode.tchaikovsky.exception.SpeakerException;

/**
 * The {@link AllPlayAudioSink} make AllPlay speakers available as an {@link AudioSink}.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayAudioSink extends AudioSinkAsync {

    private final Logger logger = LoggerFactory.getLogger(AllPlayAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);
    private final AllPlayHandler handler;
    private final AudioHTTPServer audioHTTPServer;
    private final String callbackUrl;

    /**
     * @param handler The related {@link AllPlayHandler}
     * @param audioHTTPServer The {@link AudioHTTPServer} for serving the stream
     * @param callbackUrl The callback URL to stream the audio from
     */
    public AllPlayAudioSink(AllPlayHandler handler, AudioHTTPServer audioHTTPServer, String callbackUrl) {
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
    protected void processAsynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            return;
        }
        String url;
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, the speaker can access it itself and play it
            url = urlAudioStream.getURL();
            tryClose(audioStream);
        } else if (callbackUrl != null) {
            StreamServed streamServed;
            try {
                streamServed = audioHTTPServer.serve(audioStream, 10, true);
            } catch (IOException e) {
                tryClose(audioStream);
                throw new UnsupportedAudioStreamException(
                        "AllPlay was not able to handle the audio stream (cache on disk failed).",
                        audioStream.getClass(), e);
            }
            url = callbackUrl + streamServed.url();
            streamServed.playEnd().thenRun(() -> this.playbackFinished(audioStream));
        } else {
            logger.warn("We do not have any callback url, so AllPlay cannot play the audio stream!");
            tryClose(audioStream);
            return;
        }
        try {
            handler.playUrl(url);
        } catch (SpeakerException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Unable to play audio stream on speaker {}", getId(), e);
            } else {
                logger.warn("Unable to play audio stream on speaker {}: {}", getId(), e.getMessage());
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
        try {
            return handler.getVolume();
        } catch (SpeakerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        try {
            handler.handleVolumeCommand(volume);
        } catch (SpeakerException e) {
            throw new IOException(e);
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
}
