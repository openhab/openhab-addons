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
package org.openhab.binding.mqtt.awtrixlight.internal.ng;

import static org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgBindingConstants.APP_NAME_PATTERN;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.NotificationPayload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link NotificationParams} turns the parameters of a rule into a notification. They use the keys of the payload
 * of NG. AWTRIX NG rejects a payload with an unknown key as a whole, and tells only the clock's error topic, so the
 * keys are checked here to tell the rule.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class NotificationParams {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private NotificationParams() {
    }

    /**
     * @throws IllegalArgumentException if a key is not one of the notification payload, or a value does not fit it
     */
    public static NotificationPayload toPayload(Map<String, ?> params) {
        // A rule can pass null, which is not an object
        if (!(GSON.toJsonTree(params) instanceof JsonObject input)) {
            throw new IllegalArgumentException("The notification is empty");
        }
        normalize(input);
        NotificationPayload payload;
        try {
            payload = GSON.fromJson(input, NotificationPayload.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("A value does not fit its key: " + e.getMessage(), e);
        }
        if (payload == null) {
            throw new IllegalArgumentException("The notification is empty");
        }
        // What the payload writes out differently, or not at all, was not a key of it, or Gson made something of a
        // value that does not fit (a boolean from the text "yes")
        JsonObject output = GSON.toJsonTree(payload).getAsJsonObject();
        Set<String> unknown = new TreeSet<>();
        Set<String> misfit = new TreeSet<>();
        for (Map.Entry<String, JsonElement> entry : input.entrySet()) {
            JsonElement written = output.get(entry.getKey());
            if (written == null) {
                unknown.add(entry.getKey());
            } else if (!written.equals(entry.getValue())) {
                misfit.add(entry.getKey());
            }
        }
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown notification keys: " + String.join(", ", unknown));
        }
        if (!misfit.isEmpty()) {
            throw new IllegalArgumentException("A value does not fit its key: " + String.join(", ", misfit));
        }
        String name = payload.name;
        if (name != null && !APP_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("The name must have 1 to 32 letters, digits, - or _: " + name);
        }
        return payload;
    }

    /** Accepts the short forms of NG: a sound given by its name, and a text with font tags. */
    private static void normalize(JsonObject input) {
        JsonElement sound = input.get("sound");
        if (sound != null && sound.isJsonPrimitive()) {
            JsonObject file = new JsonObject();
            file.add("file", sound);
            input.add("sound", file);
        }
        JsonElement text = input.get("text");
        if (text != null && text.isJsonPrimitive()) {
            input.add("text", TextMarkup.toJson(text.getAsString(), null));
        }
    }
}
