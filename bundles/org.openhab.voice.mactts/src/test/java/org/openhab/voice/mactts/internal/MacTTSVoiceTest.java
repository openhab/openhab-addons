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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test MacTTSVoice
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class MacTTSVoiceTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Test MacTTSVoice(String) constructor
     */
    @Test
    public void testConstructor() {
        Assume.assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            Assert.assertNotNull("The MacTTSVoice(String) constructor failed", voiceMacOS);
        } catch (IOException e) {
            Assert.fail("testConstructor() failed with IOException: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    /**
     * Test VoiceMacOS.getUID()
     */
    @Test
    public void getUIDTest() {
        Assume.assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice macTTSVoice = new MacTTSVoice(nextLine);
            Assert.assertTrue("The VoiceMacOS UID has an incorrect format",
                    (0 == macTTSVoice.getUID().indexOf("mactts:")));
        } catch (IOException e) {
            Assert.fail("getUIDTest() failed with IOException: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    /**
     * Test MacTTSVoice.getLabel()
     */
    @Test
    public void getLabelTest() {
        Assume.assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            Assert.assertNotNull("The MacTTSVoice label has an incorrect format", voiceMacOS.getLabel());
        } catch (IOException e) {
            Assert.fail("getLabelTest() failed with IOException: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    /**
     * Test MacTTSVoice.getLocale()
     */
    @Test
    public void getLocaleTest() {
        Assume.assumeTrue("Mac OS X" == System.getProperty("os.name"));
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            Assert.assertNotNull("The MacTTSVoice locale has an incorrect format", voiceMacOS.getLocale());
        } catch (IOException e) {
            Assert.fail("getLocaleTest() failed with IOException: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }
}
