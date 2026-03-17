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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * {@link TestEnvironmentSensor} Tests device handler creation, initializing and commands for environment sensors
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestEnvironmentSensor {
    private static String deviceIdTimmerflotte = "94f3d9d7-95ee-496d-9b83-2d5de9a7c2c1_1";
    private static String deviceIdAlpstuga = "0d9da61c-2c59-4d32-90a4-b165b8cc4cbe_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_ENVIRONMENT_SENSOR;

    BaseMatterHandler getHandler(String deviceId) {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof BaseMatterHandler);
        return (BaseMatterHandler) factoryHandler;
    }

    public static Stream<Arguments> testHandlerCreation() {
        return Stream.of( //
                Arguments.of(deviceIdTimmerflotte, 10, 7), //
                Arguments.of(deviceIdAlpstuga, 14, 9) //
        );
    }

    @ParameterizedTest
    @MethodSource
    void testHandlerCreation(String deviceId, int expectedProperties, int expectedChannels) {
        MatterSensor handler = (MatterSensor) getHandler(deviceId);
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        assertEquals(expectedProperties, thing.getProperties().size(), "Mismatched number of properties");
        assertEquals(expectedChannels, thing.getChannels().size(), "Mismatched number of channels");
    }

    public static Stream<Arguments> testInitialization() {
        return Stream.of( //
                Arguments.of(deviceIdTimmerflotte, Map.of(//
                        "dirigera:environment-sensor:test-device:battery-level", QuantityType.valueOf("95 %"), //
                        "dirigera:environment-sensor:test-device:humidity", QuantityType.valueOf("46 %"), //
                        "dirigera:environment-sensor:test-device:temperature", QuantityType.valueOf("22.22 °C") //
                )), //
                Arguments.of(deviceIdAlpstuga, Map.of(//
                        "dirigera:environment-sensor:test-device:particulate-matter", QuantityType.valueOf("4 µg/m³"), //
                        "dirigera:environment-sensor:test-device:display-switch", OnOffType.ON, //
                        "dirigera:environment-sensor:test-device:co2", QuantityType.valueOf("766 ppm"), //
                        "dirigera:environment-sensor:test-device:humidity", QuantityType.valueOf("52 %"), //
                        "dirigera:environment-sensor:test-device:temperature", QuantityType.valueOf("19.82 °C") //
                )) //
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInitialization(String deviceId, Map<String, State> expectedStates) {
        MatterSensor handler = (MatterSensor) getHandler(deviceId);
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        expectedStates.forEach((channel, expectedState) -> {
            State state = callback.getState(channel);
            assertNotNull(state, "State for channel " + channel + " is null");
            assertTrue(state.getClass().equals(expectedState.getClass()),
                    "Mismatched state type for channel " + channel);
            if (state instanceof Number) {
                assertEquals(((Number) expectedState).doubleValue(), ((Number) state).doubleValue(), 0.01,
                        "Mismatched numeric state for channel " + channel);
            } else {
                assertEquals(expectedState.toFullString(), state.toFullString(),
                        "Mismatched state for channel " + channel);
            }
        });
    }

    public static Stream<Arguments> testCommands() {
        return Stream.of( //
                Arguments.of(deviceIdTimmerflotte, Map.of(//
                        CHANNEL_CUSTOM_NAME, Map.of(new StringType("HollaDieWaldfee"),
                                "{\"attributes\":{\"customName\":\"HollaDieWaldfee\"}}") //
                )), //
                Arguments.of(deviceIdAlpstuga, Map.of(//
                        CHANNEL_CUSTOM_NAME,
                        Map.of(new StringType("HollaDieWaldfee"),
                                "{\"attributes\":{\"customName\":\"HollaDieWaldfee\"}}"), //
                        CHANNEL_DISPLAY_SWITCH, Map.of(OnOffType.OFF, "{\"attributes\":{\"isOn\":false}}") //
                )) //
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCommands(String deviceId, Map<String, Map<Command, String>> expectedCommands) {
        MatterSensor handler = (MatterSensor) getHandler(deviceId);
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        expectedCommands.forEach((channel, commandMap) -> {
            api.clear();
            commandMap.forEach((command, expectedPatch) -> {
                handler.handleCommand(new ChannelUID(thing.getUID(), channel), command);
                String patch = api.getPatch(deviceId);
                assertNotNull(patch);
                assertEquals(expectedPatch, patch,
                        "Mismatched patch for channel " + channel + " and command " + command);
            });
        });
    }
}
