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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.voice.tts.TTSCache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ContentResponse response = mock(ContentResponse.class);
    private @NonNullByDefault({}) OpenAITTSService service;

    @BeforeEach
    public void setUp() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);

        service = new OpenAITTSService(httpClientFactory, mock(TTSCache.class), Map.of());
        service.activate(Map.of());
    }

    @Test
    public void wavRequestAsksForWavAndReturnsActualFormat() throws Exception {
        when(response.getContent()).thenReturn(wav(24000));

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), WAV_44K);

        assertEquals("wav", requestBody().get("response_format").getAsString());
        assertEquals(AudioFormat.CONTAINER_WAVE, stream.getFormat().getContainer());
        assertEquals(24000L, stream.getFormat().getFrequency());
    }

    @Test
    public void wavRequestWithNonWavResponseReturnsMp3() throws Exception {
        when(response.getContent()).thenReturn(new byte[] { 'I', 'D', '3', 4, 0 });

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), WAV_44K);

        assertEquals(MP3, stream.getFormat());
    }

    @Test
    public void mp3RequestAsksForMp3() throws Exception {
        when(response.getContent()).thenReturn(new byte[] { 1, 2, 3 });

        AudioStream stream = service.synthesizeForCache("hello", new OpenAITTSVoice("nova"), MP3);

        assertEquals("mp3", requestBody().get("response_format").getAsString());
        assertEquals(MP3, stream.getFormat());
    }

    private JsonObject requestBody() {
        ArgumentCaptor<ContentProvider> content = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request).content(content.capture());
        StringBuilder body = new StringBuilder();
        content.getValue().forEach(buffer -> body.append(StandardCharsets.UTF_8.decode(buffer)));
        return JsonParser.parseString(body.toString()).getAsJsonObject();
    }

    private static byte[] wav(int sampleRate) throws IOException {
        byte[] pcm = new byte[sampleRate / 10 * 2];
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(sampleRate, 16, 1, true, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(pcm), format, pcm.length / 2),
                AudioFileFormat.Type.WAVE, out);
        return out.toByteArray();
    }
}
