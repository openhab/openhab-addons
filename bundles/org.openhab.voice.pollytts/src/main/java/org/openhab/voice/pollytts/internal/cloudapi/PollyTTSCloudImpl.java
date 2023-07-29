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
package org.openhab.voice.pollytts.internal.cloudapi;

import static java.util.stream.Collectors.*;
import static org.openhab.core.audio.AudioFormat.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
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
            .unmodifiableSet(Stream.of(CODEC_MP3, CONTAINER_OGG).collect(toSet()));

    protected final PollyTTSConfig config;

    private final AmazonPolly client;
    private final Map<String, String> labelToID;
    private final List<Voice> voices;

    public PollyTTSCloudImpl(PollyTTSConfig config) {
        this.config = config;

        AWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        client = AmazonPollyClientBuilder.standard().withRegion(config.getServiceRegion())
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        voices = client.describeVoices(new DescribeVoicesRequest()).getVoices();

        // create voice to ID translation for service invocation
        labelToID = voices.stream().collect(toMap(Voice::getName, Voice::getId));
    }

    /**
     * Get all supported audio formats by the TTS service. This includes MP3,
     * WAV and more audio formats as used in APIs.
     */
    public Set<String> getAvailableAudioFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    public Set<Locale> getAvailableLocales() {
        // @formatter:off
        return voices.stream()
                .map(voice -> Locale.forLanguageTag(voice.getLanguageCode()))
                .collect(toSet());
        // @formatter:on
    }

    public Set<String> getAvailableVoices() {
        // @formatter:off
        return voices.stream()
                .map(Voice::getName)
                .collect(toSet());
        // @formatter:on
    }

    public Set<String> getAvailableVoices(Locale locale) {
        // @formatter:off
        return voices.stream()
                .filter(voice -> voice.getLanguageCode().equalsIgnoreCase(locale.toLanguageTag()))
                .map(Voice::getName)
                .collect(toSet());
        // @formatter:on
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
        String voiceID = labelToID.get(label);
        String format = audioFormat.toLowerCase();
        if ("ogg".equals(format)) {
            format = "ogg_vorbis";
        }
        TextType textType = text.startsWith("<speak>") ? TextType.Ssml : TextType.Text;
        SynthesizeSpeechRequest request = new SynthesizeSpeechRequest().withTextType(textType).withText(text)
                .withVoiceId(voiceID).withOutputFormat(OutputFormat.fromValue(format));
        return client.synthesizeSpeech(request).getAudioStream();
    }
}
