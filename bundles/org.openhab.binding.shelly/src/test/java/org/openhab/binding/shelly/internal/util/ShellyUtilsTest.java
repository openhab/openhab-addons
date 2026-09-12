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
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.MEDIA_VOLUME_STEPSIZE;

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

    @ParameterizedTest
    @MethodSource("provideTestCasesForMediaVolumeToPercent")
    void mediaVolumeToPercent(int volume, int expectedPercent) {
        assertEquals(expectedPercent, ShellyUtils.mediaVolumeToPercent(volume));
    }

    private static Stream<Arguments> provideTestCasesForMediaVolumeToPercent() {
        return Stream.of(Arguments.of(0, 0), Arguments.of(1, 10), Arguments.of(5, 50), Arguments.of(10, 100),
                Arguments.of(-1, 0), Arguments.of(11, 100));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForPercentToMediaVolume")
    void percentToMediaVolume(int percent, int expectedVolume) {
        assertEquals(expectedVolume, ShellyUtils.percentToMediaVolume(percent));
    }

    private static Stream<Arguments> provideTestCasesForPercentToMediaVolume() {
        return Stream.of(Arguments.of(0, 0), Arguments.of(4, 0), Arguments.of(5, 1), Arguments.of(50, 5),
                Arguments.of(55, 6), Arguments.of(100, 10), Arguments.of(-5, 0), Arguments.of(120, 10));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForVolumeStepRoundTrip")
    void volumeStepChangesTheDeviceVolumeInBothDirections(int volume) {
        int percent = ShellyUtils.mediaVolumeToPercent(volume);

        assertEquals(volume, ShellyUtils.percentToMediaVolume(percent));
        assertEquals(volume + 1, ShellyUtils.percentToMediaVolume(percent + MEDIA_VOLUME_STEPSIZE));
        assertEquals(volume - 1, ShellyUtils.percentToMediaVolume(percent - MEDIA_VOLUME_STEPSIZE));
    }

    private static Stream<Arguments> provideTestCasesForVolumeStepRoundTrip() {
        return Stream.of(Arguments.of(1), Arguments.of(5), Arguments.of(9));
    }
}
