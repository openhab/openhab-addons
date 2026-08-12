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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_RSSI;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ACCOUNT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATW_UNIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeRequestPacer;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwControlRequest;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeAtwUnit;
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

    // ADR-009: a value absent from the settings array (or a null top-level field) must be pushed as UnDefType.UNDEF,
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
}
