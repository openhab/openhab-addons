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
    public static final Integer ERR_API_COMMON_FAILED = -1;
    public static final Integer ERR_API_SESSION_TIMEOUT = 9999;
    public static final Integer ERR_API_NULL_TRANSPORT = 1000;
    public static final Integer ERR_API_REQUEST = 1002;
    public static final Integer ERR_API_HAND_SHAKE_FAILED = 1100;
    public static final Integer ERR_API_LOGIN_FAILED = 1111;
    public static final Integer ERR_API_HTTP_TRANSPORT_FAILED = 1112;
    public static final Integer ERR_API_MULTI_REQUEST_FAILED = 1200;
    public static final Integer ERR_API_JSON_DECODE_FAIL = -1003;
    public static final Integer ERR_API_JSON_ENCODE_FAIL = -1004;
    public static final Integer ERR_API_AES_DECODE_FAIL = -1005;
    public static final Integer ERR_API_REQUEST_LEN_ERROR = -1006;
    public static final Integer ERR_API_CLOUD_FAILED = -1007;
    public static final Integer ERR_API_PARAMS = -1008;
    public static final Integer ERR_API_RSA_KEY_LENGTH = -1010;
    public static final Integer ERR_API_SESSION_PARAM = -1101;
    public static final Integer ERR_API_QUICK_SETUP = -1201;
    public static final Integer ERR_API_DEVICE = -1301;
    public static final Integer ERR_API_DEVICE_NEXT_EVENT = -1302;
    public static final Integer ERR_API_FIRMWARE = -1401;
    public static final Integer ERR_API_FIRMWARE_VER_ERROR = -1402;
    public static final Integer ERR_API_LOGIN = -1501;
    public static final Integer ERR_API_TIME = -1601;
    public static final Integer ERR_API_TIME_SYS = -1602;
    public static final Integer ERR_API_TIME_SAVE = -1603;
    public static final Integer ERR_API_WIRELESS = -1701;
    public static final Integer ERR_API_WIRELESS_UNSUPPORTED = -1702;
    public static final Integer ERR_API_SCHEDULE = -1801;
    public static final Integer ERR_API_SCHEDULE_FULL = -1802;
    public static final Integer ERR_API_SCHEDULE_CONFLICT = -1803;
    public static final Integer ERR_API_SCHEDULE_SAVE = -1804;
    public static final Integer ERR_API_SCHEDULE_INDEX = -1805;
    public static final Integer ERR_API_COUNTDOWN = -1901;
    public static final Integer ERR_API_COUNTDOWN_CONFLICT = -1902;
    public static final Integer ERR_API_COUNTDOWN_SAVE = -1903;
    public static final Integer ERR_API_ANTITHEFT = -2001;
    public static final Integer ERR_API_ANTITHEFT_CONFLICT = -2002;
    public static final Integer ERR_API_ANTITHEFT_SAVE = -2003;
    public static final Integer ERR_API_ACCOUNT = -2101;
    public static final Integer ERR_API_STAT = -2201;
    public static final Integer ERR_API_STAT_SAVE = -2202;
    public static final Integer ERR_API_DST = -2301;
    public static final Integer ERR_API_DST_SAVE = -2302;

    // List of Cloud-ErrorCodes
    public static final Integer ERR_CLOUD_API_RATE = -20004;
    public static final Integer ERR_CLOUD_CREDENTIALS = -20601;
    public static final Integer ERR_CLOUD_JSON_FORMAT = -10100;
    public static final Integer ERR_CLOUD_METHOD_MISSING = -20103;
    public static final Integer ERR_CLOUD_PARAMETER_MISSING = -20104;
    public static final Integer ERR_CLOUD_TOKEN_EXPIRED = -20651;

    // List of Binding-ErrorCodes
    public static final Integer ERR_BINDING_HTTP_RESPONSE = 9001;
    public static final Integer ERR_BINDING_COOKIE = 9002;
    public static final Integer ERR_BINDING_CREDENTIALS = 9003;
    public static final Integer ERR_BINDING_DEVICE_OFFLINE = 9009;
    public static final Integer ERR_BINDING_CONNECT_TIMEOUT = 9010;

    // List of Config-ErrorCodes
    public static final Integer ERR_CONFIG_IP = 10001; // ip not set
    public static final Integer ERR_CONFIG_CREDENTIALS = 10002; // credentials not set
    public static final Integer ERR_CONFIG_NO_BRIDGE = 10003; // no bridge configured

    /****************************************
     * ErrorTypes
     ****************************************/
    // communication errors - set device to offline (retry connect)
    public static final Set<Integer> LIST_COMMUNICATION_ERRORS = Set.of(ERR_BINDING_HTTP_RESPONSE, ERR_BINDING_COOKIE,
            ERR_BINDING_DEVICE_OFFLINE, ERR_BINDING_CONNECT_TIMEOUT);
    // configuration errors - set device to state configuration error (don't retry)
    public static final Set<Integer> LIST_CONFIGURATION_ERRORS = Set.of(ERR_BINDING_CREDENTIALS);
    // reauthenticate errors (trying login immediatly)
    public static final Set<Integer> LIST_REAUTH_ERRORS = Set.of(ERR_API_SESSION_TIMEOUT, ERR_API_HAND_SHAKE_FAILED);
}
