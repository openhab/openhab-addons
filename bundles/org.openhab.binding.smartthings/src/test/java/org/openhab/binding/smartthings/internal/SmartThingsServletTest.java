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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link SmartThingsServlet}.
 */
@NonNullByDefault
class SmartThingsServletTest {

    @ParameterizedTest
    @ValueSource(strings = { "/finish", "/smartthings/account", "/smartthings/account/" })
    void oauthCallbackPathsAcceptLocalListenerAndAccountServletRedirects(String path) {
        assertTrue(SmartThingsServlet.isOAuthCallbackPath(path, "/smartthings/account"));
    }

    @Test
    void oauthCallbackPathsRejectOtherAccountAndWebhookCallback() {
        assertFalse(SmartThingsServlet.isOAuthCallbackPath("/smartthings/other", "/smartthings/account"));
        assertFalse(SmartThingsServlet.isOAuthCallbackPath("/smartthings/account/cb", "/smartthings/account"));
    }

    @Test
    void accountServletPathUsesBridgeId() {
        assertEquals("/smartthings/account", SmartThingsServlet.getServletPath("account"));
    }

    @Test
    void accountServletRegistrationUsesUniqueServletName() {
        Dictionary<String, String> firstParams = SmartThingsServlet.createServletParams("/smartthings/first");
        Dictionary<String, String> secondParams = SmartThingsServlet.createServletParams("/smartthings/second");

        assertEquals("org.openhab.binding.smartthings.internal.SmartThingsServlet.smartthings.first",
                firstParams.get("servlet-name"));
        assertEquals("org.openhab.binding.smartthings.internal.SmartThingsServlet.smartthings.second",
                secondParams.get("servlet-name"));
        assertNotEquals(firstParams.get("servlet-name"), secondParams.get("servlet-name"));
    }
}
