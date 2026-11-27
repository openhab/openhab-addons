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
package org.openhab.binding.miio.internal.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link VacuumErrorType}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class VacuumErrorTypeTest {

    @Test
    public void knownErrorCodesTest() {
        assertEquals(VacuumErrorType.ERROR00, VacuumErrorType.getType(0));
        assertEquals(VacuumErrorType.ERROR23, VacuumErrorType.getType(23));
        assertEquals("Check the clean water tank", VacuumErrorType.getType(38).getDescription());
        assertEquals(VacuumErrorType.ERROR254, VacuumErrorType.getType(254));
    }

    @Test
    public void errorCode29IsModelSpecificTest() {
        // S7 family: manual lists "Cannot cross carpet"
        assertEquals(VacuumErrorType.ERROR29_CARPET, VacuumErrorType.getType(29, "roborock.vacuum.a15"));
        assertEquals(VacuumErrorType.ERROR29_CARPET, VacuumErrorType.getType(29, "roborock.vacuum.a14"));
        assertEquals(VacuumErrorType.ERROR29_CARPET, VacuumErrorType.getType(29, "roborock.vacuum.a15v3"));
        // newer models: suspected pet waste
        assertEquals(VacuumErrorType.ERROR29, VacuumErrorType.getType(29, "roborock.vacuum.a38"));
        assertEquals(VacuumErrorType.ERROR29, VacuumErrorType.getType(29, "roborock.vacuum.a150"));
        assertEquals(VacuumErrorType.ERROR29, VacuumErrorType.getType(29, ""));
        assertEquals("Suspected pet waste found", VacuumErrorType.getType(29).getDescription());
        assertEquals("Unable to cross carpet", VacuumErrorType.getType(29, "roborock.vacuum.a15").getDescription());
    }

    @Test
    public void modelDoesNotChangeOtherCodesTest() {
        assertEquals(VacuumErrorType.ERROR10, VacuumErrorType.getType(10, "roborock.vacuum.a15"));
        assertEquals(VacuumErrorType.ERROR32, VacuumErrorType.getType(32, "roborock.vacuum.a15"));
        assertEquals(VacuumErrorType.ERROR32, VacuumErrorType.getType(32, "roborock.vacuum.a38"));
        assertEquals(VacuumErrorType.ERROR33, VacuumErrorType.getType(33, "roborock.vacuum.a15"));
        assertEquals(VacuumErrorType.ERROR255, VacuumErrorType.getType(100, "roborock.vacuum.a15"));
    }

    @Test
    public void dockErrorCodesTest() {
        assertEquals("Dock fan error", VacuumErrorType.getType(126).getDescription());
        assertEquals("Air pump switch error", VacuumErrorType.getType(134).getDescription());
        assertEquals("Solenoid valve error", VacuumErrorType.getType(150).getDescription());
    }

    @Test
    public void unlistedInternalErrorCodesTest() {
        assertEquals(VacuumErrorType.ERROR255, VacuumErrorType.getType(100));
        assertEquals(VacuumErrorType.ERROR255, VacuumErrorType.getType(125));
        assertEquals(VacuumErrorType.ERROR255, VacuumErrorType.getType(255));
    }

    @Test
    public void unknownErrorCodesTest() {
        assertEquals(VacuumErrorType.UNKNOWN, VacuumErrorType.getType(30));
        assertEquals(VacuumErrorType.UNKNOWN, VacuumErrorType.getType(99));
        assertEquals(VacuumErrorType.UNKNOWN, VacuumErrorType.getType(256));
        assertEquals(VacuumErrorType.UNKNOWN, VacuumErrorType.getType(-5));
    }
}
