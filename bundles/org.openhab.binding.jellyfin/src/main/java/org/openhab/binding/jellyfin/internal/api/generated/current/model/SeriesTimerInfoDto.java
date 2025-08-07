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
import java.util.UUID;

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

    public static final String JSON_PROPERTY_RECORD_ANY_TIME = "RecordAnyTime";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean recordAnyTime;

    public static final String JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY = "SkipEpisodesInLibrary";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean skipEpisodesInLibrary;

    public static final String JSON_PROPERTY_RECORD_ANY_CHANNEL = "RecordAnyChannel";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean recordAnyChannel;

    public static final String JSON_PROPERTY_KEEP_UP_TO = "KeepUpTo";
    @org.eclipse.jdt.annotation.NonNull
    private Integer keepUpTo;

    public static final String JSON_PROPERTY_RECORD_NEW_ONLY = "RecordNewOnly";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean recordNewOnly;

    public static final String JSON_PROPERTY_DAYS = "Days";
    @org.eclipse.jdt.annotation.NonNull
    private List<DayOfWeek> days;

    public static final String JSON_PROPERTY_DAY_PATTERN = "DayPattern";
    @org.eclipse.jdt.annotation.NonNull
    private DayPattern dayPattern;

    public static final String JSON_PROPERTY_IMAGE_TAGS = "ImageTags";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> imageTags;

    public static final String JSON_PROPERTY_PARENT_THUMB_ITEM_ID = "ParentThumbItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String parentThumbItemId;

    public static final String JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG = "ParentThumbImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String parentThumbImageTag;

    public static final String JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID = "ParentPrimaryImageItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String parentPrimaryImageItemId;

    public static final String JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG = "ParentPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String parentPrimaryImageTag;

    public SeriesTimerInfoDto() {
    }

    public SeriesTimerInfoDto id(@org.eclipse.jdt.annotation.NonNull String id) {
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

    public SeriesTimerInfoDto type(@org.eclipse.jdt.annotation.NonNull String type) {
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

    public SeriesTimerInfoDto serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
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

    public SeriesTimerInfoDto externalId(@org.eclipse.jdt.annotation.NonNull String externalId) {
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

    public SeriesTimerInfoDto channelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
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

    public SeriesTimerInfoDto externalChannelId(@org.eclipse.jdt.annotation.NonNull String externalChannelId) {
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

    public SeriesTimerInfoDto channelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
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

    public SeriesTimerInfoDto channelPrimaryImageTag(
            @org.eclipse.jdt.annotation.NonNull String channelPrimaryImageTag) {
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

    public SeriesTimerInfoDto programId(@org.eclipse.jdt.annotation.NonNull String programId) {
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

    public SeriesTimerInfoDto externalProgramId(@org.eclipse.jdt.annotation.NonNull String externalProgramId) {
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

    public SeriesTimerInfoDto name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public SeriesTimerInfoDto overview(@org.eclipse.jdt.annotation.NonNull String overview) {
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

    public SeriesTimerInfoDto startDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
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

    public SeriesTimerInfoDto endDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
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

    public SeriesTimerInfoDto serviceName(@org.eclipse.jdt.annotation.NonNull String serviceName) {
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

    public SeriesTimerInfoDto priority(@org.eclipse.jdt.annotation.NonNull Integer priority) {
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

    public SeriesTimerInfoDto prePaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer prePaddingSeconds) {
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

    public SeriesTimerInfoDto postPaddingSeconds(@org.eclipse.jdt.annotation.NonNull Integer postPaddingSeconds) {
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

    public SeriesTimerInfoDto isPrePaddingRequired(@org.eclipse.jdt.annotation.NonNull Boolean isPrePaddingRequired) {
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

    public SeriesTimerInfoDto parentBackdropItemId(@org.eclipse.jdt.annotation.NonNull String parentBackdropItemId) {
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

    public SeriesTimerInfoDto parentBackdropImageTags(
            @org.eclipse.jdt.annotation.NonNull List<String> parentBackdropImageTags) {
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

    public SeriesTimerInfoDto isPostPaddingRequired(@org.eclipse.jdt.annotation.NonNull Boolean isPostPaddingRequired) {
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

    public SeriesTimerInfoDto keepUntil(@org.eclipse.jdt.annotation.NonNull KeepUntil keepUntil) {
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

    public SeriesTimerInfoDto recordAnyTime(@org.eclipse.jdt.annotation.NonNull Boolean recordAnyTime) {
        this.recordAnyTime = recordAnyTime;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [record any time].
     * 
     * @return recordAnyTime
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RECORD_ANY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRecordAnyTime() {
        return recordAnyTime;
    }

    @JsonProperty(JSON_PROPERTY_RECORD_ANY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordAnyTime(@org.eclipse.jdt.annotation.NonNull Boolean recordAnyTime) {
        this.recordAnyTime = recordAnyTime;
    }

    public SeriesTimerInfoDto skipEpisodesInLibrary(@org.eclipse.jdt.annotation.NonNull Boolean skipEpisodesInLibrary) {
        this.skipEpisodesInLibrary = skipEpisodesInLibrary;
        return this;
    }

    /**
     * Get skipEpisodesInLibrary
     * 
     * @return skipEpisodesInLibrary
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSkipEpisodesInLibrary() {
        return skipEpisodesInLibrary;
    }

    @JsonProperty(JSON_PROPERTY_SKIP_EPISODES_IN_LIBRARY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSkipEpisodesInLibrary(@org.eclipse.jdt.annotation.NonNull Boolean skipEpisodesInLibrary) {
        this.skipEpisodesInLibrary = skipEpisodesInLibrary;
    }

    public SeriesTimerInfoDto recordAnyChannel(@org.eclipse.jdt.annotation.NonNull Boolean recordAnyChannel) {
        this.recordAnyChannel = recordAnyChannel;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [record any channel].
     * 
     * @return recordAnyChannel
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RECORD_ANY_CHANNEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRecordAnyChannel() {
        return recordAnyChannel;
    }

    @JsonProperty(JSON_PROPERTY_RECORD_ANY_CHANNEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordAnyChannel(@org.eclipse.jdt.annotation.NonNull Boolean recordAnyChannel) {
        this.recordAnyChannel = recordAnyChannel;
    }

    public SeriesTimerInfoDto keepUpTo(@org.eclipse.jdt.annotation.NonNull Integer keepUpTo) {
        this.keepUpTo = keepUpTo;
        return this;
    }

    /**
     * Get keepUpTo
     * 
     * @return keepUpTo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_KEEP_UP_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getKeepUpTo() {
        return keepUpTo;
    }

    @JsonProperty(JSON_PROPERTY_KEEP_UP_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKeepUpTo(@org.eclipse.jdt.annotation.NonNull Integer keepUpTo) {
        this.keepUpTo = keepUpTo;
    }

    public SeriesTimerInfoDto recordNewOnly(@org.eclipse.jdt.annotation.NonNull Boolean recordNewOnly) {
        this.recordNewOnly = recordNewOnly;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [record new only].
     * 
     * @return recordNewOnly
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RECORD_NEW_ONLY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRecordNewOnly() {
        return recordNewOnly;
    }

    @JsonProperty(JSON_PROPERTY_RECORD_NEW_ONLY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecordNewOnly(@org.eclipse.jdt.annotation.NonNull Boolean recordNewOnly) {
        this.recordNewOnly = recordNewOnly;
    }

    public SeriesTimerInfoDto days(@org.eclipse.jdt.annotation.NonNull List<DayOfWeek> days) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DayOfWeek> getDays() {
        return days;
    }

    @JsonProperty(JSON_PROPERTY_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDays(@org.eclipse.jdt.annotation.NonNull List<DayOfWeek> days) {
        this.days = days;
    }

    public SeriesTimerInfoDto dayPattern(@org.eclipse.jdt.annotation.NonNull DayPattern dayPattern) {
        this.dayPattern = dayPattern;
        return this;
    }

    /**
     * Gets or sets the day pattern.
     * 
     * @return dayPattern
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DAY_PATTERN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DayPattern getDayPattern() {
        return dayPattern;
    }

    @JsonProperty(JSON_PROPERTY_DAY_PATTERN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDayPattern(@org.eclipse.jdt.annotation.NonNull DayPattern dayPattern) {
        this.dayPattern = dayPattern;
    }

    public SeriesTimerInfoDto imageTags(@org.eclipse.jdt.annotation.NonNull Map<String, String> imageTags) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_TAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getImageTags() {
        return imageTags;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_TAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageTags(@org.eclipse.jdt.annotation.NonNull Map<String, String> imageTags) {
        this.imageTags = imageTags;
    }

    public SeriesTimerInfoDto parentThumbItemId(@org.eclipse.jdt.annotation.NonNull String parentThumbItemId) {
        this.parentThumbItemId = parentThumbItemId;
        return this;
    }

    /**
     * Gets or sets the parent thumb item id.
     * 
     * @return parentThumbItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_THUMB_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParentThumbItemId() {
        return parentThumbItemId;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_THUMB_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentThumbItemId(@org.eclipse.jdt.annotation.NonNull String parentThumbItemId) {
        this.parentThumbItemId = parentThumbItemId;
    }

    public SeriesTimerInfoDto parentThumbImageTag(@org.eclipse.jdt.annotation.NonNull String parentThumbImageTag) {
        this.parentThumbImageTag = parentThumbImageTag;
        return this;
    }

    /**
     * Gets or sets the parent thumb image tag.
     * 
     * @return parentThumbImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParentThumbImageTag() {
        return parentThumbImageTag;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentThumbImageTag(@org.eclipse.jdt.annotation.NonNull String parentThumbImageTag) {
        this.parentThumbImageTag = parentThumbImageTag;
    }

    public SeriesTimerInfoDto parentPrimaryImageItemId(
            @org.eclipse.jdt.annotation.NonNull String parentPrimaryImageItemId) {
        this.parentPrimaryImageItemId = parentPrimaryImageItemId;
        return this;
    }

    /**
     * Gets or sets the parent primary image item identifier.
     * 
     * @return parentPrimaryImageItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParentPrimaryImageItemId() {
        return parentPrimaryImageItemId;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentPrimaryImageItemId(@org.eclipse.jdt.annotation.NonNull String parentPrimaryImageItemId) {
        this.parentPrimaryImageItemId = parentPrimaryImageItemId;
    }

    public SeriesTimerInfoDto parentPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String parentPrimaryImageTag) {
        this.parentPrimaryImageTag = parentPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the parent primary image tag.
     * 
     * @return parentPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getParentPrimaryImageTag() {
        return parentPrimaryImageTag;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String parentPrimaryImageTag) {
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
}
