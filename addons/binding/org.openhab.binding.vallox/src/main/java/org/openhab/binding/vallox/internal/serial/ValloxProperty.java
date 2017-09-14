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
    FAN_SPEED("FanSpeed"), // VALLOX_VARIABLE_FAN_SPEED
    TEMP_INSIDE("TempInside"), // VALLOX_VARIABLE_TEMP_INSIDE
    TEMP_OUTSIDE("TempOutside"), // VALLOX_VARIABLE_TEMP_OUTSIDE
    TEMP_EXHAUST("TempExhaust"), // VALLOX_VARIABLE_TEMP_EXHAUST
    TEMP_INCOMMING("TempIncomming"), // VALLOX_VARIABLE_TEMP_INCOMMING

    // status bits
    POWER_STATE("PowerState"), // VALLOX_VARIABLE_SELECT
    CO2_ADJUST_STATE("CO2AdjustState"), // VALLOX_VARIABLE_SELECT
    HUMIDITY_ADJUST_STATE("HumidityAdjustState"), // VALLOX_VARIABLE_SELECT
    HEATING_STATE("HeatingState"), // VALLOX_VARIABLE_SELECT
    FILTER_GUARD_INDICATOR("FilterGuardIndicator"), // VALLOX_VARIABLE_SELECT
    HEATING_INDICATOR("HeatingIndicator"), // VALLOX_VARIABLE_SELECT
    FAULT_INDICATOR("FaultIndicator"), // VALLOX_VARIABLE_SELECT
    SERVICE_REMINDER_INDICATOR("ServiceReminderIndicator"), // VALLOX_VARIABLE_SELECT

    HUMIDITY("Humidity"), // VALLOX_VARIABLE_HUMIDITY
    BASIC_HUMIDITY_LEVEL("BasicHumidityLevel"), // VALLOX_VARIABLE_BASIC_HUMIDITY_LEVEL
    HUMIDITY_SENSOR_1("HumiditySensor1"), // VALLOX_VARIABLE_HUMIDITY_SENSOR1
    HUMIDITY_SENSOR_2("HumiditySensor2"), // VALLOX_VARIABLE_HUMIDITY_SENSOR2

    CO2_HIGH("CO2High"), // VALLOX_VARIABLE_CO2_HIGH
    CO2_LOW("CO2Low"), // VALLOX_VARIABLE_CO2_LOW
    CO2_SETPOINT_HIGH("CO2SetPointHigh"), // VALLOX_VARIABLE_CO2_SET_POINT_UPPER
    CO2_SETPOINT_LOW("CO2SetPointLow"), // VALLOX_VARIABLE_CO2_SET_POINT_LOWER

    FAN_SPEED_MAX("FanSpeedMax"), // VALLOX_VARIABLE_FAN_SPEED_MAX
    FAN_SPEED_MIN("FanSpeedMin"), // VALLOX_VARIABLE_FAN_SPEED_MIN
    DC_FAN_INPUT_ADJUSTMENT("DCFanInputAdjustment"), // VALLOX_VARIABLE_DC_FAN_INPUT_ADJUSTMENT
    DC_FAN_OUTPUT_ADJUSTMENT("DCFanOutputAdjustment"), // VALLOX_VARIABLE_DC_FAN_OUTPUT_ADJUSTMENT
    INPUT_FAN_STOP_THRESHOLD("InputFanStopThreshold"), // VALLOX_VARIABLE_INPUT_FAN_STOP
    HEATING_SETPOINT("HeatingSetPoint"), // VALLOX_VARIABLE_HEATING_SET_POINT
    PRE_HEATING_SETPOINT("PreHeatingSetPoint"), // VALLOX_VARIABLE_PRE_HEATING_SET_POINT
    HRC_BYPASS_THRESHOLD("HrcBypassThreshold"), // VALLOX_VARIABLE_HRC_BYPASS
    CELL_DEFROSTING_THRESHOLD("CellDefrostingThreshold"), // VALLOX_VARIABLE_CELL_DEFROSTING

    // program
    ADJUSTMENT_INTERVAL_MINUTES("AdjustmentIntervalMinutes"), // VALLOX_VARIABLE_PROGRAM
    AUTOMATIC_HUMIDITY_LEVEL_SEEKER_STATE("AutomaticHumidityLevelSeekerState"), // VALLOX_VARIABLE_PROGRAM
    BOOST_SWITCH_MODE("BoostSwitchMode"), // VALLOX_VARIABLE_PROGRAM
    RADIATOR_TYPE("RadiatorType"), // VALLOX_VARIABLE_PROGRAM
    CASCADE_ADJUST("CascadeAdjust"), // VALLOX_VARIABLE_PROGRAM

    // program2
    MAX_SPEED_LIMIT_MODE("MaxSpeedLimitMode"), // VALLOX_VARIABLE_PROGRAM2

    SERVICE_REMINDER("ServiceReminder"), // VALLOX_VARIABLE_SERVICE_REMINDER

    // ioport multi purpose 1
    POST_HEATING_ON("PostHeatingOn"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_1

    // ioport multi purpose 2
    DAMPER_MOTOR_POSITION("DamperMotorPosition"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    FAULT_SIGNAL_RELAY_CLOSED("FaultSignalRelayClosed"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    SUPPLY_FAN_OFF("SupplyFanOff"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    PRE_HEATING_ON("PreHeatingOn"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    EXHAUST_FAN_OFF("ExhaustFanOff"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2
    FIRE_PLACE_BOOSTER_CLOSED("FirePlaceBoosterClosed"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2

    INCOMMING_CURRENT("IncommingCurrent"), // VALLOX_VARIABLE_CURRENT_INCOMMING
    LAST_ERROR_NUMBER("LastErrorNumber"), // VALLOX_VARIABLE_LAST_ERROR_NUMBER

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
    IN_EFFICIENCY("InEfficiency"),
    OUT_EFFICIENCY("OutEfficiency"),
    AVERAGE_EFFICIENCY("AverageEfficiency"),

    // virtual properties to be able to poll for this variable
    SELECT_STATUS("SelectStatus"),
    PROGRAM("Program"), // VALLOX_VARIABLE_PROGRAM
    PROGRAM_2("Program2"), // VALLOX_VARIABLE_PROGRAM2
    IO_PORT_MULTI_PURPOSE_1("IoPortMultiPurpose1"), // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_1
    IO_PORT_MULTI_PURPOSE_2("IoPortMultiPurpose2"); // VALLOX_VARIABLE_IOPORT_MULTI_PURPOSE_2

    ValloxProperty(String channelName) {
        this.channelName = channelName;
    }

    String channelName;

    /**
     * Get the String representation of the Property as used in the corresponding Channel Name in
     * the channel XML declaration.
     * 
     * @return Channel name exactly as specified
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Get the Vallox Property for the given channel name.
     * Might throw IllegalArgumentException if no Property with the
     * channel name is defined.
     * 
     * @param channelName
     * @return ValloxProperty with given channelName
     */
    public static ValloxProperty getProperty(String channelName) {
        for (ValloxProperty p : values()) {
            if (p.getChannelName().equals(channelName)) {
                return p;
            }
        }
        // just like valueOf(String name) does
        throw new IllegalArgumentException("There is no ValloxProperty with channelName " + channelName);
    }
}
