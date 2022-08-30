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
package org.openhab.binding.hdpowerview.internal.api;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * An class that builds a Gson processor customised either for Hub Generation 1/2 or Generation 3 payloads.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GsonCustomBuilder {

    private static class ShadeDataImpl1Deserializer implements JsonDeserializer<ShadeData> {
        @Override
        public @Nullable ShadeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ShadeDataV1.class);
        }
    }

    private static class ShadePositionImpl1Deserializer implements JsonDeserializer<ShadePosition> {
        @Override
        public @Nullable ShadePosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ShadePositionV1.class);
        }
    }

    private static class ShadeDataImpl3Deserializer implements JsonDeserializer<ShadeData> {
        @Override
        public @Nullable ShadeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ShadeDataV3.class);
        }
    }

    private static class ShadePositionImpl3Deserializer implements JsonDeserializer<ShadePosition> {
        @Override
        public @Nullable ShadePosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return context.deserialize(jsonObject, ShadePositionV3.class);
        }
    }

    public static Gson getGen1() {
        return new GsonBuilder().registerTypeAdapter(ShadeData.class, new ShadeDataImpl1Deserializer())
                .registerTypeAdapter(ShadePosition.class, new ShadePositionImpl1Deserializer()).create();
    }

    public static Gson getGen3() {
        return new GsonBuilder().registerTypeAdapter(ShadeData.class, new ShadeDataImpl3Deserializer())
                .registerTypeAdapter(ShadePosition.class, new ShadePositionImpl3Deserializer()).create();
    }
}
