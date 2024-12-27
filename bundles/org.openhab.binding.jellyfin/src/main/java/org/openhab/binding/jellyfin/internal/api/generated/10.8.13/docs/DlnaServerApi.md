# DlnaServerApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getConnectionManager**](DlnaServerApi.md#getConnectionManager) | **GET** /Dlna/{serverId}/ConnectionManager | Gets Dlna media receiver registrar xml. |
| [**getConnectionManager2**](DlnaServerApi.md#getConnectionManager2) | **GET** /Dlna/{serverId}/ConnectionManager/ConnectionManager | Gets Dlna media receiver registrar xml. |
| [**getConnectionManager3**](DlnaServerApi.md#getConnectionManager3) | **GET** /Dlna/{serverId}/ConnectionManager/ConnectionManager.xml | Gets Dlna media receiver registrar xml. |
| [**getContentDirectory**](DlnaServerApi.md#getContentDirectory) | **GET** /Dlna/{serverId}/ContentDirectory | Gets Dlna content directory xml. |
| [**getContentDirectory2**](DlnaServerApi.md#getContentDirectory2) | **GET** /Dlna/{serverId}/ContentDirectory/ContentDirectory | Gets Dlna content directory xml. |
| [**getContentDirectory3**](DlnaServerApi.md#getContentDirectory3) | **GET** /Dlna/{serverId}/ContentDirectory/ContentDirectory.xml | Gets Dlna content directory xml. |
| [**getDescriptionXml**](DlnaServerApi.md#getDescriptionXml) | **GET** /Dlna/{serverId}/description | Get Description Xml. |
| [**getDescriptionXml2**](DlnaServerApi.md#getDescriptionXml2) | **GET** /Dlna/{serverId}/description.xml | Get Description Xml. |
| [**getIcon**](DlnaServerApi.md#getIcon) | **GET** /Dlna/icons/{fileName} | Gets a server icon. |
| [**getIconId**](DlnaServerApi.md#getIconId) | **GET** /Dlna/{serverId}/icons/{fileName} | Gets a server icon. |
| [**getMediaReceiverRegistrar**](DlnaServerApi.md#getMediaReceiverRegistrar) | **GET** /Dlna/{serverId}/MediaReceiverRegistrar | Gets Dlna media receiver registrar xml. |
| [**getMediaReceiverRegistrar2**](DlnaServerApi.md#getMediaReceiverRegistrar2) | **GET** /Dlna/{serverId}/MediaReceiverRegistrar/MediaReceiverRegistrar | Gets Dlna media receiver registrar xml. |
| [**getMediaReceiverRegistrar3**](DlnaServerApi.md#getMediaReceiverRegistrar3) | **GET** /Dlna/{serverId}/MediaReceiverRegistrar/MediaReceiverRegistrar.xml | Gets Dlna media receiver registrar xml. |
| [**processConnectionManagerControlRequest**](DlnaServerApi.md#processConnectionManagerControlRequest) | **POST** /Dlna/{serverId}/ConnectionManager/Control | Process a connection manager control request. |
| [**processContentDirectoryControlRequest**](DlnaServerApi.md#processContentDirectoryControlRequest) | **POST** /Dlna/{serverId}/ContentDirectory/Control | Process a content directory control request. |
| [**processMediaReceiverRegistrarControlRequest**](DlnaServerApi.md#processMediaReceiverRegistrarControlRequest) | **POST** /Dlna/{serverId}/MediaReceiverRegistrar/Control | Process a media receiver registrar control request. |


<a id="getConnectionManager"></a>
# **getConnectionManager**
> File getConnectionManager(serverId)

Gets Dlna media receiver registrar xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getConnectionManager(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getConnectionManager");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna media receiver registrar xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getConnectionManager2"></a>
# **getConnectionManager2**
> File getConnectionManager2(serverId)

Gets Dlna media receiver registrar xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getConnectionManager2(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getConnectionManager2");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna media receiver registrar xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getConnectionManager3"></a>
# **getConnectionManager3**
> File getConnectionManager3(serverId)

Gets Dlna media receiver registrar xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getConnectionManager3(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getConnectionManager3");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna media receiver registrar xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getContentDirectory"></a>
# **getContentDirectory**
> File getContentDirectory(serverId)

Gets Dlna content directory xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getContentDirectory(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getContentDirectory");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna content directory returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getContentDirectory2"></a>
# **getContentDirectory2**
> File getContentDirectory2(serverId)

Gets Dlna content directory xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getContentDirectory2(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getContentDirectory2");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna content directory returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getContentDirectory3"></a>
# **getContentDirectory3**
> File getContentDirectory3(serverId)

Gets Dlna content directory xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getContentDirectory3(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getContentDirectory3");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna content directory returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDescriptionXml"></a>
# **getDescriptionXml**
> File getDescriptionXml(serverId)

Get Description Xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getDescriptionXml(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getDescriptionXml");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Description xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDescriptionXml2"></a>
# **getDescriptionXml2**
> File getDescriptionXml2(serverId)

Get Description Xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getDescriptionXml2(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getDescriptionXml2");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Description xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getIcon"></a>
# **getIcon**
> File getIcon(fileName)

Gets a server icon.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String fileName = "fileName_example"; // String | The icon filename.
    try {
      File result = apiInstance.getIcon(fileName);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getIcon");
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
| **fileName** | **String**| The icon filename. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Request processed. |  -  |
| **404** | Not Found. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getIconId"></a>
# **getIconId**
> File getIconId(serverId, fileName)

Gets a server icon.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    String fileName = "fileName_example"; // String | The icon filename.
    try {
      File result = apiInstance.getIconId(serverId, fileName);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getIconId");
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
| **serverId** | **String**| Server UUID. | |
| **fileName** | **String**| The icon filename. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Request processed. |  -  |
| **404** | Not Found. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMediaReceiverRegistrar"></a>
# **getMediaReceiverRegistrar**
> File getMediaReceiverRegistrar(serverId)

Gets Dlna media receiver registrar xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getMediaReceiverRegistrar(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getMediaReceiverRegistrar");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna media receiver registrar xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMediaReceiverRegistrar2"></a>
# **getMediaReceiverRegistrar2**
> File getMediaReceiverRegistrar2(serverId)

Gets Dlna media receiver registrar xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getMediaReceiverRegistrar2(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getMediaReceiverRegistrar2");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna media receiver registrar xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMediaReceiverRegistrar3"></a>
# **getMediaReceiverRegistrar3**
> File getMediaReceiverRegistrar3(serverId)

Gets Dlna media receiver registrar xml.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.getMediaReceiverRegistrar3(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#getMediaReceiverRegistrar3");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dlna media receiver registrar xml returned. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="processConnectionManagerControlRequest"></a>
# **processConnectionManagerControlRequest**
> File processConnectionManagerControlRequest(serverId)

Process a connection manager control request.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.processConnectionManagerControlRequest(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#processConnectionManagerControlRequest");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Request processed. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="processContentDirectoryControlRequest"></a>
# **processContentDirectoryControlRequest**
> File processContentDirectoryControlRequest(serverId)

Process a content directory control request.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.processContentDirectoryControlRequest(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#processContentDirectoryControlRequest");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Request processed. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="processMediaReceiverRegistrarControlRequest"></a>
# **processMediaReceiverRegistrarControlRequest**
> File processMediaReceiverRegistrarControlRequest(serverId)

Process a media receiver registrar control request.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaServerApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaServerApi apiInstance = new DlnaServerApi(defaultClient);
    String serverId = "serverId_example"; // String | Server UUID.
    try {
      File result = apiInstance.processMediaReceiverRegistrarControlRequest(serverId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaServerApi#processMediaReceiverRegistrarControlRequest");
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
| **serverId** | **String**| Server UUID. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Request processed. |  -  |
| **503** | DLNA is disabled. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

