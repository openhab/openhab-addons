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
package org.openhab.binding.ojelectronics.internal.models.groups;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model for content of a group
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class GroupContent {

    @SerializedName("Action")
    @Expose
    public Integer action = 0;
    @SerializedName("GroupId")
    @Expose
    public Integer groupId = 0;
    @SerializedName("GroupName")
    @Expose
    public String groupName = "";
    @SerializedName("Thermostats")
    @Expose
    public List<Thermostat> thermostats = new ArrayList<Thermostat>();
    @SerializedName("RegulationMode")
    @Expose
    public Integer regulationMode = 0;
    @SerializedName("Schedule")
    @Expose
    public @Nullable Schedule schedule;
    @SerializedName("ComfortSetpoint")
    @Expose
    public Integer comfortSetpoint = 0;
    @SerializedName("ComfortEndTime")
    @Expose
    public String comfortEndTime = "";
    @SerializedName("ManualModeSetpoint")
    @Expose
    public Integer manualModeSetpoint = 0;
    @SerializedName("VacationEnabled")
    @Expose
    public Boolean vacationEnabled = false;
    @SerializedName("VacationBeginDay")
    @Expose
    public String vacationBeginDay = "";
    @SerializedName("VacationEndDay")
    @Expose
    public String vacationEndDay = "";
    @SerializedName("VacationTemperature")
    @Expose
    public Integer vacationTemperature = 0;
    @SerializedName("LastPrimaryModeIsAuto")
    @Expose
    public Boolean lastPrimaryModeIsAuto = false;
    @SerializedName("BoostEndTime")
    @Expose
    public String boostEndTime = "";
    @SerializedName("FrostProtectionTemperature")
    @Expose
    public Integer frostProtectionTemperature = 0;

}
