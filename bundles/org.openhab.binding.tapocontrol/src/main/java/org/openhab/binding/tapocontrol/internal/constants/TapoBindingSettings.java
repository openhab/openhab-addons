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
    public static final Integer TAPO_HTTP_CLOUD_TIMEOUT_MS = 10000; // http request cloud timeout
    public static final Integer TAPO_PING_TIMEOUT_MS = 2000; // ping timeout
    public static final Integer TAPO_QUERY_MIN_GAP_MS = 1000; // min gap between sending query request
    public static final Integer TAPO_SEND_MIN_GAP_MS = 1000; // min gap between sending command request
    public static final Integer TAPO_LOGIN_MIN_GAP_MS = 5000; // min gap between sending login request
    public static final Integer TAPO_LOGIN_MAX_GAP_M = 1440; // max minutes to relogin to device
    public static final Integer TAPO_DISCOVERY_TIMEOUT_S = 20; // timout device discovery in seconds
    public static final Integer POLLING_MIN_INTERVAL_S = 1; // min polling interval (settings)
    public static final Integer TAPO_MULTI_COMMAND_OFFSET_MS = 100; // Offset between sending multiple commands in ms

    // FORMATING CONSTANTS
    public static final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    public static final char MAC_DIVISION_CHAR = '-';
}
