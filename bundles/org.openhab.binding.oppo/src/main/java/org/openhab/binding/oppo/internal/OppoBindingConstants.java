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
package org.openhab.binding.oppo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OppoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class OppoBindingConstants {
    public static final String BINDING_ID = "oppo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");

    public static final int MODEL83 = 83;
    public static final int MODEL103 = 103;
    public static final int MODEL105 = 105;
    public static final int MODEL203 = 203;
    public static final int MODEL205 = 205;

    public static final Integer BDP83_PORT = 19999;
    public static final Integer BDP10X_PORT = 48360;
    public static final Integer BDP20X_PORT = 23;

    // List of all Channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_SOURCE = "source";
    public static final String CHANNEL_PLAY_MODE = "play_mode";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_TIME_MODE = "time_mode";
    public static final String CHANNEL_TIME_DISPLAY = "time_display";
    public static final String CHANNEL_CURRENT_TITLE = "current_title";
    public static final String CHANNEL_TOTAL_TITLE = "total_title";
    public static final String CHANNEL_CURRENT_CHAPTER = "current_chapter";
    public static final String CHANNEL_TOTAL_CHAPTER = "total_chapter";
    public static final String CHANNEL_REPEAT_MODE = "repeat_mode";
    public static final String CHANNEL_ZOOM_MODE = "zoom_mode";
    public static final String CHANNEL_DISC_TYPE = "disc_type";
    public static final String CHANNEL_AUDIO_TYPE = "audio_type";
    public static final String CHANNEL_SUBTITLE_TYPE = "subtitle_type";
    public static final String CHANNEL_ASPECT_RATIO = "aspect_ratio"; // 203 and 205 only
    public static final String CHANNEL_SOURCE_RESOLUTION = "source_resolution";
    public static final String CHANNEL_OUTPUT_RESOLUTION = "output_resolution";
    public static final String CHANNEL_3D_INDICATOR = "3d_indicator";
    public static final String CHANNEL_SUB_SHIFT = "sub_shift"; // not on 83
    public static final String CHANNEL_OSD_POSITION = "osd_position"; // not on 83
    public static final String CHANNEL_HDMI_MODE = "hdmi_mode";
    public static final String CHANNEL_HDR_MODE = "hdr_mode"; // 203 and 205 only
    public static final String CHANNEL_REMOTE_BUTTON = "remote_button";

    // misc
    public static final String BLANK = "";
    public static final String SPACE = " ";
    public static final String SLASH = "/";
    public static final String UNDERSCORE = "_";
    public static final String COLON = ":";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String ONE = "1";
    public static final String ZERO = "0";
    public static final String UNDEF = "UNDEF";
    public static final String VERBOSE_2 = "2";
    public static final String VERBOSE_3 = "3";
    public static final String MUTE = "MUTE";
    public static final String MUT = "MUT";
    public static final String UMT = "UMT";
    public static final String CDDA = "CDDA";

    public static final String NOP = "NOP";
    public static final String UTC = "UTC";
    public static final String QTE = "QTE";
    public static final String QTR = "QTR";
    public static final String QCE = "QCE";
    public static final String QCR = "QCR";
    public static final String QVR = "QVR";
    public static final String QPW = "QPW";
    public static final String UPW = "UPW";
    public static final String QVL = "QVL";
    public static final String UVL = "UVL";
    public static final String VUP = "VUP";
    public static final String VDN = "VDN";
    public static final String QIS = "QIS";
    public static final String UIS = "UIS";
    public static final String UPL = "UPL";
    public static final String QTK = "QTK";
    public static final String QCH = "QCH";
    public static final String QPL = "QPL";
    public static final String QRP = "QRP";
    public static final String QZM = "QZM";
    public static final String UDT = "UDT";
    public static final String QDT = "QDT";
    public static final String UAT = "UAT";
    public static final String QAT = "QAT";
    public static final String UST = "UST";
    public static final String QST = "QST";
    public static final String UAR = "UAR";
    public static final String UVO = "UVO";
    public static final String U3D = "U3D";
    public static final String QSH = "QSH";
    public static final String QOP = "QOP";
    public static final String QHD = "QHD";
    public static final String QHR = "QHR";

    public static final String UNKNOW_DISC = "UNKNOW-DISC";
    public static final String NO_DISC = "NO DISC";
    public static final String LOADING = "LOADING";
    public static final String OPEN = "OPEN";
    public static final String CLOSE = "CLOSE";
    public static final String STOP = "STOP";
    public static final String PLAY = "PLAY";

    public static final String T = "T";
    public static final String X = "X";
    public static final String C = "C";
    public static final String K = "K";
}
