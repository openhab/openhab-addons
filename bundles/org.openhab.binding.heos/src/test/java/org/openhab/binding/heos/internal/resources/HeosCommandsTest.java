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
package org.openhab.binding.heos.internal.resources;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link HeosCommands}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class HeosCommandsTest {
    /**
     * Per HEOS CLI Protocol Specification, only '&', '=', and '%' must be URL-encoded in attribute values.
     */
    @ParameterizedTest
    @MethodSource("provideTestCasesForSignInCommandShouldEncodePasswordCorrectly")
    void signInCommandShouldEncodePasswordCorrectly(String username, String password, String expected) {
        assertThat(HeosCommands.signIn(username, password), is(equalTo(expected)));
    }

    private static Stream<Arguments> provideTestCasesForSignInCommandShouldEncodePasswordCorrectly() {
        return Stream.of( //
                Arguments.of("user@gmail.com", "12345", "heos://system/sign_in?un=user@gmail.com&pw=12345"),
                Arguments.of("user@foo.bar", "a&b", "heos://system/sign_in?un=user@foo.bar&pw=a%26b"),
                Arguments.of("user@foo.bar", "1=1", "heos://system/sign_in?un=user@foo.bar&pw=1%3D1"),
                Arguments.of("user@foo.bar", "%&%&", "heos://system/sign_in?un=user@foo.bar&pw=%25%26%25%26"),
                Arguments.of("user@foo.bar", "!\"#$/`", "heos://system/sign_in?un=user@foo.bar&pw=!\"#$/`"),
                Arguments.of("user@foo.bar", "føøbar", "heos://system/sign_in?un=user@foo.bar&pw=føøbar"),
                Arguments.of("user@foo.bar", "%26%26", "heos://system/sign_in?un=user@foo.bar&pw=%2526%2526"));
    }
}
