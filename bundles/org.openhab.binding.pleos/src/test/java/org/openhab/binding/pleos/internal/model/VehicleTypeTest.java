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
package org.openhab.binding.pleos.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VehicleType}.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class VehicleTypeTest {

    @Test
    public void testFromApiValueKnownTypes() {
        assertEquals(VehicleType.GN, VehicleType.fromApiValue("GN"));
        assertEquals(VehicleType.GN, VehicleType.fromApiValue("gn"));
        assertEquals(VehicleType.EV, VehicleType.fromApiValue("EV"));
        assertEquals(VehicleType.HV, VehicleType.fromApiValue("HV"));
        assertEquals(VehicleType.PE, VehicleType.fromApiValue("PE"));
        assertEquals(VehicleType.FV, VehicleType.fromApiValue("FV"));
    }

    @Test
    public void testFromApiValueUnknownAndNull() {
        assertEquals(VehicleType.UNKNOWN, VehicleType.fromApiValue(null));
        assertEquals(VehicleType.UNKNOWN, VehicleType.fromApiValue(""));
        assertEquals(VehicleType.UNKNOWN, VehicleType.fromApiValue("   "));
        assertEquals(VehicleType.UNKNOWN, VehicleType.fromApiValue("OTHER"));
    }

    @Test
    public void testSupportsCharging() {
        assertTrue(VehicleType.EV.supportsCharging());
        assertTrue(VehicleType.PE.supportsCharging());
        assertTrue(VehicleType.UNKNOWN.supportsCharging());

        assertFalse(VehicleType.GN.supportsCharging());
        assertFalse(VehicleType.HV.supportsCharging());
        assertFalse(VehicleType.FV.supportsCharging());
    }

    @Test
    public void testSupportsFuel() {
        assertTrue(VehicleType.GN.supportsFuel());
        assertTrue(VehicleType.HV.supportsFuel());
        assertTrue(VehicleType.PE.supportsFuel());
        assertTrue(VehicleType.FV.supportsFuel());
        assertTrue(VehicleType.UNKNOWN.supportsFuel());

        assertFalse(VehicleType.EV.supportsFuel());
    }
}
