/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
