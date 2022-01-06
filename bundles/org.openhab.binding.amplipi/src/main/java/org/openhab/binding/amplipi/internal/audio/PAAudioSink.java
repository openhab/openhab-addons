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
package org.openhab.binding.amplipi.internal.audio;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.AmpliPiHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an audio sink that allows to do public announcements on the AmpliPi.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public class PAAudioSink implements AudioSink, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(PAAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Set
            .of(FixedLengthAudioStream.class, URLAudioStream.class);

    private @Nullable AmpliPiHandler handler;

    private @Nullable PercentType volume;

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.debug("Web Audio sink does not support stopping the currently playing stream.");
            return;
        }
        AmpliPiHandler localHandler = this.handler;
        if (localHandler != null) {
            try (AudioStream stream = audioStream) {
                logger.debug("Received audio stream of format {}", audioStream.getFormat());
                String audioUrl;
                if (audioStream instanceof URLAudioStream) {
                    // it is an external URL, so we can directly pass this on.
                    URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                    audioUrl = urlAudioStream.getURL();
                } else if (audioStream instanceof FixedLengthAudioStream) {
                    String callbackUrl = localHandler.getCallbackUrl();
                    if (callbackUrl == null) {
                        throw new UnsupportedAudioStreamException(
                                "Cannot play audio since no callback url is available.", audioStream.getClass());
                    } else {
                        // we need to serve it for a while, hence only
                        // FixedLengthAudioStreams are supported.
                        String relativeUrl = localHandler.getAudioHTTPServer()
                                .serve((FixedLengthAudioStream) audioStream, 10).toString();
                        audioUrl = callbackUrl + relativeUrl;
                    }
                } else {
                    throw new UnsupportedAudioStreamException(
                            "Web audio sink can only handle FixedLengthAudioStreams and URLAudioStreams.",
                            audioStream.getClass());
                }
                localHandler.playPA(audioUrl, volume);
                // we reset the volume value again, so that a next invocation without a volume will again use the zones
                // defaults.
                volume = null;
            } catch (IOException e) {
                logger.debug("Error while closing the audio stream: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_AUDIO_STREAMS;
    }

    @Override
    public String getId() {
        if (handler != null) {
            return handler.getThing().getUID().toString();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        if (handler != null) {
            return handler.getThing().getLabel();
        } else {
            return null;
        }
    }

    @Override
    public PercentType getVolume() throws IOException {
        PercentType vol = volume;
        if (vol != null) {
            return vol;
        } else {
            throw new IOException("Audio sink does not support reporting the volume.");
        }
    }

    @Override
    public void setVolume(final PercentType volume) throws IOException {
        this.volume = volume;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (AmpliPiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
