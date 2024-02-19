/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal.utilities;

import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Some helper method to ease and centralize use of GSON.
 *
 * @author Patrik Gfeller - Initial Contribution
 * @author Simon Spielmann - Rename and generalization
 *
 */
@NonNullByDefault
public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().create();

    private static final Type STRING_OBJ_MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    /**
     * Parse JSON to {@link Map}
     *
     * @param json JSON String or {@code null}.
     * @return Parsed data or {@code null}
     * @throws JsonSyntaxException If there is a JSON syntax error.
     */
    public static @Nullable Map<String, Object> toMap(@Nullable String json) throws JsonSyntaxException {
        return GSON.fromJson(json, STRING_OBJ_MAP_TYPE);
    }

    /**
     * Converts to JSON with {@link Gson}{@link #toJson(Object)}.
     *
     * @param data Data to convert.
     * @return JSON representation of data.
     */
    public static @Nullable String toJson(@Nullable Object data) {
        return GSON.toJson(data);
    }

    /**
     * Defaults to {@link Gson#fromJson(String, Class)}.
     *
     * @param data Data to parse.
     * @param classOfT Destination type
     * @param <T> Destination type param
     * @return Given type or {@code null}.
     *
     * @see Gson#fromJson(String, Class)
     */
    public static <@Nullable T> T fromJson(String data, Class<@NonNull T> classOfT) {
        return GSON.fromJson(data, classOfT);
    }
}
