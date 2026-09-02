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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Tests {@link ShellyChannelDefinitions#createLightChannels}, in particular the whiteGroup
 * selection that routes brightness/colorTemp channels to either the shared white-control group
 * (Bulb/Duo) or the per-light indexed group (RGBW2/RGBW PM in white mode).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelDefinitionsLightTest {

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

    private static ShellySettingsRgbwLight newLight() {
        ShellySettingsRgbwLight light = new ShellySettingsRgbwLight();
        light.name = "";
        return light;
    }

    @Test
    void gen1Rgbw2WhiteModeRoutesBrightnessToIndexedGroup() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.inColor = false;
        profile.settings.lights = new ArrayList<>(java.util.List.of(newLight(), newLight(), newLight(), newLight()));

        ShellyStatusLightChannel status = new ShellyStatusLightChannel();
        status.brightness = 42;

        Map<String, Channel> created = ShellyChannelDefinitions.createLightChannels(mockThing("shellyrgbw2-white"),
                profile, status, 1);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_LIGHT_INDEX + "2", CHANNEL_BRIGHTNESS)));
        assertFalse(created.containsKey(mkChannelId(CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_BRIGHTNESS)));
    }

    @Test
    void gen1Rgbw2WhiteModeWithTempCreatesColorTempChannelSharedWithProRgbwwPmCctx2Profile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.inColor = false;
        profile.settings.lights = new ArrayList<>(java.util.List.of(newLight(), newLight(), newLight(), newLight()));

        ShellyStatusLightChannel status = new ShellyStatusLightChannel();
        status.temp = 4500;

        Map<String, Channel> created = ShellyChannelDefinitions.createLightChannels(mockThing("shellyrgbw2-white"),
                profile, status, 1);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_LIGHT_INDEX + "2", CHANNEL_COLOR_TEMP)));
    }

    @Test
    void gen2RgbwPmWhiteModeRoutesBrightnessToIndexedGroup() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        profile.inColor = false;
        profile.settings.lights = new ArrayList<>(java.util.List.of(newLight(), newLight(), newLight(), newLight()));

        ShellyStatusLightChannel status = new ShellyStatusLightChannel();
        status.brightness = 77;

        Map<String, Channel> created = ShellyChannelDefinitions.createLightChannels(mockThing("shellyplusrgbwpm"),
                profile, status, 0);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_LIGHT_INDEX + "1", CHANNEL_BRIGHTNESS)));
        assertFalse(created.containsKey(mkChannelId(CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_BRIGHTNESS)));
    }

    @Test
    void bulbWhiteModeRoutesBrightnessAndColorTempToSharedWhiteGroup() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);
        profile.inColor = false;
        profile.settings.lights = new ArrayList<>(java.util.List.of(newLight()));

        ShellyStatusLightChannel status = new ShellyStatusLightChannel();
        status.brightness = 50;
        status.temp = 4500;

        Map<String, Channel> created = ShellyChannelDefinitions.createLightChannels(mockThing("shellybulb"), profile,
                status, 0);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_BRIGHTNESS)));
        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_COLOR_TEMP)));
    }

    @Test
    void indexedLightGroupHasColorTempDefinitionForProRgbwwPmCctx2Profile() {
        assertDoesNotThrow(
                () -> ShellyChannelDefinitions.getDefinition(CHANNEL_GROUP_LIGHT_INDEX + "1#" + CHANNEL_COLOR_TEMP));
    }
}
