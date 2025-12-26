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
 * Class MovePlaylistItemRequestDto.
 */
@JsonPropertyOrder({ MovePlaylistItemRequestDto.JSON_PROPERTY_PLAYLIST_ITEM_ID,
        MovePlaylistItemRequestDto.JSON_PROPERTY_NEW_INDEX })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MovePlaylistItemRequestDto {
    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID playlistItemId;

    public static final String JSON_PROPERTY_NEW_INDEX = "NewIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer newIndex;

    public MovePlaylistItemRequestDto() {
    }

    public MovePlaylistItemRequestDto playlistItemId(@org.eclipse.jdt.annotation.NonNull UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets or sets the playlist identifier of the item.
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

    public MovePlaylistItemRequestDto newIndex(@org.eclipse.jdt.annotation.NonNull Integer newIndex) {
        this.newIndex = newIndex;
        return this;
    }

    /**
     * Gets or sets the new position.
     * 
     * @return newIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NEW_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getNewIndex() {
        return newIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_NEW_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewIndex(@org.eclipse.jdt.annotation.NonNull Integer newIndex) {
        this.newIndex = newIndex;
    }

    /**
     * Return true if this MovePlaylistItemRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MovePlaylistItemRequestDto movePlaylistItemRequestDto = (MovePlaylistItemRequestDto) o;
        return Objects.equals(this.playlistItemId, movePlaylistItemRequestDto.playlistItemId)
                && Objects.equals(this.newIndex, movePlaylistItemRequestDto.newIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistItemId, newIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MovePlaylistItemRequestDto {\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
        sb.append("    newIndex: ").append(toIndentedString(newIndex)).append("\n");
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

        // add `NewIndex` to the URL query string
        if (getNewIndex() != null) {
            joiner.add(String.format(Locale.ROOT, "%sNewIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNewIndex()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MovePlaylistItemRequestDto instance;

        public Builder() {
            this(new MovePlaylistItemRequestDto());
        }

        protected Builder(MovePlaylistItemRequestDto instance) {
            this.instance = instance;
        }

        public MovePlaylistItemRequestDto.Builder playlistItemId(UUID playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        public MovePlaylistItemRequestDto.Builder newIndex(Integer newIndex) {
            this.instance.newIndex = newIndex;
            return this;
        }

        /**
         * returns a built MovePlaylistItemRequestDto instance.
         *
         * The builder is not reusable.
         */
        public MovePlaylistItemRequestDto build() {
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
    public static MovePlaylistItemRequestDto.Builder builder() {
        return new MovePlaylistItemRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MovePlaylistItemRequestDto.Builder toBuilder() {
        return new MovePlaylistItemRequestDto.Builder().playlistItemId(getPlaylistItemId()).newIndex(getNewIndex());
    }
}
