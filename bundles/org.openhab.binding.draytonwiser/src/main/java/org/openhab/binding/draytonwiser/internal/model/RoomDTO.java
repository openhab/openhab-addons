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
package org.openhab.binding.draytonwiser.internal.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class RoomDTO {

    @SerializedName("id")
    private Integer id;
    private Integer overrideSetpoint;
    private Integer roomStatId;
    private List<Integer> smartValveIds;
    private String name;
    private String mode;
    private String demandType;
    private int calculatedTemperature;
    private int currentSetPoint;
    private Integer percentageDemand;
    private String controlOutputState;
    private String windowState;
    private Integer displayedSetPoint;
    private String overrideType;
    private Boolean windowDetectionActive;
    private Integer overrideTimeoutUnixTime;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Integer getOverrideTimeoutUnixTime() {
        return overrideTimeoutUnixTime;
    }

    public Integer getOverrideSetpoint() {
        return overrideSetpoint;
    }

    public void setOverrideSetpoint(final Integer overrideSetpoint) {
        this.overrideSetpoint = overrideSetpoint;
    }

    public Integer getRoomStatId() {
        return roomStatId;
    }

    public void setRoomStatId(final Integer roomStatId) {
        this.roomStatId = roomStatId;
    }

    public List<Integer> getSmartValveIds() {
        return smartValveIds;
    }

    public void setSmartValveIds(final List<Integer> smartValveIds) {
        this.smartValveIds = smartValveIds;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(final String mode) {
        this.mode = mode;
    }

    public String getDemandType() {
        return demandType;
    }

    public void setDemandType(final String demandType) {
        this.demandType = demandType;
    }

    public int getCalculatedTemperature() {
        return calculatedTemperature;
    }

    public int getCurrentSetPoint() {
        return currentSetPoint < 0 ? 0 : currentSetPoint;
    }

    public void setCurrentSetPoint(final Integer currentSetPoint) {
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

    public void setDisplayedSetPoint(final Integer displayedSetPoint) {
        this.displayedSetPoint = displayedSetPoint;
    }

    public String getOverrideType() {
        return overrideType;
    }

    public Boolean getWindowDetectionActive() {
        return windowDetectionActive;
    }
}
