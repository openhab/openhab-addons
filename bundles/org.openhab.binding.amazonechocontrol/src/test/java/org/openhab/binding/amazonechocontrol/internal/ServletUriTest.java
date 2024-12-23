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
package org.openhab.binding.amazonechocontrol.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlServlet.SERVLET_PATH;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The {@link ServletUriTest} contains tests for the {@link ServletUri} record
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ServletUriTest {

    private static Stream<Arguments> testGetStrippedUri() {
        return Stream.of(Arguments.of(SERVLET_PATH, new ServletUri("", "")), //
                Arguments.of(SERVLET_PATH + "/", new ServletUri("", "")), //
                Arguments.of(SERVLET_PATH + "/accountUid", new ServletUri("accountUid", "")), //
                Arguments.of(SERVLET_PATH + "/accountUid/", new ServletUri("accountUid", "")), //
                Arguments.of(SERVLET_PATH + "/accountUid/foo/bar", new ServletUri("accountUid", "/foo/bar")), //
                Arguments.of(SERVLET_PATH + "/accountUid/foo/bar/", new ServletUri("accountUid", "/foo/bar/")), //
                Arguments.of("/foo/bar", null), //
                Arguments.of(null, null));
    }

    @ParameterizedTest
    @MethodSource
    public void testGetStrippedUri(@Nullable String in, @Nullable ServletUri expected) {
        assertThat(in, ServletUri.fromFullUri(in), is(expected));
    }
}
