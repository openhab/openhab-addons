

# UpdatePlaylistDto

Update existing playlist dto. Fields set to `null` will not be updated and keep their current values.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Gets or sets the name of the new playlist. |  [optional] |
|**ids** | **List&lt;UUID&gt;** | Gets or sets item ids of the playlist. |  [optional] |
|**users** | [**List&lt;PlaylistUserPermissions&gt;**](PlaylistUserPermissions.md) | Gets or sets the playlist users. |  [optional] |
|**isPublic** | **Boolean** | Gets or sets a value indicating whether the playlist is public. |  [optional] |



