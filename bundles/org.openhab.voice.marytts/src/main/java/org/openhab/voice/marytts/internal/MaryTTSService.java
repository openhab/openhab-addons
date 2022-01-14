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
package org.openhab.voice.marytts.internal;

import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.modules.synthesis.Voice;

/**
 * This is a TTS service implementation for using MaryTTS.
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to updated APIs and moved to openHAB
 */
@Component
public class MaryTTSService implements TTSService {

    private final Logger logger = LoggerFactory.getLogger(MaryTTSService.class);

    private MaryInterface marytts;

    /**
     * Set of supported voices
     */
    private Set<org.openhab.core.voice.Voice> voices;

    /**
     * Set of supported audio formats
     */
    private Set<AudioFormat> audioFormats;

    protected void activate() {
        try {
            marytts = new LocalMaryInterface();
            voices = initVoices();
            audioFormats = initAudioFormats();
        } catch (MaryConfigurationException e) {
            logger.error("Failed to initialize MaryTTS: {}", e.getMessage(), e);
        }
    }

    @Override
    public Set<org.openhab.core.voice.Voice> getAvailableVoices() {
        return voices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return audioFormats;
    }

    @Override
    public AudioStream synthesize(String text, org.openhab.core.voice.Voice voice, AudioFormat requestedFormat)
            throws TTSException {
        // Validate arguments
        if (text == null || text.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        if (audioFormats.stream().noneMatch(f -> f.isCompatible(requestedFormat))) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        /*
         * NOTE: For each MaryTTS voice only a single AudioFormat is supported
         * However, the TTSService interface allows the AudioFormat and
         * the Voice to vary independently. Thus, an external user does
         * not know about the requirement that a given voice is paired
         * with a given AudioFormat. The test below enforces this.
         *
         * However, this leads to a problem. The user has no way to
         * know which AudioFormat is apropos for a give Voice. Thus,
         * throwing a TTSException for the wrong AudioFormat makes
         * the user guess the right AudioFormat, a painful process.
         * Alternatively, we can get the right AudioFormat for the
         * Voice and ignore what the user requests, also wrong.
         *
         * TODO: Decide what to do
         * Voice maryTTSVoice = Voice.getVoice(voice.getLabel());
         * AudioFormat maryTTSVoiceAudioFormat = getAudioFormat(maryTTSVoice.dbAudioFormat());
         * if (!maryTTSVoiceAudioFormat.isCompatible(requestedFormat)) {
         * throw new TTSException("The passed AudioFormat is incompatable with the voice");
         * }
         */
        Voice maryTTSVoice = Voice.getVoice(voice.getLabel());
        AudioFormat maryTTSVoiceAudioFormat = getAudioFormat(maryTTSVoice.dbAudioFormat());

        // Synchronize on marytts
        synchronized (marytts) {
            // Set voice (Each voice supports only a single AudioFormat)
            marytts.setLocale(voice.getLocale());
            marytts.setVoice(voice.getLabel());

            try {
                return new MaryTTSAudioStream(marytts.generateAudio(text), maryTTSVoiceAudioFormat);
            } catch (SynthesisException | IOException e) {
                throw new TTSException("Error generating an AudioStream", e);
            }
        }
    }

    /**
     * Initializes voices
     *
     * @return The voices of this instance
     */
    private Set<org.openhab.core.voice.Voice> initVoices() {
        Set<org.openhab.core.voice.Voice> voices = new HashSet<>();
        for (Locale locale : marytts.getAvailableLocales()) {
            for (String voiceLabel : marytts.getAvailableVoices(locale)) {
                voices.add(new MaryTTSVoice(locale, voiceLabel));
            }
        }
        return voices;
    }

    /**
     * Initializes audioFormats
     *
     * @return The audio formats of this instance
     */
    private Set<AudioFormat> initAudioFormats() {
        Set<AudioFormat> audioFormats = new HashSet<>();
        for (String voiceLabel : marytts.getAvailableVoices()) {
            audioFormats.add(getAudioFormat(Voice.getVoice(voiceLabel).dbAudioFormat()));
        }
        return audioFormats;
    }

    /**
     * Obtains an AudioFormat from a javax.sound.sampled.AudioFormat
     *
     * @param audioFormat The javax.sound.sampled.AudioFormat
     * @return The corresponding AudioFormat
     */
    private AudioFormat getAudioFormat(javax.sound.sampled.AudioFormat audioFormat) {
        String container = AudioFormat.CONTAINER_WAVE;
        String codec = audioFormat.getEncoding().toString();
        Boolean bigEndian = audioFormat.isBigEndian();

        int frameSize = audioFormat.getFrameSize(); // In bytes
        int bitsPerFrame = frameSize * 8;
        Integer bitDepth = NOT_SPECIFIED == frameSize ? null : bitsPerFrame;

        float frameRate = audioFormat.getFrameRate();
        Integer bitRate = NOT_SPECIFIED == frameRate ? null : (int) frameRate * bitsPerFrame;

        float sampleRate = audioFormat.getSampleRate();
        Long frequency = NOT_SPECIFIED == sampleRate ? null : (long) sampleRate;

        return new AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency);
    }

    @Override
    public String getId() {
        return "marytts";
    }

    @Override
    public String getLabel(Locale locale) {
        return "MaryTTS";
    }
}
