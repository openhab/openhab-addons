/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.dto;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * The {@link SDMGson} class provides a {@link Gson} instance configured for (de)serializing all SDM and Pub/Sub data
 * from/to JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMGson {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SDMResourceName.class, new SDMResourceNameConverter()) //
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeConverter()) //
            .create();

    private static class SDMResourceNameConverter
            implements JsonSerializer<SDMResourceName>, JsonDeserializer<SDMResourceName> {

        @Override
        public JsonElement serialize(SDMResourceName src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public @Nullable SDMResourceName deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new SDMResourceName(json.getAsString());
        }
    }

    private static class ZonedDateTimeConverter
            implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(FORMATTER.format(src));
        }

        @Override
        public @Nullable ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return ZonedDateTime.parse(json.getAsString());
        }
    }
}
