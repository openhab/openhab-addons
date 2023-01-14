/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link TestParadoxUtil} This test tests various functions from ParadoxUtils class
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class TestParadoxUtil {

    @Test
    public void testExtendArray() {
        byte[] arrayToExtend = { 0x0A, 0x50, 0x08, 0x00, 0x00, 0x01, 0x00, 0x00, 0x59 };
        final int rate = 16;
        byte[] extendedArray = ParadoxUtil.extendArray(arrayToExtend, rate);

        final byte[] EXPECTED_RESULT = { 0x0A, 0x50, 0x08, 0x00, 0x00, 0x01, 0x00, 0x00, 0x59, (byte) 0xEE, (byte) 0xEE,
                (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE };
        assertArrayEquals(EXPECTED_RESULT, extendedArray); //
    }

    @Test
    public void testMergeArrays() {
        final byte[] ARR1 = { 0x01, 0x02, 0x03 };
        final byte[] ARR2 = { 0x04, 0x05, 0x06 };
        final byte[] ARR3 = { 0x07, 0x08, 0x09 };
        byte[] mergedArrays = ParadoxUtil.mergeByteArrays(ARR1, ARR2, ARR3);

        final byte[] EXPECTED_RESULT = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };
        assertArrayEquals(EXPECTED_RESULT, mergedArrays);
    }
}
