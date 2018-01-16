/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.pico2wave.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;

/**
 * @author Florian Schmidt
 */
public class Pico2WaveService implements TTSService {
    private final Set<Voice> voices = Arrays
            .asList(new Pico2WaveVoice("de-DE"), new Pico2WaveVoice("en-US"), new Pico2WaveVoice("en-GB"),
                    new Pico2WaveVoice("es-ES"), new Pico2WaveVoice("fr-FR"), new Pico2WaveVoice("it-IT"))
            .stream().collect(Collectors.toSet());

    private final Set<AudioFormat> audioFormats = Collections.singleton(
            new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, null));

    @Override
    public Set<Voice> getAvailableVoices() {
        return this.voices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        if (text == null || text.isEmpty()) {
            throw new TTSException("The passed text can not be null or empty");
        }

        if (!this.voices.contains(voice)) {
            throw new TTSException("The passed voice is no supported");
        }

        boolean isAudioFormatSupported = this.audioFormats.stream().anyMatch(audioFormat -> {
            return audioFormat.isCompatible(requestedFormat);
        });

        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        try {
            return new Pico2WaveAudioStream(text, voice, requestedFormat);
        } catch (AudioException e) {
            throw new TTSException(e);
        }
    }

    @Override
    public String getId() {
        return "pico2wave";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Pico2Wave TTS";
    }

}
