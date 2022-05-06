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
package org.openhab.binding.netatmo.internal.deserialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link NAObjectMapDeserializer} is a specialized deserializer aimed to transform
 * a list of `NAObjects` into a map identified by the object's id.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class NAObjectMapDeserializer implements JsonDeserializer<NAObjectMap<?>> {
    @Override
    public @Nullable NAObjectMap<?> deserialize(JsonElement json, Type clazz, JsonDeserializationContext context)
            throws JsonParseException {
        ParameterizedType parameterized = (ParameterizedType) clazz;
        Type[] typeArguments = parameterized.getActualTypeArguments();
        if (typeArguments.length > 0 && json instanceof JsonArray) {
            Type objectType = typeArguments[0];
            NAObjectMap<NAObject> result = new NAObjectMap<>();
            ((JsonArray) json).forEach(item -> {
                result.put(context.deserialize(item, objectType));
            });
            return result;
        }
        return null;
    }
}
