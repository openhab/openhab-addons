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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests API exception message formatting.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioApiExceptionTest {
    @Test
    void unknownHostExceptionUsesHostFromCause() {
        String message = new RachioApiException("Unable to connect", new UnknownHostException("api.rach.io"))
                .toString();

        assertThat(message, containsString("Unable to connect to api.rach.io"));
    }

    @Test
    void malformedUrlExceptionUsesCauseMessage() {
        String message = new RachioApiException("Invalid endpoint", new MalformedURLException("no protocol"))
                .toString();

        assertThat(message, containsString("Invalid URL: no protocol"));
    }

    @Test
    void wrappedExceptionMessageIncludesClosingParenthesis() {
        String message = new RachioApiException("Request failed", new IllegalStateException("boom")).toString();

        assertThat(message, containsString("java.lang.IllegalStateException: boom (boom)"));
    }
}
