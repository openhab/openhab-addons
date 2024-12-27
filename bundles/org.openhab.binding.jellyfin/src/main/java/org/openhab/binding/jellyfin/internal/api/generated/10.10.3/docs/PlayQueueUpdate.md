

# PlayQueueUpdate

Class PlayQueueUpdate.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**reason** | **PlayQueueUpdateReason** | Gets the request type that originated this update. |  [optional] |
|**lastUpdate** | **OffsetDateTime** | Gets the UTC time of the last change to the playing queue. |  [optional] |
|**playlist** | [**List&lt;SyncPlayQueueItem&gt;**](SyncPlayQueueItem.md) | Gets the playlist. |  [optional] |
|**playingItemIndex** | **Integer** | Gets the playing item index in the playlist. |  [optional] |
|**startPositionTicks** | **Long** | Gets the start position ticks. |  [optional] |
|**isPlaying** | **Boolean** | Gets a value indicating whether the current item is playing. |  [optional] |
|**shuffleMode** | **GroupShuffleMode** | Gets the shuffle mode. |  [optional] |
|**repeatMode** | **GroupRepeatMode** | Gets the repeat mode. |  [optional] |



