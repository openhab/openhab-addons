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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ChannelFeatures
 */
@JsonPropertyOrder({ ChannelFeatures.JSON_PROPERTY_NAME, ChannelFeatures.JSON_PROPERTY_ID,
        ChannelFeatures.JSON_PROPERTY_CAN_SEARCH, ChannelFeatures.JSON_PROPERTY_MEDIA_TYPES,
        ChannelFeatures.JSON_PROPERTY_CONTENT_TYPES, ChannelFeatures.JSON_PROPERTY_MAX_PAGE_SIZE,
        ChannelFeatures.JSON_PROPERTY_AUTO_REFRESH_LEVELS, ChannelFeatures.JSON_PROPERTY_DEFAULT_SORT_FIELDS,
        ChannelFeatures.JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE, ChannelFeatures.JSON_PROPERTY_SUPPORTS_LATEST_MEDIA,
        ChannelFeatures.JSON_PROPERTY_CAN_FILTER, ChannelFeatures.JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING })

public class ChannelFeatures {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_CAN_SEARCH = "CanSearch";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canSearch;

    public static final String JSON_PROPERTY_MEDIA_TYPES = "MediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChannelMediaType> mediaTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTENT_TYPES = "ContentTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChannelMediaContentType> contentTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_MAX_PAGE_SIZE = "MaxPageSize";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxPageSize;

    public static final String JSON_PROPERTY_AUTO_REFRESH_LEVELS = "AutoRefreshLevels";
    @org.eclipse.jdt.annotation.NonNull
    private Integer autoRefreshLevels;

    public static final String JSON_PROPERTY_DEFAULT_SORT_FIELDS = "DefaultSortFields";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChannelItemSortField> defaultSortFields = new ArrayList<>();

    public static final String JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE = "SupportsSortOrderToggle";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsSortOrderToggle;

    public static final String JSON_PROPERTY_SUPPORTS_LATEST_MEDIA = "SupportsLatestMedia";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsLatestMedia;

    public static final String JSON_PROPERTY_CAN_FILTER = "CanFilter";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canFilter;

    public static final String JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING = "SupportsContentDownloading";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsContentDownloading;

    public ChannelFeatures() {
    }

    public ChannelFeatures name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public ChannelFeatures id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public ChannelFeatures canSearch(@org.eclipse.jdt.annotation.NonNull Boolean canSearch) {
        this.canSearch = canSearch;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can search.
     * 
     * @return canSearch
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAN_SEARCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCanSearch() {
        return canSearch;
    }

    @JsonProperty(JSON_PROPERTY_CAN_SEARCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanSearch(@org.eclipse.jdt.annotation.NonNull Boolean canSearch) {
        this.canSearch = canSearch;
    }

    public ChannelFeatures mediaTypes(@org.eclipse.jdt.annotation.NonNull List<ChannelMediaType> mediaTypes) {
        this.mediaTypes = mediaTypes;
        return this;
    }

    public ChannelFeatures addMediaTypesItem(ChannelMediaType mediaTypesItem) {
        if (this.mediaTypes == null) {
            this.mediaTypes = new ArrayList<>();
        }
        this.mediaTypes.add(mediaTypesItem);
        return this;
    }

    /**
     * Gets or sets the media types.
     * 
     * @return mediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ChannelMediaType> getMediaTypes() {
        return mediaTypes;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaTypes(@org.eclipse.jdt.annotation.NonNull List<ChannelMediaType> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public ChannelFeatures contentTypes(
            @org.eclipse.jdt.annotation.NonNull List<ChannelMediaContentType> contentTypes) {
        this.contentTypes = contentTypes;
        return this;
    }

    public ChannelFeatures addContentTypesItem(ChannelMediaContentType contentTypesItem) {
        if (this.contentTypes == null) {
            this.contentTypes = new ArrayList<>();
        }
        this.contentTypes.add(contentTypesItem);
        return this;
    }

    /**
     * Gets or sets the content types.
     * 
     * @return contentTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTENT_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ChannelMediaContentType> getContentTypes() {
        return contentTypes;
    }

    @JsonProperty(JSON_PROPERTY_CONTENT_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentTypes(@org.eclipse.jdt.annotation.NonNull List<ChannelMediaContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public ChannelFeatures maxPageSize(@org.eclipse.jdt.annotation.NonNull Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Gets or sets the maximum number of records the channel allows retrieving at a time.
     * 
     * @return maxPageSize
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_PAGE_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxPageSize() {
        return maxPageSize;
    }

    @JsonProperty(JSON_PROPERTY_MAX_PAGE_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxPageSize(@org.eclipse.jdt.annotation.NonNull Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public ChannelFeatures autoRefreshLevels(@org.eclipse.jdt.annotation.NonNull Integer autoRefreshLevels) {
        this.autoRefreshLevels = autoRefreshLevels;
        return this;
    }

    /**
     * Gets or sets the automatic refresh levels.
     * 
     * @return autoRefreshLevels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUTO_REFRESH_LEVELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAutoRefreshLevels() {
        return autoRefreshLevels;
    }

    @JsonProperty(JSON_PROPERTY_AUTO_REFRESH_LEVELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutoRefreshLevels(@org.eclipse.jdt.annotation.NonNull Integer autoRefreshLevels) {
        this.autoRefreshLevels = autoRefreshLevels;
    }

    public ChannelFeatures defaultSortFields(
            @org.eclipse.jdt.annotation.NonNull List<ChannelItemSortField> defaultSortFields) {
        this.defaultSortFields = defaultSortFields;
        return this;
    }

    public ChannelFeatures addDefaultSortFieldsItem(ChannelItemSortField defaultSortFieldsItem) {
        if (this.defaultSortFields == null) {
            this.defaultSortFields = new ArrayList<>();
        }
        this.defaultSortFields.add(defaultSortFieldsItem);
        return this;
    }

    /**
     * Gets or sets the default sort orders.
     * 
     * @return defaultSortFields
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEFAULT_SORT_FIELDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ChannelItemSortField> getDefaultSortFields() {
        return defaultSortFields;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_SORT_FIELDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultSortFields(@org.eclipse.jdt.annotation.NonNull List<ChannelItemSortField> defaultSortFields) {
        this.defaultSortFields = defaultSortFields;
    }

    public ChannelFeatures supportsSortOrderToggle(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsSortOrderToggle) {
        this.supportsSortOrderToggle = supportsSortOrderToggle;
        return this;
    }

    /**
     * Gets or sets a value indicating whether a sort ascending/descending toggle is supported.
     * 
     * @return supportsSortOrderToggle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportsSortOrderToggle() {
        return supportsSortOrderToggle;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsSortOrderToggle(@org.eclipse.jdt.annotation.NonNull Boolean supportsSortOrderToggle) {
        this.supportsSortOrderToggle = supportsSortOrderToggle;
    }

    public ChannelFeatures supportsLatestMedia(@org.eclipse.jdt.annotation.NonNull Boolean supportsLatestMedia) {
        this.supportsLatestMedia = supportsLatestMedia;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [supports latest media].
     * 
     * @return supportsLatestMedia
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_LATEST_MEDIA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportsLatestMedia() {
        return supportsLatestMedia;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_LATEST_MEDIA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsLatestMedia(@org.eclipse.jdt.annotation.NonNull Boolean supportsLatestMedia) {
        this.supportsLatestMedia = supportsLatestMedia;
    }

    public ChannelFeatures canFilter(@org.eclipse.jdt.annotation.NonNull Boolean canFilter) {
        this.canFilter = canFilter;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can filter.
     * 
     * @return canFilter
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAN_FILTER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCanFilter() {
        return canFilter;
    }

    @JsonProperty(JSON_PROPERTY_CAN_FILTER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanFilter(@org.eclipse.jdt.annotation.NonNull Boolean canFilter) {
        this.canFilter = canFilter;
    }

    public ChannelFeatures supportsContentDownloading(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsContentDownloading) {
        this.supportsContentDownloading = supportsContentDownloading;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [supports content downloading].
     * 
     * @return supportsContentDownloading
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportsContentDownloading() {
        return supportsContentDownloading;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsContentDownloading(@org.eclipse.jdt.annotation.NonNull Boolean supportsContentDownloading) {
        this.supportsContentDownloading = supportsContentDownloading;
    }

    /**
     * Return true if this ChannelFeatures object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelFeatures channelFeatures = (ChannelFeatures) o;
        return Objects.equals(this.name, channelFeatures.name) && Objects.equals(this.id, channelFeatures.id)
                && Objects.equals(this.canSearch, channelFeatures.canSearch)
                && Objects.equals(this.mediaTypes, channelFeatures.mediaTypes)
                && Objects.equals(this.contentTypes, channelFeatures.contentTypes)
                && Objects.equals(this.maxPageSize, channelFeatures.maxPageSize)
                && Objects.equals(this.autoRefreshLevels, channelFeatures.autoRefreshLevels)
                && Objects.equals(this.defaultSortFields, channelFeatures.defaultSortFields)
                && Objects.equals(this.supportsSortOrderToggle, channelFeatures.supportsSortOrderToggle)
                && Objects.equals(this.supportsLatestMedia, channelFeatures.supportsLatestMedia)
                && Objects.equals(this.canFilter, channelFeatures.canFilter)
                && Objects.equals(this.supportsContentDownloading, channelFeatures.supportsContentDownloading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, canSearch, mediaTypes, contentTypes, maxPageSize, autoRefreshLevels,
                defaultSortFields, supportsSortOrderToggle, supportsLatestMedia, canFilter, supportsContentDownloading);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChannelFeatures {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    canSearch: ").append(toIndentedString(canSearch)).append("\n");
        sb.append("    mediaTypes: ").append(toIndentedString(mediaTypes)).append("\n");
        sb.append("    contentTypes: ").append(toIndentedString(contentTypes)).append("\n");
        sb.append("    maxPageSize: ").append(toIndentedString(maxPageSize)).append("\n");
        sb.append("    autoRefreshLevels: ").append(toIndentedString(autoRefreshLevels)).append("\n");
        sb.append("    defaultSortFields: ").append(toIndentedString(defaultSortFields)).append("\n");
        sb.append("    supportsSortOrderToggle: ").append(toIndentedString(supportsSortOrderToggle)).append("\n");
        sb.append("    supportsLatestMedia: ").append(toIndentedString(supportsLatestMedia)).append("\n");
        sb.append("    canFilter: ").append(toIndentedString(canFilter)).append("\n");
        sb.append("    supportsContentDownloading: ").append(toIndentedString(supportsContentDownloading)).append("\n");
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
