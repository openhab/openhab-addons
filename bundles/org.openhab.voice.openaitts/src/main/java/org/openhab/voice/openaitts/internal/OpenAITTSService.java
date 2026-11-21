/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.voice.tts.AbstractCachedTTSService;
import org.openhab.core.voice.tts.TTSCache;
import org.openhab.core.voice.tts.TTSException;
import org.openhab.core.voice.tts.TTSService;
import org.openhab.core.voice.tts.Voice;
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
 * @author Dan Cunningham - Add WAV output
 */
@Component(configurationPid = TTS_SERVICE_PID, property = Constants.SERVICE_PID + "="
        + TTS_SERVICE_PID, service = TTSService.class)
@ConfigurableService(category = "voice", label = "OpenAI TTS Service", description_uri = "voice:" + TTS_SERVICE_ID)

@NonNullByDefault
public class OpenAITTSService extends AbstractCachedTTSService {

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    // Jetty's default 2 MiB buffer holds only about 43 seconds of 24 kHz WAV
    private static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;
    private static final AudioFormat MP3_FORMAT = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3,
            null, 16, 64000, 44100L);
    // the sample rate comes from the WAV header of the response
    private static final AudioFormat WAV_FORMAT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, null, 1);
    private static final byte[] RIFF_MAGIC = "RIFF".getBytes(StandardCharsets.US_ASCII);
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
        return Set.of(MP3_FORMAT, WAV_FORMAT);
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
        boolean wav = AudioFormat.CONTAINER_WAVE.equals(requestedFormat.getContainer());
        content.addProperty("response_format", wav ? "wav" : "mp3");

        if (!"tts-1".equals(config.model) && !"tts-1-hd".equals(config.model) && !config.instructions.isEmpty()) {
            content.addProperty("instructions", config.instructions);
        }

        String queryJson = gson.toJson(content);

        logger.trace("Send query: {}", queryJson);

        try {
            Request request = httpClient.newRequest(config.apiUrl).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header("Authorization", "Bearer " + config.apiKey).header("Content-Type", "application/json")
                    .content(new StringContentProvider(queryJson));
            FutureResponseListener listener = new FutureResponseListener(request, MAX_RESPONSE_BYTES);
            request.send(listener);
            ContentResponse response = listener.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (response.getStatus() == HttpStatus.OK_200) {
                byte[] audio = response.getContent();
                return new ByteArrayAudioStream(audio, wav ? getWavResponseFormat(audio) : MP3_FORMAT);
            } else {
                logger.error("Request failed with status {}: {}", response.getStatus(), response.getContentAsString());
                throw new TTSException("Failed to generate audio data");
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Request to OpenAI failed: {}", e.getMessage(), e);
            throw new TTSException("Failed to generate audio data");
        }
    }

    private AudioFormat getWavResponseFormat(byte[] audio) throws TTSException {
        // a server at a custom apiUrl may ignore response_format and send MP3
        if (audio.length < RIFF_MAGIC.length
                || !Arrays.equals(audio, 0, RIFF_MAGIC.length, RIFF_MAGIC, 0, RIFF_MAGIC.length)) {
            logger.debug("WAV was requested but the response is not WAV, treating it as MP3");
            return MP3_FORMAT;
        }
        try (InputStream inputStream = new ByteArrayInputStream(audio)) {
            return AudioWaveUtils.parseWavFormat(inputStream);
        } catch (IOException e) {
            throw new TTSException("Cannot parse WAV format", e);
        }
    }
}
