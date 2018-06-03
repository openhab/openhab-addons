/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;
import org.openhab.voice.voicerss.internal.cloudapi.CachedVoiceRSSCloudImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for using VoiceRSS TTS service.
 *
 * @author Jochen Hiller - Initial contribution and API
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 */
public class VoiceRSSTTSService implements TTSService {

    /** Cache folder name is below userdata/voicerss/cache. */
    private static final String CACHE_FOLDER_NAME = "voicerss/cache";

    // API Key comes from ConfigAdmin
    private static final String CONFIG_API_KEY = "apiKey";
    private String apiKey = null;

    private final Logger logger = LoggerFactory.getLogger(VoiceRSSTTSService.class);

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private CachedVoiceRSSCloudImplementation voiceRssImpl;

    /**
     * Set of supported voices
     */
    private HashSet<Voice> voices;

    /**
     * Set of supported audio formats
     */
    private HashSet<AudioFormat> audioFormats;

    /**
     * DS activate, with access to ConfigAdmin
     */
    protected void activate(Map<String, Object> config) {
        try {
            modified(config);
            voiceRssImpl = initVoiceImplementation();
            voices = initVoices();
            audioFormats = initAudioFormats();

            logger.info("Using VoiceRSS cache folder {}", getCacheFolderName());
        } catch (Throwable t) {
            logger.error("Failed to activate VoiceRSS: {}", t.getMessage(), t);
        }
    }

    protected void modified(Map<String, Object> config) {
        if (config != null) {
            this.apiKey = config.containsKey(CONFIG_API_KEY) ? config.get(CONFIG_API_KEY).toString() : null;
        }
    }

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
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        // Validate known api key
        if (this.apiKey == null) {
            throw new TTSException("Missing API key, configure it first before using");
        }
        // Validate arguments
        // trim text
        text = text.trim();
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

        // now create the input stream for given text, locale, format. There is
        // only a default voice
        try {
            File cacheAudioFile = voiceRssImpl.getTextToSpeechAsFile(this.apiKey, text,
                    voice.getLocale().toLanguageTag(), getApiAudioFormat(requestedFormat));
            if (cacheAudioFile == null) {
                throw new TTSException("Could not read from VoiceRSS service");
            }
            AudioStream audioStream = new VoiceRSSAudioStream(cacheAudioFile, requestedFormat);
            return audioStream;
        } catch (AudioException ex) {
            throw new TTSException("Could not create AudioStream: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new TTSException("Could not read from VoiceRSS service: " + ex.getMessage(), ex);
        }
    }

    /**
     * Initializes this.voices.
     *
     * @return The voices of this instance
     */
    private final HashSet<Voice> initVoices() {
        HashSet<Voice> voices = new HashSet<Voice>();
        Set<Locale> locales = voiceRssImpl.getAvailableLocales();
        for (Locale local : locales) {
            Set<String> voiceLabels = voiceRssImpl.getAvailableVoices(local);
            for (String voiceLabel : voiceLabels) {
                voices.add(new VoiceRSSVoice(local, voiceLabel));
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
        Set<String> formats = voiceRssImpl.getAvailableAudioFormats();
        for (String format : formats) {
            audioFormats.add(getAudioFormat(format));
        }
        return audioFormats;
    }

    private final AudioFormat getAudioFormat(String apiFormat) {
        Boolean bigEndian = null;
        Integer bitDepth = 16;
        Integer bitRate = null;
        Long frequency = 44100L;

        if ("MP3".equals(apiFormat)) {
            // we use by default: MP3, 44khz_16bit_mono with bitrate 64 kbps
            bitRate = 64000;

            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, bigEndian, bitDepth, bitRate,
                    frequency);
        } else if ("OGG".equals(apiFormat)) {
            // we use by default: OGG, 44khz_16bit_mono

            return new AudioFormat(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_VORBIS, bigEndian, bitDepth, bitRate,
                    frequency);
        } else if ("AAC".equals(apiFormat)) {
            // we use by default: AAC, 44khz_16bit_mono

            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_AAC, bigEndian, bitDepth, bitRate,
                    frequency);
        } else {
            throw new IllegalArgumentException("Audio format " + apiFormat + " not yet supported");
        }
    }

    private final String getApiAudioFormat(AudioFormat format) {
        if (format.getCodec().equals(AudioFormat.CODEC_MP3)) {
            return "MP3";
        } else if (format.getCodec().equals(AudioFormat.CODEC_VORBIS)) {
            return "OGG";
        } else if (format.getCodec().equals(AudioFormat.CODEC_AAC)) {
            return "AAC";
        } else {
            throw new IllegalArgumentException("Audio format " + format.getCodec() + " not yet supported");
        }
    }

    private final CachedVoiceRSSCloudImplementation initVoiceImplementation() {
        CachedVoiceRSSCloudImplementation apiImpl = new CachedVoiceRSSCloudImplementation(getCacheFolderName());
        return apiImpl;
    }

    String getCacheFolderName() {
        String folderName = ConfigConstants.getUserDataFolder();
        // we assume that this folder does NOT have a trailing separator
        return folderName + File.separator + CACHE_FOLDER_NAME;
    }

    @Override
    public String getId() {
        return "voicerss";
    }

    @Override
    public String getLabel(Locale locale) {
        return "VoiceRSS Text-to-Speech Engine";
    }

}
