package org.openhab.binding.boschshc.internal.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

import java.lang.reflect.Type;

public class BoschServiceDataDeserializer implements JsonDeserializer<BoschSHCServiceState> {
    @Override
    public BoschSHCServiceState deserialize(JsonElement jsonElement,
                                            Type type,
                                            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {


        switch (jsonElement.getAsJsonObject().get("@type").getAsString()) {
            case "DeviceServiceData" -> {
                var deviceServiceData = new DeviceServiceData();
                deviceServiceData.deviceId = jsonElement.getAsJsonObject().get("deviceId").getAsString();
                deviceServiceData.state = jsonElement.getAsJsonObject().get("state");
                deviceServiceData.id = jsonElement.getAsJsonObject().get("id").getAsString();
                deviceServiceData.path = jsonElement.getAsJsonObject().get("path").getAsString();
                return deviceServiceData;
            }
            case "scenarioTriggered" -> {
                var scenario = new Scenario();
                scenario.id = jsonElement.getAsJsonObject().get("id").getAsString();
                scenario.name = jsonElement.getAsJsonObject().get("name").getAsString();
                scenario.lastTimeTriggered = jsonElement.getAsJsonObject().get("lastTimeTriggered").getAsString();
                return scenario;
            }
            default -> {
                return new BoschSHCServiceState(jsonElement.getAsJsonObject().get("@type").getAsString());
            }
        }
    }
}
