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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class SeriesTimerInfoDto.
 */
@JsonPropertyOrder({ SeriesTimerInfoDto.JSON_PROPERTY_ID, SeriesTimerInfoDto.JSON_PROPERTY_TYPE,
        SeriesTimerInfoDto.JSON_PROPERTY_SERVER_ID, SeriesTimerInfoDto.JSON_PROPERTY_EXTERNAL_ID,
        SeriesTimerInfoDto.JSON_PROPERTY_CHANNEL_ID, SeriesTimerInfoDto.JSON_PROPERTY_EXTERNAL_CHANNEL_ID,
        SeriesTimerInfoDto.JSON_PROPERTY_CHANNEL_NAME, SeriesTimerInfoDto.JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG,
        SeriesTimerInfoDto.JSON_PROPERTY_PROGRAM_ID, SeriesTimerInfoDto.JSON_PROPERTY_EXTERNAL_PROGRAM_ID,
        SeriesTimerInfoDto.JSON_PROPERTY_NAME, SeriesTimerInfoDto.JSON_PROPERTY_OVERVIEW,
        SeriesTimerInfoDto.JSON_PROPERTY_START_DATE, SeriesTimerInfoDto.JSON_PROPERTY_END_DATE,
        SeriesTimerInfoDto.JSON_PROPERTY_SERVICE_NAME, SeriesTimerInfoDto.JSON_PROPERTY_PRIORITY,
        SeriesTimerInfoDto.JSON_PROPERTY_PRE_PADDING_SECONDS, SeriesTimerInfoDto.JSON_PROPERTY_POST_PADDING_SECONDS,
        SeriesTimerInfoDto.JSON_PROPERTY_IS_PRE_PADDING_REQUIRED,
        SeriesTimerInfoDto.JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID,
        SeriesTimerInfoDto.JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS,
        SeriesTimerInfoDto.JSON_PROPERTY_IS_POST_PADDING_REQUIRED, SeriesTimerInfoDto.JSON_PROPERTY_KEEP_UNTIL,
        SeriesTimerInfoDto.JSON_PROPERTY_RECORD_ANY_TIME, SeriesTimerInfoDto.JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY,
        SeriesTimerInfoDto.JSON_PROPERTY_RECORD_ANY_CHANNEL, SeriesTimerInfoDto.JSON_PROPERTY_KEEP_UP_TO,
        SeriesTimerInfoDto.JSON_PROPERTY_RECORD_NEW_ONLY, SeriesTimerInfoDto.JSON_PROPERTY_DAYS,
        SeriesTimerInfoDto.JSON_PROPERTY_DAY_PATTERN, SeriesTimerInfoDto.JSON_PROPERTY_IMAGE_TAGS,
        SeriesTimerInfoDto.JSON_PROPERTY_PARENT_THUMB_ITEM_ID, SeriesTimerInfoDto.JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG,
        SeriesTimerInfoDto.JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID,
        SeriesTimerInfoDto.JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SeriesTimerInfoDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private String id;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private String type;

    public static final String JSON_PROPERTY_SERVER_ID = "ServerId";
    @org.eclipse.jdt.annotation.Nullable
    private String serverId;

    public static final String JSON_PROPERTY_EXTERNAL_ID = "ExternalId";
    @org.eclipse.jdt.annotation.Nullable
    private String externalId;

    public static final String JSON_PROPERTY_CHANNEL_ID = "ChannelId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID channelId;

    public static final String JSON_PROPERTY_EXTERNAL_CHANNEL_ID = "ExternalChannelId";
    @org.eclipse.jdt.annotation.Nullable
    private String externalChannelId;

    public static final String JSON_PROPERTY_CHANNEL_NAME = "ChannelName";
    @org.eclipse.jdt.annotation.Nullable
    private String channelName;

    public static final String JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG = "ChannelPrimaryImageTag";
    @org.eclipse.jdt.annotation.Nullable
    private String channelPrimaryImageTag;

    public static final String JSON_PROPERTY_PROGRAM_ID = "ProgramId";
    @org.eclipse.jdt.annotation.Nullable
    private String programId;

    public static final String JSON_PROPERTY_EXTERNAL_PROGRAM_ID = "ExternalProgramId";
    @org.eclipse.jdt.annotation.Nullable
    private String externalProgramId;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_OVERVIEW = "Overview";
    @org.eclipse.jdt.annotation.Nullable
    private String overview;

    public static final String JSON_PROPERTY_START_DATE = "StartDate";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime startDate;

    public static final String JSON_PROPERTY_END_DATE = "EndDate";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime endDate;

    public static final String JSON_PROPERTY_SERVICE_NAME = "ServiceName";
    @org.eclipse.jdt.annotation.Nullable
    private String serviceName;

    public static final String JSON_PROPERTY_PRIORITY = "Priority";
    @org.eclipse.jdt.annotation.Nullable
    private Integer priority;

    public static final String JSON_PROPERTY_PRE_PADDING_SECONDS = "PrePaddingSeconds";
    @org.eclipse.jdt.annotation.Nullable
    private Integer prePaddingSeconds;

    public static final String JSON_PROPERTY_POST_PADDING_SECONDS = "PostPaddingSeconds";
    @org.eclipse.jdt.annotation.Nullable
    private Integer postPaddingSeconds;

    public static final String JSON_PROPERTY_IS_PRE_PADDING_REQUIRED = "IsPrePaddingRequired";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isPrePaddingRequired;

    public static final String JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID = "ParentBackdropItemId";
    @org.eclipse.jdt.annotation.Nullable
    private String parentBackdropItemId;

    public static final String JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS = "ParentBackdropImageTags";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> parentBackdropImageTags;

    public static final String JSON_PROPERTY_IS_POST_PADDING_REQUIRED = "IsPostPaddingRequired";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isPostPaddingRequired;

    public static final String JSON_PROPERTY_KEEP_UNTIL = "KeepUntil";
    @org.eclipse.jdt.annotation.Nullable
    private KeepUntil keepUntil;

    public static final String JSON_PROPERTY_RECORD_ANY_TIME = "RecordAnyTime";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean recordAnyTime;

    public static final String JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY = "SkipEpisodesInLibrary";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean skipEpisodesInLibrary;

    public static final String JSON_PROPERTY_RECORD_ANY_CHANNEL = "RecordAnyChannel";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean recordAnyChannel;

    public static final String JSON_PROPERTY_KEEP_UP_TO = "KeepUpTo";
    @org.eclipse.jdt.annotation.Nullable
    private Integer keepUpTo;

    public static final String JSON_PROPERTY_RECORD_NEW_ONLY = "RecordNewOnly";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean recordNewOnly;

    public static final String JSON_PROPERTY_DAYS = "Days";
    @org.eclipse.jdt.annotation.Nullable
    private List<DayOfWeek> days;

    public static final String JSON_PROPERTY_DAY_PATTERN = "DayPattern";
    @org.eclipse.jdt.annotation.Nullable
    private DayPattern dayPattern;

    public static final String JSON_PROPERTY_IMAGE_TAGS = "ImageTags";
    @org.eclipse.jdt.annotation.Nullable
    private Map<String, String> imageTags;

    public static final String JSON_PROPERTY_PARENT_THUMB_ITEM_ID = "ParentThumbItemId";
    @org.eclipse.jdt.annotation.Nullable
    private String parentThumbItemId;

    public static final String JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG = "ParentThumbImageTag";
    @org.eclipse.jdt.annotation.Nullable
    private String parentThumbImageTag;

    public static final String JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID = "ParentPrimaryImageItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID parentPrimaryImageItemId;

    public static final String JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG = "ParentPrimaryImageTag";
    @org.eclipse.jdt.annotation.Nullable
    private String parentPrimaryImageTag;

    public SeriesTimerInfoDto() {
    }

    public SeriesTimerInfoDto id(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the Id of the recording.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
    }

    public SeriesTimerInfoDto type(@org.eclipse.jdt.annotation.Nullable String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable String type) {
        this.type = type;
    }

    public SeriesTimerInfoDto serverId(@org.eclipse.jdt.annotation.Nullable String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Gets or sets the server identifier.
     * 
     * @return serverId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerId() {
        return serverId;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerId(@org.eclipse.jdt.annotation.Nullable String serverId) {
        this.serverId = serverId;
    }

    public SeriesTimerInfoDto externalId(@org.eclipse.jdt.annotation.Nullable String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Gets or sets the external identifier.
     * 
     * @return externalId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalId() {
        return externalId;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalId(@org.eclipse.jdt.annotation.Nullable String externalId) {
        this.externalId = externalId;
    }

    public SeriesTimerInfoDto channelId(@org.eclipse.jdt.annotation.Nullable UUID channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Gets or sets the channel id of the recording.
     * 
     * @return channelId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getChannelId() {
        return channelId;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelId(@org.eclipse.jdt.annotation.Nullable UUID channelId) {
        this.channelId = channelId;
    }

    public SeriesTimerInfoDto externalChannelId(@org.eclipse.jdt.annotation.Nullable String externalChannelId) {
        this.externalChannelId = externalChannelId;
        return this;
    }

    /**
     * Gets or sets the external channel identifier.
     * 
     * @return externalChannelId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalChannelId() {
        return externalChannelId;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalChannelId(@org.eclipse.jdt.annotation.Nullable String externalChannelId) {
        this.externalChannelId = externalChannelId;
    }

    public SeriesTimerInfoDto channelName(@org.eclipse.jdt.annotation.Nullable String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Gets or sets the channel name of the recording.
     * 
     * @return channelName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelName(@org.eclipse.jdt.annotation.Nullable String channelName) {
        this.channelName = channelName;
    }

    public SeriesTimerInfoDto channelPrimaryImageTag(
            @org.eclipse.jdt.annotation.Nullable String channelPrimaryImageTag) {
        this.channelPrimaryImageTag = channelPrimaryImageTag;
        return this;
    }

    /**
     * Get channelPrimaryImageTag
     * 
     * @return channelPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelPrimaryImageTag() {
        return channelPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelPrimaryImageTag(@org.eclipse.jdt.annotation.Nullable String channelPrimaryImageTag) {
        this.channelPrimaryImageTag = channelPrimaryImageTag;
    }

    public SeriesTimerInfoDto programId(@org.eclipse.jdt.annotation.Nullable String programId) {
        this.programId = programId;
        return this;
    }

    /**
     * Gets or sets the program identifier.
     * 
     * @return programId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProgramId() {
        return programId;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramId(@org.eclipse.jdt.annotation.Nullable String programId) {
        this.programId = programId;
    }

    public SeriesTimerInfoDto externalProgramId(@org.eclipse.jdt.annotation.Nullable String externalProgramId) {
        this.externalProgramId = externalProgramId;
        return this;
    }

    /**
     * Gets or sets the external program identifier.
     * 
     * @return externalProgramId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalProgramId() {
        return externalProgramId;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalProgramId(@org.eclipse.jdt.annotation.Nullable String externalProgramId) {
        this.externalProgramId = externalProgramId;
    }

    public SeriesTimerInfoDto name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of the recording.
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

    public SeriesTimerInfoDto overview(@org.eclipse.jdt.annotation.Nullable String overview) {
        this.overview = overview;
        return this;
    }

    /**
     * Gets or sets the description of the recording.
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

    public SeriesTimerInfoDto startDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Gets or sets the start date of the recording, in UTC.
     * 
     * @return startDate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getStartDate() {
        return startDate;
    }

    @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public SeriesTimerInfoDto endDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Gets or sets the end date of the recording, in UTC.
     * 
     * @return endDate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndDate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public SeriesTimerInfoDto serviceName(@org.eclipse.jdt.annotation.Nullable String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Gets or sets the name of the service.
     * 
     * @return serviceName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SERVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceName(@org.eclipse.jdt.annotation.Nullable String serviceName) {
        this.serviceName = serviceName;
    }

    public SeriesTimerInfoDto priority(@org.eclipse.jdt.annotation.Nullable Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets or sets the priority.
     * 
     * @return priority
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PRIORITY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIORITY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPriority(@org.eclipse.jdt.annotation.Nullable Integer priority) {
        this.priority = priority;
    }

    public SeriesTimerInfoDto prePaddingSeconds(@org.eclipse.jdt.annotation.Nullable Integer prePaddingSeconds) {
        this.prePaddingSeconds = prePaddingSeconds;
        return this;
    }

    /**
     * Gets or sets the pre padding seconds.
     * 
     * @return prePaddingSeconds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PRE_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPrePaddingSeconds() {
        return prePaddingSeconds;
    }

    @JsonProperty(value = JSON_PROPERTY_PRE_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrePaddingSeconds(@org.eclipse.jdt.annotation.Nullable Integer prePaddingSeconds) {
        this.prePaddingSeconds = prePaddingSeconds;
    }

    public SeriesTimerInfoDto postPaddingSeconds(@org.eclipse.jdt.annotation.Nullable Integer postPaddingSeconds) {
        this.postPaddingSeconds = postPaddingSeconds;
        return this;
    }

    /**
     * Gets or sets the post padding seconds.
     * 
     * @return postPaddingSeconds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_POST_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPostPaddingSeconds() {
        return postPaddingSeconds;
    }

    @JsonProperty(value = JSON_PROPERTY_POST_PADDING_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPostPaddingSeconds(@org.eclipse.jdt.annotation.Nullable Integer postPaddingSeconds) {
        this.postPaddingSeconds = postPaddingSeconds;
    }

    public SeriesTimerInfoDto isPrePaddingRequired(@org.eclipse.jdt.annotation.Nullable Boolean isPrePaddingRequired) {
        this.isPrePaddingRequired = isPrePaddingRequired;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is pre padding required.
     * 
     * @return isPrePaddingRequired
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_PRE_PADDING_REQUIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPrePaddingRequired() {
        return isPrePaddingRequired;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PRE_PADDING_REQUIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPrePaddingRequired(@org.eclipse.jdt.annotation.Nullable Boolean isPrePaddingRequired) {
        this.isPrePaddingRequired = isPrePaddingRequired;
    }

    public SeriesTimerInfoDto parentBackdropItemId(@org.eclipse.jdt.annotation.Nullable String parentBackdropItemId) {
        this.parentBackdropItemId = parentBackdropItemId;
        return this;
    }

    /**
     * Gets or sets the Id of the Parent that has a backdrop if the item does not have one.
     * 
     * @return parentBackdropItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentBackdropItemId() {
        return parentBackdropItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentBackdropItemId(@org.eclipse.jdt.annotation.Nullable String parentBackdropItemId) {
        this.parentBackdropItemId = parentBackdropItemId;
    }

    public SeriesTimerInfoDto parentBackdropImageTags(
            @org.eclipse.jdt.annotation.Nullable List<String> parentBackdropImageTags) {
        this.parentBackdropImageTags = parentBackdropImageTags;
        return this;
    }

    public SeriesTimerInfoDto addParentBackdropImageTagsItem(String parentBackdropImageTagsItem) {
        if (this.parentBackdropImageTags == null) {
            this.parentBackdropImageTags = new ArrayList<>();
        }
        this.parentBackdropImageTags.add(parentBackdropImageTagsItem);
        return this;
    }

    /**
     * Gets or sets the parent backdrop image tags.
     * 
     * @return parentBackdropImageTags
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getParentBackdropImageTags() {
        return parentBackdropImageTags;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentBackdropImageTags(@org.eclipse.jdt.annotation.Nullable List<String> parentBackdropImageTags) {
        this.parentBackdropImageTags = parentBackdropImageTags;
    }

    public SeriesTimerInfoDto isPostPaddingRequired(
            @org.eclipse.jdt.annotation.Nullable Boolean isPostPaddingRequired) {
        this.isPostPaddingRequired = isPostPaddingRequired;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is post padding required.
     * 
     * @return isPostPaddingRequired
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_POST_PADDING_REQUIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPostPaddingRequired() {
        return isPostPaddingRequired;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_POST_PADDING_REQUIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPostPaddingRequired(@org.eclipse.jdt.annotation.Nullable Boolean isPostPaddingRequired) {
        this.isPostPaddingRequired = isPostPaddingRequired;
    }

    public SeriesTimerInfoDto keepUntil(@org.eclipse.jdt.annotation.Nullable KeepUntil keepUntil) {
        this.keepUntil = keepUntil;
        return this;
    }

    /**
     * Get keepUntil
     * 
     * @return keepUntil
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_KEEP_UNTIL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public KeepUntil getKeepUntil() {
        return keepUntil;
    }

    @JsonProperty(value = JSON_PROPERTY_KEEP_UNTIL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKeepUntil(@org.eclipse.jdt.annotation.Nullable KeepUntil keepUntil) {
        this.keepUntil = keepUntil;
    }

    public SeriesTimerInfoDto recordAnyTime(@org.eclipse.jdt.annotation.Nullable Boolean recordAnyTime) {
        this.recordAnyTime = recordAnyTime;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [record any time].
     * 
     * @return recordAnyTime
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RECORD_ANY_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRecordAnyTime() {
        return recordAnyTime;
    }

    @JsonProperty(value = JSON_PROPERTY_RECORD_ANY_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordAnyTime(@org.eclipse.jdt.annotation.Nullable Boolean recordAnyTime) {
        this.recordAnyTime = recordAnyTime;
    }

    public SeriesTimerInfoDto skipEpisodesInLibrary(
            @org.eclipse.jdt.annotation.Nullable Boolean skipEpisodesInLibrary) {
        this.skipEpisodesInLibrary = skipEpisodesInLibrary;
        return this;
    }

    /**
     * Get skipEpisodesInLibrary
     * 
     * @return skipEpisodesInLibrary
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSkipEpisodesInLibrary() {
        return skipEpisodesInLibrary;
    }

    @JsonProperty(value = JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipEpisodesInLibrary(@org.eclipse.jdt.annotation.Nullable Boolean skipEpisodesInLibrary) {
        this.skipEpisodesInLibrary = skipEpisodesInLibrary;
    }

    public SeriesTimerInfoDto recordAnyChannel(@org.eclipse.jdt.annotation.Nullable Boolean recordAnyChannel) {
        this.recordAnyChannel = recordAnyChannel;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [record any channel].
     * 
     * @return recordAnyChannel
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RECORD_ANY_CHANNEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRecordAnyChannel() {
        return recordAnyChannel;
    }

    @JsonProperty(value = JSON_PROPERTY_RECORD_ANY_CHANNEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordAnyChannel(@org.eclipse.jdt.annotation.Nullable Boolean recordAnyChannel) {
        this.recordAnyChannel = recordAnyChannel;
    }

    public SeriesTimerInfoDto keepUpTo(@org.eclipse.jdt.annotation.Nullable Integer keepUpTo) {
        this.keepUpTo = keepUpTo;
        return this;
    }

    /**
     * Get keepUpTo
     * 
     * @return keepUpTo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_KEEP_UP_TO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getKeepUpTo() {
        return keepUpTo;
    }

    @JsonProperty(value = JSON_PROPERTY_KEEP_UP_TO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKeepUpTo(@org.eclipse.jdt.annotation.Nullable Integer keepUpTo) {
        this.keepUpTo = keepUpTo;
    }

    public SeriesTimerInfoDto recordNewOnly(@org.eclipse.jdt.annotation.Nullable Boolean recordNewOnly) {
        this.recordNewOnly = recordNewOnly;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [record new only].
     * 
     * @return recordNewOnly
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RECORD_NEW_ONLY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRecordNewOnly() {
        return recordNewOnly;
    }

    @JsonProperty(value = JSON_PROPERTY_RECORD_NEW_ONLY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordNewOnly(@org.eclipse.jdt.annotation.Nullable Boolean recordNewOnly) {
        this.recordNewOnly = recordNewOnly;
    }

    public SeriesTimerInfoDto days(@org.eclipse.jdt.annotation.Nullable List<DayOfWeek> days) {
        this.days = days;
        return this;
    }

    public SeriesTimerInfoDto addDaysItem(DayOfWeek daysItem) {
        if (this.days == null) {
            this.days = new ArrayList<>();
        }
        this.days.add(daysItem);
        return this;
    }

    /**
     * Gets or sets the days.
     * 
     * @return days
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<DayOfWeek> getDays() {
        return days;
    }

    @JsonProperty(value = JSON_PROPERTY_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDays(@org.eclipse.jdt.annotation.Nullable List<DayOfWeek> days) {
        this.days = days;
    }

    public SeriesTimerInfoDto dayPattern(@org.eclipse.jdt.annotation.Nullable DayPattern dayPattern) {
        this.dayPattern = dayPattern;
        return this;
    }

    /**
     * Gets or sets the day pattern.
     * 
     * @return dayPattern
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DAY_PATTERN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DayPattern getDayPattern() {
        return dayPattern;
    }

    @JsonProperty(value = JSON_PROPERTY_DAY_PATTERN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDayPattern(@org.eclipse.jdt.annotation.Nullable DayPattern dayPattern) {
        this.dayPattern = dayPattern;
    }

    public SeriesTimerInfoDto imageTags(@org.eclipse.jdt.annotation.Nullable Map<String, String> imageTags) {
        this.imageTags = imageTags;
        return this;
    }

    public SeriesTimerInfoDto putImageTagsItem(String key, String imageTagsItem) {
        if (this.imageTags == null) {
            this.imageTags = new HashMap<>();
        }
        this.imageTags.put(key, imageTagsItem);
        return this;
    }

    /**
     * Gets or sets the image tags.
     * 
     * @return imageTags
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getImageTags() {
        return imageTags;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageTags(@org.eclipse.jdt.annotation.Nullable Map<String, String> imageTags) {
        this.imageTags = imageTags;
    }

    public SeriesTimerInfoDto parentThumbItemId(@org.eclipse.jdt.annotation.Nullable String parentThumbItemId) {
        this.parentThumbItemId = parentThumbItemId;
        return this;
    }

    /**
     * Gets or sets the parent thumb item id.
     * 
     * @return parentThumbItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentThumbItemId() {
        return parentThumbItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentThumbItemId(@org.eclipse.jdt.annotation.Nullable String parentThumbItemId) {
        this.parentThumbItemId = parentThumbItemId;
    }

    public SeriesTimerInfoDto parentThumbImageTag(@org.eclipse.jdt.annotation.Nullable String parentThumbImageTag) {
        this.parentThumbImageTag = parentThumbImageTag;
        return this;
    }

    /**
     * Gets or sets the parent thumb image tag.
     * 
     * @return parentThumbImageTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentThumbImageTag() {
        return parentThumbImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentThumbImageTag(@org.eclipse.jdt.annotation.Nullable String parentThumbImageTag) {
        this.parentThumbImageTag = parentThumbImageTag;
    }

    public SeriesTimerInfoDto parentPrimaryImageItemId(
            @org.eclipse.jdt.annotation.Nullable UUID parentPrimaryImageItemId) {
        this.parentPrimaryImageItemId = parentPrimaryImageItemId;
        return this;
    }

    /**
     * Gets or sets the parent primary image item identifier.
     * 
     * @return parentPrimaryImageItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentPrimaryImageItemId() {
        return parentPrimaryImageItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentPrimaryImageItemId(@org.eclipse.jdt.annotation.Nullable UUID parentPrimaryImageItemId) {
        this.parentPrimaryImageItemId = parentPrimaryImageItemId;
    }

    public SeriesTimerInfoDto parentPrimaryImageTag(@org.eclipse.jdt.annotation.Nullable String parentPrimaryImageTag) {
        this.parentPrimaryImageTag = parentPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the parent primary image tag.
     * 
     * @return parentPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentPrimaryImageTag() {
        return parentPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentPrimaryImageTag(@org.eclipse.jdt.annotation.Nullable String parentPrimaryImageTag) {
        this.parentPrimaryImageTag = parentPrimaryImageTag;
    }

    /**
     * Return true if this SeriesTimerInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeriesTimerInfoDto seriesTimerInfoDto = (SeriesTimerInfoDto) o;
        return Objects.equals(this.id, seriesTimerInfoDto.id) && Objects.equals(this.type, seriesTimerInfoDto.type)
                && Objects.equals(this.serverId, seriesTimerInfoDto.serverId)
                && Objects.equals(this.externalId, seriesTimerInfoDto.externalId)
                && Objects.equals(this.channelId, seriesTimerInfoDto.channelId)
                && Objects.equals(this.externalChannelId, seriesTimerInfoDto.externalChannelId)
                && Objects.equals(this.channelName, seriesTimerInfoDto.channelName)
                && Objects.equals(this.channelPrimaryImageTag, seriesTimerInfoDto.channelPrimaryImageTag)
                && Objects.equals(this.programId, seriesTimerInfoDto.programId)
                && Objects.equals(this.externalProgramId, seriesTimerInfoDto.externalProgramId)
                && Objects.equals(this.name, seriesTimerInfoDto.name)
                && Objects.equals(this.overview, seriesTimerInfoDto.overview)
                && Objects.equals(this.startDate, seriesTimerInfoDto.startDate)
                && Objects.equals(this.endDate, seriesTimerInfoDto.endDate)
                && Objects.equals(this.serviceName, seriesTimerInfoDto.serviceName)
                && Objects.equals(this.priority, seriesTimerInfoDto.priority)
                && Objects.equals(this.prePaddingSeconds, seriesTimerInfoDto.prePaddingSeconds)
                && Objects.equals(this.postPaddingSeconds, seriesTimerInfoDto.postPaddingSeconds)
                && Objects.equals(this.isPrePaddingRequired, seriesTimerInfoDto.isPrePaddingRequired)
                && Objects.equals(this.parentBackdropItemId, seriesTimerInfoDto.parentBackdropItemId)
                && Objects.equals(this.parentBackdropImageTags, seriesTimerInfoDto.parentBackdropImageTags)
                && Objects.equals(this.isPostPaddingRequired, seriesTimerInfoDto.isPostPaddingRequired)
                && Objects.equals(this.keepUntil, seriesTimerInfoDto.keepUntil)
                && Objects.equals(this.recordAnyTime, seriesTimerInfoDto.recordAnyTime)
                && Objects.equals(this.skipEpisodesInLibrary, seriesTimerInfoDto.skipEpisodesInLibrary)
                && Objects.equals(this.recordAnyChannel, seriesTimerInfoDto.recordAnyChannel)
                && Objects.equals(this.keepUpTo, seriesTimerInfoDto.keepUpTo)
                && Objects.equals(this.recordNewOnly, seriesTimerInfoDto.recordNewOnly)
                && Objects.equals(this.days, seriesTimerInfoDto.days)
                && Objects.equals(this.dayPattern, seriesTimerInfoDto.dayPattern)
                && Objects.equals(this.imageTags, seriesTimerInfoDto.imageTags)
                && Objects.equals(this.parentThumbItemId, seriesTimerInfoDto.parentThumbItemId)
                && Objects.equals(this.parentThumbImageTag, seriesTimerInfoDto.parentThumbImageTag)
                && Objects.equals(this.parentPrimaryImageItemId, seriesTimerInfoDto.parentPrimaryImageItemId)
                && Objects.equals(this.parentPrimaryImageTag, seriesTimerInfoDto.parentPrimaryImageTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, serverId, externalId, channelId, externalChannelId, channelName,
                channelPrimaryImageTag, programId, externalProgramId, name, overview, startDate, endDate, serviceName,
                priority, prePaddingSeconds, postPaddingSeconds, isPrePaddingRequired, parentBackdropItemId,
                parentBackdropImageTags, isPostPaddingRequired, keepUntil, recordAnyTime, skipEpisodesInLibrary,
                recordAnyChannel, keepUpTo, recordNewOnly, days, dayPattern, imageTags, parentThumbItemId,
                parentThumbImageTag, parentPrimaryImageItemId, parentPrimaryImageTag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SeriesTimerInfoDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    serverId: ").append(toIndentedString(serverId)).append("\n");
        sb.append("    externalId: ").append(toIndentedString(externalId)).append("\n");
        sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
        sb.append("    externalChannelId: ").append(toIndentedString(externalChannelId)).append("\n");
        sb.append("    channelName: ").append(toIndentedString(channelName)).append("\n");
        sb.append("    channelPrimaryImageTag: ").append(toIndentedString(channelPrimaryImageTag)).append("\n");
        sb.append("    programId: ").append(toIndentedString(programId)).append("\n");
        sb.append("    externalProgramId: ").append(toIndentedString(externalProgramId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    overview: ").append(toIndentedString(overview)).append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
        sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
        sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
        sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
        sb.append("    prePaddingSeconds: ").append(toIndentedString(prePaddingSeconds)).append("\n");
        sb.append("    postPaddingSeconds: ").append(toIndentedString(postPaddingSeconds)).append("\n");
        sb.append("    isPrePaddingRequired: ").append(toIndentedString(isPrePaddingRequired)).append("\n");
        sb.append("    parentBackdropItemId: ").append(toIndentedString(parentBackdropItemId)).append("\n");
        sb.append("    parentBackdropImageTags: ").append(toIndentedString(parentBackdropImageTags)).append("\n");
        sb.append("    isPostPaddingRequired: ").append(toIndentedString(isPostPaddingRequired)).append("\n");
        sb.append("    keepUntil: ").append(toIndentedString(keepUntil)).append("\n");
        sb.append("    recordAnyTime: ").append(toIndentedString(recordAnyTime)).append("\n");
        sb.append("    skipEpisodesInLibrary: ").append(toIndentedString(skipEpisodesInLibrary)).append("\n");
        sb.append("    recordAnyChannel: ").append(toIndentedString(recordAnyChannel)).append("\n");
        sb.append("    keepUpTo: ").append(toIndentedString(keepUpTo)).append("\n");
        sb.append("    recordNewOnly: ").append(toIndentedString(recordNewOnly)).append("\n");
        sb.append("    days: ").append(toIndentedString(days)).append("\n");
        sb.append("    dayPattern: ").append(toIndentedString(dayPattern)).append("\n");
        sb.append("    imageTags: ").append(toIndentedString(imageTags)).append("\n");
        sb.append("    parentThumbItemId: ").append(toIndentedString(parentThumbItemId)).append("\n");
        sb.append("    parentThumbImageTag: ").append(toIndentedString(parentThumbImageTag)).append("\n");
        sb.append("    parentPrimaryImageItemId: ").append(toIndentedString(parentPrimaryImageItemId)).append("\n");
        sb.append("    parentPrimaryImageTag: ").append(toIndentedString(parentPrimaryImageTag)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        // add `ExternalId` to the URL query string
        if (getExternalId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sExternalId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalId()))));
        }

        // add `ChannelId` to the URL query string
        if (getChannelId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelId()))));
        }

        // add `ExternalChannelId` to the URL query string
        if (getExternalChannelId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sExternalChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalChannelId()))));
        }

        // add `ChannelName` to the URL query string
        if (getChannelName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChannelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelName()))));
        }

        // add `ChannelPrimaryImageTag` to the URL query string
        if (getChannelPrimaryImageTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChannelPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelPrimaryImageTag()))));
        }

        // add `ProgramId` to the URL query string
        if (getProgramId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProgramId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramId()))));
        }

        // add `ExternalProgramId` to the URL query string
        if (getExternalProgramId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sExternalProgramId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalProgramId()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Overview` to the URL query string
        if (getOverview() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sOverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOverview()))));
        }

        // add `StartDate` to the URL query string
        if (getStartDate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartDate()))));
        }

        // add `EndDate` to the URL query string
        if (getEndDate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEndDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndDate()))));
        }

        // add `ServiceName` to the URL query string
        if (getServiceName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sServiceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServiceName()))));
        }

        // add `Priority` to the URL query string
        if (getPriority() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPriority%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPriority()))));
        }

        // add `PrePaddingSeconds` to the URL query string
        if (getPrePaddingSeconds() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPrePaddingSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrePaddingSeconds()))));
        }

        // add `PostPaddingSeconds` to the URL query string
        if (getPostPaddingSeconds() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPostPaddingSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPostPaddingSeconds()))));
        }

        // add `IsPrePaddingRequired` to the URL query string
        if (getIsPrePaddingRequired() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsPrePaddingRequired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPrePaddingRequired()))));
        }

        // add `ParentBackdropItemId` to the URL query string
        if (getParentBackdropItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sParentBackdropItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentBackdropItemId()))));
        }

        // add `ParentBackdropImageTags` to the URL query string
        if (getParentBackdropImageTags() != null) {
            for (int i = 0; i < getParentBackdropImageTags().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sParentBackdropImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getParentBackdropImageTags().get(i)))));
            }
        }

        // add `IsPostPaddingRequired` to the URL query string
        if (getIsPostPaddingRequired() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsPostPaddingRequired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPostPaddingRequired()))));
        }

        // add `KeepUntil` to the URL query string
        if (getKeepUntil() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sKeepUntil%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKeepUntil()))));
        }

        // add `RecordAnyTime` to the URL query string
        if (getRecordAnyTime() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRecordAnyTime%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecordAnyTime()))));
        }

        // add `SkipEpisodesInLibrary` to the URL query string
        if (getSkipEpisodesInLibrary() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSkipEpisodesInLibrary%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSkipEpisodesInLibrary()))));
        }

        // add `RecordAnyChannel` to the URL query string
        if (getRecordAnyChannel() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRecordAnyChannel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecordAnyChannel()))));
        }

        // add `KeepUpTo` to the URL query string
        if (getKeepUpTo() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sKeepUpTo%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKeepUpTo()))));
        }

        // add `RecordNewOnly` to the URL query string
        if (getRecordNewOnly() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRecordNewOnly%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecordNewOnly()))));
        }

        // add `Days` to the URL query string
        if (getDays() != null) {
            for (int i = 0; i < getDays().size(); i++) {
                if (getDays().get(i) != null) {
                    joiner.add(String.format(java.util.Locale.ROOT, "%sDays%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                            containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getDays().get(i)))));
                }
            }
        }

        // add `DayPattern` to the URL query string
        if (getDayPattern() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDayPattern%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDayPattern()))));
        }

        // add `ImageTags` to the URL query string
        if (getImageTags() != null) {
            for (String _key : getImageTags().keySet()) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, _key,
                                        containerSuffix),
                        getImageTags().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getImageTags().get(_key)))));
            }
        }

        // add `ParentThumbItemId` to the URL query string
        if (getParentThumbItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sParentThumbItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentThumbItemId()))));
        }

        // add `ParentThumbImageTag` to the URL query string
        if (getParentThumbImageTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sParentThumbImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentThumbImageTag()))));
        }

        // add `ParentPrimaryImageItemId` to the URL query string
        if (getParentPrimaryImageItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sParentPrimaryImageItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentPrimaryImageItemId()))));
        }

        // add `ParentPrimaryImageTag` to the URL query string
        if (getParentPrimaryImageTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sParentPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentPrimaryImageTag()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SeriesTimerInfoDto instance;

        public Builder() {
            this(new SeriesTimerInfoDto());
        }

        protected Builder(SeriesTimerInfoDto instance) {
            this.instance = instance;
        }

        public SeriesTimerInfoDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public SeriesTimerInfoDto.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public SeriesTimerInfoDto.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        public SeriesTimerInfoDto.Builder externalId(String externalId) {
            this.instance.externalId = externalId;
            return this;
        }

        public SeriesTimerInfoDto.Builder channelId(UUID channelId) {
            this.instance.channelId = channelId;
            return this;
        }

        public SeriesTimerInfoDto.Builder externalChannelId(String externalChannelId) {
            this.instance.externalChannelId = externalChannelId;
            return this;
        }

        public SeriesTimerInfoDto.Builder channelName(String channelName) {
            this.instance.channelName = channelName;
            return this;
        }

        public SeriesTimerInfoDto.Builder channelPrimaryImageTag(String channelPrimaryImageTag) {
            this.instance.channelPrimaryImageTag = channelPrimaryImageTag;
            return this;
        }

        public SeriesTimerInfoDto.Builder programId(String programId) {
            this.instance.programId = programId;
            return this;
        }

        public SeriesTimerInfoDto.Builder externalProgramId(String externalProgramId) {
            this.instance.externalProgramId = externalProgramId;
            return this;
        }

        public SeriesTimerInfoDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public SeriesTimerInfoDto.Builder overview(String overview) {
            this.instance.overview = overview;
            return this;
        }

        public SeriesTimerInfoDto.Builder startDate(OffsetDateTime startDate) {
            this.instance.startDate = startDate;
            return this;
        }

        public SeriesTimerInfoDto.Builder endDate(OffsetDateTime endDate) {
            this.instance.endDate = endDate;
            return this;
        }

        public SeriesTimerInfoDto.Builder serviceName(String serviceName) {
            this.instance.serviceName = serviceName;
            return this;
        }

        public SeriesTimerInfoDto.Builder priority(Integer priority) {
            this.instance.priority = priority;
            return this;
        }

        public SeriesTimerInfoDto.Builder prePaddingSeconds(Integer prePaddingSeconds) {
            this.instance.prePaddingSeconds = prePaddingSeconds;
            return this;
        }

        public SeriesTimerInfoDto.Builder postPaddingSeconds(Integer postPaddingSeconds) {
            this.instance.postPaddingSeconds = postPaddingSeconds;
            return this;
        }

        public SeriesTimerInfoDto.Builder isPrePaddingRequired(Boolean isPrePaddingRequired) {
            this.instance.isPrePaddingRequired = isPrePaddingRequired;
            return this;
        }

        public SeriesTimerInfoDto.Builder parentBackdropItemId(String parentBackdropItemId) {
            this.instance.parentBackdropItemId = parentBackdropItemId;
            return this;
        }

        public SeriesTimerInfoDto.Builder parentBackdropImageTags(List<String> parentBackdropImageTags) {
            this.instance.parentBackdropImageTags = parentBackdropImageTags;
            return this;
        }

        public SeriesTimerInfoDto.Builder isPostPaddingRequired(Boolean isPostPaddingRequired) {
            this.instance.isPostPaddingRequired = isPostPaddingRequired;
            return this;
        }

        public SeriesTimerInfoDto.Builder keepUntil(KeepUntil keepUntil) {
            this.instance.keepUntil = keepUntil;
            return this;
        }

        public SeriesTimerInfoDto.Builder recordAnyTime(Boolean recordAnyTime) {
            this.instance.recordAnyTime = recordAnyTime;
            return this;
        }

        public SeriesTimerInfoDto.Builder skipEpisodesInLibrary(Boolean skipEpisodesInLibrary) {
            this.instance.skipEpisodesInLibrary = skipEpisodesInLibrary;
            return this;
        }

        public SeriesTimerInfoDto.Builder recordAnyChannel(Boolean recordAnyChannel) {
            this.instance.recordAnyChannel = recordAnyChannel;
            return this;
        }

        public SeriesTimerInfoDto.Builder keepUpTo(Integer keepUpTo) {
            this.instance.keepUpTo = keepUpTo;
            return this;
        }

        public SeriesTimerInfoDto.Builder recordNewOnly(Boolean recordNewOnly) {
            this.instance.recordNewOnly = recordNewOnly;
            return this;
        }

        public SeriesTimerInfoDto.Builder days(List<DayOfWeek> days) {
            this.instance.days = days;
            return this;
        }

        public SeriesTimerInfoDto.Builder dayPattern(DayPattern dayPattern) {
            this.instance.dayPattern = dayPattern;
            return this;
        }

        public SeriesTimerInfoDto.Builder imageTags(Map<String, String> imageTags) {
            this.instance.imageTags = imageTags;
            return this;
        }

        public SeriesTimerInfoDto.Builder parentThumbItemId(String parentThumbItemId) {
            this.instance.parentThumbItemId = parentThumbItemId;
            return this;
        }

        public SeriesTimerInfoDto.Builder parentThumbImageTag(String parentThumbImageTag) {
            this.instance.parentThumbImageTag = parentThumbImageTag;
            return this;
        }

        public SeriesTimerInfoDto.Builder parentPrimaryImageItemId(UUID parentPrimaryImageItemId) {
            this.instance.parentPrimaryImageItemId = parentPrimaryImageItemId;
            return this;
        }

        public SeriesTimerInfoDto.Builder parentPrimaryImageTag(String parentPrimaryImageTag) {
            this.instance.parentPrimaryImageTag = parentPrimaryImageTag;
            return this;
        }

        /**
         * returns a built SeriesTimerInfoDto instance.
         *
         * The builder is not reusable.
         */
        public SeriesTimerInfoDto build() {
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
    public static SeriesTimerInfoDto.Builder builder() {
        return new SeriesTimerInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SeriesTimerInfoDto.Builder toBuilder() {
        return new SeriesTimerInfoDto.Builder().id(getId()).type(getType()).serverId(getServerId())
                .externalId(getExternalId()).channelId(getChannelId()).externalChannelId(getExternalChannelId())
                .channelName(getChannelName()).channelPrimaryImageTag(getChannelPrimaryImageTag())
                .programId(getProgramId()).externalProgramId(getExternalProgramId()).name(getName())
                .overview(getOverview()).startDate(getStartDate()).endDate(getEndDate()).serviceName(getServiceName())
                .priority(getPriority()).prePaddingSeconds(getPrePaddingSeconds())
                .postPaddingSeconds(getPostPaddingSeconds()).isPrePaddingRequired(getIsPrePaddingRequired())
                .parentBackdropItemId(getParentBackdropItemId()).parentBackdropImageTags(getParentBackdropImageTags())
                .isPostPaddingRequired(getIsPostPaddingRequired()).keepUntil(getKeepUntil())
                .recordAnyTime(getRecordAnyTime()).skipEpisodesInLibrary(getSkipEpisodesInLibrary())
                .recordAnyChannel(getRecordAnyChannel()).keepUpTo(getKeepUpTo()).recordNewOnly(getRecordNewOnly())
                .days(getDays()).dayPattern(getDayPattern()).imageTags(getImageTags())
                .parentThumbItemId(getParentThumbItemId()).parentThumbImageTag(getParentThumbImageTag())
                .parentPrimaryImageItemId(getParentPrimaryImageItemId())
                .parentPrimaryImageTag(getParentPrimaryImageTag());
    }
}
