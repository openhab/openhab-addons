/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.audio.FileAudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.audio.utils.AudioStreamUtils;
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

    private static final HashSet<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    // Needed because Squeezebox does multiple requests for the stream
    private final int STREAM_TIMEOUT = 15;

    static {
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_FORMATS.add(AudioFormat.MP3);

        SUPPORTED_STREAMS.add(FixedLengthAudioStream.class);
        SUPPORTED_STREAMS.add(URLAudioStream.class);
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
            String host = playerHandler.getHostAndPort();
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
