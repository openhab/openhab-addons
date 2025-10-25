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
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * TimerInfoDto
 */
@JsonPropertyOrder({ TimerInfoDto.JSON_PROPERTY_ID, TimerInfoDto.JSON_PROPERTY_TYPE,
        TimerInfoDto.JSON_PROPERTY_SERVER_ID, TimerInfoDto.JSON_PROPERTY_EXTERNAL_ID,
        TimerInfoDto.JSON_PROPERTY_CHANNEL_ID, TimerInfoDto.JSON_PROPERTY_EXTERNAL_CHANNEL_ID,
        TimerInfoDto.JSON_PROPERTY_CHANNEL_NAME, TimerInfoDto.JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG,
        TimerInfoDto.JSON_PROPERTY_PROGRAM_ID, TimerInfoDto.JSON_PROPERTY_EXTERNAL_PROGRAM_ID,
        TimerInfoDto.JSON_PROPERTY_NAME, TimerInfoDto.JSON_PROPERTY_OVERVIEW, TimerInfoDto.JSON_PROPERTY_START_DATE,
        TimerInfoDto.JSON_PROPERTY_END_DATE, TimerInfoDto.JSON_PROPERTY_SERVICE_NAME,
        TimerInfoDto.JSON_PROPERTY_PRIORITY, TimerInfoDto.JSON_PROPERTY_PRE_PADDING_SECONDS,
        TimerInfoDto.JSON_PROPERTY_POST_PADDING_SECONDS, TimerInfoDto.JSON_PROPERTY_IS_PRE_PADDING_REQUIRED,
        TimerInfoDto.JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID, TimerInfoDto.JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS,
        TimerInfoDto.JSON_PROPERTY_IS_POST_PADDING_REQUIRED, TimerInfoDto.JSON_PROPERTY_KEEP_UNTIL,
        TimerInfoDto.JSON_PROPERTY_STATUS, TimerInfoDto.JSON_PROPERTY_SERIES_TIMER_ID,
        TimerInfoDto.JSON_PROPERTY_EXTERNAL_SERIES_TIMER_ID, TimerInfoDto.JSON_PROPERTY_RUN_TIME_TICKS,
        TimerInfoDto.JSON_PROPERTY_PROGRAM_INFO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TimerInfoDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_SERVER_ID = "ServerId";
    @org.eclipse.jdt.annotation.NonNull
    private String serverId;

    public static final String JSON_PROPERTY_EXTERNAL_ID = "ExternalId";
    @org.eclipse.jdt.annotation.NonNull
    private String externalId;

    public static final String JSON_PROPERTY_CHANNEL_ID = "ChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID channelId;

    public static final String JSON_PROPERTY_EXTERNAL_CHANNEL_ID = "ExternalChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private String externalChannelId;

    public static final String JSON_PROPERTY_CHANNEL_NAME = "ChannelName";
    @org.eclipse.jdt.annotation.NonNull
    private String channelName;

    public static final String JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG = "ChannelPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String channelPrimaryImageTag;

    public static final String JSON_PROPERTY_PROGRAM_ID = "ProgramId";
    @org.eclipse.jdt.annotation.NonNull
    private String programId;

    public static final String JSON_PROPERTY_EXTERNAL_PROGRAM_ID = "ExternalProgramId";
    @org.eclipse.jdt.annotation.NonNull
    private String externalProgramId;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_OVERVIEW = "Overview";
    @org.eclipse.jdt.annotation.NonNull
    private String overview;

    public static final String JSON_PROPERTY_START_DATE = "StartDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime startDate;

    public static final String JSON_PROPERTY_END_DATE = "EndDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime endDate;

    public static final String JSON_PROPERTY_SERVICE_NAME = "ServiceName";
    @org.eclipse.jdt.annotation.NonNull
    private String serviceName;

    public static final String JSON_PROPERTY_PRIORITY = "Priority";
    @org.eclipse.jdt.annotation.NonNull
    private Integer priority;

    public static final String JSON_PROPERTY_PRE_PADDING_SECONDS = "PrePaddingSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer prePaddingSeconds;

    public static final String JSON_PROPERTY_POST_PADDING_SECONDS = "PostPaddingSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer postPaddingSeconds;

    public static final String JSON_PROPERTY_IS_PRE_PADDING_REQUIRED = "IsPrePaddingRequired";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPrePaddingRequired;

    public static final String JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID = "ParentBackdropItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String parentBackdropItemId;

    public static final String JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS = "ParentBackdropImageTags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> parentBackdropImageTags;

    public static final String JSON_PROPERTY_IS_POST_PADDING_REQUIRED = "IsPostPaddingRequired";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPostPaddingRequired;

    public static final String JSON_PROPERTY_KEEP_UNTIL = "KeepUntil";
    @org.eclipse.jdt.annotation.NonNull
    private KeepUntil keepUntil;

    public static final String JSON_PROPERTY_STATUS = "Status";
    @org.eclipse.jdt.annotation.NonNull
    private RecordingStatus status;

    public static final String JSON_PROPERTY_SERIES_TIMER_ID = "SeriesTimerId";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesTimerId;

    public static final String JSON_PROPERTY_EXTERNAL_SERIES_TIMER_ID = "ExternalSeriesTimerId";
    @org.eclipse.jdt.annotation.NonNull
    private String externalSeriesTimerId;

    public static final String JSON_PROPERTY_RUN_TIME_TICKS = "RunTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long runTimeTicks;

    public static final String JSON_PROPERTY_PROGRAM_INFO = "ProgramInfo";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto programInfo;

    public TimerInfoDto() {
    }

    public TimerInfoDto id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the Id of the recording.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public TimerInfoDto type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public TimerInfoDto serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Gets or sets the server identifier.
     * 
     * @return serverId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERVER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerId() {
        return serverId;
    }

    @JsonProperty(JSON_PROPERTY_SERVER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
    }

    public TimerInfoDto externalId(@org.eclipse.jdt.annotation.NonNull String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Gets or sets the external identifier.
     * 
     * @return externalId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EXTERNAL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalId() {
        return externalId;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalId(@org.eclipse.jdt.annotation.NonNull String externalId) {
        this.externalId = externalId;
    }

    public TimerInfoDto channelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Gets or sets the channel id of the recording.
     * 
     * @return channelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHANNEL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getChannelId() {
        return channelId;
    }

    @JsonProperty(JSON_PROPERTY_CHANNEL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
        this.channelId = channelId;
    }

    public TimerInfoDto externalChannelId(@org.eclipse.jdt.annotation.NonNull String externalChannelId) {
        this.externalChannelId = externalChannelId;
        return this;
    }

    /**
     * Gets or sets the external channel identifier.
     * 
     * @return externalChannelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EXTERNAL_CHANNEL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalChannelId() {
        return externalChannelId;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_CHANNEL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalChannelId(@org.eclipse.jdt.annotation.NonNull String externalChannelId) {
        this.externalChannelId = externalChannelId;
    }

    public TimerInfoDto channelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Gets or sets the channel name of the recording.
     * 
     * @return channelName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHANNEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty(JSON_PROPERTY_CHANNEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
        this.channelName = channelName;
    }

    public TimerInfoDto channelPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String channelPrimaryImageTag) {
        this.channelPrimaryImageTag = channelPrimaryImageTag;
        return this;
    }

    /**
     * Get channelPrimaryImageTag
     * 
     * @return channelPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelPrimaryImageTag() {
        return channelPrimaryImageTag;
    }

    @JsonProperty(JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String channelPrimaryImageTag) {
        this.channelPrimaryImageTag = channelPrimaryImageTag;
    }

    public TimerInfoDto programId(@org.eclipse.jdt.annotation.NonNull String programId) {
        this.programId = programId;
        return this;
    }

    /**
     * Gets or sets the program identifier.
     * 
     * @return programId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROGRAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProgramId() {
        return programId;
    }

    @JsonProperty(JSON_PROPERTY_PROGRAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramId(@org.eclipse.jdt.annotation.NonNull String programId) {
        this.programId = programId;
    }

    public TimerInfoDto externalProgramId(@org.eclipse.jdt.annotation.NonNull String externalProgramId) {
        this.externalProgramId = externalProgramId;
        return this;
    }

    /**
     * Gets or sets the external program identifier.
     * 
     * @return externalProgramId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EXTERNAL_PROGRAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalProgramId() {
        return externalProgramId;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_PROGRAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalProgramId(@org.eclipse.jdt.annotation.NonNull String externalProgramId) {
        this.externalProgramId = externalProgramId;
    }

    public TimerInfoDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of the recording.
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

    public TimerInfoDto overview(@org.eclipse.jdt.annotation.NonNull String overview) {
        this.overview = overview;
        return this;
    }

    /**
     * Gets or sets the description of the recording.
     * 
     * @return overview
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_OVERVIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOverview() {
        return overview;
    }

    @JsonProperty(JSON_PROPERTY_OVERVIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOverview(@org.eclipse.jdt.annotation.NonNull String overview) {
        this.overview = overview;
    }

    public TimerInfoDto startDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Gets or sets the start date of the recording, in UTC.
     * 
     * @return startDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getStartDate() {
        return startDate;
    }

    @JsonProperty(JSON_PROPERTY_START_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public TimerInfoDto endDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Gets or sets the end date of the recording, in UTC.
     * 
     * @return endDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    @JsonProperty(JSON_PROPERTY_END_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public TimerInfoDto serviceName(@org.eclipse.jdt.annotation.NonNull String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Gets or sets the name of the service.
     * 
     * @return serviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERVICE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty(JSON_PROPERTY_SERVICE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServiceName(@org.eclipse.jdt.annotation.NonNull String serviceName) {
        this.serviceName = serviceName;
    }

    public TimerInfoDto priority(@org.eclipse.jdt.annotation.NonNull Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets or sets the priority.
     * 
     * @return priority
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty(JSON_PROPERTY_PRIORITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPriority(@org.eclipse.jdt.annotation.NonNull Integer priority) {
        this.priority = priority;
    }

    public TimerInfoDto prePaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer prePaddingSeconds) {
        this.prePaddingSeconds = prePaddingSeconds;
        return this;
    }

    /**
     * Gets or sets the pre padding seconds.
     * 
     * @return prePaddingSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PRE_PADDING_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPrePaddingSeconds() {
        return prePaddingSeconds;
    }

    @JsonProperty(JSON_PROPERTY_PRE_PADDING_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrePaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer prePaddingSeconds) {
        this.prePaddingSeconds = prePaddingSeconds;
    }

    public TimerInfoDto postPaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer postPaddingSeconds) {
        this.postPaddingSeconds = postPaddingSeconds;
        return this;
    }

    /**
     * Gets or sets the post padding seconds.
     * 
     * @return postPaddingSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_POST_PADDING_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPostPaddingSeconds() {
        return postPaddingSeconds;
    }

    @JsonProperty(JSON_PROPERTY_POST_PADDING_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPostPaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer postPaddingSeconds) {
        this.postPaddingSeconds = postPaddingSeconds;
    }

    public TimerInfoDto isPrePaddingRequired(@org.eclipse.jdt.annotation.NonNull Boolean isPrePaddingRequired) {
        this.isPrePaddingRequired = isPrePaddingRequired;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is pre padding required.
     * 
     * @return isPrePaddingRequired
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_PRE_PADDING_REQUIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPrePaddingRequired() {
        return isPrePaddingRequired;
    }

    @JsonProperty(JSON_PROPERTY_IS_PRE_PADDING_REQUIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPrePaddingRequired(@org.eclipse.jdt.annotation.NonNull Boolean isPrePaddingRequired) {
        this.isPrePaddingRequired = isPrePaddingRequired;
    }

    public TimerInfoDto parentBackdropItemId(@org.eclipse.jdt.annotation.NonNull String parentBackdropItemId) {
        this.parentBackdropItemId = parentBackdropItemId;
        return this;
    }

    /**
     * Gets or sets the Id of the Parent that has a backdrop if the item does not have one.
     * 
     * @return parentBackdropItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentBackdropItemId() {
        return parentBackdropItemId;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentBackdropItemId(@org.eclipse.jdt.annotation.NonNull String parentBackdropItemId) {
        this.parentBackdropItemId = parentBackdropItemId;
    }

    public TimerInfoDto parentBackdropImageTags(
            @org.eclipse.jdt.annotation.NonNull List<String> parentBackdropImageTags) {
        this.parentBackdropImageTags = parentBackdropImageTags;
        return this;
    }

    public TimerInfoDto addParentBackdropImageTagsItem(String parentBackdropImageTagsItem) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getParentBackdropImageTags() {
        return parentBackdropImageTags;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentBackdropImageTags(@org.eclipse.jdt.annotation.NonNull List<String> parentBackdropImageTags) {
        this.parentBackdropImageTags = parentBackdropImageTags;
    }

    public TimerInfoDto isPostPaddingRequired(@org.eclipse.jdt.annotation.NonNull Boolean isPostPaddingRequired) {
        this.isPostPaddingRequired = isPostPaddingRequired;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is post padding required.
     * 
     * @return isPostPaddingRequired
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_POST_PADDING_REQUIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPostPaddingRequired() {
        return isPostPaddingRequired;
    }

    @JsonProperty(JSON_PROPERTY_IS_POST_PADDING_REQUIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPostPaddingRequired(@org.eclipse.jdt.annotation.NonNull Boolean isPostPaddingRequired) {
        this.isPostPaddingRequired = isPostPaddingRequired;
    }

    public TimerInfoDto keepUntil(@org.eclipse.jdt.annotation.NonNull KeepUntil keepUntil) {
        this.keepUntil = keepUntil;
        return this;
    }

    /**
     * Get keepUntil
     * 
     * @return keepUntil
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_KEEP_UNTIL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public KeepUntil getKeepUntil() {
        return keepUntil;
    }

    @JsonProperty(JSON_PROPERTY_KEEP_UNTIL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKeepUntil(@org.eclipse.jdt.annotation.NonNull KeepUntil keepUntil) {
        this.keepUntil = keepUntil;
    }

    public TimerInfoDto status(@org.eclipse.jdt.annotation.NonNull RecordingStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets or sets the status.
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public RecordingStatus getStatus() {
        return status;
    }

    @JsonProperty(JSON_PROPERTY_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull RecordingStatus status) {
        this.status = status;
    }

    public TimerInfoDto seriesTimerId(@org.eclipse.jdt.annotation.NonNull String seriesTimerId) {
        this.seriesTimerId = seriesTimerId;
        return this;
    }

    /**
     * Gets or sets the series timer identifier.
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

    public TimerInfoDto externalSeriesTimerId(@org.eclipse.jdt.annotation.NonNull String externalSeriesTimerId) {
        this.externalSeriesTimerId = externalSeriesTimerId;
        return this;
    }

    /**
     * Gets or sets the external series timer identifier.
     * 
     * @return externalSeriesTimerId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EXTERNAL_SERIES_TIMER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getExternalSeriesTimerId() {
        return externalSeriesTimerId;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_SERIES_TIMER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalSeriesTimerId(@org.eclipse.jdt.annotation.NonNull String externalSeriesTimerId) {
        this.externalSeriesTimerId = externalSeriesTimerId;
    }

    public TimerInfoDto runTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
        return this;
    }

    /**
     * Gets or sets the run time ticks.
     * 
     * @return runTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RUN_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getRunTimeTicks() {
        return runTimeTicks;
    }

    @JsonProperty(JSON_PROPERTY_RUN_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRunTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
    }

    public TimerInfoDto programInfo(@org.eclipse.jdt.annotation.NonNull BaseItemDto programInfo) {
        this.programInfo = programInfo;
        return this;
    }

    /**
     * Gets or sets the program information.
     * 
     * @return programInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROGRAM_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getProgramInfo() {
        return programInfo;
    }

    @JsonProperty(JSON_PROPERTY_PROGRAM_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramInfo(@org.eclipse.jdt.annotation.NonNull BaseItemDto programInfo) {
        this.programInfo = programInfo;
    }

    /**
     * Return true if this TimerInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimerInfoDto timerInfoDto = (TimerInfoDto) o;
        return Objects.equals(this.id, timerInfoDto.id) && Objects.equals(this.type, timerInfoDto.type)
                && Objects.equals(this.serverId, timerInfoDto.serverId)
                && Objects.equals(this.externalId, timerInfoDto.externalId)
                && Objects.equals(this.channelId, timerInfoDto.channelId)
                && Objects.equals(this.externalChannelId, timerInfoDto.externalChannelId)
                && Objects.equals(this.channelName, timerInfoDto.channelName)
                && Objects.equals(this.channelPrimaryImageTag, timerInfoDto.channelPrimaryImageTag)
                && Objects.equals(this.programId, timerInfoDto.programId)
                && Objects.equals(this.externalProgramId, timerInfoDto.externalProgramId)
                && Objects.equals(this.name, timerInfoDto.name) && Objects.equals(this.overview, timerInfoDto.overview)
                && Objects.equals(this.startDate, timerInfoDto.startDate)
                && Objects.equals(this.endDate, timerInfoDto.endDate)
                && Objects.equals(this.serviceName, timerInfoDto.serviceName)
                && Objects.equals(this.priority, timerInfoDto.priority)
                && Objects.equals(this.prePaddingSeconds, timerInfoDto.prePaddingSeconds)
                && Objects.equals(this.postPaddingSeconds, timerInfoDto.postPaddingSeconds)
                && Objects.equals(this.isPrePaddingRequired, timerInfoDto.isPrePaddingRequired)
                && Objects.equals(this.parentBackdropItemId, timerInfoDto.parentBackdropItemId)
                && Objects.equals(this.parentBackdropImageTags, timerInfoDto.parentBackdropImageTags)
                && Objects.equals(this.isPostPaddingRequired, timerInfoDto.isPostPaddingRequired)
                && Objects.equals(this.keepUntil, timerInfoDto.keepUntil)
                && Objects.equals(this.status, timerInfoDto.status)
                && Objects.equals(this.seriesTimerId, timerInfoDto.seriesTimerId)
                && Objects.equals(this.externalSeriesTimerId, timerInfoDto.externalSeriesTimerId)
                && Objects.equals(this.runTimeTicks, timerInfoDto.runTimeTicks)
                && Objects.equals(this.programInfo, timerInfoDto.programInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, serverId, externalId, channelId, externalChannelId, channelName,
                channelPrimaryImageTag, programId, externalProgramId, name, overview, startDate, endDate, serviceName,
                priority, prePaddingSeconds, postPaddingSeconds, isPrePaddingRequired, parentBackdropItemId,
                parentBackdropImageTags, isPostPaddingRequired, keepUntil, status, seriesTimerId, externalSeriesTimerId,
                runTimeTicks, programInfo);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TimerInfoDto {\n");
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
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    seriesTimerId: ").append(toIndentedString(seriesTimerId)).append("\n");
        sb.append("    externalSeriesTimerId: ").append(toIndentedString(externalSeriesTimerId)).append("\n");
        sb.append("    runTimeTicks: ").append(toIndentedString(runTimeTicks)).append("\n");
        sb.append("    programInfo: ").append(toIndentedString(programInfo)).append("\n");
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
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format("%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        // add `ExternalId` to the URL query string
        if (getExternalId() != null) {
            joiner.add(String.format("%sExternalId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalId()))));
        }

        // add `ChannelId` to the URL query string
        if (getChannelId() != null) {
            joiner.add(String.format("%sChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelId()))));
        }

        // add `ExternalChannelId` to the URL query string
        if (getExternalChannelId() != null) {
            joiner.add(String.format("%sExternalChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalChannelId()))));
        }

        // add `ChannelName` to the URL query string
        if (getChannelName() != null) {
            joiner.add(String.format("%sChannelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelName()))));
        }

        // add `ChannelPrimaryImageTag` to the URL query string
        if (getChannelPrimaryImageTag() != null) {
            joiner.add(String.format("%sChannelPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelPrimaryImageTag()))));
        }

        // add `ProgramId` to the URL query string
        if (getProgramId() != null) {
            joiner.add(String.format("%sProgramId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramId()))));
        }

        // add `ExternalProgramId` to the URL query string
        if (getExternalProgramId() != null) {
            joiner.add(String.format("%sExternalProgramId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalProgramId()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format("%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Overview` to the URL query string
        if (getOverview() != null) {
            joiner.add(String.format("%sOverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOverview()))));
        }

        // add `StartDate` to the URL query string
        if (getStartDate() != null) {
            joiner.add(String.format("%sStartDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartDate()))));
        }

        // add `EndDate` to the URL query string
        if (getEndDate() != null) {
            joiner.add(String.format("%sEndDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndDate()))));
        }

        // add `ServiceName` to the URL query string
        if (getServiceName() != null) {
            joiner.add(String.format("%sServiceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServiceName()))));
        }

        // add `Priority` to the URL query string
        if (getPriority() != null) {
            joiner.add(String.format("%sPriority%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPriority()))));
        }

        // add `PrePaddingSeconds` to the URL query string
        if (getPrePaddingSeconds() != null) {
            joiner.add(String.format("%sPrePaddingSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrePaddingSeconds()))));
        }

        // add `PostPaddingSeconds` to the URL query string
        if (getPostPaddingSeconds() != null) {
            joiner.add(String.format("%sPostPaddingSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPostPaddingSeconds()))));
        }

        // add `IsPrePaddingRequired` to the URL query string
        if (getIsPrePaddingRequired() != null) {
            joiner.add(String.format("%sIsPrePaddingRequired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPrePaddingRequired()))));
        }

        // add `ParentBackdropItemId` to the URL query string
        if (getParentBackdropItemId() != null) {
            joiner.add(String.format("%sParentBackdropItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentBackdropItemId()))));
        }

        // add `ParentBackdropImageTags` to the URL query string
        if (getParentBackdropImageTags() != null) {
            for (int i = 0; i < getParentBackdropImageTags().size(); i++) {
                joiner.add(String.format("%sParentBackdropImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getParentBackdropImageTags().get(i)))));
            }
        }

        // add `IsPostPaddingRequired` to the URL query string
        if (getIsPostPaddingRequired() != null) {
            joiner.add(String.format("%sIsPostPaddingRequired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPostPaddingRequired()))));
        }

        // add `KeepUntil` to the URL query string
        if (getKeepUntil() != null) {
            joiner.add(String.format("%sKeepUntil%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKeepUntil()))));
        }

        // add `Status` to the URL query string
        if (getStatus() != null) {
            joiner.add(String.format("%sStatus%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
        }

        // add `SeriesTimerId` to the URL query string
        if (getSeriesTimerId() != null) {
            joiner.add(String.format("%sSeriesTimerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesTimerId()))));
        }

        // add `ExternalSeriesTimerId` to the URL query string
        if (getExternalSeriesTimerId() != null) {
            joiner.add(String.format("%sExternalSeriesTimerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExternalSeriesTimerId()))));
        }

        // add `RunTimeTicks` to the URL query string
        if (getRunTimeTicks() != null) {
            joiner.add(String.format("%sRunTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRunTimeTicks()))));
        }

        // add `ProgramInfo` to the URL query string
        if (getProgramInfo() != null) {
            joiner.add(getProgramInfo().toUrlQueryString(prefix + "ProgramInfo" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TimerInfoDto instance;

        public Builder() {
            this(new TimerInfoDto());
        }

        protected Builder(TimerInfoDto instance) {
            this.instance = instance;
        }

        public TimerInfoDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public TimerInfoDto.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public TimerInfoDto.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        public TimerInfoDto.Builder externalId(String externalId) {
            this.instance.externalId = externalId;
            return this;
        }

        public TimerInfoDto.Builder channelId(UUID channelId) {
            this.instance.channelId = channelId;
            return this;
        }

        public TimerInfoDto.Builder externalChannelId(String externalChannelId) {
            this.instance.externalChannelId = externalChannelId;
            return this;
        }

        public TimerInfoDto.Builder channelName(String channelName) {
            this.instance.channelName = channelName;
            return this;
        }

        public TimerInfoDto.Builder channelPrimaryImageTag(String channelPrimaryImageTag) {
            this.instance.channelPrimaryImageTag = channelPrimaryImageTag;
            return this;
        }

        public TimerInfoDto.Builder programId(String programId) {
            this.instance.programId = programId;
            return this;
        }

        public TimerInfoDto.Builder externalProgramId(String externalProgramId) {
            this.instance.externalProgramId = externalProgramId;
            return this;
        }

        public TimerInfoDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public TimerInfoDto.Builder overview(String overview) {
            this.instance.overview = overview;
            return this;
        }

        public TimerInfoDto.Builder startDate(OffsetDateTime startDate) {
            this.instance.startDate = startDate;
            return this;
        }

        public TimerInfoDto.Builder endDate(OffsetDateTime endDate) {
            this.instance.endDate = endDate;
            return this;
        }

        public TimerInfoDto.Builder serviceName(String serviceName) {
            this.instance.serviceName = serviceName;
            return this;
        }

        public TimerInfoDto.Builder priority(Integer priority) {
            this.instance.priority = priority;
            return this;
        }

        public TimerInfoDto.Builder prePaddingSeconds(Integer prePaddingSeconds) {
            this.instance.prePaddingSeconds = prePaddingSeconds;
            return this;
        }

        public TimerInfoDto.Builder postPaddingSeconds(Integer postPaddingSeconds) {
            this.instance.postPaddingSeconds = postPaddingSeconds;
            return this;
        }

        public TimerInfoDto.Builder isPrePaddingRequired(Boolean isPrePaddingRequired) {
            this.instance.isPrePaddingRequired = isPrePaddingRequired;
            return this;
        }

        public TimerInfoDto.Builder parentBackdropItemId(String parentBackdropItemId) {
            this.instance.parentBackdropItemId = parentBackdropItemId;
            return this;
        }

        public TimerInfoDto.Builder parentBackdropImageTags(List<String> parentBackdropImageTags) {
            this.instance.parentBackdropImageTags = parentBackdropImageTags;
            return this;
        }

        public TimerInfoDto.Builder isPostPaddingRequired(Boolean isPostPaddingRequired) {
            this.instance.isPostPaddingRequired = isPostPaddingRequired;
            return this;
        }

        public TimerInfoDto.Builder keepUntil(KeepUntil keepUntil) {
            this.instance.keepUntil = keepUntil;
            return this;
        }

        public TimerInfoDto.Builder status(RecordingStatus status) {
            this.instance.status = status;
            return this;
        }

        public TimerInfoDto.Builder seriesTimerId(String seriesTimerId) {
            this.instance.seriesTimerId = seriesTimerId;
            return this;
        }

        public TimerInfoDto.Builder externalSeriesTimerId(String externalSeriesTimerId) {
            this.instance.externalSeriesTimerId = externalSeriesTimerId;
            return this;
        }

        public TimerInfoDto.Builder runTimeTicks(Long runTimeTicks) {
            this.instance.runTimeTicks = runTimeTicks;
            return this;
        }

        public TimerInfoDto.Builder programInfo(BaseItemDto programInfo) {
            this.instance.programInfo = programInfo;
            return this;
        }

        /**
         * returns a built TimerInfoDto instance.
         *
         * The builder is not reusable.
         */
        public TimerInfoDto build() {
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
    public static TimerInfoDto.Builder builder() {
        return new TimerInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TimerInfoDto.Builder toBuilder() {
        return new TimerInfoDto.Builder().id(getId()).type(getType()).serverId(getServerId())
                .externalId(getExternalId()).channelId(getChannelId()).externalChannelId(getExternalChannelId())
                .channelName(getChannelName()).channelPrimaryImageTag(getChannelPrimaryImageTag())
                .programId(getProgramId()).externalProgramId(getExternalProgramId()).name(getName())
                .overview(getOverview()).startDate(getStartDate()).endDate(getEndDate()).serviceName(getServiceName())
                .priority(getPriority()).prePaddingSeconds(getPrePaddingSeconds())
                .postPaddingSeconds(getPostPaddingSeconds()).isPrePaddingRequired(getIsPrePaddingRequired())
                .parentBackdropItemId(getParentBackdropItemId()).parentBackdropImageTags(getParentBackdropImageTags())
                .isPostPaddingRequired(getIsPostPaddingRequired()).keepUntil(getKeepUntil()).status(getStatus())
                .seriesTimerId(getSeriesTimerId()).externalSeriesTimerId(getExternalSeriesTimerId())
                .runTimeTicks(getRunTimeTicks()).programInfo(getProgramInfo());
    }
}
