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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

/**
 * Verifies stable discovery identities and duplicate suppression for vacuums.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumDiscoveryServiceTest {
    @Test
    void identitiesAreStableAndDoNotCollapsePunctuation() {
        String id = DreameVacuumDiscoveryService.thingId("device:123");
        assertEquals(id, DreameVacuumDiscoveryService.thingId("device:123"));
        assertTrue(id.matches("vacuum-[a-f0-9-]+"));
        assertNotEquals(id, DreameVacuumDiscoveryService.thingId("device_123"));
        assertNotEquals(DreameVacuumDiscoveryService.thingId("-123"), DreameVacuumDiscoveryService.thingId("n123"));
    }

    @Test
    void existingDeviceIsNotRediscoveredUnderAnotherThingId() {
        ThingUID discovered = new ThingUID(DreameBindingConstants.THING_TYPE_VACUUM, "account", "vacuum-123");
        Thing existing = Objects.requireNonNull(mock(Thing.class));
        when(existing.getUID()).thenReturn(new ThingUID(DreameBindingConstants.THING_TYPE_VACUUM, "account", "custom"));
        when(existing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", "123")));
        assertTrue(DreameVacuumDiscoveryService.isAlreadyConfigured(List.of(existing), discovered, "123"));
        assertFalse(DreameVacuumDiscoveryService.isAlreadyConfigured(List.of(existing), discovered, "456"));
    }
}
