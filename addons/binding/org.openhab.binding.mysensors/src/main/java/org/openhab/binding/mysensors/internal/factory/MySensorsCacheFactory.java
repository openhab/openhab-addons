/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.factory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * ID Cache is used to store the ids used in the MySensors network.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsCacheFactory {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static MySensorsCacheFactory singleton = null;

    private static final String CACHE_BASE_PATH = "./mysensors/cache";
    private static final String CACHE_FILE_SUFFIX = ".cached";

    public static final String GIVEN_IDS_CACHE_FILE = "given_ids";

    private Gson gson = null;

    /**
     * Singelton of the CacheFactory
     *
     * @return Returns the singleton of the CacheFactory. Only one instance is allowed at a time.
     */
    public static MySensorsCacheFactory getCacheFactory() {
        if (singleton == null) {
            singleton = new MySensorsCacheFactory();
        }

        return singleton;
    }

    private MySensorsCacheFactory() {
        gson = new Gson();
        initializeCacheDir();
    }

    private void initializeCacheDir() {
        File f = new File(CACHE_BASE_PATH);
        if (!f.exists()) {
            logger.debug("Creating cache directory...");
            f.mkdirs();
        }
    }

    /**
     * Read the cache file.
     */
    public <T> T readCache(String cacheName, T defaulT, Type clasz) {
        return jsonFromFile(cacheName, defaulT, clasz);
    }

    /**
     * Write the cache file.
     */
    public <T> void writeCache(String cacheName, T obj, Type clasz) {
        jsonToFile(cacheName, obj, clasz);
    }

    /**
     * Read the cache file.
     */
    private synchronized <T> T jsonFromFile(String fileName, T def, Type clasz) {

        T ret = def;

        try {
            File f = new File(CACHE_BASE_PATH + "/" + GIVEN_IDS_CACHE_FILE + CACHE_FILE_SUFFIX);

            if (f.exists()) {
                logger.debug("Cache file: {} exist.", GIVEN_IDS_CACHE_FILE + CACHE_FILE_SUFFIX);
                JsonReader jReader = new JsonReader(new FileReader(f));
                ret = gson.fromJson(jReader, clasz);
            } else {
                logger.debug("Cache file: {} not exist.", GIVEN_IDS_CACHE_FILE + CACHE_FILE_SUFFIX);
                if (def != null) {
                    logger.debug("Cache file: {} not exist. Default passed, creating it...",
                            GIVEN_IDS_CACHE_FILE + CACHE_FILE_SUFFIX);
                    jsonToFile(fileName, def, clasz);
                } else {
                    logger.warn("Cache file: {} not exist. Default NOT passed, cache won't be created!",
                            GIVEN_IDS_CACHE_FILE + CACHE_FILE_SUFFIX);
                }
            }
        } catch (Exception e) {
            logger.error("Cache reading throws an exception, cause: {} ({})", e.getClass(), e.getMessage());
        }

        logger.debug("Cache ({}) content: {}", GIVEN_IDS_CACHE_FILE, ret);
        return ret;
    }

    /**
     * Write the cache file.
     */
    private synchronized <T> void jsonToFile(String fileName, T obj, Type clasz) {
        JsonWriter jsonWriter = null;
        try {
            File f = new File(CACHE_BASE_PATH + "/" + GIVEN_IDS_CACHE_FILE + CACHE_FILE_SUFFIX);

            jsonWriter = new JsonWriter(new FileWriter(f));

            logger.debug("Writing on cache {}, content: {}", GIVEN_IDS_CACHE_FILE, gson.toJson(obj, clasz));
            gson.toJson(obj, clasz, jsonWriter);
        } catch (Exception e) {
            logger.error("Cache writing throws an exception, cause: {} ({})", e.getClass(), e.getMessage());
        } finally {
            if (jsonWriter != null) {
                try {
                    jsonWriter.close();
                } catch (IOException e) {
                    logger.error("Cannot close Json writer");
                }
            }
        }
    }
}
