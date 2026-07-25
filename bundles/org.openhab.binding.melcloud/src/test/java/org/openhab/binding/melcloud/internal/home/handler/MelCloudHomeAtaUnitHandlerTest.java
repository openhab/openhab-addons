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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_FAN_SPEED;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_OPERATION_MODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_SET_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_VANE_HORIZONTAL;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_VANE_VERTICAL;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ACCOUNT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATA_UNIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
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
}
