/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.serialization;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A GSON {@link TypeAdapter} that will properly write/read {@link AtomicReference} strings.
 *
 * @author Tim Roberts - Initial contribution
 */
public class AtomicStringTypeAdapter extends TypeAdapter<AtomicReference<String>> {

    /**
     * Overridden to read the string from the {@link JsonReader} and create an
     * {@link AtomicReference} from it.
     *
     * @param reader the non-null {@link JsonReader}
     * @return the atomic reference from the reader
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public AtomicReference<String> read(JsonReader reader) throws IOException {
        Objects.requireNonNull(reader, "reader cannot be null");

        final AtomicReference<String> value = new AtomicReference<>();

        final JsonParser jsonParser = new JsonParser();
        final JsonElement je = jsonParser.parse(reader);

        if (je.isJsonNull()) {
            value.set(null);
        } else if (je instanceof JsonPrimitive) {
            value.set(((JsonPrimitive) je).getAsString());
        } else if (je instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) je;
            value.set(jsonObject.get("value").getAsString());
        }

        return value;
    }

    /**
     * Overridden to write out the underlying string.
     *
     * @param writer the non-null writer
     * @param value the possibly null value
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void write(JsonWriter writer, AtomicReference<String> value) throws IOException {
        Objects.requireNonNull(writer, "writer cannot be null");

        if (value != null) {
            writer.value(value.get());
        }
    }
}
