# TimeSyncApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getUtcTime**](TimeSyncApi.md#getUtcTime) | **GET** /GetUtcTime | Gets the current UTC time. |


<a id="getUtcTime"></a>
# **getUtcTime**
> UtcTimeResponse getUtcTime()

Gets the current UTC time.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TimeSyncApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    TimeSyncApi apiInstance = new TimeSyncApi(defaultClient);
    try {
      UtcTimeResponse result = apiInstance.getUtcTime();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TimeSyncApi#getUtcTime");
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

[**UtcTimeResponse**](UtcTimeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Time returned. |  -  |

