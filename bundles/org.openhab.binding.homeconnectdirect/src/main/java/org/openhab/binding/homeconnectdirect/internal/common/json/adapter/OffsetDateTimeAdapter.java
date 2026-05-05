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
package org.openhab.binding.homeconnectdirect.internal.common.json.adapter;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson adapter for {@link java.time.OffsetDateTime} serialization and deserialization.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class OffsetDateTimeAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public JsonElement serialize(@Nullable OffsetDateTime src, @Nullable Type typeOfSrc,
            @Nullable JsonSerializationContext context) {
        if (src == null) {
            return new JsonPrimitive("");
        }
        return new JsonPrimitive(src.format(FORMATTER));
    }

    @Override
    public @Nullable OffsetDateTime deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        return OffsetDateTime.parse(json.getAsString(), FORMATTER);
    }
}
