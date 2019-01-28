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
package org.openhab.voice.pollytts.internal.cloudapi;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.Voice;

/**
 * This class implements the Cloud service for PollyTTS.
 *
 * The implementation supports:
 * <ul>
 * <li>All languages</li>
 * <li>All voices</li>
 * <li>MP3 and OGG formats</li>
 * </ul>
 *
 * @author Robert Hillman - Initial contribution
 */
public class PollyTTSCloudImpl {

    private static final Set<String> SUPPORTED_AUDIO_FORMATS = Collections
            .unmodifiableSet(Stream.of("MP3", "OGG").collect(toSet()));

    /**
     * Get all supported audio formats by the TTS service. This includes MP3,
     * WAV and more audio formats as used in APIs.
     */
    public Set<String> getAvailableAudioFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    // Get all supported locales by the TTS service
    public Set<Locale> getAvailableLocales() {
        Set<Locale> supportedLocales = new HashSet<>();
        for (Voice voice : PollyClientConfig.pollyVoices) {
            supportedLocales.add(Locale.forLanguageTag(voice.getLanguageCode()));
        }
        return supportedLocales;
    }

    // Get all supported voices.
    public Set<String> getAvailableVoices() {
        Set<String> supportedVoices = new HashSet<>();
        for (Voice voice : PollyClientConfig.pollyVoices) {
            supportedVoices.add(voice.getName());
        }
        return supportedVoices;
    }

    // Get all supported voices for a specified locale.
    public Set<String> getAvailableVoices(Locale locale) {
        Set<String> localeVoices = new HashSet<>();
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
        String voiceID = PollyClientConfig.labelToID.get(label);
        String format = audioFormat.toLowerCase();
        if (audioFormat.equals("ogg")) {
            format = "ogg_vorbis";
        }
        TextType textType = text.startsWith("<speak>") ? TextType.Ssml : TextType.Text;
        SynthesizeSpeechRequest request = new SynthesizeSpeechRequest().withTextType(textType).withText(text)
                .withVoiceId(voiceID).withOutputFormat(OutputFormat.fromValue(format));
        return PollyClientConfig.pollyClientInterface.synthesizeSpeech(request).getAudioStream();
    }

}
