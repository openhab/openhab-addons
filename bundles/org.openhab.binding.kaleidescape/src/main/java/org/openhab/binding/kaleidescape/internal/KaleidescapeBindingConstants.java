/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.kaleidescape.internal;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KaleidescapeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeBindingConstants {
    public static final String BINDING_ID = "kaleidescape";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER_ZONE = new ThingTypeUID(BINDING_ID, "player");

    public static final int DEFAULT_API_PORT = 10000;
    public static final short DISCOVERY_SUBNET_MASK = 24;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;

    // Component Types
    public static final String PLAYER = "Player";
    public static final String CINEMA_ONE = "Cinema One";
    public static final String ALTO = "Alto";
    public static final String STRATO = "Strato";
    public static final String DISC_VAULT = "Disc Vault";

    public static final ArrayList<String> ALLOWED_DEVICES = new ArrayList<String>(
            Arrays.asList(PLAYER, CINEMA_ONE, ALTO, STRATO, DISC_VAULT));

    // List of all Channels
    public static final String POWER = "ui#power";
    public static final String VOLUME = "ui#volume";
    public static final String MUTE = "ui#mute";
    public static final String CONTROL = "ui#control";
    public static final String TITLE_NAME = "ui#title_name";
    public static final String PLAY_MODE = "ui#play_mode";
    public static final String PLAY_SPEED = "ui#play_speed";
    public static final String TITLE_NUM = "ui#title_num";
    public static final String TITLE_LENGTH = "ui#title_length";
    public static final String TITLE_LOC = "ui#title_loc";
    public static final String CHAPTER_NUM = "ui#chapter_num";
    public static final String CHAPTER_LENGTH = "ui#chapter_length";
    public static final String CHAPTER_LOC = "ui#chapter_loc";
    public static final String MOVIE_MEDIA_TYPE = "ui#movie_media_type";
    public static final String MOVIE_LOCATION = "ui#movie_location";
    public static final String ASPECT_RATIO = "ui#aspect_ratio";
    public static final String VIDEO_MODE = "ui#video_mode";
    public static final String VIDEO_MODE_COMPOSITE = "ui#video_mode_composite";
    public static final String VIDEO_MODE_COMPONENT = "ui#video_mode_component";
    public static final String VIDEO_MODE_HDMI = "ui#video_mode_hdmi";
    public static final String VIDEO_COLOR = "ui#video_color";
    public static final String VIDEO_COLOR_EOTF = "ui#video_color_eotf";
    public static final String CONTENT_COLOR = "ui#content_color";
    public static final String CONTENT_COLOR_EOTF = "ui#content_color_eotf";
    public static final String SCALE_MODE = "ui#scale_mode";
    public static final String SCREEN_MASK = "ui#screen_mask";
    public static final String SCREEN_MASK2 = "ui#screen_mask2";
    public static final String CINEMASCAPE_MASK = "ui#cinemascape_mask";
    public static final String CINEMASCAPE_MODE = "ui#cinemascape_mode";
    public static final String UI_STATE = "ui#ui_state";
    public static final String CHILD_MODE_STATE = "ui#child_mode_state";
    public static final String HIGHLIGHTED_SELECTION = "ui#highlighted_selection";
    public static final String USER_DEFINED_EVENT = "ui#user_defined_event";
    public static final String USER_INPUT = "ui#user_input";
    public static final String USER_INPUT_PROMPT = "ui#user_input_prompt";

    public static final String MUSIC = "music#";
    public static final String MUSIC_CONTROL = "music#control";
    public static final String MUSIC_REPEAT = "music#repeat";
    public static final String MUSIC_RANDOM = "music#random";
    public static final String MUSIC_TRACK = "music#track";
    public static final String MUSIC_ARTIST = "music#artist";
    public static final String MUSIC_ALBUM = "music#album";
    public static final String MUSIC_PLAY_MODE = "music#play_mode";
    public static final String MUSIC_PLAY_SPEED = "music#play_speed";
    public static final String MUSIC_TRACK_LENGTH = "music#track_length";
    public static final String MUSIC_TRACK_POSITION = "music#track_position";
    public static final String MUSIC_TRACK_PROGRESS = "music#track_progress";
    public static final String MUSIC_TRACK_HANDLE = "music#track_handle";
    public static final String MUSIC_ALBUM_HANDLE = "music#album_handle";
    public static final String MUSIC_NOWPLAY_HANDLE = "music#nowplay_handle";

    public static final String DETAIL = "detail#";

    // metadata details - the values are keyed to what is sent by the component
    // prefaced with 'detail_' when updating the channel
    public static final String DETAIL_TYPE = "type";
    public static final String DETAIL_TITLE = "title"; // movie
    public static final String DETAIL_ALBUM_TITLE = "album_title"; // album
    public static final String DETAIL_COVER_ART = "cover_art"; // both
    public static final String DETAIL_COVER_URL = "cover_url"; // both
    public static final String DETAIL_HIRES_COVER_URL = "hires_cover_url"; // both
    public static final String DETAIL_RATING = "rating"; // movie
    public static final String DETAIL_YEAR = "year"; // both
    public static final String DETAIL_RUNNING_TIME = "running_time"; // both
    public static final String DETAIL_ACTORS = "actors"; // movie
    public static final String DETAIL_ARTIST = "artist"; // album
    public static final String DETAIL_DIRECTORS = "directors"; // movie
    public static final String DETAIL_GENRES = "genres"; // both
    public static final String DETAIL_RATING_REASON = "rating_reason"; // movie
    public static final String DETAIL_SYNOPSIS = "synopsis"; // movie
    public static final String DETAIL_REVIEW = "review"; // album
    public static final String DETAIL_COLOR_DESCRIPTION = "color_description"; // movie
    public static final String DETAIL_COUNTRY = "country"; // movie
    public static final String DETAIL_ASPECT_RATIO = "aspect_ratio"; // movie
    public static final String DETAIL_DISC_LOCATION = "disc_location"; // both

    // make a list of all allowed metatdata channels,
    // used to filter out what we don't want from the component
    public static ArrayList<String> metadataChannels = new ArrayList<String>(
            Arrays.asList(DETAIL_TITLE, DETAIL_ALBUM_TITLE, DETAIL_COVER_URL, DETAIL_HIRES_COVER_URL, DETAIL_RATING,
                    DETAIL_YEAR, DETAIL_RUNNING_TIME, DETAIL_ACTORS, DETAIL_ARTIST, DETAIL_DIRECTORS, DETAIL_GENRES,
                    DETAIL_RATING_REASON, DETAIL_SYNOPSIS, DETAIL_REVIEW, DETAIL_COLOR_DESCRIPTION, DETAIL_COUNTRY,
                    DETAIL_ASPECT_RATIO, DETAIL_DISC_LOCATION));

    public static final String SYSTEM_READINESS_STATE = "system#readiness_state";
    public static final String COMPONENT_TYPE = "system#component_type";
    public static final String FRIENDLY_NAME = "system#friendly_name";
    public static final String SERIAL_NUMBER = "system#serial_number";
    public static final String CONTROL_PROTOCOL_ID = "system#control_protocol_id";
    public static final String SYSTEM_VERSION = "system#system_version";
    public static final String PROTOCOL_VERSION = "system#protocol_version";
}
