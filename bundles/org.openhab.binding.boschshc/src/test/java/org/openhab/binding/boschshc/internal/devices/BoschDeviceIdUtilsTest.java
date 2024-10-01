/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BoschDeviceIdUtils}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class BoschDeviceIdUtilsTest {

    @Test
    void testIsChildDeviceId() {
        assertFalse(BoschDeviceIdUtils.isChildDeviceId("hdm:ZigBee:70ac08fffe5294ea"));
        assertTrue(BoschDeviceIdUtils.isChildDeviceId("hdm:ZigBee:70ac08fffe5294ea#3"));
    }

    @Test
    void testGetParentDeviceId() {
        assertEquals("hdm:ZigBee:70ac08fffe5294ea",
                BoschDeviceIdUtils.getParentDeviceId("hdm:ZigBee:70ac08fffe5294ea#3"));
        assertEquals("hdm:ZigBee:70ac08fffe5294ea",
                BoschDeviceIdUtils.getParentDeviceId("hdm:ZigBee:70ac08fffe5294ea"));
    }
}
