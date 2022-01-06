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
package org.openhab.binding.ecobee.internal.dto.thermostat;

/**
 * The {@link SettingsDTO} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SettingsDTO {

    /*
     * The current HVAC mode the thermostat is in. Values: auto, auxHeatOnly, cool, heat, off.
     */
    public String hvacMode;

    /*
     * The last service date of the HVAC equipment.
     */
    public String lastServiceDate;

    /*
     * Whether to send an alert when service is required again.
     */
    public Boolean serviceRemindMe;

    /*
     * The user configured monthly interval between HVAC service reminders
     */
    public Integer monthsBetweenService;

    /*
     * Date to be reminded about the next HVAC service date.
     */
    public String remindMeDate;

    /*
     * The ventilator mode. Values: auto, minontime, on, off.
     */
    public String vent;

    /*
     * The minimum time in minutes the ventilator is configured to run. The thermostat will always
     * guarantee that the ventilator runs for this minimum duration whenever engaged.
     */
    public Integer ventilatorMinOnTime;

    /*
     * Whether the technician associated with this thermostat should receive the HVAC service reminders as well.
     */
    public Boolean serviceRemindTechnician;

    /*
     * A note about the physical location where the SMART or EMS Equipment Interface module is located.
     */
    public String eiLocation;

    /*
     * The temperature at which a cold temp alert is triggered.
     */
    public Integer coldTempAlert;

    /*
     * Whether cold temperature alerts are enabled.
     */
    public Boolean coldTempAlertEnabled;

    /*
     * The temperature at which a hot temp alert is triggered.
     */
    public Integer hotTempAlert;

    /*
     * Whether hot temperature alerts are enabled.
     */
    public Boolean hotTempAlertEnabled;

    /*
     * The number of cool stages the connected HVAC equipment supports.
     */
    public Integer coolStages;

    /*
     * The number of heat stages the connected HVAC equipment supports.
     */
    public Integer heatStages;

    /*
     * The maximum automated set point set back offset allowed in degrees.
     */
    public Integer maxSetBack;

    /*
     * The maximum automated set point set forward offset allowed in degrees.
     */
    public Integer maxSetForward;

    /*
     * The set point set back offset, in degrees, configured for a quick save event.
     */
    public Integer quickSaveSetBack;

    /*
     * The set point set forward offset, in degrees, configured for a quick save event.
     */
    public Integer quickSaveSetForward;

    /*
     * Whether the thermostat is controlling a heat pump.
     */
    public Boolean hasHeatPump;

    /*
     * Whether the thermostat is controlling a forced air furnace.
     */
    public Boolean hasForcedAir;

    /*
     * Whether the thermostat is controlling a boiler.
     */
    public Boolean hasBoiler;

    /*
     * Whether the thermostat is controlling a humidifier.
     */
    public Boolean hasHumidifier;

    /*
     * Whether the thermostat is controlling an energy recovery ventilator.
     */
    public Boolean hasErv;

    /*
     * Whether the thermostat is controlling a heat recovery ventilator.
     */
    public Boolean hasHrv;

    /*
     * Whether the thermostat is in frost control mode.
     */
    public Boolean condensationAvoid;

    /*
     * Whether the thermostat is configured to report in degrees Celsius.
     */
    public Boolean useCelsius;

    /*
     * Whether the thermostat is using 12hr time format.
     */
    public Boolean useTimeFormat12;

    /*
     * Multilanguage support, currently only "en" - english is supported. In future others
     * locales can be supported.
     */
    public String locale;

    /*
     * The minimum humidity level (in percent) set point for the humidifier
     */
    public String humidity;

    /*
     * The humidifier mode. Values: auto, manual, off.
     */
    public String humidifierMode;

    /*
     * The thermostat backlight intensity when on. A value between 0 and 10, with 0
     * meaning 'off' - the zero value may not be honored by all ecobee versions.
     */
    public Integer backlightOnIntensity;

    /*
     * The thermostat backlight intensity when asleep. A value between 0 and 10, with 0
     * meaning 'off' - the zero value may not be honored by all ecobee versions.
     */
    public Integer backlightSleepIntensity;

    /*
     * The time in seconds before the thermostat screen goes into sleep mode.
     */
    public Integer backlightOffTime;

    /*
     * The field is deprecated. Please use Audio.soundTickVolume.
     */
    public Integer soundTickVolume;

    /*
     * The field is deprecated. Please use Audio.soundAlertVolume.
     */
    public Integer soundAlertVolume;

    /*
     * The minimum time the compressor must be off for in order to prevent short-cycling.
     */
    public Integer compressorProtectionMinTime;

    /*
     * The minimum outdoor temperature that the compressor can operate at - applies
     * more to air source heat pumps than geothermal.
     */
    public Integer compressorProtectionMinTemp;

    /*
     * The difference between current temperature and set-point that will trigger stage 2 heating.
     */
    public Integer stage1HeatingDifferentialTemp;

    /*
     * The difference between current temperature and set-point that will trigger stage 2 cooling.
     */
    public Integer stage1CoolingDifferentialTemp;

    /*
     * The time after a heating cycle that the fan will run for to extract any heating left
     * in the system - 30 second default.
     */
    public Integer stage1HeatingDissipationTime;

    /*
     * The time after a cooling cycle that the fan will run for to extract any cooling left
     * in the system - 30 second default.
     */
    public Integer stage1CoolingDissipationTime;

    /*
     * The flag to tell if the heat pump is in heating mode or in cooling when the relay
     * is engaged. If set to zero it's heating when the reversing valve is open, cooling
     * when closed and if it's one - it's the opposite.
     */
    public Boolean heatPumpReversalOnCool;

    /*
     * Whether fan control by the Thermostat is required in auxiliary heating (gas/electric/boiler),
     * otherwise controlled by furnace.
     */
    public Boolean fanControlRequired;

    /*
     * The minimum time, in minutes, to run the fan each hour. Value from 1 to 60.
     */
    public Integer fanMinOnTime;

    /*
     * The minimum temperature difference between the heat and cool values. Used to ensure that when
     * thermostat is in auto mode, the heat and cool values are separated by at least this value.
     */
    public Integer heatCoolMinDelta;

    /*
     * The amount to adjust the temperature reading in degrees F - this value is subtracted from
     * the temperature read from the sensor.
     */
    public Integer tempCorrection;

    /*
     * The default end time setting the thermostat applies to user temperature holds. Values useEndTime4hour,
     * useEndTime2hour (EMS Only), nextPeriod, indefinite, askMe
     */
    public String holdAction;

    /*
     * Whether the Thermostat uses a geothermal / ground source heat pump.
     */
    public Boolean heatPumpGroundWater;

    /*
     * Whether the thermostat is connected to an electric HVAC system.
     */
    public Boolean hasElectric;

    /*
     * Whether the thermostat is connected to a dehumidifier. If true or dehumidifyOvercoolOffset > 0 then
     * allow setting dehumidifierMode and dehumidifierLevel.
     */
    public Boolean hasDehumidifier;

    /*
     * The dehumidifier mode. Values: on, off. If set to off then the dehumidifier will not run,
     * nor will the AC overcool run.
     */
    public String dehumidifierMode;

    /*
     * The dehumidification set point in percentage.
     */
    public Integer dehumidifierLevel;

    /*
     * Whether the thermostat should use AC overcool to dehumidify. When set to true a postive integer value
     * must be supplied for dehumidifyOvercoolOffset otherwise an API validation exception will be thrown.
     */
    public Boolean dehumidifyWithAC;

    /*
     * Whether the thermostat should use AC overcool to dehumidify and what that temperature offset
     * should be. A value of 0 means this feature is disabled and dehumidifyWithAC will be set to false.
     * Value represents the value in F to subract from the current set point. Values should be in the
     * range 0 - 50 and be divisible by 5.
     */
    public Integer dehumidifyOvercoolOffset;

    /*
     * If enabled, allows the Thermostat to be put in HVACAuto mode.
     */
    public Boolean autoHeatCoolFeatureEnabled;

    /*
     * Whether the alert for when wifi is offline is enabled.
     */
    public Boolean wifiOfflineAlert;

    /*
     * The minimum heat set point allowed by the thermostat firmware.
     */
    public Integer heatMinTemp;

    /*
     * The maximum heat set point allowed by the thermostat firmware.
     */
    public Integer heatMaxTemp;

    /*
     * The minimum cool set point allowed by the thermostat firmware.
     */
    public Integer coolMinTemp;

    /*
     * The maximum cool set point allowed by the thermostat firmware.
     */
    public Integer coolMaxTemp;

    /*
     * The maximum heat set point configured by the user's preferences.
     */
    public Integer heatRangeHigh;

    /*
     * The minimum heat set point configured by the user's preferences.
     */
    public Integer heatRangeLow;

    /*
     * The maximum cool set point configured by the user's preferences.
     */
    public Integer coolRangeHigh;

    /*
     * The minimum heat set point configured by the user's preferences.
     */
    public Integer coolRangeLow;

    /*
     * The user access code value for this thermostat. See the SecuritySettings object for more information.
     */
    public String userAccessCode;

    /*
     * The integer representation of the user access settings. See the SecuritySettings object for more information.
     */
    public Integer userAccessSetting;

    /*
     * The temperature at which an auxHeat temperature alert is triggered.
     */
    public Integer auxRuntimeAlert;

    /*
     * The temperature at which an auxOutdoor temperature alert is triggered.
     */
    public Integer auxOutdoorTempAlert;

    /*
     * The maximum outdoor temperature above which aux heat will not run.
     */
    public Integer auxMaxOutdoorTemp;

    /*
     * Whether the auxHeat temperature alerts are enabled.
     */
    public Boolean auxRuntimeAlertNotify;

    /*
     * Whether the auxOutdoor temperature alerts are enabled.
     */
    public Boolean auxOutdoorTempAlertNotify;

    /*
     * Whether the auxHeat temperature alerts for the technician are enabled.
     */
    public Boolean auxRuntimeAlertNotifyTechnician;

    /*
     * Whether the auxOutdoor temperature alerts for the technician are enabled.
     */
    public Boolean auxOutdoorTempAlertNotifyTechnician;

    /*
     * Whether the thermostat should use pre heating to reach the set point on time.
     */
    public Boolean disablePreHeating;

    /*
     * Whether the thermostat should use pre cooling to reach the set point on time.
     */
    public Boolean disablePreCooling;

    /*
     * Whether an installer code is required.
     */
    public Boolean installerCodeRequired;

    /*
     * Whether Demand Response requests are accepted by this thermostat. Possible values
     * are: always, askMe, customerSelect, defaultAccept, defaultDecline, never.
     */
    public String drAccept;

    /*
     * Whether the property is a rental, or not.
     */
    public Boolean isRentalProperty;

    /*
     * Whether to use a zone controller or not.
     */
    public Boolean useZoneController;

    /*
     * Whether random start delay is enabled for cooling.
     */
    public Integer randomStartDelayCool;

    /*
     * Whether random start delay is enabled for heating.
     */
    public Integer randomStartDelayHeat;

    /*
     * The humidity level to trigger a high humidity alert.
     */
    public Integer humidityHighAlert;

    /*
     * The humidity level to trigger a low humidity alert.
     */
    public Integer humidityLowAlert;

    /*
     * Whether heat pump alerts are disabled.
     */
    public Boolean disableHeatPumpAlerts;

    /*
     * Whether alerts are disabled from showing on the thermostat.
     */
    public Boolean disableAlertsOnIdt;

    /*
     * Whether humidification alerts are enabled to the thermsotat owner.
     */
    public Boolean humidityAlertNotify;

    /*
     * Whether humidification alerts are enabled to the technician associated with the thermsotat.
     */
    public Boolean humidityAlertNotifyTechnician;

    /*
     * Whether temperature alerts are enabled to the thermsotat owner.
     */
    public Boolean tempAlertNotify;

    /*
     * Whether temperature alerts are enabled to the technician associated with the thermostat.
     */
    public Boolean tempAlertNotifyTechnician;

    /*
     * The dollar amount the owner specifies for their desired maximum electricy bill.
     */
    public Integer monthlyElectricityBillLimit;

    /*
     * Whether electricity bill alerts are enabled.
     */
    public Boolean enableElectricityBillAlert;

    /*
     * Whether electricity bill projection alerts are enabled
     */
    public Boolean enableProjectedElectricityBillAlert;

    /*
     * The day of the month the owner's electricty usage is billed.
     */
    public Integer electricityBillingDayOfMonth;

    /*
     * The owners billing cycle duration in months.
     */
    public Integer electricityBillCycleMonths;

    /*
     * The annual start month of the owners billing cycle.
     */
    public Integer electricityBillStartMonth;

    /*
     * The number of minutes to run ventilator per hour when home.
     */
    public Integer ventilatorMinOnTimeHome;

    /*
     * The number of minutes to run ventilator per hour when away.
     */
    public Integer ventilatorMinOnTimeAway;

    /*
     * Determines whether or not to turn the backlight off during sleep.
     */
    public Boolean backlightOffDuringSleep;

    /*
     * When set to true if no occupancy motion detected thermostat will go into indefinite away
     * hold, until either the user presses resume schedule or motion is detected.
     */
    public Boolean autoAway;

    /*
     * When set to true if a larger than normal delta is found between sensors the fan
     * will be engaged for 15min/hour.
     */
    public Boolean smartCirculation;

    /*
     * When set to true if a sensor has detected presense for more than 10 minutes then
     * include that sensor in temp average. If no activity has been seen on a sensor for
     * more than 1 hour then remove this sensor from temperature average.
     */
    public Boolean followMeComfort;

    /*
     * This read-only field represents the type of ventilator present for the Thermostat.
     * The possible values are none, ventilator, hrv, and erv.
     */
    public String ventilatorType;

    /*
     * This Boolean field represents whether the ventilator timer is on or off. The default
     * value is false. If set to true the ventilatorOffDateTime is set to now() + 20 minutes.
     * If set to false the ventilatorOffDateTime is set to it's default value.
     */
    public Boolean isVentilatorTimerOn;

    /*
     * This read-only field represents the Date and Time the ventilator will run until.
     * The default value is 2014-01-01 00:00:00.
     */
    public String ventilatorOffDateTime;

    /*
     * This Boolean field represents whether the HVAC system has a UV filter. The default value is true.
     */
    public Boolean hasUVFilter;

    /*
     * This field represents whether to permit the cooling to operate when the Outdoor temeperature
     * is under a specific threshold, currently 55F. The default value is false.
     */
    public Boolean coolingLockout;

    /*
     * Whether to use the ventilator to dehumidify when climate or calendar event indicates the owner
     * is home. The default value is false.
     */
    public Boolean ventilatorFreeCooling;

    /*
     * This field represents whether to permit dehumidifer to operate when the heating is
     * running. The default value is false.
     */
    public Boolean dehumidifyWhenHeating;

    /*
     * This field represents whether or not to allow dehumification when cooling. The default value is true.
     */
    public Boolean ventilatorDehumidify;

    /*
     * The unique reference to the group this thermostat belongs to, if any. See GET Group request and POST
     * Group request for more information.
     */
    public String groupRef;

    /*
     * The name of the the group this thermostat belongs to, if any. See GET Group request and POST Group
     * request for more information.
     */
    public String groupName;

    /*
     * The setting value for the group this thermostat belongs to, if any. See GET Group request and POST
     * Group request for more information.
     */
    public Integer groupSetting;
}
