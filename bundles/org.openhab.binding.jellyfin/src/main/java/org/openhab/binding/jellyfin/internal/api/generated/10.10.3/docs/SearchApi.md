# SearchApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getSearchHints**](SearchApi.md#getSearchHints) | **GET** /Search/Hints | Gets the search hint result. |


<a id="getSearchHints"></a>
# **getSearchHints**
> SearchHintResult getSearchHints(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia, includeGenres, includeStudios, includeArtists)

Gets the search hint result.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.SearchApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    SearchApi apiInstance = new SearchApi(defaultClient);
    String searchTerm = "searchTerm_example"; // String | The search term to filter on.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Supply a user id to search within a user's library or omit to search all.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | If specified, only results with the specified item types are returned. This allows multiple, comma delimited.
    List<BaseItemKind> excludeItemTypes = Arrays.asList(); // List<BaseItemKind> | If specified, results with these item types are filtered out. This allows multiple, comma delimited.
    List<MediaType> mediaTypes = Arrays.asList(); // List<MediaType> | If specified, only results with the specified media types are returned. This allows multiple, comma delimited.
    UUID parentId = UUID.randomUUID(); // UUID | If specified, only children of the parent are returned.
    Boolean isMovie = true; // Boolean | Optional filter for movies.
    Boolean isSeries = true; // Boolean | Optional filter for series.
    Boolean isNews = true; // Boolean | Optional filter for news.
    Boolean isKids = true; // Boolean | Optional filter for kids.
    Boolean isSports = true; // Boolean | Optional filter for sports.
    Boolean includePeople = true; // Boolean | Optional filter whether to include people.
    Boolean includeMedia = true; // Boolean | Optional filter whether to include media.
    Boolean includeGenres = true; // Boolean | Optional filter whether to include genres.
    Boolean includeStudios = true; // Boolean | Optional filter whether to include studios.
    Boolean includeArtists = true; // Boolean | Optional filter whether to include artists.
    try {
      SearchHintResult result = apiInstance.getSearchHints(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia, includeGenres, includeStudios, includeArtists);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SearchApi#getSearchHints");
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
| **searchTerm** | **String**| The search term to filter on. | |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **userId** | **UUID**| Optional. Supply a user id to search within a user&#39;s library or omit to search all. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| If specified, only results with the specified item types are returned. This allows multiple, comma delimited. | [optional] |
| **excludeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| If specified, results with these item types are filtered out. This allows multiple, comma delimited. | [optional] |
| **mediaTypes** | [**List&lt;MediaType&gt;**](MediaType.md)| If specified, only results with the specified media types are returned. This allows multiple, comma delimited. | [optional] |
| **parentId** | **UUID**| If specified, only children of the parent are returned. | [optional] |
| **isMovie** | **Boolean**| Optional filter for movies. | [optional] |
| **isSeries** | **Boolean**| Optional filter for series. | [optional] |
| **isNews** | **Boolean**| Optional filter for news. | [optional] |
| **isKids** | **Boolean**| Optional filter for kids. | [optional] |
| **isSports** | **Boolean**| Optional filter for sports. | [optional] |
| **includePeople** | **Boolean**| Optional filter whether to include people. | [optional] [default to true] |
| **includeMedia** | **Boolean**| Optional filter whether to include media. | [optional] [default to true] |
| **includeGenres** | **Boolean**| Optional filter whether to include genres. | [optional] [default to true] |
| **includeStudios** | **Boolean**| Optional filter whether to include studios. | [optional] [default to true] |
| **includeArtists** | **Boolean**| Optional filter whether to include artists. | [optional] [default to true] |

### Return type

[**SearchHintResult**](SearchHintResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Search hint returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

