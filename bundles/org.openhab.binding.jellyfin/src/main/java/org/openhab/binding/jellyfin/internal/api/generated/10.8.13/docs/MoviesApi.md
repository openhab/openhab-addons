# MoviesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getMovieRecommendations**](MoviesApi.md#getMovieRecommendations) | **GET** /Movies/Recommendations | Gets movie recommendations. |


<a id="getMovieRecommendations"></a>
# **getMovieRecommendations**
> List&lt;RecommendationDto&gt; getMovieRecommendations(userId, parentId, fields, categoryLimit, itemLimit)

Gets movie recommendations.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MoviesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MoviesApi apiInstance = new MoviesApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. The fields to return.
    Integer categoryLimit = 5; // Integer | The max number of categories to return.
    Integer itemLimit = 8; // Integer | The max number of items to return per category.
    try {
      List<RecommendationDto> result = apiInstance.getMovieRecommendations(userId, parentId, fields, categoryLimit, itemLimit);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MoviesApi#getMovieRecommendations");
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
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. The fields to return. | [optional] |
| **categoryLimit** | **Integer**| The max number of categories to return. | [optional] [default to 5] |
| **itemLimit** | **Integer**| The max number of items to return per category. | [optional] [default to 8] |

### Return type

[**List&lt;RecommendationDto&gt;**](RecommendationDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Movie recommendations returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

