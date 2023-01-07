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
package org.openhab.voice.pollytts.internal;

import static java.util.stream.Collectors.toSet;
import static org.openhab.core.audio.AudioFormat.*;
import static org.openhab.voice.pollytts.internal.PollyTTSService.*;

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
import org.openhab.voice.pollytts.internal.cloudapi.CachedPollyTTSCloudImpl;
import org.openhab.voice.pollytts.internal.cloudapi.PollyTTSConfig;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for using Polly Text-to-Speech.
 *
 * @author Robert Hillman - Initial contribution
 */
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Text-to-Speech", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
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

    /**
     * Cache folder under $userdata
     */
    private static final String CACHE_FOLDER_NAME = "cache";

    private final Logger logger = LoggerFactory.getLogger(PollyTTSService.class);

    /**
     * We need the cached implementation to allow for FixedLengthAudioStream.
     */
    private CachedPollyTTSCloudImpl pollyTTSImpl;

    /**
     * Set of supported voices
     */
    private final Set<Voice> voices = new HashSet<>();

    /**
     * Set of supported audio formats
     */
    private final Set<AudioFormat> audioFormats = new HashSet<>();

    private PollyTTSConfig pollyTTSConfig;

    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        try {
            pollyTTSConfig = new PollyTTSConfig(config);
            logger.debug("Using configuration {}", config);

            // create cache folder
            File cacheFolder = new File(new File(OpenHAB.getUserDataFolder(), CACHE_FOLDER_NAME), SERVICE_PID);
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
            }
            logger.info("Using cache folder {}", cacheFolder.getAbsolutePath());

            pollyTTSImpl = new CachedPollyTTSCloudImpl(pollyTTSConfig, cacheFolder);

            audioFormats.clear();
            audioFormats.addAll(initAudioFormats());

            voices.clear();
            voices.addAll(initVoices());

            logger.debug("PollyTTS service initialized");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to initialize PollyTTS: {}", e.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to initialize PollyTTS", e);
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

    /**
     * obtain audio stream from cache or Amazon Polly service and return it to play the audio
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
        boolean isAudioFormatSupported = audioFormats.stream()
                .filter(audioFormat -> audioFormat.isCompatible(requestedFormat)).findAny().isPresent();

        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        // now create the input stream for given text, locale, format. There is
        // only a default voice
        try {
            File cacheAudioFile = pollyTTSImpl.getTextToSpeechAsFile(text, voice.getLabel(),
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

    private Set<Voice> initVoices() {
        // @formatter:off
        return pollyTTSImpl.getAvailableLocales().stream()
            .flatMap(locale ->
                pollyTTSImpl.getAvailableVoices(locale).stream()
                    .map(label -> new PollyTTSVoice(locale, label)))
            .collect(toSet());
        // @formatter:on
    }

    private Set<AudioFormat> initAudioFormats() {
        // @formatter:off
        return pollyTTSImpl.getAvailableAudioFormats().stream()
                .map(this::getAudioFormat)
                .collect(toSet());
        // @formatter:on
    }

    private AudioFormat getAudioFormat(String apiFormat) {
        if (CODEC_MP3.equals(apiFormat)) {
            // use by default: MP3, 22khz_16bit_mono with bitrate 64 kbps
            return new AudioFormat(CONTAINER_NONE, CODEC_MP3, null, 16, 64000, 22050L);
        } else if (CONTAINER_OGG.equals(apiFormat)) {
            // use by default: OGG, 22khz_16bit_mono
            return new AudioFormat(CONTAINER_OGG, CODEC_VORBIS, null, 16, null, 22050L);
        } else {
            throw new IllegalArgumentException("Audio format " + apiFormat + " not yet supported");
        }
    }

    private String getApiAudioFormat(AudioFormat format) {
        if (!"default".equals(pollyTTSConfig.getAudioFormat())) {
            // Override system specified with user preferred value
            return pollyTTSConfig.getAudioFormat();
        }
        if (CODEC_MP3.equals(format.getCodec())) {
            return CODEC_MP3;
        } else if (CODEC_VORBIS.equals(format.getCodec())) {
            return CONTAINER_OGG;
        } else {
            throw new IllegalArgumentException("Audio format " + format.getCodec() + " not yet supported");
        }
    }

    @Override
    public String getId() {
        return "pollytts";
    }

    @Override
    public String getLabel(Locale locale) {
        return "PollyTTS";
    }
}
