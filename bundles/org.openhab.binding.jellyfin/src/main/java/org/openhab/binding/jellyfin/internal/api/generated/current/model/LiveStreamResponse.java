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
 * LiveStreamResponse
 */
@JsonPropertyOrder({ LiveStreamResponse.JSON_PROPERTY_MEDIA_SOURCE })

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
}
