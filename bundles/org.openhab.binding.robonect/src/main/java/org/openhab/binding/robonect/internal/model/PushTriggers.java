/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
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
 * @author Nico Brüttner - Dynamic push triggers
 */
@NonNullByDefault
@JsonAdapter(PushTriggers.Deserializer.class)
public class PushTriggers {
    private @Nullable Integer interval;
    private final Map<String, Trigger> triggers = new HashMap<>();

    public @Nullable Integer getInterval() {
        return interval;
    }

    public Map<String, Trigger> getTriggers() {
        return Map.copyOf(triggers);
    }

    public static class Trigger {
        private @Nullable String name;
        private @Nullable Boolean enter;
        private @Nullable Boolean leave;

        public @Nullable String getName() {
            return name;
        }

        public @Nullable Boolean getEnter() {
            return enter;
        }

        public @Nullable Boolean getLeave() {
            return leave;
        }
    }

    public static class Deserializer implements JsonDeserializer<PushTriggers> {
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
                    result.triggers.put(key.substring("trigger".length()),
                            context.deserialize(entry.getValue(), Trigger.class));
                }
            }
            return result;
        }
    }
}
