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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class ReadyRequest.
 */
@JsonPropertyOrder({ ReadyRequestDto.JSON_PROPERTY_WHEN, ReadyRequestDto.JSON_PROPERTY_POSITION_TICKS,
        ReadyRequestDto.JSON_PROPERTY_IS_PLAYING, ReadyRequestDto.JSON_PROPERTY_PLAYLIST_ITEM_ID })

public class ReadyRequestDto {
    public static final String JSON_PROPERTY_WHEN = "When";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime when;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long positionTicks;

    public static final String JSON_PROPERTY_IS_PLAYING = "IsPlaying";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPlaying;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID playlistItemId;

    public ReadyRequestDto() {
    }

    public ReadyRequestDto when(@org.eclipse.jdt.annotation.NonNull OffsetDateTime when) {
        this.when = when;
        return this;
    }

    /**
     * Gets or sets when the request has been made by the client.
     * 
     * @return when
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_WHEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getWhen() {
        return when;
    }

    @JsonProperty(JSON_PROPERTY_WHEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWhen(@org.eclipse.jdt.annotation.NonNull OffsetDateTime when) {
        this.when = when;
    }

    public ReadyRequestDto positionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public ReadyRequestDto isPlaying(@org.eclipse.jdt.annotation.NonNull Boolean isPlaying) {
        this.isPlaying = isPlaying;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the client playback is unpaused.
     * 
     * @return isPlaying
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_PLAYING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsPlaying() {
        return isPlaying;
    }

    @JsonProperty(JSON_PROPERTY_IS_PLAYING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPlaying(@org.eclipse.jdt.annotation.NonNull Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public ReadyRequestDto playlistItemId(@org.eclipse.jdt.annotation.NonNull UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets or sets the playlist item identifier of the playing item.
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

    /**
     * Return true if this ReadyRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadyRequestDto readyRequestDto = (ReadyRequestDto) o;
        return Objects.equals(this.when, readyRequestDto.when)
                && Objects.equals(this.positionTicks, readyRequestDto.positionTicks)
                && Objects.equals(this.isPlaying, readyRequestDto.isPlaying)
                && Objects.equals(this.playlistItemId, readyRequestDto.playlistItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(when, positionTicks, isPlaying, playlistItemId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ReadyRequestDto {\n");
        sb.append("    when: ").append(toIndentedString(when)).append("\n");
        sb.append("    positionTicks: ").append(toIndentedString(positionTicks)).append("\n");
        sb.append("    isPlaying: ").append(toIndentedString(isPlaying)).append("\n");
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
}
