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
import org.openhab.voice.pollytts.internal.cloudapi.CachedPollyTTSCloudImpl;
import org.openhab.voice.pollytts.internal.cloudapi.PollyClientConfig;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;

/**
 * This is a TTS service implementation for Amazon Polly.
 *
 * @author Robert Hillman - Initial contribution
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
    private static final String CACHE_FOLDER_NAME = "pollytts/cache";

    private final Logger logger = LoggerFactory.getLogger(PollyTTSService.class);

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private CachedPollyTTSCloudImpl pollyTssImpl;

    /**
     * Set of supported voices
     */
    private final Set<Voice> voices = new HashSet<>();

    /**
     * Set of supported audio formats
     */
    private final Set<AudioFormat> audioFormats = new HashSet<>();

    /**
     * DS activate, with access to ConfigAdmin
     */
    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    /**
     * initialize voice service according to config values
     * Keys come from ConfigAdmin
     */
    @Modified
    protected void modified(Map<String, Object> config) {
        PollyClientConfig polly = new PollyClientConfig();
        polly.setAccessKey(config.get("accessKey").toString());
        polly.setSecretKey(config.get("secretKey").toString());
        polly.setRegionVal(config.get("serviceRegion").toString());
        polly.setAudioFormat(config.get("audioFormat").toString());
        polly.setExpireDate((int) Double.parseDouble(config.get("cacheExpiration").toString()));

        try {
            polly.initPollyServiceInterface();
            pollyTssImpl = initVoiceImplementation();

            audioFormats.clear();
            audioFormats.addAll(initAudioFormats());

            voices.clear();
            voices.addAll(initVoices());

            logger.debug("PollyTTS service initialized");
            logger.debug("Using PollyTTS cache folder {}", getCacheFolderName());
        } catch (AmazonServiceException | IllegalArgumentException | IOException e) {
            logger.error("Failed to initialize PollyTTS", e);
        }
    }

    /**
     * return voices available for the user to select
     */
    @Override
    public Set<Voice> getAvailableVoices() {
        return voices;
    }

    /**
     * return possible auto formats provided by the system
     */
    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return audioFormats;
    }

    /**
     * obtain audio stream from cach to amazon polly service and return it
     * to play the audio
     */
    @Override
    public AudioStream synthesize(String inText, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' in format {}", inText, requestedFormat);
        logger.debug("voice UID: '{}' voice label: '{}' voice Locale: {}", voice.getUID(), voice.getLabel(),
                voice.getLocale());

        // Validate arguments
        // trim text
        String text = inText.trim();
        if (text == null || text.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        boolean isAudioFormatSupported = false;
        for (AudioFormat audioFormat : audioFormats) {
            if (audioFormat.isCompatible(requestedFormat)) {
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
     * Initializes voices.
     *
     * @return The voices of this instance
     */
    private Set<Voice> initVoices() {
        Set<Voice> voices = new HashSet<>();
        for (Locale local : pollyTssImpl.getAvailableLocales()) {
            for (String voiceLabel : pollyTssImpl.getAvailableVoices(local)) {
                voices.add(new PollyTTSVoice(local, voiceLabel));
                logger.debug("locale '{}' for voice {}", local, voiceLabel);
            }
        }
        return voices;
    }

    /**
     * Initializes audio formats.
     *
     * @return The audio formats of this instance
     */
    private Set<AudioFormat> initAudioFormats() {
        Set<AudioFormat> audioFormats = new HashSet<>();
        for (String format : pollyTssImpl.getAvailableAudioFormats()) {
            audioFormats.add(getAudioFormat(format));
        }
        return audioFormats;
    }

    private AudioFormat getAudioFormat(String apiFormat) {
        if ("MP3".equals(apiFormat)) {
            // use by default: MP3, 22khz_16bit_mono with bitrate 64 kbps
            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, 16, 64000, 22050L);
        } else if ("OGG".equals(apiFormat)) {
            // use by default: OGG, 22khz_16bit_mono
            return new AudioFormat(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_VORBIS, null, 16, null, 22050L);
        } else {
            throw new IllegalArgumentException("Audio format " + apiFormat + " not yet supported");
        }
    }

    private String getApiAudioFormat(AudioFormat format) {
        if (!PollyClientConfig.getAudioFormat().equals("default")) {
            // Override system specified with user preferred value
            return PollyClientConfig.getAudioFormat();
        }
        if (AudioFormat.CODEC_MP3.equals(format.getCodec())) {
            return "MP3";
        } else if (AudioFormat.CODEC_VORBIS.equals(format.getCodec())) {
            return "OGG";
        } else {
            throw new IllegalArgumentException("Audio format " + format.getCodec() + " not yet supported");
        }
    }

    private CachedPollyTTSCloudImpl initVoiceImplementation() throws IOException {
        return new CachedPollyTTSCloudImpl(getCacheFolderName());
    }

    /**
     * fetch the name of cache folder to use
     */
    private String getCacheFolderName() {
        // we assume that this folder does NOT have a trailing separator
        return ConfigConstants.getUserDataFolder() + File.separator + CACHE_FOLDER_NAME;
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
