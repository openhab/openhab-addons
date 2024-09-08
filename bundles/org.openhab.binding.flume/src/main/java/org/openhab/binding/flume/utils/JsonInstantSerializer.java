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

package org.openhab.binding.flume.utils;

import java.lang.reflect.Type;
import java.time.Instant;
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
 * {@link JsonInstantSerializer} implements gson serializer for Java Instant
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JsonInstantSerializer implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    private DateTimeFormatter dtf;

    public JsonInstantSerializer() {
        dtf = DateTimeFormatter.ISO_INSTANT;
    }

    public JsonInstantSerializer(String format) {
        dtf = DateTimeFormatter.ofPattern(format);
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(dtf.format(src));
    }

    @Override
    public @Nullable Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return dtf.parse(json.getAsString(), Instant::from);
    }
}
