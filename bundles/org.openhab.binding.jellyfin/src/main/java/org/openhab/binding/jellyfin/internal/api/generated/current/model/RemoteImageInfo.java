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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class RemoteImageInfo.
 */
@JsonPropertyOrder({ RemoteImageInfo.JSON_PROPERTY_PROVIDER_NAME, RemoteImageInfo.JSON_PROPERTY_URL,
        RemoteImageInfo.JSON_PROPERTY_THUMBNAIL_URL, RemoteImageInfo.JSON_PROPERTY_HEIGHT,
        RemoteImageInfo.JSON_PROPERTY_WIDTH, RemoteImageInfo.JSON_PROPERTY_COMMUNITY_RATING,
        RemoteImageInfo.JSON_PROPERTY_VOTE_COUNT, RemoteImageInfo.JSON_PROPERTY_LANGUAGE,
        RemoteImageInfo.JSON_PROPERTY_TYPE, RemoteImageInfo.JSON_PROPERTY_RATING_TYPE })

public class RemoteImageInfo {
    public static final String JSON_PROPERTY_PROVIDER_NAME = "ProviderName";
    @org.eclipse.jdt.annotation.NonNull
    private String providerName;

    public static final String JSON_PROPERTY_URL = "Url";
    @org.eclipse.jdt.annotation.NonNull
    private String url;

    public static final String JSON_PROPERTY_THUMBNAIL_URL = "ThumbnailUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String thumbnailUrl;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.NonNull
    private Integer height;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.NonNull
    private Integer width;

    public static final String JSON_PROPERTY_COMMUNITY_RATING = "CommunityRating";
    @org.eclipse.jdt.annotation.NonNull
    private Double communityRating;

    public static final String JSON_PROPERTY_VOTE_COUNT = "VoteCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer voteCount;

    public static final String JSON_PROPERTY_LANGUAGE = "Language";
    @org.eclipse.jdt.annotation.NonNull
    private String language;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private ImageType type;

    public static final String JSON_PROPERTY_RATING_TYPE = "RatingType";
    @org.eclipse.jdt.annotation.NonNull
    private RatingType ratingType;

    public RemoteImageInfo() {
    }

    public RemoteImageInfo providerName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
        return this;
    }

    /**
     * Gets or sets the name of the provider.
     * 
     * @return providerName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
    }

    public RemoteImageInfo url(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets or sets the URL.
     * 
     * @return url
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUrl() {
        return url;
    }

    @JsonProperty(JSON_PROPERTY_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrl(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
    }

    public RemoteImageInfo thumbnailUrl(@org.eclipse.jdt.annotation.NonNull String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    /**
     * Gets or sets a url used for previewing a smaller version.
     * 
     * @return thumbnailUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THUMBNAIL_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @JsonProperty(JSON_PROPERTY_THUMBNAIL_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThumbnailUrl(@org.eclipse.jdt.annotation.NonNull String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public RemoteImageInfo height(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets or sets the height.
     * 
     * @return height
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getHeight() {
        return height;
    }

    @JsonProperty(JSON_PROPERTY_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeight(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
    }

    public RemoteImageInfo width(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets or sets the width.
     * 
     * @return width
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getWidth() {
        return width;
    }

    @JsonProperty(JSON_PROPERTY_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWidth(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
    }

    public RemoteImageInfo communityRating(@org.eclipse.jdt.annotation.NonNull Double communityRating) {
        this.communityRating = communityRating;
        return this;
    }

    /**
     * Gets or sets the community rating.
     * 
     * @return communityRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMMUNITY_RATING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getCommunityRating() {
        return communityRating;
    }

    @JsonProperty(JSON_PROPERTY_COMMUNITY_RATING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommunityRating(@org.eclipse.jdt.annotation.NonNull Double communityRating) {
        this.communityRating = communityRating;
    }

    public RemoteImageInfo voteCount(@org.eclipse.jdt.annotation.NonNull Integer voteCount) {
        this.voteCount = voteCount;
        return this;
    }

    /**
     * Gets or sets the vote count.
     * 
     * @return voteCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VOTE_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getVoteCount() {
        return voteCount;
    }

    @JsonProperty(JSON_PROPERTY_VOTE_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVoteCount(@org.eclipse.jdt.annotation.NonNull Integer voteCount) {
        this.voteCount = voteCount;
    }

    public RemoteImageInfo language(@org.eclipse.jdt.annotation.NonNull String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets or sets the language.
     * 
     * @return language
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLanguage() {
        return language;
    }

    @JsonProperty(JSON_PROPERTY_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLanguage(@org.eclipse.jdt.annotation.NonNull String language) {
        this.language = language;
    }

    public RemoteImageInfo type(@org.eclipse.jdt.annotation.NonNull ImageType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ImageType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull ImageType type) {
        this.type = type;
    }

    public RemoteImageInfo ratingType(@org.eclipse.jdt.annotation.NonNull RatingType ratingType) {
        this.ratingType = ratingType;
        return this;
    }

    /**
     * Gets or sets the type of the rating.
     * 
     * @return ratingType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RATING_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RatingType getRatingType() {
        return ratingType;
    }

    @JsonProperty(JSON_PROPERTY_RATING_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatingType(@org.eclipse.jdt.annotation.NonNull RatingType ratingType) {
        this.ratingType = ratingType;
    }

    /**
     * Return true if this RemoteImageInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteImageInfo remoteImageInfo = (RemoteImageInfo) o;
        return Objects.equals(this.providerName, remoteImageInfo.providerName)
                && Objects.equals(this.url, remoteImageInfo.url)
                && Objects.equals(this.thumbnailUrl, remoteImageInfo.thumbnailUrl)
                && Objects.equals(this.height, remoteImageInfo.height)
                && Objects.equals(this.width, remoteImageInfo.width)
                && Objects.equals(this.communityRating, remoteImageInfo.communityRating)
                && Objects.equals(this.voteCount, remoteImageInfo.voteCount)
                && Objects.equals(this.language, remoteImageInfo.language)
                && Objects.equals(this.type, remoteImageInfo.type)
                && Objects.equals(this.ratingType, remoteImageInfo.ratingType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerName, url, thumbnailUrl, height, width, communityRating, voteCount, language, type,
                ratingType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoteImageInfo {\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    thumbnailUrl: ").append(toIndentedString(thumbnailUrl)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    communityRating: ").append(toIndentedString(communityRating)).append("\n");
        sb.append("    voteCount: ").append(toIndentedString(voteCount)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    ratingType: ").append(toIndentedString(ratingType)).append("\n");
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
