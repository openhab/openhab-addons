# EnvironmentApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getDefaultDirectoryBrowser**](EnvironmentApi.md#getDefaultDirectoryBrowser) | **GET** /Environment/DefaultDirectoryBrowser | Get Default directory browser. |
| [**getDirectoryContents**](EnvironmentApi.md#getDirectoryContents) | **GET** /Environment/DirectoryContents | Gets the contents of a given directory in the file system. |
| [**getDrives**](EnvironmentApi.md#getDrives) | **GET** /Environment/Drives | Gets available drives from the server&#39;s file system. |
| [**getNetworkShares**](EnvironmentApi.md#getNetworkShares) | **GET** /Environment/NetworkShares | Gets network paths. |
| [**getParentPath**](EnvironmentApi.md#getParentPath) | **GET** /Environment/ParentPath | Gets the parent path of a given path. |
| [**validatePath**](EnvironmentApi.md#validatePath) | **POST** /Environment/ValidatePath | Validates path. |


<a id="getDefaultDirectoryBrowser"></a>
# **getDefaultDirectoryBrowser**
> DefaultDirectoryBrowserInfoDto getDefaultDirectoryBrowser()

Get Default directory browser.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnvironmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    EnvironmentApi apiInstance = new EnvironmentApi(defaultClient);
    try {
      DefaultDirectoryBrowserInfoDto result = apiInstance.getDefaultDirectoryBrowser();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnvironmentApi#getDefaultDirectoryBrowser");
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

[**DefaultDirectoryBrowserInfoDto**](DefaultDirectoryBrowserInfoDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Default directory browser returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDirectoryContents"></a>
# **getDirectoryContents**
> List&lt;FileSystemEntryInfo&gt; getDirectoryContents(path, includeFiles, includeDirectories)

Gets the contents of a given directory in the file system.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnvironmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    EnvironmentApi apiInstance = new EnvironmentApi(defaultClient);
    String path = "path_example"; // String | The path.
    Boolean includeFiles = false; // Boolean | An optional filter to include or exclude files from the results. true/false.
    Boolean includeDirectories = false; // Boolean | An optional filter to include or exclude folders from the results. true/false.
    try {
      List<FileSystemEntryInfo> result = apiInstance.getDirectoryContents(path, includeFiles, includeDirectories);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnvironmentApi#getDirectoryContents");
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
| **path** | **String**| The path. | |
| **includeFiles** | **Boolean**| An optional filter to include or exclude files from the results. true/false. | [optional] [default to false] |
| **includeDirectories** | **Boolean**| An optional filter to include or exclude folders from the results. true/false. | [optional] [default to false] |

### Return type

[**List&lt;FileSystemEntryInfo&gt;**](FileSystemEntryInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Directory contents returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDrives"></a>
# **getDrives**
> List&lt;FileSystemEntryInfo&gt; getDrives()

Gets available drives from the server&#39;s file system.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnvironmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    EnvironmentApi apiInstance = new EnvironmentApi(defaultClient);
    try {
      List<FileSystemEntryInfo> result = apiInstance.getDrives();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnvironmentApi#getDrives");
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

[**List&lt;FileSystemEntryInfo&gt;**](FileSystemEntryInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of entries returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getNetworkShares"></a>
# **getNetworkShares**
> List&lt;FileSystemEntryInfo&gt; getNetworkShares()

Gets network paths.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnvironmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    EnvironmentApi apiInstance = new EnvironmentApi(defaultClient);
    try {
      List<FileSystemEntryInfo> result = apiInstance.getNetworkShares();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnvironmentApi#getNetworkShares");
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

[**List&lt;FileSystemEntryInfo&gt;**](FileSystemEntryInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Empty array returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getParentPath"></a>
# **getParentPath**
> String getParentPath(path)

Gets the parent path of a given path.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnvironmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    EnvironmentApi apiInstance = new EnvironmentApi(defaultClient);
    String path = "path_example"; // String | The path.
    try {
      String result = apiInstance.getParentPath(path);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnvironmentApi#getParentPath");
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
| **path** | **String**| The path. | |

### Return type

**String**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="validatePath"></a>
# **validatePath**
> validatePath(validatePathDto)

Validates path.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.EnvironmentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    EnvironmentApi apiInstance = new EnvironmentApi(defaultClient);
    ValidatePathDto validatePathDto = new ValidatePathDto(); // ValidatePathDto | Validate request object.
    try {
      apiInstance.validatePath(validatePathDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling EnvironmentApi#validatePath");
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
| **validatePathDto** | [**ValidatePathDto**](ValidatePathDto.md)| Validate request object. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Path validated. |  -  |
| **404** | Path not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

