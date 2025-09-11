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
package org.openhab.binding.amazonechocontrol.internal.util;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link NonNullListTypeAdapterFactory} is a {@link TypeAdapterFactory} for allowing annotation based
 * null-serialization
 * <p />
 * Fields that shall be serialized even if they are null need a {@link SerializeNull} annotation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class NonNullListTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable TypeAdapter<T> create(@NonNullByDefault({}) Gson gson,
            @NonNullByDefault({}) TypeToken<T> type) {

        Class<T> rawType = (Class<T>) type.getRawType();
        if (rawType != List.class) {
            return null;
        }
        TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(NonNullListTypeAdapterFactory.this, type);

        return new DeserializeNonNullTypeAdapter<>(delegateAdapter);
    }

    private static class DeserializeNonNullTypeAdapter<T> extends TypeAdapter<T> {
        private final TypeAdapter<T> delegateTypeAdapter;

        public DeserializeNonNullTypeAdapter(TypeAdapter<T> delegateTypeAdapter) {
            this.delegateTypeAdapter = delegateTypeAdapter;
        }

        @Override
        public void write(JsonWriter writer, @Nullable T value) throws IOException {
            delegateTypeAdapter.write(writer, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable T read(JsonReader reader) throws IOException {
            final JsonToken peek = reader.peek();
            if (peek == JsonToken.NULL) {
                reader.nextNull();
                return (T) List.of();
            }
            return delegateTypeAdapter.read(reader);
        }
    }
}
