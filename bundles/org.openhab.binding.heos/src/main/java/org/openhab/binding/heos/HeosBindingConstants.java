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
package org.openhab.binding.heos;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.heos.internal.resources.HeosConstants;

/**
 * The {@link HeosBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosBindingConstants extends HeosConstants {

    public static final String BINDING_ID = "heos";

    // List of all Bridge Type UIDs

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");

    // List off all Channel Types

    public static final ChannelTypeUID CH_TYPE_PLAYER = new ChannelTypeUID(BINDING_ID, "chPlayer");
    public static final ChannelTypeUID CH_TYPE_FAVORITE = new ChannelTypeUID(BINDING_ID, "chFavorite");
    public static final ChannelTypeUID CH_TYPE_GROUP = new ChannelTypeUID(BINDING_ID, "chGroup");

    // List of all Channel IDs
    public static final String CH_ID_CONTROL = "Control";
    public static final String CH_ID_VOLUME = "Volume";
    public static final String CH_ID_MUTE = "Mute";
    public static final String CH_ID_UNGROUP = "Ungroup";
    public static final String CH_ID_SONG = "Title";
    public static final String CH_ID_ARTIST = "Artist";
    public static final String CH_ID_ALBUM = "Album";
    public static final String CH_ID_PLAYER = "Player";
    public static final String CH_ID_BUILDGROUP = "BuildGroup";
    public static final String CH_ID_DYNGROUPSHAND = "DynamicGroupHandling";
    public static final String CH_ID_REBOOT = "Reboot";
    public static final String CH_ID_COVER = "Cover";
    public static final String CH_ID_PLAYLISTS = "Playlists";
    public static final String CH_ID_INPUTS = "Inputs";
    public static final String CH_ID_CUR_POS = "CurrentPosition";
    public static final String CH_ID_DURATION = "Duration";
    public static final String CH_ID_STATION = "Station";
    public static final String CH_ID_RAW_COMMAND = "RawCommand";
    public static final String CH_ID_TYPE = "Type";
    public static final String CH_ID_PLAY_URL = "PlayUrl";
    public static final String CH_ID_SHUFFLE_MODE = "Shuffel";
    public static final String CH_ID_REPEAT_MODE = "RepeatMode";

    // Values for Bridge, Player and Group Properties;
    // Using this values to display the correct name
    // within the thing properties.

    public static final String PROP_PID = "pid";
    public static final String PROP_GROUP_MEMBERS = "members";
    public static final String PROP_NAME = "Name";
    public static final String PROP_GID = "Group ID";
    public static final String PROP_MODEL = "Model";
    public static final String PROP_IP = "IP Address";
    public static final String PROP_NETOWRK = "Connection";
    public static final String PROP_VERSION = "Version";
    public static final String PROP_GROUP_HASH = "Members Hash value";
    public static final String PROP_GROUP_LEADER = "Group leader";
    public static final String PROP_ROLE = "Role";
    public static final String PROP_BRIDGE = "Heos Bridge";

    public static final String HOST = "ipAddress";
    public static final String PLAYER_TYPE = "model";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String HEARTBEAT = "heartbeat";
    public static final String TYPE_BRIDGE = "heosBridge";

    public static final String PLAYER = "Player";
    public static final String GROUP = "Group";

    public static final String ONLINE = "ONLINE";
    public static final String OFFLINE = "OFFLINE";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_BRIDGE, THING_TYPE_GROUP, THING_TYPE_PLAYER).collect(Collectors.toSet()));

}
