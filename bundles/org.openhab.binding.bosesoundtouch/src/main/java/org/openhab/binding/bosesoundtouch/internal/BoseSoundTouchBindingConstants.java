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
package org.openhab.binding.bosesoundtouch.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BoseSoundTouchBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
@NonNullByDefault
public class BoseSoundTouchBindingConstants {

    public static final String BINDING_ID = "bosesoundtouch";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BST_UNKNOWN_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "device");

    public static final ThingTypeUID BST_10_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "10");
    public static final ThingTypeUID BST_20_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "20");
    public static final ThingTypeUID BST_30_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "30");
    public static final ThingTypeUID BST_300_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "300");
    public static final ThingTypeUID BST_WLA_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "wirelessLinkAdapter");
    public static final ThingTypeUID BST_WSMS_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "waveSoundTouchMusicSystemIV");
    public static final ThingTypeUID BST_SA5A_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "sa5Amplifier");

    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(BST_UNKNOWN_THING_TYPE_UID, BST_10_THING_TYPE_UID, BST_20_THING_TYPE_UID, BST_30_THING_TYPE_UID,
                    BST_300_THING_TYPE_UID, BST_WLA_THING_TYPE_UID, BST_WSMS_THING_TYPE_UID, BST_SA5A_THING_TYPE_UID)
            .collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(SUPPORTED_KNOWN_THING_TYPES_UIDS);

    // Partial list of Channel Type IDs
    public static final String CHANNEL_TYPE_OPERATION_MODE_DEFAULT = "operationMode_default";
    public static final String CHANNEL_TYPE_OPERATION_MODE_BST_10_20_30 = "operationMode_BST_10_20_30";
    public static final String CHANNEL_TYPE_OPERATION_MODE_BST_300 = "operationMode_BST_300";
    public static final String CHANNEL_TYPE_OPERATION_MODE_BST_SA5A = "operationMode_BST_SA5_Amplifier";
    public static final String CHANNEL_TYPE_OPERATION_MODE_BST_WLA = "operationMode_BST_WLA";

    // List of all Channel IDs
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_OPERATIONMODE = "operationMode";
    public static final String CHANNEL_PLAYER_CONTROL = "playerControl";
    public static final String CHANNEL_PRESET = "preset";
    public static final String CHANNEL_BASS = "bass";
    public static final String CHANNEL_RATEENABLED = "rateEnabled";
    public static final String CHANNEL_SKIPENABLED = "skipEnabled";
    public static final String CHANNEL_SKIPPREVIOUSENABLED = "skipPreviousEnabled";
    public static final String CHANNEL_SAVE_AS_PRESET = "saveAsPreset";
    public static final String CHANNEL_KEY_CODE = "keyCode";
    public static final String CHANNEL_NOWPLAYING_ALBUM = "nowPlayingAlbum";
    public static final String CHANNEL_NOWPLAYING_ARTWORK = "nowPlayingArtwork";
    public static final String CHANNEL_NOWPLAYING_ARTIST = "nowPlayingArtist";
    public static final String CHANNEL_NOWPLAYING_DESCRIPTION = "nowPlayingDescription";
    public static final String CHANNEL_NOWPLAYING_GENRE = "nowPlayingGenre";
    public static final String CHANNEL_NOWPLAYING_ITEMNAME = "nowPlayingItemName";
    public static final String CHANNEL_NOWPLAYING_STATIONLOCATION = "nowPlayingStationLocation";
    public static final String CHANNEL_NOWPLAYING_STATIONNAME = "nowPlayingStationName";
    public static final String CHANNEL_NOWPLAYING_TRACK = "nowPlayingTrack";
    public static final String CHANNEL_NOTIFICATION_SOUND = "notificationsound";

    public static final List<String> CHANNEL_IDS = Collections.unmodifiableList(
            Stream.of(CHANNEL_POWER, CHANNEL_VOLUME, CHANNEL_MUTE, CHANNEL_OPERATIONMODE, CHANNEL_PLAYER_CONTROL,
                    CHANNEL_PRESET, CHANNEL_BASS, CHANNEL_RATEENABLED, CHANNEL_SKIPENABLED, CHANNEL_SKIPPREVIOUSENABLED,
                    CHANNEL_SAVE_AS_PRESET, CHANNEL_KEY_CODE, CHANNEL_NOWPLAYING_ALBUM, CHANNEL_NOWPLAYING_ARTWORK,
                    CHANNEL_NOWPLAYING_ARTIST, CHANNEL_NOWPLAYING_DESCRIPTION, CHANNEL_NOWPLAYING_GENRE,
                    CHANNEL_NOWPLAYING_ITEMNAME, CHANNEL_NOWPLAYING_STATIONLOCATION, CHANNEL_NOWPLAYING_STATIONNAME,
                    CHANNEL_NOWPLAYING_TRACK, CHANNEL_NOTIFICATION_SOUND).collect(Collectors.toList()));

    // Device information parameters;
    public static final String DEVICE_INFO_NAME = "INFO_NAME";
    public static final String DEVICE_INFO_TYPE = "INFO_TYPE";
}
