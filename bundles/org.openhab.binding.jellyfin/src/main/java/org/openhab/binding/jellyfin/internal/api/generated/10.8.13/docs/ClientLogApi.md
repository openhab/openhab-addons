# ClientLogApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**logFile**](ClientLogApi.md#logFile) | **POST** /ClientLog/Document | Upload a document. |


<a id="logFile"></a>
# **logFile**
> ClientLogDocumentResponseDto logFile(body)

Upload a document.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ClientLogApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ClientLogApi apiInstance = new ClientLogApi(defaultClient);
    File body = new File("/path/to/file"); // File | 
    try {
      ClientLogDocumentResponseDto result = apiInstance.logFile(body);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ClientLogApi#logFile");
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
| **body** | **File**|  | [optional] |

### Return type

[**ClientLogDocumentResponseDto**](ClientLogDocumentResponseDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: text/plain
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Document saved. |  -  |
| **403** | Event logging disabled. |  -  |
| **413** | Upload size too large. |  -  |
| **401** | Unauthorized |  -  |

