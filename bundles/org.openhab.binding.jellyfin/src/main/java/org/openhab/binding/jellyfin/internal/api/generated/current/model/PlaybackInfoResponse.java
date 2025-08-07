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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class PlaybackInfoResponse.
 */
@JsonPropertyOrder({ PlaybackInfoResponse.JSON_PROPERTY_MEDIA_SOURCES,
        PlaybackInfoResponse.JSON_PROPERTY_PLAY_SESSION_ID, PlaybackInfoResponse.JSON_PROPERTY_ERROR_CODE })

public class PlaybackInfoResponse {
    public static final String JSON_PROPERTY_MEDIA_SOURCES = "MediaSources";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaSourceInfo> mediaSources = new ArrayList<>();

    public static final String JSON_PROPERTY_PLAY_SESSION_ID = "PlaySessionId";
    @org.eclipse.jdt.annotation.NonNull
    private String playSessionId;

    public static final String JSON_PROPERTY_ERROR_CODE = "ErrorCode";
    @org.eclipse.jdt.annotation.NonNull
    private PlaybackErrorCode errorCode;

    public PlaybackInfoResponse() {
    }

    public PlaybackInfoResponse mediaSources(@org.eclipse.jdt.annotation.NonNull List<MediaSourceInfo> mediaSources) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<MediaSourceInfo> getMediaSources() {
        return mediaSources;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSources(@org.eclipse.jdt.annotation.NonNull List<MediaSourceInfo> mediaSources) {
        this.mediaSources = mediaSources;
    }

    public PlaybackInfoResponse playSessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
        return this;
    }

    /**
     * Gets or sets the play session identifier.
     * 
     * @return playSessionId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaySessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public PlaybackInfoResponse errorCode(@org.eclipse.jdt.annotation.NonNull PlaybackErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Gets or sets the error code.
     * 
     * @return errorCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ERROR_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlaybackErrorCode getErrorCode() {
        return errorCode;
    }

    @JsonProperty(JSON_PROPERTY_ERROR_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setErrorCode(@org.eclipse.jdt.annotation.NonNull PlaybackErrorCode errorCode) {
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
}
