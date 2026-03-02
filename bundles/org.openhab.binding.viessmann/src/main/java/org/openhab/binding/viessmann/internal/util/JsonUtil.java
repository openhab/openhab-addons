/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.JsonElement;

/**
 * The {@link JsonUtil} class provides utility methods for JSON serialization
 * and deserialization using Jackson.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class JsonUtil {

    /**
     * Module that teaches Jackson how to serialize Gson's JsonElement tree types (JsonObject/JsonArray/...).
     * Without this, Jackson may reflect into Gson internals (e.g. "asDouble") and fail.
     */
    private static final SimpleModule GSON_MODULE = createGsonModule();

    private static final ObjectMapper COMPACT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL).registerModule(GSON_MODULE);

    private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL).enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(GSON_MODULE);

    private JsonUtil() {
        // utility class
    }

    private static SimpleModule createGsonModule() {
        SimpleModule m = new SimpleModule("gson-json");
        m.addSerializer(JsonElement.class, new GsonJsonElementSerializer());
        return m;
    }

    /**
     * Serializes the given object to compact JSON.
     *
     * @param object the object to serialize
     * @return JSON representation
     * @throws Exception if serialization fails
     */
    public static String toJson(Object object) throws Exception {
        return COMPACT_MAPPER.writeValueAsString(object);
    }

    /**
     * Serializes the given object to formatted (pretty-printed) JSON.
     *
     * @param object the object to serialize
     * @return formatted JSON representation
     * @throws Exception if serialization fails
     */
    public static String toPrettyJson(Object object) throws Exception {
        return PRETTY_MAPPER.writeValueAsString(object);
    }

    /**
     * Deserializes the given JSON string into the specified type.
     *
     * @param json the JSON string
     * @param type the target class
     * @param <T> the target type
     * @return deserialized object
     * @throws Exception if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> type) throws Exception {
        return COMPACT_MAPPER.readValue(json, type);
    }
}
