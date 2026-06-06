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
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * Tests for Rachio HTTP log sanitization.
 *
 * @author openHAB Contributors - Initial contribution
 */
class RachioHttpSanitizerTest {

    @Test
    void sanitizeForLoggingMasksCallbackUrlCredentials() {
        String input = """
                {"url":"https://webhook-user:webhook-password@host.example/rachio/webhook"}
                """;

        String sanitized = RachioHttp.sanitizeForLogging(input);

        assertThat(sanitized.contains("webhook-user"), is(false));
        assertThat(sanitized.contains("webhook-password"), is(false));
        assertThat(sanitized.contains("https://***:***@host.example/rachio/webhook"), is(true));
    }

    @Test
    void sanitizeForLoggingMasksEscapedCallbackUrlCredentials() {
        String input = """
                {"url":"https:\\/\\/webhook-user:webhook-password@host.example\\/rachio\\/webhook"}
                """;

        String sanitized = RachioHttp.sanitizeForLogging(input);

        assertThat(sanitized.contains("webhook-user:webhook-password"), is(false));
        assertThat(sanitized.contains("https:\\/\\/***:***@host.example"), is(true));
    }

    @Test
    void sanitizeForLoggingMasksAuthorizationAndApiTokens() {
        String input = """
                Authorization: Bearer abc123.secret-token, {"apikey":"very-secret-api-key","authorization":"Bearer json-token"}
                https://api.example.org/path?apikey=another-secret&other=value
                """;

        String sanitized = RachioHttp.sanitizeForLogging(input);

        assertThat(sanitized.contains("abc123.secret-token"), is(false));
        assertThat(sanitized.contains("json-token"), is(false));
        assertThat(sanitized.contains("very-secret-api-key"), is(false));
        assertThat(sanitized.contains("another-secret"), is(false));
        assertThat(sanitized.contains("Bearer [redacted]"), is(true));
        assertThat(sanitized.contains("\"apikey\":\"[redacted]\""), is(true));
        assertThat(sanitized.contains("apikey=[redacted]"), is(true));
    }
}
