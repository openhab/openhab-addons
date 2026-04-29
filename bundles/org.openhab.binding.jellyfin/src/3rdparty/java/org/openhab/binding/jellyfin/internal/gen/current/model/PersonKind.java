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
 * The person kind.
 */
public enum PersonKind {
  ACTOR("Actor"),
  ALBUM_ARTIST("AlbumArtist"),
  ARRANGER("Arranger"),
  ARTIST("Artist"),
  AUTHOR("Author"),
  COLORIST("Colorist"),
  COMPOSER("Composer"),
  CONDUCTOR("Conductor"),
  COVER_ARTIST("CoverArtist"),
  CREATOR("Creator"),
  DIRECTOR("Director"),
  EDITOR("Editor"),
  ENGINEER("Engineer"),
  GUEST_STAR("GuestStar"),
  ILLUSTRATOR("Illustrator"),
  INKER("Inker"),
  LETTERER("Letterer"),
  LYRICIST("Lyricist"),
  MIXER("Mixer"),
  PENCILLER("Penciller"),
  PRODUCER("Producer"),
  REMIXER("Remixer"),
  TRANSLATOR("Translator"),
  UNKNOWN("Unknown"),
  WRITER("Writer");

  private String value;

  PersonKind(String value) {
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
  public static PersonKind fromValue(String value) {
    for (PersonKind b : PersonKind.values()) {
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

