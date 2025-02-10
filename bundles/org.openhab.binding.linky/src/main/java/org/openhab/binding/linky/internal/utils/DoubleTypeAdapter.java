/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.utils;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * {@link DoubleTypeAdapter} A type adapter for gson / double.
 *
 * Will prevent Null exception error when api return incomplete value.
 * We can have this scenario when we ask to today consumption after midnight, but before enedis update the value.
 * In this case, we don't want to failed all the data just because of one missing value.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class DoubleTypeAdapter extends TypeAdapter<Double> {

    @Override
    public Double read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return Double.NaN;
        }
        String stringValue = reader.nextString();
        try {
            Double value = Double.valueOf(stringValue);
            return value;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    @Override
    public void write(JsonWriter writer, Double value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value.doubleValue());
    }
}
