# ActivityLogApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getLogEntries**](ActivityLogApi.md#getLogEntries) | **GET** /System/ActivityLog/Entries | Gets activity log entries. |


<a id="getLogEntries"></a>
# **getLogEntries**
> ActivityLogEntryQueryResult getLogEntries(startIndex, limit, minDate, hasUserId)

Gets activity log entries.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ActivityLogApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ActivityLogApi apiInstance = new ActivityLogApi(defaultClient);
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    OffsetDateTime minDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The minimum date. Format = ISO.
    Boolean hasUserId = true; // Boolean | Optional. Filter log entries if it has user id, or not.
    try {
      ActivityLogEntryQueryResult result = apiInstance.getLogEntries(startIndex, limit, minDate, hasUserId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ActivityLogApi#getLogEntries");
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
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **minDate** | **OffsetDateTime**| Optional. The minimum date. Format &#x3D; ISO. | [optional] |
| **hasUserId** | **Boolean**| Optional. Filter log entries if it has user id, or not. | [optional] |

### Return type

[**ActivityLogEntryQueryResult**](ActivityLogEntryQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Activity log returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

