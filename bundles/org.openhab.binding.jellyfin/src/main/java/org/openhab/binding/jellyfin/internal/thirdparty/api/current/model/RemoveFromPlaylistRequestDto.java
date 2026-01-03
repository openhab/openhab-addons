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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class RemoveFromPlaylistRequestDto.
 */
@JsonPropertyOrder({ RemoveFromPlaylistRequestDto.JSON_PROPERTY_PLAYLIST_ITEM_IDS,
        RemoveFromPlaylistRequestDto.JSON_PROPERTY_CLEAR_PLAYLIST,
        RemoveFromPlaylistRequestDto.JSON_PROPERTY_CLEAR_PLAYING_ITEM })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RemoveFromPlaylistRequestDto {
    public static final String JSON_PROPERTY_PLAYLIST_ITEM_IDS = "PlaylistItemIds";
    @org.eclipse.jdt.annotation.Nullable
    private List<UUID> playlistItemIds = new ArrayList<>();

    public static final String JSON_PROPERTY_CLEAR_PLAYLIST = "ClearPlaylist";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean clearPlaylist;

    public static final String JSON_PROPERTY_CLEAR_PLAYING_ITEM = "ClearPlayingItem";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean clearPlayingItem;

    public RemoveFromPlaylistRequestDto() {
    }

    public RemoveFromPlaylistRequestDto playlistItemIds(
            @org.eclipse.jdt.annotation.Nullable List<UUID> playlistItemIds) {
        this.playlistItemIds = playlistItemIds;
        return this;
    }

    public RemoveFromPlaylistRequestDto addPlaylistItemIdsItem(UUID playlistItemIdsItem) {
        if (this.playlistItemIds == null) {
            this.playlistItemIds = new ArrayList<>();
        }
        this.playlistItemIds.add(playlistItemIdsItem);
        return this;
    }

    /**
     * Gets or sets the playlist identifiers of the items. Ignored when clearing the playlist.
     * 
     * @return playlistItemIds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getPlaylistItemIds() {
        return playlistItemIds;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemIds(@org.eclipse.jdt.annotation.Nullable List<UUID> playlistItemIds) {
        this.playlistItemIds = playlistItemIds;
    }

    public RemoveFromPlaylistRequestDto clearPlaylist(@org.eclipse.jdt.annotation.Nullable Boolean clearPlaylist) {
        this.clearPlaylist = clearPlaylist;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the entire playlist should be cleared.
     * 
     * @return clearPlaylist
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CLEAR_PLAYLIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getClearPlaylist() {
        return clearPlaylist;
    }

    @JsonProperty(value = JSON_PROPERTY_CLEAR_PLAYLIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClearPlaylist(@org.eclipse.jdt.annotation.Nullable Boolean clearPlaylist) {
        this.clearPlaylist = clearPlaylist;
    }

    public RemoveFromPlaylistRequestDto clearPlayingItem(
            @org.eclipse.jdt.annotation.Nullable Boolean clearPlayingItem) {
        this.clearPlayingItem = clearPlayingItem;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the playing item should be removed as well. Used only when clearing the
     * playlist.
     * 
     * @return clearPlayingItem
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CLEAR_PLAYING_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getClearPlayingItem() {
        return clearPlayingItem;
    }

    @JsonProperty(value = JSON_PROPERTY_CLEAR_PLAYING_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClearPlayingItem(@org.eclipse.jdt.annotation.Nullable Boolean clearPlayingItem) {
        this.clearPlayingItem = clearPlayingItem;
    }

    /**
     * Return true if this RemoveFromPlaylistRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto = (RemoveFromPlaylistRequestDto) o;
        return Objects.equals(this.playlistItemIds, removeFromPlaylistRequestDto.playlistItemIds)
                && Objects.equals(this.clearPlaylist, removeFromPlaylistRequestDto.clearPlaylist)
                && Objects.equals(this.clearPlayingItem, removeFromPlaylistRequestDto.clearPlayingItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistItemIds, clearPlaylist, clearPlayingItem);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoveFromPlaylistRequestDto {\n");
        sb.append("    playlistItemIds: ").append(toIndentedString(playlistItemIds)).append("\n");
        sb.append("    clearPlaylist: ").append(toIndentedString(clearPlaylist)).append("\n");
        sb.append("    clearPlayingItem: ").append(toIndentedString(clearPlayingItem)).append("\n");
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

        // add `PlaylistItemIds` to the URL query string
        if (getPlaylistItemIds() != null) {
            for (int i = 0; i < getPlaylistItemIds().size(); i++) {
                if (getPlaylistItemIds().get(i) != null) {
                    joiner.add(String.format(java.util.Locale.ROOT, "%sPlaylistItemIds%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                            containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemIds().get(i)))));
                }
            }
        }

        // add `ClearPlaylist` to the URL query string
        if (getClearPlaylist() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sClearPlaylist%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getClearPlaylist()))));
        }

        // add `ClearPlayingItem` to the URL query string
        if (getClearPlayingItem() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sClearPlayingItem%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getClearPlayingItem()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private RemoveFromPlaylistRequestDto instance;

        public Builder() {
            this(new RemoveFromPlaylistRequestDto());
        }

        protected Builder(RemoveFromPlaylistRequestDto instance) {
            this.instance = instance;
        }

        public RemoveFromPlaylistRequestDto.Builder playlistItemIds(List<UUID> playlistItemIds) {
            this.instance.playlistItemIds = playlistItemIds;
            return this;
        }

        public RemoveFromPlaylistRequestDto.Builder clearPlaylist(Boolean clearPlaylist) {
            this.instance.clearPlaylist = clearPlaylist;
            return this;
        }

        public RemoveFromPlaylistRequestDto.Builder clearPlayingItem(Boolean clearPlayingItem) {
            this.instance.clearPlayingItem = clearPlayingItem;
            return this;
        }

        /**
         * returns a built RemoveFromPlaylistRequestDto instance.
         *
         * The builder is not reusable.
         */
        public RemoveFromPlaylistRequestDto build() {
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
    public static RemoveFromPlaylistRequestDto.Builder builder() {
        return new RemoveFromPlaylistRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RemoveFromPlaylistRequestDto.Builder toBuilder() {
        return new RemoveFromPlaylistRequestDto.Builder().playlistItemIds(getPlaylistItemIds())
                .clearPlaylist(getClearPlaylist()).clearPlayingItem(getClearPlayingItem());
    }
}
