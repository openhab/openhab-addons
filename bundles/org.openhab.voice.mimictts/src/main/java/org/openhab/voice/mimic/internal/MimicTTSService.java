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
package org.openhab.voice.mimic.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.openhab.voice.mimic.internal.dto.VoiceDto;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Mimic Voice service implementation.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Component(configurationPid = MimicTTSService.SERVICE_PID, property = Constants.SERVICE_PID + "="
        + MimicTTSService.SERVICE_PID)
@ConfigurableService(category = MimicTTSService.SERVICE_CATEGORY, label = MimicTTSService.SERVICE_NAME
        + " Text-to-Speech", description_uri = MimicTTSService.SERVICE_CATEGORY + ":" + MimicTTSService.SERVICE_ID)
@NonNullByDefault
public class MimicTTSService implements TTSService {

    static final String SERVICE_CATEGORY = "voice";
    static final String SERVICE_ID = "mimictts";
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;
    static final String SERVICE_NAME = "Mimic";

    /**
     * Configuration parameters
     */
    private static final String PARAM_URL = "url";
    private static final String PARAM_SPEAKINGRATE = "speakingRate";
    private static final String PARAM_AUDIOVOLATITLITY = "audioVolatility";
    private static final String PARAM_PHONEMEVOLATITLITY = "phonemeVolatility";

    /**
     * Url
     */
    private static final String LIST_VOICES_URL = "/api/voices";
    private static final String SYNTHETIZE_URL = "/api/tts";

    /** The only wave format supported */
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, 52000, 22050L, 1);;

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(MimicTTSService.class);

    private final MimicConfiguration config;

    private final Gson gson = new GsonBuilder().create();

    @Activate
    public MimicTTSService(final @Reference ConfigurationAdmin configAdmin) {
        Dictionary<String, Object> dict;
        config = new MimicConfiguration();
        try {
            dict = configAdmin.getConfiguration(SERVICE_PID).getProperties();
            if (dict != null) {
                // convert dict to map :
                Map<String, Object> dictCopy = Collections.list(dict.keys()).stream()
                        .collect(Collectors.toMap(Function.identity(), dict::get));
                updateConfig(dictCopy);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        updateConfig(config);
    }

    /**
     * Called by the framework when the configuration was updated.
     *
     * @param newConfig Updated configuration
     */
    @Modified
    private void updateConfig(Map<String, Object> newConfig) {
        logger.debug("Updating configuration");
        // client id
        Object param = newConfig.get(PARAM_URL);
        if (param == null) {
            logger.warn("Missing URL to access Mimic TTS API. Using localhost");
        } else {
            config.url = param.toString();
        }
        // audio volatility
        param = newConfig.get(PARAM_AUDIOVOLATITLITY);
        if (param != null) {
            config.audioVolatility = Double.parseDouble(param.toString());
        }

        // phoneme volatility
        param = newConfig.get(PARAM_PHONEMEVOLATITLITY);
        if (param != null) {
            config.phonemeVolatility = Double.parseDouble(param.toString());
        }

        // speakingRate
        param = newConfig.get(PARAM_SPEAKINGRATE);
        if (param != null) {
            config.speakingRate = Double.parseDouble(param.toString());
        }
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        String url = config.url + LIST_VOICES_URL;
        try {
            String responseVoices = HttpRequestBuilder.getFrom(url).getContentAsString();
            VoiceDto[] mimicVoiceResponse = gson.fromJson(responseVoices, VoiceDto[].class);
            if (mimicVoiceResponse == null) {
                logger.error("Cannot get mimic voices from the URL {}", url);
                return Collections.emptySet();
            }
            if (mimicVoiceResponse.length == 0) {
                logger.warn("Voice set from mimic is empty ?!");
            }
            Set<Voice> voiceSet = new HashSet<>();
            for (VoiceDto voiceDto : mimicVoiceResponse) {
                if (voiceDto.speakers != null && voiceDto.speakers.size() > 0) {
                    for (String speaker : voiceDto.speakers) {
                        voiceSet.add(new MimicVoice(voiceDto.key, voiceDto.language, voiceDto.name, speaker));
                    }
                } else {
                    voiceSet.add(new MimicVoice(voiceDto.key, voiceDto.language, voiceDto.name, null));
                }
            }
            return voiceSet;
        } catch (IOException e) {
            logger.error("Cannot get mimic voices from the URL {}", url);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.<AudioFormat> of(AUDIO_FORMAT);
    }

    /**
     * Checks parameters and calls the API to synthesize voice.
     *
     * @param text Input text.
     * @param voice Selected voice.
     * @param requestedFormat Format that is supported by the target sink as well.
     * @return Output audio stream
     * @throws TTSException in case the service is unavailable or a parameter is invalid.
     */
    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        // Validate arguments
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!AUDIO_FORMAT.isCompatible(requestedFormat)) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }
        String encodedText;
        try {
            encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot encode text in URL " + text);
        }

        String ssml = "";
        if (text.startsWith("<speak>")) {
            ssml = "&ssml=true";
        }

        // create the audio byte array for given text, locale, format
        String urlTTS = config.url + SYNTHETIZE_URL + "?text=" + encodedText + "&voice="
                + ((MimicVoice) voice).getTechnicalName() + ssml + "&noiseScale=" + config.audioVolatility + "&noiseW="
                + config.phonemeVolatility + "&lengthScale=" + config.speakingRate + "&audioTarget=client";
        logger.debug("Querying mimic with URL {}", urlTTS);
        RawType responseWav = HttpUtil.downloadData(urlTTS, "audio/wav", false, -1);
        if (responseWav == null) {
            throw new TTSException("Cannot get wav from mimic url " + urlTTS);
        }
        return new ByteArrayAudioStream(responseWav.getBytes(), AUDIO_FORMAT);
    }
}
