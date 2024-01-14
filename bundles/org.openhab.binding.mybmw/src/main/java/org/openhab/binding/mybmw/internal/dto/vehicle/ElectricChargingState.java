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
package org.openhab.binding.mybmw.internal.dto.vehicle;

/**
 *
 * derived from the API responses
 *
 * @author Martin Grassl - initial contribution
 * @author Mark Herwege - refactoring, V2 API charging
 */
public class ElectricChargingState {
    private String chargingConnectionType = ""; // UNKNOWN,
    private String chargingStatus = ""; // FINISHED_FULLY_CHARGED,
    private boolean isChargerConnected = false; // true,
    private int chargingTarget = -1; // 80,
    private int chargingLevelPercent = -1; // 80,
    private int remainingChargingMinutes = -1; // 178
    private int range = -1; // 286

    /**
     * @return the chargingConnectionType
     */
    public String getChargingConnectionType() {
        return chargingConnectionType;
    }

    /**
     * @param chargingConnectionType the chargingConnectionType to set
     */
    public void setChargingConnectionType(String chargingConnectionType) {
        this.chargingConnectionType = chargingConnectionType;
    }

    /**
     * @return the chargingStatus
     */
    public String getChargingStatus() {
        return chargingStatus;
    }

    /**
     * @param chargingStatus the chargingStatus to set
     */
    public void setChargingStatus(String chargingStatus) {
        this.chargingStatus = chargingStatus;
    }

    /**
     * @return the isChargerConnected
     */
    public boolean isChargerConnected() {
        return isChargerConnected;
    }

    /**
     * @param isChargerConnected the isChargerConnected to set
     */
    public void setChargerConnected(boolean isChargerConnected) {
        this.isChargerConnected = isChargerConnected;
    }

    /**
     * @return the chargingTarget
     */
    public int getChargingTarget() {
        return chargingTarget;
    }

    /**
     * @param chargingTarget the chargingTarget to set
     */
    public void setChargingTarget(int chargingTarget) {
        this.chargingTarget = chargingTarget;
    }

    /**
     * @return the chargingLevelPercent
     */
    public int getChargingLevelPercent() {
        return chargingLevelPercent;
    }

    /**
     * @param chargingLevelPercent the chargingLevelPercent to set
     */
    public void setChargingLevelPercent(int chargingLevelPercent) {
        this.chargingLevelPercent = chargingLevelPercent;
    }

    /**
     * @return the remainingChargingMinutes
     */
    public int getRemainingChargingMinutes() {
        return remainingChargingMinutes;
    }

    /**
     * @param remainingChargingMinutes the remainingChargingMinutes to set
     */
    public void setRemainingChargingMinutes(int remainingChargingMinutes) {
        this.remainingChargingMinutes = remainingChargingMinutes;
    }

    /**
     * @return the range
     */
    public int getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(int range) {
        this.range = range;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "ElectricChargingState [chargingConnectionType=" + chargingConnectionType + ", chargingStatus="
                + chargingStatus + ", isChargerConnected=" + isChargerConnected + ", chargingTarget=" + chargingTarget
                + ", chargingLevelPercent=" + chargingLevelPercent + ", remainingChargingMinutes="
                + remainingChargingMinutes + ", range=" + range + "]";
    }
}
