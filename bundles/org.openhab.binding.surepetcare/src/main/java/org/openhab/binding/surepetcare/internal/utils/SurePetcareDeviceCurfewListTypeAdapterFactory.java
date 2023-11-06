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
package org.openhab.binding.surepetcare.internal.utils;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceCurfew;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceCurfewList;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

/**
 * The {@link SurePetcareDeviceCurfewListTypeAdapterFactory} class is a custom TypeAdapter factory to ensure
 * deserialization always returns a list even if the Json document contains only a single curfew object and
 * not an array.
 *
 * See https://stackoverflow.com/questions/43412261/make-gson-accept-single-objects-where-it-expects-arrays
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public final class SurePetcareDeviceCurfewListTypeAdapterFactory<E> implements TypeAdapterFactory {

    // Gson can instantiate it itself
    private SurePetcareDeviceCurfewListTypeAdapterFactory() {
    }

    @Override
    public @Nullable <T> TypeAdapter<T> create(final @Nullable Gson gson, final @Nullable TypeToken<T> typeToken) {
        if (gson == null || typeToken == null) {
            return null;
        }
        final TypeAdapter<SurePetcareDeviceCurfew> elementTypeAdapter = gson
                .getAdapter(TypeToken.get(SurePetcareDeviceCurfew.class));
        @SuppressWarnings("unchecked")
        final TypeAdapter<T> alwaysListTypeAdapter = (TypeAdapter<T>) new SurePetcareDeviceCurfewListTypeAdapter(
                elementTypeAdapter).nullSafe();
        return alwaysListTypeAdapter;
    }

    private static final class SurePetcareDeviceCurfewListTypeAdapter
            extends TypeAdapter<List<SurePetcareDeviceCurfew>> {

        private final TypeAdapter<SurePetcareDeviceCurfew> elementTypeAdapter;

        private SurePetcareDeviceCurfewListTypeAdapter(final TypeAdapter<SurePetcareDeviceCurfew> elementTypeAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
        }

        @Override
        public void write(final @Nullable JsonWriter out, @Nullable final List<SurePetcareDeviceCurfew> list)
                throws IOException {
            if (out != null && list != null) {
                out.beginArray();
                for (SurePetcareDeviceCurfew curfew : list) {
                    elementTypeAdapter.write(out, curfew);
                }
                out.endArray();
            }
        }

        @Override
        public @Nullable List<SurePetcareDeviceCurfew> read(final @Nullable JsonReader in) throws IOException {
            // This is where we detect the list "type"
            final SurePetcareDeviceCurfewList list = new SurePetcareDeviceCurfewList();
            if (in != null) {
                final JsonToken token = in.peek();
                switch (token) {
                    case BEGIN_ARRAY:
                        // If it's a regular list, just consume [, <all elements>, and ]
                        in.beginArray();
                        while (in.hasNext()) {
                            SurePetcareDeviceCurfew cf = elementTypeAdapter.read(in);
                            if (cf != null) {
                                list.add(cf);
                            }
                        }
                        in.endArray();
                        break;
                    case BEGIN_OBJECT:
                        SurePetcareDeviceCurfew cf = elementTypeAdapter.read(in);
                        if (cf != null) {
                            list.add(cf);
                        }
                        break;
                    case NULL:
                        throw new AssertionError(
                                "Must never happen: check if the type adapter configured with .nullSafe()");
                    case STRING:
                    case NUMBER:
                    case BOOLEAN:
                    case NAME:
                    case END_ARRAY:
                    case END_OBJECT:
                    case END_DOCUMENT:
                        throw new MalformedJsonException("Unexpected token: " + token);
                    default:
                        throw new AssertionError("Must never happen: " + token);
                }
            }
            return list;
        }
    }
}
