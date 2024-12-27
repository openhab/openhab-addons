# PlaylistsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addItemToPlaylist**](PlaylistsApi.md#addItemToPlaylist) | **POST** /Playlists/{playlistId}/Items | Adds items to a playlist. |
| [**createPlaylist**](PlaylistsApi.md#createPlaylist) | **POST** /Playlists | Creates a new playlist. |
| [**getPlaylist**](PlaylistsApi.md#getPlaylist) | **GET** /Playlists/{playlistId} | Get a playlist. |
| [**getPlaylistItems**](PlaylistsApi.md#getPlaylistItems) | **GET** /Playlists/{playlistId}/Items | Gets the original items of a playlist. |
| [**getPlaylistUser**](PlaylistsApi.md#getPlaylistUser) | **GET** /Playlists/{playlistId}/Users/{userId} | Get a playlist user. |
| [**getPlaylistUsers**](PlaylistsApi.md#getPlaylistUsers) | **GET** /Playlists/{playlistId}/Users | Get a playlist&#39;s users. |
| [**moveItem**](PlaylistsApi.md#moveItem) | **POST** /Playlists/{playlistId}/Items/{itemId}/Move/{newIndex} | Moves a playlist item. |
| [**removeItemFromPlaylist**](PlaylistsApi.md#removeItemFromPlaylist) | **DELETE** /Playlists/{playlistId}/Items | Removes items from a playlist. |
| [**removeUserFromPlaylist**](PlaylistsApi.md#removeUserFromPlaylist) | **DELETE** /Playlists/{playlistId}/Users/{userId} | Remove a user from a playlist&#39;s users. |
| [**updatePlaylist**](PlaylistsApi.md#updatePlaylist) | **POST** /Playlists/{playlistId} | Updates a playlist. |
| [**updatePlaylistUser**](PlaylistsApi.md#updatePlaylistUser) | **POST** /Playlists/{playlistId}/Users/{userId} | Modify a user of a playlist&#39;s users. |


<a id="addItemToPlaylist"></a>
# **addItemToPlaylist**
> addItemToPlaylist(playlistId, ids, userId)

Adds items to a playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    List<UUID> ids = Arrays.asList(); // List<UUID> | Item id, comma delimited.
    UUID userId = UUID.randomUUID(); // UUID | The userId.
    try {
      apiInstance.addItemToPlaylist(playlistId, ids, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#addItemToPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| Item id, comma delimited. | [optional] |
| **userId** | **UUID**| The userId. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Items added to playlist. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="createPlaylist"></a>
# **createPlaylist**
> PlaylistCreationResult createPlaylist(name, ids, userId, mediaType, createPlaylistDto)

Creates a new playlist.

For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence.  Query parameters are obsolete.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    String name = "name_example"; // String | The playlist name.
    List<UUID> ids = Arrays.asList(); // List<UUID> | The item ids.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    MediaType mediaType = MediaType.fromValue("Unknown"); // MediaType | The media type.
    CreatePlaylistDto createPlaylistDto = new CreatePlaylistDto(); // CreatePlaylistDto | The create playlist payload.
    try {
      PlaylistCreationResult result = apiInstance.createPlaylist(name, ids, userId, mediaType, createPlaylistDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#createPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **name** | **String**| The playlist name. | [optional] |
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| The item ids. | [optional] |
| **userId** | **UUID**| The user id. | [optional] |
| **mediaType** | **MediaType**| The media type. | [optional] [enum: Unknown, Video, Audio, Photo, Book] |
| **createPlaylistDto** | [**CreatePlaylistDto**](CreatePlaylistDto.md)| The create playlist payload. | [optional] |

### Return type

[**PlaylistCreationResult**](PlaylistCreationResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Playlist created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPlaylist"></a>
# **getPlaylist**
> PlaylistDto getPlaylist(playlistId)

Get a playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    try {
      PlaylistDto result = apiInstance.getPlaylist(playlistId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#getPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |

### Return type

[**PlaylistDto**](PlaylistDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The playlist. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPlaylistItems"></a>
# **getPlaylistItems**
> BaseItemDtoQueryResult getPlaylistItems(playlistId, userId, startIndex, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Gets the original items of a playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getPlaylistItems(playlistId, userId, startIndex, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#getPlaylistItems");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |
| **userId** | **UUID**| User id. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Original playlist returned. |  -  |
| **403** | Forbidden |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="getPlaylistUser"></a>
# **getPlaylistUser**
> PlaylistUserPermissions getPlaylistUser(playlistId, userId)

Get a playlist user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      PlaylistUserPermissions result = apiInstance.getPlaylistUser(playlistId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#getPlaylistUser");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |
| **userId** | **UUID**| The user id. | |

### Return type

[**PlaylistUserPermissions**](PlaylistUserPermissions.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User permission found. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="getPlaylistUsers"></a>
# **getPlaylistUsers**
> List&lt;PlaylistUserPermissions&gt; getPlaylistUsers(playlistId)

Get a playlist&#39;s users.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    try {
      List<PlaylistUserPermissions> result = apiInstance.getPlaylistUsers(playlistId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#getPlaylistUsers");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |

### Return type

[**List&lt;PlaylistUserPermissions&gt;**](PlaylistUserPermissions.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Found shares. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="moveItem"></a>
# **moveItem**
> moveItem(playlistId, itemId, newIndex)

Moves a playlist item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    String playlistId = "playlistId_example"; // String | The playlist id.
    String itemId = "itemId_example"; // String | The item id.
    Integer newIndex = 56; // Integer | The new index.
    try {
      apiInstance.moveItem(playlistId, itemId, newIndex);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#moveItem");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **String**| The playlist id. | |
| **itemId** | **String**| The item id. | |
| **newIndex** | **Integer**| The new index. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Item moved to new index. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="removeItemFromPlaylist"></a>
# **removeItemFromPlaylist**
> removeItemFromPlaylist(playlistId, entryIds)

Removes items from a playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    String playlistId = "playlistId_example"; // String | The playlist id.
    List<String> entryIds = Arrays.asList(); // List<String> | The item ids, comma delimited.
    try {
      apiInstance.removeItemFromPlaylist(playlistId, entryIds);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#removeItemFromPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **String**| The playlist id. | |
| **entryIds** | [**List&lt;String&gt;**](String.md)| The item ids, comma delimited. | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Items removed. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="removeUserFromPlaylist"></a>
# **removeUserFromPlaylist**
> removeUserFromPlaylist(playlistId, userId)

Remove a user from a playlist&#39;s users.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      apiInstance.removeUserFromPlaylist(playlistId, userId);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#removeUserFromPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |
| **userId** | **UUID**| The user id. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User permissions removed from playlist. |  -  |
| **403** | Forbidden |  -  |
| **404** | No playlist or user permissions found. |  -  |
| **401** | Unauthorized access. |  -  |

<a id="updatePlaylist"></a>
# **updatePlaylist**
> updatePlaylist(playlistId, updatePlaylistDto)

Updates a playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    UpdatePlaylistDto updatePlaylistDto = new UpdatePlaylistDto(); // UpdatePlaylistDto | The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id.
    try {
      apiInstance.updatePlaylist(playlistId, updatePlaylistDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#updatePlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |
| **updatePlaylistDto** | [**UpdatePlaylistDto**](UpdatePlaylistDto.md)| The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Playlist updated. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

<a id="updatePlaylistUser"></a>
# **updatePlaylistUser**
> updatePlaylistUser(playlistId, userId, updatePlaylistUserDto)

Modify a user of a playlist&#39;s users.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaylistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaylistsApi apiInstance = new PlaylistsApi(defaultClient);
    UUID playlistId = UUID.randomUUID(); // UUID | The playlist id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    UpdatePlaylistUserDto updatePlaylistUserDto = new UpdatePlaylistUserDto(); // UpdatePlaylistUserDto | The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto.
    try {
      apiInstance.updatePlaylistUser(playlistId, userId, updatePlaylistUserDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaylistsApi#updatePlaylistUser");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **playlistId** | **UUID**| The playlist id. | |
| **userId** | **UUID**| The user id. | |
| **updatePlaylistUserDto** | [**UpdatePlaylistUserDto**](UpdatePlaylistUserDto.md)| The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | User&#39;s permissions modified. |  -  |
| **403** | Access forbidden. |  -  |
| **404** | Playlist not found. |  -  |
| **401** | Unauthorized |  -  |

