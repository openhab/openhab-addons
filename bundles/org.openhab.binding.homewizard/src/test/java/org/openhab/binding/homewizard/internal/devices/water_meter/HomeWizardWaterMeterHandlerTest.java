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
package org.openhab.binding.homewizard.internal.devices.water_meter;

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
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardWaterMeterHandlerTest extends HomeWizardHandlerTest {

    private static Thing mockThing(boolean legacy) {
        final Thing thing = mock(Thing.class);

        if (legacy) {
            when(thing.getUID()).thenReturn(
                    new ThingUID(HomeWizardBindingConstants.THING_TYPE_WATERMETER, "homewizard-test-thing-wtr"));
            when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_WATERMETER);
        } else {
            when(thing.getUID()).thenReturn(
                    new ThingUID(HomeWizardBindingConstants.THING_TYPE_HWE_WTR, "homewizard-test-thing-wtr"));
            when(thing.getThingTypeUID()).thenReturn(HomeWizardBindingConstants.THING_TYPE_HWE_WTR);
        }
        when(thing.getUID())
                .thenReturn(new ThingUID(HomeWizardBindingConstants.THING_TYPE_HWE_WTR, "homewizard-test-thing-wtr"));
        when(thing.getConfiguration()).thenReturn(CONFIG_V1);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(),
                        HomeWizardBindingConstants.CHANNEL_GROUP_WATER + "#"
                                + HomeWizardBindingConstants.CHANNEL_ACTIVE_LITER),
                mockChannel(thing.getUID(),
                        HomeWizardBindingConstants.CHANNEL_GROUP_WATER + "#"
                                + HomeWizardBindingConstants.CHANNEL_TOTAL_LITER),

                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_TOTAL_LITER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ACTIVE_LITER));

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static HomeWizardWaterMeterHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final HomeWizardWaterMeterHandlerMock handler = spy(new HomeWizardWaterMeterHandlerMock(thing));

        try {
            doReturn(DataUtil.fromFile("response-device-information-water-meter.json")).when(handler)
                    .getDeviceInformationData();
            doReturn(DataUtil.fromFile("response-measurement-water-meter.json")).when(handler).getMeasurementData();
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
        final HomeWizardWaterMeterHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            HomeWizardBindingConstants.CHANNEL_GROUP_WATER + "#"
                                    + HomeWizardBindingConstants.CHANNEL_ACTIVE_LITER),
                    getState(7.2, Units.LITRE_PER_MINUTE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(),
                            HomeWizardBindingConstants.CHANNEL_GROUP_WATER + "#"
                                    + HomeWizardBindingConstants.CHANNEL_TOTAL_LITER),
                    getState(123.456, SIUnits.CUBIC_METRE));

        } finally {
            handler.dispose();
        }
    }

    @Test
    public void testUpdateLegacyChannels() {
        final Thing thing = mockThing(true);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final HomeWizardWaterMeterHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_ACTIVE_LITER),
                    getState(7.2, Units.LITRE_PER_MINUTE));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.LEGACY_CHANNEL_TOTAL_LITER),
                    getState(123.456, SIUnits.CUBIC_METRE));

        } finally {
            handler.dispose();
        }
    }
}
