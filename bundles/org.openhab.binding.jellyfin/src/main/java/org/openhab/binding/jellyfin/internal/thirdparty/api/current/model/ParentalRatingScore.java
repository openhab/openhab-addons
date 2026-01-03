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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A class representing an parental rating score.
 */
@JsonPropertyOrder({ ParentalRatingScore.JSON_PROPERTY_SCORE, ParentalRatingScore.JSON_PROPERTY_SUB_SCORE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ParentalRatingScore {
    public static final String JSON_PROPERTY_SCORE = "score";
    @org.eclipse.jdt.annotation.Nullable
    private Integer score;

    public static final String JSON_PROPERTY_SUB_SCORE = "subScore";
    @org.eclipse.jdt.annotation.Nullable
    private Integer subScore;

    public ParentalRatingScore() {
    }

    public ParentalRatingScore score(@org.eclipse.jdt.annotation.Nullable Integer score) {
        this.score = score;
        return this;
    }

    /**
     * Gets or sets the score.
     * 
     * @return score
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SCORE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getScore() {
        return score;
    }

    @JsonProperty(value = JSON_PROPERTY_SCORE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScore(@org.eclipse.jdt.annotation.Nullable Integer score) {
        this.score = score;
    }

    public ParentalRatingScore subScore(@org.eclipse.jdt.annotation.Nullable Integer subScore) {
        this.subScore = subScore;
        return this;
    }

    /**
     * Gets or sets the sub score.
     * 
     * @return subScore
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUB_SCORE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSubScore() {
        return subScore;
    }

    @JsonProperty(value = JSON_PROPERTY_SUB_SCORE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubScore(@org.eclipse.jdt.annotation.Nullable Integer subScore) {
        this.subScore = subScore;
    }

    /**
     * Return true if this ParentalRatingScore object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParentalRatingScore parentalRatingScore = (ParentalRatingScore) o;
        return Objects.equals(this.score, parentalRatingScore.score)
                && Objects.equals(this.subScore, parentalRatingScore.subScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, subScore);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ParentalRatingScore {\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    subScore: ").append(toIndentedString(subScore)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
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

        // add `score` to the URL query string
        if (getScore() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sscore%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getScore()))));
        }

        // add `subScore` to the URL query string
        if (getSubScore() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%ssubScore%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSubScore()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ParentalRatingScore instance;

        public Builder() {
            this(new ParentalRatingScore());
        }

        protected Builder(ParentalRatingScore instance) {
            this.instance = instance;
        }

        public ParentalRatingScore.Builder score(Integer score) {
            this.instance.score = score;
            return this;
        }

        public ParentalRatingScore.Builder subScore(Integer subScore) {
            this.instance.subScore = subScore;
            return this;
        }

        /**
         * returns a built ParentalRatingScore instance.
         *
         * The builder is not reusable.
         */
        public ParentalRatingScore build() {
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
    public static ParentalRatingScore.Builder builder() {
        return new ParentalRatingScore.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ParentalRatingScore.Builder toBuilder() {
        return new ParentalRatingScore.Builder().score(getScore()).subScore(getSubScore());
    }
}
