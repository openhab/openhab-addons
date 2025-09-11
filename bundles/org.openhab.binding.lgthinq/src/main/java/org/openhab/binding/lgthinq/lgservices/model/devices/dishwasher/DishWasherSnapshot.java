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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link DishWasherSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class DishWasherSnapshot extends AbstractSnapshotDefinition {
    private DevicePowerState powerState = DevicePowerState.DV_POWER_UNK;
    private String state = "";
    private String processState = "";
    private boolean online;
    private String course = "";
    private String smartCourse = "";
    private String doorLock = "";
    private Double remainingHour = 0.00;
    private Double remainingMinute = 0.00;
    private Double reserveHour = 0.00;
    private Double reserveMinute = 0.00;

    @JsonAlias({ "Course" })
    @JsonProperty("course")
    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @JsonProperty("process")
    @JsonAlias({ "Process" })
    public String getProcessState() {
        return processState;
    }

    public void setProcessState(String processState) {
        this.processState = processState;
    }

    @Override
    public DevicePowerState getPowerStatus() {
        return powerState;
    }

    @Override
    public void setPowerStatus(DevicePowerState value) {
        this.powerState = value;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @JsonProperty("state")
    @JsonAlias({ "State" })
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        if (state.equals(DW_POWER_OFF_VALUE)) {
            powerState = DevicePowerState.DV_POWER_OFF;
        } else {
            powerState = DevicePowerState.DV_POWER_ON;
        }
    }

    @JsonProperty("smartCourse")
    @JsonAlias({ "SmartCourse" })
    public String getSmartCourse() {
        return smartCourse;
    }

    public void setSmartCourse(String smartCourse) {
        this.smartCourse = smartCourse;
    }

    @JsonIgnore
    public String getRemainingTime() {
        return String.format("%02.0f:%02.0f", getRemainingHour(), getRemainingMinute());
    }

    @JsonIgnore
    public String getReserveTime() {
        return String.format("%02.0f:%02.0f", getReserveHour(), getReserveMinute());
    }

    @JsonProperty("remainTimeHour")
    @JsonAlias({ "Remain_Time_H" })
    public Double getRemainingHour() {
        return remainingHour;
    }

    public void setRemainingHour(Double remainingHour) {
        this.remainingHour = remainingHour;
    }

    @JsonProperty("remainTimeMinute")
    @JsonAlias({ "Remain_Time_M" })
    public Double getRemainingMinute() {
        // Issue in some DW when the remainingMinute stay in 1 after complete in some cases
        return DW_STATE_COMPLETE.equals(getState()) ? 0.0 : remainingMinute;
    }

    public void setRemainingMinute(Double remainingMinute) {
        this.remainingMinute = remainingMinute;
    }

    @JsonProperty("reserveTimeHour")
    @JsonAlias({ "Reserve_Time_H" })
    public Double getReserveHour() {
        return reserveHour;
    }

    public void setReserveHour(Double reserveHour) {
        this.reserveHour = reserveHour;
    }

    @JsonProperty("reserveTimeMinute")
    @JsonAlias({ "Reserve_Time_M" })
    public Double getReserveMinute() {
        return reserveMinute;
    }

    public void setReserveMinute(Double reserveMinute) {
        this.reserveMinute = reserveMinute;
    }

    @JsonProperty("door")
    @JsonAlias({ "Door" })
    public String getDoorLock() {
        return doorLock;
    }

    public void setDoorLock(String doorLock) {
        this.doorLock = doorLock;
    }
}
