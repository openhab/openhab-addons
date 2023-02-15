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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoBindingSettings} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoBindingSettings {
    public static final String BINDING_ID = "tapocontrol";

    // List of all constant configurations
    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String HTTP_AUTH_TYPE_COOKIE = "cookie";
    public static final String CONTENT_CHARSET = "UTF-8";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String TAPO_CLOUD_URL = "https://eu-wap.tplinkcloud.com";
    public static final String TAPO_APP_TYPE = "Tapo_Ios";
    public static final String TAPO_DEVICE_URL = "http://%s/app";
    public static final Integer HTTP_MAX_CONNECTIONS = 10; // setMaxConnectionsPerDestination for HTTP-Client
    public static final Integer HTTP_MAX_QUEUED_REQUESTS = 10; // setMaxRequestsQueuedPerDestination for HTTP-Client
    public static final Integer TAPO_HTTP_TIMEOUT_MS = 5000; // http request timeout
    public static final Integer TAPO_PING_TIMEOUT_MS = 2000; // ping timeout
    public static final Integer TAPO_REFRESH_MIN_GAP_MS = 5000; // min gap between sending refresh request
    public static final Integer TAPO_SEND_MIN_GAP_MS = 1000; // min gap between sending command request
    public static final Integer TAPO_LOGIN_MIN_GAP_MS = 5000; // min gap between sending login request
    public static final Integer TAPO_LOGIN_MAX_GAP_M = 1440; // max minutes to relogin to device
    public static final Integer TAPO_DISCOVERY_TIMEOUT_S = 6; // timout device discovery in seconds
    public static final Integer POLLING_MIN_INTERVAL_S = 10; // min polling interval (settings)

    // FORMATING CONSTANTS
    public static final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    public static final char MAC_DIVISION_CHAR = '-';

    // LIST OF DEVICE-COMMANDS
    public static final String DEVICE_CMD_GETINFO = "get_device_info";
    public static final String DEVICE_CMD_SETINFO = "set_device_info";
    public static final String DEVICE_CMD_GETENERGY = "get_energy_usage";
    public static final String DEVICE_CMD_CHILD_DEVICE_LIST = "get_child_device_list";
    public static final String DEVICE_CMD_CONTROL_CHILD = "control_child";
    public static final String DEVICE_CMD_MULTIPLE_REQ = "multipleRequest";
    public static final String DEVICE_CMD_CUSTOM = "custom_command";
}
