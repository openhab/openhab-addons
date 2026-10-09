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
package org.openhab.binding.caldav.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.logic.CalendarLimitException;

import com.google.gson.JsonParser;

/**
 * Verifies the Core Thing status localization format against the binding's English templates.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalDavErrorsTest {
    @Test
    void errorReferencesResolveToTheSameSafeTextAsItemStates() throws IOException {
        Properties messages = messages();
        for (String operation : List.of("PROPFIND", "REPORT")) {
            for (int status : List.of(401, 403, 404, 405, 500, 501)) {
                CalDavHttpException error = new CalDavHttpException(operation, status);
                assertTranslation(messages, CalDavErrors.account(error));
                assertTranslation(messages, CalDavErrors.calendar(error, false));
                assertTranslation(messages, CalDavErrors.calendar(error, true));
                IOException wrapped = new IOException("https://user:SECRET@example.org/private", error);
                assertTranslation(messages, CalDavErrors.account(wrapped));
                assertTranslation(messages, CalDavErrors.calendar(wrapped, true));
            }
        }
        for (Exception error : List.of(new UnknownHostException("private-host SECRET"),
                new SSLException("private-certificate SECRET"), new CertificateException("private-certificate SECRET"),
                new SocketTimeoutException("private-host SECRET"), new TimeoutException("private-host SECRET"),
                new ConnectException("private-host SECRET"), new NoRouteToHostException("private-host SECRET"))) {
            IOException wrapped = new IOException("https://user:SECRET@example.org/private", error);
            assertTranslation(messages, CalDavErrors.account(wrapped));
            assertTranslation(messages, CalDavErrors.calendar(wrapped, true));
        }
        for (Exception error : List.of(new IllegalArgumentException("https://user:SECRET@example.org/private"),
                new IOException("private response body SECRET"))) {
            assertTranslation(messages, CalDavErrors.account(error));
            assertTranslation(messages, CalDavErrors.calendar(error, false));
        }
        assertTranslation(messages, CalDavErrors.calendar(new CalendarLimitException("private data SECRET"), true));
        assertTranslation(messages, CalDavErrors
                .calendar(new IOException("private data SECRET", new CalDavHttpException("GET", 404)), true));
    }

    @Test
    void httpParametersUseAJsonStringArrayContainingOnlyTheStatusCode() throws IOException {
        CalDavErrors.Failure failure = CalDavErrors.calendar(new CalDavHttpException("REPORT", 500), true);
        assertEquals("@text/status.calendar.http-error [\"500\"]", failure.statusDescription());
        assertEquals("Server returned HTTP 500", failure.description());
        assertTranslation(messages(), failure);
    }

    @Test
    void lifecycleDescriptionsHaveEnglishDefaultTranslations() throws IOException {
        Properties messages = messages();
        assertEquals("Waiting for CalDAV server communication", messages.getProperty("status.account.waiting"));
        assertEquals("Waiting for initial calendar synchronization", messages.getProperty("status.calendar.waiting"));
        assertEquals("Fetching calendar data", messages.getProperty("status.calendar.fetching"));
        assertEquals("Account bridge is offline", messages.getProperty("status.calendar.bridge-offline"));
    }

    private static Properties messages() throws IOException {
        Properties messages = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(CalDavErrorsTest.class.getResourceAsStream("/OH-INF/i18n/caldav.properties")),
                StandardCharsets.UTF_8)) {
            messages.load(reader);
        }
        return messages;
    }

    private static void assertTranslation(Properties messages, CalDavErrors.Failure failure) {
        String reference = failure.statusDescription();
        assertTrue(reference.startsWith("@text/"));
        String[] parts = reference.substring("@text/".length()).split(" ", 2);
        String template = Objects.requireNonNull(messages.getProperty(parts[0]), parts[0]);
        Object[] parameters = parts.length == 1 ? new Object[0]
                : JsonParser.parseString(parts[1]).getAsJsonArray().asList().stream().map(value -> {
                    assertTrue(value.isJsonPrimitive() && value.getAsJsonPrimitive().isString());
                    assertTrue(value.getAsString().matches("[0-9]+"));
                    return value.getAsString();
                }).toArray();
        assertEquals(failure.description(), MessageFormat.format(template, parameters));
        assertFalse(failure.description().startsWith("@text/"));
        for (String secret : List.of("SECRET", "private", "example.org", "user:")) {
            assertFalse(reference.contains(secret));
            assertFalse(failure.description().contains(secret));
        }
    }
}
