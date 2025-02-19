# SubtitleApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteSubtitle**](SubtitleApi.md#deleteSubtitle) | **DELETE** /Videos/{itemId}/Subtitles/{index} | Deletes an external subtitle file. |
| [**downloadRemoteSubtitles**](SubtitleApi.md#downloadRemoteSubtitles) | **POST** /Items/{itemId}/RemoteSearch/Subtitles/{subtitleId} | Downloads a remote subtitle. |
| [**getFallbackFont**](SubtitleApi.md#getFallbackFont) | **GET** /FallbackFont/Fonts/{name} | Gets a fallback font file. |
| [**getFallbackFontList**](SubtitleApi.md#getFallbackFontList) | **GET** /FallbackFont/Fonts | Gets a list of available fallback font files. |
| [**getRemoteSubtitles**](SubtitleApi.md#getRemoteSubtitles) | **GET** /Providers/Subtitles/Subtitles/{subtitleId} | Gets the remote subtitles. |
| [**getSubtitle**](SubtitleApi.md#getSubtitle) | **GET** /Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/Stream.{routeFormat} | Gets subtitles in a specified format. |
| [**getSubtitlePlaylist**](SubtitleApi.md#getSubtitlePlaylist) | **GET** /Videos/{itemId}/{mediaSourceId}/Subtitles/{index}/subtitles.m3u8 | Gets an HLS subtitle playlist. |
| [**getSubtitleWithTicks**](SubtitleApi.md#getSubtitleWithTicks) | **GET** /Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/{routeStartPositionTicks}/Stream.{routeFormat} | Gets subtitles in a specified format. |
| [**searchRemoteSubtitles**](SubtitleApi.md#searchRemoteSubtitles) | **GET** /Items/{itemId}/RemoteSearch/Subtitles/{language} | Search remote subtitles. |
| [**uploadSubtitle**](SubtitleApi.md#uploadSubtitle) | **POST** /Videos/{itemId}/Subtitles | Upload an external subtitle file. |


<a id="deleteSubtitle"></a>
# **deleteSubtitle**
> deleteSubtitle(itemId, index)

Deletes an external subtitle file.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Integer index = 56; // Integer | The index of the subtitle file.
    try {
      apiInstance.deleteSubtitle(itemId, index);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#deleteSubtitle");
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
| **index** | **Integer**| The index of the subtitle file. | |

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
| **204** | Subtitle deleted. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="downloadRemoteSubtitles"></a>
# **downloadRemoteSubtitles**
> downloadRemoteSubtitles(itemId, subtitleId)

Downloads a remote subtitle.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String subtitleId = "subtitleId_example"; // String | The subtitle id.
    try {
      apiInstance.downloadRemoteSubtitles(itemId, subtitleId);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#downloadRemoteSubtitles");
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
| **subtitleId** | **String**| The subtitle id. | |

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
| **204** | Subtitle downloaded. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getFallbackFont"></a>
# **getFallbackFont**
> File getFallbackFont(name)

Gets a fallback font file.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    String name = "name_example"; // String | The name of the fallback font file to get.
    try {
      File result = apiInstance.getFallbackFont(name);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#getFallbackFont");
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
| **name** | **String**| The name of the fallback font file to get. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: font/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Fallback font file retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getFallbackFontList"></a>
# **getFallbackFontList**
> List&lt;FontFile&gt; getFallbackFontList()

Gets a list of available fallback font files.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    try {
      List<FontFile> result = apiInstance.getFallbackFontList();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#getFallbackFontList");
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

[**List&lt;FontFile&gt;**](FontFile.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Information retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRemoteSubtitles"></a>
# **getRemoteSubtitles**
> File getRemoteSubtitles(subtitleId)

Gets the remote subtitles.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    String subtitleId = "subtitleId_example"; // String | The item id.
    try {
      File result = apiInstance.getRemoteSubtitles(subtitleId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#getRemoteSubtitles");
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
| **subtitleId** | **String**| The item id. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | File returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSubtitle"></a>
# **getSubtitle**
> File getSubtitle(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId, mediaSourceId, index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks)

Gets subtitles in a specified format.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID routeItemId = UUID.randomUUID(); // UUID | The (route) item id.
    String routeMediaSourceId = "routeMediaSourceId_example"; // String | The (route) media source id.
    Integer routeIndex = 56; // Integer | The (route) subtitle stream index.
    String routeFormat = "routeFormat_example"; // String | The (route) format of the returned subtitle.
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The media source id.
    Integer index = 56; // Integer | The subtitle stream index.
    String format = "format_example"; // String | The format of the returned subtitle.
    Long endPositionTicks = 56L; // Long | Optional. The end position of the subtitle in ticks.
    Boolean copyTimestamps = false; // Boolean | Optional. Whether to copy the timestamps.
    Boolean addVttTimeMap = false; // Boolean | Optional. Whether to add a VTT time map.
    Long startPositionTicks = 0L; // Long | The start position of the subtitle in ticks.
    try {
      File result = apiInstance.getSubtitle(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId, mediaSourceId, index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#getSubtitle");
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
| **routeItemId** | **UUID**| The (route) item id. | |
| **routeMediaSourceId** | **String**| The (route) media source id. | |
| **routeIndex** | **Integer**| The (route) subtitle stream index. | |
| **routeFormat** | **String**| The (route) format of the returned subtitle. | |
| **itemId** | **UUID**| The item id. | [optional] |
| **mediaSourceId** | **String**| The media source id. | [optional] |
| **index** | **Integer**| The subtitle stream index. | [optional] |
| **format** | **String**| The format of the returned subtitle. | [optional] |
| **endPositionTicks** | **Long**| Optional. The end position of the subtitle in ticks. | [optional] |
| **copyTimestamps** | **Boolean**| Optional. Whether to copy the timestamps. | [optional] [default to false] |
| **addVttTimeMap** | **Boolean**| Optional. Whether to add a VTT time map. | [optional] [default to false] |
| **startPositionTicks** | **Long**| The start position of the subtitle in ticks. | [optional] [default to 0] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | File returned. |  -  |

<a id="getSubtitlePlaylist"></a>
# **getSubtitlePlaylist**
> File getSubtitlePlaylist(itemId, index, mediaSourceId, segmentLength)

Gets an HLS subtitle playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Integer index = 56; // Integer | The subtitle stream index.
    String mediaSourceId = "mediaSourceId_example"; // String | The media source id.
    Integer segmentLength = 56; // Integer | The subtitle segment length.
    try {
      File result = apiInstance.getSubtitlePlaylist(itemId, index, mediaSourceId, segmentLength);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#getSubtitlePlaylist");
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
| **index** | **Integer**| The subtitle stream index. | |
| **mediaSourceId** | **String**| The media source id. | |
| **segmentLength** | **Integer**| The subtitle segment length. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Subtitle playlist retrieved. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSubtitleWithTicks"></a>
# **getSubtitleWithTicks**
> File getSubtitleWithTicks(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks, routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps, addVttTimeMap)

Gets subtitles in a specified format.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID routeItemId = UUID.randomUUID(); // UUID | The (route) item id.
    String routeMediaSourceId = "routeMediaSourceId_example"; // String | The (route) media source id.
    Integer routeIndex = 56; // Integer | The (route) subtitle stream index.
    Long routeStartPositionTicks = 56L; // Long | The (route) start position of the subtitle in ticks.
    String routeFormat = "routeFormat_example"; // String | The (route) format of the returned subtitle.
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The media source id.
    Integer index = 56; // Integer | The subtitle stream index.
    Long startPositionTicks = 56L; // Long | The start position of the subtitle in ticks.
    String format = "format_example"; // String | The format of the returned subtitle.
    Long endPositionTicks = 56L; // Long | Optional. The end position of the subtitle in ticks.
    Boolean copyTimestamps = false; // Boolean | Optional. Whether to copy the timestamps.
    Boolean addVttTimeMap = false; // Boolean | Optional. Whether to add a VTT time map.
    try {
      File result = apiInstance.getSubtitleWithTicks(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks, routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps, addVttTimeMap);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#getSubtitleWithTicks");
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
| **routeItemId** | **UUID**| The (route) item id. | |
| **routeMediaSourceId** | **String**| The (route) media source id. | |
| **routeIndex** | **Integer**| The (route) subtitle stream index. | |
| **routeStartPositionTicks** | **Long**| The (route) start position of the subtitle in ticks. | |
| **routeFormat** | **String**| The (route) format of the returned subtitle. | |
| **itemId** | **UUID**| The item id. | [optional] |
| **mediaSourceId** | **String**| The media source id. | [optional] |
| **index** | **Integer**| The subtitle stream index. | [optional] |
| **startPositionTicks** | **Long**| The start position of the subtitle in ticks. | [optional] |
| **format** | **String**| The format of the returned subtitle. | [optional] |
| **endPositionTicks** | **Long**| Optional. The end position of the subtitle in ticks. | [optional] |
| **copyTimestamps** | **Boolean**| Optional. Whether to copy the timestamps. | [optional] [default to false] |
| **addVttTimeMap** | **Boolean**| Optional. Whether to add a VTT time map. | [optional] [default to false] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | File returned. |  -  |

<a id="searchRemoteSubtitles"></a>
# **searchRemoteSubtitles**
> List&lt;RemoteSubtitleInfo&gt; searchRemoteSubtitles(itemId, language, isPerfectMatch)

Search remote subtitles.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String language = "language_example"; // String | The language of the subtitles.
    Boolean isPerfectMatch = true; // Boolean | Optional. Only show subtitles which are a perfect match.
    try {
      List<RemoteSubtitleInfo> result = apiInstance.searchRemoteSubtitles(itemId, language, isPerfectMatch);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#searchRemoteSubtitles");
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
| **language** | **String**| The language of the subtitles. | |
| **isPerfectMatch** | **Boolean**| Optional. Only show subtitles which are a perfect match. | [optional] |

### Return type

[**List&lt;RemoteSubtitleInfo&gt;**](RemoteSubtitleInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Subtitles retrieved. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="uploadSubtitle"></a>
# **uploadSubtitle**
> uploadSubtitle(itemId, uploadSubtitleDto)

Upload an external subtitle file.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SubtitleApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SubtitleApi apiInstance = new SubtitleApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item the subtitle belongs to.
    UploadSubtitleDto uploadSubtitleDto = new UploadSubtitleDto(); // UploadSubtitleDto | The request body.
    try {
      apiInstance.uploadSubtitle(itemId, uploadSubtitleDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling SubtitleApi#uploadSubtitle");
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
| **itemId** | **UUID**| The item the subtitle belongs to. | |
| **uploadSubtitleDto** | [**UploadSubtitleDto**](UploadSubtitleDto.md)| The request body. | |

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
| **204** | Subtitle uploaded. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

