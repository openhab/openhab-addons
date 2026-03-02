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
package org.openhab.binding.viessmann.internal.util;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link GsonJsonElementSerializer} provides Jackson serializer
 * for Gson JsonElement trees (JsonObject/JsonArray/JsonPrimitive).
 * Prevents Jackson from reflecting into Gson getters like "asDouble".
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class GsonJsonElementSerializer extends JsonSerializer<JsonElement> {

    @Override
    @NonNullByDefault({})
    public void serialize(JsonElement value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        write(value, gen);
    }

    private static void write(JsonElement el, JsonGenerator gen) throws IOException {
        if (el instanceof JsonNull || el.isJsonNull()) {
            gen.writeNull();
            return;
        }

        if (el.isJsonPrimitive()) {
            JsonPrimitive p = el.getAsJsonPrimitive();
            if (p.isBoolean()) {
                gen.writeBoolean(p.getAsBoolean());
            } else if (p.isNumber()) {
                // BigDecimal avoids double rounding noise in debug JSON
                gen.writeNumber(p.getAsBigDecimal());
            } else {
                gen.writeString(p.getAsString());
            }
            return;
        }

        if (el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            gen.writeStartArray();
            for (JsonElement e : arr) {
                write(e, gen);
            }
            gen.writeEndArray();
            return;
        }

        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            gen.writeStartObject();
            for (var entry : obj.entrySet()) {
                gen.writeFieldName(entry.getKey());
                write(entry.getValue(), gen);
            }
            gen.writeEndObject();
            return;
        }

        // Fallback (should not happen)
        gen.writeString(el.toString());
    }
}
