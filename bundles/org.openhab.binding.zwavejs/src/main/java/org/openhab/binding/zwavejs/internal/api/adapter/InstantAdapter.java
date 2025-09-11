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

package org.openhab.binding.zwavejs.internal.api.adapter;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * * The {@link InstantAdapter} is a custom Gson TypeAdapter for serializing and deserializing an Instant object.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InstantAdapter extends TypeAdapter<@Nullable Instant> {
    @Override
    public void write(@Nullable JsonWriter out, @Nullable Instant value) throws IOException {
        if (out == null) {
            return;
        }
        out.value(value == null ? null : value.toString());
    }

    @Override
    public @Nullable Instant read(@Nullable JsonReader in) throws IOException {
        if (in == null) {
            return null;
        }

        try {
            return Instant.parse(in.nextString());
        } catch (DateTimeParseException e) {
            // If the string cannot be parsed as an Instant, return null
            return null;
        }
    }
}
