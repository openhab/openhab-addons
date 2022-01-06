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
package org.openhab.binding.teleinfo.internal.reader.io.serialport;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.teleinfo.internal.reader.io.TeleinfoInputStream;
import org.openhab.binding.teleinfo.internal.serial.TeleinfoTicMode;
import org.openhab.binding.teleinfo.util.TestUtils;

/**
 *
 * @author Olivier MARCEAU - Initial contribution
 */
@NonNullByDefault
public class FrameUtilTest {

    @Test
    public void testComputeGroupLineChecksumThreePhaseProd() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(TestUtils.getTestFile("linky-tic-mode-standard-three-phase-prod.raw"))));
        String groupLine;
        int i = 0;
        while ((groupLine = bufferedReader.readLine()) != null) {
            if (i >= 1 && !TeleinfoInputStream.isHeaderFrame(groupLine)) {
                char expected = groupLine.charAt(groupLine.length() - 1);
                char actual = FrameUtil.computeGroupLineChecksum(groupLine.substring(0, groupLine.length() - 2),
                        TeleinfoTicMode.STANDARD);
                assertEquals(expected, actual, i + " " + groupLine + " " + (int) expected + " " + (int) actual);
            }
            i++;
        }
    }

    @Test
    public void testComputeGroupLineChecksumSinglePhaseProd() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(TestUtils.getTestFile("linky-tic-mode-standard-single-phase-prod.raw"))));
        String groupLine;
        int i = 0;
        while ((groupLine = bufferedReader.readLine()) != null) {
            if (i >= 1 && !TeleinfoInputStream.isHeaderFrame(groupLine)) {
                char expected = groupLine.charAt(groupLine.length() - 1);
                char actual = FrameUtil.computeGroupLineChecksum(groupLine.substring(0, groupLine.length() - 2),
                        TeleinfoTicMode.STANDARD);
                assertEquals(expected, actual, i + " " + groupLine + " " + (int) expected + " " + (int) actual);
            }
            i++;
        }
    }

    @Test
    public void testComputeRelaisStates() {
        assertArrayEquals(new boolean[] { true, false, false, false, false, false, false, false },
                FrameUtil.parseRelaisStates("001"));
        assertArrayEquals(new boolean[] { false, false, true, true, false, false, false, true },
                FrameUtil.parseRelaisStates("140"));
    }
}
