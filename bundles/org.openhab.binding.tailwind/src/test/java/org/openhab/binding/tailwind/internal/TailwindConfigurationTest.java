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
package org.openhab.binding.tailwind.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TailwindConfiguration}.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindConfigurationTest {

    @Test
    public void plainHostNamesAndAddressesAreKept() {
        assertEquals("tailwind-30aea4801880.local",
                TailwindConfiguration.normalizeHostname("tailwind-30aea4801880.local"));
        assertEquals("192.168.1.50", TailwindConfiguration.normalizeHostname(" 192.168.1.50 "));
    }

    @Test
    public void urlPartsAreRemoved() {
        assertEquals("tailwind-30aea4801880.local",
                TailwindConfiguration.normalizeHostname("http://tailwind-30aea4801880.local"));
        assertEquals("tailwind-30aea4801880.local",
                TailwindConfiguration.normalizeHostname("HTTPS://tailwind-30aea4801880.local/"));
        assertEquals("192.168.1.50", TailwindConfiguration.normalizeHostname("http://192.168.1.50/json"));
    }

    @Test
    public void blankStaysBlank() {
        assertEquals("", TailwindConfiguration.normalizeHostname("  "));
        assertEquals("", TailwindConfiguration.normalizeHostname("http://"));
    }
}
