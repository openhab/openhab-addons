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
package org.openhab.binding.ojelectronics.internal.models.thermostat;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ojelectronics.internal.models.groups.ScheduleModel;

import com.google.gson.annotations.SerializedName;

/**
 * Model for a thermostat
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class ThermostatModel extends ThermostatModelBase {

    public int id;

    public int action;

    public String groupName = "";

    public int groupId;

    public int customerId;

    @SerializedName("SWversion")
    public String softwareVersion = "";

    public boolean online;

    public boolean heating;

    public int roomTemperature;

    public int floorTemperature;

    public int regulationMode;

    public @Nullable ScheduleModel schedule;

    public int comfortSetpoint;

    public Date comfortEndTime = new Date();

    public int manualModeSetpoint;

    public boolean vacationEnabled;

    public Date vacationBeginDay = new Date();

    public Date vacationEndDay = new Date();

    public int vacationTemperature;

    public boolean lastPrimaryModeIsAuto;

    public Date boostEndTime = new Date();

    public int frostProtectionTemperature;

    public int errorCode;

    public String thermostatName = "";

    public boolean openWindow;

    public boolean adaptiveMode;

    public boolean daylightSaving;

    public int sensorAppl;

    public int minSetpoint;

    public int maxSetpoint;

    public int timeZone;

    public boolean daylightSavingActive;

    public int floorType;
}
