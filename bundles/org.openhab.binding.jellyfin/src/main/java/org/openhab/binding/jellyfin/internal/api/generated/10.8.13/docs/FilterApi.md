# FilterApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getQueryFilters**](FilterApi.md#getQueryFilters) | **GET** /Items/Filters2 | Gets query filters. |
| [**getQueryFiltersLegacy**](FilterApi.md#getQueryFiltersLegacy) | **GET** /Items/Filters | Gets legacy query filters. |


<a id="getQueryFilters"></a>
# **getQueryFilters**
> QueryFilters getQueryFilters(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive)

Gets query filters.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.FilterApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    FilterApi apiInstance = new FilterApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. User id.
    UUID parentId = UUID.randomUUID(); // UUID | Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    Boolean isAiring = true; // Boolean | Optional. Is item airing.
    Boolean isMovie = true; // Boolean | Optional. Is item movie.
    Boolean isSports = true; // Boolean | Optional. Is item sports.
    Boolean isKids = true; // Boolean | Optional. Is item kids.
    Boolean isNews = true; // Boolean | Optional. Is item news.
    Boolean isSeries = true; // Boolean | Optional. Is item series.
    Boolean recursive = true; // Boolean | Optional. Search recursive.
    try {
      QueryFilters result = apiInstance.getQueryFilters(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling FilterApi#getQueryFilters");
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
| **userId** | **UUID**| Optional. User id. | [optional] |
| **parentId** | **UUID**| Optional. Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **isAiring** | **Boolean**| Optional. Is item airing. | [optional] |
| **isMovie** | **Boolean**| Optional. Is item movie. | [optional] |
| **isSports** | **Boolean**| Optional. Is item sports. | [optional] |
| **isKids** | **Boolean**| Optional. Is item kids. | [optional] |
| **isNews** | **Boolean**| Optional. Is item news. | [optional] |
| **isSeries** | **Boolean**| Optional. Is item series. | [optional] |
| **recursive** | **Boolean**| Optional. Search recursive. | [optional] |

### Return type

[**QueryFilters**](QueryFilters.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Filters retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getQueryFiltersLegacy"></a>
# **getQueryFiltersLegacy**
> QueryFiltersLegacy getQueryFiltersLegacy(userId, parentId, includeItemTypes, mediaTypes)

Gets legacy query filters.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.FilterApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    FilterApi apiInstance = new FilterApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. User id.
    UUID parentId = UUID.randomUUID(); // UUID | Optional. Parent id.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    List<String> mediaTypes = Arrays.asList(); // List<String> | Optional. Filter by MediaType. Allows multiple, comma delimited.
    try {
      QueryFiltersLegacy result = apiInstance.getQueryFiltersLegacy(userId, parentId, includeItemTypes, mediaTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling FilterApi#getQueryFiltersLegacy");
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
| **userId** | **UUID**| Optional. User id. | [optional] |
| **parentId** | **UUID**| Optional. Parent id. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **mediaTypes** | [**List&lt;String&gt;**](String.md)| Optional. Filter by MediaType. Allows multiple, comma delimited. | [optional] |

### Return type

[**QueryFiltersLegacy**](QueryFiltersLegacy.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Legacy filters retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

