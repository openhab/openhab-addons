# OpenSubtitlesApi

All URIs are relative to *http://nuc.ehrendingen:8096*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**validateLoginInfo**](OpenSubtitlesApi.md#validateLoginInfo) | **POST** /Jellyfin.Plugin.OpenSubtitles/ValidateLoginInfo |  |


<a id="validateLoginInfo"></a>
# **validateLoginInfo**
> validateLoginInfo(loginInfoInput)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.OpenSubtitlesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    OpenSubtitlesApi apiInstance = new OpenSubtitlesApi(defaultClient);
    LoginInfoInput loginInfoInput = new LoginInfoInput(); // LoginInfoInput | 
    try {
      apiInstance.validateLoginInfo(loginInfoInput);
    } catch (ApiException e) {
      System.err.println("Exception when calling OpenSubtitlesApi#validateLoginInfo");
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
| **loginInfoInput** | [**LoginInfoInput**](LoginInfoInput.md)|  | [optional] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

