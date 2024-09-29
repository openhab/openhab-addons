/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Test case for {@link MiIoQuantityTypes}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class MiIoQuantityTypesTest {

    @Test
    public void unknownUnitTest() {
        String unitName = "some none existent unit";
        assertNull(MiIoQuantiyTypes.get(unitName));
    }

    @Test
    public void regularsUnitTest() {
        String unitName = "minute";
        assertEquals(Units.MINUTE, MiIoQuantiyTypes.get(unitName));

        unitName = "Minute";
        assertEquals(Units.MINUTE, MiIoQuantiyTypes.get(unitName));
    }

    @Test
    public void aliasUnitsTest() {
        String unitName = "square_meter";
        assertEquals(SIUnits.SQUARE_METRE, MiIoQuantiyTypes.get(unitName));

        unitName = "Square_meter";
        assertEquals(SIUnits.SQUARE_METRE, MiIoQuantiyTypes.get(unitName));

        unitName = "squaremeter";
        assertEquals(SIUnits.SQUARE_METRE, MiIoQuantiyTypes.get(unitName));
    }
}
