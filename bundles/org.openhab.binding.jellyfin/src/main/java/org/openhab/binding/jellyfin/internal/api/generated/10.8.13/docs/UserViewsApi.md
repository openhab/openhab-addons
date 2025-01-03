# UserViewsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getGroupingOptions**](UserViewsApi.md#getGroupingOptions) | **GET** /Users/{userId}/GroupingOptions | Get user view grouping options. |
| [**getUserViews**](UserViewsApi.md#getUserViews) | **GET** /Users/{userId}/Views | Get user views. |


<a id="getGroupingOptions"></a>
# **getGroupingOptions**
> List&lt;SpecialViewOptionDto&gt; getGroupingOptions(userId)

Get user view grouping options.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserViewsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserViewsApi apiInstance = new UserViewsApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      List<SpecialViewOptionDto> result = apiInstance.getGroupingOptions(userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserViewsApi#getGroupingOptions");
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

### Return type

[**List&lt;SpecialViewOptionDto&gt;**](SpecialViewOptionDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User view grouping options returned. |  -  |
| **404** | User not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getUserViews"></a>
# **getUserViews**
> BaseItemDtoQueryResult getUserViews(userId, includeExternalContent, presetViews, includeHidden)

Get user views.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserViewsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserViewsApi apiInstance = new UserViewsApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    Boolean includeExternalContent = true; // Boolean | Whether or not to include external views such as channels or live tv.
    List<String> presetViews = Arrays.asList(); // List<String> | Preset views.
    Boolean includeHidden = false; // Boolean | Whether or not to include hidden content.
    try {
      BaseItemDtoQueryResult result = apiInstance.getUserViews(userId, includeExternalContent, presetViews, includeHidden);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserViewsApi#getUserViews");
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
| **includeExternalContent** | **Boolean**| Whether or not to include external views such as channels or live tv. | [optional] |
| **presetViews** | [**List&lt;String&gt;**](String.md)| Preset views. | [optional] |
| **includeHidden** | **Boolean**| Whether or not to include hidden content. | [optional] [default to false] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User views returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

