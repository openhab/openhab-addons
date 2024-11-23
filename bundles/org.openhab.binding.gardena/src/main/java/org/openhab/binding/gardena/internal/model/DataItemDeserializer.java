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
package org.openhab.binding.gardena.internal.model;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer for Gardena DataItems.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class DataItemDeserializer implements JsonDeserializer<DataItem<?>> {
    private static Gson gson = new GsonBuilder().create();

    @Override
    public @Nullable DataItem<?> deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
        try {
            JsonObject jsonObj = element.getAsJsonObject();
            return gson.fromJson(element, DataItemFactory.create(jsonObj.get("type").getAsString()));
        } catch (GardenaException ex) {
            throw new JsonParseException(ex.getMessage(), ex);
        }
    }
}
