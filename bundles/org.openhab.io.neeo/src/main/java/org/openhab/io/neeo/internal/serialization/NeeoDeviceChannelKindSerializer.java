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
package org.openhab.io.neeo.internal.serialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelKind;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} and {@link JsonDeserializer} to serialize/deserial
 * {@link NeeoDeviceChannelKind}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceChannelKindSerializer
        implements JsonSerializer<NeeoDeviceChannelKind>, JsonDeserializer<NeeoDeviceChannelKind> {
    @Override
    public @Nullable NeeoDeviceChannelKind deserialize(JsonElement elm, Type type,
            JsonDeserializationContext jsonContext) throws JsonParseException {
        if (elm.isJsonNull()) {
            throw new JsonParseException("NeeoDeviceChannelKind could not be parsed from null");
        }

        return NeeoDeviceChannelKind.parse(elm.getAsString());
    }

    @Override
    public JsonElement serialize(NeeoDeviceChannelKind ndck, Type type, JsonSerializationContext jsonContext) {
        return new JsonPrimitive(ndck.toString());
    }
}
