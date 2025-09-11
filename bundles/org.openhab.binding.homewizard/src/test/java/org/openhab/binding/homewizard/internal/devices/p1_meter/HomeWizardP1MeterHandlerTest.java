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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.devices.HomeWizardHandlerTest;
import org.openhab.binding.homewizard.internal.dto.DataUtil;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.unit.SIUnits;
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
 * @author Leo Siepel - Initial contribution
 * @author Gearrel Welvaart - Added tests
 */
@NonNullByDefault
public class HomeWizardP1MeterHandlerTest extends HomeWizardHandlerTest {

    private static Thing mockThing(boolean legacy) {
        final Thing thing = mock(Thing.class);
        if (legacy) {
            when(thing.getUID()).thenReturn(
                    new ThingUID(HomeWizardBindingConstants.THING_TYPE_P1_METER, "homewizard-test-thing-p1"));
            when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_P1_METER);
        } else {
            when(thing.getUID())
                    .thenReturn(new ThingUID(HomeWizardBindingConstants.THING_TYPE_HWE_P1, "homewizard-test-thing-p1"));
            when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_HWE_P1);
        }
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_FAILURES), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_GAS_TOTAL));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static HomeWizardP1MeterHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        doReturn(ZoneId.systemDefault()).when(timeZoneProvider).getTimeZone();
        final HomeWizardP1MeterHandlerMock handler = spy(new HomeWizardP1MeterHandlerMock(thing, timeZoneProvider));

        try {
            doReturn(DataUtil.fromFile("response-device-information-p1-meter.json")).when(handler)
                    .getDeviceInformationData();
            doReturn(DataUtil.fromFile("response-measurement-p1-meter.json")).when(handler).getMeasurementData();
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
        final HomeWizardP1MeterHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT_L1),
                    getState(-4.0, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT_L2),
                    getState(2.0, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT_L3),
                    getState(333.0, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER),
                    getState(-543, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_L1),
                    getState(-676, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_L2),
                    getState(133, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_L3),
                    getState(18, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE_L1),
                    getState(221, Units.VOLT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE_L2),
                    getState(222, Units.VOLT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE_L3),
                    getState(223, Units.VOLT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT),
                    getState(8877.0, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1),
                    getState(8874, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2),
                    getState(7788, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT),
                    getState(13779.338, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1),
                    getState(10830.511, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2),
                    getState(2948.827, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_FAILURES),
                    getState(7));
            verify(callback).stateUpdated(
                    getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_LONG_POWER_FAILURES), getState(2));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP),
                    new DateTimeType(ZonedDateTime.of(2021, 6, 06, 14, 0, 10, 0, ZoneId.systemDefault())));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_GAS_TOTAL),
                    getState(2569.646, SIUnits.CUBIC_METRE));
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUpdateLegacyChannels() {
        final Thing thing = mockThing(true);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final HomeWizardP1MeterHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T1),
                    getState(10830.511, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_IMPORT_T2),
                    getState(2948.827, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T1),
                    getState(8874.0, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ENERGY_EXPORT_T2),
                    getState(7788.0, Units.KILOWATT_HOUR));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_POWER),
                    getState(-543, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L1),
                    getState(-676, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L2),
                    getState(133, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_POWER_L3),
                    getState(18, Units.WATT));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L1),
                    getState(221, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L2),
                    getState(222, Units.VOLT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_VOLTAGE_L3),
                    getState(223, Units.VOLT));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L1),
                    getState(-4.0, Units.AMPERE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L2),
                    getState(2.0, Units.AMPERE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_CURRENT_L3),
                    getState(333.0, Units.AMPERE));

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
