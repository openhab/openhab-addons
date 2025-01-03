# LibraryApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteItem**](LibraryApi.md#deleteItem) | **DELETE** /Items/{itemId} | Deletes an item from the library and filesystem. |
| [**deleteItems**](LibraryApi.md#deleteItems) | **DELETE** /Items | Deletes items from the library and filesystem. |
| [**getAncestors**](LibraryApi.md#getAncestors) | **GET** /Items/{itemId}/Ancestors | Gets all parents of an item. |
| [**getCriticReviews**](LibraryApi.md#getCriticReviews) | **GET** /Items/{itemId}/CriticReviews | Gets critic review for an item. |
| [**getDownload**](LibraryApi.md#getDownload) | **GET** /Items/{itemId}/Download | Downloads item media. |
| [**getFile**](LibraryApi.md#getFile) | **GET** /Items/{itemId}/File | Get the original file of an item. |
| [**getItemCounts**](LibraryApi.md#getItemCounts) | **GET** /Items/Counts | Get item counts. |
| [**getLibraryOptionsInfo**](LibraryApi.md#getLibraryOptionsInfo) | **GET** /Libraries/AvailableOptions | Gets the library options info. |
| [**getMediaFolders**](LibraryApi.md#getMediaFolders) | **GET** /Library/MediaFolders | Gets all user media folders. |
| [**getPhysicalPaths**](LibraryApi.md#getPhysicalPaths) | **GET** /Library/PhysicalPaths | Gets a list of physical paths from virtual folders. |
| [**getSimilarAlbums**](LibraryApi.md#getSimilarAlbums) | **GET** /Albums/{itemId}/Similar | Gets similar items. |
| [**getSimilarArtists**](LibraryApi.md#getSimilarArtists) | **GET** /Artists/{itemId}/Similar | Gets similar items. |
| [**getSimilarItems**](LibraryApi.md#getSimilarItems) | **GET** /Items/{itemId}/Similar | Gets similar items. |
| [**getSimilarMovies**](LibraryApi.md#getSimilarMovies) | **GET** /Movies/{itemId}/Similar | Gets similar items. |
| [**getSimilarShows**](LibraryApi.md#getSimilarShows) | **GET** /Shows/{itemId}/Similar | Gets similar items. |
| [**getSimilarTrailers**](LibraryApi.md#getSimilarTrailers) | **GET** /Trailers/{itemId}/Similar | Gets similar items. |
| [**getThemeMedia**](LibraryApi.md#getThemeMedia) | **GET** /Items/{itemId}/ThemeMedia | Get theme songs and videos for an item. |
| [**getThemeSongs**](LibraryApi.md#getThemeSongs) | **GET** /Items/{itemId}/ThemeSongs | Get theme songs for an item. |
| [**getThemeVideos**](LibraryApi.md#getThemeVideos) | **GET** /Items/{itemId}/ThemeVideos | Get theme videos for an item. |
| [**postAddedMovies**](LibraryApi.md#postAddedMovies) | **POST** /Library/Movies/Added | Reports that new movies have been added by an external source. |
| [**postAddedSeries**](LibraryApi.md#postAddedSeries) | **POST** /Library/Series/Added | Reports that new episodes of a series have been added by an external source. |
| [**postUpdatedMedia**](LibraryApi.md#postUpdatedMedia) | **POST** /Library/Media/Updated | Reports that new movies have been added by an external source. |
| [**postUpdatedMovies**](LibraryApi.md#postUpdatedMovies) | **POST** /Library/Movies/Updated | Reports that new movies have been added by an external source. |
| [**postUpdatedSeries**](LibraryApi.md#postUpdatedSeries) | **POST** /Library/Series/Updated | Reports that new episodes of a series have been added by an external source. |
| [**refreshLibrary**](LibraryApi.md#refreshLibrary) | **POST** /Library/Refresh | Starts a library scan. |


<a id="deleteItem"></a>
# **deleteItem**
> deleteItem(itemId)

Deletes an item from the library and filesystem.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    try {
      apiInstance.deleteItem(itemId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#deleteItem");
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
| **itemId** | **UUID**| The item id. | |

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
| **204** | Item deleted. |  -  |
| **401** | Unauthorized access. |  -  |
| **403** | Forbidden |  -  |

<a id="deleteItems"></a>
# **deleteItems**
> deleteItems(ids)

Deletes items from the library and filesystem.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    List<UUID> ids = Arrays.asList(); // List<UUID> | The item ids.
    try {
      apiInstance.deleteItems(ids);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#deleteItems");
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
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| The item ids. | [optional] |

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
| **204** | Items deleted. |  -  |
| **401** | Unauthorized access. |  -  |
| **403** | Forbidden |  -  |

<a id="getAncestors"></a>
# **getAncestors**
> List&lt;BaseItemDto&gt; getAncestors(itemId, userId)

Gets all parents of an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    try {
      List<BaseItemDto> result = apiInstance.getAncestors(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getAncestors");
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
| **itemId** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |

### Return type

[**List&lt;BaseItemDto&gt;**](BaseItemDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item parents returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getCriticReviews"></a>
# **getCriticReviews**
> BaseItemDtoQueryResult getCriticReviews(itemId)

Gets critic review for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    String itemId = "itemId_example"; // String | 
    try {
      BaseItemDtoQueryResult result = apiInstance.getCriticReviews(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getCriticReviews");
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
| **itemId** | **String**|  | |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Critic reviews returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getDownload"></a>
# **getDownload**
> File getDownload(itemId)

Downloads item media.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    try {
      File result = apiInstance.getDownload(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getDownload");
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
| **itemId** | **UUID**| The item id. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: video/*, audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Media downloaded. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getFile"></a>
# **getFile**
> File getFile(itemId)

Get the original file of an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    try {
      File result = apiInstance.getFile(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getFile");
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
| **itemId** | **UUID**| The item id. | |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: video/*, audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | File stream returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getItemCounts"></a>
# **getItemCounts**
> ItemCounts getItemCounts(userId, isFavorite)

Get item counts.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | Optional. Get counts from a specific user's library.
    Boolean isFavorite = true; // Boolean | Optional. Get counts of favorite items.
    try {
      ItemCounts result = apiInstance.getItemCounts(userId, isFavorite);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getItemCounts");
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
| **userId** | **UUID**| Optional. Get counts from a specific user&#39;s library. | [optional] |
| **isFavorite** | **Boolean**| Optional. Get counts of favorite items. | [optional] |

### Return type

[**ItemCounts**](ItemCounts.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item counts returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLibraryOptionsInfo"></a>
# **getLibraryOptionsInfo**
> LibraryOptionsResultDto getLibraryOptionsInfo(libraryContentType, isNewLibrary)

Gets the library options info.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    String libraryContentType = "libraryContentType_example"; // String | Library content type.
    Boolean isNewLibrary = false; // Boolean | Whether this is a new library.
    try {
      LibraryOptionsResultDto result = apiInstance.getLibraryOptionsInfo(libraryContentType, isNewLibrary);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getLibraryOptionsInfo");
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
| **libraryContentType** | **String**| Library content type. | [optional] |
| **isNewLibrary** | **Boolean**| Whether this is a new library. | [optional] [default to false] |

### Return type

[**LibraryOptionsResultDto**](LibraryOptionsResultDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Library options info returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMediaFolders"></a>
# **getMediaFolders**
> BaseItemDtoQueryResult getMediaFolders(isHidden)

Gets all user media folders.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    Boolean isHidden = true; // Boolean | Optional. Filter by folders that are marked hidden, or not.
    try {
      BaseItemDtoQueryResult result = apiInstance.getMediaFolders(isHidden);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getMediaFolders");
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
| **isHidden** | **Boolean**| Optional. Filter by folders that are marked hidden, or not. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Media folders returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPhysicalPaths"></a>
# **getPhysicalPaths**
> List&lt;String&gt; getPhysicalPaths()

Gets a list of physical paths from virtual folders.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    try {
      List<String> result = apiInstance.getPhysicalPaths();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getPhysicalPaths");
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

**List&lt;String&gt;**

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Physical paths returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSimilarAlbums"></a>
# **getSimilarAlbums**
> BaseItemDtoQueryResult getSimilarAlbums(itemId, excludeArtistIds, userId, limit, fields)

Gets similar items.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Exclude artist ids.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSimilarAlbums(itemId, excludeArtistIds, userId, limit, fields);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getSimilarAlbums");
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
| **itemId** | **UUID**| The item id. | |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Exclude artist ids. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Similar items returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSimilarArtists"></a>
# **getSimilarArtists**
> BaseItemDtoQueryResult getSimilarArtists(itemId, excludeArtistIds, userId, limit, fields)

Gets similar items.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Exclude artist ids.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSimilarArtists(itemId, excludeArtistIds, userId, limit, fields);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getSimilarArtists");
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
| **itemId** | **UUID**| The item id. | |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Exclude artist ids. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Similar items returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSimilarItems"></a>
# **getSimilarItems**
> BaseItemDtoQueryResult getSimilarItems(itemId, excludeArtistIds, userId, limit, fields)

Gets similar items.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Exclude artist ids.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSimilarItems(itemId, excludeArtistIds, userId, limit, fields);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getSimilarItems");
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
| **itemId** | **UUID**| The item id. | |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Exclude artist ids. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Similar items returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSimilarMovies"></a>
# **getSimilarMovies**
> BaseItemDtoQueryResult getSimilarMovies(itemId, excludeArtistIds, userId, limit, fields)

Gets similar items.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Exclude artist ids.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSimilarMovies(itemId, excludeArtistIds, userId, limit, fields);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getSimilarMovies");
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
| **itemId** | **UUID**| The item id. | |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Exclude artist ids. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Similar items returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSimilarShows"></a>
# **getSimilarShows**
> BaseItemDtoQueryResult getSimilarShows(itemId, excludeArtistIds, userId, limit, fields)

Gets similar items.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Exclude artist ids.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSimilarShows(itemId, excludeArtistIds, userId, limit, fields);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getSimilarShows");
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
| **itemId** | **UUID**| The item id. | |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Exclude artist ids. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Similar items returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSimilarTrailers"></a>
# **getSimilarTrailers**
> BaseItemDtoQueryResult getSimilarTrailers(itemId, excludeArtistIds, userId, limit, fields)

Gets similar items.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Exclude artist ids.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSimilarTrailers(itemId, excludeArtistIds, userId, limit, fields);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getSimilarTrailers");
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
| **itemId** | **UUID**| The item id. | |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Exclude artist ids. | [optional] |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Similar items returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getThemeMedia"></a>
# **getThemeMedia**
> AllThemeMediaResult getThemeMedia(itemId, userId, inheritFromParent)

Get theme songs and videos for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Boolean inheritFromParent = false; // Boolean | Optional. Determines whether or not parent items should be searched for theme media.
    try {
      AllThemeMediaResult result = apiInstance.getThemeMedia(itemId, userId, inheritFromParent);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getThemeMedia");
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
| **itemId** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **inheritFromParent** | **Boolean**| Optional. Determines whether or not parent items should be searched for theme media. | [optional] [default to false] |

### Return type

[**AllThemeMediaResult**](AllThemeMediaResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Theme songs and videos returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getThemeSongs"></a>
# **getThemeSongs**
> ThemeMediaResult getThemeSongs(itemId, userId, inheritFromParent)

Get theme songs for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Boolean inheritFromParent = false; // Boolean | Optional. Determines whether or not parent items should be searched for theme media.
    try {
      ThemeMediaResult result = apiInstance.getThemeSongs(itemId, userId, inheritFromParent);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getThemeSongs");
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
| **itemId** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **inheritFromParent** | **Boolean**| Optional. Determines whether or not parent items should be searched for theme media. | [optional] [default to false] |

### Return type

[**ThemeMediaResult**](ThemeMediaResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Theme songs returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getThemeVideos"></a>
# **getThemeVideos**
> ThemeMediaResult getThemeVideos(itemId, userId, inheritFromParent)

Get theme videos for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Boolean inheritFromParent = false; // Boolean | Optional. Determines whether or not parent items should be searched for theme media.
    try {
      ThemeMediaResult result = apiInstance.getThemeVideos(itemId, userId, inheritFromParent);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#getThemeVideos");
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
| **itemId** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **inheritFromParent** | **Boolean**| Optional. Determines whether or not parent items should be searched for theme media. | [optional] [default to false] |

### Return type

[**ThemeMediaResult**](ThemeMediaResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Theme videos returned. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postAddedMovies"></a>
# **postAddedMovies**
> postAddedMovies(tmdbId, imdbId)

Reports that new movies have been added by an external source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    String tmdbId = "tmdbId_example"; // String | The tmdbId.
    String imdbId = "imdbId_example"; // String | The imdbId.
    try {
      apiInstance.postAddedMovies(tmdbId, imdbId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#postAddedMovies");
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
| **tmdbId** | **String**| The tmdbId. | [optional] |
| **imdbId** | **String**| The imdbId. | [optional] |

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
| **204** | Report success. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postAddedSeries"></a>
# **postAddedSeries**
> postAddedSeries(tvdbId)

Reports that new episodes of a series have been added by an external source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    String tvdbId = "tvdbId_example"; // String | The tvdbId.
    try {
      apiInstance.postAddedSeries(tvdbId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#postAddedSeries");
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
| **tvdbId** | **String**| The tvdbId. | [optional] |

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
| **204** | Report success. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postUpdatedMedia"></a>
# **postUpdatedMedia**
> postUpdatedMedia(mediaUpdateInfoDto)

Reports that new movies have been added by an external source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    MediaUpdateInfoDto mediaUpdateInfoDto = new MediaUpdateInfoDto(); // MediaUpdateInfoDto | The update paths.
    try {
      apiInstance.postUpdatedMedia(mediaUpdateInfoDto);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#postUpdatedMedia");
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
| **mediaUpdateInfoDto** | [**MediaUpdateInfoDto**](MediaUpdateInfoDto.md)| The update paths. | |

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
| **204** | Report success. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postUpdatedMovies"></a>
# **postUpdatedMovies**
> postUpdatedMovies(tmdbId, imdbId)

Reports that new movies have been added by an external source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    String tmdbId = "tmdbId_example"; // String | The tmdbId.
    String imdbId = "imdbId_example"; // String | The imdbId.
    try {
      apiInstance.postUpdatedMovies(tmdbId, imdbId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#postUpdatedMovies");
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
| **tmdbId** | **String**| The tmdbId. | [optional] |
| **imdbId** | **String**| The imdbId. | [optional] |

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
| **204** | Report success. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="postUpdatedSeries"></a>
# **postUpdatedSeries**
> postUpdatedSeries(tvdbId)

Reports that new episodes of a series have been added by an external source.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    String tvdbId = "tvdbId_example"; // String | The tvdbId.
    try {
      apiInstance.postUpdatedSeries(tvdbId);
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#postUpdatedSeries");
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
| **tvdbId** | **String**| The tvdbId. | [optional] |

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
| **204** | Report success. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="refreshLibrary"></a>
# **refreshLibrary**
> refreshLibrary()

Starts a library scan.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    LibraryApi apiInstance = new LibraryApi(defaultClient);
    try {
      apiInstance.refreshLibrary();
    } catch (ApiException e) {
      System.err.println("Exception when calling LibraryApi#refreshLibrary");
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

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Library scan started. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

