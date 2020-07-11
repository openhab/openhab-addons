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

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Model for a thermostat
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class Thermostat {

    @SerializedName("Id")
    public Integer id = 0;
    @SerializedName("Action")
    public Integer action = 0;
    @SerializedName("SerialNumber")
    public String serialNumber = "";
    @SerializedName("GroupName")
    public String groupName = "";
    @SerializedName("GroupId")
    public Integer groupId = 0;
    @SerializedName("CustomerId")
    public Integer customerId = 0;
    @SerializedName("SWversion")
    public String sWversion = "";
    @SerializedName("Online")
    public Boolean online = false;
    @SerializedName("Heating")
    public Boolean heating = false;
    @SerializedName("RoomTemperature")
    public Integer roomTemperature = 0;
    @SerializedName("FloorTemperature")
    public Integer floorTemperature = 0;
    @SerializedName("RegulationMode")
    public Integer regulationMode = 0;
    @SerializedName("Schedule")
    public @Nullable Schedule schedule;
    @SerializedName("ComfortSetpoint")
    public Integer comfortSetpoint = 0;
    @SerializedName("ComfortEndTime")
    public Date comfortEndTime = new Date();
    @SerializedName("ManualModeSetpoint")
    public Integer manualModeSetpoint = 0;
    @SerializedName("VacationEnabled")
    public Boolean vacationEnabled = false;
    @SerializedName("VacationBeginDay")
    public Date vacationBeginDay = new Date();
    @SerializedName("VacationEndDay")
    public Date vacationEndDay = new Date();
    @SerializedName("VacationTemperature")
    public Integer vacationTemperature = 0;
    @SerializedName("LastPrimaryModeIsAuto")
    public Boolean lastPrimaryModeIsAuto = false;
    @SerializedName("BoostEndTime")
    public Date boostEndTime = new Date();
    @SerializedName("FrostProtectionTemperature")
    public Integer frostProtectionTemperature = 0;
    @SerializedName("ErrorCode")
    public Integer errorCode = 0;
    @SerializedName("ThermostatName")
    public String thermostatName = "";
    @SerializedName("OpenWindow")
    public Boolean openWindow = false;
    @SerializedName("AdaptiveMode")
    public Boolean adaptiveMode = false;
    @SerializedName("DaylightSaving")
    public Boolean daylightSaving = false;
    @SerializedName("SensorAppl")
    public Integer sensorAppl = 0;
    @SerializedName("MinSetpoint")
    public Integer minSetpoint = 0;
    @SerializedName("MaxSetpoint")
    public Integer maxSetpoint = 0;
    @SerializedName("TimeZone")
    public Integer timeZone = 0;
    @SerializedName("DaylightSavingActive")
    public Boolean daylightSavingActive = false;
    @SerializedName("FloorType")
    public Integer floorType = 0;
}
