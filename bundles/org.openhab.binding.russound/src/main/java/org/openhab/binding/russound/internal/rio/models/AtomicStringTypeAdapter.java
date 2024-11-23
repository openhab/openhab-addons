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
package org.openhab.binding.russound.internal.rio.models;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A GSON {@link TypeAdapter} that will properly write/read {@link AtomicReference} strings
 *
 * @author Tim Roberts - Initial contribution
 */
public class AtomicStringTypeAdapter extends TypeAdapter<AtomicReference<String>> {

    /**
     * Overriden to read the string from the {@link JsonReader} and create an
     * {@link AtomicReference} from it
     */
    @Override
    public AtomicReference<String> read(JsonReader in) throws IOException {
        AtomicReference<String> value = null;

        JsonElement je = JsonParser.parseReader(in);

        if (je instanceof JsonPrimitive jsonPrimitive) {
            value = new AtomicReference<>();
            value.set(jsonPrimitive.getAsString());
        } else if (je instanceof JsonObject jsonObject) {
            value = new AtomicReference<>();
            value.set(jsonObject.get("value").getAsString());
        }

        return value;
    }

    /**
     * Overridden to write out the underlying string
     */
    @Override
    public void write(JsonWriter out, AtomicReference<String> value) throws IOException {
        if (value != null) {
            out.value(value.get());
        }
    }
}
