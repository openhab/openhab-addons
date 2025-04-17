/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.*;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaDataType.*;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    power("power", ON_OFF, CHANNEL_MAIN_ZONE_POWER, GENERAL),
    source("source", STRING, CHANNEL_SOURCE, GENERAL),
    dim("dim", DIMENSIONLESS_PERCENT, CHANNEL_DIM, UI_DEVICE),
    mode("mode", STRING, CHANNEL_MODE, GENERAL),
    speaker_preset("speaker-preset", STRING, CHANNEL_SPEAKER_PRESET, AUDIO_ADJUSTMENT),
    center("center", DIMENSIONLESS_DECIBEL, CHANNEL_CENTER, AUDIO_ADJUSTMENT),
    subwoofer("subwoofer", DIMENSIONLESS_DECIBEL, CHANNEL_SUBWOOFER, AUDIO_ADJUSTMENT),
    surround("surround", DIMENSIONLESS_DECIBEL, CHANNEL_SURROUND, AUDIO_ADJUSTMENT),
    back("back", DIMENSIONLESS_DECIBEL, CHANNEL_BACK, AUDIO_ADJUSTMENT),
    volume("volume", DIMENSIONLESS_DECIBEL, CHANNEL_MAIN_VOLUME, GENERAL),
    loudness("loudness", ON_OFF, CHANNEL_LOUDNESS, AUDIO_ADJUSTMENT),
    treble("treble", DIMENSIONLESS_DECIBEL, CHANNEL_TREBLE, AUDIO_ADJUSTMENT),
    bass("bass", DIMENSIONLESS_DECIBEL, CHANNEL_BASS, AUDIO_ADJUSTMENT),
    zone2_power("zone2-power", ON_OFF, CHANNEL_ZONE2_POWER, ZONE2_GENERAL),
    zone2_volume("zone2-volume", DIMENSIONLESS_DECIBEL, CHANNEL_ZONE2_VOLUME, ZONE2_GENERAL),
    zone2_input("zone2-input", STRING, CHANNEL_ZONE2_SOURCE, ZONE2_GENERAL),
    tuner_band("tuner-band", STRING, CHANNEL_TUNER_BAND, TUNER),
    tuner_channel("tuner-channel", FREQUENCY_HERTZ, CHANNEL_TUNER_CHANNEL, TUNER),
    tuner_signal("tuner-signal", STRING, CHANNEL_TUNER_SIGNAL, TUNER),
    tuner_program("tuner-program", STRING, CHANNEL_TUNER_PROGRAM, TUNER),
    tuner_RDS("tuner-RDS", STRING, CHANNEL_TUNER_RDS, TUNER),
    audio_input("audio-input", STRING, CHANNEL_AUDIO_INPUT, AUDIO_INFO),
    audio_bitstream("audio-bitstream", STRING, CHANNEL_AUDIO_BITSTREAM, AUDIO_INFO),
    audio_bits("audio-bits", STRING, CHANNEL_AUDIO_BITS, AUDIO_INFO),
    video_input("video-input", STRING, CHANNEL_VIDEO_INPUT, VIDEO_INFO),
    video_format("video-format", STRING, CHANNEL_VIDEO_FORMAT, VIDEO_INFO),
    video_space("video-space", STRING, CHANNEL_VIDEO_SPACE, VIDEO_INFO),
    input_1("input-1", STRING, CHANNEL_INPUT1, SOURCES),
    input_2("input-2", STRING, CHANNEL_INPUT2, SOURCES),
    input_3("input-3", STRING, CHANNEL_INPUT3, SOURCES),
    input_4("input-4", STRING, CHANNEL_INPUT4, SOURCES),
    input_5("input-5", STRING, CHANNEL_INPUT5, SOURCES),
    input_6("input-6", STRING, CHANNEL_INPUT6, SOURCES),
    input_7("input-7", STRING, CHANNEL_INPUT7, SOURCES),
    input_8("input-8", STRING, CHANNEL_INPUT8, SOURCES),

    /* Protocol V2 notify tags */
    selected_mode("selected-mode", STRING, CHANNEL_SELECTED_MODE, GENERAL),
    selected_movie_music("selected-movie-music", STRING, CHANNEL_SELECTED_MOVIE_MUSIC, GENERAL),
    mode_ref_stereo("mode-ref-stereo", STRING, CHANNEL_MODE_REF_STEREO, SOURCES),
    mode_stereo("mode-stereo", STRING, CHANNEL_MODE_STEREO, SOURCES),
    mode_music("mode-music", STRING, CHANNEL_MODE_MUSIC, SOURCES),
    mode_movie("mode-movie", STRING, CHANNEL_MODE_MOVIE, SOURCES),
    mode_direct("mode-direct", STRING, CHANNEL_MODE_DIRECT, SOURCES),
    mode_dolby("mode-dolby", STRING, CHANNEL_MODE_DOLBY, SOURCES),
    mode_dts("mode-dts", STRING, CHANNEL_MODE_DTS, SOURCES),
    mode_all_stereo("mode-all-stereo", STRING, CHANNEL_MODE_ALL_STEREO, SOURCES),
    mode_auto("mode-auto", STRING, CHANNEL_MODE_AUTO, SOURCES),
    mode_surround("mode-surround", STRING, CHANNEL_MODE_SURROUND, SOURCES),
    menu("menu", ON_OFF, CHANNEL_MENU, UI_MENU),
    menu_update("menu-update", STRING, CHANNEL_MENU_DISPLAY_PREFIX, UI_MENU),

    /* Protocol V3 notify tags */
    keepAlive("keepAlive", KEEP_ALIVE, "", GENERAL),
    goodBye("goodBye", GOODBYE, "", GENERAL),
    bar_update("bar-update", STRING, CHANNEL_BAR, UI_DEVICE),
    width("width", DIMENSIONLESS_DECIBEL, CHANNEL_WIDTH, AUDIO_ADJUSTMENT),
    height("height", DIMENSIONLESS_DECIBEL, CHANNEL_HEIGHT, AUDIO_ADJUSTMENT),

    /* Notify tag not in the documentation */
    source_tuner("source-tuner", ON_OFF, "", SOURCES),

    /* No match tag */
    unknown("unknown", UNKNOWN, "", EmotivaSubscriptionTagGroup.NONE);

    private final Logger logger = LoggerFactory.getLogger(EmotivaSubscriptionTags.class);

    /* For error handling */
    public static final String UNKNOWN_TAG = "unknown";

    private final String name;
    private final EmotivaDataType dataType;
    private final String channel;
    private final EmotivaSubscriptionTagGroup subscriptionTagGroup;

    EmotivaSubscriptionTags(String name, EmotivaDataType dataType, String channel,
            EmotivaSubscriptionTagGroup subscriptionTagGroup) {
        this.name = name;
        this.dataType = dataType;
        this.channel = channel;
        this.subscriptionTagGroup = subscriptionTagGroup;
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

    public static List<EmotivaSubscriptionTags> channels(String prefix) {
        List<EmotivaSubscriptionTags> tags = new ArrayList<>();
        for (EmotivaSubscriptionTags value : values()) {
            if (value.channel.startsWith(prefix)) {
                tags.add(value);
            }
        }

        // Always add keepAlive tag to the general prefix
        if ("general".equals(prefix)) {
            tags.add(EmotivaSubscriptionTags.keepAlive);
        }
        return tags;
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

    public static List<EmotivaSubscriptionTags> getBySubscriptionTagGroups(Set<EmotivaSubscriptionTagGroup> groups) {
        List<EmotivaSubscriptionTags> tags = new ArrayList<>();
        for (EmotivaSubscriptionTagGroup group : groups) {
            for (EmotivaSubscriptionTags value : values()) {
                if (value.subscriptionTagGroup.equals(group)) {
                    tags.add(value);
                }
            }
        }
        return tags;
    }

    public String getName() {
        return name;
    }

    public String getEmotivaName() {
        String retVal = name.replaceAll("-", "_");
        logger.trace("Converting OH channel '{}' to Emotiva command '{}'", name, retVal);
        return retVal;
    }

    public EmotivaDataType getDataType() {
        return dataType;
    }

    public String getChannel() {
        return channel;
    }
}
