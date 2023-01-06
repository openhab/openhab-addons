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
package org.openhab.binding.ihc.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SignalLevelConverterTest {

    @Test
    public void checkSignalLevelConverter() {
        // IHC signal levels are between 0-18.

        assertEquals(0, new SignalLevelConverter(0).getSystemWideSignalLevel());
        assertEquals(0, new SignalLevelConverter(1).getSystemWideSignalLevel());
        assertEquals(1, new SignalLevelConverter(2).getSystemWideSignalLevel());
        assertEquals(1, new SignalLevelConverter(3).getSystemWideSignalLevel());
        assertEquals(1, new SignalLevelConverter(4).getSystemWideSignalLevel());
        assertEquals(2, new SignalLevelConverter(5).getSystemWideSignalLevel());
        assertEquals(2, new SignalLevelConverter(6).getSystemWideSignalLevel());
        assertEquals(2, new SignalLevelConverter(7).getSystemWideSignalLevel());
        assertEquals(2, new SignalLevelConverter(8).getSystemWideSignalLevel());
        assertEquals(3, new SignalLevelConverter(9).getSystemWideSignalLevel());
        assertEquals(3, new SignalLevelConverter(10).getSystemWideSignalLevel());
        assertEquals(3, new SignalLevelConverter(11).getSystemWideSignalLevel());
        assertEquals(3, new SignalLevelConverter(12).getSystemWideSignalLevel());
        assertEquals(3, new SignalLevelConverter(13).getSystemWideSignalLevel());
        assertEquals(4, new SignalLevelConverter(14).getSystemWideSignalLevel());
        assertEquals(4, new SignalLevelConverter(15).getSystemWideSignalLevel());
        assertEquals(4, new SignalLevelConverter(16).getSystemWideSignalLevel());
        assertEquals(4, new SignalLevelConverter(17).getSystemWideSignalLevel());
        assertEquals(4, new SignalLevelConverter(18).getSystemWideSignalLevel());
    }
}
