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
 * Session info DTO.
 */
@JsonPropertyOrder({ SessionInfoDto.JSON_PROPERTY_PLAY_STATE, SessionInfoDto.JSON_PROPERTY_ADDITIONAL_USERS,
        SessionInfoDto.JSON_PROPERTY_CAPABILITIES, SessionInfoDto.JSON_PROPERTY_REMOTE_END_POINT,
        SessionInfoDto.JSON_PROPERTY_PLAYABLE_MEDIA_TYPES, SessionInfoDto.JSON_PROPERTY_ID,
        SessionInfoDto.JSON_PROPERTY_USER_ID, SessionInfoDto.JSON_PROPERTY_USER_NAME,
        SessionInfoDto.JSON_PROPERTY_CLIENT, SessionInfoDto.JSON_PROPERTY_LAST_ACTIVITY_DATE,
        SessionInfoDto.JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN, SessionInfoDto.JSON_PROPERTY_LAST_PAUSED_DATE,
        SessionInfoDto.JSON_PROPERTY_DEVICE_NAME, SessionInfoDto.JSON_PROPERTY_DEVICE_TYPE,
        SessionInfoDto.JSON_PROPERTY_NOW_PLAYING_ITEM, SessionInfoDto.JSON_PROPERTY_NOW_VIEWING_ITEM,
        SessionInfoDto.JSON_PROPERTY_DEVICE_ID, SessionInfoDto.JSON_PROPERTY_APPLICATION_VERSION,
        SessionInfoDto.JSON_PROPERTY_TRANSCODING_INFO, SessionInfoDto.JSON_PROPERTY_IS_ACTIVE,
        SessionInfoDto.JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL, SessionInfoDto.JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL,
        SessionInfoDto.JSON_PROPERTY_NOW_PLAYING_QUEUE, SessionInfoDto.JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS,
        SessionInfoDto.JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME, SessionInfoDto.JSON_PROPERTY_PLAYLIST_ITEM_ID,
        SessionInfoDto.JSON_PROPERTY_SERVER_ID, SessionInfoDto.JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG,
        SessionInfoDto.JSON_PROPERTY_SUPPORTED_COMMANDS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SessionInfoDto {
    public static final String JSON_PROPERTY_PLAY_STATE = "PlayState";
    @org.eclipse.jdt.annotation.NonNull
    private PlayerStateInfo playState;

    public static final String JSON_PROPERTY_ADDITIONAL_USERS = "AdditionalUsers";
    @org.eclipse.jdt.annotation.NonNull
    private List<SessionUserInfo> additionalUsers;

    public static final String JSON_PROPERTY_CAPABILITIES = "Capabilities";
    @org.eclipse.jdt.annotation.NonNull
    private ClientCapabilitiesDto capabilities;

    public static final String JSON_PROPERTY_REMOTE_END_POINT = "RemoteEndPoint";
    @org.eclipse.jdt.annotation.NonNull
    private String remoteEndPoint;

    public static final String JSON_PROPERTY_PLAYABLE_MEDIA_TYPES = "PlayableMediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaType> playableMediaTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_USER_NAME = "UserName";
    @org.eclipse.jdt.annotation.NonNull
    private String userName;

    public static final String JSON_PROPERTY_CLIENT = "Client";
    @org.eclipse.jdt.annotation.NonNull
    private String client;

    public static final String JSON_PROPERTY_LAST_ACTIVITY_DATE = "LastActivityDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastActivityDate;

    public static final String JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN = "LastPlaybackCheckIn";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastPlaybackCheckIn;

    public static final String JSON_PROPERTY_LAST_PAUSED_DATE = "LastPausedDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastPausedDate;

    public static final String JSON_PROPERTY_DEVICE_NAME = "DeviceName";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceName;

    public static final String JSON_PROPERTY_DEVICE_TYPE = "DeviceType";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceType;

    public static final String JSON_PROPERTY_NOW_PLAYING_ITEM = "NowPlayingItem";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto nowPlayingItem;

    public static final String JSON_PROPERTY_NOW_VIEWING_ITEM = "NowViewingItem";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto nowViewingItem;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public static final String JSON_PROPERTY_APPLICATION_VERSION = "ApplicationVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String applicationVersion;

    public static final String JSON_PROPERTY_TRANSCODING_INFO = "TranscodingInfo";
    @org.eclipse.jdt.annotation.NonNull
    private TranscodingInfo transcodingInfo;

    public static final String JSON_PROPERTY_IS_ACTIVE = "IsActive";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isActive;

    public static final String JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL = "SupportsMediaControl";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsMediaControl;

    public static final String JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL = "SupportsRemoteControl";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsRemoteControl;

    public static final String JSON_PROPERTY_NOW_PLAYING_QUEUE = "NowPlayingQueue";
    @org.eclipse.jdt.annotation.NonNull
    private List<QueueItem> nowPlayingQueue;

    public static final String JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS = "NowPlayingQueueFullItems";
    @org.eclipse.jdt.annotation.NonNull
    private List<BaseItemDto> nowPlayingQueueFullItems;

    public static final String JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME = "HasCustomDeviceName";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasCustomDeviceName;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String playlistItemId;

    public static final String JSON_PROPERTY_SERVER_ID = "ServerId";
    @org.eclipse.jdt.annotation.NonNull
    private String serverId;

    public static final String JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG = "UserPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String userPrimaryImageTag;

    public static final String JSON_PROPERTY_SUPPORTED_COMMANDS = "SupportedCommands";
    @org.eclipse.jdt.annotation.NonNull
    private List<GeneralCommandType> supportedCommands = new ArrayList<>();

    public SessionInfoDto() {
    }

    public SessionInfoDto playState(@org.eclipse.jdt.annotation.NonNull PlayerStateInfo playState) {
        this.playState = playState;
        return this;
    }

    /**
     * Gets or sets the play state.
     * 
     * @return playState
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlayerStateInfo getPlayState() {
        return playState;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayState(@org.eclipse.jdt.annotation.NonNull PlayerStateInfo playState) {
        this.playState = playState;
    }

    public SessionInfoDto additionalUsers(@org.eclipse.jdt.annotation.NonNull List<SessionUserInfo> additionalUsers) {
        this.additionalUsers = additionalUsers;
        return this;
    }

    public SessionInfoDto addAdditionalUsersItem(SessionUserInfo additionalUsersItem) {
        if (this.additionalUsers == null) {
            this.additionalUsers = new ArrayList<>();
        }
        this.additionalUsers.add(additionalUsersItem);
        return this;
    }

    /**
     * Gets or sets the additional users.
     * 
     * @return additionalUsers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ADDITIONAL_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<SessionUserInfo> getAdditionalUsers() {
        return additionalUsers;
    }

    @JsonProperty(value = JSON_PROPERTY_ADDITIONAL_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAdditionalUsers(@org.eclipse.jdt.annotation.NonNull List<SessionUserInfo> additionalUsers) {
        this.additionalUsers = additionalUsers;
    }

    public SessionInfoDto capabilities(@org.eclipse.jdt.annotation.NonNull ClientCapabilitiesDto capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /**
     * Gets or sets the client capabilities.
     * 
     * @return capabilities
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAPABILITIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ClientCapabilitiesDto getCapabilities() {
        return capabilities;
    }

    @JsonProperty(value = JSON_PROPERTY_CAPABILITIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCapabilities(@org.eclipse.jdt.annotation.NonNull ClientCapabilitiesDto capabilities) {
        this.capabilities = capabilities;
    }

    public SessionInfoDto remoteEndPoint(@org.eclipse.jdt.annotation.NonNull String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
        return this;
    }

    /**
     * Gets or sets the remote end point.
     * 
     * @return remoteEndPoint
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REMOTE_END_POINT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    @JsonProperty(value = JSON_PROPERTY_REMOTE_END_POINT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteEndPoint(@org.eclipse.jdt.annotation.NonNull String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
    }

    public SessionInfoDto playableMediaTypes(@org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes) {
        this.playableMediaTypes = playableMediaTypes;
        return this;
    }

    public SessionInfoDto addPlayableMediaTypesItem(MediaType playableMediaTypesItem) {
        if (this.playableMediaTypes == null) {
            this.playableMediaTypes = new ArrayList<>();
        }
        this.playableMediaTypes.add(playableMediaTypesItem);
        return this;
    }

    /**
     * Gets or sets the playable media types.
     * 
     * @return playableMediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYABLE_MEDIA_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaType> getPlayableMediaTypes() {
        return playableMediaTypes;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYABLE_MEDIA_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayableMediaTypes(@org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes) {
        this.playableMediaTypes = playableMediaTypes;
    }

    public SessionInfoDto id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public SessionInfoDto userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the user id.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public SessionInfoDto userName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Gets or sets the username.
     * 
     * @return userName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserName() {
        return userName;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
    }

    public SessionInfoDto client(@org.eclipse.jdt.annotation.NonNull String client) {
        this.client = client;
        return this;
    }

    /**
     * Gets or sets the type of the client.
     * 
     * @return client
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CLIENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getClient() {
        return client;
    }

    @JsonProperty(value = JSON_PROPERTY_CLIENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClient(@org.eclipse.jdt.annotation.NonNull String client) {
        this.client = client;
    }

    public SessionInfoDto lastActivityDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
        return this;
    }

    /**
     * Gets or sets the last activity date.
     * 
     * @return lastActivityDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LAST_ACTIVITY_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_ACTIVITY_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastActivityDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public SessionInfoDto lastPlaybackCheckIn(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlaybackCheckIn) {
        this.lastPlaybackCheckIn = lastPlaybackCheckIn;
        return this;
    }

    /**
     * Gets or sets the last playback check in.
     * 
     * @return lastPlaybackCheckIn
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastPlaybackCheckIn() {
        return lastPlaybackCheckIn;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastPlaybackCheckIn(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlaybackCheckIn) {
        this.lastPlaybackCheckIn = lastPlaybackCheckIn;
    }

    public SessionInfoDto lastPausedDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPausedDate) {
        this.lastPausedDate = lastPausedDate;
        return this;
    }

    /**
     * Gets or sets the last paused date.
     * 
     * @return lastPausedDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LAST_PAUSED_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastPausedDate() {
        return lastPausedDate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_PAUSED_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastPausedDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPausedDate) {
        this.lastPausedDate = lastPausedDate;
    }

    public SessionInfoDto deviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    /**
     * Gets or sets the name of the device.
     * 
     * @return deviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceName() {
        return deviceName;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
    }

    public SessionInfoDto deviceType(@org.eclipse.jdt.annotation.NonNull String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * Gets or sets the type of the device.
     * 
     * @return deviceType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceType() {
        return deviceType;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceType(@org.eclipse.jdt.annotation.NonNull String deviceType) {
        this.deviceType = deviceType;
    }

    public SessionInfoDto nowPlayingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowPlayingItem) {
        this.nowPlayingItem = nowPlayingItem;
        return this;
    }

    /**
     * Gets or sets the now playing item.
     * 
     * @return nowPlayingItem
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getNowPlayingItem() {
        return nowPlayingItem;
    }

    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowPlayingItem) {
        this.nowPlayingItem = nowPlayingItem;
    }

    public SessionInfoDto nowViewingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowViewingItem) {
        this.nowViewingItem = nowViewingItem;
        return this;
    }

    /**
     * Gets or sets the now viewing item.
     * 
     * @return nowViewingItem
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NOW_VIEWING_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getNowViewingItem() {
        return nowViewingItem;
    }

    @JsonProperty(value = JSON_PROPERTY_NOW_VIEWING_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowViewingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowViewingItem) {
        this.nowViewingItem = nowViewingItem;
    }

    public SessionInfoDto deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets or sets the device id.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public SessionInfoDto applicationVersion(@org.eclipse.jdt.annotation.NonNull String applicationVersion) {
        this.applicationVersion = applicationVersion;
        return this;
    }

    /**
     * Gets or sets the application version.
     * 
     * @return applicationVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APPLICATION_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getApplicationVersion() {
        return applicationVersion;
    }

    @JsonProperty(value = JSON_PROPERTY_APPLICATION_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setApplicationVersion(@org.eclipse.jdt.annotation.NonNull String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public SessionInfoDto transcodingInfo(@org.eclipse.jdt.annotation.NonNull TranscodingInfo transcodingInfo) {
        this.transcodingInfo = transcodingInfo;
        return this;
    }

    /**
     * Gets or sets the transcoding info.
     * 
     * @return transcodingInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TranscodingInfo getTranscodingInfo() {
        return transcodingInfo;
    }

    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingInfo(@org.eclipse.jdt.annotation.NonNull TranscodingInfo transcodingInfo) {
        this.transcodingInfo = transcodingInfo;
    }

    public SessionInfoDto isActive(@org.eclipse.jdt.annotation.NonNull Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this session is active.
     * 
     * @return isActive
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_ACTIVE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsActive() {
        return isActive;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_ACTIVE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsActive(@org.eclipse.jdt.annotation.NonNull Boolean isActive) {
        this.isActive = isActive;
    }

    public SessionInfoDto supportsMediaControl(@org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl) {
        this.supportsMediaControl = supportsMediaControl;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the session supports media control.
     * 
     * @return supportsMediaControl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsMediaControl() {
        return supportsMediaControl;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsMediaControl(@org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl) {
        this.supportsMediaControl = supportsMediaControl;
    }

    public SessionInfoDto supportsRemoteControl(@org.eclipse.jdt.annotation.NonNull Boolean supportsRemoteControl) {
        this.supportsRemoteControl = supportsRemoteControl;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the session supports remote control.
     * 
     * @return supportsRemoteControl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsRemoteControl() {
        return supportsRemoteControl;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsRemoteControl(@org.eclipse.jdt.annotation.NonNull Boolean supportsRemoteControl) {
        this.supportsRemoteControl = supportsRemoteControl;
    }

    public SessionInfoDto nowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
        return this;
    }

    public SessionInfoDto addNowPlayingQueueItem(QueueItem nowPlayingQueueItem) {
        if (this.nowPlayingQueue == null) {
            this.nowPlayingQueue = new ArrayList<>();
        }
        this.nowPlayingQueue.add(nowPlayingQueueItem);
        return this;
    }

    /**
     * Gets or sets the now playing queue.
     * 
     * @return nowPlayingQueue
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<QueueItem> getNowPlayingQueue() {
        return nowPlayingQueue;
    }

    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
    }

    public SessionInfoDto nowPlayingQueueFullItems(
            @org.eclipse.jdt.annotation.NonNull List<BaseItemDto> nowPlayingQueueFullItems) {
        this.nowPlayingQueueFullItems = nowPlayingQueueFullItems;
        return this;
    }

    public SessionInfoDto addNowPlayingQueueFullItemsItem(BaseItemDto nowPlayingQueueFullItemsItem) {
        if (this.nowPlayingQueueFullItems == null) {
            this.nowPlayingQueueFullItems = new ArrayList<>();
        }
        this.nowPlayingQueueFullItems.add(nowPlayingQueueFullItemsItem);
        return this;
    }

    /**
     * Gets or sets the now playing queue full items.
     * 
     * @return nowPlayingQueueFullItems
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<BaseItemDto> getNowPlayingQueueFullItems() {
        return nowPlayingQueueFullItems;
    }

    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueueFullItems(
            @org.eclipse.jdt.annotation.NonNull List<BaseItemDto> nowPlayingQueueFullItems) {
        this.nowPlayingQueueFullItems = nowPlayingQueueFullItems;
    }

    public SessionInfoDto hasCustomDeviceName(@org.eclipse.jdt.annotation.NonNull Boolean hasCustomDeviceName) {
        this.hasCustomDeviceName = hasCustomDeviceName;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the session has a custom device name.
     * 
     * @return hasCustomDeviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasCustomDeviceName() {
        return hasCustomDeviceName;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasCustomDeviceName(@org.eclipse.jdt.annotation.NonNull Boolean hasCustomDeviceName) {
        this.hasCustomDeviceName = hasCustomDeviceName;
    }

    public SessionInfoDto playlistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets or sets the playlist item id.
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    public SessionInfoDto serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Gets or sets the server id.
     * 
     * @return serverId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerId() {
        return serverId;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
    }

    public SessionInfoDto userPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String userPrimaryImageTag) {
        this.userPrimaryImageTag = userPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the user primary image tag.
     * 
     * @return userPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserPrimaryImageTag() {
        return userPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String userPrimaryImageTag) {
        this.userPrimaryImageTag = userPrimaryImageTag;
    }

    public SessionInfoDto supportedCommands(
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands) {
        this.supportedCommands = supportedCommands;
        return this;
    }

    public SessionInfoDto addSupportedCommandsItem(GeneralCommandType supportedCommandsItem) {
        if (this.supportedCommands == null) {
            this.supportedCommands = new ArrayList<>();
        }
        this.supportedCommands.add(supportedCommandsItem);
        return this;
    }

    /**
     * Gets or sets the supported commands.
     * 
     * @return supportedCommands
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_COMMANDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<GeneralCommandType> getSupportedCommands() {
        return supportedCommands;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_COMMANDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedCommands(@org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands) {
        this.supportedCommands = supportedCommands;
    }

    /**
     * Return true if this SessionInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionInfoDto sessionInfoDto = (SessionInfoDto) o;
        return Objects.equals(this.playState, sessionInfoDto.playState)
                && Objects.equals(this.additionalUsers, sessionInfoDto.additionalUsers)
                && Objects.equals(this.capabilities, sessionInfoDto.capabilities)
                && Objects.equals(this.remoteEndPoint, sessionInfoDto.remoteEndPoint)
                && Objects.equals(this.playableMediaTypes, sessionInfoDto.playableMediaTypes)
                && Objects.equals(this.id, sessionInfoDto.id) && Objects.equals(this.userId, sessionInfoDto.userId)
                && Objects.equals(this.userName, sessionInfoDto.userName)
                && Objects.equals(this.client, sessionInfoDto.client)
                && Objects.equals(this.lastActivityDate, sessionInfoDto.lastActivityDate)
                && Objects.equals(this.lastPlaybackCheckIn, sessionInfoDto.lastPlaybackCheckIn)
                && Objects.equals(this.lastPausedDate, sessionInfoDto.lastPausedDate)
                && Objects.equals(this.deviceName, sessionInfoDto.deviceName)
                && Objects.equals(this.deviceType, sessionInfoDto.deviceType)
                && Objects.equals(this.nowPlayingItem, sessionInfoDto.nowPlayingItem)
                && Objects.equals(this.nowViewingItem, sessionInfoDto.nowViewingItem)
                && Objects.equals(this.deviceId, sessionInfoDto.deviceId)
                && Objects.equals(this.applicationVersion, sessionInfoDto.applicationVersion)
                && Objects.equals(this.transcodingInfo, sessionInfoDto.transcodingInfo)
                && Objects.equals(this.isActive, sessionInfoDto.isActive)
                && Objects.equals(this.supportsMediaControl, sessionInfoDto.supportsMediaControl)
                && Objects.equals(this.supportsRemoteControl, sessionInfoDto.supportsRemoteControl)
                && Objects.equals(this.nowPlayingQueue, sessionInfoDto.nowPlayingQueue)
                && Objects.equals(this.nowPlayingQueueFullItems, sessionInfoDto.nowPlayingQueueFullItems)
                && Objects.equals(this.hasCustomDeviceName, sessionInfoDto.hasCustomDeviceName)
                && Objects.equals(this.playlistItemId, sessionInfoDto.playlistItemId)
                && Objects.equals(this.serverId, sessionInfoDto.serverId)
                && Objects.equals(this.userPrimaryImageTag, sessionInfoDto.userPrimaryImageTag)
                && Objects.equals(this.supportedCommands, sessionInfoDto.supportedCommands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playState, additionalUsers, capabilities, remoteEndPoint, playableMediaTypes, id, userId,
                userName, client, lastActivityDate, lastPlaybackCheckIn, lastPausedDate, deviceName, deviceType,
                nowPlayingItem, nowViewingItem, deviceId, applicationVersion, transcodingInfo, isActive,
                supportsMediaControl, supportsRemoteControl, nowPlayingQueue, nowPlayingQueueFullItems,
                hasCustomDeviceName, playlistItemId, serverId, userPrimaryImageTag, supportedCommands);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SessionInfoDto {\n");
        sb.append("    playState: ").append(toIndentedString(playState)).append("\n");
        sb.append("    additionalUsers: ").append(toIndentedString(additionalUsers)).append("\n");
        sb.append("    capabilities: ").append(toIndentedString(capabilities)).append("\n");
        sb.append("    remoteEndPoint: ").append(toIndentedString(remoteEndPoint)).append("\n");
        sb.append("    playableMediaTypes: ").append(toIndentedString(playableMediaTypes)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
        sb.append("    client: ").append(toIndentedString(client)).append("\n");
        sb.append("    lastActivityDate: ").append(toIndentedString(lastActivityDate)).append("\n");
        sb.append("    lastPlaybackCheckIn: ").append(toIndentedString(lastPlaybackCheckIn)).append("\n");
        sb.append("    lastPausedDate: ").append(toIndentedString(lastPausedDate)).append("\n");
        sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
        sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
        sb.append("    nowPlayingItem: ").append(toIndentedString(nowPlayingItem)).append("\n");
        sb.append("    nowViewingItem: ").append(toIndentedString(nowViewingItem)).append("\n");
        sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
        sb.append("    applicationVersion: ").append(toIndentedString(applicationVersion)).append("\n");
        sb.append("    transcodingInfo: ").append(toIndentedString(transcodingInfo)).append("\n");
        sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
        sb.append("    supportsMediaControl: ").append(toIndentedString(supportsMediaControl)).append("\n");
        sb.append("    supportsRemoteControl: ").append(toIndentedString(supportsRemoteControl)).append("\n");
        sb.append("    nowPlayingQueue: ").append(toIndentedString(nowPlayingQueue)).append("\n");
        sb.append("    nowPlayingQueueFullItems: ").append(toIndentedString(nowPlayingQueueFullItems)).append("\n");
        sb.append("    hasCustomDeviceName: ").append(toIndentedString(hasCustomDeviceName)).append("\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
        sb.append("    serverId: ").append(toIndentedString(serverId)).append("\n");
        sb.append("    userPrimaryImageTag: ").append(toIndentedString(userPrimaryImageTag)).append("\n");
        sb.append("    supportedCommands: ").append(toIndentedString(supportedCommands)).append("\n");
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

        // add `PlayState` to the URL query string
        if (getPlayState() != null) {
            joiner.add(getPlayState().toUrlQueryString(prefix + "PlayState" + suffix));
        }

        // add `AdditionalUsers` to the URL query string
        if (getAdditionalUsers() != null) {
            for (int i = 0; i < getAdditionalUsers().size(); i++) {
                if (getAdditionalUsers().get(i) != null) {
                    joiner.add(getAdditionalUsers().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sAdditionalUsers%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Capabilities` to the URL query string
        if (getCapabilities() != null) {
            joiner.add(getCapabilities().toUrlQueryString(prefix + "Capabilities" + suffix));
        }

        // add `RemoteEndPoint` to the URL query string
        if (getRemoteEndPoint() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRemoteEndPoint%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRemoteEndPoint()))));
        }

        // add `PlayableMediaTypes` to the URL query string
        if (getPlayableMediaTypes() != null) {
            for (int i = 0; i < getPlayableMediaTypes().size(); i++) {
                if (getPlayableMediaTypes().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sPlayableMediaTypes%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getPlayableMediaTypes().get(i)))));
                }
            }
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `UserName` to the URL query string
        if (getUserName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserName()))));
        }

        // add `Client` to the URL query string
        if (getClient() != null) {
            joiner.add(String.format(Locale.ROOT, "%sClient%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getClient()))));
        }

        // add `LastActivityDate` to the URL query string
        if (getLastActivityDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastActivityDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastActivityDate()))));
        }

        // add `LastPlaybackCheckIn` to the URL query string
        if (getLastPlaybackCheckIn() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastPlaybackCheckIn%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastPlaybackCheckIn()))));
        }

        // add `LastPausedDate` to the URL query string
        if (getLastPausedDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLastPausedDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastPausedDate()))));
        }

        // add `DeviceName` to the URL query string
        if (getDeviceName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceName()))));
        }

        // add `DeviceType` to the URL query string
        if (getDeviceType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceType()))));
        }

        // add `NowPlayingItem` to the URL query string
        if (getNowPlayingItem() != null) {
            joiner.add(getNowPlayingItem().toUrlQueryString(prefix + "NowPlayingItem" + suffix));
        }

        // add `NowViewingItem` to the URL query string
        if (getNowViewingItem() != null) {
            joiner.add(getNowViewingItem().toUrlQueryString(prefix + "NowViewingItem" + suffix));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        // add `ApplicationVersion` to the URL query string
        if (getApplicationVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sApplicationVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getApplicationVersion()))));
        }

        // add `TranscodingInfo` to the URL query string
        if (getTranscodingInfo() != null) {
            joiner.add(getTranscodingInfo().toUrlQueryString(prefix + "TranscodingInfo" + suffix));
        }

        // add `IsActive` to the URL query string
        if (getIsActive() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsActive%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsActive()))));
        }

        // add `SupportsMediaControl` to the URL query string
        if (getSupportsMediaControl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsMediaControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsMediaControl()))));
        }

        // add `SupportsRemoteControl` to the URL query string
        if (getSupportsRemoteControl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsRemoteControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsRemoteControl()))));
        }

        // add `NowPlayingQueue` to the URL query string
        if (getNowPlayingQueue() != null) {
            for (int i = 0; i < getNowPlayingQueue().size(); i++) {
                if (getNowPlayingQueue().get(i) != null) {
                    joiner.add(getNowPlayingQueue().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sNowPlayingQueue%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `NowPlayingQueueFullItems` to the URL query string
        if (getNowPlayingQueueFullItems() != null) {
            for (int i = 0; i < getNowPlayingQueueFullItems().size(); i++) {
                if (getNowPlayingQueueFullItems().get(i) != null) {
                    joiner.add(getNowPlayingQueueFullItems().get(i).toUrlQueryString(String.format(Locale.ROOT,
                            "%sNowPlayingQueueFullItems%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `HasCustomDeviceName` to the URL query string
        if (getHasCustomDeviceName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasCustomDeviceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasCustomDeviceName()))));
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        // add `UserPrimaryImageTag` to the URL query string
        if (getUserPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserPrimaryImageTag()))));
        }

        // add `SupportedCommands` to the URL query string
        if (getSupportedCommands() != null) {
            for (int i = 0; i < getSupportedCommands().size(); i++) {
                if (getSupportedCommands().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sSupportedCommands%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getSupportedCommands().get(i)))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private SessionInfoDto instance;

        public Builder() {
            this(new SessionInfoDto());
        }

        protected Builder(SessionInfoDto instance) {
            this.instance = instance;
        }

        public SessionInfoDto.Builder playState(PlayerStateInfo playState) {
            this.instance.playState = playState;
            return this;
        }

        public SessionInfoDto.Builder additionalUsers(List<SessionUserInfo> additionalUsers) {
            this.instance.additionalUsers = additionalUsers;
            return this;
        }

        public SessionInfoDto.Builder capabilities(ClientCapabilitiesDto capabilities) {
            this.instance.capabilities = capabilities;
            return this;
        }

        public SessionInfoDto.Builder remoteEndPoint(String remoteEndPoint) {
            this.instance.remoteEndPoint = remoteEndPoint;
            return this;
        }

        public SessionInfoDto.Builder playableMediaTypes(List<MediaType> playableMediaTypes) {
            this.instance.playableMediaTypes = playableMediaTypes;
            return this;
        }

        public SessionInfoDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public SessionInfoDto.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public SessionInfoDto.Builder userName(String userName) {
            this.instance.userName = userName;
            return this;
        }

        public SessionInfoDto.Builder client(String client) {
            this.instance.client = client;
            return this;
        }

        public SessionInfoDto.Builder lastActivityDate(OffsetDateTime lastActivityDate) {
            this.instance.lastActivityDate = lastActivityDate;
            return this;
        }

        public SessionInfoDto.Builder lastPlaybackCheckIn(OffsetDateTime lastPlaybackCheckIn) {
            this.instance.lastPlaybackCheckIn = lastPlaybackCheckIn;
            return this;
        }

        public SessionInfoDto.Builder lastPausedDate(OffsetDateTime lastPausedDate) {
            this.instance.lastPausedDate = lastPausedDate;
            return this;
        }

        public SessionInfoDto.Builder deviceName(String deviceName) {
            this.instance.deviceName = deviceName;
            return this;
        }

        public SessionInfoDto.Builder deviceType(String deviceType) {
            this.instance.deviceType = deviceType;
            return this;
        }

        public SessionInfoDto.Builder nowPlayingItem(BaseItemDto nowPlayingItem) {
            this.instance.nowPlayingItem = nowPlayingItem;
            return this;
        }

        public SessionInfoDto.Builder nowViewingItem(BaseItemDto nowViewingItem) {
            this.instance.nowViewingItem = nowViewingItem;
            return this;
        }

        public SessionInfoDto.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public SessionInfoDto.Builder applicationVersion(String applicationVersion) {
            this.instance.applicationVersion = applicationVersion;
            return this;
        }

        public SessionInfoDto.Builder transcodingInfo(TranscodingInfo transcodingInfo) {
            this.instance.transcodingInfo = transcodingInfo;
            return this;
        }

        public SessionInfoDto.Builder isActive(Boolean isActive) {
            this.instance.isActive = isActive;
            return this;
        }

        public SessionInfoDto.Builder supportsMediaControl(Boolean supportsMediaControl) {
            this.instance.supportsMediaControl = supportsMediaControl;
            return this;
        }

        public SessionInfoDto.Builder supportsRemoteControl(Boolean supportsRemoteControl) {
            this.instance.supportsRemoteControl = supportsRemoteControl;
            return this;
        }

        public SessionInfoDto.Builder nowPlayingQueue(List<QueueItem> nowPlayingQueue) {
            this.instance.nowPlayingQueue = nowPlayingQueue;
            return this;
        }

        public SessionInfoDto.Builder nowPlayingQueueFullItems(List<BaseItemDto> nowPlayingQueueFullItems) {
            this.instance.nowPlayingQueueFullItems = nowPlayingQueueFullItems;
            return this;
        }

        public SessionInfoDto.Builder hasCustomDeviceName(Boolean hasCustomDeviceName) {
            this.instance.hasCustomDeviceName = hasCustomDeviceName;
            return this;
        }

        public SessionInfoDto.Builder playlistItemId(String playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        public SessionInfoDto.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        public SessionInfoDto.Builder userPrimaryImageTag(String userPrimaryImageTag) {
            this.instance.userPrimaryImageTag = userPrimaryImageTag;
            return this;
        }

        public SessionInfoDto.Builder supportedCommands(List<GeneralCommandType> supportedCommands) {
            this.instance.supportedCommands = supportedCommands;
            return this;
        }

        /**
         * returns a built SessionInfoDto instance.
         *
         * The builder is not reusable.
         */
        public SessionInfoDto build() {
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
    public static SessionInfoDto.Builder builder() {
        return new SessionInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SessionInfoDto.Builder toBuilder() {
        return new SessionInfoDto.Builder().playState(getPlayState()).additionalUsers(getAdditionalUsers())
                .capabilities(getCapabilities()).remoteEndPoint(getRemoteEndPoint())
                .playableMediaTypes(getPlayableMediaTypes()).id(getId()).userId(getUserId()).userName(getUserName())
                .client(getClient()).lastActivityDate(getLastActivityDate())
                .lastPlaybackCheckIn(getLastPlaybackCheckIn()).lastPausedDate(getLastPausedDate())
                .deviceName(getDeviceName()).deviceType(getDeviceType()).nowPlayingItem(getNowPlayingItem())
                .nowViewingItem(getNowViewingItem()).deviceId(getDeviceId()).applicationVersion(getApplicationVersion())
                .transcodingInfo(getTranscodingInfo()).isActive(getIsActive())
                .supportsMediaControl(getSupportsMediaControl()).supportsRemoteControl(getSupportsRemoteControl())
                .nowPlayingQueue(getNowPlayingQueue()).nowPlayingQueueFullItems(getNowPlayingQueueFullItems())
                .hasCustomDeviceName(getHasCustomDeviceName()).playlistItemId(getPlaylistItemId())
                .serverId(getServerId()).userPrimaryImageTag(getUserPrimaryImageTag())
                .supportedCommands(getSupportedCommands());
    }
}
