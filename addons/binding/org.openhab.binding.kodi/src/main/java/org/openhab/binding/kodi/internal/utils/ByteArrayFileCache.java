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
package org.openhab.binding.kodi.internal.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple file based cache implementation.
 * <b>NOTE:</b> This class is a duplicate of
 * https://github.com/eclipse/smarthome/blob/master/extensions/binding/org.eclipse.smarthome.binding.openweathermap/src/main/java/org/eclipse/smarthome/binding/openweathermap/internal/utils/ByteArrayFileCache.java
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class ByteArrayFileCache {

    private final Logger logger = LoggerFactory.getLogger(ByteArrayFileCache.class);

    private static final String CACHE_FOLDER_NAME = "cache";
    public static final char EXTENSION_SEPARATOR = '.';

    protected final File cacheFolder;

    public ByteArrayFileCache(String servicePID) {
        // TODO track and limit folder size
        // TODO support user specific folder
        cacheFolder = new File(new File(new File(ConfigConstants.getUserDataFolder()), CACHE_FOLDER_NAME), servicePID);
        if (!cacheFolder.exists()) {
            logger.debug("Creating cache folder '{}'", cacheFolder.getAbsolutePath());
            cacheFolder.mkdirs();
        }
        logger.debug("Using cache folder '{}'", cacheFolder.getAbsolutePath());
    }

    /**
     * Adds a file to the cache. If the cache previously contained a file for the key, the old file is replaced by the
     * new content.
     *
     * @param key the key with which the file is to be associated
     * @param content the content for the file to be associated with the specified key
     */
    public void put(String key, byte[] content) {
        writeFile(getUniqueFile(key), content);
    }

    /**
     * Adds a file to the cache.
     *
     * @param key the key with which the file is to be associated
     * @param content the content for the file to be associated with the specified key
     */
    public void putIfAbsent(String key, byte[] content) {
        File fileInCache = getUniqueFile(key);
        if (fileInCache.exists()) {
            logger.debug("File '{}' present in cache", fileInCache.getName());
        } else {
            writeFile(fileInCache, content);
        }
    }

    /**
     * Adds a file to the cache and returns the content of the file.
     *
     * @param key the key with which the file is to be associated
     * @param content the content for the file to be associated with the specified key
     * @return the content of the file associated with the given key
     */
    public byte[] putIfAbsentAndGet(String key, byte[] content) {
        putIfAbsent(key, content);

        return content;
    }

    /**
     * Writes the given content to the given {@link File}.
     *
     * @param fileInCache the {@link File}
     * @param content the content to be written
     */
    private void writeFile(File fileInCache, byte[] content) {
        logger.debug("Caching file '{}'", fileInCache.getName());
        try {
            Files.write(fileInCache.toPath(), content);
        } catch (IOException e) {
            logger.warn("Could not write file '{}' to cache", fileInCache.getName(), e);
        }
    }

    /**
     * Checks if the key is present in the cache.
     *
     * @param key the key whose presence in the cache is to be tested
     * @return true if the cache contains a file for the specified key
     */
    public boolean containsKey(String key) {
        return getUniqueFile(key).exists();
    }

    /**
     * Removes the file associated with the given key from the cache.
     *
     * @param key the key whose associated file is to be removed
     */
    public void remove(String key) {
        deleteFile(getUniqueFile(key));
    }

    /**
     * Deletes the given {@link File}.
     *
     * @param fileInCache the {@link File}
     */
    private void deleteFile(File fileInCache) {
        if (fileInCache.exists()) {
            logger.debug("Deleting file '{}' from cache", fileInCache.getName());
            fileInCache.delete();
        } else {
            logger.debug("File '{}' not found in cache", fileInCache.getName());
        }
    }

    /**
     * Removes all files from the cache.
     */
    public void clear() {
        File[] filesInCache = cacheFolder.listFiles();
        if (filesInCache != null && filesInCache.length > 0) {
            logger.debug("Deleting all files from cache");
            Arrays.stream(filesInCache).forEach(File::delete);
        }
    }

    /**
     * Returns the content of the file associated with the given key, if it is present.
     *
     * @param key the key whose associated file is to be returned
     * @return the content of the file associated with the given key
     */
    public byte[] get(String key) {
        return readFile(getUniqueFile(key));
    }

    /**
     * Reads the content from the given {@link File}, if it is present.
     *
     * @param fileInCache the {@link File}
     * @return the content of the file
     */
    private byte[] readFile(File fileInCache) {
        if (fileInCache.exists()) {
            logger.debug("Reading file '{}' from cache", fileInCache.getName());
            try {
                return Files.readAllBytes(fileInCache.toPath());
            } catch (IOException e) {
                logger.warn("Could not read file '{}' from cache", fileInCache.getName(), e);
            }
        } else {
            logger.debug("File '{}' not found in cache", fileInCache.getName());
        }
        return new byte[0];
    }

    /**
     * Creates a unique {@link File} from the key with which the file is to be associated.
     *
     * @param key the key with which the file is to be associated
     * @return unique file for the file associated with the given key
     */
    private File getUniqueFile(String key) {
        // TODO: store / cache file internally for faster operations
        String fileExtension = getFileExtension(key);
        return new File(cacheFolder,
                getUniqueFileName(key) + (fileExtension == null ? "" : EXTENSION_SEPARATOR + fileExtension));
    }

    /**
     * Gets the extension of a file name.
     *
     * @param fileName the file name to retrieve the extension of
     * @return the extension of the file or null if none exists
     */
    private @Nullable String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        // exclude file names starting with a dot
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return null;
        }
    }

    /**
     * Creates a unique file name from the key with which the file is to be associated.
     *
     * @param key the key with which the file is to be associated
     * @return unique file name for the file associated with the given key
     */
    private String getUniqueFileName(String key) {
        try {
            byte[] bytesOfKey = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md.digest(bytesOfKey);
            BigInteger bigInt = new BigInteger(1, md5Hash);
            StringBuilder fileNameHash = new StringBuilder(bigInt.toString(16));
            // Now we need to zero pad it if you actually want the full 32 chars
            while (fileNameHash.length() < 32) {
                fileNameHash.insert(0, "0");
            }
            return fileNameHash.toString();
        } catch (NoSuchAlgorithmException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for key '{}'", key, ex);
            return key.toString();
        }
    }
}
