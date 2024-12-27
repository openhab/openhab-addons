# HlsSegmentApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getHlsAudioSegmentLegacyAac**](HlsSegmentApi.md#getHlsAudioSegmentLegacyAac) | **GET** /Audio/{itemId}/hls/{segmentId}/stream.aac | Gets the specified audio segment for an audio item. |
| [**getHlsAudioSegmentLegacyMp3**](HlsSegmentApi.md#getHlsAudioSegmentLegacyMp3) | **GET** /Audio/{itemId}/hls/{segmentId}/stream.mp3 | Gets the specified audio segment for an audio item. |
| [**getHlsPlaylistLegacy**](HlsSegmentApi.md#getHlsPlaylistLegacy) | **GET** /Videos/{itemId}/hls/{playlistId}/stream.m3u8 | Gets a hls video playlist. |
| [**getHlsVideoSegmentLegacy**](HlsSegmentApi.md#getHlsVideoSegmentLegacy) | **GET** /Videos/{itemId}/hls/{playlistId}/{segmentId}.{segmentContainer} | Gets a hls video segment. |
| [**stopEncodingProcess**](HlsSegmentApi.md#stopEncodingProcess) | **DELETE** /Videos/ActiveEncodings | Stops an active encoding. |


<a id="getHlsAudioSegmentLegacyAac"></a>
# **getHlsAudioSegmentLegacyAac**
> File getHlsAudioSegmentLegacyAac(itemId, segmentId)

Gets the specified audio segment for an audio item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HlsSegmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    HlsSegmentApi apiInstance = new HlsSegmentApi(defaultClient);
    String itemId = "itemId_example"; // String | The item id.
    String segmentId = "segmentId_example"; // String | The segment id.
    try {
      File result = apiInstance.getHlsAudioSegmentLegacyAac(itemId, segmentId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HlsSegmentApi#getHlsAudioSegmentLegacyAac");
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
| **itemId** | **String**| The item id. | |
| **segmentId** | **String**| The segment id. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Hls audio segment returned. |  -  |

<a id="getHlsAudioSegmentLegacyMp3"></a>
# **getHlsAudioSegmentLegacyMp3**
> File getHlsAudioSegmentLegacyMp3(itemId, segmentId)

Gets the specified audio segment for an audio item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HlsSegmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    HlsSegmentApi apiInstance = new HlsSegmentApi(defaultClient);
    String itemId = "itemId_example"; // String | The item id.
    String segmentId = "segmentId_example"; // String | The segment id.
    try {
      File result = apiInstance.getHlsAudioSegmentLegacyMp3(itemId, segmentId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HlsSegmentApi#getHlsAudioSegmentLegacyMp3");
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
| **itemId** | **String**| The item id. | |
| **segmentId** | **String**| The segment id. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Hls audio segment returned. |  -  |

<a id="getHlsPlaylistLegacy"></a>
# **getHlsPlaylistLegacy**
> File getHlsPlaylistLegacy(itemId, playlistId)

Gets a hls video playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HlsSegmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    HlsSegmentApi apiInstance = new HlsSegmentApi(defaultClient);
    String itemId = "itemId_example"; // String | The video id.
    String playlistId = "playlistId_example"; // String | The playlist id.
    try {
      File result = apiInstance.getHlsPlaylistLegacy(itemId, playlistId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HlsSegmentApi#getHlsPlaylistLegacy");
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
| **itemId** | **String**| The video id. | |
| **playlistId** | **String**| The playlist id. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Hls video playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getHlsVideoSegmentLegacy"></a>
# **getHlsVideoSegmentLegacy**
> File getHlsVideoSegmentLegacy(itemId, playlistId, segmentId, segmentContainer)

Gets a hls video segment.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HlsSegmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    HlsSegmentApi apiInstance = new HlsSegmentApi(defaultClient);
    String itemId = "itemId_example"; // String | The item id.
    String playlistId = "playlistId_example"; // String | The playlist id.
    String segmentId = "segmentId_example"; // String | The segment id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    try {
      File result = apiInstance.getHlsVideoSegmentLegacy(itemId, playlistId, segmentId, segmentContainer);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling HlsSegmentApi#getHlsVideoSegmentLegacy");
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
| **itemId** | **String**| The item id. | |
| **playlistId** | **String**| The playlist id. | |
| **segmentId** | **String**| The segment id. | |
| **segmentContainer** | **String**| The segment container. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Hls video segment returned. |  -  |
| **404** | Hls segment not found. |  -  |

<a id="stopEncodingProcess"></a>
# **stopEncodingProcess**
> stopEncodingProcess(deviceId, playSessionId)

Stops an active encoding.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HlsSegmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    HlsSegmentApi apiInstance = new HlsSegmentApi(defaultClient);
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    try {
      apiInstance.stopEncodingProcess(deviceId, playSessionId);
    } catch (ApiException e) {
      System.err.println("Exception when calling HlsSegmentApi#stopEncodingProcess");
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
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | |
| **playSessionId** | **String**| The play session id. | |

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
| **204** | Encoding stopped successfully. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

