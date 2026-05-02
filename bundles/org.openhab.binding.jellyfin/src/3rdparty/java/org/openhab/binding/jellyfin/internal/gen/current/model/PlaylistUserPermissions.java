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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.openhab.binding.jellyfin.internal.gen.ApiClient;
/**
 * Class to hold data on user permissions for playlists.
 */
@JsonPropertyOrder({
  PlaylistUserPermissions.JSON_PROPERTY_USER_ID,
  PlaylistUserPermissions.JSON_PROPERTY_CAN_EDIT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaylistUserPermissions {
  public static final String JSON_PROPERTY_USER_ID = "UserId";
  @org.eclipse.jdt.annotation.Nullable

  private UUID userId;

  public static final String JSON_PROPERTY_CAN_EDIT = "CanEdit";
  @org.eclipse.jdt.annotation.Nullable

  private Boolean canEdit;

  public PlaylistUserPermissions() { 
  }

  public PlaylistUserPermissions userId(@org.eclipse.jdt.annotation.Nullable
 UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Gets or sets the user id.
   * @return userId
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_USER_ID)
  public UUID getUserId() {
    return userId;
  }


  @JsonProperty(value = JSON_PROPERTY_USER_ID)
  public void setUserId(@org.eclipse.jdt.annotation.Nullable
 UUID userId) {
    this.userId = userId;
  }


  public PlaylistUserPermissions canEdit(@org.eclipse.jdt.annotation.Nullable
 Boolean canEdit) {
    this.canEdit = canEdit;
    return this;
  }

  /**
   * Gets or sets a value indicating whether the user has edit permissions.
   * @return canEdit
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_CAN_EDIT)
  public Boolean getCanEdit() {
    return canEdit;
  }


  @JsonProperty(value = JSON_PROPERTY_CAN_EDIT)
  public void setCanEdit(@org.eclipse.jdt.annotation.Nullable
 Boolean canEdit) {
    this.canEdit = canEdit;
  }


  /**
   * Return true if this PlaylistUserPermissions object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlaylistUserPermissions playlistUserPermissions = (PlaylistUserPermissions) o;
    return Objects.equals(this.userId, playlistUserPermissions.userId) &&
        Objects.equals(this.canEdit, playlistUserPermissions.canEdit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, canEdit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlaylistUserPermissions {\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    canEdit: ").append(toIndentedString(canEdit)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `UserId` to the URL query string
    if (getUserId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sUserId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
    }

    // add `CanEdit` to the URL query string
    if (getCanEdit() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sCanEdit%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCanEdit()))));
    }

    return joiner.toString();
  }

    public static class Builder {

    private PlaylistUserPermissions instance;

    public Builder() {
      this(new PlaylistUserPermissions());
    }

    protected Builder(PlaylistUserPermissions instance) {
      this.instance = instance;
    }

    public PlaylistUserPermissions.Builder userId(UUID userId) {
      this.instance.userId = userId;
      return this;
    }
    public PlaylistUserPermissions.Builder canEdit(Boolean canEdit) {
      this.instance.canEdit = canEdit;
      return this;
    }


    /**
    * returns a built PlaylistUserPermissions instance.
    *
    * The builder is not reusable.
    */
    public PlaylistUserPermissions build() {
      try {
        return this.instance;
      } finally {
        // ensure that this.instance is not reused
        this.instance = null;
      }
    }

    @Override
    public String toString() {
      return getClass() + "=(" + instance + ")";
    }
  }

  /**
  * Create a builder with no initialized field.
  */
  public static PlaylistUserPermissions.Builder builder() {
    return new PlaylistUserPermissions.Builder();
  }

  /**
  * Create a builder with a shallow copy of this instance.
  */
  public PlaylistUserPermissions.Builder toBuilder() {
    return new PlaylistUserPermissions.Builder()
      .userId(getUserId())
      .canEdit(getCanEdit());
  }

}

