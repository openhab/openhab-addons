# RemoteImageApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**downloadRemoteImage**](RemoteImageApi.md#downloadRemoteImage) | **POST** /Items/{itemId}/RemoteImages/Download | Downloads a remote image for an item. |
| [**getRemoteImageProviders**](RemoteImageApi.md#getRemoteImageProviders) | **GET** /Items/{itemId}/RemoteImages/Providers | Gets available remote image providers for an item. |
| [**getRemoteImages**](RemoteImageApi.md#getRemoteImages) | **GET** /Items/{itemId}/RemoteImages | Gets available remote images for an item. |


<a id="downloadRemoteImage"></a>
# **downloadRemoteImage**
> downloadRemoteImage(itemId, type, imageUrl)

Downloads a remote image for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RemoteImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    RemoteImageApi apiInstance = new RemoteImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item Id.
    ImageType type = ImageType.fromValue("Primary"); // ImageType | The image type.
    String imageUrl = "imageUrl_example"; // String | The image url.
    try {
      apiInstance.downloadRemoteImage(itemId, type, imageUrl);
    } catch (ApiException e) {
      System.err.println("Exception when calling RemoteImageApi#downloadRemoteImage");
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
| **itemId** | **UUID**| Item Id. | |
| **type** | **ImageType**| The image type. | [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **imageUrl** | **String**| The image url. | [optional] |

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
| **204** | Remote image downloaded. |  -  |
| **404** | Remote image not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRemoteImageProviders"></a>
# **getRemoteImageProviders**
> List&lt;ImageProviderInfo&gt; getRemoteImageProviders(itemId)

Gets available remote image providers for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RemoteImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    RemoteImageApi apiInstance = new RemoteImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item Id.
    try {
      List<ImageProviderInfo> result = apiInstance.getRemoteImageProviders(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RemoteImageApi#getRemoteImageProviders");
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
| **itemId** | **UUID**| Item Id. | |

### Return type

[**List&lt;ImageProviderInfo&gt;**](ImageProviderInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Returned remote image providers. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRemoteImages"></a>
# **getRemoteImages**
> RemoteImageResult getRemoteImages(itemId, type, startIndex, limit, providerName, includeAllLanguages)

Gets available remote images for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RemoteImageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    RemoteImageApi apiInstance = new RemoteImageApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item Id.
    ImageType type = ImageType.fromValue("Primary"); // ImageType | The image type.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    String providerName = "providerName_example"; // String | Optional. The image provider to use.
    Boolean includeAllLanguages = false; // Boolean | Optional. Include all languages.
    try {
      RemoteImageResult result = apiInstance.getRemoteImages(itemId, type, startIndex, limit, providerName, includeAllLanguages);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RemoteImageApi#getRemoteImages");
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
| **itemId** | **UUID**| Item Id. | |
| **type** | **ImageType**| The image type. | [optional] [enum: Primary, Art, Backdrop, Banner, Logo, Thumb, Disc, Box, Screenshot, Menu, Chapter, BoxRear, Profile] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **providerName** | **String**| Optional. The image provider to use. | [optional] |
| **includeAllLanguages** | **Boolean**| Optional. Include all languages. | [optional] [default to false] |

### Return type

[**RemoteImageResult**](RemoteImageResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Remote Images returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

