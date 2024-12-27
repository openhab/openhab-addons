# DashboardApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getConfigurationPages**](DashboardApi.md#getConfigurationPages) | **GET** /web/ConfigurationPages | Gets the configuration pages. |
| [**getDashboardConfigurationPage**](DashboardApi.md#getDashboardConfigurationPage) | **GET** /web/ConfigurationPage | Gets a dashboard configuration page. |


<a id="getConfigurationPages"></a>
# **getConfigurationPages**
> List&lt;ConfigurationPageInfo&gt; getConfigurationPages(enableInMainMenu)

Gets the configuration pages.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DashboardApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DashboardApi apiInstance = new DashboardApi(defaultClient);
    Boolean enableInMainMenu = true; // Boolean | Whether to enable in the main menu.
    try {
      List<ConfigurationPageInfo> result = apiInstance.getConfigurationPages(enableInMainMenu);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DashboardApi#getConfigurationPages");
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
| **enableInMainMenu** | **Boolean**| Whether to enable in the main menu. | [optional] |

### Return type

[**List&lt;ConfigurationPageInfo&gt;**](ConfigurationPageInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | ConfigurationPages returned. |  -  |
| **404** | Server still loading. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDashboardConfigurationPage"></a>
# **getDashboardConfigurationPage**
> File getDashboardConfigurationPage(name)

Gets a dashboard configuration page.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DashboardApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    DashboardApi apiInstance = new DashboardApi(defaultClient);
    String name = "name_example"; // String | The name of the page.
    try {
      File result = apiInstance.getDashboardConfigurationPage(name);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DashboardApi#getDashboardConfigurationPage");
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
| **name** | **String**| The name of the page. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/html, application/x-javascript, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | ConfigurationPage returned. |  -  |
| **404** | Plugin configuration page not found. |  -  |

