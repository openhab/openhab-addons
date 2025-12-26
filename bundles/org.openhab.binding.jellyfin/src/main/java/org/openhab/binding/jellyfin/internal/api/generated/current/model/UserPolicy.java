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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * UserPolicy
 */
@JsonPropertyOrder({ UserPolicy.JSON_PROPERTY_IS_ADMINISTRATOR, UserPolicy.JSON_PROPERTY_IS_HIDDEN,
        UserPolicy.JSON_PROPERTY_ENABLE_COLLECTION_MANAGEMENT, UserPolicy.JSON_PROPERTY_ENABLE_SUBTITLE_MANAGEMENT,
        UserPolicy.JSON_PROPERTY_ENABLE_LYRIC_MANAGEMENT, UserPolicy.JSON_PROPERTY_IS_DISABLED,
        UserPolicy.JSON_PROPERTY_MAX_PARENTAL_RATING, UserPolicy.JSON_PROPERTY_MAX_PARENTAL_SUB_RATING,
        UserPolicy.JSON_PROPERTY_BLOCKED_TAGS, UserPolicy.JSON_PROPERTY_ALLOWED_TAGS,
        UserPolicy.JSON_PROPERTY_ENABLE_USER_PREFERENCE_ACCESS, UserPolicy.JSON_PROPERTY_ACCESS_SCHEDULES,
        UserPolicy.JSON_PROPERTY_BLOCK_UNRATED_ITEMS, UserPolicy.JSON_PROPERTY_ENABLE_REMOTE_CONTROL_OF_OTHER_USERS,
        UserPolicy.JSON_PROPERTY_ENABLE_SHARED_DEVICE_CONTROL, UserPolicy.JSON_PROPERTY_ENABLE_REMOTE_ACCESS,
        UserPolicy.JSON_PROPERTY_ENABLE_LIVE_TV_MANAGEMENT, UserPolicy.JSON_PROPERTY_ENABLE_LIVE_TV_ACCESS,
        UserPolicy.JSON_PROPERTY_ENABLE_MEDIA_PLAYBACK, UserPolicy.JSON_PROPERTY_ENABLE_AUDIO_PLAYBACK_TRANSCODING,
        UserPolicy.JSON_PROPERTY_ENABLE_VIDEO_PLAYBACK_TRANSCODING, UserPolicy.JSON_PROPERTY_ENABLE_PLAYBACK_REMUXING,
        UserPolicy.JSON_PROPERTY_FORCE_REMOTE_SOURCE_TRANSCODING, UserPolicy.JSON_PROPERTY_ENABLE_CONTENT_DELETION,
        UserPolicy.JSON_PROPERTY_ENABLE_CONTENT_DELETION_FROM_FOLDERS,
        UserPolicy.JSON_PROPERTY_ENABLE_CONTENT_DOWNLOADING, UserPolicy.JSON_PROPERTY_ENABLE_SYNC_TRANSCODING,
        UserPolicy.JSON_PROPERTY_ENABLE_MEDIA_CONVERSION, UserPolicy.JSON_PROPERTY_ENABLED_DEVICES,
        UserPolicy.JSON_PROPERTY_ENABLE_ALL_DEVICES, UserPolicy.JSON_PROPERTY_ENABLED_CHANNELS,
        UserPolicy.JSON_PROPERTY_ENABLE_ALL_CHANNELS, UserPolicy.JSON_PROPERTY_ENABLED_FOLDERS,
        UserPolicy.JSON_PROPERTY_ENABLE_ALL_FOLDERS, UserPolicy.JSON_PROPERTY_INVALID_LOGIN_ATTEMPT_COUNT,
        UserPolicy.JSON_PROPERTY_LOGIN_ATTEMPTS_BEFORE_LOCKOUT, UserPolicy.JSON_PROPERTY_MAX_ACTIVE_SESSIONS,
        UserPolicy.JSON_PROPERTY_ENABLE_PUBLIC_SHARING, UserPolicy.JSON_PROPERTY_BLOCKED_MEDIA_FOLDERS,
        UserPolicy.JSON_PROPERTY_BLOCKED_CHANNELS, UserPolicy.JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT,
        UserPolicy.JSON_PROPERTY_AUTHENTICATION_PROVIDER_ID, UserPolicy.JSON_PROPERTY_PASSWORD_RESET_PROVIDER_ID,
        UserPolicy.JSON_PROPERTY_SYNC_PLAY_ACCESS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UserPolicy {
    public static final String JSON_PROPERTY_IS_ADMINISTRATOR = "IsAdministrator";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isAdministrator;

    public static final String JSON_PROPERTY_IS_HIDDEN = "IsHidden";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isHidden;

    public static final String JSON_PROPERTY_ENABLE_COLLECTION_MANAGEMENT = "EnableCollectionManagement";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableCollectionManagement = false;

    public static final String JSON_PROPERTY_ENABLE_SUBTITLE_MANAGEMENT = "EnableSubtitleManagement";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSubtitleManagement = false;

    public static final String JSON_PROPERTY_ENABLE_LYRIC_MANAGEMENT = "EnableLyricManagement";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableLyricManagement = false;

    public static final String JSON_PROPERTY_IS_DISABLED = "IsDisabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isDisabled;

    public static final String JSON_PROPERTY_MAX_PARENTAL_RATING = "MaxParentalRating";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxParentalRating;

    public static final String JSON_PROPERTY_MAX_PARENTAL_SUB_RATING = "MaxParentalSubRating";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxParentalSubRating;

    public static final String JSON_PROPERTY_BLOCKED_TAGS = "BlockedTags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> blockedTags;

    public static final String JSON_PROPERTY_ALLOWED_TAGS = "AllowedTags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> allowedTags;

    public static final String JSON_PROPERTY_ENABLE_USER_PREFERENCE_ACCESS = "EnableUserPreferenceAccess";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableUserPreferenceAccess;

    public static final String JSON_PROPERTY_ACCESS_SCHEDULES = "AccessSchedules";
    @org.eclipse.jdt.annotation.NonNull
    private List<AccessSchedule> accessSchedules;

    public static final String JSON_PROPERTY_BLOCK_UNRATED_ITEMS = "BlockUnratedItems";
    @org.eclipse.jdt.annotation.NonNull
    private List<UnratedItem> blockUnratedItems;

    public static final String JSON_PROPERTY_ENABLE_REMOTE_CONTROL_OF_OTHER_USERS = "EnableRemoteControlOfOtherUsers";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableRemoteControlOfOtherUsers;

    public static final String JSON_PROPERTY_ENABLE_SHARED_DEVICE_CONTROL = "EnableSharedDeviceControl";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSharedDeviceControl;

    public static final String JSON_PROPERTY_ENABLE_REMOTE_ACCESS = "EnableRemoteAccess";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableRemoteAccess;

    public static final String JSON_PROPERTY_ENABLE_LIVE_TV_MANAGEMENT = "EnableLiveTvManagement";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableLiveTvManagement;

    public static final String JSON_PROPERTY_ENABLE_LIVE_TV_ACCESS = "EnableLiveTvAccess";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableLiveTvAccess;

    public static final String JSON_PROPERTY_ENABLE_MEDIA_PLAYBACK = "EnableMediaPlayback";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableMediaPlayback;

    public static final String JSON_PROPERTY_ENABLE_AUDIO_PLAYBACK_TRANSCODING = "EnableAudioPlaybackTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAudioPlaybackTranscoding;

    public static final String JSON_PROPERTY_ENABLE_VIDEO_PLAYBACK_TRANSCODING = "EnableVideoPlaybackTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableVideoPlaybackTranscoding;

    public static final String JSON_PROPERTY_ENABLE_PLAYBACK_REMUXING = "EnablePlaybackRemuxing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enablePlaybackRemuxing;

    public static final String JSON_PROPERTY_FORCE_REMOTE_SOURCE_TRANSCODING = "ForceRemoteSourceTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean forceRemoteSourceTranscoding;

    public static final String JSON_PROPERTY_ENABLE_CONTENT_DELETION = "EnableContentDeletion";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableContentDeletion;

    public static final String JSON_PROPERTY_ENABLE_CONTENT_DELETION_FROM_FOLDERS = "EnableContentDeletionFromFolders";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> enableContentDeletionFromFolders;

    public static final String JSON_PROPERTY_ENABLE_CONTENT_DOWNLOADING = "EnableContentDownloading";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableContentDownloading;

    public static final String JSON_PROPERTY_ENABLE_SYNC_TRANSCODING = "EnableSyncTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSyncTranscoding;

    public static final String JSON_PROPERTY_ENABLE_MEDIA_CONVERSION = "EnableMediaConversion";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableMediaConversion;

    public static final String JSON_PROPERTY_ENABLED_DEVICES = "EnabledDevices";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> enabledDevices;

    public static final String JSON_PROPERTY_ENABLE_ALL_DEVICES = "EnableAllDevices";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAllDevices;

    public static final String JSON_PROPERTY_ENABLED_CHANNELS = "EnabledChannels";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> enabledChannels;

    public static final String JSON_PROPERTY_ENABLE_ALL_CHANNELS = "EnableAllChannels";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAllChannels;

    public static final String JSON_PROPERTY_ENABLED_FOLDERS = "EnabledFolders";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> enabledFolders;

    public static final String JSON_PROPERTY_ENABLE_ALL_FOLDERS = "EnableAllFolders";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAllFolders;

    public static final String JSON_PROPERTY_INVALID_LOGIN_ATTEMPT_COUNT = "InvalidLoginAttemptCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer invalidLoginAttemptCount;

    public static final String JSON_PROPERTY_LOGIN_ATTEMPTS_BEFORE_LOCKOUT = "LoginAttemptsBeforeLockout";
    @org.eclipse.jdt.annotation.NonNull
    private Integer loginAttemptsBeforeLockout;

    public static final String JSON_PROPERTY_MAX_ACTIVE_SESSIONS = "MaxActiveSessions";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxActiveSessions;

    public static final String JSON_PROPERTY_ENABLE_PUBLIC_SHARING = "EnablePublicSharing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enablePublicSharing;

    public static final String JSON_PROPERTY_BLOCKED_MEDIA_FOLDERS = "BlockedMediaFolders";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> blockedMediaFolders;

    public static final String JSON_PROPERTY_BLOCKED_CHANNELS = "BlockedChannels";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> blockedChannels;

    public static final String JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT = "RemoteClientBitrateLimit";
    @org.eclipse.jdt.annotation.NonNull
    private Integer remoteClientBitrateLimit;

    public static final String JSON_PROPERTY_AUTHENTICATION_PROVIDER_ID = "AuthenticationProviderId";
    @org.eclipse.jdt.annotation.Nullable
    private String authenticationProviderId;

    public static final String JSON_PROPERTY_PASSWORD_RESET_PROVIDER_ID = "PasswordResetProviderId";
    @org.eclipse.jdt.annotation.Nullable
    private String passwordResetProviderId;

    public static final String JSON_PROPERTY_SYNC_PLAY_ACCESS = "SyncPlayAccess";
    @org.eclipse.jdt.annotation.NonNull
    private SyncPlayUserAccessType syncPlayAccess;

    public UserPolicy() {
    }

    public UserPolicy isAdministrator(@org.eclipse.jdt.annotation.NonNull Boolean isAdministrator) {
        this.isAdministrator = isAdministrator;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is administrator.
     * 
     * @return isAdministrator
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_ADMINISTRATOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsAdministrator() {
        return isAdministrator;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_ADMINISTRATOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAdministrator(@org.eclipse.jdt.annotation.NonNull Boolean isAdministrator) {
        this.isAdministrator = isAdministrator;
    }

    public UserPolicy isHidden(@org.eclipse.jdt.annotation.NonNull Boolean isHidden) {
        this.isHidden = isHidden;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is hidden.
     * 
     * @return isHidden
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_HIDDEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsHidden() {
        return isHidden;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_HIDDEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsHidden(@org.eclipse.jdt.annotation.NonNull Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public UserPolicy enableCollectionManagement(
            @org.eclipse.jdt.annotation.NonNull Boolean enableCollectionManagement) {
        this.enableCollectionManagement = enableCollectionManagement;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can manage collections.
     * 
     * @return enableCollectionManagement
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_COLLECTION_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableCollectionManagement() {
        return enableCollectionManagement;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_COLLECTION_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableCollectionManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableCollectionManagement) {
        this.enableCollectionManagement = enableCollectionManagement;
    }

    public UserPolicy enableSubtitleManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableSubtitleManagement) {
        this.enableSubtitleManagement = enableSubtitleManagement;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can manage subtitles.
     * 
     * @return enableSubtitleManagement
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_SUBTITLE_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSubtitleManagement() {
        return enableSubtitleManagement;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_SUBTITLE_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSubtitleManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableSubtitleManagement) {
        this.enableSubtitleManagement = enableSubtitleManagement;
    }

    public UserPolicy enableLyricManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableLyricManagement) {
        this.enableLyricManagement = enableLyricManagement;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this user can manage lyrics.
     * 
     * @return enableLyricManagement
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_LYRIC_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableLyricManagement() {
        return enableLyricManagement;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_LYRIC_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableLyricManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableLyricManagement) {
        this.enableLyricManagement = enableLyricManagement;
    }

    public UserPolicy isDisabled(@org.eclipse.jdt.annotation.NonNull Boolean isDisabled) {
        this.isDisabled = isDisabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is disabled.
     * 
     * @return isDisabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_DISABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsDisabled() {
        return isDisabled;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_DISABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsDisabled(@org.eclipse.jdt.annotation.NonNull Boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public UserPolicy maxParentalRating(@org.eclipse.jdt.annotation.NonNull Integer maxParentalRating) {
        this.maxParentalRating = maxParentalRating;
        return this;
    }

    /**
     * Gets or sets the max parental rating.
     * 
     * @return maxParentalRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MAX_PARENTAL_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxParentalRating() {
        return maxParentalRating;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_PARENTAL_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxParentalRating(@org.eclipse.jdt.annotation.NonNull Integer maxParentalRating) {
        this.maxParentalRating = maxParentalRating;
    }

    public UserPolicy maxParentalSubRating(@org.eclipse.jdt.annotation.NonNull Integer maxParentalSubRating) {
        this.maxParentalSubRating = maxParentalSubRating;
        return this;
    }

    /**
     * Get maxParentalSubRating
     * 
     * @return maxParentalSubRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MAX_PARENTAL_SUB_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxParentalSubRating() {
        return maxParentalSubRating;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_PARENTAL_SUB_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxParentalSubRating(@org.eclipse.jdt.annotation.NonNull Integer maxParentalSubRating) {
        this.maxParentalSubRating = maxParentalSubRating;
    }

    public UserPolicy blockedTags(@org.eclipse.jdt.annotation.NonNull List<String> blockedTags) {
        this.blockedTags = blockedTags;
        return this;
    }

    public UserPolicy addBlockedTagsItem(String blockedTagsItem) {
        if (this.blockedTags == null) {
            this.blockedTags = new ArrayList<>();
        }
        this.blockedTags.add(blockedTagsItem);
        return this;
    }

    /**
     * Get blockedTags
     * 
     * @return blockedTags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BLOCKED_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getBlockedTags() {
        return blockedTags;
    }

    @JsonProperty(value = JSON_PROPERTY_BLOCKED_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlockedTags(@org.eclipse.jdt.annotation.NonNull List<String> blockedTags) {
        this.blockedTags = blockedTags;
    }

    public UserPolicy allowedTags(@org.eclipse.jdt.annotation.NonNull List<String> allowedTags) {
        this.allowedTags = allowedTags;
        return this;
    }

    public UserPolicy addAllowedTagsItem(String allowedTagsItem) {
        if (this.allowedTags == null) {
            this.allowedTags = new ArrayList<>();
        }
        this.allowedTags.add(allowedTagsItem);
        return this;
    }

    /**
     * Get allowedTags
     * 
     * @return allowedTags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALLOWED_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getAllowedTags() {
        return allowedTags;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOWED_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowedTags(@org.eclipse.jdt.annotation.NonNull List<String> allowedTags) {
        this.allowedTags = allowedTags;
    }

    public UserPolicy enableUserPreferenceAccess(
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserPreferenceAccess) {
        this.enableUserPreferenceAccess = enableUserPreferenceAccess;
        return this;
    }

    /**
     * Get enableUserPreferenceAccess
     * 
     * @return enableUserPreferenceAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_USER_PREFERENCE_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableUserPreferenceAccess() {
        return enableUserPreferenceAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_USER_PREFERENCE_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableUserPreferenceAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableUserPreferenceAccess) {
        this.enableUserPreferenceAccess = enableUserPreferenceAccess;
    }

    public UserPolicy accessSchedules(@org.eclipse.jdt.annotation.NonNull List<AccessSchedule> accessSchedules) {
        this.accessSchedules = accessSchedules;
        return this;
    }

    public UserPolicy addAccessSchedulesItem(AccessSchedule accessSchedulesItem) {
        if (this.accessSchedules == null) {
            this.accessSchedules = new ArrayList<>();
        }
        this.accessSchedules.add(accessSchedulesItem);
        return this;
    }

    /**
     * Get accessSchedules
     * 
     * @return accessSchedules
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ACCESS_SCHEDULES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<AccessSchedule> getAccessSchedules() {
        return accessSchedules;
    }

    @JsonProperty(value = JSON_PROPERTY_ACCESS_SCHEDULES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccessSchedules(@org.eclipse.jdt.annotation.NonNull List<AccessSchedule> accessSchedules) {
        this.accessSchedules = accessSchedules;
    }

    public UserPolicy blockUnratedItems(@org.eclipse.jdt.annotation.NonNull List<UnratedItem> blockUnratedItems) {
        this.blockUnratedItems = blockUnratedItems;
        return this;
    }

    public UserPolicy addBlockUnratedItemsItem(UnratedItem blockUnratedItemsItem) {
        if (this.blockUnratedItems == null) {
            this.blockUnratedItems = new ArrayList<>();
        }
        this.blockUnratedItems.add(blockUnratedItemsItem);
        return this;
    }

    /**
     * Get blockUnratedItems
     * 
     * @return blockUnratedItems
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BLOCK_UNRATED_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UnratedItem> getBlockUnratedItems() {
        return blockUnratedItems;
    }

    @JsonProperty(value = JSON_PROPERTY_BLOCK_UNRATED_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlockUnratedItems(@org.eclipse.jdt.annotation.NonNull List<UnratedItem> blockUnratedItems) {
        this.blockUnratedItems = blockUnratedItems;
    }

    public UserPolicy enableRemoteControlOfOtherUsers(
            @org.eclipse.jdt.annotation.NonNull Boolean enableRemoteControlOfOtherUsers) {
        this.enableRemoteControlOfOtherUsers = enableRemoteControlOfOtherUsers;
        return this;
    }

    /**
     * Get enableRemoteControlOfOtherUsers
     * 
     * @return enableRemoteControlOfOtherUsers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_CONTROL_OF_OTHER_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableRemoteControlOfOtherUsers() {
        return enableRemoteControlOfOtherUsers;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_CONTROL_OF_OTHER_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableRemoteControlOfOtherUsers(
            @org.eclipse.jdt.annotation.NonNull Boolean enableRemoteControlOfOtherUsers) {
        this.enableRemoteControlOfOtherUsers = enableRemoteControlOfOtherUsers;
    }

    public UserPolicy enableSharedDeviceControl(@org.eclipse.jdt.annotation.NonNull Boolean enableSharedDeviceControl) {
        this.enableSharedDeviceControl = enableSharedDeviceControl;
        return this;
    }

    /**
     * Get enableSharedDeviceControl
     * 
     * @return enableSharedDeviceControl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_SHARED_DEVICE_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSharedDeviceControl() {
        return enableSharedDeviceControl;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_SHARED_DEVICE_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSharedDeviceControl(@org.eclipse.jdt.annotation.NonNull Boolean enableSharedDeviceControl) {
        this.enableSharedDeviceControl = enableSharedDeviceControl;
    }

    public UserPolicy enableRemoteAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
        return this;
    }

    /**
     * Get enableRemoteAccess
     * 
     * @return enableRemoteAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableRemoteAccess() {
        return enableRemoteAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_REMOTE_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableRemoteAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableRemoteAccess) {
        this.enableRemoteAccess = enableRemoteAccess;
    }

    public UserPolicy enableLiveTvManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableLiveTvManagement) {
        this.enableLiveTvManagement = enableLiveTvManagement;
        return this;
    }

    /**
     * Get enableLiveTvManagement
     * 
     * @return enableLiveTvManagement
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_LIVE_TV_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableLiveTvManagement() {
        return enableLiveTvManagement;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_LIVE_TV_MANAGEMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableLiveTvManagement(@org.eclipse.jdt.annotation.NonNull Boolean enableLiveTvManagement) {
        this.enableLiveTvManagement = enableLiveTvManagement;
    }

    public UserPolicy enableLiveTvAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableLiveTvAccess) {
        this.enableLiveTvAccess = enableLiveTvAccess;
        return this;
    }

    /**
     * Get enableLiveTvAccess
     * 
     * @return enableLiveTvAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_LIVE_TV_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableLiveTvAccess() {
        return enableLiveTvAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_LIVE_TV_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableLiveTvAccess(@org.eclipse.jdt.annotation.NonNull Boolean enableLiveTvAccess) {
        this.enableLiveTvAccess = enableLiveTvAccess;
    }

    public UserPolicy enableMediaPlayback(@org.eclipse.jdt.annotation.NonNull Boolean enableMediaPlayback) {
        this.enableMediaPlayback = enableMediaPlayback;
        return this;
    }

    /**
     * Get enableMediaPlayback
     * 
     * @return enableMediaPlayback
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_MEDIA_PLAYBACK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableMediaPlayback() {
        return enableMediaPlayback;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_MEDIA_PLAYBACK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMediaPlayback(@org.eclipse.jdt.annotation.NonNull Boolean enableMediaPlayback) {
        this.enableMediaPlayback = enableMediaPlayback;
    }

    public UserPolicy enableAudioPlaybackTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioPlaybackTranscoding) {
        this.enableAudioPlaybackTranscoding = enableAudioPlaybackTranscoding;
        return this;
    }

    /**
     * Get enableAudioPlaybackTranscoding
     * 
     * @return enableAudioPlaybackTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUDIO_PLAYBACK_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAudioPlaybackTranscoding() {
        return enableAudioPlaybackTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUDIO_PLAYBACK_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAudioPlaybackTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioPlaybackTranscoding) {
        this.enableAudioPlaybackTranscoding = enableAudioPlaybackTranscoding;
    }

    public UserPolicy enableVideoPlaybackTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean enableVideoPlaybackTranscoding) {
        this.enableVideoPlaybackTranscoding = enableVideoPlaybackTranscoding;
        return this;
    }

    /**
     * Get enableVideoPlaybackTranscoding
     * 
     * @return enableVideoPlaybackTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_VIDEO_PLAYBACK_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableVideoPlaybackTranscoding() {
        return enableVideoPlaybackTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_VIDEO_PLAYBACK_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableVideoPlaybackTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean enableVideoPlaybackTranscoding) {
        this.enableVideoPlaybackTranscoding = enableVideoPlaybackTranscoding;
    }

    public UserPolicy enablePlaybackRemuxing(@org.eclipse.jdt.annotation.NonNull Boolean enablePlaybackRemuxing) {
        this.enablePlaybackRemuxing = enablePlaybackRemuxing;
        return this;
    }

    /**
     * Get enablePlaybackRemuxing
     * 
     * @return enablePlaybackRemuxing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_PLAYBACK_REMUXING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePlaybackRemuxing() {
        return enablePlaybackRemuxing;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_PLAYBACK_REMUXING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePlaybackRemuxing(@org.eclipse.jdt.annotation.NonNull Boolean enablePlaybackRemuxing) {
        this.enablePlaybackRemuxing = enablePlaybackRemuxing;
    }

    public UserPolicy forceRemoteSourceTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean forceRemoteSourceTranscoding) {
        this.forceRemoteSourceTranscoding = forceRemoteSourceTranscoding;
        return this;
    }

    /**
     * Get forceRemoteSourceTranscoding
     * 
     * @return forceRemoteSourceTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FORCE_REMOTE_SOURCE_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getForceRemoteSourceTranscoding() {
        return forceRemoteSourceTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_FORCE_REMOTE_SOURCE_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setForceRemoteSourceTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean forceRemoteSourceTranscoding) {
        this.forceRemoteSourceTranscoding = forceRemoteSourceTranscoding;
    }

    public UserPolicy enableContentDeletion(@org.eclipse.jdt.annotation.NonNull Boolean enableContentDeletion) {
        this.enableContentDeletion = enableContentDeletion;
        return this;
    }

    /**
     * Get enableContentDeletion
     * 
     * @return enableContentDeletion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_CONTENT_DELETION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableContentDeletion() {
        return enableContentDeletion;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_CONTENT_DELETION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableContentDeletion(@org.eclipse.jdt.annotation.NonNull Boolean enableContentDeletion) {
        this.enableContentDeletion = enableContentDeletion;
    }

    public UserPolicy enableContentDeletionFromFolders(
            @org.eclipse.jdt.annotation.NonNull List<String> enableContentDeletionFromFolders) {
        this.enableContentDeletionFromFolders = enableContentDeletionFromFolders;
        return this;
    }

    public UserPolicy addEnableContentDeletionFromFoldersItem(String enableContentDeletionFromFoldersItem) {
        if (this.enableContentDeletionFromFolders == null) {
            this.enableContentDeletionFromFolders = new ArrayList<>();
        }
        this.enableContentDeletionFromFolders.add(enableContentDeletionFromFoldersItem);
        return this;
    }

    /**
     * Get enableContentDeletionFromFolders
     * 
     * @return enableContentDeletionFromFolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_CONTENT_DELETION_FROM_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getEnableContentDeletionFromFolders() {
        return enableContentDeletionFromFolders;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_CONTENT_DELETION_FROM_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableContentDeletionFromFolders(
            @org.eclipse.jdt.annotation.NonNull List<String> enableContentDeletionFromFolders) {
        this.enableContentDeletionFromFolders = enableContentDeletionFromFolders;
    }

    public UserPolicy enableContentDownloading(@org.eclipse.jdt.annotation.NonNull Boolean enableContentDownloading) {
        this.enableContentDownloading = enableContentDownloading;
        return this;
    }

    /**
     * Get enableContentDownloading
     * 
     * @return enableContentDownloading
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_CONTENT_DOWNLOADING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableContentDownloading() {
        return enableContentDownloading;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_CONTENT_DOWNLOADING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableContentDownloading(@org.eclipse.jdt.annotation.NonNull Boolean enableContentDownloading) {
        this.enableContentDownloading = enableContentDownloading;
    }

    public UserPolicy enableSyncTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean enableSyncTranscoding) {
        this.enableSyncTranscoding = enableSyncTranscoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [enable synchronize].
     * 
     * @return enableSyncTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_SYNC_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSyncTranscoding() {
        return enableSyncTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_SYNC_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSyncTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean enableSyncTranscoding) {
        this.enableSyncTranscoding = enableSyncTranscoding;
    }

    public UserPolicy enableMediaConversion(@org.eclipse.jdt.annotation.NonNull Boolean enableMediaConversion) {
        this.enableMediaConversion = enableMediaConversion;
        return this;
    }

    /**
     * Get enableMediaConversion
     * 
     * @return enableMediaConversion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_MEDIA_CONVERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableMediaConversion() {
        return enableMediaConversion;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_MEDIA_CONVERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMediaConversion(@org.eclipse.jdt.annotation.NonNull Boolean enableMediaConversion) {
        this.enableMediaConversion = enableMediaConversion;
    }

    public UserPolicy enabledDevices(@org.eclipse.jdt.annotation.NonNull List<String> enabledDevices) {
        this.enabledDevices = enabledDevices;
        return this;
    }

    public UserPolicy addEnabledDevicesItem(String enabledDevicesItem) {
        if (this.enabledDevices == null) {
            this.enabledDevices = new ArrayList<>();
        }
        this.enabledDevices.add(enabledDevicesItem);
        return this;
    }

    /**
     * Get enabledDevices
     * 
     * @return enabledDevices
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLED_DEVICES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getEnabledDevices() {
        return enabledDevices;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED_DEVICES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabledDevices(@org.eclipse.jdt.annotation.NonNull List<String> enabledDevices) {
        this.enabledDevices = enabledDevices;
    }

    public UserPolicy enableAllDevices(@org.eclipse.jdt.annotation.NonNull Boolean enableAllDevices) {
        this.enableAllDevices = enableAllDevices;
        return this;
    }

    /**
     * Get enableAllDevices
     * 
     * @return enableAllDevices
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_DEVICES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAllDevices() {
        return enableAllDevices;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_DEVICES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAllDevices(@org.eclipse.jdt.annotation.NonNull Boolean enableAllDevices) {
        this.enableAllDevices = enableAllDevices;
    }

    public UserPolicy enabledChannels(@org.eclipse.jdt.annotation.NonNull List<UUID> enabledChannels) {
        this.enabledChannels = enabledChannels;
        return this;
    }

    public UserPolicy addEnabledChannelsItem(UUID enabledChannelsItem) {
        if (this.enabledChannels == null) {
            this.enabledChannels = new ArrayList<>();
        }
        this.enabledChannels.add(enabledChannelsItem);
        return this;
    }

    /**
     * Get enabledChannels
     * 
     * @return enabledChannels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLED_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getEnabledChannels() {
        return enabledChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabledChannels(@org.eclipse.jdt.annotation.NonNull List<UUID> enabledChannels) {
        this.enabledChannels = enabledChannels;
    }

    public UserPolicy enableAllChannels(@org.eclipse.jdt.annotation.NonNull Boolean enableAllChannels) {
        this.enableAllChannels = enableAllChannels;
        return this;
    }

    /**
     * Get enableAllChannels
     * 
     * @return enableAllChannels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAllChannels() {
        return enableAllChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAllChannels(@org.eclipse.jdt.annotation.NonNull Boolean enableAllChannels) {
        this.enableAllChannels = enableAllChannels;
    }

    public UserPolicy enabledFolders(@org.eclipse.jdt.annotation.NonNull List<UUID> enabledFolders) {
        this.enabledFolders = enabledFolders;
        return this;
    }

    public UserPolicy addEnabledFoldersItem(UUID enabledFoldersItem) {
        if (this.enabledFolders == null) {
            this.enabledFolders = new ArrayList<>();
        }
        this.enabledFolders.add(enabledFoldersItem);
        return this;
    }

    /**
     * Get enabledFolders
     * 
     * @return enabledFolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLED_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getEnabledFolders() {
        return enabledFolders;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabledFolders(@org.eclipse.jdt.annotation.NonNull List<UUID> enabledFolders) {
        this.enabledFolders = enabledFolders;
    }

    public UserPolicy enableAllFolders(@org.eclipse.jdt.annotation.NonNull Boolean enableAllFolders) {
        this.enableAllFolders = enableAllFolders;
        return this;
    }

    /**
     * Get enableAllFolders
     * 
     * @return enableAllFolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAllFolders() {
        return enableAllFolders;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_ALL_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAllFolders(@org.eclipse.jdt.annotation.NonNull Boolean enableAllFolders) {
        this.enableAllFolders = enableAllFolders;
    }

    public UserPolicy invalidLoginAttemptCount(@org.eclipse.jdt.annotation.NonNull Integer invalidLoginAttemptCount) {
        this.invalidLoginAttemptCount = invalidLoginAttemptCount;
        return this;
    }

    /**
     * Get invalidLoginAttemptCount
     * 
     * @return invalidLoginAttemptCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INVALID_LOGIN_ATTEMPT_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getInvalidLoginAttemptCount() {
        return invalidLoginAttemptCount;
    }

    @JsonProperty(value = JSON_PROPERTY_INVALID_LOGIN_ATTEMPT_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInvalidLoginAttemptCount(@org.eclipse.jdt.annotation.NonNull Integer invalidLoginAttemptCount) {
        this.invalidLoginAttemptCount = invalidLoginAttemptCount;
    }

    public UserPolicy loginAttemptsBeforeLockout(
            @org.eclipse.jdt.annotation.NonNull Integer loginAttemptsBeforeLockout) {
        this.loginAttemptsBeforeLockout = loginAttemptsBeforeLockout;
        return this;
    }

    /**
     * Get loginAttemptsBeforeLockout
     * 
     * @return loginAttemptsBeforeLockout
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOGIN_ATTEMPTS_BEFORE_LOCKOUT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLoginAttemptsBeforeLockout() {
        return loginAttemptsBeforeLockout;
    }

    @JsonProperty(value = JSON_PROPERTY_LOGIN_ATTEMPTS_BEFORE_LOCKOUT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLoginAttemptsBeforeLockout(@org.eclipse.jdt.annotation.NonNull Integer loginAttemptsBeforeLockout) {
        this.loginAttemptsBeforeLockout = loginAttemptsBeforeLockout;
    }

    public UserPolicy maxActiveSessions(@org.eclipse.jdt.annotation.NonNull Integer maxActiveSessions) {
        this.maxActiveSessions = maxActiveSessions;
        return this;
    }

    /**
     * Get maxActiveSessions
     * 
     * @return maxActiveSessions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MAX_ACTIVE_SESSIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxActiveSessions() {
        return maxActiveSessions;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_ACTIVE_SESSIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxActiveSessions(@org.eclipse.jdt.annotation.NonNull Integer maxActiveSessions) {
        this.maxActiveSessions = maxActiveSessions;
    }

    public UserPolicy enablePublicSharing(@org.eclipse.jdt.annotation.NonNull Boolean enablePublicSharing) {
        this.enablePublicSharing = enablePublicSharing;
        return this;
    }

    /**
     * Get enablePublicSharing
     * 
     * @return enablePublicSharing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_PUBLIC_SHARING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnablePublicSharing() {
        return enablePublicSharing;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_PUBLIC_SHARING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnablePublicSharing(@org.eclipse.jdt.annotation.NonNull Boolean enablePublicSharing) {
        this.enablePublicSharing = enablePublicSharing;
    }

    public UserPolicy blockedMediaFolders(@org.eclipse.jdt.annotation.NonNull List<UUID> blockedMediaFolders) {
        this.blockedMediaFolders = blockedMediaFolders;
        return this;
    }

    public UserPolicy addBlockedMediaFoldersItem(UUID blockedMediaFoldersItem) {
        if (this.blockedMediaFolders == null) {
            this.blockedMediaFolders = new ArrayList<>();
        }
        this.blockedMediaFolders.add(blockedMediaFoldersItem);
        return this;
    }

    /**
     * Get blockedMediaFolders
     * 
     * @return blockedMediaFolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BLOCKED_MEDIA_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getBlockedMediaFolders() {
        return blockedMediaFolders;
    }

    @JsonProperty(value = JSON_PROPERTY_BLOCKED_MEDIA_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlockedMediaFolders(@org.eclipse.jdt.annotation.NonNull List<UUID> blockedMediaFolders) {
        this.blockedMediaFolders = blockedMediaFolders;
    }

    public UserPolicy blockedChannels(@org.eclipse.jdt.annotation.NonNull List<UUID> blockedChannels) {
        this.blockedChannels = blockedChannels;
        return this;
    }

    public UserPolicy addBlockedChannelsItem(UUID blockedChannelsItem) {
        if (this.blockedChannels == null) {
            this.blockedChannels = new ArrayList<>();
        }
        this.blockedChannels.add(blockedChannelsItem);
        return this;
    }

    /**
     * Get blockedChannels
     * 
     * @return blockedChannels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BLOCKED_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getBlockedChannels() {
        return blockedChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_BLOCKED_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlockedChannels(@org.eclipse.jdt.annotation.NonNull List<UUID> blockedChannels) {
        this.blockedChannels = blockedChannels;
    }

    public UserPolicy remoteClientBitrateLimit(@org.eclipse.jdt.annotation.NonNull Integer remoteClientBitrateLimit) {
        this.remoteClientBitrateLimit = remoteClientBitrateLimit;
        return this;
    }

    /**
     * Get remoteClientBitrateLimit
     * 
     * @return remoteClientBitrateLimit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getRemoteClientBitrateLimit() {
        return remoteClientBitrateLimit;
    }

    @JsonProperty(value = JSON_PROPERTY_REMOTE_CLIENT_BITRATE_LIMIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteClientBitrateLimit(@org.eclipse.jdt.annotation.NonNull Integer remoteClientBitrateLimit) {
        this.remoteClientBitrateLimit = remoteClientBitrateLimit;
    }

    public UserPolicy authenticationProviderId(@org.eclipse.jdt.annotation.Nullable String authenticationProviderId) {
        this.authenticationProviderId = authenticationProviderId;
        return this;
    }

    /**
     * Get authenticationProviderId
     * 
     * @return authenticationProviderId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUTHENTICATION_PROVIDER_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getAuthenticationProviderId() {
        return authenticationProviderId;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTHENTICATION_PROVIDER_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAuthenticationProviderId(@org.eclipse.jdt.annotation.Nullable String authenticationProviderId) {
        this.authenticationProviderId = authenticationProviderId;
    }

    public UserPolicy passwordResetProviderId(@org.eclipse.jdt.annotation.Nullable String passwordResetProviderId) {
        this.passwordResetProviderId = passwordResetProviderId;
        return this;
    }

    /**
     * Get passwordResetProviderId
     * 
     * @return passwordResetProviderId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PASSWORD_RESET_PROVIDER_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getPasswordResetProviderId() {
        return passwordResetProviderId;
    }

    @JsonProperty(value = JSON_PROPERTY_PASSWORD_RESET_PROVIDER_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPasswordResetProviderId(@org.eclipse.jdt.annotation.Nullable String passwordResetProviderId) {
        this.passwordResetProviderId = passwordResetProviderId;
    }

    public UserPolicy syncPlayAccess(@org.eclipse.jdt.annotation.NonNull SyncPlayUserAccessType syncPlayAccess) {
        this.syncPlayAccess = syncPlayAccess;
        return this;
    }

    /**
     * Gets or sets a value indicating what SyncPlay features the user can access.
     * 
     * @return syncPlayAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SYNC_PLAY_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SyncPlayUserAccessType getSyncPlayAccess() {
        return syncPlayAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_SYNC_PLAY_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSyncPlayAccess(@org.eclipse.jdt.annotation.NonNull SyncPlayUserAccessType syncPlayAccess) {
        this.syncPlayAccess = syncPlayAccess;
    }

    /**
     * Return true if this UserPolicy object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserPolicy userPolicy = (UserPolicy) o;
        return Objects.equals(this.isAdministrator, userPolicy.isAdministrator)
                && Objects.equals(this.isHidden, userPolicy.isHidden)
                && Objects.equals(this.enableCollectionManagement, userPolicy.enableCollectionManagement)
                && Objects.equals(this.enableSubtitleManagement, userPolicy.enableSubtitleManagement)
                && Objects.equals(this.enableLyricManagement, userPolicy.enableLyricManagement)
                && Objects.equals(this.isDisabled, userPolicy.isDisabled)
                && Objects.equals(this.maxParentalRating, userPolicy.maxParentalRating)
                && Objects.equals(this.maxParentalSubRating, userPolicy.maxParentalSubRating)
                && Objects.equals(this.blockedTags, userPolicy.blockedTags)
                && Objects.equals(this.allowedTags, userPolicy.allowedTags)
                && Objects.equals(this.enableUserPreferenceAccess, userPolicy.enableUserPreferenceAccess)
                && Objects.equals(this.accessSchedules, userPolicy.accessSchedules)
                && Objects.equals(this.blockUnratedItems, userPolicy.blockUnratedItems)
                && Objects.equals(this.enableRemoteControlOfOtherUsers, userPolicy.enableRemoteControlOfOtherUsers)
                && Objects.equals(this.enableSharedDeviceControl, userPolicy.enableSharedDeviceControl)
                && Objects.equals(this.enableRemoteAccess, userPolicy.enableRemoteAccess)
                && Objects.equals(this.enableLiveTvManagement, userPolicy.enableLiveTvManagement)
                && Objects.equals(this.enableLiveTvAccess, userPolicy.enableLiveTvAccess)
                && Objects.equals(this.enableMediaPlayback, userPolicy.enableMediaPlayback)
                && Objects.equals(this.enableAudioPlaybackTranscoding, userPolicy.enableAudioPlaybackTranscoding)
                && Objects.equals(this.enableVideoPlaybackTranscoding, userPolicy.enableVideoPlaybackTranscoding)
                && Objects.equals(this.enablePlaybackRemuxing, userPolicy.enablePlaybackRemuxing)
                && Objects.equals(this.forceRemoteSourceTranscoding, userPolicy.forceRemoteSourceTranscoding)
                && Objects.equals(this.enableContentDeletion, userPolicy.enableContentDeletion)
                && Objects.equals(this.enableContentDeletionFromFolders, userPolicy.enableContentDeletionFromFolders)
                && Objects.equals(this.enableContentDownloading, userPolicy.enableContentDownloading)
                && Objects.equals(this.enableSyncTranscoding, userPolicy.enableSyncTranscoding)
                && Objects.equals(this.enableMediaConversion, userPolicy.enableMediaConversion)
                && Objects.equals(this.enabledDevices, userPolicy.enabledDevices)
                && Objects.equals(this.enableAllDevices, userPolicy.enableAllDevices)
                && Objects.equals(this.enabledChannels, userPolicy.enabledChannels)
                && Objects.equals(this.enableAllChannels, userPolicy.enableAllChannels)
                && Objects.equals(this.enabledFolders, userPolicy.enabledFolders)
                && Objects.equals(this.enableAllFolders, userPolicy.enableAllFolders)
                && Objects.equals(this.invalidLoginAttemptCount, userPolicy.invalidLoginAttemptCount)
                && Objects.equals(this.loginAttemptsBeforeLockout, userPolicy.loginAttemptsBeforeLockout)
                && Objects.equals(this.maxActiveSessions, userPolicy.maxActiveSessions)
                && Objects.equals(this.enablePublicSharing, userPolicy.enablePublicSharing)
                && Objects.equals(this.blockedMediaFolders, userPolicy.blockedMediaFolders)
                && Objects.equals(this.blockedChannels, userPolicy.blockedChannels)
                && Objects.equals(this.remoteClientBitrateLimit, userPolicy.remoteClientBitrateLimit)
                && Objects.equals(this.authenticationProviderId, userPolicy.authenticationProviderId)
                && Objects.equals(this.passwordResetProviderId, userPolicy.passwordResetProviderId)
                && Objects.equals(this.syncPlayAccess, userPolicy.syncPlayAccess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAdministrator, isHidden, enableCollectionManagement, enableSubtitleManagement,
                enableLyricManagement, isDisabled, maxParentalRating, maxParentalSubRating, blockedTags, allowedTags,
                enableUserPreferenceAccess, accessSchedules, blockUnratedItems, enableRemoteControlOfOtherUsers,
                enableSharedDeviceControl, enableRemoteAccess, enableLiveTvManagement, enableLiveTvAccess,
                enableMediaPlayback, enableAudioPlaybackTranscoding, enableVideoPlaybackTranscoding,
                enablePlaybackRemuxing, forceRemoteSourceTranscoding, enableContentDeletion,
                enableContentDeletionFromFolders, enableContentDownloading, enableSyncTranscoding,
                enableMediaConversion, enabledDevices, enableAllDevices, enabledChannels, enableAllChannels,
                enabledFolders, enableAllFolders, invalidLoginAttemptCount, loginAttemptsBeforeLockout,
                maxActiveSessions, enablePublicSharing, blockedMediaFolders, blockedChannels, remoteClientBitrateLimit,
                authenticationProviderId, passwordResetProviderId, syncPlayAccess);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserPolicy {\n");
        sb.append("    isAdministrator: ").append(toIndentedString(isAdministrator)).append("\n");
        sb.append("    isHidden: ").append(toIndentedString(isHidden)).append("\n");
        sb.append("    enableCollectionManagement: ").append(toIndentedString(enableCollectionManagement)).append("\n");
        sb.append("    enableSubtitleManagement: ").append(toIndentedString(enableSubtitleManagement)).append("\n");
        sb.append("    enableLyricManagement: ").append(toIndentedString(enableLyricManagement)).append("\n");
        sb.append("    isDisabled: ").append(toIndentedString(isDisabled)).append("\n");
        sb.append("    maxParentalRating: ").append(toIndentedString(maxParentalRating)).append("\n");
        sb.append("    maxParentalSubRating: ").append(toIndentedString(maxParentalSubRating)).append("\n");
        sb.append("    blockedTags: ").append(toIndentedString(blockedTags)).append("\n");
        sb.append("    allowedTags: ").append(toIndentedString(allowedTags)).append("\n");
        sb.append("    enableUserPreferenceAccess: ").append(toIndentedString(enableUserPreferenceAccess)).append("\n");
        sb.append("    accessSchedules: ").append(toIndentedString(accessSchedules)).append("\n");
        sb.append("    blockUnratedItems: ").append(toIndentedString(blockUnratedItems)).append("\n");
        sb.append("    enableRemoteControlOfOtherUsers: ").append(toIndentedString(enableRemoteControlOfOtherUsers))
                .append("\n");
        sb.append("    enableSharedDeviceControl: ").append(toIndentedString(enableSharedDeviceControl)).append("\n");
        sb.append("    enableRemoteAccess: ").append(toIndentedString(enableRemoteAccess)).append("\n");
        sb.append("    enableLiveTvManagement: ").append(toIndentedString(enableLiveTvManagement)).append("\n");
        sb.append("    enableLiveTvAccess: ").append(toIndentedString(enableLiveTvAccess)).append("\n");
        sb.append("    enableMediaPlayback: ").append(toIndentedString(enableMediaPlayback)).append("\n");
        sb.append("    enableAudioPlaybackTranscoding: ").append(toIndentedString(enableAudioPlaybackTranscoding))
                .append("\n");
        sb.append("    enableVideoPlaybackTranscoding: ").append(toIndentedString(enableVideoPlaybackTranscoding))
                .append("\n");
        sb.append("    enablePlaybackRemuxing: ").append(toIndentedString(enablePlaybackRemuxing)).append("\n");
        sb.append("    forceRemoteSourceTranscoding: ").append(toIndentedString(forceRemoteSourceTranscoding))
                .append("\n");
        sb.append("    enableContentDeletion: ").append(toIndentedString(enableContentDeletion)).append("\n");
        sb.append("    enableContentDeletionFromFolders: ").append(toIndentedString(enableContentDeletionFromFolders))
                .append("\n");
        sb.append("    enableContentDownloading: ").append(toIndentedString(enableContentDownloading)).append("\n");
        sb.append("    enableSyncTranscoding: ").append(toIndentedString(enableSyncTranscoding)).append("\n");
        sb.append("    enableMediaConversion: ").append(toIndentedString(enableMediaConversion)).append("\n");
        sb.append("    enabledDevices: ").append(toIndentedString(enabledDevices)).append("\n");
        sb.append("    enableAllDevices: ").append(toIndentedString(enableAllDevices)).append("\n");
        sb.append("    enabledChannels: ").append(toIndentedString(enabledChannels)).append("\n");
        sb.append("    enableAllChannels: ").append(toIndentedString(enableAllChannels)).append("\n");
        sb.append("    enabledFolders: ").append(toIndentedString(enabledFolders)).append("\n");
        sb.append("    enableAllFolders: ").append(toIndentedString(enableAllFolders)).append("\n");
        sb.append("    invalidLoginAttemptCount: ").append(toIndentedString(invalidLoginAttemptCount)).append("\n");
        sb.append("    loginAttemptsBeforeLockout: ").append(toIndentedString(loginAttemptsBeforeLockout)).append("\n");
        sb.append("    maxActiveSessions: ").append(toIndentedString(maxActiveSessions)).append("\n");
        sb.append("    enablePublicSharing: ").append(toIndentedString(enablePublicSharing)).append("\n");
        sb.append("    blockedMediaFolders: ").append(toIndentedString(blockedMediaFolders)).append("\n");
        sb.append("    blockedChannels: ").append(toIndentedString(blockedChannels)).append("\n");
        sb.append("    remoteClientBitrateLimit: ").append(toIndentedString(remoteClientBitrateLimit)).append("\n");
        sb.append("    authenticationProviderId: ").append(toIndentedString(authenticationProviderId)).append("\n");
        sb.append("    passwordResetProviderId: ").append(toIndentedString(passwordResetProviderId)).append("\n");
        sb.append("    syncPlayAccess: ").append(toIndentedString(syncPlayAccess)).append("\n");
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

        // add `IsAdministrator` to the URL query string
        if (getIsAdministrator() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsAdministrator%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsAdministrator()))));
        }

        // add `IsHidden` to the URL query string
        if (getIsHidden() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsHidden%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsHidden()))));
        }

        // add `EnableCollectionManagement` to the URL query string
        if (getEnableCollectionManagement() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableCollectionManagement%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableCollectionManagement()))));
        }

        // add `EnableSubtitleManagement` to the URL query string
        if (getEnableSubtitleManagement() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableSubtitleManagement%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSubtitleManagement()))));
        }

        // add `EnableLyricManagement` to the URL query string
        if (getEnableLyricManagement() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableLyricManagement%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableLyricManagement()))));
        }

        // add `IsDisabled` to the URL query string
        if (getIsDisabled() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsDisabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsDisabled()))));
        }

        // add `MaxParentalRating` to the URL query string
        if (getMaxParentalRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMaxParentalRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxParentalRating()))));
        }

        // add `MaxParentalSubRating` to the URL query string
        if (getMaxParentalSubRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMaxParentalSubRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxParentalSubRating()))));
        }

        // add `BlockedTags` to the URL query string
        if (getBlockedTags() != null) {
            for (int i = 0; i < getBlockedTags().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sBlockedTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getBlockedTags().get(i)))));
            }
        }

        // add `AllowedTags` to the URL query string
        if (getAllowedTags() != null) {
            for (int i = 0; i < getAllowedTags().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sAllowedTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getAllowedTags().get(i)))));
            }
        }

        // add `EnableUserPreferenceAccess` to the URL query string
        if (getEnableUserPreferenceAccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableUserPreferenceAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableUserPreferenceAccess()))));
        }

        // add `AccessSchedules` to the URL query string
        if (getAccessSchedules() != null) {
            for (int i = 0; i < getAccessSchedules().size(); i++) {
                if (getAccessSchedules().get(i) != null) {
                    joiner.add(getAccessSchedules().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sAccessSchedules%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `BlockUnratedItems` to the URL query string
        if (getBlockUnratedItems() != null) {
            for (int i = 0; i < getBlockUnratedItems().size(); i++) {
                if (getBlockUnratedItems().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sBlockUnratedItems%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getBlockUnratedItems().get(i)))));
                }
            }
        }

        // add `EnableRemoteControlOfOtherUsers` to the URL query string
        if (getEnableRemoteControlOfOtherUsers() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableRemoteControlOfOtherUsers%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableRemoteControlOfOtherUsers()))));
        }

        // add `EnableSharedDeviceControl` to the URL query string
        if (getEnableSharedDeviceControl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableSharedDeviceControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSharedDeviceControl()))));
        }

        // add `EnableRemoteAccess` to the URL query string
        if (getEnableRemoteAccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableRemoteAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableRemoteAccess()))));
        }

        // add `EnableLiveTvManagement` to the URL query string
        if (getEnableLiveTvManagement() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableLiveTvManagement%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableLiveTvManagement()))));
        }

        // add `EnableLiveTvAccess` to the URL query string
        if (getEnableLiveTvAccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableLiveTvAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableLiveTvAccess()))));
        }

        // add `EnableMediaPlayback` to the URL query string
        if (getEnableMediaPlayback() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableMediaPlayback%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableMediaPlayback()))));
        }

        // add `EnableAudioPlaybackTranscoding` to the URL query string
        if (getEnableAudioPlaybackTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableAudioPlaybackTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAudioPlaybackTranscoding()))));
        }

        // add `EnableVideoPlaybackTranscoding` to the URL query string
        if (getEnableVideoPlaybackTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableVideoPlaybackTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableVideoPlaybackTranscoding()))));
        }

        // add `EnablePlaybackRemuxing` to the URL query string
        if (getEnablePlaybackRemuxing() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnablePlaybackRemuxing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePlaybackRemuxing()))));
        }

        // add `ForceRemoteSourceTranscoding` to the URL query string
        if (getForceRemoteSourceTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sForceRemoteSourceTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getForceRemoteSourceTranscoding()))));
        }

        // add `EnableContentDeletion` to the URL query string
        if (getEnableContentDeletion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableContentDeletion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableContentDeletion()))));
        }

        // add `EnableContentDeletionFromFolders` to the URL query string
        if (getEnableContentDeletionFromFolders() != null) {
            for (int i = 0; i < getEnableContentDeletionFromFolders().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sEnableContentDeletionFromFolders%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getEnableContentDeletionFromFolders().get(i)))));
            }
        }

        // add `EnableContentDownloading` to the URL query string
        if (getEnableContentDownloading() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableContentDownloading%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableContentDownloading()))));
        }

        // add `EnableSyncTranscoding` to the URL query string
        if (getEnableSyncTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableSyncTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSyncTranscoding()))));
        }

        // add `EnableMediaConversion` to the URL query string
        if (getEnableMediaConversion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableMediaConversion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableMediaConversion()))));
        }

        // add `EnabledDevices` to the URL query string
        if (getEnabledDevices() != null) {
            for (int i = 0; i < getEnabledDevices().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sEnabledDevices%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getEnabledDevices().get(i)))));
            }
        }

        // add `EnableAllDevices` to the URL query string
        if (getEnableAllDevices() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableAllDevices%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAllDevices()))));
        }

        // add `EnabledChannels` to the URL query string
        if (getEnabledChannels() != null) {
            for (int i = 0; i < getEnabledChannels().size(); i++) {
                if (getEnabledChannels().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sEnabledChannels%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getEnabledChannels().get(i)))));
                }
            }
        }

        // add `EnableAllChannels` to the URL query string
        if (getEnableAllChannels() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableAllChannels%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAllChannels()))));
        }

        // add `EnabledFolders` to the URL query string
        if (getEnabledFolders() != null) {
            for (int i = 0; i < getEnabledFolders().size(); i++) {
                if (getEnabledFolders().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sEnabledFolders%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getEnabledFolders().get(i)))));
                }
            }
        }

        // add `EnableAllFolders` to the URL query string
        if (getEnableAllFolders() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableAllFolders%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAllFolders()))));
        }

        // add `InvalidLoginAttemptCount` to the URL query string
        if (getInvalidLoginAttemptCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sInvalidLoginAttemptCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getInvalidLoginAttemptCount()))));
        }

        // add `LoginAttemptsBeforeLockout` to the URL query string
        if (getLoginAttemptsBeforeLockout() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLoginAttemptsBeforeLockout%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLoginAttemptsBeforeLockout()))));
        }

        // add `MaxActiveSessions` to the URL query string
        if (getMaxActiveSessions() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMaxActiveSessions%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxActiveSessions()))));
        }

        // add `EnablePublicSharing` to the URL query string
        if (getEnablePublicSharing() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnablePublicSharing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnablePublicSharing()))));
        }

        // add `BlockedMediaFolders` to the URL query string
        if (getBlockedMediaFolders() != null) {
            for (int i = 0; i < getBlockedMediaFolders().size(); i++) {
                if (getBlockedMediaFolders().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sBlockedMediaFolders%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getBlockedMediaFolders().get(i)))));
                }
            }
        }

        // add `BlockedChannels` to the URL query string
        if (getBlockedChannels() != null) {
            for (int i = 0; i < getBlockedChannels().size(); i++) {
                if (getBlockedChannels().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sBlockedChannels%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getBlockedChannels().get(i)))));
                }
            }
        }

        // add `RemoteClientBitrateLimit` to the URL query string
        if (getRemoteClientBitrateLimit() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRemoteClientBitrateLimit%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRemoteClientBitrateLimit()))));
        }

        // add `AuthenticationProviderId` to the URL query string
        if (getAuthenticationProviderId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAuthenticationProviderId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAuthenticationProviderId()))));
        }

        // add `PasswordResetProviderId` to the URL query string
        if (getPasswordResetProviderId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPasswordResetProviderId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPasswordResetProviderId()))));
        }

        // add `SyncPlayAccess` to the URL query string
        if (getSyncPlayAccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSyncPlayAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSyncPlayAccess()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UserPolicy instance;

        public Builder() {
            this(new UserPolicy());
        }

        protected Builder(UserPolicy instance) {
            this.instance = instance;
        }

        public UserPolicy.Builder isAdministrator(Boolean isAdministrator) {
            this.instance.isAdministrator = isAdministrator;
            return this;
        }

        public UserPolicy.Builder isHidden(Boolean isHidden) {
            this.instance.isHidden = isHidden;
            return this;
        }

        public UserPolicy.Builder enableCollectionManagement(Boolean enableCollectionManagement) {
            this.instance.enableCollectionManagement = enableCollectionManagement;
            return this;
        }

        public UserPolicy.Builder enableSubtitleManagement(Boolean enableSubtitleManagement) {
            this.instance.enableSubtitleManagement = enableSubtitleManagement;
            return this;
        }

        public UserPolicy.Builder enableLyricManagement(Boolean enableLyricManagement) {
            this.instance.enableLyricManagement = enableLyricManagement;
            return this;
        }

        public UserPolicy.Builder isDisabled(Boolean isDisabled) {
            this.instance.isDisabled = isDisabled;
            return this;
        }

        public UserPolicy.Builder maxParentalRating(Integer maxParentalRating) {
            this.instance.maxParentalRating = maxParentalRating;
            return this;
        }

        public UserPolicy.Builder maxParentalSubRating(Integer maxParentalSubRating) {
            this.instance.maxParentalSubRating = maxParentalSubRating;
            return this;
        }

        public UserPolicy.Builder blockedTags(List<String> blockedTags) {
            this.instance.blockedTags = blockedTags;
            return this;
        }

        public UserPolicy.Builder allowedTags(List<String> allowedTags) {
            this.instance.allowedTags = allowedTags;
            return this;
        }

        public UserPolicy.Builder enableUserPreferenceAccess(Boolean enableUserPreferenceAccess) {
            this.instance.enableUserPreferenceAccess = enableUserPreferenceAccess;
            return this;
        }

        public UserPolicy.Builder accessSchedules(List<AccessSchedule> accessSchedules) {
            this.instance.accessSchedules = accessSchedules;
            return this;
        }

        public UserPolicy.Builder blockUnratedItems(List<UnratedItem> blockUnratedItems) {
            this.instance.blockUnratedItems = blockUnratedItems;
            return this;
        }

        public UserPolicy.Builder enableRemoteControlOfOtherUsers(Boolean enableRemoteControlOfOtherUsers) {
            this.instance.enableRemoteControlOfOtherUsers = enableRemoteControlOfOtherUsers;
            return this;
        }

        public UserPolicy.Builder enableSharedDeviceControl(Boolean enableSharedDeviceControl) {
            this.instance.enableSharedDeviceControl = enableSharedDeviceControl;
            return this;
        }

        public UserPolicy.Builder enableRemoteAccess(Boolean enableRemoteAccess) {
            this.instance.enableRemoteAccess = enableRemoteAccess;
            return this;
        }

        public UserPolicy.Builder enableLiveTvManagement(Boolean enableLiveTvManagement) {
            this.instance.enableLiveTvManagement = enableLiveTvManagement;
            return this;
        }

        public UserPolicy.Builder enableLiveTvAccess(Boolean enableLiveTvAccess) {
            this.instance.enableLiveTvAccess = enableLiveTvAccess;
            return this;
        }

        public UserPolicy.Builder enableMediaPlayback(Boolean enableMediaPlayback) {
            this.instance.enableMediaPlayback = enableMediaPlayback;
            return this;
        }

        public UserPolicy.Builder enableAudioPlaybackTranscoding(Boolean enableAudioPlaybackTranscoding) {
            this.instance.enableAudioPlaybackTranscoding = enableAudioPlaybackTranscoding;
            return this;
        }

        public UserPolicy.Builder enableVideoPlaybackTranscoding(Boolean enableVideoPlaybackTranscoding) {
            this.instance.enableVideoPlaybackTranscoding = enableVideoPlaybackTranscoding;
            return this;
        }

        public UserPolicy.Builder enablePlaybackRemuxing(Boolean enablePlaybackRemuxing) {
            this.instance.enablePlaybackRemuxing = enablePlaybackRemuxing;
            return this;
        }

        public UserPolicy.Builder forceRemoteSourceTranscoding(Boolean forceRemoteSourceTranscoding) {
            this.instance.forceRemoteSourceTranscoding = forceRemoteSourceTranscoding;
            return this;
        }

        public UserPolicy.Builder enableContentDeletion(Boolean enableContentDeletion) {
            this.instance.enableContentDeletion = enableContentDeletion;
            return this;
        }

        public UserPolicy.Builder enableContentDeletionFromFolders(List<String> enableContentDeletionFromFolders) {
            this.instance.enableContentDeletionFromFolders = enableContentDeletionFromFolders;
            return this;
        }

        public UserPolicy.Builder enableContentDownloading(Boolean enableContentDownloading) {
            this.instance.enableContentDownloading = enableContentDownloading;
            return this;
        }

        public UserPolicy.Builder enableSyncTranscoding(Boolean enableSyncTranscoding) {
            this.instance.enableSyncTranscoding = enableSyncTranscoding;
            return this;
        }

        public UserPolicy.Builder enableMediaConversion(Boolean enableMediaConversion) {
            this.instance.enableMediaConversion = enableMediaConversion;
            return this;
        }

        public UserPolicy.Builder enabledDevices(List<String> enabledDevices) {
            this.instance.enabledDevices = enabledDevices;
            return this;
        }

        public UserPolicy.Builder enableAllDevices(Boolean enableAllDevices) {
            this.instance.enableAllDevices = enableAllDevices;
            return this;
        }

        public UserPolicy.Builder enabledChannels(List<UUID> enabledChannels) {
            this.instance.enabledChannels = enabledChannels;
            return this;
        }

        public UserPolicy.Builder enableAllChannels(Boolean enableAllChannels) {
            this.instance.enableAllChannels = enableAllChannels;
            return this;
        }

        public UserPolicy.Builder enabledFolders(List<UUID> enabledFolders) {
            this.instance.enabledFolders = enabledFolders;
            return this;
        }

        public UserPolicy.Builder enableAllFolders(Boolean enableAllFolders) {
            this.instance.enableAllFolders = enableAllFolders;
            return this;
        }

        public UserPolicy.Builder invalidLoginAttemptCount(Integer invalidLoginAttemptCount) {
            this.instance.invalidLoginAttemptCount = invalidLoginAttemptCount;
            return this;
        }

        public UserPolicy.Builder loginAttemptsBeforeLockout(Integer loginAttemptsBeforeLockout) {
            this.instance.loginAttemptsBeforeLockout = loginAttemptsBeforeLockout;
            return this;
        }

        public UserPolicy.Builder maxActiveSessions(Integer maxActiveSessions) {
            this.instance.maxActiveSessions = maxActiveSessions;
            return this;
        }

        public UserPolicy.Builder enablePublicSharing(Boolean enablePublicSharing) {
            this.instance.enablePublicSharing = enablePublicSharing;
            return this;
        }

        public UserPolicy.Builder blockedMediaFolders(List<UUID> blockedMediaFolders) {
            this.instance.blockedMediaFolders = blockedMediaFolders;
            return this;
        }

        public UserPolicy.Builder blockedChannels(List<UUID> blockedChannels) {
            this.instance.blockedChannels = blockedChannels;
            return this;
        }

        public UserPolicy.Builder remoteClientBitrateLimit(Integer remoteClientBitrateLimit) {
            this.instance.remoteClientBitrateLimit = remoteClientBitrateLimit;
            return this;
        }

        public UserPolicy.Builder authenticationProviderId(String authenticationProviderId) {
            this.instance.authenticationProviderId = authenticationProviderId;
            return this;
        }

        public UserPolicy.Builder passwordResetProviderId(String passwordResetProviderId) {
            this.instance.passwordResetProviderId = passwordResetProviderId;
            return this;
        }

        public UserPolicy.Builder syncPlayAccess(SyncPlayUserAccessType syncPlayAccess) {
            this.instance.syncPlayAccess = syncPlayAccess;
            return this;
        }

        /**
         * returns a built UserPolicy instance.
         *
         * The builder is not reusable.
         */
        public UserPolicy build() {
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
    public static UserPolicy.Builder builder() {
        return new UserPolicy.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UserPolicy.Builder toBuilder() {
        return new UserPolicy.Builder().isAdministrator(getIsAdministrator()).isHidden(getIsHidden())
                .enableCollectionManagement(getEnableCollectionManagement())
                .enableSubtitleManagement(getEnableSubtitleManagement())
                .enableLyricManagement(getEnableLyricManagement()).isDisabled(getIsDisabled())
                .maxParentalRating(getMaxParentalRating()).maxParentalSubRating(getMaxParentalSubRating())
                .blockedTags(getBlockedTags()).allowedTags(getAllowedTags())
                .enableUserPreferenceAccess(getEnableUserPreferenceAccess()).accessSchedules(getAccessSchedules())
                .blockUnratedItems(getBlockUnratedItems())
                .enableRemoteControlOfOtherUsers(getEnableRemoteControlOfOtherUsers())
                .enableSharedDeviceControl(getEnableSharedDeviceControl()).enableRemoteAccess(getEnableRemoteAccess())
                .enableLiveTvManagement(getEnableLiveTvManagement()).enableLiveTvAccess(getEnableLiveTvAccess())
                .enableMediaPlayback(getEnableMediaPlayback())
                .enableAudioPlaybackTranscoding(getEnableAudioPlaybackTranscoding())
                .enableVideoPlaybackTranscoding(getEnableVideoPlaybackTranscoding())
                .enablePlaybackRemuxing(getEnablePlaybackRemuxing())
                .forceRemoteSourceTranscoding(getForceRemoteSourceTranscoding())
                .enableContentDeletion(getEnableContentDeletion())
                .enableContentDeletionFromFolders(getEnableContentDeletionFromFolders())
                .enableContentDownloading(getEnableContentDownloading())
                .enableSyncTranscoding(getEnableSyncTranscoding()).enableMediaConversion(getEnableMediaConversion())
                .enabledDevices(getEnabledDevices()).enableAllDevices(getEnableAllDevices())
                .enabledChannels(getEnabledChannels()).enableAllChannels(getEnableAllChannels())
                .enabledFolders(getEnabledFolders()).enableAllFolders(getEnableAllFolders())
                .invalidLoginAttemptCount(getInvalidLoginAttemptCount())
                .loginAttemptsBeforeLockout(getLoginAttemptsBeforeLockout()).maxActiveSessions(getMaxActiveSessions())
                .enablePublicSharing(getEnablePublicSharing()).blockedMediaFolders(getBlockedMediaFolders())
                .blockedChannels(getBlockedChannels()).remoteClientBitrateLimit(getRemoteClientBitrateLimit())
                .authenticationProviderId(getAuthenticationProviderId())
                .passwordResetProviderId(getPasswordResetProviderId()).syncPlayAccess(getSyncPlayAccess());
    }
}
