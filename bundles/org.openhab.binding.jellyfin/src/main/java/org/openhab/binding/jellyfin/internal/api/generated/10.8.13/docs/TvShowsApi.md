# TvShowsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getEpisodes**](TvShowsApi.md#getEpisodes) | **GET** /Shows/{seriesId}/Episodes | Gets episodes for a tv season. |
| [**getNextUp**](TvShowsApi.md#getNextUp) | **GET** /Shows/NextUp | Gets a list of next up episodes. |
| [**getSeasons**](TvShowsApi.md#getSeasons) | **GET** /Shows/{seriesId}/Seasons | Gets seasons for a tv series. |
| [**getUpcomingEpisodes**](TvShowsApi.md#getUpcomingEpisodes) | **GET** /Shows/Upcoming | Gets a list of upcoming episodes. |


<a id="getEpisodes"></a>
# **getEpisodes**
> BaseItemDtoQueryResult getEpisodes(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo, startItemId, startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy)

Gets episodes for a tv season.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TvShowsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TvShowsApi apiInstance = new TvShowsApi(defaultClient);
    UUID seriesId = UUID.randomUUID(); // UUID | The series id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    Integer season = 56; // Integer | Optional filter by season number.
    UUID seasonId = UUID.randomUUID(); // UUID | Optional. Filter by season id.
    Boolean isMissing = true; // Boolean | Optional. Filter by items that are missing episodes or not.
    String adjacentTo = "adjacentTo_example"; // String | Optional. Return items that are siblings of a supplied item.
    UUID startItemId = UUID.randomUUID(); // UUID | Optional. Skip through the list until a given item is found.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    Boolean enableImages = true; // Boolean | Optional, include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional, the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    String sortBy = "sortBy_example"; // String | Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist, Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate, ProductionYear, SortName, Random, Revenue, Runtime.
    try {
      BaseItemDtoQueryResult result = apiInstance.getEpisodes(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo, startItemId, startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TvShowsApi#getEpisodes");
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
| **seriesId** | **UUID**| The series id. | |
| **userId** | **UUID**| The user id. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |
| **season** | **Integer**| Optional filter by season number. | [optional] |
| **seasonId** | **UUID**| Optional. Filter by season id. | [optional] |
| **isMissing** | **Boolean**| Optional. Filter by items that are missing episodes or not. | [optional] |
| **adjacentTo** | **String**| Optional. Return items that are siblings of a supplied item. | [optional] |
| **startItemId** | **UUID**| Optional. Skip through the list until a given item is found. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **enableImages** | **Boolean**| Optional, include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional, the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **sortBy** | **String**| Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist, Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate, ProductionYear, SortName, Random, Revenue, Runtime. | [optional] |

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
| **200** | Success |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getNextUp"></a>
# **getNextUp**
> BaseItemDtoQueryResult getNextUp(userId, startIndex, limit, fields, seriesId, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount, disableFirstEpisode, enableRewatching)

Gets a list of next up episodes.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TvShowsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TvShowsApi apiInstance = new TvShowsApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id of the user to get the next up episodes for.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    String seriesId = "seriesId_example"; // String | Optional. Filter by series id.
    UUID parentId = UUID.randomUUID(); // UUID | Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    OffsetDateTime nextUpDateCutoff = OffsetDateTime.now(); // OffsetDateTime | Optional. Starting date of shows to show in Next Up section.
    Boolean enableTotalRecordCount = true; // Boolean | Whether to enable the total records count. Defaults to true.
    Boolean disableFirstEpisode = false; // Boolean | Whether to disable sending the first episode in a series as next up.
    Boolean enableRewatching = false; // Boolean | Whether to include watched episode in next up results.
    try {
      BaseItemDtoQueryResult result = apiInstance.getNextUp(userId, startIndex, limit, fields, seriesId, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount, disableFirstEpisode, enableRewatching);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TvShowsApi#getNextUp");
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
| **userId** | **UUID**| The user id of the user to get the next up episodes for. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **seriesId** | **String**| Optional. Filter by series id. | [optional] |
| **parentId** | **UUID**| Optional. Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **nextUpDateCutoff** | **OffsetDateTime**| Optional. Starting date of shows to show in Next Up section. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Whether to enable the total records count. Defaults to true. | [optional] [default to true] |
| **disableFirstEpisode** | **Boolean**| Whether to disable sending the first episode in a series as next up. | [optional] [default to false] |
| **enableRewatching** | **Boolean**| Whether to include watched episode in next up results. | [optional] [default to false] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getSeasons"></a>
# **getSeasons**
> BaseItemDtoQueryResult getSeasons(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages, imageTypeLimit, enableImageTypes, enableUserData)

Gets seasons for a tv series.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TvShowsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TvShowsApi apiInstance = new TvShowsApi(defaultClient);
    UUID seriesId = UUID.randomUUID(); // UUID | The series id.
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
    Boolean isSpecialSeason = true; // Boolean | Optional. Filter by special season.
    Boolean isMissing = true; // Boolean | Optional. Filter by items that are missing episodes or not.
    String adjacentTo = "adjacentTo_example"; // String | Optional. Return items that are siblings of a supplied item.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    try {
      BaseItemDtoQueryResult result = apiInstance.getSeasons(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages, imageTypeLimit, enableImageTypes, enableUserData);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TvShowsApi#getSeasons");
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
| **seriesId** | **UUID**| The series id. | |
| **userId** | **UUID**| The user id. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls. | [optional] |
| **isSpecialSeason** | **Boolean**| Optional. Filter by special season. | [optional] |
| **isMissing** | **Boolean**| Optional. Filter by items that are missing episodes or not. | [optional] |
| **adjacentTo** | **String**| Optional. Return items that are siblings of a supplied item. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |

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
| **200** | Success |  -  |
| **404** | Not Found |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getUpcomingEpisodes"></a>
# **getUpcomingEpisodes**
> BaseItemDtoQueryResult getUpcomingEpisodes(userId, startIndex, limit, fields, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData)

Gets a list of upcoming episodes.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TvShowsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TvShowsApi apiInstance = new TvShowsApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id of the user to get the upcoming episodes for.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    UUID parentId = UUID.randomUUID(); // UUID | Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    try {
      BaseItemDtoQueryResult result = apiInstance.getUpcomingEpisodes(userId, startIndex, limit, fields, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TvShowsApi#getUpcomingEpisodes");
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
| **userId** | **UUID**| The user id of the user to get the upcoming episodes for. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **parentId** | **UUID**| Optional. Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

