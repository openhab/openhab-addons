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
package org.openhab.binding.freebox.internal;

import static org.openhab.core.audio.AudioFormat.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.config.FreeboxAirPlayDeviceConfiguration;
import org.openhab.binding.freebox.internal.handler.FreeboxThingHandler;
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
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This makes an AirPlay device to serve as an {@link AudioSink}-
 *
 * @author Laurent Garnier - Initial contribution for AudioSink and notifications
 * @author Laurent Garnier - Support for more audio streams through the HTTP audio servlet
 */
@NonNullByDefault
public class FreeboxAirPlayAudioSink extends AudioSinkAsync {

    private final Logger logger = LoggerFactory.getLogger(FreeboxAirPlayAudioSink.class);

    private static final AudioFormat MP3_96 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 96000, null);
    private static final AudioFormat MP3_112 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 112000, null);
    private static final AudioFormat MP3_128 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 128000, null);
    private static final AudioFormat MP3_160 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 160000, null);
    private static final AudioFormat MP3_192 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 192000, null);
    private static final AudioFormat MP3_224 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 224000, null);
    private static final AudioFormat MP3_256 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 256000, null);
    private static final AudioFormat MP3_320 = new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 320000, null);

    private static final Set<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);
    private AudioHTTPServer audioHTTPServer;
    private FreeboxThingHandler handler;
    private @Nullable String callbackUrl;

    public FreeboxAirPlayAudioSink(FreeboxThingHandler handler, AudioHTTPServer audioHTTPServer,
            @Nullable String callbackUrl) {
        this.handler = handler;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
        Boolean acceptLowBitrate = (Boolean) handler.getThing().getConfiguration()
                .get(FreeboxAirPlayDeviceConfiguration.ACCEPT_ALL_MP3);
        SUPPORTED_FORMATS.add(WAV);
        if (acceptLowBitrate) {
            SUPPORTED_FORMATS.add(MP3);
        } else {
            // Only accept MP3 bitrates >= 96 kbps
            SUPPORTED_FORMATS.add(MP3_96);
            SUPPORTED_FORMATS.add(MP3_112);
            SUPPORTED_FORMATS.add(MP3_128);
            SUPPORTED_FORMATS.add(MP3_160);
            SUPPORTED_FORMATS.add(MP3_192);
            SUPPORTED_FORMATS.add(MP3_224);
            SUPPORTED_FORMATS.add(MP3_256);
            SUPPORTED_FORMATS.add(MP3_320);
        }
        // OGG seems to not be properly supported (tested with a file produced by VoiceRSS)
        // SUPPORTED_FORMATS.add(OGG);
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
        if (!ThingHandlerHelper.isHandlerInitialized(handler)
                || ((handler.getThing().getStatus() == ThingStatus.OFFLINE)
                        && ((handler.getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE)
                                || (handler.getThing().getStatusInfo()
                                        .getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)))) {
            tryClose(audioStream);
            return;
        }

        if (audioStream == null) {
            try {
                handler.stopMedia();
            } catch (FreeboxException e) {
                logger.warn("Exception while stopping audio stream playback: {}", e.getMessage());
            }
            return;
        }

        String url;
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, we can access it directly
            url = urlAudioStream.getURL();
            tryClose(audioStream);
        } else if (callbackUrl != null) {
            // we serve it on our own HTTP server
            logger.debug("audioStream {} {}", audioStream.getClass().getSimpleName(), audioStream.getFormat());
            StreamServed streamServed;
            try {
                streamServed = audioHTTPServer.serve(audioStream, 5, true);
            } catch (IOException e) {
                tryClose(audioStream);
                throw new UnsupportedAudioStreamException(
                        "AirPlay device was not able to handle the audio stream (cache on disk failed).",
                        audioStream.getClass(), e);
            }
            url = callbackUrl + streamServed.url();
            streamServed.playEnd().thenRun(() -> {
                try {
                    handler.stopMedia();
                } catch (FreeboxException e) {
                    logger.warn("Exception while stopping audio stream playback: {}", e.getMessage());
                }
                this.playbackFinished(audioStream);
            });
        } else {
            logger.warn("We do not have any callback url, so AirPlay device cannot play the audio stream!");
            tryClose(audioStream);
            return;
        }
        try {
            logger.debug("AirPlay audio sink: process url {}", url);
            handler.playMedia(url);
        } catch (FreeboxException e) {
            logger.warn("Audio stream playback failed: {}", e.getMessage());
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
    public PercentType getVolume() {
        throw new UnsupportedOperationException("Volume can not be determined");
    }

    @Override
    public void setVolume(PercentType volume) {
        throw new UnsupportedOperationException("Volume can not be set");
    }
}
