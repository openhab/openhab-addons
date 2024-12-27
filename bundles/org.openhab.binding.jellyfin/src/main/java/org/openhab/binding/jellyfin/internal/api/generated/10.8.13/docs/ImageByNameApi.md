# ImageByNameApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getGeneralImage**](ImageByNameApi.md#getGeneralImage) | **GET** /Images/General/{name}/{type} | Get General Image. |
| [**getGeneralImages**](ImageByNameApi.md#getGeneralImages) | **GET** /Images/General | Get all general images. |
| [**getMediaInfoImage**](ImageByNameApi.md#getMediaInfoImage) | **GET** /Images/MediaInfo/{theme}/{name} | Get media info image. |
| [**getMediaInfoImages**](ImageByNameApi.md#getMediaInfoImages) | **GET** /Images/MediaInfo | Get all media info images. |
| [**getRatingImage**](ImageByNameApi.md#getRatingImage) | **GET** /Images/Ratings/{theme}/{name} | Get rating image. |
| [**getRatingImages**](ImageByNameApi.md#getRatingImages) | **GET** /Images/Ratings | Get all general images. |


<a id="getGeneralImage"></a>
# **getGeneralImage**
> File getGeneralImage(name, type)

Get General Image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageByNameApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageByNameApi apiInstance = new ImageByNameApi(defaultClient);
    String name = "name_example"; // String | The name of the image.
    String type = "type_example"; // String | Image Type (primary, backdrop, logo, etc).
    try {
      File result = apiInstance.getGeneralImage(name, type);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageByNameApi#getGeneralImage");
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
| **name** | **String**| The name of the image. | |
| **type** | **String**| Image Type (primary, backdrop, logo, etc). | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream retrieved. |  -  |
| **404** | Image not found. |  -  |

<a id="getGeneralImages"></a>
# **getGeneralImages**
> List&lt;ImageByNameInfo&gt; getGeneralImages()

Get all general images.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageByNameApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageByNameApi apiInstance = new ImageByNameApi(defaultClient);
    try {
      List<ImageByNameInfo> result = apiInstance.getGeneralImages();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageByNameApi#getGeneralImages");
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

[**List&lt;ImageByNameInfo&gt;**](ImageByNameInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Retrieved list of images. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMediaInfoImage"></a>
# **getMediaInfoImage**
> File getMediaInfoImage(theme, name)

Get media info image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageByNameApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageByNameApi apiInstance = new ImageByNameApi(defaultClient);
    String theme = "theme_example"; // String | The theme to get the image from.
    String name = "name_example"; // String | The name of the image.
    try {
      File result = apiInstance.getMediaInfoImage(theme, name);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageByNameApi#getMediaInfoImage");
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
| **theme** | **String**| The theme to get the image from. | |
| **name** | **String**| The name of the image. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream retrieved. |  -  |
| **404** | Image not found. |  -  |

<a id="getMediaInfoImages"></a>
# **getMediaInfoImages**
> List&lt;ImageByNameInfo&gt; getMediaInfoImages()

Get all media info images.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageByNameApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageByNameApi apiInstance = new ImageByNameApi(defaultClient);
    try {
      List<ImageByNameInfo> result = apiInstance.getMediaInfoImages();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageByNameApi#getMediaInfoImages");
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

[**List&lt;ImageByNameInfo&gt;**](ImageByNameInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image list retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRatingImage"></a>
# **getRatingImage**
> File getRatingImage(theme, name)

Get rating image.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageByNameApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ImageByNameApi apiInstance = new ImageByNameApi(defaultClient);
    String theme = "theme_example"; // String | The theme to get the image from.
    String name = "name_example"; // String | The name of the image.
    try {
      File result = apiInstance.getRatingImage(theme, name);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageByNameApi#getRatingImage");
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
| **theme** | **String**| The theme to get the image from. | |
| **name** | **String**| The name of the image. | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Image stream retrieved. |  -  |
| **404** | Image not found. |  -  |

<a id="getRatingImages"></a>
# **getRatingImages**
> List&lt;ImageByNameInfo&gt; getRatingImages()

Get all general images.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ImageByNameApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ImageByNameApi apiInstance = new ImageByNameApi(defaultClient);
    try {
      List<ImageByNameInfo> result = apiInstance.getRatingImages();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ImageByNameApi#getRatingImages");
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

[**List&lt;ImageByNameInfo&gt;**](ImageByNameInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Retrieved list of images. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

