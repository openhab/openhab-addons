/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay.internal;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.allplay.handler.AllPlayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kaizencode.tchaikovsky.exception.SpeakerException;

/**
 * The {@link AllPlayAudioSink} make AllPlay speakers available as a {@link AudioSink}.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(AllPlayAudioSink.class);

    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private final AllPlayHandler handler;
    private final AudioHTTPServer audioHTTPServer;
    private final String callbackUrl;

    static {
        SUPPORTED_FORMATS.add(AudioFormat.MP3);
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
    }

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
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        try {
            String url = convertAudioStreamToUrl(audioStream);
            handler.playUrl(url);
        } catch (SpeakerException | AllPlayAudioStreamException e) {
            logger.warn("Unable to play audio stream on speaker {}", getId(), e);
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
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

    /**
     * Converts the given {@link AudioStream} into an URL which can be used for streaming.
     *
     * @param audioStream The incoming {@link AudioStream}
     * @return The URL to use for streaming
     * @throws AllPlayAudioStreamException Exception if the URL cannot be created
     */
    private String convertAudioStreamToUrl(AudioStream audioStream) throws AllPlayAudioStreamException {
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it
            return ((URLAudioStream) audioStream).getURL();
        } else {
            return createUrlForLocalHttpServer(audioStream);
        }
    }

    private String createUrlForLocalHttpServer(AudioStream audioStream) throws AllPlayAudioStreamException {
        if (callbackUrl != null) {
            String relativeUrl = audioHTTPServer.serve(audioStream);
            return callbackUrl + relativeUrl;
        } else {
            throw new AllPlayAudioStreamException("Unable to play audio stream as callback URL is not set");
        }
    }

    @SuppressWarnings("serial")
    private class AllPlayAudioStreamException extends Exception {

        public AllPlayAudioStreamException(String message) {
            super(message);
        }
    }

}
