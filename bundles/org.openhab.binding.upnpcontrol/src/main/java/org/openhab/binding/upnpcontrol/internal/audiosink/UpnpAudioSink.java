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
package org.openhab.binding.upnpcontrol.internal.audiosink;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpRendererHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(UpnpAudioSink.class);

    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Stream
            .of(AudioStream.class, FixedLengthAudioStream.class).collect(Collectors.toSet());
    protected UpnpRendererHandler handler;
    protected AudioHTTPServer audioHTTPServer;
    protected String callbackUrl;

    public UpnpAudioSink(UpnpRendererHandler handler, AudioHTTPServer audioHTTPServer, String callbackUrl) {
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
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            stopMedia();
            return;
        }

        String url = null;
        if (audioStream instanceof URLAudioStream) {
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            url = urlAudioStream.getURL();
        } else if (!callbackUrl.isEmpty()) {
            String relativeUrl = audioStream instanceof FixedLengthAudioStream
                    ? audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 20)
                    : audioHTTPServer.serve(audioStream);
            url = String.valueOf(this.callbackUrl) + relativeUrl;
        } else {
            logger.warn("We do not have any callback url, so {} cannot play the audio stream!", handler.getUDN());
            return;
        }
        playMedia(url);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return handler.getSupportedAudioFormats();
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        return handler.getCurrentVolume();
    }

    @Override
    public void setVolume(@Nullable PercentType volume) throws IOException {
        if (volume != null) {
            handler.setVolume(volume);
        }
    }

    protected void stopMedia() {
        handler.stop();
    }

    protected void playMedia(String url) {
        String newUrl = url;
        if (!url.startsWith("x-") && !url.startsWith("http")) {
            newUrl = "x-file-cifs:" + url;
        }
        handler.setCurrentURI(newUrl, "");
        handler.play();
    }
}
