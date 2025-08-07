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
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class MovePlaylistItemRequestDto.
 */
@JsonPropertyOrder({ MovePlaylistItemRequestDto.JSON_PROPERTY_PLAYLIST_ITEM_ID,
        MovePlaylistItemRequestDto.JSON_PROPERTY_NEW_INDEX })

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
    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
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
    @JsonProperty(JSON_PROPERTY_NEW_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getNewIndex() {
        return newIndex;
    }

    @JsonProperty(JSON_PROPERTY_NEW_INDEX)
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
}
