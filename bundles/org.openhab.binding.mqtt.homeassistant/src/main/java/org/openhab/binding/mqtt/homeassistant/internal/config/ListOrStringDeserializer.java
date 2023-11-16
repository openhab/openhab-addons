/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * JsonTypeAdapter which will read a single string or a string list
 *
 * see: https://www.home-assistant.io/components/binary_sensor.mqtt/ -> device / identifiers
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class ListOrStringDeserializer extends TypeAdapter<List<String>> {

    @Override
    public void write(@Nullable JsonWriter out, @Nullable List<String> value) throws IOException {
        Objects.requireNonNull(out);

        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginArray();
        for (String str : value) {
            out.jsonValue(str);
        }
        out.endArray();
    }

    @Override
    public @Nullable List<String> read(@Nullable JsonReader in) throws IOException {
        Objects.requireNonNull(in);

        JsonToken peek = in.peek();

        switch (peek) {
            case NULL:
                in.nextNull();
                return null;
            case STRING:
                return List.of(in.nextString());
            case BEGIN_ARRAY:
                return readList(in);
            default:
                throw new IOException("unexpected token " + peek + ". Array of string or string expected");
        }
    }

    private List<String> readList(JsonReader in) throws IOException {
        in.beginArray();

        List<String> result = new ArrayList<>();

        JsonToken peek = in.peek();

        while (peek != JsonToken.END_ARRAY) {
            if (peek == JsonToken.STRING) {
                result.add(in.nextString());
            } else {
                throw new IOException("unexpected token " + peek + ". Array of string or string expected");
            }
            peek = in.peek();
        }
        in.endArray();

        return result;
    }
}
