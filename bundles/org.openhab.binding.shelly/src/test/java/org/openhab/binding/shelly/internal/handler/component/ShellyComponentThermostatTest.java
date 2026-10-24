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
package org.openhab.binding.shelly.internal.handler.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2DeviceStatusThermostat;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null", "unchecked" })
public class ShellyComponentThermostatTest {
    private static final String ENABLE_ID = CHANNEL_GROUP_CONTROL + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + CHANNEL_THERMOSTAT_ENABLE;
    private static final String TARGET_TEMP_ID = CHANNEL_GROUP_CONTROL + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + CHANNEL_CONTROL_SETTEMP;

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    private ShellyThingInterface newHandler(ThingTypeUID thingTypeUID) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(thingTypeUID, "test"));
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getProfile()).thenReturn(new ShellyDeviceProfile(thingTypeUID));
        return handler;
    }

    @Test
    void thermostatCreatesChannelsAndPublishesState() {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.relayInThermostat = true;
        status.sensorInThermostat = false;
        status.thermostat = new Shelly2DeviceStatusThermostat();
        status.thermostat.enable = true;
        status.thermostat.targetC = 23.5;
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);

        ShellyComponentThermostat.updateChannels(handler, status);

        ArgumentCaptor<Map<String, Channel>> channels = ArgumentCaptor.forClass(Map.class);
        verify(handler).updateThingChannels(eq(Map.of()), channels.capture());
        assertEquals(Set.of(ENABLE_ID, TARGET_TEMP_ID), channels.getValue().keySet());
        verify(handler, never()).removeChannels(any());
        verify(handler).updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RELAY_IN_THERMOSTAT, OnOffType.ON);
        verify(handler).updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_SENSOR_IN_THERMOSTAT, OnOffType.OFF);
        verify(handler).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_THERMOSTAT_ENABLE, OnOffType.ON);
        verify(handler).updateChannel(eq(CHANNEL_GROUP_CONTROL), eq(CHANNEL_CONTROL_SETTEMP),
                argThat(s -> s instanceof QuantityType<?> q && q.doubleValue() == 23.5));
    }

    @ParameterizedTest
    @MethodSource("provideRemovedChannelsWithoutThermostat")
    void missingThermostatRemovesChannelsButKeepsTrvTargetTemp(ThingTypeUID thingTypeUID, Set<String> removed) {
        ShellyThingInterface handler = newHandler(thingTypeUID);

        ShellyComponentThermostat.updateChannels(handler, new ShellySettingsStatus());

        verify(handler).removeChannels(removed);
        verify(handler, never()).updateThingChannels(any(), any());
    }

    private static Stream<Arguments> provideRemovedChannelsWithoutThermostat() {
        return Stream.of(Arguments.of(THING_TYPE_SHELLYPLUSWALLDISPLAY, Set.of(ENABLE_ID, TARGET_TEMP_ID)),
                Arguments.of(THING_TYPE_SHELLYTRV, Set.of(ENABLE_ID)));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void onlyFullStatusClearsMissingThermostat(boolean fullStatus) {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.thermostat = new Shelly2DeviceStatusThermostat();

        ShellyComponentThermostat.fillStatus(status, new Shelly2DeviceStatusResult(), fullStatus);

        assertEquals(fullStatus, status.thermostat == null);
    }

    @Test
    void targetTempCommandSetsThermostatConfig() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);

        ShellyComponentThermostat.handleCommand(handler, CHANNEL_CONTROL_SETTEMP, new DecimalType(21.5));

        ArgumentCaptor<Shelly2RpcRequest> request = ArgumentCaptor.forClass(Shelly2RpcRequest.class);
        verify(api).apiRequest(request.capture());
        assertEquals(SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG, request.getValue().method);
        assertEquals(0, request.getValue().params.id);
        assertEquals(21.5, request.getValue().params.config.targetC);
        assertNull(request.getValue().params.config.enable);
    }
}
