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
package org.openhab.binding.shadeauto.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ShadeAutoBindingConstants}.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
class ShadeAutoBindingConstantsTest {

    @Test
    void thingTypeUidsHaveCorrectBindingId() {
        assertEquals("shadeauto", ShadeAutoBindingConstants.THING_TYPE_HUB.getBindingId());
        assertEquals("shadeauto", ShadeAutoBindingConstants.THING_TYPE_SHADE.getBindingId());
    }

    @Test
    void thingTypeUidsHaveCorrectIds() {
        assertEquals("hub", ShadeAutoBindingConstants.THING_TYPE_HUB.getId());
        assertEquals("shade", ShadeAutoBindingConstants.THING_TYPE_SHADE.getId());
    }

    @Test
    void supportedThingTypesContainsAllTypes() {
        assertTrue(ShadeAutoBindingConstants.SUPPORTED_THING_TYPES_UIDS
                .contains(ShadeAutoBindingConstants.THING_TYPE_HUB));
        assertTrue(ShadeAutoBindingConstants.SUPPORTED_THING_TYPES_UIDS
                .contains(ShadeAutoBindingConstants.THING_TYPE_SHADE));
        assertEquals(2, ShadeAutoBindingConstants.SUPPORTED_THING_TYPES_UIDS.size());
    }

    @Test
    void apiPortIsCorrect() {
        assertEquals(10123, ShadeAutoBindingConstants.API_PORT);
    }

    @Test
    void lowBatteryThresholdIsReasonable() {
        assertTrue(ShadeAutoBindingConstants.LOW_BATTERY_THRESHOLD > 0);
        assertTrue(ShadeAutoBindingConstants.LOW_BATTERY_THRESHOLD < 50);
    }
}
