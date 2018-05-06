/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * A {@link JsonSerializer} and {@link JsonDeserializer} for the {@link RioPreset}. Simply writes/reads the ID and
 * name to elements called "id", "valid", "name", "bank" and "bankPreset" values.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioPresetSerializer implements JsonSerializer<RioPreset>, JsonDeserializer<RioPreset> {

    /**
     * Overridden to simply write out the id/valid/name/bank/bankPreset elements from the {@link RioPreset}
     *
     * @param preset the {@link RioPreset} to write out
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public JsonElement serialize(RioPreset preset, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("id", preset.getId());
        root.addProperty("valid", preset.isValid());
        root.addProperty("name", preset.getName());
        root.addProperty("bank", preset.getBank());
        root.addProperty("bankPreset", preset.getBankPreset());

        return root;
    }

    /**
     * Overridden to simply read the id/valid/name elements and create a {@link RioPreset}. Please note that
     * the bank/bankPreset are calculated fields from the ID and do not need to be read.
     *
     * @param elm the {@link JsonElement} to read from
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public RioPreset deserialize(JsonElement elm, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jo = (JsonObject) elm;
        final JsonElement id = jo.get("id");
        final JsonElement valid = jo.get("valid");
        final JsonElement name = jo.get("name");

        return new RioPreset((id == null ? -1 : id.getAsInt()), (valid == null ? false : valid.getAsBoolean()),
                (name == null ? null : name.getAsString()));
    }
}
