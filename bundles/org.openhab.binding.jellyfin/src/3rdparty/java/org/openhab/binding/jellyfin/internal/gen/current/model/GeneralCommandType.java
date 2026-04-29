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
 * This exists simply to identify a set of known commands.
 */
public enum GeneralCommandType {
  BACK("Back"),
  CHANNEL_DOWN("ChannelDown"),
  CHANNEL_UP("ChannelUp"),
  DISPLAY_CONTENT("DisplayContent"),
  DISPLAY_MESSAGE("DisplayMessage"),
  GO_HOME("GoHome"),
  GO_TO_SEARCH("GoToSearch"),
  GO_TO_SETTINGS("GoToSettings"),
  GUIDE("Guide"),
  MOVE_DOWN("MoveDown"),
  MOVE_LEFT("MoveLeft"),
  MOVE_RIGHT("MoveRight"),
  MOVE_UP("MoveUp"),
  MUTE("Mute"),
  NEXT_LETTER("NextLetter"),
  PAGE_DOWN("PageDown"),
  PAGE_UP("PageUp"),
  PLAY("Play"),
  PLAY_MEDIA_SOURCE("PlayMediaSource"),
  PLAY_NEXT("PlayNext"),
  PLAY_STATE("PlayState"),
  PLAY_TRAILERS("PlayTrailers"),
  PREVIOUS_LETTER("PreviousLetter"),
  SELECT("Select"),
  SEND_KEY("SendKey"),
  SEND_STRING("SendString"),
  SET_AUDIO_STREAM_INDEX("SetAudioStreamIndex"),
  SET_MAX_STREAMING_BITRATE("SetMaxStreamingBitrate"),
  SET_PLAYBACK_ORDER("SetPlaybackOrder"),
  SET_REPEAT_MODE("SetRepeatMode"),
  SET_SHUFFLE_QUEUE("SetShuffleQueue"),
  SET_SUBTITLE_STREAM_INDEX("SetSubtitleStreamIndex"),
  SET_VOLUME("SetVolume"),
  TAKE_SCREENSHOT("TakeScreenshot"),
  TOGGLE_CONTEXT_MENU("ToggleContextMenu"),
  TOGGLE_FULLSCREEN("ToggleFullscreen"),
  TOGGLE_MUTE("ToggleMute"),
  TOGGLE_OSD("ToggleOsd"),
  TOGGLE_OSD_MENU("ToggleOsdMenu"),
  TOGGLE_STATS("ToggleStats"),
  UNMUTE("Unmute"),
  VOLUME_DOWN("VolumeDown"),
  VOLUME_UP("VolumeUp");

  private String value;

  GeneralCommandType(String value) {
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
  public static GeneralCommandType fromValue(String value) {
    for (GeneralCommandType b : GeneralCommandType.values()) {
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

