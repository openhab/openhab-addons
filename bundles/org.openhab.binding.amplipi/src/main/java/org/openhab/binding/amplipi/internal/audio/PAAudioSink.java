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
package org.openhab.binding.amplipi.internal.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.AmpliPiHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSinkSync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.StreamServed;
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
public class PAAudioSink extends AudioSinkSync implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(PAAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Set.of(AudioStream.class);

    private @Nullable AmpliPiHandler handler;

    private @Nullable PercentType volume;

    @Override
    protected void processSynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.debug("AmpliPi sink does not support stopping the currently playing stream.");
            return;
        }
        AmpliPiHandler localHandler = this.handler;
        if (localHandler == null) {
            tryClose(audioStream);
            return;
        }
        logger.debug("Received audio stream of format {}", audioStream.getFormat());
        String callbackUrl = localHandler.getCallbackUrl();
        String audioUrl;
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, so we can directly pass this on.
            audioUrl = urlAudioStream.getURL();
            tryClose(audioStream);
        } else if (callbackUrl != null) {
            // we need to serve it for a while
            StreamServed streamServed;
            try {
                streamServed = localHandler.getAudioHTTPServer().serve(audioStream, 10, true);
            } catch (IOException e) {
                tryClose(audioStream);
                throw new UnsupportedAudioStreamException(
                        "AmpliPi was not able to handle the audio stream (cache on disk failed).",
                        audioStream.getClass(), e);
            }
            audioUrl = callbackUrl + streamServed.url();
        } else {
            logger.warn("We do not have any callback url, so AmpliPi cannot play the audio stream!");
            tryClose(audioStream);
            return;
        }
        localHandler.playPA(audioUrl, volume);
        // we reset the volume value again, so that a next invocation without a volume will again use the zones
        // defaults.
        volume = null;
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
