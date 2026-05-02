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
 * Class SessionUserInfo.
 */
@JsonPropertyOrder({
  SessionUserInfo.JSON_PROPERTY_USER_ID,
  SessionUserInfo.JSON_PROPERTY_USER_NAME
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SessionUserInfo {
  public static final String JSON_PROPERTY_USER_ID = "UserId";
  @org.eclipse.jdt.annotation.Nullable

  private UUID userId;

  public static final String JSON_PROPERTY_USER_NAME = "UserName";
  @org.eclipse.jdt.annotation.Nullable

  private String userName;

  public SessionUserInfo() { 
  }

  public SessionUserInfo userId(@org.eclipse.jdt.annotation.Nullable
 UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Gets or sets the user identifier.
   * @return userId
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
  public UUID getUserId() {
    return userId;
  }


  @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
  public void setUserId(@org.eclipse.jdt.annotation.Nullable
 UUID userId) {
    this.userId = userId;
  }


  public SessionUserInfo userName(@org.eclipse.jdt.annotation.Nullable
 String userName) {
    this.userName = userName;
    return this;
  }

  /**
   * Gets or sets the name of the user.
   * @return userName
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_USER_NAME, required = false)
  public String getUserName() {
    return userName;
  }


  @JsonProperty(value = JSON_PROPERTY_USER_NAME, required = false)
  public void setUserName(@org.eclipse.jdt.annotation.Nullable
 String userName) {
    this.userName = userName;
  }


  /**
   * Return true if this SessionUserInfo object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SessionUserInfo sessionUserInfo = (SessionUserInfo) o;
    return Objects.equals(this.userId, sessionUserInfo.userId) &&
        Objects.equals(this.userName, sessionUserInfo.userName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, userName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SessionUserInfo {\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
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

    // add `UserName` to the URL query string
    if (getUserName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sUserName%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUserName()))));
    }

    return joiner.toString();
  }

    public static class Builder {

    private SessionUserInfo instance;

    public Builder() {
      this(new SessionUserInfo());
    }

    protected Builder(SessionUserInfo instance) {
      this.instance = instance;
    }

    public SessionUserInfo.Builder userId(UUID userId) {
      this.instance.userId = userId;
      return this;
    }
    public SessionUserInfo.Builder userName(String userName) {
      this.instance.userName = userName;
      return this;
    }


    /**
    * returns a built SessionUserInfo instance.
    *
    * The builder is not reusable.
    */
    public SessionUserInfo build() {
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
  public static SessionUserInfo.Builder builder() {
    return new SessionUserInfo.Builder();
  }

  /**
  * Create a builder with a shallow copy of this instance.
  */
  public SessionUserInfo.Builder toBuilder() {
    return new SessionUserInfo.Builder()
      .userId(getUserId())
      .userName(getUserName());
  }

}

