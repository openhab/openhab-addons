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
package org.openhab.binding.candy.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.candy.internal.protocol.CandyProtocolException.*;

import java.io.IOException;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verifies translated messages, plain-text passthrough and typed configuration failures.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class CandyProtocolExceptionTest {
    @Test
    void translatesTheMessageAndPreservesTheCause() {
        IOException cause = new IOException("private transport details");
        CandyProtocolException exception = new CandyProtocolException("@text/protocol-error-json", cause);
        assertEquals(translations().getString("protocol-error-json"), exception.getMessage());
        assertEquals(exception.getMessage(), exception.getLocalizedMessage());
        assertSame(cause, exception.getCause());
        String message = exception.getMessage();
        assertNotNull(message);
        assertFalse(message.contains("@text/"));
        assertFalse(message.contains("private transport details"));
    }

    @ParameterizedTest
    @MethodSource("defaultFailures")
    void translatesEveryDefaultMessage(CandyProtocolException exception, String key) {
        assertEquals(translations().getString(key), exception.getMessage());
        assertEquals(exception.getMessage(), exception.getLocalizedMessage());
    }

    private static Stream<Arguments> defaultFailures() {
        return Stream.of(Arguments.of(new CandyProtocolException(TRANSPORT), "communication-error-transport"),
                Arguments.of(new CandyProtocolException(TIMEOUT), "communication-error-timeout"),
                Arguments.of(new CandyProtocolException(HTTP_STATUS), "communication-error-http"),
                Arguments.of(new CandyProtocolException(CLIENT_UNAVAILABLE), "communication-error-client"),
                Arguments.of(new CandyProtocolException(INVALID_RESPONSE), "communication-error-response"),
                Arguments.of(new CandyAuthenticationException(), "account-error-authentication"),
                Arguments.of(new CandyInvalidHostException(), "config-error-host"),
                Arguments.of(new CandyApplianceSelectionException(), "account-error-appliance"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "plain diagnostic message", "prefix @text/protocol-error-json", "", "@text/",
            "@text/a key", "@text/key {}", "@text/key [503]", "@text/key [null]", "@text/key [true]",
            "@text/key [\"unterminated]" })
    void acceptsPlainMessagesAndUnresolvableReferencesWithoutChangingThem(String message) {
        CandyProtocolException exception = new CandyProtocolException(message);
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getLocalizedMessage());
    }

    @Test
    void plainMessagesDoNotLoadTranslations() {
        try (var resources = mockStatic(ResourceBundle.class)) {
            var exception = new CandyProtocolException("plain diagnostic message");
            assertEquals("plain diagnostic message", exception.getMessage());
            assertEquals("plain diagnostic message", exception.getLocalizedMessage());
            resources.verifyNoInteractions();
        }
    }

    @Test
    void unknownKeyRetainsTheMessageWithoutExposingTheCause() {
        var cause = new IOException("private details");
        var exception = new CandyProtocolException("@text/missing-test-key", cause);
        assertEquals("@text/missing-test-key", exception.getMessage());
        assertEquals(exception.getMessage(), exception.getLocalizedMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void translatesOnceAtConstructionAndPreservesApostrophes() {
        Locale locale = Locale.getDefault();
        ClassLoader loader = CandyProtocolException.class.getClassLoader();
        ResourceBundle translations = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] { { "protocol-error-json", "L'application ne peut pas lire la réponse JSON." } };
            }
        };
        CandyProtocolException exception;
        try (var resources = mockStatic(ResourceBundle.class)) {
            resources.when(() -> ResourceBundle.getBundle("OH-INF.i18n.candy", locale, loader))
                    .thenReturn(translations);
            exception = new CandyProtocolException("@text/protocol-error-json");
        }
        assertEquals("L'application ne peut pas lire la réponse JSON.", exception.getMessage());
        assertEquals(exception.getMessage(), exception.getLocalizedMessage());
    }

    @Test
    void missingBundleRetainsTheOriginalMessage() {
        Locale locale = Locale.getDefault();
        ClassLoader loader = CandyProtocolException.class.getClassLoader();
        try (var resources = mockStatic(ResourceBundle.class)) {
            resources.when(() -> ResourceBundle.getBundle("OH-INF.i18n.candy", locale, loader))
                    .thenThrow(new MissingResourceException("unavailable", "candy", "protocol-error-json"));
            var exception = new CandyProtocolException("@text/protocol-error-json");
            assertEquals("@text/protocol-error-json", exception.getMessage());
            assertEquals(exception.getMessage(), exception.getLocalizedMessage());
        }
    }

    @Test
    void authenticationFailuresKeepTheirTypeWithSpecificOrPlainMessages() {
        var cause = new IOException("private details");
        var exception = new CandyAuthenticationException("@text/account-error-redirect", cause);
        assertEquals(translations().getString("account-error-redirect"), exception.getMessage());
        assertSame(cause, exception.getCause());
        assertEquals("plain diagnostic message",
                new CandyAuthenticationException("plain diagnostic message").getMessage());
    }

    private static ResourceBundle translations() {
        return ResourceBundle.getBundle("OH-INF.i18n.candy", Locale.getDefault(),
                CandyProtocolException.class.getClassLoader());
    }
}
