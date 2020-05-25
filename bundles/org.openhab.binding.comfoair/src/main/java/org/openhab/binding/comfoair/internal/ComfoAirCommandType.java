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
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.comfoair.internal.datatypes.ComfoAirDataType;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeBoolean;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeMessage;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeNumber;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeRPM;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeTemperature;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeVolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    ACTIVATE("bindingControl#activate", DataTypeBoolean.class, new int[] { 0x03 }, Constants.REQUEST_SET_RS232, 1, 0,
            Constants.EMPTY_STRING_ARRAY, Constants.REPLY_SET_RS232, Constants.REPLY_SET_RS232, new int[] { 0 }, 0x03),
    MENU20_MODE("menuP1#menu20Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x01),
    MENU21_MODE("menuP1#menu21Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x02),
    MENU22_MODE("menuP1#menu22Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x04),
    MENU23_MODE("menuP1#menu23Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x08),
    MENU24_MODE("menuP1#menu24Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x10),
    MENU25_MODE("menuP1#menu25Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x20),
    MENU26_MODE("menuP1#menu26Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x40),
    MENU27_MODE("menuP1#menu27Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 6 }, 0x80),
    MENU28_MODE("menuP1#menu28Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 7 }, 0x01),
    MENU29_MODE("menuP1#menu29Mode", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 7 }, 0x02),
    BATHROOM_START_DELAY("menuP2#bathroomStartDelay", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 0,
            new String[] { "menuP1#menu21Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 0 }),
    BATHROOM_END_DELAY("menuP2#bathroomEndDelay", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 1,
            new String[] { "menuP1#menu22Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 1 }),
    L1_END_DELAY("menuP2#L1EndDelay", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 2,
            new String[] { "menuP1#menu27Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 2 }),
    PULSE_VENTILATION("menuP2#pulseVentilation", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 3,
            new String[] { "menuP1#menu23Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 3 }),
    FILTER_WEEKS("menuP2#filterWeeks", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 4,
            new String[] { "menuP1#menu24Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 4 }),
    RF_SHORT_DELAY("menuP2#RFShortDelay", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 5,
            new String[] { "menuP1#menu25Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 5 }),
    RF_LONG_DELAY("menuP2#RFLongDelay", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 6,
            new String[] { "menuP1#menu26Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 6 }),
    COOKERHOOD_DELAY("menuP2#cookerhoodDelay", DataTypeNumber.class, Constants.REQUEST_SET_DELAYS, 8, 7,
            new String[] { "menuP1#menu20Mode" }, Constants.REQUEST_GET_DELAYS, Constants.REPLY_GET_DELAYS,
            new int[] { 7 }),
    CHIMNEY_STATE("menuP9#chimneyState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES,
            Constants.REPLY_GET_STATES, new int[] { 8 }, 0x01),
    BYPASS_STATE("menuP9#bypassState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 8 }, 0x02),
    EWT_STATE("menuP9#ewtState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 8 }, 0x04),
    HEATER_STATE("menuP9#heaterState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 8 }, 0x08),
    V_CONTROL_STATE("menuP9#vControlState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES,
            Constants.REPLY_GET_STATES, new int[] { 8 }, 0x10),
    FROST_STATE("menuP9#frostState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES,
            new int[] { 8 }, 0x20),
    COOKERHOOD_STATE("menuP9#cookerhoodState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES,
            Constants.REPLY_GET_STATES, new int[] { 8 }, 0x40),
    ENTHALPY_STATE("menuP9#enthalpyState", DataTypeBoolean.class, Constants.REQUEST_GET_STATES,
            Constants.REPLY_GET_STATES, new int[] { 8 }, 0x80),
    FAN_OUT_0("ventilation#fanOut0", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 0,
            new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 0 }),
    FAN_OUT_1("ventilation#fanOut1", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 1,
            new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 1 }),
    FAN_OUT_2("ventilation#fanOut2", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 2,
            new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 2 }),
    FAN_OUT_3("ventilation#fanOut3", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 6,
            new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 10 }),
    FAN_IN_0("ventilation#fanIn0", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 3,
            new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 3 }),
    FAN_IN_1("ventilation#fanIn1", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 4,
            new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 4 }),
    FAN_IN_2("ventilation#fanIn2", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 5,
            new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 5 }),
    FAN_IN_3("ventilation#fanIn3", DataTypeNumber.class, Constants.REQUEST_SET_FAN_LEVEL, 9, 7,
            new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" }, Constants.REQUEST_GET_FAN_LEVEL,
            Constants.REPLY_GET_FAN_LEVEL, new int[] { 11 }),
    FAN_IN_PERCENT("ventilation#fanInPercent", DataTypeNumber.class, Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN,
            new int[] { 0 }),
    FAN_OÚT_PERCENT("ventilation#fanOutPercent", DataTypeNumber.class, Constants.REQUEST_GET_FAN,
            Constants.REPLY_GET_FAN, new int[] { 1 }),
    FAN_IN_RPM("ventilation#fanInRPM", DataTypeRPM.class, Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN,
            new int[] { 2, 3 }),
    FAN_OUT_RPM("ventilation#fanOutRPM", DataTypeRPM.class, Constants.REQUEST_GET_FAN, Constants.REPLY_GET_FAN,
            new int[] { 4, 5 }),
    FAN_LEVEL("ccease#fanLevel", DataTypeNumber.class, new int[] { 0x01, 0x02, 0x03, 0x04 },
            Constants.REQUEST_SET_LEVEL, 1, 0,
            new String[] { "ventilation#fanInPercent", "ventilation#fanOutPercent", "ventilation#fanInRPM",
                    "ventilation#fanOutRPM" },
            Constants.REQUEST_GET_FAN_LEVEL, Constants.REPLY_GET_FAN_LEVEL, new int[] { 8 }),
    TARGET_TEMPERATUR("ccease#targetTemperature", DataTypeTemperature.class, Constants.REQUEST_SET_TEMPS, 1, 0,
            new String[] { "bypass#bypassFactor", "bypass#bypassLevel", "bypass#bypassSummer" },
            Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 0 }),
    OUTDOOR_TEMPERATURE_IN("temperatures#outdoorTemperatureIn", DataTypeTemperature.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 1 }),
    OUTDOOR_TEMPERATURE_OUT("temperatures#outdoorTemperatureOut", DataTypeTemperature.class,
            Constants.REQUEST_GET_TEMPS, Constants.REPLY_GET_TEMPS, new int[] { 4 }),
    INDOOR_TEMPERATURE_IN("temperatures#indoorTemperatureIn", DataTypeTemperature.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 2 }),
    INDOOR_TEMPERATURE_OUT("temperatures#indoorTemperatureOut", DataTypeTemperature.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 3 }),
    IS_T1_SENSOR("temperatures#isT1Sensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x01),
    IS_T2_SENSOR("temperatures#isT2Sensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x02),
    IS_T3_SENSOR("temperatures#isT3Sensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x04),
    IS_T4_SENSOR("temperatures#isT4Sensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x08),
    IS_EWT_SENSOR("temperatures#isEWTSensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x10),
    IS_HEATER_SENSOR("temperatures#isHeaterSensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x20),
    IS_COOKERHOOD_SENSOR("temperatures#isCookerhoodSensor", DataTypeBoolean.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 5 }, 0x40),
    EWT_TEMPERATUR("temperatures#ewtTemperature", DataTypeTemperature.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 6 }),
    HEATER_TEMPERATUR("temperatures#heaterTemperature", DataTypeTemperature.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 7 }),
    COOKERHOOD_TEMPERATUR("temperatures#cookerhoodTemperature", DataTypeTemperature.class, Constants.REQUEST_GET_TEMPS,
            Constants.REPLY_GET_TEMPS, new int[] { 8 }),
    IS_PREHEATER("options#isPreheater", DataTypeBoolean.class, Constants.REQUEST_SET_STATES, 8, 0,
            new String[] { "temperatures#outdoorTemperatureIn", "temperatures#indoorTemperatureIn",
                    "preheater#preheaterFrostProtect", "preheater#preheaterFrostTime", "preheater#preheaterHeating",
                    "menuP9#frostState", "preheater#preheaterSafety", "times#preheaterTime",
                    "preheater#preheaterValve" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 0 }),
    IS_BYPASS("options#isBypass", DataTypeBoolean.class, Constants.REQUEST_SET_STATES, 8, 1,
            new String[] { "temperatures#indoorTemperatureIn", "temperatures#outdoorTemperatureOut" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 1 }),
    RECU_TYPE("options#recuType", DataTypeNumber.class, new int[] { 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 2,
            new String[] { "ventilation#fanInPercent", "ventilation#fanOutPercent", "temperatures#indoorTemperatureIn",
                    "temperatures#outdoorTemperatureOut", "temperatures#indoorTemperatureOut",
                    "temperatures#outdoorTemperatureIn" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 2 }),
    RECU_SIZE("options#recuSize", DataTypeNumber.class, new int[] { 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 3,
            new String[] { "ventilation#fanInPercent", "ventilation#fanOutPercent", "ventilation#fanOut0",
                    "ventilation#fanOut1", "ventilation#fanOut2", "ventilation#fanOut3", "ventilation#fanIn0",
                    "ventilation#fanIn1", "ventilation#fanIn2", "ventilation#fanIn3" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 3 }),
    IS_CHIMNEY("options#isChimney", DataTypeBoolean.class, new int[] { 0x01 }, Constants.REQUEST_SET_STATES, 8, 4,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 4 },
            0x01),
    IS_COOKERHOOD("options#isCookerhood", DataTypeBoolean.class, new int[] { 0x02 }, Constants.REQUEST_SET_STATES, 8, 4,
            new String[] { "menuP2#cookerhoodDelay", "menuP9#cookerhoodState", "cookerhood#cookerhoodSpeed",
                    "temperatures#cookerhoodTemperature" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 4 }, 0x02),
    IS_HEATER("options#isHeater", DataTypeBoolean.class, new int[] { 0x04 }, Constants.REQUEST_SET_STATES, 8, 4,
            new String[] { "heater#heaterTargetTemperature", "heater#heaterPower", "menuP9#heaterState",
                    "heater#heaterPowerI", "temperatures#heaterTemperature" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 4 }, 0x04),
    IS_ENTHALPY("options#isEnthalpy", DataTypeNumber.class, new int[] { 0x00, 0x01, 0x02 },
            Constants.REQUEST_SET_STATES, 8, 6,
            new String[] { "enthalpy#enthalpyTemperature", "enthalpy#enthalpyHumidity", "enthalpy#enthalpyLevel",
                    "menuP9#enthalpyState", "enthalpy#enthalpyTime" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 9 }),
    IS_EWT("options#isEWT", DataTypeNumber.class, new int[] { 0x00, 0x01, 0x02 }, Constants.REQUEST_SET_STATES, 8, 7,
            new String[] { "ewt#ewtSpeed", "ewt#ewtTemperatureLow", "menuP9#ewtState", "ewt#ewtTemperatureHigh",
                    "temperatures#ewtTemperature" },
            Constants.REQUEST_GET_STATES, Constants.REPLY_GET_STATES, new int[] { 10 }),
    EWT_SPEED("ewt#ewtSpeed", DataTypeNumber.class, Constants.REQUEST_SET_EWT, 5, 2,
            new String[] { "menuP9#ewtState", "temperatures#ewtTemperature" }, Constants.REQUEST_GET_EWT,
            Constants.REPLY_GET_EWT, new int[] { 2 }),
    EWT_TEMPERATURE_LOW("ewt#ewtTemperatureLow", DataTypeTemperature.class, Constants.REQUEST_SET_EWT, 5, 0,
            new String[] { "menuP9#ewtState" }, Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 0 }),
    EWT_TEMPERATURE_HIGH("ewt#ewtTemperatureHigh", DataTypeTemperature.class, Constants.REQUEST_SET_EWT, 5, 1,
            new String[] { "menuP9#ewtState" }, Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 1 }),
    COOKERHOOD_SPEED("cookerhood#cookerhoodSpeed", DataTypeNumber.class, Constants.REQUEST_SET_EWT, 5, 3,
            new String[] { "menuP9#cookerhoodState", "temperatures#cookerhoodTemperature" }, Constants.REQUEST_GET_EWT,
            Constants.REPLY_GET_EWT, new int[] { 3 }),
    HEATER_POWER("heater#heaterPower", DataTypeNumber.class, Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT,
            new int[] { 4 }),
    HEATER_POWER_I("heater#heaterPowerI", DataTypeNumber.class, Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT,
            new int[] { 5 }),
    HEATER_TARGET_TEMPERATUR("heater#heaterTargetTemperature", DataTypeTemperature.class, Constants.REQUEST_SET_EWT, 5,
            4, new String[] { "menuP9#heaterState", "heater#heaterPower", "temperatures#heaterTemperature" },
            Constants.REQUEST_GET_EWT, Constants.REPLY_GET_EWT, new int[] { 6 }),
    SOFTWARE_MAIN_VERSION("softwareMainVersion", DataTypeNumber.class, Constants.REQUEST_GET_FIRMWARE,
            Constants.REPLY_GET_FIRMWARE, new int[] { 0 }),
    SOFTWARE_MINOR_VERSION("softwareMinorVersion", DataTypeNumber.class, Constants.REQUEST_GET_FIRMWARE,
            Constants.REPLY_GET_FIRMWARE, new int[] { 1 }),
    SOFTWARE_BETA_VERSION("softwareBetaVersion", DataTypeNumber.class, Constants.REQUEST_GET_FIRMWARE,
            Constants.REPLY_GET_FIRMWARE, new int[] { 2 }),
    ERROR_MESSAGE("ccease#errorMessage", DataTypeMessage.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 0, 1, 9, 13 }),
    ERRORA_CURRENT("error#errorACurrent", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 0 }),
    ERRORA_LAST("error#errorALast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS,
            new int[] { 2 }),
    ERRORA_PRELAST("error#errorAPrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 4 }),
    ERRORA_PREPRELAST("error#errorAPrePrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 6 }),
    ERRORAHIGH_CURRENT("error#errorAHighCurrent", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 13 }),
    ERRORAHIGH_LAST("error#errorAHighLast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 14 }),
    ERRORAHIGH_PRELAST("error#errorAHighPrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 15 }),
    ERRORAHIGH_PREPRELAST("error#errorAHighPrePrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 16 }),
    ERRORE_CURRENT("error#errorECurrent", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 1 }),
    ERRORE_LAST("error#errorELast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS,
            new int[] { 3 }),
    ERRORE_PRELAST("error#errorEPrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 5 }),
    ERRORE_PREPRELAST("error#errorEPrePrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 7 }),
    ERROREA_CURRENT("error#errorEACurrent", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 9 }),
    ERROREA_LAST("error#errorEALast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS,
            new int[] { 10 }),
    ERROREA_PRELAST("error#errorEAPrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 11 }),
    ERROREA_PREPRELAST("error#errorEAPrePrelast", DataTypeNumber.class, Constants.REQUEST_GET_ERRORS,
            Constants.REPLY_GET_ERRORS, new int[] { 12 }),
    ERROR_RESET("ccease#errorReset", DataTypeBoolean.class, new int[] { 0x01 }, Constants.REQUEST_SET_RESETS, 4, 0,
            new String[] { "ccease#errorMessage" }),
    FILTER_HOURS("times#filterHours", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 15, 16 }),
    FILTER_RESET("ccease#filterReset", DataTypeBoolean.class, new int[] { 0x01 }, Constants.REQUEST_SET_RESETS, 4, 3,
            new String[] { "times#filterHours", "ccease#filterError" }),
    FILTER_ERROR("ccease#filterError", DataTypeBoolean.class, Constants.REQUEST_GET_ERRORS, Constants.REPLY_GET_ERRORS,
            new int[] { 8 }, 0x01),
    BYPASS_FACTOR("bypass#bypassFactor", DataTypeNumber.class, Constants.REQUEST_GET_BYPASS, Constants.REPLY_GET_BYPASS,
            new int[] { 2 }),
    BYPASS_LEVEL("bypass#bypassLevel", DataTypeNumber.class, Constants.REQUEST_GET_BYPASS, Constants.REPLY_GET_BYPASS,
            new int[] { 3 }),
    BYPASS_CORRECTION("bypass#bypassCorrection", DataTypeNumber.class, Constants.REQUEST_GET_BYPASS,
            Constants.REPLY_GET_BYPASS, new int[] { 4 }),
    BYPASS_SUMMER("bypass#bypassSummer", DataTypeBoolean.class, Constants.REQUEST_GET_BYPASS,
            Constants.REPLY_GET_BYPASS, new int[] { 6 }),
    ENTHALPY_TEMPERATUR("enthalpy#enthalpyTemperature", DataTypeTemperature.class, Constants.REQUEST_GET_SENSORS,
            Constants.REPLY_GET_SENSORS, new int[] { 0 }),
    ENTHALPY_HUMIDITY("enthalpy#enthalpyHumidity", DataTypeNumber.class, Constants.REQUEST_GET_SENSORS,
            Constants.REPLY_GET_SENSORS, new int[] { 1 }),
    ENTHALPY_LEVEL("enthalpy#enthalpyLevel", DataTypeNumber.class, Constants.REQUEST_GET_SENSORS,
            Constants.REPLY_GET_SENSORS, new int[] { 4 }),
    ENTHALPY_TIME("enthalpy#enthalpyTime", DataTypeNumber.class, Constants.REQUEST_GET_SENSORS,
            Constants.REPLY_GET_SENSORS, new int[] { 5 }),
    PREHEATER_VALVE("preheater#preheaterValve", DataTypeNumber.class, Constants.REQUEST_GET_PREHEATER,
            Constants.REPLY_GET_PREHEATER, new int[] { 0 }),
    PREHEATER_FROST_PROTECT("preheater#preheaterFrostProtect", DataTypeBoolean.class, Constants.REQUEST_GET_PREHEATER,
            Constants.REPLY_GET_PREHEATER, new int[] { 1 }),
    PREHEATER_HEATING("preheater#preheaterHeating", DataTypeBoolean.class, Constants.REQUEST_GET_PREHEATER,
            Constants.REPLY_GET_PREHEATER, new int[] { 2 }),
    PREHEATER_FROST_TIME("preheater#preheaterFrostTime", DataTypeNumber.class, Constants.REQUEST_GET_PREHEATER,
            Constants.REPLY_GET_PREHEATER, new int[] { 3, 4 }),
    PREHEATER_OPTION("preheater#preheaterSafety", DataTypeNumber.class, Constants.REQUEST_GET_PREHEATER,
            Constants.REPLY_GET_PREHEATER, new int[] { 5 }),
    LEVEL0_TIME("times#level0Time", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 0, 1, 2 }),
    LEVEL1_TIME("times#level1Time", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 3, 4, 5 }),
    LEVEL2_TIME("times#level2Time", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 6, 7, 8 }),
    LEVEL3_TIME("times#level3Time", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 17, 18, 19 }),
    FREEZE_TIME("times#freezeTime", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 9, 10 }),
    PREHEATER_TIME("times#preheaterTime", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 11, 12 }),
    BYPASS_TIME("times#bypassTime", DataTypeNumber.class, Constants.REQUEST_GET_HOURS, Constants.REPLY_GET_HOURS,
            new int[] { 13, 14 }),
    IS_ANALOG1("analog#isAnalog1", DataTypeBoolean.class, new int[] { 0x01 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { "analog#analog1Mode", "analog#analog1Negative", "analog#analog1Min", "analog#analog1Max",
                    "analog#analog1Value", "analog#analog1Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x01),
    IS_ANALOG2("analog#isAnalog2", DataTypeBoolean.class, new int[] { 0x02 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { "analog#analog2Mode", "analog#analog2Negative", "analog#analog2Min", "analog#analog2Max",
                    "analog#analog2Value", "analog#analog2Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x02),
    IS_ANALOG3("analog#isAnalog3", DataTypeBoolean.class, new int[] { 0x04 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { "analog#analog3Mode", "analog#analog3Negative", "analog#analog3Min", "analog#analog3Max",
                    "analog#analog3Value", "analog#analog3Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x04),
    IS_ANALOG4("analog#isAnalog4", DataTypeBoolean.class, new int[] { 0x08 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { "analog#analog4Mode", "analog#analog4Negative", "analog#analog4Min", "analog#analog4Max",
                    "analog#analog4Value", "analog#analog4Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x08),
    IS_RF("analog#isRF", DataTypeBoolean.class, new int[] { 0x10 }, Constants.REQUEST_SET_ANALOGS, 19, 0,
            new String[] { "analog#RFMode", "analog#RFNegative", "analog#RFMin", "analog#RFMax", "analog#RFValue" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 0 }, 0x10),
    ANALOG1_MODE("analog#analog1Mode", DataTypeBoolean.class, new int[] { 0x01 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { "analog#analog1Negative", "analog#analog1Min", "analog#analog1Max", "analog#analog1Value",
                    "analog#analog1Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x01),
    ANALOG2_MODE("analog#analog2Mode", DataTypeBoolean.class, new int[] { 0x02 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { "analog#analog2Negative", "analog#analog2Min", "analog#analog2Max", "analog#analog2Value",
                    "analog#analog2Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x02),
    ANALOG3_MODE("analog#analog3Mode", DataTypeBoolean.class, new int[] { 0x04 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { "analog#analog3Negative", "analog#analog3Min", "analog#analog3Max", "analog#analog3Value",
                    "analog#analog3Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x04),
    ANALOG4_MODE("analog#analog4Mode", DataTypeBoolean.class, new int[] { 0x08 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { "analog#analog4Negative", "analog#analog4Min", "analog#analog4Max", "analog#analog4Value",
                    "analog#analog4Volt" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x08),
    RF_MODE("analog#RFMode", DataTypeBoolean.class, new int[] { 0x10 }, Constants.REQUEST_SET_ANALOGS, 19, 1,
            new String[] { "analog#RFNegative", "analog#RFMin", "analog#RFMax", "analog#RFValue" },
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 1 }, 0x10),
    ANALOG1_NEGATIVE("analog#analog1Negative", DataTypeBoolean.class, new int[] { 0x01 }, Constants.REQUEST_SET_ANALOGS,
            19, 2, Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS,
            new int[] { 2 }, 0x01),
    ANALOG2_NEGATIVE("analog#analog2Negative", DataTypeBoolean.class, new int[] { 0x02 }, Constants.REQUEST_SET_ANALOGS,
            19, 2, Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS,
            new int[] { 2 }, 0x02),
    ANALOG3_NEGATIVE("analog#analog3Negative", DataTypeBoolean.class, new int[] { 0x04 }, Constants.REQUEST_SET_ANALOGS,
            19, 2, Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS,
            new int[] { 2 }, 0x04),
    ANALOG4_NEGATIVE("analog#analog4Negative", DataTypeBoolean.class, new int[] { 0x08 }, Constants.REQUEST_SET_ANALOGS,
            19, 2, Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS,
            new int[] { 2 }, 0x08),
    RF_NEGATIVE("analog#RFNegative", DataTypeBoolean.class, new int[] { 0x10 }, Constants.REQUEST_SET_ANALOGS, 19, 2,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 2 },
            0x10),
    ANALOG1_MIN("analog#analog1Min", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 3,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 3 }),
    ANALOG1_MAX("analog#analog1Max", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 4,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 4 }),
    ANALOG1_VALUE("analog#analog1Value", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 5,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 5 }),
    ANALOG2_MIN("analog#analog2Min", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 6,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 6 }),
    ANALOG2_MAX("analog#analog2Max", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 7,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 7 }),
    ANALOG2_VALUE("analog#analog2Value", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 8,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 8 }),
    ANALOG3_MIN("analog#analog3Min", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 9,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 9 }),
    ANALOG3_MAX("analog#analog3Max", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 10,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 10 }),
    ANALOG3_VALUE("analog#analog3Value", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 11,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 11 }),
    ANALOG4_MIN("analog#analog4Min", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 12,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 12 }),
    ANALOG4_MAX("analog#analog4Max", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 13,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 13 }),
    ANALOG4_VALUE("analog#analog4Value", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 14,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 14 }),
    RF_MIN("analog#RFMin", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 15, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 15 }),
    RF_MAX("analog#RFMax", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 16, Constants.EMPTY_STRING_ARRAY,
            Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 16 }),
    RF_VALUE("analog#RFValue", DataTypeNumber.class, Constants.REQUEST_SET_ANALOGS, 19, 17,
            Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS, new int[] { 17 }),
    ANALOG_MODE("analog#analogMode", DataTypeNumber.class, new int[] { 0x00, 0x01 }, Constants.REQUEST_SET_ANALOGS, 19,
            18, Constants.EMPTY_STRING_ARRAY, Constants.REQUEST_GET_ANALOGS, Constants.REPLY_GET_ANALOGS,
            new int[] { 18 }),
    ANALOG1_VOLT("analog#analog1Volt", DataTypeVolt.class, Constants.REQUEST_GET_ANALOG_VOLTS,
            Constants.REPLY_GET_ANALOG_VOLTS, new int[] { 0 }),
    ANALOG2_VOLT("analog#analog2Volt", DataTypeVolt.class, Constants.REQUEST_GET_ANALOG_VOLTS,
            Constants.REPLY_GET_ANALOG_VOLTS, new int[] { 1 }),
    ANALOG3_VOLT("analog#analog3Volt", DataTypeVolt.class, Constants.REQUEST_GET_ANALOG_VOLTS,
            Constants.REPLY_GET_ANALOG_VOLTS, new int[] { 2 }),
    ANALOG4_VOLT("analog#analog4Volt", DataTypeVolt.class, Constants.REQUEST_GET_ANALOG_VOLTS,
            Constants.REPLY_GET_ANALOG_VOLTS, new int[] { 3 }),
    IS_L1_SWITCH("inputs#isL1Switch", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS,
            new int[] { 0 }, 0x01),
    IS_L2_SWITCH("inputs#isL2Switch", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS,
            new int[] { 0 }, 0x02),
    IS_BATHROOM_SWITCH("inputs#isBathroomSwitch", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS,
            Constants.REPLY_GET_INPUTS, new int[] { 1 }, 0x01),
    IS_COOKERHOOD_SWITCH("inputs#isCookerhoodSwitch", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS,
            Constants.REPLY_GET_INPUTS, new int[] { 1 }, 0x02),
    IS_EXTERNAL_FILTER("inputs#isExternalFilter", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS,
            Constants.REPLY_GET_INPUTS, new int[] { 1 }, 0x04),
    IS_WTW("inputs#isWTW", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS, Constants.REPLY_GET_INPUTS,
            new int[] { 1 }, 0x08),
    IS_BATHROOM2_SWITCH("inputs#isBathroom2Switch", DataTypeBoolean.class, Constants.REQUEST_GET_INPUTS,
            Constants.REPLY_GET_INPUTS, new int[] { 1 }, 0x10);

    private final Logger logger = LoggerFactory.getLogger(ComfoAirCommandType.class);
    private String key;
    private Class<? extends ComfoAirDataType> data_type;

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
    private ComfoAirCommandType(String key, Class<? extends ComfoAirDataType> data_type, int[] possible_values,
            int change_command, int change_data_size, int change_data_pos, String[] change_affected, int read_command,
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
    private ComfoAirCommandType(String key, Class<? extends ComfoAirDataType> data_type, int[] possible_values,
            int change_command, int change_data_size, int change_data_pos, String[] change_affected, int read_command,
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
    private ComfoAirCommandType(String key, Class<? extends ComfoAirDataType> data_type, int change_command,
            int change_data_size, int change_data_pos, String[] change_affected, int read_command,
            int read_reply_command, int[] read_reply_data_pos) {
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
    private ComfoAirCommandType(String key, Class<? extends ComfoAirDataType> data_type, int[] possible_values,
            int change_command, int change_data_size, int change_data_pos, String[] change_affected) {
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
    private ComfoAirCommandType(String key, Class<? extends ComfoAirDataType> data_type, int read_command,
            int read_reply_command, int[] read_reply_data_pos, int read_reply_data_bits) {
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
    private ComfoAirCommandType(String key, Class<? extends ComfoAirDataType> data_type, int read_command,
            int read_reply_command, int[] read_reply_data_pos) {
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
    public @Nullable ComfoAirDataType getDataType() {
        try {
            return data_type.newInstance();
        } catch (Exception e) {
            logger.debug("Creating new DataType went wrong ", e);
        }
        return null;
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
    public static @Nullable ComfoAirCommand getChangeCommand(String key, State value) {
        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);
        DecimalType decimalValue = value.as(DecimalType.class);

        if (commandType != null && decimalValue != null) {
            ComfoAirDataType dataType = commandType.getDataType();
            if (dataType != null) {
                int[] data = dataType.convertFromState(value, commandType);

                if (data != null) {
                    int dataPossition = commandType.getChangeDataPos();
                    int intValue = decimalValue.intValue();

                    return new ComfoAirCommand(key, commandType.change_command, null, data, dataPossition, intValue);
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

        Map<Integer, ComfoAirCommand> commands = new HashMap<Integer, ComfoAirCommand>();

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

        Map<Integer, ComfoAirCommand> commands = new HashMap<Integer, ComfoAirCommand>();
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
        List<ComfoAirCommandType> commands = new ArrayList<ComfoAirCommandType>();
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
