/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * Thermostat
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ThermostatCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0201;
    public static final String CLUSTER_NAME = "Thermostat";
    public static final String CLUSTER_PREFIX = "thermostat";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_LOCAL_TEMPERATURE = "localTemperature";
    public static final String ATTRIBUTE_OUTDOOR_TEMPERATURE = "outdoorTemperature";
    public static final String ATTRIBUTE_OCCUPANCY = "occupancy";
    public static final String ATTRIBUTE_ABS_MIN_HEAT_SETPOINT_LIMIT = "absMinHeatSetpointLimit";
    public static final String ATTRIBUTE_ABS_MAX_HEAT_SETPOINT_LIMIT = "absMaxHeatSetpointLimit";
    public static final String ATTRIBUTE_ABS_MIN_COOL_SETPOINT_LIMIT = "absMinCoolSetpointLimit";
    public static final String ATTRIBUTE_ABS_MAX_COOL_SETPOINT_LIMIT = "absMaxCoolSetpointLimit";
    public static final String ATTRIBUTE_PI_COOLING_DEMAND = "piCoolingDemand";
    public static final String ATTRIBUTE_PI_HEATING_DEMAND = "piHeatingDemand";
    public static final String ATTRIBUTE_LOCAL_TEMPERATURE_CALIBRATION = "localTemperatureCalibration";
    public static final String ATTRIBUTE_OCCUPIED_COOLING_SETPOINT = "occupiedCoolingSetpoint";
    public static final String ATTRIBUTE_OCCUPIED_HEATING_SETPOINT = "occupiedHeatingSetpoint";
    public static final String ATTRIBUTE_UNOCCUPIED_COOLING_SETPOINT = "unoccupiedCoolingSetpoint";
    public static final String ATTRIBUTE_UNOCCUPIED_HEATING_SETPOINT = "unoccupiedHeatingSetpoint";
    public static final String ATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT = "minHeatSetpointLimit";
    public static final String ATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT = "maxHeatSetpointLimit";
    public static final String ATTRIBUTE_MIN_COOL_SETPOINT_LIMIT = "minCoolSetpointLimit";
    public static final String ATTRIBUTE_MAX_COOL_SETPOINT_LIMIT = "maxCoolSetpointLimit";
    public static final String ATTRIBUTE_MIN_SETPOINT_DEAD_BAND = "minSetpointDeadBand";
    public static final String ATTRIBUTE_REMOTE_SENSING = "remoteSensing";
    public static final String ATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION = "controlSequenceOfOperation";
    public static final String ATTRIBUTE_SYSTEM_MODE = "systemMode";
    public static final String ATTRIBUTE_THERMOSTAT_RUNNING_MODE = "thermostatRunningMode";
    public static final String ATTRIBUTE_START_OF_WEEK = "startOfWeek";
    public static final String ATTRIBUTE_NUMBER_OF_WEEKLY_TRANSITIONS = "numberOfWeeklyTransitions";
    public static final String ATTRIBUTE_NUMBER_OF_DAILY_TRANSITIONS = "numberOfDailyTransitions";
    public static final String ATTRIBUTE_TEMPERATURE_SETPOINT_HOLD = "temperatureSetpointHold";
    public static final String ATTRIBUTE_TEMPERATURE_SETPOINT_HOLD_DURATION = "temperatureSetpointHoldDuration";
    public static final String ATTRIBUTE_THERMOSTAT_PROGRAMMING_OPERATION_MODE = "thermostatProgrammingOperationMode";
    public static final String ATTRIBUTE_THERMOSTAT_RUNNING_STATE = "thermostatRunningState";
    public static final String ATTRIBUTE_SETPOINT_CHANGE_SOURCE = "setpointChangeSource";
    public static final String ATTRIBUTE_SETPOINT_CHANGE_AMOUNT = "setpointChangeAmount";
    public static final String ATTRIBUTE_SETPOINT_CHANGE_SOURCE_TIMESTAMP = "setpointChangeSourceTimestamp";
    public static final String ATTRIBUTE_OCCUPIED_SETBACK = "occupiedSetback";
    public static final String ATTRIBUTE_OCCUPIED_SETBACK_MIN = "occupiedSetbackMin";
    public static final String ATTRIBUTE_OCCUPIED_SETBACK_MAX = "occupiedSetbackMax";
    public static final String ATTRIBUTE_UNOCCUPIED_SETBACK = "unoccupiedSetback";
    public static final String ATTRIBUTE_UNOCCUPIED_SETBACK_MIN = "unoccupiedSetbackMin";
    public static final String ATTRIBUTE_UNOCCUPIED_SETBACK_MAX = "unoccupiedSetbackMax";
    public static final String ATTRIBUTE_EMERGENCY_HEAT_DELTA = "emergencyHeatDelta";
    public static final String ATTRIBUTE_AC_TYPE = "acType";
    public static final String ATTRIBUTE_AC_CAPACITY = "acCapacity";
    public static final String ATTRIBUTE_AC_REFRIGERANT_TYPE = "acRefrigerantType";
    public static final String ATTRIBUTE_AC_COMPRESSOR_TYPE = "acCompressorType";
    public static final String ATTRIBUTE_AC_ERROR_CODE = "acErrorCode";
    public static final String ATTRIBUTE_AC_LOUVER_POSITION = "acLouverPosition";
    public static final String ATTRIBUTE_AC_COIL_TEMPERATURE = "acCoilTemperature";
    public static final String ATTRIBUTE_AC_CAPACITY_FORMAT = "acCapacityFormat";
    public static final String ATTRIBUTE_PRESET_TYPES = "presetTypes";
    public static final String ATTRIBUTE_SCHEDULE_TYPES = "scheduleTypes";
    public static final String ATTRIBUTE_NUMBER_OF_PRESETS = "numberOfPresets";
    public static final String ATTRIBUTE_NUMBER_OF_SCHEDULES = "numberOfSchedules";
    public static final String ATTRIBUTE_NUMBER_OF_SCHEDULE_TRANSITIONS = "numberOfScheduleTransitions";
    public static final String ATTRIBUTE_NUMBER_OF_SCHEDULE_TRANSITION_PER_DAY = "numberOfScheduleTransitionPerDay";
    public static final String ATTRIBUTE_ACTIVE_PRESET_HANDLE = "activePresetHandle";
    public static final String ATTRIBUTE_ACTIVE_SCHEDULE_HANDLE = "activeScheduleHandle";
    public static final String ATTRIBUTE_PRESETS = "presets";
    public static final String ATTRIBUTE_SCHEDULES = "schedules";
    public static final String ATTRIBUTE_SETPOINT_HOLD_EXPIRY_TIMESTAMP = "setpointHoldExpiryTimestamp";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current Calculated Local Temperature, when available.
     * • If the LTNE feature is not supported:
     * ◦ If the LocalTemperatureCalibration is invalid or currently unavailable, the attribute shall report null.
     * ◦ If the LocalTemperatureCalibration is valid, the attribute shall report that value.
     * • Otherwise, if the LTNE feature is supported, there is no feedback externally available for the
     * LocalTemperatureCalibration. In that case, the LocalTemperature attribute shall always report null.
     */
    public Integer localTemperature; // 0 temperature R V
    /**
     * Indicates the outdoor temperature, as measured locally or remotely (over the network).
     */
    public Integer outdoorTemperature; // 1 temperature R V
    /**
     * Indicates whether the heated/cooled space is occupied or not, as measured locally or remotely (over the network).
     */
    public OccupancyBitmap occupancy; // 2 OccupancyBitmap R V
    /**
     * Indicates the absolute minimum level that the heating setpoint may be set to. This is a limitation imposed by the
     * manufacturer.
     * ### Refer to Setpoint Limits for constraints
     */
    public Integer absMinHeatSetpointLimit; // 3 temperature R V
    public Integer absMaxHeatSetpointLimit; // 4 temperature R V
    public Integer absMinCoolSetpointLimit; // 5 temperature R V
    /**
     * Indicates the absolute maximum level that the cooling setpoint may be set to. This is a limitation imposed by the
     * manufacturer.
     * ### Refer to Setpoint Limits for constraints
     */
    public Integer absMaxCoolSetpointLimit; // 6 temperature R V
    /**
     * Indicates the level of cooling demanded by the PI (proportional integral) control loop in use by the thermostat
     * (if any), in percent. This value is 0 when the thermostat is in “off” or “heating” mode.
     * This attribute is reported regularly and may be used to control a cooling device.
     */
    public Integer piCoolingDemand; // 7 uint8 R V
    /**
     * Indicates the level of heating demanded by the PI loop in percent. This value is 0 when the thermostat is in
     * “off” or “cooling” mode.
     * This attribute is reported regularly and may be used to control a heating device.
     */
    public Integer piHeatingDemand; // 8 uint8 R V
    /**
     * Indicates the offset the Thermostat server shall make to the measured temperature (locally or remotely) to adjust
     * the Calculated Local Temperature prior to using, displaying or reporting it.
     * The purpose of this attribute is to adjust the calibration of the Thermostat server per the user’s preferences
     * (e.g., to match if there are multiple servers displaying different values for the same HVAC area) or compensate
     * for variability amongst temperature sensors.
     * If a Thermostat client attempts to write LocalTemperatureCalibration attribute to an unsupported value (e.g., out
     * of the range supported by the Thermostat server), the Thermostat server shall respond with a status of SUCCESS
     * and set the value of LocalTemperatureCalibration to the upper or lower limit reached.
     * &gt; [!NOTE]
     * &gt; Prior to revision 8 of this cluster specification the value of this attribute was constrained to a range of
     * -2.5°C to 2.5°C.
     */
    public Integer localTemperatureCalibration; // 16 SignedTemperature RW VM
    /**
     * Indicates the cooling mode setpoint when the room is occupied. Refer to Setpoint Limits for constraints.
     * If an attempt is made to set this attribute to a value greater than MaxCoolSetpointLimit or less than
     * MinCoolSetpointLimit, a response with the status code CONSTRAINT_ERROR shall be returned.
     * If this attribute is set to a value that is less than (OccupiedHeatingSetpoint + MinSetpointDeadBand), the value
     * of OccupiedHeatingSetpoint shall be adjusted to (OccupiedCoolingSetpoint - MinSetpointDeadBand).
     * If the occupancy status of the room is unknown, this attribute shall be used as the cooling mode setpoint.
     * If a client changes the value of this attribute, the server supports the PRES feature, and the server either does
     * not support the OCC feature or the Occupied bit is set on the Occupancy attribute, the value of the
     * ActivePresetHandle attribute shall be set to null.
     */
    public Integer occupiedCoolingSetpoint; // 17 temperature RW VO
    /**
     * Indicates the heating mode setpoint when the room is occupied. Refer to Setpoint Limits for constraints.
     * If an attempt is made to set this attribute to a value greater than MaxHeatSetpointLimit or less than
     * MinHeatSetpointLimit, a response with the status code CONSTRAINT_ERROR shall be returned.
     * If this attribute is set to a value that is greater than (OccupiedCoolingSetpoint - MinSetpointDeadBand), the
     * value of OccupiedCoolingSetpoint shall be adjusted to (OccupiedHeatingSetpoint + MinSetpointDeadBand).
     * If the occupancy status of the room is unknown, this attribute shall be used as the heating mode setpoint.
     * If a client changes the value of this attribute, the server supports the PRES feature, and the server either does
     * not support the OCC feature or the Occupied bit is set on the Occupancy attribute, the value of the
     * ActivePresetHandle attribute shall be set to null.
     */
    public Integer occupiedHeatingSetpoint; // 18 temperature RW VO
    /**
     * Indicates the cooling mode setpoint when the room is unoccupied. Refer to Setpoint Limits for constraints.
     * If an attempt is made to set this attribute to a value greater than MaxCoolSetpointLimit or less than
     * MinCoolSetpointLimit, a response with the status code CONSTRAINT_ERROR shall be returned.
     * If this attribute is set to a value that is less than (UnoccupiedHeatingSetpoint + MinSetpointDeadBand), the
     * value of UnoccupiedHeatingSetpoint shall be adjusted to (UnoccupiedCoolingSetpoint - MinSetpointDeadBand).
     * If the occupancy status of the room is unknown, this attribute shall NOT be used.
     * If a client changes the value of this attribute, the server supports the PRES and OCC features, and the Occupied
     * bit is not set on the Occupancy attribute, the value of the ActivePresetHandle attribute shall be set to null.
     */
    public Integer unoccupiedCoolingSetpoint; // 19 temperature RW VO
    /**
     * Indicates the heating mode setpoint when the room is unoccupied. Refer to Setpoint Limits for constraints.
     * If an attempt is made to set this attribute to a value greater than MaxHeatSetpointLimit or less than
     * MinHeatSetpointLimit, a response with the status code CONSTRAINT_ERROR shall be returned.
     * If this attribute is set to a value that is greater than (UnoccupiedCoolingSetpoint - MinSetpointDeadBand), the
     * value of UnoccupiedCoolingSetpoint shall be adjusted to (UnoccupiedHeatingSetpoint + MinSetpointDeadBand).
     * If the occupancy status of the room is unknown, this attribute shall NOT be used.
     * If a client changes the value of this attribute, the server supports the PRES and OCC features, and the Occupied
     * bit is not set on the Occupancy attribute, the value of the ActivePresetHandle attribute shall be set to null.
     */
    public Integer unoccupiedHeatingSetpoint; // 20 temperature RW VO
    /**
     * Indicates the minimum level that the heating setpoint may be set to.
     * This attribute, and the following three attributes, allow the user to define setpoint limits more constrictive
     * than the manufacturer imposed ones. Limiting users (e.g., in a commercial building) to such setpoint limits can
     * help conserve power.
     * Refer to Setpoint Limits for constraints. If an attempt is made to set this attribute to a value which conflicts
     * with setpoint values then those setpoints shall be adjusted by the minimum amount to permit this attribute to be
     * set to the desired value. If an attempt is made to set this attribute to a value which is not consistent with the
     * constraints and cannot be resolved by modifying setpoints then a response with the status code CONSTRAINT_ERROR
     * shall be returned.
     */
    public Integer minHeatSetpointLimit; // 21 temperature RW VM
    /**
     * Indicates the maximum level that the heating setpoint may be set to.
     * Refer to Setpoint Limits for constraints. If an attempt is made to set this attribute to a value which conflicts
     * with setpoint values then those setpoints shall be adjusted by the minimum amount to permit this attribute to be
     * set to the desired value. If an attempt is made to set this attribute to a value which is not consistent with the
     * constraints and cannot be resolved by modifying setpoints then a response with the status code CONSTRAINT_ERROR
     * shall be returned.
     */
    public Integer maxHeatSetpointLimit; // 22 temperature RW VM
    /**
     * Indicates the minimum level that the cooling setpoint may be set to.
     * Refer to Setpoint Limits for constraints. If an attempt is made to set this attribute to a value which conflicts
     * with setpoint values then those setpoints shall be adjusted by the minimum amount to permit this attribute to be
     * set to the desired value. If an attempt is made to set this attribute to a value which is not consistent with the
     * constraints and cannot be resolved by modifying setpoints then a response with the status code CONSTRAINT_ERROR
     * shall be returned.
     */
    public Integer minCoolSetpointLimit; // 23 temperature RW VM
    /**
     * Indicates the maximum level that the cooling setpoint may be set to.
     * Refer to Setpoint Limits for constraints. If an attempt is made to set this attribute to a value which conflicts
     * with setpoint values then those setpoints shall be adjusted by the minimum amount to permit this attribute to be
     * set to the desired value. If an attempt is made to set this attribute to a value which is not consistent with the
     * constraints and cannot be resolved by modifying setpoints then a response with the status code CONSTRAINT_ERROR
     * shall be returned.
     */
    public Integer maxCoolSetpointLimit; // 24 temperature RW VM
    /**
     * On devices which support the AUTO feature, this attribute shall indicate the minimum difference between the Heat
     * Setpoint and the Cool Setpoint.
     * Refer to Setpoint Limits for constraints.
     * &gt; [!NOTE]
     * &gt; Prior to revision 8 of this cluster specification the value of this attribute was constrained to a range of
     * 0°C to 2.5°C.
     * For backwards compatibility, this attribute is optionally writeable. However any writes to this attribute shall
     * be silently ignored.
     */
    public Integer minSetpointDeadBand; // 25 SignedTemperature R[W] VM
    /**
     * Indicates when the local temperature, outdoor temperature and occupancy are being sensed by remote networked
     * sensors, rather than internal sensors.
     * If the LTNE feature is present in the server, the LocalTemperature RemoteSensing bit value shall always report a
     * value of 0.
     * If the LocalTemperature RemoteSensing bit is written with a value of 1 when the LTNE feature is present, the
     * write shall fail and the server shall report a CONSTRAINT_ERROR.
     */
    public RemoteSensingBitmap remoteSensing; // 26 RemoteSensingBitmap RW VM
    /**
     * Indicates the overall operating environment of the thermostat, and thus the possible system modes that the
     * thermostat can operate in.
     * If an attempt is made to write to this attribute, the server shall silently ignore the write and the value of
     * this attribute shall remain unchanged. This behavior is in place for backwards compatibility with existing
     * thermostats.
     */
    public ControlSequenceOfOperationEnum controlSequenceOfOperation; // 27 ControlSequenceOfOperationEnum RW VM
    /**
     * Indicates the current operating mode of the thermostat. Its value shall be limited by the
     * ControlSequenceOfOperation attribute.
     */
    public SystemModeEnum systemMode; // 28 SystemModeEnum RW VM
    /**
     * Indicates the running mode of the thermostat. This attribute uses the same values as SystemModeEnum but can only
     * be Off, Cool or Heat. This attribute is intended to provide additional information when the thermostat’s system
     * mode is in auto mode.
     */
    public ThermostatRunningModeEnum thermostatRunningMode; // 30 ThermostatRunningModeEnum R V
    /**
     * Indicates the day of the week that this thermostat considers to be the start of week for weekly setpoint
     * scheduling.
     * This attribute may be able to be used as the base to determine if the device supports weekly scheduling by
     * reading the attribute. Successful response means that the weekly scheduling is supported.
     */
    public StartOfWeekEnum startOfWeek; // 32 StartOfWeekEnum R V
    /**
     * Indicates how many weekly schedule transitions the thermostat is capable of handling.
     */
    public Integer numberOfWeeklyTransitions; // 33 uint8 R V
    /**
     * Indicates how many daily schedule transitions the thermostat is capable of handling.
     */
    public Integer numberOfDailyTransitions; // 34 uint8 R V
    /**
     * Indicates the temperature hold status on the thermostat. If hold status is on, the thermostat SHOULD maintain the
     * temperature setpoint for the current mode until a system mode change. If hold status is off, the thermostat
     * SHOULD follow the setpoint transitions specified by its internal scheduling program. If the thermostat supports
     * setpoint hold for a specific duration, it SHOULD also implement the TemperatureSetpointHoldDuration attribute.
     * If the server supports a setpoint hold for a specific duration, it SHOULD also implement the
     * SetpointHoldExpiryTimestamp attribute.
     * If this attribute is updated to SetpointHoldOn and the TemperatureSetpointHoldDuration has a non-null value and
     * the SetpointHoldExpiryTimestamp is supported, the server shall update the SetpointHoldExpiryTimestamp with a
     * value of current UTC timestamp, in seconds, plus the value in TemperatureSetpointHoldDuration multiplied by 60.
     * If this attribute is updated to SetpointHoldOff and the SetpointHoldExpiryTimestamp is supported, the server
     * shall set the SetpointHoldExpiryTimestamp to null.
     */
    public TemperatureSetpointHoldEnum temperatureSetpointHold; // 35 TemperatureSetpointHoldEnum RW VM
    /**
     * Indicates the period in minutes for which a setpoint hold is active. Thermostats that support hold for a
     * specified duration SHOULD implement this attribute. The null value indicates the field is unused. All other
     * values are reserved.
     * If this attribute is updated to a non-null value and the TemperatureSetpointHold is set to SetpointHoldOn and the
     * SetpointHoldExpiryTimestamp is supported, the server shall update SetpointHoldExpiryTimestamp with a value of
     * current UTC timestamp, in seconds, plus the new value of this attribute multiplied by 60.
     * If this attribute is set to null and the SetpointHoldExpiryTimestamp is supported, the server shall set the
     * SetpointHoldExpiryTimestamp to null.
     */
    public Integer temperatureSetpointHoldDuration; // 36 uint16 RW VM
    /**
     * Indicates the operational state of the thermostat’s programming. The thermostat shall modify its programming
     * operation when this attribute is modified by a client and update this attribute when its programming operation is
     * modified locally by a user. The thermostat may support more than one active ProgrammingOperationModeBitmap. For
     * example, the thermostat may operate simultaneously in Schedule Programming Mode and Recovery Mode.
     * Thermostats which contain a schedule may use this attribute to control how that schedule is used, even if they do
     * not support the ScheduleConfiguration feature.
     * When ScheduleActive is not set, the setpoint is altered only by manual up/down changes at the thermostat or
     * remotely, not by internal schedule programming.
     * &gt; [!NOTE]
     * &gt; Modifying the ScheduleActive bit does not clear or delete previous weekly schedule programming
     * configurations.
     */
    public ProgrammingOperationModeBitmap thermostatProgrammingOperationMode; // 37 ProgrammingOperationModeBitmap RW VM
    /**
     * Indicates the current relay state of the heat, cool, and fan relays. Unimplemented outputs shall be treated as if
     * they were Off.
     */
    public RelayStateBitmap thermostatRunningState; // 41 RelayStateBitmap R V
    /**
     * Indicates the source of the current active OccupiedCoolingSetpoint or OccupiedHeatingSetpoint (i.e., who or what
     * determined the current setpoint).
     * This attribute enables service providers to determine whether changes to setpoints were initiated due to occupant
     * comfort, scheduled programming or some other source (e.g., electric utility or other service provider). Because
     * automation services may initiate frequent setpoint changes, this attribute clearly differentiates the source of
     * setpoint changes made at the thermostat.
     */
    public SetpointChangeSourceEnum setpointChangeSource; // 48 SetpointChangeSourceEnum R V
    /**
     * Indicates the delta between the current active OccupiedCoolingSetpoint or OccupiedHeatingSetpoint and the
     * previous active setpoint. This attribute is meant to accompany the SetpointChangeSource attribute; devices
     * implementing SetpointChangeAmount SHOULD also implement SetpointChangeSource.
     * The null value indicates that the previous setpoint was unknown.
     */
    public Integer setpointChangeAmount; // 49 TemperatureDifference R V
    /**
     * Indicates the time in UTC at which the SetpointChangeAmount attribute change was recorded.
     */
    public Integer setpointChangeSourceTimestamp; // 50 epoch-s R V
    /**
     * Indicates the amount that the Thermostat server will allow the Calculated Local Temperature to float above the
     * OccupiedCoolingSetpoint (i.e., OccupiedCoolingSetpoint + OccupiedSetback) or below the OccupiedHeatingSetpoint
     * setpoint (i.e., OccupiedHeatingSetpoint – OccupiedSetback) before initiating a state change to bring the
     * temperature back to the user’s
     * desired setpoint. This attribute is sometimes also referred to as the “span.”
     * The purpose of this attribute is to allow remote configuration of the span between the desired setpoint and the
     * measured temperature to help prevent over-cycling and reduce energy bills, though this may result in lower
     * comfort on the part of some users.
     * The null value indicates the attribute is unused.
     * If the Thermostat client attempts to write OccupiedSetback to a value greater than OccupiedSetbackMax, the
     * Thermostat server shall set its OccupiedSetback value to OccupiedSetbackMax and shall send a Write Attribute
     * Response command with a Status Code field enumeration of SUCCESS response.
     * If the Thermostat client attempts to write OccupiedSetback to a value less than OccupiedSetbackMin, the
     * Thermostat server shall set its OccupiedSetback value to OccupiedSetbackMin and shall send a Write Attribute
     * Response command with a Status Code field enumeration of SUCCESS response.
     */
    public Integer occupiedSetback; // 52 UnsignedTemperature RW VM
    /**
     * Indicates the minimum value that the Thermostat server will allow the OccupiedSetback attribute to be configured
     * by a user.
     * The null value indicates the attribute is unused.
     */
    public Integer occupiedSetbackMin; // 53 UnsignedTemperature R V
    /**
     * Indicates the maximum value that the Thermostat server will allow the OccupiedSetback attribute to be configured
     * by a user.
     * The null value indicates the attribute is unused.
     */
    public Integer occupiedSetbackMax; // 54 UnsignedTemperature R V
    /**
     * Indicates the amount that the Thermostat server will allow the Calculated Local Temperature to float above the
     * UnoccupiedCoolingSetpoint (i.e., UnoccupiedCoolingSetpoint + UnoccupiedSetback) or below the
     * UnoccupiedHeatingSetpoint setpoint (i.e., UnoccupiedHeatingSetpoint - UnoccupiedSetback) before initiating a
     * state change to bring the temperature back to the user’s desired setpoint. This attribute is sometimes also
     * referred to as the “span.”
     * The purpose of this attribute is to allow remote configuration of the span between the desired setpoint and the
     * measured temperature to help prevent over-cycling and reduce energy bills, though this may result in lower
     * comfort on the part of some users.
     * The null value indicates the attribute is unused.
     * If the Thermostat client attempts to write UnoccupiedSetback to a value greater than UnoccupiedSetbackMax, the
     * Thermostat server shall set its UnoccupiedSetback value to UnoccupiedSetbackMax and shall send a Write Attribute
     * Response command with a Status Code field enumeration of SUCCESS response.
     * If the Thermostat client attempts to write UnoccupiedSetback to a value less than UnoccupiedSetbackMin, the
     * Thermostat server shall set its UnoccupiedSetback value to UnoccupiedSetbackMin and shall send a Write Attribute
     * Response command with a Status Code field enumeration of SUCCESS response.
     */
    public Integer unoccupiedSetback; // 55 UnsignedTemperature RW VM
    /**
     * Indicates the minimum value that the Thermostat server will allow the UnoccupiedSetback attribute to be
     * configured by a user.
     * The null value indicates the attribute is unused.
     */
    public Integer unoccupiedSetbackMin; // 56 UnsignedTemperature R V
    /**
     * Indicates the maximum value that the Thermostat server will allow the UnoccupiedSetback attribute to be
     * configured by a user.
     * The null value indicates the attribute is unused.
     */
    public Integer unoccupiedSetbackMax; // 57 UnsignedTemperature R V
    /**
     * Indicates the delta between the Calculated Local Temperature and the OccupiedHeatingSetpoint or
     * UnoccupiedHeatingSetpoint attributes at which the Thermostat server will operate in emergency heat mode.
     * If the difference between the Calculated Local Temperature and OccupiedCoolingSetpoint or
     * UnoccupiedCoolingSetpoint is greater than or equal to the EmergencyHeatDelta and the Thermostat server’s
     * SystemMode attribute is in a heating-related mode, then the Thermostat server shall immediately switch to the
     * SystemMode attribute value that provides the highest stage of heating (e.g., emergency heat) and continue
     * operating in that running state until the OccupiedHeatingSetpoint value is reached. For example:
     * • Calculated Local Temperature &#x3D; 10.0°C
     * • OccupiedHeatingSetpoint &#x3D; 16.0°C
     * • EmergencyHeatDelta &#x3D; 2.0°C
     * ⇒ OccupiedHeatingSetpoint - Calculated Local Temperature ≥? EmergencyHeatDelta
     * ⇒ 16°C - 10°C ≥? 2°C
     * ⇒ TRUE &gt;&gt;&gt; Thermostat server changes its SystemMode to operate in 2nd stage or emergency heat mode
     * The purpose of this attribute is to provide Thermostat clients the ability to configure rapid heating when a
     * setpoint is of a specified amount greater than the measured temperature. This allows the heated space to be
     * quickly heated to the desired level set by the user.
     */
    public Integer emergencyHeatDelta; // 58 UnsignedTemperature RW VM
    /**
     * Indicates the type of Mini Split ACTypeEnum of Mini Split AC is defined depending on how Cooling and Heating
     * condition is achieved by Mini Split AC.
     */
    public ACTypeEnum acType; // 64 ACTypeEnum RW VM
    /**
     * Indicates capacity of Mini Split AC in terms of the format defined by the ACCapacityFormat attribute
     */
    public Integer acCapacity; // 65 uint16 RW VM
    /**
     * Indicates type of refrigerant used within the Mini Split AC.
     */
    public ACRefrigerantTypeEnum acRefrigerantType; // 66 ACRefrigerantTypeEnum RW VM
    /**
     * Indicates the type of compressor used within the Mini Split AC.
     */
    public ACCompressorTypeEnum acCompressorType; // 67 ACCompressorTypeEnum RW VM
    /**
     * Indicates the type of errors encountered within the Mini Split AC.
     */
    public ACErrorCodeBitmap acErrorCode; // 68 ACErrorCodeBitmap RW VM
    /**
     * Indicates the position of Louver on the AC.
     */
    public ACLouverPositionEnum acLouverPosition; // 69 ACLouverPositionEnum RW VM
    /**
     * Indicates the temperature of the AC coil, as measured locally or remotely (over the network).
     */
    public Integer acCoilTemperature; // 70 temperature R V
    /**
     * Indicates the format for the ACCapacity attribute.
     */
    public ACCapacityFormatEnum acCapacityFormat; // 71 ACCapacityFormatEnum RW VM
    /**
     * Indicates the supported PresetScenarioEnum values, limits on how many presets can be created for each
     * PresetScenarioEnum, and whether or not a thermostat can transition automatically to a given scenario.
     */
    public List<PresetTypeStruct> presetTypes; // 72 list R V
    /**
     * Indicates the supported SystemMode values for Schedules, limits on how many schedules can be created for each
     * SystemMode value, and whether or not a given SystemMode value supports transitions to Presets, target setpoints,
     * or both.
     */
    public List<ScheduleTypeStruct> scheduleTypes; // 73 list R V
    /**
     * Indicates the maximum number of entries supported by the Presets attribute.
     */
    public Integer numberOfPresets; // 74 uint8 R V
    /**
     * Indicates the maximum number of entries supported by the Schedules attribute.
     */
    public Integer numberOfSchedules; // 75 uint8 R V
    /**
     * Indicates the maximum number of transitions per Schedules attribute entry.
     */
    public Integer numberOfScheduleTransitions; // 76 uint8 R V
    public Integer numberOfScheduleTransitionPerDay; // 77 uint8 R V
    /**
     * Indicates the PresetHandle of the active preset. If this attribute is null, then there is no active preset.
     */
    public OctetString activePresetHandle; // 78 octstr R V
    /**
     * Indicates the ScheduleHandle of the active schedule. A null value in this attribute indicates that there is no
     * active schedule.
     */
    public OctetString activeScheduleHandle; // 79 octstr R V
    /**
     * This attribute shall contain the current list of configured presets. On receipt of a write request:
     * 1. If the PresetHandle field is null, the PresetStruct shall be treated as an added preset, and the device shall
     * create a new unique value for the PresetHandle field.
     * a. If the BuiltIn field is true, a response with the status code CONSTRAINT_ERROR shall be returned.
     * 2. If the PresetHandle field is not null, the PresetStruct shall be treated as a modification of an existing
     * preset.
     * a. If the value of the PresetHandle field does not match any of the existing presets, a response with the status
     * code NOT_FOUND shall be returned.
     * b. If the value of the PresetHandle field is duplicated on multiple presets in the updated list, a response with
     * the status code CONSTRAINT_ERROR shall be returned.
     * c. If the BuiltIn field is true, and the PresetStruct in the current value with a matching PresetHandle field has
     * a BuiltIn field set to false, a response with the status code CONSTRAINT_ERROR shall be returned.
     * d. If the BuiltIn field is false, and the PresetStruct in the current value with a matching PresetHandle field
     * has a BuiltIn field set to true, a response with the status code CONSTRAINT_ERROR shall be returned.
     * 3. If the specified PresetScenarioEnum value does not exist in PresetTypes, a response with the status code
     * CONSTRAINT_ERROR shall be returned.
     * 4. If the Name is set, but the associated PresetTypeStruct does not have the SupportsNames bit set, a response
     * with the status code CONSTRAINT_ERROR shall be returned.
     * 5. If appending the received PresetStruct to the pending list of Presets would cause the total number of pending
     * presets to exceed the value of the NumberOfPresets attribute, a response with the status code RESOURCE_EXHAUSTED
     * shall be returned.
     * 6. If appending the received PresetStruct to the pending list of Presets would cause the total number of pending
     * presets whose PresetScenario field matches the appended preset’s PresetScenario field to exceed the value of the
     * NumberOfPresets field on the PresetTypeStruct whose PresetScenario matches the appended preset’s PresetScenario
     * field, a response with the status code RESOURCE_EXHAUSTED shall be returned.
     * 7. Otherwise, the write shall be pended until receipt of a commit request, and the status code SUCCESS shall be
     * returned.
     * a. If the BuiltIn field is null:
     * i. If there is a PresetStruct in the current value with a matching PresetHandle field, the BuiltIn field on the
     * pending PresetStruct shall be set to the value of the BuiltIn on the matching PresetStruct.
     * ii. Otherwise, the BuiltIn field on the pending PresetStruct shall be set to false.
     * On an attempt to commit, the status of this attribute shall be determined as follows:
     * 1. For all existing presets:
     * a. If, after applying all pending changes, the updated value of the Presets attribute would not contain a
     * PresetStruct with a matching PresetHandle field, indicating the removal of the PresetStruct, the server shall
     * check for invalid removal of the PresetStruct:
     * i. If the BuiltIn field is true on the removed PresetStruct, the attribute status shall be CONSTRAINT_ERROR.
     * ii. If the MSCH feature is supported and the removed PresetHandle would be referenced by any PresetHandle on any
     * ScheduleTransitionStruct on any ScheduleStruct in the updated value of the Schedules attribute, the attribute
     * status shall be INVALID_IN_STATE.
     * iii. If the removed PresetHandle is equal to the value of the ActivePresetHandle attribute, the attribute status
     * shall be INVALID_IN_STATE.
     * 2. Otherwise, the attribute status shall be SUCCESS.
     */
    public List<PresetStruct> presets; // 80 list RW VM
    /**
     * This attribute shall contain a list of ScheduleStructs. On receipt of a write request:
     * 1. For all schedules in the write request:
     * a. If the ScheduleHandle field is null, the ScheduleStruct shall be treated as an added schedule, and the device
     * shall create a new unique value for the ScheduleHandle field.
     * i. If the BuiltIn field is true, a response with the status code CONSTRAINT_ERROR shall be returned.
     * b. Otherwise, if the ScheduleHandle field is not null, the ScheduleStruct shall be treated as a modification of
     * an existing schedule.
     * i. If the value of the ScheduleHandle field does not match any of the existing schedules, a response with the
     * status code NOT_FOUND shall be returned.
     * ii. If the BuiltIn field is true, and the ScheduleStruct in the current value with a matching ScheduleHandle
     * field has a BuiltIn field set to false, a response with the status code CONSTRAINT_ERROR shall be returned.
     * iii. If the BuiltIn field is false, and the ScheduleStruct in the current value with a matching ScheduleHandle
     * field has a BuiltIn field set to true, a response with the status code CONSTRAINT_ERROR shall be returned.
     * c. If the specified SystemMode does not exist in ScheduleTypes, a response with the status code CONSTRAINT_ERROR
     * shall be returned.
     * d. If the number of transitions exceeds the NumberOfScheduleTransitions value, a response with the status code
     * RESOURCE_EXHAUSTED shall be returned.
     * e. If the value of the NumberOfScheduleTransitionsPerDay attribute is not null, and the number of transitions on
     * any single day of the week exceeds the NumberOfScheduleTransitionsPerDay value, a response with the status code
     * RESOURCE_EXHAUSTED shall be returned.
     * f. If the PresetHandle field is present, but the associated ScheduleTypeStruct does not have the SupportsPresets
     * bit set, a response with the status code CONSTRAINT_ERROR shall be returned.
     * g. If the PresetHandle field is present, but after applying all pending changes, the Presets attribute would not
     * contain a PresetStruct whose PresetHandle field matches the value of the PresetHandle field, a response with the
     * status code CONSTRAINT_ERROR shall be returned.
     * h. If the Name is set, but the associated ScheduleTypeStruct does not have the SupportsNames bit set, a response
     * with the status code CONSTRAINT_ERROR shall be returned.
     * i. For all transitions in all schedules in the write request:
     * i. If the PresetHandle field is present, but the ScheduleTypeStruct matching the value of the SystemMode field on
     * the encompassing ScheduleStruct does not have the SupportsPresets bit set, a response with the status code
     * CONSTRAINT_ERROR shall be returned.
     * j. If the PresetHandle field is present, but after applying all pending changes, the Presets attribute would not
     * contain a PresetStruct whose PresetHandle field matches the value of the PresetHandle field, a response with the
     * status code CONSTRAINT_ERROR shall be returned.
     * i. If the SystemMode field is present, but the ScheduleTypeStruct matching the value of the SystemMode field on
     * the encompassing ScheduleStruct does not have the SupportsSetpoints bit set, a response with the status code
     * CONSTRAINT_ERROR shall be returned.
     * ii. If the SystemMode field is has a value of SystemModeOff, but the ScheduleTypeStruct matching the value of the
     * SystemMode field on the encompassing ScheduleStruct does not have the SupportsOff bit set, a response with the
     * status code CONSTRAINT_ERROR shall be returned.
     * k. If the HeatingSetpoint field is present, but the ScheduleTypeStruct matching the value of the SystemMode field
     * on the encompassing ScheduleStruct does not have the SupportsSetpoints bit set, a response with the status code
     * CONSTRAINT_ERROR shall be returned.
     * l. If the CoolingSetpoint field is present, but the ScheduleTypeStruct matching the value of the SystemMode field
     * on the encompassing ScheduleStruct does not have the SupportsSetpoints bit set, a response with the status code
     * CONSTRAINT_ERROR shall be returned.
     * 2. If appending the received ScheduleStruct to the pending list of Schedules would cause the total number of
     * pending schedules to exceed the value of the NumberOfSchedules attribute, a response with the status code
     * RESOURCE_EXHAUSTED shall be returned.
     * 3. If appending the received ScheduleStruct to the pending list of Schedules would cause the total number of
     * pending schedules whose SystemMode field matches the appended schedule’s SystemMode field to exceed the value of
     * the NumberOfSchedules field on the ScheduleTypeStruct whose SystemMode field matches the appended schedule’s
     * SystemMode field, a response with the status code RESOURCE_EXHAUSTED shall be returned.
     * 4. Otherwise, the write shall be pended until receipt of a commit request, and the attribute status shall be
     * SUCCESS.
     * a. If the BuiltIn field is null:
     * i. If there is a ScheduleStruct in the current value with a matching ScheduleHandle field, the BuiltIn field on
     * the pending ScheduleStruct shall be set to the value of the BuiltIn on the matching ScheduleStruct.
     * ii. Otherwise, the BuiltIn field on the pending ScheduleStruct shall be set to false.
     * On an attempt to commit, the status of this attribute shall be determined as follows:
     * 1. For all existing schedules:
     * a. If, after applying all pending changes, the updated value of the Schedules attribute would not contain a
     * ScheduleStruct with a matching ScheduleHandle field, indicating the removal of the ScheduleStruct, the server
     * shall check for invalid removal of the ScheduleStruct:
     * i. If the BuiltIn field is true on the removed ScheduleStruct, the attribute status shall be CONSTRAINT_ERROR.
     * ii. If the removed ScheduleHandle is equal to the value of the ActiveScheduleHandle attribute, the attribute
     * status shall be INVALID_IN_STATE.
     * 2. Otherwise, the attribute status shall be SUCCESS.
     */
    public List<ScheduleStruct> schedules; // 81 list RW VM
    /**
     * If there is a known time when the TemperatureSetpointHold shall be cleared, this attribute shall contain the
     * timestamp in UTC indicating when that will happen. If there is no such known time, this attribute shall be null.
     * If the TemperatureSetpointHold is set to SetpointHoldOff or the TemperatureSetpointHoldDuration is set to null,
     * this attribute shall be set to null indicating there is no hold on the Thermostat either with or without a
     * duration.
     */
    public Integer setpointHoldExpiryTimestamp; // 82 epoch-s R V

    // Structs
    public static class PresetStruct {
        /**
         * This field shall indicate a device generated identifier for this preset. It shall be unique on the device,
         * and shall NOT be reused after the associated preset has been deleted.
         * This field shall only be null when the encompassing PresetStruct is appended to the Presets attribute for the
         * purpose of creating a new Preset. Refer to Presets for the creation of Preset handles.
         */
        public OctetString presetHandle; // octstr
        /**
         * This field shall indicate the associated PresetScenarioEnum value for this preset.
         */
        public PresetScenarioEnum presetScenario; // PresetScenarioEnum
        /**
         * This field shall indicate a name provided by a user. The null value shall indicate no name.
         * Within each subset of presets sharing the same PresetScenario field value, there shall NOT be any presets
         * with the same value, including null as a value, in the Name field.
         */
        public String name; // string
        /**
         * This field shall indicate the cooling setpoint for the preset. Refer to Setpoint Limits for value
         * constraints.
         */
        public Integer coolingSetpoint; // temperature
        /**
         * This field shall indicate the heating setpoint for the preset. Refer to Setpoint Limits for value
         * constraints.
         */
        public Integer heatingSetpoint; // temperature
        /**
         * This field shall indicate whether the preset is marked as &quot;built-in&quot;, meaning that it can be
         * modified, but it cannot be deleted.
         */
        public Boolean builtIn; // bool

        public PresetStruct(OctetString presetHandle, PresetScenarioEnum presetScenario, String name,
                Integer coolingSetpoint, Integer heatingSetpoint, Boolean builtIn) {
            this.presetHandle = presetHandle;
            this.presetScenario = presetScenario;
            this.name = name;
            this.coolingSetpoint = coolingSetpoint;
            this.heatingSetpoint = heatingSetpoint;
            this.builtIn = builtIn;
        }
    }

    public static class PresetTypeStruct {
        /**
         * This field shall specify a PresetScenarioEnum value supported by this thermostat.
         */
        public PresetScenarioEnum presetScenario; // PresetScenarioEnum
        /**
         * This field shall specify a limit for the number of presets for this PresetScenarioEnum.
         */
        public Integer numberOfPresets; // uint8
        /**
         * This field shall specify a bitmap of features for this PresetTypeStruct.
         */
        public PresetTypeFeaturesBitmap presetTypeFeatures; // PresetTypeFeaturesBitmap

        public PresetTypeStruct(PresetScenarioEnum presetScenario, Integer numberOfPresets,
                PresetTypeFeaturesBitmap presetTypeFeatures) {
            this.presetScenario = presetScenario;
            this.numberOfPresets = numberOfPresets;
            this.presetTypeFeatures = presetTypeFeatures;
        }
    }

    /**
     * This represents a single transition in a Thermostat schedule
     */
    public static class WeeklyScheduleTransitionStruct {
        /**
         * This field shall represent the start time of the schedule transition during the associated day. The time will
         * be represented by a 16 bits unsigned integer to designate the minutes since midnight. For example, 6am will
         * be represented by 360 minutes since midnight and 11:30pm will be represented by 1410 minutes since midnight.
         */
        public Integer transitionTime; // uint16
        /**
         * This field shall represent the heat setpoint to be applied at this associated transition start time.
         */
        public Integer heatSetpoint; // temperature
        /**
         * This field shall represent the cool setpoint to be applied at this associated transition start time.
         */
        public Integer coolSetpoint; // temperature

        public WeeklyScheduleTransitionStruct(Integer transitionTime, Integer heatSetpoint, Integer coolSetpoint) {
            this.transitionTime = transitionTime;
            this.heatSetpoint = heatSetpoint;
            this.coolSetpoint = coolSetpoint;
        }
    }

    public static class ScheduleStruct {
        /**
         * This field shall indicate a device generated identifier for this schedule. It shall be unique on the device,
         * and shall NOT be reused after the associated schedule has been deleted.
         * This field shall only be null when the encompassing ScheduleStruct is appended to the Schedules attribute for
         * the purpose of creating a new Schedule. Refer to Schedules for the creation of Schedule handles.
         */
        public OctetString scheduleHandle; // octstr
        /**
         * This field shall specify the default thermostat system mode for transitions in this schedule. The only valid
         * values for this field shall be Auto, Heat, and Cool.
         */
        public SystemModeEnum systemMode; // SystemModeEnum
        /**
         * This field shall specify a name for the ScheduleStruct.
         */
        public String name; // string
        /**
         * This field shall indicate the default PresetHandle value for transitions in this schedule.
         */
        public OctetString presetHandle; // octstr
        /**
         * This field shall specify a list of transitions for the schedule.
         * This field shall NOT contain more than one ScheduleStruct with the same TransitionTime field and overlapping
         * DayOfWeek fields; i.e. there shall be no duplicate transitions.
         * If the NumberOfScheduleTransitionsPerDay attribute is not null, then for each bit in ScheduleDayOfWeekBitmap,
         * the number of transitions with that bit set in DayOfWeek shall NOT be greater than the value of the
         * NumberOfScheduleTransitionsPerDay attribute.
         * For the purposes of determining which ScheduleStruct in this list is currently active, the current time shall
         * be the number of minutes past midnight in the display value of the current time, not the actual number of
         * minutes that have elapsed since midnight. On days which transition into or out of daylight saving time,
         * certain values may repeat or not occur during the transition period.
         * A ScheduleTransitionStruct in this list shall be active if the current day of the week matches its DayOfWeek
         * field and the current time is greater than or equal to the TransitionTime, but less than the TransitionTime
         * on any other ScheduleTransitionStruct in the Transitions field whose DayOfWeek field also matches the current
         * day of the week.
         * If the current time is less than every ScheduleTransitionStruct whose DayOfWeek field also matches the
         * current day of the week, the server shall attempt the same process to identify the active
         * ScheduleTransitionStruct for the day preceding the previously attempted day of the week, repeating until an
         * active ScheduleTransitionStruct is found or the attempted day is the current day of the week again. If no
         * active ScheduleTransitionStruct is found, then the active ScheduleTransitionStruct shall be the
         * ScheduleTransitionStruct with the largest TransitionTime field from the set of ScheduleTransitionStructs
         * whose DayOfWeek field matches the current day of the week.
         */
        public List<ScheduleTransitionStruct> transitions; // list
        /**
         * This field shall indicate whether the schedule is marked as &quot;built-in&quot;, meaning that it can be
         * modified, but it cannot be deleted.
         */
        public Boolean builtIn; // bool

        public ScheduleStruct(OctetString scheduleHandle, SystemModeEnum systemMode, String name,
                OctetString presetHandle, List<ScheduleTransitionStruct> transitions, Boolean builtIn) {
            this.scheduleHandle = scheduleHandle;
            this.systemMode = systemMode;
            this.name = name;
            this.presetHandle = presetHandle;
            this.transitions = transitions;
            this.builtIn = builtIn;
        }
    }

    /**
     * This struct provides a time of day and a set of days of the week for a state transition within a schedule. The
     * thermostat shall use the following order of precedence for determining a new setpoint at the time of transition:
     * 1. If the PresetHandle field is provided, then the setpoint for the PresetStruct in the Presets attribute with
     * that identifier shall be used
     * 2. If either the HeatingSetpoint or CoolingSetpoint is provided, then it shall be used
     * a. If the SystemMode field is provided, the HeatingSetpoint and CoolingSetpoint fields shall be interpreted using
     * the SystemMode field
     * b. If the SystemMode field is not provided, the HeatingSetpoint and CoolingSetpoint fields shall be interpreted
     * using the SystemMode field on the parent ScheduleStruct
     * 3. If neither the PresetHandle field or any Setpoint field is provided, then the PresetHandle field on the parent
     * ScheduleStruct shall be used to determine the active PresetStruct
     * 4. If the PresetHandle is not indicated and no setpoint is provided for the current SystemMode, the server shall
     * use a default value for the current SystemMode.
     * If the setpoint was derived from a preset, then the ActivePresetHandle shall be set to the PresetHandle of that
     * preset.
     * If a CoolingSetpoint was used to determine the cooling setpoint:
     * • If the server supports the OCC feature, and the Occupied bit is not set on the Occupancy attribute, then the
     * UnoccupiedCoolingSetpoint attribute shall be set to the CoolingSetpoint
     * • Otherwise, the OccupiedCoolingSetpoint attribute shall be set to the CoolingSetpoint If a HeatingSetpoint was
     * used to determine the heating setpoint:
     * • If the server supports the OCC feature, and the Occupied bit is not set on the Occupancy attribute, then the
     * UnoccupiedHeatingSetpoint attribute shall be set to the HeatingSetpoint
     * • Otherwise, the OccupiedHeatingSetpoint attribute shall be set to the HeatingSetpoint The
     * ScheduleTransitionStruct shall be invalid if all the following are true:
     * • The HeatingSetpoint field is not provided
     * • The PresetHandle field is not provided
     * • The PresetHandle field on the encompassing ScheduleStruct is not provided
     * • The SystemMode field is provided and has the value Heat or Auto, or the SystemMode field on the parent
     * ScheduleStruct has the value Heat or Auto
     * The ScheduleTransitionStruct shall be invalid if all the following are true:
     * • The CoolingSetpoint field is not provided
     * • The PresetHandle field is not provided
     * • The PresetHandle field on the encompassing ScheduleStruct is not provided
     * • The SystemMode field is provided and has the value Cool or Auto, or the SystemMode field on the parent
     * ScheduleStruct has the value Cool or Auto
     */
    public static class ScheduleTransitionStruct {
        /**
         * This field shall specify a bitmask of days of the week that the transition applies to. The Vacation bit shall
         * NOT be set; vacation schedules shall be set via the vacation preset.
         */
        public ScheduleDayOfWeekBitmap dayOfWeek; // ScheduleDayOfWeekBitmap
        /**
         * This shall specify the time of day at which the transition becomes active, in terms of minutes within the day
         * representing the wall clock, where 0 is 00:00:00, 1 is 00:01:00 and 1439 is 23:59:00.
         * Handling of transitions during the changeover of Daylight Saving Time is implementation-dependent.
         */
        public Integer transitionTime; // uint16
        /**
         * This field shall specify the preset used at the TransitionTime. If this field is provided, then the
         * SystemMode, CoolingSetpoint and HeatingSetpoint fields shall NOT be provided.
         */
        public OctetString presetHandle; // octstr
        /**
         * This shall specify the default mode to which the thermostat will switch for this transition, overriding the
         * default for the schedule. The only valid values for this field shall be Auto, Heat, Cool and Off. This field
         * shall only be included when the required system mode differs from the schedule’s default SystemMode.
         */
        public SystemModeEnum systemMode; // SystemModeEnum
        /**
         * This field shall specify the cooling setpoint for the transition. If PresetHandle is set, this field shall
         * NOT be included. Refer to Setpoint Limits for value constraints.
         */
        public Integer coolingSetpoint; // temperature
        /**
         * This field shall specify the cooling setpoint for the transition. If PresetHandle is set, this field shall
         * NOT be included. Refer to Setpoint Limits for value constraints.
         */
        public Integer heatingSetpoint; // temperature

        public ScheduleTransitionStruct(ScheduleDayOfWeekBitmap dayOfWeek, Integer transitionTime,
                OctetString presetHandle, SystemModeEnum systemMode, Integer coolingSetpoint, Integer heatingSetpoint) {
            this.dayOfWeek = dayOfWeek;
            this.transitionTime = transitionTime;
            this.presetHandle = presetHandle;
            this.systemMode = systemMode;
            this.coolingSetpoint = coolingSetpoint;
            this.heatingSetpoint = heatingSetpoint;
        }
    }

    public static class ScheduleTypeStruct {
        /**
         * This field shall specify a SystemModeEnum supported by this thermostat for Schedules. The only valid values
         * for this field shall be Auto, Heat, and Cool.
         */
        public SystemModeEnum systemMode; // SystemModeEnum
        /**
         * This field shall specify a limit for the number of Schedules for this SystemMode.
         */
        public Integer numberOfSchedules; // uint8
        /**
         * This field shall specify a bitmap of features for this schedule entry. At least one of SupportsPresets and
         * SupportsSetpoints shall be set.
         */
        public ScheduleTypeFeaturesBitmap scheduleTypeFeatures; // ScheduleTypeFeaturesBitmap

        public ScheduleTypeStruct(SystemModeEnum systemMode, Integer numberOfSchedules,
                ScheduleTypeFeaturesBitmap scheduleTypeFeatures) {
            this.systemMode = systemMode;
            this.numberOfSchedules = numberOfSchedules;
            this.scheduleTypeFeatures = scheduleTypeFeatures;
        }
    }

    // Enums
    public enum ACCapacityFormatEnum implements MatterEnum {
        BT_UH(0, "Bt Uh");

        public final Integer value;
        public final String label;

        private ACCapacityFormatEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ACCompressorTypeEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        T1(1, "T 1"),
        T2(2, "T 2"),
        T3(3, "T 3");

        public final Integer value;
        public final String label;

        private ACCompressorTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ACLouverPositionEnum implements MatterEnum {
        CLOSED(1, "Closed"),
        OPEN(2, "Open"),
        QUARTER(3, "Quarter"),
        HALF(4, "Half"),
        THREE_QUARTERS(5, "Three Quarters");

        public final Integer value;
        public final String label;

        private ACLouverPositionEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ACRefrigerantTypeEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        R22(1, "R 22"),
        R410A(2, "R 410 A"),
        R407C(3, "R 407 C");

        public final Integer value;
        public final String label;

        private ACRefrigerantTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ACTypeEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        COOLING_FIXED(1, "Cooling Fixed"),
        HEAT_PUMP_FIXED(2, "Heat Pump Fixed"),
        COOLING_INVERTER(3, "Cooling Inverter"),
        HEAT_PUMP_INVERTER(4, "Heat Pump Inverter");

        public final Integer value;
        public final String label;

        private ACTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum SetpointRaiseLowerModeEnum implements MatterEnum {
        HEAT(0, "Heat"),
        COOL(1, "Cool"),
        BOTH(2, "Both");

        public final Integer value;
        public final String label;

        private SetpointRaiseLowerModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * &gt; [!NOTE]
     * &gt; CoolingAndHeating
     * A thermostat indicating it supports CoolingAndHeating (or CoolingAndHeatingWithReheat) SHOULD be able to request
     * heating or cooling on demand and will usually support the Auto SystemMode.
     * Systems which support cooling or heating, requiring external intervention to change modes or where the whole
     * building must be in the same mode, SHOULD report CoolingOnly or HeatingOnly based on the current capability.
     */
    public enum ControlSequenceOfOperationEnum implements MatterEnum {
        COOLING_ONLY(0, "Cooling Only"),
        COOLING_WITH_REHEAT(1, "Cooling With Reheat"),
        HEATING_ONLY(2, "Heating Only"),
        HEATING_WITH_REHEAT(3, "Heating With Reheat"),
        COOLING_AND_HEATING(4, "Cooling And Heating"),
        COOLING_AND_HEATING_WITH_REHEAT(5, "Cooling And Heating With Reheat");

        public final Integer value;
        public final String label;

        private ControlSequenceOfOperationEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum PresetScenarioEnum implements MatterEnum {
        OCCUPIED(1, "Occupied"),
        UNOCCUPIED(2, "Unoccupied"),
        SLEEP(3, "Sleep"),
        WAKE(4, "Wake"),
        VACATION(5, "Vacation"),
        GOING_TO_SLEEP(6, "Going To Sleep"),
        USER_DEFINED(254, "User Defined");

        public final Integer value;
        public final String label;

        private PresetScenarioEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum SetpointChangeSourceEnum implements MatterEnum {
        MANUAL(0, "Manual"),
        SCHEDULE(1, "Schedule"),
        EXTERNAL(2, "External");

        public final Integer value;
        public final String label;

        private SetpointChangeSourceEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum StartOfWeekEnum implements MatterEnum {
        SUNDAY(0, "Sunday"),
        MONDAY(1, "Monday"),
        TUESDAY(2, "Tuesday"),
        WEDNESDAY(3, "Wednesday"),
        THURSDAY(4, "Thursday"),
        FRIDAY(5, "Friday"),
        SATURDAY(6, "Saturday");

        public final Integer value;
        public final String label;

        private StartOfWeekEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * Table 9. Interpretation of Heat, Cool and Auto SystemModeEnum Values
     */
    public enum SystemModeEnum implements MatterEnum {
        OFF(0, "Off"),
        AUTO(1, "Auto"),
        COOL(3, "Cool"),
        HEAT(4, "Heat"),
        EMERGENCY_HEAT(5, "Emergency Heat"),
        PRECOOLING(6, "Precooling"),
        FAN_ONLY(7, "Fan Only"),
        DRY(8, "Dry"),
        SLEEP(9, "Sleep");

        public final Integer value;
        public final String label;

        private SystemModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ThermostatRunningModeEnum implements MatterEnum {
        OFF(0, "Off"),
        COOL(3, "Cool"),
        HEAT(4, "Heat");

        public final Integer value;
        public final String label;

        private ThermostatRunningModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum TemperatureSetpointHoldEnum implements MatterEnum {
        SETPOINT_HOLD_OFF(0, "Setpoint Hold Off"),
        SETPOINT_HOLD_ON(1, "Setpoint Hold On");

        public final Integer value;
        public final String label;

        private TemperatureSetpointHoldEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class ACErrorCodeBitmap {
        public boolean compressorFail;
        public boolean roomSensorFail;
        public boolean outdoorSensorFail;
        public boolean coilSensorFail;
        public boolean fanFail;

        public ACErrorCodeBitmap(boolean compressorFail, boolean roomSensorFail, boolean outdoorSensorFail,
                boolean coilSensorFail, boolean fanFail) {
            this.compressorFail = compressorFail;
            this.roomSensorFail = roomSensorFail;
            this.outdoorSensorFail = outdoorSensorFail;
            this.coilSensorFail = coilSensorFail;
            this.fanFail = fanFail;
        }
    }

    public static class AlarmCodeBitmap {
        public boolean initialization;
        public boolean hardware;
        public boolean selfCalibration;

        public AlarmCodeBitmap(boolean initialization, boolean hardware, boolean selfCalibration) {
            this.initialization = initialization;
            this.hardware = hardware;
            this.selfCalibration = selfCalibration;
        }
    }

    public static class HVACSystemTypeBitmap {
        /**
         * Stage of cooling the HVAC system is using.
         * These bits shall indicate what stage of cooling the HVAC system is using.
         * • 00 &#x3D; Cool Stage 1
         * • 01 &#x3D; Cool Stage 2
         * • 10 &#x3D; Cool Stage 3
         * • 11 &#x3D; Reserved
         */
        public short coolingStage;
        /**
         * Stage of heating the HVAC system is using.
         * These bits shall indicate what stage of heating the HVAC system is using.
         * • 00 &#x3D; Heat Stage 1
         * • 01 &#x3D; Heat Stage 2
         * • 10 &#x3D; Heat Stage 3
         * • 11 &#x3D; Reserved
         */
        public short heatingStage;
        /**
         * Is the heating type Heat Pump.
         * This bit shall indicate whether the HVAC system is conventional or a heat pump.
         * • 0 &#x3D; Conventional
         * • 1 &#x3D; Heat Pump
         */
        public boolean heatingIsHeatPump;
        /**
         * Does the HVAC system use fuel.
         * This bit shall indicate whether the HVAC system uses fuel.
         * • 0 &#x3D; Does not use fuel
         * • 1 &#x3D; Uses fuel
         */
        public boolean heatingUsesFuel;

        public HVACSystemTypeBitmap(short coolingStage, short heatingStage, boolean heatingIsHeatPump,
                boolean heatingUsesFuel) {
            this.coolingStage = coolingStage;
            this.heatingStage = heatingStage;
            this.heatingIsHeatPump = heatingIsHeatPump;
            this.heatingUsesFuel = heatingUsesFuel;
        }
    }

    public static class OccupancyBitmap {
        /**
         * Indicates the occupancy state
         * If this bit is set, it shall indicate the occupied state else if the bit if not set, it shall indicate the
         * unoccupied state.
         */
        public boolean occupied;

        public OccupancyBitmap(boolean occupied) {
            this.occupied = occupied;
        }
    }

    public static class PresetTypeFeaturesBitmap {
        public boolean automatic;
        public boolean supportsNames;

        public PresetTypeFeaturesBitmap(boolean automatic, boolean supportsNames) {
            this.automatic = automatic;
            this.supportsNames = supportsNames;
        }
    }

    public static class ProgrammingOperationModeBitmap {
        public boolean scheduleActive;
        public boolean autoRecovery;
        public boolean economy;

        public ProgrammingOperationModeBitmap(boolean scheduleActive, boolean autoRecovery, boolean economy) {
            this.scheduleActive = scheduleActive;
            this.autoRecovery = autoRecovery;
            this.economy = economy;
        }
    }

    public static class RelayStateBitmap {
        public boolean heat;
        public boolean cool;
        public boolean fan;
        public boolean heatStage2;
        public boolean coolStage2;
        public boolean fanStage2;
        public boolean fanStage3;

        public RelayStateBitmap(boolean heat, boolean cool, boolean fan, boolean heatStage2, boolean coolStage2,
                boolean fanStage2, boolean fanStage3) {
            this.heat = heat;
            this.cool = cool;
            this.fan = fan;
            this.heatStage2 = heatStage2;
            this.coolStage2 = coolStage2;
            this.fanStage2 = fanStage2;
            this.fanStage3 = fanStage3;
        }
    }

    public static class RemoteSensingBitmap {
        public boolean localTemperature;
        /**
         * OutdoorTemperature is derived from a remote node
         * This bit shall be supported if the OutdoorTemperature attribute is supported.
         */
        public boolean outdoorTemperature;
        public boolean occupancy;

        public RemoteSensingBitmap(boolean localTemperature, boolean outdoorTemperature, boolean occupancy) {
            this.localTemperature = localTemperature;
            this.outdoorTemperature = outdoorTemperature;
            this.occupancy = occupancy;
        }
    }

    public static class ScheduleTypeFeaturesBitmap {
        /**
         * Supports presets
         * This bit shall indicate that any ScheduleStruct with a SystemMode field whose value matches the SystemMode
         * field on the encompassing ScheduleTypeStruct supports specifying presets on ScheduleTransitionStructs
         * contained in its Transitions field.
         */
        public boolean supportsPresets;
        /**
         * Supports setpoints
         * This bit shall indicate that any ScheduleStruct with a SystemMode field whose value matches the SystemMode
         * field on the encompassing ScheduleTypeStruct supports specifying setpoints on ScheduleTransitionStructs
         * contained in its Transitions field.
         */
        public boolean supportsSetpoints;
        /**
         * Supports user-provided names
         * This bit shall indicate that any ScheduleStruct with a SystemMode field whose value matches the SystemMode
         * field on the encompassing ScheduleTypeStruct supports setting the value of the Name field.
         */
        public boolean supportsNames;
        /**
         * Supports transitioning to SystemModeOff
         * This bit shall indicate that any ScheduleStruct with a SystemMode field whose value matches the SystemMode
         * field on the encompassing ScheduleTypeStruct supports setting its SystemMode field to Off.
         */
        public boolean supportsOff;

        public ScheduleTypeFeaturesBitmap(boolean supportsPresets, boolean supportsSetpoints, boolean supportsNames,
                boolean supportsOff) {
            this.supportsPresets = supportsPresets;
            this.supportsSetpoints = supportsSetpoints;
            this.supportsNames = supportsNames;
            this.supportsOff = supportsOff;
        }
    }

    public static class ScheduleDayOfWeekBitmap {
        public boolean sunday;
        public boolean monday;
        public boolean tuesday;
        public boolean wednesday;
        public boolean thursday;
        public boolean friday;
        public boolean saturday;
        public boolean away;

        public ScheduleDayOfWeekBitmap(boolean sunday, boolean monday, boolean tuesday, boolean wednesday,
                boolean thursday, boolean friday, boolean saturday, boolean away) {
            this.sunday = sunday;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
            this.away = away;
        }
    }

    public static class ScheduleModeBitmap {
        public boolean heatSetpointPresent;
        public boolean coolSetpointPresent;

        public ScheduleModeBitmap(boolean heatSetpointPresent, boolean coolSetpointPresent) {
            this.heatSetpointPresent = heatSetpointPresent;
            this.coolSetpointPresent = coolSetpointPresent;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Thermostat is capable of managing a heating device
         */
        public boolean heating;
        /**
         * 
         * Thermostat is capable of managing a cooling device
         */
        public boolean cooling;
        /**
         * 
         * Supports Occupied and Unoccupied setpoints
         */
        public boolean occupancy;
        /**
         * 
         * Supports remote configuration of a weekly schedule of setpoint transitions
         */
        public boolean scheduleConfiguration;
        /**
         * 
         * Supports configurable setback (or span)
         */
        public boolean setback;
        /**
         * 
         * Supports a System Mode of Auto
         */
        public boolean autoMode;
        /**
         * 
         * This feature indicates that the Calculated Local Temperature used internally is unavailable to report
         * externally, for example due to the temperature control being done by a separate subsystem which does not
         * offer a view into the currently measured temperature, but allows setpoints to be provided.
         */
        public boolean localTemperatureNotExposed;
        /**
         * 
         * Supports enhanced schedules
         */
        public boolean matterScheduleConfiguration;
        /**
         * 
         * Thermostat supports setpoint presets
         */
        public boolean presets;

        public FeatureMap(boolean heating, boolean cooling, boolean occupancy, boolean scheduleConfiguration,
                boolean setback, boolean autoMode, boolean localTemperatureNotExposed,
                boolean matterScheduleConfiguration, boolean presets) {
            this.heating = heating;
            this.cooling = cooling;
            this.occupancy = occupancy;
            this.scheduleConfiguration = scheduleConfiguration;
            this.setback = setback;
            this.autoMode = autoMode;
            this.localTemperatureNotExposed = localTemperatureNotExposed;
            this.matterScheduleConfiguration = matterScheduleConfiguration;
            this.presets = presets;
        }
    }

    public ThermostatCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 513, "Thermostat");
    }

    protected ThermostatCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    public static ClusterCommand setpointRaiseLower(SetpointRaiseLowerModeEnum mode, Integer amount) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (mode != null) {
            map.put("mode", mode);
        }
        if (amount != null) {
            map.put("amount", amount);
        }
        return new ClusterCommand("setpointRaiseLower", map);
    }

    /**
     * This command is used to update the thermostat weekly setpoint schedule from a management system. If the
     * thermostat already has a weekly setpoint schedule programmed, then it SHOULD replace each daily setpoint set as
     * it receives the updates from the management system. For example, if the thermostat has 4 setpoints for every day
     * of the week and is sent a SetWeeklySchedule command with one setpoint for Saturday then the thermostat SHOULD
     * remove all 4 setpoints for Saturday and replace those with the updated setpoint but leave all other days
     * unchanged. If the schedule is larger than what fits in one frame or contains more than 10 transitions, the
     * schedule shall then be sent using multiple SetWeeklySchedule Commands.
     */
    public static ClusterCommand setWeeklySchedule(Integer numberOfTransitionsForSequence,
            ScheduleDayOfWeekBitmap dayOfWeekForSequence, ScheduleModeBitmap modeForSequence,
            List<WeeklyScheduleTransitionStruct> transitions) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (numberOfTransitionsForSequence != null) {
            map.put("numberOfTransitionsForSequence", numberOfTransitionsForSequence);
        }
        if (dayOfWeekForSequence != null) {
            map.put("dayOfWeekForSequence", dayOfWeekForSequence);
        }
        if (modeForSequence != null) {
            map.put("modeForSequence", modeForSequence);
        }
        if (transitions != null) {
            map.put("transitions", transitions);
        }
        return new ClusterCommand("setWeeklySchedule", map);
    }

    public static ClusterCommand getWeeklySchedule(ScheduleDayOfWeekBitmap daysToReturn,
            ScheduleModeBitmap modeToReturn) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (daysToReturn != null) {
            map.put("daysToReturn", daysToReturn);
        }
        if (modeToReturn != null) {
            map.put("modeToReturn", modeToReturn);
        }
        return new ClusterCommand("getWeeklySchedule", map);
    }

    /**
     * This command is used to clear the weekly schedule. The Clear weekly schedule has no payload.
     * Upon receipt, all transitions currently stored shall be cleared and a default response of SUCCESS shall be sent
     * in response. There are no error responses to this command.
     */
    public static ClusterCommand clearWeeklySchedule() {
        return new ClusterCommand("clearWeeklySchedule");
    }

    public static ClusterCommand setActiveScheduleRequest(OctetString scheduleHandle) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (scheduleHandle != null) {
            map.put("scheduleHandle", scheduleHandle);
        }
        return new ClusterCommand("setActiveScheduleRequest", map);
    }

    public static ClusterCommand setActivePresetRequest(OctetString presetHandle) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (presetHandle != null) {
            map.put("presetHandle", presetHandle);
        }
        return new ClusterCommand("setActivePresetRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "localTemperature : " + localTemperature + "\n";
        str += "outdoorTemperature : " + outdoorTemperature + "\n";
        str += "occupancy : " + occupancy + "\n";
        str += "absMinHeatSetpointLimit : " + absMinHeatSetpointLimit + "\n";
        str += "absMaxHeatSetpointLimit : " + absMaxHeatSetpointLimit + "\n";
        str += "absMinCoolSetpointLimit : " + absMinCoolSetpointLimit + "\n";
        str += "absMaxCoolSetpointLimit : " + absMaxCoolSetpointLimit + "\n";
        str += "piCoolingDemand : " + piCoolingDemand + "\n";
        str += "piHeatingDemand : " + piHeatingDemand + "\n";
        str += "localTemperatureCalibration : " + localTemperatureCalibration + "\n";
        str += "occupiedCoolingSetpoint : " + occupiedCoolingSetpoint + "\n";
        str += "occupiedHeatingSetpoint : " + occupiedHeatingSetpoint + "\n";
        str += "unoccupiedCoolingSetpoint : " + unoccupiedCoolingSetpoint + "\n";
        str += "unoccupiedHeatingSetpoint : " + unoccupiedHeatingSetpoint + "\n";
        str += "minHeatSetpointLimit : " + minHeatSetpointLimit + "\n";
        str += "maxHeatSetpointLimit : " + maxHeatSetpointLimit + "\n";
        str += "minCoolSetpointLimit : " + minCoolSetpointLimit + "\n";
        str += "maxCoolSetpointLimit : " + maxCoolSetpointLimit + "\n";
        str += "minSetpointDeadBand : " + minSetpointDeadBand + "\n";
        str += "remoteSensing : " + remoteSensing + "\n";
        str += "controlSequenceOfOperation : " + controlSequenceOfOperation + "\n";
        str += "systemMode : " + systemMode + "\n";
        str += "thermostatRunningMode : " + thermostatRunningMode + "\n";
        str += "startOfWeek : " + startOfWeek + "\n";
        str += "numberOfWeeklyTransitions : " + numberOfWeeklyTransitions + "\n";
        str += "numberOfDailyTransitions : " + numberOfDailyTransitions + "\n";
        str += "temperatureSetpointHold : " + temperatureSetpointHold + "\n";
        str += "temperatureSetpointHoldDuration : " + temperatureSetpointHoldDuration + "\n";
        str += "thermostatProgrammingOperationMode : " + thermostatProgrammingOperationMode + "\n";
        str += "thermostatRunningState : " + thermostatRunningState + "\n";
        str += "setpointChangeSource : " + setpointChangeSource + "\n";
        str += "setpointChangeAmount : " + setpointChangeAmount + "\n";
        str += "setpointChangeSourceTimestamp : " + setpointChangeSourceTimestamp + "\n";
        str += "occupiedSetback : " + occupiedSetback + "\n";
        str += "occupiedSetbackMin : " + occupiedSetbackMin + "\n";
        str += "occupiedSetbackMax : " + occupiedSetbackMax + "\n";
        str += "unoccupiedSetback : " + unoccupiedSetback + "\n";
        str += "unoccupiedSetbackMin : " + unoccupiedSetbackMin + "\n";
        str += "unoccupiedSetbackMax : " + unoccupiedSetbackMax + "\n";
        str += "emergencyHeatDelta : " + emergencyHeatDelta + "\n";
        str += "acType : " + acType + "\n";
        str += "acCapacity : " + acCapacity + "\n";
        str += "acRefrigerantType : " + acRefrigerantType + "\n";
        str += "acCompressorType : " + acCompressorType + "\n";
        str += "acErrorCode : " + acErrorCode + "\n";
        str += "acLouverPosition : " + acLouverPosition + "\n";
        str += "acCoilTemperature : " + acCoilTemperature + "\n";
        str += "acCapacityFormat : " + acCapacityFormat + "\n";
        str += "presetTypes : " + presetTypes + "\n";
        str += "scheduleTypes : " + scheduleTypes + "\n";
        str += "numberOfPresets : " + numberOfPresets + "\n";
        str += "numberOfSchedules : " + numberOfSchedules + "\n";
        str += "numberOfScheduleTransitions : " + numberOfScheduleTransitions + "\n";
        str += "numberOfScheduleTransitionPerDay : " + numberOfScheduleTransitionPerDay + "\n";
        str += "activePresetHandle : " + activePresetHandle + "\n";
        str += "activeScheduleHandle : " + activeScheduleHandle + "\n";
        str += "presets : " + presets + "\n";
        str += "schedules : " + schedules + "\n";
        str += "setpointHoldExpiryTimestamp : " + setpointHoldExpiryTimestamp + "\n";
        return str;
    }
}
