

# CreatePlaylistDto

Create new playlist dto.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Gets or sets the name of the new playlist. |  [optional] |
|**ids** | **List&lt;UUID&gt;** | Gets or sets item ids to add to the playlist. |  [optional] |
|**userId** | **UUID** | Gets or sets the user id. |  [optional] |
|**mediaType** | **MediaType** | Gets or sets the media type. |  [optional] |
|**users** | [**List&lt;PlaylistUserPermissions&gt;**](PlaylistUserPermissions.md) | Gets or sets the playlist users. |  [optional] |
|**isPublic** | **Boolean** | Gets or sets a value indicating whether the playlist is public. |  [optional] |



