/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class Schedule {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("Monday")
    @Expose
    private ScheduleDay monday;
    @SerializedName("Tuesday")
    @Expose
    private ScheduleDay tuesday;
    @SerializedName("Wednesday")
    @Expose
    private ScheduleDay wednesday;
    @SerializedName("Thursday")
    @Expose
    private ScheduleDay thursday;
    @SerializedName("Friday")
    @Expose
    private ScheduleDay friday;
    @SerializedName("Saturday")
    @Expose
    private ScheduleDay saturday;
    @SerializedName("Sunday")
    @Expose
    private ScheduleDay sunday;
    @SerializedName("Type")
    @Expose
    private String type;
    @SerializedName("CurrentSetpoint")
    @Expose
    private Integer currentSetpoint;
    @SerializedName("NextEventTime")
    @Expose
    private Integer nextEventTime;
    @SerializedName("NextEventSetpoint")
    @Expose
    private Integer nextEventSetpoint;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ScheduleDay getMonday() {
        return monday;
    }

    public void setMonday(ScheduleDay monday) {
        this.monday = monday;
    }

    public ScheduleDay getTuesday() {
        return tuesday;
    }

    public void setTuesday(ScheduleDay tuesday) {
        this.tuesday = tuesday;
    }

    public ScheduleDay getWednesday() {
        return wednesday;
    }

    public void setWednesday(ScheduleDay wednesday) {
        this.wednesday = wednesday;
    }

    public ScheduleDay getThursday() {
        return thursday;
    }

    public void setThursday(ScheduleDay thursday) {
        this.thursday = thursday;
    }

    public ScheduleDay getFriday() {
        return friday;
    }

    public void setFriday(ScheduleDay friday) {
        this.friday = friday;
    }

    public ScheduleDay getSaturday() {
        return saturday;
    }

    public void setSaturday(ScheduleDay saturday) {
        this.saturday = saturday;
    }

    public ScheduleDay getSunday() {
        return sunday;
    }

    public void setSunday(ScheduleDay sunday) {
        this.sunday = sunday;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCurrentSetpoint() {
        return currentSetpoint;
    }

    public void setCurrentSetpoint(Integer currentSetpoint) {
        this.currentSetpoint = currentSetpoint;
    }

    public Integer getNextEventTime() {
        return nextEventTime;
    }

    public void setNextEventTime(Integer nextEventTime) {
        this.nextEventTime = nextEventTime;
    }

    public Integer getNextEventSetpoint() {
        return nextEventSetpoint;
    }

    public void setNextEventSetpoint(Integer nextEventSetpoint) {
        this.nextEventSetpoint = nextEventSetpoint;
    }

}
