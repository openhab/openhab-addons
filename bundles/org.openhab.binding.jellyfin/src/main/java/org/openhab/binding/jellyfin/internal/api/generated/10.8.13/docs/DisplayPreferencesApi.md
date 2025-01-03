# DisplayPreferencesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getDisplayPreferences**](DisplayPreferencesApi.md#getDisplayPreferences) | **GET** /DisplayPreferences/{displayPreferencesId} | Get Display Preferences. |
| [**updateDisplayPreferences**](DisplayPreferencesApi.md#updateDisplayPreferences) | **POST** /DisplayPreferences/{displayPreferencesId} | Update Display Preferences. |


<a id="getDisplayPreferences"></a>
# **getDisplayPreferences**
> DisplayPreferencesDto getDisplayPreferences(displayPreferencesId, userId, client)

Get Display Preferences.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DisplayPreferencesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DisplayPreferencesApi apiInstance = new DisplayPreferencesApi(defaultClient);
    String displayPreferencesId = "displayPreferencesId_example"; // String | Display preferences id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    String client = "client_example"; // String | Client.
    try {
      DisplayPreferencesDto result = apiInstance.getDisplayPreferences(displayPreferencesId, userId, client);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DisplayPreferencesApi#getDisplayPreferences");
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
| **displayPreferencesId** | **String**| Display preferences id. | |
| **userId** | **UUID**| User id. | |
| **client** | **String**| Client. | |

### Return type

[**DisplayPreferencesDto**](DisplayPreferencesDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Display preferences retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateDisplayPreferences"></a>
# **updateDisplayPreferences**
> updateDisplayPreferences(displayPreferencesId, userId, client, displayPreferencesDto)

Update Display Preferences.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DisplayPreferencesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DisplayPreferencesApi apiInstance = new DisplayPreferencesApi(defaultClient);
    String displayPreferencesId = "displayPreferencesId_example"; // String | Display preferences id.
    UUID userId = UUID.randomUUID(); // UUID | User Id.
    String client = "client_example"; // String | Client.
    DisplayPreferencesDto displayPreferencesDto = new DisplayPreferencesDto(); // DisplayPreferencesDto | New Display Preferences object.
    try {
      apiInstance.updateDisplayPreferences(displayPreferencesId, userId, client, displayPreferencesDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling DisplayPreferencesApi#updateDisplayPreferences");
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
| **displayPreferencesId** | **String**| Display preferences id. | |
| **userId** | **UUID**| User Id. | |
| **client** | **String**| Client. | |
| **displayPreferencesDto** | [**DisplayPreferencesDto**](DisplayPreferencesDto.md)| New Display Preferences object. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Display preferences updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

