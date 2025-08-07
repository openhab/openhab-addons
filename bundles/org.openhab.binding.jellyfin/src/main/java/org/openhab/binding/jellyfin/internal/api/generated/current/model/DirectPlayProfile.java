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
    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getContainer() {
        return container;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER)
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
    @JsonProperty(JSON_PROPERTY_AUDIO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAudioCodec() {
        return audioCodec;
    }

    @JsonProperty(JSON_PROPERTY_AUDIO_CODEC)
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
    @JsonProperty(JSON_PROPERTY_VIDEO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getVideoCodec() {
        return videoCodec;
    }

    @JsonProperty(JSON_PROPERTY_VIDEO_CODEC)
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
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DlnaProfileType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
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
}
