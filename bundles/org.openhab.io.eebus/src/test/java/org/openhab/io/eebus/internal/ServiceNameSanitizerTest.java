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
package org.openhab.io.eebus.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ServiceNameSanitizer}.
 */
@NonNullByDefault
class ServiceNameSanitizerTest {

    @Test
    void leavesAlreadySafeNamesUnchanged() {
        assertEquals("openHAB", ServiceNameSanitizer.sanitize("openHAB"));
        assertEquals("Living-Room-EMS", ServiceNameSanitizer.sanitize("Living-Room-EMS"));
    }

    @Test
    void replacesSpacesAndOtherUnsafeCharactersWithHyphens() {
        // Confirmed live: a space in this value crashes the inbound TLS handshake via SNI
        // validation (see ServiceNameSanitizer javadoc).
        assertEquals("openHAB-EEBus-Test-Harness", ServiceNameSanitizer.sanitize("openHAB EEBus Test Harness"));
        assertEquals("Caf-EMS", ServiceNameSanitizer.sanitize("Café EMS"));
    }

    @Test
    void collapsesConsecutiveUnsafeCharactersAndTrimsHyphens() {
        assertEquals("a-b", ServiceNameSanitizer.sanitize("  a___b!!  "));
    }

    @Test
    void fallsBackToOpenHabWhenNothingSafeRemains() {
        assertEquals("openHAB", ServiceNameSanitizer.sanitize("!!!"));
        assertEquals("openHAB", ServiceNameSanitizer.sanitize(""));
    }
}
