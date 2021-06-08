/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.deserialization;

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
 * This enforces a fallback to UNKNOWN when deserializing enum types, marked as
 *
 * @NonNull whereas they were valued to null if the appropriate value is absent.
 *          It will give more resilience to the binding when Netatmo API evolves.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class StrictEnumTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public @Nullable <T> TypeAdapter<T> create(@NonNullByDefault({}) Gson gson,
            @NonNullByDefault({}) TypeToken<T> type) {
        @SuppressWarnings("unchecked")
        Class<T> rawType = (Class<T>) type.getRawType();
        if (!rawType.isEnum()) {
            return null;
        }
        return newStrictEnumAdapter(gson.getDelegateAdapter(this, type));
    }

    private <T> TypeAdapter<T> newStrictEnumAdapter(final TypeAdapter<T> delegateAdapter) {
        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, @Nullable T value) throws IOException {
                delegateAdapter.write(out, value);
            }

            @Override
            public @Nullable T read(JsonReader in) throws IOException {
                String enumValue = in.nextString();
                JsonReader delegateReader = new JsonReader(new StringReader('"' + enumValue + '"'));
                T value = delegateAdapter.read(delegateReader);
                if (value == null) {
                    value = delegateAdapter.read(new JsonReader(new StringReader("\"UNKNOWN\"")));
                }
                delegateReader.close();
                return value;
            }
        };
    }
}
