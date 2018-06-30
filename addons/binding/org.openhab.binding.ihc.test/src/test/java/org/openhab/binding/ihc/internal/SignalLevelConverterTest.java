/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SignalLevelConverterTest {

    @Test
    public void checkSignalLevelConverter() {

        // IHC signal levels are between 0-18.
        for (int i = 0; i <= 18; i++) {
            int signalLevel = new SignalLevelConverter(i).getSystemWideSignalLevel();
            switch (i) {
                case 0:
                case 1:
                    assertEquals(0, signalLevel);
                    break;

                case 2:
                case 3:
                case 4:
                    assertEquals(1, signalLevel);
                    break;

                case 5:
                case 6:
                case 7:
                case 8:
                    assertEquals(2, signalLevel);
                    break;

                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                    assertEquals(3, signalLevel);
                    break;

                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                    assertEquals(4, signalLevel);
                    break;

                default:
                    fail("Illegal state");
            }
        }
    }
}
