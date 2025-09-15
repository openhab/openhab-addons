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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * LiveStreamResponse
 */
@JsonPropertyOrder({ LiveStreamResponse.JSON_PROPERTY_MEDIA_SOURCE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LiveStreamResponse {
    public static final String JSON_PROPERTY_MEDIA_SOURCE = "MediaSource";
    @org.eclipse.jdt.annotation.NonNull
    private MediaSourceInfo mediaSource;

    public LiveStreamResponse() {
    }

    public LiveStreamResponse mediaSource(@org.eclipse.jdt.annotation.NonNull MediaSourceInfo mediaSource) {
        this.mediaSource = mediaSource;
        return this;
    }

    /**
     * Get mediaSource
     * 
     * @return mediaSource
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaSourceInfo getMediaSource() {
        return mediaSource;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSource(@org.eclipse.jdt.annotation.NonNull MediaSourceInfo mediaSource) {
        this.mediaSource = mediaSource;
    }

    /**
     * Return true if this LiveStreamResponse object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveStreamResponse liveStreamResponse = (LiveStreamResponse) o;
        return Objects.equals(this.mediaSource, liveStreamResponse.mediaSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaSource);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LiveStreamResponse {\n");
        sb.append("    mediaSource: ").append(toIndentedString(mediaSource)).append("\n");
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

        // add `MediaSource` to the URL query string
        if (getMediaSource() != null) {
            joiner.add(getMediaSource().toUrlQueryString(prefix + "MediaSource" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LiveStreamResponse instance;

        public Builder() {
            this(new LiveStreamResponse());
        }

        protected Builder(LiveStreamResponse instance) {
            this.instance = instance;
        }

        public LiveStreamResponse.Builder mediaSource(MediaSourceInfo mediaSource) {
            this.instance.mediaSource = mediaSource;
            return this;
        }

        /**
         * returns a built LiveStreamResponse instance.
         *
         * The builder is not reusable.
         */
        public LiveStreamResponse build() {
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
    public static LiveStreamResponse.Builder builder() {
        return new LiveStreamResponse.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LiveStreamResponse.Builder toBuilder() {
        return new LiveStreamResponse.Builder().mediaSource(getMediaSource());
    }
}
