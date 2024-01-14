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
import org.openhab.io.neeo.internal.models.NeeoThingUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} and {@link JsonDeserializer} to serialize/deserial {@link NeeoThingUID}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoThingUIDSerializer implements JsonSerializer<NeeoThingUID>, JsonDeserializer<NeeoThingUID> {

    @Override
    public @Nullable NeeoThingUID deserialize(JsonElement elm, Type type, JsonDeserializationContext jsonContext)
            throws JsonParseException {
        if (elm.isJsonNull()) {
            throw new JsonParseException("Not a valid ChannelUID: (null)");
        }

        try {
            return new NeeoThingUID(elm.getAsString());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Not a valid thingUID: " + elm.getAsString(), e);
        }
    }

    @Override
    public JsonElement serialize(NeeoThingUID uid, Type type, JsonSerializationContext jsonContext) {
        return new JsonPrimitive(uid.getAsString());
    }
}
