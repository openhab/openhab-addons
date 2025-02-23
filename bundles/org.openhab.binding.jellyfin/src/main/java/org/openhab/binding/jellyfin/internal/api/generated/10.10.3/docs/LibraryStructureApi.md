# LibraryStructureApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addMediaPath**](LibraryStructureApi.md#addMediaPath) | **POST** /Library/VirtualFolders/Paths | Add a media path to a library. |
| [**addVirtualFolder**](LibraryStructureApi.md#addVirtualFolder) | **POST** /Library/VirtualFolders | Adds a virtual folder. |
| [**getVirtualFolders**](LibraryStructureApi.md#getVirtualFolders) | **GET** /Library/VirtualFolders | Gets all virtual folders. |
| [**removeMediaPath**](LibraryStructureApi.md#removeMediaPath) | **DELETE** /Library/VirtualFolders/Paths | Remove a media path. |
| [**removeVirtualFolder**](LibraryStructureApi.md#removeVirtualFolder) | **DELETE** /Library/VirtualFolders | Removes a virtual folder. |
| [**renameVirtualFolder**](LibraryStructureApi.md#renameVirtualFolder) | **POST** /Library/VirtualFolders/Name | Renames a virtual folder. |
| [**updateLibraryOptions**](LibraryStructureApi.md#updateLibraryOptions) | **POST** /Library/VirtualFolders/LibraryOptions | Update library options. |
| [**updateMediaPath**](LibraryStructureApi.md#updateMediaPath) | **POST** /Library/VirtualFolders/Paths/Update | Updates a media path. |


<a id="addMediaPath"></a>
# **addMediaPath**
> addMediaPath(mediaPathDto, refreshLibrary)

Add a media path to a library.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    MediaPathDto mediaPathDto = new MediaPathDto(); // MediaPathDto | The media path dto.
    Boolean refreshLibrary = false; // Boolean | Whether to refresh the library.
    try {
      apiInstance.addMediaPath(mediaPathDto, refreshLibrary);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#addMediaPath");
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
| **mediaPathDto** | [**MediaPathDto**](MediaPathDto.md)| The media path dto. | |
| **refreshLibrary** | **Boolean**| Whether to refresh the library. | [optional] [default to false] |

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
| **204** | Media path added. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="addVirtualFolder"></a>
# **addVirtualFolder**
> addVirtualFolder(name, collectionType, paths, refreshLibrary, addVirtualFolderDto)

Adds a virtual folder.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    String name = "name_example"; // String | The name of the virtual folder.
    CollectionTypeOptions collectionType = CollectionTypeOptions.fromValue("movies"); // CollectionTypeOptions | The type of the collection.
    List<String> paths = Arrays.asList(); // List<String> | The paths of the virtual folder.
    Boolean refreshLibrary = false; // Boolean | Whether to refresh the library.
    AddVirtualFolderDto addVirtualFolderDto = new AddVirtualFolderDto(); // AddVirtualFolderDto | The library options.
    try {
      apiInstance.addVirtualFolder(name, collectionType, paths, refreshLibrary, addVirtualFolderDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#addVirtualFolder");
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
| **name** | **String**| The name of the virtual folder. | [optional] |
| **collectionType** | **CollectionTypeOptions**| The type of the collection. | [optional] [enum: movies, tvshows, music, musicvideos, homevideos, boxsets, books, mixed] |
| **paths** | [**List&lt;String&gt;**](String.md)| The paths of the virtual folder. | [optional] |
| **refreshLibrary** | **Boolean**| Whether to refresh the library. | [optional] [default to false] |
| **addVirtualFolderDto** | [**AddVirtualFolderDto**](AddVirtualFolderDto.md)| The library options. | [optional] |

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
| **204** | Folder added. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getVirtualFolders"></a>
# **getVirtualFolders**
> List&lt;VirtualFolderInfo&gt; getVirtualFolders()

Gets all virtual folders.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    try {
      List<VirtualFolderInfo> result = apiInstance.getVirtualFolders();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#getVirtualFolders");
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

[**List&lt;VirtualFolderInfo&gt;**](VirtualFolderInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Virtual folders retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="removeMediaPath"></a>
# **removeMediaPath**
> removeMediaPath(name, path, refreshLibrary)

Remove a media path.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    String name = "name_example"; // String | The name of the library.
    String path = "path_example"; // String | The path to remove.
    Boolean refreshLibrary = false; // Boolean | Whether to refresh the library.
    try {
      apiInstance.removeMediaPath(name, path, refreshLibrary);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#removeMediaPath");
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
| **name** | **String**| The name of the library. | [optional] |
| **path** | **String**| The path to remove. | [optional] |
| **refreshLibrary** | **Boolean**| Whether to refresh the library. | [optional] [default to false] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Media path removed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="removeVirtualFolder"></a>
# **removeVirtualFolder**
> removeVirtualFolder(name, refreshLibrary)

Removes a virtual folder.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    String name = "name_example"; // String | The name of the folder.
    Boolean refreshLibrary = false; // Boolean | Whether to refresh the library.
    try {
      apiInstance.removeVirtualFolder(name, refreshLibrary);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#removeVirtualFolder");
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
| **name** | **String**| The name of the folder. | [optional] |
| **refreshLibrary** | **Boolean**| Whether to refresh the library. | [optional] [default to false] |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Folder removed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="renameVirtualFolder"></a>
# **renameVirtualFolder**
> renameVirtualFolder(name, newName, refreshLibrary)

Renames a virtual folder.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    String name = "name_example"; // String | The name of the virtual folder.
    String newName = "newName_example"; // String | The new name.
    Boolean refreshLibrary = false; // Boolean | Whether to refresh the library.
    try {
      apiInstance.renameVirtualFolder(name, newName, refreshLibrary);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#renameVirtualFolder");
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
| **name** | **String**| The name of the virtual folder. | [optional] |
| **newName** | **String**| The new name. | [optional] |
| **refreshLibrary** | **Boolean**| Whether to refresh the library. | [optional] [default to false] |

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
| **204** | Folder renamed. |  -  |
| **404** | Library doesn&#39;t exist. |  -  |
| **409** | Library already exists. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateLibraryOptions"></a>
# **updateLibraryOptions**
> updateLibraryOptions(updateLibraryOptionsDto)

Update library options.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    UpdateLibraryOptionsDto updateLibraryOptionsDto = new UpdateLibraryOptionsDto(); // UpdateLibraryOptionsDto | The library name and options.
    try {
      apiInstance.updateLibraryOptions(updateLibraryOptionsDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#updateLibraryOptions");
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
| **updateLibraryOptionsDto** | [**UpdateLibraryOptionsDto**](UpdateLibraryOptionsDto.md)| The library name and options. | [optional] |

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
| **204** | Library updated. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateMediaPath"></a>
# **updateMediaPath**
> updateMediaPath(updateMediaPathRequestDto)

Updates a media path.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryStructureApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryStructureApi apiInstance = new LibraryStructureApi(defaultClient);
    UpdateMediaPathRequestDto updateMediaPathRequestDto = new UpdateMediaPathRequestDto(); // UpdateMediaPathRequestDto | The name of the library and path infos.
    try {
      apiInstance.updateMediaPath(updateMediaPathRequestDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryStructureApi#updateMediaPath");
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
| **updateMediaPathRequestDto** | [**UpdateMediaPathRequestDto**](UpdateMediaPathRequestDto.md)| The name of the library and path infos. | |

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
| **204** | Media path updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

