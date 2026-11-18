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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.voice.tts.TTSCache;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests for {@link OpenAITTSService}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OpenAITTSServiceTest {
    private static final AudioFormat MP3 = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, 16,
            64000, 44100L);
    private static final AudioFormat WAV_44K = new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED,
            false, 16, 705600, 44100L, 1);

    private final HttpClient httpClient = new HttpClient();
    private @NonNullByDefault({}) HttpServer server;
    private @NonNullByDefault({}) OpenAITTSService service;
    private volatile byte[] responseBody = new byte[0];
    private volatile String requestBody = "";

    @BeforeEach
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBody.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(responseBody);
            }
        });
        server.start();
        httpClient.start();

        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        service = new OpenAITTSService(httpClientFactory, mock(TTSCache.class), Map.of());
        service.activate(Map.of("apiUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/audio/speech"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        httpClient.stop();
        server.stop(0);
    }

    @Test
    public void wavRequestAsksForWavAndReturnsActualFormat() throws Exception {
        responseBody = wav(24000, 100);

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), WAV_44K);

        assertEquals("wav", requestedFormat());
        assertEquals(AudioFormat.CONTAINER_WAVE, stream.getFormat().getContainer());
        assertEquals(24000L, stream.getFormat().getFrequency());
    }

    @Test
    public void wavResponseLargerThanJettyDefaultBufferIsReturned() throws Exception {
        responseBody = wav(24000, 50_000);
        assertTrue(responseBody.length > 2 * 1024 * 1024);

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), WAV_44K);

        assertEquals(responseBody.length, stream.readAllBytes().length);
    }

    @Test
    public void wavRequestWithNonWavResponseReturnsMp3() throws Exception {
        responseBody = new byte[] { 'I', 'D', '3', 4, 0 };

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), WAV_44K);

        assertEquals(MP3, stream.getFormat());
    }

    @Test
    public void mp3RequestAsksForMp3() throws Exception {
        responseBody = new byte[] { 1, 2, 3 };

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), MP3);

        assertEquals("mp3", requestedFormat());
        assertEquals(MP3, stream.getFormat());
    }

    private String requestedFormat() {
        return JsonParser.parseString(requestBody).getAsJsonObject().get("response_format").getAsString();
    }

    private static byte[] wav(int sampleRate, int durationMs) throws IOException {
        byte[] pcm = new byte[sampleRate / 1000 * durationMs * 2];
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(sampleRate, 16, 1, true, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(pcm), format, pcm.length / 2),
                AudioFileFormat.Type.WAVE, out);
        return out.toByteArray();
    }
}
