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
public enum ComfoAirCommandType {

    /**
     * Below all valid commands to change or read parameters from ComfoAir
     *
     * @param key
     *            command name
     * @param data_tape
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
    ACTIVATE {
        {
            key = "bindingControl#activate";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x03 };
            change_command = 0x9b;
            change_data_size = 1;
            change_data_pos = 0;
            change_affected = new String[] {};
            read_command = 0x9c;
            read_reply_command = 0x9c;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x03;
        }
    },

    MENU20_MODE {
        {
            key = "menuP1#menu20Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x01;
        }
    },

    MENU21_MODE {
        {
            key = "menuP1#menu21Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x02;
        }
    },

    MENU22_MODE {
        {
            key = "menuP1#menu22Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x04;
        }
    },

    MENU23_MODE {
        {
            key = "menuP1#menu23Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x08;
        }
    },

    MENU24_MODE {
        {
            key = "menuP1#menu24Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x10;
        }
    },

    MENU25_MODE {
        {
            key = "menuP1#menu25Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x20;
        }
    },

    MENU26_MODE {
        {
            key = "menuP1#menu26Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x40;
        }
    },

    MENU27_MODE {
        {
            key = "menuP1#menu27Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 6 };
            read_reply_data_bits = 0x80;
        }
    },

    MENU28_MODE {
        {
            key = "menuP1#menu28Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 7 };
            read_reply_data_bits = 0x01;
        }
    },

    MENU29_MODE {
        {
            key = "menuP1#menu29Mode";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 7 };
            read_reply_data_bits = 0x02;
        }
    },

    BATHROOM_START_DELAY {
        {
            key = "menuP2#bathroomStartDelay";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 0;
            change_affected = new String[] { "menuP1#menu21Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    BATHROOM_END_DELAY {
        {
            key = "menuP2#bathroomEndDelay";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 1;
            change_affected = new String[] { "menuP1#menu22Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    L1_END_DELAY {
        {
            key = "menuP2#L1EndDelay";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 2;
            change_affected = new String[] { "menuP1#menu27Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    PULSE_VENTILATION {
        {
            key = "menuP2#pulseVentilation";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 3;
            change_affected = new String[] { "menuP1#menu23Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    FILTER_WEEKS {
        {
            key = "menuP2#filterWeeks";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 4;
            change_affected = new String[] { "menuP1#menu24Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    RF_SHORT_DELAY {
        {
            key = "menuP2#RFShortDelay";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 5;
            change_affected = new String[] { "menuP1#menu25Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    RF_LONG_DELAY {
        {
            key = "menuP2#RFLongDelay";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 6;
            change_affected = new String[] { "menuP1#menu26Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 6 };
        }
    },

    COOKERHOOD_DELAY {
        {
            key = "menuP2#cookerhoodDelay";
            data_type = DataTypeNumber.class;
            change_command = 0xcb;
            change_data_size = 8;
            change_data_pos = 7;
            change_affected = new String[] { "menuP1#menu20Mode" };
            read_command = 0xc9;
            read_reply_command = 0xca;
            read_reply_data_pos = new int[] { 7 };
        }
    },

    CHIMNEY_STATE {
        {
            key = "menuP9#chimneyState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x01;
        }
    },

    BYPASS_STATE {
        {
            key = "menuP9#bypassState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x02;
        }
    },

    EWT_STATE {
        {
            key = "menuP9#EWTState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x04;
        }
    },

    HEATER_STATE {
        {
            key = "menuP9#heaterState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x08;
        }
    },

    V_CONTROL_STATE {
        {
            key = "menuP9#vControlState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x10;
        }
    },

    FROST_STATE {
        {
            key = "menuP9#frostState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x20;
        }
    },

    COOKERHOOD_STATE {
        {
            key = "menuP9#cookerhoodState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x40;
        }
    },

    ENTHALPY_STATE {
        {
            key = "menuP9#enthalpyState";
            data_type = DataTypeBoolean.class;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x80;
        }
    },

    FAN_OUT_0 {
        {
            key = "ventilation#fanOut0";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 0;
            change_affected = new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    FAN_OUT_1 {
        {
            key = "ventilation#fanOut1";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 1;
            change_affected = new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    FAN_OUT_2 {
        {
            key = "ventilation#fanOut2";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 2;
            change_affected = new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    FAN_OUT_3 {
        {
            key = "ventilation#fanOut3";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 6;
            change_affected = new String[] { "ventilation#fanOutPercent", "ventilation#fanOutRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 10 };
        }
    },

    FAN_IN_0 {
        {
            key = "ventilation#fanIn0";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 3;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    FAN_IN_1 {
        {
            key = "ventilation#fanIn1";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 4;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    FAN_IN_2 {
        {
            key = "ventilation#fanIn2";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 5;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    FAN_IN_3 {
        {
            key = "ventilation#fanIn3";
            data_type = DataTypeNumber.class;
            change_command = 0xcf;
            change_data_size = 9;
            change_data_pos = 7;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanInRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 11 };
        }
    },

    FAN_IN_PERCENT {
        {
            key = "ventilation#fanInPercent";
            data_type = DataTypeNumber.class;
            read_command = 0x0b;
            read_reply_command = 0x0c;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    FAN_OÚT_PERCENT {
        {
            key = "ventilation#fanOutPercent";
            data_type = DataTypeNumber.class;
            read_command = 0x0b;
            read_reply_command = 0x0c;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    FAN_IN_RPM {
        {
            key = "ventilation#fanInRPM";
            data_type = DataTypeRPM.class;
            read_command = 0x0b;
            read_reply_command = 0x0c;
            read_reply_data_pos = new int[] { 2, 3 };
        }
    },

    FAN_OUT_RPM {
        {
            key = "ventilation#fanOutRPM";
            data_type = DataTypeRPM.class;
            read_command = 0x0b;
            read_reply_command = 0x0c;
            read_reply_data_pos = new int[] { 4, 5 };
        }
    },

    FAN_LEVEL {
        {
            key = "ccease#fanLevel";
            data_type = DataTypeNumber.class;
            possible_values = new int[] { 0x01, 0x02, 0x03, 0x04 };
            change_command = 0x99;
            change_data_size = 1;
            change_data_pos = 0;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanOutPercent",
                    "ventilation#fanInRPM", "ventilation#fanOutRPM" };
            read_command = 0xcd;
            read_reply_command = 0xce;
            read_reply_data_pos = new int[] { 8 };
        }
    },

    TARGET_TEMPERATUR {
        {
            key = "ccease#targetTemperature";
            data_type = DataTypeTemperature.class;
            change_command = 0xd3;
            change_data_size = 1;
            change_data_pos = 0;
            change_affected = new String[] { "bypass#bypassFactor", "bypass#bypassLevel", "bypass#bypassSummer" };
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    OUTDOOR_TEMPERATURE_IN {
        {
            key = "temperatures#outdoorTemperatureIn";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    OUTDOOR_TEMPERATURE_OUT {
        {
            key = "temperatures#outdoorTemperatureOut";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    INDOOR_TEMPERATURE_IN {
        {
            key = "temperatures#indoorTemperatureIn";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    INDOOR_TEMPERATURE_OUT {
        {
            key = "temperatures#indoorTemperatureOut";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    IS_T1_SENSOR {
        {
            key = "temperatures#isT1Sensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x01;
        }
    },

    IS_T2_SENSOR {
        {
            key = "temperatures#isT2Sensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x02;
        }
    },

    IS_T3_SENSOR {
        {
            key = "temperatures#isT3Sensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x04;
        }
    },

    IS_T4_SENSOR {
        {
            key = "temperatures#isT4Sensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x08;
        }
    },

    IS_EWT_SENSOR {
        {
            key = "temperatures#isEWTSensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x10;
        }
    },

    IS_HEATER_SENSOR {
        {
            key = "temperatures#isHeaterSensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x20;
        }
    },

    IS_COOKERHOOD_SENSOR {
        {
            key = "temperatures#isCookerhoodSensor";
            data_type = DataTypeBoolean.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 5 };
            read_reply_data_bits = 0x40;
        }
    },

    EWT_TEMPERATUR {
        {
            key = "temperatures#ewtTemperature";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 6 };
        }
    },

    HEATER_TEMPERATUR {
        {
            key = "temperatures#heaterTemperature";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 7 };
        }
    },

    COOKERHOOD_TEMPERATUR {
        {
            key = "temperatures#cookerhoodTemperature";
            data_type = DataTypeTemperature.class;
            read_command = 0xd1;
            read_reply_command = 0xd2;
            read_reply_data_pos = new int[] { 8 };
        }
    },

    IS_PREHEATER {
        {
            key = "options#isPreheater";
            data_type = DataTypeBoolean.class;
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 1;
            change_affected = new String[] { "temperatures#outdoorTemperatureIn", "temperatures#indoorTemperatureIn",
                    "preheater#preheaterFrostProtect", "preheater#preheaterFrostTime", "preheater#preheaterHeating",
                    "menuP9#frostState", "preheater#preheaterSafety", "times#preheaterTime",
                    "preheater#preheaterValve" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    IS_BYPASS {
        {
            key = "options#isBypass";
            data_type = DataTypeBoolean.class;
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 1;
            change_affected = new String[] { "temperatures#indoorTemperatureIn", "temperatures#outdoorTemperatureOut" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    RECU_TYPE {
        {
            key = "options#recuType";
            data_type = DataTypeNumber.class;
            possible_values = new int[] { 0x01, 0x02 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 2;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanOutPercent",
                    "temperatures#indoorTemperatureIn", "temperatures#outdoorTemperatureOut",
                    "temperatures#indoorTemperatureOut", "temperatures#outdoorTemperatureIn" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    RECU_SIZE {
        {
            key = "options#recuSize";
            data_type = DataTypeNumber.class;
            possible_values = new int[] { 0x01, 0x02 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 3;
            change_affected = new String[] { "ventilation#fanInPercent", "ventilation#fanOutPercent",
                    "ventilation#fanOut0", "ventilation#fanOut1", "ventilation#fanOut2", "ventilation#fanOut3",
                    "ventilation#fanIn0", "ventilation#fanIn1", "ventilation#fanIn2", "ventilation#fanIn3" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    IS_CHIMNEY {
        {
            key = "options#isChimney";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x01 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 4;
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 4 };
            read_reply_data_bits = 0x01;
        }
    },

    IS_COOKERHOOD {
        {
            key = "options#isCookerhood";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x02 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 4;
            change_affected = new String[] { "menuP2#cookerhoodDelay", "menuP9#cookerhoodState",
                    "cookerhood#cookerhoodSpeed", "temperatures#cookerhoodTemperature" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 4 };
            read_reply_data_bits = 0x02;
        }
    },

    IS_HEATER {
        {
            key = "options#isHeater";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x04 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 4;
            change_affected = new String[] { "heater#heaterTargetTemperature", "heater#heaterPower",
                    "menuP9#heaterState", "heater#heaterPowerI", "temperatures#heaterTemperature" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 4 };
            read_reply_data_bits = 0x04;
        }
    },

    IS_ENTHALPY {
        {
            key = "options#isEnthalpy";
            data_type = DataTypeNumber.class;
            possible_values = new int[] { 0x00, 0x01, 0x02 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 6;
            change_affected = new String[] { "enthalpy#enthalpyTemperature", "enthalpy#enthalpyHumidity",
                    "enthalpy#enthalpyLevel", "menuP9#enthalpyState", "enthalpy#enthalpyTime" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 9 };
        }
    },

    IS_EWT {
        {
            key = "options#isEWT";
            data_type = DataTypeNumber.class;
            possible_values = new int[] { 0x00, 0x01, 0x02 };
            change_command = 0xd7;
            change_data_size = 8;
            change_data_pos = 7;
            change_affected = new String[] { "ewt#ewtSpeed", "ewt#ewtTemperatureLow", "menuP9#ewtState",
                    "ewt#ewtTemperatureHigh", "temperatures#ewtTemperature" };
            read_command = 0xd5;
            read_reply_command = 0xd6;
            read_reply_data_pos = new int[] { 10 };
        }
    },

    EWT_SPEED {
        {
            key = "ewt#ewtSpeed";
            data_type = DataTypeNumber.class;
            change_command = 0xed;
            change_data_size = 5;
            change_data_pos = 2;
            change_affected = new String[] { "menuP9#ewtState", "temperatures#ewtTemperature" };
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    EWT_TEMPERATURE_LOW {
        {
            key = "ewt#ewtTemperatureLow";
            data_type = DataTypeTemperature.class;
            change_command = 0xed;
            change_data_size = 5;
            change_data_pos = 0;
            change_affected = new String[] { "menuP9#ewtState" };
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    EWT_TEMPERATURE_HIGH {
        {
            key = "ewt#ewtTemperatureHigh";
            data_type = DataTypeTemperature.class;
            change_command = 0xed;
            change_data_size = 5;
            change_data_pos = 1;
            change_affected = new String[] { "menuP9#ewtState" };
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    COOKERHOOD_SPEED {
        {
            key = "cookerhood#cookerhoodSpeed";
            data_type = DataTypeNumber.class;
            change_command = 0xed;
            change_data_size = 5;
            change_data_pos = 3;
            change_affected = new String[] { "menuP9#cookerhoodState", "temperatures#cookerhoodTemperature" };
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    HEATER_POWER {
        {
            key = "heater#heaterPower";
            data_type = DataTypeNumber.class;
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    HEATER_POWER_I {
        {
            key = "heater#heaterPowerI";
            data_type = DataTypeNumber.class;
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    HEATER_TARGET_TEMPERATUR {
        {
            key = "heater#heaterTargetTemperature";
            data_type = DataTypeTemperature.class;
            change_command = 0xed;
            change_data_size = 5;
            change_data_pos = 4;
            change_affected = new String[] { "menuP9#heaterState", "heater#heaterPower",
                    "temperatures#heaterTemperature" };
            read_command = 0xeb;
            read_reply_command = 0xec;
            read_reply_data_pos = new int[] { 6 };
        }
    },

    SOFTWARE_MAIN_VERSION {
        {
            key = "software#softwareMainVersion";
            data_type = DataTypeNumber.class;
            read_command = 0x69;
            read_reply_command = 0x6a;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    SOFTWARE_MINOR_VERSION {
        {
            key = "software#softwareMinorVersion";
            data_type = DataTypeNumber.class;
            read_command = 0x69;
            read_reply_command = 0x6a;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    SOFTWARE_BETA_VERSION {
        {
            key = "software#softwareBetaVersion";
            data_type = DataTypeNumber.class;
            read_command = 0x69;
            read_reply_command = 0x6a;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    ERROR_MESSAGE {
        {
            key = "ccease#errorMessage";
            data_type = DataTypeMessage.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 0, 1, 9, 13 };
        }
    },

    ERRORA_CURRENT {
        {
            key = "error#errorACurrent";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    ERRORA_LAST {
        {
            key = "error#errorALast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    ERRORA_PRELAST {
        {
            key = "error#errorAPrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    ERRORA_PREPRELAST {
        {
            key = "error#errorAPrePrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 6 };
        }
    },

    ERRORAHIGH_CURRENT {
        {
            key = "error#errorAHighCurrent";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 13 };
        }
    },

    ERRORAHIGH_LAST {
        {
            key = "error#errorAHighLast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 14 };
        }
    },

    ERRORAHIGH_PRELAST {
        {
            key = "error#errorAHighPrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 15 };
        }
    },

    ERRORAHIGH_PREPRELAST {
        {
            key = "error#errorAHighPrePrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 16 };
        }
    },

    ERRORE_CURRENT {
        {
            key = "error#errorECurrent";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    ERRORE_LAST {
        {
            key = "error#errorELast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    ERRORE_PRELAST {
        {
            key = "error#errorEPrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    ERRORE_PREPRELAST {
        {
            key = "error#errorEPrePrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 7 };
        }
    },

    ERROREA_CURRENT {
        {
            key = "error#errorEACurrent";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 9 };
        }
    },

    ERROREA_LAST {
        {
            key = "error#errorEALast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 10 };
        }
    },

    ERROREA_PRELAST {
        {
            key = "error#errorEAPrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 11 };
        }
    },

    ERROREA_PREPRELAST {
        {
            key = "error#errorEAPrePrelast";
            data_type = DataTypeNumber.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 12 };
        }
    },

    ERROR_RESET {
        {
            key = "ccease#errorReset";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x01 };
            change_command = 0xdb;
            change_data_size = 4;
            change_data_pos = 0;
            change_affected = new String[] { "ccease#errorMessage" };
        }
    },

    FILTER_HOURS {
        {
            key = "times#filterHours";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 15, 16 };
        }
    },

    FILTER_RESET {
        {
            key = "ccease#filterReset";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x01 };
            change_command = 0xdb;
            change_data_size = 4;
            change_data_pos = 3;
            change_affected = new String[] { "times#filterHours", "ccease#filterError" };
        }
    },

    FILTER_ERROR {
        {
            key = "ccease#filterError";
            data_type = DataTypeBoolean.class;
            read_command = 0xd9;
            read_reply_command = 0xda;
            read_reply_data_pos = new int[] { 8 };
            read_reply_data_bits = 0x01;
        }
    },

    BYPASS_FACTOR {
        {
            key = "bypass#bypassFactor";
            data_type = DataTypeNumber.class;
            read_command = 0xdf;
            read_reply_command = 0xe0;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    BYPASS_LEVEL {
        {
            key = "bypass#bypassLevel";
            data_type = DataTypeNumber.class;
            read_command = 0xdf;
            read_reply_command = 0xe0;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    BYPASS_CORRECTION {
        {
            key = "bypass#bypassCorrection";
            data_type = DataTypeNumber.class;
            read_command = 0xdf;
            read_reply_command = 0xe0;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    BYPASS_SUMMER {
        {
            key = "bypass#bypassSummer";
            data_type = DataTypeBoolean.class;
            read_command = 0xdf;
            read_reply_command = 0xe0;
            read_reply_data_pos = new int[] { 6 };
        }
    },

    ENTHALPY_TEMPERATUR {
        {
            key = "enthalpy#enthalpyTemperature";
            data_type = DataTypeTemperature.class;
            read_command = 0x97;
            read_reply_command = 0x98;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    ENTHALPY_HUMIDITY {
        {
            key = "enthalpy#enthalpyHumidity";
            data_type = DataTypeNumber.class;
            read_command = 0x97;
            read_reply_command = 0x98;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    ENTHALPY_LEVEL {
        {
            key = "enthalpy#enthalpyLevel";
            data_type = DataTypeNumber.class;
            read_command = 0x97;
            read_reply_command = 0x98;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    ENTHALPY_TIME {
        {
            key = "enthalpy#enthalpyTime";
            data_type = DataTypeNumber.class;
            read_command = 0x97;
            read_reply_command = 0x98;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    PREHEATER_VALVE {
        {
            key = "preheater#preheaterValve";
            data_type = DataTypeNumber.class;
            read_command = 0xe1;
            read_reply_command = 0xe2;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    PREHEATER_FROST_PROTECT {
        {
            key = "preheater#preheaterFrostProtect";
            data_type = DataTypeBoolean.class;
            read_command = 0xe1;
            read_reply_command = 0xe2;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    PREHEATER_HEATING {
        {
            key = "preheater#preheaterHeating";
            data_type = DataTypeBoolean.class;
            read_command = 0xe1;
            read_reply_command = 0xe2;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    PREHEATER_FROST_TIME {
        {
            key = "preheater#preheaterFrostTime";
            data_type = DataTypeNumber.class;
            read_command = 0xe1;
            read_reply_command = 0xe2;
            read_reply_data_pos = new int[] { 3, 4 };
        }
    },

    PREHEATER_OPTION {
        {
            key = "preheater#preheaterSafety";
            data_type = DataTypeNumber.class;
            read_command = 0xe1;
            read_reply_command = 0xe2;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    LEVEL0_TIME {
        {
            key = "times#level0Time";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 0, 1, 2 };
        }
    },

    LEVEL1_TIME {
        {
            key = "times#level1Time";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 3, 4, 5 };
        }
    },

    LEVEL2_TIME {
        {
            key = "times#level2Time";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 6, 7, 8 };
        }
    },

    LEVEL3_TIME {
        {
            key = "times#level3Time";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 17, 18, 19 };
        }
    },

    FREEZE_TIME {
        {
            key = "times#freezeTime";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 9, 10 };
        }
    },

    PREHEATER_TIME {
        {
            key = "times#preheaterTime";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 11, 12 };
        }
    },

    BYPASS_TIME {
        {
            key = "times#bypassTime";
            data_type = DataTypeNumber.class;
            read_command = 0xdd;
            read_reply_command = 0xde;
            read_reply_data_pos = new int[] { 13, 14 };
        }
    },

    IS_ANALOG1 {
        {
            key = "analog#isAnalog1";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x01 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 0;
            change_affected = new String[] { "analog#analog1Mode", "analog#analog1Negative", "analog#analog1Min",
                    "analog#analog1Max", "analog#analog1Value", "analog#analog1Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x01;
        }
    },

    IS_ANALOG2 {
        {
            key = "analog#isAnalog2";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x02 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 0;
            change_affected = new String[] { "analog#analog2Mode", "analog#analog2Negative", "analog#analog2Min",
                    "analog#analog2Max", "analog#analog2Value", "analog#analog2Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x02;
        }
    },

    IS_ANALOG3 {
        {
            key = "analog#isAnalog3";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x04 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 0;
            change_affected = new String[] { "analog#analog3Mode", "analog#analog3Negative", "analog#analog3Min",
                    "analog#analog3Max", "analog#analog3Value", "analog#analog3Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x04;
        }
    },

    IS_ANALOG4 {
        {
            key = "analog#isAnalog4";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x08 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 0;
            change_affected = new String[] { "analog#analog4Mode", "analog#analog4Negative", "analog#analog4Min",
                    "analog#analog4Max", "analog#analog4Value", "analog#analog4Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x08;
        }
    },

    IS_RF {
        {
            key = "analog#isRF";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x10 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 0;
            change_affected = new String[] { "analog#RFMode", "analog#RFNegative", "analog#RFMin", "analog#RFMax",
                    "analog#RFValue" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x10;
        }
    },

    ANALOG1_MODE {
        {
            key = "analog#analog1Mode";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x01 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 1;
            change_affected = new String[] { "analog#analog1Negative", "analog#analog1Min", "analog#analog1Max",
                    "analog#analog1Value", "analog#analog1Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x01;
        }
    },

    ANALOG2_MODE {
        {
            key = "analog#analog2Mode";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x02 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 1;
            change_affected = new String[] { "analog#analog2Negative", "analog#analog2Min", "analog#analog2Max",
                    "analog#analog2Value", "analog#analog2Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x02;
        }
    },

    ANALOG3_MODE {
        {
            key = "analog#analog3Mode";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x04 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 1;
            change_affected = new String[] { "analog#analog3Negative", "analog#analog3Min", "analog#analog3Max",
                    "analog#analog3Value", "analog#analog3Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x04;
        }
    },

    ANALOG4_MODE {
        {
            key = "analog#analog4Mode";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x08 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 1;
            change_affected = new String[] { "analog#analog4Negative", "analog#analog4Min", "analog#analog4Max",
                    "analog#analog4Value", "analog#analog4Volt" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x08;
        }
    },

    RF_MODE {
        {
            key = "analog#RFMode";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x10 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 1;
            change_affected = new String[] { "analog#RFNegative", "analog#RFMin", "analog#RFMax", "analog#RFValue" };
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x10;
        }
    },

    ANALOG1_NEGATIVE {
        {
            key = "analog#analog1Negative";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x01 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 2;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 2 };
            read_reply_data_bits = 0x01;
        }
    },

    ANALOG2_NEGATIVE {
        {
            key = "analog#analog2Negative";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x02 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 2;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 2 };
            read_reply_data_bits = 0x02;
        }
    },

    ANALOG3_NEGATIVE {
        {
            key = "analog#analog3Negative";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x04 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 2;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 2 };
            read_reply_data_bits = 0x04;
        }
    },

    ANALOG4_NEGATIVE {
        {
            key = "analog#analog4Negative";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x08 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 2;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 2 };
            read_reply_data_bits = 0x08;
        }
    },

    RF_NEGATIVE {
        {
            key = "analog#RFNegative";
            data_type = DataTypeBoolean.class;
            possible_values = new int[] { 0x10 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 2;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 2 };
            read_reply_data_bits = 0x10;
        }
    },

    ANALOG1_MIN {
        {
            key = "analog#analog1Min";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 3;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    ANALOG1_MAX {
        {
            key = "analog#analog1Max";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 4;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 4 };
        }
    },

    ANALOG1_VALUE {
        {
            key = "analog#analog1Value";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 5;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 5 };
        }
    },

    ANALOG2_MIN {
        {
            key = "analog#analog2Min";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 6;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 6 };
        }
    },

    ANALOG2_MAX {
        {
            key = "analog#analog2Max";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 7;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 7 };
        }
    },

    ANALOG2_VALUE {
        {
            key = "analog#analog2Value";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 8;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 8 };
        }
    },

    ANALOG3_MIN {
        {
            key = "analog#analog3Min";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 9;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 9 };
        }
    },

    ANALOG3_MAX {
        {
            key = "analog#analog3Max";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 10;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 10 };
        }
    },

    ANALOG3_VALUE {
        {
            key = "analog#analog3Value";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 11;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 11 };
        }
    },

    ANALOG4_MIN {
        {
            key = "analog#analog4Min";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 12;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 12 };
        }
    },

    ANALOG4_MAX {
        {
            key = "analog#analog4Max";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 13;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 13 };
        }
    },

    ANALOG4_VALUE {
        {
            key = "analog#analog4Value";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 14;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 14 };
        }
    },

    RF_MIN {
        {
            key = "analog#RFMin";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 15;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 15 };
        }
    },

    RF_MAX {
        {
            key = "analog#RFMax";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 16;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 16 };
        }
    },

    RF_VALUE {
        {
            key = "analog#RFValue";
            data_type = DataTypeNumber.class;
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 17;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 17 };
        }
    },

    ANALOG_MODE {
        {
            key = "analog#analogMode";
            data_type = DataTypeNumber.class;
            possible_values = new int[] { 0x00, 0x01 };
            change_command = 0x9f;
            change_data_size = 19;
            change_data_pos = 18;
            change_affected = new String[] {};
            read_command = 0x9d;
            read_reply_command = 0x9e;
            read_reply_data_pos = new int[] { 18 };
        }
    },

    ANALOG1_VOLT {
        {
            key = "analog#analog1Volt";
            data_type = DataTypeVolt.class;
            read_command = 0x13;
            read_reply_command = 0x14;
            read_reply_data_pos = new int[] { 0 };
        }
    },

    ANALOG2_VOLT {
        {
            key = "analog#analog2Volt";
            data_type = DataTypeVolt.class;
            read_command = 0x13;
            read_reply_command = 0x14;
            read_reply_data_pos = new int[] { 1 };
        }
    },

    ANALOG3_VOLT {
        {
            key = "analog#analog3Volt";
            data_type = DataTypeVolt.class;
            read_command = 0x13;
            read_reply_command = 0x14;
            read_reply_data_pos = new int[] { 2 };
        }
    },

    ANALOG4_VOLT {
        {
            key = "analog#analog4Volt";
            data_type = DataTypeVolt.class;
            read_command = 0x13;
            read_reply_command = 0x14;
            read_reply_data_pos = new int[] { 3 };
        }
    },

    IS_L1_SWITCH {
        {
            key = "inputs#isL1Switch";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x01;
        }
    },

    IS_L2_SWITCH {
        {
            key = "inputs#isL2Switch";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 0 };
            read_reply_data_bits = 0x02;
        }
    },

    IS_BATHROOM_SWITCH {
        {
            key = "inputs#isBathroomSwitch";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x01;
        }
    },

    IS_COOKERHOOD_SWITCH {
        {
            key = "inputs#isCookerhoodSwitch";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x02;
        }
    },

    IS_EXTERNAL_FILTER {
        {
            key = "inputs#isExternalFilter";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x04;
        }
    },

    IS_WTW {
        {
            key = "inputs#isWTW";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x08;
        }
    },

    IS_BATHROOM2_SWITCH {
        {
            key = "inputs#isBathroom2Switch";
            data_type = DataTypeBoolean.class;
            read_command = 0x03;
            read_reply_command = 0x04;
            read_reply_data_pos = new int[] { 1 };
            read_reply_data_bits = 0x10;
        }
    };

    Logger logger = LoggerFactory.getLogger(ComfoAirCommandType.class);
    String key;
    Class<? extends ComfoAirDataType> data_type;

    /*
     * Possible values
     */
    int[] possible_values;

    /*
     * Cmd code to change properties on the comfoair.
     */
    int change_command;
    /*
     * The size of the data block.
     */
    int change_data_size;
    /*
     * The byte inside the data block which holds the crucial value.
     */
    int change_data_pos;
    /*
     * Affected commands which should be refreshed after a successful change
     * command call.
     */
    String[] change_affected;

    /*
     * Command for reading properties.
     */
    int read_command;

    /*
     * ACK Command which identifies the matching response.
     */
    int read_reply_command;

    /*
     * The byte position inside the response data.
     */
    int[] read_reply_data_pos;

    /*
     * Bit mask for boolean response properties to identify a true value.
     */
    int read_reply_data_bits;

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
    public int[] getPossibleValues() {
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
    public int[] getGetReplyDataPos() {
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
    public static ComfoAirCommand getReadCommand(String key) {
        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);

        if (commandType != null) {
            Integer getCmd = commandType.read_command == 0 ? null : new Integer(commandType.read_command);
            Integer replyCmd = new Integer(commandType.read_reply_command);

            return new ComfoAirCommand(key, getCmd, replyCmd, new int[0], null, null);
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
    public static ComfoAirCommand getChangeCommand(String key, State value) {
        ComfoAirCommandType commandType = ComfoAirCommandType.getCommandTypeByKey(key);
        DecimalType decimalValue = value.as(DecimalType.class);

        if (commandType != null && decimalValue != null) {
            ComfoAirDataType dataType = commandType.getDataType();
            int[] data = dataType.convertFromState(value, commandType);
            int dataPossition = commandType.getChangeDataPos();
            int intValue = decimalValue.intValue();

            return new ComfoAirCommand(key, commandType.change_command, null, data, dataPossition, intValue);
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
        if (commandType.read_reply_command != 0) {
            Integer getCmd = commandType.read_command == 0 ? null : new Integer(commandType.read_command);
            Integer replyCmd = new Integer(commandType.read_reply_command);

            ComfoAirCommand command = new ComfoAirCommand(key, getCmd, replyCmd, new int[0], null, null);
            commands.put(command.getReplyCmd(), command);
        }

        for (String affectedKey : commandType.change_affected) {
            // refresh affected event keys only when they are used
            if (!usedKeys.contains(affectedKey)) {
                continue;
            }

            ComfoAirCommandType affectedCommandType = ComfoAirCommandType.getCommandTypeByKey(affectedKey);

            Integer getCmd = affectedCommandType.read_command == 0 ? null
                    : new Integer(affectedCommandType.read_command);
            Integer replyCmd = new Integer(affectedCommandType.read_reply_command);

            ComfoAirCommand command = commands.get(replyCmd);

            if (command == null) {
                command = new ComfoAirCommand(affectedKey, getCmd, replyCmd, new int[0], null, null);
                commands.put(command.getReplyCmd(), command);
            } else {
                command.addKey(affectedKey);
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

            Integer getCmd = entry.read_command == 0 ? null : new Integer(entry.read_command);
            Integer replyCmd = new Integer(entry.read_reply_command);

            ComfoAirCommand command = commands.get(replyCmd);

            if (command == null) {
                command = new ComfoAirCommand(entry.key, getCmd, replyCmd, new int[0], null, null);
                commands.put(command.getReplyCmd(), command);
            } else {
                command.addKey(entry.key);
            }
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
    public static ComfoAirCommandType getCommandTypeByKey(String key) {
        for (ComfoAirCommandType entry : values()) {
            if (entry.key.equals(key)) {
                return entry;
            }
        }
        return null;
    }

}
