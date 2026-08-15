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
 * <p>
 * This class used to also decode raw {@code .blob} captures of the legacy {@code VEPUpdate} wire format
 * (via {@code src/test/resources/proto-blob/}) to test {@code Utils.proto2Json} and end-of-charge-time
 * formatting against real server data. That code path was removed entirely (see
 * {@code docs/changes/remove-vepupdate/proposal.md} and its addendum) - the legacy WebSocket ingress branch
 * in {@code AccountHandler} no longer exists, so decoding {@code VEPUpdate} wire bytes is no longer
 * something the binding does. The {@code proto-blob/*.blob} fixtures are now orphaned and were left in
 * place only because this sandbox cannot delete files on the mounted project folder; a human should
 * {@code git rm} the {@code src/test/resources/proto-blob/} directory.
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
