/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link TailwindBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindBindingConstants {

    public static final String BINDING_ID = "tailwind";
    // private static final String SYSTEM_STATE_CHANNEL_TYPE = "system";

    // List of all Thing Type UIDs (TailwindPro may not be needed???)
    public static final ThingTypeUID THING_TYPE_TAILWIND = new ThingTypeUID(BINDING_ID, "tailwindPro");
    public static final ThingTypeUID THING_TYPE_TAILWIND_UNSUPPORTED = new ThingTypeUID(BINDING_ID,
            "tailwindUnsupported");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Stream
            .concat(Stream.of(THING_TYPE_TAILWIND),
                    Arrays.stream(TailwindModel.values()).map(model -> new ThingTypeUID(BINDING_ID, model.getId())))
            .collect(Collectors.toSet());

    /**
     * Miscellaneous constants
     */
    public static final String TAILWIND_VENDOR_NAME = "tailwind";
    public static final Integer TAILWIND_STATUS_REQUEST_JOB_INTERVAL = 300; // every xxx seconds to get updates
    public static final String TAILWIND_THING_LABEL_NAME = "TailWind";
    public static final String TAILWIND_HTTP_SERVER_URL = "httpServerUrl";
    public static final String TAILWIND_PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String TAILWIND_PROPERTY_HARDWARE_VERSION = "hardwareVersion";
    public static final String TAILWIND_PROPERTY_SOFTWARE_VERSION = "firmwareVersion";
    public static final String TAILWIND_PROPERTY_MODEL_ID = "modelId";
    public static final String TAILWIND_PROPERTY_MAX_DOORS = "maxDoors";
    public static final String TAILWIND_BASE_URL_PART_1 = "http://";
    public static final String TAILWIND_BASE_URL_PART_2 = "/json";
    public static final String TAILWIND_HTTP_HEADER_TOKEN = "TOKEN";
    public static final Integer TAILWIND_OPENHAB_HOST_UDP_PORT = 50904;
    public static final String NOT_FOUND_ERROR = "Not_Found";
    public static final String JSON_RESPONSE_RESULT_OK = "OK";
    public static final String TAILWIND_CONFIG_WEB_SERVER_ADDRESSS_KEY = "webServerAddress";
    public static final double PARTIAL_OPEN_DEFAULT_SETTING = 2.5d;
    public static final String VALID_IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
    /** Thing is set OFFLINE after so many communication errors. */
    public static final int ATTEMPTS_WITH_COMMUNICATION_ERRORS = 3;
    /** Name for thread running UDP Connection */
    public static final String TAILWIND_UDP_CONNECTOR_THREAD_NAME = BINDING_ID + "_UDP_Connector";

    // Channel Group TailWind Controller
    public static final String CHANNEL_GROUP_CONTROLLER = "controller";

    // List of Controller Channel ID's
    public static final String CHANNEL_DOOR_NUM = "controller#doorNum";
    public static final String CHANNEL_NIGHT_MODE_ENABLED = "controller#nightModeEnabled";
    public static final String CHANNEL_LED_BRIGHTNESS = "controller#ledBrightness";
    public static final String CHANNEL_ROUTER_RSSI = "controller#routerRssi";
    public static final String CHANNEL_PRODUCT_ID = "controller#productID";
    public static final String CHANNEL_DEVICE_ID = "controller#deviceID";
    public static final String CHANNEL_FIRMWARE_VERSION = "controller#firmwareVersion";
    public static final String CHANNEL_SUPPORT_COMMAND = "controller#supportCommand";
    public static final String CHANNEL_SERVER_MONITOR = "controller#serverMonitor";
    public static final String PARAMETER_DOOR_NUM_CONNECTED = "numberOfDoorsConnected";

    // Channel Group Door One Controls
    public static final String CHANNEL_GROUP_DOOR_ONE = "doorOne";

    public static final String CHANNEL_DOOR_1_CONTROLS_INDEX = "doorOne#index";
    public static final String CHANNEL_DOOR_1_CONTROLS_STATUS = "doorOne#status";
    public static final String CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE = "doorOne#openClose";
    public static final String CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN = "doorOne#partialOpen";
    public static final String CHANNEL_DOOR_1_CONTROLS_LOCKUP = "doorOne#lockup";
    public static final String CHANNEL_DOOR_1_CONTROLS_DISABLED = "doorOne#disabled";

    // Channel Group Door One Controls
    public static final String CHANNEL_GROUP_DOOR_TWO = "doorTwo";

    public static final String CHANNEL_DOOR_2_CONTROLS_INDEX = "doorTwo#index";
    public static final String CHANNEL_DOOR_2_CONTROLS_STATUS = "doorTwo#status";
    public static final String CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE = "doorTwo#openClose";
    public static final String CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN = "doorTwo#partialOpen";
    public static final String CHANNEL_DOOR_2_CONTROLS_LOCKUP = "doorTwo#lockup";
    public static final String CHANNEL_DOOR_2_CONTROLS_DISABLED = "doorTwo#disabled";

    // Channel Group Door One Controls
    public static final String CHANNEL_GROUP_DOOR_THREE = "doorThree";

    public static final String CHANNEL_DOOR_3_CONTROLS_INDEX = "doorThree#index";
    public static final String CHANNEL_DOOR_3_CONTROLS_STATUS = "doorThree#status";
    public static final String CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE = "doorThree#openClose";
    public static final String CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN = "doorThree#partialOpen";
    public static final String CHANNEL_DOOR_3_CONTROLS_LOCKUP = "doorThree#lockup";
    public static final String CHANNEL_DOOR_3_CONTROLS_DISABLED = "doorThree#disabled";

    // Map of Door 2 Channel Type UIDs (to be added to Thing later when needed)
    public static final Map<String, ChannelTypeUID> DOOR_1_CHANNEL_TYPES = new LinkedHashMap<>();
    static {
        DOOR_1_CHANNEL_TYPES.put(CHANNEL_DOOR_1_CONTROLS_INDEX, new ChannelTypeUID(BINDING_ID, "index"));
        DOOR_1_CHANNEL_TYPES.put(CHANNEL_DOOR_1_CONTROLS_STATUS, new ChannelTypeUID(BINDING_ID, "status"));
        DOOR_1_CHANNEL_TYPES.put(CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE, new ChannelTypeUID(BINDING_ID, "openClose"));
        DOOR_1_CHANNEL_TYPES.put(CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN, new ChannelTypeUID(BINDING_ID, "partialOpen"));
        DOOR_1_CHANNEL_TYPES.put(CHANNEL_DOOR_1_CONTROLS_LOCKUP, new ChannelTypeUID(BINDING_ID, "lockup"));
        DOOR_1_CHANNEL_TYPES.put(CHANNEL_DOOR_1_CONTROLS_DISABLED, new ChannelTypeUID(BINDING_ID, "disabled"));
    }

    // Map of Door 2 Channel Type UIDs (to be added to Thing later when needed)
    public static final Map<String, ChannelTypeUID> DOOR_2_CHANNEL_TYPES = new LinkedHashMap<>();
    static {
        DOOR_2_CHANNEL_TYPES.put(CHANNEL_DOOR_2_CONTROLS_INDEX, new ChannelTypeUID(BINDING_ID, "index"));
        DOOR_2_CHANNEL_TYPES.put(CHANNEL_DOOR_2_CONTROLS_STATUS, new ChannelTypeUID(BINDING_ID, "status"));
        DOOR_2_CHANNEL_TYPES.put(CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE, new ChannelTypeUID(BINDING_ID, "openClose"));
        DOOR_2_CHANNEL_TYPES.put(CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN, new ChannelTypeUID(BINDING_ID, "partialOpen"));
        DOOR_2_CHANNEL_TYPES.put(CHANNEL_DOOR_2_CONTROLS_LOCKUP, new ChannelTypeUID(BINDING_ID, "lockup"));
        DOOR_2_CHANNEL_TYPES.put(CHANNEL_DOOR_2_CONTROLS_DISABLED, new ChannelTypeUID(BINDING_ID, "disabled"));
    }

    // Map of Door 3 Channel Type UIDs (to be added to Thing later when needed)
    public static final Map<String, ChannelTypeUID> DOOR_3_CHANNEL_TYPES = new LinkedHashMap<>();
    static {
        DOOR_3_CHANNEL_TYPES.put(CHANNEL_DOOR_3_CONTROLS_INDEX, new ChannelTypeUID(BINDING_ID, "index"));
        DOOR_3_CHANNEL_TYPES.put(CHANNEL_DOOR_3_CONTROLS_STATUS, new ChannelTypeUID(BINDING_ID, "status"));
        DOOR_3_CHANNEL_TYPES.put(CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE, new ChannelTypeUID(BINDING_ID, "openClose"));
        DOOR_3_CHANNEL_TYPES.put(CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN, new ChannelTypeUID(BINDING_ID, "partialOpen"));
        DOOR_3_CHANNEL_TYPES.put(CHANNEL_DOOR_3_CONTROLS_LOCKUP, new ChannelTypeUID(BINDING_ID, "lockup"));
        DOOR_3_CHANNEL_TYPES.put(CHANNEL_DOOR_3_CONTROLS_DISABLED, new ChannelTypeUID(BINDING_ID, "disabled"));
    }

    /**
     * Static mapping of ChannelType-to-ItemType
     * The following items belong to the core:
     */
    public static final Map<String, String> CHANNEL_ITEM_TYPES = new HashMap<>();
    static {
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_1_CONTROLS_INDEX, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_1_CONTROLS_STATUS, "String");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE, "String");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_1_CONTROLS_LOCKUP, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_1_CONTROLS_DISABLED, "Number");

        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_2_CONTROLS_INDEX, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_2_CONTROLS_STATUS, "String");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE, "String");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_2_CONTROLS_LOCKUP, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_2_CONTROLS_DISABLED, "Number");

        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_3_CONTROLS_INDEX, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_3_CONTROLS_STATUS, "String");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE, "String");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_3_CONTROLS_LOCKUP, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_DOOR_3_CONTROLS_DISABLED, "Number");
    }

    /**
     * Static mapping of ChannelType-to-ItemLables
     */
    public static final Map<String, String> CHANNEL_ITEM_LABELS = new HashMap<>();
    static {
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_1_CONTROLS_INDEX, "Index");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_1_CONTROLS_STATUS, "Status");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE, "Door Control");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN, "Partial Open Time");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_1_CONTROLS_LOCKUP, "Locked Up Status");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_1_CONTROLS_DISABLED, "Is Configured");

        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_2_CONTROLS_INDEX, "Index");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_2_CONTROLS_STATUS, "Status");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE, "Door Control");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN, "Partial Open Time");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_2_CONTROLS_LOCKUP, "Locked Up Status");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_2_CONTROLS_DISABLED, "Is Configured");

        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_3_CONTROLS_INDEX, "Index");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_3_CONTROLS_STATUS, "Status");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE, "Door Control");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN, "Partial Open Time");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_3_CONTROLS_LOCKUP, "Locked Up Status");
        CHANNEL_ITEM_LABELS.put(CHANNEL_DOOR_3_CONTROLS_DISABLED, "Is Configured");
    }

    /**
     * Static mapping of ChannelType-to-ItemLables
     */
    public static final Map<String, String> CHANNEL_ITEM_DESCRIPTION = new HashMap<>();
    static {
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_1_CONTROLS_INDEX, "Door number index (0-2)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_1_CONTROLS_STATUS,
                "Door status (open, partial, close, lock, enable, disable, reboot)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_1_CONTROLS_OPEN_CLOSE, "Door control (open, partial open, close)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_1_CONTROLS_PARTIAL_OPEN,
                "Partial door open time in seconds (0.5 - 15)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_1_CONTROLS_LOCKUP, "Door lockup (0 - not locked up, 1 - locked up)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_1_CONTROLS_DISABLED, "Door is configured (0 - yes, 1 - no)");

        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_2_CONTROLS_INDEX, "Door number index (0-2)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_2_CONTROLS_STATUS,
                "Door status (open, partial, close, lock, enable, disable, reboot)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_2_CONTROLS_OPEN_CLOSE, "Door control (open, partial open, close)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_2_CONTROLS_PARTIAL_OPEN,
                "Partial door open time in seconds (0.5 - 15)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_2_CONTROLS_LOCKUP, "Door lockup (0 - not locked up, 1 - locked up)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_2_CONTROLS_DISABLED, "Door is configured (0 - yes, 1 - no)");

        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_3_CONTROLS_INDEX, "Door number index (0-2)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_3_CONTROLS_STATUS,
                "Door status (open, partial, close, lock, enable, disable, reboot)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_3_CONTROLS_OPEN_CLOSE, "Door control (open, partial open, close)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_3_CONTROLS_PARTIAL_OPEN,
                "Partial door open time in seconds (0.5 - 15)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_3_CONTROLS_LOCKUP, "Door lockup (0 - not locked up, 1 - locked up)");
        CHANNEL_ITEM_DESCRIPTION.put(CHANNEL_DOOR_3_CONTROLS_DISABLED, "Door is configured (0 - yes, 1 - no)");
    }

    /**
     * TailWind device commands. To be parsed into JSON object and modified to reflect
     * the command that is to be sent to the TailWind controller's HTTP server API.
     */

    /* ------- Keys for JSON values ----------------- */
    public static final String TAILWIND_JSON_KEY_VERSION = "version";
    public static final String TAILWIND_JSON_KEY_PRODUCT = "product";
    public static final String TAILWIND_JSON_KEY_DATA = "data";
    public static final String TAILWIND_JSON_KEY_TYPE = "type";
    public static final String TAILWIND_JSON_KEY_NAME = "name";
    public static final String TAILWIND_JSON_KEY_VALUE = "value";
    public static final String TAILWIND_JSON_KEY_URL = "url";
    public static final String TAILWIND_JSON_KEY_PROTO = "proto";
    public static final String TAILWIND_JSON_KEY_ENABLE = "enable";
    public static final String TAILWIND_JSON_KEY_DOOR_IDX = "door_idx";
    public static final String TAILWIND_JSON_KEY_CMD = "cmd";
    public static final String TAILWIND_JSON_KEY_PARTIAL = "partial_time";
    public static final String TAILWIND_JSON_KEY_BRIGHTNESS = "brightness";

    /* ------- JSON key values ----------------- */
    public static final String TAILWIND_JSON_VALUE_PRODUCT = "iQ3";
    public static final String TAILWIND_JSON_VALUE_VER_01 = "0.1";
    public static final String TAILWIND_JSON_VALUE_VER_02 = "0.2";
    public static final String TAILWIND_JSON_VALUE_TYPE_GET = "get";
    public static final String TAILWIND_JSON_VALUE_TYPE_SET = "set";
    public static final String TAILWIND_JSON_VALUE_NAME_DEV_ST = "dev_st";
    public static final String TAILWIND_JSON_VALUE_NAME_NOTIFY_URL = "notify_url";
    public static final String TAILWIND_JSON_VALUE_NAME_DOOR_OP = "door_op";
    public static final String TAILWIND_JSON_VALUE_NAME_STATUS_LED = "status_led";
    public static final String TAILWIND_JSON_VALUE_NAME_IDENTIFY = "identify";
    public static final String TAILWIND_JSON_VALUE_NAME_REBOOT = "reboot";
    public static final String TAILWIND_JSON_VALUE_NAME_SERVER_MONITOR = "server_monitor";
    public static final String TAILWIND_JSON_VALUE_NAME_PROTO_HTTP = "http";
    public static final String TAILWIND_JSON_VALUE_NAME_PROTPO_UDP = "udp";
    public static final int TAILWIND_JSON_VALUE_ENABLE_ON = 1;
    public static final int TAILWIND_JSON_VALUE_ENABLE_OFF = 0;
    public static final String TAILWIND_JSON_VALUE_CMD_OPEN = "open";
    public static final String TAILWIND_JSON_VALUE_CMD_CLOSE = "close";
    public static final String TAILWIND_JSON_VALUE_CMD_PARTIAL_TIME = "partial";
    public static final int TAILWIND_JSON_VALUE_DOOR_ONE_INDEX = 0;

    /* ------- JSON TailWind controller command templates ---------------------- */
    public static final String TAILWIND_CMD_DEVICE_STATUS = "{\"version\": \"0.1\",\"data\": {\"type\": \"get\",\"name\": \"dev_st\"}}";
    public static final String TAILWIND_CMD_SET_STATUS_REPORT = "{\"product\": \"iQ3\",\"version\": \"0.1\",\"data\": {\"type\": \"set\",\"name\": \"notify_url\",\"value\": {\"url\": \"http://192.168.1.1:8888/report\",\"proto\": \"udp\",\"enable\": 1}}}";
    public static final String TAILWIND_CMD_DOOR_OPEN_OR_CLOSE = "{\"product\": \"iQ3\",\"version\": \"0.1\",\"data\": {\"type\": \"set\",\"name\": \"door_op\",\"value\": {\"door_idx\": 0,\"cmd\": \"open\",}}}";
    public static final String TAILWIND_CMD_SET_LED_BRIGHTNESS = "{\"product\": \"iQ3\",\"version\": \"0.1\",\"data\": {\"type\": \"set\",\"name\": \"status_led\",\"value\": {\"brightness\": 100}}}";
    public static final String TAILWIND_CMD_IDENTIFY_DEVICE = "{\"product\": \"iQ3\",\"version\": \"0.2\",\"data\": {\"type\": \"set\",\"name\": \"identify\"}}";
    public static final String TAILWIND_CMD_SET_SERVER_MONITOR = "{\"product\": \"iQ3\",\"version\": \"0.2\",\"data\": {\"type\": \"set\",\"name\": \"server_monitor\",\"value\": {\"enable\": true}}}";
    public static final String TAILWIND_CMD_REBOOT_DEVICE = "{\"product\": \"iQ3\",\"version\": \"0.2\",\"data\": {\"type\": \"set\",\"name\": \"reboot\"}}";
}
