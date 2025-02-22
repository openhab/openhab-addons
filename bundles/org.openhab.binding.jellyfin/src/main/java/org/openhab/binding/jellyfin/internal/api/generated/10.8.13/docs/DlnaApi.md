# DlnaApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createProfile**](DlnaApi.md#createProfile) | **POST** /Dlna/Profiles | Creates a profile. |
| [**deleteProfile**](DlnaApi.md#deleteProfile) | **DELETE** /Dlna/Profiles/{profileId} | Deletes a profile. |
| [**getDefaultProfile**](DlnaApi.md#getDefaultProfile) | **GET** /Dlna/Profiles/Default | Gets the default profile. |
| [**getProfile**](DlnaApi.md#getProfile) | **GET** /Dlna/Profiles/{profileId} | Gets a single profile. |
| [**getProfileInfos**](DlnaApi.md#getProfileInfos) | **GET** /Dlna/ProfileInfos | Get profile infos. |
| [**updateProfile**](DlnaApi.md#updateProfile) | **POST** /Dlna/Profiles/{profileId} | Updates a profile. |


<a id="createProfile"></a>
# **createProfile**
> createProfile(deviceProfile)

Creates a profile.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaApi apiInstance = new DlnaApi(defaultClient);
    DeviceProfile deviceProfile = new DeviceProfile(); // DeviceProfile | Device profile.
    try {
      apiInstance.createProfile(deviceProfile);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaApi#createProfile");
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
| **deviceProfile** | [**DeviceProfile**](DeviceProfile.md)| Device profile. | [optional] |

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
| **204** | Device profile created. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="deleteProfile"></a>
# **deleteProfile**
> deleteProfile(profileId)

Deletes a profile.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaApi apiInstance = new DlnaApi(defaultClient);
    String profileId = "profileId_example"; // String | Profile id.
    try {
      apiInstance.deleteProfile(profileId);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaApi#deleteProfile");
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
| **profileId** | **String**| Profile id. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Device profile deleted. |  -  |
| **404** | Device profile not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDefaultProfile"></a>
# **getDefaultProfile**
> DeviceProfile getDefaultProfile()

Gets the default profile.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaApi apiInstance = new DlnaApi(defaultClient);
    try {
      DeviceProfile result = apiInstance.getDefaultProfile();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaApi#getDefaultProfile");
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

[**DeviceProfile**](DeviceProfile.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Default device profile returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getProfile"></a>
# **getProfile**
> DeviceProfile getProfile(profileId)

Gets a single profile.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaApi apiInstance = new DlnaApi(defaultClient);
    String profileId = "profileId_example"; // String | Profile Id.
    try {
      DeviceProfile result = apiInstance.getProfile(profileId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaApi#getProfile");
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
| **profileId** | **String**| Profile Id. | |

### Return type

[**DeviceProfile**](DeviceProfile.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Device profile returned. |  -  |
| **404** | Device profile not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getProfileInfos"></a>
# **getProfileInfos**
> List&lt;DeviceProfileInfo&gt; getProfileInfos()

Get profile infos.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaApi apiInstance = new DlnaApi(defaultClient);
    try {
      List<DeviceProfileInfo> result = apiInstance.getProfileInfos();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaApi#getProfileInfos");
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

[**List&lt;DeviceProfileInfo&gt;**](DeviceProfileInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Device profile infos returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateProfile"></a>
# **updateProfile**
> updateProfile(profileId, deviceProfile)

Updates a profile.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DlnaApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DlnaApi apiInstance = new DlnaApi(defaultClient);
    String profileId = "profileId_example"; // String | Profile id.
    DeviceProfile deviceProfile = new DeviceProfile(); // DeviceProfile | Device profile.
    try {
      apiInstance.updateProfile(profileId, deviceProfile);
    } catch (ApiException e) {
      System.err.println("Exception when calling DlnaApi#updateProfile");
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
| **profileId** | **String**| Profile id. | |
| **deviceProfile** | [**DeviceProfile**](DeviceProfile.md)| Device profile. | [optional] |

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
| **204** | Device profile updated. |  -  |
| **404** | Device profile not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

