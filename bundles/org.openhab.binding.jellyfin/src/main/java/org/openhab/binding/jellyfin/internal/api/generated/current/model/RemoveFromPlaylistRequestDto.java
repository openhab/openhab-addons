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
import java.util.UUID;

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
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> playlistItemIds = new ArrayList<>();

    public static final String JSON_PROPERTY_CLEAR_PLAYLIST = "ClearPlaylist";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean clearPlaylist;

    public static final String JSON_PROPERTY_CLEAR_PLAYING_ITEM = "ClearPlayingItem";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean clearPlayingItem;

    public RemoveFromPlaylistRequestDto() {
    }

    public RemoveFromPlaylistRequestDto playlistItemIds(
            @org.eclipse.jdt.annotation.NonNull List<UUID> playlistItemIds) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getPlaylistItemIds() {
        return playlistItemIds;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemIds(@org.eclipse.jdt.annotation.NonNull List<UUID> playlistItemIds) {
        this.playlistItemIds = playlistItemIds;
    }

    public RemoveFromPlaylistRequestDto clearPlaylist(@org.eclipse.jdt.annotation.NonNull Boolean clearPlaylist) {
        this.clearPlaylist = clearPlaylist;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the entire playlist should be cleared.
     * 
     * @return clearPlaylist
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CLEAR_PLAYLIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getClearPlaylist() {
        return clearPlaylist;
    }

    @JsonProperty(JSON_PROPERTY_CLEAR_PLAYLIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClearPlaylist(@org.eclipse.jdt.annotation.NonNull Boolean clearPlaylist) {
        this.clearPlaylist = clearPlaylist;
    }

    public RemoveFromPlaylistRequestDto clearPlayingItem(@org.eclipse.jdt.annotation.NonNull Boolean clearPlayingItem) {
        this.clearPlayingItem = clearPlayingItem;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the playing item should be removed as well. Used only when clearing the
     * playlist.
     * 
     * @return clearPlayingItem
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CLEAR_PLAYING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getClearPlayingItem() {
        return clearPlayingItem;
    }

    @JsonProperty(JSON_PROPERTY_CLEAR_PLAYING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClearPlayingItem(@org.eclipse.jdt.annotation.NonNull Boolean clearPlayingItem) {
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
}
