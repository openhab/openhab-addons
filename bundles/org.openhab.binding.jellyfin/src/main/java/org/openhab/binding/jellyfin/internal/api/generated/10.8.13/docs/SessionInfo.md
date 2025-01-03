

# SessionInfo

Class SessionInfo.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**playState** | [**PlayerStateInfo**](PlayerStateInfo.md) |  |  [optional] |
|**additionalUsers** | [**List&lt;SessionUserInfo&gt;**](SessionUserInfo.md) |  |  [optional] |
|**capabilities** | [**ClientCapabilities**](ClientCapabilities.md) |  |  [optional] |
|**remoteEndPoint** | **String** | Gets or sets the remote end point. |  [optional] |
|**playableMediaTypes** | **List&lt;String&gt;** | Gets the playable media types. |  [optional] [readonly] |
|**id** | **String** | Gets or sets the id. |  [optional] |
|**userId** | **UUID** | Gets or sets the user id. |  [optional] |
|**userName** | **String** | Gets or sets the username. |  [optional] |
|**client** | **String** | Gets or sets the type of the client. |  [optional] |
|**lastActivityDate** | **OffsetDateTime** | Gets or sets the last activity date. |  [optional] |
|**lastPlaybackCheckIn** | **OffsetDateTime** | Gets or sets the last playback check in. |  [optional] |
|**deviceName** | **String** | Gets or sets the name of the device. |  [optional] |
|**deviceType** | **String** | Gets or sets the type of the device. |  [optional] |
|**nowPlayingItem** | [**BaseItemDto**](BaseItemDto.md) | Gets or sets the now playing item. |  [optional] |
|**fullNowPlayingItem** | [**BaseItem**](BaseItem.md) | Class BaseItem. |  [optional] |
|**nowViewingItem** | [**BaseItemDto**](BaseItemDto.md) | This is strictly used as a data transfer object from the api layer.  This holds information about a BaseItem in a format that is convenient for the client. |  [optional] |
|**deviceId** | **String** | Gets or sets the device id. |  [optional] |
|**applicationVersion** | **String** | Gets or sets the application version. |  [optional] |
|**transcodingInfo** | [**TranscodingInfo**](TranscodingInfo.md) |  |  [optional] |
|**isActive** | **Boolean** | Gets a value indicating whether this instance is active. |  [optional] [readonly] |
|**supportsMediaControl** | **Boolean** |  |  [optional] [readonly] |
|**supportsRemoteControl** | **Boolean** |  |  [optional] [readonly] |
|**nowPlayingQueue** | [**List&lt;QueueItem&gt;**](QueueItem.md) |  |  [optional] |
|**nowPlayingQueueFullItems** | [**List&lt;BaseItemDto&gt;**](BaseItemDto.md) |  |  [optional] |
|**hasCustomDeviceName** | **Boolean** |  |  [optional] |
|**playlistItemId** | **String** |  |  [optional] |
|**serverId** | **String** |  |  [optional] |
|**userPrimaryImageTag** | **String** |  |  [optional] |
|**supportedCommands** | **List&lt;GeneralCommandType&gt;** | Gets the supported commands. |  [optional] [readonly] |



