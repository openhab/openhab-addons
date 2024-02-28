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
package org.openhab.binding.rainsoft.internal;

/**
 * @author Ben Rosenblum - Initial contribution
 */

public class ApiConstants {
    // API resources
    public static final String API_ACCEPT_JSON = "application/json";
    public static final String API_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String API_BASE = "https://remind.rainsoft.com/api/remindapp/v2";
    public static final String URL_LOGIN = API_BASE + "/login";
    public static final String URL_CUSTOMER = API_BASE + "/customer";
    public static final String URL_LOCATIONS = API_BASE + "/locations";
    public static final String URL_DEVICE = API_BASE + "/device";

    // JSON devices
    public static final String DEVICES_WCS = "wcs";

    // JSON data names for generic devices
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_MODEL = "model";
    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_SERIALNUMBER = "serialNumber";
}
