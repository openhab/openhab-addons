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
package org.openhab.binding.growatt.internal.dto.helper;

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Special deserializer for integer values. It processes inputs which overflow the Integer.MAX_VALUE limit by
 * transposing them to negative numbers by means of the 2's complement process.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrottIntegerDeserializer implements JsonDeserializer<Integer> {

    private static final long INT_BIT_MASK = 0xffffffff;

    @Override
    public @NonNull Integer deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        long value = Long.parseLong(Objects.requireNonNull(json).getAsString());
        if (value > Integer.MAX_VALUE) {
            // transpose values above Integer.MAX_VALUE to a negative int by 2's complement
            return Integer.valueOf(1 - (int) (value ^ INT_BIT_MASK));
        }
        return Long.valueOf(value).intValue();
    }
}
