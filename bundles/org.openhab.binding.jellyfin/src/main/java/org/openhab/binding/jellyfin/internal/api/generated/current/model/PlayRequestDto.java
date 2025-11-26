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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

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
    @JsonProperty(value = JSON_PROPERTY_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getPlayingQueue() {
        return playingQueue;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYING_QUEUE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PLAYING_ITEM_POSITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPlayingItemPosition() {
        return playingItemPosition;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYING_ITEM_POSITION, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_START_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStartPositionTicks() {
        return startPositionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_START_POSITION_TICKS, required = false)
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

        // add `PlayingQueue` to the URL query string
        if (getPlayingQueue() != null) {
            for (int i = 0; i < getPlayingQueue().size(); i++) {
                if (getPlayingQueue().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sPlayingQueue%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getPlayingQueue().get(i)))));
                }
            }
        }

        // add `PlayingItemPosition` to the URL query string
        if (getPlayingItemPosition() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlayingItemPosition%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayingItemPosition()))));
        }

        // add `StartPositionTicks` to the URL query string
        if (getStartPositionTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartPositionTicks()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlayRequestDto instance;

        public Builder() {
            this(new PlayRequestDto());
        }

        protected Builder(PlayRequestDto instance) {
            this.instance = instance;
        }

        public PlayRequestDto.Builder playingQueue(List<UUID> playingQueue) {
            this.instance.playingQueue = playingQueue;
            return this;
        }

        public PlayRequestDto.Builder playingItemPosition(Integer playingItemPosition) {
            this.instance.playingItemPosition = playingItemPosition;
            return this;
        }

        public PlayRequestDto.Builder startPositionTicks(Long startPositionTicks) {
            this.instance.startPositionTicks = startPositionTicks;
            return this;
        }

        /**
         * returns a built PlayRequestDto instance.
         *
         * The builder is not reusable.
         */
        public PlayRequestDto build() {
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
    public static PlayRequestDto.Builder builder() {
        return new PlayRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlayRequestDto.Builder toBuilder() {
        return new PlayRequestDto.Builder().playingQueue(getPlayingQueue())
                .playingItemPosition(getPlayingItemPosition()).startPositionTicks(getStartPositionTicks());
    }
}
