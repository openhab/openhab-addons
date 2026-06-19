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
package org.openhab.binding.homeconnectdirect.internal.handler.model;

import static org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils.createGson;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

/**
 * Appliance message model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record ApplianceMessage(OffsetDateTime dateTime, long id, MessageType type, Resource resource, int version,
        long sessionId, long messageId, Action action, @Nullable Integer code, @Nullable JsonArray rawPayload,
        @Nullable List<Value> values, @Nullable List<DeviceDescriptionChange> descriptionChanges) {

    private static final Gson GSON = createGson();

    public <T> @Nullable List<T> getRawPayloadAsList(Class<T> clazz) {
        var dataJsonArray = rawPayload();
        if (dataJsonArray != null) {
            return GSON.fromJson(dataJsonArray, TypeToken.getParameterized(List.class, clazz).getType());
        }

        return null;
    }
}
