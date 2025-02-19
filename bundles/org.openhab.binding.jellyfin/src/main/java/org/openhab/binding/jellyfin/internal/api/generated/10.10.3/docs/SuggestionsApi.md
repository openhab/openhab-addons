# SuggestionsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getSuggestions**](SuggestionsApi.md#getSuggestions) | **GET** /Items/Suggestions | Gets suggestions. |


<a id="getSuggestions"></a>
# **getSuggestions**
> BaseItemDtoQueryResult getSuggestions(userId, mediaType, type, startIndex, limit, enableTotalRecordCount)

Gets suggestions.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SuggestionsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SuggestionsApi apiInstance = new SuggestionsApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    List<MediaType> mediaType = Arrays.asList(); // List<MediaType> | The media types.
    List<BaseItemKind> type = Arrays.asList(); // List<BaseItemKind> | The type.
    Integer startIndex = 56; // Integer | Optional. The start index.
    Integer limit = 56; // Integer | Optional. The limit.
    Boolean enableTotalRecordCount = false; // Boolean | Whether to enable the total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSuggestions(userId, mediaType, type, startIndex, limit, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SuggestionsApi#getSuggestions");
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
| **userId** | **UUID**| The user id. | [optional] |
| **mediaType** | [**List&lt;MediaType&gt;**](MediaType.md)| The media types. | [optional] |
| **type** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| The type. | [optional] |
| **startIndex** | **Integer**| Optional. The start index. | [optional] |
| **limit** | **Integer**| Optional. The limit. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Whether to enable the total record count. | [optional] [default to false] |

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
| **200** | Suggestions returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

