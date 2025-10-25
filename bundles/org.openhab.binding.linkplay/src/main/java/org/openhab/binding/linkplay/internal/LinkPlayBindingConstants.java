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
package org.openhab.binding.linkplay.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LinkPlayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayBindingConstants {

    private static final String BINDING_ID = "linkplay";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PLAYER);

    public static final String PROPERTY_FIRMWARE = "firmwareVersion";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_IP = "ipAddress";
    public static final String PROPERTY_MAC = "macAddress";
    public static final String PROPERTY_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_DEVICE_NAME = "deviceName";
    public static final String PROPERTY_GROUP_NAME = "groupName";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_PORT = "port";
    public static final String CONFIG_UDN = "udn";
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";

    // ----------------- Channel Group IDs -----------------
    public static final String GROUP_PLAYBACK = "playback";
    public static final String GROUP_METADATA = "metadata";
    public static final String GROUP_INPUT = "input";
    public static final String GROUP_EQUALISER = "equalizer";
    public static final String GROUP_MULTIROOM = "multiroom";
    public static final String GROUP_DEVICE = "device";
    public static final String GROUP_PRESETS = "presets";
    public static final String GROUP_PRESET = "preset";

    // ----------------- Channel IDs -----------------
    public static final String CHANNEL_PLAYER_CONTROL = "control";
    public static final String CHANNEL_PLAYBACK_STATE = "state";
    public static final String CHANNEL_TRACK_POSITION = "position";
    public static final String CHANNEL_TRACK_DURATION = "duration";
    public static final String CHANNEL_REPEAT_SHUFFLE_MODE = "repeat-shuffle-mode";
    public static final String CHANNEL_EQ_PRESET = "eq-preset";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";

    // Metadata
    public static final String CHANNEL_TRACK_TITLE = "track-title";
    public static final String CHANNEL_TRACK_ARTIST = "track-artist";
    public static final String CHANNEL_TRACK_ALBUM = "track-album";
    public static final String CHANNEL_TRACK_URI = "track-uri";
    public static final String CHANNEL_TRACK_SOURCE = "track-source";
    public static final String CHANNEL_ALBUM_ART_URL = "album-art-url";
    public static final String CHANNEL_ALBUM_ART = "album-art";
    public static final String CHANNEL_SAMPLE_RATE = "sample-rate";
    public static final String CHANNEL_BIT_DEPTH = "bit-depth";
    public static final String CHANNEL_BIT_RATE = "bit-rate";
    public static final String CHANNEL_CURRENT_PLAYLIST_NAME = "current-playlist-name";

    // Input / Source
    public static final String CHANNEL_SOURCE_INPUT = "source";
    public static final String CHANNEL_BT_CONNECTED = "bluetooth-connected";
    public static final String CHANNEL_BT_PAIRED_DEVICE = "bluetooth-paired-device";
    public static final String CHANNEL_LINE_IN_ACTIVE = "line-in-active";

    // Equaliser & Output
    public static final String CHANNEL_EQ_ENABLED = "enabled";
    public static final String CHANNEL_EQ_BAND = "band";
    public static final String CHANNEL_OUTPUT_HW_MODE = "output-hardware-mode";
    public static final String CHANNEL_CHANNEL_BALANCE = "channel-balance";
    public static final String CHANNEL_SPDIF_DELAY = "spdif-switch-delay-ms";

    // Multi-room
    public static final String CHANNEL_MULTIROOM_VOLUME = "volume";
    public static final String CHANNEL_MULTIROOM_MUTE = "mute";
    public static final String CHANNEL_MULTIROOM_ACTIVE = "active";
    public static final String CHANNEL_MULTIROOM_LEADER = "leader";
    public static final String CHANNEL_MULTIROOM_JOIN = "join";
    public static final String CHANNEL_MULTIROOM_LEAVE = "leave";
    public static final String CHANNEL_MULTIROOM_MANAGE = "manage";
    public static final String CHANNEL_MULTIROOM_UNGROUP = "ungroup";

    // Device & System
    public static final String CHANNEL_LED_ENABLED = "led-enabled";
    public static final String CHANNEL_TOUCH_KEYS_ENABLED = "touch-keys-enabled";
    public static final String CHANNEL_SHUTDOWN_TIMER = "shutdown-timer";
    public static final String CHANNEL_REBOOT = "reboot";
    public static final String CHANNEL_FACTORY_RESET = "factory-reset";

    // Presets
    public static final String CHANNEL_PRESET_COUNT = "count";
    public static final String CHANNEL_PLAY_PRESET = "play";

    // preset instance
    public static final String CHANNEL_PRESET_URL = "url";
    public static final String CHANNEL_PRESET_SOURCE = "source";
    public static final String CHANNEL_PRESET_PIC_URL = "pic-url";
    public static final String CHANNEL_PRESET_PIC = "pic";
    public static final String CHANNEL_PRESET_PLAY = "play";
    public static final String CHANNEL_PRESET_NAME = "name";

    // Group proxy channels that are set by the leader when in a group
    public static final Set<String> GROUP_PROXY_CHANNELS = Set.of(CHANNEL_PLAYER_CONTROL, CHANNEL_PLAYBACK_STATE,
            CHANNEL_TRACK_POSITION, CHANNEL_TRACK_DURATION, CHANNEL_REPEAT_SHUFFLE_MODE, CHANNEL_TRACK_TITLE,
            CHANNEL_TRACK_ARTIST, CHANNEL_TRACK_ALBUM, CHANNEL_ALBUM_ART_URL, CHANNEL_ALBUM_ART, CHANNEL_SAMPLE_RATE,
            CHANNEL_BIT_DEPTH, CHANNEL_BIT_RATE);
}
