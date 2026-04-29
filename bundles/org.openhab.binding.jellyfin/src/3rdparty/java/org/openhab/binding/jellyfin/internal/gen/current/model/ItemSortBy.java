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
 * These represent sort orders.
 */
public enum ItemSortBy {
  AIRED_EPISODE_ORDER("AiredEpisodeOrder"),
  AIR_TIME("AirTime"),
  ALBUM("Album"),
  ALBUM_ARTIST("AlbumArtist"),
  ARTIST("Artist"),
  COMMUNITY_RATING("CommunityRating"),
  CRITIC_RATING("CriticRating"),
  DATE_CREATED("DateCreated"),
  DATE_LAST_CONTENT_ADDED("DateLastContentAdded"),
  DATE_PLAYED("DatePlayed"),
  DEFAULT("Default"),
  INDEX_NUMBER("IndexNumber"),
  IS_FAVORITE_OR_LIKED("IsFavoriteOrLiked"),
  IS_FOLDER("IsFolder"),
  IS_PLAYED("IsPlayed"),
  IS_UNPLAYED("IsUnplayed"),
  NAME("Name"),
  OFFICIAL_RATING("OfficialRating"),
  PARENT_INDEX_NUMBER("ParentIndexNumber"),
  PLAY_COUNT("PlayCount"),
  PREMIERE_DATE("PremiereDate"),
  PRODUCTION_YEAR("ProductionYear"),
  RANDOM("Random"),
  RUNTIME("Runtime"),
  SERIES_DATE_PLAYED("SeriesDatePlayed"),
  SERIES_SORT_NAME("SeriesSortName"),
  SORT_NAME("SortName"),
  START_DATE("StartDate"),
  STUDIO("Studio"),
  VIDEO_BIT_RATE("VideoBitRate");

  private String value;

  ItemSortBy(String value) {
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
  public static ItemSortBy fromValue(String value) {
    for (ItemSortBy b : ItemSortBy.values()) {
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

