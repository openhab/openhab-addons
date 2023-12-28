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
package org.openhab.binding.emotiva.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.dto.ControlDTO;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EmotivaBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaBindingConstants {

    public static final String BINDING_ID = "emotiva";

    /** Property name to uniquely identify (discovered) things. */
    static final String UNIQUE_PROPERTY_NAME = "ip4Address";

    /** Default port used to discover Emotiva devices. */
    static final int DEFAULT_PING_PORT = 7000;

    /** Default port used to receive transponder (discovered) Emotiva devices. */
    static final int DEFAULT_TRANSPONDER_PORT = 7001;

    /** Default timeout in milliseconds for sending UDP packets. */
    static final int DEFAULT_UDP_SENDING_TIMEOUT = 1000;

    /** Number of connection attempts, set OFFLINE if no success and a retry job is then started. */
    static final int DEFAULT_CONNECTION_RETRIES = 3;

    /** Connection retry interval in minutes */
    static final int DEFAULT_RETRY_INTERVAL_MINUTES = 2;

    /** Default Emotiva device keep alive in milliseconds. {@link ControlDTO} */
    static int DEFAULT_KEEP_ALIVE_IN_MILLISECONDS = 7500;

    /** Default Emotiva device considered list in milliseconds. {@link ControlDTO} */
    static int DEFAULT_KEEP_ALIVE_CONSIDERED_LOST_IN_MILLISECONDS = 30000;

    /** Default Emotiva control message value **/
    public static final String DEFAULT_CONTROL_MESSAGE_SET_DEFAULT_VALUE = "0";

    /** Default value for ack property in Emotiva control messages **/
    public static final String DEFAULT_CONTROL_ACK_VALUE = "yes";

    /** Default discovery timeout in seconds **/
    public static final int DISCOVERY_TIMEOUT_SECONDS = 5;

    /** Default discovery broadcast address **/
    public static final String DISCOVERY_BROADCAST_ADDRESS = "255.255.255.255";

    /** List of all Thing Type UIDs **/
    static final ThingTypeUID THING_PROCESSOR = new ThingTypeUID(BINDING_ID, "processor");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(List.of(THING_PROCESSOR));

    /** Default values for Emotiva channels **/
    public static final String DEFAULT_EMOTIVA_PROTOCOL_VERSION = "2.0";
    public static final int DEFAULT_VOLUME_MIN_DECIBEL = -96;
    public static final int DEFAULT_VOLUME_MAX_DECIBEL = 15;
    public static final int DEFAULT_TRIM_MIN_DECIBEL = -12;
    public static final int DEFAULT_TRIM_MAX_DECIBEL = 12;
    public static final String MAP_SOURCES_MAIN_ZONE = "sources";
    public static final String MAP_SOURCES_ZONE_2 = "zone2_sources";

    /** Miscellaneous Constants **/
    public static final int PROTOCOL_V3_LEVEL_MULTIPLIER = 2;
    public static final String TRIM_SET_COMMAND_SUFFIX = "_trim_set";
    static final String MENU_PANEL_CHECKBOX_ON = "on";
    static final String MENU_PANEL_HIGHLIGHTED = "true";
    static final String EMOTIVA_SOURCE_COMMAND_PREFIX = "source_";

    /** Emotiva Protocol V1 channels **/
    public static final String CHANNEL_STANDBY = "general#standby";
    public static final String CHANNEL_MAIN_ZONE_POWER = "mainZone#power";
    public static final String CHANNEL_SOURCE = "mainZone#source";
    public static final String CHANNEL_MENU = "general#menu";
    public static final String CHANNEL_MENU_CONTROL = "general#menu_control";
    public static final String CHANNEL_MENU_UP = "general#up";
    public static final String CHANNEL_MENU_DOWN = "general#down";
    public static final String CHANNEL_MENU_LEFT = "general#left";
    public static final String CHANNEL_MENU_RIGHT = "general#right";
    public static final String CHANNEL_MENU_ENTER = "general#enter";
    public static final String CHANNEL_MUTE = "mainZone#mute";
    public static final String CHANNEL_DIM = "general#dim";
    public static final String CHANNEL_MODE = "general#mode";
    public static final String CHANNEL_CENTER = "general#center";
    public static final String CHANNEL_SUBWOOFER = "general#subwoofer";
    public static final String CHANNEL_SURROUND = "general#surround";
    public static final String CHANNEL_BACK = "general#back";
    public static final String CHANNEL_MODE_SURROUND = "general#mode_surround";
    public static final String CHANNEL_SPEAKER_PRESET = "general#speaker_preset";
    public static final String CHANNEL_MAIN_VOLUME = "mainZone#volume";
    public static final String CHANNEL_MAIN_VOLUME_DB = "mainZone#volume_db";
    public static final String CHANNEL_LOUDNESS = "general#loudness";
    public static final String CHANNEL_ZONE2_POWER = "zone2#power";
    public static final String CHANNEL_ZONE2_VOLUME = "zone2#volume";
    public static final String CHANNEL_ZONE2_VOLUME_DB = "zone2#volume_db";
    public static final String CHANNEL_ZONE2_MUTE = "zone2#mute";
    public static final String CHANNEL_ZONE2_SOURCE = "zone2#source";
    public static final String CHANNEL_FREQUENCY = "general#frequency";
    public static final String CHANNEL_SEEK = "general#seek";
    public static final String CHANNEL_CHANNEL = "general#channel";
    public static final String CHANNEL_TUNER_BAND = "general#tuner_band";
    public static final String CHANNEL_TUNER_CHANNEL = "general#tuner_channel";
    public static final String CHANNEL_TUNER_CHANNEL_SELECT = "general#tuner_channel_select";
    public static final String CHANNEL_TUNER_SIGNAL = "general#tuner_signal";
    public static final String CHANNEL_TUNER_PROGRAM = "general#tuner_program";
    public static final String CHANNEL_TUNER_RDS = "general#tuner_RDS";
    public static final String CHANNEL_AUDIO_INPUT = "general#audio_input";
    public static final String CHANNEL_AUDIO_BITSTREAM = "general#audio_bitstream";
    public static final String CHANNEL_AUDIO_BITS = "general#audio_bits";
    public static final String CHANNEL_VIDEO_INPUT = "general#video_input";
    public static final String CHANNEL_VIDEO_FORMAT = "general#video_format";
    public static final String CHANNEL_VIDEO_SPACE = "general#video_space";
    public static final String CHANNEL_INPUT1 = "general#input_1";
    public static final String CHANNEL_INPUT2 = "general#input_2";
    public static final String CHANNEL_INPUT3 = "general#input_3";
    public static final String CHANNEL_INPUT4 = "general#input_4";
    public static final String CHANNEL_INPUT5 = "general#input_5";
    public static final String CHANNEL_INPUT6 = "general#input_6";
    public static final String CHANNEL_INPUT7 = "general#input_7";
    public static final String CHANNEL_INPUT8 = "general#input_8";
    public static final String CHANNEL_MODE_REF_STEREO = "general#mode_ref_stereo";
    public static final String CHANNEL_SURROUND_MODE = "general#surround_mode";
    public static final String CHANNEL_MODE_STEREO = "general#mode_stereo";
    public static final String CHANNEL_MODE_MUSIC = "general#mode_music";
    public static final String CHANNEL_MODE_MOVIE = "general#mode_movie";
    public static final String CHANNEL_MODE_DIRECT = "general#mode_direct";
    public static final String CHANNEL_MODE_DOLBY = "general#mode_dolby";
    public static final String CHANNEL_MODE_DTS = "general#mode_dts";
    public static final String CHANNEL_MODE_ALL_STEREO = "general#mode_all_stereo";
    public static final String CHANNEL_MODE_AUTO = "general#mode_auto";

    /** Emotiva Protocol V2 channels **/
    public static final String CHANNEL_SELECTED_MODE = "general#selected_mode";
    public static final String CHANNEL_SELECTED_MOVIE_MUSIC = "general#selected_movie_music";

    /** Emotiva Protocol V3 channels **/
    public static final String CHANNEL_TREBLE = "general#treble";
    public static final String CHANNEL_BASS = "general#bass";
    public static final String CHANNEL_WIDTH = "general#width";
    public static final String CHANNEL_HEIGHT = "general#height";
    public static final String CHANNEL_KEEP_ALIVE = "general#keep_alive";
    public static final String CHANNEL_BAR = "general#bar";
    public static final String CHANNEL_MENU_DISPLAY_PREFIX = "general#menu_display";
    public static final String CHANNEL_MENU_DISPLAY_HIGHLIGHT = "general#menu_display_highlight";
}
