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
package org.openhab.binding.homewizard.internal.devices.kwh_meter;

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
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
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
public class HomeWizardKwhMeterHandlerTest extends HomeWizardHandlerTest {

    private static Thing mockThing(boolean legacy) {
        final Thing thing = mock(Thing.class);
        when(thing.getUID())
                .thenReturn(new ThingUID(HomeWizardBindingConstants.THING_TYPE_HWE_SKT, "homewizard-test-thing-skt"));
        when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_HWE_KWH);
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_POWER_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_CURRENT_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L2), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_VOLTAGE_L3), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_APPARENT_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
                        HomeWizardBindingConstants.CHANNEL_FREQUENCY)

        );

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static HomeWizardKwhMeterHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final HomeWizardKwhMeterHandlerMock handler = spy(new HomeWizardKwhMeterHandlerMock(thing));

        try {
            doReturn(DataUtil.fromFile("response-device-information-kwh3-meter.json")).when(handler)
                    .getDeviceInformationData();
            doReturn(DataUtil.fromFile("response-measurement-kwh-meter.json")).when(handler).getMeasurementData();
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
        final HomeWizardKwhMeterHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT),
                    getState(2940.101, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT),
                    getState(0.0, Units.KILOWATT_HOUR));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER),
                    getState(-543.0, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_L1),
                    getState(0.0, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_L2),
                    getState(3547.015, Units.WATT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_POWER_L3),
                    getState(3553.263, Units.WATT));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT),
                    getState(30.999, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT_L1),
                    getState(0.0, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT_L2),
                    getState(15.521, Units.AMPERE));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_CURRENT_L3),
                    getState(15.477, Units.AMPERE));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE_L1),
                    getState(230.751, Units.VOLT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE_L2),
                    getState(228.391, Units.VOLT));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_VOLTAGE_L3),
                    getState(229.612, Units.VOLT));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER),
                    getState(-429.025, Units.VAR));
            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_APPARENT_POWER),
                    getState(7112.293, Units.VOLT_AMPERE));

            verify(callback).stateUpdated(getEnergyChannelUid(thing, HomeWizardBindingConstants.CHANNEL_FREQUENCY),
                    getState(49.926, Units.HERTZ));

        } finally {
            handler.dispose();
        }
    }
}
