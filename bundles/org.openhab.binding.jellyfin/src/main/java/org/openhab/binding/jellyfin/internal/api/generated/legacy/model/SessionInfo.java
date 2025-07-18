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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class SessionInfo.
 */
@JsonPropertyOrder({ SessionInfo.JSON_PROPERTY_PLAY_STATE, SessionInfo.JSON_PROPERTY_ADDITIONAL_USERS,
        SessionInfo.JSON_PROPERTY_CAPABILITIES, SessionInfo.JSON_PROPERTY_REMOTE_END_POINT,
        SessionInfo.JSON_PROPERTY_PLAYABLE_MEDIA_TYPES, SessionInfo.JSON_PROPERTY_ID, SessionInfo.JSON_PROPERTY_USER_ID,
        SessionInfo.JSON_PROPERTY_USER_NAME, SessionInfo.JSON_PROPERTY_CLIENT,
        SessionInfo.JSON_PROPERTY_LAST_ACTIVITY_DATE, SessionInfo.JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN,
        SessionInfo.JSON_PROPERTY_DEVICE_NAME, SessionInfo.JSON_PROPERTY_DEVICE_TYPE,
        SessionInfo.JSON_PROPERTY_NOW_PLAYING_ITEM, SessionInfo.JSON_PROPERTY_FULL_NOW_PLAYING_ITEM,
        SessionInfo.JSON_PROPERTY_NOW_VIEWING_ITEM, SessionInfo.JSON_PROPERTY_DEVICE_ID,
        SessionInfo.JSON_PROPERTY_APPLICATION_VERSION, SessionInfo.JSON_PROPERTY_TRANSCODING_INFO,
        SessionInfo.JSON_PROPERTY_IS_ACTIVE, SessionInfo.JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL,
        SessionInfo.JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL, SessionInfo.JSON_PROPERTY_NOW_PLAYING_QUEUE,
        SessionInfo.JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS, SessionInfo.JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME,
        SessionInfo.JSON_PROPERTY_PLAYLIST_ITEM_ID, SessionInfo.JSON_PROPERTY_SERVER_ID,
        SessionInfo.JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG, SessionInfo.JSON_PROPERTY_SUPPORTED_COMMANDS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SessionInfo {
    public static final String JSON_PROPERTY_PLAY_STATE = "PlayState";
    @org.eclipse.jdt.annotation.NonNull
    private PlayerStateInfo playState;

    public static final String JSON_PROPERTY_ADDITIONAL_USERS = "AdditionalUsers";
    @org.eclipse.jdt.annotation.NonNull
    private List<SessionUserInfo> additionalUsers;

    public static final String JSON_PROPERTY_CAPABILITIES = "Capabilities";
    @org.eclipse.jdt.annotation.NonNull
    private ClientCapabilities capabilities;

    public static final String JSON_PROPERTY_REMOTE_END_POINT = "RemoteEndPoint";
    @org.eclipse.jdt.annotation.NonNull
    private String remoteEndPoint;

    public static final String JSON_PROPERTY_PLAYABLE_MEDIA_TYPES = "PlayableMediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> playableMediaTypes;

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

    public static final String JSON_PROPERTY_DEVICE_NAME = "DeviceName";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceName;

    public static final String JSON_PROPERTY_DEVICE_TYPE = "DeviceType";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceType;

    public static final String JSON_PROPERTY_NOW_PLAYING_ITEM = "NowPlayingItem";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto nowPlayingItem;

    public static final String JSON_PROPERTY_FULL_NOW_PLAYING_ITEM = "FullNowPlayingItem";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItem fullNowPlayingItem;

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
    private List<GeneralCommandType> supportedCommands;

    public SessionInfo() {
    }

    @JsonCreator
    public SessionInfo(@JsonProperty(JSON_PROPERTY_PLAYABLE_MEDIA_TYPES) List<String> playableMediaTypes,
            @JsonProperty(JSON_PROPERTY_IS_ACTIVE) Boolean isActive,
            @JsonProperty(JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL) Boolean supportsMediaControl,
            @JsonProperty(JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL) Boolean supportsRemoteControl,
            @JsonProperty(JSON_PROPERTY_SUPPORTED_COMMANDS) List<GeneralCommandType> supportedCommands) {
        this();
        this.playableMediaTypes = playableMediaTypes;
        this.isActive = isActive;
        this.supportsMediaControl = supportsMediaControl;
        this.supportsRemoteControl = supportsRemoteControl;
        this.supportedCommands = supportedCommands;
    }

    public SessionInfo playState(@org.eclipse.jdt.annotation.NonNull PlayerStateInfo playState) {
        this.playState = playState;
        return this;
    }

    /**
     * Get playState
     * 
     * @return playState
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlayerStateInfo getPlayState() {
        return playState;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayState(@org.eclipse.jdt.annotation.NonNull PlayerStateInfo playState) {
        this.playState = playState;
    }

    public SessionInfo additionalUsers(@org.eclipse.jdt.annotation.NonNull List<SessionUserInfo> additionalUsers) {
        this.additionalUsers = additionalUsers;
        return this;
    }

    public SessionInfo addAdditionalUsersItem(SessionUserInfo additionalUsersItem) {
        if (this.additionalUsers == null) {
            this.additionalUsers = new ArrayList<>();
        }
        this.additionalUsers.add(additionalUsersItem);
        return this;
    }

    /**
     * Get additionalUsers
     * 
     * @return additionalUsers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ADDITIONAL_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<SessionUserInfo> getAdditionalUsers() {
        return additionalUsers;
    }

    @JsonProperty(JSON_PROPERTY_ADDITIONAL_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAdditionalUsers(@org.eclipse.jdt.annotation.NonNull List<SessionUserInfo> additionalUsers) {
        this.additionalUsers = additionalUsers;
    }

    public SessionInfo capabilities(@org.eclipse.jdt.annotation.NonNull ClientCapabilities capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /**
     * Get capabilities
     * 
     * @return capabilities
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAPABILITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ClientCapabilities getCapabilities() {
        return capabilities;
    }

    @JsonProperty(JSON_PROPERTY_CAPABILITIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCapabilities(@org.eclipse.jdt.annotation.NonNull ClientCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public SessionInfo remoteEndPoint(@org.eclipse.jdt.annotation.NonNull String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
        return this;
    }

    /**
     * Gets or sets the remote end point.
     * 
     * @return remoteEndPoint
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REMOTE_END_POINT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    @JsonProperty(JSON_PROPERTY_REMOTE_END_POINT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteEndPoint(@org.eclipse.jdt.annotation.NonNull String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
    }

    /**
     * Gets the playable media types.
     * 
     * @return playableMediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYABLE_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getPlayableMediaTypes() {
        return playableMediaTypes;
    }

    public SessionInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
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

    public SessionInfo userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the user id.
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

    public SessionInfo userName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Gets or sets the username.
     * 
     * @return userName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserName() {
        return userName;
    }

    @JsonProperty(JSON_PROPERTY_USER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserName(@org.eclipse.jdt.annotation.NonNull String userName) {
        this.userName = userName;
    }

    public SessionInfo client(@org.eclipse.jdt.annotation.NonNull String client) {
        this.client = client;
        return this;
    }

    /**
     * Gets or sets the type of the client.
     * 
     * @return client
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CLIENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getClient() {
        return client;
    }

    @JsonProperty(JSON_PROPERTY_CLIENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClient(@org.eclipse.jdt.annotation.NonNull String client) {
        this.client = client;
    }

    public SessionInfo lastActivityDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
        return this;
    }

    /**
     * Gets or sets the last activity date.
     * 
     * @return lastActivityDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_ACTIVITY_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    @JsonProperty(JSON_PROPERTY_LAST_ACTIVITY_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastActivityDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public SessionInfo lastPlaybackCheckIn(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlaybackCheckIn) {
        this.lastPlaybackCheckIn = lastPlaybackCheckIn;
        return this;
    }

    /**
     * Gets or sets the last playback check in.
     * 
     * @return lastPlaybackCheckIn
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastPlaybackCheckIn() {
        return lastPlaybackCheckIn;
    }

    @JsonProperty(JSON_PROPERTY_LAST_PLAYBACK_CHECK_IN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastPlaybackCheckIn(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastPlaybackCheckIn) {
        this.lastPlaybackCheckIn = lastPlaybackCheckIn;
    }

    public SessionInfo deviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    /**
     * Gets or sets the name of the device.
     * 
     * @return deviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEVICE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceName() {
        return deviceName;
    }

    @JsonProperty(JSON_PROPERTY_DEVICE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceName(@org.eclipse.jdt.annotation.NonNull String deviceName) {
        this.deviceName = deviceName;
    }

    public SessionInfo deviceType(@org.eclipse.jdt.annotation.NonNull String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * Gets or sets the type of the device.
     * 
     * @return deviceType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEVICE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceType() {
        return deviceType;
    }

    @JsonProperty(JSON_PROPERTY_DEVICE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceType(@org.eclipse.jdt.annotation.NonNull String deviceType) {
        this.deviceType = deviceType;
    }

    public SessionInfo nowPlayingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowPlayingItem) {
        this.nowPlayingItem = nowPlayingItem;
        return this;
    }

    /**
     * Gets or sets the now playing item.
     * 
     * @return nowPlayingItem
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getNowPlayingItem() {
        return nowPlayingItem;
    }

    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowPlayingItem) {
        this.nowPlayingItem = nowPlayingItem;
    }

    public SessionInfo fullNowPlayingItem(@org.eclipse.jdt.annotation.NonNull BaseItem fullNowPlayingItem) {
        this.fullNowPlayingItem = fullNowPlayingItem;
        return this;
    }

    /**
     * Class BaseItem.
     * 
     * @return fullNowPlayingItem
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FULL_NOW_PLAYING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItem getFullNowPlayingItem() {
        return fullNowPlayingItem;
    }

    @JsonProperty(JSON_PROPERTY_FULL_NOW_PLAYING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFullNowPlayingItem(@org.eclipse.jdt.annotation.NonNull BaseItem fullNowPlayingItem) {
        this.fullNowPlayingItem = fullNowPlayingItem;
    }

    public SessionInfo nowViewingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowViewingItem) {
        this.nowViewingItem = nowViewingItem;
        return this;
    }

    /**
     * This is strictly used as a data transfer object from the api layer. This holds information about a BaseItem in a
     * format that is convenient for the client.
     * 
     * @return nowViewingItem
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOW_VIEWING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getNowViewingItem() {
        return nowViewingItem;
    }

    @JsonProperty(JSON_PROPERTY_NOW_VIEWING_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowViewingItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto nowViewingItem) {
        this.nowViewingItem = nowViewingItem;
    }

    public SessionInfo deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets or sets the device id.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEVICE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(JSON_PROPERTY_DEVICE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public SessionInfo applicationVersion(@org.eclipse.jdt.annotation.NonNull String applicationVersion) {
        this.applicationVersion = applicationVersion;
        return this;
    }

    /**
     * Gets or sets the application version.
     * 
     * @return applicationVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_APPLICATION_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getApplicationVersion() {
        return applicationVersion;
    }

    @JsonProperty(JSON_PROPERTY_APPLICATION_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setApplicationVersion(@org.eclipse.jdt.annotation.NonNull String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public SessionInfo transcodingInfo(@org.eclipse.jdt.annotation.NonNull TranscodingInfo transcodingInfo) {
        this.transcodingInfo = transcodingInfo;
        return this;
    }

    /**
     * Get transcodingInfo
     * 
     * @return transcodingInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TranscodingInfo getTranscodingInfo() {
        return transcodingInfo;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingInfo(@org.eclipse.jdt.annotation.NonNull TranscodingInfo transcodingInfo) {
        this.transcodingInfo = transcodingInfo;
    }

    /**
     * Gets a value indicating whether this instance is active.
     * 
     * @return isActive
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_ACTIVE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Get supportsMediaControl
     * 
     * @return supportsMediaControl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsMediaControl() {
        return supportsMediaControl;
    }

    /**
     * Get supportsRemoteControl
     * 
     * @return supportsRemoteControl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_REMOTE_CONTROL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsRemoteControl() {
        return supportsRemoteControl;
    }

    public SessionInfo nowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
        return this;
    }

    public SessionInfo addNowPlayingQueueItem(QueueItem nowPlayingQueueItem) {
        if (this.nowPlayingQueue == null) {
            this.nowPlayingQueue = new ArrayList<>();
        }
        this.nowPlayingQueue.add(nowPlayingQueueItem);
        return this;
    }

    /**
     * Get nowPlayingQueue
     * 
     * @return nowPlayingQueue
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<QueueItem> getNowPlayingQueue() {
        return nowPlayingQueue;
    }

    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
    }

    public SessionInfo nowPlayingQueueFullItems(
            @org.eclipse.jdt.annotation.NonNull List<BaseItemDto> nowPlayingQueueFullItems) {
        this.nowPlayingQueueFullItems = nowPlayingQueueFullItems;
        return this;
    }

    public SessionInfo addNowPlayingQueueFullItemsItem(BaseItemDto nowPlayingQueueFullItemsItem) {
        if (this.nowPlayingQueueFullItems == null) {
            this.nowPlayingQueueFullItems = new ArrayList<>();
        }
        this.nowPlayingQueueFullItems.add(nowPlayingQueueFullItemsItem);
        return this;
    }

    /**
     * Get nowPlayingQueueFullItems
     * 
     * @return nowPlayingQueueFullItems
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<BaseItemDto> getNowPlayingQueueFullItems() {
        return nowPlayingQueueFullItems;
    }

    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE_FULL_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueueFullItems(
            @org.eclipse.jdt.annotation.NonNull List<BaseItemDto> nowPlayingQueueFullItems) {
        this.nowPlayingQueueFullItems = nowPlayingQueueFullItems;
    }

    public SessionInfo hasCustomDeviceName(@org.eclipse.jdt.annotation.NonNull Boolean hasCustomDeviceName) {
        this.hasCustomDeviceName = hasCustomDeviceName;
        return this;
    }

    /**
     * Get hasCustomDeviceName
     * 
     * @return hasCustomDeviceName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasCustomDeviceName() {
        return hasCustomDeviceName;
    }

    @JsonProperty(JSON_PROPERTY_HAS_CUSTOM_DEVICE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasCustomDeviceName(@org.eclipse.jdt.annotation.NonNull Boolean hasCustomDeviceName) {
        this.hasCustomDeviceName = hasCustomDeviceName;
    }

    public SessionInfo playlistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Get playlistItemId
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    public SessionInfo serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Get serverId
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

    public SessionInfo userPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String userPrimaryImageTag) {
        this.userPrimaryImageTag = userPrimaryImageTag;
        return this;
    }

    /**
     * Get userPrimaryImageTag
     * 
     * @return userPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserPrimaryImageTag() {
        return userPrimaryImageTag;
    }

    @JsonProperty(JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String userPrimaryImageTag) {
        this.userPrimaryImageTag = userPrimaryImageTag;
    }

    /**
     * Gets the supported commands.
     * 
     * @return supportedCommands
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTED_COMMANDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<GeneralCommandType> getSupportedCommands() {
        return supportedCommands;
    }

    /**
     * Return true if this SessionInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionInfo sessionInfo = (SessionInfo) o;
        return Objects.equals(this.playState, sessionInfo.playState)
                && Objects.equals(this.additionalUsers, sessionInfo.additionalUsers)
                && Objects.equals(this.capabilities, sessionInfo.capabilities)
                && Objects.equals(this.remoteEndPoint, sessionInfo.remoteEndPoint)
                && Objects.equals(this.playableMediaTypes, sessionInfo.playableMediaTypes)
                && Objects.equals(this.id, sessionInfo.id) && Objects.equals(this.userId, sessionInfo.userId)
                && Objects.equals(this.userName, sessionInfo.userName)
                && Objects.equals(this.client, sessionInfo.client)
                && Objects.equals(this.lastActivityDate, sessionInfo.lastActivityDate)
                && Objects.equals(this.lastPlaybackCheckIn, sessionInfo.lastPlaybackCheckIn)
                && Objects.equals(this.deviceName, sessionInfo.deviceName)
                && Objects.equals(this.deviceType, sessionInfo.deviceType)
                && Objects.equals(this.nowPlayingItem, sessionInfo.nowPlayingItem)
                && Objects.equals(this.fullNowPlayingItem, sessionInfo.fullNowPlayingItem)
                && Objects.equals(this.nowViewingItem, sessionInfo.nowViewingItem)
                && Objects.equals(this.deviceId, sessionInfo.deviceId)
                && Objects.equals(this.applicationVersion, sessionInfo.applicationVersion)
                && Objects.equals(this.transcodingInfo, sessionInfo.transcodingInfo)
                && Objects.equals(this.isActive, sessionInfo.isActive)
                && Objects.equals(this.supportsMediaControl, sessionInfo.supportsMediaControl)
                && Objects.equals(this.supportsRemoteControl, sessionInfo.supportsRemoteControl)
                && Objects.equals(this.nowPlayingQueue, sessionInfo.nowPlayingQueue)
                && Objects.equals(this.nowPlayingQueueFullItems, sessionInfo.nowPlayingQueueFullItems)
                && Objects.equals(this.hasCustomDeviceName, sessionInfo.hasCustomDeviceName)
                && Objects.equals(this.playlistItemId, sessionInfo.playlistItemId)
                && Objects.equals(this.serverId, sessionInfo.serverId)
                && Objects.equals(this.userPrimaryImageTag, sessionInfo.userPrimaryImageTag)
                && Objects.equals(this.supportedCommands, sessionInfo.supportedCommands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playState, additionalUsers, capabilities, remoteEndPoint, playableMediaTypes, id, userId,
                userName, client, lastActivityDate, lastPlaybackCheckIn, deviceName, deviceType, nowPlayingItem,
                fullNowPlayingItem, nowViewingItem, deviceId, applicationVersion, transcodingInfo, isActive,
                supportsMediaControl, supportsRemoteControl, nowPlayingQueue, nowPlayingQueueFullItems,
                hasCustomDeviceName, playlistItemId, serverId, userPrimaryImageTag, supportedCommands);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SessionInfo {\n");
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
        sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
        sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
        sb.append("    nowPlayingItem: ").append(toIndentedString(nowPlayingItem)).append("\n");
        sb.append("    fullNowPlayingItem: ").append(toIndentedString(fullNowPlayingItem)).append("\n");
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
                    joiner.add(getAdditionalUsers().get(i).toUrlQueryString(String.format("%sAdditionalUsers%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Capabilities` to the URL query string
        if (getCapabilities() != null) {
            joiner.add(getCapabilities().toUrlQueryString(prefix + "Capabilities" + suffix));
        }

        // add `RemoteEndPoint` to the URL query string
        if (getRemoteEndPoint() != null) {
            joiner.add(String.format("%sRemoteEndPoint%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRemoteEndPoint()))));
        }

        // add `PlayableMediaTypes` to the URL query string
        if (getPlayableMediaTypes() != null) {
            for (int i = 0; i < getPlayableMediaTypes().size(); i++) {
                joiner.add(String.format("%sPlayableMediaTypes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getPlayableMediaTypes().get(i)))));
            }
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format("%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `UserName` to the URL query string
        if (getUserName() != null) {
            joiner.add(String.format("%sUserName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserName()))));
        }

        // add `Client` to the URL query string
        if (getClient() != null) {
            joiner.add(String.format("%sClient%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getClient()))));
        }

        // add `LastActivityDate` to the URL query string
        if (getLastActivityDate() != null) {
            joiner.add(String.format("%sLastActivityDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastActivityDate()))));
        }

        // add `LastPlaybackCheckIn` to the URL query string
        if (getLastPlaybackCheckIn() != null) {
            joiner.add(String.format("%sLastPlaybackCheckIn%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastPlaybackCheckIn()))));
        }

        // add `DeviceName` to the URL query string
        if (getDeviceName() != null) {
            joiner.add(String.format("%sDeviceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceName()))));
        }

        // add `DeviceType` to the URL query string
        if (getDeviceType() != null) {
            joiner.add(String.format("%sDeviceType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceType()))));
        }

        // add `NowPlayingItem` to the URL query string
        if (getNowPlayingItem() != null) {
            joiner.add(getNowPlayingItem().toUrlQueryString(prefix + "NowPlayingItem" + suffix));
        }

        // add `FullNowPlayingItem` to the URL query string
        if (getFullNowPlayingItem() != null) {
            joiner.add(getFullNowPlayingItem().toUrlQueryString(prefix + "FullNowPlayingItem" + suffix));
        }

        // add `NowViewingItem` to the URL query string
        if (getNowViewingItem() != null) {
            joiner.add(getNowViewingItem().toUrlQueryString(prefix + "NowViewingItem" + suffix));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format("%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        // add `ApplicationVersion` to the URL query string
        if (getApplicationVersion() != null) {
            joiner.add(String.format("%sApplicationVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getApplicationVersion()))));
        }

        // add `TranscodingInfo` to the URL query string
        if (getTranscodingInfo() != null) {
            joiner.add(getTranscodingInfo().toUrlQueryString(prefix + "TranscodingInfo" + suffix));
        }

        // add `IsActive` to the URL query string
        if (getIsActive() != null) {
            joiner.add(String.format("%sIsActive%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsActive()))));
        }

        // add `SupportsMediaControl` to the URL query string
        if (getSupportsMediaControl() != null) {
            joiner.add(String.format("%sSupportsMediaControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsMediaControl()))));
        }

        // add `SupportsRemoteControl` to the URL query string
        if (getSupportsRemoteControl() != null) {
            joiner.add(String.format("%sSupportsRemoteControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsRemoteControl()))));
        }

        // add `NowPlayingQueue` to the URL query string
        if (getNowPlayingQueue() != null) {
            for (int i = 0; i < getNowPlayingQueue().size(); i++) {
                if (getNowPlayingQueue().get(i) != null) {
                    joiner.add(getNowPlayingQueue().get(i).toUrlQueryString(String.format("%sNowPlayingQueue%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `NowPlayingQueueFullItems` to the URL query string
        if (getNowPlayingQueueFullItems() != null) {
            for (int i = 0; i < getNowPlayingQueueFullItems().size(); i++) {
                if (getNowPlayingQueueFullItems().get(i) != null) {
                    joiner.add(getNowPlayingQueueFullItems().get(i).toUrlQueryString(String.format(
                            "%sNowPlayingQueueFullItems%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `HasCustomDeviceName` to the URL query string
        if (getHasCustomDeviceName() != null) {
            joiner.add(String.format("%sHasCustomDeviceName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasCustomDeviceName()))));
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format("%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format("%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        // add `UserPrimaryImageTag` to the URL query string
        if (getUserPrimaryImageTag() != null) {
            joiner.add(String.format("%sUserPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserPrimaryImageTag()))));
        }

        // add `SupportedCommands` to the URL query string
        if (getSupportedCommands() != null) {
            for (int i = 0; i < getSupportedCommands().size(); i++) {
                if (getSupportedCommands().get(i) != null) {
                    joiner.add(String.format("%sSupportedCommands%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getSupportedCommands().get(i)))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private SessionInfo instance;

        public Builder() {
            this(new SessionInfo());
        }

        protected Builder(SessionInfo instance) {
            this.instance = instance;
        }

        public SessionInfo.Builder playState(PlayerStateInfo playState) {
            this.instance.playState = playState;
            return this;
        }

        public SessionInfo.Builder additionalUsers(List<SessionUserInfo> additionalUsers) {
            this.instance.additionalUsers = additionalUsers;
            return this;
        }

        public SessionInfo.Builder capabilities(ClientCapabilities capabilities) {
            this.instance.capabilities = capabilities;
            return this;
        }

        public SessionInfo.Builder remoteEndPoint(String remoteEndPoint) {
            this.instance.remoteEndPoint = remoteEndPoint;
            return this;
        }

        public SessionInfo.Builder playableMediaTypes(List<String> playableMediaTypes) {
            this.instance.playableMediaTypes = playableMediaTypes;
            return this;
        }

        public SessionInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public SessionInfo.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public SessionInfo.Builder userName(String userName) {
            this.instance.userName = userName;
            return this;
        }

        public SessionInfo.Builder client(String client) {
            this.instance.client = client;
            return this;
        }

        public SessionInfo.Builder lastActivityDate(OffsetDateTime lastActivityDate) {
            this.instance.lastActivityDate = lastActivityDate;
            return this;
        }

        public SessionInfo.Builder lastPlaybackCheckIn(OffsetDateTime lastPlaybackCheckIn) {
            this.instance.lastPlaybackCheckIn = lastPlaybackCheckIn;
            return this;
        }

        public SessionInfo.Builder deviceName(String deviceName) {
            this.instance.deviceName = deviceName;
            return this;
        }

        public SessionInfo.Builder deviceType(String deviceType) {
            this.instance.deviceType = deviceType;
            return this;
        }

        public SessionInfo.Builder nowPlayingItem(BaseItemDto nowPlayingItem) {
            this.instance.nowPlayingItem = nowPlayingItem;
            return this;
        }

        public SessionInfo.Builder fullNowPlayingItem(BaseItem fullNowPlayingItem) {
            this.instance.fullNowPlayingItem = fullNowPlayingItem;
            return this;
        }

        public SessionInfo.Builder nowViewingItem(BaseItemDto nowViewingItem) {
            this.instance.nowViewingItem = nowViewingItem;
            return this;
        }

        public SessionInfo.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public SessionInfo.Builder applicationVersion(String applicationVersion) {
            this.instance.applicationVersion = applicationVersion;
            return this;
        }

        public SessionInfo.Builder transcodingInfo(TranscodingInfo transcodingInfo) {
            this.instance.transcodingInfo = transcodingInfo;
            return this;
        }

        public SessionInfo.Builder isActive(Boolean isActive) {
            this.instance.isActive = isActive;
            return this;
        }

        public SessionInfo.Builder supportsMediaControl(Boolean supportsMediaControl) {
            this.instance.supportsMediaControl = supportsMediaControl;
            return this;
        }

        public SessionInfo.Builder supportsRemoteControl(Boolean supportsRemoteControl) {
            this.instance.supportsRemoteControl = supportsRemoteControl;
            return this;
        }

        public SessionInfo.Builder nowPlayingQueue(List<QueueItem> nowPlayingQueue) {
            this.instance.nowPlayingQueue = nowPlayingQueue;
            return this;
        }

        public SessionInfo.Builder nowPlayingQueueFullItems(List<BaseItemDto> nowPlayingQueueFullItems) {
            this.instance.nowPlayingQueueFullItems = nowPlayingQueueFullItems;
            return this;
        }

        public SessionInfo.Builder hasCustomDeviceName(Boolean hasCustomDeviceName) {
            this.instance.hasCustomDeviceName = hasCustomDeviceName;
            return this;
        }

        public SessionInfo.Builder playlistItemId(String playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        public SessionInfo.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        public SessionInfo.Builder userPrimaryImageTag(String userPrimaryImageTag) {
            this.instance.userPrimaryImageTag = userPrimaryImageTag;
            return this;
        }

        public SessionInfo.Builder supportedCommands(List<GeneralCommandType> supportedCommands) {
            this.instance.supportedCommands = supportedCommands;
            return this;
        }

        /**
         * returns a built SessionInfo instance.
         *
         * The builder is not reusable.
         */
        public SessionInfo build() {
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
    public static SessionInfo.Builder builder() {
        return new SessionInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SessionInfo.Builder toBuilder() {
        return new SessionInfo.Builder().playState(getPlayState()).additionalUsers(getAdditionalUsers())
                .capabilities(getCapabilities()).remoteEndPoint(getRemoteEndPoint())
                .playableMediaTypes(getPlayableMediaTypes()).id(getId()).userId(getUserId()).userName(getUserName())
                .client(getClient()).lastActivityDate(getLastActivityDate())
                .lastPlaybackCheckIn(getLastPlaybackCheckIn()).deviceName(getDeviceName()).deviceType(getDeviceType())
                .nowPlayingItem(getNowPlayingItem()).fullNowPlayingItem(getFullNowPlayingItem())
                .nowViewingItem(getNowViewingItem()).deviceId(getDeviceId()).applicationVersion(getApplicationVersion())
                .transcodingInfo(getTranscodingInfo()).isActive(getIsActive())
                .supportsMediaControl(getSupportsMediaControl()).supportsRemoteControl(getSupportsRemoteControl())
                .nowPlayingQueue(getNowPlayingQueue()).nowPlayingQueueFullItems(getNowPlayingQueueFullItems())
                .hasCustomDeviceName(getHasCustomDeviceName()).playlistItemId(getPlaylistItemId())
                .serverId(getServerId()).userPrimaryImageTag(getUserPrimaryImageTag())
                .supportedCommands(getSupportedCommands());
    }
}
