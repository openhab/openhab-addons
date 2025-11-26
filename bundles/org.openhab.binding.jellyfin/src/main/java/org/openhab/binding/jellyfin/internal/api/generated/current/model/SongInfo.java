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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * SongInfo
 */
@JsonPropertyOrder({ SongInfo.JSON_PROPERTY_NAME, SongInfo.JSON_PROPERTY_ORIGINAL_TITLE, SongInfo.JSON_PROPERTY_PATH,
        SongInfo.JSON_PROPERTY_METADATA_LANGUAGE, SongInfo.JSON_PROPERTY_METADATA_COUNTRY_CODE,
        SongInfo.JSON_PROPERTY_PROVIDER_IDS, SongInfo.JSON_PROPERTY_YEAR, SongInfo.JSON_PROPERTY_INDEX_NUMBER,
        SongInfo.JSON_PROPERTY_PARENT_INDEX_NUMBER, SongInfo.JSON_PROPERTY_PREMIERE_DATE,
        SongInfo.JSON_PROPERTY_IS_AUTOMATED, SongInfo.JSON_PROPERTY_ALBUM_ARTISTS, SongInfo.JSON_PROPERTY_ALBUM,
        SongInfo.JSON_PROPERTY_ARTISTS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SongInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ORIGINAL_TITLE = "OriginalTitle";
    @org.eclipse.jdt.annotation.NonNull
    private String originalTitle;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_METADATA_LANGUAGE = "MetadataLanguage";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataLanguage;

    public static final String JSON_PROPERTY_METADATA_COUNTRY_CODE = "MetadataCountryCode";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataCountryCode;

    public static final String JSON_PROPERTY_PROVIDER_IDS = "ProviderIds";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> providerIds;

    public static final String JSON_PROPERTY_YEAR = "Year";
    @org.eclipse.jdt.annotation.NonNull
    private Integer year;

    public static final String JSON_PROPERTY_INDEX_NUMBER = "IndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer indexNumber;

    public static final String JSON_PROPERTY_PARENT_INDEX_NUMBER = "ParentIndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer parentIndexNumber;

    public static final String JSON_PROPERTY_PREMIERE_DATE = "PremiereDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime premiereDate;

    public static final String JSON_PROPERTY_IS_AUTOMATED = "IsAutomated";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isAutomated;

    public static final String JSON_PROPERTY_ALBUM_ARTISTS = "AlbumArtists";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> albumArtists;

    public static final String JSON_PROPERTY_ALBUM = "Album";
    @org.eclipse.jdt.annotation.NonNull
    private String album;

    public static final String JSON_PROPERTY_ARTISTS = "Artists";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> artists;

    public SongInfo() {
    }

    public SongInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public SongInfo originalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
        return this;
    }

    /**
     * Gets or sets the original title.
     * 
     * @return originalTitle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ORIGINAL_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonProperty(value = JSON_PROPERTY_ORIGINAL_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOriginalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public SongInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public SongInfo metadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
        return this;
    }

    /**
     * Gets or sets the metadata language.
     * 
     * @return metadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataLanguage() {
        return metadataLanguage;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    public SongInfo metadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public SongInfo providerIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
        return this;
    }

    public SongInfo putProviderIdsItem(String key, String providerIdsItem) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getProviderIds() {
        return providerIds;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
    }

    public SongInfo year(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
        return this;
    }

    /**
     * Gets or sets the year.
     * 
     * @return year
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getYear() {
        return year;
    }

    @JsonProperty(value = JSON_PROPERTY_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setYear(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
    }

    public SongInfo indexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Get indexNumber
     * 
     * @return indexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndexNumber() {
        return indexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public SongInfo parentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Get parentIndexNumber
     * 
     * @return parentIndexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getParentIndexNumber() {
        return parentIndexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
    }

    public SongInfo premiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
        return this;
    }

    /**
     * Get premiereDate
     * 
     * @return premiereDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getPremiereDate() {
        return premiereDate;
    }

    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPremiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
    }

    public SongInfo isAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
        return this;
    }

    /**
     * Get isAutomated
     * 
     * @return isAutomated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_AUTOMATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsAutomated() {
        return isAutomated;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_AUTOMATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
    }

    public SongInfo albumArtists(@org.eclipse.jdt.annotation.NonNull List<String> albumArtists) {
        this.albumArtists = albumArtists;
        return this;
    }

    public SongInfo addAlbumArtistsItem(String albumArtistsItem) {
        if (this.albumArtists == null) {
            this.albumArtists = new ArrayList<>();
        }
        this.albumArtists.add(albumArtistsItem);
        return this;
    }

    /**
     * Get albumArtists
     * 
     * @return albumArtists
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getAlbumArtists() {
        return albumArtists;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtists(@org.eclipse.jdt.annotation.NonNull List<String> albumArtists) {
        this.albumArtists = albumArtists;
    }

    public SongInfo album(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
        return this;
    }

    /**
     * Get album
     * 
     * @return album
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbum() {
        return album;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbum(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
    }

    public SongInfo artists(@org.eclipse.jdt.annotation.NonNull List<String> artists) {
        this.artists = artists;
        return this;
    }

    public SongInfo addArtistsItem(String artistsItem) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getArtists() {
        return artists;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtists(@org.eclipse.jdt.annotation.NonNull List<String> artists) {
        this.artists = artists;
    }

    /**
     * Return true if this SongInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SongInfo songInfo = (SongInfo) o;
        return Objects.equals(this.name, songInfo.name) && Objects.equals(this.originalTitle, songInfo.originalTitle)
                && Objects.equals(this.path, songInfo.path)
                && Objects.equals(this.metadataLanguage, songInfo.metadataLanguage)
                && Objects.equals(this.metadataCountryCode, songInfo.metadataCountryCode)
                && Objects.equals(this.providerIds, songInfo.providerIds) && Objects.equals(this.year, songInfo.year)
                && Objects.equals(this.indexNumber, songInfo.indexNumber)
                && Objects.equals(this.parentIndexNumber, songInfo.parentIndexNumber)
                && Objects.equals(this.premiereDate, songInfo.premiereDate)
                && Objects.equals(this.isAutomated, songInfo.isAutomated)
                && Objects.equals(this.albumArtists, songInfo.albumArtists)
                && Objects.equals(this.album, songInfo.album) && Objects.equals(this.artists, songInfo.artists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, originalTitle, path, metadataLanguage, metadataCountryCode, providerIds, year,
                indexNumber, parentIndexNumber, premiereDate, isAutomated, albumArtists, album, artists);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SongInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    originalTitle: ").append(toIndentedString(originalTitle)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    metadataLanguage: ").append(toIndentedString(metadataLanguage)).append("\n");
        sb.append("    metadataCountryCode: ").append(toIndentedString(metadataCountryCode)).append("\n");
        sb.append("    providerIds: ").append(toIndentedString(providerIds)).append("\n");
        sb.append("    year: ").append(toIndentedString(year)).append("\n");
        sb.append("    indexNumber: ").append(toIndentedString(indexNumber)).append("\n");
        sb.append("    parentIndexNumber: ").append(toIndentedString(parentIndexNumber)).append("\n");
        sb.append("    premiereDate: ").append(toIndentedString(premiereDate)).append("\n");
        sb.append("    isAutomated: ").append(toIndentedString(isAutomated)).append("\n");
        sb.append("    albumArtists: ").append(toIndentedString(albumArtists)).append("\n");
        sb.append("    album: ").append(toIndentedString(album)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `OriginalTitle` to the URL query string
        if (getOriginalTitle() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOriginalTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOriginalTitle()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `MetadataLanguage` to the URL query string
        if (getMetadataLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMetadataLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataLanguage()))));
        }

        // add `MetadataCountryCode` to the URL query string
        if (getMetadataCountryCode() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMetadataCountryCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataCountryCode()))));
        }

        // add `ProviderIds` to the URL query string
        if (getProviderIds() != null) {
            for (String _key : getProviderIds().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sProviderIds%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getProviderIds().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getProviderIds().get(_key)))));
            }
        }

        // add `Year` to the URL query string
        if (getYear() != null) {
            joiner.add(String.format(Locale.ROOT, "%sYear%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getYear()))));
        }

        // add `IndexNumber` to the URL query string
        if (getIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumber()))));
        }

        // add `ParentIndexNumber` to the URL query string
        if (getParentIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentIndexNumber()))));
        }

        // add `PremiereDate` to the URL query string
        if (getPremiereDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPremiereDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPremiereDate()))));
        }

        // add `IsAutomated` to the URL query string
        if (getIsAutomated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsAutomated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsAutomated()))));
        }

        // add `AlbumArtists` to the URL query string
        if (getAlbumArtists() != null) {
            for (int i = 0; i < getAlbumArtists().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sAlbumArtists%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getAlbumArtists().get(i)))));
            }
        }

        // add `Album` to the URL query string
        if (getAlbum() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbum%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbum()))));
        }

        // add `Artists` to the URL query string
        if (getArtists() != null) {
            for (int i = 0; i < getArtists().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sArtists%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getArtists().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private SongInfo instance;

        public Builder() {
            this(new SongInfo());
        }

        protected Builder(SongInfo instance) {
            this.instance = instance;
        }

        public SongInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public SongInfo.Builder originalTitle(String originalTitle) {
            this.instance.originalTitle = originalTitle;
            return this;
        }

        public SongInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public SongInfo.Builder metadataLanguage(String metadataLanguage) {
            this.instance.metadataLanguage = metadataLanguage;
            return this;
        }

        public SongInfo.Builder metadataCountryCode(String metadataCountryCode) {
            this.instance.metadataCountryCode = metadataCountryCode;
            return this;
        }

        public SongInfo.Builder providerIds(Map<String, String> providerIds) {
            this.instance.providerIds = providerIds;
            return this;
        }

        public SongInfo.Builder year(Integer year) {
            this.instance.year = year;
            return this;
        }

        public SongInfo.Builder indexNumber(Integer indexNumber) {
            this.instance.indexNumber = indexNumber;
            return this;
        }

        public SongInfo.Builder parentIndexNumber(Integer parentIndexNumber) {
            this.instance.parentIndexNumber = parentIndexNumber;
            return this;
        }

        public SongInfo.Builder premiereDate(OffsetDateTime premiereDate) {
            this.instance.premiereDate = premiereDate;
            return this;
        }

        public SongInfo.Builder isAutomated(Boolean isAutomated) {
            this.instance.isAutomated = isAutomated;
            return this;
        }

        public SongInfo.Builder albumArtists(List<String> albumArtists) {
            this.instance.albumArtists = albumArtists;
            return this;
        }

        public SongInfo.Builder album(String album) {
            this.instance.album = album;
            return this;
        }

        public SongInfo.Builder artists(List<String> artists) {
            this.instance.artists = artists;
            return this;
        }

        /**
         * returns a built SongInfo instance.
         *
         * The builder is not reusable.
         */
        public SongInfo build() {
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
    public static SongInfo.Builder builder() {
        return new SongInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SongInfo.Builder toBuilder() {
        return new SongInfo.Builder().name(getName()).originalTitle(getOriginalTitle()).path(getPath())
                .metadataLanguage(getMetadataLanguage()).metadataCountryCode(getMetadataCountryCode())
                .providerIds(getProviderIds()).year(getYear()).indexNumber(getIndexNumber())
                .parentIndexNumber(getParentIndexNumber()).premiereDate(getPremiereDate()).isAutomated(getIsAutomated())
                .albumArtists(getAlbumArtists()).album(getAlbum()).artists(getArtists());
    }
}
