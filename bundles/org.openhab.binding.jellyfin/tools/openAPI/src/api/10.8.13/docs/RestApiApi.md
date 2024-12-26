# RestApiApi

All URIs are relative to *http://nuc.ehrendingen:8096*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createMobileSession**](RestApiApi.md#createMobileSession) | **POST** /Lastfm/Login |  |


<a id="createMobileSession"></a>
# **createMobileSession**
> createMobileSession(lastFMUser)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RestApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");

    RestApiApi apiInstance = new RestApiApi(defaultClient);
    LastFMUser lastFMUser = new LastFMUser(); // LastFMUser | 
    try {
      apiInstance.createMobileSession(lastFMUser);
    } catch (ApiException e) {
      System.err.println("Exception when calling RestApiApi#createMobileSession");
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
| **lastFMUser** | [**LastFMUser**](LastFMUser.md)|  | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |

