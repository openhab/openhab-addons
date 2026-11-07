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
package org.openhab.binding.smartthings.internal.ocf;

import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Sanitizes OCF representations before they are written to diagnostic logs.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class ApplianceDiagnostics {
    private static final Set<String> SENSITIVE_KEYS = Set.of("di", "deviceid", "uuid", "serial", "serialnumber", "n",
            "dn", "name", "pi", "ownerid", "ownerpsk", "password", "passwd", "token", "secret", "credential",
            "credentials", "key", "auth", "authorization", "apikey", "accesskey", "privatekey", "certificate", "cert",
            "mac", "macaddress");

    private ApplianceDiagnostics() {
    }

    static JsonElement sanitize(JsonElement representation) {
        if (representation.isJsonArray()) {
            JsonArray sanitized = new JsonArray();
            representation.getAsJsonArray().forEach(value -> sanitized.add(sanitize(value)));
            return sanitized;
        }
        if (!representation.isJsonObject()) {
            return representation.deepCopy();
        }
        JsonObject sanitized = new JsonObject();
        representation.getAsJsonObject().entrySet().forEach(entry -> {
            if (isSensitive(entry.getKey())) {
                sanitized.addProperty(entry.getKey(), "[REDACTED]");
            } else {
                sanitized.add(entry.getKey(), sanitize(entry.getValue()));
            }
        });
        return sanitized;
    }

    private static boolean isSensitive(String key) {
        String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return SENSITIVE_KEYS.contains(normalized) || normalized.contains("password") || normalized.contains("token")
                || normalized.contains("secret") || normalized.contains("credential") || normalized.contains("ownerid")
                || normalized.contains("ownerpsk") || normalized.contains("deviceid") || normalized.contains("uuid")
                || normalized.contains("serial") || normalized.contains("privatekey")
                || normalized.contains("authorization") || normalized.contains("macaddress");
    }
}
