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
package org.openhab.binding.shelly.internal.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiLightUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;

/**
 * Tests for {@link ShellyApiLightUtil}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiLightUtilTest {
    @ParameterizedTest
    @MethodSource("provideTestCasesForIsComponentPredicates")
    void isComponentPredicatesMatchOnlyTheirOwnTag(ShellyLightApiComponent tag, boolean isColor, boolean isRgb,
            boolean isRgbw, boolean isCct, boolean isLight) {
        assertEquals(isColor, isColorComponent(tag), "isColorComponent(" + tag + ")");
        assertEquals(isRgb, isRgbComponent(tag), "isRgbComponent(" + tag + ")");
        assertEquals(isRgbw, isRgbwComponent(tag), "isRgbwComponent(" + tag + ")");
        assertEquals(isCct, isCctComponent(tag), "isCctComponent(" + tag + ")");
        assertEquals(isLight, isLightComponent(tag), "isLightComponent(" + tag + ")");
    }

    private static Stream<Arguments> provideTestCasesForIsComponentPredicates() {
        return Stream.of( //
                Arguments.of(ShellyLightApiComponent.RGB, true, true, false, false, false), //
                Arguments.of(ShellyLightApiComponent.RGBW, true, false, true, false, false), //
                Arguments.of(ShellyLightApiComponent.CCT, false, false, false, true, false), //
                Arguments.of(ShellyLightApiComponent.LIGHT, false, false, false, false, true), //
                Arguments.of(ShellyLightApiComponent.NONE, false, false, false, false, false));
    }

    @Test
    void tagAtReturnsNoneForNullList() {
        assertEquals(ShellyLightApiComponent.NONE, tagAt(null, 0));
    }

    @Test
    void tagAtReturnsNoneForOutOfRangeIndex() {
        List<ShellySettingsRgbwLight> lights = List.of(taggedLight(ShellyLightApiComponent.RGB));
        assertEquals(ShellyLightApiComponent.NONE, tagAt(lights, 1));
        assertEquals(ShellyLightApiComponent.NONE, tagAt(lights, -1));
    }

    @Test
    void tagAtReturnsNoneForUntaggedEntry() {
        List<ShellySettingsRgbwLight> lights = List.of(new ShellySettingsRgbwLight());
        assertEquals(ShellyLightApiComponent.NONE, tagAt(lights, 0));
    }

    @Test
    void tagAtReturnsTaggedEntry() {
        List<ShellySettingsRgbwLight> lights = List.of(taggedLight(ShellyLightApiComponent.RGB),
                taggedLight(ShellyLightApiComponent.CCT));
        assertEquals(ShellyLightApiComponent.CCT, tagAt(lights, 1));
    }

    @Test
    void hasColorComponentFalseWhenNoEntryIsColor() {
        List<ShellySettingsRgbwLight> lights = List.of(taggedLight(ShellyLightApiComponent.CCT),
                taggedLight(ShellyLightApiComponent.LIGHT));
        assertFalse(hasColorComponent(lights));
    }

    @Test
    void hasColorComponentTrueWhenAnyEntryIsColor() {
        List<ShellySettingsRgbwLight> lights = List.of(taggedLight(ShellyLightApiComponent.CCT),
                taggedLight(ShellyLightApiComponent.RGBW));
        assertTrue(hasColorComponent(lights));
    }

    @Test
    void getLightIdFromGroupParsesCurrentLightPrefix() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        assertEquals(2, getLightIdFromGroup(CHANNEL_GROUP_LIGHT_INDEX + "3", profile));
    }

    @Test
    void getLightIdFromGroupParsesDeprecatedChannelPrefix() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        assertEquals(1, getLightIdFromGroup(CHANNEL_GROUP_LIGHT_CHANNEL + "2", profile));
    }

    @Test
    void getLightIdFromGroupReturnsZeroForUnrelatedGroup() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        assertEquals(0, getLightIdFromGroup(CHANNEL_GROUP_LIGHT_CONTROL, profile));
    }

    @Test
    void getLightIdFromGroupShiftsByColorComponentCountOnHybridRgbcctProfile() {
        // rgbcct: settings.lights[0] is the RGB color component (bare "control"), settings.lights[1] is CCT:0
        // (indexed "light1") - the group number alone is one short of the flat index.
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPRORGBWWPM);
        profile.inColor = true;

        assertEquals(1, getLightIdFromGroup(CHANNEL_GROUP_LIGHT_INDEX + "1", profile));
    }

    @Test
    void getLightIdFromGroupShiftsByColorComponentCountOnHybridRgbx2lightProfile() {
        // rgbx2light: settings.lights[0] is RGB, [1] is Light:0 ("light1"), [2] is Light:1 ("light2").
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPRORGBWWPM);
        profile.inColor = true;

        assertEquals(1, getLightIdFromGroup(CHANNEL_GROUP_LIGHT_INDEX + "1", profile));
        assertEquals(2, getLightIdFromGroup(CHANNEL_GROUP_LIGHT_INDEX + "2", profile));
    }

    @Test
    void lightChannelGroupPrefixUsesLegacyChannelGroupForUnmigratedGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.hasLegacyLightChannels = true;

        assertEquals(CHANNEL_GROUP_LIGHT_CHANNEL, lightChannelGroupPrefix(profile));
    }

    @Test
    void lightChannelGroupPrefixUsesLightIndexForFreshlyDiscoveredGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.hasLegacyLightChannels = false;

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX, lightChannelGroupPrefix(profile));
    }

    @Test
    void lightChannelGroupPrefixUsesLightIndexForGen2RegardlessOfLegacyFlag() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        profile.hasLegacyLightChannels = true;

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX, lightChannelGroupPrefix(profile));
    }

    @Test
    void buildWhiteGroupNameUsesWhiteControlForBulb() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);

        assertEquals(CHANNEL_GROUP_WHITE_CONTROL, buildWhiteGroupName(profile, 0));
    }

    @Test
    void buildWhiteGroupNameUsesWhiteControlForDuo() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYDUO);

        assertEquals(CHANNEL_GROUP_WHITE_CONTROL, buildWhiteGroupName(profile, 0));
    }

    @Test
    void buildWhiteGroupNameDelegatesToControlGroupForGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.settings.lights = new ArrayList<>(
                List.of(new ShellySettingsRgbwLight(), new ShellySettingsRgbwLight(), new ShellySettingsRgbwLight()));

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX + "3", buildWhiteGroupName(profile, 2));
    }

    @Test
    void buildWhiteGroupNameDelegatesToControlGroupForGen2RgbwPm() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        profile.settings.lights = new ArrayList<>(
                List.of(new ShellySettingsRgbwLight(), new ShellySettingsRgbwLight(), new ShellySettingsRgbwLight()));

        assertThat(buildWhiteGroupName(profile, 2), is(equalTo(CHANNEL_GROUP_LIGHT_INDEX + "3")));
    }

    private static ShellySettingsRgbwLight taggedLight(ShellyLightApiComponent apiComponent) {
        ShellySettingsRgbwLight light = new ShellySettingsRgbwLight();
        light.apiComponent = apiComponent;
        return light;
    }
}
