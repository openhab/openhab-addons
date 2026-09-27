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
package org.openhab.binding.modbus.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2BitChannelConfigurationTest {

    static Stream<Arguments> invalidConfigurations() {
        return Stream.of(Arguments.of(Map.of(), "Channel 'coil' is missing required configuration 'address'"),
                Arguments.of(Map.of("address", "100.1"), "Channel 'coil' has invalid bit address '100.1'; expected X"),
                Arguments.of(Map.of("address", "100", "valueType", "uint16"),
                        "Channel 'coil' has invalid valueType 'uint16'; coils and discrete inputs are bits"),
                Arguments.of(Map.of("address", "99"), "Channel 'coil': Address 99 is before poll window 100..103"),
                Arguments.of(Map.of("address", "104"), "Channel 'coil': Address 104 is outside poll window 100..103"));
    }

    @ParameterizedTest
    @MethodSource("invalidConfigurations")
    public void reportsActionableBitChannelConfigurationErrors(Map<String, Object> configuration,
            String expectedMessage) {
        assertEquals(expectedMessage,
                Poller2BitChannelConfiguration.create("coil", configuration, 100, 4).error().orElseThrow());
    }

    static Stream<Arguments> validConfigurations() {
        return Stream.of(Arguments.of(Map.of("address", "100")),
                Arguments.of(Map.of("address", "100", "valueType", "bit")));
    }

    @ParameterizedTest
    @MethodSource("validConfigurations")
    public void defaultsCoilAndDiscreteChannelsToBit(Map<String, Object> configuration) {
        assertEquals(true, Poller2BitChannelConfiguration.create("coil", configuration, 100, 4).channel().isPresent());
    }
}
