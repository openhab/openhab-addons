

# TimerInfoDto


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** | Gets or sets the Id of the recording. |  [optional] |
|**type** | **String** |  |  [optional] |
|**serverId** | **String** | Gets or sets the server identifier. |  [optional] |
|**externalId** | **String** | Gets or sets the external identifier. |  [optional] |
|**channelId** | **UUID** | Gets or sets the channel id of the recording. |  [optional] |
|**externalChannelId** | **String** | Gets or sets the external channel identifier. |  [optional] |
|**channelName** | **String** | Gets or sets the channel name of the recording. |  [optional] |
|**channelPrimaryImageTag** | **String** |  |  [optional] |
|**programId** | **String** | Gets or sets the program identifier. |  [optional] |
|**externalProgramId** | **String** | Gets or sets the external program identifier. |  [optional] |
|**name** | **String** | Gets or sets the name of the recording. |  [optional] |
|**overview** | **String** | Gets or sets the description of the recording. |  [optional] |
|**startDate** | **OffsetDateTime** | Gets or sets the start date of the recording, in UTC. |  [optional] |
|**endDate** | **OffsetDateTime** | Gets or sets the end date of the recording, in UTC. |  [optional] |
|**serviceName** | **String** | Gets or sets the name of the service. |  [optional] |
|**priority** | **Integer** | Gets or sets the priority. |  [optional] |
|**prePaddingSeconds** | **Integer** | Gets or sets the pre padding seconds. |  [optional] |
|**postPaddingSeconds** | **Integer** | Gets or sets the post padding seconds. |  [optional] |
|**isPrePaddingRequired** | **Boolean** | Gets or sets a value indicating whether this instance is pre padding required. |  [optional] |
|**parentBackdropItemId** | **String** | Gets or sets the Id of the Parent that has a backdrop if the item does not have one. |  [optional] |
|**parentBackdropImageTags** | **List&lt;String&gt;** | Gets or sets the parent backdrop image tags. |  [optional] |
|**isPostPaddingRequired** | **Boolean** | Gets or sets a value indicating whether this instance is post padding required. |  [optional] |
|**keepUntil** | **KeepUntil** |  |  [optional] |
|**status** | **RecordingStatus** | Gets or sets the status. |  [optional] |
|**seriesTimerId** | **String** | Gets or sets the series timer identifier. |  [optional] |
|**externalSeriesTimerId** | **String** | Gets or sets the external series timer identifier. |  [optional] |
|**runTimeTicks** | **Long** | Gets or sets the run time ticks. |  [optional] |
|**programInfo** | [**BaseItemDto**](BaseItemDto.md) | Gets or sets the program information. |  [optional] |



