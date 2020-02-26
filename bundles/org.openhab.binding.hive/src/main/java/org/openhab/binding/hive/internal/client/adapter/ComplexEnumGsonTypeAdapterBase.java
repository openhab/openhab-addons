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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
abstract class ComplexEnumGsonTypeAdapterBase<E extends Enum<E>> extends GsonTypeAdapterBase<E> {
    private final EnumMapper<E> enumMap;

    protected ComplexEnumGsonTypeAdapterBase(final EnumMapper<E> enumMap) {
        this.enumMap = Objects.requireNonNull(enumMap);
    }

    @Override
    public @Nullable E readValue(final JsonReader in) throws IOException {
        final @Nullable String enumString = in.nextString();

        if (enumString != null) {
            return this.enumMap.getEnumForString(enumString);
        } else {
            return null;
        }
    }

    @Override
    public void writeValue(final JsonWriter out, final E value) throws IOException {
        out.value(this.enumMap.getStringForEnum(value));
    }
}
