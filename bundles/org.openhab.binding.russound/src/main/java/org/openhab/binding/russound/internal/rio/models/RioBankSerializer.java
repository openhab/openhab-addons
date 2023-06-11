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
package org.openhab.binding.russound.internal.rio.models;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * A {@link JsonSerializer} and {@link JsonDeserializer} for the {@link RioBank}. Simply writes/reads the ID and name to
 * elements called "id" and "name"
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioBankSerializer implements JsonSerializer<RioBank>, JsonDeserializer<RioBank> {

    /**
     * Overridden to simply write out the id/name elements from the {@link RioBank}
     *
     * @param bank the {@link RioBank} to write out
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public JsonElement serialize(RioBank bank, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("id", bank.getId());
        root.addProperty("name", bank.getName());

        return root;
    }

    /**
     * Overridden to simply read the id/name elements and create a {@link RioBank}
     *
     * @param elm the {@link JsonElement} to read from
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public RioBank deserialize(JsonElement elm, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jo = (JsonObject) elm;

        final JsonElement id = jo.get("id");
        final JsonElement name = jo.get("name");
        return new RioBank((id == null ? -1 : id.getAsInt()), (name == null ? null : name.getAsString()));
    }
}
