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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Unit tests for {@link BoschServiceDataDeserializer}.
 *
 * @author Patrick Gell - Initial contribution
 * @author David Pace - Added tests for all supported service data classes
 *
 */
@NonNullByDefault
class BoschServiceDataDeserializerTest {

    @Test
    void deserializationOfLongPollingResult() {
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
        // note: when using assertThat() to check that the value is non-null, we get compiler warnings.
        assertNotNull(longPollResult);
        List<BoschSHCServiceState> results = longPollResult.result;
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(2));

        var resultClasses = new HashSet<>(results.stream().map(e -> e.getClass().getName()).toList());
        assertThat(resultClasses, hasSize(2));
        assertThat(resultClasses, containsInAnyOrder(DeviceServiceData.class.getName(), Scenario.class.getName()));
    }

    @Test
    void testDeserializeDeletedDeviceServiceData() {
        var resultJson = """
                {
                    "result": [
                        {
                            "@type": "DeviceServiceData",
                            "deleted": true,
                            "id": "CommunicationQuality",
                            "deviceId": "hdm:ZigBee:30fb10fffe46d732"
                        }
                    ],
                    "jsonrpc":"2.0"
                }
                """;

        var longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(resultJson, LongPollResult.class);
        // note: when using assertThat() to check that the value is non-null, we get compiler warnings.
        assertNotNull(longPollResult);
        List<BoschSHCServiceState> results = longPollResult.result;
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(1));

        DeviceServiceData deviceServiceData = (DeviceServiceData) longPollResult.result.get(0);
        assertThat(deviceServiceData.type, is("DeviceServiceData"));
        assertThat(deviceServiceData.id, is("CommunicationQuality"));
        assertThat(deviceServiceData.deviceId, is("hdm:ZigBee:30fb10fffe46d732"));
    }

    @Test
    void testDeserializeScenarioTriggered() {
        String resultJson = """
                {
                    "result": [
                        {
                            "@type": "scenarioTriggered",
                            "name": "My Scenario",
                            "id": "509bd737-eed0-40b7-8caa-e8686a714399",
                            "lastTimeTriggered": "1693758693032"
                        }
                    ],
                    "jsonrpc": "2.0"
                }
                """;

        var longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(resultJson, LongPollResult.class);
        // note: when using assertThat() to check that the value is non-null, we get compiler warnings.
        assertNotNull(longPollResult);
        List<BoschSHCServiceState> results = longPollResult.result;
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(1));

        Scenario scenario = (Scenario) longPollResult.result.get(0);
        assertThat(scenario.type, is("scenarioTriggered"));
        assertThat(scenario.name, is("My Scenario"));
        assertThat(scenario.id, is("509bd737-eed0-40b7-8caa-e8686a714399"));
        assertThat(scenario.lastTimeTriggered, is("1693758693032"));
    }

    @Test
    void testDeserializeUserDefinedState() {
        String resultJson = """
                {
                    "result": [
                        {
                            "@type": "userDefinedState",
                            "deleted": false,
                            "name": "Test State",
                            "id": "3d8023d6-69ca-4e79-89dd-7090295cefbf",
                            "state": true
                        }
                    ],
                    "jsonrpc": "2.0"
                }
                """;

        var longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(resultJson, LongPollResult.class);
        // note: when using assertThat() to check that the value is non-null, we get compiler warnings.
        assertNotNull(longPollResult);
        List<BoschSHCServiceState> results = longPollResult.result;
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(1));

        UserDefinedState userDefinedState = (UserDefinedState) longPollResult.result.get(0);
        assertThat(userDefinedState.type, is("userDefinedState"));
        assertThat(userDefinedState.getName(), is("Test State"));
        assertThat(userDefinedState.getId(), is("3d8023d6-69ca-4e79-89dd-7090295cefbf"));
        assertThat(userDefinedState.isState(), is(true));
    }
}
