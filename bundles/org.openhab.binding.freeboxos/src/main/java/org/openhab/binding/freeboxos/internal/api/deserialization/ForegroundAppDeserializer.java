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
package org.openhab.binding.freeboxos.internal.api.deserialization;

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager.ForegroundApp;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager.PlayerContext;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager.TvContext;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer to handle {@link ForegroundApp} object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ForegroundAppDeserializer implements JsonDeserializer<ForegroundApp> {

    @Override
    public @NonNull ForegroundApp deserialize(@NonNullByDefault({}) JsonElement json,
            @NonNullByDefault({}) Type typeOfT, @NonNullByDefault({}) JsonDeserializationContext context)
            throws JsonParseException {

        Object obj;

        String _package = json.getAsJsonObject().get("package").getAsString();
        JsonElement jsonElement2 = json.getAsJsonObject().get("context");
        if (jsonElement2 == null) {
            obj = null;
        } else if ("fr.freebox.tv".equals(_package)) {
            obj = context.deserialize(jsonElement2, TvContext.class);
        } else {
            obj = context.deserialize(jsonElement2, PlayerContext.class);
        }

        int packageId = json.getAsJsonObject().get("package_id").getAsInt();
        String curlUrl = json.getAsJsonObject().get("cur_url").getAsString();
        Objects.requireNonNull(_package);
        return new ForegroundApp(packageId, curlUrl, obj, _package);
    }
}
