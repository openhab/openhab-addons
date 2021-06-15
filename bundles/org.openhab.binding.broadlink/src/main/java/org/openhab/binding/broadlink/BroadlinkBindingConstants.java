/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BroadlinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkBindingConstants {

    public static final String BINDING_ID = "broadlink";
    public static final String CAST_ADDRESS = "255.255.255.255";
    public static final int CAST_PORT = 80;
    public static final int BUFFER_LENGTH = 1024;
    public static final int TIMEOUT = 5000;
    public static final int DISCOVERY_TIMEOUT = 10;
    public static final int POLLING_TIME = 30;
    public static final ThingTypeUID THING_TYPE_RM = new ThingTypeUID("broadlink", "rm");
    public static final ThingTypeUID THING_TYPE_RM2 = new ThingTypeUID("broadlink", "rm2");
    public static final ThingTypeUID THING_TYPE_RM3 = new ThingTypeUID("broadlink", "rm3");
    public static final ThingTypeUID THING_TYPE_RM3Q = new ThingTypeUID("broadlink", "rm3q");
    public static final ThingTypeUID THING_TYPE_RM4 = new ThingTypeUID("broadlink", "rm4");
    public static final ThingTypeUID THING_TYPE_A1 = new ThingTypeUID("broadlink", "a1");
    public static final ThingTypeUID THING_TYPE_MP1 = new ThingTypeUID("broadlink", "mp1");
    public static final ThingTypeUID THING_TYPE_MP1_1K3S2U = new ThingTypeUID("broadlink", "mp1_1k3s2u");
    public static final ThingTypeUID THING_TYPE_MP2 = new ThingTypeUID("broadlink", "mp2");
    public static final ThingTypeUID THING_TYPE_SP1 = new ThingTypeUID("broadlink", "sp1");
    public static final ThingTypeUID THING_TYPE_SP2 = new ThingTypeUID("broadlink", "sp2");
    public static final ThingTypeUID THING_TYPE_SP3 = new ThingTypeUID("broadlink", "sp3");
    // public static final ThingTypeUID THING_TYPE_S1C = new ThingTypeUID("broadlink", "s1c");
    public static final ThingTypeUID THING_TYPE_PIR = new ThingTypeUID("broadlink", "s1p");
    public static final ThingTypeUID THING_TYPE_MAGNET = new ThingTypeUID("broadlink", "s1m");
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_NOISE = "noise";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_AIR = "air";
    public static final String CHANNEL_POWER_ON = "powerOn";
    public static final String CHANNEL_POWER_CONSUMPTION = "powerConsumption";
    public static final String CHANNEL_S1_POWER_ON = "s1powerOn";
    public static final String CHANNEL_S2_POWER_ON = "s2powerOn";
    public static final String CHANNEL_S3_POWER_ON = "s3powerOn";
    public static final String CHANNEL_S4_POWER_ON = "s4powerOn";
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_MOTION_OFF_TIMER = "motionOffTimer";
    public static final String CHANNEL_LAST_MOTION = "lastMotion";
    public static final String CHANNEL_IS_OPEN = "isOpen";
    public static final String CHANNEL_LAST_OPENED = "lastOpened";
    public static final String CHANNEL_OPEN_ALARM = "isOpenAlarm";
    public static final String CHANNEL_OPEN_ALARM_TIMER = "isOpenAlarmTimer";
    public static final String HOST = "ipAddress";
    public static final String PORT = "port";
    public static final String KEY = "key";
    public static final String ID = "id";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final String CONFIG_MAP_FILENAME = "mapFilename";
    public static final String AUTHORIZATION_KEY = "authorizationKey";
    public static final String IV = "iv";
    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_ID = "id";
    public static final String API_ID = "api_id";
    public static final String COMMAND = "command";
    public static final String LICENSE = "license";
    public static final String CODE = "code";
    public static final String MSG = "msg";
    public static final String STATUS = "status";
    public static final String TEMPERATURE = "temperature";
    public static final float INVALID_TEMPERATURE = -999F;
    public static final String RM = "Broadlink RM";
    public static final String RM1 = "Broadlink RM1";
    public static final String RM2 = "Broadlink RM2";
    public static final String RM3 = "Broadlink RM3";
    public static final String RM3Q = "Broadlink RM3 v11057";
    public static final String RM4 = "Broadlink RM4 / RM4 Mini / RM4 Pro";
    public static final String RMPROPHICOMM = "RMProPhicomm";
    public static final String RM2HOMEPLUS = "RM2HomePlus";
    public static final String RM2HOMEPLUSGDT = "RM2HomePlusGDT";
    public static final String RM2PROPLUS = "RM2ProPlus";
    public static final String RM2PROPLUS2 = "RM2ProPlus2";
    public static final String RM2PROPLUSBL = "RM2ProPlusBL";
    public static final String RMMINISHATE = "RMMiniShate";
    public static final String A1 = "Broadlink A1";
    public static final String TC1 = "Broadlink TC1";
    public static final String TC2 = "Broadlink TC2";
    public static final String MP1 = "Broadlink MP1";
    public static final String MP1_1K3S2U = "Broadlink MP1 1K3S2U";
    public static final String MP2 = "Broadlink MP2";
    public static final String SPMINI = "SPMini";
    public static final String SP1 = "SP1";
    public static final String SP2 = "SP2";
    public static final String SP3 = "SP3";
    public static final String SPMINI2 = "SPMini2";
    public static final String OEMSPMINI = "OEMSPMini";
    public static final String OEMSPMINI2 = "OEMSPMini2";
    public static final String SPMINIPLUS = "SPMiniPlus";
    public static final String S1C = "Smart One Controller";
    public static final String S1P = "Smart One PIR Sensor";
    public static final String S1M = "Smart One Magnet Sensor";
    public static final String NOT_SUPPORTED = "Not_supported";
    public static final byte CMD_COMMAND = 106;
    public static final byte CMD_AUTHORIZE = 101;
    public static final byte CMD_DISCOVER = 26;
    public static final String CMD_NETWORK_INIT = "network_init";
    public static final String CMD_SDK_VERSION = "SDK_version";
    public static final String CMD_PROBE_LIST = "probe_list";
    public static final String CMD_DEVICE_ADD = "device_add";
    public static final String CMD_DEVICE_UPDATE = "device_update";
    public static final String CMD_DEVICE_DELETE = "device_delete";
    public static final String CMD_DEVICE_LAN_IP = "device_lan_ip";
    public static final String CMD_DEVICE_STATE = "device_state";
    public static final String CMD_SP2_REFRESH = "sp2_refresh";
    public static final String CMD_SP2_CONTROL = "sp2_control";
    public static final String CMD_SP2_CURRENT_POWER = "sp2_current_power";
    public static final String CMD_RM1_AUTH = "rm1_auth";
    public static final String CMD_RM1_STUDY = "rm1_study";
    public static final String CMD_RM1_CODE = "rm1_code";
    public static final String CMD_RM1_SEND = "rm1_send";
    public static final String CMD_RM2_REFRESH = "rm2_refresh";
    public static final String CMD_RM2_STUDY = "rm2_study";
    public static final String CMD_RM2_CODE = "rm2_code";
    public static final String CMD_RM2_SEND = "rm2_send";
    public static final String CMD_EASY_CONFIG = "easyconfig";
    public static final int CMD_NETWORK_INIT_ID = 1;
    public static final int CMD_SDK_VERSION_ID = 2;
    public static final int CMD_PROBE_LIST_ID = 11;
    public static final int CMD_DEVICE_ADD_ID = 12;
    public static final int CMD_DEVICE_UPDATE_ID = 13;
    public static final int CMD_DEVICE_DELETE_ID = 14;
    public static final int CMD_DEVICE_LAN_IP_ID = 15;
    public static final int CMD_DEVICE_STATE_ID = 16;
    public static final int CMD_SP2_REFRESH_ID = 71;
    public static final int CMD_SP2_CONTROL_ID = 72;
    public static final int CMD_SP2_CURRENT_POWER_ID = 74;
    public static final int CMD_RM1_AUTH_ID = 101;
    public static final int CMD_RM1_STUDY_ID = 102;
    public static final int CMD_RM1_CODE_ID = 103;
    public static final int CMD_RM1_SEND_ID = 104;
    public static final int CMD_RM2_REFRESH_ID = 131;
    public static final int CMD_RM2_STUDY_ID = 132;
    public static final int CMD_RM2_CODE_ID = 133;
    public static final int CMD_RM2_SEND_ID = 134;
    public static final int CMD_EASY_CONFIG_ID = 10000;
    public static final String BROADLINK_AUTH_KEY = "097628343fe99e23765c1513accf8b02";
    public static final String BROADLINK_IV = "562e17996d093d28ddb3ba695a2e6f58";

    public static final Map<ThingTypeUID, String> SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP = new HashMap<ThingTypeUID, String>();

    static {
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM, RM);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM2, RM2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM3, RM3);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM3Q, RM3Q);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_RM4, RM4);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_A1, A1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP1, MP1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP1_1K3S2U, MP1_1K3S2U);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MP2, MP2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP1, SP1);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP2, SP2);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_SP3, SP3);
        // SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_S1C, S1C);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_PIR, S1P);
        SUPPORTED_THING_TYPES_UIDS_TO_NAME_MAP.put(THING_TYPE_MAGNET, S1M);
    }
}
