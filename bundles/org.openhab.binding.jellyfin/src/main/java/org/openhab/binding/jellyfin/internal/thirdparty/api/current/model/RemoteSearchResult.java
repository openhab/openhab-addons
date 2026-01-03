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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * RemoteSearchResult
 */
@JsonPropertyOrder({ RemoteSearchResult.JSON_PROPERTY_NAME, RemoteSearchResult.JSON_PROPERTY_PROVIDER_IDS,
        RemoteSearchResult.JSON_PROPERTY_PRODUCTION_YEAR, RemoteSearchResult.JSON_PROPERTY_INDEX_NUMBER,
        RemoteSearchResult.JSON_PROPERTY_INDEX_NUMBER_END, RemoteSearchResult.JSON_PROPERTY_PARENT_INDEX_NUMBER,
        RemoteSearchResult.JSON_PROPERTY_PREMIERE_DATE, RemoteSearchResult.JSON_PROPERTY_IMAGE_URL,
        RemoteSearchResult.JSON_PROPERTY_SEARCH_PROVIDER_NAME, RemoteSearchResult.JSON_PROPERTY_OVERVIEW,
        RemoteSearchResult.JSON_PROPERTY_ALBUM_ARTIST, RemoteSearchResult.JSON_PROPERTY_ARTISTS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RemoteSearchResult {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_PROVIDER_IDS = "ProviderIds";
    @org.eclipse.jdt.annotation.Nullable
    private Map<String, String> providerIds;

    public static final String JSON_PROPERTY_PRODUCTION_YEAR = "ProductionYear";
    @org.eclipse.jdt.annotation.Nullable
    private Integer productionYear;

    public static final String JSON_PROPERTY_INDEX_NUMBER = "IndexNumber";
    @org.eclipse.jdt.annotation.Nullable
    private Integer indexNumber;

    public static final String JSON_PROPERTY_INDEX_NUMBER_END = "IndexNumberEnd";
    @org.eclipse.jdt.annotation.Nullable
    private Integer indexNumberEnd;

    public static final String JSON_PROPERTY_PARENT_INDEX_NUMBER = "ParentIndexNumber";
    @org.eclipse.jdt.annotation.Nullable
    private Integer parentIndexNumber;

    public static final String JSON_PROPERTY_PREMIERE_DATE = "PremiereDate";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime premiereDate;

    public static final String JSON_PROPERTY_IMAGE_URL = "ImageUrl";
    @org.eclipse.jdt.annotation.Nullable
    private String imageUrl;

    public static final String JSON_PROPERTY_SEARCH_PROVIDER_NAME = "SearchProviderName";
    @org.eclipse.jdt.annotation.Nullable
    private String searchProviderName;

    public static final String JSON_PROPERTY_OVERVIEW = "Overview";
    @org.eclipse.jdt.annotation.Nullable
    private String overview;

    public static final String JSON_PROPERTY_ALBUM_ARTIST = "AlbumArtist";
    @org.eclipse.jdt.annotation.Nullable
    private RemoteSearchResult albumArtist;

    public static final String JSON_PROPERTY_ARTISTS = "Artists";
    @org.eclipse.jdt.annotation.Nullable
    private List<RemoteSearchResult> artists;

    public RemoteSearchResult() {
    }

    public RemoteSearchResult name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public RemoteSearchResult providerIds(@org.eclipse.jdt.annotation.Nullable Map<String, String> providerIds) {
        this.providerIds = providerIds;
        return this;
    }

    public RemoteSearchResult putProviderIdsItem(String key, String providerIdsItem) {
        if (this.providerIds == null) {
            this.providerIds = new HashMap<>();
        }
        this.providerIds.put(key, providerIdsItem);
        return this;
    }

    /**
     * Gets or sets the provider ids.
     * 
     * @return providerIds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getProviderIds() {
        return providerIds;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderIds(@org.eclipse.jdt.annotation.Nullable Map<String, String> providerIds) {
        this.providerIds = providerIds;
    }

    public RemoteSearchResult productionYear(@org.eclipse.jdt.annotation.Nullable Integer productionYear) {
        this.productionYear = productionYear;
        return this;
    }

    /**
     * Gets or sets the year.
     * 
     * @return productionYear
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getProductionYear() {
        return productionYear;
    }

    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProductionYear(@org.eclipse.jdt.annotation.Nullable Integer productionYear) {
        this.productionYear = productionYear;
    }

    public RemoteSearchResult indexNumber(@org.eclipse.jdt.annotation.Nullable Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Get indexNumber
     * 
     * @return indexNumber
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndexNumber() {
        return indexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumber(@org.eclipse.jdt.annotation.Nullable Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public RemoteSearchResult indexNumberEnd(@org.eclipse.jdt.annotation.Nullable Integer indexNumberEnd) {
        this.indexNumberEnd = indexNumberEnd;
        return this;
    }

    /**
     * Get indexNumberEnd
     * 
     * @return indexNumberEnd
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER_END, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndexNumberEnd() {
        return indexNumberEnd;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER_END, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumberEnd(@org.eclipse.jdt.annotation.Nullable Integer indexNumberEnd) {
        this.indexNumberEnd = indexNumberEnd;
    }

    public RemoteSearchResult parentIndexNumber(@org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Get parentIndexNumber
     * 
     * @return parentIndexNumber
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getParentIndexNumber() {
        return parentIndexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentIndexNumber(@org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
    }

    public RemoteSearchResult premiereDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
        return this;
    }

    /**
     * Get premiereDate
     * 
     * @return premiereDate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getPremiereDate() {
        return premiereDate;
    }

    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPremiereDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
    }

    public RemoteSearchResult imageUrl(@org.eclipse.jdt.annotation.Nullable String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    /**
     * Get imageUrl
     * 
     * @return imageUrl
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getImageUrl() {
        return imageUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageUrl(@org.eclipse.jdt.annotation.Nullable String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public RemoteSearchResult searchProviderName(@org.eclipse.jdt.annotation.Nullable String searchProviderName) {
        this.searchProviderName = searchProviderName;
        return this;
    }

    /**
     * Get searchProviderName
     * 
     * @return searchProviderName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEARCH_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSearchProviderName() {
        return searchProviderName;
    }

    @JsonProperty(value = JSON_PROPERTY_SEARCH_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSearchProviderName(@org.eclipse.jdt.annotation.Nullable String searchProviderName) {
        this.searchProviderName = searchProviderName;
    }

    public RemoteSearchResult overview(@org.eclipse.jdt.annotation.Nullable String overview) {
        this.overview = overview;
        return this;
    }

    /**
     * Get overview
     * 
     * @return overview
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOverview() {
        return overview;
    }

    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOverview(@org.eclipse.jdt.annotation.Nullable String overview) {
        this.overview = overview;
    }

    public RemoteSearchResult albumArtist(@org.eclipse.jdt.annotation.Nullable RemoteSearchResult albumArtist) {
        this.albumArtist = albumArtist;
        return this;
    }

    /**
     * Get albumArtist
     * 
     * @return albumArtist
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public RemoteSearchResult getAlbumArtist() {
        return albumArtist;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtist(@org.eclipse.jdt.annotation.Nullable RemoteSearchResult albumArtist) {
        this.albumArtist = albumArtist;
    }

    public RemoteSearchResult artists(@org.eclipse.jdt.annotation.Nullable List<RemoteSearchResult> artists) {
        this.artists = artists;
        return this;
    }

    public RemoteSearchResult addArtistsItem(RemoteSearchResult artistsItem) {
        if (this.artists == null) {
            this.artists = new ArrayList<>();
        }
        this.artists.add(artistsItem);
        return this;
    }

    /**
     * Get artists
     * 
     * @return artists
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<RemoteSearchResult> getArtists() {
        return artists;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtists(@org.eclipse.jdt.annotation.Nullable List<RemoteSearchResult> artists) {
        this.artists = artists;
    }

    /**
     * Return true if this RemoteSearchResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteSearchResult remoteSearchResult = (RemoteSearchResult) o;
        return Objects.equals(this.name, remoteSearchResult.name)
                && Objects.equals(this.providerIds, remoteSearchResult.providerIds)
                && Objects.equals(this.productionYear, remoteSearchResult.productionYear)
                && Objects.equals(this.indexNumber, remoteSearchResult.indexNumber)
                && Objects.equals(this.indexNumberEnd, remoteSearchResult.indexNumberEnd)
                && Objects.equals(this.parentIndexNumber, remoteSearchResult.parentIndexNumber)
                && Objects.equals(this.premiereDate, remoteSearchResult.premiereDate)
                && Objects.equals(this.imageUrl, remoteSearchResult.imageUrl)
                && Objects.equals(this.searchProviderName, remoteSearchResult.searchProviderName)
                && Objects.equals(this.overview, remoteSearchResult.overview)
                && Objects.equals(this.albumArtist, remoteSearchResult.albumArtist)
                && Objects.equals(this.artists, remoteSearchResult.artists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, providerIds, productionYear, indexNumber, indexNumberEnd, parentIndexNumber,
                premiereDate, imageUrl, searchProviderName, overview, albumArtist, artists);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RemoteSearchResult {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    providerIds: ").append(toIndentedString(providerIds)).append("\n");
        sb.append("    productionYear: ").append(toIndentedString(productionYear)).append("\n");
        sb.append("    indexNumber: ").append(toIndentedString(indexNumber)).append("\n");
        sb.append("    indexNumberEnd: ").append(toIndentedString(indexNumberEnd)).append("\n");
        sb.append("    parentIndexNumber: ").append(toIndentedString(parentIndexNumber)).append("\n");
        sb.append("    premiereDate: ").append(toIndentedString(premiereDate)).append("\n");
        sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
        sb.append("    searchProviderName: ").append(toIndentedString(searchProviderName)).append("\n");
        sb.append("    overview: ").append(toIndentedString(overview)).append("\n");
        sb.append("    albumArtist: ").append(toIndentedString(albumArtist)).append("\n");
        sb.append("    artists: ").append(toIndentedString(artists)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `ProviderIds` to the URL query string
        if (getProviderIds() != null) {
            for (String _key : getProviderIds().keySet()) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sProviderIds%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, _key,
                                        containerSuffix),
                        getProviderIds().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getProviderIds().get(_key)))));
            }
        }

        // add `ProductionYear` to the URL query string
        if (getProductionYear() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProductionYear%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProductionYear()))));
        }

        // add `IndexNumber` to the URL query string
        if (getIndexNumber() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumber()))));
        }

        // add `IndexNumberEnd` to the URL query string
        if (getIndexNumberEnd() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIndexNumberEnd%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumberEnd()))));
        }

        // add `ParentIndexNumber` to the URL query string
        if (getParentIndexNumber() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sParentIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentIndexNumber()))));
        }

        // add `PremiereDate` to the URL query string
        if (getPremiereDate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPremiereDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPremiereDate()))));
        }

        // add `ImageUrl` to the URL query string
        if (getImageUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImageUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageUrl()))));
        }

        // add `SearchProviderName` to the URL query string
        if (getSearchProviderName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSearchProviderName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSearchProviderName()))));
        }

        // add `Overview` to the URL query string
        if (getOverview() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sOverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOverview()))));
        }

        // add `AlbumArtist` to the URL query string
        if (getAlbumArtist() != null) {
            joiner.add(getAlbumArtist().toUrlQueryString(prefix + "AlbumArtist" + suffix));
        }

        // add `Artists` to the URL query string
        if (getArtists() != null) {
            for (int i = 0; i < getArtists().size(); i++) {
                if (getArtists().get(i) != null) {
                    joiner.add(getArtists().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sArtists%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private RemoteSearchResult instance;

        public Builder() {
            this(new RemoteSearchResult());
        }

        protected Builder(RemoteSearchResult instance) {
            this.instance = instance;
        }

        public RemoteSearchResult.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public RemoteSearchResult.Builder providerIds(Map<String, String> providerIds) {
            this.instance.providerIds = providerIds;
            return this;
        }

        public RemoteSearchResult.Builder productionYear(Integer productionYear) {
            this.instance.productionYear = productionYear;
            return this;
        }

        public RemoteSearchResult.Builder indexNumber(Integer indexNumber) {
            this.instance.indexNumber = indexNumber;
            return this;
        }

        public RemoteSearchResult.Builder indexNumberEnd(Integer indexNumberEnd) {
            this.instance.indexNumberEnd = indexNumberEnd;
            return this;
        }

        public RemoteSearchResult.Builder parentIndexNumber(Integer parentIndexNumber) {
            this.instance.parentIndexNumber = parentIndexNumber;
            return this;
        }

        public RemoteSearchResult.Builder premiereDate(OffsetDateTime premiereDate) {
            this.instance.premiereDate = premiereDate;
            return this;
        }

        public RemoteSearchResult.Builder imageUrl(String imageUrl) {
            this.instance.imageUrl = imageUrl;
            return this;
        }

        public RemoteSearchResult.Builder searchProviderName(String searchProviderName) {
            this.instance.searchProviderName = searchProviderName;
            return this;
        }

        public RemoteSearchResult.Builder overview(String overview) {
            this.instance.overview = overview;
            return this;
        }

        public RemoteSearchResult.Builder albumArtist(RemoteSearchResult albumArtist) {
            this.instance.albumArtist = albumArtist;
            return this;
        }

        public RemoteSearchResult.Builder artists(List<RemoteSearchResult> artists) {
            this.instance.artists = artists;
            return this;
        }

        /**
         * returns a built RemoteSearchResult instance.
         *
         * The builder is not reusable.
         */
        public RemoteSearchResult build() {
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
    public static RemoteSearchResult.Builder builder() {
        return new RemoteSearchResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RemoteSearchResult.Builder toBuilder() {
        return new RemoteSearchResult.Builder().name(getName()).providerIds(getProviderIds())
                .productionYear(getProductionYear()).indexNumber(getIndexNumber()).indexNumberEnd(getIndexNumberEnd())
                .parentIndexNumber(getParentIndexNumber()).premiereDate(getPremiereDate()).imageUrl(getImageUrl())
                .searchProviderName(getSearchProviderName()).overview(getOverview()).albumArtist(getAlbumArtist())
                .artists(getArtists());
    }
}
