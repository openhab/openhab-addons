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
package org.openhab.binding.pleos.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PleosAccountHandler}.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosAccountHandlerTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Berlin");

    @Test
    public void testCalculateSecretExpiryValidDate() {
        ZonedDateTime expiry = PleosAccountHandler.calculateSecretExpiry("2026-01-01", TEST_ZONE);
        assertNotNull(expiry);

        ZonedDateTime expected = LocalDate.of(2026, 1, 1).atStartOfDay(TEST_ZONE).plusDays(90);
        assertEquals(expected, expiry);
    }

    @Test
    public void testCalculateSecretExpiryInvalidDate() {
        assertNull(PleosAccountHandler.calculateSecretExpiry("invalid-date", TEST_ZONE));
        assertNull(PleosAccountHandler.calculateSecretExpiry("2026/01/01", TEST_ZONE));
    }

    @Test
    public void testCalculateSecretExpiryNullOrBlank() {
        assertNull(PleosAccountHandler.calculateSecretExpiry(null, TEST_ZONE));
        assertNull(PleosAccountHandler.calculateSecretExpiry("", TEST_ZONE));
        assertNull(PleosAccountHandler.calculateSecretExpiry("   ", TEST_ZONE));
    }
}
