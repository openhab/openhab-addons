package org.openhab.binding.boschshc.internal.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;

public class BoschServiceDataDeserializerTest {

    @Test
    public void deserializationOfLongPollingResult() {
        var resultJson = """
                {
                    "result": [
                        {
                            "@type": "scenarioTriggered",
                            "name": "MyTriggeredScenario",
                            "id": "509bd737-eed0-40b7-8caa-e8686a714399",
                            "lastTimeTriggered": "1689417526720"
                        },
                        {
                            "path":"/devices/hdm:HomeMaticIP:3014F711A0001916D859A8A9/services/PowerSwitch",
                            "@type":"DeviceServiceData",
                            "id":"PowerSwitch",
                            "state":{
                                "@type":"powerSwitchState",
                                "switchState":"ON"
                            },
                            "deviceId":"hdm:HomeMaticIP:3014F711A0001916D859A8A9"
                        }
                    ],
                    "jsonrpc": "2.0"
                }
                """;

        var longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(resultJson, LongPollResult.class);
        assertNotNull(longPollResult);
        assertEquals(2, longPollResult.result.size());

        var resultClasses = new ArrayList<>(longPollResult.result.stream().map(e -> e.getClass().getName()).toList());
        for (var className : List.of(DeviceServiceData.class.getName(), Scenario.class.getName())) {
            resultClasses.remove(className);
        }
        assertEquals(0, resultClasses.size());
    }
}
