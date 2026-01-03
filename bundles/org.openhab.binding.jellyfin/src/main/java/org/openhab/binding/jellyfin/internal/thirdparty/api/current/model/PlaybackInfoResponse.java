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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class PlaybackInfoResponse.
 */
@JsonPropertyOrder({ PlaybackInfoResponse.JSON_PROPERTY_MEDIA_SOURCES,
        PlaybackInfoResponse.JSON_PROPERTY_PLAY_SESSION_ID, PlaybackInfoResponse.JSON_PROPERTY_ERROR_CODE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaybackInfoResponse {
    public static final String JSON_PROPERTY_MEDIA_SOURCES = "MediaSources";
    @org.eclipse.jdt.annotation.Nullable
    private List<MediaSourceInfo> mediaSources = new ArrayList<>();

    public static final String JSON_PROPERTY_PLAY_SESSION_ID = "PlaySessionId";
    @org.eclipse.jdt.annotation.Nullable
    private String playSessionId;

    public static final String JSON_PROPERTY_ERROR_CODE = "ErrorCode";
    @org.eclipse.jdt.annotation.Nullable
    private PlaybackErrorCode errorCode;

    public PlaybackInfoResponse() {
    }

    public PlaybackInfoResponse mediaSources(@org.eclipse.jdt.annotation.Nullable List<MediaSourceInfo> mediaSources) {
        this.mediaSources = mediaSources;
        return this;
    }

    public PlaybackInfoResponse addMediaSourcesItem(MediaSourceInfo mediaSourcesItem) {
        if (this.mediaSources == null) {
            this.mediaSources = new ArrayList<>();
        }
        this.mediaSources.add(mediaSourcesItem);
        return this;
    }

    /**
     * Gets or sets the media sources.
     * 
     * @return mediaSources
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaSourceInfo> getMediaSources() {
        return mediaSources;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSources(@org.eclipse.jdt.annotation.Nullable List<MediaSourceInfo> mediaSources) {
        this.mediaSources = mediaSources;
    }

    public PlaybackInfoResponse playSessionId(@org.eclipse.jdt.annotation.Nullable String playSessionId) {
        this.playSessionId = playSessionId;
        return this;
    }

    /**
     * Gets or sets the play session identifier.
     * 
     * @return playSessionId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaySessionId(@org.eclipse.jdt.annotation.Nullable String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public PlaybackInfoResponse errorCode(@org.eclipse.jdt.annotation.Nullable PlaybackErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Gets or sets the error code.
     * 
     * @return errorCode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ERROR_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlaybackErrorCode getErrorCode() {
        return errorCode;
    }

    @JsonProperty(value = JSON_PROPERTY_ERROR_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setErrorCode(@org.eclipse.jdt.annotation.Nullable PlaybackErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Return true if this PlaybackInfoResponse object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaybackInfoResponse playbackInfoResponse = (PlaybackInfoResponse) o;
        return Objects.equals(this.mediaSources, playbackInfoResponse.mediaSources)
                && Objects.equals(this.playSessionId, playbackInfoResponse.playSessionId)
                && Objects.equals(this.errorCode, playbackInfoResponse.errorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaSources, playSessionId, errorCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaybackInfoResponse {\n");
        sb.append("    mediaSources: ").append(toIndentedString(mediaSources)).append("\n");
        sb.append("    playSessionId: ").append(toIndentedString(playSessionId)).append("\n");
        sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
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

        // add `MediaSources` to the URL query string
        if (getMediaSources() != null) {
            for (int i = 0; i < getMediaSources().size(); i++) {
                if (getMediaSources().get(i) != null) {
                    joiner.add(getMediaSources().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sMediaSources%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `PlaySessionId` to the URL query string
        if (getPlaySessionId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaySessionId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaySessionId()))));
        }

        // add `ErrorCode` to the URL query string
        if (getErrorCode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sErrorCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getErrorCode()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlaybackInfoResponse instance;

        public Builder() {
            this(new PlaybackInfoResponse());
        }

        protected Builder(PlaybackInfoResponse instance) {
            this.instance = instance;
        }

        public PlaybackInfoResponse.Builder mediaSources(List<MediaSourceInfo> mediaSources) {
            this.instance.mediaSources = mediaSources;
            return this;
        }

        public PlaybackInfoResponse.Builder playSessionId(String playSessionId) {
            this.instance.playSessionId = playSessionId;
            return this;
        }

        public PlaybackInfoResponse.Builder errorCode(PlaybackErrorCode errorCode) {
            this.instance.errorCode = errorCode;
            return this;
        }

        /**
         * returns a built PlaybackInfoResponse instance.
         *
         * The builder is not reusable.
         */
        public PlaybackInfoResponse build() {
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
    public static PlaybackInfoResponse.Builder builder() {
        return new PlaybackInfoResponse.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlaybackInfoResponse.Builder toBuilder() {
        return new PlaybackInfoResponse.Builder().mediaSources(getMediaSources()).playSessionId(getPlaySessionId())
                .errorCode(getErrorCode());
    }
}
