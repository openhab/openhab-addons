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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link SerializeNullTypeAdapterFactory} is a {@link TypeAdapterFactory} for allowing annotation based
 * null-serialization
 * <p />
 * Fields that shall be serialized even if they are null need a {@link SerializeNull} annotation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SerializeNullTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> @Nullable TypeAdapter<T> create(@NonNullByDefault({}) Gson gson,
            @NonNullByDefault({}) TypeToken<T> type) {
        List<Field> fields = Arrays.asList(type.getRawType().getFields());
        if (fields.stream().noneMatch(field -> field.isAnnotationPresent(SerializeNull.class))) {
            // this type has no fields annotated with @SerializeNull, so we don't return a type adapter for this one
            return null;
        }

        List<String> nonNullFields = fields.stream().filter(field -> !field.isAnnotationPresent(SerializeNull.class))
                .map(this::getRealName).toList();

        TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(SerializeNullTypeAdapterFactory.this, type);
        TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

        return new SerializeNullTypeAdapter<>(delegateAdapter, elementAdapter, nonNullFields);
    }

    private String getRealName(Field field) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);
        return serializedName != null ? serializedName.value() : field.getName();
    }

    private static class SerializeNullTypeAdapter<T> extends TypeAdapter<T> {
        private final TypeAdapter<T> delegateTypeAdapter;
        private final TypeAdapter<JsonElement> elementTypeAdapter;
        private final List<String> nonNullFields;

        public SerializeNullTypeAdapter(TypeAdapter<T> delegateTypeAdapter, TypeAdapter<JsonElement> elementTypeAdapter,
                List<String> nonNullFields) {
            this.delegateTypeAdapter = delegateTypeAdapter;
            this.elementTypeAdapter = elementTypeAdapter;
            this.nonNullFields = nonNullFields;
        }

        @Override
        public void write(JsonWriter writer, @Nullable T value) throws IOException {
            JsonObject jsonObject = delegateTypeAdapter.toJsonTree(value).getAsJsonObject();

            // remove all null-fields that are not annotated with @SerializeNull
            nonNullFields.forEach(fieldName -> removeNullFields(jsonObject, fieldName));

            writer.setSerializeNulls(true);
            elementTypeAdapter.write(writer, jsonObject);
            writer.setSerializeNulls(false);
        }

        @Override
        public T read(JsonReader reader) throws IOException {
            return delegateTypeAdapter.read(reader);
        }

        private void removeNullFields(JsonObject jsonObject, String fieldName) {
            if (jsonObject.has(fieldName) && jsonObject.get(fieldName).isJsonNull()) {
                jsonObject.remove(fieldName);
            }
        }
    }
}
