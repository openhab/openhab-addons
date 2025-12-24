/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.voice.openaitts.internal;

import static org.openhab.voice.openaitts.internal.OpenAITTSConstants.*;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.voice.AbstractCachedTTSService;
import org.openhab.core.voice.TTSCache;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Artur Fedjukevits - Initial contribution
 *         API documentation: https://platform.openai.com/docs/guides/text-to-speech
 */
@Component(configurationPid = TTS_SERVICE_PID, property = Constants.SERVICE_PID + "="
        + TTS_SERVICE_PID, service = TTSService.class)
@ConfigurableService(category = "voice", label = "OpenAI TTS Service", description_uri = "voice:" + TTS_SERVICE_ID)

@NonNullByDefault
public class OpenAITTSService extends AbstractCachedTTSService {

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private final Logger logger = LoggerFactory.getLogger(OpenAITTSService.class);
    private OpenAITTSConfiguration config = new OpenAITTSConfiguration();
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private static final Set<Voice> VOICES = Stream
            .of("nova", "alloy", "ash", "ballad", "coral", "sage", "echo", "fable", "onyx", "shimmer", "verse")
            .map(OpenAITTSVoice::new).collect(Collectors.toSet());

    @Activate
    public OpenAITTSService(@Reference HttpClientFactory httpClientFactory, @Reference TTSCache ttsCache,
            Map<String, Object> config) {
        super(ttsCache);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        this.config = new Configuration(config).as(OpenAITTSConfiguration.class);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(OpenAITTSConfiguration.class);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, 16, 64000, 44100L));
    }

    @Override
    public String getId() {
        return TTS_SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "OpenAI TTS Service";
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        return VOICES;
    }

    /**
     * Synthesizes the given text to audio data using the OpenAI API
     *
     * @param text The text to synthesize
     * @param voice The voice to use
     * @param requestedFormat The requested audio format
     * @return The synthesized audio data
     * @throws TTSException If the synthesis fails
     */
    @Override
    public AudioStream synthesizeForCache(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        JsonObject content = new JsonObject();
        content.addProperty("model", config.model);
        content.addProperty("input", text);
        content.addProperty("voice", voice.getLabel().toLowerCase());
        content.addProperty("speed", config.speed);

        if (!"tts-1".equals(config.model) && !"tts-1-hd".equals(config.model) && !config.instructions.isEmpty()) {
            content.addProperty("instructions", config.instructions);
        }

        String queryJson = gson.toJson(content);

        logger.trace("Send query: {}", queryJson);

        try {
            ContentResponse response = httpClient.newRequest(config.apiUrl).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header("Authorization", "Bearer " + config.apiKey).header("Content-Type", "application/json")
                    .content(new StringContentProvider(queryJson)).send();

            if (response.getStatus() == HttpStatus.OK_200) {
                return new ByteArrayAudioStream(response.getContent(), requestedFormat);
            } else {
                logger.error("Request failed with status {}: {}", response.getStatus(), response.getContentAsString());
                throw new TTSException("Failed to generate audio data");
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Request to OpenAI failed: {}", e.getMessage(), e);
            throw new TTSException("Failed to generate audio data");
        }
    }
}
