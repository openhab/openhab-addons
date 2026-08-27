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
package org.openhab.binding.amazonechocontrol.internal.connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Guards the call site: Amazon answers this one endpoint with 400 ThrottlingException for the app agent the binding
 * otherwise sends, so dropping the override here silently brings the 400 back. That the override then reaches the
 * request is tested in {@code HttpRequestBuilderTest}.
 *
 * <p>
 * Asserted against the source because reaching the call needs the Alexa endpoint and this bundle has no harness that
 * can instantiate {@link Connection}.
 * </p>
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
class NotificationsUserAgentTest {
    private static final Path SOURCE = Path
            .of("src/main/java/org/openhab/binding/amazonechocontrol/internal/connection/Connection.java");

    private static String getNotifications() throws IOException {
        String source = Files.readString(SOURCE);
        int start = source.indexOf("public List<NotificationTO> getNotifications()");
        assertTrue(start > 0, "getNotifications() not found - this test cannot guard what it cannot read");
        int end = source.indexOf("\n    }", start);
        assertTrue(end > start, "the end of getNotifications() moved - adjust this test");
        return source.substring(start, end);
    }

    @Test
    void notificationPollOverridesTheUserAgent() throws IOException {
        assertTrue(getNotifications().contains(".withHeader(\"User-Agent\", NOTIFICATIONS_USER_AGENT)"),
                "the notification poll no longer overrides the user agent - Amazon throttles the default one here");
    }

    @Test
    void theOverrideIsABrowserAgentNotTheDefaultAppAgent() throws IOException {
        String source = Files.readString(SOURCE);
        int at = source.indexOf("NOTIFICATIONS_USER_AGENT =");
        assertTrue(at > 0, "the NOTIFICATIONS_USER_AGENT constant is gone - this test is testing nothing");
        String value = source.substring(at, source.indexOf(';', at));
        assertTrue(value.contains("Mozilla"), "the override is not a browser user agent");
        assertFalse(value.contains("AmazonWebView"), "the override is the throttled app agent again");
    }
}
