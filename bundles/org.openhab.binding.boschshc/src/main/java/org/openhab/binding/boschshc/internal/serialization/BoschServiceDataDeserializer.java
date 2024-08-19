/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.serialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Message;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Utility class for JSON deserialization of device data and triggered scenarios using Google Gson.
 *
 * @author Patrick Gell - Initial contribution
 * @author David Pace - Fixed NPEs and simplified code, added sanity check
 *
 */
@NonNullByDefault
public class BoschServiceDataDeserializer implements JsonDeserializer<BoschSHCServiceState> {

    @Nullable
    @Override
    public BoschSHCServiceState deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement dataTypeElement = jsonObject.get("@type");
        if (dataTypeElement == null) {
            throw new IllegalArgumentException("Received a service state without a @type property: " + jsonObject);
        }

        String dataType = dataTypeElement.getAsString();
        return switch (dataType) {
            case "DeviceServiceData" -> GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(jsonObject, DeviceServiceData.class);
            case "scenarioTriggered" -> GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(jsonObject, Scenario.class);
            case "userDefinedState" -> GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(jsonObject, UserDefinedState.class);
            case "message" -> GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(jsonElement, Message.class);
            default -> new BoschSHCServiceState(dataType);
        };
    }
}
