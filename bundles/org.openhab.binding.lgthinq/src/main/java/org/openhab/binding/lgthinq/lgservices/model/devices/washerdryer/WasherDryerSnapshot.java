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
package org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.WMD_POWER_OFF_VALUE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link WasherDryerSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class WasherDryerSnapshot extends AbstractSnapshotDefinition {
    private DevicePowerState powerState = DevicePowerState.DV_POWER_UNK;
    private String state = "";
    private String processState = "";
    private boolean online;
    private String course = "";
    private String smartCourse = "";
    private String downloadedCourse = "";
    private String temperatureLevel = "";
    private String doorLock = "";
    private String option1 = "";
    private String option2 = "";
    private String childLock = "";
    private Double remainingHour = 0.00;
    private Double remainingMinute = 0.00;
    private Double reserveHour = 0.00;
    private Double reserveMinute = 0.00;

    private String remoteStart = "";
    private boolean remoteStartEnabled = false;
    private String standByStatus = "";

    private String dryLevel = "";
    private boolean standBy = false;
    private String error = "";
    private String rinse = "";
    private String spin = "";

    private String loadItem = "";

    public String getLoadItem() {
        return loadItem;
    }

    @JsonAlias({ "LoadItem" })
    @JsonProperty("loadItemWasher")
    public void setLoadItem(String loadItem) {
        this.loadItem = loadItem;
    }

    @JsonAlias({ "Course", "courseFL24inchBaseTitan" })
    @JsonProperty("courseFL24inchBaseTitan")
    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @JsonProperty("dryLevel")
    @JsonAlias({ "DryLevel" })
    public String getDryLevel() {
        return dryLevel;
    }

    public void setDryLevel(String dryLevel) {
        this.dryLevel = dryLevel;
    }

    @JsonProperty("processState")
    @JsonAlias({ "ProcessState", "preState", "PreState" })
    public String getProcessState() {
        return processState;
    }

    public void setProcessState(String processState) {
        this.processState = processState;
    }

    @JsonProperty("error")
    @JsonAlias({ "Error" })
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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
    @JsonAlias({ "state", "State" })
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        if (state.equals(WMD_POWER_OFF_VALUE)) {
            powerState = DevicePowerState.DV_POWER_OFF;
        } else {
            powerState = DevicePowerState.DV_POWER_ON;
        }
    }

    @JsonProperty("smartCourseFL24inchBaseTitan")
    @JsonAlias({ "smartCourseFL24inchBaseTitan", "SmartCourse" })
    public String getSmartCourse() {
        return smartCourse;
    }

    public void setSmartCourse(String smartCourse) {
        this.smartCourse = smartCourse;
    }

    @JsonProperty("downloadedCourseFL24inchBaseTitan")
    @JsonAlias({ "downloadedCourseFLUpper25inchBaseUS" })
    public String getDownloadedCourse() {
        return downloadedCourse;
    }

    public void setDownloadedCourse(String downloadedCourse) {
        this.downloadedCourse = downloadedCourse;
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
    @JsonAlias({ "remainTimeHour", "Remain_Time_H" })
    public Double getRemainingHour() {
        return remainingHour;
    }

    public void setRemainingHour(Double remainingHour) {
        this.remainingHour = remainingHour;
    }

    @JsonProperty("remainTimeMinute")
    @JsonAlias({ "remainTimeMinute", "Remain_Time_M" })
    public Double getRemainingMinute() {
        return remainingMinute;
    }

    public void setRemainingMinute(Double remainingMinute) {
        this.remainingMinute = remainingMinute;
    }

    @JsonProperty("reserveTimeHour")
    @JsonAlias({ "reserveTimeHour", "Reserve_Time_H" })
    public Double getReserveHour() {
        return reserveHour;
    }

    public void setReserveHour(Double reserveHour) {
        this.reserveHour = reserveHour;
    }

    @JsonProperty("reserveTimeMinute")
    @JsonAlias({ "reserveTimeMinute", "Reserve_Time_M" })
    public Double getReserveMinute() {
        return reserveMinute;
    }

    public void setReserveMinute(Double reserveMinute) {
        this.reserveMinute = reserveMinute;
    }

    @JsonProperty("temp")
    @JsonAlias({ "WaterTemp" })
    public String getTemperatureLevel() {
        return temperatureLevel;
    }

    public void setTemperatureLevel(String temperatureLevel) {
        this.temperatureLevel = temperatureLevel;
    }

    @JsonProperty("doorLock")
    @JsonAlias({ "DoorLock", "DoorClose" })
    public String getDoorLock() {
        return doorLock;
    }

    public void setDoorLock(String doorLock) {
        this.doorLock = doorLock;
    }

    @JsonProperty("ChildLock")
    @JsonAlias({ "childLock" })
    public String getChildLock() {
        return childLock;
    }

    public void setChildLock(String childLock) {
        this.childLock = childLock;
    }

    public boolean isRemoteStartEnabled() {
        return remoteStartEnabled;
    }

    @JsonProperty("remoteStart")
    @JsonAlias({ "RemoteStart" })
    public String getRemoteStart() {
        return remoteStart;
    }

    public void setRemoteStart(String remoteStart) {
        this.remoteStart = remoteStart.contains("ON") || "1".equals(remoteStart) ? "ON"
                : (remoteStart.contains("OFF") || "0".equals(remoteStart) ? "OFF" : remoteStart);
        remoteStartEnabled = "ON".equals(this.remoteStart);
    }

    @JsonProperty("standby")
    @JsonAlias({ "Standby" })
    public String getStandByStatus() {
        return standByStatus;
    }

    public void setStandByStatus(String standByStatus) {
        this.standByStatus = standByStatus.contains("ON") || "1".equals(standByStatus) ? "ON"
                : (standByStatus.contains("OFF") || "0".equals(standByStatus) ? "OFF" : standByStatus);
        standBy = this.standByStatus.contains("ON");
    }

    public boolean isStandBy() {
        return standBy;
    }

    @JsonProperty("rinse")
    @JsonAlias({ "RinseOption" })
    public String getRinse() {
        return rinse;
    }

    public void setRinse(String rinse) {
        this.rinse = rinse;
    }

    @JsonProperty("spin")
    @JsonAlias({ "SpinSpeed" })
    public String getSpin() {
        return spin;
    }

    public void setSpin(String spin) {
        this.spin = spin;
    }

    @JsonProperty("Option1")
    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    @JsonProperty("Option2")
    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }
}
