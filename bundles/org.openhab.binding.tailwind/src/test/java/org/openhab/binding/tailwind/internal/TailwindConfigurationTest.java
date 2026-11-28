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

import static org.junit.jupiter.api.Assertions.*;

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
    public void hostNamesAndAddressesAreValid() {
        assertTrue(TailwindConfiguration.isValidHostname("tailwind-30aea4801880.local"));
        assertTrue(TailwindConfiguration.isValidHostname("tailwind-30aea4801880.local."));
        assertTrue(TailwindConfiguration.isValidHostname("192.168.1.50"));
    }

    @Test
    public void urlsAreNotValid() {
        assertFalse(TailwindConfiguration.isValidHostname("http://tailwind-30aea4801880.local"));
        assertFalse(TailwindConfiguration.isValidHostname("192.168.1.50/json"));
        assertFalse(TailwindConfiguration.isValidHostname("192.168.1.50:80"));
        assertFalse(TailwindConfiguration.isValidHostname("tailwind .local"));
        assertFalse(TailwindConfiguration.isValidHostname("-tailwind.local"));
    }

    @Test
    public void partialOpenTimeIsInMilliseconds() {
        TailwindConfiguration config = new TailwindConfiguration();
        config.door2PartialOpenTime = 4.5;
        assertEquals(2500, config.partialOpenTimeMillis(0));
        assertEquals(4500, config.partialOpenTimeMillis(1));
    }
}
