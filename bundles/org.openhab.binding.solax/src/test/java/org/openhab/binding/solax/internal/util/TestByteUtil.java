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
package org.openhab.binding.solax.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link TestByteUtil} Simple test that tests the methods of the ByteUtil
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestByteUtil {

    @Test
    public void testRead32BitSignedWithNegativeInputs() {
        assertEquals(-65536, ByteUtil.read32BitSigned((short) 0, (short) -1));
        assertEquals(-1, ByteUtil.read32BitSigned((short) -1, (short) -1));
        assertEquals(-2, ByteUtil.read32BitSigned((short) -2, (short) -1));
    }

    @Test
    public void testRead32BitSignedWithBoundaryValues() {
        assertEquals(Short.MAX_VALUE, ByteUtil.read32BitSigned((short) Short.MAX_VALUE, (short) 0));
        assertEquals((Short.MIN_VALUE & 0xFFFF) | (Short.MAX_VALUE << 16),
                ByteUtil.read32BitSigned((short) Short.MIN_VALUE, (short) Short.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, ByteUtil.read32BitSigned((short) 0, (short) (Integer.MIN_VALUE >> 16)));
    }

    @Test
    public void testRead32BitSignedResultingInNegative() {
        assertEquals(-131072, ByteUtil.read32BitSigned((short) 0, (short) -2));
    }

    @Test
    public void testRead32BitSignedResultingInMaxInteger() {
        assertEquals(Integer.MAX_VALUE, ByteUtil.read32BitSigned((short) 0xFFFF, (short) 0x7FFF));
    }
}
