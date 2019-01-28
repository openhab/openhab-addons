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
package org.openhab.voice.pollytts.internal;

import static org.openhab.voice.pollytts.internal.PollyTTSService.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;
import org.openhab.voice.pollytts.internal.cloudapi.CachedPollyTTSCloudImplementation;
import org.openhab.voice.pollytts.internal.cloudapi.PollyClientConfig;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for using PollyTTS TTS service.
 *
 * work derived for voicetts as sample
 * * @author Robert Hillman - converted to Polly API, added polly voice interface, only MP3 audio currently
 */
@Component(configurationPid = SERVICE_PID, property = { Constants.SERVICE_PID + "=" + SERVICE_PID,
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=" + SERVICE_NAME + " Text-to-Speech",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=" + SERVICE_CATEGORY + ":" + SERVICE_ID,
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=" + SERVICE_CATEGORY })
public class PollyTTSService implements TTSService {

    /**
     * Service name
     */
    static final String SERVICE_NAME = "Polly";

    /**
     * Service id
     */
    static final String SERVICE_ID = "pollytts";

    /**
     * Service category
     */
    static final String SERVICE_CATEGORY = "voice";

    /**
     * Service pid
     */
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;

    /** Cache folder name is below userdata/polly/cache. */
    private final String CACHE_FOLDER_NAME = "pollytts/cache";

    private final Logger logger = LoggerFactory.getLogger(PollyTTSService.class);

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
    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            modified(config);
        } catch (IOException e) {
            logger.error("config file error, could not initialize", e);
        }
    }

    /**
     * initialize voice service according to config values
     * Keys come from ConfigAdmin
     */
    @Modified
    protected void modified(Map<String, Object> config) throws IOException {
        PollyClientConfig polly = new PollyClientConfig();
        polly.setAccessKey(config.get("accessKey").toString());
        polly.setSecretKey(config.get("secretKey").toString());
        polly.setRegionVal(config.get("serviceRegion").toString());
        polly.setAudioFormat(config.get("audioFormat").toString());
        polly.setExpireDate((int) Double.parseDouble(config.get("cacheExpiration").toString()));

        boolean successful = polly.initPollyServiceInterface();

        if (successful) {
            pollyTssImpl = initVoiceImplementation();
            audioFormats = initAudioFormats();
            voices = initVoices();
            logger.info("PollyTTS voice service initialized");
            logger.debug("Using PollyTTS cache folder {}", getCacheFolderName());
        } else {
            logger.error("PollyTTS not initialized");
        }
    }

    /**
     * return voices available for the user to select
     */
    @Override
    public Set<Voice> getAvailableVoices() {
        if (this.voices == null) {
            logger.debug("PollyTTS interface never initalized, check congiguration elements");
        }
        return this.voices;
    }

    /**
     * return possible auto formats provided by the system
     */
    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    /**
     * obtain audio stream from cach to amazon polly service and return it
     * to play the audio
     */
    @Override
    public AudioStream synthesize(String inText, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' in format {}", inText, requestedFormat);
        logger.debug("voice UID: '{}'   voice Label: '{}'  voice Locale: {}", voice.getUID(), voice.getLabel(),
                voice.getLocale());

        // Validate arguments
        // trim text
        String text = inText.trim();
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

    private final CachedPollyTTSCloudImplementation initVoiceImplementation() throws IOException {
        CachedPollyTTSCloudImplementation apiImpl = new CachedPollyTTSCloudImplementation(getCacheFolderName());
        return apiImpl;
    }

    /**
     * fetch the name of cache folder to use
     */
    String getCacheFolderName() {
        String folderName = ConfigConstants.getUserDataFolder();
        // we assume that this folder does NOT have a trailing separator
        return folderName + File.separator + CACHE_FOLDER_NAME;
    }

    /**
     * returns a unique identifier for the service
     */
    @Override
    public String getId() {
        return "pollytts";
    }

    /**
     * returns txt description of this service
     */
    @Override
    public String getLabel(Locale locale) {
        return "PollyTTS Text-to-Speech Engine";
    }

}
