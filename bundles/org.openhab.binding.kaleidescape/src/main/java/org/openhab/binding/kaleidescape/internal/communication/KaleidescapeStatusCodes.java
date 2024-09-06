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
package org.openhab.binding.kaleidescape.internal.communication;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides mapping of various Kaleidescape status codes to plain language meanings
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeStatusCodes {
    private static final String UNUSED = "unused";
    private static final String UNKNOWN = "unknown";
    private static final String RESERVED = "reserved";

    // map to lookup play mode
    public static final Map<String, String> PLAY_MODE = new HashMap<>();
    static {
        PLAY_MODE.put("0", "Nothing playing");
        PLAY_MODE.put("1", "Paused");
        PLAY_MODE.put("2", "Playing");
        PLAY_MODE.put("3", UNUSED);
        PLAY_MODE.put("4", "Forward scan");
        PLAY_MODE.put("5", UNUSED);
        PLAY_MODE.put("6", "Reverse scan");
    }

    // map to lookup media type
    public static final Map<String, String> MEDIA_TYPE = new HashMap<>();
    static {
        MEDIA_TYPE.put("00", "Nothing playing");
        MEDIA_TYPE.put("01", "DVD");
        MEDIA_TYPE.put("02", "Video stream");
        MEDIA_TYPE.put("03", "Blu-ray Disc");
    }

    // map to lookup movie location
    public static final Map<String, String> MOVIE_LOCATION = new HashMap<>();
    static {
        MOVIE_LOCATION.put("00", UNKNOWN);
        MOVIE_LOCATION.put("01", UNUSED);
        MOVIE_LOCATION.put("02", UNUSED);
        MOVIE_LOCATION.put("03", "Main content");
        MOVIE_LOCATION.put("04", "Intermission");
        MOVIE_LOCATION.put("05", "End Credits");
        MOVIE_LOCATION.put("06", "DVD/Blu-ray Disc Menu");
    }

    // map to lookup aspect ratio
    public static final Map<String, String> ASPECT_RATIO = new HashMap<>();
    static {
        ASPECT_RATIO.put("00", UNKNOWN);
        ASPECT_RATIO.put("01", "1.33");
        ASPECT_RATIO.put("02", "1.66");
        ASPECT_RATIO.put("03", "1.78");
        ASPECT_RATIO.put("04", "1.85");
        ASPECT_RATIO.put("05", "2.35");
    }

    public static final Map<String, String> VIDEO_MODE = new HashMap<>();

    static {
        VIDEO_MODE.put("00", "No output");
        VIDEO_MODE.put("01", "720x480i59.94 4:3");
        VIDEO_MODE.put("02", "720x480i59.94 16:9");
        VIDEO_MODE.put("03", "720x480p59.94 4:3");
        VIDEO_MODE.put("04", "720x480p59.94 16:9");
        VIDEO_MODE.put("05", "720x576i50 4:3");
        VIDEO_MODE.put("06", "720x576i50 16:9");
        VIDEO_MODE.put("07", "720x576p50 4:3");
        VIDEO_MODE.put("08", "720x576p50 16:9");
        VIDEO_MODE.put("09", "1280x720p59.94 NTSC HD");
        VIDEO_MODE.put("10", "1280x720p50 PAL HD");
        VIDEO_MODE.put("11", "1920x1080i59.94 16:9");
        VIDEO_MODE.put("12", "1920x1080i50 16:9");
        VIDEO_MODE.put("13", "1920x1080p59.94 16:9");
        VIDEO_MODE.put("14", "1920x1080p50 16:9");
        VIDEO_MODE.put("15", "1280x720p23.976 16:9");
        VIDEO_MODE.put("16", "1280x720p24 16:9");
        VIDEO_MODE.put("17", "1920x1080p23.976 16:9");
        VIDEO_MODE.put("18", "1920x1080p24 16:9");
        VIDEO_MODE.put("19", "720x480i59.94 64:27");
        VIDEO_MODE.put("20", "720x576i50 64:27");
        VIDEO_MODE.put("21", "1920x1080i59.94 64:27");
        VIDEO_MODE.put("22", "1920x1080i50 64:27");
        VIDEO_MODE.put("23", "1920x1080p59.94 64:27");
        VIDEO_MODE.put("24", "1920x1080p50 64:27");
        VIDEO_MODE.put("25", "1920x1080p23.976 64:27");
        VIDEO_MODE.put("26", "1920x1080p24 64:27");
        VIDEO_MODE.put("27", "3840x2160p23.976 16:9");
        VIDEO_MODE.put("28", "3840x2160p23.976 64:27");
        VIDEO_MODE.put("29", "3840x2160p29.97 16:9");
        VIDEO_MODE.put("30", "3840x2160p29.97 64:27");
        VIDEO_MODE.put("31", "3840x2160p59.94 16:9");
        VIDEO_MODE.put("32", "3840x2160p59.94 64:27");
        VIDEO_MODE.put("33", "3840x2160p25 16:9");
        VIDEO_MODE.put("34", "3840x2160p25 64:27");
        VIDEO_MODE.put("35", "3840x2160p50 16:9");
        VIDEO_MODE.put("36", "3840x2160p50 64:27");
        VIDEO_MODE.put("37", "3840x2160p24 16:9");
        VIDEO_MODE.put("38", "3840x2160p24 64:27");
        VIDEO_MODE.put("39", "1280x720p60 16:9");
        VIDEO_MODE.put("40", "1920x1080i60 16:9");
        VIDEO_MODE.put("41", "1920x1080i60 64:27");
        VIDEO_MODE.put("42", "1920x1080p60 16:9");
        VIDEO_MODE.put("43", "1920x1080p60 64:27");
        VIDEO_MODE.put("44", "3840x2160p 16:9");
        VIDEO_MODE.put("45", "3840x2160p 64:27");
        VIDEO_MODE.put("46", "1280x720p25 16:9");
        VIDEO_MODE.put("47", "1920x1080p25 16:9");
        VIDEO_MODE.put("48", "1920x1080p25 64:27");
        VIDEO_MODE.put("49", RESERVED);
        VIDEO_MODE.put("50", "1280x720p29.97 16:9");
        VIDEO_MODE.put("51", "1920x1080p29.97 16:9");
        VIDEO_MODE.put("52", "1920x1080p29.97 64:27");
        VIDEO_MODE.put("53", "1280x720p30 16:9");
        VIDEO_MODE.put("54", "1920x1080p30 16:9");
        VIDEO_MODE.put("55", "1920x1080p30 64:27");
        VIDEO_MODE.put("56", "3840x2160p30 16:9");
        VIDEO_MODE.put("57", "3840x2160p30 64:27");
    }

    // map to lookup eotf
    public static final Map<String, String> EOTF = new HashMap<>();
    static {
        EOTF.put("00", UNKNOWN);
        EOTF.put("01", "SDR");
        EOTF.put("02", RESERVED);
        EOTF.put("03", "HDR10");
        EOTF.put("04", RESERVED);
        EOTF.put("05", "Dolby Vision - standard");
        EOTF.put("03", "Dolby Vision - low-latency");
    }

    // map to lookup readiness state
    public static final Map<String, String> READINESS_STATE = new HashMap<>();
    static {
        READINESS_STATE.put("0", "system is ready");
        READINESS_STATE.put("1", "system is becoming ready");
        READINESS_STATE.put("2", "system is idle");
    }
}
