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
package org.openhab.binding.awtrixng.internal;

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AwtrixNgBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class AwtrixNgBindingConstants {

    public static final String BINDING_ID = "awtrixng";

    public static final ThingTypeUID THING_TYPE_CLOCK = new ThingTypeUID(BINDING_ID, "clock");
    public static final ThingTypeUID THING_TYPE_APP = new ThingTypeUID(BINDING_ID, "app");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_CLOCK, THING_TYPE_APP);

    // Topic suffixes, appended to the base topic ("prefix") of the clock
    public static final String TOPIC_AVAILABILITY = "/availability";
    public static final String TOPIC_STATE = "/state";
    public static final String TOPIC_STATE_DEVICE = TOPIC_STATE + "/device";
    public static final String TOPIC_STATE_SETTINGS = TOPIC_STATE + "/settings";
    public static final String TOPIC_STATE_ACTIVE_APP = TOPIC_STATE + "/apps/active";
    public static final String TOPIC_STATE_BUTTON_LEFT = TOPIC_STATE + "/buttons/left";
    public static final String TOPIC_STATE_BUTTON_SELECT = TOPIC_STATE + "/buttons/select";
    public static final String TOPIC_STATE_BUTTON_RIGHT = TOPIC_STATE + "/buttons/right";
    public static final String TOPIC_STATE_SCREEN = TOPIC_STATE + "/screen";
    public static final String TOPIC_STATE_CAPABILITIES = TOPIC_STATE + "/capabilities";
    public static final String TOPIC_EVENT_ERROR = "/event/error";

    public static final String TOPIC_CMD_SETTINGS = "/cmd/settings";
    public static final String TOPIC_CMD_DISPLAY = "/cmd/display";
    public static final String TOPIC_CMD_MOODLIGHT = "/cmd/display/moodlight";
    public static final String TOPIC_CMD_INDICATOR = "/cmd/indicators/";
    public static final String TOPIC_CMD_AUDIO_PLAY = "/cmd/audio/play";
    public static final String TOPIC_CMD_APPS_SWITCH = "/cmd/apps/switch";
    public static final String TOPIC_CMD_SCREEN_GET = "/cmd/screen/get";
    public static final String TOPIC_CMD_AUDIO_STOP = "/cmd/audio/stop";
    public static final String TOPIC_CMD_DEVICE_REBOOT = "/cmd/device/reboot";
    public static final String TOPIC_CMD_DEVICE_SLEEP = "/cmd/device/sleep";
    public static final String TOPIC_CMD_NOTIFY = "/cmd/notify";
    public static final String TOPIC_CMD_NOTIFY_DISMISS = "/cmd/notify/dismiss";
    public static final String TOPIC_CMD_APPS_NEXT = "/cmd/apps/next";
    public static final String TOPIC_CMD_APPS_PREVIOUS = "/cmd/apps/previous";
    public static final String TOPIC_CMD_APPS_ORDER = "/cmd/apps/order";
    public static final String TOPIC_CMD_APPS = "/cmd/apps/";
    public static final String TOPIC_CMD_APPS_PUSHED = TOPIC_CMD_APPS + "pushed/";
    // Retained copy of the state of every app, kept by the binding itself: the clock does not know this topic, and
    // pushed apps are gone from it after a reboot. It is what restores the apps after openHAB restarts.
    public static final String TOPIC_STORE_APPS = "/openhab/apps/";
    // Retained while an app lock is on, with the settings to go back to. NG keeps the lock over a restart, so if
    // openHAB stops before it can end the lock, the next run finds the settings here.
    public static final String TOPIC_STORE_APP_LOCK = "/openhab/applock";

    public static final String AVAILABILITY_ONLINE = "online";
    public static final String BUTTON_PRESSED_PAYLOAD = "1";
    public static final String EVENT_PRESSED = "PRESSED";
    public static final String EVENT_RELEASED = "RELEASED";

    // Channels of the clock
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_AUTO_BRIGHTNESS = "auto-brightness";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_BATTERY_VOLTAGE = "battery-voltage";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_BUTTON_LEFT = "button-left";
    public static final String CHANNEL_BUTTON_RIGHT = "button-right";
    public static final String CHANNEL_BUTTON_SELECT = "button-select";
    public static final String CHANNEL_DISPLAY = "display";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_FPS = "fps";
    public static final String CHANNEL_FREE_HEAP = "free-heap";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_INDICATOR_PREFIX = "indicator-";
    public static final String CHANNEL_IP_ADDRESS = "ip-address";
    public static final String CHANNEL_LIGHT_LEVEL = "light-level";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_MOODLIGHT = "moodlight";
    public static final String CHANNEL_OVERLAY = "overlay";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_RTTTL = "rtttl";
    public static final String CHANNEL_SCREEN = "screen";
    public static final String CHANNEL_SOUND = "sound";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TRANSITION_EFFECT = "transition-effect";
    public static final String CHANNEL_UPTIME = "uptime";

    // Channels of an app whose names the clock tells in its capabilities
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_FONT = "font";

    // Control channels of an app, besides the ones that are fields of its payload
    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_RESET = "reset";

    // NG accepts these names for pushed apps
    public static final Pattern APP_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    public static final int INDICATOR_COUNT = 3;
    // The tracks of the DFPlayer that NG plays
    public static final int MIN_TRACK = 1;
    public static final int MAX_TRACK = 2999;
    // The longest time NG takes for the blinking and the fading of an indicator
    public static final int INDICATOR_MAX_MS = 65535;
    // The groups of sounds that can be stopped, no group stops all
    public static final Set<String> SOUND_GROUPS = Set.of("alert", "app", "radio");

    // Apps that the clock has of its own; a pushed app of the same name would shadow them
    public static final Set<String> BUILT_IN_APPS = Set.of("Time", "Date", "Temperature", "Humidity", "Battery");
    // The clock shows this while a notification is displayed, it is no app
    public static final String NOTIFICATION_APP = "Notification";

    // While an app is locked for button control, the third indicator shows it, and goes red in warning before the end
    public static final int APP_LOCK_INDICATOR = 3;
    public static final String APP_LOCK_INDICATOR_COLOR = "#FF0000";
    public static final int APP_LOCK_WARNING_SECONDS = 3;
    public static final int APP_LOCK_WARNING_FADE_MS = 500;

    // Thing properties
    public static final String PROP_FIRMWARE = "firmware";
    public static final String PROP_UNIQUEID = "uniqueId";
    public static final String PROP_BOARD_TYPE = "boardType";
    public static final String PROP_MAC_ADDRESS = "macAddress";
    public static final String PROP_HOSTNAME = "hostname";

    public static final String PROP_APPID = "appid";

    // Configuration parameters, filled in by the discovery
    public static final String PROP_BASETOPIC = "basetopic";
    public static final String PROP_APPNAME = "appname";
}
