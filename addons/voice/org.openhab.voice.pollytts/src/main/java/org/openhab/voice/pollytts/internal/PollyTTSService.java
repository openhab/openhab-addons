/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.pollytts.internal;

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
import org.openhab.voice.pollytts.internal.cloudapi.CachedPollyTTSCloudImplementation;
import org.openhab.voice.pollytts.internal.cloudapi.PollyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for using PollyTTS TTS service.
 *
 * @author Jochen Hiller - Initial contribution and API
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 * @author Robert Hillman - converted to Polly API, added polly voice interface, only MP3 audio currently
 */
public class PollyTTSService implements TTSService {

    /** Cache folder name is below userdata/polly/cache. */
    private static final String CACHE_FOLDER_NAME = "pollytts/cache";

    private final Logger logger = LoggerFactory.getLogger(PollyTTSService.class);

    // Keys come from ConfigAdmin
    private static final String CONFIG_ACCESS_KEY = "accessKey";
    private static final String CONFIG_SECRET_KEY = "secretKey";
    private static final String CONFIG_REGION = "serviceRegion";
    private static final String CONFIG_EXPIRE = "cacheExpiration";
    private static final String CONFIG_FORMAT = "audioFormat";

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private CachedPollyTTSCloudImplementation pollyTssImpl;

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
        modified(config);
    }

    protected void modified(Map<String, Object> config) {
        try {
            boolean successful = false;
            if (config != null) {
                PollyClientConfig polly = new PollyClientConfig();
                String val = null;
                val = config.containsKey(CONFIG_ACCESS_KEY) ? config.get(CONFIG_ACCESS_KEY).toString() : null;
                polly.setAccessKey(val);
                val = config.containsKey(CONFIG_SECRET_KEY) ? config.get(CONFIG_SECRET_KEY).toString() : null;
                polly.setSecretKey(val);
                val = config.containsKey(CONFIG_REGION) ? config.get(CONFIG_REGION).toString() : null;
                polly.setRegionVal(val);
                val = config.containsKey(CONFIG_FORMAT) ? config.get(CONFIG_FORMAT).toString() : "default";
                polly.setAudioFormat(val);
                val = config.containsKey(CONFIG_EXPIRE) ? config.get(CONFIG_EXPIRE).toString() : "0";
                polly.setExpireDate((int) Double.parseDouble(val));
                successful = polly.initPollyServiceInterface();
            }
            if (successful) {
                pollyTssImpl = initVoiceImplementation();
                audioFormats = initAudioFormats();
                voices = initVoices();
                logger.info("PollyTTS cfg data loaded and static elements (re)initialized");
                logger.info("Using PollyTTS cache folder {}", getCacheFolderName());
            } else {
                logger.info("PollyTTS not initialized");
            }

        } catch (Throwable t) {
            logger.error("Failed to activate PollyTTS: {}", t.getMessage(), t);
        }
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        if (this.voices == null) {
            logger.error("PollyTTS interface never initalized, check congiguration elements");
        }
        return this.voices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' in format {}", text, requestedFormat);
        logger.debug("voice UID: '{}'   voice Label: '{}'  voice Locale: {}", voice.getUID(), voice.getLabel(),
                voice.getLocale());

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
            File cacheAudioFile = pollyTssImpl.getTextToSpeechAsFile(text, voice.getLabel(),
                    getApiAudioFormat(requestedFormat));
            if (cacheAudioFile == null) {
                throw new TTSException("Could not read from PollyTTS service");
            }
            logger.debug("Audio Stream for '{}' in format {}", text, requestedFormat);
            AudioStream audioStream = new PollyTTSAudioStream(cacheAudioFile, requestedFormat);
            // AudioStream audioStream = new PollyTTSAudioStream(cacheAudioFile);
            return audioStream;
        } catch (AudioException ex) {
            throw new TTSException("Could not create AudioStream: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new TTSException("Could not read from PollyTTS service: " + ex.getMessage(), ex);
        }
    }

    /**
     * Initializes this.voices.
     *
     * @return The voices of this instance
     */
    private final HashSet<Voice> initVoices() {
        HashSet<Voice> voices = new HashSet<Voice>();
        Set<Locale> locales = pollyTssImpl.getAvailableLocales();
        for (Locale local : locales) {
            Set<String> voiceLabels = pollyTssImpl.getAvailableVoices(local);
            for (String voiceLabel : voiceLabels) {
                voices.add(new PollyTTSVoice(local, voiceLabel));
                logger.debug("locale '{}' for voice {}", local, voiceLabel);
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
        Set<String> formats = pollyTssImpl.getAvailableAudioFormats();
        for (String format : formats) {
            audioFormats.add(getAudioFormat(format));
        }
        return audioFormats;
    }

    private final AudioFormat getAudioFormat(String apiFormat) {
        Boolean bigEndian = null;
        Integer bitDepth = 16;
        Integer bitRate = null;
        Long frequency = 22050L;

        if ("MP3".equals(apiFormat)) {
            // use by default: MP3, 22khz_16bit_mono with bitrate 64 kbps
            bitRate = 64000;
            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, bigEndian, bitDepth, bitRate,
                    frequency);
        } else if ("OGG".equals(apiFormat)) {
            // use by default: OGG, 22khz_16bit_mono
            return new AudioFormat(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_VORBIS, bigEndian, bitDepth, bitRate,
                    frequency);
        } else {
            throw new IllegalArgumentException("Audio format " + apiFormat + " not yet supported");
        }
    }

    private final String getApiAudioFormat(AudioFormat format) {
        if (!PollyClientConfig.getAudioFormat().equals("default")) {
            // Override system specified with user preferred value
            return PollyClientConfig.getAudioFormat();
        }
        if (format.getCodec().equals(AudioFormat.CODEC_MP3)) {
            return "MP3";
        } else if (format.getCodec().equals(AudioFormat.CODEC_VORBIS)) {
            return "OGG";
        } else {
            throw new IllegalArgumentException("Audio format " + format.getCodec() + " not yet supported");
        }
    }

    private final CachedPollyTTSCloudImplementation initVoiceImplementation() {
        CachedPollyTTSCloudImplementation apiImpl = new CachedPollyTTSCloudImplementation(getCacheFolderName());
        return apiImpl;
    }

    String getCacheFolderName() {
        String folderName = ConfigConstants.getUserDataFolder();
        // we assume that this folder does NOT have a trailing separator
        return folderName + File.separator + CACHE_FOLDER_NAME;
    }

    @Override
    public String getId() {
        return "pollytts";
    }

    @Override
    public String getLabel(Locale locale) {
        return "PollyTTS Text-to-Speech Engine";
    }

}
