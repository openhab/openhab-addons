/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

        JsonParser jsonParser = new JsonParser();
        JsonElement je = jsonParser.parse(in);

        if (je instanceof JsonPrimitive) {
            value = new AtomicReference<>();
            value.set(((JsonPrimitive) je).getAsString());
        } else if (je instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) je;
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
