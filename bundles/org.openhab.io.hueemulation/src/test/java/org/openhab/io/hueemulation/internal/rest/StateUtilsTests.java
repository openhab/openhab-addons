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
package org.openhab.io.hueemulation.internal.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.PercentType;
import org.openhab.io.hueemulation.internal.StateUtils;

/**
 * Tests for {@link StateUtils}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class StateUtilsTests {

    @ParameterizedTest
    @MethodSource("provideTestCasesForPercentTypeFromHueBrightness")
    void percentTypeFromHueBrightness(int bri, PercentType expectedPercent) {
        assertThat(StateUtils.percentTypeFromHueBrightness(bri), is(equalTo(expectedPercent)));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForHueBrightnessFromPercentType")
    void hueBrightnessFromPercentType(PercentType percentValue, int expectedBri) {
        assertThat(StateUtils.hueBrightnessFromPercentType(percentValue), is(equalTo(expectedBri)));
    }

    private static Stream<Arguments> provideTestCasesForPercentTypeFromHueBrightness() {
        return Stream.of( //
                Arguments.of(0, PercentType.ZERO), //
                Arguments.of(1, PercentType.valueOf("0.39")), //
                Arguments.of(2, PercentType.valueOf("0.79")), //
                Arguments.of(3, PercentType.valueOf("1.18")), //
                Arguments.of(127, new PercentType(50)), //
                Arguments.of(253, PercentType.valueOf("99.61")), //
                Arguments.of(254, PercentType.HUNDRED) //
        );
    }

    private static Stream<Arguments> provideTestCasesForHueBrightnessFromPercentType() {
        return Stream.of( //
                Arguments.of(PercentType.ZERO, 1), //
                Arguments.of(PercentType.valueOf("0.5"), 1), //
                Arguments.of(new PercentType(1), 3), //
                Arguments.of(new PercentType(50), 127), //
                Arguments.of(PercentType.HUNDRED, 254) //
        );
    }
}
