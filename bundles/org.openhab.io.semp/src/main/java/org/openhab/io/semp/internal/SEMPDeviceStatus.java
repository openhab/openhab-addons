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
 * SEMP consumers status
 *
 * @author Markus Eckhardt - Initial Contribution
 */
public class SEMPDeviceStatus {
    /*
     * Boolean that indicates if the device is currently considering the control signals or recommendations provided by
     * the energy manager or if it is
     * in a mode which ignores the signals or recommendations
     */
    private Boolean eMSignalsAccepted;

    /*
     * String that provides information of the current status of the device (Offline, On, Off).
     * See StatusRefType for known values.
     */
    private String status;

    /*
     * Identifies the current error state of the device. If the code is 0, no error is pending.
     */
    private Integer errorCode; /* occurs: 0 .. 1 */

    /*
     * Real average power within the interval in Watts.
     */
    private Double averagePower;

    /*
     * Minimum power value within the interval in Watts.
     */
    private Double minPower; /* occurs: 0 .. 1 */

    /*
     * Maximum power within the interval in Watts.
     */
    private Double maxPower; /* occurs: 0 .. 1 */

    /*
     * Timestamp that represents the end of the averaging interval.
     * Although this element is marked as optional it is mandatory in PowerConsumption:PowerInfo.
     */
    private Long timestamp;

    /*
     * Length of the averaging interval in seconds.
     * Although this element is marked as optional it is mandatory in PowerConsumption:PowerInfo.
     */
    private Integer averagingInterval;

    /*
     * Standard deviation within the interval in Watts.
     */
    private Double stdDevPower;

    /*
     * Skewness within the interval in Watts.
     */
    private Double skewPower;

    public SEMPDeviceStatus() {
    }

    /*
     * Setter for eMSignalsAccepted
     */
    public void setEMSignalsAccepted(boolean eMSignalsAccepted) {
        this.eMSignalsAccepted = eMSignalsAccepted;
    }

    /*
     * Getter for eMSignalsAccepted
     */
    public boolean getEMSignalsAccepted() {
        return eMSignalsAccepted;
    }

    /*
     * Checks if field eMSignalsAccepted is set
     */
    public boolean isEMSignalsAcceptedSet() {
        if (eMSignalsAccepted == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /*
     * Getter for status
     */
    public String getStatus() {
        return status;
    }

    /*
     * Checks if field status is set
     */
    public boolean isStatusSet() {
        if (status == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for errorCode
     */
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /*
     * Getter for errorCode
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /*
     * Checks if field errorCode is set
     */
    public boolean isErrorCodeSet() {
        if (errorCode == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for averagePower
     */
    public void setAveragePower(Double averagePower) {
        this.averagePower = averagePower;
    }

    /*
     * Getter for averagePower
     */
    public Double getAveragePower() {
        return averagePower;
    }

    /*
     * Checks if field averagePower is set
     */
    public boolean isAveragePowerSet() {
        if (averagePower == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * !
     *
     * Setter for minPower
     */
    public void setMinPower(Double minPower) {
        this.minPower = minPower;
    }

    /*
     * Getter for minPower
     */
    public Double getMinPower() {
        return minPower;
    }

    /*
     * Checks if field minPower is set
     */
    public boolean isMinPowerSet() {
        if (minPower == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for maxPower
     */
    public void setMaxPower(Double maxPower) {
        this.maxPower = maxPower;
    }

    /*
     * Getter for maxPower
     */
    public Double getMaxPower() {
        return maxPower;
    }

    /*
     * Checks if field maxPower is set
     */
    public boolean isMaxPowerSet() {
        if (maxPower == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for RelOrAbsTime
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /*
     * Getter for RelOrAbsTime
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /*
     * Checks if field RelOrAbsTime is set
     */
    public boolean isTimestampSet() {
        if (timestamp == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for averagingInterval
     */
    public void setAveragingInterval(Integer averagingInterval) {
        this.averagingInterval = averagingInterval;
    }

    /*
     * Getter for averagingInterval
     */
    public Integer getAveragingInterval() {
        return averagingInterval;
    }

    /*
     * Checks if field averagingInterval is set
     */
    public boolean isAveragingIntervalSet() {
        if (averagingInterval == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Checks if field stdDevPower is set
     */
    public boolean isStdDevPowerSet() {
        if (stdDevPower == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Getter for stdDevPower
     */
    public Double getStdDevPower() {
        return stdDevPower;
    }

    /*
     * Setter for stdDevPower
     */
    public void setStdDevPower(double stdDevPower) {
        this.stdDevPower = stdDevPower;
    }

    /*
     * Checks if field skewPower is set
     */
    public boolean isSkewPowerSet() {
        if (skewPower == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Getter for skewPower
     */
    public Double getSkewPower() {
        return skewPower;
    }

    /*
     * Setter for skewPower
     */
    public void setSkewPower(double skewPower) {
        this.skewPower = skewPower;
    }
}
