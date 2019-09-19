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
package org.openhab.binding.darksky.internal.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple file based cache implementation.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class ByteArrayFileCache {

    private final Logger logger = LoggerFactory.getLogger(ByteArrayFileCache.class);

    private static final String MD5_ALGORITHM = "MD5";

    static final String CACHE_FOLDER_NAME = "cache";
    private static final char EXTENSION_SEPARATOR = '.';
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    private final File cacheFolder;

    static final long ONE_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    private int expiry = 0;

    private static final Map<String, File> FILES_IN_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a new {@link ByteArrayFileCache} instance for a service. Creates a <code>cache</code> folder under
     * <code>$userdata/cache/$servicePID</code>.
     *
     * @param servicePID PID of the service
     */
    public ByteArrayFileCache(String servicePID) {
        // TODO track and limit folder size
        // TODO support user specific folder
        cacheFolder = new File(new File(ConfigConstants.getUserDataFolder(), CACHE_FOLDER_NAME), servicePID);
        if (!cacheFolder.exists()) {
            logger.debug("Creating cache folder '{}'", cacheFolder.getAbsolutePath());
            cacheFolder.mkdirs();
        }
        logger.debug("Using cache folder '{}'", cacheFolder.getAbsolutePath());
    }

    /**
     * Creates a new {@link ByteArrayFileCache} instance for a service. Creates a <code>cache</code> folder under
     * <code>$userdata/cache/$servicePID/</code>.
     *
     * @param servicePID PID of the service
     * @param int the days for how long the files stay in the cache valid. Must be positive. 0 to
     *            disables this functionality.
     */
    public ByteArrayFileCache(String servicePID, int expiry) {
        this(servicePID);
        if (expiry < 0) {
            throw new IllegalArgumentException("Cache expiration time must be greater than or equal to 0");
        }
        this.expiry = expiry;
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
            // update time of last use
            fileInCache.setLastModified(System.currentTimeMillis());
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
     * Removes expired files from the cache.
     */
    public void clearExpired() {
        // exit if expiry is set to 0 (disabled)
        if (expiry <= 0) {
            return;
        }
        File[] filesInCache = cacheFolder.listFiles();
        if (filesInCache != null && filesInCache.length > 0) {
            logger.debug("Deleting expired files from cache");
            Arrays.stream(filesInCache).filter(file -> isExpired(file)).forEach(File::delete);
        }
    }

    /**
     * Checks if the given {@link File} is expired.
     *
     * @param fileInCache the {@link File}
     * @return <code>true</code> if the file is expired, <code>false</code> otherwise
     */
    private boolean isExpired(File fileInCache) {
        // exit if expiry is set to 0 (disabled)
        if (expiry <= 0) {
            return false;
        }
        return expiry * ONE_DAY_IN_MILLIS < System.currentTimeMillis() - fileInCache.lastModified();
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
            // update time of last use
            fileInCache.setLastModified(System.currentTimeMillis());
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
    File getUniqueFile(String key) {
        String uniqueFileName = getUniqueFileName(key);
        if (FILES_IN_CACHE.containsKey(uniqueFileName)) {
            return FILES_IN_CACHE.get(uniqueFileName);
        } else {
            String fileExtension = getFileExtension(key);
            File fileInCache = new File(cacheFolder,
                    uniqueFileName + (fileExtension == null ? "" : EXTENSION_SEPARATOR + fileExtension));
            FILES_IN_CACHE.put(uniqueFileName, fileInCache);
            return fileInCache;
        }
    }

    /**
     * Gets the extension of a file name.
     *
     * @param fileName the file name to retrieve the extension of
     * @return the extension of the file or null if none exists
     */
    @Nullable
    String getFileExtension(String fileName) {
        int extensionPos = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparatorPos = Math.max(fileName.lastIndexOf(UNIX_SEPARATOR), fileName.lastIndexOf(WINDOWS_SEPARATOR));
        return lastSeparatorPos > extensionPos ? null : fileName.substring(extensionPos + 1);
    }

    /**
     * Creates a unique file name from the key with which the file is to be associated.
     *
     * @param key the key with which the file is to be associated
     * @return unique file name for the file associated with the given key
     */
    String getUniqueFileName(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] bytesOfKey = key.getBytes(StandardCharsets.UTF_8);
            byte[] md5Hash = md.digest(bytesOfKey);
            BigInteger bigInt = new BigInteger(1, md5Hash);
            String fileNameHash = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars
            while (fileNameHash.length() < 32) {
                fileNameHash = "0" + fileNameHash;
            }
            return fileNameHash;
        } catch (NoSuchAlgorithmException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for key '{}'", key, ex);
            return key;
        }
    }
}
