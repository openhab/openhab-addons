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
package org.openhab.binding.emotiva.internal.protocol;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_AUDIO_BITS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_AUDIO_BITSTREAM;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_AUDIO_INPUT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_BACK;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_BAR;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_BASS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_CENTER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_DIM;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_HEIGHT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT1;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT2;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT3;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT4;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT5;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT6;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT7;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_INPUT8;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_KEEP_ALIVE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_LOUDNESS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_ZONE_POWER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_DISPLAY_PREFIX;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_ALL_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_AUTO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DIRECT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DOLBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DTS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_MOVIE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_MUSIC;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_REF_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_SURROUND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SELECTED_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SELECTED_MOVIE_MUSIC;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SPEAKER_PRESET;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SUBWOOFER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SURROUND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TREBLE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_BAND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_CHANNEL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_PROGRAM;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_RDS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_SIGNAL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_VIDEO_FORMAT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_VIDEO_INPUT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_VIDEO_SPACE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_WIDTH;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_POWER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.DIMENSIONLESS_DECIBEL;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.DIMENSIONLESS_PERCENT;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.FREQUENCY_HERTZ;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.GOODBYE;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.NUMBER_TIME;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.ON_OFF;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.STRING;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.UNKNOWN;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emotiva subscription tags with corresponding UoM data type and channel.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaSubscriptionTags {

    /* Protocol V1 notify tags */
    power("power", ON_OFF, CHANNEL_MAIN_ZONE_POWER),
    source("source", STRING, CHANNEL_SOURCE),
    dim("dim", DIMENSIONLESS_PERCENT, CHANNEL_DIM),
    mode("mode", STRING, CHANNEL_MODE),
    speaker_preset("speaker-preset", STRING, CHANNEL_SPEAKER_PRESET),
    center("center", DIMENSIONLESS_DECIBEL, CHANNEL_CENTER),
    subwoofer("subwoofer", DIMENSIONLESS_DECIBEL, CHANNEL_SUBWOOFER),
    surround("surround", DIMENSIONLESS_DECIBEL, CHANNEL_SURROUND),
    back("back", DIMENSIONLESS_DECIBEL, CHANNEL_BACK),
    volume("volume", DIMENSIONLESS_DECIBEL, CHANNEL_MAIN_VOLUME),
    loudness("loudness", ON_OFF, CHANNEL_LOUDNESS),
    treble("treble", DIMENSIONLESS_DECIBEL, CHANNEL_TREBLE),
    bass("bass", DIMENSIONLESS_DECIBEL, CHANNEL_BASS),
    zone2_power("zone2-power", ON_OFF, CHANNEL_ZONE2_POWER),
    zone2_volume("zone2-volume", DIMENSIONLESS_DECIBEL, CHANNEL_ZONE2_VOLUME),
    zone2_input("zone2-input", STRING, CHANNEL_ZONE2_SOURCE),
    tuner_band("tuner-band", STRING, CHANNEL_TUNER_BAND),
    tuner_channel("tuner-channel", FREQUENCY_HERTZ, CHANNEL_TUNER_CHANNEL),
    tuner_signal("tuner-signal", STRING, CHANNEL_TUNER_SIGNAL),
    tuner_program("tuner-program", STRING, CHANNEL_TUNER_PROGRAM),
    tuner_RDS("tuner-RDS", STRING, CHANNEL_TUNER_RDS),
    audio_input("audio-input", STRING, CHANNEL_AUDIO_INPUT),
    audio_bitstream("audio-bitstream", STRING, CHANNEL_AUDIO_BITSTREAM),
    audio_bits("audio-bits", STRING, CHANNEL_AUDIO_BITS),
    video_input("video-input", STRING, CHANNEL_VIDEO_INPUT),
    video_format("video-format", STRING, CHANNEL_VIDEO_FORMAT),
    video_space("video-space", STRING, CHANNEL_VIDEO_SPACE),
    input_1("input-1", STRING, CHANNEL_INPUT1),
    input_2("input-2", STRING, CHANNEL_INPUT2),
    input_3("input-3", STRING, CHANNEL_INPUT3),
    input_4("input-4", STRING, CHANNEL_INPUT4),
    input_5("input-5", STRING, CHANNEL_INPUT5),
    input_6("input-6", STRING, CHANNEL_INPUT6),
    input_7("input-7", STRING, CHANNEL_INPUT7),
    input_8("input-8", STRING, CHANNEL_INPUT8),

    /* Protocol V2 notify tags */
    selected_mode("selected-mode", STRING, CHANNEL_SELECTED_MODE),
    selected_movie_music("selected-movie-music", STRING, CHANNEL_SELECTED_MOVIE_MUSIC),
    mode_ref_stereo("mode-ref-stereo", STRING, CHANNEL_MODE_REF_STEREO),
    mode_stereo("mode-stereo", STRING, CHANNEL_MODE_STEREO),
    mode_music("mode-music", STRING, CHANNEL_MODE_MUSIC),
    mode_movie("mode-movie", STRING, CHANNEL_MODE_MOVIE),
    mode_direct("mode-direct", STRING, CHANNEL_MODE_DIRECT),
    mode_dolby("mode-dolby", STRING, CHANNEL_MODE_DOLBY),
    mode_dts("mode-dts", STRING, CHANNEL_MODE_DTS),
    mode_all_stereo("mode-all-stereo", STRING, CHANNEL_MODE_ALL_STEREO),
    mode_auto("mode-auto", STRING, CHANNEL_MODE_AUTO),
    mode_surround("mode-surround", STRING, CHANNEL_MODE_SURROUND),
    menu("menu", ON_OFF, CHANNEL_MENU),
    menu_update("menu-update", STRING, CHANNEL_MENU_DISPLAY_PREFIX),

    /* Protocol V3 notify tags */
    keepAlive("keepAlive", NUMBER_TIME, CHANNEL_KEEP_ALIVE),
    goodBye("goodBye", GOODBYE, ""),
    bar_update("bar-update", STRING, CHANNEL_BAR),
    width("width", DIMENSIONLESS_DECIBEL, CHANNEL_WIDTH),
    height("height", DIMENSIONLESS_DECIBEL, CHANNEL_HEIGHT),

    /* Notify tag not in the documentation */
    source_tuner("source-tuner", ON_OFF, ""),

    /* No match tag */
    unknown("unknown", UNKNOWN, "");

    private static final Logger logger = LoggerFactory.getLogger(EmotivaSubscriptionTags.class);

    /* For error handling */
    public static final String UNKNOWN_TAG = "unknown";

    private final String name;
    private final EmotivaDataType dataType;
    private final String channel;

    EmotivaSubscriptionTags(String name, EmotivaDataType dataType, String channel) {
        this.name = name;
        this.dataType = dataType;
        this.channel = channel;
    }

    public static boolean hasChannel(String name) {
        try {
            EmotivaSubscriptionTags type = EmotivaSubscriptionTags.valueOf(name);
            if (!type.channel.isEmpty()) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        return false;
    }

    public static EmotivaSubscriptionTags fromChannelUID(String id) {
        for (EmotivaSubscriptionTags value : values()) {
            if (id.equals(value.getChannel())) {
                return value;
            }
        }
        return EmotivaSubscriptionTags.unknown;
    }

    public static EmotivaSubscriptionTags[] generalChannels() {
        List<EmotivaSubscriptionTags> tags = new ArrayList<>();
        for (EmotivaSubscriptionTags value : values()) {
            if (value.channel.startsWith("general")) {
                tags.add(value);
            }
        }
        return tags.toArray(new EmotivaSubscriptionTags[0]);
    }

    public static EmotivaSubscriptionTags[] nonGeneralChannels() {
        List<EmotivaSubscriptionTags> tags = new ArrayList<>();
        for (EmotivaSubscriptionTags value : values()) {
            if (!value.channel.startsWith("general")) {
                tags.add(value);
            }
        }
        return tags.toArray(new EmotivaSubscriptionTags[0]);
    }

    public static EmotivaSubscriptionTags[] speakerChannels() {
        List<EmotivaSubscriptionTags> tags = new ArrayList<>();
        for (EmotivaSubscriptionTags value : values()) {
            if (value.getDataType().equals(DIMENSIONLESS_DECIBEL)) {
                tags.add(value);
            }
        }
        return tags.toArray(new EmotivaSubscriptionTags[0]);
    }

    public static List<EmotivaSubscriptionTags> noSubscriptionToChannel() {
        return List.of(keepAlive, goodBye);
    }

    public String getName() {
        return name;
    }

    public String getEmotivaName() {
        String retVal = name.replaceAll("-", "_");
        logger.debug("Converting OH channel '{}' to Emotiva command '{}'", name, retVal);
        return retVal;
    }

    public EmotivaDataType getDataType() {
        return dataType;
    }

    public String getChannel() {
        return channel;
    }
}
