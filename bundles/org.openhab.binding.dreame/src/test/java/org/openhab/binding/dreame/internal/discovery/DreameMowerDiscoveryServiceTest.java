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
package org.openhab.binding.dreame.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class DreameMowerDiscoveryServiceTest {

    @Test
    void thingIdNeverStartsWithCloudIdSign() {
        assertEquals("mower-n123456789", DreameMowerDiscoveryService.thingId("-123456789"));
        assertEquals("mower-123456789", DreameMowerDiscoveryService.thingId("123456789"));
    }

    @Test
    void thingIdReplacesUnsupportedCharacters() {
        assertEquals("mower-device_123", DreameMowerDiscoveryService.thingId("device:123"));
    }
}
