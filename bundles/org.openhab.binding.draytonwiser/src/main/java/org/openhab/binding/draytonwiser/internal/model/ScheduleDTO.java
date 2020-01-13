/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class ScheduleDTO {

    @SerializedName("id")
    private Integer id;
    private ScheduleDayDTO monday;
    private ScheduleDayDTO tuesday;
    private ScheduleDayDTO wednesday;
    private ScheduleDayDTO thursday;
    private ScheduleDayDTO friday;
    private ScheduleDayDTO saturday;
    private ScheduleDayDTO sunday;
    private String type;
    private Integer currentSetpoint;
    private Integer nextEventTime;
    private Integer nextEventSetpoint;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public ScheduleDayDTO getMonday() {
        return monday;
    }

    public void setMonday(final ScheduleDayDTO monday) {
        this.monday = monday;
    }

    public ScheduleDayDTO getTuesday() {
        return tuesday;
    }

    public void setTuesday(final ScheduleDayDTO tuesday) {
        this.tuesday = tuesday;
    }

    public ScheduleDayDTO getWednesday() {
        return wednesday;
    }

    public void setWednesday(final ScheduleDayDTO wednesday) {
        this.wednesday = wednesday;
    }

    public ScheduleDayDTO getThursday() {
        return thursday;
    }

    public void setThursday(final ScheduleDayDTO thursday) {
        this.thursday = thursday;
    }

    public ScheduleDayDTO getFriday() {
        return friday;
    }

    public void setFriday(final ScheduleDayDTO friday) {
        this.friday = friday;
    }

    public ScheduleDayDTO getSaturday() {
        return saturday;
    }

    public void setSaturday(final ScheduleDayDTO saturday) {
        this.saturday = saturday;
    }

    public ScheduleDayDTO getSunday() {
        return sunday;
    }

    public void setSunday(final ScheduleDayDTO sunday) {
        this.sunday = sunday;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Integer getCurrentSetpoint() {
        return currentSetpoint;
    }

    public void setCurrentSetpoint(final Integer currentSetpoint) {
        this.currentSetpoint = currentSetpoint;
    }

    public Integer getNextEventTime() {
        return nextEventTime;
    }

    public void setNextEventTime(final Integer nextEventTime) {
        this.nextEventTime = nextEventTime;
    }

    public Integer getNextEventSetpoint() {
        return nextEventSetpoint;
    }

    public void setNextEventSetpoint(final Integer nextEventSetpoint) {
        this.nextEventSetpoint = nextEventSetpoint;
    }

}
