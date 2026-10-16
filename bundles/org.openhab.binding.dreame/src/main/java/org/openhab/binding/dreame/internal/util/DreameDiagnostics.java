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
package org.openhab.binding.dreame.internal.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Produces diagnostic values that are safe to write to the openHAB log.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameDiagnostics {
    private static final String REDACTED = "***";
    private static final Set<String> SENSITIVE_KEYS = Set.of("access_token", "authorization", "dreame-auth", "email",
            "id", "jti", "mac", "masteruid", "mobile", "name", "password", "phone", "property", "refresh_token", "sn",
            "token", "u", "username");
    private static final Set<String> IDENTIFIER_KEYS = Set.of("dept_id", "did", "deviceid", "role_id", "uid");
    private static final Gson GSON = new Gson();

    private DreameDiagnostics() {
    }

    public static String sanitize(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                return GSON.toJson(sanitizeJson(JsonParser.parseString(trimmed)));
            } catch (RuntimeException ignored) {
                return REDACTED;
            }
        }
        if (trimmed.contains("=")) {
            return Arrays.stream(trimmed.split("&")).map(DreameDiagnostics::sanitizeFormEntry)
                    .collect(Collectors.joining("&"));
        }
        return REDACTED;
    }

    public static String maskIdentifier(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        if (value.length() <= 4) {
            return REDACTED;
        }
        return REDACTED + value.substring(value.length() - 4);
    }

    private static String sanitizeFormEntry(String entry) {
        int separator = entry.indexOf('=');
        if (separator < 0) {
            return entry;
        }
        String key = entry.substring(0, separator);
        String normalizedKey = URLDecoder.decode(key, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        String value = entry.substring(separator + 1);
        if (SENSITIVE_KEYS.contains(normalizedKey)) {
            value = REDACTED;
        } else if (IDENTIFIER_KEYS.contains(normalizedKey)) {
            value = maskIdentifier(value);
        }
        return key + "=" + value;
    }

    private static JsonElement sanitizeJson(JsonElement element) {
        if (element instanceof JsonObject object) {
            JsonObject sanitized = new JsonObject();
            object.entrySet().forEach(
                    entry -> sanitized.add(entry.getKey(), sanitizeJsonValue(entry.getKey(), entry.getValue())));
            return sanitized;
        }
        if (element instanceof JsonArray array) {
            JsonArray sanitized = new JsonArray();
            array.forEach(value -> sanitized.add(sanitizeJson(value)));
            return sanitized;
        }
        return element.deepCopy();
    }

    private static JsonElement sanitizeJsonValue(String key, JsonElement value) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_KEYS.contains(normalizedKey)) {
            return new JsonPrimitive(REDACTED);
        }
        if (IDENTIFIER_KEYS.contains(normalizedKey) && value.isJsonPrimitive()) {
            return new JsonPrimitive(maskIdentifier(value.getAsString()));
        }
        return sanitizeJson(value);
    }
}
