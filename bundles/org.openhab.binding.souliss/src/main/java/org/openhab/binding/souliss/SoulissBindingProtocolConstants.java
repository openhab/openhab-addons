/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class contains Souliss constants. Original version is taken from
 * SoulissApp. For scope of this binding not all constants are used.
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 * @author Luca Remigio
 * @since 2.0.0
 */
@NonNullByDefault
public class SoulissBindingProtocolConstants {

    public static final String TAG = "SoulissApp:Typicals";

    /**
     * /** // Defines for Typicals C LIBRARY
     *
     * #define Souliss_T31 31 // Temperature control #define Souliss_T41 41 //
     * Anti-theft integration (Main) #define Souliss_T42 42 // Anti-theft
     * integration (Peer)
     */
    public static final byte Souliss_T_empty = 0;
    public static final byte Souliss_T_related = (byte) 0xFF;

    public static final byte Souliss_TService_NODE_HEALTY = (byte) 0x98;
    public static final byte Souliss_TService_NODE_TIMESTAMP = (byte) 0x99;

    public static final int Souliss_TService_NODE_HEALTY_VIRTUAL_SLOT = 998;
    public static final int Souliss_TService_NODE_TIMESTAMP_VIRTUAL_SLOT = 999;

    // Defines for Typicals
    public static final byte Souliss_T11 = 0x11;
    public static final byte Souliss_T12 = 0x12;
    public static final byte Souliss_T13 = 0x13;
    public static final byte Souliss_T14 = 0x14;
    public static final byte Souliss_T1n_RGB = 0x15;// RGB Light
    public static final byte Souliss_T16 = 0x16;
    public static final byte Souliss_T18 = 0x18;
    public static final byte Souliss_T19 = 0x19;
    public static final byte Souliss_T1A = 0x1A;

    public static final byte Souliss_T21 = 0x21;// Motorized devices with limit
                                                // switches
    public static final byte Souliss_T22 = 0x22;// Motorized devices with limit
                                                // switches and middle
                                                // position
    public static final byte Souliss_T31 = 0x31;
    public static final byte Souliss_T32_IrCom_AirCon = 0x32;

    public static final byte Souliss_T42_Antitheft_Group = 0x40; // Anti-theft
                                                                 // group
                                                                 // (used w/
                                                                 // massive
                                                                 // commands)
    public static final byte Souliss_T41_Antitheft_Main = 0x41; // Anti-theft
                                                                // integration
                                                                // (Main)
    public static final byte Souliss_T42_Antitheft_Peer = 0x42; // Anti-theft
                                                                // integration
                                                                // (Peer)

    public static final byte Souliss_T51 = 0x51;
    public static final byte Souliss_T52_TemperatureSensor = 0x52;
    public static final byte Souliss_T53_HumiditySensor = 0x53;
    public static final byte Souliss_T54_LuxSensor = 0x54;
    public static final byte Souliss_T55_VoltageSensor = 0x55;
    public static final byte Souliss_T56_CurrentSensor = 0x56;
    public static final byte Souliss_T57_PowerSensor = 0x57;
    public static final byte Souliss_T58_PressureSensor = 0x58;

    public static final byte Souliss_T61 = 0x61;
    public static final byte Souliss_T62_TemperatureSensor = 0x62;
    public static final byte Souliss_T63_HumiditySensor = 0x63;
    public static final byte Souliss_T64_LuxSensor = 0x64;
    public static final byte Souliss_T65_VoltageSensor = 0x65;
    public static final byte Souliss_T66_CurrentSensor = 0x66;
    public static final byte Souliss_T67_PowerSensor = 0x67;
    public static final byte Souliss_T68_PressureSensor = 0x68;

    public static final byte Souliss_Topics = 0x72;

    // customized (remote) AirCon commands
    public static final int Souliss_T_IrCom_AirCon_Pow_On = 0x8FFE;
    public static final int Souliss_T_IrCom_AirCon_Pow_Auto_20 = 0x8FFD;
    public static final int Souliss_T_IrCom_AirCon_Pow_Auto_24 = 0x8FFE;
    public static final int Souliss_T_IrCom_AirCon_Pow_Cool_18 = 0x807B;
    public static final int Souliss_T_IrCom_AirCon_Pow_Cool_22 = 0x8079;
    public static final int Souliss_T_IrCom_AirCon_Pow_Cool_26 = 0x807A;
    public static final int Souliss_T_IrCom_AirCon_Pow_Fan = 0x8733;
    public static final int Souliss_T_IrCom_AirCon_Pow_Dry = 0x87BE;
    public static final int Souliss_T_IrCom_AirCon_Pow_Off = 0x70FE;

    // Souliss Aircon Temperature

    public static final byte Souliss_T_IrCom_AirCon_temp_16C = 0xF;
    public static final byte Souliss_T_IrCom_AirCon_temp_17C = 0x7;
    public static final byte Souliss_T_IrCom_AirCon_temp_18C = 0xB;
    public static final byte Souliss_T_IrCom_AirCon_temp_19C = 0x3;
    public static final byte Souliss_T_IrCom_AirCon_temp_20C = 0xD;
    public static final byte Souliss_T_IrCom_AirCon_temp_21C = 0x5;
    public static final byte Souliss_T_IrCom_AirCon_temp_22C = 0x9;
    public static final byte Souliss_T_IrCom_AirCon_temp_23C = 0x1;
    public static final byte Souliss_T_IrCom_AirCon_temp_24C = 0xE;
    public static final byte Souliss_T_IrCom_AirCon_temp_25C = 0x6;
    public static final byte Souliss_T_IrCom_AirCon_temp_26C = 0xA;
    public static final byte Souliss_T_IrCom_AirCon_temp_27C = 0x2;
    public static final byte Souliss_T_IrCom_AirCon_temp_28C = 0xC;
    public static final byte Souliss_T_IrCom_AirCon_temp_29C = 0x4;
    public static final byte Souliss_T_IrCom_AirCon_temp_30C = 0x8;

    // Souliss conditioner Function

    public static final byte Souliss_T_IrCom_AirCon_Fun_Auto = 0xF;
    public static final byte Souliss_T_IrCom_AirCon_Fun_Dry = 0xB;
    public static final byte Souliss_T_IrCom_AirCon_Fun_Fan = 0x3;
    public static final byte Souliss_T_IrCom_AirCon_Fun_Heat = 0xD;
    public static final byte Souliss_T_IrCom_AirCon_Fun_Cool = 0x7;

    public static final byte Souliss_T_IrCom_AirCon_Fan_Auto = 0x7;
    public static final byte Souliss_T_IrCom_AirCon_Fan_High = 0x2;
    public static final byte Souliss_T_IrCom_AirCon_Fan_Medium = 0x6;
    public static final byte Souliss_T_IrCom_AirCon_Fan_Low = 0x5;

    // optional switches. May be used to toggle
    // custom aircon functions as air deflector, ionizer, turbomode, etc.
    public static final byte Souliss_T_IrCom_AirCon_Opt1 = 0x2D;
    public static final byte Souliss_T_IrCom_AirCon_Opt2 = 0x77;

    public static final byte Souliss_T_IrCom_AirCon_Reset = 0x00;

    // General defines for T1n
    public static final byte Souliss_T1n_ToogleCmd = 0x01;
    public static final byte Souliss_T1n_OnCmd = 0x02;
    public static final byte Souliss_T1n_OffCmd = 0x04;
    public static final byte Souliss_T1n_AutoCmd = 0x08;
    public static final byte Souliss_T1n_Timed = 0x30;
    public static final byte Souliss_T1n_RstCmd = 0x00;
    public static final byte Souliss_T1n_OnCoil = 0x01;
    public static final byte Souliss_T1n_OffCoil = 0x00;
    public static final byte Souliss_T1n_OnCoil_Auto = (byte) 0xF1;
    public static final byte Souliss_T1n_OffCoil_Auto = (byte) 0xF0;
    public static final byte Souliss_T1n_Set = 0x22; // Set a state

    public static final byte Souliss_T1n_BrightUp = 0x10; // Increase Light
    public static final byte Souliss_T1n_BrightDown = 0x20; // Decrease Light
    public static final byte Souliss_T1n_Flash = 0x21; // Flash Light

    public static final byte Souliss_T1n_OnFeedback = 0x23;
    public static final byte Souliss_T1n_OffFeedback = 0x24;
    public static final String Souliss_T12_Use_Of_Slot_AUTOMODE = "automode";
    public static final String Souliss_T12_Use_Of_Slot_SWITCH = "switch";

    public static final long Souliss_T16_Red = 0x22FF0000; // Set a state
    public static final long Souliss_T16_Green = 0x2200FF00;
    public static final long Souliss_T16_Blue = 0x220000FF;
    public static final long Souliss_T18_Pulse = 0xA1;
    /*
     * IR RGB Typical
     */
    public static final byte Souliss_T1n_RGB_OnCmd = 0x1;
    public static final byte Souliss_T1n_RGB_OffCmd = 0x9;

    // Souliss RGB main colours
    public static final byte Souliss_T1n_RGB_R = 0x2;
    public static final byte Souliss_T1n_RGB_G = 0x3;
    public static final byte Souliss_T1n_RGB_B = 0x4;
    public static final byte Souliss_T1n_RGB_W = 0x5;
    // Souliss RGB Controls
    public static final byte Souliss_T_IrCom_RGB_bright_up = 0x6;
    public static final byte Souliss_T_IrCom_RGB_bright_down = 0x7;
    // MODES;
    public static final byte Souliss_T_IrCom_RGB_mode_flash = (byte) 0xA1;
    public static final byte Souliss_T_IrCom_RGB_mode_strobe = (byte) 0xA2;
    public static final byte Souliss_T_IrCom_RGB_mode_fade = (byte) 0xA3;
    public static final byte Souliss_T_IrCom_RGB_mode_smooth = (byte) 0xA4;

    public static final byte Souliss_T1n_RGB_R2 = (byte) 0xB1;
    public static final byte Souliss_T1n_RGB_R3 = (byte) 0xB2;
    public static final byte Souliss_T1n_RGB_R4 = (byte) 0xB3;
    public static final byte Souliss_T1n_RGB_R5 = (byte) 0xB4;
    public static final byte Souliss_T1n_RGB_G2 = (byte) 0xC1;
    public static final byte Souliss_T1n_RGB_G3 = (byte) 0xC2;
    public static final byte Souliss_T1n_RGB_G4 = (byte) 0xC3;
    public static final byte Souliss_T1n_RGB_G5 = (byte) 0xC4;
    public static final byte Souliss_T1n_RGB_B2 = (byte) 0xD1;
    public static final byte Souliss_T1n_RGB_B3 = (byte) 0xD2;
    public static final byte Souliss_T1n_RGB_B4 = (byte) 0xD3;
    public static final byte Souliss_T1n_RGB_B5 = (byte) 0xD4;

    public static final byte Souliss_T1n_RGB_RstCmd = 0x00;

    // Defines for Typical 2n
    public static final byte Souliss_T2n_CloseCmd = 0x01;
    public static final byte Souliss_T2n_OpenCmd = 0x02;
    public static final byte Souliss_T2n_StopCmd = 0x04;
    public static final byte Souliss_T2n_CloseCmd_Local = 0x08; // Close Command (only from local pushbutton)
    public static final byte Souliss_T2n_OpenCmd_Local = 0x10; // Open Command (only from local pushbutton)
    public static final byte Souliss_T2n_ToogleCmd = 0x08;
    public static final byte Souliss_T2n_RstCmd = 0x00;
    public static final byte Souliss_T2n_Timer_Val = (byte) 0xC0; // Timer set value
    public static final byte Souliss_T2n_Timer_Off = (byte) 0xA0; // Timer expired value
    public static final byte Souliss_T2n_TimedStop_Val = (byte) 0xC2; // Timed stop value
    public static final byte Souliss_T2n_TimedStop_Off = (byte) 0xC0; // Timed stop exipred value
    public static final byte Souliss_T2n_LimSwitch_Close = 0x14;
    public static final byte Souliss_T2n_LimSwitch_Open = 0x16;
    public static final byte Souliss_T2n_State_Close = 0x08; // Close Feedback from Limit Switch
    public static final byte Souliss_T2n_State_Open = 0x10; // Open Feedback from Limit Switch

    public static final byte Souliss_T2n_NoLimSwitch = 0x20;
    public static final byte Souliss_T2n_Coil_Close = 0x01;
    public static final byte Souliss_T2n_Coil_Open = 0x02;
    public static final byte Souliss_T2n_Coil_Stop = 0x03;
    public static final byte Souliss_T2n_Coil_Off = 0x00;

    // General defines for T3n
    public static final String Souliss_T31_Use_Of_Slot_SETPOINT = "setpoint";
    public static final String Souliss_T31_Use_Of_Slot_MEASURED = "measured";
    public static final String Souliss_T31_Use_Of_Slot_SETASMEASURED = "setasmeasured";

    public static final byte Souliss_T31_Use_Of_Slot_SETPOINT_COMMAND = 0x0C;
    public static final byte Souliss_T31_Use_Of_Slot_HEATING = 0x05;
    public static final byte Souliss_T31_Use_Of_Slot_COOLING = 0x04;
    public static final String Souliss_T31_Use_Of_Slot_HEATING_COOLING = "heatingcooling";
    public static final byte Souliss_T31_Use_Of_Slot_FANOFF = 0x06;
    public static final byte Souliss_T31_Use_Of_Slot_FANLOW = 0x07;
    public static final byte Souliss_T31_Use_Of_Slot_FANMED = 0x08;
    public static final byte Souliss_T31_Use_Of_Slot_FANHIGH = 0x09;
    public static final byte Souliss_T31_Use_Of_Slot_FANAUTOMODE = 0x0A;
    public static final String Souliss_T31_Use_Of_Slot_POWER = "power";

    public static final byte Souliss_T3n_InSetPoint = 0x01;
    public static final byte Souliss_T3n_OutSetPoint = 0x02;
    public static final byte Souliss_T3n_AsMeasured = 0x03;
    public static final byte Souliss_T3n_Cooling = 0x04;
    public static final byte Souliss_T3n_Heating = 0x05;
    public static final byte Souliss_T3n_FanOff = 0x06;
    public static final byte Souliss_T3n_FanLow = 0x07;
    public static final byte Souliss_T3n_FanMed = 0x08;
    public static final byte Souliss_T3n_FanHigh = 0x09;
    public static final byte Souliss_T3n_FanAuto = 0x0A;
    public static final byte Souliss_T3n_FanManual = 0x0B;
    public static final byte Souliss_T3n_SetTemp = 0x0C;
    public static final byte Souliss_T3n_ShutDown = 0x0D;

    public static final String Souliss_T3n_HeatingOn = "0x02";
    public static final String Souliss_T3n_CoolingOn = "0x03";
    public static final String Souliss_T3n_FanOn1 = "0x08";
    public static final String Souliss_T3n_FanOn2 = "0x10";
    public static final String Souliss_T3n_FanOn3 = "0x20";

    // General defines for T4n
    public static final byte Souliss_T4n_Alarm = 0x01; // Alarm Condition
                                                       // Detected (Input)
    public static final byte Souliss_T4n_RstCmd = 0x00;
    public static final byte Souliss_T4n_ReArm = 0x03; // Silence and Arm
                                                       // Command
    public static final byte Souliss_T4n_NotArmed = 0x04; // Anti-theft not
                                                          // Armed Command
    public static final byte Souliss_T4n_Armed = 0x05; // Anti-theft Armed
                                                       // Command
    public static final byte Souliss_T4n_Antitheft = 0x01; // Anti-theft Armed
                                                           // Feedback
    public static final byte Souliss_T4n_NoAntitheft = 0x00; // Anti-theft not
                                                             // Armed
                                                             // Feedback
    public static final byte Souliss_T4n_InAlarm = 0x03; // Anti-theft in Alarm

    public static final byte Souliss_RstCmd = 0x00;
    public static final byte Souliss_NOTTRIGGED = 0x00;
    public static final byte Souliss_TRIGGED = 0x01;

    // Defines for current sensor
    public static final byte Souliss_T_CurrentSensor = 0x65;

    // REMOVE THESE
    public static final byte Souliss_T_TemperatureSensor = 0x67;
    public static final byte Souliss_T_TemperatureSensor_refresh = 0x02;

    public static final byte Souliss_T_HumiditySensor = 0x69;
    public static final byte Souliss_T_HumiditySensor_refresh = 0x03;

}
