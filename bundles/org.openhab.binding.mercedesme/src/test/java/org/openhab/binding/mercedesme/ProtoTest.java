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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.utils.Utils;

/**
 * {@link ProtoTest} checks small conversion helpers used by the binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ProtoTest {

    @Test
    void testChargeProgramValues() {
        assertEquals(0, Utils.getChargeProgramNumber("DEFAULT_CHARGE_PROGRAM"), "Default Charge Program");
        assertEquals(2, Utils.getChargeProgramNumber("HOME_CHARGE_PROGRAM"), "Home Charge Program");
        assertEquals(3, Utils.getChargeProgramNumber("WORK_CHARGE_PROGRAM"), "Work Charge Program");
        assertEquals(-1, Utils.getChargeProgramNumber("whatever"), "Fail Value");
    }

    @Test
    void testTemperaturePointsValues() {
        assertEquals(3, Utils.getZoneNumber("frontCenter"), "Front Center Zone");
        assertEquals(1, Utils.getZoneNumber("frontLeft"), "Front Left Zone");
        assertEquals(2, Utils.getZoneNumber("frontRight"), "Front Right Zone");
        assertEquals(-1, Utils.getZoneNumber("whatever"), "Fail Value");
    }
}
