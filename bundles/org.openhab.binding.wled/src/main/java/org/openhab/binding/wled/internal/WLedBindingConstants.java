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
package org.openhab.binding.wled.internal;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WLedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WLedBindingConstants {

    public static final String BINDING_ID = "wled";
    public static final String BRIDGE_TYPE_ID = "json";
    public static final BigDecimal BIG_DECIMAL_2_55 = new BigDecimal(2.55);
    public static final BigDecimal BIG_DECIMAL_182_04 = new BigDecimal(182.04);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SEGMENT = new ThingTypeUID(BINDING_ID, "segment");
    public static final ThingTypeUID THING_TYPE_JSON = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SEGMENT, THING_TYPE_JSON);

    // Configs
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_POLL_TIME = "pollTime";
    public static final String CONFIG_SEGMENT_INDEX = "segmentIndex";
    public static final String CONFIG_SAT_THRESHOLD = "saturationThreshold";

    // Channels
    public static final String CHANNEL_GLOBAL_BRIGHTNESS = "globalBrightness";
    public static final String CHANNEL_MASTER_CONTROLS = "masterControls";
    public static final String CHANNEL_SEGMENT_BRIGHTNESS = "segmentBrightness";
    public static final String CHANNEL_PRIMARY_COLOR = "primaryColor";
    public static final String CHANNEL_SECONDARY_COLOR = "secondaryColor";
    public static final String CHANNEL_THIRD_COLOR = "tertiaryColor";
    public static final String CHANNEL_PRIMARY_WHITE = "primaryWhite";
    public static final String CHANNEL_SECONDARY_WHITE = "secondaryWhite";
    public static final String CHANNEL_THIRD_WHITE = "tertiaryWhite";
    public static final String CHANNEL_PALETTES = "palettes";
    public static final String CHANNEL_PRESETS = "presets";
    public static final String CHANNEL_PLAYLISTS = "playlists";
    public static final String CHANNEL_PRESET_DURATION = "presetDuration";
    public static final String CHANNEL_TRANS_TIME = "transformTime";
    public static final String CHANNEL_PRESET_CYCLE = "presetCycle";
    public static final String CHANNEL_FX = "fx";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_INTENSITY = "intensity";
    public static final String CHANNEL_MIRROR = "mirror";
    public static final String CHANNEL_REVERSE = "reverse";
    public static final String CHANNEL_GROUPING = "grouping";
    public static final String CHANNEL_SPACING = "spacing";
    public static final String CHANNEL_LIVE_OVERRIDE = "liveOverride";
    public static final String CHANNEL_SLEEP = "sleep";
    public static final String CHANNEL_SLEEP_MODE = "sleepMode";
    public static final String CHANNEL_SLEEP_DURATION = "sleepDuration";
    public static final String CHANNEL_SLEEP_BRIGHTNESS = "sleepTargetBrightness";
    public static final String CHANNEL_SYNC_SEND = "syncSend";
    public static final String CHANNEL_SYNC_RECEIVE = "syncReceive";
    public static final String CHANNEL_SLEEP_REMAINING = "sleepRemaining";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_FREE_HEAP = "freeHeap";
    public static final String CHANNEL_WIFI_SIGNAL = "wifiSignal";
    public static final String CHANNEL_CURRENT = "estimatedCurrent";
    public static final String CHANNEL_FPS = "fps";
    public static final String CHANNEL_WHITE_TEMPERATURE = "whiteTemperature";
    public static final String CHANNEL_CUSTOM_1 = "custom1";
    public static final String CHANNEL_CUSTOM_2 = "custom2";
    public static final String CHANNEL_CUSTOM_3 = "custom3";
    public static final String CHANNEL_OPTION_1 = "option1";
    public static final String CHANNEL_OPTION_2 = "option2";
    public static final String CHANNEL_OPTION_3 = "option3";
    public static final String CHANNEL_FREEZE = "freeze";
    public static final String CHANNEL_SELECTED = "selected";
    public static final String CHANNEL_SEGMENT_START = "segmentStart";
    public static final String CHANNEL_SEGMENT_STOP = "segmentStop";
    public static final String CHANNEL_SEGMENT_NAME = "segmentName";
    public static final String CHANNEL_SYNC_SEND_GROUPS = "syncSendGroups";
    public static final String CHANNEL_SYNC_RECEIVE_GROUPS = "syncReceiveGroups";
    public static final String CHANNEL_MAIN_SEGMENT = "mainSegment";
    public static final String CHANNEL_LED_MAP = "ledMap";
    public static final String CHANNEL_LIVE_ACTIVE = "liveActive";
    public static final String CHANNEL_LIVE_MODE = "liveMode";
    public static final String CHANNEL_LIVE_SOURCE = "liveSource";
    public static final String CHANNEL_AUDIO_REACTIVE = "audioReactive";
    public static final String CHANNEL_AUDIO_SYNC_MODE = "audioSyncMode";
    public static final String CHANNEL_MAPPING = "mapping";
    public static final String CHANNEL_SOUND_SIMULATION = "soundSimulation";

    // Properties
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_LED_COUNT = "ledCount";
    public static final String PROPERTY_ARCHITECTURE = "architecture";
    public static final String PROPERTY_BRAND = "brand";

    // Firmware versions as int, 0.14.0 is 140
    public static final int FIRMWARE_0_14 = 140;
}
