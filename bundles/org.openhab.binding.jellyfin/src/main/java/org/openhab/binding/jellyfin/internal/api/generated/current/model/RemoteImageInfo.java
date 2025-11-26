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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

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
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_NAME, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUrl() {
        return url;
    }

    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_THUMBNAIL_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_THUMBNAIL_URL, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getHeight() {
        return height;
    }

    @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getWidth() {
        return width;
    }

    @JsonProperty(value = JSON_PROPERTY_WIDTH, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_COMMUNITY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getCommunityRating() {
        return communityRating;
    }

    @JsonProperty(value = JSON_PROPERTY_COMMUNITY_RATING, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_VOTE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getVoteCount() {
        return voteCount;
    }

    @JsonProperty(value = JSON_PROPERTY_VOTE_COUNT, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLanguage() {
        return language;
    }

    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ImageType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_RATING_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public RatingType getRatingType() {
        return ratingType;
    }

    @JsonProperty(value = JSON_PROPERTY_RATING_TYPE, required = false)
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

        // add `ProviderName` to the URL query string
        if (getProviderName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProviderName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderName()))));
        }

        // add `Url` to the URL query string
        if (getUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
        }

        // add `ThumbnailUrl` to the URL query string
        if (getThumbnailUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sThumbnailUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThumbnailUrl()))));
        }

        // add `Height` to the URL query string
        if (getHeight() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHeight()))));
        }

        // add `Width` to the URL query string
        if (getWidth() != null) {
            joiner.add(String.format(Locale.ROOT, "%sWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWidth()))));
        }

        // add `CommunityRating` to the URL query string
        if (getCommunityRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCommunityRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCommunityRating()))));
        }

        // add `VoteCount` to the URL query string
        if (getVoteCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVoteCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVoteCount()))));
        }

        // add `Language` to the URL query string
        if (getLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLanguage()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `RatingType` to the URL query string
        if (getRatingType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRatingType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRatingType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private RemoteImageInfo instance;

        public Builder() {
            this(new RemoteImageInfo());
        }

        protected Builder(RemoteImageInfo instance) {
            this.instance = instance;
        }

        public RemoteImageInfo.Builder providerName(String providerName) {
            this.instance.providerName = providerName;
            return this;
        }

        public RemoteImageInfo.Builder url(String url) {
            this.instance.url = url;
            return this;
        }

        public RemoteImageInfo.Builder thumbnailUrl(String thumbnailUrl) {
            this.instance.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public RemoteImageInfo.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public RemoteImageInfo.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public RemoteImageInfo.Builder communityRating(Double communityRating) {
            this.instance.communityRating = communityRating;
            return this;
        }

        public RemoteImageInfo.Builder voteCount(Integer voteCount) {
            this.instance.voteCount = voteCount;
            return this;
        }

        public RemoteImageInfo.Builder language(String language) {
            this.instance.language = language;
            return this;
        }

        public RemoteImageInfo.Builder type(ImageType type) {
            this.instance.type = type;
            return this;
        }

        public RemoteImageInfo.Builder ratingType(RatingType ratingType) {
            this.instance.ratingType = ratingType;
            return this;
        }

        /**
         * returns a built RemoteImageInfo instance.
         *
         * The builder is not reusable.
         */
        public RemoteImageInfo build() {
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
    public static RemoteImageInfo.Builder builder() {
        return new RemoteImageInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RemoteImageInfo.Builder toBuilder() {
        return new RemoteImageInfo.Builder().providerName(getProviderName()).url(getUrl())
                .thumbnailUrl(getThumbnailUrl()).height(getHeight()).width(getWidth())
                .communityRating(getCommunityRating()).voteCount(getVoteCount()).language(getLanguage()).type(getType())
                .ratingType(getRatingType());
    }
}
