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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_ERROR_CODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_FAN_SPEED;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_OPERATION_MODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_ROOM_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_SET_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_VANE_HORIZONTAL;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_VANE_VERTICAL;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_RSSI;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_HAS_ENERGY_CONSUMED_METER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_HAS_HALF_DEGREE_INCREMENTS;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_HAS_STANDBY;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_HAS_SWING;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_MAX_TEMP_COOL_DRY;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_MAX_TEMP_HEAT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_MIN_TEMP_COOL_DRY;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_MIN_TEMP_HEAT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.PROPERTY_ATA_NUMBER_OF_FAN_SPEEDS;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ACCOUNT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATA_UNIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeRequestPacer;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaCapabilities;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtaUnit;
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
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link MelCloudHomeAtaUnitHandler}.
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
class MelCloudHomeAtaUnitHandlerTest {

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

    private MelCloudHomeAtaUnitHandler createHandler(String unitId, boolean withBridge, ThingStatus bridgeStatus) {
        Configuration configuration = new Configuration(Map.of("unitId", unitId));
        ThingBuilder builder = ThingBuilder.create(THING_TYPE_MELCLOUD_HOME_ATA_UNIT, "test")
                .withConfiguration(configuration);
        if (withBridge) {
            builder = builder.withBridge(bridgeUID);
        }
        Thing thing = builder.build();

        if (withBridge) {
            Bridge bridge = mock(Bridge.class);
            when(bridge.getHandler()).thenReturn(accountHandler);
            when(bridge.getStatus()).thenReturn(bridgeStatus);
            callback.setBridge(bridge);
        }

        MelCloudHomeAtaUnitHandler handler = new MelCloudHomeAtaUnitHandler(thing);
        handler.setCallback(callback);
        return handler;
    }

    private static MelCloudHomeAtaUnit unitWithSettings(String... nameValuePairs) {
        MelCloudHomeAtaUnit unit = new MelCloudHomeAtaUnit();
        unit.id = UNIT_ID;
        unit.givenDisplayName = "Living Room";
        unit.rssi = -55;
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
    void whenUnitIdIsMissingThenThingGoesOffline() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler("", false, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenBridgeIsNotSetThenThingGoesOffline() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, false, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenBridgeIsOnlineButNoUnitUpdateHasArrivedYetThenThingStaysUnknown() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        assertEquals(ThingStatus.UNKNOWN, callback.getStatus().getStatus());
        verify(accountHandler).registerAtaUnitListener(eq(UNIT_ID), eq(handler));
    }

    @Test
    void whenUnitUpdateArrivesThenThingGoesOnlineAndChannelsAreUpdated() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True", "OperationMode", "Cool", "SetTemperature", "21.5",
                "RoomTemperature", "22.0", "SetFanSpeed", "2");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        callback.waitForOnline();
        assertEquals(OnOffType.ON, callback.getState(CHANNEL_POWER));
        assertEquals(new QuantityType<>(21.5, SIUnits.CELSIUS), callback.getState(CHANNEL_HOME_SET_TEMPERATURE));
        assertEquals(new DecimalType(3), callback.getState(CHANNEL_HOME_OPERATION_MODE));
        assertEquals(new DecimalType(2), callback.getState(CHANNEL_HOME_FAN_SPEED));
    }

    @Test
    void whenFanSpeedSettingIsWordThenChannelIsUpdatedWithMatchingCode() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("SetFanSpeed", "Four");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(new DecimalType(4), callback.getState(CHANNEL_HOME_FAN_SPEED));
    }

    @Test
    void whenFanSpeedCommandIsSentThenControlAtaUnitIsCalledWithMatchingWord() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_FAN_SPEED);

        // Act
        handler.handleCommand(channelUID, new DecimalType(0));

        // Assert
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals("Auto", captor.getValue().setFanSpeed);
    }

    @Test
    void whenOperationModeSettingIsWordThenChannelIsUpdatedWithMatchingCode() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("OperationMode", "Automatic");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(new DecimalType(8), callback.getState(CHANNEL_HOME_OPERATION_MODE));
    }

    @Test
    void whenVaneHorizontalSettingIsWordThenChannelIsUpdatedWithMatchingCode() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("VaneHorizontalDirection", "RightCentre");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(new DecimalType(4), callback.getState(CHANNEL_HOME_VANE_HORIZONTAL));
    }

    @Test
    void whenVaneVerticalSettingIsNumericStringThenChannelIsUpdatedWithMatchingCode() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("VaneVerticalDirection", "7");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(new DecimalType(7), callback.getState(CHANNEL_HOME_VANE_VERTICAL));
    }

    @Test
    void whenOperationModeCommandIsSentThenControlAtaUnitIsCalledWithMatchingWord() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_OPERATION_MODE);

        // Act
        handler.handleCommand(channelUID, new DecimalType(3));

        // Assert
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals("Cool", captor.getValue().operationMode);
    }

    @Test
    void whenVaneHorizontalCommandIsSentThenControlAtaUnitIsCalledWithMatchingWord() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_VANE_HORIZONTAL);

        // Act
        handler.handleCommand(channelUID, new DecimalType(1));

        // Assert
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals("Left", captor.getValue().vaneHorizontalDirection);
    }

    @Test
    void whenVaneVerticalCommandIsSentThenControlAtaUnitIsCalledWithMatchingWord() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_VANE_VERTICAL);

        // Act
        handler.handleCommand(channelUID, new DecimalType(7));

        // Assert
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals("Swing", captor.getValue().vaneVerticalDirection);
    }

    @Test
    void whenOperationModeCommandHasUnknownCodeThenCommandIsIgnored() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_HOME_OPERATION_MODE);

        // Act
        handler.handleCommand(channelUID, new DecimalType(99));

        // Assert
        verify(apiClient, never()).controlAtaUnit(any(), any(), any());
    }

    @Test
    void whenBridgeIsOfflineThenThingGoesOffline() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.OFFLINE);

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenPowerOnCommandIsSentThenControlAtaUnitIsCalledWithPowerTrue() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().power);
    }

    @Test
    void whenCommandMatchesLastKnownStateThenControlCallIsSkipped() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        handler.onAtaUnitUpdated(unitWithSettings("Power", "True"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        verify(apiClient, never()).controlAtaUnit(any(), any(), any());
    }

    @Test
    void whenCommandDiffersFromLastKnownStateThenControlCallIsSent() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        handler.onAtaUnitUpdated(unitWithSettings("Power", "True"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.OFF);

        // Assert
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().power);
    }

    @Test
    void whenNoPriorKnownStateExistsThenCommandIsAlwaysSent() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        verify(apiClient).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), any());
    }

    @Test
    void whenCommandIsRevertedBeforeNextPollThenBothCommandsAreSent() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        handler.onAtaUnitUpdated(unitWithSettings("Power", "False"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);
        handler.handleCommand(channelUID, OnOffType.OFF);

        // Assert: the polled state still says OFF, but the binding itself switched the unit ON in between
        ArgumentCaptor<MelCloudHomeAtaControlRequest> captor = ArgumentCaptor
                .forClass(MelCloudHomeAtaControlRequest.class);
        verify(apiClient, timeout(3000).times(2)).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), captor.capture());
        assertEquals(Boolean.TRUE, captor.getAllValues().get(0).power);
        assertEquals(Boolean.FALSE, captor.getAllValues().get(1).power);
    }

    @Test
    void whenSameCommandIsRepeatedBeforeNextPollThenOnlyTheFirstIsSent() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        handler.onAtaUnitUpdated(unitWithSettings("Power", "False"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        verify(apiClient, times(1)).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), any());
    }

    @Test
    void whenControlCallFailedThenSameCommandIsSentAgain() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        handler.onAtaUnitUpdated(unitWithSettings("Power", "False"));
        ChannelUID channelUID = new ChannelUID(handler.getThing().getUID(), CHANNEL_POWER);
        doThrow(new MelCloudCommException("boom")).doNothing().when(apiClient).controlAtaUnit(any(), any(), any());

        // Act
        handler.handleCommand(channelUID, OnOffType.ON);
        handler.handleCommand(channelUID, OnOffType.ON);

        // Assert
        verify(apiClient, timeout(3000).times(2)).controlAtaUnit(eq(ACCESS_TOKEN), eq(UNIT_ID), any());
    }

    // A value absent from the settings array (or a null top-level field) must be pushed as UnDefType.UNDEF,
    // not silently skipped.

    @Test
    void whenSetTemperatureIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_HOME_SET_TEMPERATURE));
    }

    @Test
    void whenRoomTemperatureIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_HOME_ROOM_TEMPERATURE));
    }

    @Test
    void whenFanSpeedIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_HOME_FAN_SPEED));
    }

    @Test
    void whenFanSpeedWordIsUnrecognizedThenChannelIsNotUpdatedAtAll() {
        // Arrange: present-but-unrecognized is a different failure mode than missing and keeps the
        // existing "log and leave untouched" behavior rather than becoming UNDEF. onAtaUnitUpdated runs
        // synchronously, so the state map can be asserted directly without callback.getState()'s blocking wait
        // (which is designed for values that DO eventually arrive, not for asserting a permanent absence).
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("SetFanSpeed", "SuperTurbo");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(null, callback.stateMap.get(CHANNEL_HOME_FAN_SPEED));
    }

    @Test
    void whenErrorCodeIsMissingFromSettingsThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True");

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_ERROR_CODE));
    }

    @Test
    void whenRssiIsNullThenChannelIsUpdatedWithUndef() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True");
        unit.rssi = null;

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_RSSI));
    }

    @Test
    void whenOutdoorTemperatureFetchReturnsEmptyThenChannelIsUpdatedWithUndef() throws Exception {
        // Arrange: exercises the telemetry-poll path directly (fetchLatestOutdoorTemperature/fetchLatestEnergyWh),
        // not just the settings-array path covered above. callback.getState(...) itself blocks/retries for the
        // update to arrive from the scheduler-driven poll, so no extra wait helper is needed.
        when(apiClient.fetchLatestOutdoorTemperature(eq(ACCESS_TOKEN), eq(UNIT_ID), any(), any()))
                .thenReturn(Optional.empty());
        when(apiClient.fetchLatestEnergyWh(eq(ACCESS_TOKEN), eq(UNIT_ID), any(), any(), any()))
                .thenReturn(Optional.empty());
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        assertEquals(UnDefType.UNDEF, callback.getState(CHANNEL_OUTDOOR_TEMPERATURE));
    }

    @Test
    void whenUnitUpdateArrivesThenCapabilitiesAreWrittenAsThingProperties() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit unit = unitWithSettings("Power", "True");
        MelCloudHomeAtaCapabilities capabilities = new MelCloudHomeAtaCapabilities();
        capabilities.numberOfFanSpeeds = 5;
        capabilities.minTempHeat = 10.0;
        capabilities.maxTempHeat = 31.0;
        capabilities.minTempCoolDry = 16.0;
        capabilities.maxTempCoolDry = 31.0;
        capabilities.hasHalfDegreeIncrements = true;
        capabilities.hasSwing = true;
        capabilities.hasStandby = true;
        capabilities.hasEnergyConsumedMeter = true;
        unit.capabilities = capabilities;

        // Act
        handler.onAtaUnitUpdated(unit);

        // Assert
        Map<String, String> properties = handler.getThing().getProperties();
        assertEquals("5", properties.get(PROPERTY_ATA_NUMBER_OF_FAN_SPEEDS));
        assertEquals("10.0", properties.get(PROPERTY_ATA_MIN_TEMP_HEAT));
        assertEquals("31.0", properties.get(PROPERTY_ATA_MAX_TEMP_HEAT));
        assertEquals("16.0", properties.get(PROPERTY_ATA_MIN_TEMP_COOL_DRY));
        assertEquals("31.0", properties.get(PROPERTY_ATA_MAX_TEMP_COOL_DRY));
        assertEquals("true", properties.get(PROPERTY_ATA_HAS_HALF_DEGREE_INCREMENTS));
        assertEquals("true", properties.get(PROPERTY_ATA_HAS_SWING));
        assertEquals("true", properties.get(PROPERTY_ATA_HAS_STANDBY));
        assertEquals("true", properties.get(PROPERTY_ATA_HAS_ENERGY_CONSUMED_METER));
    }

    @Test
    void whenSecondUnitUpdateArrivesWithDifferentCapabilitiesThenPropertiesAreNotOverwritten() {
        // Arrange: capabilities are static for a unit's lifetime, so the handler
        // writes them once and ignores any later change rather than re-writing on every /context poll.
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);
        handler.initialize();
        MelCloudHomeAtaUnit firstUnit = unitWithSettings("Power", "True");
        firstUnit.capabilities.numberOfFanSpeeds = 5;
        MelCloudHomeAtaUnit secondUnit = unitWithSettings("Power", "True");
        secondUnit.capabilities.numberOfFanSpeeds = 3;

        // Act
        handler.onAtaUnitUpdated(firstUnit);
        handler.onAtaUnitUpdated(secondUnit);

        // Assert
        assertEquals("5", handler.getThing().getProperties().get(PROPERTY_ATA_NUMBER_OF_FAN_SPEEDS));
    }
}
