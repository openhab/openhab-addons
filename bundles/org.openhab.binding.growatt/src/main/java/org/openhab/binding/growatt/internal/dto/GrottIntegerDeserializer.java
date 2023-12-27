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
package org.openhab.binding.growatt.internal.dto;

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Special deserializer for integer values. Handles integer overflows gently.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class GrottIntegerDeserializer implements JsonDeserializer<Integer> {

    @Override
    public @NonNull Integer deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        long value = Long.parseLong(Objects.requireNonNull(json).getAsString());
        return Long.valueOf(value).intValue();
    }
}
