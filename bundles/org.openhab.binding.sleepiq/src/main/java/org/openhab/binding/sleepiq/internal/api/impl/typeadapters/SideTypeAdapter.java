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
package org.openhab.binding.sleepiq.internal.api.impl.typeadapters;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sleepiq.internal.api.enums.Side;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * The {@link SideTypeAdapter} converts the Side enum to the format expected by the sleepiq API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SideTypeAdapter implements JsonDeserializer<Side>, JsonSerializer<Side> {

    @Override
    public JsonElement serialize(Side side, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(side.equals(Side.LEFT) ? "L" : "R");
    }

    @Override
    public @Nullable Side deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        int key = element.getAsInt();
        return Side.forValue(key);
    }
}
