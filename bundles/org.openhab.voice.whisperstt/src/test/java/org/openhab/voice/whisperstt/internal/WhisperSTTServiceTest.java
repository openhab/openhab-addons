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
package org.openhab.voice.whisperstt.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.rest.LocaleService;
import org.openhab.core.voice.stt.STTEvent;
import org.openhab.core.voice.stt.SpeechRecognitionEvent;
import org.openhab.voice.whisperstt.internal.utils.VADTest;

/**
 * Tests for {@link WhisperSTTService}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class WhisperSTTServiceTest {

    @Test
    public void transcribesIncompleteStepAtEndOfAudio() throws Exception {
        short[] speech = VADTest.speechSamples();
        // the clip does not end on a step boundary
        assertNotEquals(0, speech.length % 16000);

        assertEquals(speech.length, transcribedSamples(speech, 0f));
    }

    @Test
    public void padsShortAudioToMinSeconds() throws Exception {
        short[] speech = Arrays.copyOf(VADTest.speechSamples(), 24000);

        assertEquals(32000, transcribedSamples(speech, 2f));
    }

    private static int transcribedSamples(short[] samples, float minSeconds) throws Exception {
        CompletableFuture<Integer> transcribed = new CompletableFuture<>();
        WhisperSTTService service = new WhisperSTTService(mock(LocaleService.class), mock(HttpClientFactory.class)) {
            @Override
            @Nullable
            String transcribe(int audioSamplesOffset, short[] audioSamples, String language) {
                transcribed.complete(audioSamplesOffset);
                return "turn on the kitchen lights";
            }
        };
        Map<String, Object> config = new HashMap<>();
        config.put("mode", "API");
        config.put("removeSilence", false);
        config.put("minSeconds", minSeconds);
        service.modified(config);

        ByteBuffer bytes = ByteBuffer.allocate(samples.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        bytes.asShortBuffer().put(samples);
        AudioFormat format = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null,
                16000L, 1);
        CompletableFuture<STTEvent> result = new CompletableFuture<>();
        service.recognize(event -> {
            if (event instanceof SpeechRecognitionEvent) {
                result.complete(event);
            }
        }, new ByteArrayAudioStream(bytes.array(), format), Locale.ENGLISH);

        result.get(30, TimeUnit.SECONDS);
        return transcribed.get();
    }
}
