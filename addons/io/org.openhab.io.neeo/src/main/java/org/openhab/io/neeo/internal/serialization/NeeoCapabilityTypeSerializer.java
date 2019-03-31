/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.serialization;

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} and {@link JsonDeserializer} to serialize/deserial
 * {@link NeeoCapabilityType}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoCapabilityTypeSerializer
        implements JsonSerializer<NeeoCapabilityType>, JsonDeserializer<NeeoCapabilityType> {
    @Override
    public NeeoCapabilityType deserialize(@Nullable JsonElement elm, @Nullable Type type,
            @Nullable JsonDeserializationContext jsonContext) throws JsonParseException {
        Objects.requireNonNull(elm, "elm cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(jsonContext, "jsonContext cannot be null");

        if (elm.isJsonNull()) {
            throw new JsonParseException("NeeoCapabilityType could not be parsed from null");
        }

        return NeeoCapabilityType.parse(elm.getAsString());
    }

    @Override
    public JsonElement serialize(NeeoCapabilityType nct, @Nullable Type type,
            @Nullable JsonSerializationContext jsonContext) {
        Objects.requireNonNull(nct, "nct cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(jsonContext, "jsonContext cannot be null");

        return new JsonPrimitive(nct.toString());
    }
}
