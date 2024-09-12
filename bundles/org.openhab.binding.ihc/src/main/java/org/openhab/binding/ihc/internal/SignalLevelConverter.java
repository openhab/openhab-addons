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
package org.openhab.binding.ihc.internal;

/**
 * Class to convert IHC RF signal level value to openHAB signal level.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SignalLevelConverter {

    int signalLevel;

    public SignalLevelConverter(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getSystemWideSignalLevel() {
        return convertSignalLevelToSystemWideLevel(signalLevel);
    }

    /**
     * Convert internal signal level (0-18) to system wide signal level (0-4).
     *
     * @param signalLevel Internal signal level
     * @return Signal level in system wide level
     */
    private int convertSignalLevelToSystemWideLevel(int signalLevel) {
        int newLevel;

        /*
         * IHC signal levels are always between 0-18.
         *
         * Use switch case to make level adaption easier in future if needed.
         */

        switch (signalLevel) {
            case 0:
            case 1:
                newLevel = 0;
                break;

            case 2:
            case 3:
            case 4:
                newLevel = 1;
                break;

            case 5:
            case 6:
            case 7:
            case 8:
                newLevel = 2;
                break;

            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                newLevel = 3;
                break;

            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            default:
                newLevel = 4;
        }

        return newLevel;
    }
}
