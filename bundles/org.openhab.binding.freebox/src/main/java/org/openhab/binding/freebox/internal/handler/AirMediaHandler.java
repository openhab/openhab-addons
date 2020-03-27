/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.handler;

import static org.eclipse.smarthome.core.audio.AudioFormat.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiverRequest;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiverRequest.MediaAction;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiverRequest.MediaType;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiversResponse;
import org.openhab.binding.freebox.internal.config.AirPlayerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirMediaHandler} is responsible for handling everything associated to
 * any Air media things.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaHandler extends APIConsumerHandler implements AudioSink {
    private final Logger logger = LoggerFactory.getLogger(AirMediaHandler.class);

    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Collections.singleton(AudioStream.class);
    private static final Set<AudioFormat> BASIC_FORMATS = Collections
            .unmodifiableSet(Stream.of(WAV, OGG).collect(Collectors.toSet()));
    private static final Set<AudioFormat> ALL_MP3_FORMATS = Collections
            .unmodifiableSet(Stream
                    .of(new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 96000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 112000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 128000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 160000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 192000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 224000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 256000, null),
                            new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, null, 320000, null))
                    .collect(Collectors.toSet()));

    private final AudioHTTPServer audioHTTPServer;
    private final String callbackUrl;
    private final Set<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();

    private @NonNullByDefault({}) AirPlayerConfiguration airPlayConfig;

    public AirMediaHandler(Thing thing, TimeZoneProvider timeZoneProvider, AudioHTTPServer audioHTTPServer,
            String callbackUrl) {
        super(thing, timeZoneProvider);
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public void initialize() {
        super.initialize();
        airPlayConfig = getConfigAs(AirPlayerConfiguration.class);
    }

    @Override
    protected Map<String, String> discoverAttributes() throws FreeboxException {
        final Map<String, String> properties = super.discoverAttributes();
        airPlayConfig = getConfigAs(AirPlayerConfiguration.class);
        List<AirMediaReceiver> devices = getApiManager().executeGet(AirMediaReceiversResponse.class, null);
        List<AirMediaReceiver> matching = devices.stream().filter(device -> airPlayConfig.name.equals(device.getName()))
                .collect(Collectors.toList());
        if (!matching.isEmpty()) {
            properties.put("audio", Boolean.valueOf(matching.get(0).isAudioCapable()).toString());
            properties.put("video", Boolean.valueOf(matching.get(0).isVideoCapable()).toString());
        }
        return properties;
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            AirMediaReceiverRequest request = new AirMediaReceiverRequest();
            if (airPlayConfig.password.length() > 0) {
                request.setPassword(airPlayConfig.password);
            }

            if (audioStream == null) {
                try {
                    request.setAction(MediaAction.STOP);
                    request.setType(MediaType.VIDEO);
                    getApiManager().execute(request, airPlayConfig.name);
                } catch (FreeboxException e) {
                    logger.warn("Exception while stopping audio stream playback: {}", e.getMessage());
                }
            } else {
                String url = null;
                if (audioStream instanceof URLAudioStream) {
                    // it is an external URL, we can access it directly
                    URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                    url = urlAudioStream.getURL();
                } else {
                    // we serve it on our own HTTP server
                    String relativeUrl = "";
                    if (audioStream instanceof FixedLengthAudioStream) {
                        relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 20);
                    } else {
                        relativeUrl = audioHTTPServer.serve(audioStream);
                    }
                    url = callbackUrl + relativeUrl;
                }
                try {
                    audioStream.close();
                    try {
                        logger.debug("AirPlay audio sink: process url {}", url);
                        request.setAction(MediaAction.START);
                        request.setType(MediaType.VIDEO);
                        request.setMedia(url);
                        getApiManager().execute(request, airPlayConfig.name);
                    } catch (FreeboxException e) {
                        logger.warn("Audio stream playback failed: {}", e.getMessage());
                    }
                } catch (IOException e) {
                    logger.debug("Exception while closing audioStream", e);
                }
            }
        }

    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        if (SUPPORTED_FORMATS.isEmpty()) {
            SUPPORTED_FORMATS.addAll(BASIC_FORMATS);
            if (airPlayConfig.acceptAllMp3) {
                SUPPORTED_FORMATS.add(MP3);
            } else { // Only accept MP3 bitrates >= 96 kbps
                SUPPORTED_FORMATS.addAll(ALL_MP3_FORMATS);
            }
        }
        return SUPPORTED_FORMATS;
    }

    @Override
    public String getId() {
        return getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return getThing().getLabel();
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        logger.info("getVolume received but AirMedia does not have the capability - returning 100%.");
        return PercentType.HUNDRED;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        logger.info("setVolume received but AirMedia does not have the capability - ignoring it.");
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        // TODO Auto-generated method stub

    }

}
