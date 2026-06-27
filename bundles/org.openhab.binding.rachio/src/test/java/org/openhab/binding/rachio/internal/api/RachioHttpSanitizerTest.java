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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for Rachio HTTP log sanitization.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioHttpSanitizerTest {

    @Test
    void sanitizeForLoggingMasksCallbackUrlCredentials() {
        String input = """
                {"url":"https://user@example.com:secret-password@home.myopenhab.org/rachio/webhook"}
                """;

        String sanitized = RachioHttp.sanitizeForLogging(input);

        assertThat(sanitized.contains("user@example.com"), is(false));
        assertThat(sanitized.contains("secret-password"), is(false));
        assertThat(sanitized.contains("https://***:***@home.myopenhab.org/rachio/webhook"), is(true));
    }

    @Test
    void sanitizeForLoggingMasksEscapedCallbackUrlCredentials() {
        String input = """
                {"url":"https:\\/\\/user:password@home.myopenhab.org\\/rachio\\/webhook"}
                """;

        String sanitized = RachioHttp.sanitizeForLogging(input);

        assertThat(sanitized.contains("user:password"), is(false));
        assertThat(sanitized.contains("https:\\/\\/***:***@home.myopenhab.org"), is(true));
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

    @Test
    void callbackUrlLogReferenceUsesHashWithoutUrlPartsOrCredentials() {
        String reference = RachioApi.callbackUrlLogReference(
                "https://user@example.com:secret-password@home.myopenhab.org/rachio/webhook?token=callback-token");

        assertThat(reference, startsWith("callbackUrlHash="));
        assertThat(reference.contains("user@example.com"), is(false));
        assertThat(reference.contains("secret-password"), is(false));
        assertThat(reference.contains("home.myopenhab.org"), is(false));
        assertThat(reference.contains("callback-token"), is(false));
    }

    @Test
    void webhookRegistrationExceptionDiagnosticUsesHashAndExceptionClassOnly() {
        String callbackUrl = "https://user@example.com:secret-password@home.myopenhab.org/rachio/webhook?token=secret";
        String diagnostic = RachioApi.webhookRegistrationExceptionDiagnostic(
                new RachioApiException("Failed to register " + callbackUrl), callbackUrl);

        assertThat(diagnostic, containsString("cause=RachioApiException"));
        assertThat(diagnostic, containsString("callbackUrlHash="));
        assertThat(diagnostic.contains("user@example.com"), is(false));
        assertThat(diagnostic.contains("secret-password"), is(false));
        assertThat(diagnostic.contains("home.myopenhab.org"), is(false));
        assertThat(diagnostic.contains("token=secret"), is(false));
        assertThat(diagnostic.contains("Failed to register"), is(false));
    }
}
