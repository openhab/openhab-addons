# SyncPlayApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**syncPlayBuffering**](SyncPlayApi.md#syncPlayBuffering) | **POST** /SyncPlay/Buffering | Notify SyncPlay group that member is buffering. |
| [**syncPlayCreateGroup**](SyncPlayApi.md#syncPlayCreateGroup) | **POST** /SyncPlay/New | Create a new SyncPlay group. |
| [**syncPlayGetGroups**](SyncPlayApi.md#syncPlayGetGroups) | **GET** /SyncPlay/List | Gets all SyncPlay groups. |
| [**syncPlayJoinGroup**](SyncPlayApi.md#syncPlayJoinGroup) | **POST** /SyncPlay/Join | Join an existing SyncPlay group. |
| [**syncPlayLeaveGroup**](SyncPlayApi.md#syncPlayLeaveGroup) | **POST** /SyncPlay/Leave | Leave the joined SyncPlay group. |
| [**syncPlayMovePlaylistItem**](SyncPlayApi.md#syncPlayMovePlaylistItem) | **POST** /SyncPlay/MovePlaylistItem | Request to move an item in the playlist in SyncPlay group. |
| [**syncPlayNextItem**](SyncPlayApi.md#syncPlayNextItem) | **POST** /SyncPlay/NextItem | Request next item in SyncPlay group. |
| [**syncPlayPause**](SyncPlayApi.md#syncPlayPause) | **POST** /SyncPlay/Pause | Request pause in SyncPlay group. |
| [**syncPlayPing**](SyncPlayApi.md#syncPlayPing) | **POST** /SyncPlay/Ping | Update session ping. |
| [**syncPlayPreviousItem**](SyncPlayApi.md#syncPlayPreviousItem) | **POST** /SyncPlay/PreviousItem | Request previous item in SyncPlay group. |
| [**syncPlayQueue**](SyncPlayApi.md#syncPlayQueue) | **POST** /SyncPlay/Queue | Request to queue items to the playlist of a SyncPlay group. |
| [**syncPlayReady**](SyncPlayApi.md#syncPlayReady) | **POST** /SyncPlay/Ready | Notify SyncPlay group that member is ready for playback. |
| [**syncPlayRemoveFromPlaylist**](SyncPlayApi.md#syncPlayRemoveFromPlaylist) | **POST** /SyncPlay/RemoveFromPlaylist | Request to remove items from the playlist in SyncPlay group. |
| [**syncPlaySeek**](SyncPlayApi.md#syncPlaySeek) | **POST** /SyncPlay/Seek | Request seek in SyncPlay group. |
| [**syncPlaySetIgnoreWait**](SyncPlayApi.md#syncPlaySetIgnoreWait) | **POST** /SyncPlay/SetIgnoreWait | Request SyncPlay group to ignore member during group-wait. |
| [**syncPlaySetNewQueue**](SyncPlayApi.md#syncPlaySetNewQueue) | **POST** /SyncPlay/SetNewQueue | Request to set new playlist in SyncPlay group. |
| [**syncPlaySetPlaylistItem**](SyncPlayApi.md#syncPlaySetPlaylistItem) | **POST** /SyncPlay/SetPlaylistItem | Request to change playlist item in SyncPlay group. |
| [**syncPlaySetRepeatMode**](SyncPlayApi.md#syncPlaySetRepeatMode) | **POST** /SyncPlay/SetRepeatMode | Request to set repeat mode in SyncPlay group. |
| [**syncPlaySetShuffleMode**](SyncPlayApi.md#syncPlaySetShuffleMode) | **POST** /SyncPlay/SetShuffleMode | Request to set shuffle mode in SyncPlay group. |
| [**syncPlayStop**](SyncPlayApi.md#syncPlayStop) | **POST** /SyncPlay/Stop | Request stop in SyncPlay group. |
| [**syncPlayUnpause**](SyncPlayApi.md#syncPlayUnpause) | **POST** /SyncPlay/Unpause | Request unpause in SyncPlay group. |


<a id="syncPlayBuffering"></a>
# **syncPlayBuffering**
> syncPlayBuffering(bufferRequestDto)

Notify SyncPlay group that member is buffering.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    BufferRequestDto bufferRequestDto = new BufferRequestDto(); // BufferRequestDto | The player status.
    try {
      apiInstance.syncPlayBuffering(bufferRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayBuffering");
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
| **bufferRequestDto** | [**BufferRequestDto**](BufferRequestDto.md)| The player status. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Group state update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayCreateGroup"></a>
# **syncPlayCreateGroup**
> syncPlayCreateGroup(newGroupRequestDto)

Create a new SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    NewGroupRequestDto newGroupRequestDto = new NewGroupRequestDto(); // NewGroupRequestDto | The settings of the new group.
    try {
      apiInstance.syncPlayCreateGroup(newGroupRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayCreateGroup");
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
| **newGroupRequestDto** | [**NewGroupRequestDto**](NewGroupRequestDto.md)| The settings of the new group. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | New group created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayGetGroups"></a>
# **syncPlayGetGroups**
> List&lt;GroupInfoDto&gt; syncPlayGetGroups()

Gets all SyncPlay groups.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    try {
      List<GroupInfoDto> result = apiInstance.syncPlayGetGroups();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayGetGroups");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;GroupInfoDto&gt;**](GroupInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Groups returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayJoinGroup"></a>
# **syncPlayJoinGroup**
> syncPlayJoinGroup(joinGroupRequestDto)

Join an existing SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    JoinGroupRequestDto joinGroupRequestDto = new JoinGroupRequestDto(); // JoinGroupRequestDto | The group to join.
    try {
      apiInstance.syncPlayJoinGroup(joinGroupRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayJoinGroup");
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
| **joinGroupRequestDto** | [**JoinGroupRequestDto**](JoinGroupRequestDto.md)| The group to join. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Group join successful. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayLeaveGroup"></a>
# **syncPlayLeaveGroup**
> syncPlayLeaveGroup()

Leave the joined SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    try {
      apiInstance.syncPlayLeaveGroup();
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayLeaveGroup");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Group leave successful. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayMovePlaylistItem"></a>
# **syncPlayMovePlaylistItem**
> syncPlayMovePlaylistItem(movePlaylistItemRequestDto)

Request to move an item in the playlist in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    MovePlaylistItemRequestDto movePlaylistItemRequestDto = new MovePlaylistItemRequestDto(); // MovePlaylistItemRequestDto | The new position for the item.
    try {
      apiInstance.syncPlayMovePlaylistItem(movePlaylistItemRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayMovePlaylistItem");
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
| **movePlaylistItemRequestDto** | [**MovePlaylistItemRequestDto**](MovePlaylistItemRequestDto.md)| The new position for the item. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayNextItem"></a>
# **syncPlayNextItem**
> syncPlayNextItem(nextItemRequestDto)

Request next item in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    NextItemRequestDto nextItemRequestDto = new NextItemRequestDto(); // NextItemRequestDto | The current item information.
    try {
      apiInstance.syncPlayNextItem(nextItemRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayNextItem");
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
| **nextItemRequestDto** | [**NextItemRequestDto**](NextItemRequestDto.md)| The current item information. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Next item update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayPause"></a>
# **syncPlayPause**
> syncPlayPause()

Request pause in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    try {
      apiInstance.syncPlayPause();
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayPause");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Pause update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayPing"></a>
# **syncPlayPing**
> syncPlayPing(pingRequestDto)

Update session ping.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    PingRequestDto pingRequestDto = new PingRequestDto(); // PingRequestDto | The new ping.
    try {
      apiInstance.syncPlayPing(pingRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayPing");
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
| **pingRequestDto** | [**PingRequestDto**](PingRequestDto.md)| The new ping. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Ping updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayPreviousItem"></a>
# **syncPlayPreviousItem**
> syncPlayPreviousItem(previousItemRequestDto)

Request previous item in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    PreviousItemRequestDto previousItemRequestDto = new PreviousItemRequestDto(); // PreviousItemRequestDto | The current item information.
    try {
      apiInstance.syncPlayPreviousItem(previousItemRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayPreviousItem");
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
| **previousItemRequestDto** | [**PreviousItemRequestDto**](PreviousItemRequestDto.md)| The current item information. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Previous item update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayQueue"></a>
# **syncPlayQueue**
> syncPlayQueue(queueRequestDto)

Request to queue items to the playlist of a SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    QueueRequestDto queueRequestDto = new QueueRequestDto(); // QueueRequestDto | The items to add.
    try {
      apiInstance.syncPlayQueue(queueRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayQueue");
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
| **queueRequestDto** | [**QueueRequestDto**](QueueRequestDto.md)| The items to add. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayReady"></a>
# **syncPlayReady**
> syncPlayReady(readyRequestDto)

Notify SyncPlay group that member is ready for playback.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    ReadyRequestDto readyRequestDto = new ReadyRequestDto(); // ReadyRequestDto | The player status.
    try {
      apiInstance.syncPlayReady(readyRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayReady");
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
| **readyRequestDto** | [**ReadyRequestDto**](ReadyRequestDto.md)| The player status. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Group state update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayRemoveFromPlaylist"></a>
# **syncPlayRemoveFromPlaylist**
> syncPlayRemoveFromPlaylist(removeFromPlaylistRequestDto)

Request to remove items from the playlist in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto = new RemoveFromPlaylistRequestDto(); // RemoveFromPlaylistRequestDto | The items to remove.
    try {
      apiInstance.syncPlayRemoveFromPlaylist(removeFromPlaylistRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayRemoveFromPlaylist");
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
| **removeFromPlaylistRequestDto** | [**RemoveFromPlaylistRequestDto**](RemoveFromPlaylistRequestDto.md)| The items to remove. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlaySeek"></a>
# **syncPlaySeek**
> syncPlaySeek(seekRequestDto)

Request seek in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    SeekRequestDto seekRequestDto = new SeekRequestDto(); // SeekRequestDto | The new playback position.
    try {
      apiInstance.syncPlaySeek(seekRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlaySeek");
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
| **seekRequestDto** | [**SeekRequestDto**](SeekRequestDto.md)| The new playback position. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Seek update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlaySetIgnoreWait"></a>
# **syncPlaySetIgnoreWait**
> syncPlaySetIgnoreWait(ignoreWaitRequestDto)

Request SyncPlay group to ignore member during group-wait.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    IgnoreWaitRequestDto ignoreWaitRequestDto = new IgnoreWaitRequestDto(); // IgnoreWaitRequestDto | The settings to set.
    try {
      apiInstance.syncPlaySetIgnoreWait(ignoreWaitRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlaySetIgnoreWait");
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
| **ignoreWaitRequestDto** | [**IgnoreWaitRequestDto**](IgnoreWaitRequestDto.md)| The settings to set. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Member state updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlaySetNewQueue"></a>
# **syncPlaySetNewQueue**
> syncPlaySetNewQueue(playRequestDto)

Request to set new playlist in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    PlayRequestDto playRequestDto = new PlayRequestDto(); // PlayRequestDto | The new playlist to play in the group.
    try {
      apiInstance.syncPlaySetNewQueue(playRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlaySetNewQueue");
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
| **playRequestDto** | [**PlayRequestDto**](PlayRequestDto.md)| The new playlist to play in the group. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlaySetPlaylistItem"></a>
# **syncPlaySetPlaylistItem**
> syncPlaySetPlaylistItem(setPlaylistItemRequestDto)

Request to change playlist item in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    SetPlaylistItemRequestDto setPlaylistItemRequestDto = new SetPlaylistItemRequestDto(); // SetPlaylistItemRequestDto | The new item to play.
    try {
      apiInstance.syncPlaySetPlaylistItem(setPlaylistItemRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlaySetPlaylistItem");
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
| **setPlaylistItemRequestDto** | [**SetPlaylistItemRequestDto**](SetPlaylistItemRequestDto.md)| The new item to play. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlaySetRepeatMode"></a>
# **syncPlaySetRepeatMode**
> syncPlaySetRepeatMode(setRepeatModeRequestDto)

Request to set repeat mode in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    SetRepeatModeRequestDto setRepeatModeRequestDto = new SetRepeatModeRequestDto(); // SetRepeatModeRequestDto | The new repeat mode.
    try {
      apiInstance.syncPlaySetRepeatMode(setRepeatModeRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlaySetRepeatMode");
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
| **setRepeatModeRequestDto** | [**SetRepeatModeRequestDto**](SetRepeatModeRequestDto.md)| The new repeat mode. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Play queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlaySetShuffleMode"></a>
# **syncPlaySetShuffleMode**
> syncPlaySetShuffleMode(setShuffleModeRequestDto)

Request to set shuffle mode in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    SetShuffleModeRequestDto setShuffleModeRequestDto = new SetShuffleModeRequestDto(); // SetShuffleModeRequestDto | The new shuffle mode.
    try {
      apiInstance.syncPlaySetShuffleMode(setShuffleModeRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlaySetShuffleMode");
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
| **setShuffleModeRequestDto** | [**SetShuffleModeRequestDto**](SetShuffleModeRequestDto.md)| The new shuffle mode. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Play queue update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayStop"></a>
# **syncPlayStop**
> syncPlayStop()

Request stop in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    try {
      apiInstance.syncPlayStop();
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayStop");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Stop update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="syncPlayUnpause"></a>
# **syncPlayUnpause**
> syncPlayUnpause()

Request unpause in SyncPlay group.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SyncPlayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SyncPlayApi apiInstance = new SyncPlayApi(defaultClient);
    try {
      apiInstance.syncPlayUnpause();
    } catch (ApiException e) {
      System.err.println("Exception when calling SyncPlayApi#syncPlayUnpause");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Unpause update sent to all group members. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

