# InstantMixApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getInstantMixFromAlbum**](InstantMixApi.md#getInstantMixFromAlbum) | **GET** /Albums/{id}/InstantMix | Creates an instant playlist based on a given album. |
| [**getInstantMixFromArtists**](InstantMixApi.md#getInstantMixFromArtists) | **GET** /Artists/{id}/InstantMix | Creates an instant playlist based on a given artist. |
| [**getInstantMixFromArtists2**](InstantMixApi.md#getInstantMixFromArtists2) | **GET** /Artists/InstantMix | Creates an instant playlist based on a given artist. |
| [**getInstantMixFromItem**](InstantMixApi.md#getInstantMixFromItem) | **GET** /Items/{id}/InstantMix | Creates an instant playlist based on a given item. |
| [**getInstantMixFromMusicGenreById**](InstantMixApi.md#getInstantMixFromMusicGenreById) | **GET** /MusicGenres/InstantMix | Creates an instant playlist based on a given genre. |
| [**getInstantMixFromMusicGenreByName**](InstantMixApi.md#getInstantMixFromMusicGenreByName) | **GET** /MusicGenres/{name}/InstantMix | Creates an instant playlist based on a given genre. |
| [**getInstantMixFromPlaylist**](InstantMixApi.md#getInstantMixFromPlaylist) | **GET** /Playlists/{id}/InstantMix | Creates an instant playlist based on a given playlist. |
| [**getInstantMixFromSong**](InstantMixApi.md#getInstantMixFromSong) | **GET** /Songs/{id}/InstantMix | Creates an instant playlist based on a given song. |


<a id="getInstantMixFromAlbum"></a>
# **getInstantMixFromAlbum**
> BaseItemDtoQueryResult getInstantMixFromAlbum(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given album.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromAlbum(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromAlbum");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromArtists"></a>
# **getInstantMixFromArtists**
> BaseItemDtoQueryResult getInstantMixFromArtists(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given artist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromArtists(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromArtists");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromArtists2"></a>
# **getInstantMixFromArtists2**
> BaseItemDtoQueryResult getInstantMixFromArtists2(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given artist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromArtists2(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromArtists2");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromItem"></a>
# **getInstantMixFromItem**
> BaseItemDtoQueryResult getInstantMixFromItem(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given item.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromItem(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromItem");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromMusicGenreById"></a>
# **getInstantMixFromMusicGenreById**
> BaseItemDtoQueryResult getInstantMixFromMusicGenreById(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given genre.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromMusicGenreById(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromMusicGenreById");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromMusicGenreByName"></a>
# **getInstantMixFromMusicGenreByName**
> BaseItemDtoQueryResult getInstantMixFromMusicGenreByName(name, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given genre.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    String name = "name_example"; // String | The genre name.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromMusicGenreByName(name, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromMusicGenreByName");
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
| **name** | **String**| The genre name. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromPlaylist"></a>
# **getInstantMixFromPlaylist**
> BaseItemDtoQueryResult getInstantMixFromPlaylist(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given playlist.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromPlaylist(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromPlaylist");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getInstantMixFromSong"></a>
# **getInstantMixFromSong**
> BaseItemDtoQueryResult getInstantMixFromSong(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes)

Creates an instant playlist based on a given song.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.InstantMixApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    InstantMixApi apiInstance = new InstantMixApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getInstantMixFromSong(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling InstantMixApi#getInstantMixFromSong");
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
| **id** | **UUID**| The item id. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |

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
| **200** | Instant playlist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

