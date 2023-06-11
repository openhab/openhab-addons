/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;
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
    public void testConstructor() throws IOException {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = Runtime.getRuntime().exec("say -v ?");
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            assertNotNull(voiceMacOS, "The MacTTSVoice(String) constructor failed");
        }
    }

    /**
     * Test VoiceMacOS.getUID()
     */
    @Test
    public void getUIDTest() throws IOException {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = Runtime.getRuntime().exec("say -v ?");
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String nextLine = bufferedReader.readLine();
            MacTTSVoice macTTSVoice = new MacTTSVoice(nextLine);
            assertTrue(0 == macTTSVoice.getUID().indexOf("mactts:"), "The VoiceMacOS UID has an incorrect format");
        }
    }

    /**
     * Test MacTTSVoice.getLabel()
     */
    @Test
    public void getLabelTest() throws IOException {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = Runtime.getRuntime().exec("say -v ?");
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            assertNotNull(voiceMacOS.getLabel(), "The MacTTSVoice label has an incorrect format");
        }
    }

    /**
     * Test MacTTSVoice.getLocale()
     */
    @Test
    public void getLocaleTest() throws IOException {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = Runtime.getRuntime().exec("say -v ?");
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String nextLine = bufferedReader.readLine();
            MacTTSVoice voiceMacOS = new MacTTSVoice(nextLine);
            assertNotNull(voiceMacOS.getLocale(), "The MacTTSVoice locale has an incorrect format");
        }
    }
}
