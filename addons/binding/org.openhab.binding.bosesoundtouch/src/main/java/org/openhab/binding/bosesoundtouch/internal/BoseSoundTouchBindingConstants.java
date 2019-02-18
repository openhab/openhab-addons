/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BoseSoundTouchBindinConstantsg} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
public class BoseSoundTouchBindingConstants {

    public static final String BINDING_ID = "bosesoundtouch";

    // List of all Thing Type UIDs
    public final static ThingTypeUID BST_UNKNOWN_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "device");

    public final static ThingTypeUID BST_10_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "10");
    public final static ThingTypeUID BST_20_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "20");
    public final static ThingTypeUID BST_30_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "30");
    public final static ThingTypeUID BST_300_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "300");
    public final static ThingTypeUID BST_WLA_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "wirelessLinkAdapter");
    public final static ThingTypeUID BST_WSMS_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "waveSoundTouchMusicSystemIV");
    public final static ThingTypeUID BST_SA5A_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "sa5Amplifier");

    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(BST_UNKNOWN_THING_TYPE_UID, BST_10_THING_TYPE_UID, BST_20_THING_TYPE_UID, BST_30_THING_TYPE_UID,
                    BST_300_THING_TYPE_UID, BST_WLA_THING_TYPE_UID, BST_WSMS_THING_TYPE_UID, BST_SA5A_THING_TYPE_UID)
            .collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(SUPPORTED_KNOWN_THING_TYPES_UIDS);

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

    // Device information parameters;
    public static final String DEVICE_INFO_NAME = "INFO_NAME";
    public static final String DEVICE_INFO_TYPE = "INFO_TYPE";
}
