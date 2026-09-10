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
package org.openhab.binding.evcc.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Thread-safe wrapper for cached JSON state with debugging and hardened access.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class CachedJsonState {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedJsonState.class);

    private final Object lock = new Object();
    private volatile JsonObject state = new JsonObject();

    public void updateFull(JsonObject newState) {
        synchronized (lock) {
            state = newState.deepCopy();
            LOGGER.debug("Full state update: {}", abbreviateJson(state));
        }
    }

    public void updatePartial(String key, JsonElement value) {
        synchronized (lock) {
            LOGGER.debug("Partial state update: key='{}', value={}", key, abbreviateJson(value));
            updateState(key, value);
        }
    }

    private void updateState(String key, JsonElement value) {
        String[] parts = key.split("\\.", 2);
        if (parts.length < 1) {
            return;
        }

        String root = parts[0];

        if (parts.length == 2) {
            String[] indexParts = parts[1].split("\\.", 2);
            if (indexParts.length == 2 && indexParts[0].matches("\\d+")) {
                // Indexed array format: loadpoints.0.power
                int index = Integer.parseInt(indexParts[0]);
                state.remove(key);
                JsonArray arr = state.getAsJsonArray(root);
                if (arr == null) {
                    arr = new JsonArray();
                    state.add(root, arr);
                }
                while (arr.size() <= index) {
                    arr.add(new JsonObject());
                }
                arr.get(index).getAsJsonObject().add(indexParts[1], value);
            } else {
                // Nested object format: forecast.co2
                JsonObject obj = state.getAsJsonObject(root);
                if (obj == null) {
                    obj = new JsonObject();
                    state.add(root, obj);
                }
                obj.add(parts[1], value);
            }
        } else {
            // Root-level key
            state.add(root, value);
        }
    }

    public JsonObject getCopy() {
        synchronized (lock) {
            return state.deepCopy();
        }
    }

    public @Nullable JsonElement get(String key) {
        synchronized (lock) {
            return state.get(key);
        }
    }

    public @Nullable JsonObject getAsJsonObject(String key) {
        synchronized (lock) {
            return state.getAsJsonObject(key);
        }
    }

    public @Nullable JsonArray getAsJsonArray(String key) {
        synchronized (lock) {
            return state.getAsJsonArray(key);
        }
    }

    public boolean has(String key) {
        synchronized (lock) {
            return state.has(key);
        }
    }

    /**
     * Creates an abbreviated string representation of the JSON for logging purposes.
     * Truncates long arrays and objects to avoid excessive log output.
     */
    private static String abbreviateJson(JsonElement element) {
        if (element == null) {
            return "null";
        }

        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }

        if (element.isJsonArray()) {
            com.google.gson.JsonArray arr = element.getAsJsonArray();
            int size = arr.size();
            if (size > 5) {
                return "[array with " + size + " elements]";
            }
            return arr.toString();
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            int keyCount = obj.keySet().size();
            if (keyCount > 10) {
                return "{object with " + keyCount + " keys}";
            }
            return obj.toString();
        }

        return element.toString();
    }
}
