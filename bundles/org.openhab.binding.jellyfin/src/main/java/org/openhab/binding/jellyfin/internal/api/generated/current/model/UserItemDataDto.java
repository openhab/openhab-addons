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
 * Class UserItemDataDto.
 */
@JsonPropertyOrder({ UserItemDataDto.JSON_PROPERTY_RATING, UserItemDataDto.JSON_PROPERTY_PLAYED_PERCENTAGE,
        UserItemDataDto.JSON_PROPERTY_UNPLAYED_ITEM_COUNT, UserItemDataDto.JSON_PROPERTY_PLAYBACK_POSITION_TICKS,
        UserItemDataDto.JSON_PROPERTY_PLAY_COUNT, UserItemDataDto.JSON_PROPERTY_IS_FAVORITE,
        UserItemDataDto.JSON_PROPERTY_LIKES, UserItemDataDto.JSON_PROPERTY_LAST_PLAYED_DATE,
        UserItemDataDto.JSON_PROPERTY_PLAYED, UserItemDataDto.JSON_PROPERTY_KEY,
        UserItemDataDto.JSON_PROPERTY_ITEM_ID })

public class UserItemDataDto {
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
    private UUID itemId;

    public UserItemDataDto() {
    }

    public UserItemDataDto rating(@org.eclipse.jdt.annotation.NonNull Double rating) {
        this.rating = rating;
        return this;
    }

    /**
     * Gets or sets the rating.
     * 
     * @return rating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RATING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getRating() {
        return rating;
    }

    @JsonProperty(JSON_PROPERTY_RATING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRating(@org.eclipse.jdt.annotation.NonNull Double rating) {
        this.rating = rating;
    }

    public UserItemDataDto playedPercentage(@org.eclipse.jdt.annotation.NonNull Double playedPercentage) {
        this.playedPercentage = playedPercentage;
        return this;
    }

    /**
     * Gets or sets the played percentage.
     * 
     * @return playedPercentage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYED_PERCENTAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getPlayedPercentage() {
        return playedPercentage;
    }

    @JsonProperty(JSON_PROPERTY_PLAYED_PERCENTAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayedPercentage(@org.eclipse.jdt.annotation.NonNull Double playedPercentage) {
        this.playedPercentage = playedPercentage;
    }

    public UserItemDataDto unplayedItemCount(@org.eclipse.jdt.annotation.NonNull Integer unplayedItemCount) {
        this.unplayedItemCount = unplayedItemCount;
        return this;
    }

    /**
     * Gets or sets the unplayed item count.
     * 
     * @return unplayedItemCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_UNPLAYED_ITEM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getUnplayedItemCount() {
        return unplayedItemCount;
    }

    @JsonProperty(JSON_PROPERTY_UNPLAYED_ITEM_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnplayedItemCount(@org.eclipse.jdt.annotation.NonNull Integer unplayedItemCount) {
        this.unplayedItemCount = unplayedItemCount;
    }

    public UserItemDataDto playbackPositionTicks(@org.eclipse.jdt.annotation.NonNull Long playbackPositionTicks) {
        this.playbackPositionTicks = playbackPositionTicks;
        return this;
    }

    /**
     * Gets or sets the playback position ticks.
     * 
     * @return playbackPositionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYBACK_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPlaybackPositionTicks() {
        return playbackPositionTicks;
    }

    @JsonProperty(JSON_PROPERTY_PLAYBACK_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackPositionTicks(@org.eclipse.jdt.annotation.NonNull Long playbackPositionTicks) {
        this.playbackPositionTicks = playbackPositionTicks;
    }

    public UserItemDataDto playCount(@org.eclipse.jdt.annotation.NonNull Integer playCount) {
        this.playCount = playCount;
        return this;
    }

    /**
     * Gets or sets the play count.
     * 
     * @return playCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAY_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPlayCount() {
        return playCount;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayCount(@org.eclipse.jdt.annotation.NonNull Integer playCount) {
        this.playCount = playCount;
    }

    public UserItemDataDto isFavorite(@org.eclipse.jdt.annotation.NonNull Boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is favorite.
     * 
     * @return isFavorite
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_FAVORITE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    @JsonProperty(JSON_PROPERTY_IS_FAVORITE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFavorite(@org.eclipse.jdt.annotation.NonNull Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public UserItemDataDto likes(@org.eclipse.jdt.annotation.NonNull Boolean likes) {
        this.likes = likes;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is likes.
     * 
     * @return likes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIKES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getLikes() {
        return likes;
    }

    @JsonProperty(JSON_PROPERTY_LIKES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLikes(@org.eclipse.jdt.annotation.NonNull Boolean likes) {
        this.likes = likes;
    }

    public UserItemDataDto lastPlayedDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
        return this;
    }

    /**
     * Gets or sets the last played date.
     * 
     * @return lastPlayedDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_PLAYED_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getLastPlayedDate() {
        return lastPlayedDate;
    }

    @JsonProperty(JSON_PROPERTY_LAST_PLAYED_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastPlayedDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }

    public UserItemDataDto played(@org.eclipse.jdt.annotation.NonNull Boolean played) {
        this.played = played;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Dto.UserItemDataDto is played.
     * 
     * @return played
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getPlayed() {
        return played;
    }

    @JsonProperty(JSON_PROPERTY_PLAYED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayed(@org.eclipse.jdt.annotation.NonNull Boolean played) {
        this.played = played;
    }

    public UserItemDataDto key(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_KEY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getKey() {
        return key;
    }

    @JsonProperty(JSON_PROPERTY_KEY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
    }

    public UserItemDataDto itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
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
}
