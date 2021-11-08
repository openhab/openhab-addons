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
package org.openhab.binding.tapocontrol.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TapoControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoControlBindingConstants {
    public static final String BINDING_ID = "tapocontrol";

    // Lisst of all constant configurations
    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String HTTP_AUTH_TYPE_COOKIE = "cookie";
    public static final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    public static final String CONTENT_CHARSET = "UTF-8"; // "utf-8";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String TAPO_CLOUD_URL = "https://eu-wap.tplinkcloud.com";
    public static final String TAPO_APP_TYPE = "Tapo_Ios";
    public static final String TAPO_TERMINAL_UUID = "0A950402-7224-46EB-A450-7362CDB902A2";
    public static final String TAPO_DEVICE_URL = "http://%s/app";
    public static final Integer TAPO_HTTP_TIMEOUT_MS = 5000; // http request timeout
    public static final Integer TAPO_PING_TIMEOUT_MS = 2000; // ping timeout
    public static final Integer TAPO_DISCOVERY_TIMEOUT_MS = 6000; // timout device discovery
    public static final Integer TAPO_REFRESH_MIN_GAP_MS = 5000; // min gap between sending refresh request
    public static final Integer TAPO_SEND_MIN_GAP_MS = 1000; // min gap between sending command request
    public static final Integer TAPO_LOGIN_MIN_GAP_MS = 5000; // min gap between sending login request
    public static final Integer TAPO_LOGIN_MAX_GAP_M = 1440; // max minutes to relogin to device
    public static final Integer POLLING_MIN_INTERVAL_S = 10; // min polling interval (settings)

    // LIST OF SUPPORTED DEVICE NAMES
    public static final String DEVICE_BRIDGE = "bridge";
    public static final String DEVICE_P100 = "P100";
    public static final String DEVICE_P105 = "P105";
    public static final String DEVICE_L510E = "L510_Series";
    public static final String DEVICE_L530E = "L530_Series";

    // LIST OF SUPPORTED DEVICE DESCRIPTIONS
    public static final String DEVICE_DESCRIPTION_BRIDGE = "TapoControl Cloud-Login";
    public static final String DEVICE_DESCRIPTION_SMART_PLUG = "SmartPlug";
    public static final String DEVICE_DESCRIPTION_WHITE_BULB = "White-Light-Bulb";
    public static final String DEVICE_DESCRIPTION_COLOR_BULB = "Color-Light-Bulb";

    // LIST OF SUPPORTED THING UIDS
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_BRIDGE);
    public static final ThingTypeUID P100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P100);
    public static final ThingTypeUID P105_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P105);
    public static final ThingTypeUID L510E_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L510E);
    public static final ThingTypeUID L530E_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L530E);

    // SET OF SUPPORTED UIDS
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_UIDS = Set.of(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_SMART_PLUG_UIDS = Set.of(P100_THING_TYPE, P105_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_WHITE_BULB_UIDS = Set.of(L510E_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_COLOR_BULB_UIDS = Set.of(L530E_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(SUPPORTED_BRIDGE_UIDS, SUPPORTED_SMART_PLUG_UIDS, SUPPORTED_WHITE_BULB_UIDS, SUPPORTED_COLOR_BULB_UIDS)
            .flatMap(Set::stream).collect(Collectors.toSet()));

    // THINGS WITH CHANNEL GROUPS
    public static final Set<ThingTypeUID> CHANNEL_GROUP_THING_SET = Set.of(P100_THING_TYPE, L510E_THING_TYPE,
            L530E_THING_TYPE);

    // DEVICE PROPERTY STRINGS (CLOUD)
    public static final String CLOUD_PROPERTY_ID = "deviceId";
    public static final String CLOUD_PROPERTY_MODEL = "deviceModel";
    public static final String CLOUD_PROPERTY_NAME = "deviceName";
    public static final String CLOUD_PROPERTY_ALIAS = "alias";
    public static final String CLOUD_PROPERTY_TYPE = "deviceType";
    public static final String CLOUD_PROPERTY_FW = "fwVer";
    public static final String CLOUD_PROPERTY_HW = "deviceHwVer";
    public static final String CLOUD_PROPERTY_SERVER_URL = "appServerUrl";
    public static final String CLOUD_PROPERTY_REGION = "deviceRegion";
    public static final String CLOUD_PROPERTY_MAC = "deviceMac";

    // DEVICE PROPERTY STRINGS (DEVICE)
    public static final String DEVICE_PROPERTY_ID = "device_id";
    public static final String DEVICE_PROPERTY_MODEL = "model";
    public static final String DEVICE_PROPERTY_FW = "fw_ver";
    public static final String DEVICE_PROPERTY_HW = "hw_ver";
    public static final String DEVICE_PROPERTY_MAC = "mac";
    public static final String DEVICE_PROPERTY_IP = "ip";
    public static final String DEVICE_PROPERTY_OVERHEAT = "overheated";
    public static final String DEVICE_PROPERTY_ON = "device_on";
    public static final String DEVICE_PROPERTY_SIGNAL = "signal_level";
    public static final String DEVICE_PROPERTY_ONTIME = "on_time";
    public static final String DEVICE_PROPERTY_BRIGHTNES = "brightness";
    public static final String DEVICE_PROPERTY_HUE = "hue";
    public static final String DEVICE_PROPERTY_SATURATION = "saturation";
    public static final String DEVICE_PROPERTY_COLORTEMP = "color_temp";
    public static final String DEVICE_REPRASENTATION_PROPERTY = "macAddress";

    // DEVICE SETTINGS
    public static final Integer BULB_MIN_COLORTEMP = 2500;
    public static final Integer BULB_MAX_COLORTEMP = 6500;

    // CHANNEL LIST
    public static final String CHANNEL_GROUP_ACTUATOR = "actuator";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_OUTPUT = "output";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR_TEMP = "colorTemperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_GROUP_DEVICE = "device";
    public static final String CHANNEL_OVERHEAT = "overheated";
    public static final String CHANNEL_ONTIME = "onTime";
    public static final String CHANNEL_WIFI_STRENGTH = "wifiSignal";

    // THING CONFIGUTATION PROPERTYS
    public static final String CONFIG_EMAIL = "username";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_DEVICE_IP = "ipAddress";
    public static final String CONFIG_UPDATE_INTERVAL = "pollingInterval";
    public static final String CONFIG_CLOUD_UPDATE_INTERVAL = "cloudReconnect";

    // LIST OF PROPERTY NAMES
    public static final String PROPERTY_WIFI_LEVEL = "signal-strength";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_FAMILY = "deviceFamily";

    // LIST OF DEVICE-COMMANDS
    public static final String DEVICE_CMD_GETINFO = "get_device_info";
    public static final String DEVICE_CMD_SETINFO = "set_device_info";
}
