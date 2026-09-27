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
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2ChannelConfigurationTest {

    static Stream<Arguments> invalidChannelConfigurations() {
        return Stream.of(
                Arguments.of(Map.of("valueType", "uint16"),
                        "Channel 'value' is missing required configuration 'address'"),
                Arguments.of(Map.of("address", "100"), "Channel 'value' is missing required configuration 'valueType'"),
                Arguments.of(Map.of("address", "100", "valueType", "nope"),
                        "Channel 'value' has invalid valueType 'nope'"),
                Arguments.of(Map.of("address", "100", "valueType", "uint16", "errorPolicy", "unknown"),
                        "Channel 'value' has invalid errorPolicy 'unknown'; expected 'undef' or 'keepLast'"),
                Arguments.of(Map.of("address", "103", "valueType", "int32"),
                        "Channel 'value': Address 103 with value type int32 exceeds poll window 100..103"));
    }

    @ParameterizedTest
    @MethodSource("invalidChannelConfigurations")
    public void reportsActionableConfigurationErrors(Map<String, Object> configuration, String expectedMessage) {
        assertEquals(expectedMessage,
                Poller2ChannelConfiguration.create("value", configuration, 100, 4).error().orElseThrow());
    }

    @ParameterizedTest
    @MethodSource("validErrorPolicies")
    public void appliesTheConfiguredReadFailurePolicy(String errorPolicy, Object expectedAfterFailure) {
        Poller2Channel channel = Poller2ChannelConfiguration
                .create("value", Map.of("address", "100", "valueType", "uint16", "errorPolicy", errorPolicy), 100, 4)
                .channel().orElseThrow();

        channel.acceptRegisters(new ModbusRegisterArray(42), 100);

        assertEquals(expectedAfterFailure, channel.acceptReadFailure());
    }

    static Stream<Arguments> validErrorPolicies() {
        return Stream.of(Arguments.of("undef", UnDefType.UNDEF), Arguments.of("keepLast", new DecimalType("42")));
    }
}
