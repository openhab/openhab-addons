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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This is used by the api to get information about a item user data.
 */
@JsonPropertyOrder({ UpdateUserItemDataDto.JSON_PROPERTY_RATING, UpdateUserItemDataDto.JSON_PROPERTY_PLAYED_PERCENTAGE,
        UpdateUserItemDataDto.JSON_PROPERTY_UNPLAYED_ITEM_COUNT,
        UpdateUserItemDataDto.JSON_PROPERTY_PLAYBACK_POSITION_TICKS, UpdateUserItemDataDto.JSON_PROPERTY_PLAY_COUNT,
        UpdateUserItemDataDto.JSON_PROPERTY_IS_FAVORITE, UpdateUserItemDataDto.JSON_PROPERTY_LIKES,
        UpdateUserItemDataDto.JSON_PROPERTY_LAST_PLAYED_DATE, UpdateUserItemDataDto.JSON_PROPERTY_PLAYED,
        UpdateUserItemDataDto.JSON_PROPERTY_KEY, UpdateUserItemDataDto.JSON_PROPERTY_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdateUserItemDataDto {
    public static final String JSON_PROPERTY_RATING = "Rating";
    @org.eclipse.jdt.annotation.NonNull
    private Double rating;

    public static final String JSON_PROPERTY_PLAYED_PERCENTAGE = "PlayedPercentage";
    @org.eclipse.jdt.annotation.NonNull
    private Double playedPercentage;

    public static final String JSON_PROPERTY_UNPLAYED_ITEM_COUNT = "UnplayedItemCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer unplayedItemCount;

    public static final String JSON_PROPERTY_PLAYBACK_POSITION_TICKS = "PlaybackPositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long playbackPositionTicks;

    public static final String JSON_PROPERTY_PLAY_COUNT = "PlayCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer playCount;

    public static final String JSON_PROPERTY_IS_FAVORITE = "IsFavorite";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isFavorite;

    public static final String JSON_PROPERTY_LIKES = "Likes";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean likes;

    public static final String JSON_PROPERTY_LAST_PLAYED_DATE = "LastPlayedDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastPlayedDate;

    public static final String JSON_PROPERTY_PLAYED = "Played";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean played;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.NonNull
    private String key;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String itemId;

    public UpdateUserItemDataDto() {
    }

    public UpdateUserItemDataDto rating(@org.eclipse.jdt.annotation.NonNull Double rating) {
        this.rating = rating;
        return this;
    }

    /**
     * Gets or sets the rating.
     * 
     * @return rating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getRating() {
        return rating;
    }

    @JsonProperty(value = JSON_PROPERTY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRating(@org.eclipse.jdt.annotation.NonNull Double rating) {
        this.rating = rating;
    }

    public UpdateUserItemDataDto playedPercentage(@org.eclipse.jdt.annotation.NonNull Double playedPercentage) {
        this.playedPercentage = playedPercentage;
        return this;
    }

    /**
     * Gets or sets the played percentage.
     * 
     * @return playedPercentage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYED_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getPlayedPercentage() {
        return playedPercentage;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYED_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayedPercentage(@org.eclipse.jdt.annotation.NonNull Double playedPercentage) {
        this.playedPercentage = playedPercentage;
    }

    public UpdateUserItemDataDto unplayedItemCount(@org.eclipse.jdt.annotation.NonNull Integer unplayedItemCount) {
        this.unplayedItemCount = unplayedItemCount;
        return this;
    }

    /**
     * Gets or sets the unplayed item count.
     * 
     * @return unplayedItemCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_UNPLAYED_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getUnplayedItemCount() {
        return unplayedItemCount;
    }

    @JsonProperty(value = JSON_PROPERTY_UNPLAYED_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnplayedItemCount(@org.eclipse.jdt.annotation.NonNull Integer unplayedItemCount) {
        this.unplayedItemCount = unplayedItemCount;
    }

    public UpdateUserItemDataDto playbackPositionTicks(@org.eclipse.jdt.annotation.NonNull Long playbackPositionTicks) {
        this.playbackPositionTicks = playbackPositionTicks;
        return this;
    }

    /**
     * Gets or sets the playback position ticks.
     * 
     * @return playbackPositionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPlaybackPositionTicks() {
        return playbackPositionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackPositionTicks(@org.eclipse.jdt.annotation.NonNull Long playbackPositionTicks) {
        this.playbackPositionTicks = playbackPositionTicks;
    }

    public UpdateUserItemDataDto playCount(@org.eclipse.jdt.annotation.NonNull Integer playCount) {
        this.playCount = playCount;
        return this;
    }

    /**
     * Gets or sets the play count.
     * 
     * @return playCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAY_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPlayCount() {
        return playCount;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayCount(@org.eclipse.jdt.annotation.NonNull Integer playCount) {
        this.playCount = playCount;
    }

    public UpdateUserItemDataDto isFavorite(@org.eclipse.jdt.annotation.NonNull Boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is favorite.
     * 
     * @return isFavorite
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_FAVORITE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsFavorite() {
        return isFavorite;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FAVORITE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFavorite(@org.eclipse.jdt.annotation.NonNull Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public UpdateUserItemDataDto likes(@org.eclipse.jdt.annotation.NonNull Boolean likes) {
        this.likes = likes;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Dto.UpdateUserItemDataDto is likes.
     * 
     * @return likes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LIKES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getLikes() {
        return likes;
    }

    @JsonProperty(value = JSON_PROPERTY_LIKES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLikes(@org.eclipse.jdt.annotation.NonNull Boolean likes) {
        this.likes = likes;
    }

    public UpdateUserItemDataDto lastPlayedDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
        return this;
    }

    /**
     * Gets or sets the last played date.
     * 
     * @return lastPlayedDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LAST_PLAYED_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastPlayedDate() {
        return lastPlayedDate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_PLAYED_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastPlayedDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }

    public UpdateUserItemDataDto played(@org.eclipse.jdt.annotation.NonNull Boolean played) {
        this.played = played;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is played.
     * 
     * @return played
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getPlayed() {
        return played;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayed(@org.eclipse.jdt.annotation.NonNull Boolean played) {
        this.played = played;
    }

    public UpdateUserItemDataDto key(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getKey() {
        return key;
    }

    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
    }

    public UpdateUserItemDataDto itemId(@org.eclipse.jdt.annotation.NonNull String itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull String itemId) {
        this.itemId = itemId;
    }

    /**
     * Return true if this UpdateUserItemDataDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateUserItemDataDto updateUserItemDataDto = (UpdateUserItemDataDto) o;
        return Objects.equals(this.rating, updateUserItemDataDto.rating)
                && Objects.equals(this.playedPercentage, updateUserItemDataDto.playedPercentage)
                && Objects.equals(this.unplayedItemCount, updateUserItemDataDto.unplayedItemCount)
                && Objects.equals(this.playbackPositionTicks, updateUserItemDataDto.playbackPositionTicks)
                && Objects.equals(this.playCount, updateUserItemDataDto.playCount)
                && Objects.equals(this.isFavorite, updateUserItemDataDto.isFavorite)
                && Objects.equals(this.likes, updateUserItemDataDto.likes)
                && Objects.equals(this.lastPlayedDate, updateUserItemDataDto.lastPlayedDate)
                && Objects.equals(this.played, updateUserItemDataDto.played)
                && Objects.equals(this.key, updateUserItemDataDto.key)
                && Objects.equals(this.itemId, updateUserItemDataDto.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating, playedPercentage, unplayedItemCount, playbackPositionTicks, playCount, isFavorite,
                likes, lastPlayedDate, played, key, itemId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateUserItemDataDto {\n");
        sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
        sb.append("    playedPercentage: ").append(toIndentedString(playedPercentage)).append("\n");
        sb.append("    unplayedItemCount: ").append(toIndentedString(unplayedItemCount)).append("\n");
        sb.append("    playbackPositionTicks: ").append(toIndentedString(playbackPositionTicks)).append("\n");
        sb.append("    playCount: ").append(toIndentedString(playCount)).append("\n");
        sb.append("    isFavorite: ").append(toIndentedString(isFavorite)).append("\n");
        sb.append("    likes: ").append(toIndentedString(likes)).append("\n");
        sb.append("    lastPlayedDate: ").append(toIndentedString(lastPlayedDate)).append("\n");
        sb.append("    played: ").append(toIndentedString(played)).append("\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
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

        // add `Rating` to the URL query string
        if (getRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRating()))));
        }

        // add `PlayedPercentage` to the URL query string
        if (getPlayedPercentage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlayedPercentage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayedPercentage()))));
        }

        // add `UnplayedItemCount` to the URL query string
        if (getUnplayedItemCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUnplayedItemCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUnplayedItemCount()))));
        }

        // add `PlaybackPositionTicks` to the URL query string
        if (getPlaybackPositionTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaybackPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaybackPositionTicks()))));
        }

        // add `PlayCount` to the URL query string
        if (getPlayCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlayCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayCount()))));
        }

        // add `IsFavorite` to the URL query string
        if (getIsFavorite() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsFavorite%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsFavorite()))));
        }

        // add `Likes` to the URL query string
        if (getLikes() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLikes%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLikes()))));
        }

        // add `LastPlayedDate` to the URL query string
        if (getLastPlayedDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastPlayedDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastPlayedDate()))));
        }

        // add `Played` to the URL query string
        if (getPlayed() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlayed%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayed()))));
        }

        // add `Key` to the URL query string
        if (getKey() != null) {
            joiner.add(String.format(Locale.ROOT, "%sKey%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKey()))));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UpdateUserItemDataDto instance;

        public Builder() {
            this(new UpdateUserItemDataDto());
        }

        protected Builder(UpdateUserItemDataDto instance) {
            this.instance = instance;
        }

        public UpdateUserItemDataDto.Builder rating(Double rating) {
            this.instance.rating = rating;
            return this;
        }

        public UpdateUserItemDataDto.Builder playedPercentage(Double playedPercentage) {
            this.instance.playedPercentage = playedPercentage;
            return this;
        }

        public UpdateUserItemDataDto.Builder unplayedItemCount(Integer unplayedItemCount) {
            this.instance.unplayedItemCount = unplayedItemCount;
            return this;
        }

        public UpdateUserItemDataDto.Builder playbackPositionTicks(Long playbackPositionTicks) {
            this.instance.playbackPositionTicks = playbackPositionTicks;
            return this;
        }

        public UpdateUserItemDataDto.Builder playCount(Integer playCount) {
            this.instance.playCount = playCount;
            return this;
        }

        public UpdateUserItemDataDto.Builder isFavorite(Boolean isFavorite) {
            this.instance.isFavorite = isFavorite;
            return this;
        }

        public UpdateUserItemDataDto.Builder likes(Boolean likes) {
            this.instance.likes = likes;
            return this;
        }

        public UpdateUserItemDataDto.Builder lastPlayedDate(OffsetDateTime lastPlayedDate) {
            this.instance.lastPlayedDate = lastPlayedDate;
            return this;
        }

        public UpdateUserItemDataDto.Builder played(Boolean played) {
            this.instance.played = played;
            return this;
        }

        public UpdateUserItemDataDto.Builder key(String key) {
            this.instance.key = key;
            return this;
        }

        public UpdateUserItemDataDto.Builder itemId(String itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        /**
         * returns a built UpdateUserItemDataDto instance.
         *
         * The builder is not reusable.
         */
        public UpdateUserItemDataDto build() {
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
    public static UpdateUserItemDataDto.Builder builder() {
        return new UpdateUserItemDataDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UpdateUserItemDataDto.Builder toBuilder() {
        return new UpdateUserItemDataDto.Builder().rating(getRating()).playedPercentage(getPlayedPercentage())
                .unplayedItemCount(getUnplayedItemCount()).playbackPositionTicks(getPlaybackPositionTicks())
                .playCount(getPlayCount()).isFavorite(getIsFavorite()).likes(getLikes())
                .lastPlayedDate(getLastPlayedDate()).played(getPlayed()).key(getKey()).itemId(getItemId());
    }
}
