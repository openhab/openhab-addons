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
package org.openhab.binding.ecovacs.internal.api.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public class RequestQueryParameter {

    private RequestQueryParameter() {
        // Prevent instantiation
    }

    // Authentication
    public static final String AUTH_TIMESPAN = "authTimespan";
    public static final String AUTH_TIME_ZONE = "authTimeZone";
    public static final String AUTH_APPKEY = "authAppkey";
    public static final String AUTH_SIGN = "authSign";
    public static final String AUTH_OPEN_ID = "openId";
    public static final String AUTH_ACCOUNT = "account";
    public static final String AUTH_PASSWORD = "password";
    public static final String AUTH_REQUEST_ID = "requestId";
    public static final String AUTH_CODE_UID = "uid";
    public static final String AUTH_CODE_ACCESS_TOKEN = "accessToken";
    public static final String AUTH_CODE_BIZ_TYPE = "bizType";
    public static final String AUTH_CODE_DEVICE_ID = "deviceId";

    // Metadata
    public static final String META_COUNTRY = "country";
    public static final String META_LANG = "lang";
    public static final String META_DEVICE_ID = "deviceId";
    public static final String META_APP_CODE = "appCode";
    public static final String META_APP_VERSION = "appVersion";
    public static final String META_CHANNEL = "channel";
    public static final String META_DEVICE_TYPE = "deviceType";
}
