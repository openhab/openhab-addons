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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * LyricLineCue model, holds information about the timing of words within a LyricLine.
 */
@JsonPropertyOrder({ LyricLineCue.JSON_PROPERTY_POSITION, LyricLineCue.JSON_PROPERTY_END_POSITION,
        LyricLineCue.JSON_PROPERTY_START, LyricLineCue.JSON_PROPERTY_END })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LyricLineCue {
    public static final String JSON_PROPERTY_POSITION = "Position";
    @org.eclipse.jdt.annotation.NonNull
    private Integer position;

    public static final String JSON_PROPERTY_END_POSITION = "EndPosition";
    @org.eclipse.jdt.annotation.NonNull
    private Integer endPosition;

    public static final String JSON_PROPERTY_START = "Start";
    @org.eclipse.jdt.annotation.NonNull
    private Long start;

    public static final String JSON_PROPERTY_END = "End";
    @org.eclipse.jdt.annotation.NonNull
    private Long end;

    public LyricLineCue() {
    }

    public LyricLineCue position(@org.eclipse.jdt.annotation.NonNull Integer position) {
        this.position = position;
        return this;
    }

    /**
     * Gets the start character index of the cue.
     * 
     * @return position
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_POSITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPosition() {
        return position;
    }

    @JsonProperty(value = JSON_PROPERTY_POSITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPosition(@org.eclipse.jdt.annotation.NonNull Integer position) {
        this.position = position;
    }

    public LyricLineCue endPosition(@org.eclipse.jdt.annotation.NonNull Integer endPosition) {
        this.endPosition = endPosition;
        return this;
    }

    /**
     * Gets the end character index of the cue.
     * 
     * @return endPosition
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_END_POSITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getEndPosition() {
        return endPosition;
    }

    @JsonProperty(value = JSON_PROPERTY_END_POSITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndPosition(@org.eclipse.jdt.annotation.NonNull Integer endPosition) {
        this.endPosition = endPosition;
    }

    public LyricLineCue start(@org.eclipse.jdt.annotation.NonNull Long start) {
        this.start = start;
        return this;
    }

    /**
     * Gets the timestamp the lyric is synced to in ticks.
     * 
     * @return start
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_START, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStart() {
        return start;
    }

    @JsonProperty(value = JSON_PROPERTY_START, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStart(@org.eclipse.jdt.annotation.NonNull Long start) {
        this.start = start;
    }

    public LyricLineCue end(@org.eclipse.jdt.annotation.NonNull Long end) {
        this.end = end;
        return this;
    }

    /**
     * Gets the end timestamp the lyric is synced to in ticks.
     * 
     * @return end
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_END, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getEnd() {
        return end;
    }

    @JsonProperty(value = JSON_PROPERTY_END, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnd(@org.eclipse.jdt.annotation.NonNull Long end) {
        this.end = end;
    }

    /**
     * Return true if this LyricLineCue object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LyricLineCue lyricLineCue = (LyricLineCue) o;
        return Objects.equals(this.position, lyricLineCue.position)
                && Objects.equals(this.endPosition, lyricLineCue.endPosition)
                && Objects.equals(this.start, lyricLineCue.start) && Objects.equals(this.end, lyricLineCue.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, endPosition, start, end);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LyricLineCue {\n");
        sb.append("    position: ").append(toIndentedString(position)).append("\n");
        sb.append("    endPosition: ").append(toIndentedString(endPosition)).append("\n");
        sb.append("    start: ").append(toIndentedString(start)).append("\n");
        sb.append("    end: ").append(toIndentedString(end)).append("\n");
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

        // add `Position` to the URL query string
        if (getPosition() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPosition%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPosition()))));
        }

        // add `EndPosition` to the URL query string
        if (getEndPosition() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEndPosition%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndPosition()))));
        }

        // add `Start` to the URL query string
        if (getStart() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStart%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStart()))));
        }

        // add `End` to the URL query string
        if (getEnd() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnd%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnd()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LyricLineCue instance;

        public Builder() {
            this(new LyricLineCue());
        }

        protected Builder(LyricLineCue instance) {
            this.instance = instance;
        }

        public LyricLineCue.Builder position(Integer position) {
            this.instance.position = position;
            return this;
        }

        public LyricLineCue.Builder endPosition(Integer endPosition) {
            this.instance.endPosition = endPosition;
            return this;
        }

        public LyricLineCue.Builder start(Long start) {
            this.instance.start = start;
            return this;
        }

        public LyricLineCue.Builder end(Long end) {
            this.instance.end = end;
            return this;
        }

        /**
         * returns a built LyricLineCue instance.
         *
         * The builder is not reusable.
         */
        public LyricLineCue build() {
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
    public static LyricLineCue.Builder builder() {
        return new LyricLineCue.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LyricLineCue.Builder toBuilder() {
        return new LyricLineCue.Builder().position(getPosition()).endPosition(getEndPosition()).start(getStart())
                .end(getEnd());
    }
}
