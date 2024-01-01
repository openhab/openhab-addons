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
package org.openhab.binding.boschshc.internal.services.userstate.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.OnOffType;

/**
 * Unit tests for UserStateServiceStateTest
 *
 * @author Patrick Gell - Initial contribution
 */
class UserStateServiceStateTest {

    UserStateServiceState subject;

    @BeforeEach
    void setUp() {
        subject = new UserStateServiceState();
    }

    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    void setStateFromStringUpdatesTheState(String inputState, boolean expectedState) {
        subject.setStateFromString(inputState);

        assertEquals(expectedState, subject.isState());
    }

    private static Stream<Arguments> provideStringsForIsBlank() {
        return Stream.of(Arguments.of("true", true), Arguments.of("false", false), Arguments.of("True", true),
                Arguments.of("False", false), Arguments.of("TRUE", true), Arguments.of("FALSE", false),
                Arguments.of(null, false), Arguments.of("", false), Arguments.of("  ", false),
                Arguments.of("not blank", false));
    }

    @Test
    void getStateAsStringReturnsState() {
        subject.setState(false);

        assertEquals("false", subject.getStateAsString());

        subject.setState(true);
        assertEquals("true", subject.getStateAsString());
    }

    @Test
    void toOnOffTypeReturnsType() {
        subject.setState(false);

        assertEquals(OnOffType.OFF, subject.toOnOffType());

        subject.setState(true);
        assertEquals(OnOffType.ON, subject.toOnOffType());
    }
}
