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
package org.openhab.voice.mactts.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.voice.AbstractCachedTTSService;
import org.openhab.core.voice.TTSCache;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for Mac OS, which simply uses the "say" command from the OS.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Pauli Antilla
 * @author Kelly Davis
 * @author Laurent Garnier : Implement TTS LRU cache
 */
@Component(service = TTSService.class)
@NonNullByDefault
public class MacTTSService extends AbstractCachedTTSService {

    private final Logger logger = LoggerFactory.getLogger(MacTTSService.class);

    @Activate
    public MacTTSService(final @Reference TTSCache ttsCache) {
        super(ttsCache);
    }

    /**
     * Set of supported voices
     */
    private final Set<Voice> voices = initVoices();

    /**
     * Set of supported audio formats
     */
    private final Set<AudioFormat> audioFormats = Set.of(
            new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, (long) 44100));

    @Override
    public Set<Voice> getAvailableVoices() {
        return voices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return audioFormats;
    }

    @Override
    public AudioStream synthesizeForCache(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        // Validate arguments
        if (text.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        boolean isAudioFormatSupported = audioFormats.stream()
                .anyMatch(audioFormat -> audioFormat.isCompatible(requestedFormat));
        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        try {
            return new MacTTSAudioStream(text, voice, requestedFormat);
        } catch (AudioException e) {
            throw new TTSException(e);
        }
    }

    /**
     * Initializes this.voices
     *
     * @return The voices of this instance
     */
    private final Set<Voice> initVoices() {
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                return bufferedReader.lines().map(MacTTSVoice::new).collect(Collectors.toSet());
            }
        } catch (IOException e) {
            logger.error("Error while executing the 'say -v ?' command: {}", e.getMessage());
            return Set.of();
        }
    }

    @Override
    public String getId() {
        return "mactts";
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "macOS TTS";
    }
}
