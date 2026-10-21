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
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_LISTPROFILES;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_SETCONFIG;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2DeviceStatusThermostat;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2DeviceStatusThermostat.Shelly2DeviceStatusThermostatSchedules;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2ThermostatProfiles;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2ThermostatProfiles.Shelly2ThermostatProfile;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;

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
    private static final String SCHEDULE_ID = CHANNEL_GROUP_CONTROL + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + CHANNEL_CONTROL_SCHEDULE;
    private static final String PROFILE_ID = CHANNEL_GROUP_CONTROL + ChannelUID.CHANNEL_GROUP_SEPARATOR
            + CHANNEL_CONTROL_PROFILE;

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
        return Stream.of(
                Arguments.of(THING_TYPE_SHELLYPLUSWALLDISPLAY,
                        Set.of(ENABLE_ID, TARGET_TEMP_ID, SCHEDULE_ID, PROFILE_ID)),
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

    static Stream<Arguments> thermostatCommands() {
        return Stream.of(Arguments.of(CHANNEL_CONTROL_SETTEMP, new DecimalType(21.5), null, 21.5),
                Arguments.of(CHANNEL_CONTROL_SETTEMP, new QuantityType<>(70.7, ImperialUnits.FAHRENHEIT), null, 21.5),
                Arguments.of(CHANNEL_THERMOSTAT_ENABLE, OnOffType.OFF, false, null));
    }

    @ParameterizedTest
    @MethodSource("thermostatCommands")
    void commandSetsThermostatConfig(String channel, Command command, @Nullable Boolean enable,
            @Nullable Double targetC) throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);

        ShellyComponentThermostat.handleCommand(handler, channel, command);

        ArgumentCaptor<Shelly2RpcRequest> request = ArgumentCaptor.forClass(Shelly2RpcRequest.class);
        verify(api).apiRequest(request.capture());
        assertEquals(SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG, request.getValue().method);
        assertEquals(0, request.getValue().params.id);
        assertEquals(enable, request.getValue().params.config.enable);
        Double actualC = request.getValue().params.config.targetC;
        if (targetC == null) {
            assertNull(actualC);
        } else {
            assertNotNull(actualC);
            assertEquals(targetC, actualC, 0.01);
        }
    }

    private static Shelly2ThermostatProfiles profiles(Object... idsAndNames) {
        Shelly2ThermostatProfiles response = new Shelly2ThermostatProfiles();
        response.profiles = new ArrayList<>();
        for (int i = 0; i < idsAndNames.length; i += 2) {
            Shelly2ThermostatProfile profile = new Shelly2ThermostatProfile();
            profile.id = (Integer) idsAndNames[i];
            profile.name = (String) idsAndNames[i + 1];
            response.profiles.add(profile);
        }
        return response;
    }

    private static ShellySettingsStatus statusWithSchedule(@Nullable Boolean enable, @Nullable Integer profileId,
            @Nullable String profileName) {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.thermostat = new Shelly2DeviceStatusThermostat();
        status.thermostat.schedules = new Shelly2DeviceStatusThermostatSchedules();
        status.thermostat.schedules.enable = enable;
        status.thermostat.schedules.profileId = profileId;
        status.thermostat.schedules.profileName = profileName;
        return status;
    }

    @Test
    void scheduleCreatesChannelsReadsProfilesOnceAndPublishesState() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        when(api.apiRequest(any(Shelly2RpcRequest.class), eq(Shelly2ThermostatProfiles.class)))
                .thenReturn(profiles(0, "Winter", 1, "Sommer"));
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);
        ShellySettingsStatus status = statusWithSchedule(true, 1, "Sommer");

        ShellyComponentThermostat.updateChannels(handler, status);
        ShellyComponentThermostat.updateChannels(handler, status);

        ArgumentCaptor<Map<String, Channel>> channels = ArgumentCaptor.forClass(Map.class);
        verify(handler, times(2)).updateThingChannels(eq(Map.of()), channels.capture());
        assertEquals(Set.of(ENABLE_ID, TARGET_TEMP_ID, SCHEDULE_ID, PROFILE_ID), channels.getValue().keySet());

        ArgumentCaptor<Shelly2RpcRequest> request = ArgumentCaptor.forClass(Shelly2RpcRequest.class);
        verify(api, times(1)).apiRequest(request.capture(), eq(Shelly2ThermostatProfiles.class));
        assertEquals(SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_LISTPROFILES, request.getValue().method);
        assertEquals(0, request.getValue().params.id);

        Map<String, String> options = new LinkedHashMap<>();
        options.put("0", "Winter");
        options.put("1", "Sommer");
        verify(handler, times(1)).updateStateOptions(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE, options);
        verify(handler, times(2)).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SCHEDULE, OnOffType.ON);
        verify(handler, times(2)).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE, new StringType("1"));
    }

    @Test
    void unknownActiveProfileIsReadAgainAndKeptEvenIfNotListed() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        when(api.apiRequest(any(Shelly2RpcRequest.class), eq(Shelly2ThermostatProfiles.class)))
                .thenReturn(profiles(0, "Winter"));
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);

        ShellyComponentThermostat.updateChannels(handler, statusWithSchedule(true, 0, "Winter"));
        ShellyComponentThermostat.updateChannels(handler, statusWithSchedule(true, 7, "Guests"));
        ShellyComponentThermostat.updateChannels(handler, statusWithSchedule(true, 7, "Guests"));

        verify(api, times(2)).apiRequest(any(Shelly2RpcRequest.class), eq(Shelly2ThermostatProfiles.class));
        Map<String, String> options = new LinkedHashMap<>();
        options.put("0", "Winter");
        options.put("7", "Guests");
        verify(handler).updateStateOptions(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE, options);
    }

    @Test
    void failedProfileReadStillPublishesStateAndIsRetried() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        when(api.apiRequest(any(Shelly2RpcRequest.class), eq(Shelly2ThermostatProfiles.class)))
                .thenThrow(new ShellyApiException("timeout"));
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);
        ShellySettingsStatus status = statusWithSchedule(false, 2, "Away");

        ShellyComponentThermostat.updateChannels(handler, status);
        ShellyComponentThermostat.updateChannels(handler, status);

        verify(api, times(2)).apiRequest(any(Shelly2RpcRequest.class), eq(Shelly2ThermostatProfiles.class));
        verify(handler, never()).updateStateOptions(any(), any(), any());
        verify(handler, times(2)).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SCHEDULE, OnOffType.OFF);
        verify(handler, times(2)).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE, new StringType("2"));
    }

    @Test
    void scheduleCommandSetsScheduleConfig() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);

        ShellyComponentThermostat.handleCommand(handler, CHANNEL_CONTROL_SCHEDULE, OnOffType.OFF);

        ArgumentCaptor<Shelly2RpcRequest> request = ArgumentCaptor.forClass(Shelly2RpcRequest.class);
        verify(api).apiRequest(request.capture());
        assertEquals(SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_SETCONFIG, request.getValue().method);
        assertEquals(0, request.getValue().params.id);
        assertEquals(false, request.getValue().params.config.enable);
        assertNull(request.getValue().params.config.profileId);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", " 1 ", "sommer", "Sommer" })
    void profileCommandAcceptsIdOrName(String command) throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);
        handler.getProfile().thermostatProfiles.putAll(Map.of(0, "Winter", 1, "Sommer"));

        ShellyComponentThermostat.handleCommand(handler, CHANNEL_CONTROL_PROFILE, new StringType(command));

        ArgumentCaptor<Shelly2RpcRequest> request = ArgumentCaptor.forClass(Shelly2RpcRequest.class);
        verify(api).apiRequest(request.capture());
        assertEquals(SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_SETCONFIG, request.getValue().method);
        assertEquals(1, request.getValue().params.config.profileId);
        assertNull(request.getValue().params.config.enable);
    }

    @Test
    void profileCommandWithUnknownNameIsIgnored() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = newHandler(THING_TYPE_SHELLYPLUSWALLDISPLAY);
        when(handler.getApi()).thenReturn(api);
        handler.getProfile().thermostatProfiles.put(0, "Winter");

        ShellyComponentThermostat.handleCommand(handler, CHANNEL_CONTROL_PROFILE, new StringType("Holiday"));

        verify(api, never()).apiRequest(any(Shelly2RpcRequest.class));
    }

    @Test
    void profileIdIsResolvedFromIdOrName() {
        Map<Integer, String> profiles = Map.of(0, "Winter", 1, "Sommer");

        assertEquals(1, ShellyComponentThermostat.getProfileId(profiles, "1"));
        assertEquals(5, ShellyComponentThermostat.getProfileId(profiles, "5"));
        assertEquals(0, ShellyComponentThermostat.getProfileId(profiles, "WINTER"));
        assertNull(ShellyComponentThermostat.getProfileId(profiles, "Holiday"));
    }
}
