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
package org.openhab.binding.windcentrale.internal.dto;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link WindcentraleGson} class provides a {@link Gson} instance configured for (de)serializing all Windcentrale
 * data from/to JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindcentraleGson {

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Windmill.class, new WindmillConverter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeConverter()) //
            .create();

    public static final Type LIVE_DATA_RESPONSE_TYPE = new TypeToken<Map<Windmill, WindmillStatus>>() {
    }.getType();

    public static final Type PROJECTS_RESPONSE_TYPE = new TypeToken<List<Project>>() {
    }.getType();

    private static class WindmillConverter implements JsonSerializer<Windmill>, JsonDeserializer<Windmill> {
        @Override
        public JsonElement serialize(Windmill src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getProjectCode());
        }

        @Override
        public @Nullable Windmill deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Windmill.fromProjectCode(json.getAsString());
        }
    }

    private static class ZonedDateTimeConverter
            implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
        @Override
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toEpochSecond());
        }

        @Override
        public @Nullable ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(json.getAsLong()), ZoneId.systemDefault());
        }
    }
}
