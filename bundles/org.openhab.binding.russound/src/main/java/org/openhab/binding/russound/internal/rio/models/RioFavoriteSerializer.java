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
 * A {@link JsonSerializer} and {@link JsonDeserializer} for the {@link RioFavorite}. Simply writes/reads the ID and
 * name to elements called "id", "valid" and "name"
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioFavoriteSerializer implements JsonSerializer<RioFavorite>, JsonDeserializer<RioFavorite> {

    /**
     * Overridden to simply write out the id/valid/name elements from the {@link RioFavorite}
     *
     * @param favorite the {@link RioFavorite} to write out
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public JsonElement serialize(RioFavorite favorite, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("id", favorite.getId());
        root.addProperty("valid", favorite.isValid());
        root.addProperty("name", favorite.getName());

        return root;
    }

    /**
     * Overridden to simply read the id/valid/name elements and create a {@link RioFavorite}
     *
     * @param elm the {@link JsonElement} to read from
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public RioFavorite deserialize(JsonElement elm, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jo = (JsonObject) elm;

        final JsonElement id = jo.get("id");
        final JsonElement valid = jo.get("valid");
        final JsonElement name = jo.get("name");

        return new RioFavorite((id == null ? -1 : id.getAsInt()), (valid == null ? false : valid.getAsBoolean()),
                (name == null ? null : name.getAsString()));
    }
}
