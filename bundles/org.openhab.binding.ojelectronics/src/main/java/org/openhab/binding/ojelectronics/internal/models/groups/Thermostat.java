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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model for a thermostat
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class Thermostat {

    @SerializedName("Id")
    @Expose
    public Integer id = 0;
    @SerializedName("Action")
    @Expose
    public Integer action = 0;
    @SerializedName("SerialNumber")
    @Expose
    public String serialNumber = "";
    @SerializedName("GroupName")
    @Expose
    public String groupName = "";
    @SerializedName("GroupId")
    @Expose
    public Integer groupId = 0;
    @SerializedName("CustomerId")
    @Expose
    public Integer customerId = 0;
    @SerializedName("SWversion")
    @Expose
    public String sWversion = "";
    @SerializedName("Online")
    @Expose
    public Boolean online = false;
    @SerializedName("Heating")
    @Expose
    public Boolean heating = false;
    @SerializedName("RoomTemperature")
    @Expose
    public Integer roomTemperature = 0;
    @SerializedName("FloorTemperature")
    @Expose
    public Integer floorTemperature = 0;
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
    public Date comfortEndTime = new Date();
    @SerializedName("ManualModeSetpoint")
    @Expose
    public Integer manualModeSetpoint = 0;
    @SerializedName("VacationEnabled")
    @Expose
    public Boolean vacationEnabled = false;
    @SerializedName("VacationBeginDay")
    @Expose
    public Date vacationBeginDay = new Date();
    @SerializedName("VacationEndDay")
    @Expose
    public Date vacationEndDay = new Date();
    @SerializedName("VacationTemperature")
    @Expose
    public Integer vacationTemperature = 0;
    @SerializedName("LastPrimaryModeIsAuto")
    @Expose
    public Boolean lastPrimaryModeIsAuto = false;
    @SerializedName("BoostEndTime")
    @Expose
    public Date boostEndTime = new Date();
    @SerializedName("FrostProtectionTemperature")
    @Expose
    public Integer frostProtectionTemperature = 0;
    @SerializedName("ErrorCode")
    @Expose
    public Integer errorCode = 0;
    @SerializedName("ThermostatName")
    @Expose
    public String thermostatName = "";
    @SerializedName("OpenWindow")
    @Expose
    public Boolean openWindow = false;
    @SerializedName("AdaptiveMode")
    @Expose
    public Boolean adaptiveMode = false;
    @SerializedName("DaylightSaving")
    @Expose
    public Boolean daylightSaving = false;
    @SerializedName("SensorAppl")
    @Expose
    public Integer sensorAppl = 0;
    @SerializedName("MinSetpoint")
    @Expose
    public Integer minSetpoint = 0;
    @SerializedName("MaxSetpoint")
    @Expose
    public Integer maxSetpoint = 0;
    @SerializedName("TimeZone")
    @Expose
    public Integer timeZone = 0;
    @SerializedName("DaylightSavingActive")
    @Expose
    public Boolean daylightSavingActive = false;
    @SerializedName("FloorType")
    @Expose
    public Integer floorType = 0;
}
