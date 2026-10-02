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
package org.openhab.binding.caldav.internal.client;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * URL and XML trust-boundary regressions.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalDavUrisTest {
    @Test
    void resolvesRelativeReferencesWithoutLosingEscaping() {
        assertEquals("https://example.org/a%20b.ics",
                CalDavUris.resolve(URI.create("https://example.org/calendar/"), "../a%20b.ics").toString());
    }

    @Test
    void rejectsCredentialLeaksAndDowngrades() {
        URI base = URI.create("https://example.org/");
        for (String reference : new String[] { "https://evil.example/calendar/", "http://example.org/",
                "https://example.org:444/", "https://user:secret@example.org/", "file:///tmp/calendar",
                "https://example.org/#secret" }) {
            assertThrows(IllegalArgumentException.class, () -> CalDavUris.resolve(base, reference));
        }
    }

    @Test
    void xmlDepthIsBounded() {
        assertThrows(Exception.class, () -> CalDavXml.parse("<x>".repeat(100) + "</x>".repeat(100)));
    }
}
