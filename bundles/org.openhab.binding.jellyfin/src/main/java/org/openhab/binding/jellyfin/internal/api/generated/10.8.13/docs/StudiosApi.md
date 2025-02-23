# StudiosApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getStudio**](StudiosApi.md#getStudio) | **GET** /Studios/{name} | Gets a studio by name. |
| [**getStudios**](StudiosApi.md#getStudios) | **GET** /Studios | Gets all studios from a given item, folder, or the entire library. |


<a id="getStudio"></a>
# **getStudio**
> BaseItemDto getStudio(name, userId)

Gets a studio by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.StudiosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    StudiosApi apiInstance = new StudiosApi(defaultClient);
    String name = "name_example"; // String | Studio name.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    try {
      BaseItemDto result = apiInstance.getStudio(name, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling StudiosApi#getStudio");
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
| **200** | Studio returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getStudios"></a>
# **getStudios**
> BaseItemDtoQueryResult getStudios(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, enableImages, enableTotalRecordCount)

Gets all studios from a given item, folder, or the entire library.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.StudiosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    StudiosApi apiInstance = new StudiosApi(defaultClient);
    Integer startIndex = 56; // Integer | Optional. The record index to start at. All items with a lower index will be dropped from the results.
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    String searchTerm = "searchTerm_example"; // String | Optional. Search term.
    UUID parentId = UUID.randomUUID(); // UUID | Specify this to localize the search to a specific item or folder. Omit to use the root.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    List<BaseItemKind> excludeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
    List<BaseItemKind> includeItemTypes = Arrays.asList(); // List<BaseItemKind> | Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
    Boolean isFavorite = true; // Boolean | Optional filter by items that are marked as favorite, or not.
    Boolean enableUserData = true; // Boolean | Optional, include user data.
    Integer imageTypeLimit = 56; // Integer | Optional, the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    String nameStartsWithOrGreater = "nameStartsWithOrGreater_example"; // String | Optional filter by items whose name is sorted equally or greater than a given input string.
    String nameStartsWith = "nameStartsWith_example"; // String | Optional filter by items whose name is sorted equally than a given input string.
    String nameLessThan = "nameLessThan_example"; // String | Optional filter by items whose name is equally or lesser than a given input string.
    Boolean enableImages = true; // Boolean | Optional, include image information in output.
    Boolean enableTotalRecordCount = true; // Boolean | Total record count.
    try {
      BaseItemDtoQueryResult result = apiInstance.getStudios(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, enableImages, enableTotalRecordCount);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling StudiosApi#getStudios");
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
| **startIndex** | **Integer**| Optional. The record index to start at. All items with a lower index will be dropped from the results. | [optional] |
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **searchTerm** | **String**| Optional. Search term. | [optional] |
| **parentId** | **UUID**| Specify this to localize the search to a specific item or folder. Omit to use the root. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **excludeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited. | [optional] |
| **includeItemTypes** | [**List&lt;BaseItemKind&gt;**](BaseItemKind.md)| Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited. | [optional] |
| **isFavorite** | **Boolean**| Optional filter by items that are marked as favorite, or not. | [optional] |
| **enableUserData** | **Boolean**| Optional, include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional, the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **userId** | **UUID**| User id. | [optional] |
| **nameStartsWithOrGreater** | **String**| Optional filter by items whose name is sorted equally or greater than a given input string. | [optional] |
| **nameStartsWith** | **String**| Optional filter by items whose name is sorted equally than a given input string. | [optional] |
| **nameLessThan** | **String**| Optional filter by items whose name is equally or lesser than a given input string. | [optional] |
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
| **200** | Studios returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

