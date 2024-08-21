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
/*
 * Imported from https://github.com/google-gson/typeadapters/tree/master/jsr310/src
 * and repackaged to avoid the default package.
 */
package org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Abstract type adapter for jsr310 date-time types.
 *
 * @author Christophe Bornet - Initial contribution
 */
@NonNullByDefault
abstract class TemporalTypeAdapter<T> extends TypeAdapter<T> {

    Function<String, T> parseFunction;

    TemporalTypeAdapter(Function<String, T> parseFunction) {
        Objects.requireNonNull(parseFunction);
        this.parseFunction = parseFunction;
    }

    @Override
    public void write(JsonWriter out, @Nullable T value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public @Nullable T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String temporalString = preProcess(in.nextString());
        return parseFunction.apply(temporalString);
    }

    public String preProcess(String in) {
        return in;
    }
}
