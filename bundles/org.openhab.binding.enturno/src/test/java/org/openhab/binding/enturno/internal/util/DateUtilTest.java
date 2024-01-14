/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.enturno.internal.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link DateUtil}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DateUtilTest {
    @ParameterizedTest
    @MethodSource("provideTestCasesForGetIsoDateTime")
    void getIsoDateTime(String value, String expected) {
        assertThat(DateUtil.getIsoDateTime(value), is(expected));
    }

    private static Stream<Arguments> provideTestCasesForGetIsoDateTime() {
        return Stream.of( //
                Arguments.of("2023-10-25T09:01:00+0200", "2023-10-25T09:01:00+02:00"),
                Arguments.of("2023-10-25T09:01:00+02:00", "2023-10-25T09:01:00+02:00"),
                Arguments.of("2023-10-25T09:01:00-0300", "2023-10-25T09:01:00-03:00"),
                Arguments.of("2023-10-25T09:01:00+02:30", "2023-10-25T09:01:00+02:30"),
                Arguments.of("2023-10-25T09:01:00", "2023-10-25T09:01:00"));
    }
}
