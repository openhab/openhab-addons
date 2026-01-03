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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class ReadyRequest.
 */
@JsonPropertyOrder({ ReadyRequestDto.JSON_PROPERTY_WHEN, ReadyRequestDto.JSON_PROPERTY_POSITION_TICKS,
        ReadyRequestDto.JSON_PROPERTY_IS_PLAYING, ReadyRequestDto.JSON_PROPERTY_PLAYLIST_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ReadyRequestDto {
    public static final String JSON_PROPERTY_WHEN = "When";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime when;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long positionTicks;

    public static final String JSON_PROPERTY_IS_PLAYING = "IsPlaying";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isPlaying;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID playlistItemId;

    public ReadyRequestDto() {
    }

    public ReadyRequestDto when(@org.eclipse.jdt.annotation.Nullable OffsetDateTime when) {
        this.when = when;
        return this;
    }

    /**
     * Gets or sets when the request has been made by the client.
     * 
     * @return when
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_WHEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getWhen() {
        return when;
    }

    @JsonProperty(value = JSON_PROPERTY_WHEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWhen(@org.eclipse.jdt.annotation.Nullable OffsetDateTime when) {
        this.when = when;
    }

    public ReadyRequestDto positionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public ReadyRequestDto isPlaying(@org.eclipse.jdt.annotation.Nullable Boolean isPlaying) {
        this.isPlaying = isPlaying;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the client playback is unpaused.
     * 
     * @return isPlaying
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_PLAYING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPlaying() {
        return isPlaying;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PLAYING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPlaying(@org.eclipse.jdt.annotation.Nullable Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public ReadyRequestDto playlistItemId(@org.eclipse.jdt.annotation.Nullable UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets or sets the playlist item identifier of the playing item.
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.Nullable UUID playlistItemId) {
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

        // add `When` to the URL query string
        if (getWhen() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sWhen%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWhen()))));
        }

        // add `PositionTicks` to the URL query string
        if (getPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPositionTicks()))));
        }

        // add `IsPlaying` to the URL query string
        if (getIsPlaying() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsPlaying%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPlaying()))));
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ReadyRequestDto instance;

        public Builder() {
            this(new ReadyRequestDto());
        }

        protected Builder(ReadyRequestDto instance) {
            this.instance = instance;
        }

        public ReadyRequestDto.Builder when(OffsetDateTime when) {
            this.instance.when = when;
            return this;
        }

        public ReadyRequestDto.Builder positionTicks(Long positionTicks) {
            this.instance.positionTicks = positionTicks;
            return this;
        }

        public ReadyRequestDto.Builder isPlaying(Boolean isPlaying) {
            this.instance.isPlaying = isPlaying;
            return this;
        }

        public ReadyRequestDto.Builder playlistItemId(UUID playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        /**
         * returns a built ReadyRequestDto instance.
         *
         * The builder is not reusable.
         */
        public ReadyRequestDto build() {
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
    public static ReadyRequestDto.Builder builder() {
        return new ReadyRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ReadyRequestDto.Builder toBuilder() {
        return new ReadyRequestDto.Builder().when(getWhen()).positionTicks(getPositionTicks()).isPlaying(getIsPlaying())
                .playlistItemId(getPlaylistItemId());
    }
}
