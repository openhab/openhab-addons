# ItemRefreshApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**refreshItem**](ItemRefreshApi.md#refreshItem) | **POST** /Items/{itemId}/Refresh | Refreshes metadata for an item. |


<a id="refreshItem"></a>
# **refreshItem**
> refreshItem(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata, replaceAllImages)

Refreshes metadata for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemRefreshApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemRefreshApi apiInstance = new ItemRefreshApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    MetadataRefreshMode metadataRefreshMode = MetadataRefreshMode.fromValue("None"); // MetadataRefreshMode | (Optional) Specifies the metadata refresh mode.
    MetadataRefreshMode imageRefreshMode = MetadataRefreshMode.fromValue("None"); // MetadataRefreshMode | (Optional) Specifies the image refresh mode.
    Boolean replaceAllMetadata = false; // Boolean | (Optional) Determines if metadata should be replaced. Only applicable if mode is FullRefresh.
    Boolean replaceAllImages = false; // Boolean | (Optional) Determines if images should be replaced. Only applicable if mode is FullRefresh.
    try {
      apiInstance.refreshItem(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata, replaceAllImages);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemRefreshApi#refreshItem");
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
| **metadataRefreshMode** | [**MetadataRefreshMode**](.md)| (Optional) Specifies the metadata refresh mode. | [optional] [default to None] [enum: None, ValidationOnly, Default, FullRefresh] |
| **imageRefreshMode** | [**MetadataRefreshMode**](.md)| (Optional) Specifies the image refresh mode. | [optional] [default to None] [enum: None, ValidationOnly, Default, FullRefresh] |
| **replaceAllMetadata** | **Boolean**| (Optional) Determines if metadata should be replaced. Only applicable if mode is FullRefresh. | [optional] [default to false] |
| **replaceAllImages** | **Boolean**| (Optional) Determines if images should be replaced. Only applicable if mode is FullRefresh. | [optional] [default to false] |

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
| **204** | Item metadata refresh queued. |  -  |
| **404** | Item to refresh not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

