/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.homewizard.internal.dto.DataUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * Tests for the HomeWizard Handler
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class HomeWizardP1MeterHandlerTest {

    private static final Configuration CONFIG = createConfig();

    private static Configuration createConfig() {
        final Configuration config = new Configuration();
        config.put("ipAddress", "1.2.3.4");
        return config;
    }

    private static Thing mockThing() {
        final Thing thing = mock(Thing.class);
        when(thing.getUID())
                .thenReturn(new ThingUID(HomeWizardBindingConstants.THING_TYPE_P1_METER, "homewizard-test-thing"));
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_POWER_FAILURES), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GAS_TOTAL));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    private static HomeWizardP1MeterHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        doReturn(ZoneId.systemDefault()).when(timeZoneProvider).getTimeZone();
        final HomeWizardP1MeterHandlerMock handler = spy(new HomeWizardP1MeterHandlerMock(thing, timeZoneProvider));

        try {
            doReturn(DataUtil.fromFile("response.json")).when(handler).getData();
        } catch (IOException e) {
            assertFalse(true);
        }

        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private static State getState(final int input) {
        return new DecimalType(input);
    }

    private static State getState(final int input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    private static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    @Test
    public void testUpdateChannels() {
        final Thing thing = mockThing();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final HomeWizardP1MeterHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT),
                    getState(567.0, Units.AMPERE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L1),
                    getState(-4.0, Units.AMPERE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L2),
                    getState(2.0, Units.AMPERE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_CURRENT_L3),
                    getState(333.0, Units.AMPERE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER),
                    getState(-543, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L1),
                    getState(-676, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L2),
                    getState(133, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L3),
                    getState(18, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE),
                    getState(220, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L1),
                    getState(221, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L2),
                    getState(222, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_VOLTAGE_L3),
                    getState(223, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1),
                    getState(8874.0, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2),
                    getState(7788.0, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1),
                    getState(10830.511, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2),
                    getState(2948.827, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_POWER_FAILURES), getState(7));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES),
                    getState(2));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP),
                    new DateTimeType(ZonedDateTime.of(2021, 6, 06, 14, 0, 10, 0, ZoneId.systemDefault())));
            verify(callback).stateUpdated(new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GAS_TOTAL),
                    getState(2569.646, SIUnits.CUBIC_METRE));
        } finally {
            handler.dispose();
        }
    }
}
