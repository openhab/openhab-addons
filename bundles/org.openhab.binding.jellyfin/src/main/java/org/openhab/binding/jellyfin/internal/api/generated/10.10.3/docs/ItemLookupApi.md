# ItemLookupApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**applySearchCriteria**](ItemLookupApi.md#applySearchCriteria) | **POST** /Items/RemoteSearch/Apply/{itemId} | Applies search criteria to an item and refreshes metadata. |
| [**getBookRemoteSearchResults**](ItemLookupApi.md#getBookRemoteSearchResults) | **POST** /Items/RemoteSearch/Book | Get book remote search. |
| [**getBoxSetRemoteSearchResults**](ItemLookupApi.md#getBoxSetRemoteSearchResults) | **POST** /Items/RemoteSearch/BoxSet | Get box set remote search. |
| [**getExternalIdInfos**](ItemLookupApi.md#getExternalIdInfos) | **GET** /Items/{itemId}/ExternalIdInfos | Get the item&#39;s external id info. |
| [**getMovieRemoteSearchResults**](ItemLookupApi.md#getMovieRemoteSearchResults) | **POST** /Items/RemoteSearch/Movie | Get movie remote search. |
| [**getMusicAlbumRemoteSearchResults**](ItemLookupApi.md#getMusicAlbumRemoteSearchResults) | **POST** /Items/RemoteSearch/MusicAlbum | Get music album remote search. |
| [**getMusicArtistRemoteSearchResults**](ItemLookupApi.md#getMusicArtistRemoteSearchResults) | **POST** /Items/RemoteSearch/MusicArtist | Get music artist remote search. |
| [**getMusicVideoRemoteSearchResults**](ItemLookupApi.md#getMusicVideoRemoteSearchResults) | **POST** /Items/RemoteSearch/MusicVideo | Get music video remote search. |
| [**getPersonRemoteSearchResults**](ItemLookupApi.md#getPersonRemoteSearchResults) | **POST** /Items/RemoteSearch/Person | Get person remote search. |
| [**getSeriesRemoteSearchResults**](ItemLookupApi.md#getSeriesRemoteSearchResults) | **POST** /Items/RemoteSearch/Series | Get series remote search. |
| [**getTrailerRemoteSearchResults**](ItemLookupApi.md#getTrailerRemoteSearchResults) | **POST** /Items/RemoteSearch/Trailer | Get trailer remote search. |


<a id="applySearchCriteria"></a>
# **applySearchCriteria**
> applySearchCriteria(itemId, remoteSearchResult, replaceAllImages)

Applies search criteria to an item and refreshes metadata.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    RemoteSearchResult remoteSearchResult = new RemoteSearchResult(); // RemoteSearchResult | The remote search result.
    Boolean replaceAllImages = true; // Boolean | Optional. Whether or not to replace all images. Default: True.
    try {
      apiInstance.applySearchCriteria(itemId, remoteSearchResult, replaceAllImages);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#applySearchCriteria");
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
| **remoteSearchResult** | [**RemoteSearchResult**](RemoteSearchResult.md)| The remote search result. | |
| **replaceAllImages** | **Boolean**| Optional. Whether or not to replace all images. Default: True. | [optional] [default to true] |

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
| **204** | Item metadata refreshed. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getBookRemoteSearchResults"></a>
# **getBookRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getBookRemoteSearchResults(bookInfoRemoteSearchQuery)

Get book remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery = new BookInfoRemoteSearchQuery(); // BookInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getBookRemoteSearchResults(bookInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getBookRemoteSearchResults");
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
| **bookInfoRemoteSearchQuery** | [**BookInfoRemoteSearchQuery**](BookInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Book remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getBoxSetRemoteSearchResults"></a>
# **getBoxSetRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getBoxSetRemoteSearchResults(boxSetInfoRemoteSearchQuery)

Get box set remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery = new BoxSetInfoRemoteSearchQuery(); // BoxSetInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getBoxSetRemoteSearchResults(boxSetInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getBoxSetRemoteSearchResults");
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
| **boxSetInfoRemoteSearchQuery** | [**BoxSetInfoRemoteSearchQuery**](BoxSetInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Box set remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getExternalIdInfos"></a>
# **getExternalIdInfos**
> List&lt;ExternalIdInfo&gt; getExternalIdInfos(itemId)

Get the item&#39;s external id info.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | Item id.
    try {
      List<ExternalIdInfo> result = apiInstance.getExternalIdInfos(itemId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getExternalIdInfos");
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

### Return type

[**List&lt;ExternalIdInfo&gt;**](ExternalIdInfo.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | External id info retrieved. |  -  |
| **404** | Item not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMovieRemoteSearchResults"></a>
# **getMovieRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getMovieRemoteSearchResults(movieInfoRemoteSearchQuery)

Get movie remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery = new MovieInfoRemoteSearchQuery(); // MovieInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getMovieRemoteSearchResults(movieInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getMovieRemoteSearchResults");
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
| **movieInfoRemoteSearchQuery** | [**MovieInfoRemoteSearchQuery**](MovieInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Movie remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMusicAlbumRemoteSearchResults"></a>
# **getMusicAlbumRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getMusicAlbumRemoteSearchResults(albumInfoRemoteSearchQuery)

Get music album remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery = new AlbumInfoRemoteSearchQuery(); // AlbumInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getMusicAlbumRemoteSearchResults(albumInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getMusicAlbumRemoteSearchResults");
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
| **albumInfoRemoteSearchQuery** | [**AlbumInfoRemoteSearchQuery**](AlbumInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Music album remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMusicArtistRemoteSearchResults"></a>
# **getMusicArtistRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getMusicArtistRemoteSearchResults(artistInfoRemoteSearchQuery)

Get music artist remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery = new ArtistInfoRemoteSearchQuery(); // ArtistInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getMusicArtistRemoteSearchResults(artistInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getMusicArtistRemoteSearchResults");
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
| **artistInfoRemoteSearchQuery** | [**ArtistInfoRemoteSearchQuery**](ArtistInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Music artist remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMusicVideoRemoteSearchResults"></a>
# **getMusicVideoRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getMusicVideoRemoteSearchResults(musicVideoInfoRemoteSearchQuery)

Get music video remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery = new MusicVideoInfoRemoteSearchQuery(); // MusicVideoInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getMusicVideoRemoteSearchResults(musicVideoInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getMusicVideoRemoteSearchResults");
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
| **musicVideoInfoRemoteSearchQuery** | [**MusicVideoInfoRemoteSearchQuery**](MusicVideoInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Music video remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPersonRemoteSearchResults"></a>
# **getPersonRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getPersonRemoteSearchResults(personLookupInfoRemoteSearchQuery)

Get person remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery = new PersonLookupInfoRemoteSearchQuery(); // PersonLookupInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getPersonRemoteSearchResults(personLookupInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getPersonRemoteSearchResults");
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
| **personLookupInfoRemoteSearchQuery** | [**PersonLookupInfoRemoteSearchQuery**](PersonLookupInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Person remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSeriesRemoteSearchResults"></a>
# **getSeriesRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getSeriesRemoteSearchResults(seriesInfoRemoteSearchQuery)

Get series remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery = new SeriesInfoRemoteSearchQuery(); // SeriesInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getSeriesRemoteSearchResults(seriesInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getSeriesRemoteSearchResults");
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
| **seriesInfoRemoteSearchQuery** | [**SeriesInfoRemoteSearchQuery**](SeriesInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Series remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getTrailerRemoteSearchResults"></a>
# **getTrailerRemoteSearchResults**
> List&lt;RemoteSearchResult&gt; getTrailerRemoteSearchResults(trailerInfoRemoteSearchQuery)

Get trailer remote search.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemLookupApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ItemLookupApi apiInstance = new ItemLookupApi(defaultClient);
    TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery = new TrailerInfoRemoteSearchQuery(); // TrailerInfoRemoteSearchQuery | Remote search query.
    try {
      List<RemoteSearchResult> result = apiInstance.getTrailerRemoteSearchResults(trailerInfoRemoteSearchQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemLookupApi#getTrailerRemoteSearchResults");
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
| **trailerInfoRemoteSearchQuery** | [**TrailerInfoRemoteSearchQuery**](TrailerInfoRemoteSearchQuery.md)| Remote search query. | |

### Return type

[**List&lt;RemoteSearchResult&gt;**](RemoteSearchResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: application/json, text/json, application/*+json
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Trailer remote search executed. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

