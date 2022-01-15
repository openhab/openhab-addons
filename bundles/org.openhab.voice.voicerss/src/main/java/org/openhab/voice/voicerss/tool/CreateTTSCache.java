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
package org.openhab.voice.voicerss.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.openhab.voice.voicerss.internal.cloudapi.CachedVoiceRSSCloudImpl;

/**
 * This class fills a cache with data from the VoiceRSS TTS service.
 *
 * @author Jochen Hiller - Initial contribution
 */
public class CreateTTSCache {

    public static final int RC_OK = 0;
    public static final int RC_USAGE = 1;
    public static final int RC_INPUT_FILE_NOT_FOUND = 2;
    public static final int RC_API_KEY_MISSING = 3;

    public static void main(String[] args) throws IOException {
        CreateTTSCache tool = new CreateTTSCache();
        int rc = tool.doMain(args);
        System.exit(rc);
    }

    public int doMain(String[] args) throws IOException {
        if ((args == null) || (args.length != 5)) {
            usage();
            return RC_USAGE;
        }
        if (!args[0].equalsIgnoreCase("--api-key")) {
            usage();
            return RC_API_KEY_MISSING;
        }
        String apiKey = args[1];
        String cacheDir = args[2];
        String locale = args[3];
        String voice = args[4];
        if (args[5].startsWith("@")) {
            String inputFileName = args[5].substring(1);
            File inputFile = new File(inputFileName);
            if (!inputFile.exists()) {
                usage();
                System.err.println("File " + inputFileName + " not found");
                return RC_INPUT_FILE_NOT_FOUND;
            }
            generateCacheForFile(apiKey, cacheDir, locale, voice, inputFileName);
        } else {
            String text = args[5];
            generateCacheForMessage(apiKey, cacheDir, locale, voice, text);
        }
        return RC_OK;
    }

    private void usage() {
        System.out.println("Usage: java org.openhab.voice.voicerss.tool.CreateTTSCache <args>");
        System.out.println("Arguments: --api-key <key> <cache-dir> <locale> { <text> | @inputfile }");
        System.out.println("  key       the VoiceRSS API Key, e.g. \"123456789\"");
        System.out.println("  cache-dir is directory where the files will be stored, e.g. \"voicerss-cache\"");
        System.out.println("  locale    the language locale, has to be valid, e.g. \"en-us\", \"de-de\"");
        System.out.println("  voice     the voice, \"default\" for the default voice");
        System.out.println("  text      the text to create audio file for, e.g. \"Hello World\"");
        System.out.println(
                "  inputfile a name of a file, where all lines will be translatet to text, e.g. \"@message.txt\"");
        System.out.println();
        System.out.println(
                "Sample: java org.openhab.voice.voicerss.tool.CreateTTSCache --api-key 1234567890 cache en-US @messages.txt");
        System.out.println();
    }

    private void generateCacheForFile(String apiKey, String cacheDir, String locale, String voice, String inputFileName)
            throws IOException {
        File inputFile = new File(inputFileName);
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                generateCacheForMessage(apiKey, cacheDir, locale, voice, line);
            }
        }
    }

    private void generateCacheForMessage(String apiKey, String cacheDir, String locale, String voice, String msg)
            throws IOException {
        if (msg == null) {
            System.err.println("Ignore msg=null");
            return;
        }
        String trimmedMsg = msg.trim();
        if (trimmedMsg.length() == 0) {
            System.err.println("Ignore msg=''");
            return;
        }
        CachedVoiceRSSCloudImpl impl = new CachedVoiceRSSCloudImpl(cacheDir);
        File cachedFile = impl.getTextToSpeechAsFile(apiKey, trimmedMsg, locale, voice, "MP3");
        System.out.println(
                "Created cached audio for locale='" + locale + "', msg='" + trimmedMsg + "' to file=" + cachedFile);
    }
}
