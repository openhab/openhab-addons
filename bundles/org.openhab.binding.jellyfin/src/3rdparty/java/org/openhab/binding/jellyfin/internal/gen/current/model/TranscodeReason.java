/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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


package org.openhab.binding.jellyfin.internal.gen.current.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets TranscodeReason
 */
public enum TranscodeReason {
  ANAMORPHIC_VIDEO_NOT_SUPPORTED("AnamorphicVideoNotSupported"),
  AUDIO_BIT_DEPTH_NOT_SUPPORTED("AudioBitDepthNotSupported"),
  AUDIO_BITRATE_NOT_SUPPORTED("AudioBitrateNotSupported"),
  AUDIO_CHANNELS_NOT_SUPPORTED("AudioChannelsNotSupported"),
  AUDIO_CODEC_NOT_SUPPORTED("AudioCodecNotSupported"),
  AUDIO_IS_EXTERNAL("AudioIsExternal"),
  AUDIO_PROFILE_NOT_SUPPORTED("AudioProfileNotSupported"),
  AUDIO_SAMPLE_RATE_NOT_SUPPORTED("AudioSampleRateNotSupported"),
  CONTAINER_BITRATE_EXCEEDS_LIMIT("ContainerBitrateExceedsLimit"),
  CONTAINER_NOT_SUPPORTED("ContainerNotSupported"),
  DIRECT_PLAY_ERROR("DirectPlayError"),
  INTERLACED_VIDEO_NOT_SUPPORTED("InterlacedVideoNotSupported"),
  REF_FRAMES_NOT_SUPPORTED("RefFramesNotSupported"),
  SECONDARY_AUDIO_NOT_SUPPORTED("SecondaryAudioNotSupported"),
  STREAM_COUNT_EXCEEDS_LIMIT("StreamCountExceedsLimit"),
  SUBTITLE_CODEC_NOT_SUPPORTED("SubtitleCodecNotSupported"),
  UNKNOWN_AUDIO_STREAM_INFO("UnknownAudioStreamInfo"),
  UNKNOWN_VIDEO_STREAM_INFO("UnknownVideoStreamInfo"),
  VIDEO_BIT_DEPTH_NOT_SUPPORTED("VideoBitDepthNotSupported"),
  VIDEO_BITRATE_NOT_SUPPORTED("VideoBitrateNotSupported"),
  VIDEO_CODEC_NOT_SUPPORTED("VideoCodecNotSupported"),
  VIDEO_CODEC_TAG_NOT_SUPPORTED("VideoCodecTagNotSupported"),
  VIDEO_FRAMERATE_NOT_SUPPORTED("VideoFramerateNotSupported"),
  VIDEO_LEVEL_NOT_SUPPORTED("VideoLevelNotSupported"),
  VIDEO_PROFILE_NOT_SUPPORTED("VideoProfileNotSupported"),
  VIDEO_RANGE_TYPE_NOT_SUPPORTED("VideoRangeTypeNotSupported"),
  VIDEO_RESOLUTION_NOT_SUPPORTED("VideoResolutionNotSupported");

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

