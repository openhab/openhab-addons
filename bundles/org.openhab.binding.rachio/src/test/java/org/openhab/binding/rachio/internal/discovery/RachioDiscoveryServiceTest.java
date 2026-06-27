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
package org.openhab.binding.rachio.internal.discovery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_BASE_STATION_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_DEV_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_FLEX_SCHEDULE_RULE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_SCHEDULE_RULE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_VALVE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_VALVE_PROGRAM_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_BASE_STATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_FLEX_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE_PROGRAM;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudScheduleRule;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Tests Rachio schedule discovery result construction.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioDiscoveryServiceTest {
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_CLOUD, "bridge");

    @Test
    void scheduleDiscoveryResultContainsStableScheduleIdentity() {
        RachioDevice device = device();
        RachioCloudScheduleRule scheduleRule = scheduleRule("schedule-id", "Morning", "FIXED");

        DiscoveryResult result = Objects
                .requireNonNull(RachioDiscoveryService.buildScheduleDiscoveryResult(BRIDGE_UID, device, scheduleRule));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_SCHEDULE, BRIDGE_UID, "schedule-id")));
        assertThat(result.getBridgeUID(), is(BRIDGE_UID));
        assertThat(result.getRepresentationProperty(), is(PROPERTY_SCHEDULE_RULE_ID));
        assertThat(result.getProperties().get(PROPERTY_SCHEDULE_RULE_ID), is("schedule-id"));
        assertThat(result.getProperties().get(PROPERTY_DEV_ID), is("device-id"));
    }

    @Test
    void flexScheduleDiscoveryResultContainsStableFlexScheduleIdentity() {
        RachioDevice device = device();
        RachioCloudScheduleRule scheduleRule = scheduleRule("flex-id", "Flex", "FLEX");

        DiscoveryResult result = Objects.requireNonNull(
                RachioDiscoveryService.buildFlexScheduleDiscoveryResult(BRIDGE_UID, device, scheduleRule));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_FLEX_SCHEDULE, BRIDGE_UID, "flex-id")));
        assertThat(result.getThingUID().getAsString(), is("rachio:flex-schedule:bridge:flex-id"));
        assertThat(result.getThingUID().getAsString(), not(is("rachio:flexschedule:bridge:flex-id")));
        assertThat(result.getBridgeUID(), is(BRIDGE_UID));
        assertThat(result.getRepresentationProperty(), is(PROPERTY_FLEX_SCHEDULE_RULE_ID));
        assertThat(result.getProperties().get(PROPERTY_FLEX_SCHEDULE_RULE_ID), is("flex-id"));
        assertThat(result.getProperties().get(PROPERTY_DEV_ID), is("device-id"));
        assertThat(result.getLabel(), is("Controller: Flex"));
    }

    @Test
    void scheduleDiscoverySkipsBlankScheduleId() {
        assertThat(RachioDiscoveryService.buildScheduleDiscoveryResult(BRIDGE_UID, device(),
                scheduleRule("", "Name", "FIXED")), nullValue());
    }

    @Test
    void baseStationDiscoveryResultContainsStableBaseStationIdentity() {
        RachioBaseStation baseStation = baseStation();

        DiscoveryResult result = Objects
                .requireNonNull(RachioDiscoveryService.buildBaseStationDiscoveryResult(BRIDGE_UID, baseStation));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_BASE_STATION, BRIDGE_UID, "base-station-id")));
        assertThat(result.getBridgeUID(), is(BRIDGE_UID));
        assertThat(result.getRepresentationProperty(), is(PROPERTY_BASE_STATION_ID));
        assertThat(result.getProperties().get(PROPERTY_BASE_STATION_ID), is("base-station-id"));
    }

    @Test
    void valveDiscoveryResultContainsValveAndBaseStationIdentity() {
        RachioBaseStation baseStation = baseStation();
        RachioValve valve = valve();

        DiscoveryResult result = Objects
                .requireNonNull(RachioDiscoveryService.buildValveDiscoveryResult(BRIDGE_UID, baseStation, valve));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_VALVE, BRIDGE_UID, "valve-id")));
        assertThat(result.getBridgeUID(), is(BRIDGE_UID));
        assertThat(result.getRepresentationProperty(), is(PROPERTY_VALVE_ID));
        assertThat(result.getProperties().get(PROPERTY_VALVE_ID), is("valve-id"));
        assertThat(result.getProperties().get(PROPERTY_BASE_STATION_ID), is("base-station-id"));
    }

    @Test
    void valveProgramDiscoveryResultContainsProgramValveAndBaseStationIdentity() {
        RachioBaseStation baseStation = baseStation();
        RachioValveProgram program = valveProgram();

        DiscoveryResult result = Objects.requireNonNull(
                RachioDiscoveryService.buildValveProgramDiscoveryResult(BRIDGE_UID, baseStation, program));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_VALVE_PROGRAM, BRIDGE_UID, "program-id")));
        assertThat(result.getBridgeUID(), is(BRIDGE_UID));
        assertThat(result.getRepresentationProperty(), is(PROPERTY_VALVE_PROGRAM_ID));
        assertThat(result.getProperties().get(PROPERTY_VALVE_PROGRAM_ID), is("program-id"));
        assertThat(result.getProperties().get(PROPERTY_VALVE_ID), is("valve-id"));
        assertThat(result.getProperties().get(PROPERTY_BASE_STATION_ID), is("base-station-id"));
    }

    @Test
    void discoveryServiceRegistersAndUnregistersWithBridgeHandler() {
        RachioDiscoveryService service = new RachioDiscoveryService();
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);

        service.setThingHandler(bridgeHandler);
        service.setThingHandler(null);

        verify(bridgeHandler).registerDiscoveryService(service);
        verify(bridgeHandler).unregisterDiscoveryService(service);
    }

    private RachioDevice device() {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "device-id";
        cloudDevice.name = "Controller";
        cloudDevice.macAddress = "ABCDEF123456";
        return new RachioDevice(cloudDevice);
    }

    private RachioCloudScheduleRule scheduleRule(String id, String name, String type) {
        RachioCloudScheduleRule scheduleRule = new RachioCloudScheduleRule();
        scheduleRule.id = id;
        scheduleRule.name = name;
        scheduleRule.type = type;
        return scheduleRule;
    }

    private RachioBaseStation baseStation() {
        RachioBaseStation baseStation = new RachioBaseStation();
        baseStation.id = "base-station-id";
        baseStation.name = "Hub";
        return baseStation;
    }

    private RachioValve valve() {
        RachioValve valve = new RachioValve();
        valve.id = "valve-id";
        valve.baseStationId = "base-station-id";
        valve.name = "Garden";
        return valve;
    }

    private RachioValveProgram valveProgram() {
        RachioValveProgram program = new RachioValveProgram();
        program.id = "program-id";
        program.valveId = "valve-id";
        program.name = "Morning Hose";
        return program;
    }
}
