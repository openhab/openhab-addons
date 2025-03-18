/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author MikeTheTux - Initial contribution
 */
public class Statistics {
    private long cuttingBladeUsageTime;
    private long downTime;
    private int numberOfChargingCycles;
    private int numberOfCollisions;
    private long totalChargingTime;
    private long totalCuttingTime;
    private long totalDriveDistance; // docu states totalDrivenDistance which does not work
    private long totalRunningTime;
    private long totalSearchingTime;
    private long upTime;

    public long getCuttingBladeUsageTime() {
        return cuttingBladeUsageTime;
    }

    public void setCuttingBladeUsageTime(long cuttingBladeUsageTime) {
        this.cuttingBladeUsageTime = cuttingBladeUsageTime;
    }

    public long getDownTime() {
        return downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public int getNumberOfChargingCycles() {
        return numberOfChargingCycles;
    }

    public void setNumberOfChargingCycles(int numberOfChargingCycles) {
        this.numberOfChargingCycles = numberOfChargingCycles;
    }

    public int getNumberOfCollisions() {
        return numberOfCollisions;
    }

    public void setNumberOfCollisions(int numberOfCollisions) {
        this.numberOfCollisions = numberOfCollisions;
    }

    public long getTotalChargingTime() {
        return totalChargingTime;
    }

    public void setTotalChargingTime(long totalChargingTime) {
        this.totalChargingTime = totalChargingTime;
    }

    public long getTotalCuttingTime() {
        return totalCuttingTime;
    }

    public void setTotalCuttingTime(long totalCuttingTime) {
        this.totalCuttingTime = totalCuttingTime;
    }

    public long getTotalDriveDistance() {
        return totalDriveDistance;
    }

    public void setTotalDriveDistance(long totalDriveDistance) {
        this.totalDriveDistance = totalDriveDistance;
    }

    public long getTotalRunningTime() {
        return totalRunningTime;
    }

    public void setTotalRunningTime(long totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
    }

    public long getTotalSearchingTime() {
        return totalSearchingTime;
    }

    public void setTotalSearchingTime(long totalSearchingTime) {
        this.totalSearchingTime = totalSearchingTime;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }
}
