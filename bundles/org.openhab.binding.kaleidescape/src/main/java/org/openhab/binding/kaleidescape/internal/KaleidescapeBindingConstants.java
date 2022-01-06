/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

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
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID THING_TYPE_CINEMA_ONE = new ThingTypeUID(BINDING_ID, "cinemaone");
    public static final ThingTypeUID THING_TYPE_ALTO = new ThingTypeUID(BINDING_ID, "alto");
    public static final ThingTypeUID THING_TYPE_STRATO = new ThingTypeUID(BINDING_ID, "strato");

    public static final int DEFAULT_API_PORT = 10000;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE_MS = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE_MS = 750;

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
    public static final String SYSTEM_READINESS_STATE = "ui#readiness_state";
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
    public static final String CONTENT_HANDLE = "content_handle";
    public static final String ALBUM_CONTENT_HANDLE = "album_content_handle";
    public static final String MOVIE = "movie";
    public static final String ALBUM = "album";
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
    public static final Set<String> METADATA_CHANNELS = new HashSet<String>(
            Arrays.asList(DETAIL_TITLE, DETAIL_ALBUM_TITLE, DETAIL_COVER_URL, DETAIL_HIRES_COVER_URL, DETAIL_RATING,
                    DETAIL_YEAR, DETAIL_RUNNING_TIME, DETAIL_ACTORS, DETAIL_ARTIST, DETAIL_DIRECTORS, DETAIL_GENRES,
                    DETAIL_RATING_REASON, DETAIL_SYNOPSIS, DETAIL_REVIEW, DETAIL_COLOR_DESCRIPTION, DETAIL_COUNTRY,
                    DETAIL_ASPECT_RATIO, DETAIL_DISC_LOCATION));

    public static final String STANDBY_MSG = "Device is in standby";
    public static final String PROPERTY_COMPONENT_TYPE = "Component Type";
    public static final String PROPERTY_FRIENDLY_NAME = "Friendly Name";
    public static final String PROPERTY_SERIAL_NUMBER = "Serial Number";
    public static final String PROPERTY_CONTROL_PROTOCOL_ID = "Control Protocol ID";
    public static final String PROPERTY_SYSTEM_VERSION = "System Version";
    public static final String PROPERTY_PROTOCOL_VERSION = "Protocol Version";

    public static final String GET_DEVICE_TYPE_NAME = "GET_DEVICE_TYPE_NAME";
    public static final String GET_FRIENDLY_NAME = "GET_FRIENDLY_NAME";
    public static final String GET_DEVICE_INFO = "GET_DEVICE_INFO";
    public static final String GET_SYSTEM_VERSION = "GET_SYSTEM_VERSION";
    public static final String GET_DEVICE_POWER_STATE = "GET_DEVICE_POWER_STATE";
    public static final String GET_CINEMASCAPE_MASK = "GET_CINEMASCAPE_MASK";
    public static final String GET_CINEMASCAPE_MODE = "GET_CINEMASCAPE_MODE";
    public static final String GET_SCALE_MODE = "GET_SCALE_MODE";
    public static final String GET_SCREEN_MASK = "GET_SCREEN_MASK";
    public static final String GET_SCREEN_MASK2 = "GET_SCREEN_MASK2";
    public static final String GET_VIDEO_MODE = "GET_VIDEO_MODE";
    public static final String GET_UI_STATE = "GET_UI_STATE";
    public static final String GET_HIGHLIGHTED_SELECTION = "GET_HIGHLIGHTED_SELECTION";
    public static final String GET_CHILD_MODE_STATE = "GET_CHILD_MODE_STATE";
    public static final String GET_MOVIE_LOCATION = "GET_MOVIE_LOCATION";
    public static final String GET_MOVIE_MEDIA_TYPE = "GET_MOVIE_MEDIA_TYPE";
    public static final String GET_PLAYING_TITLE_NAME = "GET_PLAYING_TITLE_NAME";
    public static final String GET_PLAY_STATUS = "GET_PLAY_STATUS";
    public static final String GET_MUSIC_NOW_PLAYING_STATUS = "GET_MUSIC_NOW_PLAYING_STATUS";
    public static final String GET_MUSIC_PLAY_STATUS = "GET_MUSIC_PLAY_STATUS";
    public static final String GET_MUSIC_TITLE = "GET_MUSIC_TITLE";
    public static final String GET_SYSTEM_READINESS_STATE = "GET_SYSTEM_READINESS_STATE";
    public static final String GET_VIDEO_COLOR = "GET_VIDEO_COLOR";
    public static final String GET_CONTENT_COLOR = "GET_CONTENT_COLOR";
    public static final String SET_STATUS_CUE_PERIOD_1 = "SET_STATUS_CUE_PERIOD:1";
    public static final String GET_TIME = "GET_TIME";
    public static final String GET_CONTENT_DETAILS = "GET_CONTENT_DETAILS:";

    public static final String LEAVE_STANDBY = "LEAVE_STANDBY";
    public static final String ENTER_STANDBY = "ENTER_STANDBY";

    public static final String PLAY = "PLAY";
    public static final String PAUSE = "PAUSE";
    public static final String NEXT = "NEXT";
    public static final String PREVIOUS = "PREVIOUS";
    public static final String SCAN_FORWARD = "SCAN_FORWARD";
    public static final String SCAN_REVERSE = "SCAN_REVERSE";

    public static final String MUSIC_REPEAT_ON = "MUSIC_REPEAT_ON";
    public static final String MUSIC_REPEAT_OFF = "MUSIC_REPEAT_OFF";
    public static final String MUSIC_RANDOM_ON = "MUSIC_RANDOM_ON";
    public static final String MUSIC_RANDOM_OFF = "MUSIC_RANDOM_OFF";

    public static final String SEND_EVENT_VOLUME_CAPABILITIES_15 = "SEND_EVENT:VOLUME_CAPABILITIES=15";
    public static final String SEND_EVENT_VOLUME_LEVEL_EQ = "SEND_EVENT:VOLUME_LEVEL=";
    public static final String SEND_EVENT_MUTE = "SEND_EVENT:MUTE_";
    public static final String MUTE_ON = "ON_FB";
    public static final String MUTE_OFF = "OFF_FB";

    public static final String ONE = "1";
    public static final String ZERO = "0";
    public static final String EMPTY = "";
}
