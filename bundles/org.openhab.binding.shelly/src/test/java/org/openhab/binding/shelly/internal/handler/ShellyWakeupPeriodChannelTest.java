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
package org.openhab.binding.shelly.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.UnDefType;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyWakeupPeriodChannelTest {

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenReturn("mocked");
        new ShellyChannelDefinitions(messages);
    }

    @ParameterizedTest
    @CsvSource({ "shellyplusht, m, 720, 43200, true", "shellyht, h, 12, 43200, true",
            "shellyblubutton, m, 730, 43800, false", "shellytrv, h, 12, 0, false" })
    void channelFollowsConfiguredWakeupPeriod(String thingTypeId, String unit, int period, int expectedSeconds,
            boolean expectedChannel) {
        ShellySensorSleepMode sleepMode = new ShellySensorSleepMode();
        sleepMode.unit = unit;
        sleepMode.period = period;
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID(BINDING_ID, thingTypeId));
        profile.settings.sleepMode = sleepMode;

        profile.updateWatchdogPeriod();

        assertThat(profile.wakeupPeriod, is(expectedSeconds));
        assertThat(hasChannel(thingTypeId, profile), is(expectedChannel));
    }

    @ParameterizedTest
    @CsvSource({ "shellyplus1", "shellyplusht" })
    void noChannelWithoutSleepMode(String thingTypeId) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID(BINDING_ID, thingTypeId));

        assertThat(hasChannel(thingTypeId, profile), is(false));
    }

    @Test
    void channelGoesUndefWhenWakeupPeriodIsGone() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(new ShellyDeviceProfile(new ThingTypeUID(BINDING_ID, "shellyplusht")));

        ShellyComponents.updateWakeupPeriod(handler);

        verify(handler).updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP_PERIOD, UnDefType.UNDEF);
    }

    private static boolean hasChannel(String thingTypeId, ShellyDeviceProfile profile) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(BINDING_ID, thingTypeId, "test"));
        return ShellyChannelDefinitions.createDeviceChannels(thing, profile, new ShellySettingsStatus())
                .containsKey(CHANNEL_GROUP_DEV_STATUS + "#" + CHANNEL_DEVST_WAKEUP_PERIOD);
    }
}
