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
package org.openhab.voice.voicerss.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.openhab.voice.voicerss.internal.cloudapi.CachedVoiceRSSCloudImpl;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for using VoiceRSS TTS service.
 *
 * @author Jochen Hiller - Initial contribution and API
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 */
@Component(configurationPid = "org.openhab.voicerss", property = Constants.SERVICE_PID + "=org.openhab.voicerss")
@ConfigurableService(category = "voice", label = "VoiceRSS Text-to-Speech", description_uri = "voice:voicerss")
public class VoiceRSSTTSService implements TTSService {

    /** Cache folder name is below userdata/voicerss/cache. */
    private static final String CACHE_FOLDER_NAME = "voicerss" + File.separator + "cache";

    // API Key comes from ConfigAdmin
    private static final String CONFIG_API_KEY = "apiKey";

    /**
     * Map from openHAB AudioFormat Codec to VoiceRSS API Audio Codec
     */
    private static final Map<String, String> CODEC_MAP = Map.of(AudioFormat.CODEC_PCM_SIGNED, "WAV",
            AudioFormat.CODEC_PCM_UNSIGNED, "WAV", AudioFormat.CODEC_PCM_ALAW, "WAV", AudioFormat.CODEC_PCM_ULAW, "WAV",
            AudioFormat.CODEC_MP3, "MP3", AudioFormat.CODEC_VORBIS, "OGG", AudioFormat.CODEC_AAC, "AAC");

    /**
     * Map from openHAB AudioFormat Frequency to VoiceRSS API Audio Frequency
     */
    private static final Map<Long, String> FREQUENCY_MAP = Map.of(8_000L, "8khz", 11_025L, "11khz", 12_000L, "12khz",
            16_000L, "16khz", 22_050L, "22khz", 24_000L, "24khz", 32_000L, "32khz", 44_100L, "44khz", 48_000L, "48khz");

    private final Logger logger = LoggerFactory.getLogger(VoiceRSSTTSService.class);

    private String apiKey;

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private CachedVoiceRSSCloudImpl voiceRssImpl;

    /**
     * Set of supported voices
     */
    private Set<Voice> voices;

    /**
     * Set of supported audio formats
     */
    private Set<AudioFormat> audioFormats;

    /**
     * DS activate, with access to ConfigAdmin
     */
    protected void activate(Map<String, Object> config) {
        try {
            modified(config);
            voiceRssImpl = initVoiceImplementation();
            voices = initVoices();
            audioFormats = initAudioFormats();

            logger.debug("Using VoiceRSS cache folder {}", getCacheFolderName());
        } catch (IllegalStateException e) {
            logger.warn("Failed to activate VoiceRSS: {}", e.getMessage(), e);
        }
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        if (config != null) {
            apiKey = config.containsKey(CONFIG_API_KEY) ? config.get(CONFIG_API_KEY).toString() : null;
        }
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        return Collections.unmodifiableSet(voices);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Collections.unmodifiableSet(audioFormats);
    }

    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        // Validate known api key
        if (apiKey == null) {
            throw new TTSException("Missing API key, configure it first before using");
        }
        // Validate arguments
        if (text == null) {
            throw new TTSException("The passed text is null");
        }
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is empty");
        }
        if (!voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }

        // now create the input stream for given text, locale, voice, codec and format.
        try {
            File cacheAudioFile = voiceRssImpl.getTextToSpeechAsFile(apiKey, trimmedText,
                    voice.getLocale().toLanguageTag(), voice.getLabel(), getApiAudioCodec(requestedFormat),
                    getApiAudioFormat(requestedFormat));
            return new VoiceRSSAudioStream(cacheAudioFile, requestedFormat);
        } catch (AudioException ex) {
            throw new TTSException("Could not create AudioStream: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new TTSException("Could not read from VoiceRSS service: " + ex.getMessage(), ex);
        }
    }

    /**
     * Initializes voices.
     *
     * @return The voices of this instance
     */
    private Set<Voice> initVoices() {
        Set<Voice> voices = new HashSet<>();
        for (Locale locale : voiceRssImpl.getAvailableLocales()) {
            for (String voiceLabel : voiceRssImpl.getAvailableVoices(locale)) {
                voices.add(new VoiceRSSVoice(locale, voiceLabel));
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
        return voiceRssImpl.getAvailableAudioFormats();
    }

    /**
     * Map {@link AudioFormat#getCodec() codec} to VoiceRSS API codec.
     *
     * @throws TTSException if {@code format} is not supported
     */
    private String getApiAudioCodec(AudioFormat format) throws TTSException {
        final String internalCodec = format.getCodec();
        final String apiCodec = CODEC_MAP.get(internalCodec != null ? internalCodec : AudioFormat.CODEC_PCM_SIGNED);

        if (apiCodec == null) {
            throw new TTSException("Unsupported audio format: " + format);
        }

        return apiCodec;
    }

    /**
     * Map {@link AudioFormat#getBitDepth() bit depth} and {@link AudioFormat#getFrequency() frequency} to VoiceRSS API
     * format.
     *
     * @throws TTSException if {@code format} is not supported
     */
    private String getApiAudioFormat(AudioFormat format) throws TTSException {
        final int bitDepth = format.getBitDepth() != null ? format.getBitDepth() : 16;
        final Long frequency = format.getFrequency() != null ? format.getFrequency() : 44_100L;
        final String apiFrequency = FREQUENCY_MAP.get(frequency);

        if (apiFrequency == null || (bitDepth != 8 && bitDepth != 16)) {
            throw new TTSException("Unsupported audio format: " + format);
        }

        switch (format.getCodec() != null ? format.getCodec() : AudioFormat.CODEC_PCM_SIGNED) {
            case AudioFormat.CODEC_PCM_ALAW:
                return "alaw_" + apiFrequency + "_mono";
            case AudioFormat.CODEC_PCM_ULAW:
                return "ulaw_" + apiFrequency + "_mono";
            case AudioFormat.CODEC_PCM_SIGNED:
            case AudioFormat.CODEC_PCM_UNSIGNED:
            case AudioFormat.CODEC_MP3:
            case AudioFormat.CODEC_VORBIS:
            case AudioFormat.CODEC_AAC:
                return apiFrequency + "_" + bitDepth + "bit_mono";
            default:
                throw new TTSException("Unsupported audio format: " + format);
        }
    }

    private CachedVoiceRSSCloudImpl initVoiceImplementation() throws IllegalStateException {
        return new CachedVoiceRSSCloudImpl(getCacheFolderName());
    }

    private String getCacheFolderName() {
        // we assume that this folder does NOT have a trailing separator
        return OpenHAB.getUserDataFolder() + File.separator + CACHE_FOLDER_NAME;
    }

    @Override
    public String getId() {
        return "voicerss";
    }

    @Override
    public String getLabel(Locale locale) {
        return "VoiceRSS";
    }
}
