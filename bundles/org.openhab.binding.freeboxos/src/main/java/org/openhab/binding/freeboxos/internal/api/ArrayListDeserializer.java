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
package org.openhab.binding.freeboxos.internal.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link ArrayListDeserializer} is a specialized deserializer aimed to transform a null object, a single object or
 * a list of objects into a list.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class ArrayListDeserializer implements JsonDeserializer<List<?>> {

    @Override
    public @Nullable List<?> deserialize(JsonElement json, Type clazz, JsonDeserializationContext context)
            throws JsonParseException {

        JsonArray jsonArray = toJsonArray(json);
        ArrayList<?> result = new ArrayList<>(jsonArray != null ? jsonArray.size() : 0);

        if (jsonArray != null) {
            ParameterizedType parameterized = (ParameterizedType) clazz;
            Type[] typeArguments = parameterized.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type objectType = typeArguments[0];
                ((JsonArray) json).forEach(item -> {
                    result.add(context.deserialize(item, objectType));
                });
            }
        }
        return result;
    }

    private @Nullable JsonArray toJsonArray(JsonElement json) {
        if (json instanceof JsonArray) {
            return json.getAsJsonArray();
        } else if (json instanceof JsonObject) {
            JsonArray jsonArray = new JsonArray();
            if (json.getAsJsonObject().size() > 0) {
                jsonArray.add(json);
            }
            return jsonArray;
        }
        return null;
    }
}
