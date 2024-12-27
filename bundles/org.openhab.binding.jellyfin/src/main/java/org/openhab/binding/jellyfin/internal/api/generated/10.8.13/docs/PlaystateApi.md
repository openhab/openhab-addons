# PlaystateApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**markPlayedItem**](PlaystateApi.md#markPlayedItem) | **POST** /Users/{userId}/PlayedItems/{itemId} | Marks an item as played for user. |
| [**markUnplayedItem**](PlaystateApi.md#markUnplayedItem) | **DELETE** /Users/{userId}/PlayedItems/{itemId} | Marks an item as unplayed for user. |
| [**onPlaybackProgress**](PlaystateApi.md#onPlaybackProgress) | **POST** /Users/{userId}/PlayingItems/{itemId}/Progress | Reports a user&#39;s playback progress. |
| [**onPlaybackStart**](PlaystateApi.md#onPlaybackStart) | **POST** /Users/{userId}/PlayingItems/{itemId} | Reports that a user has begun playing an item. |
| [**onPlaybackStopped**](PlaystateApi.md#onPlaybackStopped) | **DELETE** /Users/{userId}/PlayingItems/{itemId} | Reports that a user has stopped playing an item. |
| [**pingPlaybackSession**](PlaystateApi.md#pingPlaybackSession) | **POST** /Sessions/Playing/Ping | Pings a playback session. |
| [**reportPlaybackProgress**](PlaystateApi.md#reportPlaybackProgress) | **POST** /Sessions/Playing/Progress | Reports playback progress within a session. |
| [**reportPlaybackStart**](PlaystateApi.md#reportPlaybackStart) | **POST** /Sessions/Playing | Reports playback has started within a session. |
| [**reportPlaybackStopped**](PlaystateApi.md#reportPlaybackStopped) | **POST** /Sessions/Playing/Stopped | Reports playback has stopped within a session. |


<a id="markPlayedItem"></a>
# **markPlayedItem**
> UserItemDataDto markPlayedItem(userId, itemId, datePlayed)

Marks an item as played for user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    OffsetDateTime datePlayed = OffsetDateTime.now(); // OffsetDateTime | Optional. The date the item was played.
    try {
      UserItemDataDto result = apiInstance.markPlayedItem(userId, itemId, datePlayed);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#markPlayedItem");
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
| **userId** | **UUID**| User id. | |
| **itemId** | **UUID**| Item id. | |
| **datePlayed** | **OffsetDateTime**| Optional. The date the item was played. | [optional] |

### Return type

[**UserItemDataDto**](UserItemDataDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item marked as played. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="markUnplayedItem"></a>
# **markUnplayedItem**
> UserItemDataDto markUnplayedItem(userId, itemId)

Marks an item as unplayed for user.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    try {
      UserItemDataDto result = apiInstance.markUnplayedItem(userId, itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#markUnplayedItem");
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
| **userId** | **UUID**| User id. | |
| **itemId** | **UUID**| Item id. | |

### Return type

[**UserItemDataDto**](UserItemDataDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item marked as unplayed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="onPlaybackProgress"></a>
# **onPlaybackProgress**
> onPlaybackProgress(userId, itemId, mediaSourceId, positionTicks, audioStreamIndex, subtitleStreamIndex, volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused, isMuted)

Reports a user&#39;s playback progress.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The id of the MediaSource.
    Long positionTicks = 56L; // Long | Optional. The current position, in ticks. 1 tick = 10000 ms.
    Integer audioStreamIndex = 56; // Integer | The audio stream index.
    Integer subtitleStreamIndex = 56; // Integer | The subtitle stream index.
    Integer volumeLevel = 56; // Integer | Scale of 0-100.
    PlayMethod playMethod = PlayMethod.fromValue("Transcode"); // PlayMethod | The play method.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    RepeatMode repeatMode = RepeatMode.fromValue("RepeatNone"); // RepeatMode | The repeat mode.
    Boolean isPaused = false; // Boolean | Indicates if the player is paused.
    Boolean isMuted = false; // Boolean | Indicates if the player is muted.
    try {
      apiInstance.onPlaybackProgress(userId, itemId, mediaSourceId, positionTicks, audioStreamIndex, subtitleStreamIndex, volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused, isMuted);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#onPlaybackProgress");
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
| **userId** | **UUID**| User id. | |
| **itemId** | **UUID**| Item id. | |
| **mediaSourceId** | **String**| The id of the MediaSource. | [optional] |
| **positionTicks** | **Long**| Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **audioStreamIndex** | **Integer**| The audio stream index. | [optional] |
| **subtitleStreamIndex** | **Integer**| The subtitle stream index. | [optional] |
| **volumeLevel** | **Integer**| Scale of 0-100. | [optional] |
| **playMethod** | [**PlayMethod**](.md)| The play method. | [optional] [enum: Transcode, DirectStream, DirectPlay] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **repeatMode** | [**RepeatMode**](.md)| The repeat mode. | [optional] [enum: RepeatNone, RepeatAll, RepeatOne] |
| **isPaused** | **Boolean**| Indicates if the player is paused. | [optional] [default to false] |
| **isMuted** | **Boolean**| Indicates if the player is muted. | [optional] [default to false] |

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
| **204** | Play progress recorded. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="onPlaybackStart"></a>
# **onPlaybackStart**
> onPlaybackStart(userId, itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod, liveStreamId, playSessionId, canSeek)

Reports that a user has begun playing an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The id of the MediaSource.
    Integer audioStreamIndex = 56; // Integer | The audio stream index.
    Integer subtitleStreamIndex = 56; // Integer | The subtitle stream index.
    PlayMethod playMethod = PlayMethod.fromValue("Transcode"); // PlayMethod | The play method.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    Boolean canSeek = false; // Boolean | Indicates if the client can seek.
    try {
      apiInstance.onPlaybackStart(userId, itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod, liveStreamId, playSessionId, canSeek);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#onPlaybackStart");
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
| **userId** | **UUID**| User id. | |
| **itemId** | **UUID**| Item id. | |
| **mediaSourceId** | **String**| The id of the MediaSource. | [optional] |
| **audioStreamIndex** | **Integer**| The audio stream index. | [optional] |
| **subtitleStreamIndex** | **Integer**| The subtitle stream index. | [optional] |
| **playMethod** | [**PlayMethod**](.md)| The play method. | [optional] [enum: Transcode, DirectStream, DirectPlay] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **canSeek** | **Boolean**| Indicates if the client can seek. | [optional] [default to false] |

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
| **204** | Play start recorded. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="onPlaybackStopped"></a>
# **onPlaybackStopped**
> onPlaybackStopped(userId, itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId, playSessionId)

Reports that a user has stopped playing an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The id of the MediaSource.
    String nextMediaType = "nextMediaType_example"; // String | The next media type that will play.
    Long positionTicks = 56L; // Long | Optional. The position, in ticks, where playback stopped. 1 tick = 10000 ms.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    try {
      apiInstance.onPlaybackStopped(userId, itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId, playSessionId);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#onPlaybackStopped");
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
| **userId** | **UUID**| User id. | |
| **itemId** | **UUID**| Item id. | |
| **mediaSourceId** | **String**| The id of the MediaSource. | [optional] |
| **nextMediaType** | **String**| The next media type that will play. | [optional] |
| **positionTicks** | **Long**| Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |

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
| **204** | Playback stop recorded. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="pingPlaybackSession"></a>
# **pingPlaybackSession**
> pingPlaybackSession(playSessionId)

Pings a playback session.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    String playSessionId = "playSessionId_example"; // String | Playback session id.
    try {
      apiInstance.pingPlaybackSession(playSessionId);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#pingPlaybackSession");
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
| **playSessionId** | **String**| Playback session id. | |

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
| **204** | Playback session pinged. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="reportPlaybackProgress"></a>
# **reportPlaybackProgress**
> reportPlaybackProgress(playbackProgressInfo)

Reports playback progress within a session.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    PlaybackProgressInfo playbackProgressInfo = new PlaybackProgressInfo(); // PlaybackProgressInfo | The playback progress info.
    try {
      apiInstance.reportPlaybackProgress(playbackProgressInfo);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#reportPlaybackProgress");
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
| **playbackProgressInfo** | [**PlaybackProgressInfo**](PlaybackProgressInfo.md)| The playback progress info. | [optional] |

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
| **204** | Playback progress recorded. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="reportPlaybackStart"></a>
# **reportPlaybackStart**
> reportPlaybackStart(playbackStartInfo)

Reports playback has started within a session.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    PlaybackStartInfo playbackStartInfo = new PlaybackStartInfo(); // PlaybackStartInfo | The playback start info.
    try {
      apiInstance.reportPlaybackStart(playbackStartInfo);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#reportPlaybackStart");
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
| **playbackStartInfo** | [**PlaybackStartInfo**](PlaybackStartInfo.md)| The playback start info. | [optional] |

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
| **204** | Playback start recorded. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="reportPlaybackStopped"></a>
# **reportPlaybackStopped**
> reportPlaybackStopped(playbackStopInfo)

Reports playback has stopped within a session.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PlaystateApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PlaystateApi apiInstance = new PlaystateApi(defaultClient);
    PlaybackStopInfo playbackStopInfo = new PlaybackStopInfo(); // PlaybackStopInfo | The playback stop info.
    try {
      apiInstance.reportPlaybackStopped(playbackStopInfo);
    } catch (ApiException e) {
      System.err.println("Exception when calling PlaystateApi#reportPlaybackStopped");
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
| **playbackStopInfo** | [**PlaybackStopInfo**](PlaybackStopInfo.md)| The playback stop info. | [optional] |

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
| **204** | Playback stop recorded. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

