/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.Voice;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Test TTSServiceMacOS
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class TTSServiceMacOSTest {

    /**
     * Test TTSServiceMacOS.getAvailableVoices()
     */
    @Test
    public void getAvailableVoicesTest() {
        Assume.assumeTrue("Mac OS X".equals(System.getProperty("os.name")));

        MacTTSService ttsServiceMacOS = new MacTTSService();
        Assert.assertFalse("The getAvailableVoicesTest() failed", ttsServiceMacOS.getAvailableVoices().isEmpty());
    }

    /**
     * Test TTSServiceMacOS.getSupportedFormats()
     */
    @Test
    public void getSupportedFormatsTest() {
        Assume.assumeTrue("Mac OS X".equals(System.getProperty("os.name")));

        MacTTSService ttsServiceMacOS = new MacTTSService();
        Assert.assertFalse("The getSupportedFormatsTest() failed", ttsServiceMacOS.getSupportedFormats().isEmpty());
    }

    /**
     * Test TTSServiceMacOS.synthesize(String,Voice,AudioFormat)
     */
    @Test
    public void synthesizeTest() {
        Assume.assumeTrue("Mac OS X".equals(System.getProperty("os.name")));

        MacTTSService ttsServiceMacOS = new MacTTSService();
        Set<Voice> voices = ttsServiceMacOS.getAvailableVoices();
        Set<AudioFormat> audioFormats = ttsServiceMacOS.getSupportedFormats();
        try (AudioStream audioStream = ttsServiceMacOS.synthesize("Hello", voices.iterator().next(),
                audioFormats.iterator().next())) {
            Assert.assertNotNull("The test synthesizeTest() created null AudioSource", audioStream);
            Assert.assertNotNull("The test synthesizeTest() created an AudioSource w/o AudioFormat",
                    audioStream.getFormat());
            Assert.assertNotNull("The test synthesizeTest() created an AudioSource w/o InputStream", audioStream);
            Assert.assertTrue("The test synthesizeTest() returned an AudioSource with no data",
                    (-1 != audioStream.read(new byte[2])));
        } catch (TTSException e) {
            Assert.fail("synthesizeTest() failed with TTSException: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("synthesizeTest() failed with IOException: " + e.getMessage());
        }
    }
}
