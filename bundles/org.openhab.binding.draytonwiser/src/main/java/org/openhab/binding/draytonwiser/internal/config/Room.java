/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class Room {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("OverrideSetpoint")
    @Expose
    private Integer overrideSetpoint;
    @SerializedName("RoomStatId")
    @Expose
    private Integer roomStatId;
    @SerializedName("SmartValveIds")
    @Expose
    private List<Integer> smartValveIds = null;
    @SerializedName("ScheduleId")
    @Expose
    private Integer scheduleId;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Mode")
    @Expose
    private String mode;
    @SerializedName("DemandType")
    @Expose
    private String demandType;
    @SerializedName("CalculatedTemperature")
    @Expose
    private Integer calculatedTemperature;
    @SerializedName("CurrentSetPoint")
    @Expose
    private Integer currentSetPoint;
    @SerializedName("PercentageDemand")
    @Expose
    private Integer percentageDemand;
    @SerializedName("ControlOutputState")
    @Expose
    private String controlOutputState;
    @SerializedName("WindowState")
    @Expose
    private String windowState;
    @SerializedName("DisplayedSetPoint")
    @Expose
    private Integer displayedSetPoint;
    @SerializedName("OverrideType")
    @Expose
    private String overrideType;
    @SerializedName("WindowDetectionActive")
    @Expose
    private Boolean windowDetectionActive;
    @SerializedName("OverrideTimeoutUnixTime")
    @Expose
    private Integer overrideTimeoutUnixTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOverrideTimeoutUnixTime() {
        return overrideTimeoutUnixTime;
    }

    public Integer getOverrideSetpoint() {
        return overrideSetpoint;
    }

    public void setOverrideSetpoint(Integer overrideSetpoint) {
        this.overrideSetpoint = overrideSetpoint;
    }

    public Integer getRoomStatId() {
        return roomStatId;
    }

    public void setRoomStatId(Integer roomStatId) {
        this.roomStatId = roomStatId;
    }

    public List<Integer> getSmartValveIds() {
        return smartValveIds;
    }

    public void setSmartValveIds(List<Integer> smartValveIds) {
        this.smartValveIds = smartValveIds;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDemandType() {
        return demandType;
    }

    public void setDemandType(String demandType) {
        this.demandType = demandType;
    }

    public Integer getCalculatedTemperature() {
        return calculatedTemperature;
    }

    public Integer getCurrentSetPoint() {
        return currentSetPoint < 0 ? 0 : currentSetPoint;
    }

    public void setCurrentSetPoint(Integer currentSetPoint) {
        this.currentSetPoint = currentSetPoint;
    }

    public Integer getPercentageDemand() {
        return percentageDemand;
    }

    public String getControlOutputState() {
        return controlOutputState;
    }

    public String getWindowState() {
        return windowState;
    }

    public Integer getDisplayedSetPoint() {
        return displayedSetPoint;
    }

    public void setDisplayedSetPoint(Integer displayedSetPoint) {
        this.displayedSetPoint = displayedSetPoint;
    }

    public String getOverrideType() {
        return overrideType;
    }

    public Boolean getWindowDetectionActive() {
        return windowDetectionActive;
    }

}
