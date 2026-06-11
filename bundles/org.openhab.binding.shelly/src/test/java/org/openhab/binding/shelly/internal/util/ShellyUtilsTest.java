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
}
