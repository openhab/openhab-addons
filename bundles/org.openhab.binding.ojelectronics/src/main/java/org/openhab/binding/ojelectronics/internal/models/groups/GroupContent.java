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

import com.google.gson.annotations.SerializedName;

/**
 * Model for content of a group
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class GroupContent {

    @SerializedName("Action")
    public Integer action = 0;
    @SerializedName("GroupId")
    public Integer groupId = 0;
    @SerializedName("GroupName")
    public String groupName = "";
    @SerializedName("Thermostats")
    public List<Thermostat> thermostats = new ArrayList<Thermostat>();
    @SerializedName("RegulationMode")
    public Integer regulationMode = 0;
    @SerializedName("Schedule")
    public @Nullable Schedule schedule;
    @SerializedName("ComfortSetpoint")
    public Integer comfortSetpoint = 0;
    @SerializedName("ComfortEndTime")
    public String comfortEndTime = "";
    @SerializedName("ManualModeSetpoint")
    public Integer manualModeSetpoint = 0;
    @SerializedName("VacationEnabled")
    public Boolean vacationEnabled = false;
    @SerializedName("VacationBeginDay")
    public String vacationBeginDay = "";
    @SerializedName("VacationEndDay")
    public String vacationEndDay = "";
    @SerializedName("VacationTemperature")
    public Integer vacationTemperature = 0;
    @SerializedName("LastPrimaryModeIsAuto")
    public Boolean lastPrimaryModeIsAuto = false;
    @SerializedName("BoostEndTime")
    public String boostEndTime = "";
    @SerializedName("FrostProtectionTemperature")
    public Integer frostProtectionTemperature = 0;
}
