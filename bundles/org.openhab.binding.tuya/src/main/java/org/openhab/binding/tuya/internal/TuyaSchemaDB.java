/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link TuyaSchemaDB} implements storage of schemas.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class TuyaSchemaDB {
    private static final Type STORAGE_TYPE = TypeToken.getParameterized(List.class, SchemaDp.class).getType();

    private static @Nullable Storage<String> storage = null;

    private static final Gson gson = new Gson();

    public static Map<String, Map<String, SchemaDp>> cache = new ConcurrentHashMap<>();

    public static void setStorage(StorageService storageService, String name) {
        storage = storageService.getStorage(name);

        cache = getSchemas();
        addRemoteSchemas();
    }

    public static boolean contains(String key) {
        return cache.containsKey(key);
    }

    public static @Nullable Map<String, SchemaDp> get(String key) {
        return cache.get(key);
    }

    public static @Nullable SchemaDp get(String key, String subkey) {
        Map<String, SchemaDp> schema = get(key);
        return (schema != null ? schema.get(subkey) : null);
    }

    public static @Nullable Map<String, SchemaDp> getOrConvert(String key1, String key2) {
        Map<String, SchemaDp> result = cache.get(key1);

        if (result == null) {
            Storage<String> persistent = storage;

            if (persistent != null) {
                List<SchemaDp> listDps = gson.fromJson(persistent.get(key2), STORAGE_TYPE);

                if (listDps != null) {
                    put(key1, listDps);
                    // persistent.remove(key2);
                }

                result = cache.get(key1);
            }
        }

        return result;
    }

    public static void remove(String key) {
        cache.remove(key);

        Storage<String> persistent = storage;

        if (persistent != null) {
            persistent.remove(key);
        }
    }

    public static void put(String key, List<SchemaDp> listDps) {
        addToCache(key, listDps);

        Storage<String> persistent = storage;

        if (persistent != null) {
            persistent.put(key, gson.toJson(listDps));
        }
    }

    private static void addToCache(String key, List<SchemaDp> listDps) {
        if (!listDps.isEmpty()) {
            Map<String, SchemaDp> schemaDps = listDps.stream().sorted((s1, s2) -> s1.id - s2.id)
                    .collect(Collectors.toMap(s -> s.code, s -> s, (e1, e2) -> e1, LinkedHashMap::new));

            cache.putIfAbsent(key, schemaDps);
        }
    }

    private static Map<String, Map<String, SchemaDp>> getSchemas() {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema.json");
        if (resource == null) {
            LoggerFactory.getLogger(TuyaSchemaDB.class)
                    .warn("Could not read resource file 'schema.json', discovery might fail");
            return new ConcurrentHashMap<>();
        }

        try (InputStreamReader reader = new InputStreamReader(resource)) {
            Gson gson = new Gson();
            Type schemaListType = TypeToken.getParameterized(LinkedHashMap.class, String.class, SchemaDp.class)
                    .getType();
            Type schemaType = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, schemaListType)
                    .getType();
            return Objects.requireNonNull(gson.fromJson(reader, schemaType));
        } catch (IOException e) {
            LoggerFactory.getLogger(TuyaSchemaDB.class).warn("Failed to read 'schema.json', discovery might fail");
            return new ConcurrentHashMap<>();
        }
    }

    private static void addRemoteSchemas() {
        Storage<String> persistent = storage;

        if (persistent != null) {
            for (String productId : persistent.getKeys()) {
                List<SchemaDp> listDps = gson.fromJson(persistent.get(productId), STORAGE_TYPE);
                if (listDps != null) {
                    addToCache(productId, listDps);
                }
            }
        }
    }
}
