/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class PollerChannelStateTest {

    static Stream<Arguments> failures() {
        return Stream.of(Arguments.of(PollerReadFailurePolicy.UNDEF, UnDefType.UNDEF),
                Arguments.of(PollerReadFailurePolicy.KEEP_LAST, new DecimalType("12.5")));
    }

    @ParameterizedTest
    @MethodSource("failures")
    public void appliesTheConfiguredReadFailurePolicy(PollerReadFailurePolicy policy, Object expectedState) {
        PollerChannelState state = new PollerChannelState(policy);
        state.acceptSuccessfulState(new DecimalType("12.5"));

        state.acceptReadFailure();

        assertEquals(expectedState, state.currentState());
    }

    @ParameterizedTest
    @MethodSource("failures")
    public void replacesFailureStateWithNextValidState(PollerReadFailurePolicy policy, Object ignored) {
        PollerChannelState state = new PollerChannelState(policy);
        state.acceptSuccessfulState(new DecimalType("12.5"));
        state.acceptReadFailure();

        state.acceptSuccessfulState(new DecimalType("-273.15"));

        assertEquals(new DecimalType("-273.15"), state.currentState());
    }

    @ParameterizedTest
    @MethodSource("failures")
    public void neverUsesNullToRepresentAReadFailure(PollerReadFailurePolicy policy, Object ignored) {
        PollerChannelState state = new PollerChannelState(policy);

        state.acceptReadFailure();

        assertEquals(UnDefType.UNDEF, state.currentState());
    }
}
