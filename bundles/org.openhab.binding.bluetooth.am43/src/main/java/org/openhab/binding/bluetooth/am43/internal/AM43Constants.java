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
package org.openhab.binding.bluetooth.am43.internal;

import java.util.UUID;

/**
 * The {@link AM43Constants} class defines common constants, which are used to communicate
 * with AM43 blinds drive motor
 *
 * @author Connor Petty - Initial contribution
 */
public class AM43Constants {

    public static final int Baiye_TYPE = 1;

    public static final int Chuizhi_TYPE = 2;

    public static final int Juanlian_TYPE = 3;

    public static final int Fengchao_TYPE = 4;

    public static final int Rousha_TYPE = 5;

    public static final int Xianggelila_TYPE = 8;

    public static final byte Command_Foot_Verification_Failure = ((byte) 206);

    public static final byte Command_Foot_Verification_Success = ((byte) 49);

    public static final byte[] Command_Head_Tag = { 0, (byte) 255, 0, 0 };

    public static final byte Command_Head_Type_Control_Direct = ((byte) 10);

    public static final byte Command_Head_Type_Control_Percent = ((byte) 13);

    public static final byte Command_Head_Type_LimitOrReset = ((byte) 34);

    public static final byte Command_Head_Type_PassWord = ((byte) 23);

    public static final byte Command_Head_Type_PassWord_Change = ((byte) 24);

    public static final byte Command_Head_Type_Season = ((byte) 22);

    public static final byte Command_Head_Type_SendTime = ((byte) 20);

    public static final byte Command_Head_Type_Setting_Frequently = (byte) 17;

    public static final byte Command_Head_Type_Setting_findAll = (byte) 167;

    public static final byte Command_Head_Type_Timing = ((byte) 21);

    public static final byte Command_Head_Type_Battery_Level = (byte) 162;

    public static final byte Command_Head_Value = ((byte) 154);

    public static final byte Command_Head_Type_Light_Level = (byte) 170;

    public static final byte Command_Notify_Content_Failure = (byte) 165;

    public static final byte Command_Notify_Content_Limit_Set_Exit = ((byte) 92);

    public static final byte Command_Notify_Content_Limit_Set_Failure = ((byte) 181);

    public static final byte Command_Notify_Content_Limit_Set_Login = (byte) 90;

    public static final byte Command_Notify_Content_Limit_Set_Succese = ((byte) 91);

    public static final byte Command_Notify_Content_Limit_Set_TimeOut = (byte) 165;

    public static final byte Command_Notify_Content_Reset_Succese = ((byte) 197);

    public static final byte Command_Notify_Content_Season_AllClose = 0;

    public static final byte Command_Notify_Content_Season_Open2Open_Close2Close = ((byte) 16);

    public static final byte Command_Notify_Content_Season_Open2Open_Close2Open = (byte) 17;

    public static final byte Command_Notify_Content_Season_Open2Stop_Close2Open = (byte) 1;

    public static final byte Command_Notify_Content_Success = (byte) 90;

    public static final byte Command_Notify_Head_Type_Fault = ((byte) 166);

    public static final byte Command_Notify_Head_Type_Find_Normal = (byte) 167;

    public static final byte Command_Notify_Head_Type_Find_Season = ((byte) 169);

    public static final byte Command_Notify_Head_Type_Find_Timing = ((byte) 168);

    public static final byte Command_Notify_Head_Type_Move = ((byte) 161);

    public static final byte Command_Notify_Head_Type_NewName = ((byte) 53);

    public static final byte Command_Notify_Head_Type_Speed = ((byte) 163);

    public static final byte Command_Notify_Head_Type_Battery_Level = (byte) 162;

    public static final byte Command_Notify_Head_Type_Light_Level = (byte) 170;

    public static final byte Command_Send_Content_Control_Close = ((byte) 238);

    public static final byte Command_Send_Content_Control_Open = ((byte) 221);

    public static final byte Command_Send_Content_Control_Stop = ((byte) 204);

    public static final byte Command_Send_Content_findLightLevel = (byte) 1;

    public static final byte Command_Send_Content_findBatteryLevel = (byte) 1;

    public static final byte Command_Send_Content_initLimit = 0;

    public static final byte Command_Send_Content_saveLimit = ((byte) 32);

    public static final byte Command_Send_Content_Type_Setting_findAll = (byte) 1;

    public static final byte Conmmand_Send_Content_exitLimit = ((byte) 64);

    public static final UUID RX_CHAR_UUID = UUID.fromString("0000fe51-0000-1000-8000-00805f9b34fb");

    public static final UUID RX_SERVICE_UUID = UUID.fromString("0000fe50-0000-1000-8000-00805f9b34fb");

    public static final UUID TX_CHAR_UUID = UUID.fromString("0000fe51-0000-1000-8000-00805f9b34fb");

    public static final Integer[] deviceTypeList = { Integer.valueOf(Baiye_TYPE), Integer.valueOf(Chuizhi_TYPE),
            Integer.valueOf(Fengchao_TYPE), Integer.valueOf(Xianggelila_TYPE), Integer.valueOf(Juanlian_TYPE),
            Integer.valueOf(Rousha_TYPE) };

}
