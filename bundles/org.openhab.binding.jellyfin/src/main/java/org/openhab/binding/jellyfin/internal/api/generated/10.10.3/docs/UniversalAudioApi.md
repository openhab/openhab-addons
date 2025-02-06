# UniversalAudioApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getUniversalAudioStream**](UniversalAudioApi.md#getUniversalAudioStream) | **GET** /Audio/{itemId}/universal | Gets an audio stream. |
| [**headUniversalAudioStream**](UniversalAudioApi.md#headUniversalAudioStream) | **HEAD** /Audio/{itemId}/universal | Gets an audio stream. |


<a id="getUniversalAudioStream"></a>
# **getUniversalAudioStream**
> File getUniversalAudioStream(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection)

Gets an audio stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UniversalAudioApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UniversalAudioApi apiInstance = new UniversalAudioApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<String> container = Arrays.asList(); // List<String> | Optional. The audio container.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    UUID userId = UUID.randomUUID(); // UUID | Optional. The user id.
    String audioCodec = "audioCodec_example"; // String | Optional. The audio codec to transcode to.
    Integer maxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels.
    Integer transcodingAudioChannels = 56; // Integer | Optional. The number of how many audio channels to transcode to.
    Integer maxStreamingBitrate = 56; // Integer | Optional. The maximum streaming bitrate.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    String transcodingContainer = "transcodingContainer_example"; // String | Optional. The container to transcode to.
    MediaStreamProtocol transcodingProtocol = MediaStreamProtocol.fromValue("http"); // MediaStreamProtocol | Optional. The transcoding protocol.
    Integer maxAudioSampleRate = 56; // Integer | Optional. The maximum audio sample rate.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Boolean enableRemoteMedia = true; // Boolean | Optional. Whether to enable remote media.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    Boolean breakOnNonKeyFrames = false; // Boolean | Optional. Whether to break on non key frames.
    Boolean enableRedirection = true; // Boolean | Whether to enable redirection. Defaults to true.
    try {
      File result = apiInstance.getUniversalAudioStream(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UniversalAudioApi#getUniversalAudioStream");
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
| **container** | [**List&lt;String&gt;**](String.md)| Optional. The audio container. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **userId** | **UUID**| Optional. The user id. | [optional] |
| **audioCodec** | **String**| Optional. The audio codec to transcode to. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. The maximum number of audio channels. | [optional] |
| **transcodingAudioChannels** | **Integer**| Optional. The number of how many audio channels to transcode to. | [optional] |
| **maxStreamingBitrate** | **Integer**| Optional. The maximum streaming bitrate. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **transcodingContainer** | **String**| Optional. The container to transcode to. | [optional] |
| **transcodingProtocol** | **MediaStreamProtocol**| Optional. The transcoding protocol. | [optional] [enum: http, hls] |
| **maxAudioSampleRate** | **Integer**| Optional. The maximum audio sample rate. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **enableRemoteMedia** | **Boolean**| Optional. Whether to enable remote media. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] [default to false] |
| **enableRedirection** | **Boolean**| Whether to enable redirection. Defaults to true. | [optional] [default to true] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |
| **302** | Redirected to remote audio stream. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="headUniversalAudioStream"></a>
# **headUniversalAudioStream**
> File headUniversalAudioStream(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection)

Gets an audio stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UniversalAudioApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UniversalAudioApi apiInstance = new UniversalAudioApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<String> container = Arrays.asList(); // List<String> | Optional. The audio container.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    UUID userId = UUID.randomUUID(); // UUID | Optional. The user id.
    String audioCodec = "audioCodec_example"; // String | Optional. The audio codec to transcode to.
    Integer maxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels.
    Integer transcodingAudioChannels = 56; // Integer | Optional. The number of how many audio channels to transcode to.
    Integer maxStreamingBitrate = 56; // Integer | Optional. The maximum streaming bitrate.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    String transcodingContainer = "transcodingContainer_example"; // String | Optional. The container to transcode to.
    MediaStreamProtocol transcodingProtocol = MediaStreamProtocol.fromValue("http"); // MediaStreamProtocol | Optional. The transcoding protocol.
    Integer maxAudioSampleRate = 56; // Integer | Optional. The maximum audio sample rate.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Boolean enableRemoteMedia = true; // Boolean | Optional. Whether to enable remote media.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    Boolean breakOnNonKeyFrames = false; // Boolean | Optional. Whether to break on non key frames.
    Boolean enableRedirection = true; // Boolean | Whether to enable redirection. Defaults to true.
    try {
      File result = apiInstance.headUniversalAudioStream(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UniversalAudioApi#headUniversalAudioStream");
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
| **container** | [**List&lt;String&gt;**](String.md)| Optional. The audio container. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **userId** | **UUID**| Optional. The user id. | [optional] |
| **audioCodec** | **String**| Optional. The audio codec to transcode to. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. The maximum number of audio channels. | [optional] |
| **transcodingAudioChannels** | **Integer**| Optional. The number of how many audio channels to transcode to. | [optional] |
| **maxStreamingBitrate** | **Integer**| Optional. The maximum streaming bitrate. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **transcodingContainer** | **String**| Optional. The container to transcode to. | [optional] |
| **transcodingProtocol** | **MediaStreamProtocol**| Optional. The transcoding protocol. | [optional] [enum: http, hls] |
| **maxAudioSampleRate** | **Integer**| Optional. The maximum audio sample rate. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **enableRemoteMedia** | **Boolean**| Optional. Whether to enable remote media. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] [default to false] |
| **enableRedirection** | **Boolean**| Whether to enable redirection. Defaults to true. | [optional] [default to true] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |
| **302** | Redirected to remote audio stream. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

