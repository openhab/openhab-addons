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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Lyric model.
 */
@JsonPropertyOrder({ LyricLine.JSON_PROPERTY_TEXT, LyricLine.JSON_PROPERTY_START })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LyricLine {
    public static final String JSON_PROPERTY_TEXT = "Text";
    @org.eclipse.jdt.annotation.NonNull
    private String text;

    public static final String JSON_PROPERTY_START = "Start";
    @org.eclipse.jdt.annotation.NonNull
    private Long start;

    public LyricLine() {
    }

    public LyricLine text(@org.eclipse.jdt.annotation.NonNull String text) {
        this.text = text;
        return this;
    }

    /**
     * Gets the text of this lyric line.
     * 
     * @return text
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getText() {
        return text;
    }

    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setText(@org.eclipse.jdt.annotation.NonNull String text) {
        this.text = text;
    }

    public LyricLine start(@org.eclipse.jdt.annotation.NonNull Long start) {
        this.start = start;
        return this;
    }

    /**
     * Gets the start time in ticks.
     * 
     * @return start
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStart() {
        return start;
    }

    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStart(@org.eclipse.jdt.annotation.NonNull Long start) {
        this.start = start;
    }

    /**
     * Return true if this LyricLine object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LyricLine lyricLine = (LyricLine) o;
        return Objects.equals(this.text, lyricLine.text) && Objects.equals(this.start, lyricLine.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, start);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LyricLine {\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("    start: ").append(toIndentedString(start)).append("\n");
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

        // add `Text` to the URL query string
        if (getText() != null) {
            joiner.add(String.format("%sText%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getText()))));
        }

        // add `Start` to the URL query string
        if (getStart() != null) {
            joiner.add(String.format("%sStart%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStart()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LyricLine instance;

        public Builder() {
            this(new LyricLine());
        }

        protected Builder(LyricLine instance) {
            this.instance = instance;
        }

        public LyricLine.Builder text(String text) {
            this.instance.text = text;
            return this;
        }

        public LyricLine.Builder start(Long start) {
            this.instance.start = start;
            return this;
        }

        /**
         * returns a built LyricLine instance.
         *
         * The builder is not reusable.
         */
        public LyricLine build() {
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
    public static LyricLine.Builder builder() {
        return new LyricLine.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LyricLine.Builder toBuilder() {
        return new LyricLine.Builder().text(getText()).start(getStart());
    }
}
