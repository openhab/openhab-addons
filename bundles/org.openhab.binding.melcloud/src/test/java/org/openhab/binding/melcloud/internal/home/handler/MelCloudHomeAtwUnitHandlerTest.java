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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_FORCED_HOTWATERMODE;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_ROOM_TEMPERATURE_ZONE2;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_HOME_SET_TEMPERATURE_ZONE1;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ACCOUNT;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ATW_UNIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
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
}
