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
package org.openhab.binding.airparif.internal.deserialization;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Enforces a fallback to UNKNOWN when deserializing enum types, marked as @NonNull whereas they were valued
 * to null if the appropriate value is absent.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class StrictEnumTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public @Nullable <T> TypeAdapter<T> create(@NonNullByDefault({}) Gson gson,
            @NonNullByDefault({}) TypeToken<T> type) {
        return type.getRawType().isEnum() ? newStrictEnumAdapter(gson.getDelegateAdapter(this, type)) : null;
    }

    private <T> TypeAdapter<T> newStrictEnumAdapter(@NonNullByDefault({}) TypeAdapter<T> delegateAdapter) {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, @Nullable T value) throws IOException {
                delegateAdapter.write(out, value);
            }

            @Override
            public @Nullable T read(JsonReader in) throws IOException {
                JsonReader delegateReader = new JsonReader(
                        new StringReader('"' + in.nextString().replace(",", "") + '"'));
                @Nullable
                T value = delegateAdapter.read(delegateReader);
                delegateReader.close();
                if (value == null) {
                    value = delegateAdapter.read(new JsonReader(new StringReader("\"UNKNOWN\"")));
                }
                return value;
            }
        };
    }
}
