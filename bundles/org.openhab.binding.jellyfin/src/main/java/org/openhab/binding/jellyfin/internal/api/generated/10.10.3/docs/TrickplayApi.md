# TrickplayApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getTrickplayHlsPlaylist**](TrickplayApi.md#getTrickplayHlsPlaylist) | **GET** /Videos/{itemId}/Trickplay/{width}/tiles.m3u8 | Gets an image tiles playlist for trickplay. |
| [**getTrickplayTileImage**](TrickplayApi.md#getTrickplayTileImage) | **GET** /Videos/{itemId}/Trickplay/{width}/{index}.jpg | Gets a trickplay tile image. |


<a id="getTrickplayHlsPlaylist"></a>
# **getTrickplayHlsPlaylist**
> File getTrickplayHlsPlaylist(itemId, width, mediaSourceId)

Gets an image tiles playlist for trickplay.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TrickplayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TrickplayApi apiInstance = new TrickplayApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Integer width = 56; // Integer | The width of a single tile.
    UUID mediaSourceId = UUID.randomUUID(); // UUID | The media version id, if using an alternate version.
    try {
      File result = apiInstance.getTrickplayHlsPlaylist(itemId, width, mediaSourceId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TrickplayApi#getTrickplayHlsPlaylist");
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
| **width** | **Integer**| The width of a single tile. | |
| **mediaSourceId** | **UUID**| The media version id, if using an alternate version. | [optional] |

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
| **200** | Tiles playlist returned. |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTrickplayTileImage"></a>
# **getTrickplayTileImage**
> File getTrickplayTileImage(itemId, width, index, mediaSourceId)

Gets a trickplay tile image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TrickplayApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TrickplayApi apiInstance = new TrickplayApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Integer width = 56; // Integer | The width of a single tile.
    Integer index = 56; // Integer | The index of the desired tile.
    UUID mediaSourceId = UUID.randomUUID(); // UUID | The media version id, if using an alternate version.
    try {
      File result = apiInstance.getTrickplayTileImage(itemId, width, index, mediaSourceId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TrickplayApi#getTrickplayTileImage");
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
| **width** | **Integer**| The width of a single tile. | |
| **index** | **Integer**| The index of the desired tile. | |
| **mediaSourceId** | **UUID**| The media version id, if using an alternate version. | [optional] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Tile image not found at specified index. |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

