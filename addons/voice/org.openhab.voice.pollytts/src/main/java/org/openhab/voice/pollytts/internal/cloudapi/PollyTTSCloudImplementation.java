/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.pollytts.internal.cloudapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;
import com.amazonaws.services.polly.model.TextType;

/**
 * This class implements the Cloud service from PollyTTS. For more information,
 *
 * Current state of implementation:
 * <ul>
 * <li>All API languages supported</li>
 * <li>Only all voices supported</li>
 * <li>Only MP3 formats supported (TBD: 3 other formats)</li>
 * </ul>
 *
 *   work derived for voicetts as sample
 * 
 * @author Robert Hillman - adapted and implemented Polly service interface
 **/

public class PollyTTSCloudImplementation {

    // private final Logger logger = LoggerFactory.getLogger(PollyTTSCloudImplementation.class);

    private static Set<String> supportedAudioFormats = new HashSet<String>();
    static {
        supportedAudioFormats.add("MP3");
        supportedAudioFormats.add("OGG");
    }

    /**
     * Get all supported audio formats by the TTS service. This includes MP3,
     * WAV and more audio formats as used in APIs.
     */
    public Set<String> getAvailableAudioFormats() {
        return supportedAudioFormats;
    }

    // Get all supported locales by the TTS service
    public Set<Locale> getAvailableLocales() {
        Set<Locale> supportedLocales = new HashSet<Locale>();
        for (Voice voice : PollyClientConfig.pollyVoices) {
            supportedLocales.add(Locale.forLanguageTag(voice.getLanguageCode()));
        }
        return supportedLocales;
    }

    // Get all supported voices.
    public Set<String> getAvailableVoices() {
        Set<String> supportedVoices = new HashSet<String>();
        for (Voice voice : PollyClientConfig.pollyVoices) {
            supportedVoices.add(voice.getName());
        }
        return supportedVoices;
    }

    // Get all supported voices for a specified locale.
    public Set<String> getAvailableVoices(Locale locale) {
        Set<String> localeVoices = new HashSet<String>();
        for (Voice voice : PollyClientConfig.pollyVoices) {
            if (voice.getLanguageCode().equalsIgnoreCase(locale.toLanguageTag())) {
                localeVoices.add(voice.getName());
            }
        }
        return localeVoices;
    }

    /**
     * This method will return an input stream to an audio stream for the given
     * parameters.
     * Get the given text in specified locale and audio format as input stream.
     *
     * @param text
     *            the text to translate into speech
     * @param label
     *            the voice Label to use
     * @param audioFormat
     *            the audio format to use
     * @return an InputStream to the audio data in specified format
     * @throws IOException
     *             will be raised if the audio data can not be retrieved from
     *             cloud service
     */
    public InputStream getTextToSpeech(String text, String label, String audioFormat) {
        InputStream is = null;
        String voiceID = PollyClientConfig.labelToID.get(label);
        String format = audioFormat.toLowerCase();
        if (audioFormat.equals("ogg")) {
            format = "ogg_vorbis";
        }
        TextType textType = text.startsWith("<speak>") ? TextType.Ssml : TextType.Text;
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest().withTextType(textType).withText(text).withVoiceId(voiceID).withOutputFormat(OutputFormat.fromValue(format));
        SynthesizeSpeechResult synthRes = PollyClientConfig.pollyClientInterface.synthesizeSpeech(synthReq);
        is = synthRes.getAudioStream();
        return is;
    }

}
