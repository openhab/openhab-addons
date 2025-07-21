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

    public static enum ServiceRegion {
        ROW,
        US,
        CH
    };

    public static final Map<ServiceRegion, String> HOSTS = Map.of(ServiceRegion.ROW, "https://api.iot.the-mspa.com",
            ServiceRegion.US, "https://api.usiot.the-mspa.com", ServiceRegion.CH, "https://api.mspa.mxchip.com.cn");
    public static final Map<ServiceRegion, String> WSS_HOSTS = Map.of(ServiceRegion.ROW,
            "wss://xvvfjuknsi.execute-api.eu-central-1.amazonaws.com/production/", ServiceRegion.US,
            "wss://27n7hwtf73.execute-api.us-east-1.amazonaws.com/production", ServiceRegion.CH,
            "wss://w7vvlxl4dk.execute-api.eu-west-1.amazonaws.com/press_test/");
    public static final Map<ServiceRegion, String> APP_IDS = Map.of(ServiceRegion.ROW,
            "e1c8e068f9ca11eba4dc0242ac120002", ServiceRegion.US, "e1c8e068f9ca11eba4dc0242ac120002", ServiceRegion.CH,
            "e1c8e068f9ca11eba4dc0242ac120002");
    public static final Map<ServiceRegion, String> APP_SECRETS = Map.of(ServiceRegion.ROW,
            "87025c9ecd18906d27225fe79cb68349", ServiceRegion.US, "87025c9ecd18906d27225fe79cb68349", ServiceRegion.CH,
            "87025c9ecd18906d27225fe79cb68349");

    public static final String ENDPOINT_TOKEN = "/api/enduser/get_token/";
    public static final String ENDPOINT_VISITOR = "/api/enduser/visitor/";
    public static final String ENDPOINT_DEVICE_LIST = "/api/enduser/devices/";
    public static final String ENDPOINT_DEVICE_SHADOW = "/api/device/thing_shadow/";
    public static final String ENDPOINT_COMMAND = "/api/device/command/";
    public static final String ENDPOINT_GRANT_DEVICE = "/api/enduser/grant_device/";

    // List of all Channel ids
    public static final String CHANNEL_HEATER = "heater";
    public static final String CHANNEL_WATER_CURRENT_TEMPERATURE = "temperature";
    public static final String CHANNEL_WATER_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_JET_STREAM = "jet-stream";
    public static final String CHANNEL_BUBBLES = "bubbles";
    public static final String CHANNEL_BUBBLE_LEVEL = "bubble-level";
    public static final String CHANNEL_CIRCULATE = "circulation";
    public static final String CHANNEL_UVC = "uvc";
    public static final String CHANNEL_OZONE = "ozone";
    public static final String CHANNEL_LOCK = "lock";

    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_PRODUCT_SERIES = "productSeries";
    public static final String PROPERTY_VISITOR_ID = "visitorId";

    public static final String UNKNOWN = "unknown";
    public static final String EMPTY = "";

    public static final String COMMAND_TEMPLATE = new String("{\"desired\": {\"state\": {\"desired\": {%s}}}}");

    public static final Map<String, String> DEVICE_PROPERTY_MAPPING = Map.of("device_id", "deviceId", //
            "product_id", "productId", //
            "sn", "serialNumber", //
            "app_id", "appId", //
            "name", "name", //
            "software_version", "softwareVersion", //
            "enduser_id", "enduserId", //
            "url", "pictureUrl", //
            "product_series", "productSeries", //
            "service_region", "serviceRegion");
}
