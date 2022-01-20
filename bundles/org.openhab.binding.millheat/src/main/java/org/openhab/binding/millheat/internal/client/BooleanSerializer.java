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
package org.openhab.binding.millheat.internal.client;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * The {@link BooleanSerializer} serializes and parses 1/0 to true/false from JSON files
 *
 * @author Arne Seime - Initial contribution
 */
public class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(final JsonElement element, final Type type, final JsonDeserializationContext context)
            throws JsonParseException {
        return element.getAsInt() == 1;
    }

    @Override
    public JsonElement serialize(final Boolean argument, final Type type, final JsonSerializationContext context) {
        return new JsonPrimitive(Boolean.TRUE.equals(argument) ? 1 : 0);
    }
}
