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

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.player.PlayerContext;
import org.openhab.binding.freeboxos.internal.api.player.PlayerStatusForegroundApp;
import org.openhab.binding.freeboxos.internal.api.player.TvContext;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer to handle {@link PlayerStatusForegroundApp} object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class ForegroundAppDeserializer implements JsonDeserializer<PlayerStatusForegroundApp> {

    @Override
    public @Nullable PlayerStatusForegroundApp deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        // GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().registerTypeAdapter(List.class,
        // new ArrayListDeserializer());
        // Gson create = gsonBuilder.create();

        Object obj;

        String _package = json.getAsJsonObject().get("package").getAsString();
        JsonElement jsonElement2 = json.getAsJsonObject().get("context");
        if (jsonElement2 == null) {
            obj = null;
        } else if ("fr.freebox.tv".equals(_package)) {
            // obj = create.fromJson(new JsonTreeReader(jsonElement2), TvContext.class);
            obj = context.deserialize(jsonElement2, TvContext.class);
        } else {
            // obj = create.fromJson(new JsonTreeReader(jsonElement2), PlayerContext.class);
            obj = context.deserialize(jsonElement2, PlayerContext.class);
        }

        int packageId = json.getAsJsonObject().get("package_id").getAsInt();
        String curlUrl = json.getAsJsonObject().get("cur_url").getAsString();
        Objects.requireNonNull(_package);
        return new PlayerStatusForegroundApp(packageId, curlUrl, obj, _package);
    }
}
