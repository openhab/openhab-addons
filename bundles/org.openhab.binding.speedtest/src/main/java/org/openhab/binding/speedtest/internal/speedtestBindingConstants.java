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
package org.openhab.binding.speedtest.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link speedtestBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
public class speedtestBindingConstants {

    private static final String BINDING_ID = "speedtest";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SPEEDTEST = new ThingTypeUID(BINDING_ID, "speedtest");

    // Config
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String EXEC_PATH = "execPath";
    public static final String SPEEDTEST_VERSION = "speedTestVersion";
    public static final String SERVER_ID = "serverID";

    // Channels
    public static final String SERVER = "server";
    public static final String PING_JITTER = "ping_jitter";
    public static final String PING_LATENCY = "ping_latency";
    public static final String DOWNLOAD_BANDWIDTH = "download_bandwidth";
    public static final String DOWNLOAD_BYTES = "download_bytes";
    public static final String DOWNLOAD_ELAPSED = "download_elapsed";
    public static final String UPLOAD_BANDWIDTH = "upload_bandwidth";
    public static final String UPLOAD_BYTES = "upload_bytes";
    public static final String UPLOAD_ELAPSED = "upload_elapsed";
    public static final String ISP = "isp";
    public static final String INTERFACE_INTERNALIP = "interface_internalIp";
    public static final String INTERFACE_EXTERNALIP = "interface_externalIp";
    public static final String RESULT_URL = "result_url";
    public static final String TRIGGER_TEST = "trigger_test";

    public static final String PROPERTY_SERVER_LIST1 = "Server List 1";
    public static final String PROPERTY_SERVER_LIST2 = "Server List 2";
    public static final String PROPERTY_SERVER_LIST3 = "Server List 3";
    public static final String PROPERTY_SERVER_LIST4 = "Server List 4";
    public static final String PROPERTY_SERVER_LIST5 = "Server List 5";
    public static final String PROPERTY_SERVER_LIST6 = "Server List 6";
    public static final String PROPERTY_SERVER_LIST7 = "Server List 7";
    public static final String PROPERTY_SERVER_LIST8 = "Server List 8";
    public static final String PROPERTY_SERVER_LIST9 = "Server List 9";
    public static final String PROPERTY_SERVER_LIST10 = "Server List 10";

    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream.of(SERVER, PING_JITTER, PING_LATENCY,
            DOWNLOAD_BANDWIDTH, DOWNLOAD_BYTES, DOWNLOAD_ELAPSED, UPLOAD_BANDWIDTH, UPLOAD_BYTES, UPLOAD_ELAPSED, ISP,
            INTERFACE_INTERNALIP, INTERFACE_EXTERNALIP, RESULT_URL).collect(Collectors.toSet());
}
