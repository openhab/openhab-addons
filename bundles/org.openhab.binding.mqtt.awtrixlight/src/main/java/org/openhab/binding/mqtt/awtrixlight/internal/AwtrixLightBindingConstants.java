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

package org.openhab.binding.mqtt.awtrixlight.internal;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AwtrixLightBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixLightBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_APP = new ThingTypeUID(BINDING_ID, "awtrix-app");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "awtrix-clock");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_APP, THING_TYPE_BRIDGE);

    // Matrix Size
    public static final int SCREEN_HEIGHT = 8;
    public static final int SCREEN_WIDTH = 32;

    // Clock Properties
    public static final String PROP_APPID = "appid";
    public static final String PROP_APPLOCKTIMEOUT = "appLockTimeout";
    public static final String PROP_APPNAME = "appname";
    public static final String PROP_APP_CONTROLLABLE = "useButtons";
    public static final String PROP_BASETOPIC = "basetopic";
    public static final String PROP_DISCOVERDEFAULT = "discoverDefaultApps";
    public static final String PROP_FIRMWARE = "firmware";
    public static final String PROP_UNIQUEID = "uniqueId";
    public static final String PROP_VENDOR = "vendor";

    // Clock Topics
    public static final String TOPIC_BASE = "awtrix";
    public static final String TOPIC_BUTLEFT = "/stats/buttonLeft";
    public static final String TOPIC_BUTRIGHT = "/stats/buttonRight";
    public static final String TOPIC_BUTSELECT = "/stats/buttonSelect";
    public static final String TOPIC_INDICATOR1 = "/indicator1";
    public static final String TOPIC_INDICATOR2 = "/indicator2";
    public static final String TOPIC_INDICATOR3 = "/indicator3";
    public static final String TOPIC_LWT = "/stats/device";
    public static final String TOPIC_NOTIFY = "/notify";
    public static final String TOPIC_POWER = "/power";
    public static final String TOPIC_REBOOT = "/reboot";
    public static final String TOPIC_RTTTL = "/rtttl";
    public static final String TOPIC_SCREEN = "/screen";
    public static final String TOPIC_SEND_SCREEN = "/sendscreen";
    public static final String TOPIC_SETTINGS = "/settings";
    public static final String TOPIC_SLEEP = "/sleep";
    public static final String TOPIC_SOUND = "/sound";
    public static final String TOPIC_STATS = "/stats";
    public static final String TOPIC_STATS_CURRENT_APP = "/stats/currentApp";
    public static final String TOPIC_SWITCH = "/switch";
    public static final String TOPIC_UPGRADE = "/doupdate";

    // Stats fields
    public static final String FIELD_BRIDGE_APP = "app";
    public static final String FIELD_BRIDGE_BATTERY = "bat";
    public static final String FIELD_BRIDGE_BATTERY_RAW = "bat_raw";
    public static final String FIELD_BRIDGE_BRIGHTNESS = "bri";
    public static final String FIELD_BRIDGE_FIRMWARE = "version";
    public static final String FIELD_BRIDGE_HUMIDITY = "hum";
    public static final String FIELD_BRIDGE_INDICATOR1 = "indicator1";
    public static final String FIELD_BRIDGE_INDICATOR2 = "indicator2";
    public static final String FIELD_BRIDGE_INDICATOR3 = "indicator3";
    public static final String FIELD_BRIDGE_INDICATOR1_COLOR = "indicator1-color";
    public static final String FIELD_BRIDGE_INDICATOR2_COLOR = "indicator2-color";
    public static final String FIELD_BRIDGE_INDICATOR3_COLOR = "indicator3-color";
    public static final String FIELD_BRIDGE_LDR_RAW = "ldr_raw";
    public static final String FIELD_BRIDGE_LUX = "lux";
    public static final String FIELD_BRIDGE_MATRIX = "matrix";
    public static final String FIELD_BRIDGE_MESSAGES = "messages";
    public static final String FIELD_BRIDGE_RAM = "ram";
    public static final String FIELD_BRIDGE_TEMPERATURE = "temp";
    public static final String FIELD_BRIDGE_TYPE = "type";
    public static final String FIELD_BRIDGE_UID = "uid";
    public static final String FIELD_BRIDGE_UPTIME = "uptime";
    public static final String FIELD_BRIDGE_WIFI_SIGNAL = "wifi_signal";

    // Settings fields
    public static final String FIELD_BRIDGE_SET_APP_TIME = "ATIME";
    public static final String FIELD_BRIDGE_SET_AUTO_BRIGHTNESS = "ABRI";
    public static final String FIELD_BRIDGE_SET_AUTO_TRANSITION = "ATRANS";
    public static final String FIELD_BRIDGE_SET_BLOCK_KEYS = "BLOCKN";
    public static final String FIELD_BRIDGE_SET_BRIGHTNESS = "BRI";
    public static final String FIELD_BRIDGE_SET_DISPLAY = "MATP";
    public static final String FIELD_BRIDGE_SET_MUTE = "SOUND";
    public static final String FIELD_BRIDGE_SET_SCROLL_SPEED = "SSPEED";
    public static final String FIELD_BRIDGE_SET_TEXT_COLOR = "TCOL";
    public static final String FIELD_BRIDGE_SET_TRANS_EFFECT = "TEFF";
    public static final String FIELD_BRIDGE_SET_TRANS_SPEED = "TSPEED";

    // Apps
    public static final String BASE_APP_TOPIC = "/custom";
    public static final String[] DEFAULT_APPS = { "Time", "Date", "Temperature", "Humidity", "Battery" };

    // Common Channels
    public static final String CHANNEL_BUTLEFT = "button-left";
    public static final String CHANNEL_BUTRIGHT = "button-right";
    public static final String CHANNEL_BUTSELECT = "button-select";

    // Clock Channels
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_AUTO_BRIGHTNESS = "auto-brightness";
    public static final String CHANNEL_BATTERY = "battery-level";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_DISPLAY = "display";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_INDICATOR1 = "indicator-1";
    public static final String CHANNEL_INDICATOR2 = "indicator-2";
    public static final String CHANNEL_INDICATOR3 = "indicator-3";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_LUX = "lux";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_RTTTL = "rtttl";
    public static final String CHANNEL_SCREEN = "screen";
    public static final String CHANNEL_SOUND = "sound";
    public static final String CHANNEL_TEMPERATURE = "temperature";

    // App Channels
    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_AUTOSCALE = "autoscale";
    public static final String CHANNEL_BACKGROUND = "background";
    public static final String CHANNEL_BAR = "bar";
    public static final String CHANNEL_BLINK_TEXT = "blink-text";
    public static final String CHANNEL_CENTER = "center";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_EFFECT_BLEND = "effect-blend";
    public static final String CHANNEL_EFFECT_PALETTE = "effect-palette";
    public static final String CHANNEL_EFFECT_SPEED = "effect-speed";
    public static final String CHANNEL_FADE_TEXT = "fade-text";
    public static final String CHANNEL_GRADIENT_COLOR = "gradient-color";
    public static final String CHANNEL_ICON = "icon";
    public static final String CHANNEL_LIFETIME = "lifetime";
    public static final String CHANNEL_LIFETIME_MODE = "lifetime-mode";
    public static final String CHANNEL_LINE = "line";
    public static final String CHANNEL_OVERLAY = "overlay";
    public static final String CHANNEL_PROGRESS = "progress";
    public static final String CHANNEL_PROGRESSC = "progress-color";
    public static final String CHANNEL_PROGRESSBC = "progress-background";
    public static final String CHANNEL_PUSH_ICON = "push-icon";
    public static final String CHANNEL_RAINBOW = "rainbow";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_RESET = "reset";
    public static final String CHANNEL_SCROLLSPEED = "scroll-speed";
    public static final String CHANNEL_TEXT = "text";
    public static final String CHANNEL_TEXTCASE = "text-case";
    public static final String CHANNEL_TEXT_OFFSET = "text-offset";
    public static final String CHANNEL_TOP_TEXT = "top-text";

    public static final String PUSH_ICON_OPTION_0 = "STATIC";
    public static final String PUSH_ICON_OPTION_1 = "PUSHOUT";
    public static final String PUSH_ICON_OPTION_2 = "PUSHOUTRETURN";
}
