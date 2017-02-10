/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal;

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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes a SqueezeBox Player serve as an {@link AudioSink}-
 *
 * @author Mark Hilbush - Implement AudioSink and notifications
 */
public class SqueezeBoxAudioSink implements AudioSink {

    private Logger logger = LoggerFactory.getLogger(SqueezeBoxAudioSink.class);

    private static HashSet<AudioFormat> supportedFormats = new HashSet<>();

    // Needed because Squeezebox does multiple requests for the stream
    private final int STREAM_TIMEOUT = 15;

    static {
        supportedFormats.add(AudioFormat.WAV);
        supportedFormats.add(AudioFormat.MP3);
    }

    private AudioHTTPServer audioHTTPServer;
    private SqueezeBoxPlayerHandler playerHandler;

    public SqueezeBoxAudioSink(SqueezeBoxPlayerHandler playerHandler, AudioHTTPServer audioHTTPServer) {
        this.playerHandler = playerHandler;
        this.audioHTTPServer = audioHTTPServer;
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
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        AudioFormat format = audioStream.getFormat();
        if (!AudioFormat.WAV.isCompatible(format) && !AudioFormat.MP3.isCompatible(format)) {
            throw new UnsupportedAudioFormatException("Currently only MP3 and WAV formats are supported: ", format);
        }

        String url;
        if (audioStream instanceof FixedLengthAudioStream) {
            // Since Squeezebox will make multiple requests for the stream, set a timeout on the stream
            url = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, STREAM_TIMEOUT).toString();

            if (AudioFormat.WAV.isCompatible(format)) {
                url += ".wav";
            } else if (AudioFormat.MP3.isCompatible(format)) {
                url += ".mp3";
            }

            // Form the URL for streaming the notification from the OH2 web server
            String host = playerHandler.getHostAndPort();
            if (host == null) {
                logger.warn("Unable to get host/port from which to stream notification");
                return;
            }
            url = host + url;
        } else if (audioStream instanceof URLAudioStream) {
            url = ((URLAudioStream) audioStream).getURL();
        } else {
            logger.warn("Audio stream must be a FixedLengthAudioStream or URLAudioStream");
            return;
        }

        logger.debug("Processing audioStream {} of format {}", url, format);
        playerHandler.playNotificationSoundURI(new StringType(url));
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return supportedFormats;
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
