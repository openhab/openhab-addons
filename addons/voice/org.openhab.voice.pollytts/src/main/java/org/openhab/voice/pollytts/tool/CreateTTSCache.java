/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.pollytts.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.smarthome.core.voice.TTSException;
import org.openhab.voice.pollytts.internal.cloudapi.CachedPollyTTSCloudImplementation;
import org.openhab.voice.pollytts.internal.cloudapi.PollyClientConfig;

/**
 * This class fills a cache with data from the PollyTTS TTS service.
 *
 * @author Jochen Hiller - Initial contribution
 * @author Robert Hillman - Moified to utilize Polly interface
 *
 *         --accessKey arg1 --secretKey arg3 --regionVal arg5 cache_dir locale text
 *
 */

public class CreateTTSCache {

    public static final int RC_OK = 0;
    public static final int RC_USAGE = 1;
    public static final int RC_INPUT_FILE_NOT_FOUND = 2;
    public static final int RC_ACCESS_KEY_MISSING = 3;
    public static final int RC_SECRET_KEY_MISSING = 4;
    public static final int RC_SERVER_REGION_MISSING = 4;

    public static void main(String[] args) throws IOException, TTSException {
        CreateTTSCache tool = new CreateTTSCache();
        int rc = tool.doMain(args);
        System.exit(rc);
    }

    public void ClientConfig(String accessKey, String secretKey, String regionVal) {
        PollyClientConfig polly = new PollyClientConfig();
        polly.setAccessKey(accessKey);
        polly.setSecretKey(secretKey);
        polly.setRegionVal(regionVal);
        polly.initPollyServiceInterface();
    }

    public int doMain(String[] args) throws IOException, TTSException {

        if ((args == null) || (args.length != 9)) {
            usage();
            return RC_USAGE;
        }
        if (!args[0].equalsIgnoreCase("--accessKey")) {
            usage();
            return RC_ACCESS_KEY_MISSING;
        }
        if (!args[2].equalsIgnoreCase("--secretKey")) {
            usage();
            return RC_SECRET_KEY_MISSING;
        }
        if (!args[4].equalsIgnoreCase("--regionVal")) {
            usage();
            return RC_SERVER_REGION_MISSING;
        }
        ClientConfig(args[1], args[3], args[5]);
        String cacheDir = args[6];
        String label = args[7];
        if (args[6].startsWith("@")) {
            String inputFileName = args[8].substring(1);
            File inputFile = new File(inputFileName);
            if (!inputFile.exists()) {
                usage();
                System.err.println("File " + inputFileName + " not found");
                return RC_INPUT_FILE_NOT_FOUND;
            }
            generateCacheForFile(cacheDir, label, inputFileName);
        } else {
            String text = args[6];
            generateCacheForMessage(cacheDir, label, text);
        }
        return RC_OK;
    }

    private void usage() {
        System.out.println("Usage: java org.openhab.voice.pollytts.tool.CreateTTSCache <args>");
        System.out.print("Arguments: --accessKey <akey> --secretKey <skey> --regionVal <region>");
        System.out.println(" <cache-dir> <voice-Name> { <text> | @inputfile }");
        System.out.println();
        System.out.println("  akey       the Polly Access Key, e.g. \"A123456789\"");
        System.out.println("  skey       the Polly Secret Key, e.g. \"S123456789\"");
        System.out.println("  region     the Polly server region, e.g. \"us-east-1\"");
        System.out.println("  cache-dir  directory where the files will be stored, e.g. \"pollytts-cache\"");
        System.out.println("  voice-name Polly voice name, has to be valid, e.g. \"Joey\", \"Joanna\"");
        System.out.println("             names are unique across all languages");
        System.out.println("             so selecting a name selects a language ");
        System.out.println("  text       the text to create audio file for, e.g. \"Hello World\"");
        System.out.println("  inputfile  name of a file, e.g. \"@message.txt\"");
        System.out.println("             all the lines within the file will be translated");
        System.out.println();
        System.out.println("Sample: java org.openhab.voice.pollytts.tool.CreateTTSCache --accessKey A1234567890");
        System.out.println("                                      --secretKey S1234567890 --regionVal us-east-1");
        System.out.println("                                      cache Joey @messages.txt");
        System.out.println();
    }

    private void generateCacheForFile(String cacheDir, String label, String inputFileName) throws IOException {
        File inputFile = new File(inputFileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                generateCacheForMessage(cacheDir, label, line);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    private void generateCacheForMessage(String cacheDir, String label, String msg) throws IOException {
        if (msg == null) {
            System.err.println("Ignore msg=null");
            return;
        }
        msg = msg.trim();
        if (msg.length() == 0) {
            System.err.println("Ignore msg=''");
            return;
        }
        CachedPollyTTSCloudImplementation impl = new CachedPollyTTSCloudImplementation(cacheDir);
        File cachedFile = impl.getTextToSpeechAsFile(msg, label, "MP3");
        System.out.println("Created cached audio for label='" + label + "', msg='" + msg + "' to file=" + cachedFile);
    }
}
