# YearsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getYear**](YearsApi.md#getYear) | **GET** /Years/{year} | Gets a year. |
| [**getYears**](YearsApi.md#getYears) | **GET** /Years | Get years. |


<a id="getYear"></a>
# **getYear**
> BaseItemDto getYear(year, userId)

Gets a year.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.YearsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    YearsApi apiInstance = new YearsApi(defaultClient);
    Integer year = 56; // Integer | The year.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    try {
      BaseItemDto result = apiInstance.getYear(year, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling YearsApi#getYear");
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
| **year** | **Integer**| The year. | |
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
| **200** | Year returned. |  -  |
| **404** | Year not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getYears"></a>
# **getYears**
> BaseItemDtoQueryResult getYears(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId, recursive, enableImages)

Get years.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.YearsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    YearsApi apiInstance = new YearsApi(defaultClient);
    Integer startIndex = 56; // Integer | Skips over a given number of items within the results. Use for paging.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    List<SortOrder> sortOrder = Arrays.asList(); // List<SortOrder> | Sort Order - Ascending,Descending.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    List<BaseItemKind> excludeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be excluded based on item type. This allows multiple, comma delimited.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be included based on item type. This allows multiple, comma delimited.
    List<String> mediaTypes = Arrays.asList(); // List<String> | Optional. Filter by MediaType. Allows multiple, comma delimited.
    List<String> sortBy = Arrays.asList(); // List<String> | Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist, Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate, ProductionYear, SortName, Random, Revenue, Runtime.
    Boolean enableUserData = true; // Boolean | Optional. Include user data.
    Integer imageTypeLimit = 56; // Integer | Optional. The max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    UUID userId = UUID.randomUUID(); // UUID | User Id.
    Boolean recursive = true; // Boolean | Search recursively.
    Boolean enableImages = true; // Boolean | Optional. Include image information in output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getYears(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId, recursive, enableImages);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling YearsApi#getYears");
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
| **startIndex** | **Integer**| Skips over a given number of items within the results. Use for paging. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **sortOrder** | [**List&lt;SortOrder&gt;**](SortOrder.md)| Sort Order - Ascending,Descending. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **excludeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be excluded based on item type. This allows multiple, comma delimited. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be included based on item type. This allows multiple, comma delimited. | [optional] |
| **mediaTypes** | [**List&lt;String&gt;**](String.md)| Optional. Filter by MediaType. Allows multiple, comma delimited. | [optional] |
| **sortBy** | [**List&lt;String&gt;**](String.md)| Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist, Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate, ProductionYear, SortName, Random, Revenue, Runtime. | [optional] |
| **enableUserData** | **Boolean**| Optional. Include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional. The max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **userId** | **UUID**| User Id. | [optional] |
| **recursive** | **Boolean**| Search recursively. | [optional] [default to true] |
| **enableImages** | **Boolean**| Optional. Include image information in output. | [optional] [default to true] |

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
| **200** | Year query returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

