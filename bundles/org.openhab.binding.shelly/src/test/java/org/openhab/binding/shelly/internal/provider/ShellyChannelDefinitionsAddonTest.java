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
package org.openhab.binding.shelly.internal.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtSwitchSettings;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtSwitchSettings.ShellyExtSwitchSettingsInput;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Tests {@link ShellyChannelDefinitions#createRelayChannels}, in particular the Sensor Addon
 * external-switch channel (issue #18913: a temperature addon wired as a standalone reed contact,
 * {@code relay_num: -1}, never got its input channel created).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelDefinitionsAddonTest {

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    private static Thing mockThing(String thingTypeId) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("shelly", thingTypeId, "test"));
        return thing;
    }

    private static ShellyDeviceProfile newRelayProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLY1);
        profile.hasRelays = true;
        profile.numRelays = 1;
        profile.settings.relays = new ArrayList<>(List.of(new ShellySettingsRelay()));
        return profile;
    }

    private static ShellySettingsRelay relay0(ShellyDeviceProfile profile) {
        return Objects.requireNonNull(profile.settings.relays).get(0);
    }

    private static ShellyDeviceProfile newProfileWithExtSwitch(int relayNum) {
        ShellyDeviceProfile profile = newRelayProfile();
        ShellyExtSwitchSettings extSwitch = new ShellyExtSwitchSettings();
        extSwitch.input0 = new ShellyExtSwitchSettingsInput();
        extSwitch.input0.relayNum = relayNum;
        profile.settings.extSwitch = extSwitch;
        return profile;
    }

    @Test
    void standaloneExtSwitchWithRelayNumMinusOneCreatesInputChannel() {
        ShellyDeviceProfile profile = newProfileWithExtSwitch(-1);

        Map<String, Channel> created = ShellyChannelDefinitions.createRelayChannels(mockThing("shelly1"), profile,
                relay0(profile), 0);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_INPUT1)));
    }

    @Test
    void extSwitchBoundToRelayZeroCreatesInputChannel() {
        ShellyDeviceProfile profile = newProfileWithExtSwitch(0);

        Map<String, Channel> created = ShellyChannelDefinitions.createRelayChannels(mockThing("shelly1"), profile,
                relay0(profile), 0);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_INPUT1)));
    }

    @Test
    void noExtSwitchCreatesNoInputChannel() {
        ShellyDeviceProfile profile = newRelayProfile();

        Map<String, Channel> created = ShellyChannelDefinitions.createRelayChannels(mockThing("shelly1"), profile,
                relay0(profile), 0);

        assertFalse(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_INPUT1)));
    }
}
