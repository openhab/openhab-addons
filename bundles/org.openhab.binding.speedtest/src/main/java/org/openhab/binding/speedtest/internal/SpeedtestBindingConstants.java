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
package org.openhab.binding.speedtest.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SpeedtestBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian Homeyer - Initial contribution
 */
@NonNullByDefault
public class SpeedtestBindingConstants {

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
    public static final String TIMESTAMP = "timestamp";
    public static final String PING_JITTER = "pingJitter";
    public static final String PING_LATENCY = "pingLatency";
    public static final String DOWNLOAD_BANDWIDTH = "downloadBandwidth";
    public static final String DOWNLOAD_BYTES = "downloadBytes";
    public static final String DOWNLOAD_ELAPSED = "downloadElapsed";
    public static final String UPLOAD_BANDWIDTH = "uploadBandwidth";
    public static final String UPLOAD_BYTES = "uploadBytes";
    public static final String UPLOAD_ELAPSED = "uploadElapsed";
    public static final String ISP = "isp";
    public static final String INTERFACE_INTERNALIP = "interfaceInternalIp";
    public static final String INTERFACE_EXTERNALIP = "interfaceExternalIp";
    public static final String RESULT_URL = "resultUrl";
    public static final String RESULT_IMAGE = "resultImage";
    public static final String TRIGGER_TEST = "triggerTest";

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

    public static final Set<String> SUPPORTED_CHANNEL_IDS = Set.of(SERVER, TIMESTAMP, PING_JITTER, PING_LATENCY,
            DOWNLOAD_BANDWIDTH, DOWNLOAD_BYTES, DOWNLOAD_ELAPSED, UPLOAD_BANDWIDTH, UPLOAD_BYTES, UPLOAD_ELAPSED, ISP,
            INTERFACE_INTERNALIP, INTERFACE_EXTERNALIP, RESULT_URL, RESULT_IMAGE, TRIGGER_TEST);
}
