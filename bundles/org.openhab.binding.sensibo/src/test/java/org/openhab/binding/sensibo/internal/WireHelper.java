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
package org.openhab.binding.sensibo.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Arne Seime - Initial contribution
 */
public class WireHelper {

    private final Gson gson;

    public WireHelper() {
        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(final JsonWriter out, final ZonedDateTime value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public ZonedDateTime read(final JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }
        }).setPrettyPrinting().create();
    }

    public <T> T deSerializeResponse(final String jsonClasspathName, final Type type) throws IOException {
        final String json = new String(WireHelper.class.getResourceAsStream(jsonClasspathName).readAllBytes(),
                StandardCharsets.UTF_8);

        final JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("success", o.get("status").getAsString());

        return gson.fromJson(o.get("result"), type);
    }

    public <T> T deSerializeFromClasspathResource(final String jsonClasspathName, final Type type) throws IOException {
        final String json = new String(WireHelper.class.getResourceAsStream(jsonClasspathName).readAllBytes(),
                StandardCharsets.UTF_8);
        return deSerializeFromString(json, type);
    }

    public <T> T deSerializeFromString(final String json, final Type type) throws IOException {
        return gson.fromJson(json, type);
    }

    public <T> String serialize(final AbstractRequest req) throws IOException {
        return gson.toJson(req);
    }
}
