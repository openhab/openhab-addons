

# UserPolicy


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**isAdministrator** | **Boolean** | Gets or sets a value indicating whether this instance is administrator. |  [optional] |
|**isHidden** | **Boolean** | Gets or sets a value indicating whether this instance is hidden. |  [optional] |
|**enableCollectionManagement** | **Boolean** | Gets or sets a value indicating whether this instance can manage collections. |  [optional] |
|**enableSubtitleManagement** | **Boolean** | Gets or sets a value indicating whether this instance can manage subtitles. |  [optional] |
|**enableLyricManagement** | **Boolean** | Gets or sets a value indicating whether this user can manage lyrics. |  [optional] |
|**isDisabled** | **Boolean** | Gets or sets a value indicating whether this instance is disabled. |  [optional] |
|**maxParentalRating** | **Integer** | Gets or sets the max parental rating. |  [optional] |
|**blockedTags** | **List&lt;String&gt;** |  |  [optional] |
|**allowedTags** | **List&lt;String&gt;** |  |  [optional] |
|**enableUserPreferenceAccess** | **Boolean** |  |  [optional] |
|**accessSchedules** | [**List&lt;AccessSchedule&gt;**](AccessSchedule.md) |  |  [optional] |
|**blockUnratedItems** | **List&lt;UnratedItem&gt;** |  |  [optional] |
|**enableRemoteControlOfOtherUsers** | **Boolean** |  |  [optional] |
|**enableSharedDeviceControl** | **Boolean** |  |  [optional] |
|**enableRemoteAccess** | **Boolean** |  |  [optional] |
|**enableLiveTvManagement** | **Boolean** |  |  [optional] |
|**enableLiveTvAccess** | **Boolean** |  |  [optional] |
|**enableMediaPlayback** | **Boolean** |  |  [optional] |
|**enableAudioPlaybackTranscoding** | **Boolean** |  |  [optional] |
|**enableVideoPlaybackTranscoding** | **Boolean** |  |  [optional] |
|**enablePlaybackRemuxing** | **Boolean** |  |  [optional] |
|**forceRemoteSourceTranscoding** | **Boolean** |  |  [optional] |
|**enableContentDeletion** | **Boolean** |  |  [optional] |
|**enableContentDeletionFromFolders** | **List&lt;String&gt;** |  |  [optional] |
|**enableContentDownloading** | **Boolean** |  |  [optional] |
|**enableSyncTranscoding** | **Boolean** | Gets or sets a value indicating whether [enable synchronize]. |  [optional] |
|**enableMediaConversion** | **Boolean** |  |  [optional] |
|**enabledDevices** | **List&lt;String&gt;** |  |  [optional] |
|**enableAllDevices** | **Boolean** |  |  [optional] |
|**enabledChannels** | **List&lt;UUID&gt;** |  |  [optional] |
|**enableAllChannels** | **Boolean** |  |  [optional] |
|**enabledFolders** | **List&lt;UUID&gt;** |  |  [optional] |
|**enableAllFolders** | **Boolean** |  |  [optional] |
|**invalidLoginAttemptCount** | **Integer** |  |  [optional] |
|**loginAttemptsBeforeLockout** | **Integer** |  |  [optional] |
|**maxActiveSessions** | **Integer** |  |  [optional] |
|**enablePublicSharing** | **Boolean** |  |  [optional] |
|**blockedMediaFolders** | **List&lt;UUID&gt;** |  |  [optional] |
|**blockedChannels** | **List&lt;UUID&gt;** |  |  [optional] |
|**remoteClientBitrateLimit** | **Integer** |  |  [optional] |
|**authenticationProviderId** | **String** |  |  |
|**passwordResetProviderId** | **String** |  |  |
|**syncPlayAccess** | **SyncPlayUserAccessType** | Enum SyncPlayUserAccessType. |  [optional] |



