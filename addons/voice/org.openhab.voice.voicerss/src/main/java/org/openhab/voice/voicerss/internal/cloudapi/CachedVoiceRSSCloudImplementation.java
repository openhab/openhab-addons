/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal.cloudapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a cache for the retrieved audio data. It will preserve
 * them in file system, as audio files with an additional .txt file to indicate
 * what content is in the audio file.
 *
 * @author Jochen Hiller - Initial contribution
 */
public class CachedVoiceRSSCloudImplementation extends VoiceRSSCloudImplementation {

    private final Logger logger = LoggerFactory.getLogger(CachedVoiceRSSCloudImplementation.class);

    private final File cacheFolder;

    public CachedVoiceRSSCloudImplementation(String cacheFolderName) {
        if (cacheFolderName == null) {
            throw new RuntimeException("Folder for cache must be defined");
        }
        // Lazy create the cache folder
        cacheFolder = new File(cacheFolderName);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
    }

    public File getTextToSpeechAsFile(String apiKey, String text, String locale, String audioFormat)
            throws IOException {
        String fileNameInCache = getUniqeFilenameForText(text, locale);
        // check if in cache
        File audioFileInCache = new File(cacheFolder, fileNameInCache + "." + audioFormat.toLowerCase());
        if (audioFileInCache.exists()) {
            return audioFileInCache;
        }

        // if not in cache, get audio data and put to cache
        try (InputStream is = super.getTextToSpeech(apiKey, text, locale, audioFormat);
                FileOutputStream fos = new FileOutputStream(audioFileInCache);) {
            copyStream(is, fos);
            // write text to file for transparency too
            // this allows to know which contents is in which audio file
            File txtFileInCache = new File(cacheFolder, fileNameInCache + ".txt");
            writeText(txtFileInCache, text);
            // return from cache
            return audioFileInCache;
        } catch (FileNotFoundException ex) {
            logger.warn("Could not write {} to cache, return null", audioFileInCache, ex);
            return null;
        } catch (IOException ex) {
            logger.error("Could not write {} to cache, return null", audioFileInCache, ex);
            return null;
        }
    }

    /**
     * Gets a unique filename for a give text, by creating a MD5 hash of it. It
     * will be preceded by the locale.
     *
     * Sample: "en-US_00a2653ac5f77063bc4ea2fee87318d3"
     */
    String getUniqeFilenameForText(String text, String locale) {
        try {
            byte[] bytesOfMessage = text.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md.digest(bytesOfMessage);
            BigInteger bigInt = new BigInteger(1, md5Hash);
            String hashtext = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32
            // chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            String fileName = locale + "_" + hashtext;
            return fileName;
        } catch (UnsupportedEncodingException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for '{}'", text, ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for '{}'", text, ex);
            return null;
        }
    }

    // helper methods

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[4096];
        int read = inputStream.read(bytes, 0, 4096);
        while (read > 0) {
            outputStream.write(bytes, 0, read);
            read = inputStream.read(bytes, 0, 4096);
        }
    }

    private void writeText(File file, String text) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes("UTF-8"));
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
