package org.openhab.binding.boschshc.internal.serialization;

import java.lang.reflect.Type;

import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

import com.google.gson.*;

public class BoschServiceDataDeserializer implements JsonDeserializer<BoschSHCServiceState> {
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
            default -> {
                return new BoschSHCServiceState(dataType.getAsString());
            }
        }
    }
}
