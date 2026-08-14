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
package org.openhab.binding.shelly.internal.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.library.types.DateTimeType;

/**
 * Tests for {@link ShellyUtils}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShellyUtilsTest {
    @ParameterizedTest
    @MethodSource("provideTestCasesForGetTimestamp")
    void getTimestamp(String zone, long timestamp, Instant expectedInstant) {
        DateTimeType actual = ShellyUtils.getTimestamp(zone, timestamp);
        DateTimeType expected = new DateTimeType(expectedInstant);
        assertThat(actual, is(equalTo(expected)));
    }

    private static Stream<Arguments> provideTestCasesForGetTimestamp() {
        return Stream.of( //
                Arguments.of("UTC", 1772900449, Instant.parse("2026-03-07T16:20:49Z")), //
                Arguments.of("Europe/Copenhagen", 1772900449, Instant.parse("2026-03-07T15:20:49Z")), //
                Arguments.of("Europe/Copenhagen", 1783441249, Instant.parse("2026-07-07T14:20:49Z")), //
                Arguments.of("", 1772900449,
                        LocalDateTime.parse("2026-03-07T16:20:49").atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Test
    void getTimestampInvalidZoneFallsBackToNow() {
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        DateTimeType actual = ShellyUtils.getTimestamp("_invalid", 123);
        Instant actualInstant = actual.getInstant();
        Instant after = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        assertThat(actualInstant, allOf(greaterThanOrEqualTo(before), lessThanOrEqualTo(after)));
        assertThat(actualInstant.getNano(), is(0));
    }

    @Test
    void buildControlGroupNameUsesDeprecatedChannelPrefixForGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.inColor = false;
        profile.hasLegacyLightChannels = true;

        assertEquals(CHANNEL_GROUP_LIGHT_CHANNEL + "2", ShellyUtils.buildControlGroupName(profile, 2));
    }

    @Test
    void buildControlGroupNameUsesLightPrefixForNewGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.inColor = false;

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX + "2", ShellyUtils.buildControlGroupName(profile, 2));
    }

    @Test
    void buildControlGroupNameUsesLightPrefixForGen2RgbwPm() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);
        profile.inColor = false;

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX + "2", ShellyUtils.buildControlGroupName(profile, 2));
    }

    @Test
    void buildControlGroupNameFallsBackToControlWhenInColorMode() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.inColor = true;

        assertEquals(CHANNEL_GROUP_LIGHT_CONTROL, ShellyUtils.buildControlGroupName(profile, 1));
    }

    @Test
    void buildControlGroupNameFallsBackToControlForNonRgbw2Devices() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);

        assertEquals(CHANNEL_GROUP_LIGHT_CONTROL, ShellyUtils.buildControlGroupName(profile, 1));
    }

    @Test
    void buildWhiteGroupNameUsesWhiteControlForBulb() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);

        assertEquals(CHANNEL_GROUP_WHITE_CONTROL, ShellyUtils.buildWhiteGroupName(profile, 1));
    }

    @Test
    void buildWhiteGroupNameUsesWhiteControlForDuo() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYDUO);

        assertEquals(CHANNEL_GROUP_WHITE_CONTROL, ShellyUtils.buildWhiteGroupName(profile, 1));
    }

    @Test
    void buildWhiteGroupNameUsesDeprecatedChannelPrefixForGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);
        profile.hasLegacyLightChannels = true;

        assertEquals(CHANNEL_GROUP_LIGHT_CHANNEL + "3", ShellyUtils.buildWhiteGroupName(profile, 3));
    }

    @Test
    void buildWhiteGroupNameUsesLightPrefixForNewGen1Rgbw2() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYRGBW2_WHITE);

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX + "3", ShellyUtils.buildWhiteGroupName(profile, 3));
    }

    @Test
    void buildWhiteGroupNameUsesLightPrefixForGen2RgbwPm() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSRGBWPM);

        assertEquals(CHANNEL_GROUP_LIGHT_INDEX + "3", ShellyUtils.buildWhiteGroupName(profile, 3));
    }

    @Test
    void getLightIdFromGroupParsesCurrentLightPrefix() {
        assertEquals(2, ShellyUtils.getLightIdFromGroup(CHANNEL_GROUP_LIGHT_INDEX + "3"));
    }

    @Test
    void getLightIdFromGroupParsesDeprecatedChannelPrefix() {
        assertEquals(1, ShellyUtils.getLightIdFromGroup(CHANNEL_GROUP_LIGHT_CHANNEL + "2"));
    }

    @Test
    void getLightIdFromGroupReturnsZeroForUnrelatedGroup() {
        assertEquals(0, ShellyUtils.getLightIdFromGroup(CHANNEL_GROUP_LIGHT_CONTROL));
    }

    @Test
    void stripDeprecatedSuffixRemovesSwitchSuffixFromDeprecatedSplitChannel() {
        assertEquals("light1#brightness", ShellyUtils.stripDeprecatedSuffix("light1#brightness$Switch"));
    }

    @Test
    void stripDeprecatedSuffixRemovesValueSuffixFromDeprecatedSplitChannel() {
        assertEquals("light1#brightness", ShellyUtils.stripDeprecatedSuffix("light1#brightness$Value"));
    }

    @Test
    void stripDeprecatedSuffixLeavesRegularChannelIdUnchanged() {
        assertEquals("light1#brightness", ShellyUtils.stripDeprecatedSuffix("light1#brightness"));
    }
}
