

# ExternalIdInfo

Represents the external id information for serialization to the client.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Gets or sets the display name of the external id provider (IE: IMDB, MusicBrainz, etc). |  [optional] |
|**key** | **String** | Gets or sets the unique key for this id. This key should be unique across all providers. |  [optional] |
|**type** | **ExternalIdMediaType** | Gets or sets the specific media type for this id. This is used to distinguish between the different  external id types for providers with multiple ids.  A null value indicates there is no specific media type associated with the external id, or this is the  default id for the external provider so there is no need to specify a type. |  [optional] |
|**urlFormatString** | **String** | Gets or sets the URL format string. |  [optional] |



