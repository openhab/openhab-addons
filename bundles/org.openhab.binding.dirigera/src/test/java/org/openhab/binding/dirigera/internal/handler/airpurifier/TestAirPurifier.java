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
package org.openhab.binding.dirigera.internal.handler.airpurifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.airpurifier.AirPurifierHandler;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.binding.dirigera.internal.mock.HandlerFactoryMock;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestAirPurifier} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestAirPurifier {

    @Test
    void testHandlerCreation() {
        HandlerFactoryMock hfm = new HandlerFactoryMock();
        assertTrue(hfm.supportsThingType(THING_TYPE_AIR_PURIFIER));
        ThingImpl thing = new ThingImpl(THING_TYPE_AIR_PURIFIER, "test-device");
        ThingHandler th = hfm.createHandler(thing);
        assertNotNull(th);
        assertTrue(th instanceof AirPurifierHandler);
    }

    @Test
    void testInitialization() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingImpl thing = new ThingImpl(THING_TYPE_AIR_PURIFIER, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        AirPurifierHandler handler = new AirPurifierHandler(thing, AIR_PURIFIER_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "a8319695-0729-428c-9465-aadc0b738995");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkAirPurifierStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CHILD_LOCK), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_DISABLE_STATUS_LIGHT), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FILTER_ALARM), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FILTER_ELAPSED), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FILTER_LIFETIME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_MOTOR_RUNTIME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PURIFIER_FAN_MODE), RefreshType.REFRESH);
        checkAirPurifierStates(callback);
    }

    @Test
    void testCommands() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingImpl thing = new ThingImpl(THING_TYPE_AIR_PURIFIER, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        AirPurifierHandler handler = new AirPurifierHandler(thing, AIR_PURIFIER_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        String deviceId = "a8319695-0729-428c-9465-aadc0b738995";
        Map<String, Object> config = new HashMap<>();
        config.put("id", deviceId);
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        // DirigeraAPISimu api = (DirigeraAPISimu) ((DirigeraHandler) hubBridge.getHandler()).api();
        handler.handleCommand(new ChannelUID(thing.getUID(), "fan-mode"), new DecimalType(4));
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"fanMode\":\"on\"}", patch, "Fan Mode on");
    }

    void checkAirPurifierStates(CallbackMock callback) {
        State otaStatus = callback.getState("dirigera:air-purifier:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:air-purifier:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:air-purifier:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");

        State disableLightState = callback.getState("dirigera:air-purifier:test-device:disable-light");
        assertNotNull(disableLightState);
        assertTrue(disableLightState instanceof OnOffType);
        assertTrue(OnOffType.ON.equals((disableLightState)), "Status Light Disabled");
        State childlockState = callback.getState("dirigera:air-purifier:test-device:child-lock");
        assertNotNull(childlockState);
        assertTrue(childlockState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((childlockState)), "Child Lock enabled");

        State filterAlarmState = callback.getState("dirigera:air-purifier:test-device:filter-alarm");
        assertNotNull(filterAlarmState);
        assertTrue(filterAlarmState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((filterAlarmState)), "Filter Alarm");
        State filterElapsedState = callback.getState("dirigera:air-purifier:test-device:filter-elapsed");
        assertNotNull(filterElapsedState);
        assertTrue(filterElapsedState instanceof QuantityType);
        assertEquals(Units.MINUTE, ((QuantityType<?>) filterElapsedState).getUnit());
        assertEquals(193540, ((QuantityType<?>) filterElapsedState).intValue());
        State filterLifetimedState = callback.getState("dirigera:air-purifier:test-device:filter-lifetime");
        assertNotNull(filterLifetimedState);
        assertTrue(filterLifetimedState instanceof QuantityType);
        assertEquals(Units.MINUTE, ((QuantityType<?>) filterLifetimedState).getUnit());
        assertEquals(259200, ((QuantityType<?>) filterLifetimedState).intValue());

        State motorRuntimeState = callback.getState("dirigera:air-purifier:test-device:motor-runtime");
        assertNotNull(motorRuntimeState);
        assertTrue(motorRuntimeState instanceof QuantityType);
        assertEquals(Units.MINUTE, ((QuantityType<?>) motorRuntimeState).getUnit());
        assertEquals(472283, ((QuantityType<?>) motorRuntimeState).intValue());
        State fanModeState = callback.getState("dirigera:air-purifier:test-device:fan-mode");
        assertNotNull(fanModeState);
        assertTrue(fanModeState instanceof DecimalType);
        assertEquals(0, ((DecimalType) fanModeState).intValue());
    }
}
