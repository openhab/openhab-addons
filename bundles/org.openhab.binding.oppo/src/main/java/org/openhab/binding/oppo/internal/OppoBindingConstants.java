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
package org.openhab.binding.oppo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OppoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class OppoBindingConstants {

    private static final String BINDING_ID = "oppo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    
    public static final String MODEL83 = "83";
    public static final String MODEL103 = "103";
    public static final String MODEL105 = "105";
    public static final String MODEL203 = "203";
    public static final String MODEL205 = "205";
    
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
    public static final String CHANNEL_ASPECT_RATIO = "aspect_ratio"; //203 and 205 only
    public static final String CHANNEL_SOURCE_RESOLUTION = "source_resolution";
    public static final String CHANNEL_OUTPUT_RESOLUTION = "output_resolution";
    public static final String CHANNEL_3D_INDICATOR = "3d_indicator";
    public static final String CHANNEL_SUB_SHIFT = "sub_shift"; //not on 83
    public static final String CHANNEL_OSD_POSITION = "osd_position"; // not on 83
    public static final String CHANNEL_HDMI_MODE = "hdmi_mode";
    public static final String CHANNEL_HDR_MODE = "hdr_mode"; //203 and 205 only
    public static final String CHANNEL_REMOTE_BUTTON = "remote_button";
    
}
