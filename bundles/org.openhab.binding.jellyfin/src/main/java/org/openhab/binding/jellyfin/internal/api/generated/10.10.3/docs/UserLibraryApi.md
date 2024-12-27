# UserLibraryApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteUserItemRating**](UserLibraryApi.md#deleteUserItemRating) | **DELETE** /UserItems/{itemId}/Rating | Deletes a user&#39;s saved personal rating for an item. |
| [**getIntros**](UserLibraryApi.md#getIntros) | **GET** /Items/{itemId}/Intros | Gets intros to play before the main media item plays. |
| [**getItem**](UserLibraryApi.md#getItem) | **GET** /Items/{itemId} | Gets an item from a user&#39;s library. |
| [**getLatestMedia**](UserLibraryApi.md#getLatestMedia) | **GET** /Items/Latest | Gets latest media. |
| [**getLocalTrailers**](UserLibraryApi.md#getLocalTrailers) | **GET** /Items/{itemId}/LocalTrailers | Gets local trailers for an item. |
| [**getRootFolder**](UserLibraryApi.md#getRootFolder) | **GET** /Items/Root | Gets the root folder from a user&#39;s library. |
| [**getSpecialFeatures**](UserLibraryApi.md#getSpecialFeatures) | **GET** /Items/{itemId}/SpecialFeatures | Gets special features for an item. |
| [**markFavoriteItem**](UserLibraryApi.md#markFavoriteItem) | **POST** /UserFavoriteItems/{itemId} | Marks an item as a favorite. |
| [**unmarkFavoriteItem**](UserLibraryApi.md#unmarkFavoriteItem) | **DELETE** /UserFavoriteItems/{itemId} | Unmarks item as a favorite. |
| [**updateUserItemRating**](UserLibraryApi.md#updateUserItemRating) | **POST** /UserItems/{itemId}/Rating | Updates a user&#39;s rating for an item. |


<a id="deleteUserItemRating"></a>
# **deleteUserItemRating**
> UserItemDataDto deleteUserItemRating(itemId, userId)

Deletes a user&#39;s saved personal rating for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      UserItemDataDto result = apiInstance.deleteUserItemRating(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#deleteUserItemRating");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

### Return type

[**UserItemDataDto**](UserItemDataDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Personal rating removed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getIntros"></a>
# **getIntros**
> BaseItemDtoQueryResult getIntros(itemId, userId)

Gets intros to play before the main media item plays.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      BaseItemDtoQueryResult result = apiInstance.getIntros(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#getIntros");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

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
| **200** | Intros returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getItem"></a>
# **getItem**
> BaseItemDto getItem(itemId, userId)

Gets an item from a user&#39;s library.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      BaseItemDto result = apiInstance.getItem(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#getItem");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

### Return type

[**BaseItemDto**](BaseItemDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLatestMedia"></a>
# **getLatestMedia**
> List&lt;BaseItemDto&gt; getLatestMedia(userId, parentId, fields, includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems)

Gets latest media.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    Boolean isPlayed = true; // Boolean | Filter by items that are played, or not.
    Boolean enableImages = true; // Boolean | Optional. include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    Boolean enableUserData = true; // Boolean | Optional. include user data.
    Integer limit = 20; // Integer | Return item limit.
    Boolean groupItems = true; // Boolean | Whether or not to group items into a parent container.
    try {
      List<BaseItemDto> result = apiInstance.getLatestMedia(userId, parentId, fields, includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#getLatestMedia");
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
| **userId** | **UUID**| User id. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **isPlayed** | **Boolean**| Filter by items that are played, or not. | [optional] |
| **enableImages** | **Boolean**| Optional. include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. include user data. | [optional] |
| **limit** | **Integer**| Return item limit. | [optional] [default to 20] |
| **groupItems** | **Boolean**| Whether or not to group items into a parent container. | [optional] [default to true] |

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
| **200** | Latest media returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLocalTrailers"></a>
# **getLocalTrailers**
> List&lt;BaseItemDto&gt; getLocalTrailers(itemId, userId)

Gets local trailers for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      List<BaseItemDto> result = apiInstance.getLocalTrailers(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#getLocalTrailers");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

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
| **200** | An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getRootFolder"></a>
# **getRootFolder**
> BaseItemDto getRootFolder(userId)

Gets the root folder from a user&#39;s library.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      BaseItemDto result = apiInstance.getRootFolder(userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#getRootFolder");
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
| **userId** | **UUID**| User id. | [optional] |

### Return type

[**BaseItemDto**](BaseItemDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Root folder returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSpecialFeatures"></a>
# **getSpecialFeatures**
> List&lt;BaseItemDto&gt; getSpecialFeatures(itemId, userId)

Gets special features for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      List<BaseItemDto> result = apiInstance.getSpecialFeatures(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#getSpecialFeatures");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

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
| **200** | Special features returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="markFavoriteItem"></a>
# **markFavoriteItem**
> UserItemDataDto markFavoriteItem(itemId, userId)

Marks an item as a favorite.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      UserItemDataDto result = apiInstance.markFavoriteItem(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#markFavoriteItem");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

### Return type

[**UserItemDataDto**](UserItemDataDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item marked as favorite. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="unmarkFavoriteItem"></a>
# **unmarkFavoriteItem**
> UserItemDataDto unmarkFavoriteItem(itemId, userId)

Unmarks item as a favorite.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    try {
      UserItemDataDto result = apiInstance.unmarkFavoriteItem(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#unmarkFavoriteItem");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |

### Return type

[**UserItemDataDto**](UserItemDataDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item unmarked as favorite. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="updateUserItemRating"></a>
# **updateUserItemRating**
> UserItemDataDto updateUserItemRating(itemId, userId, likes)

Updates a user&#39;s rating for an item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserLibraryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    UserLibraryApi apiInstance = new UserLibraryApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    Boolean likes = true; // Boolean | Whether this M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean}) is likes.
    try {
      UserItemDataDto result = apiInstance.updateUserItemRating(itemId, userId, likes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling UserLibraryApi#updateUserItemRating");
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
| **itemId** | **UUID**| Item id. | |
| **userId** | **UUID**| User id. | [optional] |
| **likes** | **Boolean**| Whether this M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean}) is likes. | [optional] |

### Return type

[**UserItemDataDto**](UserItemDataDto.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Item rating updated. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

