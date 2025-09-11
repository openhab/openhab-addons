/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.devices.energy_socket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardHandlerTest;
import org.openhab.binding.homewizard.internal.dto.DataUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for the HomeWizard Handler
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardEnergySocketHandlerTest extends HomeWizardHandlerTest {

    private static Thing mockThing(boolean legacy) {
        final Thing thing = mock(Thing.class);
        if (legacy) {
            when(thing.getUID()).thenReturn(
                    new ThingUID(HomeWizardBindingConstants.THING_TYPE_ENERGY_SOCKET, "homewizard-test-thing-skt"));
            when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_ENERGY_SOCKET);
        } else {
            when(thing.getUID()).thenReturn(
                    new ThingUID(HomeWizardBindingConstants.THING_TYPE_HWE_SKT, "homewizard-test-thing-skt"));
            when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_HWE_SKT);
        }
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_APPARENT_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_FACTOR), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_FREQUENCY), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_SWITCH), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_LOCK), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS),

                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T1),
                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T1),
                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_POWER),
                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE),
                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static HomeWizardEnergySocketHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final HomeWizardEnergySocketHandlerMock handler = spy(new HomeWizardEnergySocketHandlerMock(thing));

        try {
            doReturn(DataUtil.fromFile("response-device-information-energy-socket.json")).when(handler)
                    .getDeviceInformationData();
            doReturn(DataUtil.fromFile("response-measurement-energy-socket.json")).when(handler).getMeasurementData();
            doReturn(DataUtil.fromFile("response-state-energy-socket.json")).when(handler).getStateData();
        } catch (Exception e) {
            assertFalse(true);
        }

        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    @Test
    public void testUpdateChannels() {
        final Thing thing = mockThing(false);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final HomeWizardEnergySocketHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT),
                    getState(30.511, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT),
                    getState(85.951, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER),
                    getState(543.312, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE),
                    getState(231.539, Units.VOLT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT),
                    getState(2.346, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER),
                    getState(123.456, Units.VAR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_APPARENT_POWER),
                    getState(666.768, Units.VOLT_AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_FACTOR),
                    getState(0.81688));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_FREQUENCY),
                    getState(50.005, Units.HERTZ));
            verify(callback)
                    .stateUpdated(new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL
                            + "#" + HomeWizardBindingConstants.CHANNEL_POWER_SWITCH), getState(true));
            verify(callback)
                    .stateUpdated(new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL
                            + "#" + HomeWizardBindingConstants.CHANNEL_POWER_LOCK), getState(false));
            verify(callback)
                    .stateUpdated(new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_SKT_CONTROL
                            + "#" + HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS), getState(100.0));

        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUpdateLegacyChannels() {
        final Thing thing = mockThing(true);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final HomeWizardEnergySocketHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T1),
                    getState(30.511, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T1),
                    getState(85.951, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_POWER),
                    getState(543.312, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE),
                    getState(231.539, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_POWER_SWITCH), getState(true));
            verify(callback).stateUpdated(new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_POWER_LOCK),
                    getState(false));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS),
                    new DecimalType(100.0));
        } finally {
            handler.dispose();
        }
    }
}
