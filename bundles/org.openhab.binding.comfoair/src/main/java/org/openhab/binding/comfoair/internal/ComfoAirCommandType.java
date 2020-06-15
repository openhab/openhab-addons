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
package org.openhab.binding.comfoair.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.comfoair.internal.datatypes.ComfoAirDataType;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeBoolean;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeMessage;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeNumber;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeRPM;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeTemperature;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeTime;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeVolt;

/**
 * Represents all valid commands which could be processed by this binding
 *
 * @author Holger Hees - Initial Contribution
 * @author Hans Böhm - Refactoring
 */
@NonNullByDefault
public enum ComfoAirCommandType {
    /**
     * Below all valid commands to change or read parameters from ComfoAir
     *
     * @param key
     *            command name
     * @param data_type
     *            data type (can be: DataTypeBoolean.class, DataTypeMessage.class,
     *            DataTypeNumber.class, DataTypeRPM.class,
     *            DataTypeTemperature.class, DataTypeVolt.class)
     * @param possible_values
     *            possible values for write command, if it can only take certain values
     * @param change_command
     *            byte number for ComfoAir write command
     * @param change_data_size
     *            size of bytes list for ComfoAir write command
     * @param change_data_pos
     *            position in bytes list to change
     * @param change_affected
     *            list of affected commands (can be empty)
     *            is mandatory for read-write command
     * @param read_command
     *            request byte number for ComfoAir read command
     * @param read_reply_command
     *            reply byte list size for ComfoAir read command (list of values only)
     * @param read_reply_data_pos
     *            list of byte positions in reply bytes list from ComfoAir
     * @param read_reply_data_bits
     *            byte value on read_reply_data_pos position to be considered by command (used with
     *            DataTypeBoolean.class data_type)
     */
    ACTIVATE(ComfoAirBindingConstants.CG_CONTROL_PREFIX + ComfoAirBindingConstants.CHANNEL_ACTIVATE,
            DataTypeBoolean.getInstance(), new int[] { 0x03 }, Constants.REQUEST_SET_RS232, 1, 0,
            Constants.EMPTY_STRING_ARRAY, Constants.REPLY_SET_RS232, Constants.REPLY_SET_RS232, new int[] { 0 }, 0x03),
    MENU20_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU20_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x01),
    MENU21_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU21_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x02),
    MENU22_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU22_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x04),
    MENU23_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU23_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x08),
    MENU24_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU24_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x10),
    MENU25_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU25_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x20),
    MENU26_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU26_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x40),
    MENU27_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU27_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 6 },
            0x80),
    MENU28_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU28_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 7 },
            0x01),
    MENU29_MODE(ComfoAirBindingConstants.CG_MENUP1_PREFIX + ComfoAirBindingConstants.CHANNEL_MENU29_MODE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 7 },
            0x02),
    BATHROOM_START_DELAY(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_BR_START_DELAY,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 0, new String[] { MENU21_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 0 }),
    BATHROOM_END_DELAY(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_BR_END_DELAY,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 1, new String[] { MENU22_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 1 }),
    L1_END_DELAY(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_L1_END_DELAY,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 2, new String[] { MENU27_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 2 }),
    PULSE_VENTILATION(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_PULSE_VENTILATION,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 3, new String[] { MENU23_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 3 }),
    FILTER_WEEKS(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_FILTER_WEEKS,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 4, new String[] { MENU24_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 4 }),
    RF_SHORT_DELAY(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_SHORT_DELAY,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 5, new String[] { MENU25_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 5 }),
    RF_LONG_DELAY(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_LONG_DELAY,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 6, new String[] { MENU26_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 6 }),
    COOKERHOOD_DELAY(ComfoAirBindingConstants.CG_MENUP2_PREFIX + ComfoAirBindingConstants.CHANNEL_COOKERHOOD_DELAY,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_DELAYS, 8, 7, new String[] { MENU20_MODE.getKey() },
            Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS, new int[] { 7 }),
    CHIMNEY_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_CHIMNEY_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x01),
    BYPASS_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_BYPASS_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x02),
    EWT_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x04),
    HEATER_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_HEATER_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x08),
    V_CONTROL_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_VCONTROL_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x10),
    FROST_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_FROST_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x20),
    COOKERHOOD_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_COOKERHOOD_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x40),
    ENTHALPY_STATE(ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_ENTHALPY_STATE,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 8 },
            0x80),
    FAN_IN_PERCENT(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_IN_PERCENT,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN, new int[] { 0 }),
    FAN_OUT_PERCENT(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_OUT_PERCENT,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN, new int[] { 1 }),
    FAN_IN_RPM(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_IN_RPM,
            DataTypeRPM.getInstance(), Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN, new int[] { 2, 3 }),
    FAN_OUT_RPM(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_OUT_RPM,
            DataTypeRPM.getInstance(), Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN, new int[] { 4, 5 }),
    FAN_OUT_0(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_OUT_0,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 0,
            new String[] { FAN_OUT_PERCENT.getKey(), FAN_OUT_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 0 }),
    FAN_OUT_1(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_OUT_1,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 1,
            new String[] { FAN_OUT_PERCENT.getKey(), FAN_OUT_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 1 }),
    FAN_OUT_2(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_OUT_2,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 2,
            new String[] { FAN_OUT_PERCENT.getKey(), FAN_OUT_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 2 }),
    FAN_OUT_3(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_OUT_3,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 6,
            new String[] { FAN_OUT_PERCENT.getKey(), FAN_OUT_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 10 }),
    FAN_IN_0(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_IN_0,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 3,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_IN_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 3 }),
    FAN_IN_1(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_IN_1,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 4,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_IN_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 4 }),
    FAN_IN_2(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_IN_2,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 5,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_IN_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 5 }),
    FAN_IN_3(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_IN_3,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_FAN_LEVEL, 9, 7,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_IN_RPM.getKey() }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 11 }),
    FAN_LEVEL(ComfoAirBindingConstants.CG_VENTILATION_PREFIX + ComfoAirBindingConstants.CHANNEL_FAN_LEVEL,
            DataTypeNumber.getInstance(), new int[] { 0x01, 0x02, 0x03, 0x04 }, Constants.REQUEST_SET_LEVEL, 1, 0,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_IN_RPM.getKey(), FAN_OUT_PERCENT.getKey(),
                    FAN_OUT_RPM.getKey() },
            Constants.REQUEST_GET_FAN_LEVEL, Constants.REPLY_GET_FAN_LEVEL, new int[] { 8 }),
    LEVEL0_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_LEVEL0,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 0, 1, 2 }),
    LEVEL1_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_LEVEL1,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 3, 4, 5 }),
    LEVEL2_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_LEVEL2,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 6, 7, 8 }),
    LEVEL3_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_LEVEL3,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 17, 18, 19 }),
    FREEZE_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_FREEZE,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 9, 10 }),
    PREHEATER_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_PREHEATER,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 11, 12 }),
    BYPASS_TIME(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_BYPASS,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 13, 14 }),
    BYPASS_FACTOR(ComfoAirBindingConstants.CG_BYPASS_PREFIX + ComfoAirBindingConstants.CHANNEL_BYPASS_FACTOR,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_BYPASS, Constants.REPLY_GET_BYPASS, new int[] { 2 }),
    BYPASS_LEVEL(ComfoAirBindingConstants.CG_BYPASS_PREFIX + ComfoAirBindingConstants.CHANNEL_BYPASS_LEVEL,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_BYPASS, Constants.REPLY_GET_BYPASS, new int[] { 3 }),
    BYPASS_CORRECTION(ComfoAirBindingConstants.CG_BYPASS_PREFIX + ComfoAirBindingConstants.CHANNEL_BYPASS_CORRECTION,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_BYPASS, Constants.REPLY_GET_BYPASS, new int[] { 4 }),
    BYPASS_SUMMER(ComfoAirBindingConstants.CG_BYPASS_PREFIX + ComfoAirBindingConstants.CHANNEL_BYPASS_SUMMER,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_BYPASS, Constants.REPLY_GET_BYPASS, new int[] { 6 }),
    ENTHALPY_TEMPERATURE(ComfoAirBindingConstants.CG_ENTHALPY_PREFIX + ComfoAirBindingConstants.CHANNEL_ENTHALPY_TEMP,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_SENSORS, Constants.REPLY_GET_SENSORS,
            new int[] { 0 }),
    ENTHALPY_HUMIDITY(ComfoAirBindingConstants.CG_ENTHALPY_PREFIX + ComfoAirBindingConstants.CHANNEL_ENTHALPY_HUMIDITY,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_SENSORS, Constants.REPLY_GET_SENSORS, new int[] { 1 }),
    ENTHALPY_LEVEL(ComfoAirBindingConstants.CG_ENTHALPY_PREFIX + ComfoAirBindingConstants.CHANNEL_ENTHALPY_LEVEL,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_SENSORS, Constants.REPLY_GET_SENSORS, new int[] { 4 }),
    ENTHALPY_TIME(ComfoAirBindingConstants.CG_ENTHALPY_PREFIX + ComfoAirBindingConstants.CHANNEL_ENTHALPY_TIME,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_SENSORS, Constants.REPLY_GET_SENSORS, new int[] { 5 }),
    PREHEATER_VALVE(ComfoAirBindingConstants.CG_PREHEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_PREHEATER_VALVE,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_PREHEATER, Constants.REPLY_GET_PREHEATER,
            new int[] { 0 }),
    PREHEATER_FROST_PROTECT(
            ComfoAirBindingConstants.CG_PREHEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_PREHEATER_FROST_PROTECT,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_PREHEATER, Constants.REPLY_GET_PREHEATER,
            new int[] { 1 }),
    PREHEATER_HEATING(ComfoAirBindingConstants.CG_PREHEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_PREHEATER_HEATING,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_PREHEATER, Constants.REPLY_GET_PREHEATER,
            new int[] { 2 }),
    PREHEATER_FROST_TIME(
            ComfoAirBindingConstants.CG_PREHEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_PREHEATER_FROST_TIME,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_PREHEATER, Constants.REPLY_GET_PREHEATER,
            new int[] { 3, 4 }),
    PREHEATER_OPTION(ComfoAirBindingConstants.CG_PREHEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_PREHEATER_SAFETY,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_PREHEATER, Constants.REPLY_GET_PREHEATER,
            new int[] { 5 }),
    TARGET_TEMPERATURE(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_TARGET,
            DataTypeTemperature.getInstance(), Constants.REQUEST_SET_TEMPS, 1, 0,
            new String[] { BYPASS_FACTOR.getKey(), BYPASS_LEVEL.getKey(), BYPASS_SUMMER.getKey() },
            Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 0 }),
    OUTDOOR_TEMPERATURE_IN(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_OUTDOOR_IN,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 1 }),
    OUTDOOR_TEMPERATURE_OUT(
            ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_OUTDOOR_OUT,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 4 }),
    INDOOR_TEMPERATURE_IN(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_INDOOR_IN,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 2 }),
    INDOOR_TEMPERATURE_OUT(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_INDOOR_OUT,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 3 }),
    IS_T1_SENSOR(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_T1,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x01),
    IS_T2_SENSOR(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_T2,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x02),
    IS_T3_SENSOR(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_T3,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x04),
    IS_T4_SENSOR(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_T4,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x08),
    IS_EWT_SENSOR(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_EWT,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x10),
    IS_HEATER_SENSOR(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_HEATER,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x20),
    IS_COOKERHOOD_SENSOR(
            ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_SENSOR_COOKERHOOD,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 5 },
            0x40),
    EWT_TEMPERATURE(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_EWT,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 6 }),
    HEATER_TEMPERATURE(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_HEATER,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 7 }),
    COOKERHOOD_TEMPERATURE(ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_COOKERHOOD,
            DataTypeTemperature.getInstance(), Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 8 }),
    EWT_SPEED(ComfoAirBindingConstants.CG_EWT_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_SPEED,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_EWT, 5, 2,
            new String[] { ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_STATE,
                    ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_EWT },
            Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 2 }),
    EWT_TEMPERATURE_LOW(ComfoAirBindingConstants.CG_EWT_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_TEMP_LOW,
            DataTypeTemperature.getInstance(), Constants.REQUEST_SET_EWT, 5, 0,
            new String[] { ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_STATE },
            Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 0 }),
    EWT_TEMPERATURE_HIGH(ComfoAirBindingConstants.CG_EWT_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_TEMP_HIGH,
            DataTypeTemperature.getInstance(), Constants.REQUEST_SET_EWT, 5, 1,
            new String[] { ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_STATE },
            Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 1 }),
    COOKERHOOD_SPEED(ComfoAirBindingConstants.CG_COOKERHOOD_PREFIX + ComfoAirBindingConstants.CHANNEL_COOKERHOOD_SPEED,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_EWT, 5, 3,
            new String[] {
                    ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_COOKERHOOD_STATE,
                    ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_COOKERHOOD },
            Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 3 }),
    HEATER_POWER(ComfoAirBindingConstants.CG_HEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_HEATER_POWER,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 4 }),
    HEATER_POWER_I(ComfoAirBindingConstants.CG_HEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_HEATER_POWER_I,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 5 }),
    HEATER_TARGET_TEMPERATURE(
            ComfoAirBindingConstants.CG_HEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_HEATER_TEMP_TARGET,
            DataTypeTemperature.getInstance(), Constants.REQUEST_SET_EWT, 5, 4,
            new String[] { ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_HEATER_STATE,
                    ComfoAirBindingConstants.CG_HEATER_PREFIX + ComfoAirBindingConstants.CHANNEL_HEATER_POWER,
                    ComfoAirBindingConstants.CG_TEMPS_PREFIX + ComfoAirBindingConstants.CHANNEL_TEMP_HEATER },
            Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 6 }),
    IS_PREHEATER(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_PREHEATER,
            DataTypeBoolean.getInstance(), Constants.REQUEST_SET_STATES, 8, 0,
            new String[] { OUTDOOR_TEMPERATURE_IN.getKey(), INDOOR_TEMPERATURE_IN.getKey(),
                    PREHEATER_FROST_PROTECT.getKey(), PREHEATER_FROST_TIME.getKey(), PREHEATER_HEATING.getKey(),
                    FROST_STATE.getKey(), PREHEATER_OPTION.getKey(), PREHEATER_TIME.getKey(),
                    PREHEATER_VALVE.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 0 }),
    IS_BYPASS(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_BYPASS,
            DataTypeBoolean.getInstance(), Constants.REQUEST_SET_STATES, 8, 1,
            new String[] { INDOOR_TEMPERATURE_IN.getKey(), OUTDOOR_TEMPERATURE_OUT.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 1 }),
    RECU_TYPE(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_RECU_TYPE,
            DataTypeNumber.getInstance(), new int[] { 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 2,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_OUT_PERCENT.getKey(), INDOOR_TEMPERATURE_IN.getKey(),
                    INDOOR_TEMPERATURE_OUT.getKey(), OUTDOOR_TEMPERATURE_IN.getKey(),
                    OUTDOOR_TEMPERATURE_OUT.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 2 }),
    RECU_SIZE(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_RECU_SIZE,
            DataTypeNumber.getInstance(), new int[] { 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 3,
            new String[] { FAN_IN_PERCENT.getKey(), FAN_OUT_PERCENT.getKey(), FAN_IN_0.getKey(), FAN_IN_1.getKey(),
                    FAN_IN_2.getKey(), FAN_IN_3.getKey(), FAN_OUT_0.getKey(), FAN_OUT_1.getKey(), FAN_OUT_2.getKey(),
                    FAN_OUT_3.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 3 }),
    IS_CHIMNEY(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_CHIMNEY,
            DataTypeBoolean.getInstance(), new int[] { 0x01 }, Constants.REQUEST_SET_STATES, 8, 4,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 4 },
            0x01),
    IS_COOKERHOOD(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_COOKERHOOD,
            DataTypeBoolean.getInstance(), new int[] { 0x02 }, Constants.REQUEST_SET_STATES, 8, 4,
            new String[] { COOKERHOOD_DELAY.getKey(), COOKERHOOD_STATE.getKey(), COOKERHOOD_SPEED.getKey(),
                    COOKERHOOD_TEMPERATURE.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 4 }, 0x02),
    IS_HEATER(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_HEATER,
            DataTypeBoolean.getInstance(), new int[] { 0x04 }, Constants.REQUEST_SET_STATES, 8, 4,
            new String[] { HEATER_TARGET_TEMPERATURE.getKey(), HEATER_POWER.getKey(), HEATER_STATE.getKey(),
                    HEATER_POWER_I.getKey(), HEATER_TEMPERATURE.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 4 }, 0x04),
    IS_ENTHALPY(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_ENTHALPY,
            DataTypeNumber.getInstance(), new int[] { 0x00, 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 6,
            new String[] { ENTHALPY_TEMPERATURE.getKey(), ENTHALPY_HUMIDITY.getKey(), ENTHALPY_LEVEL.getKey(),
                    ENTHALPY_STATE.getKey(), ENTHALPY_TIME.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 9 }),
    IS_EWT(ComfoAirBindingConstants.CG_OPTIONS_PREFIX + ComfoAirBindingConstants.CHANNEL_OPTION_EWT,
            DataTypeNumber.getInstance(), new int[] { 0x00, 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 7,
            new String[] { EWT_SPEED.getKey(), EWT_TEMPERATURE_LOW.getKey(), EWT_TEMPERATURE_HIGH.getKey(),
                    EWT_STATE.getKey(), EWT_TEMPERATURE.getKey() },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 10 }),
    SOFTWARE_MAIN_VERSION("softwareMainVersion", DataTypeNumber.getInstance(), Constants.REQUEST_GET_FIRMWARE,
            Constants.REPLY_GET_FIRMWARE, new int[] { 0 }),
    SOFTWARE_MINOR_VERSION("softwareMinorVersion", DataTypeNumber.getInstance(), Constants.REQUEST_GET_FIRMWARE,
            Constants.REPLY_GET_FIRMWARE, new int[] { 1 }),
    DEVICE_NAME("deviceName", DataTypeNumber.getInstance(), Constants.REQUEST_GET_FIRMWARE,
            Constants.REPLY_GET_FIRMWARE, new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }),
    ERROR_MESSAGE(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_MESSAGE,
            DataTypeMessage.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS,
            new int[] { 0, 1, 9, 13 }),
    ERRORA_CURRENT(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_A_CURRENT,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 0 }),
    ERRORA_LAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_A_LAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 2 }),
    ERRORA_PRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_A_PRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 4 }),
    ERRORA_PREPRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_A_PREPRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 6 }),
    ERRORAHIGH_CURRENT(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_HIGH_CURRENT,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 13 }),
    ERRORAHIGH_LAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_HIGH_LAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 14 }),
    ERRORAHIGH_PRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_HIGH_PRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 15 }),
    ERRORAHIGH_PREPRELAST(
            ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_HIGH_PREPRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 16 }),
    ERRORE_CURRENT(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_E_CURRENT,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 1 }),
    ERRORE_LAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_E_LAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 3 }),
    ERRORE_PRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_E_PRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 5 }),
    ERRORE_PREPRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_E_PREPRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 7 }),
    ERROREA_CURRENT(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_EA_CURRENT,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 9 }),
    ERROREA_LAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_EA_LAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 10 }),
    ERROREA_PRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_EA_PRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 11 }),
    ERROREA_PREPRELAST(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_EA_PREPRELAST,
            DataTypeNumber.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 12 }),
    ERROR_RESET(ComfoAirBindingConstants.CG_RESETS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_RESET,
            DataTypeBoolean.getInstance(), new int[] { 0x01 }, Constants.REQUEST_SET_RESETS, 4, 0,
            new String[] {
                    ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_ERROR_MESSAGE }),
    FILTER_HOURS(ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_FILTER,
            DataTypeTime.getInstance(), Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS, new int[] { 15, 16 }),
    FILTER_RESET(ComfoAirBindingConstants.CG_RESETS_PREFIX + ComfoAirBindingConstants.CHANNEL_FILTER_RESET,
            DataTypeBoolean.getInstance(), new int[] { 0x01 }, Constants.REQUEST_SET_RESETS, 4, 3,
            new String[] { ComfoAirBindingConstants.CG_TIMES_PREFIX + ComfoAirBindingConstants.CHANNEL_TIME_FILTER,
                    ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_FILTER_ERROR }),
    FILTER_ERROR(ComfoAirBindingConstants.CG_ERRORS_PREFIX + ComfoAirBindingConstants.CHANNEL_FILTER_ERROR,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS, new int[] { 8 },
            0x01),
    ANALOG1_NEGATIVE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG1_NEGATIVE,
            DataTypeBoolean.getInstance(), new int[] { 0x01 }, Constants.REQUEST_SET_ANALOGS, 19, 2,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 2 },
            0x01),
    ANALOG2_NEGATIVE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG2_NEGATIVE,
            DataTypeBoolean.getInstance(), new int[] { 0x02 }, Constants.REQUEST_SET_ANALOGS, 19, 2,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 2 },
            0x02),
    ANALOG3_NEGATIVE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG3_NEGATIVE,
            DataTypeBoolean.getInstance(), new int[] { 0x04 }, Constants.REQUEST_SET_ANALOGS, 19, 2,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 2 },
            0x04),
    ANALOG4_NEGATIVE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG4_NEGATIVE,
            DataTypeBoolean.getInstance(), new int[] { 0x08 }, Constants.REQUEST_SET_ANALOGS, 19, 2,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 2 },
            0x08),
    RF_NEGATIVE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_NEGATIVE,
            DataTypeBoolean.getInstance(), new int[] { 0x10 }, Constants.REQUEST_SET_ANALOGS, 19, 2,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 2 },
            0x10),
    ANALOG1_MIN(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG1_MIN,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 3, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 3 }),
    ANALOG1_MAX(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG1_MAX,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 4, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 4 }),
    ANALOG1_VALUE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG1_VALUE,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 5, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 5 }),
    ANALOG2_MIN(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG2_MIN,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 6, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 6 }),
    ANALOG2_MAX(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG2_MAX,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 7, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 7 }),
    ANALOG2_VALUE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG2_VALUE,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 8, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 8 }),
    ANALOG3_MIN(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG3_MIN,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 9, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 9 }),
    ANALOG3_MAX(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG3_MAX,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 10, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 10 }),
    ANALOG3_VALUE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG3_VALUE,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 11, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 11 }),
    ANALOG4_MIN(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG4_MIN,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 12, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 12 }),
    ANALOG4_MAX(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG4_MAX,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 13, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 13 }),
    ANALOG4_VALUE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG4_VALUE,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 14, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 14 }),
    RF_MIN(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_MIN,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 15, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 15 }),
    RF_MAX(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_MAX,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 16, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 16 }),
    RF_VALUE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_VALUE,
            DataTypeNumber.getInstance(), Constants.REQUEST_SET_ANALOGS, 19, 17, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 17 }),
    ANALOG_MODE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG_MODE,
            DataTypeNumber.getInstance(), new int[] { 0x00, 0x01 }, Constants.REQUEST_SET_ANALOGS, 19, 18,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 18 }),
    ANALOG1_VOLT(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG1_VOLT,
            DataTypeVolt.getInstance(), Constants.REQUEST_GET_ANALOG_VOLTS, Constants.REPLY_GET_ANALOG_VOLTS,
            new int[] { 0 }),
    ANALOG2_VOLT(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG2_VOLT,
            DataTypeVolt.getInstance(), Constants.REQUEST_GET_ANALOG_VOLTS, Constants.REPLY_GET_ANALOG_VOLTS,
            new int[] { 1 }),
    ANALOG3_VOLT(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG3_VOLT,
            DataTypeVolt.getInstance(), Constants.REQUEST_GET_ANALOG_VOLTS, Constants.REPLY_GET_ANALOG_VOLTS,
            new int[] { 2 }),
    ANALOG4_VOLT(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG4_VOLT,
            DataTypeVolt.getInstance(), Constants.REQUEST_GET_ANALOG_VOLTS, Constants.REPLY_GET_ANALOG_VOLTS,
            new int[] { 3 }),
    ANALOG1_MODE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG1_MODE,
            DataTypeBoolean.getInstance(), new int[] { 0x01 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { ANALOG1_NEGATIVE.getKey(), ANALOG1_MIN.getKey(), ANALOG1_MAX.getKey(),
                    ANALOG1_VALUE.getKey(), ANALOG1_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x01),
    ANALOG2_MODE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG2_MODE,
            DataTypeBoolean.getInstance(), new int[] { 0x02 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { ANALOG2_NEGATIVE.getKey(), ANALOG2_MIN.getKey(), ANALOG2_MAX.getKey(),
                    ANALOG2_VALUE.getKey(), ANALOG2_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x02),
    ANALOG3_MODE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG3_MODE,
            DataTypeBoolean.getInstance(), new int[] { 0x04 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { ANALOG3_NEGATIVE.getKey(), ANALOG3_MIN.getKey(), ANALOG3_MAX.getKey(),
                    ANALOG3_VALUE.getKey(), ANALOG3_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x04),
    ANALOG4_MODE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_ANALOG4_MODE,
            DataTypeBoolean.getInstance(), new int[] { 0x08 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { ANALOG4_NEGATIVE.getKey(), ANALOG4_MIN.getKey(), ANALOG4_MAX.getKey(),
                    ANALOG4_VALUE.getKey(), ANALOG4_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x08),
    RF_MODE(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_RF_MODE,
            DataTypeBoolean.getInstance(), new int[] { 0x10 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { RF_NEGATIVE.getKey(), RF_MIN.getKey(), RF_MAX.getKey(), RF_VALUE.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x10),
    IS_L1_SWITCH(ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_L1_SWITCH,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 0 },
            0x01),
    IS_L2_SWITCH(ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_L2_SWITCH,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 0 },
            0x02),
    IS_BATHROOM_SWITCH(ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_BATHROOM_SWITCH,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 1 },
            0x01),
    IS_COOKERHOOD_SWITCH(
            ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_COOKERHOOD_SWITCH,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 1 },
            0x02),
    IS_EXTERNAL_FILTER(ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_EXTERNAL_FILTER,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 1 },
            0x04),
    IS_WTW(ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_WTW,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 1 },
            0x08),
    IS_BATHROOM2_SWITCH(
            ComfoAirBindingConstants.CG_INPUTS_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_BATHROOM2_SWITCH,
            DataTypeBoolean.getInstance(), Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS, new int[] { 1 },
            0x10),
    IS_ANALOG1(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_ANALOG1,
            DataTypeBoolean.getInstance(), new int[] { 0x01 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { ANALOG1_MODE.getKey(), ANALOG1_NEGATIVE.getKey(), ANALOG1_MIN.getKey(), ANALOG1_MAX.getKey(),
                    ANALOG1_VALUE.getKey(), ANALOG1_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x01),
    IS_ANALOG2(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_ANALOG2,
            DataTypeBoolean.getInstance(), new int[] { 0x02 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { ANALOG2_MODE.getKey(), ANALOG2_NEGATIVE.getKey(), ANALOG2_MIN.getKey(), ANALOG2_MAX.getKey(),
                    ANALOG2_VALUE.getKey(), ANALOG2_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x02),
    IS_ANALOG3(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_ANALOG3,
            DataTypeBoolean.getInstance(), new int[] { 0x04 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { ANALOG3_MODE.getKey(), ANALOG3_NEGATIVE.getKey(), ANALOG3_MIN.getKey(), ANALOG3_MAX.getKey(),
                    ANALOG3_VALUE.getKey(), ANALOG3_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x04),
    IS_ANALOG4(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_ANALOG4,
            DataTypeBoolean.getInstance(), new int[] { 0x08 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { ANALOG4_MODE.getKey(), ANALOG4_NEGATIVE.getKey(), ANALOG4_MIN.getKey(), ANALOG4_MAX.getKey(),
                    ANALOG4_VALUE.getKey(), ANALOG4_VOLT.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x08),
    IS_RF(ComfoAirBindingConstants.CG_ANALOG_PREFIX + ComfoAirBindingConstants.CHANNEL_IS_RF,
            DataTypeBoolean.getInstance(), new int[] { 0x10 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { RF_MODE.getKey(), RF_NEGATIVE.getKey(), RF_MIN.getKey(), RF_MAX.getKey(),
                    RF_VALUE.getKey() },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x10);

    private final String key;
    private final ComfoAirDataType data_type;

    /*
     * Possible values
     */
    private int @Nullable [] possible_values;

    /*
     * Cmd code to change properties on the comfoair.
     */
    private int change_command;
    /*
     * The size of the data block.
     */
    private int change_data_size;
    /*
     * The byte inside the data block which holds the crucial value.
     */
    private int change_data_pos;
    /*
     * Affected commands which should be refreshed after a successful change
     * command call.
     */
    private String @Nullable [] change_affected;

    /*
     * Command for reading properties.
     */
    private int read_command;

    /*
     * ACK Command which identifies the matching response.
     */
    private int read_reply_command;

    /*
     * The byte position inside the response data.
     */
    private int @Nullable [] read_reply_data_pos;

    /*
     * Bit mask for boolean response properties to identify a true value.
     */
    private int read_reply_data_bits;

    /*
     * Constructor for full read/write command
     */
    private ComfoAirCommandType(String key, ComfoAirDataType data_type, int[] possible_values, int change_command,
            int change_data_size, int change_data_pos, String[] change_affected, int read_command,
            int read_reply_command, int[] read_reply_data_pos, int read_reply_data_bits) {
        this.key = key;
        this.data_type = data_type;
        this.possible_values = possible_values;
        this.change_command = change_command;
        this.change_data_size = change_data_size;
        this.change_data_pos = change_data_pos;
        this.change_affected = change_affected;
        this.read_command = read_command;
        this.read_reply_command = read_reply_command;
        this.read_reply_data_pos = read_reply_data_pos;
        this.read_reply_data_bits = read_reply_data_bits;
    }

    /*
     * Constructor for read/write command w/o predefined read_reply_data_bits
     */
    private ComfoAirCommandType(String key, ComfoAirDataType data_type, int[] possible_values, int change_command,
            int change_data_size, int change_data_pos, String[] change_affected, int read_command,
            int read_reply_command, int[] read_reply_data_pos) {
        this.key = key;
        this.data_type = data_type;
        this.possible_values = possible_values;
        this.change_command = change_command;
        this.change_data_size = change_data_size;
        this.change_data_pos = change_data_pos;
        this.change_affected = change_affected;
        this.read_command = read_command;
        this.read_reply_command = read_reply_command;
        this.read_reply_data_pos = read_reply_data_pos;
    }

    /*
     * Constructor for read/write command w/o predefined read_reply_data_bits & possible_values
     */
    private ComfoAirCommandType(String key, ComfoAirDataType data_type, int change_command, int change_data_size,
            int change_data_pos, String[] change_affected, int read_command, int read_reply_command,
            int[] read_reply_data_pos) {
        this.key = key;
        this.data_type = data_type;
        this.change_command = change_command;
        this.change_data_size = change_data_size;
        this.change_data_pos = change_data_pos;
        this.change_affected = change_affected;
        this.read_command = read_command;
        this.read_reply_command = read_reply_command;
        this.read_reply_data_pos = read_reply_data_pos;
    }

    /*
     * Constructor for write-only command (reset)
     */
    private ComfoAirCommandType(String key, ComfoAirDataType data_type, int[] possible_values, int change_command,
            int change_data_size, int change_data_pos, String[] change_affected) {
        this.key = key;
        this.data_type = data_type;
        this.possible_values = possible_values;
        this.read_command = 0;
        this.change_command = change_command;
        this.change_data_size = change_data_size;
        this.change_data_pos = change_data_pos;
        this.change_affected = change_affected;
    }

    /*
     * Constructor for read-only command
     */
    private ComfoAirCommandType(String key, ComfoAirDataType data_type, int read_command, int read_reply_command,
            int[] read_reply_data_pos, int read_reply_data_bits) {
        this.key = key;
        this.data_type = data_type;
        this.read_command = read_command;
        this.read_reply_command = read_reply_command;
        this.read_reply_data_pos = read_reply_data_pos;
        this.read_reply_data_bits = read_reply_data_bits;
    }

    /*
     * Constructor for read-only command w/o read_reply_data_bits
     */
    private ComfoAirCommandType(String key, ComfoAirDataType data_type, int read_command, int read_reply_command,
            int[] read_reply_data_pos) {
        this.key = key;
        this.data_type = data_type;
        this.read_command = read_command;
        this.read_reply_command = read_reply_command;
        this.read_reply_data_pos = read_reply_data_pos;
    }

    public static class Constants {
        public static final int REQUEST_GET_INPUTS = 0x03;
        public static final int REPLY_GET_INPUTS = 0x04;
        public static final int REQUEST_GET_FAN = 0x0b;
        public static final int REPLY_GET_FAN = 0x0c;
        public static final int REQUEST_GET_ANALOG_VOLTS = 0x13;
        public static final int REPLY_GET_ANALOG_VOLTS = 0x14;
        public static final int REQUEST_GET_FIRMWARE = 0x69;
        public static final int REPLY_GET_FIRMWARE = 0x6a;
        public static final int REQUEST_GET_SENSORS = 0x97;
        public static final int REPLY_GET_SENSORS = 0x98;
        public static final int REQUEST_SET_LEVEL = 0x99;
        public static final int REQUEST_SET_RS232 = 0x9b;
        public static final int REPLY_SET_RS232 = 0x9c;
        public static final int REQUEST_GET_ANALOGS = 0x9d;
        public static final int REPLY_GET_ANALOGS = 0x9e;
        public static final int REQUEST_SET_ANALOGS = 0x9f;
        public static final int REQUEST_GET_DELAYS = 0xc9;
        public static final int REPLY_GET_DELAYS = 0xca;
        public static final int REQUEST_SET_DELAYS = 0xcb;
        public static final int REQUEST_GET_FAN_LEVEL = 0xcd;
        public static final int REPLY_GET_FAN_LEVEL = 0xce;
        public static final int REQUEST_SET_FAN_LEVEL = 0xcf;
        public static final int REQUEST_GET_TEMPS = 0xd1;
        public static final int REPLY_GET_TEMPS = 0xd2;
        public static final int REQUEST_SET_TEMPS = 0xd3;
        public static final int REQUEST_GET_STATES = 0xd5;
        public static final int REPLY_GET_STATES = 0xd6;
        public static final int REQUEST_SET_STATES = 0xd7;
        public static final int REQUEST_GET_ERRORS = 0xd9;
        public static final int REPLY_GET_ERRORS = 0xda;
        public static final int REQUEST_SET_RESETS = 0xdb;
        public static final int REQUEST_GET_HOURS = 0xdd;
        public static final int REPLY_GET_HOURS = 0xde;
        public static final int REQUEST_GET_BYPASS = 0xdf;
        public static final int REPLY_GET_BYPASS = 0xe0;
        public static final int REQUEST_GET_PREHEATER = 0xe1;
        public static final int REPLY_GET_PREHEATER = 0xe2;
        public static final int REQUEST_GET_RF = 0xe5;
        public static final int REPLY_GET_RF = 0xe6;
        public static final int REQUEST_GET_EWT = 0xeb;
        public static final int REPLY_GET_EWT = 0xec;
        public static final int REQUEST_SET_EWT = 0xed;

        public static final String[] EMPTY_STRING_ARRAY = new String[0];
        public static final int[] EMPTY_INT_ARRAY = new int[0];
    }

    /**
     * @return command key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return data type for this command key
     */
    public ComfoAirDataType getDataType() {
        return data_type;
    }

    /**
     * @return possible byte values
     */
    public int @Nullable [] getPossibleValues() {
        return possible_values;
    }

    /**
     * @return relevant byte position inside the response byte value array
     */
    public int getChangeDataPos() {
        return change_data_pos;
    }

    /**
     * @return generate a byte value sequence for the response stream
     */
    public int[] getChangeDataTemplate() {
        int[] template = new int[change_data_size];
        for (int i = 0; i < template.length; i++) {
            template[i] = 0x00;
        }
        return template;
    }

    /**
     * @return byte position inside the request byte value array
     */
    public int @Nullable [] getGetReplyDataPos() {
        return read_reply_data_pos;
    }

    /**
     * @return bit mask for the response byte value
     */
    public int getGetReplyDataBits() {
        return read_reply_data_bits;
    }

    /**
     * Get single read command to update item.
     *
     * @param key
     *
     * @return ComfoAirCommand identified by key
     */
    public static @Nullable ComfoAirCommand getReadCommand(String key) {
        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);

        if (commandType != null) {
            if (commandType.read_command == 0) {
                return null;
            }
            int getCmd = commandType.read_command;
            int replyCmd = commandType.read_reply_command;

            return new ComfoAirCommand(key, getCmd, replyCmd, Constants.EMPTY_INT_ARRAY, null, null);
        }
        return null;
    }

    /**
     * Get a command to change properties on the comfoair.
     *
     * @param key
     *            command key
     * @param value
     *            new state
     * @return initialized ComfoAirCommand
     */
    public static @Nullable ComfoAirCommand getChangeCommand(String key, Command command) {
        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);
        State value = UnDefType.NULL;

        if (commandType != null) {
            ComfoAirDataType dataType = commandType.getDataType();
            if (dataType instanceof DataTypeBoolean) {
                value = (OnOffType) command;
            } else if (dataType instanceof DataTypeNumber || dataType instanceof DataTypeRPM) {
                value = (DecimalType) command;
            } else if (dataType instanceof DataTypeTemperature) {
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> celsius = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);
                    if (celsius != null) {
                        value = celsius;
                    }
                } else {
                    value = new QuantityType<>(((DecimalType) command).doubleValue(), SIUnits.CELSIUS);
                }
            } else if (dataType instanceof DataTypeVolt) {
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> volts = ((QuantityType<?>) command).toUnit(SmartHomeUnits.VOLT);
                    if (volts != null) {
                        value = volts;
                    }
                } else {
                    value = new QuantityType<>(((DecimalType) command).doubleValue(), SmartHomeUnits.VOLT);
                }
            } else if (dataType instanceof DataTypeTime) {
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> hours = ((QuantityType<?>) command).toUnit(SmartHomeUnits.HOUR);
                    if (hours != null) {
                        value = hours;
                    }
                } else {
                    value = new QuantityType<>(((DecimalType) command).doubleValue(), SmartHomeUnits.HOUR);
                }
            }
            if (value instanceof UnDefType) {
                return null;
            } else {
                int[] data = dataType.convertFromState(value, commandType);
                DecimalType decimalValue = value.as(DecimalType.class);
                if (decimalValue != null) {
                    int intValue = decimalValue.intValue();

                    if (data != null) {
                        int dataPosition = commandType.getChangeDataPos();
                        return new ComfoAirCommand(key, commandType.change_command, null, data, dataPosition, intValue);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get all commands which should be refreshed after a successful change
     * command.
     *
     * @param key
     *            command key
     * @param usedKeys
     * @return ComfoAirCommand's which should be updated after a modifying
     *         ComfoAirCommand named by key
     */
    public static Collection<ComfoAirCommand> getAffectedReadCommands(String key, Set<String> usedKeys) {

        Map<Integer, ComfoAirCommand> commands = new HashMap<>();

        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);
        if (commandType != null) {
            if (commandType.read_reply_command != 0) {
                int getCmd = commandType.read_command == 0 ? null : commandType.read_command;
                int replyCmd = commandType.read_reply_command;

                ComfoAirCommand command = new ComfoAirCommand(key, getCmd, replyCmd, Constants.EMPTY_INT_ARRAY, null,
                        null);
                commands.put(replyCmd, command);
            }

            if (commandType.change_affected != null) {
                for (String affectedKey : commandType.change_affected) {
                    // refresh affected event keys only when they are used
                    if (!usedKeys.contains(affectedKey)) {
                        continue;
                    }

                    ComfoAirCommandType affectedCommandType = ComfoAirCommandType.getCommandTypeByKey(affectedKey);

                    if (affectedCommandType != null) {
                        if (affectedCommandType.read_reply_command == 0) {
                            continue;
                        }
                        commands = modifiedCommandCollection(commands, affectedCommandType);
                    }
                }
            }
        }
        return commands.values();
    }

    /**
     * Get all commands which receive informations to update items.
     *
     * @return all ComfoAirCommand's identified by keys
     */
    public static Collection<ComfoAirCommand> getReadCommandsByEventTypes(List<String> keys) {

        Map<Integer, ComfoAirCommand> commands = new HashMap<>();
        for (ComfoAirCommandType entry : values()) {
            if (!keys.contains(entry.key)) {
                continue;
            }
            if (entry.read_reply_command == 0) {
                continue;
            }
            commands = modifiedCommandCollection(commands, entry);
        }
        return commands.values();
    }

    /**
     * Get commandtypes which matches the replyCmd.
     *
     * @param replyCmd
     *            reply command byte value
     * @return ComfoAirCommandType identified by replyCmd
     */
    public static List<ComfoAirCommandType> getCommandTypesByReplyCmd(int replyCmd) {
        List<ComfoAirCommandType> commands = new ArrayList<>();
        for (ComfoAirCommandType entry : values()) {
            if (entry.read_reply_command != replyCmd) {
                continue;
            }
            commands.add(entry);
        }
        return commands;
    }

    /**
     * Get a specific command.
     *
     * @param key
     *            command key
     * @return ComfoAirCommandType identified by key
     */
    public static @Nullable ComfoAirCommandType getCommandTypeByKey(String key) {
        for (ComfoAirCommandType entry : values()) {
            if (entry.key.equals(key)) {
                return entry;
            }
        }
        return null;
    }

    @SuppressWarnings("null")
    private static Map<Integer, ComfoAirCommand> modifiedCommandCollection(Map<Integer, ComfoAirCommand> commands,
            ComfoAirCommandType commandType) {
        int getCmd = commandType.read_command == 0 ? null : commandType.read_command;
        int replyCmd = commandType.read_reply_command;

        ComfoAirCommand command = commands.get(replyCmd);

        if (command == null) {
            command = new ComfoAirCommand(commandType.key, getCmd, replyCmd, Constants.EMPTY_INT_ARRAY, null, null);
            commands.put(replyCmd, command);
        } else {
            command.addKey(commandType.key);
        }
        return commands;
    }
}
