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
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * {@link TestOccupancySensor} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestOccupancySensor {
    private static String deviceId = "d6ee92fc-682a-4af0-9097-c73ed70b59fd_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_OCCUPANCY_SENSOR;

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
        assertEquals(12, thing.getProperties().size(), "Matter Occupancy Sensor");
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
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_MOTION_DETECTION), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ILLUMINANCE), RefreshType.REFRESH);
        checkEnvironmentSensorStates(callback);
    }

    @Test
    void testCommands() {
        testHandlerCreation();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE), new DecimalType(1));
        String patch = DirigeraAPISimu.patchMap.get("d6ee92fc-682a-4af0-9097-c73ed70b59fd_2");
        assertNotNull(patch);
        assertEquals(
                "{\"attributes\":{\"sensorConfig\":{\"schedule\":{\"offCondition\":{\"time\":\"sunrise\"},\"onCondition\":{\"time\":\"sunset\"}},\"scheduleOn\":true}}}",
                patch, "Schedule Follow Sun");
        DirigeraAPISimu.patchMap.clear();

        DateTimeType fixed = new DateTimeType(
                Instant.now().atZone(ZoneId.of("Europe/Berlin")).withHour(10).withMinute(15));
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START), fixed);
        patch = DirigeraAPISimu.patchMap.get("d6ee92fc-682a-4af0-9097-c73ed70b59fd_2");
        assertNotNull(patch);
        assertEquals(
                "{\"attributes\":{\"sensorConfig\":{\"schedule\":{\"offCondition\":{\"time\":\"07:00\"},\"onCondition\":{\"time\":\"10:15\"}},\"scheduleOn\":true}}}",
                patch, "Schedule Time");
    }

    @Test
    void testLinks() {
        testHandlerCreation();
        handler.getLinks();
        String lightId = "8d89e4f6-cb60-443b-9c68-3094fc15e0e6_1";
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LINK_CANDIDATES), new StringType(lightId));
        String patch = DirigeraAPISimu.patchMap.get(lightId);
        assertEquals("{\"remoteLinks\":[\"d6ee92fc-682a-4af0-9097-c73ed70b59fd_2\"]}", patch, "Schedule Time");
    }

    void checkEnvironmentSensorStates(CallbackMock callback) {
        State motionDetected = callback.getState("dirigera:occupancy-sensor:test-device:motion");
        assertNotNull(motionDetected);
        assertTrue(motionDetected instanceof OnOffType);
        assertEquals(OnOffType.OFF, motionDetected, "Motion Detected");

        State illuminance = callback.getState("dirigera:occupancy-sensor:test-device:illuminance");
        assertNotNull(illuminance);
        assertTrue(illuminance instanceof QuantityType);
        assertTrue(((QuantityType<?>) illuminance).getUnit().equals(Units.LUX));
        assertEquals(12345, ((QuantityType<?>) illuminance).intValue(), "Lux");
    }
}
