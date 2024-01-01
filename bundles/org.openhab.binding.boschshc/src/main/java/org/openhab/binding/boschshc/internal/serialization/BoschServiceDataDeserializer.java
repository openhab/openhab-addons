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
 *
 */
@NonNullByDefault
public class BoschServiceDataDeserializer implements JsonDeserializer<BoschSHCServiceState> {

    @Nullable
    @Override
    public BoschSHCServiceState deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement dataType = jsonObject.get("@type");
        switch (dataType.getAsString()) {
            case "DeviceServiceData" -> {
                var deviceServiceData = new DeviceServiceData();
                deviceServiceData.deviceId = jsonObject.get("deviceId").getAsString();
                deviceServiceData.state = jsonObject.get("state");
                deviceServiceData.id = jsonObject.get("id").getAsString();
                deviceServiceData.path = jsonObject.get("path").getAsString();
                return deviceServiceData;
            }
            case "scenarioTriggered" -> {
                var scenario = new Scenario();
                scenario.id = jsonObject.get("id").getAsString();
                scenario.name = jsonObject.get("name").getAsString();
                scenario.lastTimeTriggered = jsonObject.get("lastTimeTriggered").getAsString();
                return scenario;
            }
            case "userDefinedState" -> {
                var state = new UserDefinedState();
                state.setId(jsonObject.get("id").getAsString());
                state.setName(jsonObject.get("name").getAsString());
                state.setState(jsonObject.get("state").getAsBoolean());
                return state;
            }
            default -> {
                return new BoschSHCServiceState(dataType.getAsString());
            }
        }
    }
}
