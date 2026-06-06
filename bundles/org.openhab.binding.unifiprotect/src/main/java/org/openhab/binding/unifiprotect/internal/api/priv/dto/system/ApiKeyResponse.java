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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * API response wrapper for API key operations
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ApiKeyResponse {
    private static final Gson GSON = new Gson();

    public int code;

    @SerializedName("codeS")
    public @Nullable String codeString;

    public @Nullable String msg;
    public @Nullable Object data; // Gson deserializes as Map or List<Map>, converted via helper methods

    public @Nullable ApiKey getApiKey() {
        if (data instanceof Map) {
            return GSON.fromJson(GSON.toJsonTree(data), ApiKey.class);
        }
        return null;
    }

    public List<ApiKey> getApiKeys() {
        if (data instanceof List<?> list) {
            List<ApiKey> keys = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map) {
                    ApiKey key = GSON.fromJson(GSON.toJsonTree(item), ApiKey.class);
                    if (key != null) {
                        keys.add(key);
                    }
                }
            }
            return keys;
        }
        return List.of();
    }
}
