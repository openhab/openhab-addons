# ArtistsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAlbumArtists**](ArtistsApi.md#getAlbumArtists) | **GET** /Artists/AlbumArtists | Gets all album artists from a given item, folder, or the entire library. |
| [**getArtistByName**](ArtistsApi.md#getArtistByName) | **GET** /Artists/{name} | Gets an artist by name. |
| [**getArtists**](ArtistsApi.md#getArtists) | **GET** /Artists | Gets all artists from a given item, folder, or the entire library. |


<a id="getAlbumArtists"></a>
# **getAlbumArtists**
> BaseItemDtoQueryResult getAlbumArtists(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount)

Gets all album artists from a given item, folder, or the entire library.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ArtistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ArtistsApi apiInstance = new ArtistsApi(defaultClient);
    Double minCommunityRating = 3.4D; // Double | Optional filter by minimum community rating.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    String searchTerm = "searchTerm_example"; // String | Optional. Search term.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    List<BaseItemKind> excludeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    List<ItemFilter> filters = Arrays.asList(); // List<ItemFilter> | Optional. Specify additional filters to apply.
    Boolean isFavorite = true; // Boolean | Optional filter by items that are marked as favorite, or not.
    List<MediaType> mediaTypes = Arrays.asList(); // List<MediaType> | Optional filter by MediaType. Allows multiple, comma delimited.
    List<String> genres = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
    List<UUID> genreIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
    List<String> officialRatings = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
    List<String> tags = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
    List<Integer> years = Arrays.asList(); // List<Integer> | Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
    Boolean enableUserData = true; // Boolean | Optional, include user data.
    Integer imageTypeLimit = 56; // Integer | Optional, the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    String person = "person_example"; // String | Optional. If specified, results will be filtered to include only those containing the specified person.
    List<UUID> personIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered to include only those containing the specified person ids.
    List<String> personTypes = Arrays.asList(); // List<String> | Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
    List<String> studios = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
    List<UUID> studioIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    String nameStartsWithOrGreater = "nameStartsWithOrGreater_example"; // String | Optional filter by items whose name is sorted equally or greater than a given input string.
    String nameStartsWith = "nameStartsWith_example"; // String | Optional filter by items whose name is sorted equally than a given input string.
    String nameLessThan = "nameLessThan_example"; // String | Optional filter by items whose name is equally or lesser than a given input string.
    List<ItemSortBy> sortBy = Arrays.asList(); // List<ItemSortBy> | Optional. Specify one or more sort orders, comma delimited.
    List<SortOrder> sortOrder = Arrays.asList(); // List<SortOrder> | Sort Order - Ascending,Descending.
    Boolean enableImages = true; // Boolean | Optional, include image information in output.
    Boolean enableTotalRecordCount = true; // Boolean | Total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getAlbumArtists(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ArtistsApi#getAlbumArtists");
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
| **minCommunityRating** | **Double**| Optional filter by minimum community rating. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **searchTerm** | **String**| Optional. Search term. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **excludeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **filters** | [**List&lt;ItemFilter&gt;**](ItemFilter.md)| Optional. Specify additional filters to apply. | [optional] |
| **isFavorite** | **Boolean**| Optional filter by items that are marked as favorite, or not. | [optional] |
| **mediaTypes** | [**List&lt;MediaType&gt;**](MediaType.md)| Optional filter by MediaType. Allows multiple, comma delimited. | [optional] |
| **genres** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited. | [optional] |
| **genreIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited. | [optional] |
| **officialRatings** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited. | [optional] |
| **tags** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited. | [optional] |
| **years** | [**List&lt;Integer&gt;**](Integer.md)| Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited. | [optional] |
| **enableUserData** | **Boolean**| Optional, include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional, the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **person** | **String**| Optional. If specified, results will be filtered to include only those containing the specified person. | [optional] |
| **personIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered to include only those containing the specified person ids. | [optional] |
| **personTypes** | [**List&lt;String&gt;**](String.md)| Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited. | [optional] |
| **studios** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited. | [optional] |
| **studioIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited. | [optional] |
| **userId** | **UUID**| User id. | [optional] |
| **nameStartsWithOrGreater** | **String**| Optional filter by items whose name is sorted equally or greater than a given input string. | [optional] |
| **nameStartsWith** | **String**| Optional filter by items whose name is sorted equally than a given input string. | [optional] |
| **nameLessThan** | **String**| Optional filter by items whose name is equally or lesser than a given input string. | [optional] |
| **sortBy** | [**List&lt;ItemSortBy&gt;**](ItemSortBy.md)| Optional. Specify one or more sort orders, comma delimited. | [optional] |
| **sortOrder** | [**List&lt;SortOrder&gt;**](SortOrder.md)| Sort Order - Ascending,Descending. | [optional] |
| **enableImages** | **Boolean**| Optional, include image information in output. | [optional] [default to true] |
| **enableTotalRecordCount** | **Boolean**| Total record count. | [optional] [default to true] |

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
| **200** | Album artists returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getArtistByName"></a>
# **getArtistByName**
> BaseItemDto getArtistByName(name, userId)

Gets an artist by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ArtistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ArtistsApi apiInstance = new ArtistsApi(defaultClient);
    String name = "name_example"; // String | Studio name.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    try {
      BaseItemDto result = apiInstance.getArtistByName(name, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ArtistsApi#getArtistByName");
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
| **name** | **String**| Studio name. | |
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |

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
| **200** | Artist returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getArtists"></a>
# **getArtists**
> BaseItemDtoQueryResult getArtists(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount)

Gets all artists from a given item, folder, or the entire library.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ArtistsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ArtistsApi apiInstance = new ArtistsApi(defaultClient);
    Double minCommunityRating = 3.4D; // Double | Optional filter by minimum community rating.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    String searchTerm = "searchTerm_example"; // String | Optional. Search term.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    List<BaseItemKind> excludeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    List<ItemFilter> filters = Arrays.asList(); // List<ItemFilter> | Optional. Specify additional filters to apply.
    Boolean isFavorite = true; // Boolean | Optional filter by items that are marked as favorite, or not.
    List<MediaType> mediaTypes = Arrays.asList(); // List<MediaType> | Optional filter by MediaType. Allows multiple, comma delimited.
    List<String> genres = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
    List<UUID> genreIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
    List<String> officialRatings = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
    List<String> tags = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
    List<Integer> years = Arrays.asList(); // List<Integer> | Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
    Boolean enableUserData = true; // Boolean | Optional, include user data.
    Integer imageTypeLimit = 56; // Integer | Optional, the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    String person = "person_example"; // String | Optional. If specified, results will be filtered to include only those containing the specified person.
    List<UUID> personIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered to include only those containing the specified person ids.
    List<String> personTypes = Arrays.asList(); // List<String> | Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
    List<String> studios = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
    List<UUID> studioIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    String nameStartsWithOrGreater = "nameStartsWithOrGreater_example"; // String | Optional filter by items whose name is sorted equally or greater than a given input string.
    String nameStartsWith = "nameStartsWith_example"; // String | Optional filter by items whose name is sorted equally than a given input string.
    String nameLessThan = "nameLessThan_example"; // String | Optional filter by items whose name is equally or lesser than a given input string.
    List<ItemSortBy> sortBy = Arrays.asList(); // List<ItemSortBy> | Optional. Specify one or more sort orders, comma delimited.
    List<SortOrder> sortOrder = Arrays.asList(); // List<SortOrder> | Sort Order - Ascending,Descending.
    Boolean enableImages = true; // Boolean | Optional, include image information in output.
    Boolean enableTotalRecordCount = true; // Boolean | Total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getArtists(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ArtistsApi#getArtists");
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
| **minCommunityRating** | **Double**| Optional filter by minimum community rating. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **searchTerm** | **String**| Optional. Search term. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **excludeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **filters** | [**List&lt;ItemFilter&gt;**](ItemFilter.md)| Optional. Specify additional filters to apply. | [optional] |
| **isFavorite** | **Boolean**| Optional filter by items that are marked as favorite, or not. | [optional] |
| **mediaTypes** | [**List&lt;MediaType&gt;**](MediaType.md)| Optional filter by MediaType. Allows multiple, comma delimited. | [optional] |
| **genres** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited. | [optional] |
| **genreIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited. | [optional] |
| **officialRatings** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited. | [optional] |
| **tags** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited. | [optional] |
| **years** | [**List&lt;Integer&gt;**](Integer.md)| Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited. | [optional] |
| **enableUserData** | **Boolean**| Optional, include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional, the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **person** | **String**| Optional. If specified, results will be filtered to include only those containing the specified person. | [optional] |
| **personIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered to include only those containing the specified person ids. | [optional] |
| **personTypes** | [**List&lt;String&gt;**](String.md)| Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited. | [optional] |
| **studios** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited. | [optional] |
| **studioIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited. | [optional] |
| **userId** | **UUID**| User id. | [optional] |
| **nameStartsWithOrGreater** | **String**| Optional filter by items whose name is sorted equally or greater than a given input string. | [optional] |
| **nameStartsWith** | **String**| Optional filter by items whose name is sorted equally than a given input string. | [optional] |
| **nameLessThan** | **String**| Optional filter by items whose name is equally or lesser than a given input string. | [optional] |
| **sortBy** | [**List&lt;ItemSortBy&gt;**](ItemSortBy.md)| Optional. Specify one or more sort orders, comma delimited. | [optional] |
| **sortOrder** | [**List&lt;SortOrder&gt;**](SortOrder.md)| Sort Order - Ascending,Descending. | [optional] |
| **enableImages** | **Boolean**| Optional, include image information in output. | [optional] [default to true] |
| **enableTotalRecordCount** | **Boolean**| Total record count. | [optional] [default to true] |

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
| **200** | Artists returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

