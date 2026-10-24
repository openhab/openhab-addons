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
package org.openhab.binding.e3dc.internal;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * Tests the text form and the request of a power mode.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCPowerModeTest {

    @Test
    void autoNeedsNoPowerAndNoCycles() {
        assertEquals(new E3DCPowerMode(0, 0, 0), E3DCPowerMode.parse("auto"));
        assertFalse(parsed("auto").isHeld());
    }

    @Test
    void idleCarriesOnlyTheCycles() {
        assertEquals(new E3DCPowerMode(1, 0, 60), E3DCPowerMode.parse("idle:60"));
    }

    @Test
    void thePowerModesCarryPowerAndCycles() {
        assertEquals(new E3DCPowerMode(2, 2000, 60), E3DCPowerMode.parse("discharge:2000:60"));
        assertEquals(new E3DCPowerMode(3, 2000, 60), E3DCPowerMode.parse("charge:2000:60"));
        assertEquals(new E3DCPowerMode(4, 2000, 60), E3DCPowerMode.parse(" grid_charge:2000:60 "));
        assertTrue(parsed("charge:2000:60").isHeld());
    }

    @Test
    void textThatIsNoPowerModeIsRejected() {
        for (String text : List.of("", "charge", "charge:2000", "idle", "idle:x", "boost:1:1", "charge:-5:3",
                "charge:100000:3", "charge:100:10000")) {
            assertNull(E3DCPowerMode.parse(text), text);
        }
    }

    @Test
    void theRequestIsOnePowerContainerWithModeAndValue() {
        List<RscpValue> request = new E3DCPowerMode(3, 2500, 10).toRequest();

        assertEquals(1, request.size());
        RscpValue container = request.get(0);
        assertEquals(RscpTag.EMS_REQ_SET_POWER, container.getTag());
        RscpValue mode = container.find(RscpTag.EMS_REQ_SET_POWER_MODE);
        RscpValue value = container.find(RscpTag.EMS_REQ_SET_POWER_VALUE);
        assertNotNull(mode);
        assertNotNull(value);
        assertEquals(RscpType.UCHAR8, mode.getType());
        assertEquals(RscpType.INT32, value.getType());
        assertEquals(3, ((Number) requireNonNull(mode.getValue())).intValue());
        assertEquals(2500, ((Number) requireNonNull(value.getValue())).intValue());
    }

    private static E3DCPowerMode parsed(String text) {
        return requireNonNull(E3DCPowerMode.parse(text));
    }
}
