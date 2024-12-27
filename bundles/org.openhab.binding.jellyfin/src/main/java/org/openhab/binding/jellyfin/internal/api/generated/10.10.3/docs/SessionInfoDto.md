

# SessionInfoDto

Session info DTO.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**playState** | [**PlayerStateInfo**](PlayerStateInfo.md) | Gets or sets the play state. |  [optional] |
|**additionalUsers** | [**List&lt;SessionUserInfo&gt;**](SessionUserInfo.md) | Gets or sets the additional users. |  [optional] |
|**capabilities** | [**ClientCapabilitiesDto**](ClientCapabilitiesDto.md) | Gets or sets the client capabilities. |  [optional] |
|**remoteEndPoint** | **String** | Gets or sets the remote end point. |  [optional] |
|**playableMediaTypes** | **List&lt;MediaType&gt;** | Gets or sets the playable media types. |  [optional] |
|**id** | **String** | Gets or sets the id. |  [optional] |
|**userId** | **UUID** | Gets or sets the user id. |  [optional] |
|**userName** | **String** | Gets or sets the username. |  [optional] |
|**client** | **String** | Gets or sets the type of the client. |  [optional] |
|**lastActivityDate** | **OffsetDateTime** | Gets or sets the last activity date. |  [optional] |
|**lastPlaybackCheckIn** | **OffsetDateTime** | Gets or sets the last playback check in. |  [optional] |
|**lastPausedDate** | **OffsetDateTime** | Gets or sets the last paused date. |  [optional] |
|**deviceName** | **String** | Gets or sets the name of the device. |  [optional] |
|**deviceType** | **String** | Gets or sets the type of the device. |  [optional] |
|**nowPlayingItem** | [**BaseItemDto**](BaseItemDto.md) | Gets or sets the now playing item. |  [optional] |
|**nowViewingItem** | [**BaseItemDto**](BaseItemDto.md) | Gets or sets the now viewing item. |  [optional] |
|**deviceId** | **String** | Gets or sets the device id. |  [optional] |
|**applicationVersion** | **String** | Gets or sets the application version. |  [optional] |
|**transcodingInfo** | [**TranscodingInfo**](TranscodingInfo.md) | Gets or sets the transcoding info. |  [optional] |
|**isActive** | **Boolean** | Gets or sets a value indicating whether this session is active. |  [optional] |
|**supportsMediaControl** | **Boolean** | Gets or sets a value indicating whether the session supports media control. |  [optional] |
|**supportsRemoteControl** | **Boolean** | Gets or sets a value indicating whether the session supports remote control. |  [optional] |
|**nowPlayingQueue** | [**List&lt;QueueItem&gt;**](QueueItem.md) | Gets or sets the now playing queue. |  [optional] |
|**nowPlayingQueueFullItems** | [**List&lt;BaseItemDto&gt;**](BaseItemDto.md) | Gets or sets the now playing queue full items. |  [optional] |
|**hasCustomDeviceName** | **Boolean** | Gets or sets a value indicating whether the session has a custom device name. |  [optional] |
|**playlistItemId** | **String** | Gets or sets the playlist item id. |  [optional] |
|**serverId** | **String** | Gets or sets the server id. |  [optional] |
|**userPrimaryImageTag** | **String** | Gets or sets the user primary image tag. |  [optional] |
|**supportedCommands** | **List&lt;GeneralCommandType&gt;** | Gets or sets the supported commands. |  [optional] |



