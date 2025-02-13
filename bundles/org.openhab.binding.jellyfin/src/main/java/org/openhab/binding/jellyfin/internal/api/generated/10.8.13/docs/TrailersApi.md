# TrailersApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getTrailers**](TrailersApi.md#getTrailers) | **GET** /Trailers | Finds movies and trailers similar to a given trailer. |


<a id="getTrailers"></a>
# **getTrailers**
> BaseItemDtoQueryResult getTrailers(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K, locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating, minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit, recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, filters, isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists, excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds, enableTotalRecordCount, enableImages)

Finds movies and trailers similar to a given trailer.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.TrailersApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    TrailersApi apiInstance = new TrailersApi(defaultClient);
    UUID userId = UUID.randomUUID(); // UUID | The user id.
    String maxOfficialRating = "maxOfficialRating_example"; // String | Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
    Boolean hasThemeSong = true; // Boolean | Optional filter by items with theme songs.
    Boolean hasThemeVideo = true; // Boolean | Optional filter by items with theme videos.
    Boolean hasSubtitles = true; // Boolean | Optional filter by items with subtitles.
    Boolean hasSpecialFeature = true; // Boolean | Optional filter by items with special features.
    Boolean hasTrailer = true; // Boolean | Optional filter by items with trailers.
    String adjacentTo = "adjacentTo_example"; // String | Optional. Return items that are siblings of a supplied item.
    Integer parentIndexNumber = 56; // Integer | Optional filter by parent index number.
    Boolean hasParentalRating = true; // Boolean | Optional filter by items that have or do not have a parental rating.
    Boolean isHd = true; // Boolean | Optional filter by items that are HD or not.
    Boolean is4K = true; // Boolean | Optional filter by items that are 4K or not.
    List<LocationType> locationTypes = Arrays.asList(); // List<LocationType> | Optional. If specified, results will be filtered based on LocationType. This allows multiple, comma delimited.
    List<LocationType> excludeLocationTypes = Arrays.asList(); // List<LocationType> | Optional. If specified, results will be filtered based on the LocationType. This allows multiple, comma delimited.
    Boolean isMissing = true; // Boolean | Optional filter by items that are missing episodes or not.
    Boolean isUnaired = true; // Boolean | Optional filter by items that are unaired episodes or not.
    Double minCommunityRating = 3.4D; // Double | Optional filter by minimum community rating.
    Double minCriticRating = 3.4D; // Double | Optional filter by minimum critic rating.
    OffsetDateTime minPremiereDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The minimum premiere date. Format = ISO.
    OffsetDateTime minDateLastSaved = OffsetDateTime.now(); // OffsetDateTime | Optional. The minimum last saved date. Format = ISO.
    OffsetDateTime minDateLastSavedForUser = OffsetDateTime.now(); // OffsetDateTime | Optional. The minimum last saved date for the current user. Format = ISO.
    OffsetDateTime maxPremiereDate = OffsetDateTime.now(); // OffsetDateTime | Optional. The maximum premiere date. Format = ISO.
    Boolean hasOverview = true; // Boolean | Optional filter by items that have an overview or not.
    Boolean hasImdbId = true; // Boolean | Optional filter by items that have an imdb id or not.
    Boolean hasTmdbId = true; // Boolean | Optional filter by items that have a tmdb id or not.
    Boolean hasTvdbId = true; // Boolean | Optional filter by items that have a tvdb id or not.
    Boolean isMovie = true; // Boolean | Optional filter for live tv movies.
    Boolean isSeries = true; // Boolean | Optional filter for live tv series.
    Boolean isNews = true; // Boolean | Optional filter for live tv news.
    Boolean isKids = true; // Boolean | Optional filter for live tv kids.
    Boolean isSports = true; // Boolean | Optional filter for live tv sports.
    List<UUID> excludeItemIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered by excluding item ids. This allows multiple, comma delimited.
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    Boolean recursive = true; // Boolean | When searching within folders, this determines whether or not the search will be recursive. true/false.
    String searchTerm = "searchTerm_example"; // String | Optional. Filter based on a search term.
    List<SortOrder> sortOrder = Arrays.asList(); // List<SortOrder> | Sort Order - Ascending,Descending.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines.
    List<BaseItemKind> excludeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    List<ItemFilter> filters = Arrays.asList(); // List<ItemFilter> | Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options: IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
    Boolean isFavorite = true; // Boolean | Optional filter by items that are marked as favorite, or not.
    List<String> mediaTypes = Arrays.asList(); // List<String> | Optional filter by MediaType. Allows multiple, comma delimited.
    List<ImageType> imageTypes = Arrays.asList(); // List<ImageType> | Optional. If specified, results will be filtered based on those containing image types. This allows multiple, comma delimited.
    List<String> sortBy = Arrays.asList(); // List<String> | Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist, Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate, ProductionYear, SortName, Random, Revenue, Runtime.
    Boolean isPlayed = true; // Boolean | Optional filter by items that are played, or not.
    List<String> genres = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
    List<String> officialRatings = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
    List<String> tags = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
    List<Integer> years = Arrays.asList(); // List<Integer> | Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
    Boolean enableUserData = true; // Boolean | Optional, include user data.
    Integer imageTypeLimit = 56; // Integer | Optional, the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    String person = "person_example"; // String | Optional. If specified, results will be filtered to include only those containing the specified person.
    List<UUID> personIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered to include only those containing the specified person id.
    List<String> personTypes = Arrays.asList(); // List<String> | Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
    List<String> studios = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
    List<String> artists = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on artists. This allows multiple, pipe delimited.
    List<UUID> excludeArtistIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on artist id. This allows multiple, pipe delimited.
    List<UUID> artistIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered to include only those containing the specified artist id.
    List<UUID> albumArtistIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered to include only those containing the specified album artist id.
    List<UUID> contributingArtistIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered to include only those containing the specified contributing artist id.
    List<String> albums = Arrays.asList(); // List<String> | Optional. If specified, results will be filtered based on album. This allows multiple, pipe delimited.
    List<UUID> albumIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on album id. This allows multiple, pipe delimited.
    List<UUID> ids = Arrays.asList(); // List<UUID> | Optional. If specific items are needed, specify a list of item id's to retrieve. This allows multiple, comma delimited.
    List<VideoType> videoTypes = Arrays.asList(); // List<VideoType> | Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
    String minOfficialRating = "minOfficialRating_example"; // String | Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
    Boolean isLocked = true; // Boolean | Optional filter by items that are locked.
    Boolean isPlaceHolder = true; // Boolean | Optional filter by items that are placeholders.
    Boolean hasOfficialRating = true; // Boolean | Optional filter by items that have official ratings.
    Boolean collapseBoxSetItems = true; // Boolean | Whether or not to hide items behind their boxsets.
    Integer minWidth = 56; // Integer | Optional. Filter by the minimum width of the item.
    Integer minHeight = 56; // Integer | Optional. Filter by the minimum height of the item.
    Integer maxWidth = 56; // Integer | Optional. Filter by the maximum width of the item.
    Integer maxHeight = 56; // Integer | Optional. Filter by the maximum height of the item.
    Boolean is3D = true; // Boolean | Optional filter by items that are 3D, or not.
    List<SeriesStatus> seriesStatus = Arrays.asList(); // List<SeriesStatus> | Optional filter by Series Status. Allows multiple, comma delimited.
    String nameStartsWithOrGreater = "nameStartsWithOrGreater_example"; // String | Optional filter by items whose name is sorted equally or greater than a given input string.
    String nameStartsWith = "nameStartsWith_example"; // String | Optional filter by items whose name is sorted equally than a given input string.
    String nameLessThan = "nameLessThan_example"; // String | Optional filter by items whose name is equally or lesser than a given input string.
    List<UUID> studioIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
    List<UUID> genreIds = Arrays.asList(); // List<UUID> | Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
    Boolean enableTotalRecordCount = true; // Boolean | Optional. Enable the total record count.
    Boolean enableImages = true; // Boolean | Optional, include image information in output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getTrailers(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K, locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating, minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit, recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, filters, isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists, excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds, enableTotalRecordCount, enableImages);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TrailersApi#getTrailers");
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
| **userId** | **UUID**| The user id. | [optional] |
| **maxOfficialRating** | **String**| Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). | [optional] |
| **hasThemeSong** | **Boolean**| Optional filter by items with theme songs. | [optional] |
| **hasThemeVideo** | **Boolean**| Optional filter by items with theme videos. | [optional] |
| **hasSubtitles** | **Boolean**| Optional filter by items with subtitles. | [optional] |
| **hasSpecialFeature** | **Boolean**| Optional filter by items with special features. | [optional] |
| **hasTrailer** | **Boolean**| Optional filter by items with trailers. | [optional] |
| **adjacentTo** | **String**| Optional. Return items that are siblings of a supplied item. | [optional] |
| **parentIndexNumber** | **Integer**| Optional filter by parent index number. | [optional] |
| **hasParentalRating** | **Boolean**| Optional filter by items that have or do not have a parental rating. | [optional] |
| **isHd** | **Boolean**| Optional filter by items that are HD or not. | [optional] |
| **is4K** | **Boolean**| Optional filter by items that are 4K or not. | [optional] |
| **locationTypes** | [**List&lt;LocationType&gt;**](LocationType.md)| Optional. If specified, results will be filtered based on LocationType. This allows multiple, comma delimited. | [optional] |
| **excludeLocationTypes** | [**List&lt;LocationType&gt;**](LocationType.md)| Optional. If specified, results will be filtered based on the LocationType. This allows multiple, comma delimited. | [optional] |
| **isMissing** | **Boolean**| Optional filter by items that are missing episodes or not. | [optional] |
| **isUnaired** | **Boolean**| Optional filter by items that are unaired episodes or not. | [optional] |
| **minCommunityRating** | **Double**| Optional filter by minimum community rating. | [optional] |
| **minCriticRating** | **Double**| Optional filter by minimum critic rating. | [optional] |
| **minPremiereDate** | **OffsetDateTime**| Optional. The minimum premiere date. Format &#x3D; ISO. | [optional] |
| **minDateLastSaved** | **OffsetDateTime**| Optional. The minimum last saved date. Format &#x3D; ISO. | [optional] |
| **minDateLastSavedForUser** | **OffsetDateTime**| Optional. The minimum last saved date for the current user. Format &#x3D; ISO. | [optional] |
| **maxPremiereDate** | **OffsetDateTime**| Optional. The maximum premiere date. Format &#x3D; ISO. | [optional] |
| **hasOverview** | **Boolean**| Optional filter by items that have an overview or not. | [optional] |
| **hasImdbId** | **Boolean**| Optional filter by items that have an imdb id or not. | [optional] |
| **hasTmdbId** | **Boolean**| Optional filter by items that have a tmdb id or not. | [optional] |
| **hasTvdbId** | **Boolean**| Optional filter by items that have a tvdb id or not. | [optional] |
| **isMovie** | **Boolean**| Optional filter for live tv movies. | [optional] |
| **isSeries** | **Boolean**| Optional filter for live tv series. | [optional] |
| **isNews** | **Boolean**| Optional filter for live tv news. | [optional] |
| **isKids** | **Boolean**| Optional filter for live tv kids. | [optional] |
| **isSports** | **Boolean**| Optional filter for live tv sports. | [optional] |
| **excludeItemIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered by excluding item ids. This allows multiple, comma delimited. | [optional] |
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **recursive** | **Boolean**| When searching within folders, this determines whether or not the search will be recursive. true/false. | [optional] |
| **searchTerm** | **String**| Optional. Filter based on a search term. | [optional] |
| **sortOrder** | [**List&lt;SortOrder&gt;**](SortOrder.md)| Sort Order - Ascending,Descending. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines. | [optional] |
| **excludeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **filters** | [**List&lt;ItemFilter&gt;**](ItemFilter.md)| Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options: IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. | [optional] |
| **isFavorite** | **Boolean**| Optional filter by items that are marked as favorite, or not. | [optional] |
| **mediaTypes** | [**List&lt;String&gt;**](String.md)| Optional filter by MediaType. Allows multiple, comma delimited. | [optional] |
| **imageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. If specified, results will be filtered based on those containing image types. This allows multiple, comma delimited. | [optional] |
| **sortBy** | [**List&lt;String&gt;**](String.md)| Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist, Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate, ProductionYear, SortName, Random, Revenue, Runtime. | [optional] |
| **isPlayed** | **Boolean**| Optional filter by items that are played, or not. | [optional] |
| **genres** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited. | [optional] |
| **officialRatings** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited. | [optional] |
| **tags** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited. | [optional] |
| **years** | [**List&lt;Integer&gt;**](Integer.md)| Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited. | [optional] |
| **enableUserData** | **Boolean**| Optional, include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional, the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **person** | **String**| Optional. If specified, results will be filtered to include only those containing the specified person. | [optional] |
| **personIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered to include only those containing the specified person id. | [optional] |
| **personTypes** | [**List&lt;String&gt;**](String.md)| Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited. | [optional] |
| **studios** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited. | [optional] |
| **artists** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on artists. This allows multiple, pipe delimited. | [optional] |
| **excludeArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on artist id. This allows multiple, pipe delimited. | [optional] |
| **artistIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered to include only those containing the specified artist id. | [optional] |
| **albumArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered to include only those containing the specified album artist id. | [optional] |
| **contributingArtistIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered to include only those containing the specified contributing artist id. | [optional] |
| **albums** | [**List&lt;String&gt;**](String.md)| Optional. If specified, results will be filtered based on album. This allows multiple, pipe delimited. | [optional] |
| **albumIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on album id. This allows multiple, pipe delimited. | [optional] |
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows multiple, comma delimited. | [optional] |
| **videoTypes** | [**List&lt;VideoType&gt;**](VideoType.md)| Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited. | [optional] |
| **minOfficialRating** | **String**| Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). | [optional] |
| **isLocked** | **Boolean**| Optional filter by items that are locked. | [optional] |
| **isPlaceHolder** | **Boolean**| Optional filter by items that are placeholders. | [optional] |
| **hasOfficialRating** | **Boolean**| Optional filter by items that have official ratings. | [optional] |
| **collapseBoxSetItems** | **Boolean**| Whether or not to hide items behind their boxsets. | [optional] |
| **minWidth** | **Integer**| Optional. Filter by the minimum width of the item. | [optional] |
| **minHeight** | **Integer**| Optional. Filter by the minimum height of the item. | [optional] |
| **maxWidth** | **Integer**| Optional. Filter by the maximum width of the item. | [optional] |
| **maxHeight** | **Integer**| Optional. Filter by the maximum height of the item. | [optional] |
| **is3D** | **Boolean**| Optional filter by items that are 3D, or not. | [optional] |
| **seriesStatus** | [**List&lt;SeriesStatus&gt;**](SeriesStatus.md)| Optional filter by Series Status. Allows multiple, comma delimited. | [optional] |
| **nameStartsWithOrGreater** | **String**| Optional filter by items whose name is sorted equally or greater than a given input string. | [optional] |
| **nameStartsWith** | **String**| Optional filter by items whose name is sorted equally than a given input string. | [optional] |
| **nameLessThan** | **String**| Optional filter by items whose name is equally or lesser than a given input string. | [optional] |
| **studioIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited. | [optional] |
| **genreIds** | [**List&lt;UUID&gt;**](UUID.md)| Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited. | [optional] |
| **enableTotalRecordCount** | **Boolean**| Optional. Enable the total record count. | [optional] [default to true] |
| **enableImages** | **Boolean**| Optional, include image information in output. | [optional] [default to true] |

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

