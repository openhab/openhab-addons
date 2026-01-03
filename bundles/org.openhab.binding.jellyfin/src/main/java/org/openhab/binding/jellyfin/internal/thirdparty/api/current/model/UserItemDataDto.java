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
 * Class UserItemDataDto.
 */
@JsonPropertyOrder({ UserItemDataDto.JSON_PROPERTY_RATING, UserItemDataDto.JSON_PROPERTY_PLAYED_PERCENTAGE,
        UserItemDataDto.JSON_PROPERTY_UNPLAYED_ITEM_COUNT, UserItemDataDto.JSON_PROPERTY_PLAYBACK_POSITION_TICKS,
        UserItemDataDto.JSON_PROPERTY_PLAY_COUNT, UserItemDataDto.JSON_PROPERTY_IS_FAVORITE,
        UserItemDataDto.JSON_PROPERTY_LIKES, UserItemDataDto.JSON_PROPERTY_LAST_PLAYED_DATE,
        UserItemDataDto.JSON_PROPERTY_PLAYED, UserItemDataDto.JSON_PROPERTY_KEY,
        UserItemDataDto.JSON_PROPERTY_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UserItemDataDto {
    public static final String JSON_PROPERTY_RATING = "Rating";
    @org.eclipse.jdt.annotation.Nullable
    private Double rating;

    public static final String JSON_PROPERTY_PLAYED_PERCENTAGE = "PlayedPercentage";
    @org.eclipse.jdt.annotation.Nullable
    private Double playedPercentage;

    public static final String JSON_PROPERTY_UNPLAYED_ITEM_COUNT = "UnplayedItemCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer unplayedItemCount;

    public static final String JSON_PROPERTY_PLAYBACK_POSITION_TICKS = "PlaybackPositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long playbackPositionTicks;

    public static final String JSON_PROPERTY_PLAY_COUNT = "PlayCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer playCount;

    public static final String JSON_PROPERTY_IS_FAVORITE = "IsFavorite";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isFavorite;

    public static final String JSON_PROPERTY_LIKES = "Likes";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean likes;

    public static final String JSON_PROPERTY_LAST_PLAYED_DATE = "LastPlayedDate";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime lastPlayedDate;

    public static final String JSON_PROPERTY_PLAYED = "Played";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean played;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.Nullable
    private String key;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID itemId;

    public UserItemDataDto() {
    }

    public UserItemDataDto rating(@org.eclipse.jdt.annotation.Nullable Double rating) {
        this.rating = rating;
        return this;
    }

    /**
     * Gets or sets the rating.
     * 
     * @return rating
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getRating() {
        return rating;
    }

    @JsonProperty(value = JSON_PROPERTY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRating(@org.eclipse.jdt.annotation.Nullable Double rating) {
        this.rating = rating;
    }

    public UserItemDataDto playedPercentage(@org.eclipse.jdt.annotation.Nullable Double playedPercentage) {
        this.playedPercentage = playedPercentage;
        return this;
    }

    /**
     * Gets or sets the played percentage.
     * 
     * @return playedPercentage
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYED_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getPlayedPercentage() {
        return playedPercentage;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYED_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayedPercentage(@org.eclipse.jdt.annotation.Nullable Double playedPercentage) {
        this.playedPercentage = playedPercentage;
    }

    public UserItemDataDto unplayedItemCount(@org.eclipse.jdt.annotation.Nullable Integer unplayedItemCount) {
        this.unplayedItemCount = unplayedItemCount;
        return this;
    }

    /**
     * Gets or sets the unplayed item count.
     * 
     * @return unplayedItemCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_UNPLAYED_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getUnplayedItemCount() {
        return unplayedItemCount;
    }

    @JsonProperty(value = JSON_PROPERTY_UNPLAYED_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnplayedItemCount(@org.eclipse.jdt.annotation.Nullable Integer unplayedItemCount) {
        this.unplayedItemCount = unplayedItemCount;
    }

    public UserItemDataDto playbackPositionTicks(@org.eclipse.jdt.annotation.Nullable Long playbackPositionTicks) {
        this.playbackPositionTicks = playbackPositionTicks;
        return this;
    }

    /**
     * Gets or sets the playback position ticks.
     * 
     * @return playbackPositionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPlaybackPositionTicks() {
        return playbackPositionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackPositionTicks(@org.eclipse.jdt.annotation.Nullable Long playbackPositionTicks) {
        this.playbackPositionTicks = playbackPositionTicks;
    }

    public UserItemDataDto playCount(@org.eclipse.jdt.annotation.Nullable Integer playCount) {
        this.playCount = playCount;
        return this;
    }

    /**
     * Gets or sets the play count.
     * 
     * @return playCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAY_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPlayCount() {
        return playCount;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayCount(@org.eclipse.jdt.annotation.Nullable Integer playCount) {
        this.playCount = playCount;
    }

    public UserItemDataDto isFavorite(@org.eclipse.jdt.annotation.Nullable Boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is favorite.
     * 
     * @return isFavorite
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_FAVORITE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsFavorite() {
        return isFavorite;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FAVORITE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFavorite(@org.eclipse.jdt.annotation.Nullable Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public UserItemDataDto likes(@org.eclipse.jdt.annotation.Nullable Boolean likes) {
        this.likes = likes;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is likes.
     * 
     * @return likes
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LIKES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getLikes() {
        return likes;
    }

    @JsonProperty(value = JSON_PROPERTY_LIKES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLikes(@org.eclipse.jdt.annotation.Nullable Boolean likes) {
        this.likes = likes;
    }

    public UserItemDataDto lastPlayedDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
        return this;
    }

    /**
     * Gets or sets the last played date.
     * 
     * @return lastPlayedDate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LAST_PLAYED_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastPlayedDate() {
        return lastPlayedDate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_PLAYED_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastPlayedDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }

    public UserItemDataDto played(@org.eclipse.jdt.annotation.Nullable Boolean played) {
        this.played = played;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is played.
     * 
     * @return played
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getPlayed() {
        return played;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayed(@org.eclipse.jdt.annotation.Nullable Boolean played) {
        this.played = played;
    }

    public UserItemDataDto key(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getKey() {
        return key;
    }

    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
    }

    public UserItemDataDto itemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
    }

    /**
     * Return true if this UserItemDataDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserItemDataDto userItemDataDto = (UserItemDataDto) o;
        return Objects.equals(this.rating, userItemDataDto.rating)
                && Objects.equals(this.playedPercentage, userItemDataDto.playedPercentage)
                && Objects.equals(this.unplayedItemCount, userItemDataDto.unplayedItemCount)
                && Objects.equals(this.playbackPositionTicks, userItemDataDto.playbackPositionTicks)
                && Objects.equals(this.playCount, userItemDataDto.playCount)
                && Objects.equals(this.isFavorite, userItemDataDto.isFavorite)
                && Objects.equals(this.likes, userItemDataDto.likes)
                && Objects.equals(this.lastPlayedDate, userItemDataDto.lastPlayedDate)
                && Objects.equals(this.played, userItemDataDto.played) && Objects.equals(this.key, userItemDataDto.key)
                && Objects.equals(this.itemId, userItemDataDto.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating, playedPercentage, unplayedItemCount, playbackPositionTicks, playCount, isFavorite,
                likes, lastPlayedDate, played, key, itemId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserItemDataDto {\n");
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
            joiner.add(String.format(java.util.Locale.ROOT, "%sRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRating()))));
        }

        // add `PlayedPercentage` to the URL query string
        if (getPlayedPercentage() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayedPercentage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayedPercentage()))));
        }

        // add `UnplayedItemCount` to the URL query string
        if (getUnplayedItemCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sUnplayedItemCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUnplayedItemCount()))));
        }

        // add `PlaybackPositionTicks` to the URL query string
        if (getPlaybackPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaybackPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaybackPositionTicks()))));
        }

        // add `PlayCount` to the URL query string
        if (getPlayCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayCount()))));
        }

        // add `IsFavorite` to the URL query string
        if (getIsFavorite() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsFavorite%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsFavorite()))));
        }

        // add `Likes` to the URL query string
        if (getLikes() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLikes%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLikes()))));
        }

        // add `LastPlayedDate` to the URL query string
        if (getLastPlayedDate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLastPlayedDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastPlayedDate()))));
        }

        // add `Played` to the URL query string
        if (getPlayed() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayed%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayed()))));
        }

        // add `Key` to the URL query string
        if (getKey() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sKey%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKey()))));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UserItemDataDto instance;

        public Builder() {
            this(new UserItemDataDto());
        }

        protected Builder(UserItemDataDto instance) {
            this.instance = instance;
        }

        public UserItemDataDto.Builder rating(Double rating) {
            this.instance.rating = rating;
            return this;
        }

        public UserItemDataDto.Builder playedPercentage(Double playedPercentage) {
            this.instance.playedPercentage = playedPercentage;
            return this;
        }

        public UserItemDataDto.Builder unplayedItemCount(Integer unplayedItemCount) {
            this.instance.unplayedItemCount = unplayedItemCount;
            return this;
        }

        public UserItemDataDto.Builder playbackPositionTicks(Long playbackPositionTicks) {
            this.instance.playbackPositionTicks = playbackPositionTicks;
            return this;
        }

        public UserItemDataDto.Builder playCount(Integer playCount) {
            this.instance.playCount = playCount;
            return this;
        }

        public UserItemDataDto.Builder isFavorite(Boolean isFavorite) {
            this.instance.isFavorite = isFavorite;
            return this;
        }

        public UserItemDataDto.Builder likes(Boolean likes) {
            this.instance.likes = likes;
            return this;
        }

        public UserItemDataDto.Builder lastPlayedDate(OffsetDateTime lastPlayedDate) {
            this.instance.lastPlayedDate = lastPlayedDate;
            return this;
        }

        public UserItemDataDto.Builder played(Boolean played) {
            this.instance.played = played;
            return this;
        }

        public UserItemDataDto.Builder key(String key) {
            this.instance.key = key;
            return this;
        }

        public UserItemDataDto.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        /**
         * returns a built UserItemDataDto instance.
         *
         * The builder is not reusable.
         */
        public UserItemDataDto build() {
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
    public static UserItemDataDto.Builder builder() {
        return new UserItemDataDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UserItemDataDto.Builder toBuilder() {
        return new UserItemDataDto.Builder().rating(getRating()).playedPercentage(getPlayedPercentage())
                .unplayedItemCount(getUnplayedItemCount()).playbackPositionTicks(getPlaybackPositionTicks())
                .playCount(getPlayCount()).isFavorite(getIsFavorite()).likes(getLikes())
                .lastPlayedDate(getLastPlayedDate()).played(getPlayed()).key(getKey()).itemId(getItemId());
    }
}
