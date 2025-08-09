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
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Get programs dto.
 */
@JsonPropertyOrder({ GetProgramsDto.JSON_PROPERTY_CHANNEL_IDS, GetProgramsDto.JSON_PROPERTY_USER_ID,
        GetProgramsDto.JSON_PROPERTY_MIN_START_DATE, GetProgramsDto.JSON_PROPERTY_HAS_AIRED,
        GetProgramsDto.JSON_PROPERTY_IS_AIRING, GetProgramsDto.JSON_PROPERTY_MAX_START_DATE,
        GetProgramsDto.JSON_PROPERTY_MIN_END_DATE, GetProgramsDto.JSON_PROPERTY_MAX_END_DATE,
        GetProgramsDto.JSON_PROPERTY_IS_MOVIE, GetProgramsDto.JSON_PROPERTY_IS_SERIES,
        GetProgramsDto.JSON_PROPERTY_IS_NEWS, GetProgramsDto.JSON_PROPERTY_IS_KIDS,
        GetProgramsDto.JSON_PROPERTY_IS_SPORTS, GetProgramsDto.JSON_PROPERTY_START_INDEX,
        GetProgramsDto.JSON_PROPERTY_LIMIT, GetProgramsDto.JSON_PROPERTY_SORT_BY,
        GetProgramsDto.JSON_PROPERTY_SORT_ORDER, GetProgramsDto.JSON_PROPERTY_GENRES,
        GetProgramsDto.JSON_PROPERTY_GENRE_IDS, GetProgramsDto.JSON_PROPERTY_ENABLE_IMAGES,
        GetProgramsDto.JSON_PROPERTY_ENABLE_TOTAL_RECORD_COUNT, GetProgramsDto.JSON_PROPERTY_IMAGE_TYPE_LIMIT,
        GetProgramsDto.JSON_PROPERTY_ENABLE_IMAGE_TYPES, GetProgramsDto.JSON_PROPERTY_ENABLE_USER_DATA,
        GetProgramsDto.JSON_PROPERTY_SERIES_TIMER_ID, GetProgramsDto.JSON_PROPERTY_LIBRARY_SERIES_ID,
        GetProgramsDto.JSON_PROPERTY_FIELDS })

public class GetProgramsDto {
    public static final String JSON_PROPERTY_CHANNEL_IDS = "ChannelIds";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> channelIds;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_MIN_START_DATE = "MinStartDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime minStartDate;

    public static final String JSON_PROPERTY_HAS_AIRED = "HasAired";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasAired;

    public static final String JSON_PROPERTY_IS_AIRING = "IsAiring";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isAiring;

    public static final String JSON_PROPERTY_MAX_START_DATE = "MaxStartDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime maxStartDate;

    public static final String JSON_PROPERTY_MIN_END_DATE = "MinEndDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime minEndDate;

    public static final String JSON_PROPERTY_MAX_END_DATE = "MaxEndDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime maxEndDate;

    public static final String JSON_PROPERTY_IS_MOVIE = "IsMovie";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isMovie;

    public static final String JSON_PROPERTY_IS_SERIES = "IsSeries";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isSeries;

    public static final String JSON_PROPERTY_IS_NEWS = "IsNews";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isNews;

    public static final String JSON_PROPERTY_IS_KIDS = "IsKids";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isKids;

    public static final String JSON_PROPERTY_IS_SPORTS = "IsSports";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isSports;

    public static final String JSON_PROPERTY_START_INDEX = "StartIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer startIndex;

    public static final String JSON_PROPERTY_LIMIT = "Limit";
    @org.eclipse.jdt.annotation.NonNull
    private Integer limit;

    public static final String JSON_PROPERTY_SORT_BY = "SortBy";
    @org.eclipse.jdt.annotation.NonNull
    private List<ItemSortBy> sortBy;

    public static final String JSON_PROPERTY_SORT_ORDER = "SortOrder";
    @org.eclipse.jdt.annotation.NonNull
    private List<SortOrder> sortOrder;

    public static final String JSON_PROPERTY_GENRES = "Genres";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> genres;

    public static final String JSON_PROPERTY_GENRE_IDS = "GenreIds";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> genreIds;

    public static final String JSON_PROPERTY_ENABLE_IMAGES = "EnableImages";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableImages;

    public static final String JSON_PROPERTY_ENABLE_TOTAL_RECORD_COUNT = "EnableTotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableTotalRecordCount = true;

    public static final String JSON_PROPERTY_IMAGE_TYPE_LIMIT = "ImageTypeLimit";
    @org.eclipse.jdt.annotation.NonNull
    private Integer imageTypeLimit;

    public static final String JSON_PROPERTY_ENABLE_IMAGE_TYPES = "EnableImageTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<ImageType> enableImageTypes;

    public static final String JSON_PROPERTY_ENABLE_USER_DATA = "EnableUserData";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableUserData;

    public static final String JSON_PROPERTY_SERIES_TIMER_ID = "SeriesTimerId";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesTimerId;

    public static final String JSON_PROPERTY_LIBRARY_SERIES_ID = "LibrarySeriesId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID librarySeriesId;

    public static final String JSON_PROPERTY_FIELDS = "Fields";
    @org.eclipse.jdt.annotation.NonNull
    private List<ItemFields> fields;

    public GetProgramsDto() {
    }

    public GetProgramsDto channelIds(@org.eclipse.jdt.annotation.NonNull List<UUID> channelIds) {
        this.channelIds = channelIds;
        return this;
    }

    public GetProgramsDto addChannelIdsItem(UUID channelIdsItem) {
        if (this.channelIds == null) {
            this.channelIds = new ArrayList<>();
        }
        this.channelIds.add(channelIdsItem);
        return this;
    }

    /**
     * Gets or sets the channels to return guide information for.
     * 
     * @return channelIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHANNEL_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getChannelIds() {
        return channelIds;
    }

    @JsonProperty(JSON_PROPERTY_CHANNEL_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelIds(@org.eclipse.jdt.annotation.NonNull List<UUID> channelIds) {
        this.channelIds = channelIds;
    }

    public GetProgramsDto userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets optional. Filter by user id.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public GetProgramsDto minStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate) {
        this.minStartDate = minStartDate;
        return this;
    }

    /**
     * Gets or sets the minimum premiere start date.
     * 
     * @return minStartDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getMinStartDate() {
        return minStartDate;
    }

    @JsonProperty(JSON_PROPERTY_MIN_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime minStartDate) {
        this.minStartDate = minStartDate;
    }

    public GetProgramsDto hasAired(@org.eclipse.jdt.annotation.NonNull Boolean hasAired) {
        this.hasAired = hasAired;
        return this;
    }

    /**
     * Gets or sets filter by programs that have completed airing, or not.
     * 
     * @return hasAired
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HAS_AIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getHasAired() {
        return hasAired;
    }

    @JsonProperty(JSON_PROPERTY_HAS_AIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasAired(@org.eclipse.jdt.annotation.NonNull Boolean hasAired) {
        this.hasAired = hasAired;
    }

    public GetProgramsDto isAiring(@org.eclipse.jdt.annotation.NonNull Boolean isAiring) {
        this.isAiring = isAiring;
        return this;
    }

    /**
     * Gets or sets filter by programs that are currently airing, or not.
     * 
     * @return isAiring
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_AIRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsAiring() {
        return isAiring;
    }

    @JsonProperty(JSON_PROPERTY_IS_AIRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAiring(@org.eclipse.jdt.annotation.NonNull Boolean isAiring) {
        this.isAiring = isAiring;
    }

    public GetProgramsDto maxStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate) {
        this.maxStartDate = maxStartDate;
        return this;
    }

    /**
     * Gets or sets the maximum premiere start date.
     * 
     * @return maxStartDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getMaxStartDate() {
        return maxStartDate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime maxStartDate) {
        this.maxStartDate = maxStartDate;
    }

    public GetProgramsDto minEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate) {
        this.minEndDate = minEndDate;
        return this;
    }

    /**
     * Gets or sets the minimum premiere end date.
     * 
     * @return minEndDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getMinEndDate() {
        return minEndDate;
    }

    @JsonProperty(JSON_PROPERTY_MIN_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime minEndDate) {
        this.minEndDate = minEndDate;
    }

    public GetProgramsDto maxEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate) {
        this.maxEndDate = maxEndDate;
        return this;
    }

    /**
     * Gets or sets the maximum premiere end date.
     * 
     * @return maxEndDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getMaxEndDate() {
        return maxEndDate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime maxEndDate) {
        this.maxEndDate = maxEndDate;
    }

    public GetProgramsDto isMovie(@org.eclipse.jdt.annotation.NonNull Boolean isMovie) {
        this.isMovie = isMovie;
        return this;
    }

    /**
     * Gets or sets filter for movies.
     * 
     * @return isMovie
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_MOVIE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsMovie() {
        return isMovie;
    }

    @JsonProperty(JSON_PROPERTY_IS_MOVIE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsMovie(@org.eclipse.jdt.annotation.NonNull Boolean isMovie) {
        this.isMovie = isMovie;
    }

    public GetProgramsDto isSeries(@org.eclipse.jdt.annotation.NonNull Boolean isSeries) {
        this.isSeries = isSeries;
        return this;
    }

    /**
     * Gets or sets filter for series.
     * 
     * @return isSeries
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_SERIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsSeries() {
        return isSeries;
    }

    @JsonProperty(JSON_PROPERTY_IS_SERIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsSeries(@org.eclipse.jdt.annotation.NonNull Boolean isSeries) {
        this.isSeries = isSeries;
    }

    public GetProgramsDto isNews(@org.eclipse.jdt.annotation.NonNull Boolean isNews) {
        this.isNews = isNews;
        return this;
    }

    /**
     * Gets or sets filter for news.
     * 
     * @return isNews
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_NEWS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsNews() {
        return isNews;
    }

    @JsonProperty(JSON_PROPERTY_IS_NEWS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsNews(@org.eclipse.jdt.annotation.NonNull Boolean isNews) {
        this.isNews = isNews;
    }

    public GetProgramsDto isKids(@org.eclipse.jdt.annotation.NonNull Boolean isKids) {
        this.isKids = isKids;
        return this;
    }

    /**
     * Gets or sets filter for kids.
     * 
     * @return isKids
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_KIDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsKids() {
        return isKids;
    }

    @JsonProperty(JSON_PROPERTY_IS_KIDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsKids(@org.eclipse.jdt.annotation.NonNull Boolean isKids) {
        this.isKids = isKids;
    }

    public GetProgramsDto isSports(@org.eclipse.jdt.annotation.NonNull Boolean isSports) {
        this.isSports = isSports;
        return this;
    }

    /**
     * Gets or sets filter for sports.
     * 
     * @return isSports
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_SPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsSports() {
        return isSports;
    }

    @JsonProperty(JSON_PROPERTY_IS_SPORTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsSports(@org.eclipse.jdt.annotation.NonNull Boolean isSports) {
        this.isSports = isSports;
    }

    public GetProgramsDto startIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Gets or sets the record index to start at. All items with a lower index will be dropped from the results.
     * 
     * @return startIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getStartIndex() {
        return startIndex;
    }

    @JsonProperty(JSON_PROPERTY_START_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
    }

    public GetProgramsDto limit(@org.eclipse.jdt.annotation.NonNull Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Gets or sets the maximum number of records to return.
     * 
     * @return limit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getLimit() {
        return limit;
    }

    @JsonProperty(JSON_PROPERTY_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLimit(@org.eclipse.jdt.annotation.NonNull Integer limit) {
        this.limit = limit;
    }

    public GetProgramsDto sortBy(@org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    public GetProgramsDto addSortByItem(ItemSortBy sortByItem) {
        if (this.sortBy == null) {
            this.sortBy = new ArrayList<>();
        }
        this.sortBy.add(sortByItem);
        return this;
    }

    /**
     * Gets or sets specify one or more sort orders, comma delimited. Options: Name, StartDate.
     * 
     * @return sortBy
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SORT_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ItemSortBy> getSortBy() {
        return sortBy;
    }

    @JsonProperty(JSON_PROPERTY_SORT_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortBy(@org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy) {
        this.sortBy = sortBy;
    }

    public GetProgramsDto sortOrder(@org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public GetProgramsDto addSortOrderItem(SortOrder sortOrderItem) {
        if (this.sortOrder == null) {
            this.sortOrder = new ArrayList<>();
        }
        this.sortOrder.add(sortOrderItem);
        return this;
    }

    /**
     * Gets or sets sort order.
     * 
     * @return sortOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SORT_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SortOrder> getSortOrder() {
        return sortOrder;
    }

    @JsonProperty(JSON_PROPERTY_SORT_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortOrder(@org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder) {
        this.sortOrder = sortOrder;
    }

    public GetProgramsDto genres(@org.eclipse.jdt.annotation.NonNull List<String> genres) {
        this.genres = genres;
        return this;
    }

    public GetProgramsDto addGenresItem(String genresItem) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genresItem);
        return this;
    }

    /**
     * Gets or sets the genres to return guide information for.
     * 
     * @return genres
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GENRES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getGenres() {
        return genres;
    }

    @JsonProperty(JSON_PROPERTY_GENRES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenres(@org.eclipse.jdt.annotation.NonNull List<String> genres) {
        this.genres = genres;
    }

    public GetProgramsDto genreIds(@org.eclipse.jdt.annotation.NonNull List<UUID> genreIds) {
        this.genreIds = genreIds;
        return this;
    }

    public GetProgramsDto addGenreIdsItem(UUID genreIdsItem) {
        if (this.genreIds == null) {
            this.genreIds = new ArrayList<>();
        }
        this.genreIds.add(genreIdsItem);
        return this;
    }

    /**
     * Gets or sets the genre ids to return guide information for.
     * 
     * @return genreIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GENRE_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getGenreIds() {
        return genreIds;
    }

    @JsonProperty(JSON_PROPERTY_GENRE_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenreIds(@org.eclipse.jdt.annotation.NonNull List<UUID> genreIds) {
        this.genreIds = genreIds;
    }

    public GetProgramsDto enableImages(@org.eclipse.jdt.annotation.NonNull Boolean enableImages) {
        this.enableImages = enableImages;
        return this;
    }

    /**
     * Gets or sets include image information in output.
     * 
     * @return enableImages
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_IMAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableImages() {
        return enableImages;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_IMAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableImages(@org.eclipse.jdt.annotation.NonNull Boolean enableImages) {
        this.enableImages = enableImages;
    }

    public GetProgramsDto enableTotalRecordCount(@org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) {
        this.enableTotalRecordCount = enableTotalRecordCount;
        return this;
    }

    /**
     * Gets or sets a value indicating whether retrieve total record count.
     * 
     * @return enableTotalRecordCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableTotalRecordCount() {
        return enableTotalRecordCount;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableTotalRecordCount(@org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) {
        this.enableTotalRecordCount = enableTotalRecordCount;
    }

    public GetProgramsDto imageTypeLimit(@org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit) {
        this.imageTypeLimit = imageTypeLimit;
        return this;
    }

    /**
     * Gets or sets the max number of images to return, per image type.
     * 
     * @return imageTypeLimit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_TYPE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getImageTypeLimit() {
        return imageTypeLimit;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_TYPE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageTypeLimit(@org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit) {
        this.imageTypeLimit = imageTypeLimit;
    }

    public GetProgramsDto enableImageTypes(@org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) {
        this.enableImageTypes = enableImageTypes;
        return this;
    }

    public GetProgramsDto addEnableImageTypesItem(ImageType enableImageTypesItem) {
        if (this.enableImageTypes == null) {
            this.enableImageTypes = new ArrayList<>();
        }
        this.enableImageTypes.add(enableImageTypesItem);
        return this;
    }

    /**
     * Gets or sets the image types to include in the output.
     * 
     * @return enableImageTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_IMAGE_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ImageType> getEnableImageTypes() {
        return enableImageTypes;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_IMAGE_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableImageTypes(@org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) {
        this.enableImageTypes = enableImageTypes;
    }

    public GetProgramsDto enableUserData(@org.eclipse.jdt.annotation.NonNull Boolean enableUserData) {
        this.enableUserData = enableUserData;
        return this;
    }

    /**
     * Gets or sets include user data.
     * 
     * @return enableUserData
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_USER_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableUserData() {
        return enableUserData;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_USER_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableUserData(@org.eclipse.jdt.annotation.NonNull Boolean enableUserData) {
        this.enableUserData = enableUserData;
    }

    public GetProgramsDto seriesTimerId(@org.eclipse.jdt.annotation.NonNull String seriesTimerId) {
        this.seriesTimerId = seriesTimerId;
        return this;
    }

    /**
     * Gets or sets filter by series timer id.
     * 
     * @return seriesTimerId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERIES_TIMER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSeriesTimerId() {
        return seriesTimerId;
    }

    @JsonProperty(JSON_PROPERTY_SERIES_TIMER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesTimerId(@org.eclipse.jdt.annotation.NonNull String seriesTimerId) {
        this.seriesTimerId = seriesTimerId;
    }

    public GetProgramsDto librarySeriesId(@org.eclipse.jdt.annotation.NonNull UUID librarySeriesId) {
        this.librarySeriesId = librarySeriesId;
        return this;
    }

    /**
     * Gets or sets filter by library series id.
     * 
     * @return librarySeriesId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIBRARY_SERIES_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getLibrarySeriesId() {
        return librarySeriesId;
    }

    @JsonProperty(JSON_PROPERTY_LIBRARY_SERIES_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibrarySeriesId(@org.eclipse.jdt.annotation.NonNull UUID librarySeriesId) {
        this.librarySeriesId = librarySeriesId;
    }

    public GetProgramsDto fields(@org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) {
        this.fields = fields;
        return this;
    }

    public GetProgramsDto addFieldsItem(ItemFields fieldsItem) {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }
        this.fields.add(fieldsItem);
        return this;
    }

    /**
     * Gets or sets specify additional fields of information to return in the output.
     * 
     * @return fields
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FIELDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ItemFields> getFields() {
        return fields;
    }

    @JsonProperty(JSON_PROPERTY_FIELDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFields(@org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) {
        this.fields = fields;
    }

    /**
     * Return true if this GetProgramsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetProgramsDto getProgramsDto = (GetProgramsDto) o;
        return Objects.equals(this.channelIds, getProgramsDto.channelIds)
                && Objects.equals(this.userId, getProgramsDto.userId)
                && Objects.equals(this.minStartDate, getProgramsDto.minStartDate)
                && Objects.equals(this.hasAired, getProgramsDto.hasAired)
                && Objects.equals(this.isAiring, getProgramsDto.isAiring)
                && Objects.equals(this.maxStartDate, getProgramsDto.maxStartDate)
                && Objects.equals(this.minEndDate, getProgramsDto.minEndDate)
                && Objects.equals(this.maxEndDate, getProgramsDto.maxEndDate)
                && Objects.equals(this.isMovie, getProgramsDto.isMovie)
                && Objects.equals(this.isSeries, getProgramsDto.isSeries)
                && Objects.equals(this.isNews, getProgramsDto.isNews)
                && Objects.equals(this.isKids, getProgramsDto.isKids)
                && Objects.equals(this.isSports, getProgramsDto.isSports)
                && Objects.equals(this.startIndex, getProgramsDto.startIndex)
                && Objects.equals(this.limit, getProgramsDto.limit)
                && Objects.equals(this.sortBy, getProgramsDto.sortBy)
                && Objects.equals(this.sortOrder, getProgramsDto.sortOrder)
                && Objects.equals(this.genres, getProgramsDto.genres)
                && Objects.equals(this.genreIds, getProgramsDto.genreIds)
                && Objects.equals(this.enableImages, getProgramsDto.enableImages)
                && Objects.equals(this.enableTotalRecordCount, getProgramsDto.enableTotalRecordCount)
                && Objects.equals(this.imageTypeLimit, getProgramsDto.imageTypeLimit)
                && Objects.equals(this.enableImageTypes, getProgramsDto.enableImageTypes)
                && Objects.equals(this.enableUserData, getProgramsDto.enableUserData)
                && Objects.equals(this.seriesTimerId, getProgramsDto.seriesTimerId)
                && Objects.equals(this.librarySeriesId, getProgramsDto.librarySeriesId)
                && Objects.equals(this.fields, getProgramsDto.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelIds, userId, minStartDate, hasAired, isAiring, maxStartDate, minEndDate, maxEndDate,
                isMovie, isSeries, isNews, isKids, isSports, startIndex, limit, sortBy, sortOrder, genres, genreIds,
                enableImages, enableTotalRecordCount, imageTypeLimit, enableImageTypes, enableUserData, seriesTimerId,
                librarySeriesId, fields);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GetProgramsDto {\n");
        sb.append("    channelIds: ").append(toIndentedString(channelIds)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    minStartDate: ").append(toIndentedString(minStartDate)).append("\n");
        sb.append("    hasAired: ").append(toIndentedString(hasAired)).append("\n");
        sb.append("    isAiring: ").append(toIndentedString(isAiring)).append("\n");
        sb.append("    maxStartDate: ").append(toIndentedString(maxStartDate)).append("\n");
        sb.append("    minEndDate: ").append(toIndentedString(minEndDate)).append("\n");
        sb.append("    maxEndDate: ").append(toIndentedString(maxEndDate)).append("\n");
        sb.append("    isMovie: ").append(toIndentedString(isMovie)).append("\n");
        sb.append("    isSeries: ").append(toIndentedString(isSeries)).append("\n");
        sb.append("    isNews: ").append(toIndentedString(isNews)).append("\n");
        sb.append("    isKids: ").append(toIndentedString(isKids)).append("\n");
        sb.append("    isSports: ").append(toIndentedString(isSports)).append("\n");
        sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    sortBy: ").append(toIndentedString(sortBy)).append("\n");
        sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
        sb.append("    genres: ").append(toIndentedString(genres)).append("\n");
        sb.append("    genreIds: ").append(toIndentedString(genreIds)).append("\n");
        sb.append("    enableImages: ").append(toIndentedString(enableImages)).append("\n");
        sb.append("    enableTotalRecordCount: ").append(toIndentedString(enableTotalRecordCount)).append("\n");
        sb.append("    imageTypeLimit: ").append(toIndentedString(imageTypeLimit)).append("\n");
        sb.append("    enableImageTypes: ").append(toIndentedString(enableImageTypes)).append("\n");
        sb.append("    enableUserData: ").append(toIndentedString(enableUserData)).append("\n");
        sb.append("    seriesTimerId: ").append(toIndentedString(seriesTimerId)).append("\n");
        sb.append("    librarySeriesId: ").append(toIndentedString(librarySeriesId)).append("\n");
        sb.append("    fields: ").append(toIndentedString(fields)).append("\n");
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
