/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoErrorCode} enum lists known errorcodes can be received or thrown by binding
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public enum TapoErrorCode {
    NO_ERROR(0),
    // List of API-Errorcodes from device
    ERR_UNKNOWN(-1, TapoErrorType.UNKNOWN),
    ERR_API_UNKNOWN_COM_ERROR(9999, TapoErrorType.COMMUNICATION_ERROR),
    ERR_API_NULL_TRANSPORT(1000),
    ERR_API_REQUEST(1002),
    ERR_API_PROTOCOL(1003, TapoErrorType.CONFIGURATION_ERROR),
    ERR_API_HAND_SHAKE_FAILED(1100, TapoErrorType.COMMUNICATION_ERROR),
    ERR_API_LOGIN_FAILED(1111),
    ERR_API_HTTP_TRANSPORT_FAILED(1112),
    ERR_API_MULTI_REQUEST_FAILED(1200),
    ERR_API_JSON_DECODE_FAIL(-1003),
    ERR_API_JSON_ENCODE_FAIL(-1004),
    ERR_API_AES_DECODE_FAIL(-1005),
    ERR_API_REQUEST_LEN_ERROR(-1006),
    ERR_API_CLOUD_FAILED(-1007),
    ERR_API_PARAMS(-1008),
    ERR_API_RSA_KEY_LENGTH(-1010),
    ERR_API_SESSION_PARAM(-1101),
    ERR_API_QUICK_SETUP(-1201),
    ERR_API_DEVICE(-1301),
    ERR_API_DEVICE_NEXT_EVENT(-1302),
    ERR_API_FIRMWARE(-1401),
    ERR_API_FIRMWARE_VER_ERROR(-1402),
    ERR_API_LOGIN(-1501),
    ERR_API_TIME(-1601),
    ERR_API_TIME_SYS(-1602),
    ERR_API_TIME_SAVE(-1603),
    ERR_API_WIRELESS(-1701),
    ERR_API_WIRELESS_UNSUPPORTED(-1702),
    ERR_API_SCHEDULE(-1801),
    ERR_API_SCHEDULE_FULL(-1802),
    ERR_API_SCHEDULE_CONFLICT(-1803),
    ERR_API_SCHEDULE_SAVE(-1804),
    ERR_API_SCHEDULE_INDEX(-1805),
    ERR_API_COUNTDOWN(-1901),
    ERR_API_COUNTDOWN_CONFLICT(-1902),
    ERR_API_COUNTDOWN_SAVE(-1903),
    ERR_API_ANTITHEFT(-2001),
    ERR_API_ANTITHEFT_CONFLICT(-2002),
    ERR_API_ANTITHEFT_SAVE(-2003),
    ERR_API_ACCOUNT(-2101),
    ERR_API_STAT(-2201),
    ERR_API_STAT_SAVE(-2202),
    ERR_API_DST(-2301),
    ERR_API_DST_SAVE(-2302),

    // List of Cloud-ErrorCodes
    ERR_CLOUD_API_RATE(-20004),
    ERR_CLOUD_CREDENTIALS(-20601),
    ERR_CLOUD_JSON_FORMAT(-10100),
    ERR_CLOUD_METHOD_MISSING(-20103),
    ERR_CLOUD_PARAMETER_MISSING(-20104),
    ERR_CLOUD_TOKEN_EXPIRED(-20651),

    // List of Binding-ErrorCodes
    ERR_BINDING_NOT_IMPLEMENTED(9000),
    ERR_BINDING_HTTP_RESPONSE(9001, TapoErrorType.COMMUNICATION_ERROR),
    ERR_BINDING_COOKIE(9002, TapoErrorType.COMMUNICATION_ERROR),
    ERR_BINDING_CREDENTIALS(9003, TapoErrorType.CONFIGURATION_ERROR),
    ERR_BINDING_LOGIN(9004, TapoErrorType.CONFIGURATION_ERROR),
    ERR_BINDING_DEVICE_OFFLINE(9009, TapoErrorType.COMMUNICATION_ERROR),
    ERR_BINDING_CONNECT_TIMEOUT(9010, TapoErrorType.COMMUNICATION_ERROR),
    ERR_BINDING_SEND_REQUEST(9011, TapoErrorType.COMMUNICATION_ERROR),
    ERR_BINDING_FX_NOT_FOUND(9020, TapoErrorType.CONFIGURATION_ERROR),

    // List of Data-Error
    ERR_DATA_ENCRYPTING(9500, TapoErrorType.COMMUNICATION_ERROR),
    ERR_DATA_DECRYPTING(9501, TapoErrorType.COMMUNICATION_ERROR),
    ERR_DATA_FORMAT(9505, TapoErrorType.COMMUNICATION_ERROR),
    ERR_DATA_TRANSORMATION(9506),

    // List of Binding-Config-ErrorCodes
    ERR_CONFIG_IP(10001, TapoErrorType.CONFIGURATION_ERROR), // ip not set
    ERR_CONFIG_CREDENTIALS(10002, TapoErrorType.CONFIGURATION_ERROR), // credentials not set
    ERR_CONFIG_NO_BRIDGE(10003, TapoErrorType.CONFIGURATION_ERROR), // no bridge configured
    ERR_CONFIG_PROTOCOL(10004, TapoErrorType.CONFIGURATION_ERROR); // unknown protocol

    private Integer code;
    private TapoErrorType errorType;

    /* set code */
    private TapoErrorCode(Integer code) {
        this.code = code;
        this.errorType = TapoErrorType.GENERAL;
    }

    private TapoErrorCode(Integer code, TapoErrorType errorType) {
        this.code = code;
        this.errorType = errorType;
    }

    /* get vlaues */
    public Integer getCode() {
        return this.code;
    }

    public TapoErrorType getType() {
        return this.errorType;
    }

    public static TapoErrorCode fromCode(int errorCode) {
        for (TapoErrorCode e : TapoErrorCode.values()) {
            if (e.code.equals(errorCode)) {
                return e;
            }
        }
        return ERR_UNKNOWN;
    }
}
