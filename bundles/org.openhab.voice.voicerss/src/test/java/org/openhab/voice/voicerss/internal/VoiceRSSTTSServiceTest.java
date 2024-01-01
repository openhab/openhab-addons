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
package org.openhab.voice.voicerss.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.openhab.core.audio.AudioFormat.*;
import static org.openhab.voice.voicerss.internal.CompatibleAudioFormatMatcher.compatibleAudioFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.storage.StorageService;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.internal.cache.TTSLRUCacheImpl;

/**
 * Tests for {@link VoiceRSSTTSService}.
 *
 * @author Andreas Brenk - Initial contribution
 */
public class VoiceRSSTTSServiceTest {

    private static final AudioFormat MP3_44KHZ_16BIT = new AudioFormat(AudioFormat.CONTAINER_NONE,
            AudioFormat.CODEC_MP3, null, 16, null, 44_100L);
    private static final AudioFormat OGG_44KHZ_16BIT = new AudioFormat(AudioFormat.CONTAINER_OGG,
            AudioFormat.CODEC_VORBIS, null, 16, null, 44_100L);
    private static final AudioFormat AAC_44KHZ_16BIT = new AudioFormat(AudioFormat.CONTAINER_NONE,
            AudioFormat.CODEC_MP3, null, 16, null, 44_100L);
    private static final AudioFormat WAV_22KHZ_8BIT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_UNSIGNED, null, 8, null, 22_050L);
    private static final AudioFormat WAV_48KHZ_16BIT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 48_000L);

    private StorageService storageService;

    /**
     * The {@link VoiceRSSTTSService} under test.
     */
    private TTSService ttsService;

    @BeforeEach
    public void setUp() {
        Map<String, Object> config = new HashMap<>();
        config.put("enableCacheTTS", false);
        TTSLRUCacheImpl voiceLRUCache = new TTSLRUCacheImpl(storageService, config);
        final VoiceRSSTTSService ttsService = new VoiceRSSTTSService(voiceLRUCache);
        ttsService.activate(null);

        this.ttsService = ttsService;
    }

    @Test
    public void testSupportedFormats() {
        final Set<AudioFormat> supportedFormats = ttsService.getSupportedFormats();

        // check generic formats without any further constraints
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(MP3)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(WAV)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(OGG)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(AAC)));

        // check specific formats with common constraints
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(MP3_44KHZ_16BIT)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(OGG_44KHZ_16BIT)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(AAC_44KHZ_16BIT)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(WAV_22KHZ_8BIT)));
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(WAV_48KHZ_16BIT)));

        // check specific formats with additional constraints
        assertThat(supportedFormats, hasItem(compatibleAudioFormat(bitRate(WAV, 705_600)))); // 44.1 kHz 16-bit

        // check unsupported formats
        assertThat(supportedFormats, not(hasItem(compatibleAudioFormat(bitDepth(WAV, 24)))));
    }

    private AudioFormat bitDepth(AudioFormat format, Integer bitDepth) {
        return new AudioFormat(format.getContainer(), format.getCodec(), format.isBigEndian(), bitDepth,
                format.getBitRate(), format.getFrequency());
    }

    private AudioFormat bitRate(AudioFormat format, Integer bitRate) {
        return new AudioFormat(format.getContainer(), format.getCodec(), format.isBigEndian(), format.getBitDepth(),
                bitRate, format.getFrequency());
    }
}
