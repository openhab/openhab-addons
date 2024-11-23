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
package org.openhab.binding.freeboxos.internal.api.deserialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link ListDeserializer} is a specialized deserializer aimed to transform a null object, a single object or
 * a list of objects into a list containing 0, 1 or n elements.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ListDeserializer implements JsonDeserializer<List<?>> {

    @Override
    public @NonNull List<?> deserialize(@Nullable JsonElement json, @Nullable Type clazz,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json != null && clazz != null && context != null) {
            JsonArray jsonArray = toJsonArray(json);
            ArrayList<?> result = new ArrayList<>(jsonArray != null ? jsonArray.size() : 0);

            if (jsonArray != null) {
                Type[] typeArguments = ((ParameterizedType) clazz).getActualTypeArguments();
                if (typeArguments.length > 0) {
                    Type objectType = typeArguments[0];
                    for (int i = 0; i < jsonArray.size(); i++) {
                        result.add(context.deserialize(jsonArray.get(i), objectType));
                    }
                    return result;
                }
            }
        }
        return List.of();
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
