/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * the class {@link SoulissProtocolConstants} class contains Souliss constants. Original version is taken from
 * SoulissApp. For scope of this binding not all constants are used.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 * @author Alessandro Del Pex - soulissapp
 * @author Tonino Fazio - @since 1.7.0
 * @author Luca Remigio - @since 2.0.0
 *
 */

@NonNullByDefault
public final class SoulissProtocolConstants {

    public static final String TAG = "SoulissApp:Typicals";

    /**
     * /** // Defines for Typicals C LIBRARY
     *
     * #define SOULISS_T31 31 // Temperature control #define Souliss_T41 41 //
     * Anti-theft integration (Main) #define Souliss_T42 42 // Anti-theft
     * integration (Peer)
     */
    public static final byte SOULISS_T_EMPTY = 0;
    public static final byte SOULISS_T_RELATED = (byte) 0xFF;

    public static final byte SOULISS_TSERVICE_NODE_HEALTHY = (byte) 0x98;
    public static final byte SOULISS_TSERVICE_NODE_TIMESTAMP = (byte) 0x99;

    public static final int SOULISS_TSERVICE_NODE_HEALTHY_VIRTUAL_SLOT = 998;
    public static final int SOULISS_TSERVICE_NODE_TIMESTAMP_VIRTUAL_SLOT = 999;

    // Defines for Typicals
    public static final byte SOULISS_T11 = 0x11;
    public static final byte SOULISS_T12 = 0x12;
    public static final byte SOULISS_T13 = 0x13;
    public static final byte SOULISS_T14 = 0x14;
    // RGB Light
    public static final byte SOULISS_T1N_RGB = 0x15;
    public static final byte SOULISS_T16 = 0x16;
    public static final byte SOULISS_T18 = 0x18;
    public static final byte SOULISS_T19 = 0x19;
    public static final byte SOULISS_T1A = 0x1A;

    // Motorized devices with limit switches
    public static final byte SOULISS_T21 = 0x21;
    // Motorized devices with limit switches and middle position
    public static final byte SOULISS_T22 = 0x22;
    public static final byte SOULISS_T31 = 0x31;
    public static final byte SOULISS_T32_IRCOM_AIRCON = 0x32;

    // Anti-theft group (used with massive commands)
    public static final byte SOULISS_T42_ANTITHEFT_GROUP = 0x40;
    // Anti-theft integration (Main)
    public static final byte SOULISS_T41_ANTITHEFT_MAIN = 0x41;
    // Anti-theft integration (Peer)
    public static final byte SOULISS_T42_ANTITHEFT_PEER = 0x42;

    public static final byte SOULISS_T51 = 0x51;
    public static final byte SOULISS_T52_TEMPERATURE_SENSOR = 0x52;
    public static final byte SOULISS_T53_HUMIDITY_SENSOR = 0x53;
    public static final byte SOULISS_T54_LUX_SENSOR = 0x54;
    public static final byte SOULISS_T55_VOLTAGE_SENSOR = 0x55;
    public static final byte SOULISS_T56_CURRENT_SENSOR = 0x56;
    public static final byte SOULISS_T57_POWER_SENSOR = 0x57;
    public static final byte SOULISS_T58_PRESSURE_SENSOR = 0x58;

    public static final byte SOULISS_T61 = 0x61;
    public static final byte SOULISS_T62_TEMPERATURE_SENSOR = 0x62;
    public static final byte SOULISS_T63_HUMIDITY_SENSOR = 0x63;
    public static final byte SOULISS_T64_LUX_SENSOR = 0x64;
    public static final byte SOULISS_T65_VOLTAGE_SENSOR = 0x65;
    public static final byte SOULISS_T66_CURRENT_SENSOR = 0x66;
    public static final byte SOULISS_T67_POWER_SENSOR = 0x67;
    public static final byte SOULISS_T68_PRESSURE_SENSOR = 0x68;

    public static final byte SOULISS_TOPICS = 0x72;

    // customized (remote) AirCon commands
    public static final int SOULISS_T_IRCOM_AIRCON_POW_ON = 0x8FFE;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_AUTO_20 = 0x8FFD;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_AUTO_24 = 0x8FFE;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_COOL_18 = 0x807B;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_COOL_22 = 0x8079;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_COOL_26 = 0x807A;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_FAN = 0x8733;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_DRY = 0x87BE;
    public static final int SOULISS_T_IRCOM_AIRCON_POW_OFF = 0x70FE;

    // Souliss Aircon Temperature

    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_16C = 0xF;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_17C = 0x7;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_18C = 0xB;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_19C = 0x3;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_20C = 0xD;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_21C = 0x5;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_22C = 0x9;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_23C = 0x1;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_24C = 0xE;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_25C = 0x6;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_26C = 0xA;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_27C = 0x2;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_28C = 0xC;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_29C = 0x4;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_30C = 0x8;

    // Souliss conditioner Function

    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FUN_AAUTO = 0xF;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FUN_DRY = 0xB;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FUN_FAN = 0x3;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FUN_HEAT = 0xD;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FUN_COOL = 0x7;

    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FAN_AUTO = 0x7;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FAN_HIGH = 0x2;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FAN_MEDIUM = 0x6;
    public static final byte SOULISS_T_IRCOM_AIRCON_TEMP_FAN_LOW = 0x5;

    // optional switches. May be used to toggle
    // custom aircon functions as air deflector, ionizer, turbomode, etc.
    public static final byte SOULISS_T_IRCOM_AIRCON_OPT1 = 0x2D;
    public static final byte SOULISS_T_IRCOM_AIRCON_OPT2 = 0x77;

    public static final byte SOULISS_T_IRCOM_AIRCON_RESET = 0x00;

    // General defines for T1n
    public static final byte SOULISS_T1N_TOGGLE_CMD = 0x01;
    public static final byte SOULISS_T1N_ON_CMD = 0x02;
    public static final byte SOULISS_T1N_OFF_CMD = 0x04;
    public static final byte SOULISS_T1N_AUTO_CMD = 0x08;
    public static final byte SOULISS_T1N_TIMED = 0x30;
    public static final byte SOULISS_T1N_RST_CMD = 0x00;
    public static final byte SOULISS_T1N_ON_COIL = 0x01;
    public static final byte SOULISS_T1N_OFF_COIL = 0x00;
    public static final byte SOULISS_T1N_ON_COIL_AUTO = (byte) 0xF1;
    public static final byte SOULISS_T1N_OFF_COIL_AUTO = (byte) 0xF0;
    // Set a state
    public static final byte SOULISS_T1N_SET = 0x22;

    // Increase Light
    public static final byte SOULISS_T1N_BRIGHT_UP = 0x10;
    // Decrease Light
    public static final byte SOULISS_T1N_BRIGHT_DOWN = 0x20;
    // Flash Light
    public static final byte SOULISS_T1N_FLASH = 0x21;

    public static final byte SOULISS_T1N_ON_FEEDBACK = 0x23;
    public static final byte SOULISS_T1N_OFF_FEEDBACK = 0x24;
    public static final String SOULISS_T12_USE_OF_SLOT_AUTO_MODE = "autoMode";
    public static final String SOULISS_T12_USE_OF_SLOT_SWITCH = "switch";

    // Set a state
    public static final long SOULISS_T16_RED = 0x22FF0000;
    public static final long SOULISS_T16_GREEN = 0x2200FF00;
    public static final long SOULISS_T16_BLUE = 0x220000FF;
    public static final long SOULISS_T18_PULSE = 0xA1;
    /*
     * IR RGB Typical
     */
    public static final byte SOULISS_T1N_RGB_ON_CMD = 0x1;
    public static final byte SOULISS_T1N_RGB_OFF_CMD = 0x9;

    // Souliss RGB main colours
    public static final byte SOULISS_T1N_RGB_R = 0x2;
    public static final byte SOULISS_T1N_RGB_G = 0x3;
    public static final byte SOULISS_T1N_RGB_B = 0x4;
    public static final byte SOULISS_T1N_RGB_W = 0x5;
    // Souliss RGB Controls
    public static final byte SOULISS_T_IRCOM_RGB_BRIGHT_UP = 0x6;
    public static final byte SOULISS_T_IRCOM_RGB_BRIGHT_DOWN = 0x7;
    // MODES
    public static final byte SOULISS_T_IRCOM_RGB_MODE_FLASH = (byte) 0xA1;
    public static final byte SOULISS_T_IRCOM_RGB_MODE_STROBE = (byte) 0xA2;
    public static final byte SOULISS_T_IRCOM_RGB_MODE_FADE = (byte) 0xA3;
    public static final byte SOULISS_T_IRCOM_RGB_MODE_SMOOTH = (byte) 0xA4;

    public static final byte SOULISS_T1N_RGB_R2 = (byte) 0xB1;
    public static final byte SOULISS_T1N_RGB_R3 = (byte) 0xB2;
    public static final byte SOULISS_T1N_RGB_R4 = (byte) 0xB3;
    public static final byte SOULISS_T1N_RGB_R5 = (byte) 0xB4;
    public static final byte SOULISS_T1N_RGB_G2 = (byte) 0xC1;
    public static final byte SOULISS_T1N_RGB_G3 = (byte) 0xC2;
    public static final byte SOULISS_T1N_RGB_G4 = (byte) 0xC3;
    public static final byte SOULISS_T1N_RGB_G5 = (byte) 0xC4;
    public static final byte SOULISS_T1N_RGB_B2 = (byte) 0xD1;
    public static final byte SOULISS_T1N_RGB_B3 = (byte) 0xD2;
    public static final byte SOULISS_T1N_RGB_B4 = (byte) 0xD3;
    public static final byte SOULISS_T1N_RGB_B5 = (byte) 0xD4;

    public static final byte SOULISS_T1N_RGB_RST_CMD = 0x00;

    // Defines for Typical 2n
    public static final byte SOULISS_T2N_CLOSE_CMD = 0x01;
    public static final byte SOULISS_T2N_OPEN_CMD = 0x02;
    public static final byte SOULISS_T2N_STOP_CMD = 0x04;
    // Close Command (only from local pushbutton)
    public static final byte SOULISS_T2N_CLOSE_CMD_LOCAL = 0x08;
    // Open Command (only from local pushbutton)
    public static final byte SOULISS_T2N_OPEN_CMD_LOCAL = 0x10;
    public static final byte SOULISS_T2N_TOGGLE_CMD = 0x08;
    public static final byte SOULISS_T2N_RST_CMD = 0x00;
    // Timer set value
    public static final byte SOULISS_T2N_TIMER_VAL = (byte) 0xC0;
    // Timer expired value
    public static final byte SOULISS_T2N_TIMER_OFF = (byte) 0xA0;
    // Timed stop value
    public static final byte SOULISS_T2N_TIMEDSTOP_VAL = (byte) 0xC2;
    // Timed stop exipred value
    public static final byte SOULISS_T2N_TIMEDSTOP_OFF = (byte) 0xC0;
    public static final byte SOULISS_T2N_LIMSWITCH_CLOSE = 0x14;
    public static final byte SOULISS_T2N_LIMSWITCH_OPEN = 0x16;
    // Close Feedback from Limit Switch
    public static final byte SOULISS_T2N_STATE_CLOSE = 0x08;
    // Open Feedback from Limit Switch
    public static final byte SOULISS_T2N_STATE_OPEN = 0x10;

    public static final byte SOULISS_T2N_NOLIMSWITCH = 0x20;
    public static final byte SOULISS_T2N_COIL_CLOSE = 0x01;
    public static final byte SOULISS_T2N_COIL_OPEN = 0x02;
    public static final byte SOULISS_T2N_COIL_STOP = 0x03;
    public static final byte SOULISS_T2N_COIL_OFF = 0x00;

    // General defines for T3n
    public static final String SOULISS_T31_USE_OF_SLOT_SETPOINT = "setPoint";
    public static final String SOULISS_T31_USE_OF_SLOT_MEASURED = "measured";
    public static final String SOULISS_T31_USE_OF_SLOT_SETASMEASURED = "setAsMeasured";

    public static final byte SOULISS_T31_USE_OF_SLOT_SETPOINT_COMMAND = 0x0C;
    public static final byte SOULISS_T31_USE_OF_SLOT_HEATING = 0x05;
    public static final byte SOULISS_T31_USE_OF_SLOT_COOLING = 0x04;
    public static final String SOULISS_T31_USE_OF_SLOT_HEATING_COOLING = "heatingCooling";
    public static final byte SOULISS_T31_USE_OF_SLOT_FAN_OFF = 0x06;
    public static final byte SOULISS_T31_USE_OF_SLOT_FAN_LOW = 0x07;
    public static final byte SOULISS_T31_USE_OF_SLOT_FAN_MED = 0x08;
    public static final byte SOULISS_T31_USE_OF_SLOT_FAN_HIGH = 0x09;
    public static final byte SOULISS_T31_USE_OF_SLOT_FAN_AUTOMODE = 0x0A;
    public static final String SOULISS_T31_USE_OF_SLOT_POWER = "power";

    public static final byte SOULISS_T3N_IN_SETPOINT = 0x01;
    public static final byte SOULISS_T3N_OUT_SETPOINT = 0x02;
    public static final byte SOULISS_T3N_AS_MEASURED = 0x03;
    public static final byte SOULISS_T3N_COOLING = 0x04;
    public static final byte SOULISS_T3N_HEATING = 0x05;
    public static final byte SOULISS_T3N_FAN_OFF = 0x06;
    public static final byte SOULISS_T3N_FAN_LOW = 0x07;
    public static final byte SOULISS_T3N_FAN_MED = 0x08;
    public static final byte SOULISS_T3N_FAN_HIGH = 0x09;
    public static final byte SOULISS_T3N_FAN_AUTO = 0x0A;
    public static final byte SOULISS_T3N_FAN_MANUAL = 0x0B;
    public static final byte SOULISS_T3N_SET_TEMP = 0x0C;
    public static final byte SOULISS_T3N_SHUTDOWN = 0x0D;

    public static final String SOULISS_T3N_HEATING_ON = "0x02";
    public static final String SOULISS_T3N_COOLING_ON = "0x03";
    public static final String SOULISS_T3N_FAN_ON_1 = "0x08";
    public static final String SOULISS_T3N_FAN_ON_2 = "0x10";
    public static final String SOULISS_T3N_FAN_ON_3 = "0x20";

    // General defines for T4n

    // Alarm Condition Detected (Input)
    public static final byte SOULISS_T4N_ALARM = 0x01;
    public static final byte SOULISS_T4N_RST_CMD = 0x00;
    // Silence and Arm Command
    public static final byte SOULISS_T4N_REARM = 0x03;
    // Anti-theft not Armed Command
    public static final byte SOULISS_T4N_NOT_ARMED = 0x04;
    // Anti-theft Armed Command
    public static final byte SOULISS_T4N_ARMED = 0x05;
    // Anti-theft Armed Feedback
    public static final byte SOULISS_T4N_ANTITHEFT = 0x01;
    // Anti-theft not Armed Feedback
    public static final byte SOULISS_T4N_NO_ANTITHEFT = 0x00;
    // Anti-theft in Alarm
    public static final byte SOULISS_T4N_IN_ALARM = 0x03;

    public static final byte SOULISS_RST_CMD = 0x00;
    public static final byte SOULISS_NOT_TRIGGED = 0x00;
    public static final byte SOULISS_TRIGGED = 0x01;

    // Defines for current sensor
    public static final byte SOULISS_T_CURRENT_SENSOR = 0x65;

    // REMOVE THESE
    public static final byte SOULISS_T_TEMPERATURE_SENSOR = 0x67;
    public static final byte SOULISS_T_TEMPERATURE_SENSOR_REFRESH = 0x02;

    public static final byte SOULISS_T_HUMIDITY_SENSOR = 0x69;
    public static final byte SOULISS_T_HUMIDITY_SENSOR_REFRESH = 0x03;
}
