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
 * Class SearchHintResult.
 */
@JsonPropertyOrder({ SearchHint.JSON_PROPERTY_ITEM_ID, SearchHint.JSON_PROPERTY_ID, SearchHint.JSON_PROPERTY_NAME,
        SearchHint.JSON_PROPERTY_MATCHED_TERM, SearchHint.JSON_PROPERTY_INDEX_NUMBER,
        SearchHint.JSON_PROPERTY_PRODUCTION_YEAR, SearchHint.JSON_PROPERTY_PARENT_INDEX_NUMBER,
        SearchHint.JSON_PROPERTY_PRIMARY_IMAGE_TAG, SearchHint.JSON_PROPERTY_THUMB_IMAGE_TAG,
        SearchHint.JSON_PROPERTY_THUMB_IMAGE_ITEM_ID, SearchHint.JSON_PROPERTY_BACKDROP_IMAGE_TAG,
        SearchHint.JSON_PROPERTY_BACKDROP_IMAGE_ITEM_ID, SearchHint.JSON_PROPERTY_TYPE,
        SearchHint.JSON_PROPERTY_IS_FOLDER, SearchHint.JSON_PROPERTY_RUN_TIME_TICKS,
        SearchHint.JSON_PROPERTY_MEDIA_TYPE, SearchHint.JSON_PROPERTY_START_DATE, SearchHint.JSON_PROPERTY_END_DATE,
        SearchHint.JSON_PROPERTY_SERIES, SearchHint.JSON_PROPERTY_STATUS, SearchHint.JSON_PROPERTY_ALBUM,
        SearchHint.JSON_PROPERTY_ALBUM_ID, SearchHint.JSON_PROPERTY_ALBUM_ARTIST, SearchHint.JSON_PROPERTY_ARTISTS,
        SearchHint.JSON_PROPERTY_SONG_COUNT, SearchHint.JSON_PROPERTY_EPISODE_COUNT,
        SearchHint.JSON_PROPERTY_CHANNEL_ID, SearchHint.JSON_PROPERTY_CHANNEL_NAME,
        SearchHint.JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SearchHint {
    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID itemId;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_MATCHED_TERM = "MatchedTerm";
    @org.eclipse.jdt.annotation.NonNull
    private String matchedTerm;

    public static final String JSON_PROPERTY_INDEX_NUMBER = "IndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer indexNumber;

    public static final String JSON_PROPERTY_PRODUCTION_YEAR = "ProductionYear";
    @org.eclipse.jdt.annotation.NonNull
    private Integer productionYear;

    public static final String JSON_PROPERTY_PARENT_INDEX_NUMBER = "ParentIndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer parentIndexNumber;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_TAG = "PrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String primaryImageTag;

    public static final String JSON_PROPERTY_THUMB_IMAGE_TAG = "ThumbImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String thumbImageTag;

    public static final String JSON_PROPERTY_THUMB_IMAGE_ITEM_ID = "ThumbImageItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String thumbImageItemId;

    public static final String JSON_PROPERTY_BACKDROP_IMAGE_TAG = "BackdropImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String backdropImageTag;

    public static final String JSON_PROPERTY_BACKDROP_IMAGE_ITEM_ID = "BackdropImageItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String backdropImageItemId;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemKind type;

    public static final String JSON_PROPERTY_IS_FOLDER = "IsFolder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isFolder;

    public static final String JSON_PROPERTY_RUN_TIME_TICKS = "RunTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long runTimeTicks;

    public static final String JSON_PROPERTY_MEDIA_TYPE = "MediaType";
    @org.eclipse.jdt.annotation.NonNull
    private MediaType mediaType = MediaType.UNKNOWN;

    public static final String JSON_PROPERTY_START_DATE = "StartDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime startDate;

    public static final String JSON_PROPERTY_END_DATE = "EndDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime endDate;

    public static final String JSON_PROPERTY_SERIES = "Series";
    @org.eclipse.jdt.annotation.NonNull
    private String series;

    public static final String JSON_PROPERTY_STATUS = "Status";
    @org.eclipse.jdt.annotation.NonNull
    private String status;

    public static final String JSON_PROPERTY_ALBUM = "Album";
    @org.eclipse.jdt.annotation.NonNull
    private String album;

    public static final String JSON_PROPERTY_ALBUM_ID = "AlbumId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID albumId;

    public static final String JSON_PROPERTY_ALBUM_ARTIST = "AlbumArtist";
    @org.eclipse.jdt.annotation.NonNull
    private String albumArtist;

    public static final String JSON_PROPERTY_ARTISTS = "Artists";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> artists = new ArrayList<>();

    public static final String JSON_PROPERTY_SONG_COUNT = "SongCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer songCount;

    public static final String JSON_PROPERTY_EPISODE_COUNT = "EpisodeCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer episodeCount;

    public static final String JSON_PROPERTY_CHANNEL_ID = "ChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID channelId;

    public static final String JSON_PROPERTY_CHANNEL_NAME = "ChannelName";
    @org.eclipse.jdt.annotation.NonNull
    private String channelName;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO = "PrimaryImageAspectRatio";
    @org.eclipse.jdt.annotation.NonNull
    private Double primaryImageAspectRatio;

    public SearchHint() {
    }

    public SearchHint itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item id.
     * 
     * @return itemId
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
    }

    public SearchHint id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the item id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public SearchHint name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public SearchHint matchedTerm(@org.eclipse.jdt.annotation.NonNull String matchedTerm) {
        this.matchedTerm = matchedTerm;
        return this;
    }

    /**
     * Gets or sets the matched term.
     * 
     * @return matchedTerm
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MATCHED_TERM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMatchedTerm() {
        return matchedTerm;
    }

    @JsonProperty(value = JSON_PROPERTY_MATCHED_TERM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMatchedTerm(@org.eclipse.jdt.annotation.NonNull String matchedTerm) {
        this.matchedTerm = matchedTerm;
    }

    public SearchHint indexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Gets or sets the index number.
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

    public SearchHint productionYear(@org.eclipse.jdt.annotation.NonNull Integer productionYear) {
        this.productionYear = productionYear;
        return this;
    }

    /**
     * Gets or sets the production year.
     * 
     * @return productionYear
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getProductionYear() {
        return productionYear;
    }

    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProductionYear(@org.eclipse.jdt.annotation.NonNull Integer productionYear) {
        this.productionYear = productionYear;
    }

    public SearchHint parentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Gets or sets the parent index number.
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

    public SearchHint primaryImageTag(@org.eclipse.jdt.annotation.NonNull String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
        return this;
    }

    /**
     * Gets or sets the image tag.
     * 
     * @return primaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPrimaryImageTag() {
        return primaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
    }

    public SearchHint thumbImageTag(@org.eclipse.jdt.annotation.NonNull String thumbImageTag) {
        this.thumbImageTag = thumbImageTag;
        return this;
    }

    /**
     * Gets or sets the thumb image tag.
     * 
     * @return thumbImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getThumbImageTag() {
        return thumbImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThumbImageTag(@org.eclipse.jdt.annotation.NonNull String thumbImageTag) {
        this.thumbImageTag = thumbImageTag;
    }

    public SearchHint thumbImageItemId(@org.eclipse.jdt.annotation.NonNull String thumbImageItemId) {
        this.thumbImageItemId = thumbImageItemId;
        return this;
    }

    /**
     * Gets or sets the thumb image item identifier.
     * 
     * @return thumbImageItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_THUMB_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getThumbImageItemId() {
        return thumbImageItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_THUMB_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThumbImageItemId(@org.eclipse.jdt.annotation.NonNull String thumbImageItemId) {
        this.thumbImageItemId = thumbImageItemId;
    }

    public SearchHint backdropImageTag(@org.eclipse.jdt.annotation.NonNull String backdropImageTag) {
        this.backdropImageTag = backdropImageTag;
        return this;
    }

    /**
     * Gets or sets the backdrop image tag.
     * 
     * @return backdropImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BACKDROP_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBackdropImageTag() {
        return backdropImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_BACKDROP_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackdropImageTag(@org.eclipse.jdt.annotation.NonNull String backdropImageTag) {
        this.backdropImageTag = backdropImageTag;
    }

    public SearchHint backdropImageItemId(@org.eclipse.jdt.annotation.NonNull String backdropImageItemId) {
        this.backdropImageItemId = backdropImageItemId;
        return this;
    }

    /**
     * Gets or sets the backdrop image item identifier.
     * 
     * @return backdropImageItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BACKDROP_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBackdropImageItemId() {
        return backdropImageItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_BACKDROP_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackdropImageItemId(@org.eclipse.jdt.annotation.NonNull String backdropImageItemId) {
        this.backdropImageItemId = backdropImageItemId;
    }

    public SearchHint type(@org.eclipse.jdt.annotation.NonNull BaseItemKind type) {
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
    public BaseItemKind getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull BaseItemKind type) {
        this.type = type;
    }

    public SearchHint isFolder(@org.eclipse.jdt.annotation.NonNull Boolean isFolder) {
        this.isFolder = isFolder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is folder.
     * 
     * @return isFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsFolder() {
        return isFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFolder(@org.eclipse.jdt.annotation.NonNull Boolean isFolder) {
        this.isFolder = isFolder;
    }

    public SearchHint runTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
        return this;
    }

    /**
     * Gets or sets the run time ticks.
     * 
     * @return runTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RUN_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getRunTimeTicks() {
        return runTimeTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_RUN_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRunTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
    }

    public SearchHint mediaType(@org.eclipse.jdt.annotation.NonNull MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * Gets or sets the type of the media.
     * 
     * @return mediaType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaType getMediaType() {
        return mediaType;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaType(@org.eclipse.jdt.annotation.NonNull MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public SearchHint startDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Gets or sets the start date.
     * 
     * @return startDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getStartDate() {
        return startDate;
    }

    @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public SearchHint endDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Gets or sets the end date.
     * 
     * @return endDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public SearchHint series(@org.eclipse.jdt.annotation.NonNull String series) {
        this.series = series;
        return this;
    }

    /**
     * Gets or sets the series.
     * 
     * @return series
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeries() {
        return series;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeries(@org.eclipse.jdt.annotation.NonNull String series) {
        this.series = series;
    }

    public SearchHint status(@org.eclipse.jdt.annotation.NonNull String status) {
        this.status = status;
        return this;
    }

    /**
     * Gets or sets the status.
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getStatus() {
        return status;
    }

    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull String status) {
        this.status = status;
    }

    public SearchHint album(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
        return this;
    }

    /**
     * Gets or sets the album.
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

    public SearchHint albumId(@org.eclipse.jdt.annotation.NonNull UUID albumId) {
        this.albumId = albumId;
        return this;
    }

    /**
     * Gets or sets the album id.
     * 
     * @return albumId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getAlbumId() {
        return albumId;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumId(@org.eclipse.jdt.annotation.NonNull UUID albumId) {
        this.albumId = albumId;
    }

    public SearchHint albumArtist(@org.eclipse.jdt.annotation.NonNull String albumArtist) {
        this.albumArtist = albumArtist;
        return this;
    }

    /**
     * Gets or sets the album artist.
     * 
     * @return albumArtist
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbumArtist() {
        return albumArtist;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtist(@org.eclipse.jdt.annotation.NonNull String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public SearchHint artists(@org.eclipse.jdt.annotation.NonNull List<String> artists) {
        this.artists = artists;
        return this;
    }

    public SearchHint addArtistsItem(String artistsItem) {
        if (this.artists == null) {
            this.artists = new ArrayList<>();
        }
        this.artists.add(artistsItem);
        return this;
    }

    /**
     * Gets or sets the artists.
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

    public SearchHint songCount(@org.eclipse.jdt.annotation.NonNull Integer songCount) {
        this.songCount = songCount;
        return this;
    }

    /**
     * Gets or sets the song count.
     * 
     * @return songCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SONG_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSongCount() {
        return songCount;
    }

    @JsonProperty(value = JSON_PROPERTY_SONG_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSongCount(@org.eclipse.jdt.annotation.NonNull Integer songCount) {
        this.songCount = songCount;
    }

    public SearchHint episodeCount(@org.eclipse.jdt.annotation.NonNull Integer episodeCount) {
        this.episodeCount = episodeCount;
        return this;
    }

    /**
     * Gets or sets the episode count.
     * 
     * @return episodeCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_EPISODE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getEpisodeCount() {
        return episodeCount;
    }

    @JsonProperty(value = JSON_PROPERTY_EPISODE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpisodeCount(@org.eclipse.jdt.annotation.NonNull Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public SearchHint channelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Gets or sets the channel identifier.
     * 
     * @return channelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getChannelId() {
        return channelId;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
        this.channelId = channelId;
    }

    public SearchHint channelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Gets or sets the name of the channel.
     * 
     * @return channelName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
        this.channelName = channelName;
    }

    public SearchHint primaryImageAspectRatio(@org.eclipse.jdt.annotation.NonNull Double primaryImageAspectRatio) {
        this.primaryImageAspectRatio = primaryImageAspectRatio;
        return this;
    }

    /**
     * Gets or sets the primary image aspect ratio.
     * 
     * @return primaryImageAspectRatio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getPrimaryImageAspectRatio() {
        return primaryImageAspectRatio;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageAspectRatio(@org.eclipse.jdt.annotation.NonNull Double primaryImageAspectRatio) {
        this.primaryImageAspectRatio = primaryImageAspectRatio;
    }

    /**
     * Return true if this SearchHint object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchHint searchHint = (SearchHint) o;
        return Objects.equals(this.itemId, searchHint.itemId) && Objects.equals(this.id, searchHint.id)
                && Objects.equals(this.name, searchHint.name)
                && Objects.equals(this.matchedTerm, searchHint.matchedTerm)
                && Objects.equals(this.indexNumber, searchHint.indexNumber)
                && Objects.equals(this.productionYear, searchHint.productionYear)
                && Objects.equals(this.parentIndexNumber, searchHint.parentIndexNumber)
                && Objects.equals(this.primaryImageTag, searchHint.primaryImageTag)
                && Objects.equals(this.thumbImageTag, searchHint.thumbImageTag)
                && Objects.equals(this.thumbImageItemId, searchHint.thumbImageItemId)
                && Objects.equals(this.backdropImageTag, searchHint.backdropImageTag)
                && Objects.equals(this.backdropImageItemId, searchHint.backdropImageItemId)
                && Objects.equals(this.type, searchHint.type) && Objects.equals(this.isFolder, searchHint.isFolder)
                && Objects.equals(this.runTimeTicks, searchHint.runTimeTicks)
                && Objects.equals(this.mediaType, searchHint.mediaType)
                && Objects.equals(this.startDate, searchHint.startDate)
                && Objects.equals(this.endDate, searchHint.endDate) && Objects.equals(this.series, searchHint.series)
                && Objects.equals(this.status, searchHint.status) && Objects.equals(this.album, searchHint.album)
                && Objects.equals(this.albumId, searchHint.albumId)
                && Objects.equals(this.albumArtist, searchHint.albumArtist)
                && Objects.equals(this.artists, searchHint.artists)
                && Objects.equals(this.songCount, searchHint.songCount)
                && Objects.equals(this.episodeCount, searchHint.episodeCount)
                && Objects.equals(this.channelId, searchHint.channelId)
                && Objects.equals(this.channelName, searchHint.channelName)
                && Objects.equals(this.primaryImageAspectRatio, searchHint.primaryImageAspectRatio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, id, name, matchedTerm, indexNumber, productionYear, parentIndexNumber,
                primaryImageTag, thumbImageTag, thumbImageItemId, backdropImageTag, backdropImageItemId, type, isFolder,
                runTimeTicks, mediaType, startDate, endDate, series, status, album, albumId, albumArtist, artists,
                songCount, episodeCount, channelId, channelName, primaryImageAspectRatio);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SearchHint {\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    matchedTerm: ").append(toIndentedString(matchedTerm)).append("\n");
        sb.append("    indexNumber: ").append(toIndentedString(indexNumber)).append("\n");
        sb.append("    productionYear: ").append(toIndentedString(productionYear)).append("\n");
        sb.append("    parentIndexNumber: ").append(toIndentedString(parentIndexNumber)).append("\n");
        sb.append("    primaryImageTag: ").append(toIndentedString(primaryImageTag)).append("\n");
        sb.append("    thumbImageTag: ").append(toIndentedString(thumbImageTag)).append("\n");
        sb.append("    thumbImageItemId: ").append(toIndentedString(thumbImageItemId)).append("\n");
        sb.append("    backdropImageTag: ").append(toIndentedString(backdropImageTag)).append("\n");
        sb.append("    backdropImageItemId: ").append(toIndentedString(backdropImageItemId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    isFolder: ").append(toIndentedString(isFolder)).append("\n");
        sb.append("    runTimeTicks: ").append(toIndentedString(runTimeTicks)).append("\n");
        sb.append("    mediaType: ").append(toIndentedString(mediaType)).append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
        sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
        sb.append("    series: ").append(toIndentedString(series)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    album: ").append(toIndentedString(album)).append("\n");
        sb.append("    albumId: ").append(toIndentedString(albumId)).append("\n");
        sb.append("    albumArtist: ").append(toIndentedString(albumArtist)).append("\n");
        sb.append("    artists: ").append(toIndentedString(artists)).append("\n");
        sb.append("    songCount: ").append(toIndentedString(songCount)).append("\n");
        sb.append("    episodeCount: ").append(toIndentedString(episodeCount)).append("\n");
        sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
        sb.append("    channelName: ").append(toIndentedString(channelName)).append("\n");
        sb.append("    primaryImageAspectRatio: ").append(toIndentedString(primaryImageAspectRatio)).append("\n");
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

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `MatchedTerm` to the URL query string
        if (getMatchedTerm() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMatchedTerm%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMatchedTerm()))));
        }

        // add `IndexNumber` to the URL query string
        if (getIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumber()))));
        }

        // add `ProductionYear` to the URL query string
        if (getProductionYear() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProductionYear%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProductionYear()))));
        }

        // add `ParentIndexNumber` to the URL query string
        if (getParentIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentIndexNumber()))));
        }

        // add `PrimaryImageTag` to the URL query string
        if (getPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageTag()))));
        }

        // add `ThumbImageTag` to the URL query string
        if (getThumbImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sThumbImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThumbImageTag()))));
        }

        // add `ThumbImageItemId` to the URL query string
        if (getThumbImageItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sThumbImageItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThumbImageItemId()))));
        }

        // add `BackdropImageTag` to the URL query string
        if (getBackdropImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sBackdropImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBackdropImageTag()))));
        }

        // add `BackdropImageItemId` to the URL query string
        if (getBackdropImageItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sBackdropImageItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBackdropImageItemId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `IsFolder` to the URL query string
        if (getIsFolder() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsFolder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsFolder()))));
        }

        // add `RunTimeTicks` to the URL query string
        if (getRunTimeTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRunTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRunTimeTicks()))));
        }

        // add `MediaType` to the URL query string
        if (getMediaType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMediaType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaType()))));
        }

        // add `StartDate` to the URL query string
        if (getStartDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartDate()))));
        }

        // add `EndDate` to the URL query string
        if (getEndDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEndDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndDate()))));
        }

        // add `Series` to the URL query string
        if (getSeries() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeries%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeries()))));
        }

        // add `Status` to the URL query string
        if (getStatus() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStatus%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
        }

        // add `Album` to the URL query string
        if (getAlbum() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbum%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbum()))));
        }

        // add `AlbumId` to the URL query string
        if (getAlbumId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbumId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumId()))));
        }

        // add `AlbumArtist` to the URL query string
        if (getAlbumArtist() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbumArtist%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumArtist()))));
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

        // add `SongCount` to the URL query string
        if (getSongCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSongCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSongCount()))));
        }

        // add `EpisodeCount` to the URL query string
        if (getEpisodeCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEpisodeCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEpisodeCount()))));
        }

        // add `ChannelId` to the URL query string
        if (getChannelId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelId()))));
        }

        // add `ChannelName` to the URL query string
        if (getChannelName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelName()))));
        }

        // add `PrimaryImageAspectRatio` to the URL query string
        if (getPrimaryImageAspectRatio() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageAspectRatio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageAspectRatio()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SearchHint instance;

        public Builder() {
            this(new SearchHint());
        }

        protected Builder(SearchHint instance) {
            this.instance = instance;
        }

        public SearchHint.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public SearchHint.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public SearchHint.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public SearchHint.Builder matchedTerm(String matchedTerm) {
            this.instance.matchedTerm = matchedTerm;
            return this;
        }

        public SearchHint.Builder indexNumber(Integer indexNumber) {
            this.instance.indexNumber = indexNumber;
            return this;
        }

        public SearchHint.Builder productionYear(Integer productionYear) {
            this.instance.productionYear = productionYear;
            return this;
        }

        public SearchHint.Builder parentIndexNumber(Integer parentIndexNumber) {
            this.instance.parentIndexNumber = parentIndexNumber;
            return this;
        }

        public SearchHint.Builder primaryImageTag(String primaryImageTag) {
            this.instance.primaryImageTag = primaryImageTag;
            return this;
        }

        public SearchHint.Builder thumbImageTag(String thumbImageTag) {
            this.instance.thumbImageTag = thumbImageTag;
            return this;
        }

        public SearchHint.Builder thumbImageItemId(String thumbImageItemId) {
            this.instance.thumbImageItemId = thumbImageItemId;
            return this;
        }

        public SearchHint.Builder backdropImageTag(String backdropImageTag) {
            this.instance.backdropImageTag = backdropImageTag;
            return this;
        }

        public SearchHint.Builder backdropImageItemId(String backdropImageItemId) {
            this.instance.backdropImageItemId = backdropImageItemId;
            return this;
        }

        public SearchHint.Builder type(BaseItemKind type) {
            this.instance.type = type;
            return this;
        }

        public SearchHint.Builder isFolder(Boolean isFolder) {
            this.instance.isFolder = isFolder;
            return this;
        }

        public SearchHint.Builder runTimeTicks(Long runTimeTicks) {
            this.instance.runTimeTicks = runTimeTicks;
            return this;
        }

        public SearchHint.Builder mediaType(MediaType mediaType) {
            this.instance.mediaType = mediaType;
            return this;
        }

        public SearchHint.Builder startDate(OffsetDateTime startDate) {
            this.instance.startDate = startDate;
            return this;
        }

        public SearchHint.Builder endDate(OffsetDateTime endDate) {
            this.instance.endDate = endDate;
            return this;
        }

        public SearchHint.Builder series(String series) {
            this.instance.series = series;
            return this;
        }

        public SearchHint.Builder status(String status) {
            this.instance.status = status;
            return this;
        }

        public SearchHint.Builder album(String album) {
            this.instance.album = album;
            return this;
        }

        public SearchHint.Builder albumId(UUID albumId) {
            this.instance.albumId = albumId;
            return this;
        }

        public SearchHint.Builder albumArtist(String albumArtist) {
            this.instance.albumArtist = albumArtist;
            return this;
        }

        public SearchHint.Builder artists(List<String> artists) {
            this.instance.artists = artists;
            return this;
        }

        public SearchHint.Builder songCount(Integer songCount) {
            this.instance.songCount = songCount;
            return this;
        }

        public SearchHint.Builder episodeCount(Integer episodeCount) {
            this.instance.episodeCount = episodeCount;
            return this;
        }

        public SearchHint.Builder channelId(UUID channelId) {
            this.instance.channelId = channelId;
            return this;
        }

        public SearchHint.Builder channelName(String channelName) {
            this.instance.channelName = channelName;
            return this;
        }

        public SearchHint.Builder primaryImageAspectRatio(Double primaryImageAspectRatio) {
            this.instance.primaryImageAspectRatio = primaryImageAspectRatio;
            return this;
        }

        /**
         * returns a built SearchHint instance.
         *
         * The builder is not reusable.
         */
        public SearchHint build() {
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
    public static SearchHint.Builder builder() {
        return new SearchHint.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SearchHint.Builder toBuilder() {
        return new SearchHint.Builder().itemId(getItemId()).id(getId()).name(getName()).matchedTerm(getMatchedTerm())
                .indexNumber(getIndexNumber()).productionYear(getProductionYear())
                .parentIndexNumber(getParentIndexNumber()).primaryImageTag(getPrimaryImageTag())
                .thumbImageTag(getThumbImageTag()).thumbImageItemId(getThumbImageItemId())
                .backdropImageTag(getBackdropImageTag()).backdropImageItemId(getBackdropImageItemId()).type(getType())
                .isFolder(getIsFolder()).runTimeTicks(getRunTimeTicks()).mediaType(getMediaType())
                .startDate(getStartDate()).endDate(getEndDate()).series(getSeries()).status(getStatus())
                .album(getAlbum()).albumId(getAlbumId()).albumArtist(getAlbumArtist()).artists(getArtists())
                .songCount(getSongCount()).episodeCount(getEpisodeCount()).channelId(getChannelId())
                .channelName(getChannelName()).primaryImageAspectRatio(getPrimaryImageAspectRatio());
    }
}
