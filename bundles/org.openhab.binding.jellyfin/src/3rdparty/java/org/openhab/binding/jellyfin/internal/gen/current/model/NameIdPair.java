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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.openhab.binding.jellyfin.internal.gen.ApiClient;
/**
 * NameIdPair
 */
@JsonPropertyOrder({
  NameIdPair.JSON_PROPERTY_NAME,
  NameIdPair.JSON_PROPERTY_ID
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NameIdPair {
  public static final String JSON_PROPERTY_NAME = "Name";
  @org.eclipse.jdt.annotation.Nullable

  private String name;

  public static final String JSON_PROPERTY_ID = "Id";
  @org.eclipse.jdt.annotation.Nullable

  private String id;

  public NameIdPair() { 
  }

  public NameIdPair name(@org.eclipse.jdt.annotation.Nullable
 String name) {
    this.name = name;
    return this;
  }

  /**
   * Gets or sets the name.
   * @return name
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_NAME)
  public String getName() {
    return name;
  }


  @JsonProperty(value = JSON_PROPERTY_NAME)
  public void setName(@org.eclipse.jdt.annotation.Nullable
 String name) {
    this.name = name;
  }


  public NameIdPair id(@org.eclipse.jdt.annotation.Nullable
 String id) {
    this.id = id;
    return this;
  }

  /**
   * Gets or sets the identifier.
   * @return id
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_ID)
  public String getId() {
    return id;
  }


  @JsonProperty(value = JSON_PROPERTY_ID)
  public void setId(@org.eclipse.jdt.annotation.Nullable
 String id) {
    this.id = id;
  }


  /**
   * Return true if this NameIdPair object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NameIdPair nameIdPair = (NameIdPair) o;
    return Objects.equals(this.name, nameIdPair.name) &&
        Objects.equals(this.id, nameIdPair.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NameIdPair {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

    // add `Name` to the URL query string
    if (getName() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getName()))));
    }

    // add `Id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    return joiner.toString();
  }

    public static class Builder {

    private NameIdPair instance;

    public Builder() {
      this(new NameIdPair());
    }

    protected Builder(NameIdPair instance) {
      this.instance = instance;
    }

    public NameIdPair.Builder name(String name) {
      this.instance.name = name;
      return this;
    }
    public NameIdPair.Builder id(String id) {
      this.instance.id = id;
      return this;
    }


    /**
    * returns a built NameIdPair instance.
    *
    * The builder is not reusable.
    */
    public NameIdPair build() {
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
  public static NameIdPair.Builder builder() {
    return new NameIdPair.Builder();
  }

  /**
  * Create a builder with a shallow copy of this instance.
  */
  public NameIdPair.Builder toBuilder() {
    return new NameIdPair.Builder()
      .name(getName())
      .id(getId());
  }

}

