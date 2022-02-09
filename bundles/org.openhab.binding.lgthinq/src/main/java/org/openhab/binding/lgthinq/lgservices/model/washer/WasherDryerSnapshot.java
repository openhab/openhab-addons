/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.WM_POWER_OFF_VALUE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.Snapshot;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link WasherDryerSnapshot}
 * This map the snapshot result from Washing Machine devices
 * This json payload come with path: snapshot->washerDryer, but this POJO expects
 * to map field below washerDryer
 * 
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class WasherDryerSnapshot implements Snapshot {
    private DevicePowerState powerState = DevicePowerState.DV_POWER_UNK;
    private String state = "";
    private boolean online;
    private String course = "";
    private String smartCourse = "";

    @JsonAlias({ "Course", "courseFL24inchBaseTitan" })
    @JsonProperty("courseFL24inchBaseTitan")
    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public DevicePowerState getPowerStatus() {
        return powerState;
    }

    @Override
    public void setPowerStatus(DevicePowerState value) {
        throw new IllegalArgumentException("This method must not be accessed.");
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

    @JsonProperty("smartCourseFL24inchBaseTitan")
    @JsonAlias({ "smartCourseFL24inchBaseTitan", "SmartCourse" })
    public String getSmartCourse() {
        return smartCourse;
    }

    public void setSmartCourse(String smartCourse) {
        this.smartCourse = smartCourse;
    }

    public void setState(String state) {
        this.state = state;
        if (state.equals(WM_POWER_OFF_VALUE)) {
            powerState = DevicePowerState.DV_POWER_OFF;
        } else {
            powerState = DevicePowerState.DV_POWER_ON;
        }
    }
}
