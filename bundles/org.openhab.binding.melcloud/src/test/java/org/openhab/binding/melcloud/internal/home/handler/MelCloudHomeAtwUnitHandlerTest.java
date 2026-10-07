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
package org.openhab.binding.melcloud.internal.home.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_COP;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_ENERGY_CONSUMED;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_ENERGY_PRODUCED;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_ERROR_CODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_FORCED_HOTWATERMODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_ROOM_TEMPERATURE_ZONE2;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_SET_TEMPERATURE_ZONE1;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_TANK_WATER_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_OPERATION_STATUS;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_RSSI;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_ZONE1_OPERATION_MODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_FTC_MODEL;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_COOLING_MODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_ESTIMATED_ENERGY_CONSUMPTION;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_ESTIMATED_ENERGY_PRODUCTION;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_HALF_DEGREES;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_HOT_WATER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_MEASURED_ENERGY_CONSUMPTION;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_MEASURED_ENERGY_PRODUCTION;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATW_HAS_ZONE2;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ACCOUNT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATW_UNIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeRequestPacer;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwCapabilities;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwScheduleEntry;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwScheduleWriteRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwUnit;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeSetting;
import org.openhab.binding.melcloud.internal.mock.CallbackMock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link MelCloudHomeAtwUnitHandler}, in particular the zone-2 gating behavior.
 *
 * <p>
 * {@code @SuppressWarnings("null")}: Mockito ({@code mock}/{@code ArgumentMatchers}/{@code ArgumentCaptor}) is not
 * designed with null type annotations in mind, so combining it with this {@code @NonNullByDefault} test class
 * produces "unsafe interpretation" compiler advisories with no null-safety benefit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class MelCloudHomeAtwUnitHandlerTest {

    private static final String UNIT_ID = "unit-1";
    private static final String ACCESS_TOKEN = "test-token";

    private final ThingUID bridgeUID = new ThingUID(THING_TYPE_MELCLOUD_HOME_ACCOUNT, "bridge1");
    private final CallbackMock callback = new CallbackMock();

    private MelCloudHomeAccountHandler accountHandler = mock(MelCloudHomeAccountHandler.class);
    private MelCloudHomeApiClient apiClient = mock(MelCloudHomeApiClient.class);

    @BeforeEach
    void setUp() throws Exception {
        accountHandler = mock(MelCloudHomeAccountHandler.class);
        apiClient = mock(MelCloudHomeApiClient.class);
        when(accountHandler.getApiClient()).thenReturn(apiClient);
        when(accountHandler.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(accountHandler.getRequestPacer())
                .thenReturn(new MelCloudHomeRequestPacer(Executors.newSingleThreadScheduledExecutor()));
    }

    private MelCloudHomeAtwUnitHandler createHandler() {
        Configuration configuration = new Configuration(Map.of("unitId", UNIT_ID));
        Thing thing = ThingBuilder.create(THING_TYPE_MELCLOUD_HOME_ATW_UNIT, "test").withConfiguration(configuration)
                .withBridge(bridgeUID).build();

        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        callback.setBridge(bridge);

        MelCloudHomeAtwUnitHandler handler = new MelCloudHomeAtwUnitHandler(thing);
        handler.setCallback(callback);
        return handler;
    }

    private static MelCloudHomeAtwUnit unitWithSettings(String... nameValuePairs) {
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();
        unit.id = UNIT_ID;
        unit.givenDisplayName = "Heat Pump";
        List<MelCloudHomeSetting> settings = new ArrayList<>();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            MelCloudHomeSetting setting = new MelCloudHomeSetting();
            setting.name = nameValuePairs[i];
            setting.value = nameValuePairs[i + 1];
            settings.add(setting);
        }
        unit.settings = settings;
        return unit;
    }

    @Test
    void whenHasZone2IsFalseThenZone2ChannelsAreNotUpdated() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True", "HasZone2", "False", "RoomTemperatureZone2",
                "19.0");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        callback.waitForOnline();
        assertEquals(OnOffType.ON, callback.getState(CHANNEL_POWER));
        assertNull(callback.stateMap.get(CHANNEL_HOME_ROOM_TEMPERATURE_ZONE2));
    }

    @Test
    void whenHasZone2IsTrueThenZone2ChannelsAreUpdated() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True", "HasZone2", "True", "RoomTemperatureZone2",
                "19.0");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        callback.waitForOnline();
        assertEquals(new QuantityType<>(19.0, SIUnits.CELSIUS), callback.getState(CHANNEL_HOME_ROOM_TEMPERATURE_ZONE2));
    }

    @Test
    void whenSetTemperatureZone1CommandIsSentThenControlAtwUnitIsCalledWithZone1Temperature() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_SET_TEMPERATURE_ZONE1);

        // Act
        handler.handleCommand(channelUID, new QuantityType<>(22.0, SIUnits.CELSIUS));

        // Assert
        ArgumentCaptor<MelCloudHomeAtwControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwControlRequest.class);
        verify(apiClient).controlAtwUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(22.0, captor.getValue().setTemperatureZone1);
    }

    @Test
    void whenForcedHotWaterModeCommandIsSentThenControlAtwUnitIsCalledWithForcedHotWaterModeTrue() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_FORCED_HOTWATERMODE);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        ArgumentCaptor<MelCloudHomeAtwControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwControlRequest.class);
        verify(apiClient).controlAtwUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().forcedHotWaterMode);
    }

    @Test
    void whenCommandMatchesLastKnownStateThenControlCallIsSkipped() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        handler.onAtwUnitUpdated(unitWithSettings("Power", "True"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        verify(apiClient, never()).controlAtwUnit(any(), any(), any());
    }

    @Test
    void whenCommandIsRevertedBeforeNextPollThenBothCommandsAreSent() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        handler.onAtwUnitUpdated(unitWithSettings("Power", "False"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);
        handler.handleCommand(channelUID, OnOffType.OFF);

        // Assert: the polled state still says OFF, but the binding itself switched the unit ON in between
        ArgumentCaptor<MelCloudHomeAtwControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwControlRequest.class);
        verify(apiClient, timeout(3000).times(2)).controlAtwUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(Boolean.TRUE, captor.getAllValues().get(0).power);
        assertEquals(Boolean.FALSE, captor.getAllValues().get(1).power);
    }

    @Test
    void whenCommandDiffersFromLastKnownStateThenControlCallIsSent() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        handler.onAtwUnitUpdated(unitWithSettings("Power", "True"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.OFF);

        // Assert
        ArgumentCaptor<MelCloudHomeAtwControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwControlRequest.class);
        verify(apiClient).controlAtwUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().power);
    }

    @Test
    void whenNoPriorKnownStateExistsThenCommandIsAlwaysSent() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        verify(apiClient).controlAtwUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), any());
    }

    // A value absent from the settings array (or a null top-level field) must be pushed as UnDefType.UNDEF,
    // not silently skipped. Mirrors the equivalent tests in MelCloudHomeAtaUnitHandlerTest.

    @Test
    void whenSetTemperatureZone1IsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_HOME_SET_TEMPERATURE_ZONE1));
    }

    @Test
    void whenZone1OperationModeWordIsReportedThenChannelIsUpdatedWithMappedCode() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("OperationModeZone1", "HeatCurve");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(new DecimalType(2), callback.getState(CHANNEL_ZONE1_OPERATION_MODE));
    }

    @Test
    void whenZone1OperationModeWordIsUnknownThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("OperationModeZone1", "SomeNewMode");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_ZONE1_OPERATION_MODE));
    }

    @Test
    void whenOperationStatusWordIsReportedThenChannelIsUpdatedWithMappedCode() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("OperationMode", "HotWater");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(new DecimalType(2), callback.getState(CHANNEL_OPERATION_STATUS));
    }

    @Test
    void whenZone1OperationModeCodeCommandIsSentThenControlAtwUnitIsCalledWithMappedWord() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_ZONE1_OPERATION_MODE);

        // Act
        handler.handleCommand(channelUID, new DecimalType(1));

        // Assert
        ArgumentCaptor<MelCloudHomeAtwControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwControlRequest.class);
        verify(apiClient).controlAtwUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals("HeatFlowTemperature", captor.getValue().operationModeZone1);
    }

    @Test
    void whenZone1OperationModeCommandHasUnknownCodeThenNoControlCallIsMade() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_ZONE1_OPERATION_MODE);

        // Act
        handler.handleCommand(channelUID, new DecimalType(99));

        // Assert
        verify(apiClient, never()).controlAtwUnit(any(), any(), any());
    }

    @Test
    void whenTankWaterTemperatureIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_HOME_TANK_WATER_TEMPERATURE));
    }

    @Test
    void whenOutdoorTemperatureIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange: this is the common real-world case for ATW units, whose settings array often does not include
        // OutdoorTemperature at all (confirmed against reference captures/fixtures).
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_OUTDOOR_TEMPERATURE));
    }

    @Test
    void whenErrorCodeIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_ERROR_CODE));
    }

    @Test
    void whenRssiIsNullThenChannelIsUpdatedWithUndef() {
        // Arrange: unitWithSettings() leaves rssi at its default (null).
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_RSSI));
    }

    @Test
    void whenEnergyTelemetryFetchReturnsEmptyThenChannelsAreUpdatedWithUndef() throws Exception {
        // Arrange: exercises the telemetry-poll path directly (fetchLatestEnergyWh), not just the settings-array
        // path covered above. Also covers the derived "cop" channel, which is not computable without both values.
        when(apiClient.fetchLatestEnergyWh(eq(ACCESS_TOKEN), eq(UNIT_ID), any(), any(), any()))
                .thenReturn(Optional.empty());
        MelCloudHomeAtwUnitHandler handler = createHandler();

        // Act
        handler.initialize();

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_ENERGY_CONSUMED));
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_ENERGY_PRODUCED));
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_COP));
    }

    @Test
    void whenUnitUpdateArrivesThenCapabilitiesAreWrittenAsThingProperties() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");
        MelCloudHomeAtwCapabilities capabilities = new MelCloudHomeAtwCapabilities();
        capabilities.hasHotWater = true;
        capabilities.hasZone2 = false;
        capabilities.hasHalfDegrees = true;
        capabilities.hasCoolingMode = true;
        capabilities.hasMeasuredEnergyConsumption = false;
        capabilities.hasMeasuredEnergyProduction = false;
        capabilities.hasEstimatedEnergyConsumption = true;
        capabilities.hasEstimatedEnergyProduction = true;
        capabilities.ftcModel = 5;
        unit.capabilities = capabilities;

        // Act
        handler.onAtwUnitUpdated(unit);

        // Assert
        Map<String, String> properties = handler.getThing().getProperties();
        assertEquals("true", properties.get(PROPERTY_ATW_HAS_HOT_WATER));
        assertEquals("false", properties.get(PROPERTY_ATW_HAS_ZONE2));
        assertEquals("true", properties.get(PROPERTY_ATW_HAS_HALF_DEGREES));
        assertEquals("true", properties.get(PROPERTY_ATW_HAS_COOLING_MODE));
        assertEquals("false", properties.get(PROPERTY_ATW_HAS_MEASURED_ENERGY_CONSUMPTION));
        assertEquals("false", properties.get(PROPERTY_ATW_HAS_MEASURED_ENERGY_PRODUCTION));
        assertEquals("true", properties.get(PROPERTY_ATW_HAS_ESTIMATED_ENERGY_CONSUMPTION));
        assertEquals("true", properties.get(PROPERTY_ATW_HAS_ESTIMATED_ENERGY_PRODUCTION));
        assertEquals("5", properties.get(PROPERTY_ATW_FTC_MODEL));
    }

    @Test
    void whenSecondUnitUpdateArrivesWithDifferentCapabilitiesThenPropertiesAreNotOverwritten() {
        // Arrange: capabilities are static for a unit's lifetime, so the handler writes them once and ignores any
        // later change rather than re-writing on every /context poll.
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit firstUnit = unitWithSettings("Power", "True");
        firstUnit.capabilities.ftcModel = 5;
        MelCloudHomeAtwUnit secondUnit = unitWithSettings("Power", "True");
        secondUnit.capabilities.ftcModel = 3;

        // Act
        handler.onAtwUnitUpdated(firstUnit);
        handler.onAtwUnitUpdated(secondUnit);

        // Assert
        assertEquals("5", handler.getThing().getProperties().get(PROPERTY_ATW_FTC_MODEL));
    }

    // --- Schedule management ---
    //
    // These tests verify the handler's own field/mapping logic and its calls to the (mocked) apiClient — they do
    // NOT verify that the real MELCloud Home API actually accepts these shapes, which is unconfirmed.

    @Test
    void whenListSchedulesIsCalledBeforeAnyUpdateThenItReturnsEmptyList() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act & Assert
        assertEquals(List.of(), handler.listSchedules());
    }

    @Test
    void whenListSchedulesIsCalledAfterUpdateThenItReturnsMappedEntries() {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");
        MelCloudHomeAtwScheduleEntry entry = new MelCloudHomeAtwScheduleEntry();
        entry.id = "schedule-1";
        entry.days = List.of("monday", "tuesday");
        entry.time = "06:00:00";
        entry.power = true;
        entry.zone1Active = true;
        entry.operationModeZone1 = "heatRoomTemperature";
        unit.schedule = List.of(entry);

        // Act
        handler.onAtwUnitUpdated(unit);
        List<Map<String, Object>> schedules = handler.listSchedules();

        // Assert
        assertEquals(1, schedules.size());
        Map<String, Object> mapped = schedules.get(0);
        assertEquals("schedule-1", mapped.get("id"));
        assertEquals(List.of("monday", "tuesday"), mapped.get("days"));
        assertEquals(true, mapped.get("zone1Active"));
        assertEquals("heatRoomTemperature", mapped.get("operationModeZone1"));
    }

    @Test
    void whenCreateScheduleIsCalledWithConfirmedHeatingModeThenApiClientIsCalledWithTranslatedFields()
            throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act
        String id = handler.createSchedule("monday,wednesday", "06:00:00", true, "heatRoomTemperature", 21.0, null,
                null, null);

        // Assert
        assertFalse(id.isBlank(), "createSchedule should return a generated, non-blank id on success");
        ArgumentCaptor<MelCloudHomeAtwScheduleWriteRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwScheduleWriteRequest.class);
        verify(apiClient).createOrUpdateAtwSchedule(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        MelCloudHomeAtwScheduleWriteRequest sent = captor.getValue();
        assertEquals(id, sent.id);
        assertEquals(List.of(1, 3), sent.days); // 0=Sunday..6=Saturday: monday=1, wednesday=3
        assertEquals(Integer.valueOf(0), sent.operationModeZone1); // heatRoomTemperature -> 0
        assertEquals(21.0, sent.setTemperatureZone1);
    }

    @Test
    void whenCreateScheduleIsCalledWithCoolingModeThenItIsRejectedWithoutCallingApiClient() throws Exception {
        // Arrange: cooling modes have no confirmed schedule integer code, so must be rejected, not guessed.
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act
        String id = handler.createSchedule("monday", "06:00:00", true, "CoolRoomTemperature", 21.0, null, null, null);

        // Assert
        assertEquals("", id);
        verify(apiClient, never()).createOrUpdateAtwSchedule(any(), any(), any());
    }

    @Test
    void whenCreateScheduleIsCalledWithUnknownDayThenItIsRejectedWithoutCallingApiClient() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act
        String id = handler.createSchedule("notaday", "06:00:00", true, null, null, null, null, null);

        // Assert
        assertEquals("", id);
        verify(apiClient, never()).createOrUpdateAtwSchedule(any(), any(), any());
    }

    private static MelCloudHomeAtwUnit unitWithScheduleEntry(@Nullable String operationModeZone1) {
        MelCloudHomeAtwUnit unit = unitWithSettings("Power", "True");
        MelCloudHomeAtwScheduleEntry entry = new MelCloudHomeAtwScheduleEntry();
        entry.id = "schedule-1";
        entry.days = List.of("monday", "tuesday");
        entry.time = "06:00:00";
        entry.power = true;
        entry.operationModeZone1 = operationModeZone1;
        entry.setTemperatureZone1 = 21.0;
        entry.setTankWaterTemperature = 45.0;
        entry.forcedHotWaterMode = false;
        unit.schedule = List.of(entry);
        return unit;
    }

    @Test
    void whenUpdateScheduleIsCalledWithPartialFieldsThenTheCompleteEntryIsSent() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        handler.onAtwUnitUpdated(unitWithScheduleEntry("heatRoomTemperature"));

        // Act
        boolean result = handler.updateSchedule("schedule-1", null, null, null, null, 19.0, null, null, null);

        // Assert
        assertTrue(result);
        ArgumentCaptor<MelCloudHomeAtwScheduleWriteRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtwScheduleWriteRequest.class);
        verify(apiClient).createOrUpdateAtwSchedule(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        MelCloudHomeAtwScheduleWriteRequest sent = captor.getValue();
        assertEquals("schedule-1", sent.id);
        assertEquals(List.of(1, 2), sent.days);
        assertEquals("06:00:00", sent.time);
        assertEquals(Boolean.TRUE, sent.power);
        assertEquals(Integer.valueOf(0), sent.operationModeZone1);
        assertEquals(19.0, sent.setTemperatureZone1);
        assertEquals(45.0, sent.setTankWaterTemperature);
        assertEquals(Boolean.FALSE, sent.forcedHotWaterMode);
    }

    @Test
    void whenUpdateScheduleIsCalledWithUnknownIdThenItIsRejectedWithoutCallingApiClient() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        handler.onAtwUnitUpdated(unitWithScheduleEntry("heatRoomTemperature"));

        // Act
        boolean result = handler.updateSchedule("other-id", null, null, null, null, 19.0, null, null, null);

        // Assert
        assertFalse(result);
        verify(apiClient, never()).createOrUpdateAtwSchedule(any(), any(), any());
    }

    @Test
    void whenUpdateScheduleIsCalledBeforeAnyUpdateThenItIsRejectedWithoutCallingApiClient() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act
        boolean result = handler.updateSchedule("schedule-1", null, null, null, null, 19.0, null, null, null);

        // Assert
        assertFalse(result);
        verify(apiClient, never()).createOrUpdateAtwSchedule(any(), any(), any());
    }

    @Test
    void whenExistingEntryHasCoolingModeThenUpdateScheduleIsRejectedWithoutCallingApiClient() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();
        handler.onAtwUnitUpdated(unitWithScheduleEntry("CoolRoomTemperature"));

        // Act
        boolean result = handler.updateSchedule("schedule-1", null, null, null, null, 19.0, null, null, null);

        // Assert
        assertFalse(result);
        verify(apiClient, never()).createOrUpdateAtwSchedule(any(), any(), any());
    }

    @Test
    void whenDeleteScheduleIsCalledThenApiClientDeleteIsCalledAndReturnsTrue() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act
        boolean result = handler.deleteSchedule("schedule-1");

        // Assert
        assertTrue(result);
        verify(apiClient).deleteAtwSchedule(ACCESS_TOKEN, UNIT_ID, "schedule-1");
    }

    @Test
    void whenSetSchedulesEnabledIsCalledThenApiClientIsCalledWithValue() throws Exception {
        // Arrange
        MelCloudHomeAtwUnitHandler handler = createHandler();
        handler.initialize();

        // Act
        boolean result = handler.setSchedulesEnabled(false);

        // Assert
        assertTrue(result);
        verify(apiClient).setAtwScheduleEnabled(ACCESS_TOKEN, UNIT_ID, false);
    }

    @Test
    void whenNoBridgeIsConnectedThenCreateScheduleReturnsEmptyStringWithoutCallingApiClient() throws Exception {
        // Arrange: handler.initialize() is deliberately not called, so bridgeHandler stays null.
        MelCloudHomeAtwUnitHandler handler = createHandler();

        // Act
        String id = handler.createSchedule("monday", "06:00:00", true, null, null, null, null, null);

        // Assert
        assertEquals("", id);
        verify(apiClient, never()).createOrUpdateAtwSchedule(any(), any(), any());
    }
}
