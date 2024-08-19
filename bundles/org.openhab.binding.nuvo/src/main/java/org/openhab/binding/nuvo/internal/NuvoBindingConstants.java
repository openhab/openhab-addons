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
package org.openhab.binding.nuvo.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NuvoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class NuvoBindingConstants {

    public static final String BINDING_ID = "nuvo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AMP = new ThingTypeUID(BINDING_ID, "amplifier");

    // List of all Channel types
    // system
    public static final String CHANNEL_TYPE_ALLOFF = "alloff";
    public static final String CHANNEL_TYPE_ALLMUTE = "allmute";
    public static final String CHANNEL_TYPE_PAGE = "page";
    public static final String CHANNEL_TYPE_SENDCMD = "sendcmd";
    public static final String CHANNEL_TYPE_BUTTONPRESS = "buttonpress";

    // zone
    public static final String CHANNEL_TYPE_POWER = "power";
    public static final String CHANNEL_TYPE_SOURCE = "source";
    public static final String CHANNEL_TYPE_FAVORITE = "favorite";
    public static final String CHANNEL_TYPE_VOLUME = "volume";
    public static final String CHANNEL_TYPE_MUTE = "mute";
    public static final String CHANNEL_TYPE_CONTROL = "control";
    public static final String CHANNEL_TYPE_TREBLE = "treble";
    public static final String CHANNEL_TYPE_BASS = "bass";
    public static final String CHANNEL_TYPE_BALANCE = "balance";
    public static final String CHANNEL_TYPE_LOUDNESS = "loudness";
    public static final String CHANNEL_TYPE_DND = "dnd";
    public static final String CHANNEL_TYPE_LOCK = "lock";
    public static final String CHANNEL_TYPE_PARTY = "party";

    // source
    public static final String CHANNEL_DISPLAY_LINE = "display_line";
    public static final String CHANNEL_DISPLAY_LINE1 = "display_line1";
    public static final String CHANNEL_DISPLAY_LINE2 = "display_line2";
    public static final String CHANNEL_DISPLAY_LINE3 = "display_line3";
    public static final String CHANNEL_DISPLAY_LINE4 = "display_line4";
    public static final String CHANNEL_PLAY_MODE = "play_mode";
    public static final String CHANNEL_TRACK_LENGTH = "track_length";
    public static final String CHANNEL_TRACK_POSITION = "track_position";
    public static final String CHANNEL_BUTTON_PRESS = "button_press";
    public static final String CHANNEL_ART_URL = "art_url";
    public static final String CHANNEL_ALBUM_ART = "album_art";
    public static final String CHANNEL_SOURCE_MENU = "source_menu";

    // Message types
    public static final String TYPE_VERSION = "version";
    public static final String TYPE_ALLOFF = "alloff";
    public static final String TYPE_ALLMUTE = "allmute";
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_SOURCE_UPDATE = "source_update";
    public static final String TYPE_ZONE_UPDATE = "zone_update";
    public static final String TYPE_ZONE_CONFIG = "zone_config";
    public static final String TYPE_ZONE_SOURCE_BUTTON = "zone_source_button";
    public static final String TYPE_NN_BUTTON = "nn_button";
    public static final String TYPE_NN_MENUREQ = "nn_menureq";
    public static final String TYPE_NN_MENU_ITEM_SELECTED = "nn_menu_item_selected";
    public static final String TYPE_NN_ALBUM_ART_REQ = "nn_album_art_req";
    public static final String TYPE_NN_ALBUM_ART_FRAG_REQ = "nn_album_art_frag_req";
    public static final String TYPE_NN_FAVORITE_REQ = "nn_favorite_req";

    // misc
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String ALLOFF = "ALLOFF";
    public static final String TWO = "2";
    public static final String ONE = "1";
    public static final String ZERO = "0";
    public static final String BLANK = "";
    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String DISPLINE = "DISPLINE";
    public static final String DISPINFO = "DISPINFO,"; // yes comma here
    public static final String DISP_INFO_TWO = "DISPINFOTWO";
    public static final String NAME_QUOTE = "NAME\"";
    public static final String MUTE = "MUTE";
    public static final String VOL = "VOL";
    public static final String OFFSET_ZERO = "0x";
    public static final String ZERO_COMMA = "0,0";

    // MPS4
    public static final String TYPE_PING = "PING";
    public static final String TYPE_RESTART = "RESTART";
    public static final String DISABLE = "disable";
    public static final String ALBUM_ART_ID = "albumartid";
    public static final String ALBUM_ART_AVAILABLE = "ALBUMARTAVAILABLE";
    public static final String ALBUM_ART_FRAG = "ALBUMARTFRAG";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String PLAY_MUSIC_PRESET = "PLAY_MUSIC_PRESET:";

    public static final List<String> MPS4_PLAYING_MODES = List.of("2", "6", "7", "8");
    public static final List<String> MPS4_IDLE_MODES = List.of("0", "1");

    public static final String GET_MCS_INSTANCE = "http://%s/api/Script/MRAD.SetZone%%20Zone_%s/MRAD.GetStatus/?clientId=%s";
    public static final String GET_MCS_STATUS = "http://%s/api/Script/SetInstance%%20%s/GetStatus?clientId=%s";
    public static final String GET_MCS_JSON = "http://%s/api/?clientId=%s";
    public static final String GET_MCS_ART = "http://%s/getArt?guid=%s&instance=%s&h=%s&w=%s&changed=true&c=1&fmt=jpg";
}
