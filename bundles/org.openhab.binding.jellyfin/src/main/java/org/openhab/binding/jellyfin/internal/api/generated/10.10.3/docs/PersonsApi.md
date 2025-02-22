# PersonsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getPerson**](PersonsApi.md#getPerson) | **GET** /Persons/{name} | Get person by name. |
| [**getPersons**](PersonsApi.md#getPersons) | **GET** /Persons | Gets all persons. |


<a id="getPerson"></a>
# **getPerson**
> BaseItemDto getPerson(name, userId)

Get person by name.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PersonsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PersonsApi apiInstance = new PersonsApi(defaultClient);
    String name = "name_example"; // String | Person name.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    try {
      BaseItemDto result = apiInstance.getPerson(name, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PersonsApi#getPerson");
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
| **name** | **String**| Person name. | |
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
| **200** | Person returned. |  -  |
| **404** | Person not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getPersons"></a>
# **getPersons**
> BaseItemDtoQueryResult getPersons(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages)

Gets all persons.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.PersonsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    PersonsApi apiInstance = new PersonsApi(defaultClient);
    Integer limit = 56; // Integer | Optional. The maximum number of records to return.
    String searchTerm = "searchTerm_example"; // String | The search term.
    List<ItemFields> fields = Arrays.asList(); // List<ItemFields> | Optional. Specify additional fields of information to return in the output.
    List<ItemFilter> filters = Arrays.asList(); // List<ItemFilter> | Optional. Specify additional filters to apply.
    Boolean isFavorite = true; // Boolean | Optional filter by items that are marked as favorite, or not. userId is required.
    Boolean enableUserData = true; // Boolean | Optional, include user data.
    Integer imageTypeLimit = 56; // Integer | Optional, the max number of images to return, per image type.
    List<ImageType> enableImageTypes = Arrays.asList(); // List<ImageType> | Optional. The image types to include in the output.
    List<String> excludePersonTypes = Arrays.asList(); // List<String> | Optional. If specified results will be filtered to exclude those containing the specified PersonType. Allows multiple, comma-delimited.
    List<String> personTypes = Arrays.asList(); // List<String> | Optional. If specified results will be filtered to include only those containing the specified PersonType. Allows multiple, comma-delimited.
    UUID appearsInItemId = UUID.randomUUID(); // UUID | Optional. If specified, person results will be filtered on items related to said persons.
    UUID userId = UUID.randomUUID(); // UUID | User id.
    Boolean enableImages = true; // Boolean | Optional, include image information in output.
    try {
      BaseItemDtoQueryResult result = apiInstance.getPersons(limit, searchTerm, fields, filters, isFavorite, enableUserData, imageTypeLimit, enableImageTypes, excludePersonTypes, personTypes, appearsInItemId, userId, enableImages);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PersonsApi#getPersons");
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
| **limit** | **Integer**| Optional. The maximum number of records to return. | [optional] |
| **searchTerm** | **String**| The search term. | [optional] |
| **fields** | [**List&lt;ItemFields&gt;**](ItemFields.md)| Optional. Specify additional fields of information to return in the output. | [optional] |
| **filters** | [**List&lt;ItemFilter&gt;**](ItemFilter.md)| Optional. Specify additional filters to apply. | [optional] |
| **isFavorite** | **Boolean**| Optional filter by items that are marked as favorite, or not. userId is required. | [optional] |
| **enableUserData** | **Boolean**| Optional, include user data. | [optional] |
| **imageTypeLimit** | **Integer**| Optional, the max number of images to return, per image type. | [optional] |
| **enableImageTypes** | [**List&lt;ImageType&gt;**](ImageType.md)| Optional. The image types to include in the output. | [optional] |
| **excludePersonTypes** | [**List&lt;String&gt;**](String.md)| Optional. If specified results will be filtered to exclude those containing the specified PersonType. Allows multiple, comma-delimited. | [optional] |
| **personTypes** | [**List&lt;String&gt;**](String.md)| Optional. If specified results will be filtered to include only those containing the specified PersonType. Allows multiple, comma-delimited. | [optional] |
| **appearsInItemId** | **UUID**| Optional. If specified, person results will be filtered on items related to said persons. | [optional] |
| **userId** | **UUID**| User id. | [optional] |
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
| **200** | Persons returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

