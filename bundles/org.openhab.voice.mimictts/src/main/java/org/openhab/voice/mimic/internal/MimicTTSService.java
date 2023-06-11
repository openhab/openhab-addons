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
package org.openhab.voice.mimic.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioException;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.openhab.voice.mimic.internal.dto.VoiceDto;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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

    private final Logger logger = LoggerFactory.getLogger(MimicTTSService.class);

    static final String SERVICE_CATEGORY = "voice";
    static final String SERVICE_ID = "mimictts";
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;
    static final String SERVICE_NAME = "Mimic";

    /**
     * Configuration parameters
     */
    private static final String PARAM_URL = "url";
    private static final String PARAM_WORKAROUNDSERVLETSINK = "workaroundServletSink";
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
            AudioFormat.CODEC_PCM_SIGNED, false, 16, 52000, 22050L, 1);

    private Set<Voice> availableVoices = new HashSet<>();

    private final MimicConfiguration config = new MimicConfiguration();

    private final Gson gson = new GsonBuilder().create();

    private final HttpClient httpClient;

    @Activate
    public MimicTTSService(final @Reference HttpClientFactory httpClientFactory, Map<String, Object> config) {
        updateConfig(config);
        this.httpClient = httpClientFactory.getCommonHttpClient();
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

        // workaround
        param = newConfig.get(PARAM_WORKAROUNDSERVLETSINK);
        if (param != null) {
            config.workaroundServletSink = Boolean.parseBoolean(param.toString());
        }

        // audio volatility
        try {
            param = newConfig.get(PARAM_AUDIOVOLATITLITY);
            if (param != null) {
                config.audioVolatility = Double.parseDouble(param.toString());
            }
        } catch (NumberFormatException e) {
            logger.warn("Cannot parse audioVolatility parameter. Using default");
        }

        // phoneme volatility
        try {
            param = newConfig.get(PARAM_PHONEMEVOLATITLITY);
            if (param != null) {
                config.phonemeVolatility = Double.parseDouble(param.toString());
            }
        } catch (NumberFormatException e) {
            logger.warn("Cannot parse phonemeVolatility parameter. Using default");
        }

        // speakingRate
        try {
            param = newConfig.get(PARAM_SPEAKINGRATE);
            if (param != null) {
                config.speakingRate = Double.parseDouble(param.toString());
            }
        } catch (NumberFormatException e) {
            logger.warn("Cannot parse speakingRate parameter. Using default");
        }

        refreshVoices();
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
        return availableVoices;
    }

    public void refreshVoices() {
        String url = config.url + LIST_VOICES_URL;
        availableVoices.clear();
        try {
            String responseVoices = HttpRequestBuilder.getFrom(url).getContentAsString();
            VoiceDto[] mimicVoiceResponse = gson.fromJson(responseVoices, VoiceDto[].class);
            if (mimicVoiceResponse == null) {
                logger.warn("Cannot get mimic voices from the URL {}", url);
                return;
            } else if (mimicVoiceResponse.length == 0) {
                logger.debug("Voice set response from Mimic is empty ?!");
                return;
            }
            for (VoiceDto voiceDto : mimicVoiceResponse) {
                List<String> speakers = voiceDto.speakers;
                if (speakers != null && !speakers.isEmpty()) {
                    for (String speaker : speakers) {
                        availableVoices.add(new MimicVoice(voiceDto.key, voiceDto.language, voiceDto.name, speaker));
                    }
                } else {
                    availableVoices.add(new MimicVoice(voiceDto.key, voiceDto.language, voiceDto.name, null));
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.warn("Cannot get mimic voices from the URL {}, error {}", url, e.getMessage());
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

        if (!availableVoices.contains(voice)) {
            // let a chance for the service to update :
            refreshVoices();
            if (!availableVoices.contains(voice)) {
                throw new TTSException("Voice " + voice.getUID() + " not available for MimicTTS");
            }
        }

        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);
        // Validate arguments
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is empty");
        }
        if (!AUDIO_FORMAT.isCompatible(requestedFormat)) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        String encodedVoice;
        try {
            encodedVoice = URLEncoder.encode(((MimicVoice) voice).getTechnicalName(),
                    StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot encode voice in URL " + ((MimicVoice) voice).getTechnicalName());
        }

        // create the url for given locale, format
        String urlTTS = config.url + SYNTHETIZE_URL + "?voice=" + encodedVoice + "&noiseScale=" + config.audioVolatility
                + "&noiseW=" + config.phonemeVolatility + "&lengthScale=" + config.speakingRate + "&audioTarget=client";
        logger.debug("Querying mimic with URL {}", urlTTS);

        // prepare the response as an inputstream
        InputStreamResponseListener inputStreamResponseListener = new InputStreamResponseListener();
        // we will use a POST method for the text
        StringContentProvider textContentProvider = new StringContentProvider(text, StandardCharsets.UTF_8);
        if (text.startsWith("<speak>")) {
            httpClient.POST(urlTTS).header("Content-Type", "application/ssml+xml").content(textContentProvider)
                    .accept("audio/wav").send(inputStreamResponseListener);
        } else {
            httpClient.POST(urlTTS).content(textContentProvider).accept("audio/wav").send(inputStreamResponseListener);
        }

        // compute the estimated timeout using a "stupid" method based on text length, as the response time depends on
        // the requested text. Average speaker speed estimated to 10/second.
        // Will use a safe margin multiplicator (x5) to accept very slow mimic server
        // So the constant chosen is 5 * 10 = /2
        int timeout = text.length() / 2;

        // check response status and return AudioStream
        Response response;
        try {
            response = inputStreamResponseListener.get(timeout, TimeUnit.SECONDS);
            if (response.getStatus() == HttpStatus.OK_200) {
                String lengthHeader = response.getHeaders().get(HttpHeader.CONTENT_LENGTH);
                long length;
                try {
                    length = Long.parseLong(lengthHeader);
                } catch (NumberFormatException e) {
                    throw new TTSException(
                            "Cannot get Content-Length header from mimic response. Are you sure to query a mimic TTS server at "
                                    + urlTTS + " ?");
                }

                InputStream inputStreamFromMimic = inputStreamResponseListener.getInputStream();
                try {
                    if (!config.workaroundServletSink) {
                        return new InputStreamAudioStream(inputStreamFromMimic, AUDIO_FORMAT, length);
                    } else {
                        // Some audio sinks use the openHAB servlet to get audio. This servlet require the
                        // getClonedStream()
                        // method
                        // So we cache the file on disk, thus implementing the method thanks to FileAudioStream.
                        return createTemporaryFile(inputStreamFromMimic, AUDIO_FORMAT);
                    }
                } catch (TTSException e) {
                    try {
                        inputStreamFromMimic.close();
                    } catch (IOException e1) {
                    }
                    throw e;
                }

            } else {
                String errorMessage = "Cannot get wav from mimic url " + urlTTS + " with HTTP response code "
                        + response.getStatus() + " for reason " + response.getReason();
                TTSException ttsException = new TTSException(errorMessage);
                response.abort(ttsException);
                throw ttsException;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            String errorMessage = "Cannot get wav from mimic url " + urlTTS;
            throw new TTSException(errorMessage, e);
        }
    }

    private AudioStream createTemporaryFile(InputStream inputStream, AudioFormat audioFormat) throws TTSException {
        File mimicDirectory = new File(OpenHAB.getUserDataFolder(), "mimic");
        mimicDirectory.mkdir();
        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".wav", mimicDirectory);
            tempFile.deleteOnExit();
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return new AutoDeleteFileAudioStream(tempFile, audioFormat);
        } catch (AudioException | IOException e) {
            throw new TTSException("Cannot create temporary audio file", e);
        }
    }
}
