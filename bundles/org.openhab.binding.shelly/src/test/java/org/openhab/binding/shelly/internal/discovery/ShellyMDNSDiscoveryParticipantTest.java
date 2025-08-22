/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link ShellyMDNSDiscoveryParticipantTest}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShellyMDNSDiscoveryParticipantTest {

    @ParameterizedTest
    @MethodSource("provideTestCasesForIsValidShellyServiceName")
    void isValidShellyServiceName(String serviceName, boolean expected) {
        assertThat("serviceName: " + serviceName, ShellyMDNSDiscoveryParticipant.isValidShellyServiceName(serviceName),
                is(expected));
    }

    private static Stream<Arguments> provideTestCasesForIsValidShellyServiceName() {
        return Stream.of( //
                Arguments.of("shellypmmini-123456789012", true), //
                Arguments.of("ShellyPlusPMMini-Test", true), //
                Arguments.of("shelly1-ABC", true), //
                Arguments.of("ShellyOne-001", true), //
                Arguments.of("MyShelly-001", true), //
                Arguments.of("my-shelly", false), //
                Arguments.of("shelly_one-001", false), //
                Arguments.of("shelly-", false), //
                Arguments.of("shelly 1-001", false), //
                Arguments.of("shelly1-001!", false), //
                Arguments.of("shell-001", false), //
                Arguments.of("ShellyPlusPMMini", false), //
                Arguments.of("ShellyPlusPMMini - Test", false));
    }
}
