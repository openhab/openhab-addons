/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
        VIDEO_MODE.put("01", "480i60 4:3");
        VIDEO_MODE.put("02", "480i60 16:9");
        VIDEO_MODE.put("03", "480p60 4:3");
        VIDEO_MODE.put("04", "480p60 16:9");
        VIDEO_MODE.put("05", "576i50 4:3");
        VIDEO_MODE.put("06", "576i50 16:9");
        VIDEO_MODE.put("07", "576p50 4:3");
        VIDEO_MODE.put("08", "576p50 16:9");
        VIDEO_MODE.put("09", "720p60 NTSC HD");
        VIDEO_MODE.put("10", "720p50 PAL HD");
        VIDEO_MODE.put("11", "1080i60 16:9");
        VIDEO_MODE.put("12", "1080i50 16:9");
        VIDEO_MODE.put("13", "1080p60 16:9");
        VIDEO_MODE.put("14", "1080p50 16:9");
        VIDEO_MODE.put("15", RESERVED);
        VIDEO_MODE.put("16", RESERVED);
        VIDEO_MODE.put("17", "1080p24 16:9");
        VIDEO_MODE.put("18", RESERVED);
        VIDEO_MODE.put("19", "480i60 64:27");
        VIDEO_MODE.put("20", "576i50 64:27");
        VIDEO_MODE.put("21", "1080i60 64:27");
        VIDEO_MODE.put("22", "1080i50 64:27");
        VIDEO_MODE.put("23", "1080p60 64:27");
        VIDEO_MODE.put("24", "1080p50 64:27");
        VIDEO_MODE.put("25", "1080p24 64:27");
        VIDEO_MODE.put("26", "1080p24 64:27");
        VIDEO_MODE.put("27", "3840x 2160p24 16:9");
        VIDEO_MODE.put("28", "3840x 2160p24 64:27");
        VIDEO_MODE.put("29", "3840x 2160p30 16:9");
        VIDEO_MODE.put("30", "3840x 2160p30 64:27");
        VIDEO_MODE.put("31", "3840x 2160p60 16:9");
        VIDEO_MODE.put("32", "3840x 2160p60 64:27");
        VIDEO_MODE.put("33", "3840x 2160p25 16:9");
        VIDEO_MODE.put("34", "3840x 2160p25 64:27");
        VIDEO_MODE.put("35", "3840x 2160p50 16:9");
        VIDEO_MODE.put("36", "3840x 2160p50 64:27");
        VIDEO_MODE.put("37", "3840x 2160p24 16:9");
        VIDEO_MODE.put("38", "3840x 2160p24 64:27");
    }

    // map to lookup eotf
    public static final Map<String, String> EOTF = new HashMap<>();
    static {
        EOTF.put("00", UNKNOWN);
        EOTF.put("01", "SDR");
        EOTF.put("02", "HDR");
        EOTF.put("03", "SMTPE ST 2048");
    }

    // map to lookup readiness state
    public static final Map<String, String> READINESS_STATE = new HashMap<>();
    static {
        READINESS_STATE.put("0", "system is ready");
        READINESS_STATE.put("1", "system is becoming ready");
        READINESS_STATE.put("2", "system is idle");
    }
}
