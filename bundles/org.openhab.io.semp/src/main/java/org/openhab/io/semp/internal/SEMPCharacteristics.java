/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

/**
 * SEMP consumers characteristics
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
public class SEMPCharacteristics {
    /*
     * Nominal maximum power consumption of the device in Watts. If the device is controllable with regard to power
     * consumption, the recommendation
     * of the energy management system will never exceed this value.
     */
    private Integer maxPowerConsumption;

    /*
     * If the device is switched on, it has to remain in this status for at least MinOnTime seconds.
     */
    private Integer minOnTime; /* occurs: 0 .. 1 */

    /*
     * If the device is switched off, it has to remain in this status for at least MinOffTime seconds.
     */
    private Integer minOffTime; /* occurs: 0 .. 1 */

    public SEMPCharacteristics() {
    }

    /*
     * Setter for maxPowerConsumption
     */
    public void setMaxPowerConsumption(int maxPowerConsumption) {
        this.maxPowerConsumption = maxPowerConsumption;
    }

    /*
     * Getter for maxPowerConsumption
     */
    public int getMaxPowerConsumption() {
        return maxPowerConsumption;
    }

    /*
     * Checks if field maxPowerConsumption is set
     */
    public boolean isMaxPowerConsumptionSet() {
        if (maxPowerConsumption == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for minOnTime
     */
    public void setMinOnTime(int minOnTime) {
        this.minOnTime = minOnTime;
    }

    /*
     * Getter for minOnTime
     */
    public int getMinOnTime() {
        return minOnTime;
    }

    /*
     * Checks if field minOnTime is set
     */
    public boolean isMinOnTimeSet() {
        if (minOnTime == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for minOffTime
     */
    public void setMinOffTime(int minOffTime) {
        this.minOffTime = minOffTime;
    }

    /*
     * Getter for minOffTime
     */
    public int getMinOffTime() {
        return minOffTime;
    }

    /*
     * Checks if field minOffTime is set
     */
    public boolean isMinOffTimeSet() {
        if (minOffTime == null) {
            return false;
        } else {
            return true;
        }
    }
}
