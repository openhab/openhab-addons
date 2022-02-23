/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.core.audio.AudioFormat.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaAction;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaActionData.MediaType;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaManager;
import org.openhab.binding.freeboxos.internal.api.airmedia.MediaReceiverManager;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.thing.ThingStatus;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirMediaSink} is holding AudioSink capabilities for various
 * things.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaSink implements AudioSink {
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);
    private static final Set<AudioFormat> BASIC_FORMATS = Set.of(WAV, OGG);
    private static final Set<AudioFormat> ALL_MP3_FORMATS = Set.of(
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 96000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 112000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 128000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 160000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 192000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 224000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 256000, null),
            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 320000, null));

    private final Logger logger = LoggerFactory.getLogger(AirMediaSink.class);
    private final ApiConsumerHandler thingHandler;
    private final Set<AudioFormat> supportedFormats = new HashSet<>();
    private final AudioHTTPServer audioHTTPServer;
    private final String callbackUrl;
    private final String playerName;

    public AirMediaSink(ApiConsumerHandler thingHandler, AudioHTTPServer audioHTTPServer, @Nullable String ipAddress,
            BundleContext bundleContext, @Nullable String callbackUrl, String playerName) {
        this.thingHandler = thingHandler;
        this.audioHTTPServer = audioHTTPServer;
        this.playerName = playerName;
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            this.callbackUrl = callbackUrl;
        } else {
            int port = HttpServiceUtil.getHttpServicePort(bundleContext);
            if (port != -1 && ipAddress != null) {
                // we do not use SSL as it can cause certificate validation issues.
                this.callbackUrl = String.format("http://%s:%d", ipAddress, port);
            } else {
                throw new IllegalArgumentException(
                        "No network interface could be found or cannot find port of the http service.");
            }
        }
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        logger.debug("getVolume received but AirMedia does not have the capability - returning 100%.");
        return PercentType.HUNDRED;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        logger.debug("setVolume received but AirMedia does not have the capability - ignoring it.");
    }

    @Override
    public String getId() {
        return thingHandler.getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return thingHandler.getThing().getLabel();
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        String name = this.playerName;
        try {
            AirMediaManager manager = thingHandler.getManager(AirMediaManager.class);
            MediaReceiverManager receiver = thingHandler.getManager(MediaReceiverManager.class);
            if (thingHandler.getThing().getStatus() == ThingStatus.ONLINE) {
                String password = manager.getConfig().getPassword();
                if (audioStream == null) {
                    receiver.sendToReceiver(name, password, MediaAction.STOP, MediaType.VIDEO);
                } else {
                    String url = null;
                    if (audioStream instanceof URLAudioStream) {
                        // it is an external URL, we can access it directly
                        URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                        url = urlAudioStream.getURL();
                    } else {
                        // we serve it on our own HTTP server
                        url = callbackUrl + (audioStream instanceof FixedLengthAudioStream
                                ? audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 20)
                                : audioHTTPServer.serve(audioStream));
                    }
                    logger.debug("AirPlay audio sink: process url {}", url);
                    receiver.sendToReceiver(name, password, MediaAction.STOP, MediaType.VIDEO);
                    receiver.sendToReceiver(name, password, MediaAction.START, MediaType.VIDEO, url);
                }
            }
        } catch (FreeboxException e) {
            logger.warn("Audio stream playback failed: {}", e.getMessage());
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        if (supportedFormats.isEmpty()) {
            supportedFormats.addAll(BASIC_FORMATS);
            // if (getConfigAs(PlayerConfiguration.class).acceptAllMp3) {
            supportedFormats.addAll(ALL_MP3_FORMATS);
            // } else { // Only accept MP3 bitrates >= 96 kbps
            // SUPPORTED_FORMATS.add(MP3);
            // }
        }
        return supportedFormats;
    }
}
