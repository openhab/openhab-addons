/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mspa.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MSpaConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaConstants {

    public static final String BINDING_ID = "mspa";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OWNER_ACCOUNT = new ThingTypeUID(BINDING_ID, "owner-account");
    public static final ThingTypeUID THING_TYPE_VISITOR_ACCOUNT = new ThingTypeUID(BINDING_ID, "visitor-account");
    public static final ThingTypeUID THING_TYPE_POOL = new ThingTypeUID(BINDING_ID, "pool");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_OWNER_ACCOUNT,
            THING_TYPE_VISITOR_ACCOUNT, THING_TYPE_POOL);

    public static final String REGION_ROW = "ROW";
    public static final String REGION_US = "US";
    public static final String REGION_CHINA = "CH";

    public static final String VIDEO_URL = "https://donghuiprodappconfig.s3.eu-central-1.amazonaws.com/video(1).html";
    public static final String VIDEO_URL2 = "https://donghuiprodappconfig.s3.eu-central-1.amazonaws.com/video.html";

    public static final Map<String, String> HOSTS = Map.of(REGION_ROW, "https://api.iot.the-mspa.com", REGION_US,
            "https://api.usiot.the-mspa.com", REGION_CHINA, "https://api.mspa.mxchip.com.cn");
    public static final Map<String, String> WSS_HOSTS = Map.of(REGION_ROW,
            "wss://xvvfjuknsi.execute-api.eu-central-1.amazonaws.com/production/", REGION_US,
            "wss://27n7hwtf73.execute-api.us-east-1.amazonaws.com/production", REGION_CHINA,
            "wss://w7vvlxl4dk.execute-api.eu-west-1.amazonaws.com/press_test/");
    public static final Map<String, String> APP_IDS = Map.of(REGION_ROW, "e1c8e068f9ca11eba4dc0242ac120002", REGION_US,
            "e1c8e068f9ca11eba4dc0242ac120002", REGION_CHINA, "e1c8e068f9ca11eba4dc0242ac120002");
    public static final Map<String, String> APP_SECRETS = Map.of(REGION_ROW, "87025c9ecd18906d27225fe79cb68349",
            REGION_US, "87025c9ecd18906d27225fe79cb68349", REGION_CHINA, "87025c9ecd18906d27225fe79cb68349");

    public static final String TOKEN_ENDPOINT = "/api/enduser/get_token/";
    public static final String DEVICE_LIST_ENDPOINT = "/api/enduser/devices/";
    public static final String DEVICE_SHADOW_ENDPOINT = "/api/device/thing_shadow/";
    public static final String COMMAND_ENDPOINT = "/api/device/command/";
    public static final String VISITOR_ENDPOINT = "/api/enduser/visitor/";
    public static final String GRAND_DEVICE_ENDPOINT = "/api/enduser/grant_device/";

    public static final String GET = "GET";
    public static final String POST = "POST";

    public static final String UNKNOWN = "unknown";
    public static final String EMPTY = "";

    // List of all Channel ids
    public static final String HEATER = "heater";
    public static final String WATER_CURRENT_TEMPERATURE = "temperature";
    public static final String WATER_TARGET_TEMPERATURE = "target-temperature";
    public static final String JET_STREAM = "jet-stream";
    public static final String BUBBLES = "bubbles";
    public static final String BUBBLE_LEVEL = "bubble-level";
    public static final String CIRCULATE = "circulation";
    public static final String UVC = "uvc";
    public static final String OZONE = "ozone";
    public static final String LOCK = "lock";

    public static final String COMMAND_TEMPLATE = new String("{\"desired\": {\"state\": {\"desired\": {%s}}}}");

    public static final Map<String, String> DEVICE_PROPERTY_MAPPING = Map.of("device_id", "deviceId", "product_id",
            "productId", "sn", "serialNumber", "app_id", "appId", "name", "name", "software_version", "softwareVersion",
            "enduser_id", "enduserId", "url", "pictureUrl", "product_series", "productSeries", "service_region",
            "serviceRegion");
}
