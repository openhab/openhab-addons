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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ProfileConditionValue
 */
public enum ProfileConditionValue {

    AUDIO_CHANNELS("AudioChannels"),

    AUDIO_BITRATE("AudioBitrate"),

    AUDIO_PROFILE("AudioProfile"),

    WIDTH("Width"),

    HEIGHT("Height"),

    HAS64_BIT_OFFSETS("Has64BitOffsets"),

    PACKET_LENGTH("PacketLength"),

    VIDEO_BIT_DEPTH("VideoBitDepth"),

    VIDEO_BITRATE("VideoBitrate"),

    VIDEO_FRAMERATE("VideoFramerate"),

    VIDEO_LEVEL("VideoLevel"),

    VIDEO_PROFILE("VideoProfile"),

    VIDEO_TIMESTAMP("VideoTimestamp"),

    IS_ANAMORPHIC("IsAnamorphic"),

    REF_FRAMES("RefFrames"),

    NUM_AUDIO_STREAMS("NumAudioStreams"),

    NUM_VIDEO_STREAMS("NumVideoStreams"),

    IS_SECONDARY_AUDIO("IsSecondaryAudio"),

    VIDEO_CODEC_TAG("VideoCodecTag"),

    IS_AVC("IsAvc"),

    IS_INTERLACED("IsInterlaced"),

    AUDIO_SAMPLE_RATE("AudioSampleRate"),

    AUDIO_BIT_DEPTH("AudioBitDepth"),

    VIDEO_RANGE_TYPE("VideoRangeType"),

    NUM_STREAMS("NumStreams");

    private String value;

    ProfileConditionValue(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ProfileConditionValue fromValue(String value) {
        for (ProfileConditionValue b : ProfileConditionValue.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        return String.format(java.util.Locale.ROOT, "%s=%s", prefix, this.toString());
    }
}
