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
 * Time frame data object.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class SEMPTimeFrame {
    /*
     * Represents the earliest possible time the device can be switched on by the EM.
     * The combination of EarliestStart and LatestEnd specifies the interval in which the requested runtime or energy
     * has to be allocated by the EM.
     */
    private Integer earliestStart;

    /*
     * Represents the latest possible end time the requested minimum runtime (MinRunningTime) must be allocated to the
     * device. This means at the given time the device operation must be finished. If a runtime was requested, the
     * latest possible start of operation is LatestEnd-MinRunningTime.
     *
     * The combination of EarliestStart and LatestEnd specifies the interval in which the requested runtime or energy
     * has to be allocated by the EM.
     */
    private Integer latestEnd;

    /*
     * Minimum running time within the timeframe in seconds.
     * If MinRunningTime is 0, the operation of the device in this timeframe is optional.
     * Defaults to 0 if MaxRunningTime is set.
     */
    private Integer minRunningTime;

    /*
     * Maximum running time within the timeframe in seconds.
     * If MinRunningTime equals MaxRunningTime, all of the given runtime is required.
     * If MinRunningTime is lower than MaxRunningTime, the amount of runtime given by MinRunningTime is required. The
     * runtime difference between MinRunningTime and MaxRunningTime is optional. That means that the EM will only assign
     * the optional
     * runtime to the device if certain conditions like ecological constraints and/or price of energy are met.
     * Defaults to MinRunningTime if MinRunningTime is set.
     */
    private Integer maxRunningTime;

    /*
     * Integer that provides the timestamp of the last activation.
     */
    private Long timestampActivated;

    /*
     * Integer that provides the runtime of this time frame
     */
    private Integer currentRuntime;

    public SEMPTimeFrame() {
        currentRuntime = 0;
    }

    /*
     * Setter for currentRuntime
     */
    public void setCurrentRuntime(Integer currentRuntime) {
        this.currentRuntime = currentRuntime;
    }

    /*
     * Getter for currentRuntime
     */
    public Integer getCurrentRuntime() {
        return currentRuntime;
    }

    /*
     * Checks if field currentRuntime is set
     */
    public boolean isCurrentRuntimeSet() {
        if (currentRuntime == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for timestampActivated
     */
    public void setTimestampActivated(Long statusTimestamp) {
        this.timestampActivated = statusTimestamp;
    }

    /*
     * Getter for timestampActivated
     */
    public Long getTimestampActivated() {
        return timestampActivated;
    }

    /*
     * Checks if field timestampActivated is set
     */
    public boolean isTimestampActivatedSet() {
        if (timestampActivated == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for earliestStart
     */
    public void setEarliestStart(Integer earliestStart) {
        this.earliestStart = earliestStart;
    }

    /*
     * Getter for earliestStart
     */
    public Integer getEarliestStart() {
        return earliestStart;
    }

    /*
     * Checks if field earliestStart is set
     */
    public boolean isEarliestStartSet() {
        if (earliestStart == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for latestEnd
     */
    public void setLatestEnd(Integer latestEnd) {
        this.latestEnd = latestEnd;
    }

    /*
     * Getter for latestEnd
     */
    public Integer getLatestEnd() {
        return latestEnd;
    }

    /*
     * Checks if field latestEnd is set
     */
    public boolean isLatestEndSet() {
        if (latestEnd == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for minRunningTime
     */
    public void setMinRunningTime(Integer minRunningTime) {
        this.minRunningTime = minRunningTime;
    }

    /*
     * Getter for minRunningTime
     */
    public Integer getMinRunningTime() {
        return minRunningTime;
    }

    /*
     * Checks if field minRunningTime is set
     */
    public boolean isMinRunningTimeSet() {
        if (minRunningTime == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for maxRunningTime
     */
    public void setMaxRunningTime(Integer maxRunningTime) {
        this.maxRunningTime = maxRunningTime;
    }

    /*
     * Getter for maxRunningTime
     */
    public Integer getMaxRunningTime() {
        return maxRunningTime;
    }

    /*
     * Checks if field maxRunningTime is set
     */
    public boolean isMaxRunningTimeSet() {
        if (maxRunningTime == null) {
            return false;
        } else {
            return true;
        }
    }
}
