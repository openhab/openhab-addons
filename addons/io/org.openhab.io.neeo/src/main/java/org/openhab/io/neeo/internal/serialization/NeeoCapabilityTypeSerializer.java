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
 * @author Tim Roberts - Initial contribution
 */
public class NeeoCapabilityTypeSerializer
        implements JsonSerializer<NeeoCapabilityType>, JsonDeserializer<NeeoCapabilityType> {
    @Override
    public NeeoCapabilityType deserialize(JsonElement elm, Type type, JsonDeserializationContext jsonContext)
            throws JsonParseException {
        Objects.requireNonNull(elm, "elm cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(jsonContext, "jsonContext cannot be null");

        if (elm.isJsonNull()) {
            throw new JsonParseException("NeeoCapabilityType could not be parsed from null");
        }

        final NeeoCapabilityType nct = NeeoCapabilityType.parse(elm.getAsString());
        if (nct == null) {
            throw new JsonParseException("Unknown NeeoCapabilityType: " + elm.getAsString());
        }
        return nct;
    }

    @Override
    public JsonElement serialize(NeeoCapabilityType nct, Type type, JsonSerializationContext jsonContext) {
        Objects.requireNonNull(nct, "nct cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(jsonContext, "jsonContext cannot be null");

        return new JsonPrimitive(nct.toString());
    }
}
