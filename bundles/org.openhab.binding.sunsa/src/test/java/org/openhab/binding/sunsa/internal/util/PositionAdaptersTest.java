/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sunsa.internal.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.sunsa.internal.util.PositionAdapters.PositionAdapter;

/**
 * Tests for {@link PositionAdapters}.
 * 
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class PositionAdaptersTest {

    @MethodSource("getLocalPosition_withVariousArguments_returnsLocalizedPosition_arguments")
    @ParameterizedTest
    public void getLocalPosition_withVariousArguments_returnsLocalizedPosition(final int startPosition,
            final int endPosition, final int inputRawPosition, final int expectedPosition) {
        // given
        final PositionAdapter positionAdapter = PositionAdapters.configurablePositionAdapter(startPosition,
                endPosition);

        // when
        final int result = positionAdapter.getLocalPosition(inputRawPosition);

        // then
        assertThat(result, is(expectedPosition));
    }

    private static Stream<Arguments> getLocalPosition_withVariousArguments_returnsLocalizedPosition_arguments() {
        return Stream.of(arguments(-80, 80, -80, 0), arguments(-80, 80, 80, 100), arguments(-80, 80, 0, 50),
                arguments(-80, 80, -100, 0), arguments(-80, 80, 100, 100),
                // reversed order
                arguments(80, -80, -80, 100), arguments(80, -80, 80, 0), arguments(80, -80, 0, 50),
                arguments(80, -80, -100, 100), arguments(80, -80, 100, 0),

                arguments(0, 100, 0, 0), arguments(0, 100, 80, 80), arguments(0, 100, 50, 50),
                arguments(0, 100, 100, 100), arguments(0, 100, -50, 0),

                arguments(100, 0, 0, 100), arguments(100, 0, 80, 20), arguments(100, 0, 50, 50),
                arguments(100, 0, 100, 0), arguments(100, 0, -50, 100),

                arguments(-100, 0, 0, 100), arguments(-100, 0, -80, 20), arguments(-100, 0, -50, 50),
                arguments(-100, 0, -100, 0), arguments(-100, 0, 50, 100),

                arguments(0, -100, 0, 0), arguments(0, -100, -80, 80), arguments(0, -100, -50, 50),
                arguments(0, -100, -100, 100), arguments(0, -100, 50, 0));
    }

    @MethodSource("getRawPosition_withVariousArguments_returnsLocalizedPosition_arguments")
    @ParameterizedTest
    public void getRawPosition_withVariousArguments_returnsLocalizedPosition(final int startPosition,
            final int endPosition, final float inputInterpolation, final int expectedOutput) {
        // given
        final PositionAdapter positionAdapter = PositionAdapters.configurablePositionAdapter(startPosition,
                endPosition);

        // when
        final int result = positionAdapter.getRawPosition(inputInterpolation);

        // then
        assertThat(result, is(expectedOutput));
    }

    private static Stream<Arguments> getRawPosition_withVariousArguments_returnsLocalizedPosition_arguments() {
        return Stream.of(arguments(-80, 80, 0.0f, -80), arguments(-80, 80, 0.5f, 0), arguments(-80, 80, 1.0f, 80),

                arguments(0, 100, 0.0f, 0), arguments(0, 100, 0.5f, 50), arguments(0, 100, 1.0f, 100),

                arguments(100, 0, 0.0f, 100), arguments(100, 0, 0.5f, 50), arguments(100, 0, 1.0f, 0),

                arguments(-100, 0, 0.0f, -100), arguments(-100, 0, 0.5f, -50), arguments(-100, 0, 1.0f, 0),

                arguments(80, -80, 0.0f, 80), arguments(80, -80, 0.5f, 0), arguments(80, -80, 1.0f, -80));
    }
}
