/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.websocket.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.serializer.ResourceAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.ToNumberPolicy;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * WebSocket message model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record Message(@SerializedName("sID") long sessionId, @SerializedName("msgID") long messageId, Resource resource,
        int version, Action action, @Nullable Integer code, @Nullable JsonArray data) {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceAdapter())
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

    public <T> @Nullable List<T> getDataAsList(Class<T> clazz) {
        var dataJsonArray = data();
        if (dataJsonArray != null) {
            return GSON.fromJson(dataJsonArray, TypeToken.getParameterized(List.class, clazz).getType());
        }

        return null;
    }
}
