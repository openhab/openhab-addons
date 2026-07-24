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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_SET_TEMPERATURE;
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
 * Unit tests for {@link MelCloudHomeAtaUnitHandler} (see ADR-003).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
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
    void whenUnitIdIsMissing_thenThingGoesOffline() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler("", false, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenBridgeIsNotSet_thenThingGoesOffline() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, false, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenBridgeIsOnlineButNoUnitUpdateHasArrivedYet_thenThingStaysUnknown() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.ONLINE);

        // Act
        handler.initialize();

        // Assert
        assertEquals(ThingStatus.UNKNOWN, callback.getStatus().getStatus());
        verify(accountHandler).registerAtaUnitListener(eq(UNIT_ID), eq(handler));
    }

    @Test
    void whenUnitUpdateArrives_thenThingGoesOnlineAndChannelsAreUpdated() {
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
        assertEquals(new QuantityType<>(21.5, SIUnits.CELSIUS), callback.getState(CHANNEL_SET_TEMPERATURE));
    }

    @Test
    void whenBridgeIsOffline_thenThingGoesOffline() {
        // Arrange
        MelCloudHomeAtaUnitHandler handler = createHandler(UNIT_ID, true, ThingStatus.OFFLINE);

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenPowerOnCommandIsSent_thenControlAtaUnitIsCalledWithPowerTrue() throws Exception {
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
