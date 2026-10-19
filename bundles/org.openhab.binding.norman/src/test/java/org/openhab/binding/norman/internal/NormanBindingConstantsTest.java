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
package org.openhab.binding.norman.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NormanBindingConstants}.
 *
 * @author Stephen Berg - Initial contribution
 */
@NonNullByDefault
class NormanBindingConstantsTest {

    @Test
    void thingTypeUidsHaveCorrectBindingId() {
        assertEquals("norman", NormanBindingConstants.THING_TYPE_HUB.getBindingId());
        assertEquals("norman", NormanBindingConstants.THING_TYPE_SHADE.getBindingId());
    }

    @Test
    void thingTypeUidsHaveCorrectIds() {
        assertEquals("hub", NormanBindingConstants.THING_TYPE_HUB.getId());
        assertEquals("shade", NormanBindingConstants.THING_TYPE_SHADE.getId());
    }

    @Test
    void supportedThingTypesContainsAllTypes() {
        assertTrue(NormanBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(NormanBindingConstants.THING_TYPE_HUB));
        assertTrue(NormanBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(NormanBindingConstants.THING_TYPE_SHADE));
        assertEquals(2, NormanBindingConstants.SUPPORTED_THING_TYPES_UIDS.size());
    }

    @Test
    void apiPortIsCorrect() {
        assertEquals(10123, NormanBindingConstants.API_PORT);
    }

    @Test
    void lowBatteryThresholdIsReasonable() {
        assertTrue(NormanBindingConstants.LOW_BATTERY_THRESHOLD > 0);
        assertTrue(NormanBindingConstants.LOW_BATTERY_THRESHOLD < 50);
    }
}
