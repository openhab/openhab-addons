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
 * Gets or Sets TranscodeReason
 */
public enum TranscodeReason {

    CONTAINER_NOT_SUPPORTED("ContainerNotSupported"),

    VIDEO_CODEC_NOT_SUPPORTED("VideoCodecNotSupported"),

    AUDIO_CODEC_NOT_SUPPORTED("AudioCodecNotSupported"),

    SUBTITLE_CODEC_NOT_SUPPORTED("SubtitleCodecNotSupported"),

    AUDIO_IS_EXTERNAL("AudioIsExternal"),

    SECONDARY_AUDIO_NOT_SUPPORTED("SecondaryAudioNotSupported"),

    VIDEO_PROFILE_NOT_SUPPORTED("VideoProfileNotSupported"),

    VIDEO_LEVEL_NOT_SUPPORTED("VideoLevelNotSupported"),

    VIDEO_RESOLUTION_NOT_SUPPORTED("VideoResolutionNotSupported"),

    VIDEO_BIT_DEPTH_NOT_SUPPORTED("VideoBitDepthNotSupported"),

    VIDEO_FRAMERATE_NOT_SUPPORTED("VideoFramerateNotSupported"),

    REF_FRAMES_NOT_SUPPORTED("RefFramesNotSupported"),

    ANAMORPHIC_VIDEO_NOT_SUPPORTED("AnamorphicVideoNotSupported"),

    INTERLACED_VIDEO_NOT_SUPPORTED("InterlacedVideoNotSupported"),

    AUDIO_CHANNELS_NOT_SUPPORTED("AudioChannelsNotSupported"),

    AUDIO_PROFILE_NOT_SUPPORTED("AudioProfileNotSupported"),

    AUDIO_SAMPLE_RATE_NOT_SUPPORTED("AudioSampleRateNotSupported"),

    AUDIO_BIT_DEPTH_NOT_SUPPORTED("AudioBitDepthNotSupported"),

    CONTAINER_BITRATE_EXCEEDS_LIMIT("ContainerBitrateExceedsLimit"),

    VIDEO_BITRATE_NOT_SUPPORTED("VideoBitrateNotSupported"),

    AUDIO_BITRATE_NOT_SUPPORTED("AudioBitrateNotSupported"),

    UNKNOWN_VIDEO_STREAM_INFO("UnknownVideoStreamInfo"),

    UNKNOWN_AUDIO_STREAM_INFO("UnknownAudioStreamInfo"),

    DIRECT_PLAY_ERROR("DirectPlayError"),

    VIDEO_RANGE_TYPE_NOT_SUPPORTED("VideoRangeTypeNotSupported"),

    VIDEO_CODEC_TAG_NOT_SUPPORTED("VideoCodecTagNotSupported"),

    STREAM_COUNT_EXCEEDS_LIMIT("StreamCountExceedsLimit");

    private String value;

    TranscodeReason(String value) {
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
    public static TranscodeReason fromValue(String value) {
        for (TranscodeReason b : TranscodeReason.values()) {
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
