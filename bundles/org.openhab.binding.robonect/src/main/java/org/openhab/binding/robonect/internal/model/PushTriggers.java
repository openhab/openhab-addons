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
package org.openhab.binding.robonect.internal.model;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

/**
 * Push-service polling interval and numbered triggers returned by Robonect.
 *
 * @author Nico Brüttner - Initial contribution: dynamic push triggers
 */
@NonNullByDefault
@JsonAdapter(PushTriggers.Deserializer.class)
public class PushTriggers {
    private @Nullable Integer interval;
    private final Map<String, Trigger> triggers = new HashMap<>();

    /**
     * Returns the push polling interval in seconds.
     *
     * @return the push polling interval in seconds, or {@code null} if not reported
     */
    public @Nullable Integer getInterval() {
        return interval;
    }

    /**
     * Returns an immutable map of numbered push triggers.
     *
     * @return an immutable map of numbered push triggers
     */
    public Map<String, Trigger> getTriggers() {
        return Map.copyOf(triggers);
    }

    public static class Trigger {
        private @Nullable String name;
        private @Nullable Boolean enter;
        private @Nullable Boolean leave;

        /**
         * Returns the trigger name.
         *
         * @return the trigger name, or {@code null} if not reported
         */
        public @Nullable String getName() {
            return name;
        }

        /**
         * Reports whether entry triggers notifications.
         *
         * @return whether entry triggers notifications, or {@code null} if not reported
         */
        public @Nullable Boolean getEnter() {
            return enter;
        }

        /**
         * Reports whether exit triggers notifications.
         *
         * @return whether exit triggers notifications, or {@code null} if not reported
         */
        public @Nullable Boolean getLeave() {
            return leave;
        }
    }

    public static class Deserializer implements JsonDeserializer<PushTriggers> {
        /**
         * Deserializes push interval and trigger settings from a JSON object.
         *
         * @param json the JSON object containing the push settings
         * @param type the requested result type, unused by this deserializer
         * @param context the Gson context used to deserialize individual triggers
         * @return the parsed push settings
         * @throws JsonParseException if the JSON cannot be deserialized
         */
        @Override
        public @Nullable PushTriggers deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            PushTriggers result = new PushTriggers();
            JsonElement interval = object.get("interval");
            if (interval != null && !interval.isJsonNull()) {
                result.interval = interval.getAsInt();
            }
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                if (key.matches("trigger\\d+") && entry.getValue().isJsonObject()) {
                    @Nullable
                    Trigger trigger = context.deserialize(entry.getValue(), Trigger.class);
                    if (trigger == null) {
                        throw new JsonParseException("Push trigger data must not be null");
                    }
                    result.triggers.put(key.substring("trigger".length()), trigger);
                }
            }
            return result;
        }
    }
}
