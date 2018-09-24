/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.picotts.internal;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;
import org.osgi.service.component.annotations.Component;

/**
 * @author Florian Schmidt - Initial Contribution
 */
@Component
public class PicoTTSService implements TTSService {
    private final Set<Voice> voices = Stream
            .of(new PicoTTSVoice("de-DE"), new PicoTTSVoice("en-US"), new PicoTTSVoice("en-GB"),
                    new PicoTTSVoice("es-ES"), new PicoTTSVoice("fr-FR"), new PicoTTSVoice("it-IT"))
            .collect(Collectors.toSet());

    private final Set<AudioFormat> audioFormats = Collections.singleton(
            new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L));

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
            throw new TTSException("The passed voice is unsupported");
        }

        boolean isAudioFormatSupported = this.audioFormats.stream().anyMatch(audioFormat -> {
            return audioFormat.isCompatible(requestedFormat);
        });

        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        try {
            return new PicoTTSAudioStream(text, voice, requestedFormat);
        } catch (AudioException e) {
            throw new TTSException(e);
        }
    }

    @Override
    public String getId() {
        return "picotts";
    }

    @Override
    public String getLabel(Locale locale) {
        return "PicoTTS";
    }

}
