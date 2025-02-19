# QuickConnectApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**authorizeQuickConnect**](QuickConnectApi.md#authorizeQuickConnect) | **POST** /QuickConnect/Authorize | Authorizes a pending quick connect request. |
| [**getQuickConnectEnabled**](QuickConnectApi.md#getQuickConnectEnabled) | **GET** /QuickConnect/Enabled | Gets the current quick connect state. |
| [**getQuickConnectState**](QuickConnectApi.md#getQuickConnectState) | **GET** /QuickConnect/Connect | Attempts to retrieve authentication information. |
| [**initiateQuickConnect**](QuickConnectApi.md#initiateQuickConnect) | **POST** /QuickConnect/Initiate | Initiate a new quick connect request. |


<a id="authorizeQuickConnect"></a>
# **authorizeQuickConnect**
> Boolean authorizeQuickConnect(code, userId)

Authorizes a pending quick connect request.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.QuickConnectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    QuickConnectApi apiInstance = new QuickConnectApi(defaultClient);
    String code = "code_example"; // String | Quick connect code to authorize.
    UUID userId = UUID.randomUUID(); // UUID | The user the authorize. Access to the requested user is required.
    try {
      Boolean result = apiInstance.authorizeQuickConnect(code, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling QuickConnectApi#authorizeQuickConnect");
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
| **code** | **String**| Quick connect code to authorize. | |
| **userId** | **UUID**| The user the authorize. Access to the requested user is required. | [optional] |

### Return type

**Boolean**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Quick connect result authorized successfully. |  -  |
| **403** | Unknown user id. |  -  |
| **401** | Unauthorized |  -  |

<a id="getQuickConnectEnabled"></a>
# **getQuickConnectEnabled**
> Boolean getQuickConnectEnabled()

Gets the current quick connect state.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.QuickConnectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    QuickConnectApi apiInstance = new QuickConnectApi(defaultClient);
    try {
      Boolean result = apiInstance.getQuickConnectEnabled();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling QuickConnectApi#getQuickConnectEnabled");
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

**Boolean**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Quick connect state returned. |  -  |

<a id="getQuickConnectState"></a>
# **getQuickConnectState**
> QuickConnectResult getQuickConnectState(secret)

Attempts to retrieve authentication information.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.QuickConnectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    QuickConnectApi apiInstance = new QuickConnectApi(defaultClient);
    String secret = "secret_example"; // String | Secret previously returned from the Initiate endpoint.
    try {
      QuickConnectResult result = apiInstance.getQuickConnectState(secret);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling QuickConnectApi#getQuickConnectState");
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
| **secret** | **String**| Secret previously returned from the Initiate endpoint. | |

### Return type

[**QuickConnectResult**](QuickConnectResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Quick connect result returned. |  -  |
| **404** | Unknown quick connect secret. |  -  |

<a id="initiateQuickConnect"></a>
# **initiateQuickConnect**
> QuickConnectResult initiateQuickConnect()

Initiate a new quick connect request.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.QuickConnectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    QuickConnectApi apiInstance = new QuickConnectApi(defaultClient);
    try {
      QuickConnectResult result = apiInstance.initiateQuickConnect();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling QuickConnectApi#initiateQuickConnect");
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

[**QuickConnectResult**](QuickConnectResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Quick connect request successfully created. |  -  |
| **401** | Quick connect is not active on this server. |  -  |

