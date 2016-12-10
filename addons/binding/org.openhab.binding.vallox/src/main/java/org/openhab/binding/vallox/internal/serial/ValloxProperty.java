/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.serial;

/**
 * Enum that represent channels which represent
 * properties of the vallox unit. Enum used here because
 * there are quite many different literals and as Enum it
 * can be used in switch-statements.
 *
 * The Enum literals here are in CamelCase to allow a simple
 * mapping of this vast list of enums to corresponding thing
 * channel-id-Strings.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public enum ValloxProperty {
    // sensor data
    FanSpeed, // VALLOX_VARIABLE_FAN_SPEED
    TempInside, // VALLOX_VARIABLE_TEMP_INSIDE
    TempOutside, // VALLOX_VARIABLE_TEMP_OUTSIDE
    TempExhaust, // VALLOX_VARIABLE_TEMP_EXHAUST
    TempIncomming, // VALLOX_VARIABLE_TEMP_INCOMMING

    // status bits
    PowerState, // VALLOX_VARIABLE_SELECT
    CO2AdjustState, // VALLOX_VARIABLE_SELECT
    HumidityAdjustState, // VALLOX_VARIABLE_SELECT
    HeatingState, // VALLOX_VARIABLE_SELECT
    FilterGuardIndicator, // VALLOX_VARIABLE_SELECT
    HeatingIndicator, // VALLOX_VARIABLE_SELECT
    FaultIndicator, // VALLOX_VARIABLE_SELECT
    ServiceReminderIndicator, // VALLOX_VARIABLE_SELECT

    Humidity, // VALLOX_VARIABLE_HUMIDITY
    BasicHumidityLevel, // VALLOX_VARIABLE_BASIC_HUMIDITY_LEVEL
    HumiditySensor1, // VALLOX_VARIABLE_HUMIDITY_SENSOR1
    HumiditySensor2, // VALLOX_VARIABLE_HUMIDITY_SENSOR2

    CO2High, // VALLOX_VARIABLE_CO2_HIGH
    CO2Low, // VALLOX_VARIABLE_CO2_LOW
    CO2SetPointHigh, // VALLOX_VARIABLE_CO2_SET_POINT_UPPER
    CO2SetPointLow, // VALLOX_VARIABLE_CO2_SET_POINT_LOWER

    FanSpeedMax, // VALLOX_VARIABLE_FAN_SPEED_MAX
    FanSpeedMin, // VALLOX_VARIABLE_FAN_SPEED_MIN
    DCFanInputAdjustment, // VALLOX_VARIABLE_DC_FAN_INPUT_ADJUSTMENT
    DCFanOutputAdjustment, // VALLOX_VARIABLE_DC_FAN_OUTPUT_ADJUSTMENT
    InputFanStopThreshold, // VALLOX_VARIABLE_INPUT_FAN_STOP
    HeatingSetPoint, // VALLOX_VARIABLE_HEATING_SET_POINT
    PreHeatingSetPoint, // VALLOX_VARIABLE_PRE_HEATING_SET_POINT
    HrcBypassThreshold, // VALLOX_VARIABLE_HRC_BYPASS
    CellDefrostingThreshold, // VALLOX_VARIABLE_CELL_DEFROSTING

    // program
    AdjustmentIntervalMinutes, // VALLOX_VARIABLE_PROGRAM
    AutomaticHumidityLevelSeekerState, // VALLOX_VARIABLE_PROGRAM
    BoostSwitchMode, // VALLOX_VARIABLE_PROGRAM
    RadiatorType, // VALLOX_VARIABLE_PROGRAM
    CascadeAdjust, // VALLOX_VARIABLE_PROGRAM

    // program2
    MaxSpeedLimitMode, // VALLOX_VARIABLE_PROGRAM2

    ServiceReminder, // VALLOX_VARIABLE_SERVICE_REMINDER

    // ioport multi purpose 1
    PostHeatingOn, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_1

    // ioport multi purpose 2
    DamperMotorPosition, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    FaultSignalRelayClosed, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    SupplyFanOff, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    PreHeatingOn, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    ExhaustFanOff, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    FirePlaceBoosterClosed, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2

    IncommingCurrent, // VALLOX_VARIABLE_CURRENT_INCOMMING
    LastErrorNumber, // VALLOX_VARIABLE_LAST_ERROR_NUMBER

    // those variables might be implemented in future
    // VALLOX_VARIABLE_IOPORT_FANSPEED_RELAYS
    // VALLOX_VARIABLE_INSTALLED_CO2_SENSORS
    // VALLOX_VARIABLE_POST_HEATING_ON_COUNTER
    // VALLOX_VARIABLE_POST_HEATING_OFF_TIME
    // VALLOX_VARIABLE_POST_HEATING_TARGET_VALUE
    // VALLOX_VARIABLE_FLAGS_1
    // VALLOX_VARIABLE_FLAGS_2
    // VALLOX_VARIABLE_FLAGS_3
    // VALLOX_VARIABLE_FLAGS_4
    // VALLOX_VARIABLE_FLAGS_5
    // VALLOX_VARIABLE_FLAGS_6
    // VALLOX_VARIABLE_FIRE_PLACE_BOOSTER_COUNTER
    // VALLOX_VARIABLE_MAINTENANCE_MONTH_COUNTER

    // calculated properties
    InEfficiency,
    OutEfficiency,
    AverageEfficiency,

    // virtual properties to be able to poll for this variable
    SelectStatus,
    Program, // VALLOX_VARIABLE_PROGRAM
    Program2, // VALLOX_VARIABLE_PROGRAM2
    IoPortMultiPurpose1, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_1
    IoPortMultiPurpose2, // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
}
