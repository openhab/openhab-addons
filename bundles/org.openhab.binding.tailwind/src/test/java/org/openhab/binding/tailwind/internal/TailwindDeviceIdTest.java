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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TailwindDeviceId}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class TailwindDeviceIdTest {
    @Test
    public void deviceIdBecomesMacAddressWithLeadingZeros() {
        assertEquals("30aea4801880", TailwindDeviceId.toMacAddress("_30_ae_a4_80_18_80_"));
        assertEquals("08d1f91202ec", TailwindDeviceId.toMacAddress("_8_d1_f9_12_2_ec_"));
        assertEquals("08d1f91202ec", TailwindDeviceId.toMacAddress("_8_D1_F9_12_2_EC_"));
    }

    @Test
    public void anythingElseIsNotAMacAddress() {
        assertNull(TailwindDeviceId.toMacAddress(null));
        assertNull(TailwindDeviceId.toMacAddress(""));
        assertNull(TailwindDeviceId.toMacAddress("_30_ae_a4_80_18_"));
        assertNull(TailwindDeviceId.toMacAddress("_30_ae_a4_80_18_zz_"));
        assertNull(TailwindDeviceId.toMacAddress("_30_ae_a4_80_18_80_11_"));
    }
}
