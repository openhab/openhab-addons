/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.sound.internal;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.io.javasound.internal.JavaSoundAudioSink;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * This is an audio sink that is registered as a service, which can play mp3 files to the hosts outputs (e.g. speaker,
 * line-out).
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - Refactored and moved to openHAB
 */
@Component(service = AudioSink.class, immediate = true)
public class EnhancedJavaSoundAudioSink extends JavaSoundAudioSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedJavaSoundAudioSink.class);

    private static final AudioFormat MP3 = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null,
            null, null, null);
    private static final AudioFormat WAV = new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED,
            null, null, null, null);
    private static final Set<AudioFormat> SUPPORTED_FORMATS = Collections
            .unmodifiableSet(Stream.of(MP3, WAV).collect(toSet()));

    private static Player streamPlayer = null;

    @Override
    public String getId() {
        return "enhancedjavasound";
    }

    @Override
    public String getLabel(Locale locale) {
        return "System Speaker (with mp3 support)";
    }

    @Override
    public synchronized void process(final AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream != null && audioStream.getFormat().getCodec() != AudioFormat.CODEC_MP3) {
            // we can only deal with mp3, so delegate the rest
            super.process(audioStream);
        } else {
            if (audioStream == null || audioStream instanceof URLAudioStream) {
                // we are dealing with an infinite stream here
                if (streamPlayer != null) {
                    // if we are already playing a stream, stop it first
                    streamPlayer.close();
                    streamPlayer = null;
                }
                if (audioStream == null) {
                    // the call was only for stopping the currently playing stream
                    return;
                } else {
                    try {
                        // we start a new continuous stream and store its handle
                        streamPlayer = new Player(audioStream);
                        playInThread(streamPlayer);
                    } catch (JavaLayerException e) {
                        LOGGER.error("An exception occurred while playing url audio stream : '{}'", e.getMessage());
                    }
                    return;
                }
            } else {
                // we are playing some normal file (no url stream)
                try {
                    playInThread(new Player(audioStream));
                } catch (JavaLayerException e) {
                    LOGGER.error("An exception occurred while playing audio : '{}'", e.getMessage());
                }
            }
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    private void playInThread(final Player player) {
        // run in new thread
        new Thread(() -> {
            try {
                player.play();
            } catch (Exception e) {
                LOGGER.error("An exception occurred while playing audio : '{}'", e.getMessage());
            } finally {
                player.close();
            }
        }).start();
    }

    protected synchronized void deactivate() {
        if (streamPlayer != null) {
            // stop playing streams on shutdown
            streamPlayer.close();
            streamPlayer = null;
        }
    }
}
