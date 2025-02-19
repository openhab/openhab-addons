# MediaSegmentsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getItemSegments**](MediaSegmentsApi.md#getItemSegments) | **GET** /MediaSegments/{itemId} | Gets all media segments based on an itemId. |


<a id="getItemSegments"></a>
# **getItemSegments**
> MediaSegmentDtoQueryResult getItemSegments(itemId, includeSegmentTypes)

Gets all media segments based on an itemId.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.MediaSegmentsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    MediaSegmentsApi apiInstance = new MediaSegmentsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The ItemId.
    List<MediaSegmentType> includeSegmentTypes = Arrays.asList(); // List<MediaSegmentType> | Optional filter of requested segment types.
    try {
      MediaSegmentDtoQueryResult result = apiInstance.getItemSegments(itemId, includeSegmentTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling MediaSegmentsApi#getItemSegments");
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
| **itemId** | **UUID**| The ItemId. | |
| **includeSegmentTypes** | [**List&lt;MediaSegmentType&gt;**](MediaSegmentType.md)| Optional filter of requested segment types. | [optional] |

### Return type

[**MediaSegmentDtoQueryResult**](MediaSegmentDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

