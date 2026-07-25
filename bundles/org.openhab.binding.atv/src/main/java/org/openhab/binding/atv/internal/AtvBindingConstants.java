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
package org.openhab.binding.atv.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Common constants used across the Apple TV binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvBindingConstants {

    public static final String BINDING_ID = "atv";

    // Thing types
    public static final ThingTypeUID THING_TYPE_APPLETV = new ThingTypeUID(BINDING_ID, "appletv");
    public static final ThingTypeUID THING_TYPE_SPEAKER = new ThingTypeUID(BINDING_ID, "speaker");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_APPLETV, THING_TYPE_SPEAKER);

    // Config parameters
    public static final String CONFIG_MAC = "macAddress";
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_NAME = "name";
    public static final String CONFIG_AIRPLAY_PIN = "airplayPin";
    public static final String CONFIG_COMPANION_PIN = "companionPin";
    public static final String CONFIG_RAOP_PIN = "raopPin";
    public static final String CONFIG_AIRPLAY_CREDENTIALS = "airplayCredentials";
    public static final String CONFIG_COMPANION_CREDENTIALS = "companionCredentials";
    public static final String CONFIG_RAOP_CREDENTIALS = "raopCredentials";
    public static final String CONFIG_PASSWORD = "password";

    // Thing properties
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_OS_VERSION = "osVersion";
    public static final String PROPERTY_BUILD_NUMBER = "buildNumber";

    // Channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MEDIA_CONTROL = "media-control";
    public static final String CHANNEL_REMOTE_KEY = "remote-key";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_GENRE = "genre";
    public static final String CHANNEL_MEDIA_TYPE = "media-type";
    public static final String CHANNEL_PLAYBACK_STATE = "playback-state";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_PROGRESS = "progress";
    public static final String CHANNEL_SHUFFLE = "shuffle";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_SERIES_NAME = "series-name";
    public static final String CHANNEL_SEASON_NUMBER = "season-number";
    public static final String CHANNEL_EPISODE_NUMBER = "episode-number";
    public static final String CHANNEL_CONTENT_ID = "content-id";
    public static final String CHANNEL_ITUNES_ID = "itunes-id";
    public static final String CHANNEL_ARTWORK = "artwork";
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_APP_NAME = "app-name";
    public static final String CHANNEL_ACCOUNT = "account";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_OUTPUT_DEVICES = "output-devices";
    public static final String CHANNEL_OUTPUT_DEVICE_VOLUME = "output-device-volume";
    public static final String CHANNEL_KEYBOARD_INPUT = "keyboard-input";
    public static final String CHANNEL_KEYBOARD_FOCUS = "keyboard-focus";
    public static final String CHANNEL_TOUCH_GESTURE = "touch-gesture";
    public static final String CHANNEL_PLAY_URL = "play-url";
    public static final String CHANNEL_STREAM_URL = "stream-url";

    // mDNS service type used for discovery
    public static final String MDNS_AIRPLAY = "_airplay._tcp.local.";
}
