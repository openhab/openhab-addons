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
package org.openhab.binding.neohub.internal;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * A custom deserializer for NeohubBool; we need this because newer versions of
 * NeoHub have broken JSON for some boolean values so we can't use the standard
 * deserializer, and thus have to use a custom one
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
@NonNullByDefault
public class NeohubBoolDeserializer implements JsonDeserializer<NeohubBool> {

    @Override
    public @Nullable NeohubBool deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
        if (jsonPrimitive.isBoolean()) {
            return new NeohubBool(jsonPrimitive.getAsBoolean());
        } else if (jsonPrimitive.isNumber()) {
            return new NeohubBool(jsonPrimitive.getAsNumber().intValue() != 0);
        }

        return new NeohubBool(false);
    }
}
