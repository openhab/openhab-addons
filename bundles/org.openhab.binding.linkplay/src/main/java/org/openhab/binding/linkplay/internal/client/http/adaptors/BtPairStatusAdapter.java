/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http.adaptors;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linkplay.internal.client.http.dto.BTPairStatus;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Gson adapter to map integer result field to {@link BTPairStatus.Result} enum.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class BtPairStatusAdapter implements JsonDeserializer<BTPairStatus> {

    @Override
    public @Nullable BTPairStatus deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json == null || !json.isJsonObject()) {
            throw new JsonParseException("Expected JSON object for BTPairStatus");
        }
        JsonObject obj = json.getAsJsonObject();
        BTPairStatus dto = new BTPairStatus();
        JsonElement resultEl = obj.get("result");
        if (resultEl != null && resultEl.isJsonPrimitive() && resultEl.getAsJsonPrimitive().isNumber()) {
            int code = resultEl.getAsInt();
            dto.result = BTPairStatus.Result.fromCode(code);
        } else {
            dto.result = BTPairStatus.Result.NOT_PAIRED;
        }
        return dto;
    }
}
