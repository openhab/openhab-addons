/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.sensor;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestAirQualityDevice} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestAirQualityDevice {

    @Test
    void testAirQualityDeviceWithSimuBridge() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        testAirQuality(hubBridge);
    }

    void testAirQuality(Bridge hubBridge) {
        ThingImpl thing = new ThingImpl(THING_TYPE_AIR_QUALITY, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        AirQualityHandler handler = new AirQualityHandler(thing, AIR_QUALITY_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "f80cac12-65a4-47b4-9f68-a0456a349a43_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkAirQualityStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_TEMPERATURE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_HUMIDITY), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_PARTICULATE_MATTER), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VOC_INDEX), RefreshType.REFRESH);
        checkAirQualityStates(callback);
    }

    void checkAirQualityStates(CallbackMock callback) {
        State otaStatus = callback.getState("dirigera:air-quality:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:air-quality:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:air-quality:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");

        State temperatureState = callback.getState("dirigera:air-quality:test-device:temperature");
        assertNotNull(temperatureState);
        assertTrue(temperatureState instanceof QuantityType);
        assertTrue(((QuantityType<?>) temperatureState).getUnit().equals(SIUnits.CELSIUS));
        assertEquals(20, ((QuantityType<?>) temperatureState).intValue(), "Temperature");

        State humidityState = callback.getState("dirigera:air-quality:test-device:humidity");
        assertNotNull(humidityState);
        assertTrue(humidityState instanceof QuantityType);
        assertTrue(((QuantityType<?>) humidityState).getUnit().equals(Units.PERCENT));
        assertEquals(76, ((QuantityType<?>) humidityState).intValue(), "Hunidity");
        State ppmState = callback.getState("dirigera:air-quality:test-device:particulate-matter");
        assertNotNull(ppmState);
        assertTrue(ppmState instanceof QuantityType);
        assertTrue(((QuantityType<?>) ppmState).getUnit().equals(Units.MICROGRAM_PER_CUBICMETRE));
        assertEquals(11, ((QuantityType<?>) ppmState).intValue(), "ppm");
        State vocState = callback.getState("dirigera:air-quality:test-device:voc-index");
        assertNotNull(vocState);
        assertTrue(vocState instanceof DecimalType);
        assertEquals(100, ((DecimalType) vocState).intValue(), "VOC Index");
    }
}
