# MediaInfoApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**closeLiveStream**](MediaInfoApi.md#closeLiveStream) | **POST** /LiveStreams/Close | Closes a media source. |
| [**getBitrateTestBytes**](MediaInfoApi.md#getBitrateTestBytes) | **GET** /Playback/BitrateTest | Tests the network with a request with the size of the bitrate. |
| [**getPlaybackInfo**](MediaInfoApi.md#getPlaybackInfo) | **GET** /Items/{itemId}/PlaybackInfo | Gets live playback media info for an item. |
| [**getPostedPlaybackInfo**](MediaInfoApi.md#getPostedPlaybackInfo) | **POST** /Items/{itemId}/PlaybackInfo | Gets live playback media info for an item. |
| [**openLiveStream**](MediaInfoApi.md#openLiveStream) | **POST** /LiveStreams/Open | Opens a media source. |


<a id="closeLiveStream"></a>
# **closeLiveStream**
> closeLiveStream(liveStreamId)

Closes a media source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MediaInfoApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MediaInfoApi apiInstance = new MediaInfoApi(defaultClient);
    String liveStreamId = "liveStreamId_example"; // String | The livestream id.
    try {
      apiInstance.closeLiveStream(liveStreamId);
    } catch (ApiException e) {
      System.err.println("Exception when calling MediaInfoApi#closeLiveStream");
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
| **liveStreamId** | **String**| The livestream id. | |

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
| **204** | Livestream closed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getBitrateTestBytes"></a>
# **getBitrateTestBytes**
> File getBitrateTestBytes(size)

Tests the network with a request with the size of the bitrate.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MediaInfoApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MediaInfoApi apiInstance = new MediaInfoApi(defaultClient);
    Integer size = 102400; // Integer | The bitrate. Defaults to 102400.
    try {
      File result = apiInstance.getBitrateTestBytes(size);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MediaInfoApi#getBitrateTestBytes");
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
| **size** | **Integer**| The bitrate. Defaults to 102400. | [optional] [default to 102400] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Test buffer returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPlaybackInfo"></a>
# **getPlaybackInfo**
> PlaybackInfoResponse getPlaybackInfo(itemId, userId)

Gets live playback media info for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MediaInfoApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MediaInfoApi apiInstance = new MediaInfoApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    try {
      PlaybackInfoResponse result = apiInstance.getPlaybackInfo(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MediaInfoApi#getPlaybackInfo");
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
| **itemId** | **UUID**| The item id. | |
| **userId** | **UUID**| The user id. | |

### Return type

[**PlaybackInfoResponse**](PlaybackInfoResponse.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Playback info returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPostedPlaybackInfo"></a>
# **getPostedPlaybackInfo**
> PlaybackInfoResponse getPostedPlaybackInfo(itemId, userId, maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels, mediaSourceId, liveStreamId, autoOpenLiveStream, enableDirectPlay, enableDirectStream, enableTranscoding, allowVideoStreamCopy, allowAudioStreamCopy, playbackInfoDto)

Gets live playback media info for an item.

For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence.  Query parameters are obsolete.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MediaInfoApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MediaInfoApi apiInstance = new MediaInfoApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    Integer maxStreamingBitrate = 56; // Integer | The maximum streaming bitrate.
    Long startTimeTicks = 56L; // Long | The start time in ticks.
    Integer audioStreamIndex = 56; // Integer | The audio stream index.
    Integer subtitleStreamIndex = 56; // Integer | The subtitle stream index.
    Integer maxAudioChannels = 56; // Integer | The maximum number of audio channels.
    String mediaSourceId = "mediaSourceId_example"; // String | The media source id.
    String liveStreamId = "liveStreamId_example"; // String | The livestream id.
    Boolean autoOpenLiveStream = true; // Boolean | Whether to auto open the livestream.
    Boolean enableDirectPlay = true; // Boolean | Whether to enable direct play. Default: true.
    Boolean enableDirectStream = true; // Boolean | Whether to enable direct stream. Default: true.
    Boolean enableTranscoding = true; // Boolean | Whether to enable transcoding. Default: true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether to allow to copy the video stream. Default: true.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether to allow to copy the audio stream. Default: true.
    PlaybackInfoDto playbackInfoDto = new PlaybackInfoDto(); // PlaybackInfoDto | The playback info.
    try {
      PlaybackInfoResponse result = apiInstance.getPostedPlaybackInfo(itemId, userId, maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels, mediaSourceId, liveStreamId, autoOpenLiveStream, enableDirectPlay, enableDirectStream, enableTranscoding, allowVideoStreamCopy, allowAudioStreamCopy, playbackInfoDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MediaInfoApi#getPostedPlaybackInfo");
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
| **itemId** | **UUID**| The item id. | |
| **userId** | **UUID**| The user id. | [optional] |
| **maxStreamingBitrate** | **Integer**| The maximum streaming bitrate. | [optional] |
| **startTimeTicks** | **Long**| The start time in ticks. | [optional] |
| **audioStreamIndex** | **Integer**| The audio stream index. | [optional] |
| **subtitleStreamIndex** | **Integer**| The subtitle stream index. | [optional] |
| **maxAudioChannels** | **Integer**| The maximum number of audio channels. | [optional] |
| **mediaSourceId** | **String**| The media source id. | [optional] |
| **liveStreamId** | **String**| The livestream id. | [optional] |
| **autoOpenLiveStream** | **Boolean**| Whether to auto open the livestream. | [optional] |
| **enableDirectPlay** | **Boolean**| Whether to enable direct play. Default: true. | [optional] |
| **enableDirectStream** | **Boolean**| Whether to enable direct stream. Default: true. | [optional] |
| **enableTranscoding** | **Boolean**| Whether to enable transcoding. Default: true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether to allow to copy the video stream. Default: true. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether to allow to copy the audio stream. Default: true. | [optional] |
| **playbackInfoDto** | [**PlaybackInfoDto**](PlaybackInfoDto.md)| The playback info. | [optional] |

### Return type

[**PlaybackInfoResponse**](PlaybackInfoResponse.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Playback info returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="openLiveStream"></a>
# **openLiveStream**
> LiveStreamResponse openLiveStream(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream, openLiveStreamDto)

Opens a media source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MediaInfoApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MediaInfoApi apiInstance = new MediaInfoApi(defaultClient);
    String openToken = "openToken_example"; // String | The open token.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    Integer maxStreamingBitrate = 56; // Integer | The maximum streaming bitrate.
    Long startTimeTicks = 56L; // Long | The start time in ticks.
    Integer audioStreamIndex = 56; // Integer | The audio stream index.
    Integer subtitleStreamIndex = 56; // Integer | The subtitle stream index.
    Integer maxAudioChannels = 56; // Integer | The maximum number of audio channels.
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Boolean enableDirectPlay = true; // Boolean | Whether to enable direct play. Default: true.
    Boolean enableDirectStream = true; // Boolean | Whether to enable direct stream. Default: true.
    OpenLiveStreamDto openLiveStreamDto = new OpenLiveStreamDto(); // OpenLiveStreamDto | The open live stream dto.
    try {
      LiveStreamResponse result = apiInstance.openLiveStream(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream, openLiveStreamDto);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MediaInfoApi#openLiveStream");
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
| **openToken** | **String**| The open token. | [optional] |
| **userId** | **UUID**| The user id. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **maxStreamingBitrate** | **Integer**| The maximum streaming bitrate. | [optional] |
| **startTimeTicks** | **Long**| The start time in ticks. | [optional] |
| **audioStreamIndex** | **Integer**| The audio stream index. | [optional] |
| **subtitleStreamIndex** | **Integer**| The subtitle stream index. | [optional] |
| **maxAudioChannels** | **Integer**| The maximum number of audio channels. | [optional] |
| **itemId** | **UUID**| The item id. | [optional] |
| **enableDirectPlay** | **Boolean**| Whether to enable direct play. Default: true. | [optional] |
| **enableDirectStream** | **Boolean**| Whether to enable direct stream. Default: true. | [optional] |
| **openLiveStreamDto** | [**OpenLiveStreamDto**](OpenLiveStreamDto.md)| The open live stream dto. | [optional] |

### Return type

[**LiveStreamResponse**](LiveStreamResponse.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Media source opened. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

