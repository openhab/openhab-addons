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

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.io.transport.modbus.BitArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2BitChannelTest {

    static Stream<Arguments> absoluteBitAddresses() {
        return Stream.of(Arguments.of(100, new BitArray(true, false, false, true), new DecimalType("1")),
                Arguments.of(103, new BitArray(true, false, false, true), new DecimalType("1")),
                Arguments.of(101, new BitArray(true, false, false, true), DecimalType.ZERO));
    }

    @ParameterizedTest
    @MethodSource("absoluteBitAddresses")
    public void decodesAbsoluteCoilAndDiscreteAddresses(int address, BitArray bits, Object expectedState) {
        Poller2BitChannel channel = new Poller2BitChannel(address, PollerReadFailurePolicy.UNDEF);

        assertEquals(expectedState, channel.acceptBits(bits, 100));
    }

    static Stream<Arguments> malformedResponses() {
        return Stream.of(Arguments.of(99, new BitArray(true), UnDefType.UNDEF),
                Arguments.of(101, new BitArray(true), UnDefType.UNDEF));
    }

    @ParameterizedTest
    @MethodSource("malformedResponses")
    public void mapsOutOfWindowOrShortBitResponsesToUndef(int address, BitArray bits, Object expectedState) {
        Poller2BitChannel channel = new Poller2BitChannel(address, PollerReadFailurePolicy.UNDEF);

        assertEquals(expectedState, channel.acceptBits(bits, 100));
    }
}
