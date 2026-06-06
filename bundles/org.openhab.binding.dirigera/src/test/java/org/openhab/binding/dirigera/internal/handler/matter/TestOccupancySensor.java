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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.binding.dirigera.internal.mock.HandlerFactoryMock;
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
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestOccupancySensor} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestOccupancySensor {
    private static String lightSensorId = "d6ee92fc-682a-4af0-9097-c73ed70b59fd_1";
    private static String motionSensorId = "d6ee92fc-682a-4af0-9097-c73ed70b59fd_2";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_OCCUPANCY_SENSOR;

    BaseMatterHandler getHandler() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, lightSensorId);
        assertTrue(factoryHandler instanceof BaseMatterHandler);
        return (BaseMatterHandler) factoryHandler;
    }

    @Test
    void testHandlerCreation() {
        ThingHandler factoryHandler = getHandler();
        assertTrue(factoryHandler instanceof MatterSensor);
        MatterSensor handler = (MatterSensor) factoryHandler;
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);

        callback.waitForOnline();
        assertEquals(12, thing.getProperties().size(), "Matter Occupancy Sensor");
    }

    @Test
    void testInitialization() {
        BaseMatterHandler handler = getHandler();
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
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
        BaseMatterHandler handler = getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE), new DecimalType(1));
        String patch = api.getPatch(motionSensorId);
        assertNotNull(patch);
        assertEquals(
                "{\"attributes\":{\"sensorConfig\":{\"schedule\":{\"offCondition\":{\"time\":\"sunrise\"},\"onCondition\":{\"time\":\"sunset\"}},\"scheduleOn\":true}}}",
                patch, "Schedule Follow Sun");
        api.clear();

        ZonedDateTime requestedStartTime = Instant.now().truncatedTo(ChronoUnit.MINUTES)
                .atZone(HandlerFactoryMock.timeZoneProvider.getTimeZone()).withHour(10).withMinute(15);
        DateTimeType startTime = new DateTimeType(requestedStartTime);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START), startTime);
        patch = api.getPatch(motionSensorId);
        assertNotNull(patch);
        assertEquals(
                "{\"attributes\":{\"sensorConfig\":{\"schedule\":{\"offCondition\":{\"time\":\"07:00\"},\"onCondition\":{\"time\":\"10:15\"}},\"scheduleOn\":true}}}",
                patch, "Schedule Time");
    }

    @Test
    void testLinks() {
        BaseMatterHandler handler = getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        handler.getLinks();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LINK_CANDIDATES), new StringType(lightSensorId));
        String patch = api.getPatch(lightSensorId);
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
        assertEquals(17, ((QuantityType<?>) illuminance).intValue(), "Lux");
    }
}
