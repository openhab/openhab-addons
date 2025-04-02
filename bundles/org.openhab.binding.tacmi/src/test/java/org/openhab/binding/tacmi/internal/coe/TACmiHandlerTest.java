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

package org.openhab.binding.tacmi.internal.coe;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.tacmi.internal.message.MessageType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.ThingImpl;


/**
 * Testcode for TACMI Handler
 * 
 *
 * @author Martin Schafföner
 *
 */
public class TACmiHandlerTest {

    public static Stream<Arguments> provideOutputIndices() {
        return Stream.of(Arguments.of(1, true, 0), Arguments.of(2, true, 1), Arguments.of(3, true, 2),
                Arguments.of(4, true, 3), Arguments.of(5, true, 0), Arguments.of(32, true, 3),
                Arguments.of(1, false, 0), Arguments.of(2, false, 1), Arguments.of(16, false, 15),
                Arguments.of(17, false, 0), Arguments.of(32, false, 15));
    }

    @ParameterizedTest
    @MethodSource("provideOutputIndices")
    public void testGetOutputIndexHappy(int aIndex, boolean aAnalog, int aExpected) {
        final Thing thing = new ThingImpl(new ThingTypeUID("test:test"), "test");
        final TACmiHandler sut = new TACmiHandler(thing);

        final int outputIdx = sut.getOutputIndex(aIndex, aAnalog);

        Assertions.assertEquals(outputIdx, aExpected);
    }

    // This is actual target data

    public static Stream<Arguments> providePodIds() {
        return Stream.of(Arguments.of(MessageType.ANALOG, 1, (byte) 1), Arguments.of(MessageType.ANALOG, 2, (byte) 1),
                Arguments.of(MessageType.ANALOG, 3, (byte) 1), Arguments.of(MessageType.ANALOG, 4, (byte) 1),
                Arguments.of(MessageType.ANALOG, 5, (byte) 2), Arguments.of(MessageType.ANALOG, 32, (byte) 8),
                Arguments.of(MessageType.DIGITAL, 1, (byte) 0), Arguments.of(MessageType.DIGITAL, 16, (byte) 0),
                Arguments.of(MessageType.DIGITAL, 17, (byte) 9), Arguments.of(MessageType.DIGITAL, 32, (byte) 9));
    }

    @ParameterizedTest
    @MethodSource("providePodIds")
    public void testGetPodIdHappy(final MessageType aMT, final int aOutput, final byte aExpected) {
        final Thing thing = new ThingImpl(new ThingTypeUID("test:test"), "test");
        final TACmiHandler sut = new TACmiHandler(thing);

        final byte podId = sut.getPodId(aMT, aOutput);

        Assertions.assertEquals(podId, aExpected);
    }
}
