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
package org.openhab.binding.dirigera.internal.matter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.matter.MatterSensor;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestEnvironmentSensor} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestEnvironmentSensor {
    private static String deviceId = "94f3d9d7-95ee-496d-9b83-2d5de9a7c2c1_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_SENSOR;

    private static MatterSensor handler = mock(MatterSensor.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof MatterSensor);
        handler = (MatterSensor) factoryHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
        System.out.println("thing channels: " + thing.getChannels().size());
        thing.getChannels().forEach(channel -> {
            System.out.println(" Channel: " + channel.getUID() + " type: " + channel.getChannelTypeUID());
        });
        System.out.println("Properties: " + thing.getProperties().size());
        thing.getProperties().forEach((key, value) -> {
            System.out.println(" Property: " + key + " value: " + value);
        });
        callback.waitForOnline();
    }

    @Test
    void testInitialization() {
        testHandlerCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        checkEnvironmentSensorStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_TEMPERATURE), RefreshType.REFRESH);
        checkEnvironmentSensorStates(callback);
    }

    @Test
    void testCommands() {
        testHandlerCreation();
        String command = "HollaDieWaldfee";
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CUSTOM_NAME), new StringType(command));
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"customName\":\"" + command + "\"}}", patch, "Fan Mode on");

        // handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FAN_SPEED), new PercentType(23));
        // patch = DirigeraAPISimu.patchMap.get(deviceId);
        // assertNotNull(patch);
        // assertEquals("{\"attributes\":{\"motorState\":12}}", patch, "Fan Speed");
        //
        // handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FAN_SPEED), new PercentType(100));
        // patch = DirigeraAPISimu.patchMap.get(deviceId);
        // assertNotNull(patch);
        // assertEquals("{\"attributes\":{\"motorState\":50}}", patch, "Fan Speed");
    }

    void testDump() {
        testHandlerCreation();
        assertEquals("unit-test", handler.getToken());
    }

    void checkEnvironmentSensorStates(CallbackMock callback) {
        State batteryLevel = callback.getState("dirigera:sensor:test-device:battery-level");
        assertNotNull(batteryLevel);
        assertTrue(batteryLevel instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryLevel).getUnit().equals(Units.PERCENT));
        assertEquals(95, ((QuantityType<?>) batteryLevel).intValue(), "Battery Level");

        State temperature = callback.getState("dirigera:sensor:test-device:temperature");
        assertNotNull(temperature);
        assertTrue(temperature instanceof QuantityType);
        assertTrue(((QuantityType<?>) temperature).getUnit().equals(SIUnits.CELSIUS));
        assertEquals(22.22, ((QuantityType<?>) temperature).doubleValue(), 0.001, "Temperature");

        // State otaState = callback.getState("dirigera:air-purifier:test-device:ota-state");
        // assertNotNull(otaState);
        // assertTrue(otaState instanceof DecimalType);
        // assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        // State otaProgess = callback.getState("dirigera:air-purifier:test-device:ota-progress");
        // assertNotNull(otaProgess);
        // assertTrue(otaProgess instanceof QuantityType);
        // assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        // assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");
        //
        // State disableLightState = callback.getState("dirigera:air-purifier:test-device:disable-status-light");
        // assertNotNull(disableLightState);
        // assertTrue(disableLightState instanceof OnOffType);
        // assertTrue(OnOffType.ON.equals((disableLightState)), "Status Light Disabled");
        // State childlockState = callback.getState("dirigera:air-purifier:test-device:child-lock");
        // assertNotNull(childlockState);
        // assertTrue(childlockState instanceof OnOffType);
        // assertTrue(OnOffType.OFF.equals((childlockState)), "Child Lock enabled");
        //
        // State filterAlarmState = callback.getState("dirigera:air-purifier:test-device:filter-alarm");
        // assertNotNull(filterAlarmState);
        // assertTrue(filterAlarmState instanceof OnOffType);
        // assertTrue(OnOffType.OFF.equals((filterAlarmState)), "Filter Alarm");
        // State filterElapsedState = callback.getState("dirigera:air-purifier:test-device:filter-elapsed");
        // assertNotNull(filterElapsedState);
        // assertTrue(filterElapsedState instanceof QuantityType);
        // assertEquals(Units.MINUTE, ((QuantityType<?>) filterElapsedState).getUnit());
        // assertEquals(193540, ((QuantityType<?>) filterElapsedState).intValue());
        // State filterLifetimedState = callback.getState("dirigera:air-purifier:test-device:filter-lifetime");
        // assertNotNull(filterLifetimedState);
        // assertTrue(filterLifetimedState instanceof QuantityType);
        // assertEquals(Units.MINUTE, ((QuantityType<?>) filterLifetimedState).getUnit());
        // assertEquals(259200, ((QuantityType<?>) filterLifetimedState).intValue());
        //
        // State motorRuntimeState = callback.getState("dirigera:air-purifier:test-device:fan-runtime");
        // assertNotNull(motorRuntimeState);
        // assertTrue(motorRuntimeState instanceof QuantityType);
        // assertEquals(Units.MINUTE, ((QuantityType<?>) motorRuntimeState).getUnit());
        // assertEquals(472283, ((QuantityType<?>) motorRuntimeState).intValue());
        // State fanSpeedState = callback.getState("dirigera:air-purifier:test-device:fan-speed");
        // assertNotNull(fanSpeedState);
        // assertTrue(fanSpeedState instanceof PercentType);
        // assertEquals(20, ((PercentType) fanSpeedState).intValue());
        // State fanModeState = callback.getState("dirigera:air-purifier:test-device:fan-mode");
        // assertNotNull(fanModeState);
        // assertTrue(fanModeState instanceof DecimalType);
        // assertEquals(0, ((DecimalType) fanModeState).intValue());
    }
}
