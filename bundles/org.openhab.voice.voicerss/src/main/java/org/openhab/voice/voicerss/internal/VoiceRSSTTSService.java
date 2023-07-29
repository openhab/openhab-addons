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
package org.openhab.voice.voicerss.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.osgi.service.component.annotations.Activate;
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
@NonNullByDefault
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

    private @Nullable String apiKey;

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private @Nullable CachedVoiceRSSCloudImpl voiceRssImpl;

    /**
     * Set of supported voices
     */
    private @Nullable Set<Voice> voices;

    /**
     * Set of supported audio formats
     */
    private @Nullable Set<AudioFormat> audioFormats;

    /**
     * DS activate, with access to ConfigAdmin
     */
    @Activate
    protected void activate(@Nullable Map<String, Object> config) {
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
    protected void modified(@Nullable Map<String, Object> config) {
        if (config != null) {
            apiKey = config.containsKey(CONFIG_API_KEY) ? config.get(CONFIG_API_KEY).toString() : null;
        }
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        Set<Voice> localVoices = voices;
        return localVoices == null ? Set.of() : Collections.unmodifiableSet(localVoices);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        Set<AudioFormat> localFormats = audioFormats;
        return localFormats == null ? Set.of() : Collections.unmodifiableSet(localFormats);
    }

    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        CachedVoiceRSSCloudImpl voiceRssCloud = voiceRssImpl;
        if (voiceRssCloud == null) {
            throw new TTSException("The service is not correctly initialized");
        }
        // Validate known api key
        String key = apiKey;
        if (key == null) {
            throw new TTSException("Missing API key, configure it first before using");
        }
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is empty");
        }
        Set<Voice> localVoices = voices;
        if (localVoices == null || !localVoices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }

        // now create the input stream for given text, locale, voice, codec and format.
        try {
            File cacheAudioFile = voiceRssCloud.getTextToSpeechAsFile(key, trimmedText,
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
     * @throws IllegalStateException if voiceRssImpl is null
     */
    private Set<Voice> initVoices() throws IllegalStateException {
        CachedVoiceRSSCloudImpl voiceRssCloud = voiceRssImpl;
        if (voiceRssCloud == null) {
            throw new IllegalStateException("The service is not correctly initialized");
        }
        Set<Voice> voices = new HashSet<>();
        for (Locale locale : voiceRssCloud.getAvailableLocales()) {
            for (String voiceLabel : voiceRssCloud.getAvailableVoices(locale)) {
                voices.add(new VoiceRSSVoice(locale, voiceLabel));
            }
        }
        return voices;
    }

    /**
     * Initializes audioFormats
     *
     * @return The audio formats of this instance
     * @throws IllegalStateException if voiceRssImpl is null
     */
    private Set<AudioFormat> initAudioFormats() throws IllegalStateException {
        CachedVoiceRSSCloudImpl voiceRssCloud = voiceRssImpl;
        if (voiceRssCloud == null) {
            throw new IllegalStateException("The service is not correctly initialized");
        }
        Set<AudioFormat> audioFormats = new HashSet<>();
        for (String codec : voiceRssCloud.getAvailableAudioCodecs()) {
            switch (codec) {
                case "MP3":
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, 16, 64000,
                            44_100L));
                    break;
                case "OGG":
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_VORBIS, null, 16,
                            null, 44_100L));
                    break;
                case "AAC":
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_AAC, null, 16, null,
                            44_100L));
                    break;
                case "WAV":
                    // Consider only mono formats
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 64_000, 8_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 128_000, 8_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 88_200, 11_025L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 176_400, 11_025L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 96_000, 12_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 192_000, 12_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 128_000, 16_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 256_000, 16_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 176_400, 22_050L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 352_800, 22_050L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 192_000, 24_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 384_000, 24_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 256_000, 32_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 512_000, 32_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 352_800, 44_100L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 705_600, 44_100L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_UNSIGNED, false,
                            8, 384_000, 48_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false,
                            16, 768_000, 48_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ALAW, null, 8,
                            64_000, 8_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ALAW, null, 8,
                            88_200, 11_025L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ALAW, null, 8,
                            176_400, 22_050L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ALAW, null, 8,
                            352_800, 44_100L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ULAW, null, 8,
                            64_000, 8_000L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ULAW, null, 8,
                            88_200, 11_025L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ULAW, null, 8,
                            176_400, 22_050L));
                    audioFormats.add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_ULAW, null, 8,
                            352_800, 44_100L));
                    break;
                default:
                    logger.debug("Audio codec {} not yet supported", codec);
                    break;
            }
        }
        return audioFormats;
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
        final Integer formatBitDepth = format.getBitDepth();
        final int bitDepth = formatBitDepth != null ? formatBitDepth.intValue() : 16;
        final Long formatFrequency = format.getFrequency();
        final Long frequency = formatFrequency != null ? formatFrequency.longValue() : 44_100L;
        final String apiFrequency = FREQUENCY_MAP.get(frequency);

        if (apiFrequency == null || (bitDepth != 8 && bitDepth != 16)) {
            throw new TTSException("Unsupported audio format: " + format);
        }

        String codec = format.getCodec();
        switch (codec != null ? codec : AudioFormat.CODEC_PCM_SIGNED) {
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
        return new CachedVoiceRSSCloudImpl(getCacheFolderName(), true);
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
    public String getLabel(@Nullable Locale locale) {
        return "VoiceRSS";
    }
}
