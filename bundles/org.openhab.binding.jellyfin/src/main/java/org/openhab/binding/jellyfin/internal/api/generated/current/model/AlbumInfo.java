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
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * AlbumInfo
 */
@JsonPropertyOrder({ AlbumInfo.JSON_PROPERTY_NAME, AlbumInfo.JSON_PROPERTY_ORIGINAL_TITLE, AlbumInfo.JSON_PROPERTY_PATH,
        AlbumInfo.JSON_PROPERTY_METADATA_LANGUAGE, AlbumInfo.JSON_PROPERTY_METADATA_COUNTRY_CODE,
        AlbumInfo.JSON_PROPERTY_PROVIDER_IDS, AlbumInfo.JSON_PROPERTY_YEAR, AlbumInfo.JSON_PROPERTY_INDEX_NUMBER,
        AlbumInfo.JSON_PROPERTY_PARENT_INDEX_NUMBER, AlbumInfo.JSON_PROPERTY_PREMIERE_DATE,
        AlbumInfo.JSON_PROPERTY_IS_AUTOMATED, AlbumInfo.JSON_PROPERTY_ALBUM_ARTISTS,
        AlbumInfo.JSON_PROPERTY_ARTIST_PROVIDER_IDS, AlbumInfo.JSON_PROPERTY_SONG_INFOS })

public class AlbumInfo {
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
    private List<String> albumArtists = new ArrayList<>();

    public static final String JSON_PROPERTY_ARTIST_PROVIDER_IDS = "ArtistProviderIds";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> artistProviderIds = new HashMap<>();

    public static final String JSON_PROPERTY_SONG_INFOS = "SongInfos";
    @org.eclipse.jdt.annotation.NonNull
    private List<SongInfo> songInfos = new ArrayList<>();

    public AlbumInfo() {
    }

    public AlbumInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public AlbumInfo originalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
        return this;
    }

    /**
     * Gets or sets the original title.
     * 
     * @return originalTitle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ORIGINAL_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonProperty(JSON_PROPERTY_ORIGINAL_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOriginalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public AlbumInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPath() {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public AlbumInfo metadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
        return this;
    }

    /**
     * Gets or sets the metadata language.
     * 
     * @return metadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMetadataLanguage() {
        return metadataLanguage;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    public AlbumInfo metadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_COUNTRY_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_COUNTRY_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public AlbumInfo providerIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
        return this;
    }

    public AlbumInfo putProviderIdsItem(String key, String providerIdsItem) {
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
    @JsonProperty(JSON_PROPERTY_PROVIDER_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getProviderIds() {
        return providerIds;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
    }

    public AlbumInfo year(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
        return this;
    }

    /**
     * Gets or sets the year.
     * 
     * @return year
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_YEAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getYear() {
        return year;
    }

    @JsonProperty(JSON_PROPERTY_YEAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setYear(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
    }

    public AlbumInfo indexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Get indexNumber
     * 
     * @return indexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getIndexNumber() {
        return indexNumber;
    }

    @JsonProperty(JSON_PROPERTY_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public AlbumInfo parentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Get parentIndexNumber
     * 
     * @return parentIndexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getParentIndexNumber() {
        return parentIndexNumber;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
    }

    public AlbumInfo premiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
        return this;
    }

    /**
     * Get premiereDate
     * 
     * @return premiereDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREMIERE_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getPremiereDate() {
        return premiereDate;
    }

    @JsonProperty(JSON_PROPERTY_PREMIERE_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPremiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
    }

    public AlbumInfo isAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
        return this;
    }

    /**
     * Get isAutomated
     * 
     * @return isAutomated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_AUTOMATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsAutomated() {
        return isAutomated;
    }

    @JsonProperty(JSON_PROPERTY_IS_AUTOMATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
    }

    public AlbumInfo albumArtists(@org.eclipse.jdt.annotation.NonNull List<String> albumArtists) {
        this.albumArtists = albumArtists;
        return this;
    }

    public AlbumInfo addAlbumArtistsItem(String albumArtistsItem) {
        if (this.albumArtists == null) {
            this.albumArtists = new ArrayList<>();
        }
        this.albumArtists.add(albumArtistsItem);
        return this;
    }

    /**
     * Gets or sets the album artist.
     * 
     * @return albumArtists
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALBUM_ARTISTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getAlbumArtists() {
        return albumArtists;
    }

    @JsonProperty(JSON_PROPERTY_ALBUM_ARTISTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtists(@org.eclipse.jdt.annotation.NonNull List<String> albumArtists) {
        this.albumArtists = albumArtists;
    }

    public AlbumInfo artistProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> artistProviderIds) {
        this.artistProviderIds = artistProviderIds;
        return this;
    }

    public AlbumInfo putArtistProviderIdsItem(String key, String artistProviderIdsItem) {
        if (this.artistProviderIds == null) {
            this.artistProviderIds = new HashMap<>();
        }
        this.artistProviderIds.put(key, artistProviderIdsItem);
        return this;
    }

    /**
     * Gets or sets the artist provider ids.
     * 
     * @return artistProviderIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ARTIST_PROVIDER_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getArtistProviderIds() {
        return artistProviderIds;
    }

    @JsonProperty(JSON_PROPERTY_ARTIST_PROVIDER_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtistProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> artistProviderIds) {
        this.artistProviderIds = artistProviderIds;
    }

    public AlbumInfo songInfos(@org.eclipse.jdt.annotation.NonNull List<SongInfo> songInfos) {
        this.songInfos = songInfos;
        return this;
    }

    public AlbumInfo addSongInfosItem(SongInfo songInfosItem) {
        if (this.songInfos == null) {
            this.songInfos = new ArrayList<>();
        }
        this.songInfos.add(songInfosItem);
        return this;
    }

    /**
     * Get songInfos
     * 
     * @return songInfos
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SONG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SongInfo> getSongInfos() {
        return songInfos;
    }

    @JsonProperty(JSON_PROPERTY_SONG_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSongInfos(@org.eclipse.jdt.annotation.NonNull List<SongInfo> songInfos) {
        this.songInfos = songInfos;
    }

    /**
     * Return true if this AlbumInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlbumInfo albumInfo = (AlbumInfo) o;
        return Objects.equals(this.name, albumInfo.name) && Objects.equals(this.originalTitle, albumInfo.originalTitle)
                && Objects.equals(this.path, albumInfo.path)
                && Objects.equals(this.metadataLanguage, albumInfo.metadataLanguage)
                && Objects.equals(this.metadataCountryCode, albumInfo.metadataCountryCode)
                && Objects.equals(this.providerIds, albumInfo.providerIds) && Objects.equals(this.year, albumInfo.year)
                && Objects.equals(this.indexNumber, albumInfo.indexNumber)
                && Objects.equals(this.parentIndexNumber, albumInfo.parentIndexNumber)
                && Objects.equals(this.premiereDate, albumInfo.premiereDate)
                && Objects.equals(this.isAutomated, albumInfo.isAutomated)
                && Objects.equals(this.albumArtists, albumInfo.albumArtists)
                && Objects.equals(this.artistProviderIds, albumInfo.artistProviderIds)
                && Objects.equals(this.songInfos, albumInfo.songInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, originalTitle, path, metadataLanguage, metadataCountryCode, providerIds, year,
                indexNumber, parentIndexNumber, premiereDate, isAutomated, albumArtists, artistProviderIds, songInfos);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AlbumInfo {\n");
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
        sb.append("    artistProviderIds: ").append(toIndentedString(artistProviderIds)).append("\n");
        sb.append("    songInfos: ").append(toIndentedString(songInfos)).append("\n");
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
