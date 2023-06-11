/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.thing.ChannelUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} and {@link JsonDeserializer} to serialize/deserial {@link ChannelUID}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class ChannelUIDSerializer implements JsonSerializer<ChannelUID>, JsonDeserializer<ChannelUID> {

    @Override
    public @Nullable ChannelUID deserialize(JsonElement elm, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        if (elm.isJsonNull()) {
            throw new JsonParseException("Not a valid ChannelUID: (null)");
        }

        try {
            return new ChannelUID(elm.getAsString());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Not a valid ChannelUID: " + elm.getAsString(), e);
        }
    }

    @Override
    public JsonElement serialize(ChannelUID uid, @Nullable Type type, @Nullable JsonSerializationContext context) {
        Objects.requireNonNull(uid, "uid cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        return new JsonPrimitive(uid.getAsString());
    }
}
