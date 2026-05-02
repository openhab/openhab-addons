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
import org.openhab.binding.jellyfin.internal.gen.current.model.ParentalRatingScore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import org.openhab.binding.jellyfin.internal.gen.ApiClient;
/**
 * Class ParentalRating.
 */
@JsonPropertyOrder({
  ParentalRating.JSON_PROPERTY_NAME,
  ParentalRating.JSON_PROPERTY_VALUE,
  ParentalRating.JSON_PROPERTY_RATING_SCORE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ParentalRating {
  public static final String JSON_PROPERTY_NAME = "Name";
  @org.eclipse.jdt.annotation.Nullable

  private String name;

  public static final String JSON_PROPERTY_VALUE = "Value";
  @org.eclipse.jdt.annotation.Nullable

  private Integer value;

  public static final String JSON_PROPERTY_RATING_SCORE = "RatingScore";
  @org.eclipse.jdt.annotation.Nullable

  private ParentalRatingScore ratingScore;

  public ParentalRating() { 
  }

  public ParentalRating name(@org.eclipse.jdt.annotation.Nullable
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


  public ParentalRating value(@org.eclipse.jdt.annotation.Nullable
 Integer value) {
    this.value = value;
    return this;
  }

  /**
   * Gets or sets the value.
   * @return value
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_VALUE)
  public Integer getValue() {
    return value;
  }


  @JsonProperty(value = JSON_PROPERTY_VALUE)
  public void setValue(@org.eclipse.jdt.annotation.Nullable
 Integer value) {
    this.value = value;
  }


  public ParentalRating ratingScore(@org.eclipse.jdt.annotation.Nullable
 ParentalRatingScore ratingScore) {
    this.ratingScore = ratingScore;
    return this;
  }

  /**
   * Gets or sets the rating score.
   * @return ratingScore
   */
  @org.eclipse.jdt.annotation.Nullable

  @JsonProperty(value = JSON_PROPERTY_RATING_SCORE)
  public ParentalRatingScore getRatingScore() {
    return ratingScore;
  }


  @JsonProperty(value = JSON_PROPERTY_RATING_SCORE)
  public void setRatingScore(@org.eclipse.jdt.annotation.Nullable
 ParentalRatingScore ratingScore) {
    this.ratingScore = ratingScore;
  }


  /**
   * Return true if this ParentalRating object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParentalRating parentalRating = (ParentalRating) o;
    return Objects.equals(this.name, parentalRating.name) &&
        Objects.equals(this.value, parentalRating.value) &&
        Objects.equals(this.ratingScore, parentalRating.ratingScore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, ratingScore);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ParentalRating {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    ratingScore: ").append(toIndentedString(ratingScore)).append("\n");
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

    // add `Value` to the URL query string
    if (getValue() != null) {
      joiner.add(String.format(java.util.Locale.ROOT, "%sValue%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValue()))));
    }

    // add `RatingScore` to the URL query string
    if (getRatingScore() != null) {
      joiner.add(getRatingScore().toUrlQueryString(prefix + "RatingScore" + suffix));
    }

    return joiner.toString();
  }

    public static class Builder {

    private ParentalRating instance;

    public Builder() {
      this(new ParentalRating());
    }

    protected Builder(ParentalRating instance) {
      this.instance = instance;
    }

    public ParentalRating.Builder name(String name) {
      this.instance.name = name;
      return this;
    }
    public ParentalRating.Builder value(Integer value) {
      this.instance.value = value;
      return this;
    }
    public ParentalRating.Builder ratingScore(ParentalRatingScore ratingScore) {
      this.instance.ratingScore = ratingScore;
      return this;
    }


    /**
    * returns a built ParentalRating instance.
    *
    * The builder is not reusable.
    */
    public ParentalRating build() {
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
  public static ParentalRating.Builder builder() {
    return new ParentalRating.Builder();
  }

  /**
  * Create a builder with a shallow copy of this instance.
  */
  public ParentalRating.Builder toBuilder() {
    return new ParentalRating.Builder()
      .name(getName())
      .value(getValue())
      .ratingScore(getRatingScore());
  }

}

