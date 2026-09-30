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
        assertEquals("Dock error", VacuumErrorType.getType(126).getDescription());
        assertEquals(VacuumErrorType.ERROR254, VacuumErrorType.getType(254));
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
