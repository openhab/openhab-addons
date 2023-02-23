/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Pulled from Stack Overflow answer located here: http://stackoverflow.com/a/11272452
 * and placed in an appropriate package within this library.
 */
package org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Abstract type adapter factory.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public abstract class CustomizedTypeAdapterFactory<C> implements TypeAdapterFactory {
    private final Class<C> customizedClass;

    public CustomizedTypeAdapterFactory(Class<C> customizedClass) {
        this.customizedClass = customizedClass;
    }

    @Override
    @SuppressWarnings("unchecked") // we use a runtime check to guarantee that 'C' and 'T' are equal
    @Nullable
    public final <T> TypeAdapter<T> create(@Nullable Gson gson, @Nullable TypeToken<T> type) {
        return type.getRawType() == customizedClass
                ? (TypeAdapter<T>) customizeMyClassAdapter(gson, (TypeToken<C>) type)
                : null;
    }

    private TypeAdapter<C> customizeMyClassAdapter(@Nullable Gson gson, TypeToken<C> type) {
        final TypeAdapter<C> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
        return new TypeAdapter<C>() {
            @Override
            public void write(JsonWriter out, @Nullable C value) throws IOException {
                JsonElement tree = delegate.toJsonTree(value);
                beforeWrite(value, tree);
                elementAdapter.write(out, tree);
            }

            @Override
            public @Nullable C read(JsonReader in) throws IOException {
                JsonElement tree = elementAdapter.read(in);
                afterRead(tree);
                if (tree == null) {
                    throw new IOException("null reader");
                }
                return delegate.fromJsonTree(tree);
            }
        };
    }

    /**
     * Override this to muck with {@code toSerialize} before it is written to
     * the outgoing JSON stream.
     */
    protected void beforeWrite(C source, JsonElement toSerialize) {
    }

    /**
     * Override this to muck with {@code deserialized} before it parsed into the
     * application type.
     */
    protected void afterRead(@Nullable JsonElement deserialized) {
    }
}
