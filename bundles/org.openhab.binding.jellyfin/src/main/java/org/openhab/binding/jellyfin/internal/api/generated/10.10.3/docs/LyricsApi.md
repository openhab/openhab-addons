# LyricsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteLyrics**](LyricsApi.md#deleteLyrics) | **DELETE** /Audio/{itemId}/Lyrics | Deletes an external lyric file. |
| [**downloadRemoteLyrics**](LyricsApi.md#downloadRemoteLyrics) | **POST** /Audio/{itemId}/RemoteSearch/Lyrics/{lyricId} | Downloads a remote lyric. |
| [**getLyrics**](LyricsApi.md#getLyrics) | **GET** /Audio/{itemId}/Lyrics | Gets an item&#39;s lyrics. |
| [**getRemoteLyrics**](LyricsApi.md#getRemoteLyrics) | **GET** /Providers/Lyrics/{lyricId} | Gets the remote lyrics. |
| [**searchRemoteLyrics**](LyricsApi.md#searchRemoteLyrics) | **GET** /Audio/{itemId}/RemoteSearch/Lyrics | Search remote lyrics. |
| [**uploadLyrics**](LyricsApi.md#uploadLyrics) | **POST** /Audio/{itemId}/Lyrics | Upload an external lyric file. |


<a id="deleteLyrics"></a>
# **deleteLyrics**
> deleteLyrics(itemId)

Deletes an external lyric file.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LyricsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LyricsApi apiInstance = new LyricsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    try {
      apiInstance.deleteLyrics(itemId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LyricsApi#deleteLyrics");
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
| **204** | Lyric deleted. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="downloadRemoteLyrics"></a>
# **downloadRemoteLyrics**
> LyricDto downloadRemoteLyrics(itemId, lyricId)

Downloads a remote lyric.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LyricsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LyricsApi apiInstance = new LyricsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String lyricId = "lyricId_example"; // String | The lyric id.
    try {
      LyricDto result = apiInstance.downloadRemoteLyrics(itemId, lyricId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LyricsApi#downloadRemoteLyrics");
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
| **lyricId** | **String**| The lyric id. | |

### Return type

[**LyricDto**](LyricDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Lyric downloaded. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLyrics"></a>
# **getLyrics**
> LyricDto getLyrics(itemId)

Gets an item&#39;s lyrics.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LyricsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LyricsApi apiInstance = new LyricsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    try {
      LyricDto result = apiInstance.getLyrics(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LyricsApi#getLyrics");
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
| **itemId** | **UUID**| Item id. | |

### Return type

[**LyricDto**](LyricDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Lyrics returned. |  -  |
| **404** | Something went wrong. No Lyrics will be returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRemoteLyrics"></a>
# **getRemoteLyrics**
> LyricDto getRemoteLyrics(lyricId)

Gets the remote lyrics.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LyricsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LyricsApi apiInstance = new LyricsApi(defaultClient);
    String lyricId = "lyricId_example"; // String | The remote provider item id.
    try {
      LyricDto result = apiInstance.getRemoteLyrics(lyricId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LyricsApi#getRemoteLyrics");
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
| **lyricId** | **String**| The remote provider item id. | |

### Return type

[**LyricDto**](LyricDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | File returned. |  -  |
| **404** | Lyric not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="searchRemoteLyrics"></a>
# **searchRemoteLyrics**
> List&lt;RemoteLyricInfoDto&gt; searchRemoteLyrics(itemId)

Search remote lyrics.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LyricsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LyricsApi apiInstance = new LyricsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    try {
      List<RemoteLyricInfoDto> result = apiInstance.searchRemoteLyrics(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LyricsApi#searchRemoteLyrics");
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

### Return type

[**List&lt;RemoteLyricInfoDto&gt;**](RemoteLyricInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Lyrics retrieved. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="uploadLyrics"></a>
# **uploadLyrics**
> LyricDto uploadLyrics(itemId, fileName, body)

Upload an external lyric file.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LyricsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LyricsApi apiInstance = new LyricsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item the lyric belongs to.
    String fileName = "fileName_example"; // String | Name of the file being uploaded.
    File body = new File("/path/to/file"); // File | 
    try {
      LyricDto result = apiInstance.uploadLyrics(itemId, fileName, body);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LyricsApi#uploadLyrics");
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
| **itemId** | **UUID**| The item the lyric belongs to. | |
| **fileName** | **String**| Name of the file being uploaded. | |
| **body** | **File**|  | [optional] |

### Return type

[**LyricDto**](LyricDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: text/plain
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Lyrics uploaded. |  -  |
| **400** | Error processing upload. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

