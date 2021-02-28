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
package org.openhab.binding.tapocontrol.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoErrorConstants} class defines error-message constants
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoErrorConstants {

    // List of API-ErrorCodes
    public static final Integer ERROR_API_KEY_LENGTH = -1010;
    public static final String ERROR_API_KEY_LENGTH_MSG = "Invalid Public Key Length";
    public static final Integer ERROR_API_CREDENTIALS = -1501;
    public static final String ERROR_API_CREDENTIALS_MSG = "Invalid Request or Credentials";
    public static final Integer ERROR_API_REQUEST = 1002;
    public static final String ERROR_API_REQUEST_MSG = "Incorrect Request";
    public static final Integer ERROR_JSON_FORMAT = -1003;
    public static final String ERROR_JSON_FORMAT_MSG = "JSON formatting error";

    // List of Binding-ErrorCodes
    public static final Integer ERROR_RESPONSE = 9001;
    public static final String ERROR_RESPONSE_MSG = "Invalid HTTP-Response";
    public static final Integer ERROR_COOKIE = 9002;
    public static final String ERROR_COOKIE_MSG = "Cookie Error";
    public static final Integer ERROR_LOGIN = 9003;
    public static final String ERROR_LOGIN_MSG = "Login Error";
    public static final Integer ERROR_DEVICE_OFFLINE = 9009;
    public static final String ERROR_DEVICE_OFFLINE_MSG = "Device Offline";
}
