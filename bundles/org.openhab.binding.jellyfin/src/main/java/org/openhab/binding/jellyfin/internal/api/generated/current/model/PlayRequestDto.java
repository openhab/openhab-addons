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
 * Class PlayRequestDto.
 */
@JsonPropertyOrder({ PlayRequestDto.JSON_PROPERTY_PLAYING_QUEUE, PlayRequestDto.JSON_PROPERTY_PLAYING_ITEM_POSITION,
        PlayRequestDto.JSON_PROPERTY_START_POSITION_TICKS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlayRequestDto {
    public static final String JSON_PROPERTY_PLAYING_QUEUE = "PlayingQueue";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> playingQueue = new ArrayList<>();

    public static final String JSON_PROPERTY_PLAYING_ITEM_POSITION = "PlayingItemPosition";
    @org.eclipse.jdt.annotation.NonNull
    private Integer playingItemPosition;

    public static final String JSON_PROPERTY_START_POSITION_TICKS = "StartPositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long startPositionTicks;

    public PlayRequestDto() {
    }

    public PlayRequestDto playingQueue(@org.eclipse.jdt.annotation.NonNull List<UUID> playingQueue) {
        this.playingQueue = playingQueue;
        return this;
    }

    public PlayRequestDto addPlayingQueueItem(UUID playingQueueItem) {
        if (this.playingQueue == null) {
            this.playingQueue = new ArrayList<>();
        }
        this.playingQueue.add(playingQueueItem);
        return this;
    }

    /**
     * Gets or sets the playing queue.
     * 
     * @return playingQueue
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getPlayingQueue() {
        return playingQueue;
    }

    @JsonProperty(JSON_PROPERTY_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<UUID> playingQueue) {
        this.playingQueue = playingQueue;
    }

    public PlayRequestDto playingItemPosition(@org.eclipse.jdt.annotation.NonNull Integer playingItemPosition) {
        this.playingItemPosition = playingItemPosition;
        return this;
    }

    /**
     * Gets or sets the position of the playing item in the queue.
     * 
     * @return playingItemPosition
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYING_ITEM_POSITION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPlayingItemPosition() {
        return playingItemPosition;
    }

    @JsonProperty(JSON_PROPERTY_PLAYING_ITEM_POSITION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayingItemPosition(@org.eclipse.jdt.annotation.NonNull Integer playingItemPosition) {
        this.playingItemPosition = playingItemPosition;
    }

    public PlayRequestDto startPositionTicks(@org.eclipse.jdt.annotation.NonNull Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
        return this;
    }

    /**
     * Gets or sets the start position ticks.
     * 
     * @return startPositionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getStartPositionTicks() {
        return startPositionTicks;
    }

    @JsonProperty(JSON_PROPERTY_START_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartPositionTicks(@org.eclipse.jdt.annotation.NonNull Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
    }

    /**
     * Return true if this PlayRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayRequestDto playRequestDto = (PlayRequestDto) o;
        return Objects.equals(this.playingQueue, playRequestDto.playingQueue)
                && Objects.equals(this.playingItemPosition, playRequestDto.playingItemPosition)
                && Objects.equals(this.startPositionTicks, playRequestDto.startPositionTicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingQueue, playingItemPosition, startPositionTicks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlayRequestDto {\n");
        sb.append("    playingQueue: ").append(toIndentedString(playingQueue)).append("\n");
        sb.append("    playingItemPosition: ").append(toIndentedString(playingItemPosition)).append("\n");
        sb.append("    startPositionTicks: ").append(toIndentedString(startPositionTicks)).append("\n");
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
