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
package org.openhab.binding.dirigera.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.blind.BlindHandler;
import org.openhab.binding.dirigera.mock.CallbackMock;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestBlindHandler} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestBlindHandler {
    @Test
    void testBlinds() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingImpl thing = new ThingImpl(THING_TYPE_BLIND, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        BlindHandler handler = new BlindHandler(thing, BLINDS_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "eadfad54-9d23-4475-92b6-0ee3d6f8b481_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkBlindStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BLIND_CURRENT_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BLIND_TARGET_LEVEL), RefreshType.REFRESH);
        checkBlindStates(callback);
    }

    void checkBlindStates(CallbackMock callback) {
        State decimalState = callback.getState("dirigera:blind:test-device:state");
        assertNotNull(decimalState);
        assertTrue(decimalState instanceof DecimalType);
        assertEquals(0, ((DecimalType) decimalState).intValue(), "Blind State");

        State currentLevelState = callback.getState("dirigera:blind:test-device:current");
        assertNotNull(currentLevelState);
        assertTrue(currentLevelState instanceof PercentType);
        assertEquals(60, ((PercentType) currentLevelState).intValue(), "Current Level");

        State targetLevelState = callback.getState("dirigera:blind:test-device:current");
        assertNotNull(targetLevelState);
        assertTrue(targetLevelState instanceof PercentType);
        assertEquals(60, ((PercentType) targetLevelState).intValue(), "Target Level");

        State batteryState = callback.getState("dirigera:blind:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(92, ((QuantityType<?>) batteryState).intValue(), "Battery level");

        // test ota
        State otaStatus = callback.getState("dirigera:blind:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:blind:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:blind:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");
    }
}
