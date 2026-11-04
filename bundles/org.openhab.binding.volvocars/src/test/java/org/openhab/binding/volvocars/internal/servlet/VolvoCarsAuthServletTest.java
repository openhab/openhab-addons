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
package org.openhab.binding.volvocars.internal.servlet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the parsing of the redirect URL a user pastes into the authorization page.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VolvoCarsAuthServletTest {

    @Test
    void parsesCodeAndStateFromRedirectedUrl() {
        Map<String, String> query = VolvoCarsAuthServlet
                .parseQuery(" https://example.org/volvocars?code=abc%2Fdef&state=xyz-123 ");

        assertEquals("abc/def", query.get("code"));
        assertEquals("xyz-123", query.get("state"));
    }

    @Test
    void handlesMissingOrInvalidInput() {
        assertTrue(VolvoCarsAuthServlet.parseQuery(null).isEmpty());
        assertTrue(VolvoCarsAuthServlet.parseQuery("").isEmpty());
        assertTrue(VolvoCarsAuthServlet.parseQuery("https://example.org/volvocars").isEmpty());
        assertTrue(VolvoCarsAuthServlet.parseQuery("not a url").isEmpty());
    }
}
