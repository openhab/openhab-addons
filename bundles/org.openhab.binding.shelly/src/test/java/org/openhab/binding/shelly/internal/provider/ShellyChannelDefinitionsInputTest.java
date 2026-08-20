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
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_BTNT_MOMENTARY;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Tests {@link ShellyChannelDefinitions#createInputChannels}, in particular the lastEvent/eventCount
 * channel gating for button-mode inputs.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelDefinitionsInputTest {

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

    @Test
    void gen2DimmerButtonModeInputCreatesEventChannelsBeforeFirstButtonPress() {
        // #19226 (ijasan): on a Plus/Pro Dimmer G3, relay#lastEvent1/eventCount1 were missing after
        // the initial channel creation because the input's event/eventCount are still null in the
        // very first status snapshot (the button hasn't been pressed yet) - createInputChannels()
        // must create these channels once the input is in button mode, not only once a value exists.
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDIMMER);
        profile.numInputs = 1;

        ShellySettingsDimmer dimmer = new ShellySettingsDimmer();
        profile.settings.dimmers = new ArrayList<>();
        profile.settings.dimmers.add(dimmer);

        ShellySettingsInput input = new ShellySettingsInput();
        input.btnType = SHELLY_BTNT_MOMENTARY;
        profile.settings.inputs = new ArrayList<>();
        profile.settings.inputs.add(input);

        ShellySettingsStatus status = new ShellySettingsStatus();
        ShellyInputState inputState = new ShellyInputState();
        inputState.input = 0;
        // event/eventCount deliberately left null: no button press reported yet
        status.inputs = new ArrayList<>();
        status.inputs.add(inputState);

        Map<String, Channel> created = ShellyChannelDefinitions.createInputChannels(mockThing("shellyplusdimmer"),
                profile, status);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_RELAY_CONTROL, CHANNEL_STATUS_EVENTTYPE + "1")));
        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_RELAY_CONTROL, CHANNEL_STATUS_EVENTCOUNT + "1")));
    }
}
