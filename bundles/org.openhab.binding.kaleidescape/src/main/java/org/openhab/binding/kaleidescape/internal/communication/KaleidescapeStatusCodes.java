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
    
    public static final String unused = "unused";
    public static final String unknown = "unknown";
    public static final String reserved = "reserved";

    // map to lookup play mode
    public static final Map<String, String> playMode = new HashMap<>();
    static {
        playMode.put("0", "Nothing playing");
        playMode.put("1", "Paused");
        playMode.put("2", "Playing");
        playMode.put("3", unused);
        playMode.put("4", "Forward scan");
        playMode.put("5", unused);
        playMode.put("6", "Reverse scan");
    }
    
    // map to lookup media type
    public static final Map<String, String> mediaType = new HashMap<>();
    static {
        mediaType.put("00", "Nothing playing");
        mediaType.put("01", "DVD");
        mediaType.put("02", "Video stream");
        mediaType.put("03", "Blu-ray Disc");
    }
    
    // map to lookup movie location
    public static final Map<String, String> movieLocation = new HashMap<>();
    static {
        movieLocation.put("00", unknown);
        movieLocation.put("01", unused);
        movieLocation.put("02", unused);
        movieLocation.put("03", "Main content");
        movieLocation.put("04", "Intermission");
        movieLocation.put("05", "End Credits");
        movieLocation.put("06", "DVD/Blu-ray Disc Menu"); 
    }
    
    // map to lookup aspect ratio
    public static final Map<String, String> aspectRatio = new HashMap<>();
    static {
        aspectRatio.put("00", unknown);
        aspectRatio.put("01", "1.33");
        aspectRatio.put("02", "1.66");
        aspectRatio.put("03", "1.78");
        aspectRatio.put("04", "1.85");
        aspectRatio.put("05", "2.35");      
    }
    
    public static final Map<String, String> videoMode = new HashMap<>();
    static {
        videoMode.put("00", "No output");
        videoMode.put("01", "480i60 4:3");
        videoMode.put("02", "480i60 16:9");
        videoMode.put("03", "480p60 4:3");
        videoMode.put("04", "480p60 16:9");
        videoMode.put("05", "576i50 4:3");
        videoMode.put("06", "576i50 16:9");
        videoMode.put("07", "576p50 4:3");
        videoMode.put("08", "576p50 16:9");
        videoMode.put("09", "720p60 NTSC HD");
        videoMode.put("10", "720p50 PAL HD");
        videoMode.put("11", "1080i60 16:9");
        videoMode.put("12", "1080i50 16:9");
        videoMode.put("13", "1080p60 16:9");
        videoMode.put("14", "1080p50 16:9");
        videoMode.put("15", reserved);
        videoMode.put("16", reserved);
        videoMode.put("17", "1080p24 16:9");
        videoMode.put("18", reserved);
        videoMode.put("19", "480i60 64:27");
        videoMode.put("20", "576i50 64:27");
        videoMode.put("21", "1080i60 64:27");
        videoMode.put("22", "1080i50 64:27");
        videoMode.put("23", "1080p60 64:27");
        videoMode.put("24", "1080p50 64:27");
        videoMode.put("25", "1080p24 64:27");
        videoMode.put("26", "1080p24 64:27");
        videoMode.put("27", "3840x 2160p24 16:9");
        videoMode.put("28", "3840x 2160p24 64:27");
        videoMode.put("29", "3840x 2160p30 16:9");
        videoMode.put("30", "3840x 2160p30 64:27");
        videoMode.put("31", "3840x 2160p60 16:9");
        videoMode.put("32", "3840x 2160p60 64:27");
        videoMode.put("33", "3840x 2160p25 16:9");
        videoMode.put("34", "3840x 2160p25 64:27");
        videoMode.put("35", "3840x 2160p50 16:9");
        videoMode.put("36", "3840x 2160p50 64:27");
        videoMode.put("37", "3840x 2160p24 16:9");
        videoMode.put("38", "3840x 2160p24 64:27");
    }
    
    // map to lookup eotf
    public static final Map<String, String> eotf = new HashMap<>();
    static {
        eotf.put("00", unknown);
        eotf.put("01", "SDR");
        eotf.put("02", "HDR");
        eotf.put("03", "SMTPE ST 2048");   
    }
    
    // map to lookup readiness state
    public static final Map<String, String> readinessState = new HashMap<>();
    static {
        readinessState.put("0", "system is ready");
        readinessState.put("1", "system is becoming ready");
        readinessState.put("2", "system is idle");
    }
}
