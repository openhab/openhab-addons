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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.DreameBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

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

    @Test
    void configuredDeviceIsDetectedByDeviceIdDespiteCustomThingId() {
        ThingUID discoveredUID = new ThingUID(DreameBindingConstants.THING_TYPE_MOWER, "account", "mower-123");
        Thing existingThing = Objects.requireNonNull(mock(Thing.class));
        when(existingThing.getUID())
                .thenReturn(new ThingUID(DreameBindingConstants.THING_TYPE_MOWER, "account", "garden-mower"));
        when(existingThing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", "123")));

        assertTrue(DreameMowerDiscoveryService.isAlreadyConfigured(List.of(existingThing), discoveredUID, "123"));
        assertFalse(DreameMowerDiscoveryService.isAlreadyConfigured(List.of(existingThing), discoveredUID, "456"));
    }

    @Test
    void configuredDeviceIsDetectedByCanonicalThingUid() {
        ThingUID discoveredUID = new ThingUID(DreameBindingConstants.THING_TYPE_MOWER, "account", "mower-123");
        Thing existingThing = Objects.requireNonNull(mock(Thing.class));
        when(existingThing.getUID()).thenReturn(discoveredUID);
        when(existingThing.getConfiguration()).thenReturn(new Configuration());

        assertTrue(DreameMowerDiscoveryService.isAlreadyConfigured(List.of(existingThing), discoveredUID, "123"));
    }
}
