/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onkyo.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
class URIMetaDataProcessorTest {
    private static final String MP3_STREAM_URL = "http://audio.server.local/audio.mp3";
    private static final String AAC_STREAM_URL = "http://audio.server.local/audio.aac";

    @Test
    void generateShouldReturnEmptyStringWhenStreamIsNull() {
        String result = new URIMetaDataProcessor().generate(MP3_STREAM_URL, null);

        assertEquals("", result);
    }

    @Test
    void generateShouldReturnEmptyStringWhenExceptionOccurs() {
        AudioStream audioStream = new TestAudioStream(AudioFormat.MP3) {
            @Override
            public @Nullable String getId() {
                throw new UnsupportedOperationException();
            }
        };

        String result = new URIMetaDataProcessor().generate(MP3_STREAM_URL, audioStream);

        assertEquals("", result);
    }

    @Test
    void generateShouldReturnXMLWithGivenURLAndProtocolInfo() {
        String result = new URIMetaDataProcessor().generate(MP3_STREAM_URL, new TestAudioStream(AudioFormat.MP3));

        assertTrue(result.matches("<DIDL-Lite.+</DIDL-Lite>"));
        assertTrue(result.contains(">" + MP3_STREAM_URL + "</res></item></DIDL-Lite>"));
        assertTrue(result.contains("protocolInfo=\"http-get:*:audio/mpeg:*\""));
    }

    @Test
    void generateShouldReturnXMLWithAllFormatAttributes() {
        AudioFormat audioFormat = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_AAC, true, 8, 192000,
                48000L, 2);

        String result = new URIMetaDataProcessor().generate(AAC_STREAM_URL, new TestAudioStream(audioFormat));

        assertTrue(result.contains("nrAudioChannels=\"2\""));
        assertTrue(result.contains("sampleFrequency=\"48000\""));
        assertTrue(result.contains("bitrate=\"192000\""));
        assertTrue(result.contains("protocolInfo=\"http-get:*:audio/aac:*\""));
    }

    @NonNullByDefault
    private static class TestAudioStream extends AudioStream {
        private final AudioFormat audioFormat;

        private TestAudioStream(AudioFormat audioFormat) {
            this.audioFormat = audioFormat;
        }

        @Override
        public AudioFormat getFormat() {
            return audioFormat;
        }

        @Override
        public int read() throws IOException {
            throw new IOException("Unsupported operation");
        }
    }
}
