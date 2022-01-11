/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.constants;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoErrorConstants} class defines error-message constants
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoErrorConstants {
    /****************************************
     * LIST OF ERROR CODES
     ****************************************/
    // List of API-ErrorCodes
    public static final Integer ERR_COMMON_FAILED = -1;
    public static final Integer ERR_SESSION_TIMEOUT = 9999;
    public static final Integer ERR_NULL_TRANSPORT = 1000;
    public static final Integer ERR_REQUEST = 1002;
    public static final Integer ERR_HAND_SHAKE_FAILED = 1100;
    public static final Integer ERR_LOGIN_FAILED = 1111;
    public static final Integer ERR_HTTP_TRANSPORT_FAILED = 1112;
    public static final Integer ERR_MULTI_REQUEST_FAILED = 1200;
    public static final Integer ERR_JSON_DECODE_FAIL = -1003;
    public static final Integer ERR_JSON_ENCODE_FAIL = -1004;
    public static final Integer ERR_AES_DECODE_FAIL = -1005;
    public static final Integer ERR_REQUEST_LEN_ERROR = -1006;
    public static final Integer ERR_CLOUD_FAILED = -1007;
    public static final Integer ERR_PARAMS = -1008;
    public static final Integer ERR_RSA_KEY_LENGTH = -1010;
    public static final Integer ERR_SESSION_PARAM = -1101;
    public static final Integer ERR_QUICK_SETUP = -1201;
    public static final Integer ERR_DEVICE = -1301;
    public static final Integer ERR_DEVICE_NEXT_EVENT = -1302;
    public static final Integer ERR_FIRMWARE = -1401;
    public static final Integer ERR_FIRMWARE_VER_ERROR = -1402;
    public static final Integer ERR_LOGIN = -1501;
    public static final Integer ERR_TIME = -1601;
    public static final Integer ERR_TIME_SYS = -1602;
    public static final Integer ERR_TIME_SAVE = -1603;
    public static final Integer ERR_WIRELESS = -1701;
    public static final Integer ERR_WIRELESS_UNSUPPORTED = -1702;
    public static final Integer ERR_SCHEDULE = -1801;
    public static final Integer ERR_SCHEDULE_FULL = -1802;
    public static final Integer ERR_SCHEDULE_CONFLICT = -1803;
    public static final Integer ERR_SCHEDULE_SAVE = -1804;
    public static final Integer ERR_SCHEDULE_INDEX = -1805;
    public static final Integer ERR_COUNTDOWN = -1901;
    public static final Integer ERR_COUNTDOWN_CONFLICT = -1902;
    public static final Integer ERR_COUNTDOWN_SAVE = -1903;
    public static final Integer ERR_ANTITHEFT = -2001;
    public static final Integer ERR_ANTITHEFT_CONFLICT = -2002;
    public static final Integer ERR_ANTITHEFT_SAVE = -2003;
    public static final Integer ERR_ACCOUNT = -2101;
    public static final Integer ERR_STAT = -2201;
    public static final Integer ERR_STAT_SAVE = -2202;
    public static final Integer ERR_DST = -2301;
    public static final Integer ERR_DST_SAVE = -2302;
    // -20661

    // List of Binding-ErrorCodes
    public static final Integer ERR_HTTP_RESPONSE = 9001;
    public static final Integer ERR_COOKIE = 9002;
    public static final Integer ERR_CREDENTIALS = 9003;
    public static final Integer ERR_DEVICE_OFFLINE = 9009;
    public static final Integer ERR_CONNECT_TIMEOUT = 9010;

    // List of Config-ErrorCodes
    public static final Integer ERR_CONF_IP = 10001; // ip not set
    public static final Integer ERR_CONF_CREDENTIALS = 10002; // credentials not set
    public static final Integer ERR_NO_BRIDGE = 10003; // no bridge configured

    /****************************************
     * LIST OF ERROR MESSAGES
     ****************************************/
    // List of CLOUD-Error-Messages
    public static final String ERR_COMMON_FAILED_MSG = ""; // -1;
    public static final String ERR_SESSION_TIMEOUT_MSG = "Session Timeout"; // 9999;
    public static final String ERR_NULL_TRANSPORT_MSG = ""; // 1000;
    public static final String ERR_REQUEST_MSG = "Invalid request or command"; // 1002;
    public static final String ERR_HAND_SHAKE_FAILED_MSG = "Can't create handshake"; // 1100;
    public static final String ERR_LOGIN_FAILED_MSG = ""; // 1111;
    public static final String ERR_HTTP_TRANSPORT_FAILED_MSG = ""; // 1112;
    public static final String ERR_MULTI_REQUEST_FAILED_MSG = ""; // 1200;
    public static final String ERR_JSON_DECODE_FAIL_MSG = "json decode failed"; // -1003;
    public static final String ERR_JSON_ENCODE_FAIL_MSG = "json encode failed"; // -1004;
    public static final String ERR_AES_DECODE_FAIL_MSG = ""; // -1005;
    public static final String ERR_REQUEST_LEN_ERROR_MSG = ""; // -1006;
    public static final String ERR_CLOUD_FAILED_MSG = ""; // -1007;
    public static final String ERR_PARAMS_MSG = "received invalid parameter"; // -1008;
    public static final String ERR_RSA_KEY_LENGTH_MSG = "Invalid Public Key Length"; // -1010;
    public static final String ERR_SESSION_PARAM_MSG = ""; // -1101;
    public static final String ERR_QUICK_SETUP_MSG = ""; // -1201;
    public static final String ERR_DEVICE_MSG = ""; // -1301;
    public static final String ERR_DEVICE_NEXT_EVENT_MSG = ""; // -1302;
    public static final String ERR_FIRMWARE_MSG = ""; // -1401;
    public static final String ERR_FIRMWARE_VER_ERROR_MSG = ""; // -1402;
    public static final String ERR_LOGIN_MSG = "Login Error"; // -1501;
    public static final String ERR_TIME_MSG = ""; // -1601;
    public static final String ERR_TIME_SYS_MSG = ""; // -1602;
    public static final String ERR_TIME_SAVE_MSG = ""; // -1603;
    public static final String ERR_WIRELESS_MSG = ""; // -1701;
    public static final String ERR_WIRELESS_UNSUPPORTED_MSG = ""; // -1702;
    public static final String ERR_SCHEDULE_MSG = ""; // -1801;
    public static final String ERR_SCHEDULE_FULL_MSG = ""; // -1802;
    public static final String ERR_SCHEDULE_CONFLICT_MSG = ""; // -1803;
    public static final String ERR_SCHEDULE_SAVE_MSG = ""; // -1804;
    public static final String ERR_SCHEDULE_INDEX_MSG = ""; // -1805;
    public static final String ERR_COUNTDOWN_MSG = ""; // -1901;
    public static final String ERR_COUNTDOWN_CONFLICT_MSG = ""; // -1902;
    public static final String ERR_COUNTDOWN_SAVE_MSG = ""; // -1903;
    public static final String ERR_ANTITHEFT_MSG = ""; // -2001;
    public static final String ERR_ANTITHEFT_CONFLICT_MSG = ""; // -2002;
    public static final String ERR_ANTITHEFT_SAVE_MSG = ""; // -2003;
    public static final String ERR_ACCOUNT_MSG = ""; // -2101;
    public static final String ERR_STAT_MSG = ""; // -2201;
    public static final String ERR_STAT_SAVE_MSG = ""; // -2202;
    public static final String ERR_DST_MSG = ""; // -2301;
    public static final String ERR_DST_SAVE_MSG = ""; // -2302;

    // List of Binding-Error-Messages
    public static final String ERR_HTTP_RESPONSE_MSG = "Invalid HTTP-Response"; // 9001
    public static final String ERR_COOKIE_MSG = "Cookie Error"; // 9002
    public static final String ERR_DEVICE_OFFLINE_MSG = "Device Offline"; // 9009
    public static final String ERR_CREDENTIALS_MSG = "Invalid Request or Credentials";
    public static final String ERR_CONNECT_TIMEOUT_MSG = "Connection Timeout - device not reachable";

    // List of Config-Error-Messages
    public static final String ERR_CONF_IP_MSG = "IP-Address not valid"; // 10001;
    public static final String ERR_CONF_CREDENTIALS_MSG = "credentials not set (bridge)"; // 10002;
    public static final String ERR_NO_BRIDGE_MSG = "no bridge configured"; // 10003;

    /****************************************
     * ErrorTypes
     ****************************************/
    // communication errors - set device to offline (retry connect)
    public static final Set<Integer> LIST_COMMUNICATION_ERRORS = Set.of(ERR_HTTP_RESPONSE, ERR_COOKIE,
            ERR_DEVICE_OFFLINE, ERR_CONNECT_TIMEOUT);
    // configuration errors - set device to state configuration error (don't retry)
    public static final Set<Integer> LIST_CONFIGURATION_ERRORS = Set.of(ERR_CREDENTIALS);
    // reauthenticate errors (trying login immediatly)
    public static final Set<Integer> LIST_REAUTH_ERRORS = Set.of(ERR_SESSION_TIMEOUT, ERR_HAND_SHAKE_FAILED);
}
