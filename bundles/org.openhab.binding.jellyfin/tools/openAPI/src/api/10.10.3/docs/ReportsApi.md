# ReportsApi

All URIs are relative to *http://nuc.ehrendingen:8096*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getActivityLogs**](ReportsApi.md#getActivityLogs) | **GET** /Reports/Activities |  |
| [**getItemReport**](ReportsApi.md#getItemReport) | **GET** /Reports/Items |  |
| [**getReportDownload**](ReportsApi.md#getReportDownload) | **GET** /Reports/Items/Download |  |
| [**getReportHeaders**](ReportsApi.md#getReportHeaders) | **GET** /Reports/Headers |  |


<a id="getActivityLogs"></a>
# **getActivityLogs**
> getActivityLogs(reportView, displayType, hasQueryLimit, groupBy, reportColumns, startIndex, limit, minDate, includeItemTypes)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ReportsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ReportsApi apiInstance = new ReportsApi(defaultClient);
    String reportView = "reportView_example"; // String | 
    String displayType = "displayType_example"; // String | 
    Boolean hasQueryLimit = true; // Boolean | 
    String groupBy = "groupBy_example"; // String | 
    String reportColumns = "reportColumns_example"; // String | 
    Integer startIndex = 56; // Integer | 
    Integer limit = 56; // Integer | 
    String minDate = "minDate_example"; // String | 
    String includeItemTypes = "includeItemTypes_example"; // String | 
    try {
      apiInstance.getActivityLogs(reportView, displayType, hasQueryLimit, groupBy, reportColumns, startIndex, limit, minDate, includeItemTypes);
    } catch (ApiException e) {
      System.err.println("Exception when calling ReportsApi#getActivityLogs");
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
| **reportView** | **String**|  | [optional] |
| **displayType** | **String**|  | [optional] |
| **hasQueryLimit** | **Boolean**|  | [optional] |
| **groupBy** | **String**|  | [optional] |
| **reportColumns** | **String**|  | [optional] |
| **startIndex** | **Integer**|  | [optional] |
| **limit** | **Integer**|  | [optional] |
| **minDate** | **String**|  | [optional] |
| **includeItemTypes** | **String**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getItemReport"></a>
# **getItemReport**
> ReportResult getItemReport(hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, minIndexNumber, parentIndexNumber, hasParentalRating, isHd, locationTypes, excludeLocationTypes, isMissing, isUnaried, minCommunityRating, minCriticRating, airedDuringSeason, minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isInBoxSet, excludeItemIds, enableTotalRecordCount, startIndex, limit, recursive, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, isNotFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, artists, excludeArtistIds, artistIds, albums, albumIds, ids, videoTypes, userId, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, reportView, displayType, hasQueryLimit, groupBy, reportColumns, enableImages)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ReportsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ReportsApi apiInstance = new ReportsApi(defaultClient);
    Boolean hasThemeSong = true; // Boolean | 
    Boolean hasThemeVideo = true; // Boolean | 
    Boolean hasSubtitles = true; // Boolean | 
    Boolean hasSpecialFeature = true; // Boolean | 
    Boolean hasTrailer = true; // Boolean | 
    String adjacentTo = "adjacentTo_example"; // String | 
    Integer minIndexNumber = 56; // Integer | 
    Integer parentIndexNumber = 56; // Integer | 
    Boolean hasParentalRating = true; // Boolean | 
    Boolean isHd = true; // Boolean | 
    String locationTypes = "locationTypes_example"; // String | 
    String excludeLocationTypes = "excludeLocationTypes_example"; // String | 
    Boolean isMissing = true; // Boolean | 
    Boolean isUnaried = true; // Boolean | 
    Double minCommunityRating = 3.4D; // Double | 
    Double minCriticRating = 3.4D; // Double | 
    Integer airedDuringSeason = 56; // Integer | 
    String minPremiereDate = "minPremiereDate_example"; // String | 
    String minDateLastSaved = "minDateLastSaved_example"; // String | 
    String minDateLastSavedForUser = "minDateLastSavedForUser_example"; // String | 
    String maxPremiereDate = "maxPremiereDate_example"; // String | 
    Boolean hasOverview = true; // Boolean | 
    Boolean hasImdbId = true; // Boolean | 
    Boolean hasTmdbId = true; // Boolean | 
    Boolean hasTvdbId = true; // Boolean | 
    Boolean isInBoxSet = true; // Boolean | 
    String excludeItemIds = "excludeItemIds_example"; // String | 
    Boolean enableTotalRecordCount = true; // Boolean | 
    Integer startIndex = 56; // Integer | 
    Integer limit = 56; // Integer | 
    Boolean recursive = true; // Boolean | 
    String sortOrder = "sortOrder_example"; // String | 
    String parentId = "parentId_example"; // String | 
    String fields = "fields_example"; // String | 
    String excludeItemTypes = "excludeItemTypes_example"; // String | 
    String includeItemTypes = "includeItemTypes_example"; // String | 
    String filters = "filters_example"; // String | 
    Boolean isFavorite = true; // Boolean | 
    Boolean isNotFavorite = true; // Boolean | 
    String mediaTypes = "mediaTypes_example"; // String | 
    String imageTypes = "imageTypes_example"; // String | 
    String sortBy = "sortBy_example"; // String | 
    Boolean isPlayed = true; // Boolean | 
    String genres = "genres_example"; // String | 
    String genreIds = "genreIds_example"; // String | 
    String officialRatings = "officialRatings_example"; // String | 
    String tags = "tags_example"; // String | 
    String years = "years_example"; // String | 
    Boolean enableUserData = true; // Boolean | 
    Integer imageTypeLimit = 56; // Integer | 
    String enableImageTypes = "enableImageTypes_example"; // String | 
    String person = "person_example"; // String | 
    String personIds = "personIds_example"; // String | 
    String personTypes = "personTypes_example"; // String | 
    String studios = "studios_example"; // String | 
    String studioIds = "studioIds_example"; // String | 
    String artists = "artists_example"; // String | 
    String excludeArtistIds = "excludeArtistIds_example"; // String | 
    String artistIds = "artistIds_example"; // String | 
    String albums = "albums_example"; // String | 
    String albumIds = "albumIds_example"; // String | 
    String ids = "ids_example"; // String | 
    String videoTypes = "videoTypes_example"; // String | 
    String userId = "userId_example"; // String | 
    String minOfficialRating = "minOfficialRating_example"; // String | 
    Boolean isLocked = true; // Boolean | 
    Boolean isPlaceHolder = true; // Boolean | 
    Boolean hasOfficialRating = true; // Boolean | 
    Boolean collapseBoxSetItems = true; // Boolean | 
    Boolean is3D = true; // Boolean | 
    String seriesStatus = "seriesStatus_example"; // String | 
    String nameStartsWithOrGreater = "nameStartsWithOrGreater_example"; // String | 
    String nameStartsWith = "nameStartsWith_example"; // String | 
    String nameLessThan = "nameLessThan_example"; // String | 
    String reportView = "reportView_example"; // String | 
    String displayType = "displayType_example"; // String | 
    Boolean hasQueryLimit = true; // Boolean | 
    String groupBy = "groupBy_example"; // String | 
    String reportColumns = "reportColumns_example"; // String | 
    Boolean enableImages = true; // Boolean | 
    try {
      ReportResult result = apiInstance.getItemReport(hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, minIndexNumber, parentIndexNumber, hasParentalRating, isHd, locationTypes, excludeLocationTypes, isMissing, isUnaried, minCommunityRating, minCriticRating, airedDuringSeason, minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isInBoxSet, excludeItemIds, enableTotalRecordCount, startIndex, limit, recursive, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, isNotFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, artists, excludeArtistIds, artistIds, albums, albumIds, ids, videoTypes, userId, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, reportView, displayType, hasQueryLimit, groupBy, reportColumns, enableImages);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ReportsApi#getItemReport");
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
| **hasThemeSong** | **Boolean**|  | [optional] |
| **hasThemeVideo** | **Boolean**|  | [optional] |
| **hasSubtitles** | **Boolean**|  | [optional] |
| **hasSpecialFeature** | **Boolean**|  | [optional] |
| **hasTrailer** | **Boolean**|  | [optional] |
| **adjacentTo** | **String**|  | [optional] |
| **minIndexNumber** | **Integer**|  | [optional] |
| **parentIndexNumber** | **Integer**|  | [optional] |
| **hasParentalRating** | **Boolean**|  | [optional] |
| **isHd** | **Boolean**|  | [optional] |
| **locationTypes** | **String**|  | [optional] |
| **excludeLocationTypes** | **String**|  | [optional] |
| **isMissing** | **Boolean**|  | [optional] |
| **isUnaried** | **Boolean**|  | [optional] |
| **minCommunityRating** | **Double**|  | [optional] |
| **minCriticRating** | **Double**|  | [optional] |
| **airedDuringSeason** | **Integer**|  | [optional] |
| **minPremiereDate** | **String**|  | [optional] |
| **minDateLastSaved** | **String**|  | [optional] |
| **minDateLastSavedForUser** | **String**|  | [optional] |
| **maxPremiereDate** | **String**|  | [optional] |
| **hasOverview** | **Boolean**|  | [optional] |
| **hasImdbId** | **Boolean**|  | [optional] |
| **hasTmdbId** | **Boolean**|  | [optional] |
| **hasTvdbId** | **Boolean**|  | [optional] |
| **isInBoxSet** | **Boolean**|  | [optional] |
| **excludeItemIds** | **String**|  | [optional] |
| **enableTotalRecordCount** | **Boolean**|  | [optional] |
| **startIndex** | **Integer**|  | [optional] |
| **limit** | **Integer**|  | [optional] |
| **recursive** | **Boolean**|  | [optional] |
| **sortOrder** | **String**|  | [optional] |
| **parentId** | **String**|  | [optional] |
| **fields** | **String**|  | [optional] |
| **excludeItemTypes** | **String**|  | [optional] |
| **includeItemTypes** | **String**|  | [optional] |
| **filters** | **String**|  | [optional] |
| **isFavorite** | **Boolean**|  | [optional] |
| **isNotFavorite** | **Boolean**|  | [optional] |
| **mediaTypes** | **String**|  | [optional] |
| **imageTypes** | **String**|  | [optional] |
| **sortBy** | **String**|  | [optional] |
| **isPlayed** | **Boolean**|  | [optional] |
| **genres** | **String**|  | [optional] |
| **genreIds** | **String**|  | [optional] |
| **officialRatings** | **String**|  | [optional] |
| **tags** | **String**|  | [optional] |
| **years** | **String**|  | [optional] |
| **enableUserData** | **Boolean**|  | [optional] |
| **imageTypeLimit** | **Integer**|  | [optional] |
| **enableImageTypes** | **String**|  | [optional] |
| **person** | **String**|  | [optional] |
| **personIds** | **String**|  | [optional] |
| **personTypes** | **String**|  | [optional] |
| **studios** | **String**|  | [optional] |
| **studioIds** | **String**|  | [optional] |
| **artists** | **String**|  | [optional] |
| **excludeArtistIds** | **String**|  | [optional] |
| **artistIds** | **String**|  | [optional] |
| **albums** | **String**|  | [optional] |
| **albumIds** | **String**|  | [optional] |
| **ids** | **String**|  | [optional] |
| **videoTypes** | **String**|  | [optional] |
| **userId** | **String**|  | [optional] |
| **minOfficialRating** | **String**|  | [optional] |
| **isLocked** | **Boolean**|  | [optional] |
| **isPlaceHolder** | **Boolean**|  | [optional] |
| **hasOfficialRating** | **Boolean**|  | [optional] |
| **collapseBoxSetItems** | **Boolean**|  | [optional] |
| **is3D** | **Boolean**|  | [optional] |
| **seriesStatus** | **String**|  | [optional] |
| **nameStartsWithOrGreater** | **String**|  | [optional] |
| **nameStartsWith** | **String**|  | [optional] |
| **nameLessThan** | **String**|  | [optional] |
| **reportView** | **String**|  | [optional] |
| **displayType** | **String**|  | [optional] |
| **hasQueryLimit** | **Boolean**|  | [optional] |
| **groupBy** | **String**|  | [optional] |
| **reportColumns** | **String**|  | [optional] |
| **enableImages** | **Boolean**|  | [optional] [default to true] |

### Return type

[**ReportResult**](ReportResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getReportDownload"></a>
# **getReportDownload**
> ReportResult getReportDownload(hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, minIndexNumber, parentIndexNumber, hasParentalRating, isHd, locationTypes, excludeLocationTypes, isMissing, isUnaried, minCommunityRating, minCriticRating, airedDuringSeason, minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isInBoxSet, excludeItemIds, enableTotalRecordCount, startIndex, limit, recursive, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, isNotFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, artists, excludeArtistIds, artistIds, albums, albumIds, ids, videoTypes, userId, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, reportView, displayType, hasQueryLimit, groupBy, reportColumns, minDate, exportType, enableImages)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ReportsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ReportsApi apiInstance = new ReportsApi(defaultClient);
    Boolean hasThemeSong = true; // Boolean | 
    Boolean hasThemeVideo = true; // Boolean | 
    Boolean hasSubtitles = true; // Boolean | 
    Boolean hasSpecialFeature = true; // Boolean | 
    Boolean hasTrailer = true; // Boolean | 
    String adjacentTo = "adjacentTo_example"; // String | 
    Integer minIndexNumber = 56; // Integer | 
    Integer parentIndexNumber = 56; // Integer | 
    Boolean hasParentalRating = true; // Boolean | 
    Boolean isHd = true; // Boolean | 
    String locationTypes = "locationTypes_example"; // String | 
    String excludeLocationTypes = "excludeLocationTypes_example"; // String | 
    Boolean isMissing = true; // Boolean | 
    Boolean isUnaried = true; // Boolean | 
    Double minCommunityRating = 3.4D; // Double | 
    Double minCriticRating = 3.4D; // Double | 
    Integer airedDuringSeason = 56; // Integer | 
    String minPremiereDate = "minPremiereDate_example"; // String | 
    String minDateLastSaved = "minDateLastSaved_example"; // String | 
    String minDateLastSavedForUser = "minDateLastSavedForUser_example"; // String | 
    String maxPremiereDate = "maxPremiereDate_example"; // String | 
    Boolean hasOverview = true; // Boolean | 
    Boolean hasImdbId = true; // Boolean | 
    Boolean hasTmdbId = true; // Boolean | 
    Boolean hasTvdbId = true; // Boolean | 
    Boolean isInBoxSet = true; // Boolean | 
    String excludeItemIds = "excludeItemIds_example"; // String | 
    Boolean enableTotalRecordCount = true; // Boolean | 
    Integer startIndex = 56; // Integer | 
    Integer limit = 56; // Integer | 
    Boolean recursive = true; // Boolean | 
    String sortOrder = "sortOrder_example"; // String | 
    String parentId = "parentId_example"; // String | 
    String fields = "fields_example"; // String | 
    String excludeItemTypes = "excludeItemTypes_example"; // String | 
    String includeItemTypes = "includeItemTypes_example"; // String | 
    String filters = "filters_example"; // String | 
    Boolean isFavorite = true; // Boolean | 
    Boolean isNotFavorite = true; // Boolean | 
    String mediaTypes = "mediaTypes_example"; // String | 
    String imageTypes = "imageTypes_example"; // String | 
    String sortBy = "sortBy_example"; // String | 
    Boolean isPlayed = true; // Boolean | 
    String genres = "genres_example"; // String | 
    String genreIds = "genreIds_example"; // String | 
    String officialRatings = "officialRatings_example"; // String | 
    String tags = "tags_example"; // String | 
    String years = "years_example"; // String | 
    Boolean enableUserData = true; // Boolean | 
    Integer imageTypeLimit = 56; // Integer | 
    String enableImageTypes = "enableImageTypes_example"; // String | 
    String person = "person_example"; // String | 
    String personIds = "personIds_example"; // String | 
    String personTypes = "personTypes_example"; // String | 
    String studios = "studios_example"; // String | 
    String studioIds = "studioIds_example"; // String | 
    String artists = "artists_example"; // String | 
    String excludeArtistIds = "excludeArtistIds_example"; // String | 
    String artistIds = "artistIds_example"; // String | 
    String albums = "albums_example"; // String | 
    String albumIds = "albumIds_example"; // String | 
    String ids = "ids_example"; // String | 
    String videoTypes = "videoTypes_example"; // String | 
    String userId = "userId_example"; // String | 
    String minOfficialRating = "minOfficialRating_example"; // String | 
    Boolean isLocked = true; // Boolean | 
    Boolean isPlaceHolder = true; // Boolean | 
    Boolean hasOfficialRating = true; // Boolean | 
    Boolean collapseBoxSetItems = true; // Boolean | 
    Boolean is3D = true; // Boolean | 
    String seriesStatus = "seriesStatus_example"; // String | 
    String nameStartsWithOrGreater = "nameStartsWithOrGreater_example"; // String | 
    String nameStartsWith = "nameStartsWith_example"; // String | 
    String nameLessThan = "nameLessThan_example"; // String | 
    String reportView = "reportView_example"; // String | 
    String displayType = "displayType_example"; // String | 
    Boolean hasQueryLimit = true; // Boolean | 
    String groupBy = "groupBy_example"; // String | 
    String reportColumns = "reportColumns_example"; // String | 
    String minDate = "minDate_example"; // String | 
    ReportExportType exportType = ReportExportType.fromValue("CSV"); // ReportExportType | 
    Boolean enableImages = true; // Boolean | 
    try {
      ReportResult result = apiInstance.getReportDownload(hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, minIndexNumber, parentIndexNumber, hasParentalRating, isHd, locationTypes, excludeLocationTypes, isMissing, isUnaried, minCommunityRating, minCriticRating, airedDuringSeason, minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isInBoxSet, excludeItemIds, enableTotalRecordCount, startIndex, limit, recursive, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, isNotFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, artists, excludeArtistIds, artistIds, albums, albumIds, ids, videoTypes, userId, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, reportView, displayType, hasQueryLimit, groupBy, reportColumns, minDate, exportType, enableImages);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ReportsApi#getReportDownload");
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
| **hasThemeSong** | **Boolean**|  | [optional] |
| **hasThemeVideo** | **Boolean**|  | [optional] |
| **hasSubtitles** | **Boolean**|  | [optional] |
| **hasSpecialFeature** | **Boolean**|  | [optional] |
| **hasTrailer** | **Boolean**|  | [optional] |
| **adjacentTo** | **String**|  | [optional] |
| **minIndexNumber** | **Integer**|  | [optional] |
| **parentIndexNumber** | **Integer**|  | [optional] |
| **hasParentalRating** | **Boolean**|  | [optional] |
| **isHd** | **Boolean**|  | [optional] |
| **locationTypes** | **String**|  | [optional] |
| **excludeLocationTypes** | **String**|  | [optional] |
| **isMissing** | **Boolean**|  | [optional] |
| **isUnaried** | **Boolean**|  | [optional] |
| **minCommunityRating** | **Double**|  | [optional] |
| **minCriticRating** | **Double**|  | [optional] |
| **airedDuringSeason** | **Integer**|  | [optional] |
| **minPremiereDate** | **String**|  | [optional] |
| **minDateLastSaved** | **String**|  | [optional] |
| **minDateLastSavedForUser** | **String**|  | [optional] |
| **maxPremiereDate** | **String**|  | [optional] |
| **hasOverview** | **Boolean**|  | [optional] |
| **hasImdbId** | **Boolean**|  | [optional] |
| **hasTmdbId** | **Boolean**|  | [optional] |
| **hasTvdbId** | **Boolean**|  | [optional] |
| **isInBoxSet** | **Boolean**|  | [optional] |
| **excludeItemIds** | **String**|  | [optional] |
| **enableTotalRecordCount** | **Boolean**|  | [optional] |
| **startIndex** | **Integer**|  | [optional] |
| **limit** | **Integer**|  | [optional] |
| **recursive** | **Boolean**|  | [optional] |
| **sortOrder** | **String**|  | [optional] |
| **parentId** | **String**|  | [optional] |
| **fields** | **String**|  | [optional] |
| **excludeItemTypes** | **String**|  | [optional] |
| **includeItemTypes** | **String**|  | [optional] |
| **filters** | **String**|  | [optional] |
| **isFavorite** | **Boolean**|  | [optional] |
| **isNotFavorite** | **Boolean**|  | [optional] |
| **mediaTypes** | **String**|  | [optional] |
| **imageTypes** | **String**|  | [optional] |
| **sortBy** | **String**|  | [optional] |
| **isPlayed** | **Boolean**|  | [optional] |
| **genres** | **String**|  | [optional] |
| **genreIds** | **String**|  | [optional] |
| **officialRatings** | **String**|  | [optional] |
| **tags** | **String**|  | [optional] |
| **years** | **String**|  | [optional] |
| **enableUserData** | **Boolean**|  | [optional] |
| **imageTypeLimit** | **Integer**|  | [optional] |
| **enableImageTypes** | **String**|  | [optional] |
| **person** | **String**|  | [optional] |
| **personIds** | **String**|  | [optional] |
| **personTypes** | **String**|  | [optional] |
| **studios** | **String**|  | [optional] |
| **studioIds** | **String**|  | [optional] |
| **artists** | **String**|  | [optional] |
| **excludeArtistIds** | **String**|  | [optional] |
| **artistIds** | **String**|  | [optional] |
| **albums** | **String**|  | [optional] |
| **albumIds** | **String**|  | [optional] |
| **ids** | **String**|  | [optional] |
| **videoTypes** | **String**|  | [optional] |
| **userId** | **String**|  | [optional] |
| **minOfficialRating** | **String**|  | [optional] |
| **isLocked** | **Boolean**|  | [optional] |
| **isPlaceHolder** | **Boolean**|  | [optional] |
| **hasOfficialRating** | **Boolean**|  | [optional] |
| **collapseBoxSetItems** | **Boolean**|  | [optional] |
| **is3D** | **Boolean**|  | [optional] |
| **seriesStatus** | **String**|  | [optional] |
| **nameStartsWithOrGreater** | **String**|  | [optional] |
| **nameStartsWith** | **String**|  | [optional] |
| **nameLessThan** | **String**|  | [optional] |
| **reportView** | **String**|  | [optional] |
| **displayType** | **String**|  | [optional] |
| **hasQueryLimit** | **Boolean**|  | [optional] |
| **groupBy** | **String**|  | [optional] |
| **reportColumns** | **String**|  | [optional] |
| **minDate** | **String**|  | [optional] |
| **exportType** | [**ReportExportType**](.md)|  | [optional] [default to CSV] [enum: CSV, Excel] |
| **enableImages** | **Boolean**|  | [optional] [default to true] |

### Return type

[**ReportResult**](ReportResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getReportHeaders"></a>
# **getReportHeaders**
> getReportHeaders(reportView, includeItemTypes)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ReportsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://nuc.ehrendingen:8096");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    ReportsApi apiInstance = new ReportsApi(defaultClient);
    String reportView = "reportView_example"; // String | 
    String includeItemTypes = "includeItemTypes_example"; // String | 
    try {
      apiInstance.getReportHeaders(reportView, includeItemTypes);
    } catch (ApiException e) {
      System.err.println("Exception when calling ReportsApi#getReportHeaders");
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
| **reportView** | **String**|  | [optional] |
| **includeItemTypes** | **String**|  | [optional] |

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
| **200** | Success |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

