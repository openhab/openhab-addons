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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class SetPlaylistItemRequestDto.
 */
@JsonPropertyOrder({ SetPlaylistItemRequestDto.JSON_PROPERTY_PLAYLIST_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SetPlaylistItemRequestDto {
    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID playlistItemId;

    public SetPlaylistItemRequestDto() {
    }

    public SetPlaylistItemRequestDto playlistItemId(@org.eclipse.jdt.annotation.NonNull UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets or sets the playlist identifier of the playing item.
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    /**
     * Return true if this SetPlaylistItemRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetPlaylistItemRequestDto setPlaylistItemRequestDto = (SetPlaylistItemRequestDto) o;
        return Objects.equals(this.playlistItemId, setPlaylistItemRequestDto.playlistItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistItemId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SetPlaylistItemRequestDto {\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
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

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SetPlaylistItemRequestDto instance;

        public Builder() {
            this(new SetPlaylistItemRequestDto());
        }

        protected Builder(SetPlaylistItemRequestDto instance) {
            this.instance = instance;
        }

        public SetPlaylistItemRequestDto.Builder playlistItemId(UUID playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        /**
         * returns a built SetPlaylistItemRequestDto instance.
         *
         * The builder is not reusable.
         */
        public SetPlaylistItemRequestDto build() {
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
    public static SetPlaylistItemRequestDto.Builder builder() {
        return new SetPlaylistItemRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SetPlaylistItemRequestDto.Builder toBuilder() {
        return new SetPlaylistItemRequestDto.Builder().playlistItemId(getPlaylistItemId());
    }
}
