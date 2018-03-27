/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.kodi.handler.KodiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes Kodi to serve as an {@link AudioSink}-
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Paul Frank - Adapted for Kodi
 *
 */
public class KodiAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(KodiAudioSink.class);

    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    static {
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_FORMATS.add(AudioFormat.MP3);

        SUPPORTED_STREAMS.add(FixedLengthAudioStream.class);
        SUPPORTED_STREAMS.add(URLAudioStream.class);
    }

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
        String url = null;
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            url = urlAudioStream.getURL();
        } else if (audioStream instanceof FixedLengthAudioStream) {
            FixedLengthAudioStream fixedLengthAudioStream = (FixedLengthAudioStream) audioStream;
            if (callbackUrl != null) {
                // we serve it on our own HTTP server for 30 seconds as Kodi requests the stream several times
                String relativeUrl = audioHTTPServer.serve(fixedLengthAudioStream, 30);
                url = callbackUrl + relativeUrl;
            } else {
                logger.warn("We do not have any callback url, so Kodi cannot play the audio stream!");
                return;
            }
        } else {
            throw new UnsupportedAudioStreamException("Kodi can only handle URLAudioStream or FixedLengthAudioStreams.",
                    null);
        }
        logger.trace("Using callback url: '{}'", url);
        handler.playURI(new StringType(url));
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
        return handler.getNotificationSoundVolume();
    }

    @Override
    public void setVolume(PercentType volume) {
        handler.setNotificationSoundVolume(volume);
    }

}
