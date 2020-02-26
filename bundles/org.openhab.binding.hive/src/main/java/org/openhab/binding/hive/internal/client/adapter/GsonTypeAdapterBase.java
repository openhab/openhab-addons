/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * An abstract base class for gson {@link TypeAdapter}s.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
abstract class GsonTypeAdapterBase<T> extends TypeAdapter<T> {
    @Override
    public final T read(final @Nullable JsonReader in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }

        return readValue(in);
    }

    @Override
    public final void write(final @Nullable JsonWriter out, final @Nullable T value) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out must not be null");
        }

        if (value == null) {
            out.nullValue();
        } else {
            this.writeValue(out, value);
        }
    }

    abstract @Nullable T readValue(final JsonReader in) throws IOException;

    public void writeValue(final JsonWriter out, final T value) throws IOException {
        // Default for simple types where toString() is good enough.
        out.value(value.toString());
    }
}
