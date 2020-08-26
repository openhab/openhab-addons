/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.openhab.binding.gardena.internal.model.api.DataItem;

import com.google.gson.*;

/**
 * Custom deserializer for Gardena DataItems.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DataItemDeserializer implements JsonDeserializer<DataItem<?>> {
    private static Gson gson = new GsonBuilder().create();

    @Override
    public DataItem deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
        JsonObject jsonObj = element.getAsJsonObject();
        return gson.fromJson(element, DataItemFactory.create(jsonObj.get("type").getAsString()));
    }
}
