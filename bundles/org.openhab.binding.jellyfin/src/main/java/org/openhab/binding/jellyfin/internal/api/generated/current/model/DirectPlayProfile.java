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
 * Defines the MediaBrowser.Model.Dlna.DirectPlayProfile.
 */
@JsonPropertyOrder({ DirectPlayProfile.JSON_PROPERTY_CONTAINER, DirectPlayProfile.JSON_PROPERTY_AUDIO_CODEC,
        DirectPlayProfile.JSON_PROPERTY_VIDEO_CODEC, DirectPlayProfile.JSON_PROPERTY_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DirectPlayProfile {
    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_AUDIO_CODEC = "AudioCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String audioCodec;

    public static final String JSON_PROPERTY_VIDEO_CODEC = "VideoCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String videoCodec;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private DlnaProfileType type;

    public DirectPlayProfile() {
    }

    public DirectPlayProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the container.
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    public DirectPlayProfile audioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
        return this;
    }

    /**
     * Gets or sets the audio codec.
     * 
     * @return audioCodec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AUDIO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAudioCodec() {
        return audioCodec;
    }

    @JsonProperty(value = JSON_PROPERTY_AUDIO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public DirectPlayProfile videoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
        return this;
    }

    /**
     * Gets or sets the video codec.
     * 
     * @return videoCodec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VIDEO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVideoCodec() {
        return videoCodec;
    }

    @JsonProperty(value = JSON_PROPERTY_VIDEO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public DirectPlayProfile type(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the Dlna profile type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DlnaProfileType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
    }

    /**
     * Return true if this DirectPlayProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DirectPlayProfile directPlayProfile = (DirectPlayProfile) o;
        return Objects.equals(this.container, directPlayProfile.container)
                && Objects.equals(this.audioCodec, directPlayProfile.audioCodec)
                && Objects.equals(this.videoCodec, directPlayProfile.videoCodec)
                && Objects.equals(this.type, directPlayProfile.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, audioCodec, videoCodec, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DirectPlayProfile {\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    audioCodec: ").append(toIndentedString(audioCodec)).append("\n");
        sb.append("    videoCodec: ").append(toIndentedString(videoCodec)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format(Locale.ROOT, "%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `AudioCodec` to the URL query string
        if (getAudioCodec() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAudioCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioCodec()))));
        }

        // add `VideoCodec` to the URL query string
        if (getVideoCodec() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVideoCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoCodec()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DirectPlayProfile instance;

        public Builder() {
            this(new DirectPlayProfile());
        }

        protected Builder(DirectPlayProfile instance) {
            this.instance = instance;
        }

        public DirectPlayProfile.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public DirectPlayProfile.Builder audioCodec(String audioCodec) {
            this.instance.audioCodec = audioCodec;
            return this;
        }

        public DirectPlayProfile.Builder videoCodec(String videoCodec) {
            this.instance.videoCodec = videoCodec;
            return this;
        }

        public DirectPlayProfile.Builder type(DlnaProfileType type) {
            this.instance.type = type;
            return this;
        }

        /**
         * returns a built DirectPlayProfile instance.
         *
         * The builder is not reusable.
         */
        public DirectPlayProfile build() {
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
    public static DirectPlayProfile.Builder builder() {
        return new DirectPlayProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DirectPlayProfile.Builder toBuilder() {
        return new DirectPlayProfile.Builder().container(getContainer()).audioCodec(getAudioCodec())
                .videoCodec(getVideoCodec()).type(getType());
    }
}
