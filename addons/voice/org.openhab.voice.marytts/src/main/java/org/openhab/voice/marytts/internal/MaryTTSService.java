/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.marytts.internal;
/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
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
public class MaryTTSService implements TTSService {

    private final Logger logger = LoggerFactory.getLogger(MaryTTSService.class);

    private MaryInterface marytts;

    /**
     * Set of supported voices
     */
    private HashSet<org.eclipse.smarthome.core.voice.Voice> voices;

    /**
     * Set of supported audio formats
     */
    private HashSet<AudioFormat> audioFormats;

    protected void activate() {
        try {
            marytts = getMaryInterface();
            voices = initVoices();
            audioFormats = initAudioFormats();
        } catch (Throwable t) {
            logger.error("Failed to initialize MaryTTS: {}", t.getMessage(), t);
        }
    }

    @Override
    public Set<org.eclipse.smarthome.core.voice.Voice> getAvailableVoices() {
        return this.voices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    @Override
    public AudioStream synthesize(String text, org.eclipse.smarthome.core.voice.Voice voice,
            AudioFormat requestedFormat) throws TTSException {
        // Validate arguments
        if ((null == text) || text.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!this.voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        boolean isAudioFormatSupported = false;
        for (AudioFormat currentAudioFormat : this.audioFormats) {
            if (currentAudioFormat.isCompatible(requestedFormat)) {
                isAudioFormatSupported = true;
                break;
            }
        }
        if (!isAudioFormatSupported) {
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
            // AudioStream to return
            AudioStream audioStream = null;

            // Set voice (Each voice supports onl a single AudioFormat)
            marytts.setLocale(voice.getLocale());
            marytts.setVoice(voice.getLabel());

            try {
                AudioInputStream audioInputStream = marytts.generateAudio(text);
                audioStream = new MaryTTSAudioStream(audioInputStream, maryTTSVoiceAudioFormat);
            } catch (SynthesisException | IOException e) {
                throw new TTSException("Error generating an AudioStream", e);
            }

            return audioStream;
        }
    }

    /**
     * Initializes this.voices
     *
     * @return The voices of this instance
     */
    private final HashSet<org.eclipse.smarthome.core.voice.Voice> initVoices() {
        HashSet<org.eclipse.smarthome.core.voice.Voice> voices = new HashSet<org.eclipse.smarthome.core.voice.Voice>();
        Set<Locale> locales = marytts.getAvailableLocales();
        for (Locale local : locales) {
            Set<String> voiceLabels = marytts.getAvailableVoices(local);
            for (String voiceLabel : voiceLabels) {
                voices.add(new MaryTTSVoice(local, voiceLabel));
            }
        }
        return voices;
    }

    /**
     * Initializes this.audioFormats
     *
     * @return The audio formats of this instance
     */
    private final HashSet<AudioFormat> initAudioFormats() {
        HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();
        Set<String> voiceLabels = marytts.getAvailableVoices();
        for (String voiceLabel : voiceLabels) {
            Voice voice = Voice.getVoice(voiceLabel);
            audioFormats.add(getAudioFormat(voice.dbAudioFormat()));
        }
        return audioFormats;
    }

    /**
     * Obtains an AudioFormat from a javax.sound.sampled.AudioFormat
     *
     * @param audioFormat The javax.sound.sampled.AudioFormat
     * @return The corresponding AudioFormat
     */
    private final AudioFormat getAudioFormat(javax.sound.sampled.AudioFormat audioFormat) {
        String container = AudioFormat.CONTAINER_WAVE;

        String codec = audioFormat.getEncoding().toString();

        Boolean bigEndian = new Boolean(audioFormat.isBigEndian());

        int frameSize = audioFormat.getFrameSize(); // In bytes
        int bitsPerFrame = frameSize * 8;
        Integer bitDepth = ((AudioSystem.NOT_SPECIFIED == frameSize) ? null : new Integer(bitsPerFrame));

        float frameRate = audioFormat.getFrameRate();
        Integer bitRate = ((AudioSystem.NOT_SPECIFIED == frameRate) ? null
                : new Integer((int) (frameRate * bitsPerFrame)));

        float sampleRate = audioFormat.getSampleRate();
        Long frequency = ((AudioSystem.NOT_SPECIFIED == sampleRate) ? null : new Long((long) sampleRate));

        return new AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency);
    }

    /**
     * Create the MaryInterface
     *
     * @return The MaryInterface
     */
    private static final MaryInterface getMaryInterface() {
        MaryInterface maryInterface = null;
        try {
            maryInterface = new LocalMaryInterface();
        } catch (MaryConfigurationException e) {
            throw new RuntimeException("Error creating MaryInterface", e);
        }
        return maryInterface;
    }

    @Override
    public String getId() {
        return "marytts";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Mary Text-to-Speech Engine";
    }

}
