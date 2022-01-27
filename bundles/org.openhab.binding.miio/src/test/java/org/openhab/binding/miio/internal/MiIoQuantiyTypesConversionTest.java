/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.miio.internal.miot.MiIoQuantiyTypesConversion;

/**
 * Test case for {@link MiIoQuantiyTypesConversion}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class MiIoQuantiyTypesConversionTest {

    @Test
    public void unknownUnitTest() {
        String unitName = "some none existent unit";
        assertNull(MiIoQuantiyTypesConversion.getType(unitName));
    }

    @Test
    public void nullUnitTest() {
        String unitName = null;
        assertNull(MiIoQuantiyTypesConversion.getType(unitName));
    }

    @Test
    public void regularsUnitTest() {
        String unitName = "minute";
        assertEquals("Time", MiIoQuantiyTypesConversion.getType(unitName));

        unitName = "Minute";
        assertEquals("Time", MiIoQuantiyTypesConversion.getType(unitName));
    }
}
