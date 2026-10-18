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
package org.openhab.binding.rachio.internal.api.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Handles the wrapped and unwrapped JSON shapes used by the Smart Hose API.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
final class RachioSmartHoseJsonParser {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(String.class, new EmptyStringTypeAdapter())
            .create();

    private RachioSmartHoseJsonParser() {
    }

    static <T> List<T> parseArray(String json, Class<T> valueType, String... arrayNames) {
        List<T> values = new ArrayList<>();
        JsonElement root = JsonParser.parseString(json);
        if (root.isJsonArray()) {
            addArrayEntries(values, root.getAsJsonArray(), valueType);
            return values;
        }
        if (!root.isJsonObject()) {
            return values;
        }
        JsonObject object = root.getAsJsonObject();
        for (String arrayName : arrayNames) {
            JsonElement arrayElement = object.get(arrayName);
            if (arrayElement != null && arrayElement.isJsonArray()) {
                addArrayEntries(values, arrayElement.getAsJsonArray(), valueType);
            }
        }
        return values;
    }

    static <T> void addArrayEntries(List<T> values, JsonArray array, Class<T> valueType) {
        for (JsonElement element : array) {
            if (element != null && element.isJsonObject()) {
                var value = GSON.fromJson(element, valueType);
                if (value != null) {
                    values.add(value);
                }
            }
        }
    }

    static <T> @Nullable T parseObject(String json, Class<T> valueType, String... objectNames) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            return null;
        }
        JsonObject object = root.getAsJsonObject();
        for (String objectName : objectNames) {
            JsonElement nestedObject = object.get(objectName);
            if (nestedObject != null && nestedObject.isJsonObject()) {
                return GSON.fromJson(nestedObject, valueType);
            }
        }
        return GSON.fromJson(object, valueType);
    }

    private static class EmptyStringTypeAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter out, @Nullable String value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public @Nullable String read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return "";
            }
            return in.nextString();
        }
    }
}
