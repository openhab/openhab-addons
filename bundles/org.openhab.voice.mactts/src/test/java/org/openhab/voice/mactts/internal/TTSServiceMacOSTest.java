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
package org.openhab.voice.mactts.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.storage.StorageService;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.Voice;
import org.openhab.core.voice.internal.cache.TTSLRUCacheImpl;

/**
 * Test TTSServiceMacOS
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class TTSServiceMacOSTest {

    private StorageService storageService;

    /**
     * Test TTSServiceMacOS.getAvailableVoices()
     */
    @Test
    public void getAvailableVoicesTest() {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));

        Map<String, Object> config = new HashMap<>();
        config.put("enableCacheTTS", false);
        TTSLRUCacheImpl voiceLRUCache = new TTSLRUCacheImpl(storageService, config);
        MacTTSService ttsServiceMacOS = new MacTTSService(voiceLRUCache);
        assertFalse(ttsServiceMacOS.getAvailableVoices().isEmpty());
    }

    /**
     * Test TTSServiceMacOS.getSupportedFormats()
     */
    @Test
    public void getSupportedFormatsTest() {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));

        Map<String, Object> config = new HashMap<>();
        config.put("enableCacheTTS", false);
        TTSLRUCacheImpl voiceLRUCache = new TTSLRUCacheImpl(storageService, config);
        MacTTSService ttsServiceMacOS = new MacTTSService(voiceLRUCache);
        assertFalse(ttsServiceMacOS.getSupportedFormats().isEmpty());
    }

    /**
     * Test TTSServiceMacOS.synthesize(String,Voice,AudioFormat)
     */
    @Test
    public void synthesizeTest() throws IOException, TTSException {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));

        Map<String, Object> config = new HashMap<>();
        config.put("enableCacheTTS", false);
        TTSLRUCacheImpl voiceLRUCache = new TTSLRUCacheImpl(storageService, config);
        MacTTSService ttsServiceMacOS = new MacTTSService(voiceLRUCache);
        Set<Voice> voices = ttsServiceMacOS.getAvailableVoices();
        Set<AudioFormat> audioFormats = ttsServiceMacOS.getSupportedFormats();
        try (AudioStream audioStream = ttsServiceMacOS.synthesize("Hello", voices.iterator().next(),
                audioFormats.iterator().next())) {
            assertNotNull(audioStream, "created null AudioSource");
            assertNotNull(audioStream.getFormat(), "created an AudioSource w/o AudioFormat");
            assertNotNull(audioStream, "created an AudioSource w/o InputStream");
            assertTrue(-1 != audioStream.read(new byte[2]), "returned an AudioSource with no data");
        }
    }
}
